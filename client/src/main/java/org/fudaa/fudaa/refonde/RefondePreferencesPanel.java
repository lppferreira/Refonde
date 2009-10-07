/*
 * @file         RefondePreferencesPanel.java
 * @creation     1999-11-20
 * @modification $Date: 2006-09-08 16:04:27 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.memoire.bu.BuAbstractPreferencesPanel;
import com.memoire.bu.BuGridLayout;
import com.memoire.bu.BuLib;
/**
 * Panneau de preferences pour Refonde.
 *
 * @version      $Id: RefondePreferencesPanel.java,v 1.8 2006-09-08 16:04:27 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePreferencesPanel
  extends BuAbstractPreferencesPanel
  implements KeyListener {
  RefondePreferences options_;
  RefondeImplementation refonde_;
  JTextField tfNoeuds;
  JTextField tfElements;

  public String getTitle() {
    return "Refonde";
  }

  // Constructeur
  public RefondePreferencesPanel(RefondeImplementation _refonde) {
    super();
    options_= RefondePreferences.REFONDE;
    refonde_= _refonde;
    BuGridLayout ly= new BuGridLayout(2, 5, 5, false, false);
    JPanel p= new JPanel();
    p.setBorder(new TitledBorder("Limites de Refonde"));
    p.setLayout(ly);
    p.add(new JLabel("Nombre maximum de noeuds:"));
    tfNoeuds= new JTextField();
    tfNoeuds.setPreferredSize(new Dimension(70, 21));
    tfNoeuds.addKeyListener(this);
    p.add(tfNoeuds);
    p.add(new JLabel("Nombre maximum d'éléments:"));
    tfElements= new JTextField();
    tfElements.setPreferredSize(new Dimension(70, 21));
    tfElements.addKeyListener(this);
    p.add(tfElements);
    //    p.add(new JPanel());
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(5, 5, 5, 5));
    add("Center", p);
    BuLib.computeMnemonics(this);
    updateComponents();
  }

  // Evenements
  public void keyPressed(KeyEvent _evt) {
    setDirty(true);
  }
  public void keyTyped(KeyEvent _evt) {}
  public void keyReleased(KeyEvent _evt) {}

  // Methodes publiques

  public boolean isPreferencesValidable() {
    return true;
  }

  public void validatePreferences() {
    fillTable();
    options_.writeIniFile();
  }

  public boolean isPreferencesApplyable() {
    return false;
  }

  public void applyPreferences() {
    fillTable();
    options_.applyOn(refonde_);
  }

  public boolean isPreferencesCancelable() {
    return true;
  }

  public void cancelPreferences() {
    options_.readIniFile();
    updateComponents();
  }

  // Methodes privees

  private void fillTable() {
    options_.putStringProperty(
      "refonde.nombre_maxi_noeuds",
      tfNoeuds.getText());
    options_.putStringProperty(
      "refonde.nombre_maxi_elements",
      tfElements.getText());
    setDirty(false);
  }

  private void updateComponents() {
    tfNoeuds.setText(options_.getStringProperty("refonde.nombre_maxi_noeuds"));
    tfElements.setText(
      options_.getStringProperty("refonde.nombre_maxi_elements"));
    setDirty(false);
  }
}
