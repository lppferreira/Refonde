/*
 * @file         Refonde3D.java
 * @creation     2000-02-18
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.SwingUtilities;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.memoire.bu.BuInformationsDocument;
import com.memoire.bu.BuMainPanel;
import com.memoire.bu.BuTaskOperation;
import com.memoire.fu.FuLib;

import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.ressource.EbliResource;
import org.fudaa.ebli.volume.BGrilleIrreguliere;
import org.fudaa.ebli.volume.BGroupeLumiere;
import org.fudaa.ebli.volume.BGroupeStandard;
import org.fudaa.ebli.volume.BGroupeVolume;
import org.fudaa.ebli.volume.BLumiereDirectionnelle;
import org.fudaa.ebli.volume.EbliFilleVue3D;
/**
 * @version      $Id: Refonde3D.java,v 1.11 2006-09-19 15:10:22 deniger Exp $
 * @author       Christophe Delhorbe
 */
public class Refonde3D {
  RefondeProjet projet_;
  RefondeImplementation refonde;
  public Refonde3D(RefondeProjet _projet, RefondeImplementation _refonde) {
    projet_= _projet;
    refonde= _refonde;
    new BuTaskOperation(refonde, "Création de la vue 3D") {
      public void act() {
        creer3D();
      }
    }
    .start();
  }
  public void creer3D() {
    final BuMainPanel mainpanel= refonde.getMainPanel();
    mainpanel.setMessage("Création de la vue3D...");
    mainpanel.setProgression(0);
    if (projet_.estEntierementMaille()) {
      // On recupere le maillage
      GrMaillageElement maillage=
        RefondeMaillage.creeSuperMaillage(projet_);
      //projet_=null;
      // on recupere les noeuds
      GrNoeud[] noeuds= maillage.noeuds();
      int nbnoeuds= noeuds.length;
      // On recupere les elements
      GrElement[] elements= maillage.elements();
      int nbelements= elements.length;
      int type_element= elements[0].type_;
      // on cree un tableau des connectivités
      int[] connectivite= null;
      int nbconnect= 0;
      int pourcent= 0;
      if (type_element == GrElement.T3) {
        nbconnect= nbelements * 3;
        connectivite= new int[nbconnect];
        for (int i= 0; i < nbelements; i++) {
          for (int j= 0; j < 3; j++) {
            int k= 0;
            while ((!(noeuds[k].equals(elements[i].noeuds_[j])))
              && (k < nbnoeuds)) {
              k++;
            }
            if (k < nbnoeuds)
              connectivite[i * 3 + j]= k;
            else {
              System.out.println(
                "Erreur dans la construction des connectivités: element "
                  + i
                  + ", noeud "
                  + j);
              connectivite[i * 3 + j]= 0;
            }
          }
          if ((((double)i / nbelements) * 90) - pourcent > 1) {
            pourcent++;
            mainpanel.setProgression(pourcent);
          }
        }
      }
      System.out.print("generation des noeuds format Java3D");
      // generation des noeuds format Java3D
      Point3d[] noeuds_j3d= new Point3d[nbnoeuds];
      for (int i= 0; i < nbnoeuds; i++) {
        noeuds_j3d[i]=
          new Point3d(noeuds[i].point_.x_, noeuds[i].point_.y_, noeuds[i].point_.z_);
      }
      System.out.println(" ... OK");
      BGrilleIrreguliere bathy= new BGrilleIrreguliere("Bathymetrie");
      mainpanel.setProgression(98);
      bathy.setBoite(maillage.boite());
      maillage= null;
      bathy.setGeometrie(nbnoeuds, noeuds_j3d, nbconnect, connectivite);
      bathy.setCouleur(new Color(255, 200, 100));
      bathy.setEclairage(true);
      RefondeGrilleIrreguliere sol= null;
      if (projet_.hasResultats()) {
        RefondeResultats res= projet_.getResultats();
        System.out.print("generation des Resultats");

        // generation des noeuds format Java3D
        Point3d[] noeuds_res= new Point3d[nbnoeuds];
        double[] htHoule=res.getColonne(res.indexOfColonne(RefondeResultats.nomResultats[RefondeResultats.HAUTEUR_HOULE]))[0];
        double[] prof=res.getColonne(res.indexOfColonne(RefondeResultats.nomResultats[RefondeResultats.BATHYMETRIE]))[0];
        for (int i= 0; i < nbnoeuds; i++) {
          noeuds_res[i]=
            new Point3d(
              noeuds[i].point_.x_,
              noeuds[i].point_.y_,
              htHoule[i] / 2 + prof[i]);
          //           projet_.getResultats().resultats_.lignes[i].moduleHauteur/2+projet_.getResultats().resultats_.lignes[i].profondeur);
        }
        System.out.println(" ... OK");
        sol= new RefondeGrilleIrreguliere("Hauteur Houle");
        mainpanel.setProgression(99);
        sol.setBoite(bathy.getBoite());
        sol.setGeometrie(nbnoeuds, noeuds_res, nbconnect, connectivite);
        sol.setEclairage(true);
        sol.setTransparence(.5f);
        sol.setCouleur(new Color(100, 100, 255));
        sol.setSourceDonnees(
          new RefondeDataSource(
            projet_.getResultats(),
            projet_.getModeleCalcul().periodeHoule()));
      }
      projet_= null;
      BGroupeVolume imp= new BGroupeVolume();
      imp.setName("Import");
      BGroupeVolume gv= new BGroupeVolume();
      if (sol != null) {
        gv.add(sol);
        sol.setRapide(false);
        sol.setVisible(true);
      }
      gv.add(bathy);
      gv.add(imp);
      bathy.setRapide(false);
      bathy.setVisible(true);
      imp.setVisible(true);
      imp.setRapide(false);
      gv.setName("Volumes");
      System.out.println("Groupe volume lumieres");
      BGroupeLumiere gl= new BGroupeLumiere();
      gl.setName("Lumières");
      BLumiereDirectionnelle l1=
        new BLumiereDirectionnelle(new Vector3f(1, 0, 1), Color.white);
      BLumiereDirectionnelle l2=
        new BLumiereDirectionnelle(new Vector3f(-1, 0, -1), Color.white);
      l1.setName("Droite");
      l2.setName("Gauche");
      gl.add(l1);
      gl.add(l2);
      l1.setRapide(false);
      l1.setVisible(true);
      l2.setRapide(false);
      l2.setVisible(true);
      System.out.println("Groupe volume univers");
      final BGroupeStandard gs= new BGroupeStandard();
      gs.setName("Univers");
      gs.add(gv);
      gs.add(gl);
      final BuInformationsDocument idRefonde= new BuInformationsDocument();
      idRefonde.name= "Etude";
      idRefonde.version= "0.01";
      idRefonde.organization= "CETMEF";
      idRefonde.author= System.getProperty("user.name");
      idRefonde.contact= idRefonde.author + "@cetmef.equipement.gouv.fr";
      idRefonde.date= FuLib.date();
      idRefonde.logo= EbliResource.EBLI.getIcon("minlogo.gif");
      mainpanel.setProgression(100);
      SwingUtilities.invokeLater(new Runnable(){

        public void run(){
          EbliFilleVue3D fVue3D_= new EbliFilleVue3D(idRefonde, true);
          fVue3D_.setRoot(gs);
          fVue3D_.getUnivers().init();
          fVue3D_.setSize(new Dimension(600, 400));
          refonde.addInternalFrame(fVue3D_);
          try {
            fVue3D_.setIcon(true);
          } catch (PropertyVetoException e) {}
          refonde.activateInternalFrame(fVue3D_);
          fVue3D_.getUnivers().repaintCanvas();
          mainpanel.setMessage("");
          mainpanel.setProgression(0);
        }
      });

    }
  }
}
