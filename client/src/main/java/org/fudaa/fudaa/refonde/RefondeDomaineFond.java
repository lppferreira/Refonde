/*
 * @file         RefondeDomaineFond.java
 * @creation     2000-03-30
 * @modification $Date: 2007-01-19 13:14:15 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.dodico.corba.dunes.ICalculDunes;
import org.fudaa.dodico.corba.dunes.IParametresDunes;
import org.fudaa.dodico.corba.dunes.IParametresDunesHelper;
import org.fudaa.dodico.corba.dunes.IResultatsDunes;
import org.fudaa.dodico.corba.dunes.IResultatsDunesHelper;
import org.fudaa.dodico.corba.geometrie.LTypeElement;
import org.fudaa.dodico.corba.geometrie.SPoint;
import org.fudaa.dodico.corba.geometrie.SPolyligne;
import org.fudaa.dodico.corba.geometrie.SRegion;
import org.fudaa.dodico.corba.geometrie.STrou;

import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;

import org.fudaa.fudaa.commun.conversion.FudaaInterpolateurMaillage;
import org.fudaa.fudaa.commun.conversion.FudaaMaillageElement;
import org.fudaa.fudaa.commun.conversion.FudaaPoint;
import org.fudaa.fudaa.commun.conversion.FudaaPolyligne;
/**
 * Domaine géometrique de type fond.
 *
 * @version      $Id: RefondeDomaineFond.java,v 1.11 2007-01-19 13:14:15 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDomaineFond extends RefondeDomaine implements Cloneable {
  //  public static ICalculDunes SERVEUR_DUNES=null; // A initialiser par l'application
  public static final int LONGUEUR_ONDE= 0;
  public static final int CLASSIQUE= 1;
  protected int nbNoeudsOnde_= 1;
  protected double periodeHoule_= 1;
  //  protected double hauteurMer_=0;
  protected double aireMaxi_= 0;
  protected int typeMaillage_= CLASSIQUE;
  /**
   * Création d'un domaine vide
   */
  public RefondeDomaineFond() {
    super();
  }
  /**
   * Création d'un domaine de type fond
   */
  public RefondeDomaineFond(RefondeContour[] _cntrs) {
    super(_cntrs);
    triContours(_cntrs);
    aireMaxi_= aireMaxiDefaut();
  }
  /**
   * Nombre de noeuds par longueur d'onde
   */
  public void setNbNoeudsOnde(int _nbNoeuds) {
    nbNoeudsOnde_= _nbNoeuds;
  }
  public int getNbNoeudsOnde() {
    return nbNoeudsOnde_;
  }
  /**
   * Période de la houle
   */
  public void setPeriodeHoule(double _periode) {
    periodeHoule_= _periode;
  }
  public double getPeriodeHoule() {
    return periodeHoule_;
  }
  /**
   * Aire maxi pour les éléments
   */
  public void setAireMaxi(double _aire) {
    aireMaxi_= _aire;
  }
  public double getAireMaxi() {
    return aireMaxi_;
  }
  /**
   * Type du maillage (CLASSIQUE, LONGUEUR_ONDE)
   */
  public void setTypeMaillage(int _type) {
    typeMaillage_= _type;
  }
  public int getTypeMaillage() {
    return typeMaillage_;
  }
  /**
   * Affectation des contours. Le contour exterieur est toujours mis en premier.
   */
  public void setContours(RefondeContour[] _cntrs) {
    super.setContours(_cntrs);
    triContours(_cntrs);
  }
  /**
   * Mailler le domaine
   */
  public void mailler() {
    // Pour éviter les effets de bord tels que noeuds sur le contour, etc.
    setMaillage(null);
    // Recherche de noeuds sur une des polylignes d'un des contours du domaine
    boolean premail= false;
    BOUCLE_CTS : for (int i= 0; i < contours_.length; i++) {
      RefondePolyligne[] pls= contours_[i].getPolylignes();
      for (int j= 0; j < pls.length; j++)
        if (premail= pls[j].hasNoeuds())
          break BOUCLE_CTS;
    }
    if (premail)
      maillerContraint();
    else
      maillerLibre();
  }
  /*
   * Mailler librement
   */
  private void maillerLibre() {
    GrMaillageElement ml;
    GrNoeud[] nds;
    FudaaInterpolateurMaillage it;
    RefondeImplementation.statusBar.setProgression(20);
    // Maillage 2D en contours libres
    ml= maillerDomaine(true);
    if (ml == null)
      return;
    RefondeImplementation.statusBar.setProgression(80);
    // Interpolation de la cote Z des noeuds du maillage
    nds= ml.noeuds();
    it= RefondeImplementation.projet.getGeometrie().interpolateur();
    for (int i= 0; i < nds.length; i++)
      nds[i].point_= it.interpolePoint(nds[i].point_);
    RefondeImplementation.statusBar.setProgression(90);
    setMaillage(ml);
    initData();
    return;
  }
  /*
   * Mailler contraint
   */
  private void maillerContraint() {
    GrMaillageElement mlLibre;
    GrMaillageElement mlCont;
    GrNoeud[] ndsMlCont;
    GrElement[] elsMlCont;
    GrNoeud[] ndsCtCont;
    GrNoeud[] ndsPlLibre;
    GrNoeud[][][] nds;
    //    GrNoeud[] ndsPl;
    RefondeContour[] cts;
    RefondeDomaineFond dmTrans= null;
    FudaaInterpolateurMaillage it;
    Hashtable nd2Ind;
    RefondeImplementation.statusBar.setProgression(20);
    // Présence de noeuds sur une ligne d'un des contours du domaine : Dans ce
    // cas, il faut remailler en contours contraints (sans ajout de noeuds sur
    // les contours), après création de contours temporaires correspondants aux
    // positions des noeuds sur les contours comportant des noeuds provenant des
    // domaines jointifs, et sur les contours prémaillés pour ce domaine.
    nds= new GrNoeud[contours_.length][][];
    // Maillage 2D en contours libres
    mlLibre= maillerDomaine(true);
    if (mlLibre == null)
      return;
    RefondeImplementation.statusBar.setProgression(50);
    //--------------------------------------------------------------------------
    //---  Création d'un maillage contraint  -----------------------------------
    //--------------------------------------------------------------------------
    // Recupération des noeuds sur les polylignes déjà maillées des contours
    for (int i= 0; i < contours_.length; i++) {
      RefondePolyligne[] plsCt= contours_[i].getPolylignes();
      nds[i]= new GrNoeud[plsCt.length][];
      for (int j= 0; j < plsCt.length; j++)
        nds[i][j]= plsCt[j].getNoeuds();
    }
    RefondeImplementation.statusBar.setProgression(55);
    // Polylignes ne comportant pas déjà des noeuds => Récupération des noeuds
    // du maillage libre
    setMaillage(mlLibre);
    for (int i= 0; i < contours_.length; i++) {
      RefondePolyligne[] plsCt= contours_[i].getPolylignes();
      for (int j= 0; j < plsCt.length; j++)
        if (nds[i][j] == null)
          nds[i][j]= plsCt[j].getNoeuds();
    }
    setMaillage(null);
    // Création des contours contraints
    cts= new RefondeContour[contours_.length];
    for (int i= 0; i < cts.length; i++)
      cts[i]= creeContour(nds[i]);
    // Maillage d'un domaine transposé depuis les noeuds de contours
    // avec contours contraints
    try {
      dmTrans= (RefondeDomaineFond)clone();
    } catch (CloneNotSupportedException _exc) {}
    RefondeImplementation.statusBar.setProgression(60);
    dmTrans.setContours(cts);
    mlCont= dmTrans.maillerDomaine(false);
    if (mlCont == null)
      return;
    dmTrans.setMaillage(mlCont);
    RefondeImplementation.statusBar.setProgression(80);
    //--------------------------------------------------------------------------
    //---  Interpolation de la cote Z des noeuds du maillage contraint  --------
    //--------------------------------------------------------------------------
    ndsMlCont= mlCont.noeuds();
    it= RefondeImplementation.projet.getGeometrie().interpolateur();
    for (int i= 0; i < ndsMlCont.length; i++)
      ndsMlCont[i].point_= it.interpolePoint(ndsMlCont[i].point_);
    RefondeImplementation.statusBar.setProgression(85);
    //--------------------------------------------------------------------------
    //---  Collage des noeuds du maillage contraint (remplacement dans le  -----
    //---  maillage contraint des noeuds de bord par les anciens noeuds)  ------
    //--------------------------------------------------------------------------
    nd2Ind= new Hashtable(ndsMlCont.length);
    for (int i= 0; i < ndsMlCont.length; i++)
      nd2Ind.put(ndsMlCont[i], new Integer(i));
    for (int i= 0; i < contours_.length; i++) {
      RefondePolyligne[] plsCtLibre= contours_[i].getPolylignes();
      for (int j= 0; j < plsCtLibre.length; j++) {
        if ((ndsPlLibre= plsCtLibre[j].getNoeuds()) != null) {
          ndsCtCont= cts[i].getNoeuds();
          GrNoeud nd1= ndsPlLibre[0];
          boolean findNoeud= false;
          int decal;
          for (decal= 0; decal < ndsCtCont.length; decal++)
            if (findNoeud= (nd1.distance(ndsCtCont[decal]) < 1.e-4))
              break;
          if (!findNoeud)
            throw new IllegalArgumentException(
              "Erreur lors de l'opération de collage : Pas "
                + "de correspondance de noeuds");
          int sens= -1;
          if (ndsPlLibre[1].distance(ndsCtCont[(decal + 1) % ndsCtCont.length])
            < 1.e-4)
            sens= 1;
          for (int k= 0; k < ndsPlLibre.length; k++) {
            nd1=
              ndsCtCont[(decal + k * sens + ndsCtCont.length)
                % ndsCtCont.length];
            ndsMlCont[((Integer)nd2Ind.get(nd1)).intValue()]= ndsPlLibre[k];
          }
        }
      }
    }
    dmTrans.setMaillage(null);
    RefondeImplementation.statusBar.setProgression(90);
    // Remplacement dans les elements des noeuds
    elsMlCont= mlCont.elements();
    for (int i= 0; i < elsMlCont.length; i++) {
      GrNoeud[] ndsEle= elsMlCont[i].noeuds_;
      for (int j= 0; j < ndsEle.length; j++)
        ndsEle[j]= ndsMlCont[((Integer)nd2Ind.get(ndsEle[j])).intValue()];
    }
    // Nouveau maillage pour le domaine
    setMaillage(new GrMaillageElement(elsMlCont, ndsMlCont));
    initData();
    return;
  }
  /*
   * Maillage du domaine avec ou sans contrainte d'ajout de noeuds sur les
   * contours
   */
  private GrMaillageElement maillerDomaine(boolean _contoursLibres) {
    int nbLignes;
    //    GrPoint[]     pts;
    SPolyligne[] sPlsCntr;
    SPolyligne[] sPls;
    STrou[] sTrous;
    int nbTrous;
    int ncext= 0;
    int nbCntr;
    IParametresDunes duPar;
    ICalculDunes duCal;
    IResultatsDunes duRes;
    nbCntr= contours_.length;
    nbLignes= 0;
    for (int i= 0; i < nbCntr; i++)
      nbLignes += contours_[i].getPolylignes().length;
    //-------  Remplissage des parametres pour dunes  --------------------------
    //    _op.setMessage("Maillage du domaine...",0);
    System.out.println("Maillage du domaine...");
    //    duCal=SERVEUR_DUNES;
    duCal= RefondeImplementation.SERVEUR_DUNES;
    duPar=IParametresDunesHelper.narrow(duCal.parametres(RefondeImplementation.CONNEXION_DUNES));
    sPls= new SPolyligne[nbLignes];
    sTrous= new STrou[nbCntr - 1];
    nbCntr= 0;
    nbTrous= 0;
    nbLignes= 0;
    for (int i= 0; i < contours_.length; i++) {
      SPoint sPt= null;
      RefondePolyligne[] pls= contours_[i].getPolylignes();
      sPlsCntr= new SPolyligne[pls.length];
      for (int j= 0; j < pls.length; j++) {
        sPlsCntr[j]= FudaaPolyligne.gr2S(pls[j]);
        sPlsCntr[j].points[0]= sPt;
        sPt= sPlsCntr[j].points[sPlsCntr[j].points.length - 1];
      }
      sPlsCntr[0].points[0]= sPt;
      if (ncext != nbCntr)
        sTrous[nbTrous++]=
          new STrou(FudaaPoint.gr2S(contours_[i].getPointInterne()));
      System.arraycopy(sPlsCntr, 0, sPls, nbLignes, sPlsCntr.length);
      nbLignes += sPlsCntr.length;
      nbCntr++;
    }
    //-------  Maillage 2D  ----------------------------------------------------
    // Maillage 2D par longueur d'onde
    // La prise en compte du maillage par longueur d'onde oblige a passer
    // au mailleur les points de topo qui lui permettent de densifier suivant le
    // gradient. Algoritmiquement parlant, ces points entrent dans la
    // constitution du maillage comme points de passage obligés. Une trop grande
    // densification de ces points entrainera donc forcement une densification
    // du maillage, même si la pente est nulle suivant ces points (!!?).
    if (typeMaillage_ == LONGUEUR_ONDE) {
      // On remplace les points de la triangulation par les points de topo pour
      // empecher d'avoir des points a meme coordonnees x,y => Pb de noeuds double
      // en sortie du mailleur
      //      GrMaillageElement triang=getScene().getTriangulation();
      //      GrNoeud[]         nds=triang.noeuds();
      Vector vptsTopo= RefondeImplementation.projet.getGeometrie().pointsTopo;
      GrPoint[] ptsTopo;
      SPoint[] sPts;
      double hauteurMer;
      hauteurMer= RefondeImplementation.projet.getModeleCalcul().hauteurMer();
      // Controle que la bathymétrie en chaque point+hauteur de mer est bien
      // positive
      ptsTopo= new GrPoint[vptsTopo.size()];
      vptsTopo.toArray(ptsTopo);
      for (int i= 0; i < ptsTopo.length; i++)
        if (ptsTopo[i].z_ + hauteurMer <= 0)          //      for (int i=0; i<nds.length; i++)
          //       if (nds[i].point.z+hauteurMer<=0)
          throw new IllegalArgumentException(
            "La hauteur de mer doit être " + "supérieure à la bathymétrie");
      // Controle que la période > 0
      if (periodeHoule_ <= 0)
        throw new IllegalArgumentException("La période de houle doit être > 0.");
      // controle que le nombre de noeuds > 0
      if (nbNoeudsOnde_ <= 0)
        throw new IllegalArgumentException("Le nombre de noeuds par longueur d'onde doit être > 0.");
      sPts= new SPoint[ptsTopo.length];
      for (int i= 0; i < ptsTopo.length; i++)
        sPts[i]= FudaaPoint.gr2S(ptsTopo[i]);
      //      sPts=new SPoint[nds.length];
      //      for (int i=0; i<nds.length; i++) sPts[i]=FudaaPoint.gr2S(nds[i].point);
      duPar.points(sPts);
      duPar.polylignes(sPls);
      duPar.regions(new SRegion[0]);
      duPar.trous(sTrous);
      duCal.nombrePointsLongueurOnde(nbNoeudsOnde_);
      duCal.periodeHoule(periodeHoule_);
      duCal.hauteurMer(hauteurMer);
    }
    // Maillage classique
    else {
      duPar.points(new SPoint[0]);
      duPar.polylignes(sPls);
      duPar.regions(new SRegion[0]);
      duPar.trous(sTrous);
    }
    duCal.optionC(false);
    duCal.optionQ(true);
    duCal.optionA(aireMaxi_ != 0);
    duCal.aireMax(aireMaxi_);
    duCal.optionO(typeMaillage_ == LONGUEUR_ONDE);
    duCal.optionY(!_contoursLibres);
    duCal.typeElementDunes(LTypeElement.T3);
    //    _op.setMessage("Maillage 2D...",30);
    System.out.println("Maillage 2D...");
    duCal.calcul(RefondeImplementation.CONNEXION_DUNES);
    if (!duCal.estOK())
      return null;
    //    _op.setMessage("Maillage 2D...",45);
    duRes=
      IResultatsDunesHelper.narrow(duCal.resultats(RefondeImplementation.CONNEXION_DUNES));
    return FudaaMaillageElement.s2Gr(duRes.maillage());
  }
  /*
   * Création d'un contour a partir de noeuds ordonnés par polyligne
   */
  private RefondeContour creeContour(GrNoeud[][] _nds) {
    Vector vpls= new Vector();
    RefondePolyligne[] pls;
    RefondePolyligne pl;
    GrPoint pt;
    boolean sensOK;
    pt= new GrPoint();
    pl= new RefondePolyligne();
    pl.sommets_.ajoute(pt);
    // Pour chaque ensemble de noeuds de polyligne
    for (int i= 0; i < _nds.length; i++) {
      sensOK=
        (i == 0
          && (_nds[i][_nds[i].length - 1].distanceXY(_nds[i + 1][0]) < 1.e-3
            || _nds[i][_nds[i].length
              - 1].distanceXY(_nds[i + 1][_nds[i + 1].length - 1])
              < 1.e-3))
          || (i != 0
            && (_nds[i][0].distanceXY(_nds[i - 1][0]) < 1.e-3
              || _nds[i][0].distanceXY(_nds[i - 1][_nds[i - 1].length - 1])
                < 1.e-3));
      for (int j= 0; j < _nds[i].length - 1; j++) {
        GrPoint ptAr;
        if (sensOK)
          ptAr= _nds[i][j].point_;
        else
          ptAr= _nds[i][_nds[i].length - i - j].point_;
        pt= new GrPoint(ptAr.x_, ptAr.y_, ptAr.z_);
        pl.sommets_.ajoute(pt);
        vpls.add(pl);
        pl= new RefondePolyligne();
        pl.sommets_.ajoute(pt);
      }
    }
    ((RefondePolyligne)vpls.get(0)).sommets_.remplace(pt, 0);
    pls= new RefondePolyligne[vpls.size()];
    vpls.toArray(pls);
    return new RefondeContour(pls);
  }
  /*
   * Initialisation des data des noeuds/éléments
   */
  private void initData() {
    GrNoeud[] nds= mail_.noeuds();
    for (int i= 0; i < nds.length; i++)
      if (nds[i].data() == null)
        nds[i].data(new RefondeNoeudData());
    GrElement[] els= mail_.elements();
    for (int i= 0; i < els.length; i++)
      if (els[i].data() == null)
        els[i].data(new RefondeElementData());
  }
  /*
   * Retourne une aire maxi pour les éléments par défaut correspondant à
   * environ 1000 éléments sur le domaine
   */
  private double aireMaxiDefaut() {
    GrBoite bt= boite();
    return (int) ((bt.e_.y_ - bt.o_.y_) * (bt.e_.x_ - bt.o_.x_) / 10.) / 100.;
  }
}
