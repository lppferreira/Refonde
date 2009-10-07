/*
 * @file         RefondeDomaine.java
 * @creation     2000-03-22
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;


import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;
/**
 * Domaine géometrique. Un domaine est un ensemble de 1 ou plusieurs contours
 * (dans ce dernier cas, le premier contour est le contour externe, les autres
 * sont les contours internes) servant de support au maillage.
 *
 * @version      $Id: RefondeDomaine.java,v 1.8 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand
 */
public /*abstract*/ class RefondeDomaine
  implements RefondeGeom, RefondeMaillable, RefondeSupporteGroupeProprietes {
  protected RefondeGroupeProprietes gPrp_= null;
  protected RefondeContour[] contours_;
  protected GrMaillageElement mail_= null;
  /**
   * Création d'un domaine vide
   */
  public RefondeDomaine() {
    contours_= new RefondeContour[0];
  }
  /**
   * Création d'un domaine
   */
  public RefondeDomaine(RefondeContour[] _cntrs) {
    //    contours_=_cntrs;
    setContours(_cntrs);
  }
  /**
   * Affectation des contours. Le premier contour doit toujours être le contour
   * exterieur.
   */
  public void setContours(RefondeContour[] _cntrs) {
    if (contours_ != null)
      for (int i= 0; i < contours_.length; i++)
        contours_[i].removeParent(this);
    if (_cntrs != null)
      for (int i= 0; i < _cntrs.length; i++)
        _cntrs[i].addParent(this);
    contours_= _cntrs;
  }
  /**
   * Retourne les contours. Le premier contour est toujours le contour
   * exterieur.
   */
  public RefondeContour[] getContours() {
    return contours_;
  }
  /**
   * Boite englobante
   */
  public GrBoite boite() {
    GrBoite r= new GrBoite();
    for (int i= 0; i < contours_.length; i++)
      r.ajuste(contours_[i].boite());
    return r;
  }
  /**
   * Retourne vrai si le domaine est contitué du contour donné
   */
  public boolean hasChild(RefondeContour _cntr) {
    for (int i= 0; i < contours_.length; i++)
      if (contours_[i] == _cntr)
        return true;
    return false;
  }
  /**
   * Retourne vrai si le domaine contient le point 2D donné. Ce point doit être
   * dans le contour externe, pas dans les contours internes.
   */
  public boolean contient(GrPoint _pt) {
    if (contours_.length == 0 || !contours_[0].contient(_pt))
      return false;
    for (int i= 1; i < contours_.length; i++)
      if (contours_[i].contient(_pt))
        return false;
    return true;
  }
  /**
   * Trie les contours pour que le premier contour soit toujours le contour
   * exterieur
   */
  public static void triContours(RefondeContour[] _cntrs) {
    RefondeContour cntr;
    GrPolyligne[] pls;
    GrPoint[] pts;
    double xmin= Double.POSITIVE_INFINITY;
    int ncext= 0;
    // Contour exterieur => celui dont un des points a un x mini
    for (int i= 0; i < _cntrs.length; i++) {
      cntr= _cntrs[i];
      pls= _cntrs[i].getPolylignes();
      for (int j= 0; j < pls.length; j++) {
        pts= pls[j].sommets_.tableau();
        for (int k= 0; k < pts.length - 1; k++) {
          if (pts[k].x_ < xmin) {
            xmin= pts[k].x_;
            ncext= i;
          }
        }
      }
    }
    // Déplacement si le premier contour n'est pas le contour exterieur
    if (ncext != 0) {
      System.out.println("Contour exterieur : " + ncext);
      cntr= _cntrs[0];
      _cntrs[0]= _cntrs[ncext];
      _cntrs[ncext]= cntr;
    }
  }
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
   * Retourne les noeuds du domaine (null si le domaine n'est pas maillé)
   */
  public GrNoeud[] getNoeuds() {
    if (mail_ != null)
      return mail_.noeuds();
    return null;
  }
  /**
   * Maille le domaine (à surcharger)
   */
  public void mailler() {
    return;
  }

//  public abstract SMaillage maillerEtOptimiser(GrMaillageElement _init);
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
}
