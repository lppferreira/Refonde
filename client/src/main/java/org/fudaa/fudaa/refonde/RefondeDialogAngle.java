/*
 * @file         RefondeDialogAngle.java
 * @creation     1999-08-16
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.memoire.bu.BuGridLayout;

import org.fudaa.ebli.geometrie.GrPoint;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue permettant d'afficher un angle d'incidence.
 *
 * @version      $Id: RefondeDialogAngle.java,v 1.11 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogAngle extends FudaaDialog {
  //  private static final int SELECTED  =BuCheckBox3States.STATE_SELECTED;
  //  private static final int DESELECTED=BuCheckBox3States.STATE_DESELECTED;
  //  private static final int MIXED     =BuCheckBox3States.STATE_MIXED;
  public JButton reponse;
  JComboBox coType;
  JPanel pnProprietes;
  JTextField tfAngleAbs;
  JTextField tfAngleRel;
  JTextField tfX;
  JTextField tfY;
  Object[] selObjs_;
  RefondeProjet projet_;
  public RefondeDialogAngle() {
    this(null);
  }
  public RefondeDialogAngle(Frame _parent) {
    super(_parent, OK_CANCEL_APPLY_OPTION);
    /**Jbuilder**/
    jbInit();
  }
  public void jbInit() { /**JBuilder**/
    JPanel pnType= new JPanel();
    FlowLayout lyType= new FlowLayout();
    JLabel laType= new JLabel();
    CardLayout lyProprietes= new CardLayout();
    JPanel sspnPrpNul= new JPanel();
    JPanel sspnPrpDif= new JPanel();
    JPanel sspnPrpRel= new JPanel();
    JPanel sspnPrpAbs= new JPanel();
    JLabel laAngleAbs= new JLabel();
    JLabel laAngleRel= new JLabel();
    JLabel laX= new JLabel();
    JLabel laY= new JLabel();
    coType= new JComboBox();
    pnProprietes= new JPanel();
    laType.setText("Type");
    pnType.setLayout(lyType);
    pnType.add(laType, null);
    pnType.add(coType, null);
    pnProprietes.setLayout(lyProprietes);
    coType.addItem("Relatif");
    coType.addItem("Absolu");
    coType.addItem("Diffracté");
    coType.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        coType_itemStateChanged(_evt);
      }
    });
    tfX= new JTextField();
    tfX.setText("0.0");
    tfX.setPreferredSize(new Dimension(70, 21));
    tfX.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        tf_keyTyped(e);
      }
    });
    tfY= new JTextField();
    tfY.setText("0.0");
    tfY.setPreferredSize(new Dimension(70, 21));
    tfY.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        tf_keyTyped(e);
      }
    });
    laAngleAbs.setText("Angle absolu");
    laAngleRel.setText("Angle relatif");
    laX.setText("Position x du point de diffraction");
    laY.setText("Position y du point de diffraction");
    tfAngleAbs= new JTextField();
    tfAngleAbs.setPreferredSize(new Dimension(70, 21));
    tfAngleAbs.setText("0.0");
    tfAngleAbs.setToolTipText("Dans le sens horaire suivant une houle de nord");
    tfAngleAbs.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        tf_keyTyped(e);
      }
    });
    tfAngleRel= new JTextField();
    tfAngleRel.setPreferredSize(new Dimension(70, 21));
    tfAngleRel.setText("0.0");
    tfAngleRel.setToolTipText("Dans le sens trigonométrique");
    tfAngleRel.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        tf_keyTyped(e);
      }
    });
    pnProprietes.add(sspnPrpNul, new Integer(-1).toString());
    sspnPrpRel.setLayout(new BuGridLayout(2, 5, 5, false, false));
    sspnPrpRel= new JPanel();
    sspnPrpRel.add(laAngleRel);
    sspnPrpRel.add(tfAngleRel);
    pnProprietes.add(sspnPrpRel, new Integer(0).toString());
    sspnPrpAbs.setLayout(new BuGridLayout(2, 5, 5, false, false));
    sspnPrpAbs= new JPanel();
    sspnPrpAbs.add(laAngleAbs);
    sspnPrpAbs.add(tfAngleAbs);
    pnProprietes.add(sspnPrpAbs, new Integer(1).toString());
    sspnPrpDif.setLayout(new BuGridLayout(2, 5, 5, false, false));
    sspnPrpDif.add(laX);
    sspnPrpDif.add(tfX);
    sspnPrpDif.add(laY);
    sspnPrpDif.add(tfY);
    pnProprietes.add(sspnPrpDif, new Integer(2).toString());
    pnAffichage_.add(pnProprietes, BorderLayout.CENTER);
    pnAffichage_.add(pnType, BorderLayout.NORTH);
    setModal(true);
    setTitle("Définition de l'angle");
    pack();
    coType.setSelectedIndex(-1);
    btApply_.setEnabled(false);
  }
  /**
   * Initialisation du dialog pour le projet donné pour le
   * RefondeAngle sélectionné.
   */
  public void initialise(RefondeProjet _projet, Object[] _selObjs) {
    projet_= _projet;
    selObjs_= _selObjs;
    int tpAng;
    double pxDiff= 0;
    double pyDiff= 0;
    double angRel= 0;
    double angAbs= 0;
    // Recherche du type des angles sélectionné.
    tpAng= ((RefondeAngle)selObjs_[0]).getType();
    for (int i= 1; i < selObjs_.length; i++) {
      if (tpAng != ((RefondeAngle)selObjs_[i]).getType()) {
        tpAng= -1; // Type mixte.
        break;
      }
    }
    coType.setSelectedIndex((tpAng + 2) % 3);
    // Définition des données des angles
    if (tpAng != -1) {
      for (int i= 0; i < selObjs_.length; i++) {
        if (tpAng == RefondeAngle.DIFFRACTE) {
          GrPoint p= ((RefondeAngle)selObjs_[i]).getPointDiffraction();
          if (i == 0) {
            pxDiff= p.x_;
            pyDiff= p.y_;
          } else if (pxDiff != p.x_ || pyDiff != p.y_) {
            pxDiff= Double.NaN;
            pyDiff= Double.NaN;
            break;
          }
        } else if (tpAng == RefondeAngle.RELATIF) {
          double angle= ((RefondeAngle)selObjs_[i]).getAngle();
          if (i == 0) {
            angRel= angle;
          } else if (angRel != angle) {
            angRel= Double.NaN;
            break;
          }
        } else if (tpAng == RefondeAngle.ABSOLU) {
          double angle=
            ((- ((RefondeAngle)selObjs_[i]).getAngle() + 270) % 360 + 360)
              % 360;
          if (i == 0) {
            angAbs= angle;
          } else if (angAbs != angle) {
            angAbs= Double.NaN;
            break;
          }
        }
      }
      tfX.setText(Double.isNaN(pxDiff) ? "" : "" + pxDiff);
      tfY.setText(Double.isNaN(pyDiff) ? "" : "" + pyDiff);
      tfAngleRel.setText(Double.isNaN(angRel) ? "" : "" + angRel);
      tfAngleAbs.setText(Double.isNaN(angAbs) ? "" : "" + angAbs);
      btApply_.setEnabled(false);
    }
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
    majBDD();
    reponse= (JButton)_evt.getSource();
    super.btOkActionPerformed(_evt);
  }
  /**
   * Bouton "Appliquer" pressé, traitement dans le listener du dialog
   */
  protected void btApplyActionPerformed(ActionEvent _evt) {
    btApply_.setEnabled(false);
    majBDD();
    reponse= (JButton)_evt.getSource();
    super.btApplyActionPerformed(_evt);
  }
  // ---------------------------------------------------------------------------
  // Mise à jour des angles
  // ---------------------------------------------------------------------------
  private void majBDD() {
    int tpAng;
    double pxDiff= 0;
    double pyDiff= 0;
    double angRel= 0;
    double angAbs= 0;
    // Récupération des valeurs depuis l'interface.
    tpAng= coType.getSelectedIndex();
    if (tpAng == -1)
      return; // Pas de type défini => On sort.
    tpAng= (tpAng + 1) % 3;
    try {
      if (tpAng == RefondeAngle.DIFFRACTE) {
        pxDiff= Double.parseDouble(tfX.getText());
        pyDiff= Double.parseDouble(tfY.getText());
      } else if (tpAng == RefondeAngle.RELATIF) {
        angRel= Double.parseDouble(tfAngleRel.getText());
      } else if (tpAng == RefondeAngle.ABSOLU) {
        angAbs=
          ((-Double.parseDouble(tfAngleAbs.getText()) + 270) % 360 + 360) % 360;
      }
    }
    // Une erreur dans les valeurs => Sortie sans modifs.
    catch (NumberFormatException _exc) {
      return;
    }
    // Affectation
    for (int i= 0; i < selObjs_.length; i++) {
      RefondeAngle ai= ((RefondeAngle)selObjs_[i]);
      double sDeb= ai.getSDebut();
      double sFin= ai.getSFin();
      if (tpAng == RefondeAngle.DIFFRACTE) {
        ai.setDiffracte(sDeb, sFin, new GrPoint(pxDiff, pyDiff, 0.));
      } else if (tpAng == RefondeAngle.RELATIF) {
        ai.setRelatif(sDeb, sFin, angRel);
      } else if (tpAng == RefondeAngle.ABSOLU) {
        ai.setAbsolu(sDeb, sFin, angAbs);
      }
    }
    // Pour forcer le modèle de calcul à avoir un status modifié
    Vector[] ais= projet_.getModeleCalcul().angles();
    projet_.getModeleCalcul().angles(ais);
  }
  // ---------------------------------------------------------------------------
  // -------  Events interfaces  -----------------------------------------------
  // ---------------------------------------------------------------------------
  /**
   * Changement de type
   */
  void coType_itemStateChanged(ItemEvent _evt) {
    if (_evt.getStateChange() == ItemEvent.SELECTED) {
      //    if (coType.getSelectedItem()==_evt.getItem()) {
      String card= new Integer(coType.getSelectedIndex()).toString();
      ((CardLayout)pnProprietes.getLayout()).show(pnProprietes, card);
      btApply_.setEnabled(true);
    }
  }
  /**
   * Un des TextField a été modifié.
   */
  void tf_keyTyped(KeyEvent e) {
    btApply_.setEnabled(true);
  }
  /**
   * Pour tests
   */
  public static void main(String[] args) {
    RefondeDialogAngle di= new RefondeDialogAngle();
    di.show();
  }
}
