/*
 * @file         RefondeModeleFenetres.java
 * @creation     2001-09-28
 * @modification $Date: 2006-12-05 10:18:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.print.PageFormat;
import java.util.Vector;

import org.fudaa.ebli.calque.BVueCalque;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.repere.RepereMouseKeyController;
import org.fudaa.ebli.repere.RepereMouseKeyTarget;
/**
 * Un modele de vues. Il renferme des objets de type BVueCalque.
 *
 * @version      $Id: RefondeModeleFenetres.java,v 1.7 2006-12-05 10:18:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleFenetres {
  /**
   * La liste des vues. Le vecteur contient des objets de type
   * BVueCalque.
   */
  private Vector vfns_;
  /**
   * Vecteur des géométries des vues.
   */
  private Vector vfnsGeo_;
  /**
   * Format de page.
   */
  //private PageFormat pf_;
  /**
   * Création d'un modèle de vues.
   */
  public RefondeModeleFenetres() {
    vfns_= new Vector();
    vfnsGeo_= new Vector();
    //pf_= RefondeMiseEnPage.pageDefaut;
  }
  /**
   * Définition du format de page dans lequel la vue doit être ajoutée. Par
   * défaut le format de page vaut PrinterJob.getPrinterJob().defautPage().
   */
  public void setPageFormat(PageFormat _pf) {
    //pf_= _pf;
  }
  /**
   * Ajoute une vue au modèle en précisant ses dimensions.
   *
   * @param _vc La vue de post traitement.
   * @param _x  Position x de la vue.
   * @param _y  Position y de la vue.
   * @param _w  Largeur de la vue.
   * @param _h  Hauteur de la vue.
   */
  public void ajoute(
    BVueCalque _vc,
    double _x,
    double _y,
    double _w,
    double _h) {
    if (vfns_.contains(_vc))
      return;
    GrPolygone pg= new GrPolygone();
    pg.sommets_.ajoute(new GrPoint(_x, _y, 0));
    pg.sommets_.ajoute(new GrPoint(_x + _w, _y, 0));
    pg.sommets_.ajoute(new GrPoint(_x + _w, _y + _h, 0));
    pg.sommets_.ajoute(new GrPoint(_x, _y + _h, 0));
    vfns_.add(_vc);
    vfnsGeo_.add(pg);
  }
  /**
   * Enlève une vue au modèle.
   */
  public void enleve(BVueCalque _vc) {
    int i= vfns_.indexOf(_vc);
    vfns_.remove(i);
    vfnsGeo_.remove(i);
  }
  /**
   * Supprime tous les objets du modèle.
   */
  public void enleveTout() {
    vfns_.clear();
    vfnsGeo_.clear();
  }
  /**
   * Retourne les vues du modèle sous forme de vecteur.
   */
  public Vector getObjets() {
    return vfns_;
  }
  /**
   * Retourne la géométrie associée à une vue.
   * @return La géométrie ou <i>null</i> si la vue n'est pas dans le modèle.
   */
  public GrPolygone getGeometrie(BVueCalque _vc) {
    int i= vfns_.indexOf(_vc);
    return i != -1 ? (GrPolygone)vfnsGeo_.get(i) : null;
  }
  /**
   * Boite englobante des objets contenus dans le modèle.
   * @return Boite englobante. Si le modèle est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getBoite() {
    GrBoite r= null;
    if (vfnsGeo_.size() > 0) {
      if (r == null)
        r= new GrBoite();
      for (int i= 0; i < vfnsGeo_.size(); i++)
        r.ajuste(((GrPolygone)vfnsGeo_.get(i)).boite());
    }
    return r;
  }
  /**
   * Renvoi la liste des éléments sélectionnables, soit tous les objets.
   * <p>
   * Cette liste est retournée dans le sens inverse de création des objets, de
   * façon que le dernier créé soit le premier à être testé lors de la sélection.
   *
   * @return La liste des objets.
   */
  public VecteurGrContour getSelectionnables() {
    VecteurGrContour r= new VecteurGrContour();
    for (int i= vfnsGeo_.size() - 1; i >= 0; i--)
      r.ajoute((GrPolygone)vfnsGeo_.get(i));
    return r;
  }
}