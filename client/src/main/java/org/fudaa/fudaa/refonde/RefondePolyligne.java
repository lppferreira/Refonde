/*
 * @file         RefondePolyligne.java
 * @creation     2000-03-22
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.Vector;

import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;
/**
 * Une polyligne extention de GrPolyline.
 *
 * @version      $Id: RefondePolyligne.java,v 1.7 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePolyligne
  extends GrPolyligne
  implements RefondeGeom, RefondeMaillable, RefondeSupporteGroupeProprietes {
  protected RefondeGroupeProprietes gPrp_= null;
  protected GrMaillageElement mail_= null;
  /**
   * Création d'une polyligne vide
   */
  public RefondePolyligne() {
    super();
  }
  //----------------------------------------------------------------------------
  // Interface RefondeGeom
  //----------------------------------------------------------------------------
  protected RefondeGeom[] parents_= new RefondeGeom[0];
  protected RefondeScene scene_= null;
  /**
   * Retourne les parents
   */
  public RefondeGeom[] getParents() {
    return parents_;
  }
  /**
   * Ajoute un parent
   */
  public void addParent(RefondeGeom _geom) {
    for (int i= 0; i < parents_.length; i++)
      if (parents_[i] == _geom)
        return;
    RefondeGeom[] nParents= new RefondeGeom[parents_.length + 1];
    System.arraycopy(parents_, 0, nParents, 0, parents_.length);
    nParents[parents_.length]= _geom;
    parents_= nParents;
  }
  /**
   * Supprime un parent
   */
  public void removeParent(RefondeGeom _geom) {
    for (int i= 0; i < parents_.length; i++) {
      if (parents_[i] == _geom) {
        RefondeGeom[] nParents= new RefondeGeom[parents_.length - 1];
        System.arraycopy(parents_, 0, nParents, 0, i);
        System.arraycopy(parents_, i + 1, nParents, i, parents_.length - 1 - i);
        parents_= nParents;
        break;
      }
    }
  }
  /**
   * Accesseurs de la scène
   */
  public RefondeScene getScene() {
    return scene_;
  }
  public void setScene(RefondeScene _scene) {
    scene_= _scene;
  }
  //----------------------------------------------------------------------------
  // Interface RefondeSupporteGroupeProprietes
  //----------------------------------------------------------------------------
  /**
   * Accesseurs au groupe de proprietes
   */
  public void setGroupeProprietes(RefondeGroupeProprietes _gPrp) {
    gPrp_= _gPrp;
  }
  public RefondeGroupeProprietes getGroupeProprietes() {
    return gPrp_;
  }
  //----------------------------------------------------------------------------
  // Interface RefondeMaillable
  //----------------------------------------------------------------------------
  public boolean hasMaillage() {
    return getMaillage() != null;
  }
  public boolean hasNoeuds() {
    return getNoeuds() != null;
  }
  /**
   * Accesseurs sur le maillage lié au domaine. null si aucun maillage
   */
  public void setMaillage(GrMaillageElement _mail) {
    mail_= _mail;
  }
  public GrMaillageElement getMaillage() {
    return mail_;
  }
  /**
   * Retourne les noeuds de la polyligne ordonnés dans le sens de la polyligne
   * s'il existe un maillage ou si un contour parent possède des noeuds
   * (null si aucun noeud)
   */
  public GrNoeud[] getNoeuds() {
    //    GrMaillageElement mail;
    // La polyligne possède un maillage
    if (mail_ != null)
      return mail_.noeuds();
    // La polyligne ne possède pas de maillage => Recherche des noeuds en
    // provenance d'un parent
    for (int i= 0; i < parents_.length; i++) {
      GrNoeud[] ndsCtr;
      RefondeMaillable parent;
      GrPoint pt;
      GrNoeud nd;
      Vector vnds= new Vector();
      parent= (RefondeMaillable)parents_[i];
      if ((ndsCtr= parent.getNoeuds()) == null)
        continue;
      // Des noeuds existent sur le parent
      // => Retourne les noeuds pour la polyligne
      for (int j= 0; j < ndsCtr.length; j++) {
        pt= sommet(0);
        nd= ndsCtr[j];
        if (pt.distanceXY(nd.point_) < 1.e-3) {
          int sens;
          nd= ndsCtr[(j + 1) % ndsCtr.length];
          if (distanceXY(nd.point_) < 1.e-3)
            sens= 1;
          else
            sens= -1;
          pt= sommet(nombre() - 1);
          for (int k= 0; k < ndsCtr.length; k++) {
            nd= ndsCtr[((k * sens) + j + ndsCtr.length) % ndsCtr.length];
            vnds.add(nd);
            if (pt.distanceXY(nd.point_) < 1.e-3)
              break;
          }
          return (GrNoeud[])vnds.toArray(new GrNoeud[0]);
        }
      }
      System.err.println(
        "Erreur : il y a un probleme de correspondance "
          + "entre le maillage et la geometrie");
    }
    return null;
  }
}
