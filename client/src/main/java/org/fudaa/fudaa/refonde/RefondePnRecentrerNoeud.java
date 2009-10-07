/*
 * @file         RefondePnRecentrerNoeud.java
 * @creation     1999-07-09
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
/**
 * Un panneau pour le recentrage de la vue sur un noeud.
 *
 * @version      $Id: RefondePnRecentrerNoeud.java,v 1.5 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnRecentrerNoeud extends JPanel {
  boolean selEventSent_;
  JLabel lbNumNoeud= new JLabel();
  JTextField tfNumNoeud= new JTextField();
  /**
   * Constructeur
   */
  public RefondePnRecentrerNoeud() {
    super();
    jbInit();
  }
  /**
   * Définition de l'IU
   */
  private void jbInit() {
    lbNumNoeud.setText("Numéro du noeud:");
    tfNumNoeud.setPreferredSize(new Dimension(70, 21));
    this.add(lbNumNoeud, null);
    this.add(tfNumNoeud, null);
  }
  /**
   * Retourne le numéro de noeud.
   * @return <i>-1</i> Si la valeur du noeud n'est pas valide.
   */
  public int getNumeroNoeud() {
    try {
      return Integer.parseInt(tfNumNoeud.getText());
    } catch (NumberFormatException _exc) {
      return -1;
    }
  }
  /**
   * Pour test.
   */
  public static void main(String[] args) {
    JFrame f= new JFrame("Recentrer la vue");
    RefondePnRecentrerNoeud pn= new RefondePnRecentrerNoeud();
    f.getContentPane().add(pn, BorderLayout.CENTER);
    f.pack();
    f.show();
  }
}
