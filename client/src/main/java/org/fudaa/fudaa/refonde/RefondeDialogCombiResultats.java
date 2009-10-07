/*
 * @file         RefondeDialogCombiResultats.java
 * @creation     2001-07-04
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.memoire.bu.BuFileFilter;

import org.fudaa.ebli.dialog.BFileChooser;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue permettant de combiner les r�sultats depuis un projet
 * courant et un projet s�lectionn�.
 * La liste des r�sultats combinables est fig�e, et ne peut �tre modifi�e par
 * l'utilisateur.
 *
 * @version      $Id: RefondeDialogCombiResultats.java,v 1.10 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogCombiResultats extends FudaaDialog {
  //public class RefondeDialogCombiResultats extends JDialog {
  BorderLayout lyThis= new BorderLayout();
  JPanel pnListes= new JPanel();
  JPanel pnPrjCombi= new JPanel();
  JLabel lbPrjCombi= new JLabel();
  JTextField tfPrjCombi= new JTextField();
  JButton btPrjCombi= new JButton();
  GridBagLayout lyPrjCombi= new GridBagLayout();
  Border bdpnPrjCombi;
  Border bdpnListes;
  Box bxButtons;
  JButton btAjouter= new JButton();
  JButton btSupprimer= new JButton();
  GridBagLayout lyListes= new GridBagLayout();
  Box bxCombiDispo;
  JScrollPane spCombiDispo= new JScrollPane();
  JList lsCombiDispo= new JList();
  JLabel lbCombiDispo= new JLabel();
  Box bxCombiLoad;
  JList lsCombiLoad= new JList();
  JScrollPane spCombiLoad= new JScrollPane();
  JLabel lbCombiLoad= new JLabel();
  Component cpBas;
  Component cpHaut;
  Component cpMilieu;
  DefaultListModel mdlsCombiLoad= new DefaultListModel();
  DefaultListModel mdlsCombiDispo= new DefaultListModel();
  BFileChooser diFcPrjCombi_= null;
  public JButton reponse;
  //private RefondeProjet prj_;
  public final static int[] icombinaisons= { // Combinaison possibles
    RefondeResultats.DIFF_HAUTEUR,
      RefondeResultats.DIFF_SXX,
      RefondeResultats.DIFF_SXY,
      RefondeResultats.DIFF_SYY,
      };
  public final static int[] iresUtils=
    { // R�sultats utilis�s pour la combinaison
    RefondeResultats.HAUTEUR_HOULE,
      RefondeResultats.SXX,
      RefondeResultats.SXY,
      RefondeResultats.SYY,
      };
  public RefondeDialogCombiResultats() {
    this(null);
  }
  public RefondeDialogCombiResultats(Frame _parent) {
    // Le frame principal et le panel des boutons est cr�� et g�r� par la classe
    // m�re. On ne cr�e que le panel principal.
    super(_parent, OK_CANCEL_OPTION);
    jbInit();
  }
  public void jbInit() {
    // Panneau liste des combinaisons disponibles
    lbCombiDispo.setMinimumSize(new Dimension(0, 17));
    lbCombiDispo.setText("Combinaisons disponibles:");
    lsCombiDispo.setModel(mdlsCombiDispo);
    lsCombiDispo
      .getSelectionModel()
      .addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent _evt) {
        listes_valueChanged(_evt, lsCombiDispo);
      }
    });
    spCombiDispo.setAlignmentX((float)0.0);
    spCombiDispo.setPreferredSize(new Dimension(160, 100));
    spCombiDispo.getViewport().add(lsCombiDispo, null);
    bxCombiDispo= Box.createVerticalBox();
    bxCombiDispo.add(lbCombiDispo, null);
    bxCombiDispo.add(spCombiDispo, null);
    // Panneau des boutons de gestion des combinaisons
    btAjouter.setAlignmentX((float)0.5);
    btAjouter.setText(">>");
    btAjouter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        btAjouter_actionPerformed(_evt);
      }
    });
    btAjouter.setEnabled(false);
    btSupprimer.setAlignmentX((float)0.5);
    btSupprimer.setText("<<");
    btSupprimer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        btSupprimer_actionPerformed(_evt);
      }
    });
    btSupprimer.setEnabled(false);
    cpBas= Box.createGlue();
    cpHaut= Box.createGlue();
    cpMilieu= Box.createVerticalStrut(8);
    bxButtons= Box.createVerticalBox();
    bxButtons.add(cpHaut, null);
    bxButtons.add(btAjouter, null);
    bxButtons.add(cpMilieu, null);
    bxButtons.add(btSupprimer, null);
    bxButtons.add(cpBas, null);
    // Panneau liste des combinaisons charg�es
    lbCombiLoad.setMinimumSize(new Dimension(0, 17));
    lbCombiLoad.setText("Combinaisons charg�es:");
    lsCombiLoad.setModel(mdlsCombiLoad);
    lsCombiLoad
      .getSelectionModel()
      .addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent _evt) {
        listes_valueChanged(_evt, lsCombiLoad);
      }
    });
    spCombiLoad.setAlignmentX((float)0.0);
    spCombiLoad.setPreferredSize(new Dimension(160, 100));
    spCombiLoad.getViewport().add(lsCombiLoad, null);
    bxCombiLoad= Box.createVerticalBox();
    bxCombiLoad.add(lbCombiLoad, null);
    bxCombiLoad.add(spCombiLoad, null);
    bdpnListes= BorderFactory.createEmptyBorder(5, 5, 5, 5);
    pnListes.setBorder(bdpnListes);
    pnListes.setLayout(lyListes);
    pnListes.add(
      bxButtons,
      new GridBagConstraints(
        1,
        0,
        1,
        1,
        0.0,
        1.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0),
        15,
        0));
    pnListes.add(
      bxCombiDispo,
      new GridBagConstraints(
        0,
        0,
        1,
        1,
        1.0,
        1.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0),
        0,
        0));
    pnListes.add(
      bxCombiLoad,
      new GridBagConstraints(
        3,
        0,
        1,
        1,
        1.0,
        2.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0),
        0,
        0));
    // Panneau du projet de combinaison
    lbPrjCombi.setText("Projet de combinaison: ");
    btPrjCombi.setText("...");
    btPrjCombi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        btPrjCombi_actionPerformed(_evt);
      }
    });
    bdpnPrjCombi= BorderFactory.createEmptyBorder(10, 5, 10, 5);
    pnPrjCombi.setLayout(lyPrjCombi);
    pnPrjCombi.setBorder(bdpnPrjCombi);
    pnPrjCombi.add(
      lbPrjCombi,
      new GridBagConstraints(
        0,
        0,
        1,
        1,
        0.0,
        0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.NONE,
        new Insets(0, 0, 0, 0),
        0,
        0));
    pnPrjCombi.add(
      tfPrjCombi,
      new GridBagConstraints(
        1,
        0,
        1,
        1,
        1.0,
        0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 5, 0, 5),
        0,
        0));
    pnPrjCombi.add(
      btPrjCombi,
      new GridBagConstraints(
        2,
        0,
        1,
        1,
        0.0,
        0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.NONE,
        new Insets(0, 0, 0, 0),
        0,
        0));
    pnAffichage_.setLayout(lyThis);
    pnAffichage_.add(pnListes, BorderLayout.CENTER);
    pnAffichage_.add(pnPrjCombi, BorderLayout.SOUTH);
    setTitle("Combinaisons de r�sultats");
    pack();
    // Dialogue de saisie du nom du projet de combinaison.
    BuFileFilter flt= new BuFileFilter("prf", "Projet refonde");
    diFcPrjCombi_= new BFileChooser();
    diFcPrjCombi_.setFileHidingEnabled(true);
    diFcPrjCombi_.setCurrentDirectory(new File(System.getProperty("user.dir")));
    diFcPrjCombi_.setMultiSelectionEnabled(false);
    diFcPrjCombi_.addChoosableFileFilter(flt);
    diFcPrjCombi_.setFileFilter(flt);
  }
  /**
   * Initialisation avec le projet courant
   */
  public void initialise(RefondeProjet _prj) {
    //prj_= _prj;

    // Pas de r�sultat disponible, l'action de combinaison des r�sultats ne doit
    // pas �tre disponible.
    if (!_prj.hasResultats()) return;

    RefondeResultats res= _prj.getResultats();
    mdlsCombiDispo.removeAllElements();
    mdlsCombiLoad.removeAllElements();
    for (int i= 0; i < icombinaisons.length; i++) {
//      if (res.getResultat(res.nomResultats[icombinaisons[i]]) == null)
      if (res.indexOfColonne(RefondeResultats.nomResultats[icombinaisons[i]])==-1)
        mdlsCombiDispo.addElement(RefondeResultats.nomResultats[icombinaisons[i]]);
      else
        mdlsCombiLoad.addElement(RefondeResultats.nomResultats[icombinaisons[i]]);
    }
  }
  /**
   * Bouton "Annuler" press�
   */
  protected void btCancelActionPerformed(ActionEvent _evt) {
    reponse= (JButton)_evt.getSource();
    super.btCancelActionPerformed(_evt);
  }
  /**
   * Bouton "Ok" press�, effacage du dialog
   */
  protected void btOkActionPerformed(ActionEvent _evt) {
    reponse= (JButton)_evt.getSource();
    super.btOkActionPerformed(_evt);
  }
  //----------------------------------------------------------------------------
  //--- Gestion des �v�nements -------------------------------------------------
  //----------------------------------------------------------------------------
  /**
   * M�thode appel�e quand un item d'une liste est s�lectionn�.
   */
  public void listes_valueChanged(ListSelectionEvent _evt, JList _liste) {
    if (_evt.getValueIsAdjusting())
      return;
    if (_liste.getSelectedIndices().length == 0) {
      btAjouter.setEnabled(false);
      btSupprimer.setEnabled(false);
      return;
    }
    if (_liste == lsCombiDispo)
      lsCombiLoad.clearSelection();
    else
      lsCombiDispo.clearSelection();
    btAjouter.setEnabled(_liste == lsCombiDispo);
    btSupprimer.setEnabled(_liste == lsCombiLoad);
  }
  /**
   * M�thode appel�e quand le bouton ajouter est actionn�.
   */
  public void btAjouter_actionPerformed(ActionEvent _evt) {
    DefaultListModel mdlsCombiLoadTmp= (DefaultListModel)lsCombiLoad.getModel();
    DefaultListModel mdlsCombiDispoTmp= (DefaultListModel)lsCombiDispo.getModel();
    int[] itemSelects= lsCombiDispo.getSelectedIndices();
    for (int i= itemSelects.length - 1; i >= 0; i--)
      mdlsCombiLoadTmp.addElement(mdlsCombiDispoTmp.remove(itemSelects[i]));
    btAjouter.setEnabled(false);
  }
  /**
   * M�thode appel�e quand le bouton supprimer est actionn�.
   */
  public void btSupprimer_actionPerformed(ActionEvent _evt) {
    DefaultListModel mdlsCombiLoadTmp= (DefaultListModel)lsCombiLoad.getModel();
    DefaultListModel mdlsCombiDispoTmp= (DefaultListModel)lsCombiDispo.getModel();
    int[] itemSelects= lsCombiLoad.getSelectedIndices();
    for (int i= itemSelects.length - 1; i >= 0; i--)
      mdlsCombiDispoTmp.addElement(mdlsCombiLoadTmp.remove(itemSelects[i]));
    btSupprimer.setEnabled(false);
  }
  /**
   * M�thode activ�e quand le bouton btPrjCombi est actionn�
   */
  void btPrjCombi_actionPerformed(ActionEvent _evt) {
    diFcPrjCombi_.setSelectedFile(new File(tfPrjCombi.getText()));
    int ret= diFcPrjCombi_.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
      tfPrjCombi.setText("" + diFcPrjCombi_.getSelectedFile());
    }
  }
  public static void main(String[] argv) {
    RefondeDialogCombiResultats di= new RefondeDialogCombiResultats();
    di.mdlsCombiLoad.addElement("Diff�rences de Sxy");
    di.mdlsCombiLoad.addElement("Diff�rences de Syy");
    di.mdlsCombiDispo.addElement("Diff�rences de hauteur de houle");
    di.mdlsCombiDispo.addElement("Diff�rences de Sxx");
    di.show();
    System.exit(0);
  }
}
