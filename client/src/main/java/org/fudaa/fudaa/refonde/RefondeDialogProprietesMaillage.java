/*
 * @file         RefondeDialogProprietesMaillage.java
 * @creation     1999-07-09
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
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
import com.memoire.bu.BuVerticalLayout;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue affichant les propriétés avant le maillage.
 *
 * @version      $Id: RefondeDialogProprietesMaillage.java,v 1.8 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogProprietesMaillage
  extends FudaaDialog
  implements ActionListener {
  JPanel pnProprietes= new JPanel();
  BuVerticalLayout lyProprietes= new BuVerticalLayout();
  JLabel lbPeriode= new JLabel();
  JTextField tfPeriode= new JTextField();
  JLabel lbNbNoeuds= new JLabel();
  JTextField tfNbNoeuds= new JTextField();
  JPanel pnOptions= new JPanel();
  JRadioButton rblgOnde= new JRadioButton();
  JRadioButton rbClassic= new JRadioButton();
  BuGridLayout lyOptions= new BuGridLayout();
  JCheckBox cbAireMaxi= new JCheckBox();
  JTextField tfAireMaxi= new JTextField();
  JLabel lbDummy= new JLabel();
  JLabel lbDummy2= new JLabel();
  Border bdOptions;
  ButtonGroup bgOptions= new ButtonGroup();
  public RefondeDialogProprietesMaillage() {
    this(null);
  }
  public RefondeDialogProprietesMaillage(Frame _parent) {
    // Le frame principal et le panel des boutons est créé et géré par la classe
    // mère. On ne crée que le panel principal.
    super(_parent, OK_CANCEL_OPTION);
    jbInit();
  }
  public void jbInit() {
    bdOptions=
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
    bgOptions.add(rblgOnde);
    bgOptions.add(rbClassic);
    bgOptions.setSelected(rblgOnde.getModel(), true);
    lyOptions.setColumns(2);
    lyOptions.setHgap(5);
    pnOptions.setLayout(lyOptions);
    pnOptions.setBorder(bdOptions);
    cbAireMaxi.setEnabled(false);
    pnOptions.add(rblgOnde, null);
    pnOptions.add(lbDummy, null);
    pnOptions.add(lbPeriode, null);
    pnOptions.add(tfPeriode, null);
    pnOptions.add(lbNbNoeuds, null);
    pnOptions.add(tfNbNoeuds, null);
    pnOptions.add(rbClassic, null);
    pnOptions.add(lbDummy2, null);
    pnOptions.add(cbAireMaxi, null);
    pnOptions.add(tfAireMaxi, null);
    tfAireMaxi.setPreferredSize(new Dimension(70, 19));
    tfAireMaxi.setEnabled(false);
    cbAireMaxi.setText("Aire maximum autorisée pour les éléments");
    cbAireMaxi.addActionListener(this);
    cbAireMaxi.setSelected(false);
    // Ajout des composants au panel
    lyProprietes.setVgap(5);
    pnProprietes.setLayout(lyProprietes);
    pnProprietes.add(pnOptions, null);
    pnAffichage_.add("Center", pnProprietes);
    setTitle("Propriétés du maillage");
    pack();
    setResizable(false);
  }
  /**
   * Initialisation de la boite de dialogue
   * @param _periodeHoule Période de houle
   * @param _nbNoeuds     Nombre de noeuds par longueur d'onde
   */
  public void initialise(
    double _periodeHoule,
    int _nbNoeuds,
    double _aireMaxi) {
    tfPeriode.setText("" + _periodeHoule);
    tfNbNoeuds.setText("" + _nbNoeuds);
    tfAireMaxi.setText("" + _aireMaxi);
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
