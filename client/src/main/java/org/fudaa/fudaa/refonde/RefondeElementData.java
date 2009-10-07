/*
 * @file         RefondeElementData.java
 * @creation     2000-05-15
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.ebli.geometrie.GrData;
/**
 * Un objet data pour les éléments.
 *
 * @version      $Id: RefondeElementData.java,v 1.6 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeElementData implements GrData {
  /**
   * Numéro d'optimisation de l'élément (correspondant au fichier .inp)
   */
  public int numero= 0;
}
