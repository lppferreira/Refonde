/*
 * @file         RefondeAngle.java
 * @creation     1999-06-25
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.ebli.geometrie.GrPoint;
/**
 * Classe angle. Les abscisses initial et final de l'angle sont
 * compris dans l'intervalle [0,1]. L'abscisse initial est toujours inférieur à
 * l'abscisse final.
 *
 * @version      $Id: RefondeAngle.java,v 1.6 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeAngle {
  public static final int DIFFRACTE= 0; // Angle diffracté à partir d'un point
  public static final int RELATIF= 1; // Angle relatif à la normale au segment
  public static final int ABSOLU= 2; // Angle absolu.
  private int type_; // Type : DIFFRACTE, RELATIF, ABSOLU
  private double sDeb_; // Abscisse du début de segment sur le contour [0,1[
  private double sFin_; // Abscisse de fin   de segment sur le contour ]0,1]
  private double angle_;
  // Si le type est RELATIF ou ABSOLU. Angle donné en degrés [0,360[
  private GrPoint ptDiff_; // Si le type est DIFFRACTE.
  private int typeSup_;
  // Type de l'angle supplémentaire associé. ABSOLU ou RELATIF.
  private double angSup_; // Angle supplémentaire donné en dégrés [0,360[
  /**
   * Création d'un angle
   */
  public RefondeAngle() {
    type_= ABSOLU;
    sDeb_= 0.;
    sFin_= 1.;
    // B.M. 27/04/2001    sFin_ =0.;
    angle_= 0.;
    typeSup_= RELATIF;
    angSup_= 0;
  }
  /**
   * Création d'un angle depuis un autre angle. Les champs sont également
   * dupliqués
   */
  public RefondeAngle(RefondeAngle _ai) {
    type_= _ai.getType();
    sDeb_= _ai.getSDebut();
    sFin_= _ai.getSFin();
    if (type_ == DIFFRACTE) {
      GrPoint p= _ai.getPointDiffraction();
      ptDiff_= new GrPoint(p.x_, p.y_, p.z_);
    } else if (type_ == RELATIF)
      angle_= _ai.getAngle();
    else if (type_ == ABSOLU)
      angle_= _ai.getAngle();
    typeSup_= _ai.typeSup_;
    angSup_= _ai.angSup_;
  }
  /**
   * L'angle est diffracté.
   */
  public void setDiffracte(double _sDebut, double _sFin, GrPoint _point) {
    type_= DIFFRACTE;
    sDeb_= _sDebut;
    // B.M. 27/04/2001    sFin_  =_sFin%1;
    sFin_= _sFin;
    ptDiff_= _point;
  }
  /**
   * L'angle est relatif à la normale du segment qui le porte.
   */
  public void setRelatif(double _sDebut, double _sFin, double _angle) {
    type_= RELATIF;
    sDeb_= _sDebut;
    // B.M. 27/04/2001    sFin_  =_sFin%1;
    sFin_= _sFin;
    angle_= _angle;
  }
  /**
   * L'angle est absolu.
   */
  public void setAbsolu(double _sDebut, double _sFin, double _angle) {
    type_= ABSOLU;
    sDeb_= _sDebut;
    // B.M. 27/04/2001    sFin_  =_sFin%1;
    sFin_= _sFin;
    angle_= _angle;
  }
  /**
   * Retourne le type de l'angle
   */
  public int getType() {
    return type_;
  }
  /**
   * Retourne l'abscisse de début du segment support de l'angle
   */
  public double getSDebut() {
    return sDeb_;
  }
  /**
   * Definit l'abscisse de début du segment support de l'angle
   */
  public void setSDebut(double _sDebut) {
    sDeb_= _sDebut;
  }
  /**
   * Retourne l'abscisse de fin du segment support de l'angle
   */
  public double getSFin() {
    return sFin_;
  }
  /**
   * Definit l'abscisse de fin du segment support de l'angle
   */
  // B.M. 27/04/2001  public void setSFin(double _sFin) { sFin_=_sFin%1; }
  public void setSFin(double _sFin) {
    sFin_= _sFin;
  }
  /**
   * Retourne l'angle
   */
  public double getAngle() {
    return angle_;
  }
  /**
   * Retourne le point de diffraction
   */
  public GrPoint getPointDiffraction() {
    return ptDiff_;
  }
  /**
   * L'angle supplémentaire est absolu.
   */
  public void setSupAbsolu(double _angle) {
    typeSup_= ABSOLU;
    angSup_= _angle;
  }
  /**
   * L'angle supplémentaire est relatif.
   */
  public void setSupRelatif(double _angle) {
    typeSup_= RELATIF;
    angSup_= _angle;
  }
  /**
   * Retourne le type de l'angle supplémentaire.
   */
  public int getTypeSup() {
    return typeSup_;
  }
  /**
   * Retourne la valeur de l'angle supplémentaire.
   */
  public double getAngleSup() {
    return angSup_;
  }
}
