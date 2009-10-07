/*
 * @file         RefondeSymboleData.java
 * @creation     2000-05-15
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.ebli.geometrie.GrData;
/**
 * Un objet data pour les symbole.
 *
 * @version      $Id: RefondeSymboleData.java,v 1.6 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeSymboleData implements GrData {
  /**
   * Angle associé au symbole visualisé
   */
  public RefondeAngle angle= null;
}
