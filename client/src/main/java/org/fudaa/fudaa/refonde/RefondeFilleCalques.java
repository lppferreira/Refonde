/*
 * @file         RefondeFilleCalques.java
 * @creation     1999-08-09
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;

import com.memoire.bu.BuButton;
import com.memoire.bu.BuCommonInterface;
import com.memoire.bu.BuDesktop;
import com.memoire.bu.BuDialogConfirmation;
import com.memoire.bu.BuDialogError;
import com.memoire.bu.BuPopupButton;
import com.memoire.bu.BuToggleButton;

import org.fudaa.ebli.calque.*;
import org.fudaa.ebli.calque.dessin.DeForme;
import org.fudaa.ebli.calque.dessin.DeRectangle;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrSymbole;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.trace.TracePoint;
import org.fudaa.ebli.trace.TraceSurface;
/**
 * Une fenetre interne affichant les calques et permettant l'intération avec
 * l'utilisateur.
 *
 * @version      $Id: RefondeFilleCalques.java,v 1.14 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeFilleCalques extends EbliFilleCalques {
  public RefondeCalqueContour cqContours;
  public RefondeCalqueMaillage cqMaillage;
  public RefondeCalqueAngle cqAngles;
  public RefondeCalqueAngleTransmission cqAnglesTrans;
  public RefondeCalqueAngleInteraction cqSaisieAngle;
  public RefondeCalqueContourInteraction cqContoursI;
  /** Création interactive de formes - Domaines poreux */
  public BCalqueFormeInteraction cqFormeI;
  /** Affichage des domaines poreux */
  public BCalquePolygone cqDomainePoreux;
  /** Affichage des points des domaines poreux */
  public BCalquePoint cqDomainePoreuxPts;
  /** Tools spécifiques de la fenêtre. */
  private JComponent[] btTools_;
  /** Tool création de domaine poreux */
  private BuToggleButton btCreePoreux_;
  /** Tool suppression de domaine poreux */
  private BuButton btSupPoreux_;
  /** Tool modification de domaine poreux */
  private BuButton btModPoreux_;
  /** Tool modification du point d'un plan de domaine poreux */
  private BuButton btModPoint_;
  /** Actions spécifiques actives de la fenetre */
  //  private Vector enabledActions_=new Vector();
  BuCommonInterface app_;
  private Object[] bdSelects_=new Object[0];
  private Hashtable gr2Bd_;
  //  private BGroupeCalque     gc_;
  //  private BGroupeCalque     gcMai_;
  private BuToggleButton btSel2Pts_;
  private RefondeProjet prj_;
  //  private JMenu mnVoir_;
  /**
   * Création d'une fenètre de gestion des calques sans préoccupation de
   * la vue associée.
   */
  //  public RefondeFilleCalques(BuCommonInterface _app, BArbreCalque _ac) {
  //    this(_app,_ac);
  //  }
  /**
   * Création d'une fenètre de gestion des calques sans préoccupation de
   * l'arbre associé
   */
  public RefondeFilleCalques(BuCommonInterface _app, BArbreCalque _ac) {
    super(null, _app);
    this.setClosable(false);
    this.setTitle("Fenêtre principale");
    addInternalFrameListener(_ac);
    app_= _app;
    BCalque cqRoot= getVueCalque().getCalque();
    // Suppression du calque par défaut
    BCalque[] cqs= cqRoot.getTousCalques();
    for (int i= 0; i < cqs.length; i++)
      if (cqs[i].getName() == null)
        cqs[i].detruire();
    gr2Bd_= new Hashtable();
    // Domaines poreux
    cqDomainePoreux= new BCalquePolygone();
    cqDomainePoreux.setForeground(new Color(255, 0, 0, 75));
    cqDomainePoreux.setBackground(cqDomainePoreux.getForeground());
    cqDomainePoreux.setTypeSurface(TraceSurface.UNIFORME);
    cqDomainePoreux.setTitle("Domaines poreux");
    cqDomainePoreux.setName("cqPOREUX");
    cqRoot.add(cqDomainePoreux);
    // Symboles de direction des domaines poreux (sous calque domaine poreux)
    //    cqDomainePoreuxDir=new BCalqueSymbole();
    //    cqDomainePoreuxDir.setForeground(cqDomainePoreux.getForeground());
    //    cqDomainePoreuxDir.setTitle("Directions");
    //    cqDomainePoreuxDir.setName("cqDIRPOREUX");
    //    cqDomainePoreux.add(cqDomainePoreuxDir);
    // Points de plan du domaine poreux
    cqDomainePoreuxPts= new BCalquePoint();
    cqDomainePoreuxPts.setTypePoint(TracePoint.DISQUE);
    cqDomainePoreuxPts.setForeground(Color.red.darker());
    cqDomainePoreuxPts.setTitle("Points des plans");
    cqDomainePoreuxPts.setName("cqPTSPOREUX");
    cqRoot.add(cqDomainePoreuxPts);
    cqContours= new RefondeCalqueContour();
    cqContours.setGr2Bd(gr2Bd_);
    cqContours.setTitle("Contours");
    cqContours.setName("cqCONTOURS");
    cqRoot.add(cqContours);
    cqMaillage= new RefondeCalqueMaillage();
    cqMaillage.setName("Maillage");
    cqRoot.add(cqMaillage);
    cqAngles= new RefondeCalqueAngle();
    //    cqAngles.setGr2Bd(gr2Bd_);
    cqAngles.setName("Angles d'incidence");
    cqRoot.add(cqAngles);
    cqAnglesTrans= new RefondeCalqueAngleTransmission();
    cqAnglesTrans.setName("Angles de transmission");
    cqRoot.add(cqAnglesTrans);
    cqSaisieAngle=
      new RefondeCalqueAngleInteraction(cqAngles, getVueCalque(), _ac);
    cqSaisieAngle.setName("Nouvel angle");
    cqSaisieAngle.setGele(true);
    cqRoot.getCalqueParNom("cqADMIN").add(cqSaisieAngle);
    cqContoursI= new RefondeCalqueContourInteraction();
    cqContoursI.setTitle("Sélection 2 pts");
    cqContoursI.setName("cqSEL2PTS");
    cqContoursI.setGele(true);
    cqRoot.getCalqueParNom("cqADMIN").add(cqContoursI);
    cqContoursI.addSelectionListener(this);
    cqContoursI.addSelectionListener(
      (BCalqueSelection)cqRoot.getCalqueParNom("cqSELECTIONAFFICHAGE"));
    // Saisie d'un domaine poreux
    cqFormeI= new BCalqueFormeInteraction(cqDomainePoreux);
    cqFormeI.setTitle("Saisie de formes");
    cqFormeI.setName("cqFORME");
    cqFormeI.setGele(true);
    cqFormeI.setTypeForme(DeForme.RECTANGLE);
    cqFormeI.addFormeEventListener(new FormeEventListener() {
      public void formeSaisie(FormeEvent _evt) {
        cqFormeI_formeSaisie(_evt);
      }
    });
    cqRoot.getCalqueParNom("cqADMIN").add(cqFormeI);
    buildTools();
    associeBoutonCalqueInteraction(cqFormeI, btCreePoreux_);
    associeBoutonCalqueInteraction(cqContoursI, btSel2Pts_);
    _ac.refresh();
    getVueCalque().setBackground(Color.white);
    setBoutonRepereVisible(false);
    setBoutonsStandardVisible(false);
  }
  public String[] getEnabledActions() {
    //    return (String[])enabledActions_.toArray(new String[0]);
    return super.getEnabledActions();
  }
  public String[] getDisabledActions() {
    return super.getDisabledActions();
  }
  //  public JMenu[] getSpecificMenus() {
  /*    JMenu[] mnPere=super.getSpecificMenus();
      JMenu[] r     =new JMenu[mnPere.length+1];

      for (int i=0; i<mnPere.length; i++) r[i]=mnPere[i];
      r[mnPere.length+0]=mnVoir_;  */
  //    JMenu[] r=new JMenu[] {mnVoir_};
  //
  //    return r;
  //  }
  /**
   * Retourne les outils spécifiques
   */
  public JComponent[] getSpecificTools() {
    JComponent[] cpssuper= super.getSpecificTools();
    // La valeur de DesktopPane n'est validée qu'une fois la fenêtre ajoutée
    // au desktop. En théorie, il faudrait faire le traitement ci dessous
    // une fois la fenêtre ajoutée. En pratique, on le fait dans la méthode
    // getSpecificTools() appelée chaque fois que la fenêtre est rendue active.
    // B.M. 20.08.2001
    try {
      for (int i= 0; i < btTools_.length; i++)
        if (btTools_[i] != null)
          ((BuPopupButton)btTools_[i]).setDesktop((BuDesktop)getDesktopPane());
    } catch (ClassCastException e) {}
    JComponent[] cps= new JComponent[cpssuper.length + btTools_.length];
    System.arraycopy(cpssuper, 0, cps, 0, cpssuper.length);
    System.arraycopy(btTools_, 0, cps, cpssuper.length, btTools_.length);
    return cps;
  }
  /**
   * Création de la barre d'outils avec les outils de tous les calques.
   */
  private void buildTools() {
    Vector vcps= new Vector();
    // Bouton supplémentaire de sélection de contours par 2 points
    btSel2Pts_=
      new BuToggleButton(
        RefondeResource.REFONDE.getIcon("select-contours.gif"));
    btSel2Pts_.setToolTipText("Sélection des frontières par 2 points");
    btSel2Pts_.setEnabled(false);
    vcps.add(btSel2Pts_);
    //    addBouton(btSel2Pts_);
    // Separator
    vcps.add(null);
    // Bouton de création de domaine poreux
    btCreePoreux_=
      new BuToggleButton(RefondeResource.REFONDE.getIcon("creerporeux"));
    btCreePoreux_.setToolTipText("Création d'un domaine poreux");
    btCreePoreux_.setActionCommand("CREERPOREUX");
    //    enabledActions_.add("CREERPOREUX");
    btCreePoreux_.addActionListener(this);
    vcps.add(btCreePoreux_);
    // Bouton de suppression du domaine poreux
    btSupPoreux_= new BuButton(RefondeResource.REFONDE.getIcon("supporeux"));
    btSupPoreux_.setToolTipText("Suppression du domaine poreux");
    btSupPoreux_.setActionCommand("SUPPRIMERPOREUX");
    //    enabledActions_.add("SUPPRIMERPOREUX");
    btSupPoreux_.addActionListener(this);
    btSupPoreux_.setEnabled(false);
    vcps.add(btSupPoreux_);
    // Bouton de modification du domaine poreux
    btModPoreux_=
      new BuButton(RefondeResource.REFONDE.getIcon("proprietesporeux"));
    btModPoreux_.setToolTipText("Propriétés du domaine poreux");
    btModPoreux_.setActionCommand("MODIFIERPOREUX");
    //    enabledActions_.add("MODIFIERPOREUX");
    btModPoreux_.addActionListener(this);
    btModPoreux_.setEnabled(false);
    vcps.add(btModPoreux_);
    // Bouton de modification du point d'un plan de domaine poreux
    btModPoint_= new BuButton(RefondeResource.REFONDE.getIcon("deplacerpoint"));
    btModPoint_.setToolTipText(
      "Déplacement du point de plan d'un domaine poreux");
    btModPoint_.setActionCommand("DEPLACERPOINT");
    //    enabledActions_.add("DEPLACERPOINT");
    btModPoint_.addActionListener(this);
    btModPoint_.setEnabled(false);
    vcps.add(btModPoint_);
    btTools_= (JComponent[])vcps.toArray(new JComponent[0]);
    //    for (int i=0; i<btTools_.length; i++) if (btTools_[i]!=null) btTools_[i].setEnabled(true);
  }

  /**
   * Récupération des objets sélectionnés et mise à l'état activables les
   * boutons et menus concernés
   */
  public void selectedObjects(SelectionEvent _evt) {
    VecteurGrContour grSelects_=_evt.getObjects();
    //    Hashtable grClass=new Hashtable();

    // Stockage des bdObjects
    bdSelects_=new Object[grSelects_.nombre()];

    // Cas particulier des angles d'incidence/transmission
    if (_evt.getSource()==cqAngles||_evt.getSource()==cqAnglesTrans) {
      for (int i=0; i<grSelects_.nombre(); i++)
        bdSelects_[i]=((RefondeSymboleData)((GrSymbole)grSelects_.renvoie(i))
            .data())
            .angle;
    }

    // Domaines poreux
    else if (_evt.getSource()==cqDomainePoreux) {
      for (int i=0; i<grSelects_.nombre(); i++)
        bdSelects_[i]=grSelects_.renvoie(i);
    }

    // Points des domaines poreux
    else if (_evt.getSource()==cqDomainePoreuxPts) {
      for (int i=0; i<grSelects_.nombre(); i++)
        bdSelects_[i]=grSelects_.renvoie(i);
    }

    else {
      for (int i=0; i<grSelects_.nombre(); i++)
        bdSelects_[i]=gr2Bd_.get(grSelects_.renvoie(i));
    }

    valideActions();
/*    // Activation ou non des boutons et menus

    // Propriétés de bord
    active=(_evt.getSource()==cqContours&& //    active=(grClass.size()==1 &&
            //            grClass.get((new GrPolyligne()).getClass())!=null &&
            grSelects_.nombre()>0);
    app_.setEnabledForAction("PROPRIETESBORD", active);

    // Propriétés de fond
    active=(_evt.getSource()==cqContours && //    active=(grClass.size()==1 &&
            //            grClass.get((new GrPolyligne()).getClass())!=null &&
            grSelects_.nombre()>0);
    app_.setEnabledForAction("PROPRIETESFOND", active);

    // Angles d'incidence
    active=bHoule &&
           (_evt.getSource()==cqAngles && grSelects_.nombre()>0) ||
           (_evt.getSource()==cqAnglesTrans && grSelects_.nombre()>0);
    app_.setEnabledForAction("MODIFIERANGLE", active);

    // Angles supplémentaires
    active=bHoule && _evt.getSource()==cqAngles && grSelects_.nombre()>0;
    app_.setEnabledForAction("MODIFIERANGSUP", active);

    // Création d'un domaine digue
    active=bHoule && _evt.getSource()==cqContours && grSelects_.nombre()>0;
    app_.setEnabledForAction("CREERDIGUE", active);

    // Suppression d'un domaine digue
    active=bHoule && _evt.getSource()==cqContours && grSelects_.nombre()>0;
    app_.setEnabledForAction("SUPPRIMERDIGUE", active);

    // Modification des propriétés de maillage sur un domaine
    active=(_evt.getSource()==cqContours && grSelects_.nombre()>0);
    app_.setEnabledForAction("MODIFIERPROPMAILLAGE", active);

    // Maillage d'un domaine
    active=(_evt.getSource()==cqContours && grSelects_.nombre()>0);
    app_.setEnabledForAction("MAILLER", active);

    // Suppression d'un maillage
    active=(_evt.getSource()==cqContours && grSelects_.nombre()>0);
    app_.setEnabledForAction("SUPPRIMERMAILLAGE", active);

    // Suppression d'un domaine poreux
    active=bHoule && _evt.getSource()==cqDomainePoreux && grSelects_.nombre()>0;
    btSupPoreux_.setEnabled(active);

    // Modification d'un domaine poreux
    active=bHoule && _evt.getSource()==cqDomainePoreux && grSelects_.nombre()==1;
    btModPoreux_.setEnabled(active);

    // Modification d'un point du plan d'un domaine poreux
    active=bHoule && _evt.getSource()==cqDomainePoreuxPts && grSelects_.nombre()==1;
    btModPoint_.setEnabled(active);*/
  }

  /**
   * Mise a jour des menus et boutons en fonction du contexte.
   */
  private void valideActions() {
    BCalque cq=getArbreCalqueModel().getSelectedCalque();

    boolean active;
    boolean bHoule=
      prj_.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_HOULE;

    // Propriétés de bord
    active=cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("PROPRIETESBORD", active);

    // Propriétés de fond
    active=cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("PROPRIETESFOND", active);

    // Angles d'incidence
    active=bHoule && ((cq==cqAngles && bdSelects_.length>0) ||
                     (cq==cqAnglesTrans && bdSelects_.length>0));
    app_.setEnabledForAction("MODIFIERANGLE", active);

    // Angles supplémentaires
    active=bHoule && cq==cqAngles && bdSelects_.length>0;
    app_.setEnabledForAction("MODIFIERANGSUP", active);

    // Création d'un domaine digue
    active=bHoule && cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("CREERDIGUE", active);

    // Suppression d'un domaine digue
    active=bHoule && cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("SUPPRIMERDIGUE", active);

    // Modification des propriétés de maillage sur un domaine
    active=cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("MODIFIERPROPMAILLAGE", active);

    // Maillage d'un domaine
    active=cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("MAILLER", active);

    // Suppression d'un maillage
    active=cq==cqContours && bdSelects_.length>0;
    app_.setEnabledForAction("SUPPRIMERMAILLAGE", active);

    // Création d'un domaine
    active=bHoule;
    btCreePoreux_.setEnabled(active);

    // Suppression d'un domaine poreux
    active=bHoule && cq==cqDomainePoreux && bdSelects_.length>0;
    btSupPoreux_.setEnabled(active);

    // Modification d'un domaine poreux
    active=bHoule && cq==cqDomainePoreux && bdSelects_.length==1;
    btModPoreux_.setEnabled(active);

    // Modification d'un point du plan d'un domaine poreux
    active=bHoule && cq==cqDomainePoreuxPts && bdSelects_.length==1;
    btModPoint_.setEnabled(active);
  }

  /**
   * Modification du calque sélectionné. Si contours, bouton de sélection par 2
   * points activé.
   */
  public void valueChanged(TreeSelectionEvent _evt) {
    super.valueChanged(_evt);
    if(btSel2Pts_==null) return;
    Object cq= _evt.getPath().getLastPathComponent();
    btSel2Pts_.setEnabled("cqCONTOURS".equals(((BCalque)cq).getName()));
  }
  /**
   * Création d'un domaine poreux
   */
  public void cqFormeI_formeSaisie(FormeEvent _evt) {
    DeRectangle rect= (DeRectangle)_evt.getForme();
    GrPolygone pg= (GrPolygone)rect.getGeometrie();
    RefondeDomainePoreux dm= new RefondeDomainePoreux();
    dm.setPoints(new GrPoint[] { pg.sommet(0), pg.sommet(2)});
    dm.setPointsPlan(new GrPoint[] { pg.sommet(0), pg.sommet(1), pg.sommet(2)});
    RefondeDialogProprietesPoreux diPoreux=
      new RefondeDialogProprietesPoreux(app_.getFrame());
    diPoreux.setProjet(prj_);
    diPoreux.setDomainePoreux(dm);
    diPoreux.show();
    if (diPoreux.reponse == diPoreux.btOk_) {
      cqDomainePoreux.ajoute(dm);
      //      cqDomainePoreuxDir.ajoute(dm.getSymbole());
      GrPoint[] pts= dm.getPointsPlan();
      for (int j= 0; j < pts.length; j++)
        cqDomainePoreuxPts.ajoute(pts[j]);
      prj_.getModeleProprietes().addDomainePoreux(dm);
    }
    getVueCalque().repaint();
    // Désactivation du bouton et gel du calque.
    btCreePoreux_.setSelected(false);
    cqFormeI.setGele(true);
  }
  //----------------------------------------------------------------------------
  // Actions
  //----------------------------------------------------------------------------
  /**
   * Action quand une action spécifique à la fenêtre est déclenchée.
   */
  public void actionPerformed(ActionEvent _evt) {
    if (_evt.getActionCommand().equals("SUPPRIMERPOREUX"))
      cmdSupprimerPoreux();
    else if (_evt.getActionCommand().equals("MODIFIERPOREUX"))
      cmdModifierPoreux();
    else if (_evt.getActionCommand().equals("CREERPOREUX"))
      cmdCreerPoreux();
    else if (_evt.getActionCommand().equals("DEPLACERPOINT"))
      cmdDeplacerPoint();
    else
      super.actionPerformed(_evt);
  }
  /**
   * Suppression d'un domaine poreux
   */
  private void cmdSupprimerPoreux() {
    if (new BuDialogConfirmation(app_,
      app_.getInformationsSoftware(),
      "Suppression du(des) domaine(s) poreux sélectionné(s) ?")
      .activate()
      != JOptionPane.OK_OPTION)
      return;
    for (int i= 0; i < bdSelects_.length; i++) {
      RefondeDomainePoreux dm= (RefondeDomainePoreux)bdSelects_[i];
      cqDomainePoreux.enleve(dm);
      GrPoint[] pts= dm.getPointsPlan();
      for (int j= 0; j < pts.length; j++)
        cqDomainePoreuxPts.enleve(pts[j]);
      prj_.getModeleProprietes().removeDomainePoreux(dm);
    }
    videSelection();
  }
  /**
   * Modification d'un domaine poreux
   */
  private void cmdModifierPoreux() {
    RefondeDomainePoreux dm= (RefondeDomainePoreux)bdSelects_[0];
    RefondeDialogProprietesPoreux diPoreux=
      new RefondeDialogProprietesPoreux(app_.getFrame());
    diPoreux.setProjet(prj_);
    diPoreux.setDomainePoreux(dm);
    diPoreux.show();
    if (diPoreux.reponse == diPoreux.btOk_) {
      prj_.getModeleProprietes().modifie= true;
      getVueCalque().repaint();
    }
  }
  /**
   * Création d'un domaine poreux. On vide juste la selection si necessaire pour
   * ne pas avoir des comportements bizarres, le reste est fait par le calque de
   * saisie de forme.
   */
  private void cmdCreerPoreux() {
    if (bdSelects_ != null && bdSelects_.length != 0)
      videSelection();
  }
  /**
   * Déplacement du point du plan d'un domaine poreux.
   */
  private void cmdDeplacerPoint() {
    GrPoint pt= (GrPoint)bdSelects_[0];
    RefondeDomainePoreux[] dmps= prj_.getModeleProprietes().getDomainesPoreux();
    int idm= 0;
    int ind= 0;
    DMPS : for (idm= 0; idm < dmps.length; idm++) {
      GrPoint[] ptsPlan= dmps[idm].getPointsPlan();
      for (ind= 0; ind < ptsPlan.length; ind++)
        if (ptsPlan[ind] == pt)
          break DMPS;
    }
    final RefondeDomainePoreux dmp= dmps[idm];
    final int ipt= ind;
    RefondePnPointPlan pnPoint= new RefondePnPointPlan();
    pnPoint.setProjet(prj_);
    pnPoint.setZAutomatique(dmp.isZAutomatique(ipt));
    pnPoint.setPoint(pt);
    RefondeDialogPanneau di=
      new RefondeDialogPanneau(
        app_.getFrame(),
        pnPoint,
        "Modification du point",
        RefondeDialogPanneau.OK_CANCEL_APPLY_OPTION) {
      public boolean actionApply() {
        RefondePnPointPlan pn= (RefondePnPointPlan)getPanneauPrincipal();
        GrPoint[] ptsPlan= dmp.getPointsPlan();
        // Controle de validité des valeurs données.
        if (!pn.isOK()) {
          new BuDialogError(
            app_,
            app_.getInformationsSoftware(),
            "Un des paramètres n'a pas un format valide")
            .activate();
          return false;
        }
        ptsPlan[ipt]= pn.getPoint();
        dmp.setZAutomatique(pn.isZAutomatique(), ipt);
        pn.setPoint(ptsPlan[ipt]); // Pour remettre à jour le panneau
        RefondeFilleCalques.this.getVueCalque().repaint();
        return true;
      }
    };
    di.show();
  }
  //----------------------------------------------------------------------------

  /**
   * Retourne les objets sélectionnés
   */
  public Object[] getSelectedObjects() {
    return bdSelects_;
  }

  /**
   * Initialise le calque avec les informations issues du projet
   * @param _projet Projet contenant la géométrie à représenter
   */
  public void initialise(RefondeProjet _projet) {
    gr2Bd_.clear();
    bdSelects_=new Object[0];

    BCalque cqRoot= getVueCalque().getCalque();
    cqRoot.remove(cqDomainePoreux);
    cqRoot.remove(cqDomainePoreuxPts);
    cqRoot.remove(cqAngles);
    cqRoot.remove(cqAnglesTrans);
    cqRoot.getCalqueParNom("cqADMIN").remove(cqSaisieAngle);
    cqRoot.getCalqueParNom("cqADMIN").remove(cqFormeI);
    getVueCalque().repaint();

    cqContours.initialise(_projet);
    cqMaillage.initialise(_projet);
    cqContoursI.initialise(_projet, cqContours);
    prj_= _projet;

    // Spécifique en fonction du modèle choisi.
    boolean bHoule=
      prj_.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_HOULE;

    if (bHoule) {
      cqAngles.initialise(_projet);
      cqAnglesTrans.initialise(_projet);
      cqSaisieAngle.initialise(_projet);

      // Affichage des domaines poreux
      cqDomainePoreux.reinitialise();
      cqDomainePoreuxPts.reinitialise();
      //    cqDomainePoreuxDir.reinitialise();
      RefondeDomainePoreux[] dms=
        _projet.getModeleProprietes().getDomainesPoreux();
      for (int i= 0; i < dms.length; i++) {
        cqDomainePoreux.ajoute(dms[i]);
        //      cqDomainePoreuxDir.ajoute(dms[i].getSymbole());
        GrPoint[] pts= dms[i].getPointsPlan();
        for (int j= 0; j < pts.length; j++)
          cqDomainePoreuxPts.ajoute(pts[j]);
      }

      // Ajout des calques spécifiques
      cqRoot.add(cqDomainePoreux);
      cqRoot.add(cqDomainePoreuxPts);
      cqRoot.add(cqAngles);
      cqRoot.add(cqAnglesTrans);

      cqRoot.getCalqueParNom("cqADMIN").add(cqSaisieAngle);
      cqRoot.getCalqueParNom("cqADMIN").add(cqFormeI);
    }

    getArbreCalqueModel().refresh();
    valideActions();
  }

  /**
   * Affichage d'un calque
   */
  public void afficheCalque(BCalque _calque) {
    BCalque[] cls;
    boolean clVisible;
    BCalque cqRoot= getVueCalque().getCalque();
    // Affichage du calque (s'il n'est déjà affiché)
    cls= cqRoot.getCalques();
    clVisible= false;
    for (int i= 0; i < cls.length; i++)
      if (cls[i] == _calque)
        clVisible= true;
    if (!clVisible) {
      cqRoot.add(_calque);
      getVueCalque().repaint();
      getArbreCalqueModel().refresh();
    }
  }
  /**
   * Effacage d'un calque
   */
  public void effaceCalque(BCalque _calque) {
    BCalque[] cls;
    boolean clVisible;
    BCalque cqRoot= getVueCalque().getCalque();
    // Effacage du calque (s'il est affiché)
    cls= cqRoot.getCalques();
    clVisible= false;
    for (int i= 0; i < cls.length; i++)
      if (cls[i] == _calque)
        clVisible= true;
    if (clVisible) {
      cqRoot.remove(_calque);
      getVueCalque().repaint();
      getArbreCalqueModel().refresh();
    }
  }
  /**
   * Bloque les evenements souris sur tous les calques autres que le calque
   * donné
   */
  public void setLockEvents(BCalqueInteraction _calque) {
    BCalque[] cls= getVueCalque().getCalque().getTousCalques();
    for (int i= 0; i < cls.length; i++) {
      if (cls[i] instanceof BCalqueInteraction) {
        if (!(cls[i] instanceof BCalqueSuiviSourisInteraction))
           ((BCalqueInteraction)cls[i]).setGele(true);
      }
    }
    _calque.setGele(false);
    getArbreCalqueModel().quickRefresh();
  }
}
