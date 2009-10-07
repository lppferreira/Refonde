/*
 * @file         RefondeGrilleIrreguliere.java
 * @creation     2000-02-18
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import javax.media.j3d.IndexedGeometryArray;
import javax.vecmath.Point3d;

import org.fudaa.ebli.volume.BGrilleIrreguliere;
/**
 * @version      $Id: RefondeGrilleIrreguliere.java,v 1.7 2006-09-08 16:04:28 opasteur Exp $
 * @author       Christophe Delhorbe
 */
public class RefondeGrilleIrreguliere extends BGrilleIrreguliere {
  private RefondeDataSource source_donnees_;
  private Point3d[] next_figure_;
  public RefondeGrilleIrreguliere(String _source) {
    super(_source);
  }
  public void setSourceDonnees(RefondeDataSource _source) {
    source_donnees_= _source;
  }
  public void setTemps(long t) {
    actualise(t);
    affiche();
  }
  public void actualise(long t) {
    double tps= (double)t / 1000;
    //    System.out.println("temps :"+tps);
    double[] hauteurs= source_donnees_.getDonnees(tps);
    next_figure_= new Point3d[hauteurs.length];
    //    System.out.println("next : "+next_figure_.length+"; previous : "+((IndexedGeometryArray)shape_.getGeometry()).getVertexCount());
    for (int i= 0; i < next_figure_.length; i++) {
      Point3d ancien= new Point3d();
      ((IndexedGeometryArray)shape_.getGeometry()).getCoordinate(i, ancien);
      next_figure_[i]=
        new Point3d(
          ancien.x,
          ancien.y,
          hauteurs[i] /*/ ParametresVolumes.ECHELLE*/);
    }
    affiche();
  }
  public void affiche() {
    //for (int i=0;i<10;i++) System.out.print(" "+next_figure_[i]);
    //System.out.println();
    ((IndexedGeometryArray)shape_.getGeometry()).setCoordinates(
      0,
      next_figure_);
  }
}
