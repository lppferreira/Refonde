/*
 * @file         RefondeCalqueContour.java
 * @creation     1999-08-10
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.ebli.calque.BCalquePolyligne;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.trace.TraceGeometrie;
import org.fudaa.ebli.trace.TraceLigne;
/**
 * Un calque de tracé des contours géométriques. Le type de trait est donné dans
 * chaque RefondePolyligne. S'il ne l'est pas, on prend le type de trait
 * du calque.
 *
 * @version      $Id: RefondeCalqueContour.java,v 1.9 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand , Bertrand Marchand
 */
public class RefondeCalqueContour extends BCalquePolyligne {
  private Hashtable gr2Bd_;
  private RefondeProjet projet_;
  /**
   * Contructeur du calque
   */
  public RefondeCalqueContour() {
    super();
    setDestructible(false);
    gr2Bd_= new Hashtable();
  }

  public void paintComponent(Graphics _g) {
    Color fg= getForeground();
    if (isAttenue())
      fg= attenueCouleur(fg);
    TraceGeometrie trace= new TraceGeometrie(getVersEcran());
    trace.setForeground(fg);
    boolean rapide= isRapide();
    for (int i= 0; i < lignes_.nombre(); i += 1000 / getDensite()) {
      dessinePolyligne((Graphics2D)_g,trace,lignes_.renvoie(i), rapide);
    }
  }
  /**
   * Affichage des polylignes suivant un tracé mixte. Si une polyligne n'est pas
   * sur une frontière, elle est tracée en atténué.
   */
  private void dessinePolyligne(Graphics2D _g2d,
    TraceGeometrie _trace,
    GrPolyligne _ligne,
    boolean _rapide) {
    int typeTrait;
    RefondeGroupeProprietes gpe;
    RefondePolyligne plFr; // Frontière associée
    plFr= projet_.getGeometrie().frontiere((RefondePolyligne)_ligne);
    if (plFr == null) {
      typeTrait= TraceLigne.LISSE;
    } else {
      gpe= ((RefondePolyligne)_ligne).getGroupeProprietes();
      int type= gpe.getType();
      if (type==RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE ||
          type==RefondeGroupeProprietes.SEICHE_BORD_OUVERT)
        typeTrait= TraceLigne.POINTILLE;
      else if (type==RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE)
        typeTrait= TraceLigne.TIRETE;
      else
        typeTrait= TraceLigne.LISSE;
    }
    _trace.setTypeTrait(typeTrait);
    _trace.dessinePolyligne(_g2d,_ligne, resolution_, _rapide);
  }
  /**
   * Initialisation du calque avec le projet
   */
  public void initialise(RefondeProjet _projet) {
    projet_= _projet;
    Vector vpls= _projet.getGeometrie().scene_.getPolylignes();
    //    Vector      ctrs=_projet.getGeometrie().cntrsFond();
    //    Vector      ctr;
    RefondePolyligne pl;
    //    IPoint[]    pts;
    //    GrPoint     grPt;
    //    GrPoint     grPtSav;
    //    double[]    coor;
    //    int         ptGrPl;
    //    gr2Bd_.clear();
    // Nettoyage du calque
    reinitialise();
    for (int i= 0; i < vpls.size(); i++) {
      pl= (RefondePolyligne)vpls.get(i);
      ajoute(pl);
      gr2Bd_.put(pl, pl);
    }
    //    for (Iterator i=ctrs.iterator(); i.hasNext();) {
    //      ctr=(Vector)i.next();
    //      for (Iterator j=ctr.iterator(); j.hasNext();) {
    //        pl=(RefondePolyligne)j.next();
    //        ajoute(pl);
    //        gr2Bd_.put(pl,pl);
    //      }
    //    }
    /*    for (Iterator i=ctrs.iterator(); i.hasNext();) {
          ctr=(Vector)i.next();

          pl=(IPolyligne)ctr.elementAt(0);
          coor=pl.point(0).coordonnees();
          grPt=new GrPoint(coor[0],coor[1],coor[2]);
          grPtSav=grPt;

          for (Iterator j=ctr.iterator(); j.hasNext();) {
            grPl=new GrPolyligne();
            grPl.sommets.ajoute(grPt);
            pl=(IPolyligne)j.next();
            coor=pl.point(1).coordonnees();
            grPt=new GrPoint(coor[0],coor[1],coor[2]);
            grPl.sommets.ajoute(grPt);

            ajoute(grPl);
            gr2Bd_.put(grPl,pl);
          }

          // remplacement du dernier point par le premier du contour
          grPl.sommets.remplace(grPtSav,grPl.sommets.indice(grPt));
        } */
  }
  /**
   * Affectation de la table de hachage globale de Gr vers Bd.
   */
  public void setGr2Bd(Hashtable _gr2Bd) {
    gr2Bd_= _gr2Bd;
  }
}
