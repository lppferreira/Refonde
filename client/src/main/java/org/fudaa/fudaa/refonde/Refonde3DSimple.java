/*
 * @file         Refonde3DSimple.java
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
import org.fudaa.ebli.volume.BGroupeLumiere;
import org.fudaa.ebli.volume.BGroupeStandard;
import org.fudaa.ebli.volume.BGroupeVolume;
import org.fudaa.ebli.volume.BLumiereDirectionnelle;
import org.fudaa.ebli.volume.BTriangles;
import org.fudaa.ebli.volume.EbliFilleVue3D;
/**
 * @version      $Id: Refonde3DSimple.java,v 1.12 2006-09-19 15:10:22 deniger Exp $
 * @author       Christophe Delhorbe
 */
public class Refonde3DSimple {
  RefondeProjet projet_;
  RefondeImplementation refonde;
  public Refonde3DSimple(
    RefondeProjet _projet,
    RefondeImplementation _refonde) {
    projet_= _projet;
    refonde= _refonde;
    new BuTaskOperation(refonde, "Création de la vue 3D") {
      public void act() {
        creer3D();
      }
    }
    .start();
  }

  /**
   * Ne s'occupe que du premier pas de temps.
   * @todo Gérer éventuellement le pas de temps.
   */
  public void creer3D() {
    BuMainPanel mainpanel= refonde.getMainPanel();
    mainpanel.setMessage("Création de la vue3D...");
    mainpanel.setProgression(0);
    if (projet_.estEntierementMaille()) {
      // On recupere le maillage
      GrMaillageElement maillage=
        RefondeMaillage.creeSuperMaillage(projet_);
      //projet_=null;
      GrNoeud[] noeuds= maillage.noeuds();
      // On recupere les elements
      GrElement[] elements= maillage.elements();
      int nbelements= elements.length;
      int nbnoeuds= nbelements * 3;
      //      int type_element=elements[0].type;
      System.out.print("generation des noeuds format Java3D");
      // generation des noeuds format Java3D
      Point3d[] noeuds_j3d= new Point3d[nbnoeuds];
      for (int i= 0; i < nbelements; i++)
        for (int j= 0; j < 3; j++) {
          noeuds_j3d[i * 3 + j]=
            new Point3d(
              elements[i].noeuds_[j].point_.x_,
              elements[i].noeuds_[j].point_.y_,
              elements[i].noeuds_[j].point_.z_);
        }
      System.out.println(" ... OK");
      BTriangles bathy= new BTriangles("Bathymetrie");
      mainpanel.setProgression(45);
      bathy.setBoite(maillage.boite());
      maillage= null;
      bathy.setGeometrie(nbnoeuds, noeuds_j3d);
      bathy.setCouleur(new Color(255, 200, 100));
      //      bathy.setEclairage(true);
      BTriangles sol= null;
      if (projet_.hasResultats()) {
        RefondeResultats res= projet_.getResultats();
//        double[] htHoule= res.getResultat(res.nomResultats[res.HAUTEUR_HOULE]);
        double[] htHoule= res.getColonne(res.indexOfColonne(RefondeResultats.nomResultats[RefondeResultats.HAUTEUR_HOULE]))[0];
        for (int i= 0; i < noeuds.length; i++)
          noeuds[i].point_.z_= htHoule[i];
        //        noeuds[i].point.z=projet_.getResultats().resultats_.lignes[i].moduleHauteur;
        System.out.print("generation des noeuds Solutions");
        // generation des noeuds solution
        Point3d[] noeuds_sol= new Point3d[nbnoeuds];
        for (int i= 0; i < nbelements; i++)
          for (int j= 0; j < 3; j++) {
            noeuds_sol[i * 3 + j]=
              new Point3d(
                elements[i].noeuds_[j].point_.x_,
                elements[i].noeuds_[j].point_.y_,
                elements[i].noeuds_[j].point_.z_);
          }
        /*        System.out.print("generation des Resultats");
                // generation des noeuds format Java3D
                Point3d[] noeuds_res=new Point3d[nbnoeuds];
                for (int i=0;i<nbnoeuds;i++)
                {
                  noeuds_res[i]=new Point3d(noeuds_j3d[i].point.x,
                                            noeuds_j3d[i].point.y,
                                            projet_.getResultats().resultats_.lignes[i].moduleHauteur);
                }
                projet_=null;
                noeuds=null;
                System.out.println(" ... OK");
        */
        sol= new BTriangles("Hauteur");
        mainpanel.setProgression(90);
        sol.setBoite(bathy.getBoite());
        sol.setGeometrie(nbnoeuds, noeuds_sol);
        sol.setCouleur(new Color(100, 100, 255));
        //        sol.setEclairage(true);
      }
      BGroupeVolume imp= new BGroupeVolume();
      imp.setName("Import");
      BGroupeVolume gv= new BGroupeVolume();
      gv.add(bathy);
      bathy.setRapide(false);
      bathy.setVisible(true);
      if (sol != null) {
        gv.add(sol);
        sol.setRapide(false);
        sol.setVisible(true);
      }
      gv.add(imp);
      imp.setVisible(true);
      imp.setRapide(false);
      gv.setName("Volumes");
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
      BGroupeStandard gs= new BGroupeStandard();
      gs.setName("Univers");
      gs.add(gv);
      gs.add(gl);
      BuInformationsDocument idRefonde= new BuInformationsDocument();
      idRefonde.name= "Etude";
      idRefonde.version= "0.01";
      idRefonde.organization= "CETMEF";
      idRefonde.author= System.getProperty("user.name");
      idRefonde.contact= idRefonde.author + "@cetmef.equipement.gouv.fr";
      idRefonde.date= FuLib.date();
      idRefonde.logo= EbliResource.EBLI.getIcon("minlogo.gif");
      mainpanel.setProgression(100);
      EbliFilleVue3D fVue3D_= new EbliFilleVue3D(idRefonde, false);
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
  }
}
