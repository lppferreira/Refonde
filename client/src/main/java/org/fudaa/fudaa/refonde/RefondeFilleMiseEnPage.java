/*
 * @file         RefondeFilleMiseEnPage.java
 * @creation     1999-08-09
 * @modification $Date: 2006-09-19 15:10:23 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.TreeSelectionEvent;

import com.memoire.bu.BuCommonInterface;
import com.memoire.bu.BuDesktop;
import com.memoire.bu.BuPopupButton;
import com.memoire.bu.BuResource;
import com.memoire.bu.BuToggleButton;

import org.fudaa.ebli.calque.BArbreCalque;
import org.fudaa.ebli.calque.BCalque;
import org.fudaa.ebli.calque.BCalqueDeplacementInteraction;
import org.fudaa.ebli.calque.BCalqueDessinInteraction;
import org.fudaa.ebli.calque.BCalqueTailleInteraction;
import org.fudaa.ebli.calque.CalqueGuiHelper;
import org.fudaa.ebli.calque.EbliFilleCalques;
import org.fudaa.ebli.calque.SelectionEvent;
import org.fudaa.ebli.calque.dessin.DeForme;
import org.fudaa.ebli.calque.dessin.DeRectangle;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrVecteur;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.palette.BPaletteForme;
import org.fudaa.ebli.repere.RepereEvent;
import org.fudaa.ebli.ressource.EbliResource;
import org.fudaa.ebli.trace.TraceLigne;
/**
 * Une fen�tre de mise en page des fen�tres de post traitement.
 *
 * @version      $Id: RefondeFilleMiseEnPage.java,v 1.11 2006-09-19 15:10:23 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeFilleMiseEnPage
  extends EbliFilleCalques
  implements PropertyChangeListener {
  /**
   * Calque de limite de page
   */
  private RefondeCalqueDessin cqPage_;
  /**
   * Calque des fen�tres
   */
  private RefondeCalqueFenetres cqFns_;
  /**
   * Calque des l�gendes
   */
  private RefondeCalqueLegendes cqLgs_;
  /**
   * Calque des dessin, bas� sur le mod�le de dessins.
   */
  private RefondeCalqueDessin cqDes_;
  /**
   * Calque d'interaction pour la cr�ation des dessins.
   */
  private BCalqueDessinInteraction cqDesI_;
  /**
   * Calque de d�placement interactif.
   */
  private BCalqueDeplacementInteraction cqDepI_;
  /**
   * Calque de changement de taille interactif.
   */
  private BCalqueTailleInteraction cqTaiI_;
  /**
   * Tools sp�cifiques de la fen�tre.
   */
  private JComponent[] btTools_;
  private BuToggleButton btRetailler_;
  private BuToggleButton btDeplacer_;
  private BuToggleButton btForme_;
  /**
   * D�finition des coordonn�es de la page d'impression en mm. Les coordonn�es
   * donn�es depuis le dialogue d'impression sont en 1/72 d'inches.
   */
  //  private PageFormat pgmm_;
  /**
   * Liste des objets s�lectionn�s
   */
  private VecteurGrContour selectedObjets_;
  /**
   * Application.
   */
  private BuCommonInterface app_;
  /**
   * La valeur d'un inch en mm.
   */
  private static final double UN_INCH_EN_MM= 25.4;
  /**
   * Pour passer des coordonn�es imprimante en coordonn�es millim�tres. Une
   * unit� d'impression repr�sente 1/72ieme d'inch.
   */
  private static final double ImpToMM= UN_INCH_EN_MM / 72;
  /**
   * Cr�ation d'une fen�tre de gestion des calques sans pr�occupation de
   * l'arbre associ�.
   */
  public RefondeFilleMiseEnPage(BuCommonInterface _app, BArbreCalque _ac) {
    super();
    addInternalFrameListener(_ac);
    app_= _app;
    BCalque cqRoot= getVueCalque().getCalque();
    // Suppression du calque par d�faut
    BCalque[] cqs= cqRoot.getTousCalques();
    for (int i= 0; i < cqs.length; i++)
      if (cqs[i].getName() == null)
        cqs[i].detruire();
    // Calque dessin trac�
    cqDes_= new RefondeCalqueDessin();
    cqDes_.setName("cqDESSIN");
    cqDes_.setTitle("Dessin");
    cqRoot.add(cqDes_);
    // Calque dessin interaction
    cqDesI_= new BCalqueDessinInteraction(cqDes_);
    cqDesI_.setName("cqDESSIN-I");
    cqDesI_.setTitle("D-Interaction");
    cqDesI_.setGele(true);
    cqRoot.add(cqDesI_);
    // Calque des l�gendes
    cqLgs_= new RefondeCalqueLegendes();
    cqLgs_.setBackground(new Color(100, 100, 255));
    cqLgs_.setForeground(Color.cyan);
    cqLgs_.setName("cqLEGENDES");
    cqLgs_.setTitle("L�gendes");
    cqRoot.add(cqLgs_);
    // Calque des fen�tres
    cqFns_= new RefondeCalqueFenetres();
    cqFns_.setBackground(new Color(100, 100, 255));
    cqFns_.setForeground(Color.lightGray);
    cqFns_.setRapide(true);
    cqFns_.setName("cqFENETRES");
    cqFns_.setTitle("Fen�tres");
    cqRoot.add(cqFns_);
    // Calque de limite de page
    cqPage_= new RefondeCalqueDessin();
    cqPage_.setName("cqPAGE");
    cqPage_.setTitle("Page");
    cqRoot.add(cqPage_);
    // Calque de d�placement interactif
    cqDepI_= new BCalqueDeplacementInteraction();
    cqDepI_.setName("cqDEPLACEMENT-I");
    cqDepI_.setTitle("D�placement");
    cqDepI_.setGele(true);
    cqRoot.add(cqDepI_);
    // Calque de redimensionnement interactif
    cqTaiI_= new BCalqueTailleInteraction();
    cqTaiI_.setName("cqTAILLE-I");
    cqTaiI_.setTitle("Taille");
    cqTaiI_.setGele(true);
    cqRoot.add(cqTaiI_);
    // Tools sp�cifiques de la fen�tre
    CalqueGuiHelper.CALQUE_OUTILS.addPropertyChangeListener(
      _ac.getArbreModel());
    CalqueGuiHelper.CALQUE_OUTILS.addPropertyChangeListener(this);
    buildTools();
    btRetailler_= new BuToggleButton(BuResource.BU.getIcon("redimensionner"));
    btRetailler_.setActionCommand("REDIMENSIONNER");
    btRetailler_.setToolTipText("Redimensionner les objets s�lectionn�s");
    btDeplacer_=
      new BuToggleButton(RefondeResource.REFONDE.getIcon("deplacer-objet"));
    btDeplacer_.setActionCommand("DEPLACER");
    btDeplacer_.setToolTipText("D�placer les objets s�lectionn�s");
    BPaletteForme pf= new BPaletteForme();
    pf.addPropertyChangeListener("TypeForme", this);
    btForme_= new BuPopupButton(EbliResource.EBLI.getString("Forme"), pf);
    btForme_.setActionCommand("CREERFORME");
    btForme_.setToolTipText("Cr�ation de forme");
    btForme_.setIcon(EbliResource.EBLI.getIcon("polyg"));
    // Association des boutons et des calques d'interaction.
    associeBoutonCalqueInteraction(cqDepI_, btDeplacer_);
    associeBoutonCalqueInteraction(cqDesI_, btForme_);
    associeBoutonCalqueInteraction(cqTaiI_, btRetailler_);
    getVueCalque().setBackground(Color.darkGray);
    setTitle("Mise en page");
    setFrameIcon(BuResource.BU.getIcon("PREVISUALISER"));
    setBoutonRepereVisible(false);
    setBoutonsStandardVisible(false);
    setSelectionVisible(true);
  }
  /**
   * D�finit la mise en page.
   */
  public void setMiseEnPage(RefondeMiseEnPage _mep) {
    DeRectangle r;
    PageFormat pf= _mep.getPageFormat();
    cqPage_.getModele().enleveTout();
    // Cr�ation de la page
    r=
      new DeRectangle(
        new GrPoint(0, 0, 0),
        new GrVecteur(pf.getWidth(), 0, 0),
        new GrVecteur(0, pf.getHeight(), 0));
    r.setTypeTrait(TraceLigne.INVISIBLE);
    r.setBackground(Color.lightGray);
    cqPage_.getModele().ajoute(r);
    // Cr�ation des marges
    r=
      new DeRectangle(
        new GrPoint(pf.getImageableX(), pf.getImageableY(), 0),
        new GrVecteur(pf.getImageableWidth(), 0, 0),
        new GrVecteur(0, pf.getImageableHeight(), 0));
    r.setTypeTrait(TraceLigne.INVISIBLE);
    r.setBackground(Color.white);
    cqPage_.getModele().ajoute(r);
    // Affectation du mod�le de dessin
    cqDes_.setModele(_mep.getModeleDessin());
    // Affectation du mod�le de fenetres
    cqFns_.setModele(_mep.getModeleFenetres());
    // Affectation du mod�le de l�gendes
    cqLgs_.setModele(_mep.getModeleLegendes());
  }
  /**
   * Initialisation avec le projet et le format de page.
   */
  /*  public void initialise(RefondeProjet _prj, PageFormat _pf) {
      DeRectangle r;

      // Conversion des dimensions en 1/72 inches en mm.
      pgmm_=(PageFormat)_pf.clone();
      Paper pp=_pf.getPaper();
      Paper ppmm=new Paper();
      ppmm.setSize(pp.getWidth() *ImpToMM,
                   pp.getHeight()*ImpToMM);

      ppmm.setImageableArea(pp.getImageableX()     *ImpToMM,
                            pp.getImageableY()     *ImpToMM,
                            pp.getImageableWidth() *ImpToMM,
                            pp.getImageableHeight()*ImpToMM);
      pgmm_.setPaper(ppmm);

      // Cr�ation de la page
      r=new DeRectangle(new GrPoint(0,0,0),
                        new GrVecteur(pgmm_.getWidth(),0,0),
                        new GrVecteur(0,pgmm_.getHeight(),0));
      r.setTypeTrait(TraceLigne.INVISIBLE);
      r.setBackground(Color.lightGray);
      cqPage_.ajoute(r);

      // Cr�ation des marges
      r=new DeRectangle(new GrPoint(pgmm_.getImageableX(),pgmm_.getImageableY(),0),
                        new GrVecteur(pgmm_.getImageableWidth(),0,0),
                        new GrVecteur(0,pgmm_.getImageableHeight(),0));
      r.setTypeTrait(TraceLigne.INVISIBLE);
      r.setBackground(Color.white);
      cqPage_.ajoute(r);

      // Affectation du mod�le de dessin
      cqDes_.setModele(_prj.getMiseEnPage().getModeleDessin());

      // Affectation du mod�le de fenetres
      RefondeModeleFenetres mdlFns=_prj.getMiseEnPage().getModeleFenetres();
      mdlFns.setPageFormat(pgmm_);
      cqFns_.setModele(mdlFns);

      // Affectation du mod�le de l�gendes
      RefondeModeleLegendes mdlLgs=_prj.getMiseEnPage().getModeleLegendes();
      mdlLgs.setPageFormat(pgmm_);
      cqLgs_.setModele(mdlLgs);

      // Cr�ation des fen�tres et des l�gendes
      JInternalFrame[] frames=app_.getImplementation().getAllInternalFrames();
      for (int i=frames.length-1; i>=0; i--) {
        if (frames[i] instanceof RefondeFillePost && frames[i]!=this) {
          RefondeFillePost fnPost=(RefondeFillePost)frames[i];
          mdlFns.ajoute(fnPost);

          BCalque[] cqs=fnPost.getVueCalque().getCalque().getTousCalques();
          for (int j=0; j<cqs.length; j++) {
            if (cqs[j] instanceof BCalqueCarte) {
              BCalqueLegende cqLg=((BCalqueCarte)cqs[j]).getLegende();
              if (cqLg!=null) {
                JPanel pnLg=cqLg.getLegende((BCalqueCarte)cqs[j]);
                if (pnLg!=null) mdlLgs.ajoute(pnLg,cqLg,(BCalqueCarte)cqs[j]);
              }
            }
          }
        }
      }
    }*/
  public String[] getEnabledActions() {
    String[] superActs= super.getEnabledActions();
    String[] r= new String[superActs.length + 1];
    r[0]= "IMPRIMER";
    System.arraycopy(superActs, 0, r, 1, superActs.length);
    return r;
  }
  public String[] getDisabledActions() {
    return super.getDisabledActions();
  }
  public JMenu[] getSpecificMenus() {
    return super.getSpecificMenus();
  }
  /**
   * Outils sp�cifiques de la fen�tre.
   */
  public JComponent[] getSpecificTools() {
    JComponent[] cpssuper= super.getSpecificTools();
    // La valeur de DesktopPane n'est valid�e qu'une fois la fen�tre ajout�e
    // au desktop. En th�orie, il faudrait faire le traitement ci dessous
    // une fois la fen�tre ajout�e. En pratique, on le fait dans la m�thode
    // getSpecificTools() appel�e chaque fois que la fen�tre est rendue active.
    // B.M. 20.08.2001
    try {
      for (int i= 0; i < btTools_.length; i++)
         ((BuPopupButton)btTools_[i]).setDesktop((BuDesktop)getDesktopPane());
    } catch (ClassCastException e) {}
    JComponent[] cps= new JComponent[cpssuper.length + btTools_.length + 3];
    System.arraycopy(cpssuper, 0, cps, 0, cpssuper.length);
    System.arraycopy(btTools_, 0, cps, cpssuper.length, btTools_.length);
    // Traitement sp�cial de ces boutons pour ne pas interferer avec updateTools()
    cps[cps.length - 1]= btRetailler_;
    cps[cps.length - 2]= btDeplacer_;
    cps[cps.length - 3]= btForme_;
    ((BuPopupButton)btForme_).setDesktop((BuDesktop)getDesktopPane());
    return cps;
  }
  /**
   * Ev�nement de type s�lection.
   */
  public void selectedObjects(SelectionEvent _evt) {
    selectedObjets_= _evt.getObjects();
    boolean active;
    active= _evt.getSource() == cqDes_ && selectedObjets_.nombre() > 0;
    app_.setEnabledForAction("SUPPRIMER", active);
    cqDepI_.setSelection(selectedObjets_);
    cqTaiI_.setSelection(selectedObjets_);
  }
  /**
   * Ev�nement sur l'arbre (changement d'activation de calque).
   */
  public void valueChanged(TreeSelectionEvent _evt) {
    super.valueChanged(_evt);
    try {
      BCalque cqActif;
      cqActif= (BCalque)_evt.getNewLeadSelectionPath().getLastPathComponent();
      //      if (cqActif instanceof BCalqueAffichage) cqDepI_.setSelection(((BCalqueAffichage)cqActif).contours());
      updateTools(cqActif);
    } catch (NullPointerException e) {}
  }
  /**
   * Une propri�t� � �t� modifi�e ??
   */
  public void propertyChange(PropertyChangeEvent _evt) {
    System.out.println(
      "La property : " + _evt.getPropertyName() + " a ete modifiee");
    if (_evt.getPropertyName().equals("TypeForme")) {
      cqDesI_.setTypeForme(((Integer)_evt.getNewValue()).intValue());
      activeCalqueInteraction(cqDesI_);
    }
  }
  /**
   * Cr�ation de la barre d'outils avec les outils de tous les calques.
   */
  private void buildTools() {
    Vector vcps= new Vector();
    BCalque[] cqs= { new BCalqueDessinInteraction(cqPage_)};
    for (int i= 0; i < cqs.length; i++) {
      JComponent[] cps= CalqueGuiHelper.CALQUE_OUTILS.getSpecificTools(cqs[i]);
      for (int j= 0; j < cps.length; j++)
        if (!vcps.contains(cps[j])) {
          if (((BuPopupButton)cps[j])
            .getActionCommand()
            .equals("DEFINIRFORME"))
            continue; // Programmation degueu mais rapide.
          vcps.addElement(cps[j]);
          cps[j].setEnabled(false);
        }
    }
    btTools_= (JComponent[])vcps.toArray(new JComponent[0]);
  }
  /**
   * Mise � jour de la barre des outils pour le calque s�lectionn�.
   * @param _cq Calque s�lectionn�.
   */
  private void updateTools(BCalque _cq) {
    // Composants du calque s�lectionn�
    JComponent[] cps= CalqueGuiHelper.CALQUE_OUTILS.getSpecificTools(_cq);
    for (int i= 0; i < btTools_.length; i++)
      btTools_[i].setEnabled(false);
    for (int i= 0; i < cps.length; i++)
      cps[i].setEnabled(true);
  }
  /**
   * Suppression des objets s�lectionn�s.
   */
  public void supprimerObjetsSelectionnes() {
    for (int i= 0; i < selectedObjets_.nombre(); i++)
      cqDes_.getModele().enleve((DeForme)selectedObjets_.renvoie(i));
    //    cqDes_.repaint();
    videSelection();
  }
  // >>> Interface Printable ---------------------------------------------------
  /**
   * La m�thode d'impression de la mise en page. Le format de page est celui
   * qui a permis la mise en place de la fenetre de mise en page. On fait donc
   * juste appel � la m�thode paint du composant vueCalque.
   */
  public int print(Graphics _g, PageFormat _pf, int _numPage) {
    if (_numPage != 0)
      return Printable.NO_SUCH_PAGE;
    // On rend invisible le calque de mise en page et le calque des objets
    // s�lectionn�s.
    BCalque cqSl=
      getVueCalque().getCalque().getCalqueParNom("cqSELECTIONAFFICHAGE");
    BCalque cqPg= getVueCalque().getCalque().getCalqueParNom("cqPAGE");
    boolean cqPgVisible= cqPg.isVisible();
    boolean cqSlVisible= cqSl.isVisible();
    if (cqPgVisible)
      cqPg.setVisible(false);
    if (cqSlVisible)
      cqSl.setVisible(false);
    // On enl�ve le caract�re "rapide" du calque de fenetre, si il est visible
    boolean cqFnsVisible = cqFns_.isVisible();
    boolean cqFnsRapide = cqFns_.isRapide();
    if (cqFnsRapide && cqFnsVisible)
    	cqFns_.setRapide(false);
    // Sauvegarde du repere de la fen�tre avant modification pour trac� dans
    // l'espace de l'imprimante.
    double[][] rp= getVueCalque().getRepere();
    double[][] rpSav= new double[rp.length][rp.length != 0 ? rp[0].length : 0];
    int[] sizeSav= new int[] {getVueCalque().getWidth(), getVueCalque().getHeight()};
    for (int i= 0; i < rp.length; i++)
      System.arraycopy(rp[i], 0, rpSav[i], 0, rp[i].length);
    // Modification de l'espace d'affichage pour revenir dans celui de
    // l'imprimante.
    RepereEvent re= new RepereEvent(this, false);
    re.ajouteTransformation(RepereEvent.IDENT, 0, false);
    re.ajouteTransformation(RepereEvent.ZOOM, ImpToMM, RepereEvent.ABSOLU);
    /*re.ajouteTransformation(RepereEvent.TRANS_Y,
    ////     pgmm_.getHeight()/ImpToMM-getVueCalque().getHeight(),RepereEvent.ABSOLU);
    //     _pf.getHeight()/ImpToMM-getVueCalque().getHeight(),RepereEvent.ABSOLU);
    _pf.getHeight() - getVueCalque().getHeight(), RepereEvent.ABSOLU);*/
    getVueCalque().repereModifie(re);
    getVueCalque().setSize((int) _pf.getWidth(), (int) _pf.getHeight());
    // Impression
    getVueCalque().print(_g);
    // Restauration de l'espace d'affichage.
    getVueCalque().setSize(sizeSav[0], sizeSav[1]);
    getVueCalque().setRepere(rpSav);
    // On remet en l'�tat de visibilit� les calques de mise page et des objets
    // s�lectionn�s
    if (cqPgVisible)
      cqPg.setVisible(true);
    if (cqSlVisible)
      cqSl.setVisible(true);
    if (cqFnsRapide && cqFnsVisible)
    	cqFns_.setRapide(true); System.out.println("Passage en mode rapide");
    return Printable.PAGE_EXISTS;
  }
  // <<< Interface Printable ---------------------------------------------------
}
