/*
 * @file         RefondeServeurDunes.java
 * @creation     2001-04-24
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.dodico.corba.calcul.ICalcul;
import org.fudaa.dodico.corba.dunes.ICalculDunesHelper;

import org.fudaa.dodico.objet.CDodico;
import org.fudaa.dodico.objet.UsineLib;
/**
 * Serveur de calcul Dunes.
 *
 * @version      $Id: RefondeServeurDunes.java,v 1.7 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeServeurDunes {
  /**
   * Serveur de calcul.
   */
  private static ICalcul serveur_= null;
  /**
   * Recherche ou non d'un serveur distant.
   */
  public static boolean distant= false;
  /**
   * Initialisation du serveur de calcul.
   * @param _distant
   *  <i>true</i>Recherche un serveur de calcul distant, et crée un serveur
   *             local en cas d'échec.
   *  <i>false</i>Crée un serveur local sans recherche d'un distant.
   */
  public static void initialiser(boolean _distant) {
    if (_distant) {
      System.out.println("Recherche d'un serveur Dunes distant...");
      serveur_=
        ICalculDunesHelper.narrow(
          CDodico.findServerByInterface("::dunes::ICalculDunes", 4000));
    }
    if (serveur_ == null) {
      System.out.println("Creation d'un serveur Dunes local...");
      serveur_= UsineLib.findUsine().creeDunesCalculDunes();
    }
  }
  /**
   * Retourne le serveur associé ou null s'il n'a pas été initialisé.
   * @return Le serveur de calcul.
   */
  public static ICalcul getServeur() {
    if (serveur_ == null)
      initialiser(distant);
    return serveur_;
  }
  /**
   * Retourne si le serveur a pu être joint.
   * @return <i>true</i> Le serveur est présent.
   *         <i>false</i> Le serveur est inexistant
   */
  //  public boolean serveurExiste() { return serveur_!=null; }
  /**
   * Retourne si le calcul s'est bien déroulé.
   * @return <i>true</i> : Ok, <i>false</i> : Problème.
   */
  //  public boolean calculEstOK() {
  //    return serveurExiste() ? ((ICalculReflux3d)serveur_).estOK():false;
  //  }
  /**
   * Retourne la trace d'exécution du calcul.
   * @return La chaine d'exécution.
   */
  //  public String calculTraceExecution() {
  //    return serveurExiste() ? ((ICalculReflux3d)serveur_).traceExecution():null;
  //  }
}
