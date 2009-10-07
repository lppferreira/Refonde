/*
 * @file         RefondeDialogGroupeProprietes.java
 * @creation     1999-08-16
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.memoire.bu.BuCheckBox3States;
import com.memoire.bu.BuGridLayout;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue permettant d'afficher un groupe de proprietes.
 *
 * @version      $Id: RefondeDialogGroupeProprietes.java,v 1.10 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogGroupeProprietes extends FudaaDialog {
  private static final int SELECTED= BuCheckBox3States.STATE_SELECTED;
  private static final int DESELECTED= BuCheckBox3States.STATE_DESELECTED;
  private static final int MIXED= BuCheckBox3States.STATE_MIXED;
  JComboBox coType;
  JPanel pnProprietes;
  Object[] bdSelect_;
  //  RefondeProjet    projet_;
  RefondeCourbe[] courbes_;
  int[] tpGrps_;
  ItemListener casListener; // Un listener pour le cbTransitoire
  ItemListener imposeListener; // Un listener pour le cbImpose
  ItemListener lockListener; // Un listener qui bloque le changement d'état
  Hashtable cb2Lock= new Hashtable();
  public RefondeDialogGroupeProprietes() {
    this(null);
  }
  public RefondeDialogGroupeProprietes(Frame _parent) {
    super(_parent, OK_CANCEL_APPLY_OPTION);
    /**Jbuilder**/
    jbInit();
  }
  public void jbInit() { /**JBuilder**/
    JPanel pnType= new JPanel();
    FlowLayout lyType= new FlowLayout();
    JLabel laType= new JLabel();
    TitledBorder bdProprietes= new TitledBorder("");
    CardLayout lyProprietes= new CardLayout();
    coType= new JComboBox();
    pnProprietes= new JPanel();
    laType.setText("Type");
    pnType.setLayout(lyType);
    pnType.add(laType, null);
    pnType.add(coType, null);
    bdProprietes.setTitle("Propriétés");
    pnProprietes.setLayout(lyProprietes);
    pnProprietes.setBorder(bdProprietes);
    pnAffichage_.add(pnProprietes, BorderLayout.CENTER);
    pnAffichage_.add(pnType, BorderLayout.NORTH);
    setModal(true);
    setTitle("Groupe de propriétés");
    coType.addItem("<mixte>");
    coType.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        type_itemStateChanged(_evt);
      }
    });
    pnProprietes.add(new JPanel(), new Integer(0).toString());
    casListener= new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        cas_itemStateChanged(_evt);
      }
    };
    imposeListener= new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        impose_itemStateChanged(_evt);
      }
    };
    lockListener= new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        lock_itemStateChanged(_evt);
      }
    };
    pack();
  }
  /**
   * Construction de tous les panels proprietes à partir de la définition des
   * groupes possibles
   *
   * @param _tpGrps  Type des groupes possibles, visible dans l'ihm
   */
  public void setGroupesProprietes(int[] _tpGrps) {
    tpGrps_= _tpGrps;
    JPanel sspnProprietes;
    BuGridLayout sslyProprietes;
    BuCheckBox3States cbImpose;
    //    JLabel            lbPrp;
    BuCheckBox3States cbTransitoire;
    JTextField tfValeur;
    JComboBox coCourbe;
    JPanel pnValeur;
    CardLayout lyValeur;
    String[] lbGrps= new String[_tpGrps.length];
    int[][] imposes= new int[_tpGrps.length][];
    int[][] cas= new int[_tpGrps.length][];
    double[][] valeurs= new double[_tpGrps.length][];
    String[][] lbPrps= new String[_tpGrps.length][];
    for (int i= 0; i < _tpGrps.length; i++) {
      int[] tpPrps;
      lbGrps[i]= RefondeGroupeProprietes.labels[_tpGrps[i]];
      imposes[i]= RefondeGroupeProprietes.imposeProprietes[_tpGrps[i]];
      cas[i]= RefondeGroupeProprietes.cas[_tpGrps[i]];
      valeurs[i]= RefondeGroupeProprietes.valeursDefaut[_tpGrps[i]];
      tpPrps= RefondeGroupeProprietes.typesProprietes[_tpGrps[i]];
      lbPrps[i]= new String[tpPrps.length];
      for (int j= 0; j < tpPrps.length; j++)
        lbPrps[i][j]= RefondePropriete.labels[tpPrps[j]];
    }
    // Pour tous les groupes
    for (int i= 0; i < lbGrps.length; i++) {
      coType.addItem(lbGrps[i]);
      sspnProprietes= new JPanel();
      sslyProprietes= new BuGridLayout(3, 5, 5, false, false);
      sslyProprietes.setXAlign(0);
      sspnProprietes.setLayout(sslyProprietes);
      pnProprietes.add(sspnProprietes, new Integer(i + 1).toString());
      // Pour toutes les propriétés du groupe, construction des panels
      for (int j= 0; j < lbPrps[i].length; j++) {
        cbImpose= new BuCheckBox3States();
        cbImpose.setText(lbPrps[i][j]);
        cbImpose.addItemListener(imposeListener);
        sspnProprietes.add(cbImpose);
        //        lbPrp        =new BuLabel();
        //        lbPrp        .setText(lbPrps[i][j]);
        //        sspnProprietes.add(lbPrp);
        cbTransitoire= new BuCheckBox3States();
        cbTransitoire.setText("Transitoire");
        cbTransitoire.setEnabled(true);
        //        cbTransitoire.addItemListener(casListener);
        sspnProprietes.add(cbTransitoire);
        tfValeur= new JTextField();
        tfValeur.setText("" + valeurs[i][j]);
        tfValeur.setPreferredSize(new Dimension(70, 19));
        tfValeur.setEnabled(true);
        tfValeur.setToolTipText("Valeur stationnaire");
        coCourbe= new JComboBox();
        coCourbe.addItem("<mixte>");
        coCourbe.setEnabled(true);
        coCourbe.setToolTipText("Valeur transitoire");
        //        coCourbe     .addItemListener(new ItemListener() {
        //                        public void itemStateChanged(ItemEvent _evt) {
        //                          courbe_itemStateChanged(_evt);
        //                        }
        //                      });
        pnValeur= new JPanel();
        lyValeur= new CardLayout();
        pnValeur.setLayout(lyValeur);
        pnValeur.add(tfValeur, new Integer(DESELECTED).toString());
        pnValeur.add(coCourbe, new Integer(SELECTED).toString());
        lyValeur.show(
          pnValeur,
          new Integer(cbTransitoire.getState()).toString());
        sspnProprietes.add(pnValeur);
        // Initialisation des panels
        cbImpose.setState(SELECTED);
        cb2Lock.put(cbImpose, new Integer(imposes[i][j]));
        if (imposes[i][j] == 1) {
          cbImpose.removeItemListener(imposeListener);
          cbImpose.addItemListener(lockListener);
        }
        if (cas[i][j] == RefondePropriete.MIXTE) {
          cb2Lock.put(cbTransitoire, new Integer(MIXED));
          cbTransitoire.addItemListener(casListener);
          cbTransitoire.setState(DESELECTED);
        } else if (cas[i][j] == RefondePropriete.TRANSITOIRE) {
          cbTransitoire.setVisible(false);
          //          cb2Lock.put(cbTransitoire,new Integer(SELECTED));
          //          cbTransitoire.addItemListener(lockListener);
          //          cbTransitoire.setState(SELECTED);
        } else if (cas[i][j] == RefondePropriete.STATIONNAIRE) {
          cbTransitoire.setVisible(false);
          //          cb2Lock.put(cbTransitoire,new Integer(DESELECTED));
          //          cbTransitoire.addItemListener(lockListener);
          cbTransitoire.setState(DESELECTED);
        }
      }
    }
  }
  /**
   * Définition des courbes possibles
   * @param _courbes Les courbes d'évolution
   */
  public void setCourbes(RefondeCourbe[] _courbes) {
    courbes_= _courbes;
    Component[] cp;
    Component[] sspn= pnProprietes.getComponents();
    JComboBox coCourbe;
    for (int i= 0; i < sspn.length; i++) {
      cp= ((JPanel)sspn[i]).getComponents();
      for (int j= 0; j < cp.length; j += 3) {
        coCourbe= (JComboBox) ((JPanel)cp[j + 2]).getComponent(1);
        coCourbe.removeAllItems();
        coCourbe.addItem("<mixte>");
        for (int k= 0; k < courbes_.length; k++)
          coCourbe.addItem(courbes_[k].getName());
      }
    }
  }
  /**
   * Initialisation du dialog pour le projet donné pour les bdObjets sélectionnés.
   * En cas bde classe mère FudaaDialogGroupeProprietes, cette méthode pourrait
   * être la seule à être implémentée dans la classe fille
   */
  //  public void initialise(RefondeProjet _projet,Object[] _bdSelect) {
  public void initialise(Object[] _bdSelect) {
    //    projet_ =_projet;
    bdSelect_= _bdSelect;
    //    RefondeModeleProprietes mdlPrp=_projet.getModeleProprietes();
    RefondeGroupeProprietes gPrp;
    RefondePropriete[] prps= null;
    Integer type= null;
    boolean trans;
    Double valeur= null;
    RefondeCourbe courbe= null;
    Integer gPrpType= null;
    int[] prpsTrans= null; // Stationnaire/Transitoire/Mixte
    Double[] prpsValeur= null; // Valeur/Mixte
    RefondeCourbe[] prpsCourbe= null; // Courbe/Mixte
    //--------------------------------------------------------------------------
    // Recherche du niveau d'équivalence des groupes de propriétés des objets
    // sélectionnés
    //--------------------------------------------------------------------------
    for (int i= 0; i < _bdSelect.length; i++) {
      gPrp=
        ((RefondeSupporteGroupeProprietes)_bdSelect[i]).getGroupeProprietes();
      type= new Integer(gPrp.getType());
      prps= gPrp.getProprietes();
      if (i == 0) {
        prpsTrans= new int[prps.length];
        prpsValeur= new Double[prps.length];
        prpsCourbe= new RefondeCourbe[prps.length];
        gPrpType= type;
      } else {
        if (gPrpType.intValue() != type.intValue()) {
          gPrpType= null;
          break;
        }
      }
      for (int j= 0; j < prps.length; j++) {
        trans= (prps[j].getComportement() == RefondePropriete.TRANSITOIRE);
        if (trans)
          courbe= prps[j].getCourbe();
        else
          valeur= new Double(prps[j].getValeur());
        if (i == 0) {
          prpsTrans[j]= (trans ? SELECTED : DESELECTED);
          if (trans)
            prpsCourbe[j]= courbe;
          else
            prpsValeur[j]= valeur;
        } else {
          if (prpsTrans[j] == SELECTED) {
            if (trans) {
              if (prpsCourbe[j] != courbe)
                prpsCourbe[j]= null;
            } else
              prpsTrans[j]= MIXED;
          } else if (prpsTrans[j] == DESELECTED) {
            if (!trans) {
              if (prpsValeur[j] != null
                && prpsValeur[j].doubleValue() != valeur.doubleValue())
                prpsValeur[j]= null;
            } else
              prpsTrans[j]= MIXED;
          }
        }
      }
    }
    //--------------------------------------------------------------------------
    // Initialisation de l'interface
    //--------------------------------------------------------------------------
    // Sélection du type
    if (gPrpType == null) {
      coType.setSelectedItem("<mixte>");
    } else {
      coType.setSelectedItem(
        RefondeGroupeProprietes.labels[gPrpType.intValue()]);
      // Sélection des autres composants (propriétés)
      {
        JPanel sspn=
          (JPanel)pnProprietes.getComponent(coType.getSelectedIndex());
        JPanel pnValeur;
        Component[] cp= sspn.getComponents();
        for (int i= 0; i < prps.length; i++) {
          //          ((BuCheckBox3States)cp[i*3]).setState(prpsImpose[i]);
          //          if (prpsImpose[i]!=MIXED) {
           ((BuCheckBox3States)cp[i * 3 + 1]).setState(prpsTrans[i]);
          pnValeur= (JPanel)cp[i * 3 + 2];
          if (prpsTrans[i] == SELECTED) {
            //           if (prpsCourbe[i]!=null)((JComboBox)pnValeur.getComponent(1)).setSelectedItem(nomCourbe[i]));
            //           else                    ((JComboBox)pnValeur.getComponent(1)).setSelectedItem("<mixte>");
             ((JComboBox)pnValeur.getComponent(1)).setSelectedItem("<mixte>");
          } else if (prpsTrans[i] == DESELECTED) {
            if (prpsValeur[i] != null)
              ((JTextField)pnValeur.getComponent(0)).setText(
                prpsValeur[i].toString());
            else
               ((JTextField)pnValeur.getComponent(0)).setText("");
          }
          //          }
        }
      }
    }
  }
  /**

   * Bouton "Ok" pressé, effacage du dialog, traitement dans le listener du dialog
   */
  protected void btOkActionPerformed(ActionEvent _evt) {
    majBDD();
    super.btOkActionPerformed(_evt);
  }
  /**
   * Bouton "Appliquer" pressé, traitement dans le listener du dialog
   */
  protected void btApplyActionPerformed(ActionEvent _evt) {
    majBDD();
    super.btApplyActionPerformed(_evt);
  }
  // ---------------------------------------------------------------------------
  // Mise à jour des groupes de propriétés des objets sélectionnés
  // ---------------------------------------------------------------------------
  private void majBDD() {
    //    RefondeModeleProprietes mdlPrp=projet_.getModeleProprietes();
    RefondeGroupeProprietes gPrpNew;
    RefondeGroupeProprietes gPrpOld;
    RefondeSupporteGroupeProprietes spGprp;
    RefondePropriete[] prps;
    RefondePropriete[] prpsOldBdSelect;
    RefondePropriete[] prpsNewBdSelect;
    // Le groupe est mixte => Pas de modification
    if (coType.getSelectedIndex() == 0)
      return;
    // Création des propriétés pour celles qui ne sont pas laissées
    // par défaut
    {
      JPanel sspn= (JPanel)pnProprietes.getComponent(coType.getSelectedIndex());
      JPanel pnValeur;
      Component[] cp= sspn.getComponents();
      JTextField tfValeur;
      JComboBox coCourbe;
      int[] tpPrps;
      int tpGrp;
      int trans;
      Double valeur;
      RefondeCourbe courbe;
      tpGrp= tpGrps_[coType.getSelectedIndex() - 1];
      tpPrps= RefondeGroupeProprietes.typesProprietes[tpGrp];
      prps= new RefondePropriete[tpPrps.length];
      for (int i= 0; i < prps.length; i++) {
        prps[i]= null;
        trans= ((BuCheckBox3States)cp[i * 3 + 1]).getState();
        pnValeur= (JPanel)cp[i * 3 + 2];
        if (trans == SELECTED) {
          coCourbe= (JComboBox)pnValeur.getComponent(1);
          if (coCourbe.getSelectedIndex() != 0) {
            courbe= courbes_[coCourbe.getSelectedIndex() - 1];
            prps[i]= new RefondePropriete(tpPrps[i], courbe);
          }
        } else if (trans == DESELECTED) {
          tfValeur= (JTextField)pnValeur.getComponent(0);
          if (!tfValeur.getText().equals("")) {
            valeur= new Double(tfValeur.getText());
            prps[i]= new RefondePropriete(tpPrps[i], valeur.doubleValue());
          }
        }
      }
      gPrpNew= new RefondeGroupeProprietes(tpGrp, prps);
    }
    // Si toutes les propriétés sont non nulles, on peut créer un seul groupe
    // pour tous les objets sélectionnés
    //    boolean allNull=true;
    //    for int i=0; i<prps.length; i++) {
    //      if (prps[i]!=null) {
    //        break;
    //        allNull=false;
    //      }
    //    }
    // Création des groupes de propriétés
    for (int i= 0; i < bdSelect_.length; i++) {
      spGprp= (RefondeSupporteGroupeProprietes)bdSelect_[i];
      gPrpOld= spGprp.getGroupeProprietes();
      //      gPrpOld=mdlPrp.getGroupeProprietes((GrPolyligne)bdSelect_[i]);
      if (gPrpOld.getType() != gPrpNew.getType()) {
        spGprp.setGroupeProprietes(gPrpNew);
        //        mdlPrp.setGroupeProprietes((GrPolyligne)bdSelect_[i],gPrpNew);
      } else {
        prpsNewBdSelect= new RefondePropriete[prps.length];
        prpsOldBdSelect= gPrpOld.getProprietes();
        for (int j= 0; j < prps.length; j++) {
          if (prps[j] == null)
            prpsNewBdSelect[j]= prpsOldBdSelect[j];
          else
            prpsNewBdSelect[j]= prps[j];
        }
        //        mdlPrp.setGroupeProprietes((GrPolyligne)bdSelect_[i],
        spGprp.setGroupeProprietes(
          new RefondeGroupeProprietes(gPrpNew.getType(), prpsNewBdSelect));
      }
    }
  }
  void lock_itemStateChanged(ItemEvent _evt) {
    BuCheckBox3States cb= (BuCheckBox3States)_evt.getSource();
    cb.removeItemListener(lockListener);
    int state= ((Integer)cb2Lock.get(cb)).intValue();
    cb.setState(state);
    cb.addItemListener(lockListener);
  }
  // ---------------------------------------------------------------------------
  // Changement de type
  // ---------------------------------------------------------------------------
  void type_itemStateChanged(ItemEvent _evt) {
    if (coType.getSelectedItem() == _evt.getItem()) {
      String card= new Integer(coType.getSelectedIndex()).toString();
      ((CardLayout)pnProprietes.getLayout()).show(pnProprietes, card);
    }
  }
  // ---------------------------------------------------------------------------
  // Changement d'état de propriété
  // ---------------------------------------------------------------------------
  void impose_itemStateChanged(ItemEvent _evt) {
    //BuCheckBox3States cbImpose= (BuCheckBox3States)_evt.getSource();
    //    int state=cbImpose.getState();
    // On n'autorise pas le passage à mixte
    /*    if (state == MIXED) {
          checkBoxPE.setState((state+1)%3);
          return;
        } */
    //    changePE(lbPrp,state);
  }
  void cas_itemStateChanged(ItemEvent _evt) {
    //BuCheckBox3States cbTransitoire= (BuCheckBox3States)_evt.getSource();
    //    int state=cbTransitoire.getState();
    //    changeCas(cbTransitoire, state);
  }
}
