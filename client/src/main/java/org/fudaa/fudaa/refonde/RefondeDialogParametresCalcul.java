/*
 * @file         RefondeDialogParametresCalcul.java
 * @creation     1999-10-11
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.memoire.bu.BuDialogConfirmation;
import com.memoire.bu.BuDialogError;
import com.memoire.bu.BuGridLayout;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue pour les param�tres du calcul. Ces param�tres
 * concernent la houle al�atoire, le d�ferlement, la houle r�guli�re, les
 * param�tres g�n�raux.
 *
 * @version      $Id: RefondeDialogParametresCalcul.java,v 1.12 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogParametresCalcul extends FudaaDialog {
  //public class RefondeDialogParametresCalcul extends JDialog {
  private RefondeProjet projet_;
  private RefondeFilleCalques fnCalques_;
  JTabbedPane tpMain= new JTabbedPane();
  JPanel pnGene= new JPanel();
  JLabel lbHtMer= new JLabel();
  JTextField tfHtMer= new JTextField();
  //  JLabel lbProfExterne = new JLabel();
  //  JTextField tfProfExterne = new JTextField();
  JLabel lbCasBord= new JLabel();
  JLabel lbOrdreMax= new JLabel();
  JTextField tfOrdreMax= new JTextField();
  JComboBox coCasBord= new JComboBox();
  JLabel lbFondsPoreux= new JLabel();
  JCheckBox cbFondsPoreux= new JCheckBox();
  //  JTextField tfNbIterHR = new JTextField();
  //  JLabel lbNbIterHR = new JLabel();
  Border bdPnParamsHR;
  Border bdPnParamsHA;
  Border bdPnDefer;
  Border bdPnGene;
  //  JPanel pnNbIterHR = new JPanel();
  JRadioButton rbDissipatif= new JRadioButton();
  BuGridLayout lyFormule= new BuGridLayout(2, 5, 5);
  JComboBox coFormule= new JComboBox();
  JLabel lbDummy2= new JLabel();
  JRadioButton rbSans= new JRadioButton();
  JRadioButton rbEcretage= new JRadioButton();
  JPanel pnDefer= new JPanel();
  JLabel lbDummy1= new JLabel();
  JLabel lbFormule= new JLabel();
  JPanel pnFormule= new JPanel();
  BuGridLayout lyDefer= new BuGridLayout(2, 5, 5);
  TitledBorder bdCasCalcul;
  BuGridLayout lyParamsHA= new BuGridLayout(2, 5, 5);
  JLabel lbNbPeriodeHA= new JLabel();
  JTextField tfAngleMnHA= new JTextField();
  JPanel pnHoule= new JPanel();
  JRadioButton rbHR= new JRadioButton();
  JTextField tfPeriodeHA= new JTextField();
  JPanel pnParamsHA= new JPanel();
  JLabel lbHtHouleHA= new JLabel();
  JLabel lbPartageHA= new JLabel();
  CardLayout lyParams= new CardLayout();
  JRadioButton rbHA= new JRadioButton();
  JTextField tfHtHouleHR= new JTextField();
  JLabel lbPicHA= new JLabel();
  JLabel lbNbAngleHA= new JLabel();
  JTextField tfAngleHR= new JTextField();
  JTextField tfPicHA= new JTextField();
  JTextField tfHtHouleHA= new JTextField();
  JTextField tfPartageHA= new JTextField();
  JLabel lbAngleMxHA= new JLabel();
  JTextField tfPeriodeMxHA= new JTextField();
  JPanel pnParams= new JPanel();
  JLabel lbPeriodeHR= new JLabel();
  JTextField tfAngleHA= new JTextField();
  JLabel lbAngleMnHA= new JLabel();
  JTextField tfPeriodeMnHA= new JTextField();
  JLabel lbAngleHR= new JLabel();
  BuGridLayout lyHoule= new BuGridLayout(1);
  JTextField tfNbAngleHA= new JTextField();
  JTextField tfNbPeriodeHA= new JTextField();
  JLabel lbPeriodeHA= new JLabel();
  JPanel pnCasCalcul= new JPanel();
  JLabel lbPeriodeMxHA= new JLabel();
  BuGridLayout lyParamsHR= new BuGridLayout(2, 5, 5);
  JTextField tfAngleMxHA= new JTextField();
  JLabel lbAngleHA= new JLabel();
  JTextField tfPeriodeHR= new JTextField();
  JLabel lbPeriodeMnHA= new JLabel();
  JPanel pnParamsHR= new JPanel();
  JLabel lbHtHouleHR= new JLabel();
  JPanel pnGeneSeiche = new JPanel();
  JTextField tfNbValPropres = new JTextField();
  JLabel lbNbValPropres = new JLabel();
  BuGridLayout lyGeneSeiche = new BuGridLayout(2, 5, 5);
  JLabel lbHtMerSeiche = new JLabel();
  JTextField tfHtMerSeiche = new JTextField();
  JTextField tfNbIterMax = new JTextField();
  JLabel lbNbIterMax = new JLabel();
  JTextField tfPrec = new JTextField();
  JLabel lbDecalValPropres = new JLabel();
  JTextField tfDecalValPropres = new JTextField();
  JLabel lbPrec = new JLabel();
  public RefondeDialogParametresCalcul() {
    this(null, null);
  }
  public RefondeDialogParametresCalcul(RefondeFilleCalques _fnCalques) {
    this(null, _fnCalques);
  }
  /**
   * Cr�ation de la fenetre de dialogue avec les boutons action par defaut
   */
  public RefondeDialogParametresCalcul(
    Frame _parent,
    RefondeFilleCalques _fnCalques) {
    //  public RefondeDialogParametresCalcul(Frame _parent) {
    // Le frame principal et le panel des boutons est cr�� et g�r� par la classe
    // m�re. On ne cr�e que le panel principal.
    super(_parent, FudaaDialog.OK_CANCEL_APPLY_OPTION);
    //    super(_parent);
    fnCalques_= _fnCalques;
    jbInit();
  }
  private void jbInit() {
    BuGridLayout lyGene= new BuGridLayout(2, 5, 5);
    //    BuGridLayout lyNbIterHR =new BuGridLayout(2,5,5);
    ButtonGroup bgDefer= new ButtonGroup();
    ButtonGroup bgCasCalcul= new ButtonGroup();
    //---  Param�tres g�n�raux  ------------------------------------------------
    lbHtMer.setText("Hauteur de mer :");
    //    lbProfExterne.setText("Profondeur moyenne ext�rieure :");
    lbCasBord.setText("Traitement des fronti�res ouvertes :");
    lbOrdreMax.setText("Ordre de troncature max :");
    lbFondsPoreux.setText("Prise en compte des fonds poreux :");
    lbHtMer.setHorizontalAlignment(SwingConstants.TRAILING);
    //    lbProfExterne.setHorizontalAlignment(SwingConstants.TRAILING);
    lbCasBord.setHorizontalAlignment(SwingConstants.TRAILING);
    lbOrdreMax.setHorizontalAlignment(SwingConstants.TRAILING);
    coCasBord.addItem("Condition absorbante ordre 1");
    coCasBord.addItem("Condition absorbante ordre 2");
    coCasBord.addItem("Formulation analytique - �le");
    coCasBord.addItem("Formulation analytique - port");
    coCasBord.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        lbOrdreMax.setEnabled(coCasBord.getSelectedIndex() > 1);
        tfOrdreMax.setEnabled(coCasBord.getSelectedIndex() > 1);
      }
    });
    bdPnGene=
      BorderFactory.createCompoundBorder(
        new EtchedBorder(
          EtchedBorder.RAISED,
          Color.white,
          new Color(142, 142, 142)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnGene.setLayout(lyGene);
    pnGene.setBorder(bdPnGene);
    pnGeneSeiche.setBorder(bdPnGene);
    pnGeneSeiche.setLayout(lyGeneSeiche);
    lbNbValPropres.setText("Nombre de valeurs propres :");
    lbNbValPropres.setHorizontalAlignment(SwingConstants.TRAILING);
    lbHtMerSeiche.setHorizontalAlignment(SwingConstants.TRAILING);
    lbHtMerSeiche.setText("Hauteur de mer :");
    lbNbIterMax.setHorizontalAlignment(SwingConstants.TRAILING);
    lbNbIterMax.setText("Nombre d\'it�rations max :");
    lbDecalValPropres.setText("D�calage des valeurs propres :");
    lbDecalValPropres.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPrec.setText("Pr�cision de convergence :");
    lbPrec.setHorizontalAlignment(SwingConstants.TRAILING);
    pnGeneSeiche.add(lbHtMerSeiche, null);
    pnGeneSeiche.add(tfHtMerSeiche, null);
    pnGeneSeiche.add(lbNbValPropres, null);
    pnGene.add(lbHtMer);
    pnGene.add(tfHtMer);
    //    pnGene.add(lbProfExterne);
    //    pnGene.add(tfProfExterne);
    pnGene.add(lbCasBord);
    pnGene.add(coCasBord);
    pnGene.add(lbOrdreMax);
    pnGene.add(tfOrdreMax);
    pnGene.add(lbFondsPoreux);
    pnGene.add(cbFondsPoreux);
    //---  Houle  --------------------------------------------------------------
    // Panneau de cas de calcul
    rbHR.setText("Houle r�guli�re");
    rbHR.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        rbHoule_itemStateChanged(_evt);
      }
    });
    rbHA.setText("Houle al�atoire");
    rbHA.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        rbHoule_itemStateChanged(_evt);
      }
    });
    bgCasCalcul.add(rbHR);
    bgCasCalcul.add(rbHA);
    bdCasCalcul=
      new TitledBorder(
        new EtchedBorder(
          EtchedBorder.RAISED,
          Color.white,
          new Color(134, 134, 134)),
        "Cas de calcul");
    pnCasCalcul.setBorder(bdCasCalcul);
    pnCasCalcul.add(rbHR);
    pnCasCalcul.add(rbHA);
    // Panneau de param�tres houle r�guli�re
    lbHtHouleHR.setText("Hauteur de houle :");
    lbPeriodeHR.setText("Valeur de la p�riode :");
    lbAngleHR.setText("Valeur de la direction :");
    lbHtHouleHR.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPeriodeHR.setHorizontalAlignment(SwingConstants.TRAILING);
    lbAngleHR.setHorizontalAlignment(SwingConstants.TRAILING);
    bdPnParamsHR=
      BorderFactory.createCompoundBorder(
        new EtchedBorder(
          EtchedBorder.RAISED,
          Color.white,
          new Color(142, 142, 142)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnParamsHR.setBorder(bdPnParamsHR);
    pnParamsHR.setLayout(lyParamsHR);
    pnParamsHR.add(lbHtHouleHR);
    pnParamsHR.add(tfHtHouleHR);
    pnParamsHR.add(lbPeriodeHR);
    pnParamsHR.add(tfPeriodeHR);
    pnParamsHR.add(lbAngleHR);
    pnParamsHR.add(tfAngleHR);
    // Panneau de param�tres houle al�atoire
    lbNbPeriodeHA.setText("Nombre de p�riodes de houle :");
    lbHtHouleHA.setText("Hauteur significative de houle :");
    lbPartageHA.setText("Param�tre principal de r�partition angulaire :");
    lbPicHA.setText("Facteur de r�haussement du pic :");
    lbNbAngleHA.setText("Nombre de directions de houle :");
    lbAngleMxHA.setText("Valeur de la direction maxi :");
    lbAngleMnHA.setText("Valeur de la direction mini :");
    lbPeriodeHA.setText("P�riode de pic :");
    lbPeriodeMxHA.setText("Valeur de la p�riode maxi :");
    lbAngleHA.setText("Direction principale de houle :");
    lbPeriodeMnHA.setText("Valeur de la p�riode mini :");
    lbNbPeriodeHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbHtHouleHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPartageHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPicHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbNbAngleHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbAngleMxHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbAngleMnHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPeriodeHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPeriodeMxHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbAngleHA.setHorizontalAlignment(SwingConstants.TRAILING);
    lbPeriodeMnHA.setHorizontalAlignment(SwingConstants.TRAILING);
    tfNbPeriodeHA.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent _evt) {
        int nb= 0;
        try {
          nb= Integer.parseInt(tfNbPeriodeHA.getText());
        } catch (NumberFormatException _exc) {} finally {
          lbPeriodeMxHA.setVisible(nb != 1);
          tfPeriodeMxHA.setVisible(nb != 1);
          if (nb != 1)
            lbPeriodeMnHA.setText("Valeur de la p�riode mini :");
          else
            lbPeriodeMnHA.setText("Valeur de la p�riode :");
        }
      }
    });
    tfNbAngleHA.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent _evt) {
        int nb= 0;
        try {
          nb= Integer.parseInt(tfNbAngleHA.getText());
        } catch (NumberFormatException _exc) {} finally {
//          lbAngleMnHA.setVisible(nb != 1);
//          tfAngleMnHA.setVisible(nb != 1);
//          if (nb != 1) lbAngleMxHA.setText("Valeur de la direction maxi :");
//          else         lbAngleMxHA.setText("Valeur de la direction :");
          lbAngleMxHA.setVisible(nb!=1);
          tfAngleMxHA.setVisible(nb!=1);
          if (nb!=1) lbAngleMnHA.setText("Valeur de la direction mini :");
          else       lbAngleMnHA.setText("Valeur de la direction :");
        }
      }
    });
    bdPnParamsHA=
      BorderFactory.createCompoundBorder(
        new EtchedBorder(
          EtchedBorder.RAISED,
          Color.white,
          new Color(142, 142, 142)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnParamsHA.setBorder(bdPnParamsHA);
    pnParamsHA.setLayout(lyParamsHA);
    pnParamsHA.add(lbNbPeriodeHA);
    pnParamsHA.add(tfNbPeriodeHA);
    pnParamsHA.add(lbPeriodeMnHA);
    pnParamsHA.add(tfPeriodeMnHA);
    pnParamsHA.add(lbPeriodeMxHA);
    pnParamsHA.add(tfPeriodeMxHA);
    pnParamsHA.add(lbNbAngleHA);
    pnParamsHA.add(tfNbAngleHA);
    pnParamsHA.add(lbAngleMnHA);
    pnParamsHA.add(tfAngleMnHA);
    pnParamsHA.add(lbAngleMxHA);
    pnParamsHA.add(tfAngleMxHA);
    pnParamsHA.add(lbHtHouleHA);
    pnParamsHA.add(tfHtHouleHA);
    pnParamsHA.add(lbPeriodeHA);
    pnParamsHA.add(tfPeriodeHA);
    pnParamsHA.add(lbPicHA);
    pnParamsHA.add(tfPicHA);
    pnParamsHA.add(lbAngleHA);
    pnParamsHA.add(tfAngleHA);
    pnParamsHA.add(lbPartageHA);
    pnParamsHA.add(tfPartageHA);
    pnParams.setLayout(lyParams);
    pnParams.add(pnParamsHR, "pnParamsHR");
    pnParams.add(pnParamsHA, "pnParamsHA");
    lyHoule.setRfilled(true);
    pnHoule.setLayout(lyHoule);
    pnHoule.add(pnCasCalcul);
    pnHoule.add(pnParams);
    //---  D�ferlement  --------------------------------------------------------
    rbSans.setText("Sans");
    rbDissipatif.setText("Avec terme dissipatif");
    rbEcretage.setText("Par �cr�tage");
    rbEcretage.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        lbFormule.setEnabled(rbEcretage.isSelected());
        coFormule.setEnabled(rbEcretage.isSelected());
      }
    });
    lbFormule.setText("Formule :");
    lbFormule.setHorizontalAlignment(SwingConstants.TRAILING);
    lbFormule.setEnabled(false);
    coFormule.setEnabled(false);
    coFormule.addItem("Goda sans effet de pente");
    coFormule.addItem("Goda avec effet de pente");
    coFormule.addItem("Miche modifi�e");
    coFormule.addItem("Munk");
    lyFormule.setRfilled(true);
    pnFormule.setLayout(lyFormule);
    pnFormule.add(lbFormule);
    pnFormule.add(coFormule);
    bgDefer.add(rbSans);
    bgDefer.add(rbDissipatif);
    bgDefer.add(rbEcretage);
    bdPnDefer=
      BorderFactory.createCompoundBorder(
        new EtchedBorder(
          EtchedBorder.RAISED,
          Color.white,
          new Color(142, 142, 142)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnDefer.setBorder(bdPnDefer);
    pnDefer.setLayout(lyDefer);
    pnDefer.add(rbSans);
    pnDefer.add(lbDummy1);
    pnDefer.add(rbDissipatif);
    pnDefer.add(lbDummy2);
    pnDefer.add(rbEcretage);
    pnDefer.add(pnFormule);
    // Ajout des panneaux au panneau principal
//    tpMain.addTab("G�n�ral", null, pnGene, "Param�tres g�n�raux");
//    tpMain.addTab("Houle", null, pnHoule, "Pour la houle");
//    tpMain.addTab("D�ferlement", null, pnDefer, "Pour le d�ferlement");
    pnAffichage_.add("Center", tpMain);
    pnGeneSeiche.add(tfNbValPropres, null);
//    tpMain.add(pnGeneSeiche,  "G�n�ral");
    pnGeneSeiche.add(lbNbIterMax, null);
    pnGeneSeiche.add(tfNbIterMax, null);
    pnGeneSeiche.add(lbDecalValPropres, null);
    pnGeneSeiche.add(tfDecalValPropres, null);
    pnGeneSeiche.add(lbPrec, null);
    pnGeneSeiche.add(tfPrec, null);
    // Desactivation par d�faut de certains Widgets
    lbOrdreMax.setEnabled(false);
    tfOrdreMax.setEnabled(false);
    setTitle("Param�tres");
    setLocation(40, 40);
    //    setResizable(false);
    pack();
  }
  /**
   * Initialisation de la boite de dialogue avec le projet
   */
  public void initialise(RefondeProjet _projet) {
    projet_= _projet;
    RefondeModeleCalcul mdlCal= projet_.getModeleCalcul();

    // Mod�le de donn�es Houle

    if (mdlCal.typeModele()==RefondeModeleCalcul.MODELE_HOULE) {
      tpMain.removeAll();
      tpMain.addTab("G�n�ral", null, pnGene, "Param�tres g�n�raux");
      tpMain.addTab("Houle", null, pnHoule, "Pour la houle");
      tpMain.addTab("D�ferlement", null, pnDefer, "Pour le d�ferlement");
      pack();

      // Param�tres g�n�raux
      tfHtMer.setText(""+mdlCal.hauteurMer());
      switch (mdlCal.casBordOuvert()) {
        case RefondeModeleCalcul.BORD_COND_ORDRE_1:
          coCasBord.setSelectedIndex(0);
          break;
        case RefondeModeleCalcul.BORD_COND_ORDRE_2:
          coCasBord.setSelectedIndex(1);
          break;
        case RefondeModeleCalcul.BORD_FORM_ANA_ILE:
          coCasBord.setSelectedIndex(2);
          break;
        case RefondeModeleCalcul.BORD_FORM_ANA_PORT:
          coCasBord.setSelectedIndex(3);
          break;
      }
      tfOrdreMax.setText(""+mdlCal.ordreMax());
      cbFondsPoreux.setSelected(mdlCal.fondsPoreux_);

      // Houle r�guli�re
      tfHtHouleHR.setText(""+mdlCal.hauteurHoule());
      tfPeriodeHR.setText(""+mdlCal.periodeHoule());
      tfAngleHR.setText(""+((-mdlCal.angleHoule()+270)%360+360)%360);

      // Houle al�atoire
      tfNbPeriodeHA.setText(""+mdlCal.nbPeriodesHoule());
      tfPeriodeMnHA.setText(""+mdlCal.periodeHouleMini());
      tfPeriodeMxHA.setText(""+mdlCal.periodeHouleMaxi());
      tfNbAngleHA.setText(""+mdlCal.nbAnglesHoule());

      // Important B.M. : Les angles utilisateurs tournant dans le sens inverse
      // des angles internes, les angles mini doivent �tre les angles maxi et
      // inversement.
      tfAngleMnHA.setText(
          ""+((-mdlCal.angleHouleMaxi()+270)%360+360)%360);
      tfAngleMxHA.setText(
          ""+((-mdlCal.angleHouleMini()+270)%360+360)%360);
      tfHtHouleHA.setText(""+mdlCal.hauteurHoule());
      tfPeriodeHA.setText(""+mdlCal.periodeHoule());
      tfPicHA.setText(""+mdlCal.rehaussementPic());
      tfAngleHA.setText(""+((-mdlCal.angleHoule()+270)%360+360)%360);
      tfPartageHA.setText(""+mdlCal.repartitionAngle());
      rbHR.setSelected(mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_REG);
      rbHA.setSelected(!rbHR.isSelected());

      // D�ferlement
      switch (mdlCal.deferHoule()) {
        case RefondeModeleCalcul.DEFER_SANS:
          rbSans.setSelected(true);
          break;
        case RefondeModeleCalcul.DEFER_ITERATIF:
          rbDissipatif.setSelected(true);
          break;
        case RefondeModeleCalcul.DEFER_ECRETAGE:
          rbEcretage.setSelected(true);
          break;
      }

      //    tfNbIterHR.setText(""+mdlCal.nbIterDeferHoule());
      coFormule.setSelectedIndex(mdlCal.formuleDeferHoule());
    }

    // Mod�le de donn�es Seiche

    else {
      tpMain.removeAll();
      tpMain.addTab("G�n�ral", null, pnGeneSeiche, "Param�tres de seiche");
      pack();

      tfHtMerSeiche.setText(""+mdlCal.hauteurMer());
      tfNbValPropres.setText(""+mdlCal.nbValPropres());
      tfNbIterMax.setText(""+mdlCal.nbIterMax());
      tfDecalValPropres.setText(""+mdlCal.decalValPropres());
      tfPrec.setText(""+mdlCal.precision());
    }
  }

  protected void btApplyActionPerformed(ActionEvent _evt) {
    if (majBDD())
      super.btApplyActionPerformed(_evt);
  }

  protected void btOkActionPerformed(ActionEvent _evt) {
    if (majBDD())
      super.btOkActionPerformed(_evt);
  }

  protected void rbHoule_itemStateChanged(ItemEvent _evt) {
    if (_evt.getStateChange()==ItemEvent.DESELECTED) return;

    if (rbHR.isSelected())
      lyParams.show(pnParams, "pnParamsHR");
    else
      lyParams.show(pnParams, "pnParamsHA");
    // B.M. Le 04/02/2003 Le terme dissipatif est autoris� pour la houle al�atoire.
    /*             rbDissipatif.setEnabled(rbHR.isSelected());

                 if (!rbDissipatif.isEnabled() && rbDissipatif.isSelected()) {
                   new BuDialogMessage(null, RefondeImplementation.informationsSoftware(),
                   "Houle al�atoire : Pas de prise en compte de d�ferlement avec\n"+
                   "terme dissipatif").activate();
                  rbSans.setSelected(true);
                 }*/
  }

  /**
   * Mise � jour de la base de donn�es.
   * @return <i>true</i> : La mise � jour s'est bien pass�e.
   */
  private boolean majBDD() {
    RefondeModeleCalcul mdlCal= projet_.getModeleCalcul();
    double oldAngle= mdlCal.angleHoule();
    double htMer= 0;
    int casBord= 0;
    int ordreMax= 0;
    boolean fondsPoreux= false;
    double periode= 0;
    double angle= 0;
    double htHoule= 0;
    int nbPeriodeHA= 0;
    double periodeMnHA= 0;
    double periodeMxHA= 0;
    int nbAngleHA= 0;
    double angleMnHA= 0;
    double angleMxHA= 0;
    double picHA= 0;
    double partageHA= 0;
    int defer= 0;
    int formule= 0;
    int nbValPropres=0;
    int nbIterMax=0;
    double decalValPropres=0;
    double precision=0;

    // Controle de validit� du format des champs

    try {

      // Mod�le de houle

      if (mdlCal.typeModele()==RefondeModeleCalcul.MODELE_HOULE) {

        // Param�tres g�n�raux

        htMer=Double.parseDouble(tfHtMer.getText());
        switch (coCasBord.getSelectedIndex()) {
          case 0:
            casBord=RefondeModeleCalcul.BORD_COND_ORDRE_1;
            break;
          case 1:
            casBord=RefondeModeleCalcul.BORD_COND_ORDRE_2;
            break;
          case 2:
            casBord=RefondeModeleCalcul.BORD_FORM_ANA_ILE;
            break;
          case 3:
            casBord=RefondeModeleCalcul.BORD_FORM_ANA_PORT;
            break;
        }
        ordreMax=Integer.parseInt(tfOrdreMax.getText());
        fondsPoreux=cbFondsPoreux.isSelected();

        // Houle r�guli�re
        if (rbHR.isSelected()) {
          htHoule=Double.parseDouble(tfHtHouleHR.getText());
          periode=Double.parseDouble(tfPeriodeHR.getText());
          angle=((-Double.parseDouble(tfAngleHR.getText())+270)%360+360)%360;
        }

        // Houle al�atoire
        else {
          nbPeriodeHA=Integer.parseInt(tfNbPeriodeHA.getText());
          periodeMnHA=Double.parseDouble(tfPeriodeMnHA.getText());
          periodeMxHA=Double.parseDouble(tfPeriodeMxHA.getText());
          nbAngleHA=Integer.parseInt(tfNbAngleHA.getText());

          // Important B.M. : Les angles utilisateurs tournant dans le sens inverse
          // des angles internes, les angles mini doivent �tre les angles maxi et
          // inversement.
          angleMnHA=((-Double.parseDouble(tfAngleMxHA.getText())+270)%360+360)%
                    360;
          angleMxHA=((-Double.parseDouble(tfAngleMnHA.getText())+270)%360+360)%
                    360;

          htHoule=Double.parseDouble(tfHtHouleHA.getText());
          periode=Double.parseDouble(tfPeriodeHA.getText());
          picHA=Double.parseDouble(tfPicHA.getText());
          angle=((-Double.parseDouble(tfAngleHA.getText())+270)%360+360)%360;
          partageHA=Double.parseDouble(tfPartageHA.getText());
        }

        // D�ferlement
        if (rbSans.isSelected())
          defer=RefondeModeleCalcul.DEFER_SANS;
        else if (rbDissipatif.isSelected())
          defer=RefondeModeleCalcul.DEFER_ITERATIF;
        else
          defer=RefondeModeleCalcul.DEFER_ECRETAGE;

        formule=coFormule.getSelectedIndex();
      }

      // Mod�le de seiche

      else {
        htMer=Double.parseDouble(tfHtMerSeiche.getText());
        nbValPropres=Integer.parseInt(tfNbValPropres.getText());
        nbIterMax=Integer.parseInt(tfNbIterMax.getText());
        decalValPropres=Double.parseDouble(tfDecalValPropres.getText());
        precision=Double.parseDouble(tfPrec.getText());
      }
    }
    catch (NumberFormatException _exc) {
      new BuDialogError(
        null,
        RefondeImplementation.informationsSoftware(),
        "Un des param�tres n'a pas un format valide")
        .activate();
      return false;
    }

    // Contr�le de validit� des champs
    class Err extends Exception {
      public Err(String _msg) {
        super(_msg);
      }
    }

    try {

      // Mod�le de houle
      if (mdlCal.typeModele()==RefondeModeleCalcul.MODELE_HOULE) {

        // Houle al�atoire
        if (rbHA.isSelected()) {
          if (nbPeriodeHA<1||nbPeriodeHA>20)
            throw new Err(
                "Houle al�atoire : Le nombre de p�riodes de houle doit "
                +"�tre compris entre 1 et 20");
          if (nbAngleHA<1||nbAngleHA>20)
            throw new Err(
                "Houle al�atoire : Le nombre de directions de houle "
                +"doit �tre compris entre 1 et 20");
          if (nbPeriodeHA*nbAngleHA<=1)
            throw new Err(
                "Houle al�atoire : Le nombre de directions ou de "
                +"p�riodes de houle doit �tre sup�rieur � 1");
          if (nbPeriodeHA>1&&(periodeMnHA>periodeMxHA))
            throw new Err(
                "Houle al�atoire : La p�riode mini de houle doit �tre "
                +"sup�rieure � la p�riode maxi de houle");
          if (nbPeriodeHA>1
              &&(periode<periodeMnHA||periode>periodeMxHA))
            throw new Err(
                "Houle al�atoire : La p�riode de pic doit �tre comprise "
                +"entre les p�riodes de houle mini et maxi");
          if (picHA<1||picHA>7)
            throw new Err(
                "Houle al�atoire : Le facteur de r�haussement du pic "
                +"doit �tre compris entre 1 et 7");
          //        if (nbAngleHA>1  && (angle<angleMnHA || angle>angleMxHA))
          if (nbAngleHA>1
              &&((angleMnHA<=angleMxHA&&(angle<angleMnHA||angle>angleMxHA))
                 ||(angleMnHA>angleMxHA
                    &&(angle<angleMnHA&&angle>angleMxHA))))
            throw new Err(
                "Houle al�atoire : L'angle principal de houle doit "
                +"�tre compris entre les angles de houle mini et maxi");
          if (partageHA<1||partageHA>100)
            throw new Err(
                "Houle al�atoire : Le param�tre principal de "
                +"r�partition angulaire doit �tre compris entre 1 et 100");
        }
      }

      // Mod�le de seiche
      else {
        if (nbValPropres<1)
          throw new Err(
              "Le nombre de valeurs propres doit �tre sup�rieur � 0.");
        if (nbIterMax<1)
          throw new Err(
              "Le nombre d'it�rations max doit �tre sup�rieur � 0.");
        if (precision<0)
          throw new Err(
              "La pr�cision de convergence doit �tre positive.");
      }
    }
    catch (Err _exc) {
      new BuDialogError(
        null,
        RefondeImplementation.informationsSoftware(),
        _exc.getMessage())
        .activate();
      return false;
    }

    // Mise � jour des param�tres

    // Mod�le de houle
    if (mdlCal.typeModele()==RefondeModeleCalcul.MODELE_HOULE) {

      // Param�tres g�n�raux
      mdlCal.hauteurMer(htMer);
      mdlCal.casBordOuvert(casBord);
      mdlCal.ordreMax(ordreMax);
      mdlCal.setFondsPoreux(fondsPoreux);

      // Houle r�guli�re
      if (rbHR.isSelected()) {
        mdlCal.typeHoule(RefondeModeleCalcul.HOULE_REG);
        mdlCal.hauteurHoule(htHoule);
        mdlCal.periodeHoule(periode);
        mdlCal.angleHoule(angle);
      }

      // Houle al�atoire
      else {
        mdlCal.typeHoule(RefondeModeleCalcul.HOULE_ALEA);
        mdlCal.nbPeriodesHoule(nbPeriodeHA);
        mdlCal.periodeHouleMini(periodeMnHA);
        mdlCal.periodeHouleMaxi(periodeMxHA);
        mdlCal.nbAnglesHoule(nbAngleHA);
        mdlCal.angleHouleMini(angleMnHA);
        mdlCal.angleHouleMaxi(angleMxHA);
        mdlCal.hauteurHoule(htHoule);
        mdlCal.periodeHoule(periode);
        mdlCal.rehaussementPic(picHA);
        mdlCal.angleHoule(angle);
        mdlCal.repartitionAngle(partageHA);
      }

      // D�ferlement
      mdlCal.deferHoule(defer);
      mdlCal.formuleDeferHoule(formule);

      // Mise � jour des angles si l'angle de houle a �t� modifi�e
      if (oldAngle!=mdlCal.angleHoule()) {
        BuDialogConfirmation di;
        di=
            new BuDialogConfirmation(
            null,
            RefondeImplementation.informationsSoftware(),
            "L'angle de houle incidente a chang�.\n"
            +"Voulez vous recalculer les angles d'incidence sur les bords ?");
        if (di.activate()==JOptionPane.YES_OPTION) {
          try {
            mdlCal.calculAngles(projet_);
          }
          catch (IllegalArgumentException _exc) {
            new BuDialogError(
                null,
                RefondeImplementation.informationsSoftware(),
                _exc.getMessage())
                .activate();
          }
          finally {
            fnCalques_.cqAngles.initialise(projet_);
            fnCalques_.getVueCalque().repaint();
          }
        }
      }
    }

    // Mod�le de seiche
    else {
      mdlCal.hauteurMer(htMer);
      mdlCal.nbValPropres(nbValPropres);
      mdlCal.nbIterMax(nbIterMax);
      mdlCal.decalValPropres(decalValPropres);
      mdlCal.precision(precision);
    }
    return true;
  }

  /**
   * Pour test de la boite de dialogue.
   */
  public static void main(String[] _args) {
    try {
      UIManager.setLookAndFeel(
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception _exc) {}
    new RefondeDialogParametresCalcul().show();
  }
}
