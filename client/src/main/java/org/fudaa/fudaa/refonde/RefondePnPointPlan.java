/*
 * @file         RefondePnPointPlan.java
 * @creation     2003-02-17
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.memoire.bu.BuGridLayout;

import org.fudaa.ebli.geometrie.GrPoint;
/**
 * Un panneau pour définir les coordonnées d'un point de plan d'un domaine
 * poreux.
 *
 * @version      $Id: RefondePnPointPlan.java,v 1.7 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnPointPlan extends JPanel {
  JTextField tfZPt= new JTextField();
  JLabel lbYPt= new JLabel();
  JTextField tfYPt= new JTextField();
  BuGridLayout lyPlan= new BuGridLayout();
  JLabel lbXPt= new JLabel();
  JTextField tfXPt= new JTextField();
  JCheckBox cbZPt= new JCheckBox();
  JLabel lbDummy6= new JLabel();
  JLabel lbDummy5= new JLabel();
  JLabel lbZPt= new JLabel();
  TitledBorder ttbdpnPlan;
  Border bdpnPlan;
  /** Projet */
  private RefondeProjet prj_= null;
  /** Point */
  private GrPoint pt_= new GrPoint();
  /**
   * Constructeur
   */
  public RefondePnPointPlan() {
    super();
    jbInit();
  }
  /**
   * Définition de l'IU
   */
  private void jbInit() {
    ttbdpnPlan=
      new TitledBorder(
        BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
        "");
    bdpnPlan=
      BorderFactory.createCompoundBorder(
        ttbdpnPlan,
        BorderFactory.createEmptyBorder(5, 2, 5, 2));
    this.setLayout(lyPlan);
    lbZPt.setText("Z :");
    cbZPt.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        cbZPt_itemStateChanged(e);
      }
    });
    cbZPt.setToolTipText("Pour calculer le Z à partir de la bathymétrie");
    cbZPt.setHorizontalTextPosition(SwingConstants.LEFT);
    cbZPt.setText("Automatique :");
    tfXPt.setColumns(12);
    lbXPt.setText("X :");
    lyPlan.setHgap(5);
    lyPlan.setVgap(2);
    lyPlan.setVfilled(false);
    tfYPt.setColumns(12);
    lbYPt.setText("Y :");
    tfZPt.setColumns(12);
    this.setBorder(bdpnPlan);
    this.add(lbXPt, null);
    this.add(tfXPt, null);
    this.add(lbDummy6, null);
    this.add(lbYPt, null);
    this.add(tfYPt, null);
    this.add(lbDummy5, null);
    this.add(lbZPt, null);
    this.add(tfZPt, null);
    this.add(cbZPt, null);
  }
  void cbZPt_itemStateChanged(ItemEvent e) {
    tfZPt.setEnabled(!cbZPt.isSelected());
  }
  /**
   * Définition du titre du panneau.
   */
  public void setTitle(String _title) {
    ttbdpnPlan.setTitle(_title);
  }
  /**
   * Affectation du projet
   */
  public void setProjet(RefondeProjet _prj) {
    prj_= _prj;
  }
  /**
   * Définition de l'automaticité de la cote Z
   */
  public void setZAutomatique(boolean _b) {
    cbZPt.setSelected(_b);
    initZ();
  }
  /**
   * La cote Z est-elle automatique ?
   */
  public boolean isZAutomatique() {
    return cbZPt.isSelected();
  }
  /**
   * Définit le point.
   */
  public void setPoint(GrPoint _pt) {
    pt_= _pt;
    //... X
    tfXPt.setText("" + pt_.x_);
    //... Y
    tfYPt.setText("" + pt_.y_);
    //... Z
    initZ();
  }
  /**
   * Retourne le point avec ses coordonnées modifiées.
   */
  public GrPoint getPoint() {
    double x= 0;
    double y= 0;
    double z= 0;
    x= Double.parseDouble(tfXPt.getText());
    y= Double.parseDouble(tfYPt.getText());
    if (!cbZPt.isSelected())
      z= Double.parseDouble(tfZPt.getText());
    pt_.x_= x;
    pt_.y_= y;
    pt_.z_= z;
    return pt_;
  }
  /**
   * Initialise le Z
   */
  private void initZ() {
    if (!isZAutomatique())
      tfZPt.setText("" + pt_.z_);
    else if (prj_ != null && prj_.getGeometrie().contient(pt_))
      tfZPt.setText("** Calculé **");
    else
      tfZPt.setText("** Hors géometrie **");
  }
  /**
   * Retourne vrai si le panneau est correctement rempli
   */
  public boolean isOK() {
    try {
/*      double x= 0;
      double y= 0;
      double z= 0;*/
      /*x= */Double.parseDouble(tfXPt.getText());
      /*y= */Double.parseDouble(tfYPt.getText());
      if (!cbZPt.isSelected())
        /*z= */Double.parseDouble(tfZPt.getText());
    } catch (NumberFormatException _exc) {
      return false;
    }
    return true;
  }
  /**
   * Pour test.
   */
  public static void main(String[] args) {
    JDialog f= new JDialog((Frame) null, "Coordonnées du point", true);
    RefondePnPointPlan pn= new RefondePnPointPlan();
    pn.setTitle("Coordonnées");
    pn.setZAutomatique(true);
    pn.setPoint(new GrPoint(0, 0, 10));
    f.getContentPane().add(pn, BorderLayout.CENTER);
    f.pack();
    f.show();
    if (pn.isOK())
      System.out.println("OK");
    else
      System.out.println("Bad");
    System.exit(0);
  }
}
