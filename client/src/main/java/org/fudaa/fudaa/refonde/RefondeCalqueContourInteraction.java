/*
 * @file         BCalqueSelectionInteraction.java
 * @creation     1998-09-02
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.ebli.calque.BCalque;
import org.fudaa.ebli.calque.BCalqueInteraction;
import org.fudaa.ebli.calque.SelectionEvent;
import org.fudaa.ebli.calque.SelectionListener;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.geometrie.VecteurGrPoint;
import org.fudaa.ebli.trace.TraceGeometrie;
import org.fudaa.ebli.trace.TraceLigne;
/**
 * Un calque de sélection de polylignes sur un contour.<p>
 * Ces objets sont ajoutés au calque pour être reconnus. La fin de sélection
 * provoque la diffusion d'un évènement <i>SelectionEvent</i>.<p>
 * La sélection peut se faire en mode ponctuel, rectangle ou polygonal.<p>
 * Il est possible d'agir sur une sélection précédente en effectuant agissant
 * sur des boutons modificateurs pendant la sélection :<p>
 * <pre>
 * SHIFT      : Pour ajouter à la sélection précédente.
 * CTRL       : Pour supprimer de la sélection précédente.
 * SHIFT+CTRL : Mode XOR.
 * </pre>
 * <p>
 * Les points saisis sont gérés en coordonnées réelles pour pouvoir changer le
 * repère de la vue pendant la saisie.
 *
 * @version      $Revision: 1.10 $ $Date: 2006-09-08 16:04:26 $ by $Author: opasteur $
 * @author       Bertrand Marchand
 */
public class RefondeCalqueContourInteraction
  extends BCalqueInteraction
  implements MouseListener, MouseMotionListener {
  // Modes de sélection
  /**
   * Mode de sélection par simple clic.
   */
  public final static int PONCTUEL= 0;
  /**
   * Mode de sélection à l'aide d'un rectangle. Les objets entièrement dans le
   * rectangle sont sélectionnés.
   */
  public final static int RECTANGLE= 1;
  /**
   * Mode de sélection par un polygone. Les objets entièrement dans le rectangle
   * sont sélectionnés.
   */
  public final static int POLYGONE= 2;
  // Modificateurs de sélection
  private final static int REP= 0; // Remplacement de la sélection
  private final static int ADD= 1; // Ajout à la sélection
  private final static int DEL= 2; // Suppression de la sélection
  //private final static int XOR= 3; // Mode XOR
  //private int dstSel_; // Distance de tolérance pour la sélection en pixels
  //private int mode_;                        // Mode de sélection courant
  private boolean enCours_; // Sélection en cours
  //  private ListeGrPoint listePoints_;        // Liste des points saisis en espace réel.
  private VecteurGrPoint listePoints_;
  // Liste des points saisis en espace réel.
  //private Graphics g; // contexte graphique
  private TraceGeometrie tg_;
  // Trace dans le contexte graphique en espace réel.
  //private GrPoint ptExt_;                   // Point d'extrémité du tracé courant
  //private GrPoint ptOrig_;                  // Point d'origine du tracé courant
  private GrPolyligne plHelper_;
  // Polyligne d'aide pour le tracé. Elle contient les points saisis.
  private int modificateur_; // Modificateur de sélection
  private int typeTraitCourant_; // Type de trait pour la sélection en cours
  private int lgTrait_; // Largeur de trait pour la sélection en cours
  private Hashtable objets_;
  // Objets sélectionnables (connus du calque), par calque. En fait 1 seul calque.
  private RefondeContour ctCourant_; // Coutour courant.
  private int iplDeb_; // Indice de polyligne de debut saisie.
  private int iplFin_; // Indice de polyligne de fin saisie.
  //private Hashtable selects_; // Objets sélectionnés
  private Hashtable selectionListeners_; // Listeners de sélection
  private RefondeCalqueContour cqContours_; // Calque des contours.
  /**
   * Création d'un calque de sélection sans objets sélectionnables
   */
  public RefondeCalqueContourInteraction() {
    super();
    setDestructible(false);
    objets_= new Hashtable();
    //selects_= new Hashtable();
    setTypeTrait(TraceLigne.LISSE);
    setLargeurTrait(3);
    //    setModeSelection(RECTANGLE);
    selectionListeners_= new Hashtable();
    //    listePoints_ = new ListeGrPoint();
    listePoints_= new VecteurGrPoint();
    plHelper_= new GrPolyligne();
    plHelper_.sommets_= listePoints_;
    enCours_= false;
//    dstSel_= 4;
  }
  /**
   * Création d'un calque de sélection avec une liste d'objets sélectionnables
   * @deprecated
   */
  //  public BCalqueContourInteraction(GrContour[] _objets) {
  //    this();
  //    this.add(_objets,this);
  //  }
  /**
   * Création d'un calque de sélection avec une liste d'objets sélectionnables
   * @deprecated
   */
  //  public BCalqueContourInteraction(Vector _objets) {
  //    this();
  //    this.add(_objets,this);
  //  }
  /**
   * Création d'un calque de sélection avec un calque d'affichage associé.
   * @deprecated
   */
  //  public BCalqueContourInteraction(BCalqueAffichage _calque) {
  //    this();
  //    this.add(_calque,this);
  //  }
  /**
   * Dessin de l'icone.
   * @param _c composant dont l'icone peut deriver des proprietes
   * (couleur, ...). Ce parametre peut etre <I>null</I>. Il est ignore ici.
   * @param _g le graphics sur lequel dessiner l'icone
   * @param _x lieu cible de l'icone (x)
   * @param _y lieu cible de l'icone (y)
   */
  public void paintIcon(Component _c, Graphics _g, int _x, int _y) {
    super.paintIcon(_c, _g, _x, _y);
    int w= getIconWidth() - 1;
    int h= getIconHeight() - 1;
    Image i;
    //    if      (mode_==PONCTUEL)  i=EbliResource.EBLI.getImage("fleche");
    //    else if (mode_==RECTANGLE) i=EbliResource.EBLI.getImage("rectangle");
    //    else                       i=EbliResource.EBLI.getImage("polygone");
    i= RefondeResource.REFONDE.getImage("contour");
    if (i != null) {
      int wi= i.getWidth(this);
      int hi= i.getHeight(this);
      int r= Math.max(wi / w, hi / h);
      //DEBUG FRED
      if(r!=0)
      _g.drawImage(i, _x + 1, _y + 1, wi / r - 1, hi / r - 1, _c);
    }
  }
  /**
   * Affectation de la propriete <I>typeTrait</I>.
   */
  public void setTypeTrait(int _typeTrait) {
    typeTraitCourant_= _typeTrait;
  }
  /**
   * Accesseur de la propriete <I>typeTrait</I>. Elle fixe le type de trait
   * (pointille, tirete, ...) en prenant ses valeurs dans les champs
   * statiques de <I>TraceLigne</I>.
   * @see org.fudaa.ebli.trace.TraceLigne
   */
  public int getTypeTrait() {
    return typeTraitCourant_;
  }
  /**
   * Definition de la largeur de trait pour la représentation des polylignes
   * en cours de sélection.
   *
   * @param _lg Largeur du trait.
   */
  public void setLargeurTrait(int _lg) {
    lgTrait_= _lg;
  }
  /**
   * Retourne la largeur de trait pour les polylignes en cours de sélection.
   * @return Largeur de trait.
   */
  public int getLargeurTrait() {
    return lgTrait_;
  }
  /**
   * Affectation du mode de sélection (<I>PONCTUEL, RECTANGLE, POLYGONE</I>)
   */
  //  public void setModeSelection(int _mode) { mode_ = _mode; }
  /**
   * Retour du mode de sélection courant (<I>PONCTUEL, RECTANGLE, POLYGONE</I>)
   */
  //  public int getModeSelection() { return mode_; }
  /**
   * Ajout d'un objet contour à la liste des objets dont les polylignes sont
   * sélectionnables
   */
  public void add(RefondeContour _objet) {
    add(_objet, this);
  }
  /**
   * Ajout d'un objet à la liste des objets sélectionnables
   */
  private void add(RefondeContour _objet, BCalque _calque) {
    Vector objetsCalque= getListeObjets(_calque);
    objetsCalque.add(_objet);
  }
  /**
   * Ajout d'un tableau d'objets à la liste des objets sélectionnables
   * @deprecated
   */
  public void add(RefondeContour[] _objets) {
    add(_objets, this);
  }
  /**
   * Ajout d'un tableau d'objets à la liste des objets sélectionnables
   */
  private void add(RefondeContour[] _objets, BCalque _calque) {
    Vector objetsCalque= getListeObjets(_calque);
    for (int i= 0; i < _objets.length; i++)
      objetsCalque.add(_objets[i]);
  }
  /**
   * Suppression d'un objet de la liste des objets sélectionnables
   */
  public void remove(RefondeContour _objet) {
    remove(_objet, this);
  }
  /**
   * Suppression d'un objets de la liste des objets sélectionnables
   */
  private void remove(RefondeContour _objet, BCalque _calque) {
    Vector objetsCalque= (Vector)objets_.get(_calque);
    if (objetsCalque == null)
      return;
    objetsCalque.remove(_objet);
    if (objetsCalque.isEmpty())
      objets_.remove(objetsCalque);
  }
  /**
   * Suppression d'un tableau d'objets de la liste des objets sélectionnables
   * @deprecated
   */
  public void remove(RefondeContour[] _objets) {
    remove(_objets, this);
  }
  /**
   * Suppression d'un tableau d'objets de la liste des objets sélectionnables
   */
  private void remove(RefondeContour[] _objets, BCalque _calque) {
    Vector objetsCalque= (Vector)objets_.get(_calque);
    if (objetsCalque == null)
      return;
    for (int i= 0; i < _objets.length; i++) {
      objetsCalque.remove(_objets[i]);
      if (objetsCalque.isEmpty()) {
        objets_.remove(objetsCalque);
        break;
      }
    }
  }
  /**
   * Suppression du calque de tous les objets sélectionnables
   */
  public void removeAll() {
    objets_.clear();
  }
  /**
   * Initialisation du calque avec le projet.
   */
  public void initialise(RefondeProjet _prj, RefondeCalqueContour _cq) {
    removeAll();
    Vector vcts= _prj.getGeometrie().getContours();
    for (int i= 0; i < vcts.size(); i++)
      add((RefondeContour)vcts.get(i));
    cqContours_= _cq;
  }
  /**
   * Ajout d'un auditeur à l'évenement <I>SelectionEvent</I>
   */
  public synchronized void addSelectionListener(SelectionListener _listener) {
    selectionListeners_.put(_listener, _listener);
  }
  /**
   * Suppression d'un auditeur à l'évenement <I>SelectionEvent</I>
   */
  public synchronized void removeSelectionListener(SelectionListener _listener) {
    selectionListeners_.remove(_listener);
  }
  /**
   * Notification aux auditeurs qu'un évenement <I>SelectionEvent</I> s'est produit
   */
  public synchronized void fireSelectionEvent(SelectionEvent _evt) {
    for (Enumeration e= selectionListeners_.keys(); e.hasMoreElements();) {
      ((SelectionListener)e.nextElement()).selectedObjects(_evt);
    }
  }
  /**
   * Suppression par programmation des objets sélectionnés. A le même effet
   * qu'une sélection nulle à la souris.
   */
  //  public void videSelection() {
  //    ListeGrContour selectsCalque;
  //    BCalque        calque;
  //
  //    for (Enumeration e=selects_.keys(); e.hasMoreElements(); ) {
  //      SelectionEvent  evt;
  //
  //      calque=(BCalque)e.nextElement();
  //      selectsCalque=(ListeGrContour)selects_.get(calque);
  //      selectsCalque.vide();
  //
  //      if (e.hasMoreElements())
  //       evt=new SelectionEvent(calque,selectsCalque,SelectionEvent.HAS_NEXT);
  //      else
  //       evt=new SelectionEvent(calque,selectsCalque,SelectionEvent.LAST);
  //      fireSelectionEvent(evt);
  //    }
  //  }
  /**
   * Retourne la liste des objets sélectionnables pour ce calque
   */
  private Vector getListeObjets(BCalque _calque) {
    Vector r;
    if ((r= (Vector)objets_.get(_calque)) == null) {
      r= new Vector();
      objets_.put(_calque, r);
    }
    return r;
  }
  // ---------------------------------------------------------------------------
  // Génération de la liste des objets sélectionnés
  // ---------------------------------------------------------------------------
  //  private void formeSaisie() {
  //    GrMorphisme versEcran = this.getVersEcran();
  //    GrContour objet;
  //    GrPoint[] points;
  //    GrPoint pointEcran;
  //    BCalque        calque;
  //    ListeGrContour objetsCalque;
  //    ListeGrContour selectsCalque;
  //
  //    Polygon polygon=null;
  //    Point   pt=null;
  //
  //    // Mode ponctuel
  //    if (mode_==PONCTUEL) {
  //      pt=listePoints_.renvoie(0).applique(versEcran).point();
  //    }
  //
  //    // Mode rectangle ou polygone
  //    else {
  //      polygon=plHelper_.applique(versEcran).toGrPolygone().polygon();
  //    }
  //
  //    if (modificateur_==REP) selects_.clear();
  //
  //    NEXT_CALQUE:
  //    for (Enumeration e=objets_.keys(); e.hasMoreElements(); ) {
  //      calque=(BCalque)e.nextElement();
  //      objetsCalque=(Vector)objets_.get(calque);
  //
  //      if      (modificateur_==REP) {
  //        selectsCalque=new ListeGrContour();
  //        selects_.put(calque,selectsCalque);
  //      }
  //      else if (modificateur_==ADD) {
  //        if ((selectsCalque=(ListeGrContour)selects_.get(calque))==null) {
  //          selectsCalque=new ListeGrContour();
  //          selects_.put(calque,selectsCalque);
  //        }
  //      }
  //      else if (modificateur_==DEL) {
  //        if ((selectsCalque=(ListeGrContour)selects_.get(calque))==null)
  //         continue NEXT_CALQUE;
  //      }
  //      else  {
  //        if ((selectsCalque=(ListeGrContour)selects_.get(calque))==null) {
  //          selectsCalque=new ListeGrContour();
  //          selects_.put(calque,selectsCalque);
  //        }
  //      }
  //
  //      NEXT_OBJECT:
  //      for (int i=0; i<objetsCalque.nombre(); i++) {
  //        objet = objetsCalque.renvoie(i);
  //
  //        // Mode ponctuel
  //        if (mode_==PONCTUEL) {
  //          if (!objet.estSelectionne(versEcran,dstSel_,pt))
  //           continue NEXT_OBJECT;
  //        }
  //
  //        // Modes rectangle et polygone
  //        else {
  //          points = objet.contour();
  //
  //          // Si l'objet ne possède aucun point => Hors sélection
  //          if (points.length==0) continue;
  //
  //          // Controle que tous les points sont dans le polygone de sélection
  //          for (int j=0; j<points.length; j++) {
  //            pointEcran = points[j].applique(versEcran);
  //            if (!polygon.contains((int) pointEcran.x, (int) pointEcran.y))
  //             continue NEXT_OBJECT;
  //          }
  //        }
  //
  //        // Ajout/suppression de l'objet dans la liste des objets sélectionnés
  //        if      (modificateur_==REP) {
  //          selectsCalque.ajoute(objet);
  //        }
  //        else if (modificateur_==ADD) {
  //          if (!selectsCalque.contient(objet)) selectsCalque.ajoute(objet);
  //        }
  //        else if (modificateur_==DEL) {
  //           selectsCalque.enleve(objet);
  //        }
  //        else {
  //          if (selectsCalque.contient(objet)) selectsCalque.enleve(objet);
  //          else                               selectsCalque.ajoute(objet);
  //        }
  //
  //        if (mode_==PONCTUEL) break NEXT_OBJECT;  // Pour le mode ponctuel, sélection d'un seul objet.
  //      }
  //
  //      // Si la liste est vide, suppression
  //      if (selectsCalque.estVide()) selects_.remove(selectsCalque);
  //    }
  //
  //    for (Enumeration e=selects_.keys(); e.hasMoreElements(); ) {
  //      SelectionEvent  evt;
  //
  //      calque=(BCalque)e.nextElement();
  //      selectsCalque=(ListeGrContour)selects_.get(calque);
  //
  //      if (e.hasMoreElements())
  //       evt=new SelectionEvent(calque,selectsCalque,SelectionEvent.HAS_NEXT);
  //      else
  //       evt=new SelectionEvent(calque,selectsCalque,SelectionEvent.LAST);
  //      fireSelectionEvent(evt);
  //    }
  //  }
  // ---------------------------------------------------------------------------
  // Appelé avant chaque tracé pour définir le contexte graphique
  // ---------------------------------------------------------------------------
  private void initTrace(Graphics2D _g) {
    _g.setXORMode(Color.white);
    tg_= new TraceGeometrie( getVersEcran());
    tg_.setForeground(Color.red);
    tg_.setTypeTrait(typeTraitCourant_, lgTrait_);
  }
  // >>> Interface MouseListener  ----------------------------------------------
  /**
   * Début ou fin de sélection.
   *
   * Début : On recherche la polyligne la plus proche pour tous les contours.
   * Fin : On envoie un évenement qui contient la liste des polylignes
   * sélectionnées.
   */
  public void mouseClicked(MouseEvent _evt) {
    if (isGele())
      return;
    modificateur_= REP;
    if (_evt.isShiftDown())
      modificateur_ |= ADD;
    if (_evt.isControlDown())
      modificateur_ |= DEL;
    GrPoint ptSo=
      new GrPoint(_evt.getX(), _evt.getY(), 0.).applique(
        getVersReel());
    // Fin de sélection => Evènement d'envoi des polylignes sélectionnées
    if (enCours_) {
      RefondePolyligne[] pls= ctCourant_.getPolylignes();
      VecteurGrContour sel= new VecteurGrContour();
      for (int i= iplDeb_;; i= (i + 1) % pls.length) {
        sel.ajoute(pls[i]);
        if (i == iplFin_)
          break;
      }
      // Effacement de l'ancien tracé
      Graphics2D _g=(Graphics2D)getGraphics();
      initTrace(_g);
      for (int i= iplDeb_;; i= (i + 1) % pls.length) {
        tg_.dessinePolyligne(_g,pls[i], false);
        if (i == iplFin_)
          break;
      }
      SelectionEvent evt=
        new SelectionEvent(cqContours_, sel, SelectionEvent.LAST);
      fireSelectionEvent(evt);
      enCours_= false;
    }
    // Recherche de la polyligne la plus proche et tracé.
    else {
      Vector vctrs= getListeObjets(this);
      RefondePolyligne[] pls;
      double dstMin= Double.POSITIVE_INFINITY;
      for (int i= 0; i < vctrs.size(); i++) {
        RefondeContour ct= (RefondeContour)vctrs.get(i);
        pls= ct.getPolylignes();
        for (int j= 0; j < pls.length; j++) {
          double dst= pls[j].distanceXY(ptSo);
          if (dst < dstMin) {
            dstMin= dst;
            ctCourant_= ct;
            iplDeb_= j;
          }
        }
      }
      iplFin_= iplDeb_;
      enCours_= true;
      pls= ctCourant_.getPolylignes();
      Graphics2D _g=(Graphics2D)getGraphics();
      initTrace(_g);
      tg_.dessinePolyligne(_g,pls[iplFin_], false);
    }
  }
  /**
   * <I>Sans objet</I>
   */
  public void mousePressed(MouseEvent _evt) {}
  /**
   * <I>Sans objet</I>
   */
  public void mouseReleased(MouseEvent _evt) {}
  /**
   * Sortie de la fenêtre : On efface les polyligne en cours de sélection, pour
   * accéder par exemple à la navigation.
   */
  public void mouseEntered(MouseEvent _evt) {
    if (isGele())
      return;
    if (enCours_) {
      RefondePolyligne[] pls= ctCourant_.getPolylignes();
      // Tracé des polyligne en cours de sélection.
      Graphics2D _g=(Graphics2D)getGraphics();
      initTrace(_g);
      for (int i= iplDeb_;; i= (i + 1) % pls.length) {
        tg_.dessinePolyligne(_g,pls[i], false);
        if (i == iplFin_)
          break;
      }
    }
  }
  /**
   * Entrée dans la fenêtre : On affiche les polylignes en cours de sélection,
   * après avoir accédé par exemple à la navigation.
   */
  public void mouseExited(MouseEvent _evt) {
    if (isGele())
      return;
    if (enCours_) {
      RefondePolyligne[] pls= ctCourant_.getPolylignes();
      // Effacement des polylignes en cours de sélection.
      Graphics2D _g=(Graphics2D)getGraphics();
      initTrace(_g);
      for (int i= iplDeb_;; i= (i + 1) % pls.length) {
        tg_.dessinePolyligne(_g,pls[i], false);
        if (i == iplFin_)
          break;
      }
    }
  }
  // <<< Interface MouseListener  ----------------------------------------------
  // >>> Interface MouseMotionListener  ----------------------------------------
  /**
   * Sélection en cours.
   * <p>
   * On recherche la poyligne la plus proche pour le contour courant et tracé
   * de l'indice de la polyligne de départ à la polyligne trouvée.
   */
  public void mouseMoved(MouseEvent _evt) {
    if (isGele())
      return;
    modificateur_= REP;
    if (_evt.isShiftDown())
      modificateur_ |= ADD;
    if (_evt.isControlDown())
      modificateur_ |= DEL;
    GrPoint ptSo=
      new GrPoint(_evt.getX(), _evt.getY(), 0.).applique(
        getVersReel());
    if (enCours_) {
      RefondePolyligne[] pls= ctCourant_.getPolylignes();
      int iplCur= iplFin_;
      double dstMin= Double.POSITIVE_INFINITY;
      for (int i= 0; i < pls.length; i++) {
        double dst= pls[i].distanceXY(ptSo);
        if (dst < dstMin) {
          dstMin= dst;
          iplCur= i;
        }
      }
      if (iplCur != iplFin_) {
        Graphics2D _g=(Graphics2D)getGraphics();
        initTrace(_g);
        // Tracé de iplFin_+1 à iplCur
        if (iplFin_ >= iplDeb_
          && (iplCur > iplFin_ || iplCur < iplDeb_)
          || iplFin_ <= iplDeb_
          && (iplCur > iplFin_ && iplCur < iplDeb_)) {
          for (int i= (iplFin_ + 1) % pls.length;; i= (i + 1) % pls.length) {
            tg_.dessinePolyligne(_g,pls[i], false);
            if (i == iplCur)
              break;
          }
        }
        // Tracé de iplCur+1 à iplFin_
        else {
          for (int i= (iplCur + 1) % pls.length;; i= (i + 1) % pls.length) {
            tg_.dessinePolyligne(_g,pls[i], false);
            if (i == iplFin_)
              break;
          }
        }
        iplFin_= iplCur;
        //        // Effacement de l'ancien tracé
        //        for (int i=iplDeb_; ; i=(i+1)%pls.length ) {
        //          tg_.dessinePolyligne(pls[i],false);
        //          if (i==iplFin_) break;
        //        }
        //        // Nouveau tracé
        //        iplFin_=iplCur;
        //        for (int i=iplDeb_; ; i=(i+1)%pls.length ) {
        //          tg_.dessinePolyligne(pls[i],false);
        //          if (i==iplFin_) break;
        //        }
      }
    }
  }
  /**
   * <I>Sans objet</I>
   */
  public void mouseDragged(MouseEvent _evt) {}
  // >>> Interface MouseMotionListener  ----------------------------------------
}
