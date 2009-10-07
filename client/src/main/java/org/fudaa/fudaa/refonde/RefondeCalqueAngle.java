/*
 * @file         RefondeCalqueAngle.java
 * @creation     1999-08-10
 * @modification $Date: 2006-09-19 15:10:21 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.ebli.calque.BCalqueAffichage;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.geometrie.GrSymbole;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.geometrie.VecteurGrPoint;
import org.fudaa.ebli.geometrie.VecteurGrPolyligne;
import org.fudaa.ebli.geometrie.VecteurGrSymbole;
import org.fudaa.ebli.trace.TraceGeometrie;
import org.fudaa.ebli.trace.TraceLigne;
import org.fudaa.ebli.trace.TracePoint;
/**
 * Un calque de tracé des angles d'incidence.
 *
 * @version      $Id: RefondeCalqueAngle.java,v 1.7 2006-09-19 15:10:21 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeCalqueAngle extends BCalqueAffichage {
  private RefondeProjet projet_;
  private VecteurGrSymbole symboles_;
  private VecteurGrPolyligne lignes_;
  private VecteurGrPoint points_;
  /**
   * Création d'un calque d'affichage des angles d'incidence.
   */
  public RefondeCalqueAngle() {
    super();
    reinitialise();
    setDestructible(false);
    setForeground(Color.magenta);
  }
  /**
   * Paint de l'icon du calque des angles d'incidence.
   */
  public void paintIcon(Component _c, Graphics _g, int _x, int _y) {
    Color fg= getForeground();
    if (isAttenue())
      fg= attenueCouleur(fg);
    int w= getIconWidth();
    int h= getIconHeight();
    _g.setColor(Color.white);
    _g.fillRect(_x, _y, w, h);
    _g.setColor(isVisible() ? Color.black : Color.lightGray);
    _g.drawRect(_x, _y, w, h);
    TraceGeometrie tg= new TraceGeometrie( GrMorphisme.identite());
    tg.setForeground(fg);
    GrSymbole s;
    s=
      new GrSymbole(
        GrSymbole.FLECHE_LIEE,
        new GrPoint(_x + 5., _y + 5., 0.),
        0.,
        Math.min(w, h) - 5.);
    tg.dessineSymbole(  (Graphics2D) _g,s, false, isRapide());
    s=
      new GrSymbole(
        GrSymbole.FLECHE_DIFFRACTEE,
        new GrPoint(_x + w - 5., _y + 5., 0.),
        0.,
        Math.min(w, h) - 5.);
    tg.dessineSymbole( (Graphics2D) _g,s, false, isRapide());
  }
  /**
   * Affichage des angles.
   */
  public void paintComponent(Graphics _g) {
    if (isRapide())
      return;
    GrMorphisme versEcran= getVersEcran();
    TraceGeometrie tg= new TraceGeometrie( versEcran);
    Color fg= getForeground();
    if (isAttenue())
      fg= attenueCouleur(fg);
    tg.setForeground(fg);
    // Limites des segments
    tg.setTaillePoint(3);
    tg.setTypePoint(TracePoint.DISQUE);
    for (int i= 0; i < points_.nombre(); i++) {
      tg.dessinePoint( (Graphics2D) _g,points_.renvoie(i), isRapide());
    }
    // Lignes reliant le point de diffraction aux deux points du segment
    tg.setTypeTrait(TraceLigne.LISSE);
    for (int i= 0; i < lignes_.nombre(); i++) {
      tg.dessinePolyligne( (Graphics2D) _g,lignes_.renvoie(i), isRapide());
    }
    // Symboles
    tg.setTypeTrait(TraceLigne.LISSE);
    for (int i= 0; i < symboles_.nombre(); i++) {
      tg.dessineSymbole( (Graphics2D) _g,symboles_.renvoie(i), false, isRapide());
    }
    super.paintComponent(_g);
  }
  /**
   * Reinitilisation du calque.
   */
  public void reinitialise() {
    //    super.reinitialise();
    symboles_= new VecteurGrSymbole();
    lignes_= new VecteurGrPolyligne();
    points_= new VecteurGrPoint();
  }
  /**
   * Initialisation du calque avec le projet.
   */
  public void initialise(RefondeProjet _projet) {
    projet_= _projet;
    GrPolyligne[] plCntrs; // Polylignes totales des contours
    Vector cntrs= _projet.getGeometrie().frontieres();
    Vector cntr;
    GrPolyligne pl= null;
    GrPoint pt;
    Vector[] ais;
    RefondeAngle ai;
    RefondeAngle[] aisDigue;
    // Nettoyage du calque
    reinitialise();
    //--------------------------------------------------------------------------
    // Angles d'incidence sur les frontieres
    //--------------------------------------------------------------------------
    // Création des polylignes contours
    plCntrs= new GrPolyligne[cntrs.size()];
    for (int i= 0; i < cntrs.size(); i++) {
      cntr= (Vector)cntrs.get(i);
      plCntrs[i]= new GrPolyligne();
      for (int j= 0; j < cntr.size(); j++) {
        pl= (GrPolyligne)cntr.get(j);
        pt= pl.sommet(0);
        plCntrs[i].sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
      }
      pt= pl.sommet(1);
      plCntrs[i].sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
    }
    ais= projet_.getModeleCalcul().angles();
    for (int i= 0; i < ais.length; i++) {
      for (int j= 0; j < ais[i].size(); j++) {
        ai= (RefondeAngle)ais[i].elementAt(j);
        ajouteAngle(ai, plCntrs[i]);
      }
    }
    //--------------------------------------------------------------------------
    // Angles d'incidence sur les domaines digues
    //--------------------------------------------------------------------------
    aisDigue= projet_.getModeleCalcul().getAnglesIncidenceDigues();
    //    for (int i=0; i<aisDigue.length; i++) {
    //      pl=projet_.getModeleCalcul().getPolyligneAI(aisDigue[i]);
    //      ajouteAngle(aisDigue[i],pl);
    //    }
    // B.M. 06/09/2000 Orientation des digues de manière à ce que les repères
    // des angles d'incidence soient toujours orientés vers l'intérieur de la
    // digue
    Vector dms= projet_.getGeometrie().getDomaines();
    for (int i= 0; i < dms.size(); i++) {
      RefondeDomaine dm;
      RefondeContour ct;
      RefondeContour ctOr;
      RefondePolyligne[] pls;
      RefondePolyligne[] plsOr;
      Hashtable pl2PlOr;
      RefondePolyligne plOr;
      if (!((dm= (RefondeDomaine)dms.get(i)) instanceof RefondeDomaineDigue))
        continue;
      if (dm.getGroupeProprietes().getType()
        != RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
        continue;
      pl2PlOr= new Hashtable();
      ct= dm.getContours()[0];
      ctOr= ct.copie();
      pls= ct.getPolylignes();
      plsOr= ctOr.getPolylignes();
      for (int j= 0; j < pls.length; j++)
        pl2PlOr.put(pls[j], plsOr[j]);
      ctOr.orienteTrigo(false);
      for (int j= 0; j < aisDigue.length; j++) {
        pl= projet_.getModeleCalcul().getPolyligneAI(aisDigue[j]);
        plOr= (RefondePolyligne)pl2PlOr.get(pl);
        if (plOr != null)
          ajouteAngle(aisDigue[j], plOr);
      }
    }
  }
  /*
   * Ajout d'un angle a partir de sa polyligne support et de l'angle
   */
  private void ajouteAngle(RefondeAngle _ai, GrPolyligne _pl) {
    double sDeb;
    double sFin;
    double sPtPos;
    double angle;
    double lg;
    GrPoint ptPos;
    GrPoint pt01;
    GrPoint pt02;
    GrSymbole s= null;
    RefondeSymboleData sd;
    sDeb= _ai.getSDebut();
    sFin= _ai.getSFin();
    if (sDeb > sFin)
      lg= sFin + 1 - sDeb;
    else
      lg= sFin - sDeb;
    sPtPos= (sDeb + lg / 2.) % 1;
    ptPos= _pl.pointDAbscisse(sPtPos);
    pt01= _pl.pointDAbscisse(sDeb);
    pt02= _pl.pointDAbscisse(sFin);
    points_.ajoute(pt01);
    points_.ajoute(pt02);
    switch (_ai.getType()) {
      case RefondeAngle.ABSOLU :
        angle= _ai.getAngle() + 90.;
        symboles_.ajoute(s= new GrSymbole(GrSymbole.FLECHE, ptPos, angle, 15.));
        break;
      case RefondeAngle.RELATIF :
        GrPoint p1= _pl.pointDAbscisse((sPtPos + 0.99) % 1.);
        GrPoint p2= _pl.pointDAbscisse((sPtPos + 0.01) % 1.);
        angle=
          _ai.getAngle()
            + Math.atan2(p2.y_ - p1.y_, p2.x_ - p1.x_) * 180. / Math.PI;
        symboles_.ajoute(
          s= new GrSymbole(GrSymbole.FLECHE_LIEE, ptPos, angle, 15.));
        break;
      case RefondeAngle.DIFFRACTE :
        GrPoint ptDiff= _ai.getPointDiffraction();
        angle=
          Math.atan2(ptPos.y_ - ptDiff.y_, ptPos.x_ - ptDiff.x_) * 180. / Math.PI
            + 90.;
        symboles_.ajoute(
          s= new GrSymbole(GrSymbole.FLECHE_DIFFRACTEE, ptPos, angle, 15.));
        GrPolyligne pl= new GrPolyligne();
        pl.sommets_.ajoute(pt01);
        pl.sommets_.ajoute(ptDiff);
        pl.sommets_.ajoute(pt02);
        lignes_.ajoute(pl);
        break;
    }
    // Dans le data du symbole est affecté l'angle d'incidence correspondant
    sd= new RefondeSymboleData();
    sd.angle= _ai;
    s.data(sd);
  }
  /**
   * Renvoi de la liste des elements selectionnables.
   */
  public VecteurGrContour contours() {
    VecteurGrContour res= new VecteurGrContour();
    res.tableau(symboles_.tableau());
    return res;
  }
  /**
   * Boite englobante des objets contenus dans le calque.
   * @return Boite englobante. Si le calque est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getDomaine() {
    GrBoite r= null;
    if (isVisible()) {
      if (lignes_.nombre() > 0) {
        if (r == null)
          r= new GrBoite();
        for (int i= 0; i < lignes_.nombre(); i++)
          r.ajuste(lignes_.renvoie(i).boite());
      }
      if (symboles_.nombre() > 0) {
        if (r == null)
          r= new GrBoite();
        for (int i= 0; i < symboles_.nombre(); i++)
          r.ajuste(symboles_.renvoie(i).position_);
      }
    }
    return r;
  }
}
