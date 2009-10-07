/*
 * @file         RefondeIOException.java
 * @creation     2000-05-15
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.io.IOException;
/**
 * Une exception lancée en cas de problème lors de la lecture/ecriture des
 * données. Elle permet de faire la différence entre les IOException lancées
 * de facon non controlées par les classes java et les IOException controlées
 * (à la suite d'un controle de cohérence de la lecture).
 *
 * @version      $Id: RefondeIOException.java,v 1.4 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
class RefondeIOException extends IOException {
  /**
   * Création d'une exception sans message.
   */
  public RefondeIOException() {
    super();
  }
  /**
   * Création d'une exception avec un message.
   * @param s Message.
   */
  public RefondeIOException(String s) {
    super(s);
  }
}
