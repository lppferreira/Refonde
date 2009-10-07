/*
 * @file         RefondeContour.java
 * @creation     2000-03-22
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrVecteur;
/**
 * Un contour. Un contour est composé de RefondePolyligne jointes.<p>
 * Dans le tableau des polylignes, 2 polylignes consécutives doivent posséder
 * un point en commun (autrement dit, elles sont jointives). Elles peuvent par
 * contre être orientées différamment (le point commun pouvant être les 2 points
 * de début des 2 polylignes consécutives, ou le point de début d'une et fin de
 * l'autre, etc.).
 *
 * @version      $Id: RefondeContour.java,v 1.8 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeContour implements RefondeGeom, RefondeMaillable {
  protected RefondePolyligne[] pls_;
  protected GrMaillageElement mail_= null;
  /**
   * Création d'un contour vide
   */
  public RefondeContour() {
    pls_= new RefondePolyligne[0];
  }
  /**
   * Création d'un contour à partir d'un tableau de polylignes
   */
  public RefondeContour(RefondePolyligne[] _pls) {
    setPolylignes(_pls);
  }
  /**
   * Affectation d'un tableau de polylignes
   */
  public void setPolylignes(RefondePolyligne[] _pls) {
    if (pls_ != null)
      for (int i= 0; i < pls_.length; i++)
        pls_[i].removeParent(this);
    if (_pls != null)
      for (int i= 0; i < _pls.length; i++)
        _pls[i].addParent(this);
    pls_= _pls;
  }
  /**
   * Retourne les polylignes du contour
   */
  public RefondePolyligne[] getPolylignes() {
    return pls_;
  }
  /**
   * Retourne l'indice de la polyligne dans le tableau des polylignes (-1 si pas
   * dans le tableau)
   */
  public int indice(RefondePolyligne _pl) {
    if (pls_ == null)
      return -1;
    for (int i= 0; i < pls_.length; i++)
      if (pls_[i] == _pl)
        return i;
    return -1;
  }
  /**
   * Boite englobante
   */
  public GrBoite boite() {
    GrBoite r= new GrBoite();
    for (int i= 0; i < pls_.length; i++)
      r.ajuste(pls_[i].boite());
    return r;
  }
  /**
   * Retourne vrai si le contour est contitué du contour donné
   */
  public boolean hasChild(RefondePolyligne _pl) {
    for (int i= 0; i < pls_.length; i++)
      if (pls_[i] == _pl)
        return true;
    return false;
  }
  /**
   * Retourne vrai si le contour contient le point 2D donné.
   */
  public boolean contient(GrPoint _pt) {
    if (pls_.length == 0)
      return false;
    GeneralPath pg= new GeneralPath();
    GrPoint[] pts;
    // Construction du polygone correspondant
    for (int i= 0; i < pls_.length; i++) {
      pts= pls_[i].sommets_.tableau();
      for (int j= 0; j < pts.length - 1; j++) {
        if (i == 0 && j == 0)
          pg.moveTo((float)pts[j].x_, (float)pts[j].y_);
        else
          pg.lineTo((float)pts[j].x_, (float)pts[j].y_);
      }
    }
    pg.closePath();
    return pg.contains(_pt.x_, _pt.y_);
  }
  /**
   * Copie d'un contour récursivement. Les polylignes et les points des polylignes
   * sont recopiés sans redondance.
   */
  public RefondeContour copie() {
    Hashtable opts2npts= new Hashtable();
    RefondePolyligne[] opls= getPolylignes();
    RefondePolyligne[] npls= new RefondePolyligne[opls.length];
    // Construction des points non redondants et des polylignes
    for (int i= 0; i < npls.length; i++) {
      npls[i]= new RefondePolyligne();
      for (int j= 0; j < opls[i].nombre(); j++) {
        GrPoint opt= opls[i].sommet(j);
        GrPoint npt;
        if ((npt= (GrPoint)opts2npts.get(opt)) == null) {
          npt= new GrPoint(opt.x_, opt.y_, opt.z_);
          opts2npts.put(opt, npt);
        }
        npls[i].sommets_.ajoute(npt);
      }
    }
    return new RefondeContour(npls);
  }
  /**
   * Oriente un contour. Les lignes de ce contour
   * seront orientées dans le même sens que le contour.
   *
   * @param _trigo <i>true</i> : oriente dans le sens trigo.
   *               <i>false</i> : oriente dans le sens horaire.
   */
  public void orienteTrigo(boolean _trigo) {
    double angle;
    double cos;
    double sin;
    RefondePolyligne pl;
    GrVecteur v1;
    GrVecteur v2;
    GrPoint p1;
    GrPoint p2;
    GrPoint pt;
    RefondePolyligne[] pls;
    pls= getPolylignes();
    angle= 0.;
    pl= pls[pls.length - 1];
    // B.M. 07/09/2000
    // Inversion du vecteur dans le sens de progression du contour
    if (pl.sommet(1).equals(pls[0].sommet(0))
      || pl.sommet(1).equals(pls[0].sommet(1))) {
      p1= pl.sommet(0);
      p2= pl.sommet(1);
    } else {
      p1= pl.sommet(1);
      p2= pl.sommet(0);
    }
    v2= new GrVecteur(p2.x_ - p1.x_, p2.y_ - p1.y_, 0.).normalise();
    for (int j= 0; j < pls.length; j++) {
      v1= v2;
      pl= pls[j];
      // B.M. 07/09/2000
      // Inversion du vecteur dans le sens de progression du contour
      if (pl.sommet(0).equals(p2)) {
        p1= pl.sommet(0);
        p2= pl.sommet(1);
      } else {
        p1= pl.sommet(1);
        p2= pl.sommet(0);
      }
      v2= new GrVecteur(p2.x_ - p1.x_, p2.y_ - p1.y_, 0.).normalise();
      cos= v1.x_ * v2.x_ + v1.y_ * v2.y_;
      sin= v1.x_ * v2.y_ - v1.y_ * v2.x_;
      angle += Math.atan2(sin, cos);
    }
    // Orientation du sens du contour
    if (_trigo && angle < 0. || !_trigo && angle > 0.) {
      System.out.println("Inversion du sens du contour ");
      for (int i= 0; i < pls.length / 2; i++) {
        pl= pls[i];
        pls[i]= pls[pls.length - i - 1];
        pls[pls.length - i - 1]= pl;
      }
    }
    // Orientation des polylignes du contour
    for (int i= 0; i < pls.length; i++) {
      if ((i == 0
        && (pls[i].sommet(0) == pls[i + 1].sommet(0)
          || pls[i].sommet(0) == pls[i + 1].sommet(1)))
        || (i != 0 && pls[i].sommet(1).equals(pls[i - 1].sommet(1)))) {
        pt= pls[i].sommet(0);
        pls[i].sommets_.remplace(pls[i].sommet(1), 0);
        pls[i].sommets_.remplace(pt, 1);
      }
    }
  }
  /**
   * Recherche d'un point quelconque interne au contour
   */
  public GrPoint getPointInterne() {
    GeneralPath pg= new GeneralPath();
    GrPoint[] pts;
    double[] coorP1;
    double[] coorP2;
    double[] coorP3;
    PathIterator it;
    double xInt;
    double yInt;
    Rectangle2D box;
    // Construction du polygone correspondant
    for (int i= 0; i < pls_.length; i++) {
      pts= pls_[i].sommets_.tableau();
      for (int j= 0; j < pts.length - 1; j++) {
        if (i == 0 && j == 0)
          pg.moveTo((float)pts[j].x_, (float)pts[j].y_);
        else
          pg.lineTo((float)pts[j].x_, (float)pts[j].y_);
      }
    }
    pg.closePath();
    // Détermination d'un point a l'intérieur
    // Essai avec le point central de la boite englobante
    box= pg.getBounds2D();
    xInt= box.getCenterX();
    yInt= box.getCenterY();
    if (pg.contains(xInt, yInt))
      return new GrPoint(xInt, yInt, 0.);
    // Essai avec un point milieu des points (n,n+2)
    coorP2= new double[2];
    coorP3= new double[2];
    it= pg.getPathIterator(new AffineTransform());
    it.next();
    it.currentSegment(coorP2);
    it.next();
    it.currentSegment(coorP3);
    while (!it.isDone()) {
      coorP1= coorP2;
      coorP2= coorP3;
      coorP3= new double[2];
      it.next();
      if (it.currentSegment(coorP3) == PathIterator.SEG_CLOSE)
        break;
      xInt= (coorP3[0] - coorP1[0]) / 2.;
      yInt= (coorP3[1] - coorP1[1]) / 2.;
      if (pg.contains(xInt, yInt))
        return new GrPoint(xInt, yInt, 0.);
    }
    // Essai avec un point quelconque de la boite englobante jusqu'à obtention
    int count= 0;
    while (true) {
      xInt= box.getWidth() * Math.random() + box.getMinX();
      yInt= box.getHeight() * Math.random() + box.getMinY();
      count++;
      if (pg.contains(xInt, yInt)) {
        System.out.println(count + " points de tentative pour trouver un trou");
        return new GrPoint(xInt, yInt, 0.);
      }
    }
  }
  /**
   * Le point est il interne au contour
   */
  public boolean isPointInterne(GrPoint _pt) {
    GeneralPath pg= new GeneralPath();
    GrPoint[] pts;
    // Construction du polygone correspondant
    for (int i= 0; i < pls_.length; i++) {
      pts= pls_[i].sommets_.tableau();
      for (int j= 0; j < pts.length - 1; j++) {
        if (i == 0 && j == 0)
          pg.moveTo((float)pts[j].x_, (float)pts[j].y_);
        else
          pg.lineTo((float)pts[j].x_, (float)pts[j].y_);
      }
    }
    pg.closePath();
    // Le point est-il à l'intérieur
    return pg.contains(_pt.x_, _pt.y_);
  }
  /**
   * Le contour est-il associé à un maillage
   */
  public boolean hasMaillage() {
    return getMaillage() != null;
  }
  /**
   * Le contour possède-t-il des noeuds
   */
  public boolean hasNoeuds() {
    return getNoeuds() != null;
  }
  /**
   * Le contour peut-il retourner des aretes (éléments) issues de son maillage
   * associé ou du maillage de son parent.
   */
  public boolean hasAretes() {
    return getAretes() != null;
  }
  /**
   * Accesseurs sur le maillage lié au contour. null si aucun maillage
   */
  public void setMaillage(GrMaillageElement _mail) {
    mail_= _mail;
  }
  public GrMaillageElement getMaillage() {
    return mail_;
  }
  /**
   * Retourne les arètes existantes du contour (si le contour est maillé) ou
   * les arètes en provenance du maillage du domaine parent (arètes générées
   * pour la cause) ou null si aucun maillage.
   */
  public GrElement[] getAretes() {
    // Le contour possède un maillage
    if (mail_ != null)
      return mail_.elements();
    // Le contour ne possède pas de maillage => Recherche des aretes en
    // provenance d'un domaine parent
    for (int i= 0; i < parents_.length; i++) {
      GrMaillageElement ml;
      GrElement[][] elsCts;
      RefondeDomaine dm;
      GrPoint ptCt;
      double dstMn;
      int ict;
      if (!(parents_[i] instanceof RefondeDomaine))
        continue;
      dm= (RefondeDomaine)parents_[i];
      if ((ml= dm.getMaillage()) == null)
        continue;
      // Un maillage existe pour le domaine => Retourne les aretes de contour
      elsCts= ml.aretesContours();
      ptCt= pls_[0].sommet(0);
      dstMn= Double.POSITIVE_INFINITY;
      ict= 0;
      for (int j= 0; j < elsCts.length; j++) {
        for (int k= 0; k < elsCts[j].length; k++) {
          double dst= ptCt.distanceXY(elsCts[j][k].noeuds_[0].point_);
          if (dst < dstMn) {
            ict= j;
            dstMn= dst;
          }
        }
      }
      return elsCts[ict];
    }
    return null;
  }
  /**
   * Retourne les noeuds du contour ordonnés s'il existe un maillage ou si
   * le domaine du contour est maillé (null si aucun noeud)
   */
  public GrNoeud[] getNoeuds() {
    // Le contour possède un maillage
    if (mail_ != null)
      return mail_.noeuds();
    // Le contour ne possède pas de maillage => Recherche des noeuds en
    // provenance d'un domaine parent
    for (int i= 0; i < parents_.length; i++) {
      GrMaillageElement ml;
      GrNoeud[][] ndsCts;
      RefondeDomaine dm;
      GrPoint ptCt;
      double dstMn;
      int ict;
      if (!(parents_[i] instanceof RefondeDomaine))
        continue;
      dm= (RefondeDomaine)parents_[i];
      if ((ml= dm.getMaillage()) == null)
        continue;
      // Un maillage existe pour le domaine => Retourne les noeuds de contour
      ndsCts= ml.noeudsContours();
      ptCt= pls_[0].sommet(0);
      dstMn= Double.POSITIVE_INFINITY;
      ict= 0;
      for (int j= 0; j < ndsCts.length; j++) {
        for (int k= 0; k < ndsCts[j].length; k++) {
          double dst= ptCt.distanceXY(ndsCts[j][k].point_);
          if (dst < dstMn) {
            ict= j;
            dstMn= dst;
          }
        }
      }
      return ndsCts[ict];
    }
    return null;
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
}
