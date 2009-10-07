/*
 * @file         RefondeInformations.java
 * @creation     1999-11-22
 * @modification $Date: 2006-09-19 15:10:23 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.memoire.bu.BuGridLayout;
import com.memoire.bu.BuLib;

import org.fudaa.ebli.geometrie.GrMaillageElement;
/**
 * Une panel d'informations.
 *
 * @version      $Id: RefondeInformations.java,v 1.7 2006-09-19 15:10:23 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeInformations extends JPanel {
  JLabel laNbNoeuds;
  JLabel laNbElements;
  public RefondeInformations() {
    JLabel la;
    setLayout(new BuGridLayout(2, 5, 0, false, false));
    setForeground(Color.black);
    // Nombre de noeuds
    add(la= new JLabel("Nombre de noeuds"));
    la.setFont(BuLib.deriveFont("List", Font.PLAIN, -2));
    add(la= new JLabel());
    la.setFont(BuLib.deriveFont("List", Font.PLAIN, -2));
    laNbNoeuds= la;
    // Nombre d'éléments
    add(la= new JLabel("Nombre d'éléments"));
    la.setFont(BuLib.deriveFont("List", Font.PLAIN, -2));
    add(la= new JLabel());
    la.setFont(BuLib.deriveFont("List", Font.PLAIN, -2));
    laNbElements= la;
  }
  /**
   * Mise a jour des informations
   */
  public void update(RefondeProjet _projet) {
    if (_projet == null) {
    	laNbNoeuds.setText("");
    	laNbElements.setText("");
    	return;
    }
    int val;
    GrMaillageElement mail=
      RefondeMaillage.creeSuperMaillage(
        _projet.getGeometrie().scene_.getMaillages());
    // Nombre de noeuds
    val= mail.noeuds().length;
    if (val > RefondePreferences.REFONDE.nbMaxNoeuds())
      laNbNoeuds.setForeground(Color.red);
    else
      laNbNoeuds.setForeground(null);
    laNbNoeuds.setText("" + val);
    // Nombre d'éléments
    val= mail.elements().length;
    if (val > RefondePreferences.REFONDE.nbMaxElements())
      laNbElements.setForeground(Color.red);
    else
      laNbElements.setForeground(null);
    laNbElements.setText("" + val);
  }
}
