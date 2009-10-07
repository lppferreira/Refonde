/*
 * @file         RefondeMaillage.java
 * @creation     1999-07-01
 * @modification $Date: 2007-01-19 13:14:15 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Polygon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;

import org.fudaa.fudaa.commun.conversion.FudaaMaillageCORELEBTH;
/**
 * Classe pour la gestion du maillage du projet.
 *
 * @version      $Id: RefondeMaillage.java,v 1.12 2007-01-19 13:14:15 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeMaillage {
  // Pour la clareté du code
  //  private static final int EOF   =StreamTokenizer.TT_EOF;
  //  private static final int EOL   =StreamTokenizer.TT_EOL;
  //  private static final int NUMBER=StreamTokenizer.TT_NUMBER;
  private static final int WORD= StreamTokenizer.TT_WORD;
  /**
   * Cache pour le super maillage : On considère que si les maillages n'ont pas
   * été modifiés, le super maillage est toujours d'actualité
   */
  private static GrMaillageElement[] mails_= null;
  private static GrMaillageElement superMail_= null;
  public boolean modifie;
  /**
   * Création d'un modele de maillage par défaut depuis le projet
   */
  public static RefondeMaillage defaut(RefondeProjet _projet) {
    RefondeMaillage mai= new RefondeMaillage();
    mai.modifie= true;
    return mai;
  }
  /**
   * Création d'un super maillage à partir des éléments et noeuds de la géométrie
   * Les noeuds et les éléments restent communs, seuls les tables de
   * stockage sont modifiées.
   *
   * Si le maillage est toujours le même, on retourne le maillage dans le cache.
   */
  public static GrMaillageElement creeSuperMaillage(RefondeProjet _projet) {
    GrMaillageElement[] mls= getMaillages(_projet);
    boolean okSuperMail= true;
    if (mails_ == null || mls.length != mails_.length)
      okSuperMail= false;
    else
      for (int i= 0; i < mls.length; i++)
        if (mls[i] != mails_[i])
          okSuperMail= false;
    if (!okSuperMail) {
      mails_= mls;
      superMail_= creeSuperMaillage(mails_);
    }
    return superMail_;
  }
  /**
   * Création d'un super maillage à partir des éléments et noeuds de plusieurs
   * maillages. Les noeuds et les éléments restent communs, seuls les tables de
   * stockage sont modifiées.
   *
   * @return Le super maillage. Celui ci peut ne contenir aucun noeud ni élément
   *         s'il n'existe aucun maillage.
   */
  public static GrMaillageElement creeSuperMaillage(GrMaillageElement[] _mails) {
    //fred a voir si c'est inutile !
    if(_mails!=null && _mails.length==1) {
      //System.err.println("inutile ??");
      return _mails[0];
      }
    HashSet<GrNoeud> hnds= new HashSet<GrNoeud>();
    HashSet<GrElement> hels= new HashSet<GrElement>();
    GrNoeud[] nds;
    GrElement[] els;
    for (int i= 0; i < _mails.length; i++) {
      hnds.addAll(Arrays.asList(_mails[i].noeuds()));
      hels.addAll(Arrays.asList(_mails[i].elements()));
      //fred : ajout d'une methode pour optimiser ce passer
//      _mails[i].ajouteTousLesElements(hels);
//      _mails[i].ajouteTousLesNoeuds(hnds);
    }
    nds= new GrNoeud[hnds.size()];
    hnds.toArray(nds);
    els= new GrElement[hels.size()];
    hels.toArray(els);
    return new GrMaillageElement(els, nds);
  }
  /**
   * Retourne les maillages des domaines pour le projet donné sous forme de
   * tableau.
   *
   * @return Si aucun maillage n'existe, la valeur retournée est un tableau
   *         de longueur 0.
   */
  public static GrMaillageElement[] getMaillages(RefondeProjet _projet) {
    GrMaillageElement[] mls;
    Vector vdms= _projet.getGeometrie().scene_.getDomaines();
    Vector vmls= new Vector();
    GrMaillageElement ml;
    for (int i= 0; i < vdms.size(); i++) {
      if ((ml= ((RefondeDomaine)vdms.get(i)).getMaillage()) != null)
        vmls.add(ml);
    }
    mls= new GrMaillageElement[vmls.size()];
    vmls.toArray(mls);
    return mls;
  }
  /**
   * @param _projet le projet contenant les maillages
   * @param _sauf le domaine a ne pas prendre en compte
   * @return tableau de taille 0 si aucun maillage
   */
  public static GrMaillageElement[] getMaillagesSauf(RefondeProjet _projet,RefondeDomaine _sauf) {
    GrMaillageElement[] mls;
    Vector vdms= _projet.getGeometrie().scene_.getDomaines();
    List vmls= new ArrayList();
    //GrMaillageElement ml;
    int nb=vdms.size();
    for (int i= 0; i < nb; i++) {
      RefondeDomaine d=(RefondeDomaine)vdms.get(i);
      if( (d!=_sauf) && d.hasMaillage())
        vmls.add(d.getMaillage());
    }
    mls= new GrMaillageElement[vmls.size()];
    vmls.toArray(mls);
    return mls;
  }


  /*
   * Controle de l'optimisation du maillage
   */
  public static boolean estOptimise(GrMaillageElement _ml) {
    GrElement[] els= _ml.elements();
    HashSet num2El= new HashSet(els.length);
    for (int i= 0; i < els.length; i++) {
      int num= ((RefondeElementData)els[i].data()).numero;
      if (num > els.length)
        return false;
      num2El.add(new Integer(num));
    }
    if (num2El.size() != els.length)
      return false;
    GrNoeud[] nds= _ml.noeuds();
    HashSet num2Nd= new HashSet(nds.length);
    for (int i= 0; i < nds.length; i++) {
      int num= ((RefondeNoeudData)nds[i].data()).numero;
      if (num > nds.length)
        return false;
      num2Nd.add(new Integer(num));
    }
    if (num2Nd.size() != nds.length)
      return false;
    return true;
  }
  /**
   * Lecture d'un maillage depuis les fichiers noeuds, bathy et éléments de
   * maillage format preflux. Dans ce cas, attaché au seul domaine fond
   * @param _fichier Nom du fichier maillage. Le maillage est contenu
   *                 dans les _fichier.cor, _fichier.bth et _fichier.ele
   * @exception FileNotFoundException Un fichier de maillage n'est pas trouvé
   * @exception IOException Une erreur de lecture s'est produite
   * @return L'objet maillage
   */
  public static RefondeMaillage ouvrirPreflux(
    RefondeProjet _projet,
    File _fichier)
    throws IOException {
    RefondeMaillage mai= new RefondeMaillage();
    GrMaillageElement ml;
    //    Vector vdms=_projet.getGeometrie().getDomaines();
    GrElement[] els;
    GrNoeud[] nds;
    try {
      ml= FudaaMaillageCORELEBTH.lire(_fichier);
    } catch (FileNotFoundException _exc) {
      throw new FileNotFoundException(
        "Erreur d'ouverture de " + _exc.getMessage());
    } catch (IOException _exc) {
      throw new IOException("Erreur de lecture sur " + _exc.getMessage());
    }
    // Rajout des numéro de noeuds optimisés. Les noeuds sont décrits dans les
    // fichiers suivant un ordre correspondant à l'optimisation
    nds= ml.noeuds();
    for (int i= 0; i < nds.length; i++) {
      RefondeNoeudData dt= new RefondeNoeudData();
      dt.numero= i + 1;
      nds[i].data(dt);
    }
    // Rajout des numéro d'éléments optimisés. Les éléments sont décrits dans les
    // fichiers suivant un ordre correspondant à l'optimisation
    els= ml.elements();
    for (int i= 0; i < els.length; i++) {
      RefondeElementData dt= new RefondeElementData();
      dt.numero= i + 1;
      els[i].data(dt);
    }
    // Affectation du maillage au seul domaine fond
    //    for (int i=0; i<vdms.size(); i++) {
    //      if (vdms.get(i) instanceof RefondeDomaineFond) {
    //        ((RefondeDomaineFond)vdms.get(i)).setMaillage(ml);
    //        break;
    //      }
    //    }
    mai.decoupeMaillageGlobal(_projet, ml);
    mai.modifie= false;
    return mai;
  }
  /**
   * Enregistrement d'un maillage sur les fichiers .cor/.ele/.bth au format
   * Preflux
   * @param _fichier Nom du fichier maillage. Le maillage est contenu
   *                 dans les _fichier.cor, _fichier.bth et _fichier.ele
   * @exception FileNotFoundException Un fichier de maillage n'est pas trouvé
   * @exception IOException Une erreur de lecture s'est produite
   */
  public void enregistrerPreflux(RefondeProjet _projet, File _fichier)
    throws IOException {
    GrMaillageElement ml= creeSuperMaillage(getMaillages(_projet));
    GrElement[] els= ml.elements();
    GrNoeud[] nds= ml.noeuds();
    GrElement[] elsOpt;
    GrNoeud[] ndsOpt;
    if (ml.getNombre() == 0)
      throw new IOException("Pas de maillage");
    if (!estOptimise(ml))
      throw new IOException("Vous ne pouvez enregistrer un maillage non optimisé");
    // Ordonnancement des noeuds et éléments du super maillage suivant leur
    // numéro optimisé
    elsOpt= new GrElement[els.length];
    for (int i= 0; i < els.length; i++) {
      int ordre= ((RefondeElementData)els[i].data()).numero - 1;
      elsOpt[ordre]= els[i];
    }
    ndsOpt= new GrNoeud[nds.length];
    for (int i= 0; i < nds.length; i++) {
      int ordre= ((RefondeNoeudData)nds[i].data()).numero - 1;
      ndsOpt[ordre]= nds[i];
    }
    ml= new GrMaillageElement(elsOpt, ndsOpt);
    try {
      FudaaMaillageCORELEBTH.enregistrer(ml, _fichier, 10);
    } catch (FileNotFoundException _exc) {
      throw new FileNotFoundException(
        "Erreur d'ouverture de " + _exc.getMessage());
    } catch (IOException _exc) {
      throw new IOException("Erreur d'ecriture sur " + _exc.getMessage());
    }
    modifie= false;
  }
  /**
   * Lecture d'un maillage.
   * @param _fc Nom du fichier de maillage.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public static RefondeMaillage ouvrir(RefondeProjet _projet, File _fc)
    throws IOException, RefondeTacheInterruptionException {
    RefondeMaillage mai= new RefondeMaillage();
    StreamTokenizer file= null;
    Reader rf= null;
    String version= "??";
    // Entête et numéro de version
    try {
      // Ouverture du fichier
      file= new StreamTokenizer(rf= new FileReader(_fc));
      file.lowerCaseMode(true);
      file.wordChars('<', '<');
      file.wordChars('>', '>');
      file.wordChars('_', '_');
      // Inhibition du mode parseNumber instauré par défaut. Il ne sait pas
      // traiter des float ou double avec exposant. Le traitement se fera donc
      // dans la classe courante, et non dans StreamTokenizer
      file.ordinaryChars('0', '9');
      file.ordinaryChar('.');
      file.ordinaryChar('-');
      file.wordChars('0', '9');
      file.wordChars('.', '.');
      file.wordChars('-', '-');
      file.whitespaceChars(';', ';');
      // Entète du fichier et numéro de version
      if (file.nextToken() != WORD
        || (!file.sval.equals("refonde") && !file.sval.equals("prefonde")))
        throw new RefondeIOException("Le fichier n'est pas un fichier Refonde");
      if (file.nextToken() != WORD
        || (version= file.sval).compareTo("5.01") < 0)
        throw new RefondeIOException(
          "Le format du fichier est de version "
            + version
            + ".\nSeules les versions > à 5.01 sont autorisées");
      if (file.nextToken() != WORD || !file.sval.equals("modele_maillage"))
        throw new RefondeIOException("Le fichier n'est pas un fichier maillage");
      // Lecture par version
      mai.lire(_projet, file);
    } catch (NumberFormatException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fc + " ligne " + file.lineno());
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + _fc
          + " ligne "
          + file.lineno()
          + "\n"
          + _exc.getMessage());
    } catch (FileNotFoundException _exc) {
      throw new IOException("Erreur d'ouverture de " + _fc);
    } catch (IOException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fc + " ligne " + file.lineno());
    }
    //    catch (RefondeTacheInterruptionException _exc) {
    //      throw new RefondeTacheInterruptionException(_exc.getMessage());
    //    }
    //    catch (Exception _exc) {
    //      throw new IOException("Erreur de lecture sur "+_fc+" ligne "+file.lineno()+
    //                            "\n"+_exc.getMessage());
    //    }
    finally {
      if (rf != null)
        rf.close();
    }
    mai.modifie= false;
    return mai;
  }
  /**
   * Lecture des informations sur version courante
   */
  private void lire(RefondeProjet _projet, StreamTokenizer _file)
    throws IOException, RefondeIOException, RefondeTacheInterruptionException {
    int nbNds;
    int nbEls;
    int nbPrs;
    Vector vnds;
    Vector[] vels; // Eléments par domaines
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vdms= geo.getDomaines();
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    //---  Noeuds  -------------------------------------------------------------
    // Nombre de noeuds
    if (_file.nextToken() != WORD || !_file.sval.equals("<noeuds>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbNds= Integer.parseInt(_file.sval);
    // Noeuds
    vnds= new Vector(nbNds);
    for (int i= 0; i < nbNds; i++) {
      GrNoeud nd;
      RefondeTacheOperation.notifieArretDemande();
      //... Numéro optimisé
      RefondeNoeudData dt= new RefondeNoeudData();
      if (_file.nextToken() != WORD)
        throw new IOException();
      dt.numero= Integer.parseInt(_file.sval);
      //... Coordonnées
      GrPoint pt= new GrPoint();
      if (_file.nextToken() != WORD)
        throw new IOException();
      pt.x_= Double.parseDouble(_file.sval);
      if (_file.nextToken() != WORD)
        throw new IOException();
      pt.y_= Double.parseDouble(_file.sval);
      if (_file.nextToken() != WORD)
        throw new IOException();
      pt.z_= Double.parseDouble(_file.sval);
      nd= new GrNoeud(pt);
      nd.data(dt);
      vnds.add(nd);
    }
    //---  Eléments  -----------------------------------------------------------
    vels= new Vector[vdms.size()];
    for (int i= 0; i < vdms.size(); i++)
      vels[i]= new Vector();
    // Nombre d'éléments
    if (_file.nextToken() != WORD || !_file.sval.equals("<elements>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbEls= Integer.parseInt(_file.sval);
    // Eléments
    for (int i= 0; i < nbEls; i++) {
      GrElement el;
      GrNoeud[] ndsEl;
      int type;
      int numDm;
      RefondeElementData dt;
      RefondeTacheOperation.notifieArretDemande();
      //... Numéro optimisé
      dt= new RefondeElementData();
      if (_file.nextToken() != WORD)
        throw new IOException();
      dt.numero= Integer.parseInt(_file.sval);
      //... Type
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("l2")) {
        type= GrElement.L2;
        ndsEl= new GrNoeud[2];
      } else if (_file.sval.equals("l3")) {
        type= GrElement.L3;
        ndsEl= new GrNoeud[3];
      } else if (_file.sval.equals("q4")) {
        type= GrElement.Q4;
        ndsEl= new GrNoeud[4];
      } else if (_file.sval.equals("q8")) {
        type= GrElement.Q8;
        ndsEl= new GrNoeud[8];
      } else if (_file.sval.equals("t3")) {
        type= GrElement.T3;
        ndsEl= new GrNoeud[3];
      } else if (_file.sval.equals("t6")) {
        type= GrElement.T6;
        ndsEl= new GrNoeud[6];
      } else
        throw new IOException();
      //... Domaine
      if (_file.nextToken() != WORD)
        throw new IOException();
      numDm= Integer.parseInt(_file.sval);
      //... Noeuds
      for (int j= 0; j < ndsEl.length; j++) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        ndsEl[j]= (GrNoeud)vnds.get(Integer.parseInt(_file.sval));
      }
      el= new GrElement(ndsEl, type);
      el.data(dt);
      vels[numDm].add(el);
    }
    // Affectation des maillages aux domaines
    for (int i= 0; i < vdms.size(); i++) {
      GrElement[] els;
      RefondeTacheOperation.notifieArretDemande();
      if (vels[i].size() == 0)
        continue;
      els= new GrElement[vels[i].size()];
      vels[i].toArray(els);
      ((RefondeDomaine)vdms.get(i)).setMaillage(new GrMaillageElement(els));
    }
    //---  Propriétés de maillage sur les domaines  ----------------------------
    // Nombre de propriétés de maillage
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<proprietes_maillage>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbPrs= Integer.parseInt(_file.sval);
    // Propriétés de maillage
    for (int i= 0; i < nbPrs; i++) {
      RefondeTacheOperation.notifieArretDemande();
      // Domaine fond
      if (vdms.get(i) instanceof RefondeDomaineFond) {
        RefondeDomaineFond df= (RefondeDomaineFond)vdms.get(i);
        //... Type de maillage
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("classique")) {
          df.setTypeMaillage(RefondeDomaineFond.CLASSIQUE);
          //... Aire maxi des éléments
          if (_file.nextToken() != WORD)
            throw new IOException();
          df.setAireMaxi(Double.parseDouble(_file.sval));
        } else if (_file.sval.equals("longueur_onde")) {
          df.setTypeMaillage(RefondeDomaineFond.LONGUEUR_ONDE);
          //... Nombre de noeuds par longueur d'onde
          if (_file.nextToken() != WORD)
            throw new IOException();
          df.setNbNoeudsOnde(Integer.parseInt(_file.sval));
          //... Période de houle
          if (_file.nextToken() != WORD)
            throw new IOException();
          df.setPeriodeHoule(Double.parseDouble(_file.sval));
        } else
          throw new IOException();
      }
      // Domaine digue
      else {
        RefondeDomaineDigue dd= (RefondeDomaineDigue)vdms.get(i);
        //... Type de maillage
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("ouvrage")) {
          dd.setTypeMaillage(RefondeDomaineDigue.OUVRAGE);
          //... Nombre de troncons
          if (_file.nextToken() != WORD)
            throw new IOException();
          int[] nbTcs= new int[Integer.parseInt(_file.sval)];
          //... Nombre d'éléments par troncon
          for (int j= 0; j < nbTcs.length; j++) {
            if (_file.nextToken() != WORD)
              throw new IOException();
            nbTcs[j]= Integer.parseInt(_file.sval);
          }
          dd.setNbElements(nbTcs);
        } else
          throw new RefondeIOException("Type de maillage non prévu");
      }
    }
  }
  /**
   * Enregistrement d'un maillage et des propriétés de maillage de la géométrie
   * @param _fc Nom du fichier de maillage.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public void enregistrer(RefondeProjet _projet, File _fc) throws IOException {
    ecrire(_projet, _fc);
    modifie= false;
  }
  /**
   * Ecriture des informations sur le fichier associé
   */
  private void ecrire(RefondeProjet _projet, File _fc) throws IOException {
    RefondeGeometrie geo= _projet.getGeometrie();
    //    Vector vpls=geo.getPolylignes();
    //    Vector vcts=geo.getContours();
    Vector vdms= geo.getDomaines();
    //   Vector vpts=geo.getPoints();
    GrMaillageElement[] mls= getMaillages(_projet);
    GrMaillageElement ml= creeSuperMaillage(mls);
    GrNoeud[] nds= ml.noeuds();
    GrNoeud[] ndsEl;
    GrElement[] els;
    RefondeDomaine dm;
    // Correspondance objet->numéro
    Hashtable hnds;
    //... Noeuds
    hnds= new Hashtable(nds.length);
    for (int i= 0; i < nds.length; i++)
      hnds.put(nds[i], new Integer(i));
    PrintWriter file= null;
    try {
      // Ouverture du fichier
      file= new PrintWriter(new FileWriter(_fc));
      // Entète du fichier
      file.print("refonde ; ");
      file.print(RefondeImplementation.informationsSoftware().version + " ; ");
      file.print("modele_maillage");
      file.println();
      file.println();
      // Identifiant de géométrie
      file.println("<identifiant_geometrie> ; " + geo.identifiant());
      file.println();
      // Nombre de noeuds
      file.println("<noeuds> ; " + nds.length);
      // Noeuds
      for (int i= 0; i < nds.length; i++) {
        RefondeNoeudData dt= (RefondeNoeudData)nds[i].data();
        GrPoint pt= nds[i].point_;
        file.println(dt.numero + " ; " + pt.x_ + " ; " + pt.y_ + " ; " + pt.z_);
      }
      file.println();
      // Nombre d'éléments
      file.println("<elements> ; " + ml.elements().length);
      // Eléments
      for (int i= 0; i < vdms.size(); i++) {
        dm= (RefondeDomaine)vdms.get(i);
        if (!dm.hasMaillage())
          continue;
        els= dm.getMaillage().elements();
        for (int j= 0; j < els.length; j++) {
          //... Numéro optimisé
          RefondeElementData dt= (RefondeElementData)els[j].data();
          file.print(dt.numero + " ; ");
          //... Type
          switch (els[j].type_) {
            case GrElement.L2 :
              file.print("l2 ; ");
              break;
            case GrElement.L3 :
              file.print("l3 ; ");
              break;
            case GrElement.Q4 :
              file.print("q4 ; ");
              break;
            case GrElement.Q8 :
              file.print("q8 ; ");
              break;
            case GrElement.T3 :
              file.print("t3 ; ");
              break;
            case GrElement.T6 :
              file.print("t6 ; ");
              break;
          }
          //... Numéro de domaine
          file.print(i);
          //... Numéros de noeuds
          ndsEl= els[j].noeuds_;
          for (int k= 0; k < ndsEl.length; k++) {
            file.print(" ; " + hnds.get(ndsEl[k]));
          }
          file.println();
        }
      }
      file.println();
      // Nombre de propriétés de maillage pour le domaine
      file.println("<proprietes_maillage> ; " + vdms.size());
      // Propriétés de maillage
      for (int i= 0; i < vdms.size(); i++) {
        // Domaine fond
        if (vdms.get(i) instanceof RefondeDomaineFond) {
          RefondeDomaineFond df= (RefondeDomaineFond)vdms.get(i);
          //... Type de maillage
          if (df.getTypeMaillage() == RefondeDomaineFond.CLASSIQUE) {
            file.print("classique ; ");
            //... Aire maxi des éléments
            file.println(df.getAireMaxi());
          } else {
            file.print("longueur_onde ; ");
            //... Nombre de noeuds par longueur d'onde
            file.print(df.getNbNoeudsOnde() + " ; ");
            //... Période de houle
            file.println(df.getPeriodeHoule());
          }
        }
        // Domaine digue
        else {
          RefondeDomaineDigue dd= (RefondeDomaineDigue)vdms.get(i);
          //... Type de maillage
          if (dd.getTypeMaillage() == RefondeDomaineDigue.OUVRAGE) {
            file.print("ouvrage ; ");
            //... Nombre de troncons
            int[] nbEls= dd.getNbElements();
            file.print(nbEls.length);
            //... Nombre d'éléments par troncon
            for (int j= 0; j < nbEls.length; j++)
              file.print(" ; " + nbEls[j]);
            file.println();
          } else
            throw new RefondeIOException("Type de maillage non prévu");
        }
      }
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur d'écriture sur " + _fc + "\n" + _exc.getMessage());
    } catch (IOException _exc) {
      throw new IOException("Erreur d'écriture sur " + _fc);
    }
    //    catch (Exception _exc) {
    //      throw new IOException(_exc.getMessage());
    //    }
    finally {
      if (file != null)
        file.close();
    }
  }
  /**
   * Détermination des domaines d'appartenance d'un maillage global.
   * L'algoritme est simple : On recherche pour chaque domaine quel élément a
   * un barycentre à l'intérieur du domaine.
   */
  private void decoupeMaillageGlobal(RefondeProjet _prj, GrMaillageElement _ml)
    throws RefondeIOException {
    GrElement[] els= _ml.elements();
    Vector vdms= _prj.getGeometrie().getDomaines();
    // Détermination du maillage pour tous les domaines autres que le
    // domaine de fond
    int idf= 0;
    for (int i= 0; i < vdms.size(); i++) {
      RefondeDomaine dm= (RefondeDomaine)vdms.get(i);
      if (dm instanceof RefondeDomaineFond)
        idf= i;
      else
        determineMaillageDomaine(dm, els);
    }
    // Détermination du maillage pour le domaine fond
    determineMaillageDomaine((RefondeDomaine)vdms.get(idf), els);
    // Controle que tous les éléments ont été affectés.
    for (int i= 0; i < els.length; i++)
      if (els[i] != null)
        throw new RefondeIOException("Le maillage ne correspond probablement pas à la géométrie");
  }
  /**
   * Détermine les éléments du maillage pour le domaine donné.
   */
  private void determineMaillageDomaine(RefondeDomaine _dm, GrElement[] _els) {
    // Détermination du polygone correspondant au domaine.
    RefondeContour ct= _dm.getContours()[0];
    RefondePolyligne[] pls= ct.getPolylignes();
    GrPolygone pg= new GrPolygone();
    for (int j= 0; j < pls.length; j++)
      pg.sommets_.ajoute(pls[j].sommet(0));
    Polygon pol= pg.polygon();
    // Recherche des éléments dans ce polygone.
    Vector vels= new Vector();
    for (int j= 0; j < _els.length; j++) {
      if (_els[j] == null)
        continue;
      GrPoint bary= _els[j].barycentre();
      if (pol.contains(bary.x_, bary.y_)) {
        vels.add(_els[j]);
        _els[j]= null;
      }
    }
    // Création du nouveau maillage et affectation au domaine.
    _dm.setMaillage(
      new GrMaillageElement((GrElement[])vels.toArray(new GrElement[0])));
  }
}