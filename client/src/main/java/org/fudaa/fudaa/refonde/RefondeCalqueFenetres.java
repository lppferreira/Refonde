/*
 * @file         RefondeCalqueFenetres.java
 * @creation     1999-08-10
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Date;
import java.util.Vector;

import org.fudaa.ebli.calque.BCalqueAffichage;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.calque.BVueCalque;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrVecteur;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.trace.TraceGeometrie;
/**
 * Un calque de tracé de fenêtres graphiques.
 *
 * @version      $Id: RefondeCalqueFenetres.java,v 1.9 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand , Bertrand Marchand
 */
public class RefondeCalqueFenetres extends BCalqueAffichage {
  /**
   * Modèle de fenetres. Il contient toutes les fenêtres pour la page.
   */
  protected RefondeModeleFenetres modele_;
  /**
   * Création d'un calque de tracé de fenêtres graphiques.
   */
  public RefondeCalqueFenetres() {
    super();
    setDestructible(false);
    modele_= new RefondeModeleFenetres();
  }
  /**
   * Paint de l'icon du calque des angles d'incidence
   */
  //  public void paintIcon(Component _c, Graphics _g, int _x, int _y) {
  //    Color fg=getForeground();
  //    if(isAttenue()) fg=attenueCouleur(fg);
  //
  //    int w=getIconWidth();
  //    int h=getIconHeight();
  //
  //    _g.setColor(Color.white);
  //    _g.fillRect(_x,_y,w,h);
  //
  //    _g.setColor(isVisible() ? Color.black : Color.lightGray);
  //    _g.drawRect(_x,_y,w,h);
  //
  //    TraceGeometrie tg=new TraceGeometrie(_g,GrMorphisme.identite());
  //    tg.setForeground(fg);
  //
  //    GrSymbole s;
  //    s=new GrSymbole(GrSymbole.FLECHE_LIEE,new GrPoint(_x+5.,_y+5.,0.),0.,Math.min(w,h)-5.);
  //    tg.dessineSymbole(s,false,isRapide());
  //    s=new GrSymbole(GrSymbole.FLECHE_DIFFRACTEE,new GrPoint(_x+w-5.,_y+5.,0.),0.,Math.min(w,h)-5.);
  //    tg.dessineSymbole(s,false,isRapide());
  //  }
  /**
   * Affichage des fenêtres
   */
  public void paintComponent(Graphics _g) {
    GrMorphisme versEcran= getVersEcran();
    TraceGeometrie tg= new TraceGeometrie( versEcran);
    Color fg= getForeground();
    if (isAttenue())
      fg= attenueCouleur(fg);
    tg.setForeground(fg);
    Color bg= getBackground();
    if (isAttenue())
      bg= attenueCouleur(bg);
    tg.setBackground(bg);
    Vector vfns= modele_.getObjets();
    for (int i= 0; i < vfns.size(); i++) {
      GrPolygone pg= modele_.getGeometrie((BVueCalque)vfns.get(i));
      BVueCalque vc= (BVueCalque)vfns.get(i);
      if (isRapide()) {
        tg.dessinePolygone((Graphics2D)_g,pg, true, false);
        tg.dessineTexte((Graphics2D)_g,
          vc.getName(),
          pg.barycentre(),
          TraceGeometrie.TEXTE_CENTRE,
          false);
      } else {
        GrPoint og=
          new GrPoint(pg.sommet(3).x_, pg.sommet(3).y_, 0).applique(versEcran);
        GrVecteur od=
          new GrVecteur(
            pg.sommet(1).x_ - pg.sommet(0).x_,
            pg.sommet(0).y_ - pg.sommet(3).y_,
            0).applique(
            versEcran);
        BCalqueLegende cqLeg= null;
        cqLeg= (BCalqueLegende)vc.getCalque().getCalqueParNom("cqLegende");
        // On cache les légendes du calque des légendes
        cqLeg.setVisible(false);
//        cqLeg.setRefreshLocked(true);
//        BCalque[] cqs= vc.getCalque().getTousCalques();
//        Vector lgVis= new Vector();
//        for (int j= 0; j < cqs.length; j++) {
//          if (cqs[j] instanceof RefondeCalqueCarte) {
//            if (cqLeg.getVisible((RefondeCalqueCarte)cqs[j])) {
//              lgVis.add(cqs[j]);
//              cqLeg.setVisible((RefondeCalqueCarte)cqs[j], false);
//            }
//          }
//        }
        Dimension fnDskDim= vc.getSize();
        Graphics g2D= _g;
        if (RefondeResource.DEBUG)
          System.out.println("Time : " + new Date(System.currentTimeMillis()));
        g2D.translate((int)og.x_, (int)og.y_);
        ((Graphics2D)g2D).scale(
          (od.x_) / fnDskDim.width,
          (od.y_) / fnDskDim.height);
        vc.paintComponents(g2D);
        ((Graphics2D)g2D).scale(
          fnDskDim.width / (od.x_),
          fnDskDim.height / (od.y_));
        g2D.translate((int) - og.x_, (int) - og.y_);
        if (RefondeResource.DEBUG)
          System.out.println("Time : " + new Date(System.currentTimeMillis()));
        // On réaffiche les légendes du calque des légendes si nécessaire
//        for (int j= 0; j < lgVis.size(); j++)
//          cqLeg.setVisible((RefondeCalqueCarte)lgVis.get(j), true);
//        cqLeg.setRefreshLocked(false);
        cqLeg.setVisible(true);
      }
    }
    super.paintComponent(_g);
  }
  /**
   * Affectation du modèle de fenetres.
   */
  public void setModele(RefondeModeleFenetres _mdl) {
    modele_= _mdl;
  }
  /**
   * Retourne le modèle de fenetres.
   */
  public RefondeModeleFenetres getModele() {
    return modele_;
  }
  /**
   * Boite englobante des objets contenus dans le calque.
   * @return Boite englobante. Si le calque est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getDomaine() {
    return isVisible() ? modele_.getBoite() : null;
  }
  /**
   * Renvoi de la liste des elements selectionnables (fenêtres)
   */
  public VecteurGrContour contours() {
    return isVisible() ? modele_.getSelectionnables() : new VecteurGrContour();
  }
}
