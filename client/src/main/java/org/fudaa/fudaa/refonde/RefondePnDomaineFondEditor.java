/*
 * @file         RefondePnDomaineFondEditor.java
 * @creation     1999-07-09
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.memoire.bu.BuGridLayout;
/**
 * Une boite de dialogue affichant les propriétés avant le maillage.
 *
 * @version      $Id: RefondePnDomaineFondEditor.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnDomaineFondEditor
  extends JPanel
  implements ActionListener {
  JLabel lbPeriode= new JLabel();
  JTextField tfPeriode= new JTextField();
  JLabel lbNbNoeuds= new JLabel();
  JTextField tfNbNoeuds= new JTextField();
  JRadioButton rblgOnde= new JRadioButton();
  JRadioButton rbClassic= new JRadioButton();
  BuGridLayout lyPanel= new BuGridLayout();
  JCheckBox cbAireMaxi= new JCheckBox();
  JTextField tfAireMaxi= new JTextField();
  JLabel lbDummy= new JLabel();
  JLabel lbDummy2= new JLabel();
  Border bdPanel;
  ButtonGroup bgPanel= new ButtonGroup();
  public RefondePnDomaineFondEditor() {
    super();
    jbInit();
  }
  public void jbInit() {
    bdPanel=
      BorderFactory.createCompoundBorder(
        new EtchedBorder(
          EtchedBorder.LOWERED,
          Color.white,
          new Color(134, 134, 134)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    lbPeriode.setText("Période de houle");
    tfPeriode.setPreferredSize(new Dimension(70, 19));
    lbNbNoeuds.setText("Nombre de noeuds par longueur d'onde");
    tfNbNoeuds.setPreferredSize(new Dimension(70, 19));
    rblgOnde.setText("Maillage par longueur d'onde");
    rblgOnde.addActionListener(this);
    rbClassic.setText("Maillage classique");
    rbClassic.addActionListener(this);
    bgPanel.add(rblgOnde);
    bgPanel.add(rbClassic);
    lyPanel.setColumns(2);
    lyPanel.setHgap(5);
    setLayout(lyPanel);
    setBorder(bdPanel);
    cbAireMaxi.setEnabled(false);
    add(rblgOnde, null);
    add(lbDummy, null);
    add(lbPeriode, null);
    add(tfPeriode, null);
    add(lbNbNoeuds, null);
    add(tfNbNoeuds, null);
    add(rbClassic, null);
    add(lbDummy2, null);
    add(cbAireMaxi, null);
    add(tfAireMaxi, null);
    tfAireMaxi.setPreferredSize(new Dimension(70, 19));
    tfAireMaxi.setEnabled(false);
    cbAireMaxi.setText("Aire maximum autorisée pour les éléments");
    cbAireMaxi.addActionListener(this);
    cbAireMaxi.setSelected(false);
  }
  /**
   * Initialisation de la boite de dialogue
   */
  public void initialise(RefondeDomaineFond _doma) {
    tfPeriode.setText("" + _doma.getPeriodeHoule());
    tfNbNoeuds.setText("" + _doma.getNbNoeudsOnde());
    tfAireMaxi.setText("" + _doma.getAireMaxi());
    cbAireMaxi.setSelected(_doma.getAireMaxi() > 0);
    if (_doma.getTypeMaillage() == RefondeDomaineFond.CLASSIQUE)
      rbClassic.doClick(0);
    else
      rblgOnde.doClick(0);
  }
  /**
   * Retourne la période de houle
   */
  public double getPeriodeHoule() {
    return Double.parseDouble(tfPeriode.getText());
  }
  /**
   * Retourne le nombre de noeuds par longueur d'onde
   */
  public int getNombreNoeuds() {
    return Integer.parseInt(tfNbNoeuds.getText());
  }
  /**
   * Retourne la valeur de l'aire maxi
   */
  public double getAireMaxi() {
    return Double.parseDouble(tfAireMaxi.getText());
  }
  /**
   * Retourne l'activation du maillage par longueur d'onde
   */
  public boolean isOptionOSelected() {
    return rblgOnde.isSelected();
  }
  /**
   * Retourne l'activation du controle de l'aire
   */
  public boolean isOptionASelected() {
    return cbAireMaxi.isSelected();
  }
  /**
   * Gestion des actions
   */
  public void actionPerformed(ActionEvent _evt) {
    // Option maillage par longueur d'onde
    if (_evt.getSource() == rblgOnde) {
      lbNbNoeuds.setEnabled(true);
      tfNbNoeuds.setEnabled(true);
      lbPeriode.setEnabled(true);
      tfPeriode.setEnabled(true);
      cbAireMaxi.setEnabled(false);
      tfAireMaxi.setEnabled(false);
    }
    // Option maillage classique
    if (_evt.getSource() == rbClassic) {
      lbNbNoeuds.setEnabled(false);
      tfNbNoeuds.setEnabled(false);
      lbPeriode.setEnabled(false);
      tfPeriode.setEnabled(false);
      cbAireMaxi.setEnabled(true);
      if (cbAireMaxi.isSelected())
        tfAireMaxi.setEnabled(true);
    }
    // Option aire maxi
    if (_evt.getSource() == cbAireMaxi) {
      if (cbAireMaxi.isSelected())
        tfAireMaxi.setEnabled(true);
      else
        tfAireMaxi.setEnabled(false);
    }
  }
}
