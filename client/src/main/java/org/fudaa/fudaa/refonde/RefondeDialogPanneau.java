/*
 * @file         PRDialogPanneau.java
 * @creation     2001-01-08
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.memoire.bu.BuHorizontalLayout;
/**
 * Un dialogue destiné à afficher un panneau. Il définit des méthodes appelées
 * par les boutons action.<p>
 * Il s'utilise comme une classe abstraite a dériver, en implementant les
 * méthodes actions. Par défaut, la méthode actionOK() appelle la méthode
 * actionApply().
 *
 * @version      $Revision: 1.6 $ $Date: 2006-09-08 16:04:28 $ by $Author: opasteur $
 * @author       Bertrand Marchand
 */
public class RefondeDialogPanneau extends JDialog {
  public static final int OK_CANCEL_OPTION= 0;
  public static final int OK_APPLY_OPTION= 1;
  public static final int OK_CANCEL_APPLY_OPTION= 2;
  public JButton OK_BUTTON= new JButton();
  public JButton CANCEL_BUTTON= new JButton();
  public JButton APPLY_BUTTON= new JButton();
  protected JPanel pnMain_= new JPanel();
  protected int option;
  private JPanel pnAction= new JPanel();
  private BuHorizontalLayout lyAction= new BuHorizontalLayout();
  private JPanel pnAction1= new JPanel();
  private BorderLayout lyThis= new BorderLayout();
  private Window parent_= null;
  /**
   * Création d'une fenêtre dialogue avec centrage sur l'écran
   */
  public RefondeDialogPanneau() {
    this((Frame) null);
  }
  /**
   * Création d'une fenêtre dialogue avec centrage sur le Frame _parent
   */
  public RefondeDialogPanneau(Frame _parent) {
    this(_parent, OK_CANCEL_OPTION);
  }
  /**
   * Création d'une fenêtre dialogue avec centrage sur le Dialog _parent
   */
  public RefondeDialogPanneau(Dialog _parent) {
    this(_parent, OK_CANCEL_OPTION);
  }
  /**
   * Création d'une fenêtre dialogue avec centrage sur le Frame _parent et
   * affectation des boutons d'action (_option)
   */
  public RefondeDialogPanneau(Frame _parent, int _option) {
    this(_parent, null, null, _option);
  }
  /**
   * Création d'une fenêtre dialogue avec centrage sur le Dialog _parent et
   * affectation des boutons d'action (_option)
   */
  public RefondeDialogPanneau(Dialog _parent, int _option) {
    this(_parent, null, null, _option);
  }
  /**
   * Dialogue avec le titre, le panneau, et le Frame parent.
   */
  public RefondeDialogPanneau(Frame _parent, JPanel _pn, String _titre) {
    this(_parent, _pn, _titre, OK_CANCEL_OPTION);
  }
  /**
   * Dialogue avec le titre, le panneau, et le Dialog parent.
   */
  public RefondeDialogPanneau(Dialog _parent, JPanel _pn, String _titre) {
    this(_parent, _pn, _titre, OK_CANCEL_OPTION);
  }
  /**
   * Dialogue avec le titre, le panneau, le Frame parent et les l'option des
   * boutons.
   */
  public RefondeDialogPanneau(
    Frame _parent,
    JPanel _pn,
    String _titre,
    int _option) {
    super(_parent, _titre, true);
    // Pour recentrage de la fenêtre à l'écran
    parent_= _parent;
    init(_pn, _titre, _option);
  }
  /**
   * Dialogue avec le titre, le panneau, le Dialog parent et les l'option des
   * boutons.
   */
  public RefondeDialogPanneau(
    Dialog _parent,
    JPanel _pn,
    String _titre,
    int _option) {
    super(_parent, _titre, true);
    // Pour recentrage de la fenêtre à l'écran
    parent_= _parent;
    init(_pn, _titre, _option);
  }
  private void init(JPanel _pn, String _titre, int _option) {
    if (_pn != null)
      pnMain_= _pn;
    OK_BUTTON.setText("Continuer");
    OK_BUTTON.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OK_BUTTON_actionPerformed(e);
      }
    });
    CANCEL_BUTTON.setText("Annuler");
    CANCEL_BUTTON.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CANCEL_BUTTON_actionPerformed(e);
      }
    });
    APPLY_BUTTON.setText("Appliquer");
    APPLY_BUTTON.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        APPLY_BUTTON_actionPerformed(e);
      }
    });
    lyAction.setHgap(5);
    lyAction.setVfilled(false);
    pnAction.setLayout(lyAction);
    pnAction1.add(pnAction, null);
    this.getContentPane().setLayout(lyThis);
    this.getContentPane().add(pnAction1, BorderLayout.SOUTH);
    //    this.getContentPane().add(pnMain_, BorderLayout.CENTER);
    setPanneauPrincipal(pnMain_);
    setOption(_option);
  }
  /**
   * Affecte le panneau principal.
   * @param _pn Le panneau principal.
   */
  public void setPanneauPrincipal(JPanel _pn) {
    pnMain_= _pn;
    getContentPane().add(pnMain_, BorderLayout.CENTER);
    pack();
    setLocationRelativeTo(parent_);
  }
  /**
   * Retourne le panneau d'affichage.
   * @return Le panneau principal.
   */
  public JPanel getPanneauPrincipal() {
    return pnMain_;
  }
  /**
   * Définition des buttons actions à afficher (OK, CANCEL, APPLIQUER)
   */
  public void setOption(int _option) {
    option= _option;
    pnAction.removeAll();
    switch (_option) {
      case OK_CANCEL_OPTION :
        pnAction.add(OK_BUTTON, null);
        pnAction.add(CANCEL_BUTTON, null);
        break;
      case OK_APPLY_OPTION :
        pnAction.add(OK_BUTTON, null);
        pnAction.add(APPLY_BUTTON, null);
        break;
      case OK_CANCEL_APPLY_OPTION :
        pnAction.add(OK_BUTTON, null);
        pnAction.add(CANCEL_BUTTON, null);
        pnAction.add(APPLY_BUTTON, null);
        break;
    }
    pack();
    setLocationRelativeTo(parent_);
  }
  public int getOption() {
    return option;
  }
  /**
   * Affichage de la boite de dialogue avec un recentrage
   */
  //  public void show() {
  //    // Calcul de la position de la fenetre
  //    Dimension dParent;
  //    Point     pParent;
  //    Dimension dThis    = this.getPreferredSize();
  //    Point     pThis    = new Point();
  //
  //    if (parent_ == null) {
  //      dParent = Toolkit.getDefaultToolkit().getScreenSize();
  //      pParent = new Point(0,0);
  //    }
  //    else {
  //     dParent = parent_.getPreferredSize();
  //     pParent = parent_.getLocation();
  //    }
  //
  //    pThis.x = pParent.x+(dParent.width-dThis.width)/2;
  //    pThis.y = pParent.y+(dParent.height-dThis.height)/2;
  //    this.setLocation(pThis);
  //
  //    super.show();
  //  }
  /**
   * Ajout d'un listener aux boutons action du dialog (OK, ANNULER, APPLIQUER). Le
   * bouton pressé est obtenu par getSource().
   */
  public void addActionListener(ActionListener _listener) {
    OK_BUTTON.addActionListener(_listener);
    CANCEL_BUTTON.addActionListener(_listener);
    APPLY_BUTTON.addActionListener(_listener);
  }
  /**
   * Suppression d'un listener aux boutons action du dialog (OK, ANNULER, APPLIQUER).
   * @see #addActionListener(ActionListener)
   */
  public void removeActionListener(ActionListener _listener) {
    OK_BUTTON.removeActionListener(_listener);
    CANCEL_BUTTON.removeActionListener(_listener);
    APPLY_BUTTON.removeActionListener(_listener);
  }
  /**
   * Bouton "Ok" pressé, effacage du dialog, traitement dans le listener du dialog
   */
  void OK_BUTTON_actionPerformed(ActionEvent _evt) {
    if (actionOK())
      dispose();
  }
  /**
   * Bouton "Apply" pressé, traitement dans le listener du dialog.
   */
  void APPLY_BUTTON_actionPerformed(ActionEvent _evt) {
    actionApply();
  }
  /**
   * Bouton "annuler" pressé, effacage du dialog, traitement dans le listener du dialog
   */
  void CANCEL_BUTTON_actionPerformed(ActionEvent _evt) {
    actionCancel();
    dispose();
  }
  /**
   * Méthode action pour le bouton OK à surcharger. Pas défaut, la méthode
   * appelle la méthode actionApply() qui doit être surchargée.
   */
  protected boolean actionOK() {
    return actionApply();
  }
  /**
   * Méthode action pour le bouton APPLY à surcharger.
   * @return La méthode retourne true s'il n'y a pas eu de probleme (on peut
   *         fermer la fenetre).
   */
  protected boolean actionApply() {
    return true;
  }
  /**
   * Méthode action pour le bouton CANCEL à surcharger.
   */
  protected void actionCancel() {}
}
