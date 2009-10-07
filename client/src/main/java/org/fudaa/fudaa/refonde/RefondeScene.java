/*
 * @file         RefondeScene.java
 * @creation     2000-03-22
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.Vector;

import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrPoint;
/**
 * La scene contenant tous les objets geometriques (Domaines, contours, etc.).
 *
 * @version      $Id: RefondeScene.java,v 1.6 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeScene {
  protected Vector domas_= new Vector();
  protected Vector cntrs_= new Vector();
  protected Vector polys_= new Vector();
  protected Vector pts_= new Vector();
  //  protected GrMaillageElement triang_=null;
  /**
   * Retourne tous les maillages lies à la géometrie (que sur les domaines pour
   * l'instant)
   */
  public GrMaillageElement[] getMaillages() {
    Vector vmails= new Vector();
    GrMaillageElement mail;
    for (int i= 0; i < domas_.size(); i++)
      if ((mail= ((RefondeDomaine)domas_.get(i)).getMaillage()) != null)
        vmails.add(mail);
    return (GrMaillageElement[])vmails.toArray(new GrMaillageElement[0]);
  }
  /**
   * Attribut points
   */
  public void addPoint(GrPoint _pt) {
    pts_.add(_pt);
    //    _pt.setScene(this);
  }
  public void removePoint(GrPoint _pt) {
    pts_.remove(_pt);
    //    _pt.setScene(null);
  }
  public Vector getPoints() {
    return pts_;
  }
  /**
   * Attribut polylignes
   */
  public void addPolyligne(RefondePolyligne _poly) {
    polys_.add(_poly);
    _poly.setScene(this);
  }
  public void removePolyligne(RefondePolyligne _poly) {
    polys_.remove(_poly);
    _poly.setScene(null);
  }
  public Vector getPolylignes() {
    return polys_;
  }
  /**
   * Attribut contours
   */
  public void addContour(RefondeContour _cntr) {
    cntrs_.add(_cntr);
    _cntr.setScene(this);
  }
  public void removeContour(RefondeContour _cntr) {
    cntrs_.remove(_cntr);
    _cntr.setScene(null);
  }
  public Vector getContours() {
    return cntrs_;
  }
  /**
   * Attribut domaines
   */
  public void addDomaine(RefondeDomaine _doma) {
    domas_.add(_doma);
    _doma.setScene(this);
  }
  public void removeDomaine(RefondeDomaine _doma) {
    domas_.remove(_doma);
    _doma.setScene(null);
  }
  public Vector getDomaines() {
    return domas_;
  }
  /**
   * Accesseurs a la triangulation du projet
   */
  //  public void setTriangulation(GrMaillageElement _triang) { triang_=_triang; }
  //  public GrMaillageElement getTriangulation() { return triang_; }
}
