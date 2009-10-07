/*
 * @file         RefondePreferences.java
 * @creation     1999-11-20
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import com.memoire.bu.BuPreferences;
/**
 * Preferences pour Refonde.
 *
 * @version      $Id: RefondePreferences.java,v 1.8 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePreferences extends BuPreferences {
  public final static RefondePreferences REFONDE= new RefondePreferences();
  public RefondePreferences() {
    super();
    //    if (getStringProperty("refonde.nombre_maxi_noeuds")==null)
    //      putStringProperty("refonde.nombre_maxi_noeuds","10000");
    //    if (getStringProperty("refonde.nombre_maxi_elements")==null)
    //      putStringProperty("refonde.nombre_maxi_elements","30000");
    putIntegerProperty(
      "refonde.nombre_maxi_noeuds",
      getIntegerProperty("refonde.nombre_maxi_noeuds", 500000));
    putIntegerProperty(
      "refonde.nombre_maxi_elements",
      getIntegerProperty("refonde.nombre_maxi_elements", 1000000));
  }
  public void applyOn(Object _o) {
    if (!(_o instanceof RefondeImplementation))
      throw new RuntimeException("" + _o + " is not a RefondeImplementation.");
    //RefondeImplementation _appli=(RefondeImplementation)_o;
  }
  public int nbMaxNoeuds() {
    return getIntegerProperty("refonde.nombre_maxi_noeuds");
  }
  public int nbMaxElements() {
    return getIntegerProperty("refonde.nombre_maxi_elements");
  }
}
