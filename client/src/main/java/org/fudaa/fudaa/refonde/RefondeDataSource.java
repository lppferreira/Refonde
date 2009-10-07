/*
 * @file         RefondeDataSource.java
 * @creation     1999-10-28
 * @modification $Date: 2005-08-16 14:21:33 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * B.M. 10/07/2001
 * semble fonctionnelle pour le 3D, mais on l'adapte suite à modifs
 * RefondeResultats.
 *
 * @version      $Id: RefondeDataSource.java,v 1.6 2005-08-16 14:21:33 deniger Exp $
 * @author       Christophe Delhorbe
 */
public class RefondeDataSource {
  //  private org.fudaa.dodico.corba.refonde.SResultatsRefonde resultats_;
  private RefondeResultats resultats_;
  private double T_;
  //  public RefondeDataSource(org.fudaa.dodico.corba.refonde.SResultatsRefonde _resultats,double _T)
  public RefondeDataSource(RefondeResultats _resultats, double _T) {
    resultats_= _resultats;
    T_= _T;
  }

  /**
   * ATTENTION : La méthode ne s'occupe que du premier pas de temps.
   * @todo Gerer les pas de temps.
   * @param t double
   * @return double[]
   */
  public double[] getDonnees(double t) {
    double[] sxx=
      resultats_.getColonne(resultats_.indexOfColonne(RefondeResultats.nomResultats[RefondeResultats.SXX]))[0];
    double[] sxy=
      resultats_.getColonne(resultats_.indexOfColonne(RefondeResultats.nomResultats[RefondeResultats.SXY]))[0];
    double[] h= new double[sxx.length];
    for (int i= 0; i < h.length; i++)
      h[i]=
        sxx[i] * Math.cos(2 * Math.PI * t / T_)
          + sxy[i] * Math.sin(2 * Math.PI * t / T_);
    //    double[] h=new double[resultats_.lignes.length];
    //    int k=0;
    //    for (int i=0;i<resultats_.lignes.length;i++) {
    //      double pi=Math.PI;
    //      double nr=resultats_.lignes[i].x1;
    //      double ni=resultats_.lignes[i].phaseHauteur;
    //      h[i]=nr*Math.cos(2*pi*t/T_)+ni*Math.sin(2*pi*t/T_);
    //    }
    return h;
  }
}
