/*
 * @file         RefondeCalqueDessin.java
 * @creation     2001-09-28
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
import java.util.Vector;

import org.fudaa.ebli.calque.BCalqueDessin;
import org.fudaa.ebli.calque.dessin.DeForme;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.VecteurGrContour;
import org.fudaa.ebli.trace.TraceGeometrie;
/**
 * Un calque d'affichage d'un modèle de dessin. Ce calque affichage le modèle
 * de dessin (une liste de dessins exterieure au calque).
 *
 * @version      $Revision: 1.7 $ $Date: 2006-09-08 16:04:26 $ by $Author: opasteur $
 * @author       Bertrand Marchand
 */
public class RefondeCalqueDessin extends BCalqueDessin//  implements RefondeModeleChangeListener
{
  /**
   * Modèle de dessin. Il contient tous les dessins pour la page.
   */
  protected RefondeModeleDessin modele_;
  // Constructeurs
  public RefondeCalqueDessin() {
    super();
    modele_= new RefondeModeleDessin();
  }
  // Methodes publiques
  /**
    * Dessin de l'icone.
    * @param _c composant dont l'icone peut deriver des proprietes (couleur, ...).
    *           Ce parametre peut etre <I>null</I>. Il est ignore ici.
    * @param _g le graphics sur lequel dessiner l'icone
    * @param _x lieu cible de l'icone (x)
    * @param _y lieu cible de l'icone (y)
    */
  public void paintIcon(Component _c, Graphics _g, int _x, int _y) {
    super.paintIcon(_c, _g, _x, _y);
    Color c1= Color.red;
    Color c2= new Color(0, 150, 0);
    if (isAttenue())
      c1= attenueCouleur(c1);
    if (isAttenue())
      c2= attenueCouleur(c2);
    _g.setColor(c1);
    _g.drawRect(_x + 3, _y + 3, _x + 13, _y + 10);
    _g.setColor(c2);
    _g.drawOval(_x + 7, _y + 7, _x + 15, _y + 11);
  }
  public void paintComponent(Graphics _g) {
    super.paintComponent(_g);
    Vector vobjs= modele_.getObjets();
    TraceGeometrie trace= new TraceGeometrie( getVersEcran());
    for (int i= 0; i < vobjs.size(); i++)
       ((DeForme)vobjs.get(i)).affiche((Graphics2D)_g,trace, isRapide());
  }
  /**
   * @deprecated Il faut passer par le modèle de dessin.
   */
  public void ajoute(DeForme _forme) {
    modele_.ajoute(_forme);
  }
  /**
   * @deprecated Il faut passer par le modèle de dessin.
   */
  public void enleve(DeForme _forme) {
    modele_.enleve(_forme);
  }
  /**
   * Affectation du modèle de dessin.
   */
  public void setModele(RefondeModeleDessin _mdl) {
    modele_= _mdl;
  }
  /**
   * Retourne le modèle de dessin.
   */
  public RefondeModeleDessin getModele() {
    return modele_;
  }
  //  public void modeleChanged(ModeleChangeEvent _evt) {
  //    repaint();
  //  }
  /**
   * Boite englobante des objets contenus dans le calque.
   * @return Boite englobante. Si le calque est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getDomaine() {
    return isVisible() ? modele_.getBoite() : null;
    //    GrBoite r=null;
    //
    //    if (isVisible()) {
    //      if (formes_.size()>0) {
    //        if (r==null) r=new GrBoite();
    //        for (int i=0; i<formes_.size(); i++)
    //         r.ajuste(((DeForme)formes_.get(i)).getGeometrie().boite());
    //      }
    //    }
    //
    //    return r;
  }
  /**
   * Renvoi la liste des éléments sélectionnables, soit tous les objets.
   * <p>
   * Cette liste est retournée dans le sens inverse de création des objets, de
   * façon que le dernier créé soit le premier à être testé lors de la sélection.
   *
   * @return La liste des formes.
   */
  public VecteurGrContour contours() {
    return isVisible() ? modele_.getSelectionnables() : new VecteurGrContour();
    //    ListeGrContour res=new ListeGrContour();
    //
    //    for (int i=formes_.size()-1; i>=0; i--)
    //     res.ajoute((GrContour)formes_.get(i));
    //
    //    return res;
  }
}
