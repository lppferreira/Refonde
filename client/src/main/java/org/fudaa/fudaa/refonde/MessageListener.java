/*
 * @file         MessageListener.java
 * @creation     2001-04-25
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.EventListener;
/**
 * Interface auditeur de l'evenement <I>MessageEvent</I>.
 *
 * @version      $Id: MessageListener.java,v 1.4 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand 
 */
public interface MessageListener extends EventListener {
  /**
   * Méthode déclenchée à la suite de l'envoie d'un message de progression.
   */
  void messageEnvoye(MessageEvent _evt);
}
