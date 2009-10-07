/*
 * @file         RefondeDomainePoreux.java
 * @creation     2003-02-10
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
/**
 * Objet représentant un domaine poreux (zone représentant des caractéristiques
 * physiques de porosité).<br>
 * Le domaine poreux est géoréférencé mais ne fait pas partie de la géométrie
 * proprement dite.<br>
 * Il est limité en x et y par deux points opposés (xmin,ymin et xmax,ymax).<br>
 * Son plan est défini par les trois points de plan associés.
 *
 * @version      $Id: RefondeDomainePoreux.java,v 1.7 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDomainePoreux extends GrPolygone {
  /** Porosité */
  public double porosite_= 0.44;
  /** Coefficient de frottement */
  public double coefFrottement_= 0.4;
  /** Perméabilité */
  public double permeabilite_= 0.00025;
  /** Coefficient de masse virtuelle */
  public double coefMasse_= 0.1;
  /** Le Z des points du plan est-il calculé à partir de la bathymétrie ?
   *  true : Calculé automatiquement, false : Défini par l'utilisateur. */
  private boolean[] zAuto_= new boolean[] { true, true, true };
  /** Cache de la boite du domaine */
  private GrBoite boite_= null;
  /** Points du plan du domaine */
  private GrPoint[] ptsPlan_= null;
  /** a plan */
  private double aa_= 0;
  /** b plan */
  private double bb_= 0;
  /** d plan */
  private double dd_= 0;
  /** e plan */
  private double ee_= 0;
  /**
   * Constructeur. Un domaine est créé en 0,0 de taille 0,0 pour avoir une
   * géométrie minimale.
   */
  public RefondeDomainePoreux() {
    setPoints(new GrPoint[] { new GrPoint(0, 0, 0), new GrPoint(0, 0, 0)});
    ptsPlan_= new GrPoint[3];
    ptsPlan_[0]= new GrPoint(0, 0, 0);
    ptsPlan_[1]= new GrPoint(1, 0, 0);
    ptsPlan_[2]= new GrPoint(1, 1, 0);
  }
  /**
   * Création de la géométrie depuis les points limites. Le premier point est
   * le point en xmin, ymin, le 2eme est en xmax, ymax.
   * <p>
   * Les points passés sont copiés.
   *
   * @param _pts Points limites du domaine.
   */
  public void setPoints(GrPoint[] _pts) {
    if (_pts == null || _pts.length != 2)
      throw new RuntimeException("Invalid parameter _pts");
    double[] xn= new double[3];
    double[] yn= new double[3];
    double[] zn= new double[3];
    xn[0]= _pts[0].x_;
    yn[0]= _pts[0].y_;
    zn[0]= _pts[0].z_;
    xn[1]= _pts[1].x_;
    yn[1]= _pts[1].y_;
    zn[1]= _pts[1].z_;
    // Polygone graphique
    sommets_.vide();
    sommets_.ajoute(_pts[0].x_, _pts[0].y_, _pts[0].z_);
    sommets_.ajoute(_pts[1].x_, _pts[0].y_, _pts[0].z_);
    sommets_.ajoute(_pts[1].x_, _pts[1].y_, _pts[1].z_);
    sommets_.ajoute(_pts[0].x_, _pts[1].y_, _pts[1].z_);
    //    sommets.ajoute(sommet(0));
    //    sommets.ajoute(sommet(2));
    boite_= boite();
  }
  /**
   * Retourne une copie des 2 points limites du domaine.
   */
  public GrPoint[] getPoints() {
    return new GrPoint[] {
      new GrPoint(sommet(0).x_, sommet(0).y_, sommet(0).z_),
      new GrPoint(sommet(2).x_, sommet(2).y_, sommet(2).z_)};
  }
  /**
   * Retourne un tableau contenant les points du plan.
   */
  public GrPoint[] getPointsPlan() {
    return new GrPoint[] { ptsPlan_[0], ptsPlan_[1], ptsPlan_[2] };
  }
  /**
   * Definit les coordonnees des points du plan. Les points restent inchangés.
   */
  public void setPointsPlan(GrPoint[] _pts) {
    if (_pts == null || _pts.length != 3)
      throw new RuntimeException("Invalid parameter _pts");
    ptsPlan_[0].x_= _pts[0].x_;
    ptsPlan_[0].y_= _pts[0].y_;
    ptsPlan_[0].z_= _pts[0].z_;
    ptsPlan_[1].x_= _pts[1].x_;
    ptsPlan_[1].y_= _pts[1].y_;
    ptsPlan_[1].z_= _pts[1].z_;
    ptsPlan_[2].x_= _pts[2].x_;
    ptsPlan_[2].y_= _pts[2].y_;
    ptsPlan_[2].z_= _pts[2].z_;
    // Calcul des caractéristiques du plan
    double[] xn= new double[3];
    double[] yn= new double[3];
    double[] zn= new double[3];
    xn[0]= _pts[0].x_;
    yn[0]= _pts[0].y_;
    zn[0]= _pts[0].z_;
    xn[1]= _pts[1].x_;
    yn[1]= _pts[1].y_;
    zn[1]= _pts[1].z_;
    xn[2]= _pts[2].x_;
    yn[2]= _pts[2].y_;
    zn[2]= _pts[2].z_;
    aa_=
      yn[0] * (zn[1] - zn[2])
        + yn[1] * (zn[2] - zn[0])
        + yn[2] * (zn[0] - zn[1]);
    bb_=
      zn[0] * (xn[1] - xn[2])
        + zn[1] * (xn[2] - xn[0])
        + zn[2] * (xn[0] - xn[1]);
    dd_=
      xn[0] * (yn[1] - yn[2])
        + xn[1] * (yn[2] - yn[0])
        + xn[2] * (yn[0] - yn[1]);
    ee_= - (aa_ * xn[1] + bb_ * yn[1] + dd_ * zn[1]);
  }
  /**
   * Retourne vrai si un point 2D est dans le rectangle du domaine.
   * @return true si le point est dans le domaine.
   */
  public boolean contientXY(GrPoint _pt) {
    return boite_.contientXY(_pt);
  }
  /**
   * Calcule le Z sur le plan du domaine pour un point 2D donné. Ce point peut
   * être en dehors du domaine.
   * @see #contientXY(GrPoint) pour savoir si le point est contenu dans le domaine.
   */
  public double calculeZ(GrPoint _pt) {
    // Calcul de z par le plan ax+by+dz+e = 0
    return (-ee_ - aa_ * _pt.x_ - bb_ * _pt.y_) / dd_;
  }
  /**
   * Définit la manière dont est affecté le Z d'un point de plan.
   * @param _b true : Calculé automatiquement, false : Fixé par l'utilisateur.
   * @param _i Indice du point. De 0 à 2.
   */
  public void setZAutomatique(boolean _b, int _i) {
    if (_i < 0 || _i > 2)
      throw new RuntimeException("Invalid parameter _i");
    zAuto_[_i]= _b;
  }
  /**
   * Retourne la manière dont est affecté le Z d'un point de plan _i.
   * @param _i Indice du point. De 0 à 2.
   * @return true : Calculé automatiquement, false : Fixé par l'utilisateur.
   */
  public boolean isZAutomatique(int _i) {
    if (_i < 0 || _i > 2)
      throw new RuntimeException("Invalid parameter _i");
    return zAuto_[_i];
  }
}