/*
 * @file         RefondeDialogDomainePoreux.java
 * @creation     2003-02-10
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.memoire.bu.BuDialogError;
import com.memoire.bu.BuGridLayout;

import org.fudaa.ebli.geometrie.GrPoint;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Affiche/modifie les propriétés d'un domaine poreux.
 *
 * @version      $Id: RefondeDialogProprietesPoreux.java,v 1.9 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogProprietesPoreux extends FudaaDialog {
  JTabbedPane pnOnglets= new JTabbedPane();
  JPanel pnProprietes= new JPanel();
  JPanel pnLimites= new JPanel();
  BuGridLayout lyProprietes= new BuGridLayout();
  JLabel lbPorosite= new JLabel();
  JTextField tfPorosite= new JTextField();
  JLabel lbFrottement= new JLabel();
  JTextField tfFrottement= new JTextField();
  JLabel lbPermeabilite= new JLabel();
  JTextField tfPermeabilite= new JTextField();
  JLabel lbCm= new JLabel();
  JTextField tfCm= new JTextField();
  Border bdpnProprietes;
  BuGridLayout lyGeometrie= new BuGridLayout();
  JLabel lbXmin= new JLabel();
  JTextField tfXmin= new JTextField();
  JLabel lbYmin= new JLabel();
  JTextField tfYmin= new JTextField();
  Border bdpnPoint2;
  JLabel lbYmax= new JLabel();
  JTextField tfYmax= new JTextField();
  JLabel lbXmax= new JLabel();
  JTextField tfXmax= new JTextField();
  Border bdpnPoint1;
  JPanel pnPlan= new JPanel();
  BuGridLayout lyPlan= new BuGridLayout();
  Border border1;
  TitledBorder titledBorder1;
  Border border2;
  Border border3;
  TitledBorder titledBorder2;
  Border border4;
  Border border5;
  TitledBorder titledBorder3;
  Border border6;
  Border border7;
  /** Domaine poreux */
  private RefondeDomainePoreux domaine_;
  /** Projet */
  private RefondeProjet prj_= null;
  /** Retourne le bouton activé */
  public JButton reponse;
  //  private JTextField[] tfXPlanPt=new JTextField[3];
  //  private JTextField[] tfYPlanPt=new JTextField[3];
  //  private JTextField[] tfZPlanPt=new JTextField[3];
  //  private JCheckBox[] cbZPlanPt=new JCheckBox[3];
  RefondePnPointPlan[] pnPlanPt= new RefondePnPointPlan[3];
  RefondePnPointPlan pnPlanPt1= new RefondePnPointPlan();
  RefondePnPointPlan pnPlanPt2= new RefondePnPointPlan();
  RefondePnPointPlan pnPlanPt3= new RefondePnPointPlan();
  /**
   * Création d'un dialogue sans parent.
   */
  public RefondeDialogProprietesPoreux() {
    this(null);
  }
  /**
   * Création d'un dialogue avec parent.
   */
  public RefondeDialogProprietesPoreux(Frame _parent) {
    super(_parent);
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    pnPlanPt[0]= pnPlanPt1;
    pnPlanPt[1]= pnPlanPt2;
    pnPlanPt[2]= pnPlanPt3;
    //    tfXPlanPt[0]=tfXPlanPt1;
    //    tfXPlanPt[1]=tfXPlanPt2;
    //    tfXPlanPt[2]=tfXPlanPt3;
    //
    //    tfYPlanPt[0]=tfYPlanPt1;
    //    tfYPlanPt[1]=tfYPlanPt2;
    //    tfYPlanPt[2]=tfYPlanPt3;
    //
    //    tfZPlanPt[0]=tfZPlanPt1;
    //    tfZPlanPt[1]=tfZPlanPt2;
    //    tfZPlanPt[2]=tfZPlanPt3;
    //
    //    cbZPlanPt[0]=cbZPlanPt1;
    //    cbZPlanPt[1]=cbZPlanPt2;
    //    cbZPlanPt[2]=cbZPlanPt3;
  }
  private void jbInit() throws Exception {
    bdpnPoint1=
      BorderFactory.createCompoundBorder(
        new TitledBorder(
          BorderFactory.createEtchedBorder(
            Color.white,
            new Color(148, 145, 140)),
          "Premier point saisi"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    bdpnPoint2=
      BorderFactory.createCompoundBorder(
        new TitledBorder(
          BorderFactory.createEtchedBorder(
            Color.white,
            new Color(148, 145, 140)),
          "Deuxième point saisi"),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    border1=
      BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
    titledBorder1= new TitledBorder(border1, "Premier point");
    border2=
      BorderFactory.createCompoundBorder(
        new TitledBorder(
          BorderFactory.createEtchedBorder(
            Color.white,
            new Color(148, 145, 140)),
          "Deuxième point"),
        BorderFactory.createEmptyBorder(2, 5, 2, 5));
    border3=
      BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
    titledBorder2= new TitledBorder(border3, "Premier point");
    border4=
      BorderFactory.createCompoundBorder(
        new TitledBorder(
          BorderFactory.createEtchedBorder(
            Color.white,
            new Color(148, 145, 140)),
          "Premier point"),
        BorderFactory.createEmptyBorder(2, 5, 2, 5));
    border5=
      BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
    titledBorder3= new TitledBorder(border5, "Deuxième point");
    border6=
      BorderFactory.createCompoundBorder(
        new TitledBorder(
          BorderFactory.createEtchedBorder(
            Color.white,
            new Color(148, 145, 140)),
          "Troisième point"),
        BorderFactory.createEmptyBorder(2, 5, 2, 5));
    border7= BorderFactory.createEmptyBorder(5, 5, 5, 5);
    lbYmax.setText("Y max :");
    tfYmax.setColumns(12);
    lbXmax.setText("X max :");
    tfXmax.setColumns(12);
    this.setTitle("Propriétés du domaine poreux");
    pnLimites.setBorder(border7);
    pnLimites.setToolTipText("Limites du domaine");
    pnProprietes.setToolTipText("Propriétés du domaine");
    pnPlan.setLayout(lyPlan);
    lyPlan.setColumns(1);
    lyPlan.setHgap(5);
    lyPlan.setVgap(2);
    lyGeometrie.setColumns(2);
    lyGeometrie.setCfilled(false);
    pnLimites.add(lbXmin, null);
    pnLimites.add(tfXmin, null);
    pnLimites.add(lbXmax, null);
    pnLimites.add(tfXmax, null);
    pnLimites.add(lbYmin, null);
    pnLimites.add(tfYmin, null);
    pnLimites.add(lbYmax, null);
    pnLimites.add(tfYmax, null);
    pnProprietes.add(lbPorosite, null);
    pnProprietes.add(tfPorosite, null);
    pnProprietes.add(lbFrottement, null);
    pnProprietes.add(tfFrottement, null);
    pnProprietes.add(lbPermeabilite, null);
    pnProprietes.add(tfPermeabilite, null);
    pnProprietes.add(lbCm, null);
    pnProprietes.add(tfCm, null);
    pnOnglets.add(pnProprietes, "Propriétés");
    pnOnglets.setToolTipTextAt(0, "Propriétés du domaine ");
    pnOnglets.add(pnLimites, "Limites");
    pnOnglets.setToolTipTextAt(1, "Limites du domaine");
    pnOnglets.add(pnPlan, "Plan");
    pnPlan.add(pnPlanPt1, null);
    pnPlan.add(pnPlanPt2, null);
    pnPlan.add(pnPlanPt3, null);
    pnOnglets.setToolTipTextAt(2, "Points du plan du domaine");
    bdpnProprietes= BorderFactory.createEmptyBorder(5, 5, 5, 5);
    pnProprietes.setLayout(lyProprietes);
    lyProprietes.setColumns(2);
    lyProprietes.setHgap(5);
    lyProprietes.setVgap(5);
    lbPorosite.setHorizontalAlignment(SwingConstants.RIGHT);
    lbPorosite.setText("Porosité :");
    lbFrottement.setHorizontalAlignment(SwingConstants.RIGHT);
    lbFrottement.setText("Coefficient de frottement :");
    lbPermeabilite.setHorizontalAlignment(SwingConstants.RIGHT);
    lbPermeabilite.setText("Perméabilité [cm2] :");
    lbCm.setHorizontalAlignment(SwingConstants.RIGHT);
    lbCm.setText("Coefficient de masse :");
    pnProprietes.setBorder(bdpnProprietes);
    tfPorosite.setColumns(8);
    tfFrottement.setColumns(8);
    tfPermeabilite.setColumns(8);
    tfCm.setColumns(8);
    pnLimites.setLayout(lyGeometrie);
    lyGeometrie.setHgap(5);
    lyGeometrie.setVgap(5);
    lbXmin.setText("X min :");
    tfXmin.setColumns(12);
    lbYmin.setText("Y min :");
    tfYmin.setColumns(12);
    pnPlanPt1.setTitle("Premier point");
    pnPlanPt2.setTitle("Deuxième point");
    pnPlanPt3.setTitle("Troisième point");
    this.getContentPane().add(pnOnglets, BorderLayout.NORTH);
    this.pack();
  }
  //  private void cbZPlanPt1_itemStateChanged(ItemEvent e) {
  //    tfZPlanPt1.setEnabled(!cbZPlanPt1.isSelected());
  //  }
  //
  //  private void cbZPlanPt2_itemStateChanged(ItemEvent e) {
  //    tfZPlanPt2.setEnabled(!cbZPlanPt2.isSelected());
  //  }
  //
  //  private void cbZPlanPt3_itemStateChanged(ItemEvent e) {
  //    tfZPlanPt3.setEnabled(!cbZPlanPt3.isSelected());
  //  }
  protected void btOkActionPerformed(ActionEvent _evt) {
    if (majBDD()) {
      reponse= (JButton)_evt.getSource();
      super.btOkActionPerformed(_evt);
    }
  }
  /**
   * Mise à jour de la base de données.
   *
   * @return <i>true</i> : La mise à jour s'est bien passée.
   */
  private boolean majBDD() {
    try {
      // Panneau propriétés
      double porosite= Double.parseDouble(tfPorosite.getText());
      double coefFrottement= Double.parseDouble(tfFrottement.getText());
      double permeabilite= Double.parseDouble(tfPermeabilite.getText());
      double coefMasse= Double.parseDouble(tfCm.getText());
      // Panneau limites
      double xmin= Double.parseDouble(tfXmin.getText());
      double ymin= Double.parseDouble(tfYmin.getText());
      double xmax= Double.parseDouble(tfXmax.getText());
      double ymax= Double.parseDouble(tfYmax.getText());
      // Panneau points du plan
      GrPoint[] ptsPlan= new GrPoint[3];
      for (int i= 0; i < 3; i++)
        ptsPlan[i]= pnPlanPt[i].getPoint();
      // Champs corrects => Mise à jour du domaine
      domaine_.setPoints(
        new GrPoint[] {
          new GrPoint(xmin, ymin, 0),
          new GrPoint(xmax, ymax, 0)});
      domaine_.setPointsPlan(ptsPlan);
      for (int i= 0; i < 3; i++)
        domaine_.setZAutomatique(pnPlanPt[i].isZAutomatique(), i);
      domaine_.porosite_= porosite;
      domaine_.coefFrottement_= coefFrottement;
      domaine_.permeabilite_= permeabilite;
      domaine_.coefMasse_= coefMasse;
    } catch (NumberFormatException _exc) {
      new BuDialogError(
        null,
        RefondeImplementation.informationsSoftware(),
        "Un des paramètres n'a pas un format valide")
        .activate();
      return false;
    }
    return true;
  }
  /**
   * Affectation du domaine poreux à modifier.
   * @param _dm Domaine poreux.
   */
  public void setDomainePoreux(RefondeDomainePoreux _dm) {
    domaine_= _dm;
  }
  /**
   * Retourne le domaine poreux affiché.
   * @return Le domaine poreux éventuellement modifié.
   */
  public RefondeDomainePoreux getDomainePoreux() {
    return domaine_;
  }
  /**
   * Affectation de la géométrie (necessaire pour savoir si les points du
   * domaine poreux sont hors géométrie ou non.
   * @param _prj Le projet refonde.
   */
  public void setProjet(RefondeProjet _prj) {
    prj_= _prj;
  }
  /**
   * Visualisation de la fenetre. Surchargé.
   */
  public void show() {
    if (domaine_ != null) {
      GrPoint[] pts= domaine_.getPoints();
      GrPoint[] ptsPlan= domaine_.getPointsPlan();
      // Propriétés
      tfPorosite.setText("" + domaine_.porosite_);
      tfFrottement.setText("" + domaine_.coefFrottement_);
      tfPermeabilite.setText("" + domaine_.permeabilite_);
      tfCm.setText("" + domaine_.coefMasse_);
      // Limites
      tfXmin.setText("" + pts[0].x_);
      tfYmin.setText("" + pts[0].y_);
      tfXmax.setText("" + pts[1].x_);
      tfYmax.setText("" + pts[1].y_);
      // Points du plan
      for (int i= 0; i < 3; i++) {
        pnPlanPt[i].setProjet(prj_);
        pnPlanPt[i].setPoint(ptsPlan[i]);
        pnPlanPt[i].setZAutomatique(domaine_.isZAutomatique(i));
      }
    }
    super.show();
  }
  /**
   * Pour test de la boite de dialogue.
   */
  public static void main(String[] _args) {
    try {
      UIManager.setLookAndFeel(
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception _exc) {}
    RefondeDomainePoreux dm= new RefondeDomainePoreux();
    GrPoint[] pts= new GrPoint[2];
    pts[0]= new GrPoint(0, 0, 0);
    pts[1]= new GrPoint(10, 25, 0);
    dm.setPoints(pts);
    dm.setPointsPlan(
      new GrPoint[] {
        new GrPoint(1, 2, 3),
        new GrPoint(4, 5, 6),
        new GrPoint(7, 8, 9)});
    dm.setZAutomatique(false, 0);
    dm.setZAutomatique(true, 1);
    RefondeDialogProprietesPoreux di= new RefondeDialogProprietesPoreux();
    di.setDomainePoreux(dm);
    di.show();
    System.exit(0);
  }
}