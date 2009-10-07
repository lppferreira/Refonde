/*
 * @file         RefondePnRecentrerElement.java
 * @creation     1999-07-09
 * @modification $Date: 2006-09-08 16:04:26 $
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
 * Un panneau pour le recentrage de la vue sur un élément.
 *
 * @version      $Id: RefondePnRecentrerElement.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnRecentrerElement extends JPanel {
  boolean selEventSent_;
  JLabel lbNumElement= new JLabel();
  JTextField tfNumElement= new JTextField();
  /**
   * Constructeur
   */
  public RefondePnRecentrerElement() {
    super();
    jbInit();
  }
  /**
   * Définition de l'IU
   */
  private void jbInit() {
    lbNumElement.setText("Numéro de l'élément:");
    tfNumElement.setPreferredSize(new Dimension(70, 21));
    this.add(lbNumElement, null);
    this.add(tfNumElement, null);
  }
  /**
   * Retourne le numéro de l'élément.
   * @return <i>-1</i> Si la valeur de l'élément n'est pas valide.
   */
  public int getNumeroElement() {
    try {
      return Integer.parseInt(tfNumElement.getText());
    } catch (NumberFormatException _exc) {
      return -1;
    }
  }
  /**
   * Pour test.
   */
  public static void main(String[] args) {
    JFrame f= new JFrame("Recentrer la vue");
    RefondePnRecentrerElement pn= new RefondePnRecentrerElement();
    f.getContentPane().add(pn, BorderLayout.CENTER);
    f.pack();
    f.show();
  }
}
