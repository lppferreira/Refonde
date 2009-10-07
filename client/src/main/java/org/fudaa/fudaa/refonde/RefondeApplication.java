/*
 * @file         RefondeApplication.java
 * @creation     1999-06-25
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import com.memoire.bu.BuApplication;
/**
 * Application Refonde (parallèle à Applet).
 *
 * @version      $Id: RefondeApplication.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeApplication extends BuApplication {
  public RefondeApplication() {
    super();
    setImplementation(new RefondeImplementation());
  }
}
