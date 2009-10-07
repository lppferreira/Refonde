/*
 * @file         RefondeSupporteGroupeProprietes.java
 * @creation     2000-03-22
 * @modification $Date: 2005-08-16 14:21:33 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * Une interface pour les objets supportant des groupes de propriétés (Domaines,
 * contours, polyligne, etc.).
 *
 * @version      $Id: RefondeSupporteGroupeProprietes.java,v 1.4 2005-08-16 14:21:33 deniger Exp $
 * @author       Bertrand Marchand
 */
public interface RefondeSupporteGroupeProprietes {
  /**
   * Affecte le groupe de propriétés
   */
  void setGroupeProprietes(RefondeGroupeProprietes _gPrp);
  /**
   * Retourne le groupe de propriétés (null si aucun groupe de propriétés
   * affecté)
   */
  RefondeGroupeProprietes getGroupeProprietes();
}
