/*
 * @file         RefondeDialogFichiersProjet.java
 * @creation     1999-07-09
 * @modification $Date: 2007-01-19 13:14:15 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.memoire.bu.BuFileFilter;
import com.memoire.bu.BuGridLayout;

import org.fudaa.ctulu.CtuluLibString;

import org.fudaa.ebli.dialog.BFileChooser;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Une boite de dialogue affichant les fichiers projets.
 *
 * @version      $Id: RefondeDialogFichiersProjet.java,v 1.8 2007-01-19 13:14:15 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeDialogFichiersProjet extends FudaaDialog {
  private class Fichier {
    JCheckBox cb= new JCheckBox();
    JTextField tf= new JTextField();
    JButton bt= new JButton();
    BFileChooser di= new BFileChooser();
  }
  JPanel pnFichiers= new JPanel();
  BuGridLayout lyFichiers= new BuGridLayout();
  Hashtable cb2Fc;
  Hashtable bt2Fc;
  Hashtable tp2Fc;
  Hashtable di2Exts;
  public RefondeDialogFichiersProjet() {
    this(null);
  }
  public RefondeDialogFichiersProjet(Frame _parent) {
    // Le frame principal et le panel des boutons est créé et géré par la classe
    // mère. On ne crée que le panel principal.
    super(_parent, OK_CANCEL_OPTION);
    jbInit();
  }
  public void jbInit() {
    cb2Fc= new Hashtable();
    bt2Fc= new Hashtable();
    tp2Fc= new Hashtable();
    di2Exts= new Hashtable();
    lyFichiers.setHgap(3);
    lyFichiers.setColumns(3);
    lyFichiers.setVgap(3);
    pnFichiers.setLayout(lyFichiers);
    pnAffichage_.add("Center", pnFichiers);
    setResizable(false);
    //    pack();
    //    setSize(getPreferredSize());
  }
  /**
   * Ajoute un fichier projet.
   * @param _libelle Libellé du fichier projet. Ce libellé apparaît dans la
   *                 fenetre pour désigner le fichier (maillage, etc.)
   * @param _type    Type du fichier. Sert pour récupérer les informations si
   *                 OK ou APPLY est validé.
   * @param _fichier Fichier initial
   * @param _exts    Extensions possibles pour ce fichier
   * @param _etat    Etat du fichier, pris en compte ou non en cas de validation
   *                 de la boite
   */
  public void ajoute(
    String _libelle,
    String _type,
    File _fichier,
    String[] _exts,
    boolean _etat) {
    Fichier fc= new Fichier();
    bt2Fc.put(fc.bt, fc);
    cb2Fc.put(fc.cb, fc);
    tp2Fc.put(_type, fc);
    fc.cb.setText(_libelle);
    fc.cb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        fccb_itemStateChanged(_evt);
      }
    });
    fc.cb.setSelected(_etat);
    fc.tf.setPreferredSize(new Dimension(200, 19));
    fc.tf.setText(_fichier.getPath());
    fc.bt.setText("...");
    fc.bt.setPreferredSize(new Dimension(30, 23));
    fc.bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        fcbt_actionPerformed(_evt);
      }
    });
    // Ajout des composants au panel
    pnFichiers.add(fc.cb);
    pnFichiers.add(fc.tf);
    pnFichiers.add(fc.bt);
    fc.di.setFileHidingEnabled(true);
    fc.di.setSelectedFile(_fichier);
    fc.di.setCurrentDirectory(_fichier);
    fc.di.setMultiSelectionEnabled(false);
    BuFileFilter[] filtresFichier= { new BuFileFilter(_exts, _libelle)};
    fc.di.setDialogTitle("Fichier " + _libelle);
    fc.di.resetChoosableFileFilters();
    fc.di.addChoosableFileFilter(filtresFichier[0]);
    fc.di.setFileFilter(filtresFichier[0]);
    //    fc.di.updateUI();
    di2Exts.put(fc.di, _exts);
  }
  /**
   * Retourne l'état du fichier (pris en compte ou non).
   * @param _type type du fichier
   * @return <I>true</I> si le fichier est a prendre en compte
   *         <I>false</I> sinon
   */
  public boolean getEtat(String _type) {
    Fichier fc;
    if ((fc= (Fichier)tp2Fc.get(_type)) == null)
      return false;
    return fc.cb.isSelected();
  }
  /**
   * Retourne le fichier associé.
   */
  public File getFile(String _type) {
    Fichier fc;
    if ((fc= (Fichier)tp2Fc.get(_type)) == null)
      return null;
    return new File(fc.tf.getText());
  }
  //----------------------------------------------------------------------------
  //---  Actions  --------------------------------------------------------------
  //----------------------------------------------------------------------------
  void fcbt_actionPerformed(ActionEvent _evt) {
    JButton bt= (JButton)_evt.getSource();
    Fichier fc= (Fichier)bt2Fc.get(bt);
    //    String       nom      =(String)      bt2Nom.get(btFichier);
    //    BFileChooser diFichier=(BFileChooser)nom2Di.get(nom);
    //    JTextField   tfFichier=(JTextField)  nom2Tf.get(nom);
    String[] exts= (String[])di2Exts.get(fc.di);
    String nmfc;
    int r= fc.di.showDialog(this, "Ok");
    // Bouton OK activé. Changement du nom du fichier
    if (r == JFileChooser.APPROVE_OPTION) {
      nmfc= fc.di.getSelectedFile().getPath();
      // Si plus d'une extension possible => Nom du fichier sans extension
      if (exts.length > 1) {
        for (int i= 0; i < exts.length; i++) {
          if (nmfc.endsWith(CtuluLibString.DOT + exts[i])) {
            nmfc= nmfc.substring(0, nmfc.lastIndexOf(CtuluLibString.DOT + exts[i]));
            break;
          }
        }
      }
      fc.tf.setText(nmfc);
    }
  }
  void fccb_itemStateChanged(ItemEvent _evt) {
    JCheckBox cb= (JCheckBox)_evt.getSource();
    Fichier fc= (Fichier)cb2Fc.get(cb);
    //    String     nom      =(String)     cb2Nom.get(cbFichier);
    //    JTextField tfFichier=(JTextField) nom2Tf.get(nom);
    //    JButton    btFichier=(JButton)    nom2Bt.get(nom);
    fc.tf.setEnabled(cb.isSelected());
    fc.bt.setEnabled(cb.isSelected());
  }
}
