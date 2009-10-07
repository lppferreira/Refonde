package org.fudaa.fudaa.refonde;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import com.memoire.bu.BuFileFilter;
import com.memoire.bu.BuGridLayout;

import org.fudaa.ctulu.CtuluLibString;

import org.fudaa.ebli.dialog.BFileChooser;

/**
 * Un panneau de définition d'un nouveau projet.
 *
 * @version      $Id: RefondePnNouveauProjet.java,v 1.5 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondePnNouveauProjet extends JPanel {
  BuGridLayout lyThis = new BuGridLayout();
  JLabel lbType = new JLabel();
  JComboBox coType = new JComboBox();
  JButton btDummy = new JButton();
  JLabel lbFichiers = new JLabel();
  JTextField tfFichiers = new JTextField();
  JButton btFichiers = new JButton();
  Border bdThis;

  private static final String TYPE_MODELE_HOULE ="Modèle de houle";
  private static final String TYPE_MODELE_SEICHE="Modèle de seiches";


  public RefondePnNouveauProjet() {
    try {
      jbInit();
      customize();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    bdThis = BorderFactory.createEmptyBorder(5,5,5,5);
    lbType.setHorizontalAlignment(SwingConstants.RIGHT);
    lbType.setText("Type de modèle :");
    lyThis.setColumns(3);
    lyThis.setHfilled(true);
    lyThis.setHgap(3);
    lyThis.setVfilled(false);
    lyThis.setVgap(5);
    this.setLayout(lyThis);
    lbFichiers.setHorizontalAlignment(SwingConstants.RIGHT);
    lbFichiers.setText("Géométrie :");
    btFichiers.setText("...");
    btFichiers.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btFichiers_actionPerformed(e);
      }
    });
    tfFichiers.setPreferredSize(new Dimension(200, 21));
    tfFichiers.setText("");
    btDummy.setText("");
    this.setBorder(bdThis);
    this.add(lbType, null);
    this.add(coType, null);
    this.add(btDummy, null);
    this.add(lbFichiers, null);
    this.add(tfFichiers, null);
    this.add(btFichiers, null);
  }

  /**
   * Ce qui n'est pas produit par le RAD.
   */
  private void customize() {
    coType.addItem(TYPE_MODELE_HOULE);
    coType.addItem(TYPE_MODELE_SEICHE);
    btDummy.setVisible(false);
  }

  /**
   * Sélection du fichier.
   * @param _e Evènement déclenchant la méthode.
   */
  void btFichiers_actionPerformed(ActionEvent _e) {
    BFileChooser fc= new BFileChooser();
    fc.setFileHidingEnabled(true);
    String s=tfFichiers.getText();
    if (!s.endsWith(".10") && !s.endsWith(".12")) s+=".12";
    fc.setSelectedFile(new File(s));
    fc.setMultiSelectionEnabled(false);
    BuFileFilter[] filtresVag_=
      { new BuFileFilter(new String[] { "10", "12" }, "Géométrie Vag") };
    fc.setDialogTitle("Fichiers de géométrie");
    fc.resetChoosableFileFilters();
    fc.addChoosableFileFilter(filtresVag_[0]);
    fc.setFileFilter(filtresVag_[0]);

    if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
      s=fc.getSelectedFile().getPath();
      if (s.endsWith(".10") || s.endsWith(".12"))
        s=s.substring(0,s.lastIndexOf(CtuluLibString.DOT));
      setSelectedFile(new File(s));
    }
  }

  /**
   * Définition du fichier sélectionné sans extension.
   */
  public void setSelectedFile(File _file) {
    tfFichiers.setText(_file.getPath());
  }

  /**
   * Retourne le fichier sélectionné sans extension.
   */
  public File getSelectedFile() {
    return new File(tfFichiers.getText());
  }

  /**
   * Retourne le type de modele de données.
   */
  public int getTypeModele() {
    int r;
    if (coType.getSelectedItem()==TYPE_MODELE_HOULE)
      r=RefondeModeleCalcul.MODELE_HOULE;
    else
      r=RefondeModeleCalcul.MODELE_SEICHE;

    return r;
  }

  /**
   * Pour test.
   */
  public static void main(String[] args) {
    JDialog f= new JDialog((Frame) null, "Nouveau projet", true);
    RefondePnNouveauProjet pn= new RefondePnNouveauProjet();
    pn.setSelectedFile(new File("C:/temp/essai"));
    f.getContentPane().add(pn, BorderLayout.CENTER);
    f.pack();
    f.show();
    System.exit(0);
  }
}
