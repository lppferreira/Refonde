/*
 * @file         RefondePropriete.java
 * @creation     1999-06-28
 * @modification $Date: 2005-08-16 14:21:33 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * Classe de propri�t�s.
 *
 * @version      $Id: RefondePropriete.java,v 1.4 2005-08-16 14:21:33 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondePropriete {
  private int type_;
  private RefondeCourbe courbe_;
  private double valeur_;
  private int cmpt_;
  /**
   * Comportement MIXTE
   */
  public static final int MIXTE= 0;
  /**
   * Comportement STATIONNAIRE
   */
  public static final int STATIONNAIRE= 1;
  /**
   * Comportement TRANSITOIRE
   */
  public static final int TRANSITOIRE= 2;
  public static final int REFLEXION= 0;
  public static final int TRANSMISSION= 1;
  public static final int POROSITE= 2;
  public static final int INTG_VERTICALE= 3;
  public static final int DP_REFLEXION= 4;
  public static final int DP_TRANSMISSION= 5;
  public static final int CONTRACTION= 6;
  public static final int PERTE_CHARGE_R= 7;
  public static final int PERTE_CHARGE_I= 8;
  /**
   * Attributs de classes, architecture des groupes de propri�t�s
   */
  // Noms des groupes de propri�t�s visibles depuis l'ihm
  public static final String[] labels=
    {
      "Coefficient de r�flexion",
      "Coefficient de transmission",
      "Porosit�",
      "Int�gration verticale",
      "D�phasage de r�flexion",
      "D�phasage de transmission",
      "Coefficient de contraction",
      "Initialisation perte de charge (partie r�elle)",
      "Initialisation perte de charge (partie imaginaire)" };
  /**
   * Cr�ation d'une propriete transitoire
   */
  public RefondePropriete(int _type, RefondeCourbe _courbe) {
    type_= _type;
    courbe_= _courbe;
    cmpt_= TRANSITOIRE;
  }
  /**
   * Cr�ation d'une propri�t� stationnaire
   */
  public RefondePropriete(int _type, double _valeur) {
    type_= _type;
    valeur_= _valeur;
    cmpt_= STATIONNAIRE;
  }
  public int getType() {
    return type_;
  }
  public RefondeCourbe getCourbe() {
    if (cmpt_ != TRANSITOIRE)
      throw new UnsupportedOperationException();
    return courbe_;
  }
  public double getValeur() {
    if (cmpt_ != STATIONNAIRE)
      throw new UnsupportedOperationException();
    return valeur_;
  }
  public int getComportement() {
    return cmpt_;
  }
}
