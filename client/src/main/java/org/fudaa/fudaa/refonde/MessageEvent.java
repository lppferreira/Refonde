/*
 * @file         MessageEvent.java
 * @creation     2001-04-25
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.EventObject;
/**
 * Un évènement permettant d'envoyer un message. Cet evènement est envoyé par un
 * objet pour donner par exemple l'état d'avancement d'une méthode.
 *
 * @version      $Id: MessageEvent.java,v 1.4 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand 
 */
public class MessageEvent extends EventObject {
  private int progres_;
  private String message_;
  /**
   * Construction d'un évènement.
   * @param _source Objet déclencheur de l'évènement. En général un objet
   * modèle.
   * @param _message Le message.
   * @param _progres La progression de la tâche en train d'être accomplie.
   * Entre 0 et 100. -1 : Progression inchangée par rapport au dernier appel.
   */
  public MessageEvent(Object _source, String _message, int _progres) {
    super(_source);
    progres_= _progres;
    message_= _message;
  }
  /**
   * Retourne le message.
   * @return Le message.
   */
  public String getMessage() {
    return message_;
  }
  /**
   * Retourne la progression de la tâche.
   * @return La progression.
   */
  public int getProgression() {
    return progres_;
  }
}
