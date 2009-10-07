/*
 * @file         RefondeDialogDomaine.java
 * @creation     1999-08-16
 * @modification $Date: 2007-01-19 13:14:15 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue permettant de modifier les propriétés d'un domaine
 *
 * @version      $Id: RefondeDialogDomaine.java,v 1.7 2007-01-19 13:14:15 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogDomaine extends FudaaDialog {
  public JButton reponse;
  private RefondeDomaine doma_;
  private JPanel pn_;
  public RefondeDialogDomaine() {
    this(null);
  }
  public RefondeDialogDomaine(Frame _parent) {
    super(_parent, OK_CANCEL_OPTION);
    /**Jbuilder**/
    jbInit();
  }
  public void jbInit() { /**JBuilder**/
    setModal(true);
    setTitle("Modification des propriétés du domaine");
  }
  /**
   * Initialisation du dialog pour le domaine donné
   */
  public void initialise(RefondeDomaine _doma) {
    doma_= _doma;
    pnAffichage_.removeAll();
    if (_doma instanceof RefondeDomaineDigue) {
      pn_= new RefondePnDomaineDigueEditor();
      ((RefondePnDomaineDigueEditor)pn_).initialise((RefondeDomaineDigue)_doma);
    } else {
      pn_= new RefondePnDomaineFondEditor();
      ((RefondePnDomaineFondEditor)pn_).initialise((RefondeDomaineFond)_doma);
    }
    pnAffichage_.add(pn_, BorderLayout.CENTER);
    pack();
  }
  /**
   * Bouton "Annuler" pressé
   */
  protected void btCancelActionPerformed(ActionEvent _evt) {
    reponse= (JButton)_evt.getSource();
    super.btCancelActionPerformed(_evt);
  }
  /**
   * Bouton "Ok" pressé, effacage du dialog, traitement dans le listener du dialog
   */
  protected void btOkActionPerformed(ActionEvent _evt) {
    reponse= (JButton)_evt.getSource();
    if (doma_ instanceof RefondeDomaineDigue) {
      RefondeDomaineDigue doma= (RefondeDomaineDigue)doma_;
      RefondePnDomaineDigueEditor pn= (RefondePnDomaineDigueEditor)pn_;
      doma.setNbElements(pn.getNbElements());
    } else {
      RefondeDomaineFond doma= (RefondeDomaineFond)doma_;
      RefondePnDomaineFondEditor pn= (RefondePnDomaineFondEditor)pn_;
      // Maillage par longueur d'onde
      if (pn.isOptionOSelected()) {
        doma.setTypeMaillage(RefondeDomaineFond.LONGUEUR_ONDE);
        doma.setNbNoeudsOnde(pn.getNombreNoeuds());
        doma.setPeriodeHoule(pn.getPeriodeHoule());
      } else {
        doma.setTypeMaillage(RefondeDomaineFond.CLASSIQUE);
        doma.setAireMaxi(pn.getAireMaxi());
      }
    }
    super.btOkActionPerformed(_evt);
  }
}
