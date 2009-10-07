/*
 * @file         RefondeModeleVisuResultats.java
 * @creation     2004-04-06
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.util.HashSet;
import java.util.Iterator;

import org.fudaa.fudaa.commun.trace2d.ZModeleChangeEvent;
import org.fudaa.fudaa.commun.trace2d.ZModeleChangeListener;
import org.fudaa.fudaa.commun.trace2d.ZModeleValeur;

/**
 * Un modèle de visualisation des résultats basé sur les résultats du projet
 * avec changement possible du pas de temps sélectionné pour la colonne donnée.
 *
 * @version      $Id: RefondeModeleVisuResultats.java,v 1.3 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleVisuResultats implements ZModeleValeur {
  /** Résultats */
  private RefondeResultats res_=null;
  /** Le nom de la colonne à accéder. */
  private String nomCol_=null;
  /** Le pas de temps sélectionné */
  private Double t_=null;
  /** Listeners for modifications in modele. */
  private HashSet listeners_=new HashSet();

  /**
   * Constructeur. Se base sur une colonne de nom donné. Ainsi,
   * même si une autre colonne est supprimée, l'accès à la colonne est toujours
   * valable.
   *
   * @param _res RefondeResultats
   * @param _nomCol String
   */
  public RefondeModeleVisuResultats(RefondeResultats _res, String _nomCol) {
    res_=_res;
    nomCol_=_nomCol;
  }

  /**
   * Pour changer le pas de temps sur le modele.
   */
  public void setSelectedStep(Double _t) {
    t_= _t;
    fireModelChange(new ZModeleChangeEvent(this, ZModeleChangeEvent.VALUES_CHANGED));
  }

  /**
   * Retourne le pas de temps sélectionné.
   */
  public Double getSelectedStep() {
    return t_;
  }

  /**
   * Notification aux listeners.
   * @param _evt ZModeleChangeEvent
   */
  protected void fireModelChange(ZModeleChangeEvent _evt) {
    for (Iterator i= listeners_.iterator(); i.hasNext();) {
      ((ZModeleChangeListener)i.next()).modelChanged(_evt);
    }
  }

  // >>> ZmodeleValeur  --------------------------------------------------------

  public double valeur(int i) {
    if (t_==null) return Double.NaN;

    int iStp=res_.indexOfEtape(t_.doubleValue());
    int iCol=res_.indexOfColonne(nomCol_);
    if (iStp==-1 || iCol==-1) return Double.NaN;

    double[][] vals=res_.getEtape(iStp);
    if (i<0 || i>=vals[iCol].length) return Double.NaN;

    return vals[iCol][i];
  }

  public int nbValeurs() {
    if (t_==null) return 0;

    int ind=res_.indexOfEtape(t_.doubleValue());
    if (ind==-1) return 0;

    double[][] vals=res_.getEtape(ind);
    return vals.length>0 ? vals[0].length:0;
  }

  public double getMin() {
    double r= Double.NaN;
    if (nbValeurs() > 0)
      r= Double.POSITIVE_INFINITY;
    for (int i= 0; i < nbValeurs(); i++)
      r= Math.min(r, valeur(i));
    return r;
  }

  public double getMax() {
    double r= Double.NaN;
    if (nbValeurs() > 0)
      r= Double.NEGATIVE_INFINITY;
    for (int i= 0; i < nbValeurs(); i++)
      r= Math.max(r, valeur(i));
    return r;
  }

  public void addModelChangeListener(ZModeleChangeListener _listener) {
    listeners_.add(_listener);
  }

  public void removeModelChangeListener(ZModeleChangeListener _listener) {
    listeners_.remove(_listener);
  }

  // <<< ZmodeleValeur  --------------------------------------------------------
}
