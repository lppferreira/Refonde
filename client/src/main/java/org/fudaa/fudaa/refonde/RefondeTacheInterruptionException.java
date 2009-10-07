/*
 * @file         RefondeTacheInterruptionException.java
 * @creation     2000-05-15
 * @modification $Date: 2003-11-25 10:14:17 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * Cette exception sert � notifier un arret demand� sur la tache en cours.
 * <p>
 * Elle est lanc�e par la m�thode RefondeTacheOperation.notifieArretDemande()
 * si :
 * <ul>
 *   <li> Le thread courant est de type RefondeTacheOperation.
 *   <li> Un arret a �t� demand� sur le thread courant par une autre tache
 *        (m�thode RefondeTacheOperation.stopWhenReady()).
 * </ul><p>
 * La m�thode RefondeTacheOperation.notifieArretDemande() doit �tre appel�e
 * suffisamment souvent dans les methodes ou on veut controler
 * l'interruption de la tache courante pour pouvoir lancer l'exception,
 * en particulier � l'interieur d'une boucle longue.
 * <p>
 * <b>Exemple d'utilisation :</b>
 * <p>
 * <pre>
 *   RefondeTacheOperation th=new RefondeTacheOperation(this,"Test",true) {
 *     public void act() {
 *       try {
 *         while (true) {
 *           RefondeTacheOperation.notifieArretDemande(); // Lancement de l'exception si l'arret a �t� demand�.
 *           System.out.println("Continue la boucle");
 *         }
 *       }
 *       catch (RefondeTacheInterruptionException _exc) {
 *         System.out.println("La tache courante a �t� interrompue");
 *       }
 *     }
 *   };
 *   th.start();
 *
 *   wait(1000);
 *   th.stopWhenReady();
 * </pre>
 *
 * @version      $Id: RefondeTacheInterruptionException.java,v 1.3 2003-11-25 10:14:17 deniger Exp $
 * @author       Bertrand Marchand
 *
 * @see RefondeTacheOperation#notifieArretDemande()
 * @see RefondeTacheOperation#stopWhenReady()
 */
public class RefondeTacheInterruptionException extends Exception {
  /**
   * Cr�ation d'une exception sans message.
   */
  public RefondeTacheInterruptionException() {
    super();
  }
  /**
   * Cr�ation d'une exception avec un message.
   * @param s Message.
   */
  public RefondeTacheInterruptionException(String s) {
    super(s);
  }
}
