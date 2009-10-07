/*
 * @file         RefondeGroupeProprietes.java
 * @creation     1999-06-28
 * @modification $Date: 2005-05-20 23:27:16 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

/**
 * Classe de regroupement de propriétés suivant la nature.
 *
 * @version      $Id: RefondeGroupeProprietes.java,v 1.5 2005-05-20 23:27:16 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeGroupeProprietes {
  private int type_;
  private RefondePropriete[] proprietes_;

  public static final int HOULE_BORD_OUVERT_SORTIE= 0;
  public static final int HOULE_BORD_OUVERT_ENTREE= 1;
  public static final int HOULE_BORD_SEMI_REFLECHISSANT= 2;
  public static final int HOULE_FOND_PAROI_PERFOREE= 3;
  public static final int HOULE_FOND_DIGUE_TRANSMISSIBLE= 4;
  public static final int SEICHE_BORD_OUVERT=5;
  public static final int SEICHE_BORD_FERME=6;

  /**
   * Attributs de classes, architecture des groupes de propriétés
   */

  // Types
  public static final int[] types={
    HOULE_BORD_OUVERT_SORTIE,
    HOULE_BORD_OUVERT_ENTREE,
    HOULE_BORD_SEMI_REFLECHISSANT,
    HOULE_FOND_PAROI_PERFOREE,
    HOULE_FOND_DIGUE_TRANSMISSIBLE ,
    SEICHE_BORD_OUVERT,
    SEICHE_BORD_FERME
  };

  // Noms des groupes de propriétés visibles depuis l'ihm
  public static final String[] labels={
    "Bord ouvert en sortie",
    "Bord ouvert en entrée",
    "Bord semi réflechissant",
    "Paroi perforée",
    "Digue transmissible",
    "Bord ouvert",
    "Bord fermé"
  };

  // Types des propriétés pour le type de groupe
  public static final int[][] typesProprietes={
    {},
    {},
    { RefondePropriete.REFLEXION },

    { RefondePropriete.PERTE_CHARGE_R,
      RefondePropriete.PERTE_CHARGE_I,
      RefondePropriete.POROSITE,
      RefondePropriete.CONTRACTION,
      RefondePropriete.INTG_VERTICALE },

    { RefondePropriete.REFLEXION,
      RefondePropriete.TRANSMISSION,
      RefondePropriete.DP_REFLEXION,
      RefondePropriete.DP_TRANSMISSION },
    {},
    {}
  };

  // Imposition des proprietes
  public static final int[][] imposeProprietes={
    {},
    {},
    { 1 },
    { 1, 1, 1, 1, 1 },
    { 1, 1, 1, 1 },
    {},
    {}
  };

  // Comportement des proprietes (STATIONNAIRE, TRANSITOIRE, MIXTE)
  public static final int[][] cas={
    {},
    {},
    { RefondePropriete.STATIONNAIRE },
    { RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE },
    { RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE,
      RefondePropriete.STATIONNAIRE },
    {},
    {}
  };

  // Valeurs par défaut
  public static final double[][] valeursDefaut={
    {},
    {},
    { 1. },
    { 1., 0., 0., 0., 1. },
    { 1., 0., 0., 0. },
    {},
    {}
  };

  /**
   * Création d'un groupe de propriétés de type donné. Les propriétés sont créées
   * avec leurs valeurs par défaut
   */
  public RefondeGroupeProprietes(int _type) {
    int[] tpPrps= typesProprietes[_type];
    double[] vlDefs= valeursDefaut[_type];
    RefondePropriete[] prps= new RefondePropriete[tpPrps.length];
    for (int i= 0; i < tpPrps.length; i++)
      prps[i]= new RefondePropriete(tpPrps[i], vlDefs[i]);
    type_= _type;
    proprietes_= prps;
  }

  /**
   * Création d'un groupe de proprietes
   */
  public RefondeGroupeProprietes(int _type, RefondePropriete[] _proprietes) {
    type_= _type;
    proprietes_= _proprietes;
  }

  public int getType() {
    return type_;
  }

  public RefondePropriete[] getProprietes() {
    return proprietes_;
  }
}
