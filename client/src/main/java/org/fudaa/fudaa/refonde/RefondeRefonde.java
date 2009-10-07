/*
 * @file         RefondeRefonde.java
 * @creation     1999-10-28
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.fudaa.dodico.corba.objet.IConnexion;
import org.fudaa.dodico.corba.refonde.*;

import org.fudaa.dodico.objet.UsineLib;
import org.fudaa.dodico.refonde.DParametresRefonde;

import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;

import org.fudaa.fudaa.commun.conversion.FudaaInterpolateurMaillage;

/**
 * Transfert du projet vers les structures Refonde et calcul.
 * <p>
 * <b>Remarque</b>
 * <p>
 * Tous les angles transmis sont à présent transmis par rapport à un 0 Nord,
 * dans le sens horaire.
 *
 * @version      $Id: RefondeRefonde.java,v 1.13 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeRefonde {

  /**
   * Appel au serveur de calcul Refonde.
   * <p>
   * Les résultats sont stockés dans le projet à la suite du calcul.
   *
   * @param _serveur Serveur de calcul.
   * @param _reprise Force le calcul en mode reprise houle aléatoire.
   * @param _projet  Projet.
   */
  public static void calcul(ICalculRefonde _serveur,IConnexion _cx,boolean _reprise,
                            RefondeProjet _projet) {

    IParametresRefonde par=
      IParametresRefondeHelper.narrow(_serveur.parametres(_cx));

    // Transfert des informations dans un format de parametres serveur
    versParametresRefonde(par, _projet);

    // Calcul
    if (_reprise) par.typeCalcul(DParametresRefonde.HOULE_ALEA_REPRISE);
    _serveur.calcul(_cx);

    // Récupération des résultats
    if (_serveur.estOK()) {
      try {
        SResultatsRefonde res=
         IResultatsRefondeHelper.narrow(_serveur.resultats(_cx)).resultatsRefonde();

        System.out.println("Resultats: " + res.etapes.length + " pas");

        RefondeResultats p= new RefondeResultats();
        p.fromStruct(res);
        _projet.setResultats(p);
      }
      catch (NullPointerException ex) {
        System.out.println("Pas de resultats");
      }
    }
    else {
      _projet.setResultats(null); // Pour annuler les résultats précédents.
    }
  }

  /**
   * Transfert du projet dans un format de parametres serveur.
   *
   * @param _params L'objet contenant les informations en format serveur.
   * @param _projet Le projet.
   */
  public static void versParametresRefonde(
    IParametresRefonde _params,
    RefondeProjet _projet) {

    // Type de calcul
    if (_projet.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_SEICHE) {
      _params.typeCalcul(DParametresRefonde.MODULE_SEICHE);
    }
    else {
      _params.typeCalcul(_projet.getModeleCalcul().typeHoule());
    }
    // Racine des fichiers calcul
    _params.racine(_projet.getRacineFichiersCalcul());
    if("TRUE".equals(System.getProperty("TEST"))){
      System.err.println("nouvelle version de test");
      IParametresRefondeINP inp=UsineLib.findUsine().creeRefondeParametresRefondeINP();
      versParametresRefondeINPNewVersion(inp,_projet);
      _params.parametresINPNewVersion(inp);
      inp=null;
    }
    else {
      // Parametres INP
      SParametresRefondeINP pINP= new SParametresRefondeINP();
      versParametresRefondeINP(pINP, _projet);
      _params.parametresINP(pINP);
    }
    // Parametres SPE
    if (_projet.getModeleCalcul().typeModele()!=RefondeModeleCalcul.MODELE_SEICHE &&
        _projet.getModeleCalcul().typeHoule()==RefondeModeleCalcul.HOULE_ALEA) {
      SParametresRefondeSPE pSPE= new SParametresRefondeSPE();
      versParametresRefondeSPE(pSPE, _projet);
      _params.parametresSPE(pSPE);
    }
  }
  /**
   * Fred optimisation pour les gros projets: ne pas surcharger le client
   * Transfert du projet dans un format des parametres INP serveur.
   *
   * @param _params La structure contenant les informations en format serveur.
   * @param _projet Le projet.
   */
  private static void versParametresRefondeINPNewVersion(IParametresRefondeINP _params,
    RefondeProjet _projet) {
    System.err.println("Optimisation: cette méthode n'est pas encore validée");
    GrMaillageElement mai;
    List cntrs;
    RefondeModeleProprietes mdlPrp;
    RefondeModeleCalcul mdlCal;
    RefondeGeometrie geo;
    RefondeDomainePoreux[] dmps;

    TIntObjectHashMap hconns= new TIntObjectHashMap(); // Table des connectivités
    TIntIntHashMap htypes= new TIntIntHashMap(); // Table des types d'éléments
    TIntIntHashMap hngpes= new TIntIntHashMap(); // Table des numéros de groupe
    List vgpes= new ArrayList();        // Table des groupes

    int numEle;
    boolean bspel; // Présence de super élément (formulations analytiques)
    List scEls=new ArrayList(); // Stockage des arètes pour les frontières ouvertes seiches

    mdlPrp= _projet.getModeleProprietes();
    mdlCal= _projet.getModeleCalcul();
    int tpModele=mdlCal.typeModele();

    //--------------------------------------------------------------------------
    //---  Controles  ----------------------------------------------------------
    //--------------------------------------------------------------------------

    // Controle qu'il y a un maillage sur tous les domaines
    if (!_projet.estEntierementMaille())
      throw new IllegalArgumentException("Le projet n'est pas totalement maillé");
    mai= RefondeMaillage.creeSuperMaillage(RefondeMaillage.getMaillages(_projet));

    // Controle que l'optimisation a bien été effectuée, cad que nbNoe=numMaxNoe
    // et nbEle=numMaxEle
    if (!RefondeMaillage.estOptimise(mai))
      throw new IllegalArgumentException("Le maillage n'est pas optimisé");
    cntrs= _projet.getGeometrie().frontieres();
    geo= _projet.getGeometrie();

    //---  Controles spécifiques à la houle  -----------------------------------

    if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {
      // Controle que la hauteur de houle > 0.
      if ((mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_REG&&mdlCal.periodeHoule()<=0)
          ||(mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_ALEA
             &&mdlCal.periodeHouleMini()<=0))
        throw new IllegalArgumentException("La période de houle doit être > 0.");

      // Controle que la bathymétrie en chaque point+hauteur de mer est bien
      // positive
      final int nbNoeud=mai.getNbNoeud();
      {

        for (int i=0; i<nbNoeud; i++)
          if (mai.noeud(i).point_.z_+mdlCal.hauteurMer()<=0)
            throw new IllegalArgumentException(
                "La hauteur de mer doit être "+"supérieure à la bathymétrie");
      }

      // Si le nombre maxi de noeuds/éléments de Refonde est atteint, message
      if (mai.getNbElement()>RefondePreferences.REFONDE.nbMaxElements() ||
          nbNoeud>RefondePreferences.REFONDE.nbMaxNoeuds())
        throw new IllegalArgumentException(
            "Les limites de maillage de Refonde sont atteintes.\n"
            +"Vous ne pouvez pas passer de calcul.\n"
            +"Remaillez avec un raffinement moindre ou modifiez Refonde.");

      // Contrôle que les points de plan des domaines poreux sont correctement
      // renseignés en Z si prise en compte de la porosité
      if (mdlCal.isFondsPoreux()) {
        dmps=mdlPrp.getDomainesPoreux();
        for (int i=0; i<dmps.length; i++) {
          GrPoint[] ptsPlan=dmps[i].getPointsPlan();
          for (int j=0; j<ptsPlan.length; j++) {
            if (dmps[i].isZAutomatique(j)&&!geo.contient(ptsPlan[j]))
              throw new IllegalArgumentException(
                  "Un point du plan d'un domaine poreux est\n"
                  +"hors géométrie et non renseigné en z.");
          }
        }
      }
    }

    //--------------------------------------------------------------------------
    //---  Informations élémentaires  ------------------------------------------
    //--------------------------------------------------------------------------
int tmp;
    {
      List spEls=new ArrayList(); // Stockage des arètes pour le super-élément
      double[] spGpe= null; // Groupe de propriétés pour le super élément

      if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {

        //---  Elements de digues perforées  -----------------------------------

        {
          List vdms=geo.scene_.getDomaines();
        tmp=vdms.size();
          for (int i=0; i<tmp; i++) {
            if (!(vdms.get(i)instanceof RefondeDomaineDigue))
              continue;
//            GrElement[] els;
            RefondeDomaineDigue dm=(RefondeDomaineDigue)vdms.get(i);
            RefondeGroupeProprietes gp=dm.getGroupeProprietes();
            RefondePropriete[] prs=gp.getProprietes();
            if (gp.getType()
                !=RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE)
              continue;

//            els=dm.getMaillage().elements();

            final GrMaillageElement mailElt=dm.getMaillage();
            final int nbEl=mailElt.getNbElement();
            hconns.ensureCapacity(nbEl);
            // Connectivités
            for (int j=0; j<nbEl; j++) {
              GrElement elsj=mailElt.element(j);
              GrNoeud[] ndsEle=elsj.noeuds_;
              int[] conn=new int[ndsEle.length];
              int num=((RefondeElementData)elsj.data()).numero;
              for (int k=0; k<ndsEle.length; k++)
                conn[k]=((RefondeNoeudData)ndsEle[k].data()).numero;
              hconns.put(num, conn);
            }
            // Types
            int type=6;
            for (int j=0; j<nbEl; j++) {
              int num=((RefondeElementData)mailElt.element(j).data()).numero;
              htypes.put(num, type);
            }
            // Groupe de propriétés
            double[] gpe=new double[7];
            for (int k=0; k<7; k++)
              gpe[k]=0;
              //... Initialisation de pertes de charge (partie réelle)
            gpe[0]=prs[0].getValeur();
            //... Initialisation de pertes de charge (partie imaginaire)
            gpe[1]=prs[1].getValeur();
            //... Contraction
            gpe[2]=prs[3].getValeur();
            //... Porosité
            gpe[3]=prs[2].getValeur();
            //... Intégration verticale
            gpe[4]=prs[4].getValeur();
            // Numéros de groupes de propriété=1;
            int ngpe=getNumGpe(vgpes, gpe);
            for (int j=0; j<nbEl; j++) {
              int num=((RefondeElementData)mailElt.element(j).data()).numero;
              hngpes.put(num, ngpe);
            }
          }
        }

        //---  Elements de digues transmissibles  ------------------------------

        {
          // Polyligne vers angle d'incidence et vers angle de transmission
          RefondeAngle[] ais=mdlCal.getAnglesIncidenceDigues();
          RefondeAngle[] ats=mdlCal.getAnglesTransmissionDigues();
          Map pl2Ai=new HashMap(ais.length);
          Map pl2At=new HashMap(ats.length);
          for (int i=0; i<ais.length; i++)
            pl2Ai.put(mdlCal.getPolyligneAI(ais[i]), ais[i]);
          for (int i=0; i<ats.length; i++)
            pl2At.put(mdlCal.getPolyligneAT(ats[i]), ats[i]);
          Vector vdms=geo.scene_.getDomaines();
          int vdmsSize=vdms.size();
          for (int i=0; i<vdmsSize; i++) {
            if (!(vdms.get(i)instanceof RefondeDomaineDigue))
              continue;
//            GrElement[] els;
            RefondeDomaineDigue dm=(RefondeDomaineDigue)vdms.get(i);
            RefondeGroupeProprietes gp=dm.getGroupeProprietes();
            RefondePropriete[] prs=gp.getProprietes();
            if (gp.getType()
                !=RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
              continue;
            final GrMaillageElement grElt=dm.getMaillage();
            final int eltNb=grElt.getNbElement();
  //          els=dm.getMaillage().elements();

            // Recherche de la correspondance élément->polyligne d1,
            // élément->polyligne d2
            RefondeContour ct=dm.getContour();
            RefondePolyligne[] pls=ct.getPolylignes();
            Map el2Pl[]=
                new HashMap[] {new HashMap(), new HashMap()};
            for (int j=0; j<pls.length; j++) {
              if (pls[j]==dm.getExtremiteDigue()
                  ||pls[j]==dm.getExtremiteFinDigue())
                continue;
              GrNoeud[] ndsPl=pls[j].getNoeuds();
              NEXT_ARETE:for (int k=0; k<ndsPl.length-1; k++) {
                for (int l=0; l<eltNb; l++) {
                  GrNoeud[] ndsEle=grElt.element(l).noeuds_;
                  for (int m=0; m<2; m++) {
                    if ((ndsEle[m*2]==ndsPl[k]
                         &&ndsEle[m*2+1]==ndsPl[k+1])
                        ||(ndsEle[m*2]==ndsPl[k+1]
                           &&ndsEle[m*2+1]==ndsPl[k])) {
                      el2Pl[m].put(grElt.element(l), pls[j]);
                      continue NEXT_ARETE;
                    }
                  }
                }

                // Aucun élément trouvé pour la correspondance
                throw new IllegalArgumentException(
                    "Problème de transfert "
                    +"des éléments de digues transmissibles");
              }
            }

            // Connectivités
            for (int j=0; j<eltNb; j++) {
              GrNoeud[] ndsEle=grElt.element(j).noeuds_;
              int[] conn=new int[ndsEle.length];
              int num=((RefondeElementData)grElt.element(j).data()).numero;
              for (int k=0; k<ndsEle.length; k++)
                conn[k]=((RefondeNoeudData)ndsEle[k].data()).numero;
              hconns.put(num, conn);
            }

            // Types
            int type=5;
            for (int j=0; j<eltNb; j++) {
              int num=((RefondeElementData)grElt.element(j).data()).numero;
              htypes.put( num, type);
            }

            for (int j=0; j<eltNb; j++) {

              // Groupe de propriétés
              double[] gpe=new double[11];
              for (int k=0; k<gpe.length; k++)
                gpe[k]=0;
              RefondeAngle ai1;
              RefondeAngle at1;
              RefondeAngle ai2;
              RefondeAngle at2;
              GrElement gj=grElt.element(j);
              ai1=(RefondeAngle)pl2Ai.get(el2Pl[0].get(gj));
              at1=(RefondeAngle)pl2At.get(el2Pl[0].get(gj));
              ai2=(RefondeAngle)pl2Ai.get(el2Pl[1].get(gj));
              at2=(RefondeAngle)pl2At.get(el2Pl[1].get(gj));
              //... Code d'angle
              int code1=0;
              int code2=0;
              if (ai1.getType()==RefondeAngle.ABSOLU
                  &&at2.getType()==RefondeAngle.ABSOLU)
                code1=9;
              if (ai1.getType()==RefondeAngle.RELATIF
                  &&at2.getType()==RefondeAngle.RELATIF)
                code1=7;
              if (ai2.getType()==RefondeAngle.ABSOLU
                  &&at1.getType()==RefondeAngle.ABSOLU)
                code2=9;
              if (ai2.getType()==RefondeAngle.RELATIF
                  &&at1.getType()==RefondeAngle.RELATIF)
                code2=7;
              if (code1==0||code2==0)
                throw new IllegalArgumentException(
                    "Les angles associés de houle incidente et transmise à travers\n"
                    +"les digues transmissibles doivent être de même type "
                    +"(Relatif ou Absolu)");
              gpe[0]=code1*10+code2;
              //... Angle incidence domaine 1
              gpe[1]=code1==9?math2Navig(ai1.getAngle()):ai1.getAngle();
              //... Angle transmission domaine 2
              gpe[2]=
                  code1==9?math2Navig(at2.getAngle()):at2.getAngle()+180;
              // +180 en relatif ?
              //... Angle incidence domaine 2
              gpe[3]=code2==9?math2Navig(ai2.getAngle()):ai2.getAngle();
              //... Angle transmission domaine 1
              gpe[4]=
                  code2==9?math2Navig(at1.getAngle()):at1.getAngle()+180;
              // +180 en relatif ?
              //... Coefficient réflexion domaine 1
              gpe[5]=prs[0].getValeur();
              //... Coefficient réflexion domaine 2
              gpe[6]=gpe[5];
              //... Coefficient transmission domaine 1
              gpe[7]=prs[1].getValeur();
              //... Coefficient transmission domaine 2
              gpe[8]=gpe[7];
              //... Déphasage réflexion
              gpe[9]=prs[2].getValeur();
              //... Déphasage transmission
              gpe[10]=prs[3].getValeur();
              // Numéro de groupe
              int ngpe=getNumGpe(vgpes, gpe);
              int num=((RefondeElementData)gj.data()).numero;
              hngpes.put(num, ngpe);
            }
          }
        }
      }

      //---  Eléments de fond  -------------------------------------------------

      {
        Vector vdms= geo.scene_.getDomaines();
tmp=vdms.size();
        for (int i= 0; i < tmp; i++) {
          if (!(vdms.get(i) instanceof RefondeDomaineFond)) continue;

//          GrElement[] els;
          RefondeDomaineFond dm= (RefondeDomaineFond)vdms.get(i);
          // RefondeGroupeProprietes gp=dm.getGroupeProprietes();
  //        els= dm.getMaillage().elements();
          final GrMaillageElement grElt=dm.getMaillage();
          final int nbElt=grElt.getNbElement();
          hconns.ensureCapacity(nbElt);
          // Connectivités
          for (int j= 0; j < nbElt; j++) {
            GrElement ej=grElt.element(j);
            GrNoeud[] ndsEle= ej.noeuds_;
            int[] conn= new int[ndsEle.length];
            int num= ((RefondeElementData)ej.data()).numero;
            for (int k= 0; k < ndsEle.length; k++)
              conn[k]= ((RefondeNoeudData)ndsEle[k].data()).numero;
            hconns.put(num, conn);
          }

          // Types
          int type;
          if (tpModele==RefondeModeleCalcul.MODELE_HOULE && mdlCal.isFondsPoreux())
            type=21;
          else
            type=1;
          htypes.ensureCapacity(nbElt);
          for (int j= 0; j < nbElt; j++) {
            int num= ((RefondeElementData)grElt.element(j).data()).numero;
            htypes.put(num, type);
          }
          // 1 groupe bidon pour tous les éléments
          double[] gpe= new double[7];
          for (int k= 0; k < 7; k++)
            gpe[k]= 0;
          // Numéros de groupes de propriété=1;
          int ngpe= getNumGpe(vgpes, gpe);
          hngpes.ensureCapacity(nbElt);
          for (int j= 0; j < nbElt; j++) {
            int num= ((RefondeElementData)grElt.element(j).data()).numero;
            hngpes.put(num, ngpe);
          }
        }
      }

      //---  Eléments du bord  -------------------------------------------------

      {
        final int OUV_SOR=RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE;
        //final int OUV_ENT=RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE;
        final int SEM_REF=RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT;
        final int SCH_OUV=RefondeGroupeProprietes.SEICHE_BORD_OUVERT;

        class Segment {
          RefondeGroupeProprietes g= null;
          RefondeAngle a= null;
          GrElement e= null;
          double s= -1.;
        }

        numEle= hconns.size();
        GrElement[][] elss= mai.aretesContours();
        if (elss.length != cntrs.size()) {
          System.out.println("nb cntrs geometrie: " + cntrs.size());
          System.out.println("nb cntrs maillage : " + elss.length);
          throw new IllegalArgumentException(
            "Problème de reconnaissance " + "géométrie<->maillage");
        }

        bspel= false;
        tmp=cntrs.size();
        // Boucle sur les polylignes frontières
        for (int i= 0; i < tmp; i++) {
          GrPolyligne plTot;
          RefondePolyligne pl;
          double sDeb;
          double s;
          double dstMin;
          GrPoint pt;
          List sgs;
          int isgs;
          List ais;
          // int ngpe;
          Segment sgc= null;
          double lgSeg;
          RefondeAngle ai;
          List cntr= (List)cntrs.get(i);
          pt= ((RefondePolyligne)cntr.get(0)).sommet(0);

          // Création d'une polyligne globale
          plTot= new GrPolyligne();
          int cntrTemp=cntr.size();
          for (int j= 0; j < cntrTemp; j++) {
            pl= (RefondePolyligne)cntr.get(j);
            pt= pl.sommet(0);
            plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0));
          }
          plTot.sommets_.ajoute(plTot.sommet(0));

          // Recherche du début de bord "élément" correspondant au début de bord
          // "géométrie"
          int ncntr= -1;
          int narete= -1;
          dstMin= Double.MAX_VALUE;
          pt= ((RefondePolyligne)cntr.get(0)).sommet(0);

          NEXTCNTR : for (int j= 0; j < elss.length; j++) {
            double dst;
            for (int k= 0; k < elss[j].length; k++) {
              dst= pt.distanceXY(elss[j][k].noeuds_[0].point_);
              if (dst < dstMin) {
                ncntr= j;
                narete= k;
                dstMin= dst;
                if (dstMin == 0)
                  break NEXTCNTR;
              }
            }
          }

          sgs= new ArrayList();

          // Segmentation par les groupes de propriétés
          for (int j= 0; j < cntr.size(); j++) {
            RefondeGroupeProprietes gpe;
            Segment sg= new Segment();
            pl= geo.polyligne((RefondePolyligne)cntr.get(j));
            gpe= pl.getGroupeProprietes();
            s= plTot.abscisseDe(j + 1);
            sg.g= gpe;
            sg.s= s;
            sgs.add(sg);
          }

          // Segmentation par les éléments
          isgs= 0;
          for (int j= 0; j < elss[ncntr].length; j++) {
            Segment sg= new Segment();
            GrElement e;
            e= elss[ncntr][(j + narete) % elss[ncntr].length];
            pt= e.noeuds_[e.noeuds_.length - 1].point_;
            s= plTot.abscisseDe(new GrPoint(pt.x_, pt.y_, 0.));
            if (j == elss[ncntr].length - 1)
              s= 1;

            // Pour les problemes d'arrondis et de point initial/fin identiques
            while (isgs < sgs.size() && (sgc= (Segment)sgs.get(isgs)).s < s) {
              sgc.e= e;
              isgs++;
            }

            // Ajout du segment
            if (s < sgc.s) {
              sg.e= e;
              sg.s= s;
              sg.g= sgc.g;
              sgs.add(isgs, sg);
              isgs++;
            }

            // Pas d'ajout du segment, mais initialisation de l'ancien segment
            else if (s == sgc.s) {
              sgc.e= e;
              isgs++;
            }
          }

          // Segmentation par les angles d'incidence
          ais= mdlCal.angles()[i];
//          int nangle=-1;
//          int nais=ais.size();
//          for (int j=0; j<ais.size(); j++) {
//            ai=(RefondeAngle)ais.get(j);
//            if (ai.getSFin()<ai.getSDebut()) {
//              nangle=j;
//              if (ai.getSFin()==0) nangle++;
//              else                 nais++;
//              break;
//            }
//          }
          isgs= 0;
          for (int j= 0; j < ais.size(); j++) {
//          for (int j=0; j<nais; j++) {
            Segment sg= new Segment();
            ai= (RefondeAngle)ais.get(j);
//            ai=(RefondeAngle)ais.get((j+nangle)%ais.size());
            s= ai.getSFin();
//            System.out.println("s: "+s);
//            if (j==nais-1) s=1;

            // Recherche du segment sgc pour lequel sgc.s>=s
            while (isgs < sgs.size() && (sgc= (Segment)sgs.get(isgs)).s < s) {
              sgc.a= ai;
              isgs++;
            }

            // Ajout du segment
            if (s < sgc.s) {
              sg.a= ai;
              sg.s= s;
              sg.g= sgc.g;
              sg.e= sgc.e;
              sgs.add(isgs, sg);
              isgs++;
            }

            // Pas d'ajout du segment, mais initialisation de l'ancien segment
            else if (s == sgc.s) {
              sgc.a= ai;
              isgs++;
            }
          }
//          for (int j=0; j<sgs.size(); j++)
//           if (((Segment)sgs.get(j)).a==null) System.out.println("null");

          // Stockage des éléments de bord, des types, des propriétés, des angles

          // Pour les angles, l'angle retenu est celui le plus représenté sur
          // l'élément
          sDeb= 0;
          lgSeg= -Double.MAX_VALUE;
          ai= null;
//          sgc=(Segment)sgs.get(0);
//          ai=sgc.a;
//          Segment sgSuiv=(Segment)sgs.get(1);

          for (int j= 0; j < sgs.size(); j++) {
            Segment sg= (Segment)sgs.get(j);

            // Recherche de l'angle le plus significatif
            if (sg.s - sDeb > lgSeg) {
              lgSeg= sg.s - sDeb;
              ai= sg.a;
            }

            sDeb= sg.s;

            // Changement d'élément => Stockage
            if (j == sgs.size() - 1 || ((Segment)sgs.get(j + 1)).e != sg.e) {
              // Type
              int type;
              if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {
                if (sg.g.getType()==OUV_SOR)
                  type=2;
                else if (sg.g.getType()==SEM_REF)
                  type=3;
                else
                  type=4+mdlCal.casBordOuvert()*10;
              }
              else {
                type=13; // Pour tous les éléments de frontière modèle de seiche
              }

              // Groupe de propriétés
              double[] gpe= new double[7];
//              Arrays.fill(gpe,0);

              //... Pour une frontière ouverte en sortie ou semi refléchissante
              if (type == 2 || type == 3) {
                if (ai.getType() == RefondeAngle.RELATIF) {
                  gpe[0]= 7;
                  gpe[5]= ai.getAngle();
                } else if (ai.getType() == RefondeAngle.DIFFRACTE) {
                  gpe[0]= 8;
                  gpe[2]= ai.getPointDiffraction().x_;
                  gpe[3]= ai.getPointDiffraction().y_;
                } else {
                  gpe[0]= 9;
                  gpe[5]= math2Navig(ai.getAngle());
                }
                if (type == 3)
                  gpe[4]= sg.g.getProprietes()[0].getValeur();
              }

              //... Pour une frontière modèle seiche
              //    On crée un grp bidon pour qu'il y ai bien 2 groupes (1 pour
              //    le fond, 1 pour les bord).
              else if (type==13) {
                  gpe[0]=1; // Pour différencier de celui de fond.
              }

              //... Pour une frontière ouverte en entrée
              else {
                if (ai.getType() == RefondeAngle.ABSOLU) {
                  //... Alpha inc., Theta sup.
                  if (ai.getTypeSup() == RefondeAngle.RELATIF) {
                    gpe[0]= 4;
                    gpe[5]= math2Navig(ai.getAngle());
                    gpe[6]= ai.getAngleSup();
                  }
                  //.. Alpha inc., Alpha sup.
                  else {
                    gpe[0]= 9;
                    gpe[5]= math2Navig(ai.getAngle());
                    gpe[6]= math2Navig(ai.getAngleSup());
                  }
                }

                //... Les autres combinaisons sont interdites
                else {
                  throw new IllegalArgumentException(
                    "Une frontière ouverte doit avoir un angle d'incidence\n"
                      + "défini de manière absolue");
                }
              }

              // Frontières ouvertes modèle seiche : On stocke les éléments de
              // bord pour récupérer les noeuds plus tard.
              if (type==13 && sg.g.getType()==SCH_OUV) {
                scEls.add(sg.e);
              }

              // Recherche d'un groupe de propriétés équivalent

              // Cas d'une frontière ouverte en super élément (formulations analytiques)
              if (type == 14 || type == 24) {
                bspel= true;
                spGpe= gpe;
//                //... Ajout de 180° à l'angle d'incidence
//                spGpe[5]=(spGpe[5]+180)%360;
                spEls.add(sg.e);
              }

              // Cas général
              else {
                numEle++;
/*                NGPE : {
                  NEXT_GPE : for (int k=0; k<vgpes.size(); k++) {
                    double[] gpec=(double[])vgpes.get(k);
                    for (int l=0; l<7; l++)
                     if (gpec[l]!=gpe[l]) continue NEXT_GPE;

                    ngpe=k;
                    break NGPE;
                  }

                  ngpe=vgpes.size();
                  vgpes.add(gpe);
                }*/

                // Connectivité
                GrNoeud[] ndsEle= sg.e.noeuds_;
                int[] conn= new int[ndsEle.length];

                for (int k= 0; k < ndsEle.length; k++)
                  conn[k]= ((RefondeNoeudData)ndsEle[k].data()).numero;
                hconns.put(numEle, conn);

                // Type
                htypes.put(numEle, type);

                // Groupe + Numéro de groupe
                int ngpe= getNumGpe(vgpes, gpe);
                hngpes.put(numEle, ngpe);
              }

              lgSeg= -Double.MAX_VALUE;
//              ai=sgc.a;
//              sgc=sg;
            }
          }
        }
      }

      //---  Transfert des informations élémentaires  --------------------------

      if (bspel) {
        TIntArrayList vconn= new TIntArrayList ();
//        Vector tmp=new Vector();
        GrNoeud[] ndsEle= null;
        int numNoe;
        numEle++;

        for (int i= 0; i < spEls.size(); i++) {
          ndsEle= ((GrElement)spEls.get(i)).noeuds_;
          vconn.ensureCapacity(ndsEle.length );
          for (int j= 0; j < ndsEle.length - 1; j++) {
            numNoe= ((RefondeNoeudData)ndsEle[j].data()).numero;
            vconn.add(numNoe);
//           tmp.add((Integer)nd2Num.get(ndsEle[j]));
          }
        }
        numNoe= ((RefondeNoeudData)ndsEle[ndsEle.length - 1].data()).numero;

        if (vconn.get(0) != numNoe)
          vconn.add(numNoe);
//        Integer numNd=(Integer)nd2Num.get(ndsEle[ndsEle.length-1]);
//        if (!((Integer)tmp.get(0)).equals(numNd)) tmp.add(numNd);

/*        int[] conn= vconnnew int[vconn.size()];
        for (int i= 0; i < vconn.size(); i++)
          conn[i]= ((Integer)vconn.get(i)).intValue();*/

        // Connectivité
        hconns.put(numEle, vconn.toNativeArray());

        // Type
        htypes.put(numEle,4 + mdlCal.casBordOuvert() * 10);

        // Groupe + numéro
        int ngpe= getNumGpe(vgpes, spGpe);
        hngpes.put(numEle, ngpe);
      }
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'SPEC'  --------------------------------------------------------
    //--------------------------------------------------------------------------
SParametresRefondeINPBase base=new SParametresRefondeINPBase();

    if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
    {
      // Déferlement
      base.deferlement= mdlCal.deferHoule();
      base.formuleDeferlement= mdlCal.formuleDeferHoule();

      // Nombre de période de houle
      base.nbPeriodesHoule= mdlCal.nbPeriodesHoule();

      // Périodes
      base.periodeHoule= mdlCal.periodeHoule();
      base.periodeHouleMin= mdlCal.periodeHouleMini();
      base.periodeHouleMax= mdlCal.periodeHouleMaxi();

      // Nombre de directions de houle
      base.nbAnglesHoule= mdlCal.nbAnglesHoule();

      // Directions
      // Important B.M. : Les angles utilisateurs tournant dans le sens inverse
      // des angles internes, les angles mini doivent être les angles maxi et
      // inversement.
      base.angleHoule= math2Navig(mdlCal.angleHoule());
      base.angleHouleMin= math2Navig(mdlCal.angleHouleMaxi());
      base.angleHouleMax= math2Navig(mdlCal.angleHouleMini());
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'COOR'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    {

      //GrNoeud[] nds= mai.noeuds();
      base.nbNoeuds= mai.getNbNoeud();
      SParametresRefondeLigneXYZ[]  xyz= new SParametresRefondeLigneXYZ[base.nbNoeuds];

      for (int i= 0; i < base.nbNoeuds; i++) {
        GrNoeud n=mai.noeud(i);
        int numNoe= ((RefondeNoeudData)n.data()).numero;
        GrPoint p= n.point_;
        xyz[numNoe - 1]= new SParametresRefondeLigneXYZ(p.x_, p.y_, p.z_);
      }
      _params.xyz(xyz);
      xyz=null;
    }

    //------------------------------------------------------------------------
    //---  Bloc 'ELEM'  ------------------------------------------------------
    //------------------------------------------------------------------------

    {
      base.nbElements= hconns.size();

      // Nombre max de noeuds par élément / nombre de noeuds du super élément
      base.nbNoeudsSuperElement= 0;
      base.nbMaxNoeudsElement= 0;

      for (int i= 0; i < hconns.size(); i++) {
        if (bspel && i == hconns.size() - 1) {
          base.nbNoeudsSuperElement=
            ((int[])hconns.get(i + 1)).length;
        }
        else {
          base.nbMaxNoeudsElement=
            Math.max(
              base.nbMaxNoeudsElement,
              ((int[])hconns.get(i + 1)).length);
        }
      }

      // Nombre max de PE par groupe
      base.nbMaxPEGroupe= 0;
      tmp=vgpes.size();
      for (int i= 0; i < tmp; i++)
        base.nbMaxPEGroupe=
          Math.max(base.nbMaxPEGroupe, ((double[])vgpes.get(i)).length);

      base.nbGroupesPE= tmp;
      base.ordreMax= mdlCal.ordreMax();
      base.casBordOuvert= (bspel ? 2 : 1);
      SParametresRefondeConnectivite[] connectivites=new SParametresRefondeConnectivite[hconns.size()];

      tmp=hconns.size();
      for (int i= 0; i < tmp; i++)
        connectivites[i]=
          new SParametresRefondeConnectivite(
            (int[])hconns.get(i + 1));
      _params.connectivites(connectivites);
      connectivites=null;
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'TYPE'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    {
      tmp=htypes.size();
       int[] type= new int[tmp];
      for (int i= 0; i < tmp; i++)
        type[i]= htypes.get(i + 1);
      _params.type(type);
      type=null;
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'PREL'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    {
      int[] groupePE= new int[hngpes.size()];


      for (int i= 0; i < hngpes.size(); i++)
        groupePE[i]=hngpes.get(i + 1);
      _params.groupePE(groupePE);
      groupePE=null;

     SParametresRefondeLignePE[] valeurPE= new SParametresRefondeLignePE[vgpes.size()];

      for (int i= 0; i < vgpes.size(); i++) {
        double[] gpe= (double[])vgpes.get(i);
        valeurPE[i]= new SParametresRefondeLignePE(gpe);
      }
      _params.valeurPE(valeurPE);
      valeurPE=null;
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'COND'  --------------------------------------------------------
    //--------------------------------------------------------------------------
    SParametresRefondeSeiche seiche=new SParametresRefondeSeiche();
    if (tpModele==RefondeModeleCalcul.MODELE_SEICHE)
    {
     seiche.nbValPropres=mdlCal.nbValPropres();
      seiche.nbMaxIterations=mdlCal.nbIterMax();
      seiche.decalageValPropres=mdlCal.decalValPropres();
      seiche.precisionConvergence=mdlCal.precision();
    }
    if (tpModele==RefondeModeleCalcul.MODELE_SEICHE) {
      TIntArrayList vnum=new TIntArrayList();

      // Recherche des noeuds de bords ouverts.
      for (int i=0; i<scEls.size(); i++) {
        GrNoeud[] nds=((GrElement)scEls.get(i)).noeuds_;
        for (int j=0; j<nds.length; j++) {
          int num=((RefondeNoeudData)nds[j].data()).numero;
          if (vnum.size()==0 || (vnum.get(vnum.size()-1)!=num)) vnum.add(num);
        }
      }
      // La liste se termine par -1
      vnum.add(-1);

      // Stockage des noeuds de bords ouverts
      seiche.noeudsBord=vnum.toNativeArray();
    }
    // Modele de houle : Les structures idl ne doivent pas être nulles.
    else {
      seiche.noeudsBord=new int[0];

    }
    _params.seiche(seiche);
    seiche=null;


    //--------------------------------------------------------------------------
    //---  Bloc 'SOLC'  --------------------------------------------------------
    //--------------------------------------------------------------------------
    // Bloc supprimé du fichier .inp le 04/02/03

/*    {
      // Calcul itératif de la longueur d'onde
      double onde1=0;
      double constante=(9.81*mdlCal.periodeHoule()*mdlCal.periodeHoule())/
                       (2.*Math.PI);
      double epsilon=1.e-10;
      double norme=1;
      double onde0=constante;
//      double dist=mdlCal.hauteurMer()+mdlCal.profondeurOrigine();
      double dist=1;

      if (dist<=0)
       throw new IllegalArgumentException(
        "Impossible de calculer la longueur d'onde\n"+
        "Modifiez la hauteur de mer");

      while (norme>epsilon) {
        onde1=constante*DoubleMath.tanh(2*Math.PI*dist/onde0);
        onde1=(onde1+onde0)/2;
        if (onde1==0) break;
        norme=Math.abs((onde1-onde0)/onde1);
        onde0=onde1;
      }
      _params.longueurOnde=onde1;
    } */

    //--------------------------------------------------------------------------
    //---  Bloc 'PORO'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
    {
      base.fondsPoreux= mdlCal.isFondsPoreux();

      if (mdlCal.isFondsPoreux()) {
        dmps= mdlPrp.getDomainesPoreux();

        // Interpolation pour les points du plan des domaines
        FudaaInterpolateurMaillage it= null;

        for (int i= 0; i < dmps.length; i++) {

          // Calcul des z des points de définition des domaines poreux à
          // z automatique
          GrPoint[] ptsPlan= dmps[i].getPointsPlan();

          for (int j= 0; j < ptsPlan.length; j++) {
            if (dmps[i].isZAutomatique(j)) {
              // Initialisé uniquement si nécessaire
              if (it == null) it= new FudaaInterpolateurMaillage(mai);
              ptsPlan[j]= it.interpolePoint(ptsPlan[j]);
            }
          }

          // Réinitialisation du plan avec Z correct.
          dmps[i].setPointsPlan(ptsPlan);

        }

        // Recherche des noeuds concernés

        //GrNoeud[] nds= mai.noeuds();
        List[] vnoeuds= new List[dmps.length];
        TDoubleArrayList[] vepaisseurs= new TDoubleArrayList[dmps.length];
        int nbPoreux= 0;
        int nbNoeud=mai.getNbNoeud();

        // Pour chaque noeud
        for (int i= 0; i < nbNoeud; i++) {
          // Pour chaque domaine
          for (int j= 0; j < dmps.length; j++) {
            GrNoeud ndi=mai.noeud(i);
            // Le noeud est dans les limites du domaine
            if (dmps[j].contientXY(ndi.point_)) {
              double zplan= dmps[j].calculeZ(ndi.point_);
              double epaisseur= zplan - ndi.point_.z_;

              // Ajout du noeud pour ce domaine
              if (epaisseur > 0) {
                if (vnoeuds[j] == null) {
                  nbPoreux++;
                  vnoeuds[j]= new ArrayList();
                  vepaisseurs[j]= new TDoubleArrayList();
                }
                vnoeuds[j].add(ndi);
                vepaisseurs[j].add(epaisseur);
              }
              break;
            }
          }
        }

        // Transfert des informations
        SParametresRefondeDomainePoreux[] rdmps=
            new SParametresRefondeDomainePoreux[nbPoreux];
        nbPoreux=0;

        for (int i=0; i<dmps.length; i++) {
          if (vnoeuds[i]==null) continue;
          int[] numNds=new int[vnoeuds[i].size()];

          for (int j=0; j<numNds.length; j++)
            numNds[j]=
                ((RefondeNoeudData)((GrNoeud)vnoeuds[i].get(j)).data()).
                numero;

          double[] epaisseurs=new double[vepaisseurs[i].size()];

          for (int j=0; j<epaisseurs.length; j++)
            epaisseurs[j]=vepaisseurs[i].get(j);

          rdmps[nbPoreux]=
              new SParametresRefondeDomainePoreux(
              dmps[i].porosite_,
              dmps[i].coefFrottement_,
              dmps[i].permeabilite_,
              dmps[i].coefMasse_,
              numNds,
              epaisseurs);
          nbPoreux++;
        }
        _params.domainePoreux(rdmps);
        rdmps=null;
      }
    }

    // DEBUG Fred il y a un prob ici : les structures idl ne doivent
    //jamais etre nulles
    if (_params.domainePoreux()==null) {
      //lors de l'ecriture des parametres ce tableau n'est pas ecrit
      //si le boolean fondPoreux est a false. Donc, creer un tableau
      //vide ne devrait pas avoir d'incidence
      //Verif dans DParametresRefonde
      _params.domainePoreux(new SParametresRefondeDomainePoreux[0]);
    }
    //DEBUG FIN

    //--------------------------------------------------------------------------
    //---  Bloc 'ONDE'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    {
      base.penteFonds= 1;
      base.profondeurOrigine= 0; // Inutile si pente fond=1
      base.hauteurMer= mdlCal.hauteurMer();
      base.hauteurHoule= mdlCal.hauteurHoule();
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'LINT'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
    {
//      String fsol;
      base.nbIterationAngleFrontiere= 1; // Pas utilisé par le logiciel
//      fsol=_projet.getFichierProjet().getName();
//      fsol=fsol.substring(0,fsol.lastIndexOf(".prf"));
//      _params.fichierSolution=fsol+".sol";
    }

    //--------------------------------------------------------------------------
    //---  Bloc 'VALP'  --------------------------------------------------------
    //--------------------------------------------------------------------------

    _params.donneesBase(base);
  }

  /**
     * Transfert du projet dans un format des parametres INP serveur.
     *
     * @param _params La structure contenant les informations en format serveur.
     * @param _projet Le projet.
     */
    private static void versParametresRefondeINP(SParametresRefondeINP _params,
      RefondeProjet _projet) {

      GrMaillageElement mai;
      GrMaillageElement[] mails;
      Vector cntrs;
      RefondeModeleProprietes mdlPrp;
      RefondeModeleCalcul mdlCal;
      RefondeGeometrie geo;
      RefondeDomainePoreux[] dmps;

      Hashtable hconns= new Hashtable(); // Table des connectivités
      Hashtable htypes= new Hashtable(); // Table des types d'éléments
      Hashtable hngpes= new Hashtable(); // Table des numéros de groupe
      Vector vgpes= new Vector();        // Table des groupes

      int numEle;
      boolean bspel; // Présence de super élément (formulations analytiques)
      Vector scEls=new Vector(); // Stockage des arètes pour les frontières ouvertes seiches

      mdlPrp= _projet.getModeleProprietes();
      mdlCal= _projet.getModeleCalcul();
      int tpModele=mdlCal.typeModele();

      //--------------------------------------------------------------------------
      //---  Controles  ----------------------------------------------------------
      //--------------------------------------------------------------------------

      // Controle qu'il y a un maillage sur tous les domaines
      if (!_projet.estEntierementMaille())
        throw new IllegalArgumentException("Le projet n'est pas totalement maillé");
      mails= RefondeMaillage.getMaillages(_projet);
      mai= RefondeMaillage.creeSuperMaillage(mails);

      // Controle que l'optimisation a bien été effectuée, cad que nbNoe=numMaxNoe
      // et nbEle=numMaxEle
      if (!RefondeMaillage.estOptimise(mai))
        throw new IllegalArgumentException("Le maillage n'est pas optimisé");
      cntrs= _projet.getGeometrie().frontieres();
      geo= _projet.getGeometrie();

      //---  Controles spécifiques à la houle  -----------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {
        // Controle que la hauteur de houle > 0.
        if ((mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_REG&&mdlCal.periodeHoule()<=0)
            ||(mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_ALEA
               &&mdlCal.periodeHouleMini()<=0))
          throw new IllegalArgumentException("La période de houle doit être > 0.");

        // Controle que la bathymétrie en chaque point+hauteur de mer est bien
        // positive
        {
          GrNoeud[] nds=mai.noeuds();
          for (int i=0; i<nds.length; i++)
            if (nds[i].point_.z_+mdlCal.hauteurMer()<=0)
              throw new IllegalArgumentException(
                  "La hauteur de mer doit être "+"supérieure à la bathymétrie");
        }

        // Si le nombre maxi de noeuds/éléments de Refonde est atteint, message
        if (mai.elements().length>RefondePreferences.REFONDE.nbMaxElements() ||
            mai.noeuds().length>RefondePreferences.REFONDE.nbMaxNoeuds())
          throw new IllegalArgumentException(
              "Les limites de maillage de Refonde sont atteintes.\n"
              +"Vous ne pouvez pas passer de calcul.\n"
              +"Remaillez avec un raffinement moindre ou modifiez Refonde.");

        // Contrôle que les points de plan des domaines poreux sont correctement
        // renseignés en Z si prise en compte de la porosité
        if (mdlCal.isFondsPoreux()) {
          dmps=mdlPrp.getDomainesPoreux();
          for (int i=0; i<dmps.length; i++) {
            GrPoint[] ptsPlan=dmps[i].getPointsPlan();
            for (int j=0; j<ptsPlan.length; j++) {
              if (dmps[i].isZAutomatique(j)&&!geo.contient(ptsPlan[j]))
                throw new IllegalArgumentException(
                    "Un point du plan d'un domaine poreux est\n"
                    +"hors géométrie et non renseigné en z.");
            }
          }
        }
      }

      //--------------------------------------------------------------------------
      //---  Informations élémentaires  ------------------------------------------
      //--------------------------------------------------------------------------

      {
        Vector spEls=new Vector(); // Stockage des arètes pour le super-élément
        double[] spGpe= null; // Groupe de propriétés pour le super élément

        if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {

          //---  Elements de digues perforées  -----------------------------------

          {
            Vector vdms=geo.scene_.getDomaines();
            for (int i=0; i<vdms.size(); i++) {
              if (!(vdms.get(i)instanceof RefondeDomaineDigue))
                continue;
              GrElement[] els;
              RefondeDomaineDigue dm=(RefondeDomaineDigue)vdms.get(i);
              RefondeGroupeProprietes gp=dm.getGroupeProprietes();
              RefondePropriete[] prs=gp.getProprietes();
              if (gp.getType()
                  !=RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE)
                continue;
              els=dm.getMaillage().elements();
              // Connectivités
              for (int j=0; j<els.length; j++) {
                GrNoeud[] ndsEle=els[j].noeuds_;
                int[] conn=new int[ndsEle.length];
                int num=((RefondeElementData)els[j].data()).numero;
                for (int k=0; k<ndsEle.length; k++)
                  conn[k]=((RefondeNoeudData)ndsEle[k].data()).numero;
                hconns.put(new Integer(num), conn);
              }
              // Types
              Integer type=new Integer(6);
              for (int j=0; j<els.length; j++) {
                int num=((RefondeElementData)els[j].data()).numero;
                htypes.put(new Integer(num), type);
              }
              // Groupe de propriétés
              double[] gpe=new double[7];
              for (int k=0; k<7; k++)
                gpe[k]=0;
                //... Initialisation de pertes de charge (partie réelle)
              gpe[0]=prs[0].getValeur();
              //... Initialisation de pertes de charge (partie imaginaire)
              gpe[1]=prs[1].getValeur();
              //... Contraction
              gpe[2]=prs[3].getValeur();
              //... Porosité
              gpe[3]=prs[2].getValeur();
              //... Intégration verticale
              gpe[4]=prs[4].getValeur();
              // Numéros de groupes de propriété=1;
              Integer ngpe=new Integer(getNumGpe(vgpes, gpe));
              for (int j=0; j<els.length; j++) {
                int num=((RefondeElementData)els[j].data()).numero;
                hngpes.put(new Integer(num), ngpe);
              }
            }
          }

          //---  Elements de digues transmissibles  ------------------------------

          {
            // Polyligne vers angle d'incidence et vers angle de transmission
            RefondeAngle[] ais=mdlCal.getAnglesIncidenceDigues();
            RefondeAngle[] ats=mdlCal.getAnglesTransmissionDigues();
            Hashtable pl2Ai=new Hashtable(ais.length);
            Hashtable pl2At=new Hashtable(ats.length);
            for (int i=0; i<ais.length; i++)
              pl2Ai.put(mdlCal.getPolyligneAI(ais[i]), ais[i]);
            for (int i=0; i<ats.length; i++)
              pl2At.put(mdlCal.getPolyligneAT(ats[i]), ats[i]);
            Vector vdms=geo.scene_.getDomaines();
            for (int i=0; i<vdms.size(); i++) {
              if (!(vdms.get(i)instanceof RefondeDomaineDigue))
                continue;
              GrElement[] els;
              RefondeDomaineDigue dm=(RefondeDomaineDigue)vdms.get(i);
              RefondeGroupeProprietes gp=dm.getGroupeProprietes();
              RefondePropriete[] prs=gp.getProprietes();
              if (gp.getType()
                  !=RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
                continue;
              els=dm.getMaillage().elements();

              // Recherche de la correspondance élément->polyligne d1,
              // élément->polyligne d2
              RefondeContour ct=dm.getContour();
              RefondePolyligne[] pls=ct.getPolylignes();
              Hashtable el2Pl[]=
                  new Hashtable[] {new Hashtable(), new Hashtable()};
              for (int j=0; j<pls.length; j++) {
                if (pls[j]==dm.getExtremiteDigue()
                    ||pls[j]==dm.getExtremiteFinDigue())
                  continue;
                GrNoeud[] ndsPl=pls[j].getNoeuds();
                NEXT_ARETE:for (int k=0; k<ndsPl.length-1; k++) {
                  for (int l=0; l<els.length; l++) {
                    GrNoeud[] ndsEle=els[l].noeuds_;
                    for (int m=0; m<2; m++) {
                      if ((ndsEle[m*2]==ndsPl[k]
                           &&ndsEle[m*2+1]==ndsPl[k+1])
                          ||(ndsEle[m*2]==ndsPl[k+1]
                             &&ndsEle[m*2+1]==ndsPl[k])) {
                        el2Pl[m].put(els[l], pls[j]);
                        continue NEXT_ARETE;
                      }
                    }
                  }

                  // Aucun élément trouvé pour la correspondance
                  throw new IllegalArgumentException(
                      "Problème de transfert "
                      +"des éléments de digues transmissibles");
                }
              }

              // Connectivités
              for (int j=0; j<els.length; j++) {
                GrNoeud[] ndsEle=els[j].noeuds_;
                int[] conn=new int[ndsEle.length];
                int num=((RefondeElementData)els[j].data()).numero;
                for (int k=0; k<ndsEle.length; k++)
                  conn[k]=((RefondeNoeudData)ndsEle[k].data()).numero;
                hconns.put(new Integer(num), conn);
              }

              // Types
              Integer type=new Integer(5);
              for (int j=0; j<els.length; j++) {
                int num=((RefondeElementData)els[j].data()).numero;
                htypes.put(new Integer(num), type);
              }

              for (int j=0; j<els.length; j++) {

                // Groupe de propriétés
                double[] gpe=new double[11];
                for (int k=0; k<gpe.length; k++)
                  gpe[k]=0;
                RefondeAngle ai1;
                RefondeAngle at1;
                RefondeAngle ai2;
                RefondeAngle at2;
                ai1=(RefondeAngle)pl2Ai.get(el2Pl[0].get(els[j]));
                at1=(RefondeAngle)pl2At.get(el2Pl[0].get(els[j]));
                ai2=(RefondeAngle)pl2Ai.get(el2Pl[1].get(els[j]));
                at2=(RefondeAngle)pl2At.get(el2Pl[1].get(els[j]));
                //... Code d'angle
                int code1=0;
                int code2=0;
                if (ai1.getType()==RefondeAngle.ABSOLU
                    &&at2.getType()==RefondeAngle.ABSOLU)
                  code1=9;
                if (ai1.getType()==RefondeAngle.RELATIF
                    &&at2.getType()==RefondeAngle.RELATIF)
                  code1=7;
                if (ai2.getType()==RefondeAngle.ABSOLU
                    &&at1.getType()==RefondeAngle.ABSOLU)
                  code2=9;
                if (ai2.getType()==RefondeAngle.RELATIF
                    &&at1.getType()==RefondeAngle.RELATIF)
                  code2=7;
                if (code1==0||code2==0)
                  throw new IllegalArgumentException(
                      "Les angles associés de houle incidente et transmise à travers\n"
                      +"les digues transmissibles doivent être de même type "
                      +"(Relatif ou Absolu)");
                gpe[0]=code1*10+code2;
                //... Angle incidence domaine 1
                gpe[1]=code1==9?math2Navig(ai1.getAngle()):ai1.getAngle();
                //... Angle transmission domaine 2
                gpe[2]=
                    code1==9?math2Navig(at2.getAngle()):at2.getAngle()+180;
                // +180 en relatif ?
                //... Angle incidence domaine 2
                gpe[3]=code2==9?math2Navig(ai2.getAngle()):ai2.getAngle();
                //... Angle transmission domaine 1
                gpe[4]=
                    code2==9?math2Navig(at1.getAngle()):at1.getAngle()+180;
                // +180 en relatif ?
                //... Coefficient réflexion domaine 1
                gpe[5]=prs[0].getValeur();
                //... Coefficient réflexion domaine 2
                gpe[6]=gpe[5];
                //... Coefficient transmission domaine 1
                gpe[7]=prs[1].getValeur();
                //... Coefficient transmission domaine 2
                gpe[8]=gpe[7];
                //... Déphasage réflexion
                gpe[9]=prs[2].getValeur();
                //... Déphasage transmission
                gpe[10]=prs[3].getValeur();
                // Numéro de groupe
                Integer ngpe=new Integer(getNumGpe(vgpes, gpe));
                int num=((RefondeElementData)els[j].data()).numero;
                hngpes.put(new Integer(num), ngpe);
              }
            }
          }
        }

        //---  Eléments de fond  -------------------------------------------------

        {
          Vector vdms= geo.scene_.getDomaines();

          for (int i= 0; i < vdms.size(); i++) {
            if (!(vdms.get(i) instanceof RefondeDomaineFond)) continue;

            GrElement[] els;
            RefondeDomaineFond dm= (RefondeDomaineFond)vdms.get(i);
            // RefondeGroupeProprietes gp=dm.getGroupeProprietes();
            els= dm.getMaillage().elements();

            // Connectivités
            for (int j= 0; j < els.length; j++) {
              GrNoeud[] ndsEle= els[j].noeuds_;
              int[] conn= new int[ndsEle.length];
              int num= ((RefondeElementData)els[j].data()).numero;
              for (int k= 0; k < ndsEle.length; k++)
                conn[k]= ((RefondeNoeudData)ndsEle[k].data()).numero;
              hconns.put(new Integer(num), conn);
            }

            // Types
            Integer type;
            if (tpModele==RefondeModeleCalcul.MODELE_HOULE && mdlCal.isFondsPoreux())
              type=new Integer(21);
            else
              type=new Integer(1);

            for (int j= 0; j < els.length; j++) {
              int num= ((RefondeElementData)els[j].data()).numero;
              htypes.put(new Integer(num), type);
            }
            // 1 groupe bidon pour tous les éléments
            double[] gpe= new double[7];
            for (int k= 0; k < 7; k++)
              gpe[k]= 0;
            // Numéros de groupes de propriété=1;
            Integer ngpe= new Integer(getNumGpe(vgpes, gpe));
            for (int j= 0; j < els.length; j++) {
              int num= ((RefondeElementData)els[j].data()).numero;
              hngpes.put(new Integer(num), ngpe);
            }
          }
        }

        //---  Eléments du bord  -------------------------------------------------

        {
          final int OUV_SOR=RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE;
          //final int OUV_ENT=RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE;
          final int SEM_REF=RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT;
          final int SCH_OUV=RefondeGroupeProprietes.SEICHE_BORD_OUVERT;

          class Segment {
            RefondeGroupeProprietes g= null;
            RefondeAngle a= null;
            GrElement e= null;
            double s= -1.;
          }

          numEle= hconns.size();
          GrElement[][] elss= mai.aretesContours();
          if (elss.length != cntrs.size()) {
            System.out.println("nb cntrs geometrie: " + cntrs.size());
            System.out.println("nb cntrs maillage : " + elss.length);
            throw new IllegalArgumentException(
              "Problème de reconnaissance " + "géométrie<->maillage");
          }

          bspel= false;

          // Boucle sur les polylignes frontières
          for (int i= 0; i < cntrs.size(); i++) {
            GrPolyligne plTot;
            RefondePolyligne pl;
            double sDeb;
            double s;
            double dstMin;
            GrPoint pt;
            Vector sgs;
            int isgs;
            Vector ais;
            // int ngpe;
            Segment sgc= null;
            double lgSeg;
            RefondeAngle ai;
            Vector cntr= (Vector)cntrs.get(i);
            pt= ((RefondePolyligne)cntr.get(0)).sommet(0);

            // Création d'une polyligne globale
            plTot= new GrPolyligne();
            for (int j= 0; j < cntr.size(); j++) {
              pl= (RefondePolyligne)cntr.get(j);
              pt= pl.sommet(0);
              plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0));
            }
            plTot.sommets_.ajoute(plTot.sommet(0));

            // Recherche du début de bord "élément" correspondant au début de bord
            // "géométrie"
            int ncntr= -1;
            int narete= -1;
            dstMin= Double.MAX_VALUE;
            pt= ((RefondePolyligne)cntr.get(0)).sommet(0);

            NEXTCNTR : for (int j= 0; j < elss.length; j++) {
              double dst;
              for (int k= 0; k < elss[j].length; k++) {
                dst= pt.distanceXY(elss[j][k].noeuds_[0].point_);
                if (dst < dstMin) {
                  ncntr= j;
                  narete= k;
                  dstMin= dst;
                  if (dstMin == 0)
                    break NEXTCNTR;
                }
              }
            }

            sgs= new Vector();

            // Segmentation par les groupes de propriétés
            for (int j= 0; j < cntr.size(); j++) {
              RefondeGroupeProprietes gpe;
              Segment sg= new Segment();
              pl= geo.polyligne((RefondePolyligne)cntr.get(j));
              gpe= pl.getGroupeProprietes();
              s= plTot.abscisseDe(j + 1);
              sg.g= gpe;
              sg.s= s;
              sgs.add(sg);
            }

            // Segmentation par les éléments
            isgs= 0;
            for (int j= 0; j < elss[ncntr].length; j++) {
              Segment sg= new Segment();
              GrElement e;
              e= elss[ncntr][(j + narete) % elss[ncntr].length];
              pt= e.noeuds_[e.noeuds_.length - 1].point_;
              s= plTot.abscisseDe(new GrPoint(pt.x_, pt.y_, 0.));
              if (j == elss[ncntr].length - 1)
                s= 1;

              // Pour les problemes d'arrondis et de point initial/fin identiques
              while (isgs < sgs.size() && (sgc= (Segment)sgs.get(isgs)).s < s) {
                sgc.e= e;
                isgs++;
              }

              // Ajout du segment
              if (s < sgc.s) {
                sg.e= e;
                sg.s= s;
                sg.g= sgc.g;
                sgs.add(isgs, sg);
                isgs++;
              }

              // Pas d'ajout du segment, mais initialisation de l'ancien segment
              else if (s == sgc.s) {
                sgc.e= e;
                isgs++;
              }
            }

            // Segmentation par les angles d'incidence
            ais= mdlCal.angles()[i];
  //          int nangle=-1;
  //          int nais=ais.size();
  //          for (int j=0; j<ais.size(); j++) {
  //            ai=(RefondeAngle)ais.get(j);
  //            if (ai.getSFin()<ai.getSDebut()) {
  //              nangle=j;
  //              if (ai.getSFin()==0) nangle++;
  //              else                 nais++;
  //              break;
  //            }
  //          }
            isgs= 0;
            for (int j= 0; j < ais.size(); j++) {
  //          for (int j=0; j<nais; j++) {
              Segment sg= new Segment();
              ai= (RefondeAngle)ais.get(j);
  //            ai=(RefondeAngle)ais.get((j+nangle)%ais.size());
              s= ai.getSFin();
  //            System.out.println("s: "+s);
  //            if (j==nais-1) s=1;

              // Recherche du segment sgc pour lequel sgc.s>=s
              while (isgs < sgs.size() && (sgc= (Segment)sgs.get(isgs)).s < s) {
                sgc.a= ai;
                isgs++;
              }

              // Ajout du segment
              if (s < sgc.s) {
                sg.a= ai;
                sg.s= s;
                sg.g= sgc.g;
                sg.e= sgc.e;
                sgs.add(isgs, sg);
                isgs++;
              }

              // Pas d'ajout du segment, mais initialisation de l'ancien segment
              else if (s == sgc.s) {
                sgc.a= ai;
                isgs++;
              }
            }
  //          for (int j=0; j<sgs.size(); j++)
  //           if (((Segment)sgs.get(j)).a==null) System.out.println("null");

            // Stockage des éléments de bord, des types, des propriétés, des angles

            // Pour les angles, l'angle retenu est celui le plus représenté sur
            // l'élément
            sDeb= 0;
            lgSeg= -Double.MAX_VALUE;
            ai= null;
  //          sgc=(Segment)sgs.get(0);
  //          ai=sgc.a;
  //          Segment sgSuiv=(Segment)sgs.get(1);

            for (int j= 0; j < sgs.size(); j++) {
              Segment sg= (Segment)sgs.get(j);

              // Recherche de l'angle le plus significatif
              if (sg.s - sDeb > lgSeg) {
                lgSeg= sg.s - sDeb;
                ai= sg.a;
              }

              sDeb= sg.s;

              // Changement d'élément => Stockage
              if (j == sgs.size() - 1 || ((Segment)sgs.get(j + 1)).e != sg.e) {
                // Type
                int type;
                if (tpModele==RefondeModeleCalcul.MODELE_HOULE) {
                  if (sg.g.getType()==OUV_SOR)
                    type=2;
                  else if (sg.g.getType()==SEM_REF)
                    type=3;
                  else
                    type=4+mdlCal.casBordOuvert()*10;
                }
                else {
                  type=13; // Pour tous les éléments de frontière modèle de seiche
                }

                // Groupe de propriétés
                double[] gpe= new double[7];
                for (int k= 0; k < 7; k++)
                  gpe[k]= 0;

                //... Pour une frontière ouverte en sortie ou semi refléchissante
                if (type == 2 || type == 3) {
                  if (ai.getType() == RefondeAngle.RELATIF) {
                    gpe[0]= 7;
                    gpe[5]= ai.getAngle();
                  } else if (ai.getType() == RefondeAngle.DIFFRACTE) {
                    gpe[0]= 8;
                    gpe[2]= ai.getPointDiffraction().x_;
                    gpe[3]= ai.getPointDiffraction().y_;
                  } else {
                    gpe[0]= 9;
                    gpe[5]= math2Navig(ai.getAngle());
                  }
                  if (type == 3)
                    gpe[4]= sg.g.getProprietes()[0].getValeur();
                }

                //... Pour une frontière modèle seiche
                //    On crée un grp bidon pour qu'il y ai bien 2 groupes (1 pour
                //    le fond, 1 pour les bord).
                else if (type==13) {
                    gpe[0]=1; // Pour différencier de celui de fond.
                }

                //... Pour une frontière ouverte en entrée
                else {
                  if (ai.getType() == RefondeAngle.ABSOLU) {
                    //... Alpha inc., Theta sup.
                    if (ai.getTypeSup() == RefondeAngle.RELATIF) {
                      gpe[0]= 4;
                      gpe[5]= math2Navig(ai.getAngle());
                      gpe[6]= ai.getAngleSup();
                    }
                    //.. Alpha inc., Alpha sup.
                    else {
                      gpe[0]= 9;
                      gpe[5]= math2Navig(ai.getAngle());
                      gpe[6]= math2Navig(ai.getAngleSup());
                    }
                  }

                  //... Les autres combinaisons sont interdites
                  else {
                    throw new IllegalArgumentException(
                      "Une frontière ouverte doit avoir un angle d'incidence\n"
                        + "défini de manière absolue");
                  }
                }

                // Frontières ouvertes modèle seiche : On stocke les éléments de
                // bord pour récupérer les noeuds plus tard.
                if (type==13 && sg.g.getType()==SCH_OUV) {
                  scEls.add(sg.e);
                }

                // Recherche d'un groupe de propriétés équivalent

                // Cas d'une frontière ouverte en super élément (formulations analytiques)
                if (type == 14 || type == 24) {
                  bspel= true;
                  spGpe= gpe;
  //                //... Ajout de 180° à l'angle d'incidence
  //                spGpe[5]=(spGpe[5]+180)%360;
                  spEls.add(sg.e);
                }

                // Cas général
                else {
                  numEle++;
  /*                NGPE : {
                    NEXT_GPE : for (int k=0; k<vgpes.size(); k++) {
                      double[] gpec=(double[])vgpes.get(k);
                      for (int l=0; l<7; l++)
                       if (gpec[l]!=gpe[l]) continue NEXT_GPE;

                      ngpe=k;
                      break NGPE;
                    }

                    ngpe=vgpes.size();
                    vgpes.add(gpe);
                  }*/

                  // Connectivité
                  GrNoeud[] ndsEle= sg.e.noeuds_;
                  int[] conn= new int[ndsEle.length];

                  for (int k= 0; k < ndsEle.length; k++)
                    conn[k]= ((RefondeNoeudData)ndsEle[k].data()).numero;
                  hconns.put(new Integer(numEle), conn);

                  // Type
                  htypes.put(new Integer(numEle), new Integer(type));

                  // Groupe + Numéro de groupe
                  Integer ngpe= new Integer(getNumGpe(vgpes, gpe));
                  hngpes.put(new Integer(numEle), ngpe);
                }

                lgSeg= -Double.MAX_VALUE;
  //              ai=sgc.a;
  //              sgc=sg;
              }
            }
          }
        }

        //---  Transfert des informations élémentaires  --------------------------

        if (bspel) {
          Vector vconn= new Vector();
  //        Vector tmp=new Vector();
          GrNoeud[] ndsEle= null;
          int numNoe;
          numEle++;

          for (int i= 0; i < spEls.size(); i++) {
            ndsEle= ((GrElement)spEls.get(i)).noeuds_;

            for (int j= 0; j < ndsEle.length - 1; j++) {
              numNoe= ((RefondeNoeudData)ndsEle[j].data()).numero;
              vconn.add(new Integer(numNoe));
  //           tmp.add((Integer)nd2Num.get(ndsEle[j]));
            }
          }
          numNoe= ((RefondeNoeudData)ndsEle[ndsEle.length - 1].data()).numero;

          if (((Integer)vconn.get(0)).intValue() != numNoe)
            vconn.add(new Integer(numNoe));
  //        Integer numNd=(Integer)nd2Num.get(ndsEle[ndsEle.length-1]);
  //        if (!((Integer)tmp.get(0)).equals(numNd)) tmp.add(numNd);

          int[] conn= new int[vconn.size()];
          for (int i= 0; i < vconn.size(); i++)
            conn[i]= ((Integer)vconn.get(i)).intValue();

          // Connectivité
          hconns.put(new Integer(numEle), conn);

          // Type
          htypes.put(
            new Integer(numEle),
            new Integer(4 + mdlCal.casBordOuvert() * 10));

          // Groupe + numéro
          Integer ngpe= new Integer(getNumGpe(vgpes, spGpe));
          hngpes.put(new Integer(numEle), ngpe);
        }
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'SPEC'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
      {
        // Déferlement
        _params.deferlement= mdlCal.deferHoule();
        _params.formuleDeferlement= mdlCal.formuleDeferHoule();

        // Nombre de période de houle
        _params.nbPeriodesHoule= mdlCal.nbPeriodesHoule();

        // Périodes
        _params.periodeHoule= mdlCal.periodeHoule();
        _params.periodeHouleMin= mdlCal.periodeHouleMini();
        _params.periodeHouleMax= mdlCal.periodeHouleMaxi();

        // Nombre de directions de houle
        _params.nbAnglesHoule= mdlCal.nbAnglesHoule();

        // Directions
        // Important B.M. : Les angles utilisateurs tournant dans le sens inverse
        // des angles internes, les angles mini doivent être les angles maxi et
        // inversement.
        _params.angleHoule= math2Navig(mdlCal.angleHoule());
        _params.angleHouleMin= math2Navig(mdlCal.angleHouleMaxi());
        _params.angleHouleMax= math2Navig(mdlCal.angleHouleMini());
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'COOR'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      {
        GrNoeud[] nds= mai.noeuds();
        _params.nbNoeuds= nds.length;
        _params.xyz= new SParametresRefondeLigneXYZ[nds.length];

        for (int i= 0; i < nds.length; i++) {
          int numNoe= ((RefondeNoeudData)nds[i].data()).numero;
          GrPoint p= nds[i].point_;
          _params.xyz[numNoe - 1]= new SParametresRefondeLigneXYZ(p.x_, p.y_, p.z_);
        }
      }

      //------------------------------------------------------------------------
      //---  Bloc 'ELEM'  ------------------------------------------------------
      //------------------------------------------------------------------------

      {
        _params.nbElements= hconns.size();

        // Nombre max de noeuds par élément / nombre de noeuds du super élément
        _params.nbNoeudsSuperElement= 0;
        _params.nbMaxNoeudsElement= 0;

        for (int i= 0; i < hconns.size(); i++) {
          if (bspel && i == hconns.size() - 1) {
            _params.nbNoeudsSuperElement=
              ((int[])hconns.get(new Integer(i + 1))).length;
          }
          else {
            _params.nbMaxNoeudsElement=
              Math.max(
                _params.nbMaxNoeudsElement,
                ((int[])hconns.get(new Integer(i + 1))).length);
          }
        }

        // Nombre max de PE par groupe
        _params.nbMaxPEGroupe= 0;

        for (int i= 0; i < vgpes.size(); i++)
          _params.nbMaxPEGroupe=
            Math.max(_params.nbMaxPEGroupe, ((double[])vgpes.get(i)).length);

        _params.nbGroupesPE= vgpes.size();
        _params.ordreMax= mdlCal.ordreMax();
        _params.casBordOuvert= (bspel ? 2 : 1);
        _params.connectivites= new SParametresRefondeConnectivite[hconns.size()];

        for (int i= 0; i < hconns.size(); i++)
          _params.connectivites[i]=
            new SParametresRefondeConnectivite(
              (int[])hconns.get(new Integer(i + 1)));
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'TYPE'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      {
        _params.type= new int[htypes.size()];

        for (int i= 0; i < htypes.size(); i++)
          _params.type[i]= ((Integer)htypes.get(new Integer(i + 1))).intValue();
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'PREL'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      {
        _params.groupePE= new int[hngpes.size()];

        for (int i= 0; i < hngpes.size(); i++)
          _params.groupePE[i]=
            ((Integer)hngpes.get(new Integer(i + 1))).intValue();

        _params.valeurPE= new SParametresRefondeLignePE[vgpes.size()];

        for (int i= 0; i < vgpes.size(); i++) {
          double[] gpe= (double[])vgpes.get(i);
          _params.valeurPE[i]= new SParametresRefondeLignePE(gpe);
        }
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'COND'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_SEICHE) {
        Vector vnum=new Vector();

        // Recherche des noeuds de bords ouverts.
        for (int i=0; i<scEls.size(); i++) {
          GrNoeud[] nds=((GrElement)scEls.get(i)).noeuds_;
          for (int j=0; j<nds.length; j++) {
            Integer num=new Integer(((RefondeNoeudData)nds[j].data()).numero);
            if (vnum.size()==0 || !vnum.get(vnum.size()-1).equals(num)) vnum.add(num);
          }
        }
        // La liste se termine par -1
        vnum.add(new Integer(-1));

        // Stockage des noeuds de bords ouverts
        _params.seiche=new SParametresRefondeSeiche();
        _params.seiche.noeudsBord=new int[vnum.size()];
        for (int i=0; i<vnum.size(); i++)
          _params.seiche.noeudsBord[i]=((Integer)vnum.get(i)).intValue();
      }

      // Modele de houle : Les structures idl ne doivent pas être nulles.
      else {
        _params.seiche=new SParametresRefondeSeiche();
        _params.seiche.noeudsBord=new int[0];
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'SOLC'  --------------------------------------------------------
      //--------------------------------------------------------------------------
      // Bloc supprimé du fichier .inp le 04/02/03

  /*    {
        // Calcul itératif de la longueur d'onde
        double onde1=0;
        double constante=(9.81*mdlCal.periodeHoule()*mdlCal.periodeHoule())/
                         (2.*Math.PI);
        double epsilon=1.e-10;
        double norme=1;
        double onde0=constante;
  //      double dist=mdlCal.hauteurMer()+mdlCal.profondeurOrigine();
        double dist=1;

        if (dist<=0)
         throw new IllegalArgumentException(
          "Impossible de calculer la longueur d'onde\n"+
          "Modifiez la hauteur de mer");

        while (norme>epsilon) {
          onde1=constante*DoubleMath.tanh(2*Math.PI*dist/onde0);
          onde1=(onde1+onde0)/2;
          if (onde1==0) break;
          norme=Math.abs((onde1-onde0)/onde1);
          onde0=onde1;
        }
        _params.longueurOnde=onde1;
      } */

      //--------------------------------------------------------------------------
      //---  Bloc 'PORO'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
      {
        _params.fondsPoreux= mdlCal.isFondsPoreux();

        if (mdlCal.isFondsPoreux()) {
          dmps= mdlPrp.getDomainesPoreux();

          // Interpolation pour les points du plan des domaines
          FudaaInterpolateurMaillage it= null;

          for (int i= 0; i < dmps.length; i++) {

            // Calcul des z des points de définition des domaines poreux à
            // z automatique
            GrPoint[] ptsPlan= dmps[i].getPointsPlan();

            for (int j= 0; j < ptsPlan.length; j++) {
              if (dmps[i].isZAutomatique(j)) {
                // Initialisé uniquement si nécessaire
                if (it == null) it= new FudaaInterpolateurMaillage(mai);
                ptsPlan[j]= it.interpolePoint(ptsPlan[j]);
              }
            }

            // Réinitialisation du plan avec Z correct.
            dmps[i].setPointsPlan(ptsPlan);

          }

          // Recherche des noeuds concernés

          GrNoeud[] nds= mai.noeuds();
          Vector[] vnoeuds= new Vector[dmps.length];
          Vector[] vepaisseurs= new Vector[dmps.length];
          int nbPoreux= 0;

          // Pour chaque noeud
          for (int i= 0; i < nds.length; i++) {
            // Pour chaque domaine
            for (int j= 0; j < dmps.length; j++) {

              // Le noeud est dans les limites du domaine
              if (dmps[j].contientXY(nds[i].point_)) {
                double zplan= dmps[j].calculeZ(nds[i].point_);
                double epaisseur= zplan - nds[i].point_.z_;

                // Ajout du noeud pour ce domaine
                if (epaisseur > 0) {
                  if (vnoeuds[j] == null) {
                    nbPoreux++;
                    vnoeuds[j]= new Vector();
                    vepaisseurs[j]= new Vector();
                  }
                  vnoeuds[j].add(nds[i]);
                  vepaisseurs[j].add(new Double(epaisseur));
                }
                break;
              }
            }
          }

          // Transfert des informations
          SParametresRefondeDomainePoreux[] rdmps=
              new SParametresRefondeDomainePoreux[nbPoreux];
          nbPoreux=0;

          for (int i=0; i<dmps.length; i++) {
            if (vnoeuds[i]==null) continue;
            int[] numNds=new int[vnoeuds[i].size()];

            for (int j=0; j<numNds.length; j++)
              numNds[j]=
                  ((RefondeNoeudData)((GrNoeud)vnoeuds[i].get(j)).data()).
                  numero;

            double[] epaisseurs=new double[vepaisseurs[i].size()];

            for (int j=0; j<epaisseurs.length; j++)
              epaisseurs[j]=((Double)vepaisseurs[i].get(j)).doubleValue();

            rdmps[nbPoreux]=
                new SParametresRefondeDomainePoreux(
                dmps[i].porosite_,
                dmps[i].coefFrottement_,
                dmps[i].permeabilite_,
                dmps[i].coefMasse_,
                numNds,
                epaisseurs);
            nbPoreux++;
          }
          _params.domainePoreux= rdmps;
        }
      }

      // DEBUG Fred il y a un prob ici : les structures idl ne doivent
      //jamais etre nulles
      if (_params.domainePoreux==null) {
        //lors de l'ecriture des parametres ce tableau n'est pas ecrit
        //si le boolean fondPoreux est a false. Donc, creer un tableau
        //vide ne devrait pas avoir d'incidence
        //Verif dans DParametresRefonde
        _params.domainePoreux=new SParametresRefondeDomainePoreux[0];
      }
      //DEBUG FIN

      //--------------------------------------------------------------------------
      //---  Bloc 'ONDE'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      {
        _params.penteFonds= 1;
        _params.profondeurOrigine= 0; // Inutile si pente fond=1
        _params.hauteurMer= mdlCal.hauteurMer();
        _params.hauteurHoule= mdlCal.hauteurHoule();
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'LINT'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_HOULE)
      {
  //      String fsol;
        _params.nbIterationAngleFrontiere= 1; // Pas utilisé par le logiciel
  //      fsol=_projet.getFichierProjet().getName();
  //      fsol=fsol.substring(0,fsol.lastIndexOf(".prf"));
  //      _params.fichierSolution=fsol+".sol";
      }

      //--------------------------------------------------------------------------
      //---  Bloc 'VALP'  --------------------------------------------------------
      //--------------------------------------------------------------------------

      if (tpModele==RefondeModeleCalcul.MODELE_SEICHE)
      {
        _params.seiche.nbValPropres=mdlCal.nbValPropres();
        _params.seiche.nbMaxIterations=mdlCal.nbIterMax();
        _params.seiche.decalageValPropres=mdlCal.decalValPropres();
        _params.seiche.precisionConvergence=mdlCal.precision();
      }
    }

  /**
   * Transfert du projet dans un format des parametres SPE serveur.
   *
   * @param _params La structure contenant les informations en format serveur.
   * @param _projet Le projet.
   */
  private static void versParametresRefondeSPE(
    SParametresRefondeSPE _params,
    RefondeProjet _projet) {
    RefondeModeleCalcul mdlCal= _projet.getModeleCalcul();
    // Période de pic
    _params.periodePic= mdlCal.periodeHoule();
    // Facteur de réhaussement du pic.
    _params.rehaussementPic= mdlCal.rehaussementPic();
    // Angle principal de houle.
    _params.anglePrincipal= math2Navig(mdlCal.angleHoule());
    // Répartition angulaire.
    _params.repartitionAngulaire= mdlCal.repartitionAngle();
  }
  /*
   * Retourne le nouveau numéro de groupe de propriété ou un ancien si
   * existant
   */
  private static int getNumGpe(List _vgpes, double[] _gpe) {
    NEXT_GPE : for (int k= 0; k < _vgpes.size(); k++) {
      double[] gpec= (double[])_vgpes.get(k);
      if (gpec.length != _gpe.length)
        continue;
      for (int l= 0; l < gpec.length; l++)
        if (gpec[l] != _gpe[l])
          continue NEXT_GPE;
      return k + 1;
    }
    _vgpes.add(_gpe);
    return _vgpes.size();
  }
  /**
   * Converti un angle avec un 0 à l'Est et dans le sens trigonométrique (math)
   * en un angle avec un 0 au Sud (houle de Nord) et dans le sens horaire
   * (navigation).
   *
   * @return La valeur de l'angle dans l'intervalle [0,360°[.
   */
  private static double math2Navig(double ang) {
    return ((-ang + 270) % 360 + 360) % 360;
  }
}
