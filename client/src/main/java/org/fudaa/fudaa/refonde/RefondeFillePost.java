/*
 * @file         RefondeFillePost.java
 * @creation     1999-08-09
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.awt.Color;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.TreeSelectionEvent;

import com.memoire.bu.BuDesktop;
import com.memoire.bu.BuPopupButton;

import org.fudaa.ebli.calque.BArbreCalque;
import org.fudaa.ebli.calque.BCalque;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.calque.CalqueGuiHelper;
import org.fudaa.ebli.calque.EbliFilleCalques;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillage;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.ressource.EbliResource;

import org.fudaa.fudaa.commun.trace2d.BPaletteCouleurPlage;
import org.fudaa.fudaa.commun.trace2d.BPanneauOptionsIso;
import org.fudaa.fudaa.commun.trace2d.BPanneauPaletteCouleurPlage;
import org.fudaa.fudaa.commun.trace2d.BPanneauSelectedStep;
import org.fudaa.fudaa.commun.trace2d.ZModeleChangeListener;
import org.fudaa.fudaa.commun.trace2d.ZModeleValeur;

/**
 * Une fenetre interne affichant les calques de post traitement sans intéraction
 * possible.
 *
 * @version      $Id: RefondeFillePost.java,v 1.12 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeFillePost
  extends EbliFilleCalques
  implements PropertyChangeListener {

  /** Numéro de fenètre, incrémenté à chaque création. Ce numéro ne sert que
      dans la liste de fenètres, pour les différentier. */
  private static int numero_= 0;
  /** Table des calques résultats. */
  private Vector cqsResultats_= new Vector();
  /** Calque des légendes */
  private BCalqueLegende cqLeg_;
  /** Tools spécifiques de la fenêtre. */
  private JComponent[] btTools_;
  /** panneau de palette de couleur. */
  private BPanneauPaletteCouleurPlage pnPal_;
  /** Panneau d'options de tracé iso. */
  private BPanneauOptionsIso pnIso_;
  /** Panneau de selection du temps courant */
  private BPanneauSelectedStep pnStep_;
  /** Bouton d'accès aux modes propres. Actif que si modèle seiche. */
  private BuPopupButton btStep_=null;
  /** Le super maillage permet de visualiser les résultats. */
  private GrMaillage superMail_= null;
  /** Le calque actif (sélectionné dans l'arbre des calques). */
  private BCalque cqActif_= null;
  /** Projet */
  private RefondeProjet prj_= null;

  /**
   * Création d'une fenètre de gestion des calques sans préoccupation de
   * la vue associée.
   */
  //  public RefondeFillePost(BArbreCalque _ac) {
  //    this(new BVueCalque(), _ac);
  //  }

  /**
   * Création d'une fenetre en précisant la vue associée.
   */
  public RefondeFillePost(BArbreCalque _ac) {
    super();
    addInternalFrameListener(_ac);
    BCalque cqRoot= getVueCalque().getCalque();
    // Suppression du calque par défaut
    BCalque[] cqs= cqRoot.getTousCalques();
    for (int i= 0; i < cqs.length; i++)
      if (cqs[i].getName() == null)
        cqs[i].detruire();
    // Calque légende
    cqLeg_= new BCalqueLegende();
    cqLeg_.setName("cqLegende");
    cqLeg_.setTitle("Légende");
    cqLeg_.setForeground(Color.blue);
    cqRoot.add(cqLeg_);
    // Calque Dessin tracé
    //    BCalqueDessin cqDes=new BCalqueDessin();
    //    cqDes.setName("Dessin");
    //    cqRoot.add(cqDes);
    // Calque dessin interaction
    //    BCalqueDessinInteraction cqDesI=new BCalqueDessinInteraction(cqDes);
    //    cqDesI.setName("D-Interaction");
    //    cqDesI.setGele(true);
    //    cqRoot.add(cqDesI);
    // Tools spécifiques de la fenêtre
    CalqueGuiHelper.CALQUE_OUTILS.addPropertyChangeListener(
    		getArbreCalqueModel());//_ac.getArbreModel());
    CalqueGuiHelper.CALQUE_OUTILS.addPropertyChangeListener(this);
    buildTools();
    getVueCalque().setBackground(Color.white);
    setTitle("Fenêtre post " + (++numero_));
    setFrameIcon(RefondeResource.REFONDE.getIcon("post"));
    setBoutonRepereVisible(false);
    setBoutonsStandardVisible(false);
    setSelectionVisible(false);
    setClosable(true);
  }

  /**
   * Pour donner un nom à la vue calque, nom utilisé dans la fenetre de mise en
   * page pour l'affichage en mode rapide des vues.
   */
  public void setTitle(String _title) {
    super.setTitle(_title);
    getVueCalque().setName(_title);
  }
  /**
   * Initialisation avec le projet.
   */
  public void initialise(RefondeProjet _prj) {
    prj_=_prj;

    // Création du super maillage. Le maillage est censé être optimisé à ce
    // niveau et donc compatible avec des résultats de calcul.
    // Les noeuds sont rangés dans l'ordre de leur numérotation, les éléments
    // dans n'importe quel ordre.

    superMail_= new GrMaillage();
    GrMaillageElement mail= RefondeMaillage.creeSuperMaillage(_prj);
    GrNoeud[] nds= mail.noeuds();
    GrElement[] els= mail.elements();
    Hashtable hInd2Nd= new Hashtable();
    for (int i= 0; i < nds.length; i++)
      hInd2Nd.put(new Integer(((RefondeNoeudData)nds[i].data()).numero-1),nds[i]);

    // Noeuds dans l'ordre de leur numérotation
    for (int i= 0; i < nds.length; i++)
      superMail_.noeuds_.ajoute(((GrNoeud)hInd2Nd.get(new Integer(i))).point_);

    // Connectivités
    for (int i= 0; i < els.length; i++) {
      GrNoeud[] ndsEl= els[i].noeuds_;
      int[] conn= new int[ndsEl.length];
      for (int j= 0; j < ndsEl.length; j++)
        conn[j]= ((RefondeNoeudData)ndsEl[j].data()).numero - 1;
      superMail_.connectivites_.add(conn);
    }

    // Création des calques résultats
    clearResultats();
    if (_prj.hasResultats()) {
      RefondeResultats res=_prj.getResultats();

      // Pas dans le panneau des pas.
      pnStep_.clearSteps();
      for (int i=0; i<res.getNbEtapes(); i++)
        pnStep_.addStep(new Double(res.getInstantEtape(i)));

      // Résultats
      for (int i= 0; i < res.getNbColonnes(); i++) {
        ajouteResultat(res.getNomColonne(i));
      }
    }

    // Création du calque Bathy.
    ajouteBathy();

    // Activation ou non des modes de valeurs propres.
    btStep_.setEnabled(prj_.hasResultats() &&
      prj_.getModeleCalcul().typeModele()==RefondeModeleCalcul.MODELE_SEICHE);
  }

  /**
   * Suppression des calques de résultats
   */
  public void clearResultats() {
    for (int i= 0; i < cqsResultats_.size(); i++)
       ((BCalque)cqsResultats_.get(i)).detruire();
    cqsResultats_.clear();
  }

  /**
   * Ajout de la bathymetrie comme calque de résultat.
   */
  public void ajouteBathy() {
    BCalque cqRoot= getVueCalque().getCalque();

    // Modèle de bathymetrie
    class ModeleValeurBathy implements ZModeleValeur {
      double[] vals_= null;
      public ModeleValeurBathy(double[] _vals) {
        vals_= _vals;
      }
      public int nbValeurs() {
        return vals_.length;
      }
      public double valeur(int i) {
        return vals_[i];
      }
      public double getMin() {
        double r= Double.POSITIVE_INFINITY;
        for (int i= 0; i < vals_.length; i++)
          r= Math.min(r, vals_[i]);
        return r;
      }
      public double getMax() {
        double r= Double.NEGATIVE_INFINITY;
        for (int i= 0; i < vals_.length; i++)
          r= Math.max(r, vals_[i]);
        return r;
      }
      public void addModelChangeListener(ZModeleChangeListener _list) {} // Model doesn't change
      public void removeModelChangeListener(ZModeleChangeListener _list) {} // Model doesn't change
    }

    double[] z= new double[superMail_.noeuds_.nombre()];
    for (int i= 0; i < z.length; i++)
      z[i]= superMail_.noeuds_.renvoie(i).z_;
    //    BCalque cqRoot=getVueCalque().getCalque();
    RefondeCalqueCarte cq= new RefondeCalqueCarte();
    //    cq.setPaletteCouleur(new BPaletteCouleurSimple());
    cq.setMaillage(superMail_);
    cq.setName("cq" + "Bathy initiale");
    cq.setTitle("Bathy initiale");
    cq.setModeleValeurs(new ModeleValeurBathy(z));
    cq.setContour(false);
    cq.setIsolignes(false);
    cq.setSurface(false);
    cq.setVisible(false);
    cq.setLegende(cqLeg_);
    cqRoot.add(cq);
  }

  /**
   * Ajout d'un résultat. Le résultat n'est ajouté que s'il n'existe pas déjà un
   * calque de même nom.
   */
  public void ajouteResultat(String _nom) {
    BCalque cqRoot= getVueCalque().getCalque();
    if (cqRoot.getCalqueParTitre(_nom)!=null) return;

    RefondeModeleVisuResultats mdRes=
      new RefondeModeleVisuResultats(prj_.getResultats(),_nom);
    mdRes.setSelectedStep(pnStep_.getSelectedStep());

    RefondeCalqueCarte cq= new RefondeCalqueCarte();
    //    cq.setPaletteCouleur(new BPaletteCouleurSimple());
    cq.setMaillage(superMail_);
    cq.setName("cq" + _nom);
    cq.setTitle(_nom);
    cq.setModeleValeurs(mdRes);
    mdRes.addModelChangeListener(cq);
    cq.setContour(false);
    cq.setIsolignes(false);
    cq.setSurface(false);
    cq.setVisible(false);
    cq.setLegende(cqLeg_);
    cqsResultats_.add(cq);
    cqRoot.add(cq);
  }

  public String[] getEnabledActions() {
    return super.getEnabledActions();
  }

  public String[] getDisabledActions() {
    return super.getDisabledActions();
  }

  public JMenu[] getSpecificMenus() {
    return super.getSpecificMenus();
  }

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
   * Evènement sur l'arbre (changement d'activation de calque).
   */
  public void valueChanged(TreeSelectionEvent _evt) {
    super.valueChanged(_evt);
    try {
      cqActif_= (BCalque)_evt.getNewLeadSelectionPath().getLastPathComponent();
      //      updateTools(cqActif_);
      //      if (btTools_.length>0 ) btTools_[0].doLayout();
      // Mise a jour du panneau de palette.
      if (cqActif_ instanceof RefondeCalqueCarte) {
        pnPal_.setPalette(((RefondeCalqueCarte)cqActif_).getPalette());
        pnPal_.setBorneMin(((RefondeCalqueCarte)cqActif_).getModeleValeur().getMin());
        pnPal_.setBorneMax(((RefondeCalqueCarte)cqActif_).getModeleValeur().getMax());
      }
      else {
        pnPal_.setPalette(new BPaletteCouleurPlage());
        pnPal_.setBorneMin(pnPal_.getPalette().getMinPalette());
        pnPal_.setBorneMax(pnPal_.getPalette().getMaxPalette());
      }
    } catch (NullPointerException e) {}
  }

  /**
   * Modification des propriétés de calque courant.
   */
  public void layerPropertyChange(PropertyChangeEvent _evt) {
    if (RefondeResource.DEBUG)
      System.out.println("Event : " + _evt.getPropertyName());

    // Mise a jour du calque de tracé.
    if (cqActif_ instanceof RefondeCalqueCarte) {
      if (_evt.getPropertyName().equals("palette")) {
        ((RefondeCalqueCarte)cqActif_).setPalette(pnPal_.getPalette());
      }
      else if (_evt.getPropertyName().equals("optionsIso")) {
        ((RefondeCalqueCarte)cqActif_).setIsolignes(pnIso_.isIsolignesSelected());
        ((RefondeCalqueCarte)cqActif_).setIsosurfaces(pnIso_.isIsosurfacesSelected());
        ((RefondeCalqueCarte)cqActif_).setContour(pnIso_.isMaillageSelected());
      }
      else return;

      cqActif_.repaint();
    }
  }

  /**
   * Modification des propriétés de fenetre.
   */
  public void windowPropertyChange(PropertyChangeEvent _evt) {
    if (_evt.getPropertyName().equals("selectedStep")) {
      for (int i= 0; i < cqsResultats_.size(); i++) {
        if (cqsResultats_.get(i) instanceof RefondeCalqueCarte) {
          RefondeCalqueCarte cq=(RefondeCalqueCarte)cqsResultats_.get(i);
          RefondeModeleVisuResultats mdRes=
            (RefondeModeleVisuResultats)cq.getModeleValeur();
          mdRes.setSelectedStep(pnStep_.getSelectedStep());

          if (cq==cqActif_) {
            pnPal_.setPalette(cq.getPalette());
            pnPal_.setBorneMin(cq.getModeleValeur().getMin());
            pnPal_.setBorneMax(cq.getModeleValeur().getMax());
          }
        }
      }
      getVueCalque().repaint();
    }
  }

  /**
   * Création de la barre d'outils avec les outils de tous les calques.
   */
  private void buildTools() {
    Vector vcps= new Vector();
    BCalque[] cqs= { new BCalqueLegende()};
    for (int i= 0; i < cqs.length; i++) {
      JComponent[] cps= CalqueGuiHelper.CALQUE_OUTILS.getSpecificTools(cqs[i]);
      for (int j= 0; j < cps.length; j++)
        if (!vcps.contains(cps[j])) {
          vcps.addElement(cps[j]);
        }
    }

    // Separator
    vcps.add(null);

    // Panneau de palette de couleurs
    pnPal_= new BPanneauPaletteCouleurPlage();
    pnPal_.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent _evt) {
        layerPropertyChange(_evt);
      }
    });
    pnPal_.setPanneauTailleEnabled(false);

    BuPopupButton btPal=
      new BuPopupButton(EbliResource.EBLI.getString("palette"), pnPal_);
    btPal.setActionCommand("DEFINIRPALETTE");
    btPal.setToolTipText("Palette de couleurs");
    btPal.setIcon(EbliResource.EBLI.getIcon("palettecouleur"));
    vcps.add(btPal);

    // Panneau d'options de tracé d'isocouleurs
    pnIso_= new BPanneauOptionsIso();
    pnIso_.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent _evt) {
        layerPropertyChange(_evt);
      }
    });

    BuPopupButton btIso=
      new BuPopupButton(EbliResource.EBLI.getString("tracé iso"), pnIso_);
    btIso.setActionCommand("OPTIONSISO");
    btIso.setToolTipText("Options de tracé iso");
    btIso.setIcon(EbliResource.EBLI.getIcon("surface"));
    vcps.add(btIso);

    // Panneau de selection de pas
    pnStep_= new BPanneauSelectedStep();
    pnStep_.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent _evt) {
        windowPropertyChange(_evt);
      }
    });

    btStep_= new BuPopupButton("Pas", pnStep_);
    btStep_.setMargin(new Insets(0, 0, 0, 0));
    btStep_.setActionCommand("SELECTIONNERPAS");
    btStep_.setToolTipText("Selection de la période de résonnance");
    btStep_.setIcon(RefondeResource.REFONDE.getIcon("temps"));
    vcps.add(btStep_);

    btTools_= (JComponent[])vcps.toArray(new JComponent[0]);
    for (int i= 0; i < btTools_.length; i++)
      if (btTools_[i] != null)
        btTools_[i].setEnabled(true);
  }
  /**
   * Mise à jour de la barre des outils pour le calque sélectionné.
   * @param _cq Calque sélectionné.
   */
  //  private void updateTools(BCalque _cq) {
  //
  //    // Composants du calque sélectionné
  //    JComponent[] cps=CalqueGuiHelper.CALQUE_OUTILS.getSpecificTools(_cq);
  //
  //    for (int i=0; i<btTools_.length; i++) btTools_[i].setEnabled(false);
  //    for (int i=0; i<cps.length;      i++) cps[i].setEnabled(true);
  //  }
  
  public void propertyChange(PropertyChangeEvent _evt) {
	    if (_evt.getPropertyName().equals("ancre")) {
	    	cqLeg_.setAncre(((Integer)_evt.getNewValue()).intValue());
	    }
	  }
}
