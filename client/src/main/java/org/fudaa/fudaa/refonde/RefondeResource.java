/*
 * @file         RefondeResource.java
 * @creation     1999-06-25
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import com.memoire.bu.BuResource;
/**
 * Classe de gestion des ressources Refonde.
 *
 * @version      $Id: RefondeResource.java,v 1.4 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeResource extends BuResource {
  public final static RefondeResource REFONDE= new RefondeResource();
  // Flag de débug (false : Pas de débug, true : Débug)
  public final static boolean DEBUG= false;
}
