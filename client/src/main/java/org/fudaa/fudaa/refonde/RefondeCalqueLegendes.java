/*
 * @file         RefondeCalqueLegendes.java
 * @creation     1999-08-10
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Vector;

import javax.swing.JPanel;

import org.fudaa.ebli.calque.BCalqueAffichage;
import org.fudaa.ebli.calque.BCalqueCarte;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrVecteur;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.trace.TraceGeometrie;
/**
 * Un calque de tracé de légendes graphiques.
 *
 * @version      $Id: RefondeCalqueLegendes.java,v 1.9 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand , Bertrand Marchand
 */
public class RefondeCalqueLegendes extends BCalqueAffichage {
  /**
   * Modèle de légendes. Il contient toutes les légendes pour la page.
   */
  protected RefondeModeleLegendes modele_;
  /**
   * Création d'un calque de tracé de légendes graphiques.
   */
  public RefondeCalqueLegendes() {
    super();
    setDestructible(false);
    modele_= new RefondeModeleLegendes();
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
    TraceGeometrie tg= new TraceGeometrie(versEcran);
    Color fg= getForeground();
    if (isAttenue())
      fg= attenueCouleur(fg);
    tg.setForeground(fg);
    Color bg= getBackground();
    if (isAttenue())
      bg= attenueCouleur(bg);
    tg.setBackground(bg);
    Vector vlgs= modele_.getObjets();
    for (int i= 0; i < vlgs.size(); i++) {
      GrPolygone pg= modele_.getGeometrie((JPanel)vlgs.get(i));
      BCalqueLegende cqLg= modele_.getCalqueLegende((JPanel)vlgs.get(i));
      BCalqueCarte cq= modele_.getCalque((JPanel)vlgs.get(i));
      // On passe les légendes non visibles dans les fenêtres du bureau.
      if (!cqLg.isVisible() || !cq.isVisible())
        continue;
      if (isRapide()) {
        tg.dessinePolygone((Graphics2D)_g,pg, true, isRapide());
      } else {
        GrPoint og=
          new GrPoint(pg.sommet(3).x_, pg.sommet(3).y_, 0).applique(versEcran);
        GrVecteur od=
          new GrVecteur(
            pg.sommet(1).x_ - pg.sommet(0).x_,
            pg.sommet(0).y_ - pg.sommet(3).y_,
            0).applique(
            versEcran);
        JPanel _lg= (JPanel)vlgs.get(i);
        //        frHelper_.setContentPane(_lg);
        //        frHelper_.pack();
//        Graphics2D g2D= (Graphics2D)_g.create();
        Graphics2D g2D= (Graphics2D)_g;
        g2D.translate(og.x_, og.y_);
        g2D.scale(
          (od.x_) / _lg.getWidth(),
          (od.y_) / _lg.getHeight());
        Shape sh=g2D.getClip();
        g2D.setClip(0, 0, _lg.getWidth(), _lg.getHeight());
        _lg.paint(g2D);
        _lg.paint(_g);
        _lg.validate();
        _lg.repaint();
        g2D.setClip(sh);
        g2D.scale(
          _lg.getWidth()/(od.x_),
          _lg.getHeight()/(od.y_));
        g2D.translate(-og.x_, -og.y_);
      }
    }
    super.paintComponent(_g);
  }
  /**
   * Affectation du modèle de légendes.
   */
  public void setModele(RefondeModeleLegendes _mdl) {
    modele_= _mdl;
  }
  /**
   * Retourne le modèle de légendes.
   */
  public RefondeModeleLegendes getModele() {
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
