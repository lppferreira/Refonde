/*
 * @file         RefondeCalqueCarte.java
 * @creation     1999-08-10
 * @modification $Date: 2006-09-19 15:10:23 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.fudaa.ebli.calque.BCalqueCarte;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;

import org.fudaa.fudaa.commun.trace2d.BPaletteCouleurPlage;
import org.fudaa.fudaa.commun.trace2d.TraceIsoLignesPlages;
import org.fudaa.fudaa.commun.trace2d.TraceIsoSurfacesPlages;
import org.fudaa.fudaa.commun.trace2d.ZModeleChangeEvent;
import org.fudaa.fudaa.commun.trace2d.ZModeleChangeListener;
import org.fudaa.fudaa.commun.trace2d.ZModeleValeur;
/**
 * Un calque de tracé de cartes avec un nouvel algorithme de tracé.
 *
 * @version      $Id: RefondeCalqueCarte.java,v 1.9 2006-09-19 15:10:23 deniger Exp $
 * @author       Bertrand Marchand , Bertrand Marchand
 */
public class RefondeCalqueCarte extends BCalqueCarte implements ZModeleChangeListener {
  BPaletteCouleurPlage pal_;
  private GrBoite boite_= null;
  /** Le modèle de valeurs */
  private ZModeleValeur valeurs_=null;

  /**
   * Contructeur du calque
   */
  public RefondeCalqueCarte() {
    super();
    pal_= new BPaletteCouleurPlage();
    pal_.setTitre("");
    pal_.setSousTitre("");
    pal_.setNbPlages(10);
  }

  public void setPalette(BPaletteCouleurPlage _pal) {
    pal_= _pal;
    construitLegende();
  }

  public BPaletteCouleurPlage getPalette() {
    return pal_;
  }

  /**
   * + Réinitialisation de la palette.
   */
/*  public void setValeurs(double[] _vals) {
    super.setValeurs(_vals);
    double vmin= Double.POSITIVE_INFINITY;
    double vmax= Double.NEGATIVE_INFINITY;
    for (int i= 0; i < _vals.length; i++) {
      vmin= Math.min(vmin, _vals[i]);
      vmax= Math.max(vmax, _vals[i]);
    }
    pal_.setMinPalette(vmin);
    pal_.setMaxPalette(vmax);
    pal_.ajustePlages();
    pal_.ajusteLegendes();
    construitLegende();
  }*/

  /**
   * Affectation du modele de valeurs.
   * @param _modele Le modele
   */
  public void setModeleValeurs(ZModeleValeur _modele) {
    //ZModeleValeur vp=valeurs_;
    //    if (valeurs_!=null) valeurs_.removeModelChangeListener(this);
    valeurs_= _modele;
    //    if (valeurs_!=null) valeurs_.addModelChangeListener(this);
    modelChanged(null);
  }
  /**
   * Retourne le modele de valeurs.
   * @return Le modele.
   */
  public ZModeleValeur getModeleValeur() {
    return valeurs_;
  }

  // >>> ZModeleChangeListener  ------------------------------------------------

  public void modelChanged(ZModeleChangeEvent _evt) {
    // Mise à jour de la palette en fonction des valeurs (désactivé pour le chgt de pas de temps).
    pal_.setMinPalette(valeurs_.getMin());
    pal_.setMaxPalette(valeurs_.getMax());
    pal_.ajustePlages();
    pal_.ajusteLegendes();
    construitLegende();
  }

  // <<< ZModeleChangeListener  ------------------------------------------------

  /**
   * Construction de la légende. En fait, affecte la légende du calque au calque
   * d'affichage des légendes.
   */
  protected void construitLegende() {
    BCalqueLegende cqLg= getLegende();
    if (cqLg == null)
      return;
    cqLg.enleve(this);
    //    p.setOpaque(false);
    //    cqLg.ajoute(this, paletteLeg, p);
    cqLg.ajoute(this, pal_);
  }
  // Icon
  public void paintIcon(Component _c, Graphics _g, int _x, int _y) {
    //    super.paintIcon(_c,_g,_x,_y);
    _g.translate(_x, _y);
    boolean attenue= isAttenue();
    int w= getIconWidth();
    int h= getIconHeight();
    Color fg= getForeground();
    Color bg= getBackground();
    if (attenue)
      fg= attenueCouleur(fg);
    if (attenue)
      bg= attenueCouleur(bg);
    {
      Color c;
      c= Color.red;
      if (attenue)
        c= attenueCouleur(c);
      _g.setColor(c);
      _g.fillRect(1, 1, w - 1, h - 1);
      c= Color.yellow;
      if (attenue)
        c= attenueCouleur(c);
      _g.setColor(c);
      _g.fillOval(3, 3, w - 5, h - 5);
      c= Color.blue;
      if (attenue)
        c= attenueCouleur(c);
      _g.setColor(c);
      _g.fillOval(7, 7, w - 14, h - 14);
    }
    _g.translate(-_x, -_y);
  }

  // Paint
  public void paintComponent(Graphics _g) {
    if (RefondeResource.DEBUG) System.out.println("Paint calque Carte");

    boolean attenue= isAttenue();
    boolean rapide= isRapide();
    int i, j;
    //    double iv=vmin_;
    //    double av=vmax_;
    //    if( !Double.isNaN(minVal_) ) iv=minVal_;
    //    if( !Double.isNaN(maxVal_) ) av=maxVal_;
    if (boite_ == null) boite_=getMaillage().boite();

    GrMorphisme versEcran= getVersEcran();
    Polygon pecr= boite_.enPolygoneXY().applique(versEcran).polygon();
    Rectangle clip= _g.getClipBounds();
    if (clip==null) clip=new Rectangle(0, 0, getWidth(), getHeight());

    if (clip.intersects(pecr.getBounds())) {
      if (rapide) {
        Color c;
        c= pal_.getCouleurAutres();
        if (attenue) c=attenueCouleur(c);
        _g.setColor(c);
        _g.drawPolygon(pecr);
      }
      else {
        if (maillageEcran_ == null)
          maillageEcran_= getMaillage().applique(versEcran);
        Color fg= getForeground();
        Color bg= getBackground();
        if (attenue) fg= attenueCouleur(fg);
        if (attenue) bg= attenueCouleur(bg);

        // Palette background
        BPaletteCouleurPlage pbg=(BPaletteCouleurPlage)pal_.clone();
        pbg.propagerCouleurs(bg, bg);
        BPaletteCouleurPlage pisl= pal_;
        if (getIsosurfaces()) pisl= pbg;
        TraceIsoLignesPlages isol= new TraceIsoLignesPlages(pisl);
        TraceIsoSurfacesPlages isos= new TraceIsoSurfacesPlages(pal_);

        boolean isIsoSurfacesVisible= getIsosurfaces();
        boolean isIsoLignesVisible= getIsolignes();
        boolean isContoursVisible= getContour();

        int n= maillageEcran_.nombre();

        for (i= 0; i < n; i++) {
          Polygon p= maillageEcran_.polygon(i);
          if (!clip.intersects(p.getBounds())) continue;

          // Trace des isosurfaces / Isolignes (dans la couleur du fond si les
          // isocouleurs sont tracees).
          if (isIsoSurfacesVisible || isIsoLignesVisible) {
            int[] noeuds= (int[])maillageEcran_.connectivites_.get(i);
            int m= noeuds.length;
            double[] v= new double[m];

            for (j= 0; j < m; j++) v[j]= valeurs_.valeur(noeuds[j]);
            if (isIsoSurfacesVisible) isos.draw(_g, p, v);
            if (isIsoLignesVisible)   isol.draw(_g, p, v);
          }

          // Trace des contours (dans la couleur du trace).
          if (isContoursVisible) {
            _g.setColor(fg);
            _g.drawPolygon(p);
          }
        }
      }
    }
  }
}
