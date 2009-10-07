/*
 * @file         RefondePnDomaineDigueEditor.java
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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.memoire.bu.BuGridLayout;
/**
 * Une boite de dialogue affichant les propriétés avant le maillage.
 *
 * @version      $Id: RefondePnDomaineDigueEditor.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnDomaineDigueEditor
  extends JPanel
  implements ActionListener, AdjustmentListener, ChangeListener, FocusListener {
  //private RefondeDomaineDigue doma_;
  private int[] nbElements_;
  private int nTroncon_= 0;
  JLabel lbTroncon= new JLabel();
  BuGridLayout lyPanel= new BuGridLayout();
  ButtonGroup bgPanel= new ButtonGroup();
  JTextField tfNumTroncon= new JTextField();
  JLabel lbNbElements= new JLabel();
  JTextField tfNbElements= new JTextField();
  Border bdPanel;
  TitledBorder titledBorder1;
  JPanel pnNumTroncon= new JPanel();
  BuGridLayout lyNumTroncon= new BuGridLayout();
  JButton btMnNumTroncon= new JButton();
  JButton btPlNumTroncon= new JButton();
  TitledBorder titledBorder2;
  public RefondePnDomaineDigueEditor() {
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
    titledBorder1= new TitledBorder("");
    titledBorder2= new TitledBorder("");
    lyPanel.setColumns(4);
    lyPanel.setHgap(5);
    setLayout(lyPanel);
    setBorder(bdPanel);
    lbTroncon.setText("Tronçon");
    tfNumTroncon.setPreferredSize(new Dimension(40, 21));
    tfNumTroncon.setEditable(false);
    tfNumTroncon.setHorizontalAlignment(SwingConstants.RIGHT);
    lbNbElements.setText("Nombre d'éléments");
    tfNbElements.addFocusListener(this);
    pnNumTroncon.setLayout(lyNumTroncon);
    tfNbElements.setPreferredSize(new Dimension(40, 21));
    tfNbElements.setHorizontalAlignment(SwingConstants.RIGHT);
    btMnNumTroncon.setBorder(BorderFactory.createEtchedBorder());
    btMnNumTroncon.setText("-");
    btMnNumTroncon.addActionListener(this);
    btPlNumTroncon.setBorder(BorderFactory.createEtchedBorder());
    btPlNumTroncon.setText("+");
    btPlNumTroncon.addActionListener(this);
    lyNumTroncon.setHgap(1);
    add(lbTroncon, null);
    add(pnNumTroncon, null);
    pnNumTroncon.add(btMnNumTroncon, null);
    pnNumTroncon.add(tfNumTroncon, null);
    pnNumTroncon.add(btPlNumTroncon, null);
    add(lbNbElements, null);
    add(tfNbElements, null);
  }
  /**
   * Initialisation de la boite de dialogue
   */
  public void initialise(RefondeDomaineDigue _doma) {
    //doma_= _doma;
    nTroncon_= 0;
    int[] nbElements= _doma.getNbElements();
    nbElements_= new int[nbElements.length];
    System.arraycopy(nbElements, 0, nbElements_, 0, nbElements.length);
    tfNumTroncon.setText("" + (nTroncon_ + 1));
    tfNbElements.setText("" + nbElements_[nTroncon_]);
  }
  /**
   * Retourne le nombre d'éléments par troncon
   */
  public int[] getNbElements() {
    return nbElements_;
  }
  /**
   * Gestion des ajustements
   */
  public void adjustmentValueChanged(AdjustmentEvent _evt) {
    System.out.println("Ajustement");
    tfNumTroncon.setText("" + _evt.getValue());
    tfNbElements.setText("" + nbElements_[_evt.getValue() - 1]);
  }
  /**
   * Gestion des actions
   */
  public void actionPerformed(ActionEvent _evt) {
    // Bouton moins de numéro de troncon
    if (_evt.getSource() == btMnNumTroncon) {
      if (nTroncon_ != 0) {
        nTroncon_--;
        tfNumTroncon.setText("" + (nTroncon_ + 1));
        tfNbElements.setText("" + nbElements_[nTroncon_]);
      }
    }
    // Bouton plus de numéro de troncon
    if (_evt.getSource() == btPlNumTroncon) {
      if (nTroncon_ != nbElements_.length - 1) {
        nTroncon_++;
        tfNumTroncon.setText("" + (nTroncon_ + 1));
        tfNbElements.setText("" + nbElements_[nTroncon_]);
      }
    }
  }
  /**
   * Gestion de la perte de focus
   */
  public void focusGained(FocusEvent _evt) {}
  public void focusLost(FocusEvent _evt) {
    // Modification de la valeur du nombre d'éléments
    if (_evt.getSource() == tfNbElements)
      nbElements_[nTroncon_]= Integer.parseInt(tfNbElements.getText());
  }
  public void stateChanged(ChangeEvent _evt) {
    System.out.println("State changed");
  }
}
