/**
 * @creation     1999-06-25
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         fudaa-devel@lists.sourceforge.net
 */
package org.fudaa.fudaa.refonde;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;

import com.memoire.bu.*;

import org.fudaa.ctulu.CtuluLibString;

import org.fudaa.dodico.corba.calcul.ICalcul;
import org.fudaa.dodico.corba.dunes.ICalculDunes;
import org.fudaa.dodico.corba.dunes.ICalculDunesHelper;
import org.fudaa.dodico.corba.objet.IConnexion;
import org.fudaa.dodico.corba.olb.ICalculOlb;
import org.fudaa.dodico.corba.olb.ICalculOlbHelper;
import org.fudaa.dodico.corba.refonde.ICalculRefonde;
import org.fudaa.dodico.corba.refonde.ICalculRefondeHelper;
import org.fudaa.dodico.corba.refonde.IParametresRefonde;

import org.fudaa.dodico.dunes.DCalculDunes;
import org.fudaa.dodico.objet.UsineLib;
import org.fudaa.dodico.olb.DCalculOlb;
import org.fudaa.dodico.refonde.DCalculRefonde;
import org.fudaa.dodico.refonde.DParametresRefonde;

import org.fudaa.ebli.calque.BArbreCalque;
import org.fudaa.ebli.commun.EbliCalquesPreferencesPanel;
import org.fudaa.ebli.dialog.BFileChooser;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.impression.EbliPageable;
import org.fudaa.ebli.volume.VolumePreferencesPanel;

import org.fudaa.fudaa.commun.dodico.FudaaDodicoTacheConnexion;
import org.fudaa.fudaa.commun.dodico.FudaaImplementation;
/**
 * Classe principale d'implémentation de Refonde. Commune à l'application et à
 * l'applet.
 *
 * @version      $Id: RefondeImplementation.java,v 1.28 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeImplementation
  extends FudaaImplementation
  implements MessageListener {
  public static ICalculRefonde SERVEUR_REFONDE; // Objet Calcul Refonde
  public static IConnexion CONNEXION_REFONDE;
  public static ICalculDunes SERVEUR_DUNES;
  // Objet Calcul Dunes (pour maillage)
  public static IConnexion CONNEXION_DUNES;
  public static ICalculOlb SERVEUR_OLB;
  // Objet Calcul OLB (optimisation de largeur de bande)
  public static IConnexion CONNEXION_OLB;
  //public static IPersonne PERSONNE; // l'utilisateur
  public static RefondeProjet projet; // Projet
  public static JFrame ts; // Console système
  public static BuStatusBar statusBar;
  // Barre de status pour affichage messages
  // Informations sur Refonde
  final static BuInformationsSoftware isRefonde_=
    new BuInformationsSoftware();
  static {
    isRefonde_.name= "Refonde";
    isRefonde_.version= "5.15f-rc4";
    isRefonde_.date= "20-Mai-2009";
    isRefonde_.rights= "1998-2000 GHN & CETMEF\n2000-2005 DeltaCAD & CETMEF";
    isRefonde_.contact= "Pierre.Debaillon@developpement-durable.gouv.fr";
    isRefonde_.license= "GPL2";
    isRefonde_.logo= RefondeResource.REFONDE.getIcon("refonde-logo");
    isRefonde_.banner= RefondeResource.REFONDE.getIcon("refonde-banner");
    isRefonde_.ftp= "ftp://www.utc.fr/";
    isRefonde_.http= "http://www.utc.fr/fudaa/refonde";
    isRefonde_.man= "http://www.utc.fr/fudaa/refonde/utilisation/";
    isRefonde_.update= "http://www.utc.fr/fudaa/refonde/deltas/";
    isRefonde_.authors= new String[] { "Bertrand Marchand" };
    isRefonde_.contributors= null;
    isRefonde_.testers= null;
    BuPrinter.INFO_LOG= isRefonde_;
  }
  private static BuCommonInterface app_;

  BFileChooser diProjet_;
  /** Fichier racine du projet. Ne contient pas l'extension, il sert aussi à
      la création d'un projet depuis les fichiers .10/.12. */
  File fcProjet_=new File(System.getProperty("user.dir")+File.separator+"refonde");
  BFileChooser diFcRef_;
  private RefondeTaskView taches_;
  //  private BVueCalque   vc_;
  private BArbreCalque ac_;
  private RefondeInformations fnInfo_;
  private RefondeDialogFichiersProjet diFcPj;
  RefondeFilleCalques fnCalques_;
  private RefondeFilleMiseEnPage fnMEP_;
  //  private Vector                       fnsCalques_=new Vector(); // Vecteur contenant les fenêtres
  private BuPreferencesFrame preferences_;
  private BuHelpFrame aide_;
  //  private RefondeDialogProprietesMaillage diPrpMaillage;
  //  private RefondeTacheOperation top=null;
  //  private RefondeMenuResultats mnRes_=null;
  // Test 3D
  private boolean MODULE_3D;
  public RefondeImplementation() {}

  public void init() {
    super.init();
    BuColumn rc;
    //BuInformationsSoftware is;
    BuMenuBar mb;
    JMenu mn;
    BuScrollPane sp;
    BuToolBar tb;
    Component cp;
    AbstractButton bt;
    statusBar= getMainPanel().getStatusBar();
    // Test 3D
    MODULE_3D= true;

    try {
      Class.forName("javax.media.j3d.BranchGroup");
    } catch (ClassNotFoundException ex) {
      MODULE_3D= false;
      System.out.println("Pas de 3D!!!");
    }

    try {
      /*is= */getInformationsSoftware();
      app_=getApp();

      // Titre de l'application
      setTitle(null);

      //------------------------------------------------------------------------
      //---  Menus et toolbar  -------------------------------------------------
      //------------------------------------------------------------------------

      getApp().removeAction("DEFAIRE");
      getApp().removeAction("REFAIRE");
      getApp().removeAction("COUPER");
      getApp().removeAction("COPIER");
      getApp().removeAction("COLLER");
      getApp().removeAction("VISIBLE_LEFTCOLUMN");
      getApp().removeAction("ASSISTANT");
      getApp().removeAction("REOUVRIR");
      mb= getApp().getMainMenuBar();

      // Menu fichier
      mn= mb.getMenu("MENU_FICHIER");
      mb.remove(mn);
      mn= buildFileMenu();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 0);

      BuMenuRecentFiles mr= (BuMenuRecentFiles)mb.getMenu("REOUVRIR");
      if (mr != null) {
        mr.setPreferences(RefondePreferences.REFONDE);
        mr.setResource(RefondeResource.REFONDE);
        mr.setEnabled(true);
      }

      // Menu édition
      mn= mb.getMenu("MENU_EDITION");
      mb.remove(mn);
      mn= buildEditionMenu();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 1);

      // Menu géométrie
      mn= buildGeometrieMenu();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 2);

      // Menu modèle de propriétés
      mn= buildModeleProprietes();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 3);

      // Menu maillage
      mn= buildMaillage();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 4);
      // Menu modèle de calcul
      mn= buildModeleCalcul();
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 5);

      // Menu Résultats
      mn= buildResultats();
      //      mnRes_=new RefondeMenuResultats();
      //      mn=mnRes_;
      mn.setIcon(null);
      mn.addActionListener(mb);
      mb.add(mn, 6);
      mb.computeMnemonics();

      // Menu "Fenêtres" : Rajout d'un item "Nouvelle fenêtre"
      mn= mb.getMenu("MENU_FENETRES");
      BuSeparator se= new BuSeparator("Fenêtres");
      BuMenuItem mi=
        new BuMenuItem(
          RefondeResource.REFONDE.getIcon("post"),
          "Nouvelle fenêtre de post");
      //      mi.setText("Nouvelle fenêtre de post");
      mi.setActionCommand("NOUVELLEFENETRE");
      mi.setName("mi" + mi.getActionCommand());
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.addActionListener((BuMenu)mn);
      mi.setEnabled(false);
      mn.add(se, 1);
      mn.add(mi, 2);

      // Modification des tooltips/actions sur les boutons prédéfinis
      tb= getApp().getMainToolBar();
      for (int i= 0; i < tb.getComponentCount(); i++) {
        cp= tb.getComponentAtIndex(i);
        if (cp instanceof AbstractButton) {
          bt= (AbstractButton)cp;
          if (bt.getActionCommand() == "CREER") {
            bt.setToolTipText("Nouveau projet...");
            bt.setActionCommand("CREERPROJET");
          } else if (bt.getActionCommand() == "OUVRIR") {
            bt.setToolTipText("Ouvrir...");
            bt.setActionCommand("OUVRIRPROJET");
          } else if (bt.getActionCommand() == "ENREGISTRER") {
            bt.setToolTipText("Enregistrer");
            bt.setActionCommand("ENREGISTRERPROJET");
          }
        }
      }
      //      tb.addToolButton("Test","Pour tester des solutions","TESTER",true);
      //      tb.addToolButton("Test2","Pour tester des solutions","TESTER2",true);
      getApp().setEnabledForAction("CREERPROJET", true);
      getApp().setEnabledForAction("OUVRIRPROJET", true);
      getApp().setEnabledForAction("QUITTER", true);
      getApp().setEnabledForAction("PREFERENCE", true);
      getApp().setEnabledForAction("EXPORTER", true);
      getApp().setEnabledForAction("IMPORTER", true);
      getApp().setEnabledForAction("CONSOLE", ts != null);
/*      getApp().getMainToolBar().addToolButton(
        "Connecter",
        "CONNECTER",
        FudaaLib.getIcon("connecter"),
        true);*/

      //------------------------------------------------------------------------
      //---  Colonne droite  ---------------------------------------------------
      //------------------------------------------------------------------------

      rc= getApp().getMainPanel().getRightColumn();
      // Vue des taches
      taches_= new RefondeTaskView();
      sp= new BuScrollPane(taches_);
      sp.setPreferredSize(new Dimension(130, 80));
      rc.addToggledComponent(
        BuResource.BU.getString("Tâches"),
        "TACHE",
        sp,
        this);
      main_panel_.setTaskView(taches_);
      // Arbre des calques
      ac_= new BArbreCalque();
      ac_.setShowsRootHandles(false);
      sp= new BuScrollPane(ac_);
      sp.setPreferredSize(new Dimension(130, 370));
      rc.addToggledComponent(
        BuResource.BU.getString("Calques"),
        "CALQUE",
        sp,
        this);
      // Informations
      fnInfo_= new RefondeInformations();
      sp= new BuScrollPane(fnInfo_);
      sp.setPreferredSize(new Dimension(130, 30));
      rc.addToggledComponent(
        "Informations",
        "INFO",
        BuResource.BU.getIcon("message"),
        sp,
        true,
        this);
      //------------------------------------------------------------------------
      //---  Calques  ----------------------------------------------------------
      //------------------------------------------------------------------------
      //      BGroupeCalque gc=new BGroupeCalque();
      //      gc.setName("Spécifiques");
      //
      //      ac_.setCalque(gc);
      //      BVueCalque vc=new BVueCalque();
      //      vc.setCalque(gc);
      //------------------------------------------------------------------------
      //---  Fenetres internes  ------------------------------------------------
      //------------------------------------------------------------------------
      // Fenetre des calques
      fnCalques_= new RefondeFilleCalques(this, ac_);
      //      fnCalques_=new RefondeFilleCalques(this,vc,ac_);
      fnCalques_.getVueCalque().setTaskView(taches_);
      //      addInternalFrame(fnCalques_);
      // Application des préférences
      BuPreferences.BU.applyOn(this);
      RefondePreferences.REFONDE.applyOn(this);
      main_panel_.setLogo(isRefonde_.logo);
    } catch (Throwable t) {
      System.err.println("$$$ " + t);
      t.printStackTrace();
    }
  }
  public void start() {
    super.start();
    //    RefondeDomaineFond.SERVEUR_DUNES=SERVEUR_DUNES;
    try {
      //      fnCalques_.setVisible(false);
      //      fnCalques_.setMaximum(true);
      // Colonne gauche non visible
      BuColumn lc= getApp().getMainPanel().getLeftColumn();
      lc.setVisible(false);
      //cmdConnecter();
    } catch (Throwable t) {
      System.err.println("$$$ " + t);
      t.printStackTrace();
    }
    // Voir RefondeMiseEnPage pour les explications de ce champs.
    RefondeMiseEnPage.imp= this;
  }
  /**
   * Retourne les informations Refonde.
   */
  public static BuInformationsSoftware informationsSoftware() {
    return isRefonde_;
  }
  /**
   * Retourne les informations Refonde.
   */
  public BuInformationsSoftware getInformationsSoftware() {
    return isRefonde_;
  }

  /**
   * Retourne l'interface de l'application.
   */
  public static BuCommonInterface application() {
    return app_;
  }

  /**
   * Retourne l'arbre de calque.
   */
  public BArbreCalque getArbreCalque() {
    return ac_;
  }
  /**
   * Retourne la vue des taches.
   */
  public RefondeTaskView getTaskView() {
    return taches_;
  }
  /**
   * Ajoute le titre de l'application au titre en argument.
   * @param _title En principe, le nom du projet courant
   */
  public void setTitle(String _title) {
    BuInformationsSoftware is= getInformationsSoftware();
    String title= is.name + " " + is.version;
    if (_title != null)
      title += " - " + _title;
    super.setTitle(title);
  }
  /**
   * Surcharge displayURL  pour affichage aide.
   */
  public void displayURL(String _url) {
    if (BuPreferences.BU.getIntegerProperty("browser.type", 1) != 1) {
      if (_url == null) {
        BuInformationsSoftware il= getInformationsSoftware();
        _url= il.http;
      }
      BuBrowserControl.displayURL(_url);
    } else {
      if (aide_ == null) {
        aide_= new BuHelpFrame(this);
      }
      addInternalFrame(aide_);
      aide_.setDocumentUrl(_url);
    }
  }
  /**
   * Confirmation avant sortie.
   */
  public boolean confirmExit() {
    if (projet != null) {
      BuDialogConfirmation bd=
        new BuDialogConfirmation(
          getApp(),
          isRefonde_,
          BuResource.BU.getString(
            "Voulez-vous enregistrer le projet en cours avant de sortir ?"));
      if (bd.activate() == JOptionPane.YES_OPTION) {
        oprEnregistrerProjet();
      }
    }
    return true;
  }
  /**
   * Suppression du desktop des fenetres de post traitement et de mise en page.
   */
  void clearDeskTop() {
    // Suppression des anciennes fenetres de post.
    JInternalFrame[] frames= getAllInternalFrames();
    for (int i= frames.length - 1; i >= 0; i--)
      if (frames[i] instanceof RefondeFillePost)
        removeInternalFrame(frames[i]);
    // Mise en page
    if (fnMEP_ != null)
      removeInternalFrame(fnMEP_);
    getMainPanel().getDesktop().repaint();
  }
  /**
   * Suppression du desktop de toutes les fenetres
   */
  void clearDeskTopTotal() {
	  removeInternalFrames(getAllInternalFrames());
	  getMainPanel().getDesktop().repaint();
  }
  // >>> Interface InternalFrameListener ---------------------------------------
  /**
   * Une fenetre interne est fermée.
   */
  public void internalFrameClosed(InternalFrameEvent _evt) {
    // Quand la fenetre de mise en page est fermée, on vide la mise en page.
    if (_evt.getInternalFrame() == fnMEP_) {
      super.internalFrameClosed(_evt);
      projet.getMiseEnPage().vide();
    }
  }
  // <<< Interface InternalFrameListener ---------------------------------------
  //----------------------------------------------------------------------------
  // Interface MessageListener
  //----------------------------------------------------------------------------
  /**
   * Reception d'un message objet.
   */
  public void messageEnvoye(MessageEvent _evt) {
    if (_evt.getMessage() != null)
      statusBar.setMessage(_evt.getMessage());
    if (_evt.getProgression() != -1)
      statusBar.setProgression(_evt.getProgression());
  }
  //------------------------------------------------------------------------------
  //--- ACTIONS ------------------------------------------------------------------
  //------------------------------------------------------------------------------
  public void actionPerformed(ActionEvent _evt) {
    String action= _evt.getActionCommand();
    if (action.equals("CREERPROJET")) {
      cmdNouveauProjet();
      return;
    }
    else if (action.equals("OUVRIRPROJET")) {
      cmdOuvrirProjet();
      return;
    }
    else if (action.equals("OUVRIRFICHIERS"))
      cmdOuvrirFichiers();
    else if (action.equals("ENREGISTRERPROJET")) {
      cmdEnregistrerProjet();
      return;
    }
    else if (action.equals("ENREGISTRERPROJETSOUS")) {
      cmdEnregistrerProjetSous();
      return;
    }
    else if (action.equals("ENREGISTRERFICHIERS"))
      cmdEnregistrerFichiers();
    else if (action.equals("FERMER")) {
      cmdFermerProjet();
      return;
    }
    else if (action.equals("PROPRIETESPROJET"))
      cmdProprietesProjet();
    else if (action.equals("PROPRIETESBORD"))
      cmdProprietesBord();
    else if (action.equals("PROPRIETESFOND"))
      cmdProprietesFond();
    else if (action.equals("MAILLER")) {
      cmdMailler();
      return;
    }
    else if (action.equals("SUPPRIMERMAILLAGE"))
      cmdSupprimerMaillage();
    else if (action.equals("CALCULERANGLES"))
      cmdCalculerAngles();
    else if (action.equals("NOUVELANGLE"))
      cmdNouvelAngle();
    else if (action.equals("MODIFIERANGLE"))
      cmdModifierAngle();
    else if (action.equals("MODIFIERANGSUP"))
      cmdModifierAngSup();
    else if (action.equals("PARAMETRESCALCUL"))
      cmdParametresCalcul();
    else if (action.equals("CALCULER")) {
      cmdExecuterRefonde();
      return;
    }
    else if (action.equals("REPRENDRECALCUL"))
    {
      cmdReprendreCalcul();
      return;
    }
    else if (action.equals("AFFICHERTRACEEXE"))
      cmdAfficherTraceExe(SERVEUR_REFONDE);
    else if (action.equals("PREFERENCE"))
      cmdPreferences();
    else if (action.equals("NUMNOEUDS"))
      cmdVoirNumNoeuds(((AbstractButton)_evt.getSource()).isSelected());
    else if (action.equals("NUMELEMENTS"))
      cmdVoirNumElements(((AbstractButton)_evt.getSource()).isSelected());
    else if (action.equals("VUE3D"))
      cmdVue3D();
    else if (action.equals("EXPORTERREFONDECALCUL")) {
      cmdExporterRefondeCalcul();
      return;
    }
    else if (action.equals("EXPORTERMAILLAGE")) {
      cmdExporterPostfluxMaillage();
      return;
    }
    else if (action.equals("IMPORTERMAILLAGE")) {
      cmdImporterMaillage();
      return;
    }
    else if (action.equals("CREERDIGUE"))
      cmdCreerDigue();
    else if (action.equals("SUPPRIMERDIGUE"))
      cmdSupprimerDigue();
    else if (action.equals("MODIFIERPROPMAILLAGE"))
      cmdModifierPropMaillage();
    //    else if (action.equals("OPTIMISERLB"          )) cmdOptimiserLB();
    else if (action.equals("CONSOLE"))
      ts.show();
    else if (action.equals("COMBINER"))
      cmdCombiner();
    else if (action.equals("NOUVELLEFENETRE"))
      cmdNouvelleFenetre();
    else if (action.equals("MISEENPAGE"))
      cmdMiseEnPage();
    else if (action.equals("SUPPRIMER"))
      cmdSupprimerObjets();
    else if (action.equals("IMPRIMER"))
      cmdImprimer();
    else if (action.equals("RECENTRERNOEUD"))
      cmdRecentrerNoeud();
    else if (action.equals("RECENTRERELEMENT"))
      cmdRecentrerElement();
    else if (action.startsWith("REOUVRIR")) {
      cmdReouvrir(action);
      return;
    }
    //    else if (action.equals("TESTER"               )) cmdTester();
    //    else if (action.equals("TESTER2"              )) cmdTester2();
    else if (
      action.equals("ASSISTANT")
        || action.equals("TACHE")
        || action.equals("CALQUE")
        || action.equals("INFO")) {
      BuColumn rc= getMainPanel().getRightColumn();
      rc.toggleComponent(action);
      setCheckedForAction(action, rc.isToggleComponentVisible(action));
    } else
      super.actionPerformed(_evt);
    valideActions();
  }
  /**
   * Tester des solutions
   */
  //  private void cmdTester() {
  //    try {
  //      projet.getMiseEnPage().enregistrer(projet,new File("e:\\users\\bmarchan\\fudaa\\projets\\refonde\\essai.mep.xml"));
  //    }
  //    catch (IOException _exc) {
  //      _exc.printStackTrace();
  //    }
  //  }
  /**
   * Tester des solutions
   */
  //  private void cmdTester2() {
  //    try {
  //
  //      clearDeskTop();
  //
  //      RefondeMiseEnPage mep=RefondeMiseEnPage.ouvrir(projet,new File("e:\\users\\bmarchan\\fudaa\\projets\\refonde\\essai.mep.xml"));
  //      projet.setMiseEnPage(mep);
  //
  //      // Mise ne page.
  //      fnMEP_=new RefondeFilleMiseEnPage(this,ac_);
  //      fnMEP_.setMiseEnPage(projet.getMiseEnPage());
  //      fnMEP_.getVueCalque().setTaskView(taches_);
  //      addInternalFrame(fnMEP_);
  //      fnMEP_.restaurer();
  //      fnMEP_.setClosable(true);
  //    }
  //    catch (IOException _exc) {
  //      _exc.printStackTrace();
  //    }
  //  }
  /*
   * Nouveau projet
   */
  private void cmdNouveauProjet() {
    new RefondeTacheOperation(this, "Création d'un projet") {
      public void act() {
        try {
          setEnabledForAction("MODIFIERANGLE", false);
          setEnabledForAction("MODIFIERANGSUP", false);
          setEnabledForAction("CREERDIGUE", false);
          setEnabledForAction("SUPPRIMERDIGUE", false);
          setEnabledForAction("MODIFIERPROPMAILLAGE", false);
          setEnabledForAction("MAILLER", false);
          setEnabledForAction("SUPPRIMERMAILLAGE", false);
          setEnabledForAction("PROPRIETESBORD", false);
          setEnabledForAction("PROPRIETESFOND", false);
          // Demande d'enregistrement du projet courant avant ouverture d'un nouveau.
          boolean save= false;
          if (projet != null) {
            save=
              (new BuDialogConfirmation(RefondeImplementation.this,
                isRefonde_,
                "Le projet courant va être fermé.\n"
                  + "Voulez vous l'enregistrer auparavant ?")
                .activate()
                == JOptionPane.YES_OPTION);
          }
          if (save)
            oprEnregistrerProjet();
          // Nettoyage du desktop
          clearDeskTopTotal();
          projet= null;
          // Selection de la géométrie à ouvrir
          RefondeDialogPanneau di;
          final RefondePnNouveauProjet pn=new RefondePnNouveauProjet();
          pn.setSelectedFile(fcProjet_);
          di= new RefondeDialogPanneau(getFrame(), pn, "Nouveau projet") {
            public boolean actionApply() {
              // Ouverture de la géométrie
              fcProjet_=pn.getSelectedFile();
              oprNouveauProjet(new File(fcProjet_.getPath()+".12"),pn.getTypeModele());
              return true;
            }
          };
          di.show();
//          BFileChooser fc=new BFileChooser();
//          if (diProjet_ == null) {
//            diProjet_= new BFileChooser();
//            diProjet_.setFileHidingEnabled(true);
//            diProjet_.setCurrentDirectory(
//              new File(System.getProperty("user.dir")));
//            diProjet_.setMultiSelectionEnabled(false);
//          }
//          BuFileFilter[] filtresVag_=
//            { new BuFileFilter(new String[] { "10", "12" }, "Géométrie Vag")
//          };
//          fc.setDialogTitle("Nouveau projet");
//          fc.resetChoosableFileFilters();
//          fc.addChoosableFileFilter(filtresVag_[0]);
//          fc.setFileFilter(filtresVag_[0]);
//          int r= diProjet_.showOpenDialog((BuApplication)getApp());
          // Ouverture de la géométrie
//          if (r == BFileChooser.APPROVE_OPTION)
//            oprNouveauProjet();
        }
        finally {
          valideActions();
        }
      }
    }
    .start();
  }
  void oprNouveauProjet(File _fichier, int _tpModele) {
//    File fichier= diProjet_.getSelectedFile();
    //    String path =fichier.getPath();
    try {
      statusBar.setMessage("Création d'un projet...");
      statusBar.setProgression(0);
      // Désactivation des actions
      //      setEnabledForAction("OUVRIRFICHIERS"       ,false);
      //      setEnabledForAction("ENREGISTRERPROJET"    ,false);
      //      setEnabledForAction("ENREGISTRERPROJETSOUS",false);
      //      setEnabledForAction("ENREGISTRERFICHIERS"  ,false);
      //      setEnabledForAction("PROPRIETESPROJET"     ,false);
      //      setEnabledForAction("CALCULERANGLES"       ,false);
      //      setEnabledForAction("NOUVELANGLE"          ,false);
      //      setEnabledForAction("PARAMETRESCALCUL"     ,false);
      //      setEnabledForAction("CALCULER"             ,false);
      //      setEnabledForAction("EXPORTERREFONDECALCUL",false);
      //      setEnabledForAction("EXPORTERMAILLAGE"     ,false);
      //      setEnabledForAction("OPTIMISERLB"          ,false);
      //      setEnabledForAction("NOUVELLEFENETRE"      ,false);
      //      setEnabledForAction("MISEENPAGE"           ,false);
      //      setEnabledForAction("COMBINER"             ,false);
      //      setEnabledForAction("VUE3D"                ,false);
      //      setEnabledForAction("RECENTRERNOEUD"       ,false);
      //      setEnabledForAction("RECENTRERELEMENT"     ,false);
      //      setEnabledForAction("FERMER"               ,false);
      clearDeskTop();
      projet= new RefondeProjet();
      projet.addMessageListener(this);
      projet.nouveau(_fichier,_tpModele);
      //      projet=RefondeProjet.nouveau(fichier);
      fnCalques_.initialise(projet);
      addInternalFrame(fnCalques_);
      //fnCalques_.restaurer();
      SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		fnCalques_.restaurer();
        	}
        });
      statusBar.setProgression(100);
      setTitle(projet.getFichierProjet().getPath());
      //      mnRes_.rafraichir(projet);
      //      fnCalques_.setVisible(true);
      // Activabilité des actions
      //      setEnabledForAction("OUVRIRFICHIERS"       ,true);
      //      setEnabledForAction("ENREGISTRERPROJET"    ,true);
      //      setEnabledForAction("ENREGISTRERPROJETSOUS",true);
      //      setEnabledForAction("ENREGISTRERFICHIERS"  ,true);
      //      setEnabledForAction("PROPRIETESPROJET"     ,true);
      //      setEnabledForAction("CALCULERANGLES"       ,true);
      //      setEnabledForAction("NOUVELANGLE"          ,true);
      //      setEnabledForAction("PARAMETRESCALCUL"     ,true);
      //      setEnabledForAction("CALCULER"             ,SERVEUR_REFONDE!=null);
      //      setEnabledForAction("EXPORTERREFONDECALCUL",true);
      //      setEnabledForAction("EXPORTERMAILLAGE"     ,true);
      //      setEnabledForAction("OPTIMISERLB"          ,true);
      //      setEnabledForAction("MISEENPAGE"           ,true);
      //      setEnabledForAction("NOUVELLEFENETRE"      ,projet.estEntierementMaille() && RefondeMaillage.estOptimise(projet.getSuperMaillage()));
      //      setEnabledForAction("RECENTRERNOEUD"       ,projet.getSuperMaillage().noeuds().length!=0);
      //      setEnabledForAction("RECENTRERELEMENT"     ,projet.getSuperMaillage().elements().length!=0);
      //      setEnabledForAction("FERMER"               ,true);
    } catch (IOException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
      projet= null;
      removeInternalFrame(fnCalques_);
    }
    //    catch (Exception _exc) {
    //      _exc.printStackTrace();
    //      new BuDialogError(this, getInformationsSoftware(),_exc.getMessage())
    //                        .activate();
    //      projet=null;
    //      removeInternalFrame(fnCalques_);
    //      fnCalques_.setVisible(false);
    //    }
    finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
      fnInfo_.update(projet);
      //      valideActions();
    }
  }

  /**
   * Réouverture d'un fichier projet.
   * @param _action Le nom du fichier sous la forme "REOUVRIR(&lt;fichier&gt;"
   */
  private void cmdReouvrir(String _action) {
    int i= _action.indexOf('(');
    if (i < 0) return;

    final File fichier=new File(_action.substring(i + 1, _action.length() - 1));
    _action=_action.substring(0, i);

    new RefondeTacheOperation(this, "Ouverture d'un projet", true) {
      public void act() {
        try {
          setEnabledForAction("MODIFIERANGLE", false);
          setEnabledForAction("MODIFIERANGSUP", false);
          setEnabledForAction("CREERDIGUE", false);
          setEnabledForAction("SUPPRIMERDIGUE", false);
          setEnabledForAction("MODIFIERPROPMAILLAGE", false);
          setEnabledForAction("MAILLER", false);
          setEnabledForAction("SUPPRIMERMAILLAGE", false);
          setEnabledForAction("PROPRIETESBORD", false);
          setEnabledForAction("PROPRIETESFOND", false);

          // Demande d'enregistrement du projet courant avant ouverture d'un nouveau.
          boolean save= false;
          if (projet != null) {
            save=
              (new BuDialogConfirmation(RefondeImplementation.this,
                isRefonde_,
                "Le projet courant va être fermé.\n"
                  + "Voulez vous l'enregistrer auparavant ?")
                .activate()
                == JOptionPane.YES_OPTION);
          }
          if (save)
            oprEnregistrerProjet();

          // Nettoyage du desktop
          clearDeskTopTotal();
          projet= null;

          oprOuvrirProjet(fichier);
        } finally {
          valideActions();
        }
      }
    }
    .start();
  }

  /**
   * Ouverture du projet.
   */
  private void cmdOuvrirProjet() {
    new RefondeTacheOperation(this, "Ouverture d'un projet", true) {
      public void act() {
        try {
          setEnabledForAction("MODIFIERANGLE", false);
          setEnabledForAction("MODIFIERANGSUP", false);
          setEnabledForAction("CREERDIGUE", false);
          setEnabledForAction("SUPPRIMERDIGUE", false);
          setEnabledForAction("MODIFIERPROPMAILLAGE", false);
          setEnabledForAction("MAILLER", false);
          setEnabledForAction("SUPPRIMERMAILLAGE", false);
          setEnabledForAction("PROPRIETESBORD", false);
          setEnabledForAction("PROPRIETESFOND", false);
          // Demande d'enregistrement du projet courant avant ouverture d'un nouveau.
          boolean save= false;
          if (projet != null) {
            save=
              (new BuDialogConfirmation(RefondeImplementation.this,
                isRefonde_,
                "Le projet courant va être fermé.\n"
                  + "Voulez vous l'enregistrer auparavant ?")
                .activate()
                == JOptionPane.YES_OPTION);
          }
          if (save)
            oprEnregistrerProjet();
          // Nettoyage du desktop
          clearDeskTopTotal();
          projet= null;
          // Selection du projet à ouvrir
          if (diProjet_ == null) {
            diProjet_= new BFileChooser();
            diProjet_.setFileHidingEnabled(true);
            diProjet_.setCurrentDirectory(
              new File(System.getProperty("user.dir")));
            diProjet_.setMultiSelectionEnabled(false);
          }
          BuFileFilter[] filtresProjet_=
            { new BuFileFilter(new String[] { "prf" }, "Projets")
          };
          diProjet_.setDialogTitle("Ouverture d'un projet");
          diProjet_.resetChoosableFileFilters();
          diProjet_.addChoosableFileFilter(filtresProjet_[0]);
          diProjet_.setFileFilter(filtresProjet_[0]);
          int r= diProjet_.showOpenDialog((BuApplication)getApp());
          // Ouverture du projet
          if (r == JFileChooser.APPROVE_OPTION) {
            File fichier = diProjet_.getSelectedFile();
            oprOuvrirProjet(fichier);
          }

        }        catch(Exception e){
          e.printStackTrace();
        }
        finally {
          valideActions();
        }
      }
    }
    .start();
  }
  void oprOuvrirProjet(File _fichier) {
    //    String path =fichier.getPath();
    try {
      statusBar.setMessage("Ouverture du projet depuis " + _fichier + "...");
      statusBar.setProgression(0);
      
      projet= new RefondeProjet();
      projet.addMessageListener(this);
      projet.ouvrir(_fichier);
      //      projet=RefondeProjet.ouvrir(fichier);
      fnCalques_.initialise(projet);
      addInternalFrame(fnCalques_);
      //fnCalques_.restaurer();
      SwingUtilities.invokeLater(new Runnable() {
      	public void run() {
      		fnCalques_.restaurer();
      	}
      });
      statusBar.setProgression(100);
      setTitle(projet.getFichierProjet().getPath());
      updateRecentFiles(_fichier);

      //      mnRes_.rafraichir(projet);
      //      fnCalques_.setVisible(true);
      // Mise ne page.
      if (!projet.getMiseEnPage().isVide()) {
        fnMEP_= new RefondeFilleMiseEnPage(this, ac_);
        fnMEP_.setMiseEnPage(projet.getMiseEnPage());
        fnMEP_.getVueCalque().setTaskView(taches_);
        addInternalFrame(fnMEP_);
        //fnMEP_.restaurer();
        fnMEP_.setClosable(true);
        
        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		fnMEP_.restaurer();
        	}
        });
      }
      
    } catch (RefondeTacheInterruptionException _exc) {
      new BuDialogError(this, isRefonde_, _exc.getMessage()).activate();
      projet= null;
      removeInternalFrame(fnCalques_);
    } catch (IOException _exc) {
      //      _exc.printStackTrace();
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
      projet= null;
      removeInternalFrame(fnCalques_);
      //      fnCalques_.setVisible(false);
    }catch(Exception e){
      e.printStackTrace();
    }
    finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
      fnInfo_.update(projet);
      //      valideActions();
    }
  }
  /*
   * Ouverture des fichiers du projet
   */
  private void cmdOuvrirFichiers() {
    diFcPj= new RefondeDialogFichiersProjet();
    diFcPj.ajoute(
      "Maillage",
      "MAI",
      projet.getFichierMaillage(),
      new String[] { "mai" },
      true);
    diFcPj.ajoute(
      "Modèle de propriétés",
      "PRP",
      projet.getFichierModele(),
      new String[] { "prp" },
      true);
    diFcPj.ajoute(
      "Modèle de calcul",
      "CAL",
      projet.getFichierCalcul(),
      new String[] { "cal" },
      true);
    diFcPj.setTitle("Ouverture des fichiers projet");
    diFcPj.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        diFcPjOpen_actionPerformed(_evt);
      }
    });
    diFcPj.pack();
    diFcPj.show();
  }
  //----------------------------------------------------------------------------
  // Récupération des fichiers à ouvrir
  //----------------------------------------------------------------------------
  void diFcPjOpen_actionPerformed(ActionEvent _evt) {
    if (_evt.getSource() != diFcPj.btOk_
      && _evt.getSource() != diFcPj.btApply_)
      return;
    try {
      // Récupération du maillage
      if (diFcPj.getEtat("MAI")) {
        projet.setFichierMaillage(diFcPj.getFile("MAI"));
        projet.setMaillage(
          RefondeMaillage.ouvrir(projet, projet.getFichierMaillage()));
      }
      // Récupération du modèle
      if (diFcPj.getEtat("PRP")) {
        projet.setFichierModele(diFcPj.getFile("PRP"));
        projet.setModeleProprietes(
          RefondeModeleProprietes.ouvrir(projet, projet.getFichierModele()));
      }
      // Récupération du modèle de calcul
      if (diFcPj.getEtat("CAL")) {
        projet.setFichierCalcul(diFcPj.getFile("CAL"));
        projet.setModeleCalcul(
          RefondeModeleCalcul.ouvrir(projet, projet.getFichierCalcul()));
      }
      fnCalques_.initialise(projet);
    } catch (RefondeTacheInterruptionException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } catch (IOException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } finally {
      fnInfo_.update(projet);
      //      setEnabledForAction("NOUVELLEFENETRE",projet.estEntierementMaille() && RefondeMaillage.estOptimise(projet.getSuperMaillage()));
    }
  }
  /**
   * Enregistrement du projet et de ses fichiers.
   */
  private void cmdEnregistrerProjet() {
    new RefondeTacheOperation(this, "Enregistrement du projet") {
      public void act() {
        try {
          oprEnregistrerProjet();
        } finally {
          valideActions();
        }
      }
    }
    .start();
  }
  /**
   * Enregistrement sous un nom donné du projet et de ses fichiers.
   */
  private void cmdEnregistrerProjetSous() {
    if (diProjet_ == null) {
      diProjet_= new BFileChooser();
      diProjet_.setFileHidingEnabled(true);
      diProjet_.setCurrentDirectory(new File(System.getProperty("user.dir")));
      diProjet_.setMultiSelectionEnabled(false);
    }
    BuFileFilter[] filtresProjet_=
      { new BuFileFilter(new String[] { "prf" }, "Projets")
    };
    //    diProjet_.updateUI();
    diProjet_.setDialogTitle("Enregistrement du projet");
    diProjet_.resetChoosableFileFilters();
    diProjet_.addChoosableFileFilter(filtresProjet_[0]);
    diProjet_.setFileFilter(filtresProjet_[0]);
    int r= diProjet_.showSaveDialog((BuApplication)getApp());
    if (r == JFileChooser.APPROVE_OPTION) {
      File fichier= diProjet_.getSelectedFile();
      projet.setFichierProjet(fichier);
      new RefondeTacheOperation(this, "Enregistrement du projet") {
        public void act() {
          try {
            oprEnregistrerProjet();
          } finally {
            valideActions();
          }
        }
      }
      .start();
    }
  }
  /**
   * Tâche d'enregistrement d'un projet.
   */
  void oprEnregistrerProjet() {
    File fichier= projet.getFichierProjet();
    try {
      statusBar.setMessage("Enregistrement du projet sur " + fichier + "...");
      statusBar.setProgression(0);
      projet.enregistrer();
      statusBar.setProgression(100);
      setTitle(fichier.getPath());

      updateRecentFiles(fichier);

    } catch (IOException _exc) {
      //      _exc.printStackTrace();
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
      projet= null;
    } finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
    }
  }
  /**
   * Enregistrement des fichiers du projet.
   */
  private void cmdEnregistrerFichiers() {
    diFcPj= new RefondeDialogFichiersProjet();
    diFcPj.ajoute(
      "Maillage",
      "MAI",
      projet.getFichierMaillage(),
      new String[] { "mai" },
      projet.getMaillage().modifie);
    diFcPj.ajoute(
      "Modèle de propriétés",
      "PRP",
      projet.getFichierModele(),
      new String[] { "prp" },
      projet.getModeleProprietes().modifie);
    diFcPj.ajoute(
      "Modèle de calcul",
      "CAL",
      projet.getFichierCalcul(),
      new String[] { "cal" },
      projet.getModeleCalcul().estModifie());
    diFcPj.setTitle("Enregistrement des fichiers projet");
    diFcPj.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        diFcPjSave_actionPerformed(_evt);
      }
    });
    diFcPj.pack();
    diFcPj.show();
  }
  //----------------------------------------------------------------------------
  // Sauvegarde des fichiers
  //----------------------------------------------------------------------------
  void diFcPjSave_actionPerformed(ActionEvent _evt) {
    if (_evt.getSource() != diFcPj.btOk_
      && _evt.getSource() != diFcPj.btApply_)
      return;
    try {
      // Sauvegarde du maillage
      if (diFcPj.getEtat("MAI")) {
        projet.setFichierMaillage(diFcPj.getFile("MAI"));
        projet.getMaillage().enregistrer(projet, projet.getFichierMaillage());
      }
      // Sauvegarde du modèle
      if (diFcPj.getEtat("PRP")) {
        projet.setFichierModele(diFcPj.getFile("PRP"));
        projet.getModeleProprietes().enregistrer(
          projet,
          projet.getFichierModele());
      }
      // Sauvegarde du modèle de calcul
      if (diFcPj.getEtat("CAL")) {
        projet.setFichierCalcul(diFcPj.getFile("CAL"));
        projet.getModeleCalcul().enregistrer(projet, projet.getFichierCalcul());
      }
    } catch (IOException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    }
  }
  /**
   * Fermeture du projet. Actuellement, la fermeture ne verifie pas si le projet
   * a été modifié, et propose systématiquement de le sauver auparavant.
   */
  private void cmdFermerProjet() {
    final boolean save;
    save=
      (new BuDialogConfirmation(RefondeImplementation.this,
        isRefonde_,
        "Le projet courant va être fermé.\n"
          + "Voulez vous l'enregistrer auparavant ?")
        .activate()
        == JOptionPane.YES_OPTION);
    new RefondeTacheOperation(this, "Fermeture du projet") {
      public void act() {
        try {
          if (save)
            oprEnregistrerProjet();
          clearDeskTopTotal();
          projet= null;
          fnInfo_.update(projet);
          //          setEnabledForAction("OUVRIRFICHIERS"       ,false);
          //          setEnabledForAction("ENREGISTRERPROJET"    ,false);
          //          setEnabledForAction("ENREGISTRERPROJETSOUS",false);
          //          setEnabledForAction("ENREGISTRERFICHIERS"  ,false);
          //          setEnabledForAction("PROPRIETESPROJET"     ,false);
          //          setEnabledForAction("MAILLER"              ,false);
          //          setEnabledForAction("SUPPRIMERMAILLAGE"    ,false);
          //          setEnabledForAction("CALCULERANGLES"       ,false);
          //          setEnabledForAction("NOUVELANGLE"          ,false);
          //          setEnabledForAction("MODIFIERANGLE"        ,false);
          //          setEnabledForAction("PARAMETRESCALCUL"     ,false);
          //          setEnabledForAction("CALCULER"             ,false);
          //          setEnabledForAction("EXPORTERREFONDECALCUL",false);
          //          setEnabledForAction("EXPORTERMAILLAGE"     ,false);
          //          setEnabledForAction("CREERDIGUE"           ,false);
          //          setEnabledForAction("SUPPRIMERDIGUE"       ,false);
          //          setEnabledForAction("MODIFIERPROPMAILLAGE" ,false);
          //          setEnabledForAction("OPTIMISERLB"          ,false);
          //          setEnabledForAction("NOUVELLEFENETRE"      ,false);
          //          setEnabledForAction("MISEENPAGE"           ,false);
          //          setEnabledForAction("COMBINER"             ,false);
          //          setEnabledForAction("VUE3D"                ,false);
          //          setEnabledForAction("RECENTRERNOEUD"       ,false);
          //          setEnabledForAction("RECENTRERELEMENT"     ,false);
          //          setEnabledForAction("FERMER"               ,false);
          setEnabledForAction("MODIFIERANGLE", false);
          setEnabledForAction("MODIFIERANGSUP", false);
          setEnabledForAction("CREERDIGUE", false);
          setEnabledForAction("SUPPRIMERDIGUE", false);
          setEnabledForAction("MODIFIERPROPMAILLAGE", false);
          setEnabledForAction("MAILLER", false);
          setEnabledForAction("SUPPRIMERMAILLAGE", false);
          setEnabledForAction("PROPRIETESBORD", false);
          setEnabledForAction("PROPRIETESFOND", false);
        } finally {
          valideActions();
        }
      }
    }
    .start();
  }

  /*
   * Propriétés du projet
   */
  private void cmdProprietesProjet() {
    int tpModele=projet.getModeleCalcul().typeModele();

    RefondeDialogProprietesProjet di= new RefondeDialogProprietesProjet();
    di.setTitle("Propriétés du projet");
    di.setProjet(projet);
    di.show();

    // Changement de modèle de données => Suppression des résultats.
    if (tpModele!=projet.getModeleCalcul().typeModele()) {
      clearDeskTop();
      fnCalques_.initialise(projet);
      fnCalques_.restaurer();
    }
  }

  /*
   * Propriétés élémentaires du bord sélectionné
   */
  private void cmdProprietesBord() {
    Object[] selects= fnCalques_.getSelectedObjects();
    Vector vsel= new Vector();
    // Récupérationdes seules polylignes frontieres
    for (int i= 0; i < selects.length; i++) {
      if (((RefondePolyligne)selects[i]).getGroupeProprietes() != null) {
        vsel.add(selects[i]);
      }
    }
    if (vsel.size() == 0) {
      new BuDialogError(
        this,
        isRefonde_,
        "Aucune polyligne sélectionnée n'est " + "sur une frontière")
        .activate();
      return;
    }
    int[] tpGrps;
    // Modèle de houle
    if (projet.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_HOULE) {
      tpGrps=new int[] {
             RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE,
             RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE,
             RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT};
    }
    // Modèle de seiche
    else {
      tpGrps=new int[] {
             RefondeGroupeProprietes.SEICHE_BORD_OUVERT,
             RefondeGroupeProprietes.SEICHE_BORD_FERME};
    }
    final RefondeDialogGroupeProprietes di= new RefondeDialogGroupeProprietes();
    di.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        if (_evt.getSource() == di.btCancel_)
          return;
        fnCalques_.getVueCalque().repaint();
      }
    });
    di.setGroupesProprietes(tpGrps);
    di.initialise(vsel.toArray());
    di.pack();
    di.show();
  }

  /*
   * Propriétés élémentaires du fond sélectionné
   */
  private void cmdProprietesFond() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    final int tpGp;
    final RefondeDomaine doma;
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // La polyligne ne fait pas partie d'un domaine
    doma= getDomaine(pl);
    if (doma == null) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "La polyligne ne fait pas partie d'un domaine")
        .activate();
      return;
    }
    if (!(doma instanceof RefondeDomaineDigue)) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Pas de propriétés élémentaires sur ce domaine")
        .activate();
      return;
    }
    int[] tpGrps=
      new int[] {
        RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE,
        RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE };
    tpGp= doma.getGroupeProprietes().getType();
    final RefondeDialogGroupeProprietes di= new RefondeDialogGroupeProprietes();
    di.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        if (_evt.getSource() == di.btCancel_)
          return;
        if (tpGp != doma.getGroupeProprietes().getType()) {
          if (tpGp == RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
            projet.getModeleCalcul().supprimerAngles((RefondeDomaineDigue)doma);
          else
            projet.getModeleCalcul().creerAngles((RefondeDomaineDigue)doma);
          //          tpGp=doma.getGroupeProprietes().getType();
        }
        fnCalques_.cqAngles.initialise(projet);
        fnCalques_.cqAnglesTrans.initialise(projet);
        fnCalques_.getVueCalque().repaint();
      }
    });
    di.setGroupesProprietes(tpGrps);
    di.initialise(new RefondeDomaine[] { doma });
    di.pack();
    di.show();
  }
  /*
   * Mailler le domaine
   */
  private void cmdMailler() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    final RefondeDomaine doma;
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // La polyligne ne fait pas partie d'un domaine
    doma= getDomaine(pl);
    if (doma == null) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "La polyligne ne fait pas partie d'un domaine")
        .activate();
      return;
    }
    if (doma.getMaillage() != null) {
      if (new BuDialogConfirmation(this,
        getInformationsSoftware(),
        "Un maillage existe déjà sur ce domaine. Il sera détruit")
        .activate()
        == JOptionPane.NO_OPTION)
        return;
    }
    new RefondeTacheOperation(this, "Maillage", true) {
      public void act() {
        try {
          oprMailler(doma);
        } finally {
          valideActions();
        }
      }
      protected void arretDemande() {
        SERVEUR_DUNES.setArretDemande();
        SERVEUR_OLB.setArretDemande();
      }
    }
    .start();
  }
  /**
   * Tâche de maillage : essaie de limiter les transferts client<->serveur
   * @param _doma le domaine a mailler
   */void oprMailler(RefondeDomaine _doma) {
    try {
      statusBar.setMessage("Maillage du domaine...");
      statusBar.setProgression(0);
      // Interruption demandée => Sortie.
      if (RefondeTacheOperation.isTacheCouranteArretDemande())
        return;
      _doma.mailler();
      if (_doma.getMaillage() == null && !SERVEUR_DUNES.estOK()) {
        String msg= "Le maillage s'est terminé avec des erreurs";
        new BuDialogMessage(this, getInformationsSoftware(), msg).activate();
        cmdAfficherTraceExe(SERVEUR_DUNES);
        return;
      }
      // Optimisation du maillage
      if (projet.estEntierementMaille()) {
        statusBar.setMessage("Optimisation du maillage...");
        statusBar.setProgression(0);
        // Interruption demandée => Sortie.
        if (RefondeTacheOperation.isTacheCouranteArretDemande())
          return;
        RefondeOLB.optimiser(projet.getSuperMaillage());
        if (!SERVEUR_OLB.estOK()) {
          String msg=
            "Le calcul de largeur de bande s'est terminé avec des erreurs";
          new BuDialogMessage(this, getInformationsSoftware(), msg).activate();
          cmdAfficherTraceExe(SERVEUR_OLB);
          return;
        }
      }
    } catch (IllegalArgumentException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
      return;
    } finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
      projet.setResultats(null); // Suppression des résultats
      fnCalques_.cqMaillage.initialise(projet);
      fnCalques_.getVueCalque().repaint();
      fnInfo_.update(projet);
      clearDeskTop();
      SERVEUR_DUNES.clearArretDemande();
      SERVEUR_OLB.clearArretDemande();
      //      setEnabledForAction("VUE3D",false);
      //      setEnabledForAction("COMBINER"             ,false);
      //      setEnabledForAction("VUE3D"                ,false);
      //      setEnabledForAction("NOUVELLEFENETRE"      ,projet.estEntierementMaille() && RefondeMaillage.estOptimise(projet.getSuperMaillage()));
      //      setEnabledForAction("RECENTRERNOEUD"       ,projet.getSuperMaillage().noeuds().length!=0);
      //      setEnabledForAction("RECENTRERELEMENT"     ,projet.getSuperMaillage().elements().length!=0);
    }
    //
    //    if (_doma.getMaillage()==null && !SERVEUR_DUNES.estOK()) {
    //      String msg="Le maillage s'est terminé avec des erreurs";
    //      new BuDialogMessage(this,getInformationsSoftware(),msg).activate();
    //
    //      cmdAfficherTraceExe(SERVEUR_DUNES);
    //    }
  }
  /*
   * Suppression du maillage
   */
  private void cmdSupprimerMaillage() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    final RefondeDomaine doma;
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // La polyligne ne fait pas partie d'un domaine
    doma= getDomaine(pl);
    if (doma == null) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "La polyligne ne fait pas partie d'un domaine")
        .activate();
      return;
    }
    if (doma.getMaillage() != null) {
      if (new BuDialogConfirmation(this,
        getInformationsSoftware(),
        "Voulez vous réellement supprimer le maillage ?")
        .activate()
        == JOptionPane.NO_OPTION)
        return;
      doma.setMaillage(null);
      projet.setResultats(null); // Suppression des résultats
      fnCalques_.cqMaillage.initialise(projet);
      fnCalques_.getVueCalque().repaint();
      fnInfo_.update(projet);
      clearDeskTop();
      //      setEnabledForAction("VUE3D",false);
      //      setEnabledForAction("COMBINER"             ,false);
      //      setEnabledForAction("VUE3D"                ,false);
      //      setEnabledForAction("NOUVELLEFENETRE"      ,projet.estEntierementMaille() && RefondeMaillage.estOptimise(projet.getSuperMaillage()));
      //      setEnabledForAction("RECENTRERNOEUD"       ,projet.getSuperMaillage().noeuds().length!=0);
      //      setEnabledForAction("RECENTRERELEMENT"     ,projet.getSuperMaillage().elements().length!=0);
    }
  }
  /*
   * Calcul automatique des angles d'incidence
   */
  private void cmdCalculerAngles() {
    try {
      projet.getModeleCalcul().calculAngles(projet);
    } catch (IllegalArgumentException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } finally {
      fnCalques_.cqAngles.initialise(projet);
      fnCalques_.getVueCalque().repaint();
    }
  }
  /*
   * Création d'un nouvel angle
   */
  private void cmdNouvelAngle() {
    fnCalques_.setLockEvents(fnCalques_.cqSaisieAngle);
    fnCalques_.cqSaisieAngle.initialise(projet);
  }
  /*
   * Modification d'un angle
   */
  private void cmdModifierAngle() {
    final RefondeDialogAngle di= new RefondeDialogAngle();
    di.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        if (_evt.getSource() == di.btCancel_)
          return;
        fnCalques_.cqAngles.initialise(projet);
        fnCalques_.cqAnglesTrans.initialise(projet);
        fnCalques_.getVueCalque().repaint();
      }
    });
    di.initialise(projet, fnCalques_.getSelectedObjects());
    di.show();
  }
  /*
   * Modification d'un angle supplémentaire associé.
   */
  private void cmdModifierAngSup() {
    RefondeDialogAngleSup di= new RefondeDialogAngleSup();
    //    di.addActionListener(new ActionListener() {
    //      public void actionPerformed(ActionEvent _evt) {
    //        if (_evt.getSource()==di.CANCEL_BUTTON) return;
    //        fnCalques_.clAngles.initialise(projet);
    //        fnCalques_.clAnglesTrans.initialise(projet);
    //        fnCalques_.getVueCalque().repaint();
    //      }
    //    });
    di.initialise(projet, fnCalques_.getSelectedObjects());
    di.show();
  }
  /*
   * Parametres du calcul
   */
  private void cmdParametresCalcul() {
    // Initialisation de la fenetre avec les parametres courants
    RefondeDialogParametresCalcul di=
      new RefondeDialogParametresCalcul(fnCalques_);
    di.initialise(projet);
    di.show();
  }
  /*
   * Exécution de refonde
   */
  private void cmdExecuterRefonde() {
    RefondeModeleCalcul mdlCal=projet.getModeleCalcul();
    // Conservation des fichiers de reprise de calcul ?
    if (mdlCal.typeModele()==RefondeModeleCalcul.MODELE_HOULE &&
        mdlCal.typeHoule()==RefondeModeleCalcul.HOULE_ALEA) {
      int rep=
        new BuDialogConfirmation(
          this,
          isRefonde_,
          "Voulez vous que les fichiers de reprise en houle aléatoire\n"
            + "soient conservés sur le serveur de calcul ?")
          .activate();
      SERVEUR_REFONDE.conserveFichiersReprise(rep == JOptionPane.YES_OPTION);
    }
    new RefondeTacheOperation(this, "Exécution de Refonde", true) {
      public void act() {
        try {
          oprExecuterRefonde(false);
        } finally {
          valideActions();
        }
      }
      protected void arretDemande() {
        SERVEUR_OLB.setArretDemande();
        SERVEUR_REFONDE.setArretDemande();
      }
    }
    .start();
  }
  /**
   * Reprise de calcul en houle aléatoire.
   */
  private void cmdReprendreCalcul() {
    String rac= projet.getRacineFichiersCalcul();
    int nbPeriode= projet.getModeleCalcul().nbPeriodesHoule();
    int nbAngles= projet.getModeleCalcul().nbAnglesHoule();
    // Controle de l'existence des fichiers permettant la reprise de calcul
    if (!SERVEUR_REFONDE
      .accepteRepriseHouleAleatoire(
        CONNEXION_REFONDE,
        rac,
        nbPeriode,
        nbAngles)) {
      new BuDialogError(
        this,
        isRefonde_,
        "La reprise de calcul en houle aléatoire n'est pas possible :\n"
          + "certains fichiers ne sont pas présents sur le serveur de calcul")
        .activate();
      return;
    }
    new RefondeTacheOperation(this, "Reprise de calcul en houle aléatoire") {
      public void act() {
        try {
          oprExecuterRefonde(true);
        } finally {
          valideActions();
        }
      }
    }
    .start();
  }
  /**
   * Tâche d'exécution de Refonde.
   *
   * @param _reprise Force le calcul en mode reprise houle aléatoire.
   */
  void oprExecuterRefonde(boolean _reprise) {
    if (!isConnected()) {
      new BuDialogError(
        getApp(),
        isRefonde_,
        "vous n'etes pas connecté à un serveur!")
        .activate();
      return;
    }
    try {
      //      setEnabledForAction("CALCULER"        ,false);
      //      setEnabledForAction("AFFICHERTRACEEXE",false);
      //      setEnabledForAction("COMBINER"        ,false);
      //      setEnabledForAction("VUE3D"           ,false);
      // Optimisation du maillage si necessaire
      if (projet.estEntierementMaille()
        && !RefondeMaillage.estOptimise(projet.getSuperMaillage())) {
        statusBar.setMessage("Optimisation préalable du maillage...");
        statusBar.setProgression(0);
        // Interruption demandée => Sortie.
        if (RefondeTacheOperation.isTacheCouranteArretDemande())
          return;
        RefondeOLB.optimiser(projet.getSuperMaillage());
        if (!SERVEUR_OLB.estOK()) {
          String msg=
            "Le calcul de largeur de bande s'est terminé avec des erreurs";
          new BuDialogMessage(this, getInformationsSoftware(), msg).activate();
          cmdAfficherTraceExe(SERVEUR_OLB);
          return;
        }
      }
      statusBar.setMessage("Exécution de Refonde en tâche de fond...");
      statusBar.setProgression(10);
      RefondeRefonde.calcul(SERVEUR_REFONDE, CONNEXION_REFONDE, _reprise, projet);
      String msg;
      if (SERVEUR_REFONDE.estOK())
        msg= "Le calcul de Refonde s'est terminé avec succes";
      else
        msg= "Le calcul de Refonde s'est terminé avec des erreurs";
      new BuDialogMessage(this, getInformationsSoftware(), msg).activate();
      cmdAfficherTraceExe(SERVEUR_REFONDE);
      clearDeskTop();
    } catch (IllegalArgumentException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
      return;
    } finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
      SERVEUR_REFONDE.clearArretDemande();
      SERVEUR_OLB.clearArretDemande();
      //      setEnabledForAction("CALCULER",true);
      //      setEnabledForAction("AFFICHERTRACEEXE",true);
      //      setEnabledForAction("COMBINER"        ,projet.hasResultats());
      //      setEnabledForAction("VUE3D"           ,MODULE_3D && projet.hasResultats());
    }
  }
  /**
   * Afficher la trace d'éxécution d'un exe.
   */
  private void cmdAfficherTraceExe(ICalcul _serveur) {
    String trace;
    if (_serveur instanceof ICalculRefonde)
      trace= ((ICalculRefonde)_serveur).traceExecution();
    else if (_serveur instanceof ICalculOlb)
      trace= ((ICalculOlb)_serveur).traceExecution();
    else
      trace= ((ICalculDunes)_serveur).traceExecution();
    BuLabelMultiLine lb= new BuLabelMultiLine(trace);
    lb.setFont(new Font("Monospaced", Font.PLAIN, 10));
    BuDialogMessage di=
      new BuDialogMessage(this, getInformationsSoftware(), lb);
    di.setTitle("Trace d'exécution");
    di.setResizable(true);
    di.activate();
  }
  /*
   * Affichage des préférences
   */
  private void cmdPreferences() {
    if (preferences_ == null) {
      preferences_= new BuPreferencesFrame();
      preferences_.addTab(new BuLookPreferencesPanel(this));
      preferences_.addTab(new BuDesktopPreferencesPanel(this));
      preferences_.addTab(new EbliCalquesPreferencesPanel());
      preferences_.addTab(new BuBrowserPreferencesPanel(this));
      if (!(getApp() instanceof BuApplet))
        preferences_.addTab(new BuDirWinPreferencesPanel(this));
      preferences_.addTab(new RefondePreferencesPanel(this));
      preferences_.addTab(new VolumePreferencesPanel(this));
      preferences_.addTab(new BuMonitorPreferencesPanel(this));
    }
    addInternalFrame(preferences_);
  }
  /*
   * Visualisation des numéros de noeuds
   */
  private void cmdVoirNumNoeuds(boolean _visible) {
    fnCalques_.cqMaillage.setNumerosNoeudsVisibles(_visible);
    fnCalques_.getVueCalque().repaint();
  }
  /*
   * Visualisation des numéros d'éléments
   */
  private void cmdVoirNumElements(boolean _visible) {
    fnCalques_.cqMaillage.setNumerosElementsVisibles(_visible);
    fnCalques_.getVueCalque().repaint();
  }
  // La commmande 3D
  private void cmdVue3D() {
    new Refonde3D(projet, this);
  }
  /*
   * Exportation des données vers Refonde
   */
  private void cmdExporterRefondeCalcul() {
    if (diFcRef_ == null) {
      diFcRef_= new BFileChooser();
      diFcRef_.setFileHidingEnabled(true);
      diFcRef_.setCurrentDirectory(new File(System.getProperty("user.dir")));
      diFcRef_.setMultiSelectionEnabled(false);
    }
    BuFileFilter[] fltRef=
      { new BuFileFilter(new String[] { "inp", "spe" }, "Données Refonde")
    };
    diFcRef_.setDialogTitle("Exportation des données vers Refonde");
    diFcRef_.resetChoosableFileFilters();
    diFcRef_.addChoosableFileFilter(fltRef[0]);
    diFcRef_.setFileFilter(fltRef[0]);
    int r= diFcRef_.showSaveDialog((BuApplication)getApp());
    if (r == JFileChooser.APPROVE_OPTION) {
      new RefondeTacheOperation(this, "Exportation des données vers Refonde") {
        public void act() {
          try {
            oprExporterRefondeCalcul();
          } finally {
            valideActions();
          }
        }
      }
      .start();
    }
  }
  void oprExporterRefondeCalcul() {
    File fichier= diFcRef_.getSelectedFile();
    String path= fichier.getPath();
    String racine;
    if (path.endsWith(".inp") || path.endsWith(".spe"))
      racine= path.substring(0, path.lastIndexOf(CtuluLibString.DOT));
    else
      racine= path;
    statusBar.setMessage("Exportation des données sur " + fichier + "...");
    statusBar.setProgression(0);
    try {
      statusBar.setProgression(10);
      // Transfert des données vers un objet adapté
      IParametresRefonde params=UsineLib.findUsine().creeRefondeParametresRefonde();
/*        IParametresRefondeHelper.narrow(new DParametresRefonde().tie());*/
      RefondeRefonde.versParametresRefonde(params, projet);
      // Ecriture du fichier des parametres
      DParametresRefonde.ecritSurFichiers(racine, params.typeCalcul(), params);
      statusBar.setProgression(100);
    } catch (IllegalArgumentException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
    }
  }
  /*
   * Exportation du maillage vers fichier Postflux
   */
  private void cmdExporterPostfluxMaillage() {
    if (diFcRef_ == null) {
      diFcRef_= new BFileChooser();
      diFcRef_.setFileHidingEnabled(true);
      diFcRef_.setCurrentDirectory(new File(System.getProperty("user.dir")));
      diFcRef_.setMultiSelectionEnabled(false);
    }
    BuFileFilter[] fltRef=
      {
         new BuFileFilter(
          new String[] { "cor", "ele", "bth" },
          "Maillage Postflux")
    };
    diFcRef_.setDialogTitle("Exportation du maillage vers Postflux");
    diFcRef_.resetChoosableFileFilters();
    diFcRef_.addChoosableFileFilter(fltRef[0]);
    diFcRef_.setFileFilter(fltRef[0]);
    int r= diFcRef_.showSaveDialog((BuApplication)getApp());
    if (r == JFileChooser.APPROVE_OPTION) {
      new RefondeTacheOperation(
        this,
        "Exportation du maillage au format Postflux") {
        public void act() {
          try {
            oprExporterPostfluxMaillage();
          } finally {
            valideActions();
          }
        }
        protected void arretDemande() {
          SERVEUR_OLB.setArretDemande();
        }
      }
      .start();
    }
  }
  /*
   * Opération d'exportation du maillage vers Postflux
   */
  void oprExporterPostfluxMaillage() {
    File fichier= diFcRef_.getSelectedFile();
    //    String     path      =fichier.getPath();
    try {
      // Optimisation du maillage si necessaire
      if (projet.estEntierementMaille()
        && !RefondeMaillage.estOptimise(projet.getSuperMaillage())) {
        statusBar.setMessage("Optimisation préalable du maillage...");
        statusBar.setProgression(0);
        // Interruption demandée => Sortie.
        if (RefondeTacheOperation.isTacheCouranteArretDemande())
          return;
        RefondeOLB.optimiser(projet.getSuperMaillage());
        if (!SERVEUR_OLB.estOK()) {
          String msg=
            "Le calcul de largeur de bande s'est terminé avec des erreurs";
          new BuDialogMessage(this, getInformationsSoftware(), msg).activate();
          cmdAfficherTraceExe(SERVEUR_OLB);
          return;
        }
      }
      statusBar.setMessage("Exportation du maillage sur " + fichier + "...");
      statusBar.setProgression(0);
      statusBar.setProgression(10);
      // Ecriture du fichier des parametres
      projet.getMaillage().enregistrerPreflux(projet, fichier);
      statusBar.setProgression(100);
    } catch (IllegalArgumentException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } catch (IOException _exc) {
      new BuDialogError(this, getInformationsSoftware(), _exc.getMessage())
        .activate();
    } finally {
      statusBar.setMessage("");
      statusBar.setProgression(0);
      SERVEUR_OLB.clearArretDemande();
    }
  }
  /**
   * Importation du maillage depuis les fichiers de bathymétrie.
   */
  private void cmdImporterMaillage() {
    if (diFcRef_ == null) {
      diFcRef_= new BFileChooser();
      diFcRef_.setFileHidingEnabled(true);
      diFcRef_.setCurrentDirectory(new File(System.getProperty("user.dir")));
      diFcRef_.setMultiSelectionEnabled(false);
    }
    BuFileFilter[] fltRef=
      {
         new BuFileFilter(
          new String[] { "cor", "ele", "bth" },
          "Maillage Postflux")
    };
    diFcRef_.setDialogTitle("Importation du maillage");
    diFcRef_.resetChoosableFileFilters();
    diFcRef_.addChoosableFileFilter(fltRef[0]);
    diFcRef_.setFileFilter(fltRef[0]);
    int r= diFcRef_.showOpenDialog((BuApplication)getApp());
    if (r == JFileChooser.APPROVE_OPTION) {
      new RefondeTacheOperation(this, "Importation du maillage") {
        // Importation du maillage.
        public void act() {
          try {
            File fichier= diFcRef_.getSelectedFile();
            //            String     path      =fichier.getPath();
            statusBar.setMessage(
              "Importation du maillage depuis " + fichier + "...");
            statusBar.setProgression(0);
            statusBar.setProgression(10);
            projet.setMaillage(RefondeMaillage.ouvrirPreflux(projet, fichier));
            statusBar.setProgression(80);
            fnCalques_.initialise(projet);
            statusBar.setProgression(90);
            clearDeskTop();
            statusBar.setProgression(100);
          } catch (IllegalArgumentException _exc) {
            new BuDialogError(
              RefondeImplementation.this,
              getInformationsSoftware(),
              _exc.getMessage())
              .activate();
          } catch (IOException _exc) {
            new BuDialogError(
              RefondeImplementation.this,
              getInformationsSoftware(),
              _exc.getMessage())
              .activate();
          } finally {
            valideActions();
            fnCalques_.getVueCalque().repaint();
            statusBar.setMessage("");
            statusBar.setProgression(0);
          }
        }
      }
      .start();
    }
  }
  /*
   * Créer un domaine digue
   */
  private void cmdCreerDigue() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    RefondeContour cntr= (RefondeContour)pl.getParents()[0];
    //    RefondeScene scene=pl.getScene();
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // Le contour n'a pas un nombre pair de polyligne
    if (cntr.getPolylignes().length % 2 != 0) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Le contour supportant la polyligne sélectionnée n'a\n"
          + "pas un nombre pair de polylignes")
        .activate();
      return;
    }
    // La polyligne est déjà sur le contour exterieur d'un domaine
    if (getDomaine(pl) != null) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Un domaine existe déjà pour cette polyligne")
        .activate();
      return;
    }
    RefondeDomaineDigue digue= new RefondeDomaineDigue(cntr, pl);
    RefondeDialogDomaine diDomaine= new RefondeDialogDomaine();
    diDomaine.initialise(digue);
    diDomaine.show();
    if (diDomaine.reponse == diDomaine.btOk_) {
      projet.getGeometrie().add(digue);
      //      projet.getGeometrie().initFrontieres();
      projet.setModeleProprietes(RefondeModeleProprietes.defaut(
          projet,projet.getModeleCalcul().typeModele()));
      projet.getModeleCalcul().calculAngles(projet);
      projet.getModeleCalcul().creerAngles(digue);
      fnCalques_.cqAngles.initialise(projet);
      fnCalques_.cqAnglesTrans.initialise(projet);
      fnCalques_.getVueCalque().repaint();
    }
  }
  /*
   * Supprimer un domaine digue
   */
  private void cmdSupprimerDigue() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    RefondeDomaine doma;
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // La polyligne ne fait pas partie d'un domaine digue
    doma= getDomaine(pl);
    if (doma == null || !(doma instanceof RefondeDomaineDigue)) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "La polyligne ne fait pas partie d'un domaine digue")
        .activate();
      return;
    }
    if (new BuDialogConfirmation(this,
      getInformationsSoftware(),
      "Suppression de la digue ?")
      .activate()
      == JOptionPane.OK_OPTION) {
      projet.getModeleCalcul().supprimerAngles((RefondeDomaineDigue)doma);
      projet.getGeometrie().remove(doma);
      doma.setContours(null); // Pour que le domaine disparaisse
      if (doma.hasMaillage()) {
        projet.setResultats(null); // Suppression des résultats
        clearDeskTop();
      }
      projet.setModeleProprietes(RefondeModeleProprietes.defaut(
          projet,projet.getModeleCalcul().typeModele()));
      projet.getModeleCalcul().calculAngles(projet);
      fnCalques_.cqAngles.initialise(projet);
      fnCalques_.cqAnglesTrans.initialise(projet);
      fnCalques_.cqMaillage.initialise(projet);
      fnCalques_.getVueCalque().repaint();
    }
  }
  /*
   * Modifier les proprietes d'un domaine
   */
  private void cmdModifierPropMaillage() {
    Object[] selects= fnCalques_.getSelectedObjects();
    RefondePolyligne pl= (RefondePolyligne)selects[0];
    // On suppose qu'une seule polyligne est sélectionnée
    RefondeDomaine doma;
    // La polyligne fait partie de plusieurs contours
    if (pl.getParents().length > 1) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "Sélectionnez une polyligne faisant partie d'un seul\n" + "contour")
        .activate();
      return;
    }
    // La polyligne ne fait pas partie d'un domaine
    doma= getDomaine(pl);
    if (doma == null) {
      new BuDialogError(
        this,
        getInformationsSoftware(),
        "La polyligne ne fait pas partie d'un domaine")
        .activate();
      return;
    }
    RefondeDialogDomaine diDomaine= new RefondeDialogDomaine();
    diDomaine.initialise(doma);
    diDomaine.show();
  }
  /*
   * Optimisation de la largeur de bande
   */
  /*  private void cmdOptimiserLB() {

      if (projet.getSuperMaillage().elements().length==0) {
        new BuDialogError(this, getInformationsSoftware(),
                          "Pas de maillage").activate();
        return;
      }

      new RefondeTacheOperation(this,"Optimisation de la largeur de bande",true) {
        public void act() {
          try { oprOptimiserLB(); }
          finally { valideActions(); }
        }
        protected void arretDemande() {
          SERVEUR_OLB.setArretDemande();
        }
      }.start();
    }*/
  /**
   * Opération d'optimisation de la largeur de bande
   */
  /*  private void oprOptimiserLB() {
      try {
        statusBar.setMessage("Optimisation de la largeur de bande");
        statusBar.setProgression(10);

        // Interruption demandée => Sortie.
        if (RefondeTacheOperation.isTacheCouranteArretDemande()) return;

        RefondeOLB.optimiser(projet.getSuperMaillage());
        if (fnCalques_.clMaillage.isNumerosElementsVisibles() ||
            fnCalques_.clMaillage.isNumerosNoeudsVisibles())
         fnCalques_.getVueCalque().repaint();

        clearDeskTop();

  //      setEnabledForAction("NOUVELLEFENETRE"      ,projet.estEntierementMaille() && RefondeMaillage.estOptimise(projet.getSuperMaillage()));
  //      setEnabledForAction("MISEENPAGE"           ,false);
  //      setEnabledForAction("COMBINER"             ,false);
  //      setEnabledForAction("VUE3D"                ,false);
      }
      finally {
        statusBar.setMessage("");
        statusBar.setProgression(0);
      }

      if (!SERVEUR_OLB.estOK()) {
        String msg="Le calcul de largeur de bande s'est terminé avec des erreurs";
        new BuDialogMessage(this,getInformationsSoftware(),msg).activate();

        cmdAfficherTraceExe(SERVEUR_OLB);
      }
    }*/

  /**
   * Combinaison de résultats.
   */
  private void cmdCombiner() {
    RefondeDialogCombiResultats di;
    di= new RefondeDialogCombiResultats(this.getFrame());
    di.initialise(projet);
    di.show();
    if (di.reponse != di.btCancel_) {
      RefondeResultats resPrjCur= projet.getResultats();

      // Suppression des anciens résultats déja combinés
      for (int i= 0; i < RefondeDialogCombiResultats.icombinaisons.length; i++) {
        String nom=RefondeResultats.nomResultats[RefondeDialogCombiResultats.icombinaisons[i]];
        resPrjCur.supprimeColonne(resPrjCur.indexOfColonne(nom));
      }
//        resPrjCur.supprimeResultat(resPrjCur.nomResultats[di.icombinaisons[i]]);

      if (di.mdlsCombiLoad.isEmpty()) return;

      // Ajout des nouveaux résultats

      // Nom des nouveaux résultats
      String[] nomResCombi= new String[di.mdlsCombiLoad.size()];
      for (int i= 0; i < nomResCombi.length; i++)
        nomResCombi[i]= (String)di.mdlsCombiLoad.get(i);

      try {

        // Lecture du projet de combinaison
        RefondeProjet prjCombi= new RefondeProjet();
        try {
          prjCombi.ouvrir(new File(di.tfPrjCombi.getText()));
        } catch (RefondeTacheInterruptionException _exc) {}

        if (!prjCombi.hasResultats()) {
          new BuDialogError(this,isRefonde_,
            "Le projet de combinaison ne contient aucun résultat")
            .activate();
          return;
        }

        // Combinaison et stockage du résultat
        RefondeResultats resPrjCombi= prjCombi.getResultats();
        for (int i= 0; i < nomResCombi.length; i++) {
          double[][] reCombi;
          double[][] rePrjCur;
          double[][] rePrjCombi;
          int ires;

          for (ires= 0; ires < RefondeDialogCombiResultats.icombinaisons.length; ires++)
            if (nomResCombi[i].equals(
              RefondeResultats.nomResultats[RefondeDialogCombiResultats.icombinaisons[ires]]))
              break;

          String nom=RefondeResultats.nomResultats[RefondeDialogCombiResultats.iresUtils[ires]];
          rePrjCur=resPrjCur.getColonne(resPrjCur.indexOfColonne(nom));
          rePrjCombi=resPrjCombi.getColonne(resPrjCur.indexOfColonne(nom));
//          rePrjCur=resPrjCur.getResultat(resPrjCur.nomResultats[di.iresUtils[ires]]);
//          rePrjCombi=resPrjCombi.getResultat(resPrjCombi.nomResultats[di.iresUtils[ires]]);
          if (rePrjCur == null || rePrjCombi == null) {
            new BuDialogError(this,isRefonde_,
              "Le projet courant ou le projet de combinaison ne possèdent pas\n"
                + "les résultats nécessaire pour réaliser la combinaison \""
                + nomResCombi[i]
                + "\"")
              .activate();
            return;
          }

          reCombi= new double[rePrjCur.length][rePrjCur[0].length];
          for (int j=0; j<reCombi.length; j++)
            for (int k=0; k<reCombi[0].length; k++)
              reCombi[j][k]=rePrjCur[j][k]-rePrjCombi[j][k];
          resPrjCur.ajouteColonne(nomResCombi[i], reCombi);
        }
      }
      catch (IOException _exc) {
        new BuDialogError(this, isRefonde_, _exc.getMessage()).activate();
      }
      //      mnRes_.rafraichir(projet);
    }
  }
  /**
   * Créer une nouvelle fenêtre de post traitement.
   */
  private void cmdNouvelleFenetre() {
    RefondeFillePost fn= new RefondeFillePost(ac_);
    fn.initialise(projet);
    fn.getVueCalque().setTaskView(taches_);
    addInternalFrame(fn);
    fn.restaurer();
    fn.setClosable(true);
    //    fnsCalques_.add(fn);
  }
  /**
   * Création d'une fenêtre de mise en page.
   */
  private void cmdMiseEnPage() {
    // Définition du format de page.
    PageFormat pf=
      RefondeMiseEnPageHelper.convert2Imp(
        projet.getMiseEnPage().getPageFormat());
    PageFormat pfNew;
    pfNew= PrinterJob.getPrinterJob().pageDialog(pf);
    if (pfNew == pf)
      return; // La boite de dialogue a été abortée.
    // Initialisation de la mise en page avec les fenetres existantes.
    projet.getMiseEnPage().initialise(
      this,
      RefondeMiseEnPageHelper.convert2MM(pfNew));
    if (fnMEP_ != null)
      removeInternalFrame(fnMEP_);
    fnMEP_= new RefondeFilleMiseEnPage(this, ac_);
    
    //Mise aux proportions de la page
    int surf= fnMEP_.getHeight() * fnMEP_.getWidth();
    double proport= pfNew.getWidth() / pfNew.getHeight();
    int width= (int) Math.sqrt(surf * proport);
    fnMEP_.setSize(width, (int) (width / proport));
    
    fnMEP_.setMiseEnPage(projet.getMiseEnPage());
    fnMEP_.getVueCalque().setTaskView(taches_);
    addInternalFrame(fnMEP_);
    //fnMEP_.getVueCalque().setSize((int) pf.getImageableWidth(), (int) pf.getImageableHeight());
    fnMEP_.restaurer();
    fnMEP_.setClosable(true);
  }
  /**
   * Suppression des objets sélectionnés.
   */
  private void cmdSupprimerObjets() {
    fnMEP_.supprimerObjetsSelectionnes();
  }
  /**
   * Impression de la fenêtre interne courante.
   */
  public void cmdImprimer() {
    JInternalFrame frame= getCurrentInternalFrame();
    if (frame instanceof EbliPageable) {
      if (frame instanceof RefondeFilleMiseEnPage) {
        PageFormat pf=
          RefondeMiseEnPageHelper.convert2Imp(
            projet.getMiseEnPage().getPageFormat());
        ((EbliPageable)frame).getDefaultEbliPageFormat().setPageFormat(pf);
      }
      super.cmdImprimer((EbliPageable)frame);
//      PrinterJob printJob= PrinterJob.getPrinterJob();
//      if (frame instanceof RefondeFilleMiseEnPage) {
//        PageFormat pf=
//          RefondeMiseEnPageHelper.convert2Imp(
//            projet.getMiseEnPage().getPageFormat());
//        printJob.setPrintable((Printable)frame, pf);
//      } else
//        printJob.setPrintable((Printable)frame);
//      if (printJob.printDialog()) {
//        try {
//          printJob.print();
//        } catch (Exception PrintException) {
//          PrintException.printStackTrace();
//        }
//      }
    }
  }
  /**
   * Recentrage de la vue sur un noeud de numéro donné. Pas de changement
   * d'échelle lors de ce recentrage.
   */
  private void cmdRecentrerNoeud() {
    RefondePnRecentrerNoeud pn= new RefondePnRecentrerNoeud();
    RefondeDialogPanneau di;
    di= new RefondeDialogPanneau(getFrame(), pn, "Recentrer la vue") {
      public boolean actionApply() {
        int num=
          ((RefondePnRecentrerNoeud)getPanneauPrincipal()).getNumeroNoeud();
        if (num == -1)
          return true;
        // Recherche du noeud.
        GrMaillageElement ml= RefondeMaillage.creeSuperMaillage(projet);
        GrNoeud[] nds= ml.noeuds();
        GrPoint ptC= null;
        for (int i= 0; i < nds.length; i++) {
          if (((RefondeNoeudData)nds[i].data()).numero == num) {
            ptC= nds[i].point_;
            break;
          }
        }
        if (ptC == null) {
          new BuDialogWarning(
            RefondeImplementation.this,
            isRefonde_,
            "Le projet ne contient aucun noeud de numéro " + num)
            .activate();
          return true;
        }
        // Recentrage.
        GrBoite bt= fnCalques_.getVueCalque().getViewBoite();
        GrBoite btC= new GrBoite();
        btC.ajuste(
          new GrPoint(
            ptC.x_ - (bt.e_.x_ - bt.o_.x_) / 2,
            ptC.y_ - (bt.e_.y_ - bt.o_.y_) / 2,
            0));
        btC.ajuste(
          new GrPoint(
            ptC.x_ + (bt.e_.x_ - bt.o_.x_) / 2,
            ptC.y_ + (bt.e_.y_ - bt.o_.y_) / 2,
            0));
        fnCalques_.getVueCalque().changeRepere(this, btC, 0);
        return true;
      }
    };
    di.show();
  }
  /**
   * Recentrage de la vue sur un élément de numéro donné. Pas de changement
   * d'échelle lors de ce recentrage.
   */
  private void cmdRecentrerElement() {
    RefondePnRecentrerElement pn= new RefondePnRecentrerElement();
    RefondeDialogPanneau di;
    di= new RefondeDialogPanneau(getFrame(), pn, "Recentrer la vue") {
      public boolean actionApply() {
        int num=
          ((RefondePnRecentrerElement)getPanneauPrincipal()).getNumeroElement();
        if (num == -1)
          return true;
        // Recherche de l'element.
        GrMaillageElement ml= RefondeMaillage.creeSuperMaillage(projet);
        GrElement[] els= ml.elements();
        GrPoint ptC= null;
        for (int i= 0; i < els.length; i++) {
          if (((RefondeElementData)els[i].data()).numero == num) {
            ptC= els[i].barycentre();
            break;
          }
        }
        if (ptC == null) {
          new BuDialogWarning(
            RefondeImplementation.this,
            isRefonde_,
            "Le projet ne contient aucun élément de numéro " + num)
            .activate();
          return true;
        }
        // Recentrage.
        GrBoite bt= fnCalques_.getVueCalque().getViewBoite();
        GrBoite btC= new GrBoite();
        btC.ajuste(
          new GrPoint(
            ptC.x_ - (bt.e_.x_ - bt.o_.x_) / 2,
            ptC.y_ - (bt.e_.y_ - bt.o_.y_) / 2,
            0));
        btC.ajuste(
          new GrPoint(
            ptC.x_ + (bt.e_.x_ - bt.o_.x_) / 2,
            ptC.y_ + (bt.e_.y_ - bt.o_.y_) / 2,
            0));
        fnCalques_.getVueCalque().changeRepere(this, btC, 0);
        return true;
      }
    };
    di.show();
  }

  /**
   * Retourne le domaine associé à une polyligne ou null si pas de domaine.
   */
  private RefondeDomaine getDomaine(RefondePolyligne _poly) {
    RefondeScene scene= _poly.getScene();
    RefondeDomaine[] domas= new RefondeDomaine[scene.getDomaines().size()];
    scene.getDomaines().toArray(domas);
    for (int i= 0; i < domas.length; i++)
      if (domas[i].getContours()[0].hasChild(_poly))
        return domas[i];
    return null;
  }

  /**
   * Met a jour les fichiers recents (suite à bug dans BuRecentFiles qui stocke
   * le nom du projet même s'il existe déjà dans le fichier refonde.ini).
   */
  private void updateRecentFiles(File _fichier) {
    boolean found=false;
    String s=_fichier.getPath();

    for (int i=1; i<10; i++) {
      if (s.equals(RefondePreferences.REFONDE.getStringProperty("file.recent."+i+".path")))
        found=true;
    }

    if (!found) {
      getMainMenuBar().addRecentFile(_fichier.getPath(),"refonde-logo");
      RefondePreferences.REFONDE.writeIniFile();
    }
  }

  /**
   * Active ou non les actions en fonction de l'état de l'application.
   */
  void valideActions() {
    boolean b;
    // Spécifique du modèle de houle.
    boolean bHoule=projet!=null &&
      projet.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_HOULE;

    b=projet!=null;
    setEnabledForAction("OUVRIRFICHIERS", b);

    b=projet!=null;
    setEnabledForAction("ENREGISTRERPROJET", b);

    b=projet!=null;
    setEnabledForAction("ENREGISTRERPROJETSOUS", b);

    b=projet!=null;
    setEnabledForAction("ENREGISTRERFICHIERS", b);

    b=projet!=null;
    setEnabledForAction("FERMER", b);

    b=projet!=null;
    setEnabledForAction("PROPRIETESPROJET", b);

    b=projet!=null && bHoule;
    setEnabledForAction("CALCULERANGLES", b);

    b=projet!=null && bHoule;
    setEnabledForAction("NOUVELANGLE", b);

    b=projet!=null;
    setEnabledForAction("PARAMETRESCALCUL", b);

    b=projet!=null&&SERVEUR_REFONDE!=null;
    setEnabledForAction("CALCULER", b);

    b=(projet!=null && bHoule &&
       projet.getModeleCalcul().typeHoule()==RefondeModeleCalcul.HOULE_ALEA &&
       SERVEUR_REFONDE!=null);
    setEnabledForAction("REPRENDRECALCUL", b);

    b=projet!=null && SERVEUR_REFONDE!=null &&
      !SERVEUR_REFONDE.traceExecution().equals("");
    setEnabledForAction("AFFICHERTRACEEXE", b);

    b=projet!=null && projet.getSuperMaillage().noeuds().length!=0;
    setEnabledForAction("NUMNOEUDS", b);

    b=projet!=null && projet.getSuperMaillage().elements().length!=0;
    setEnabledForAction("NUMELEMENTS", b);

    b=MODULE_3D && projet!=null && projet.hasResultats();
    setEnabledForAction("VUE3D", b);

    b=projet!=null;
    setEnabledForAction("EXPORTERREFONDECALCUL", b);

    b=projet!=null;
    setEnabledForAction("EXPORTERMAILLAGE", b);

    b=projet!=null;
    setEnabledForAction("IMPORTERMAILLAGE", b);

    b=projet!=null && projet.hasResultats() && bHoule;
    setEnabledForAction("COMBINER", b);

    b=projet!=null && projet.estEntierementMaille() &&
      RefondeMaillage.estOptimise(projet.getSuperMaillage());
    setEnabledForAction("NOUVELLEFENETRE", b);

    b=projet!=null;
    setEnabledForAction("MISEENPAGE", b);

    b=projet!=null && projet.getSuperMaillage().noeuds().length!=0;
    setEnabledForAction("RECENTRERNOEUD", b);

    b=projet!=null && projet.getSuperMaillage().elements().length!=0;
    setEnabledForAction("RECENTRERELEMENT", b);
  }

  //------------------------------------------------------------------------------
  //--- MENUS --------------------------------------------------------------------
  //------------------------------------------------------------------------------
  // Le menu "Fichier" standard est remplacé par celui-ci
  private BuMenu buildFileMenu() {
    BuMenu r= new BuMenu("Fichier", "FICHIER");
    r.addMenuItem(
      "Nouveau projet...",
      "CREERPROJET",
      BuResource.BU.getIcon("CREER"),
      false,
      KeyEvent.VK_N);
    r.addMenuItem(
      "Ouvrir...",
      "OUVRIRPROJET",
      BuResource.BU.getIcon("OUVRIR"),
      false,
      KeyEvent.VK_O);
    r.addSubMenu(new BuMenuRecentFiles(),true);
    r.addMenuItem(
      "Ouvrir les fichiers projet...",
      "OUVRIRFICHIERS",
      RefondeResource.REFONDE.getIcon("OUVRIRFICHIERS"),
      false);
    r.addSeparator();
    r.addMenuItem(
      "Enregistrer",
      "ENREGISTRERPROJET",
      BuResource.BU.getIcon("ENREGISTRER"),
      false,
      KeyEvent.VK_E);
    r.addMenuItem(
      "Enregistrer sous...",
      "ENREGISTRERPROJETSOUS",
      BuResource.BU.getIcon("ENREGISTRERSOUS"),
      false,
      KeyEvent.VK_S);
    r.addMenuItem(
      "Enregistrer les fichiers projet...",
      "ENREGISTRERFICHIERS",
      RefondeResource.REFONDE.getIcon("ENREGISTRERFICHIERS"),
      false);
    r.addMenuItem(
      BuResource.BU.getString("Fermer le projet"),
      "FERMER",
      false,
      KeyEvent.VK_W);
    r.addSeparator();
      BuMenu sm= new BuMenu("Importer", "IMPORTER");
      r.addSubMenu(sm, false);
      sm.addMenuItem("Maillage...", "IMPORTERMAILLAGE", false);
      sm= new BuMenu("Exporter", "EXPORTER");
      r.addSubMenu(sm, false);
      sm.addMenuItem(
        "Fichiers de données Refonde (.inp/.spe)...",
        "EXPORTERREFONDECALCUL",
        false);
      sm.addMenuItem(
        "Fichiers de maillage Postflux (.cor/.ele/.bth)...",
        "EXPORTERMAILLAGE",
        false);
    r.addSeparator();
    r.addMenuItem(
      "Propriétés du projet...",
      "PROPRIETESPROJET",
      RefondeResource.REFONDE.getIcon("PROPRIETES"),
      false);
    r.addSeparator();
    r.addMenuItem(
      "Mise en page...",
      "MISEENPAGE",
      BuResource.BU.getIcon("PREVISUALISER"),
      false);
    r.addMenuItem("Imprimer...", "IMPRIMER", false, KeyEvent.VK_I);
    r.addSeparator();
    r.addMenuItem("Quitter...", "QUITTER", false, KeyEvent.VK_Q);
    return r;
  }
  // Le menu "Edition" est remplacé par celui ci
  private BuMenu buildEditionMenu() {
    BuMenu r= new BuMenu("Edition", "EDITION");
    r.addMenuItem(
      BuResource.BU.getString("Supprimer"),
      "SUPPRIMER",
      BuResource.BU.getIcon("DETRUIRE"),
      false,
      KeyEvent.VK_DELETE);
    r.addSeparator("Recentrer la vue");
    r.addMenuItem(
      RefondeResource.REFONDE.getString("Sur un noeud..."),
      "RECENTRERNOEUD",
      RefondeResource.REFONDE.getIcon("CENTRER-NOEUD"),
      false);
    r.addMenuItem(
      RefondeResource.REFONDE.getString("Sur un élément..."),
      "RECENTRERELEMENT",
      RefondeResource.REFONDE.getIcon("CENTRER-ELEMENT"),
      false);
    r.addSeparator("Voir");
    r.addCheckBox(
      "Numéro des noeuds",
      "NUMNOEUDS",
      RefondeResource.REFONDE.getIcon("NUMNOEUDS"),
      false,
      false);
    r.addCheckBox(
      "Numéro des éléments",
      "NUMELEMENTS",
      RefondeResource.REFONDE.getIcon("NUMELEMENTS"),
      false,
      false);
    r.addSeparator();
    r.addMenuItem(
      "Préférences",
      "PREFERENCE",
      BuResource.BU.getIcon("PREFERENCE"),
      true,
      KeyEvent.VK_F2);
    r.addMenuItem(
      "Console",
      "CONSOLE",
      BuResource.BU.getIcon("TERMINAL"),
      false);
    return r;
  }
  // Le menu "Géométrie"
  private BuMenu buildGeometrieMenu() {
    BuMenu r= new BuMenu("Géométrie", "GEOMETRIE");
    r.addMenuItem("Créer un domaine digue", "CREERDIGUE", null, false);
    r.addMenuItem("Supprimer un domaine digue", "SUPPRIMERDIGUE", null, false);
//    r.addSeparator("Domaines poreux");
//    r.addMenuItem("Créer un domaine poreux","CREERPOREUX",RefondeResource.REFONDE.getIcon("creerporeux"),false);
//    r.addMenuItem("Supprimer le domaine poreux","SUPPRIMERPOREUX",RefondeResource.REFONDE.getIcon("supporeux"),false);
//    r.addMenuItem("Propriétés du domaine poreux","MODIFIERPOREUX",RefondeResource.REFONDE.getIcon("proprietesporeux"),false);
//    r.addMenuItem("Déplacer le point du plan","DEPLACERPOINT",RefondeResource.REFONDE.getIcon("deplacerpoint"),false);
    return r;
  }
  // Le menu "Modèle de propriétés"
  private BuMenu buildModeleProprietes() {
    BuMenu r= new BuMenu("Propriétés", "MDLPROPRIETES");
    r.addMenuItem(
      "Propriétés de bord...",
      "PROPRIETESBORD",
      RefondeResource.REFONDE.getIcon("NATURESBORD"),
      false);
    r.addMenuItem(
      "Propriétés de fond...",
      "PROPRIETESFOND",
      RefondeResource.REFONDE.getIcon("PROPRIETESFOND"),
      false);
    return r;
  }
  // Le menu "Modèle de calcul"
  private BuMenu buildModeleCalcul() {
    BuMenu r= new BuMenu("Calcul", "mnMDLCALCUL");
    r.addMenuItem(
      "Paramètres du calcul...",
      "PARAMETRESCALCUL",
      BuResource.BU.getIcon("parametre"),
      false);
    r.addSeparator();
    r.addMenuItem(
      "Exécuter Refonde",
      "CALCULER",
      BuResource.BU.getIcon("calculer"),
      false);
    r.addMenuItem("Reprise en houle aléatoire", "REPRENDRECALCUL", false);
    r.addMenuItem(
      "Afficher trace d'exécution...",
      "AFFICHERTRACEEXE",
      BuResource.BU.getIcon("texte"),
      false);
    r.addSeparator();
    r.addMenuItem(
      "Calculer les angles d'incidence",
      "CALCULERANGLES",
      RefondeResource.REFONDE.getIcon("CALCULERANGLES"),
      false);
    r.addMenuItem(
      "Nouvel angle d'incidence",
      "NOUVELANGLE",
      RefondeResource.REFONDE.getIcon("NOUVELANGLE"),
      false);
    r.addSeparator();
    r.addMenuItem(
      "Modifier les angles d'incidence/transmission...",
      "MODIFIERANGLE",
      RefondeResource.REFONDE.getIcon("MODIFIERANGLE"),
      false);
    r.addMenuItem(
      "Modifier les angles supplémentaires associés...",
      "MODIFIERANGSUP",
      RefondeResource.REFONDE.getIcon("MODIFIERANGSUP"),
      false);
    return r;
  }
  // Le menu "Maillage"
  private BuMenu buildMaillage() {
    BuMenu r= new BuMenu("Maillage", "mnMAILLAGE");
    r.addMenuItem(
      "Modifier les propriétés de maillage d'un domaine...",
      "MODIFIERPROPMAILLAGE",
      null,
      false);
    r.addSeparator();
    r.addMenuItem(
      "Mailler le domaine",
      "MAILLER",
      RefondeResource.REFONDE.getIcon("MAILLAGE"),
      false);
    r.addMenuItem(
      "Supprimer le maillage",
      "SUPPRIMERMAILLAGE",
      RefondeResource.REFONDE.getIcon("SUPMAILLAGE"),
      false);
    //    r.addSeparator();
    //    r.addMenuItem("Optimiser la largeur de bande","OPTIMISERLB",RefondeResource.REFONDE.getIcon("OLB"),false);
    return r;
  }
  // Le menu "Résultats"
  private BuMenu buildResultats() {
    BuMenu r= new BuMenu("Résultats", "mnRESULTATS");
    r.addMenuItem(
      "Afficher la vue 3D",
      "VUE3D",
      BuResource.BU.getIcon("calculer"),
      false);
    r.addSeparator();
    r.addMenuItem("Combiner les résultats...", "COMBINER", false);
    return r;
  }
  //------------------------------------------------------------------------------
  //--- CONNEXION ----------------------------------------------------------------
  //------------------------------------------------------------------------------
  public void exit() {
    closeConnexions();
    super.exit();
  }
  public void finalize() {
    closeConnexions();
  }
  public boolean isCloseFrameMode() {
    return false;
  }

  /**
   * @see org.fudaa.fudaa.commun.impl.FudaaCommonImplementation#getApplicationPreferences()
   */
  public BuPreferences getApplicationPreferences() {
    return RefondePreferences.REFONDE;
  }

  /**
   * @see org.fudaa.fudaa.commun.dodico.FudaaImplementation#clearVariables()
   */
  protected void clearVariables() {
  CONNEXION_REFONDE=null;
  SERVEUR_REFONDE=null;
  CONNEXION_DUNES=null;
  SERVEUR_DUNES=null;
  CONNEXION_OLB=null;
  SERVEUR_OLB=null;
  }
  /**
   * @see org.fudaa.fudaa.commun.dodico.FudaaImplementation#getTacheConnexionMap()
   */
  protected FudaaDodicoTacheConnexion[] getTacheConnexionMap() {
    FudaaDodicoTacheConnexion c=new FudaaDodicoTacheConnexion(SERVEUR_OLB,CONNEXION_OLB);
    FudaaDodicoTacheConnexion c1=new FudaaDodicoTacheConnexion(SERVEUR_DUNES,CONNEXION_DUNES);
    FudaaDodicoTacheConnexion c2=new FudaaDodicoTacheConnexion(SERVEUR_REFONDE,CONNEXION_REFONDE);
    return new FudaaDodicoTacheConnexion[]{c,c1,c2};
  }
  /**
   * @see org.fudaa.fudaa.commun.dodico.FudaaImplementation#getTacheDelegateClass()
   */
  protected Class[] getTacheDelegateClass() {
    return new Class[]{DCalculOlb.class,DCalculDunes.class,DCalculRefonde.class};
  }
  /**
   * @see org.fudaa.fudaa.commun.dodico.FudaaImplementation#initConnexions(java.util.Map)
   */
  protected void initConnexions(Map _r) {
  FudaaDodicoTacheConnexion c=(FudaaDodicoTacheConnexion)_r.get(DCalculRefonde.class);
  CONNEXION_REFONDE=c.getConnexion();
  SERVEUR_REFONDE=ICalculRefondeHelper.narrow(c.getTache());
  c=(FudaaDodicoTacheConnexion)_r.get(DCalculOlb.class);
  CONNEXION_OLB=c.getConnexion();
  SERVEUR_OLB=ICalculOlbHelper.narrow(c.getTache());
  c=(FudaaDodicoTacheConnexion)_r.get(DCalculDunes.class);
  CONNEXION_DUNES=c.getConnexion();
  SERVEUR_DUNES=ICalculDunesHelper.narrow(c.getTache());
  }
}
