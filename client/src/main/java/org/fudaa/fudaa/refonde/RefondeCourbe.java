/*
 * @file         RefondeCourbe.java
 * @creation     2000-03-20
 * @modification $Date: 2005-08-16 14:21:33 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * Classe de courbe.
 *
 * @version      $Id: RefondeCourbe.java,v 1.5 2005-08-16 14:21:33 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeCourbe {
  //  private int        type_;
  private double[] x_;
  private double[] y_;
  private String name_;
  /**
   * Création d'une courbe
   */
  public RefondeCourbe() {
    this("", new double[0], new double[0]);
  }
  /**
   * Création d'une courbe
   */
  public RefondeCourbe(String _name) {
    this(_name, new double[0], new double[0]);
  }
  /**
   * Création d'une courbe
   */
  public RefondeCourbe(String _name, double[] _x, double[] _y) {
    name_= _name;
    x_= _x;
    y_= _y;
  }
  /**
   * Création d'une courbe
   */
  public RefondeCourbe(String _name, double[][] _pts) {
    name_= _name;
    setPoints(_pts);
  }
  /**
   * Nombre de points de la courbe
   */
  public int nbPoints() {
    return x_.length;
  }
  /**
   * Nom de la courbe
   */
  public void setName(String _name) {
    name_= _name;
  }
  public String getName() {
    return name_;
  }
  /**
   * Valeurs des points
   */
  public void setPoints(double[] _x, double[] _y) {
    x_= _x;
    y_= _y;
  }
  public void setPoints(double[][] _pts) {
    x_= new double[_pts.length];
    y_= new double[_pts.length];
    for (int i= 0; i < _pts.length; i++) {
      x_[i]= _pts[i][0];
      y_[i]= _pts[i][1];
    }
  }
  /**
   * Valeurs X
   */
  public void setX(double[] _x) {
    x_= _x;
  }
  public double[] getX() {
    return x_;
  }
  /**
   * Valeurs Y
   */
  public void setY(double[] _y) {
    y_= _y;
  }
  public double[] getY() {
    return y_;
  }
}
