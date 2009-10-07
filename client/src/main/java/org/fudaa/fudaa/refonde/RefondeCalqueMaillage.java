/*
 * @file         RefondeCalqueMaillage.java
 * @creation     1999-11-23
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Vector;

import org.fudaa.ebli.calque.BCalqueMaillageElement;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.trace.TraceGeometrie;
/**
 * Un calque de tracé du maillage.
 *
 * @version      $Id: RefondeCalqueMaillage.java,v 1.6 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand , Bertrand Marchand
 */
public class RefondeCalqueMaillage extends BCalqueMaillageElement {
  protected boolean numNoeudsVisible_;
  protected boolean numElementsVisible_;
  /**
   * Contructeur du calque
   */
  public RefondeCalqueMaillage() {
    super();
    setDestructible(false);
    setNumerosNoeudsVisibles(false);
    setNumerosElementsVisibles(false);
  }
  /**
   * Affichage des éléments
   */
  public void paintComponent(Graphics _g) {
    super.paintComponent(_g);
    if (!isRapide()) {
      TraceGeometrie tg= new TraceGeometrie(getVersEcran());
      Color c;
      for (int i= 0; i < maillages_.size(); i++) {
        GrMaillageElement maillage_= (GrMaillageElement)maillages_.get(i);
        // Affichage des numéros de noeuds
        if (numNoeudsVisible_) {
          GrNoeud[] nds= maillage_.noeuds();
          c= new Color(0, 200, 0);
          if (isAttenue())
            c= attenueCouleur(c);
          tg.setForeground(c);
          for (int j= 0; j < nds.length; j++) {
            int num= ((RefondeNoeudData)nds[j].data()).numero;
            tg.dessineTexte((Graphics2D)_g,"" + num, nds[j].point_, false);
          }
        }
        // Affichage des numéros d'éléments
        if (numElementsVisible_) {
          GrElement[] els= maillage_.elements();
          c= Color.black;
          if (isAttenue())
            c= attenueCouleur(c);
          tg.setForeground(c);
          for (int j= 0; j < els.length; j++) {
            int num= ((RefondeElementData)els[j].data()).numero;
            tg.dessineTexte((Graphics2D)_g,"" + num, els[j].barycentre(), false);
          }
        }
      }
    }
  }
  /**
   * Rend les numéros de noeuds visibles. Dans ce cas, les noeuds sont rendus
   * visibles également
   */
  public void setNumerosNoeudsVisibles(boolean _etat) {
    numNoeudsVisible_= _etat;
    noeudsVisibles_= _etat;
  }
  public boolean isNumerosNoeudsVisibles() {
    return numNoeudsVisible_;
  }
  /**
   * Rend les numéros des éléments visibles
   */
  public void setNumerosElementsVisibles(boolean _etat) {
    numElementsVisible_= _etat;
  }
  public boolean isNumerosElementsVisibles() {
    return numElementsVisible_;
  }
  /**
   * Initialisation du calque avec le projet
   */
  public void initialise(RefondeProjet _projet) {
    Vector vdms= _projet.getGeometrie().scene_.getDomaines();
    GrMaillageElement ml;
    // Nettoyage du calque
    reinitialise();
    for (int i= 0; i < vdms.size(); i++)
      if ((ml= ((RefondeDomaine)vdms.get(i)).getMaillage()) != null)
        ajoute(ml);
  }
  /**
   * Renvoi la liste des objets selectionnables.
   */
  public VecteurGrContour contours() {
    return new VecteurGrContour();
  }
}
