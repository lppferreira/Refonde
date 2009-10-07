/*
 * @file         RefondeDialogProprietesProjet.java
 * @creation     1999-07-09
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.memoire.bu.BuDialogMessage;
import com.memoire.bu.BuFileFilter;
import com.memoire.bu.BuGridLayout;

import org.fudaa.ctulu.CtuluLibString;

import org.fudaa.ebli.dialog.BFileChooser;

import org.fudaa.fudaa.commun.impl.FudaaDialog;

/**
 * Une boite de dialogue affichant les propriétés du projet.
 *
 * @version      $Id: RefondeDialogProprietesProjet.java,v 1.10 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogProprietesProjet extends FudaaDialog {
  RefondeProjet projet_;
  JPanel pnFichiers= new JPanel();
  TitledBorder bdpnFichiers= new TitledBorder("");
  BuGridLayout lyFichiers= new BuGridLayout();
  JLabel lbMai= new JLabel();
  JTextField tfMai= new JTextField();
  JButton btMai= new JButton();
  BFileChooser diMai= new BFileChooser();
  String[] exMai;
  JLabel lbMod= new JLabel();
  JTextField tfMod= new JTextField();
  JButton btMod= new JButton();
  BFileChooser diMod= new BFileChooser();
  String[] exMod;
  JLabel lbCal= new JLabel();
  JTextField tfCal= new JTextField();
  JButton btCal= new JButton();
  BFileChooser diCal= new BFileChooser();
  String[] exCal;
  JPanel pnType=new JPanel();
  BorderLayout lyType = new BorderLayout();
  JLabel lbType = new JLabel();
  JComboBox coType = new JComboBox();
  Border bdpnType;

  boolean enableEvents;
  private static final String TYPE_MODELE_HOULE ="Modèle de houle";
  private static final String TYPE_MODELE_SEICHE="Modèle de seiches";

  public RefondeDialogProprietesProjet() {
    this(null);
  }

  public RefondeDialogProprietesProjet(Frame _parent) {
    // Le frame principal et le panel des boutons est créé et géré par la classe
    // mère. On ne crée que le panel principal.
    super(_parent, OK_CANCEL_OPTION);
    jbInit();
    customize();
    enableEvents=true;
  }

  public void jbInit() {
    BuFileFilter[] filtresFichier;
    bdpnType = BorderFactory.createEmptyBorder(5,5,5,5);
    lyFichiers.setHgap(3);
    lyFichiers.setColumns(3);
    lyFichiers.setVgap(3);
    bdpnFichiers.setBorder(BorderFactory.createEtchedBorder());
    bdpnFichiers.setTitle("Fichiers projet");
    pnFichiers.setLayout(lyFichiers);
    pnFichiers.setBorder(bdpnFichiers);
    pnType.setLayout(lyType);
    lbType.setText("Type de modèle de données");
    lyType.setHgap(5);
    lyType.setVgap(5);
    pnType.setBorder(bdpnType);
    coType.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        coType_itemStateChanged(e);
      }
    });
    pnAffichage_.add("North", pnType);
    pnAffichage_.add("Center", pnFichiers);
    lbMai.setText("Maillage");
    tfMai.setPreferredSize(new Dimension(200, 19));
    btMai.setText("...");
    btMai.setPreferredSize(new Dimension(30, 23));
    btMai.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        bt_actionPerformed(_evt);
      }
    });
    exMai= new String[] { "cor", "ele", "bth" };
    filtresFichier=
      new BuFileFilter[] { new BuFileFilter(exMai, lbMai.getText())};
    diMai.setFileHidingEnabled(true);
    diMai.setMultiSelectionEnabled(false);
    diMai.setDialogTitle("Fichier " + lbMai.getText());
    diMai.resetChoosableFileFilters();
    diMai.addChoosableFileFilter(filtresFichier[0]);
    diMai.setFileFilter(filtresFichier[0]);
    //    diMai.updateUI();
    lbMod.setText("Modèle de propriétés");
    tfMod.setPreferredSize(new Dimension(200, 19));
    btMod.setText("...");
    btMod.setPreferredSize(new Dimension(30, 23));
    btMod.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        bt_actionPerformed(_evt);
      }
    });
    exMod= new String[] { "prp" };
    filtresFichier=
      new BuFileFilter[] { new BuFileFilter(exMod, lbMod.getText())};
    diMod.setFileHidingEnabled(true);
    diMod.setMultiSelectionEnabled(false);
    diMod.setDialogTitle("Fichier " + lbMod.getText());
    diMod.resetChoosableFileFilters();
    diMod.addChoosableFileFilter(filtresFichier[0]);
    diMod.setFileFilter(filtresFichier[0]);
    //    diMod.updateUI();
    lbCal.setText("Modèle de calcul");
    tfCal.setPreferredSize(new Dimension(200, 19));
    btCal.setText("...");
    btCal.setPreferredSize(new Dimension(30, 23));
    btCal.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        bt_actionPerformed(_evt);
      }
    });
    exCal= new String[] { "cal" };
    filtresFichier=
      new BuFileFilter[] { new BuFileFilter(exCal, lbCal.getText())};
    diCal.setFileHidingEnabled(true);
    diCal.setMultiSelectionEnabled(false);
    diCal.setDialogTitle("Fichier " + lbCal.getText());
    diCal.resetChoosableFileFilters();
    diCal.addChoosableFileFilter(filtresFichier[0]);
    diCal.setFileFilter(filtresFichier[0]);
    //    diCal.updateUI();
    // Ajout du panneau du type de modèle

    // Ajout des composants au panel
    pnFichiers.add(lbMai);
    pnFichiers.add(tfMai);
    pnFichiers.add(btMai);
    pnFichiers.add(lbMod);
    pnFichiers.add(tfMod);
    pnFichiers.add(btMod);
    pnFichiers.add(lbCal);
    pnFichiers.add(tfCal);
    pnFichiers.add(btCal);
    pnType.add(lbType,  BorderLayout.WEST);
    pnType.add(coType,  BorderLayout.CENTER);
    //    setSize(getPreferredSize());
  }

  /**
   * Ce que ne peut faire JB.
   */
  public void customize() {
    coType.addItem(TYPE_MODELE_HOULE);
    coType.addItem(TYPE_MODELE_SEICHE);

    // B.M. Pour l'instant, on ne peut modifier les fichiers associés
    tfMai.setEnabled(false);
    btMai.setEnabled(false);
    tfMod.setEnabled(false);
    btMod.setEnabled(false);
    tfCal.setEnabled(false);
    btCal.setEnabled(false);
    setResizable(false);
    pack();
  }

  /**
   * Affectation du projet à la boite de dialogue.
   */
  public void setProjet(RefondeProjet _projet) {
    projet_= _projet;
    tfMai.setText(_projet.getFichierMaillage().getPath());
    diMai.setSelectedFile(_projet.getFichierMaillage());
    diMai.setCurrentDirectory(_projet.getFichierMaillage());
    tfMod.setText(_projet.getFichierModele().getPath());
    diMod.setSelectedFile(_projet.getFichierModele());
    diMod.setCurrentDirectory(_projet.getFichierModele());
    tfCal.setText(_projet.getFichierCalcul().getPath());
    diCal.setSelectedFile(_projet.getFichierCalcul());
    diCal.setCurrentDirectory(_projet.getFichierCalcul());

    // Type de calcul
    enableEvents=false;
    if (projet_.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_SEICHE)
      coType.setSelectedItem(TYPE_MODELE_SEICHE);
    else
      coType.setSelectedItem(TYPE_MODELE_HOULE);

    enableEvents=true;
  }

  /**
   * Retourne le projet initialisé.
   */
  private RefondeProjet getProjet() {
    projet_.setFichierMaillage(new File(tfMai.getText()));
    projet_.setFichierModele(new File(tfMod.getText()));
    projet_.setFichierCalcul(new File(tfCal.getText()));
    return projet_;
  }

  /**
   * Retourne le type de modèle de données.
   * @return Le type du modèle (seiche)
   */
  private int getTypeModele() {
    if (coType.getSelectedItem().equals(TYPE_MODELE_SEICHE))
      return RefondeModeleCalcul.MODELE_SEICHE;
    return RefondeModeleCalcul.MODELE_HOULE;
  }

  //--- Surcharge FudaaDialog { ------------------------------------------------

  protected void btOkActionPerformed(ActionEvent _evt) {
    projet_= getProjet();
    int type=getTypeModele();
    if (type!=projet_.getModeleCalcul().typeModele()) projet_.transmute(type);
    super.btOkActionPerformed(_evt);
  }

  protected void btApplyActionPerformed(ActionEvent _evt) {
    projet_= getProjet();
    int type=getTypeModele();
    if (type!=projet_.getModeleCalcul().typeModele()) projet_.transmute(type);
    super.btApplyActionPerformed(_evt);
  }

  void bt_actionPerformed(ActionEvent _evt) {
    JButton bt= (JButton)_evt.getSource();
    BFileChooser di;
    JTextField tf;
    String[] exts;
    String nmfc;
    if (bt == btMai) {
      di= diMai;
      tf= tfMai;
      exts= exMai;
    } else if (bt == btMod) {
      di= diMod;
      tf= tfMod;
      exts= exMod;
    } else {
      di= diCal;
      tf= tfCal;
      exts= exCal;
    }
    int r= di.showDialog(this, "Ok");
    // Bouton OK activé. Changement du nom du fichier
    if (r == JFileChooser.APPROVE_OPTION) {
      nmfc= di.getSelectedFile().getPath();
      // Si plus d'une extension possible => Nom du fichier sans extension
      if (exts.length > 1) {
        for (int i= 0; i < exts.length; i++) {
          if (nmfc.endsWith(CtuluLibString.DOT + exts[i])) {
            nmfc= nmfc.substring(0, nmfc.lastIndexOf(CtuluLibString.DOT + exts[i]));
            break;
          }
        }
      }
      tf.setText(nmfc);
    }
  }

  void coType_itemStateChanged(ItemEvent e) {
    if (!enableEvents) return;
    if (e.getStateChange()==ItemEvent.DESELECTED) return;

    // Avertissement des modifications du modèle de données.
    BuDialogMessage di=new BuDialogMessage(RefondeImplementation.application(),
                        RefondeImplementation.informationsSoftware(),
      "Attention : Le projet sera dégradé pour correspondre\n"+
      "au type de modèle de données choisi.");

    di.activate();
  }
}
