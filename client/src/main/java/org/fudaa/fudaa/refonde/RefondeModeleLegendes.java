/*
 * @file         RefondeModeleLegendes.java
 * @creation     2001-09-28
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.print.PageFormat;
import java.util.Vector;

import javax.swing.JPanel;

import org.fudaa.ebli.calque.BCalqueAffichage;
import org.fudaa.ebli.calque.BCalqueCarte;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.VecteurGrContour;
/**
 * Un modele de legendes. Il renferme des objets de type JPanel.
 *
 * @version      $Id: RefondeModeleLegendes.java,v 1.6 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleLegendes {
  /**
   * La liste des legendes. Le vecteur contient des objets de type JPanel.
   */
  private Vector vlgs_;
  /**
   * Vecteur des géométries des legendes.
   */
  private Vector vlgsGeo_;
  /**
   * Vecteur des BCalqueLegende pour la visibilité des légendes.
   */
  private Vector vlgsCqLg_;
  /**
   * Vecteur des BCalqueAffichage pour la visibilité des légendes.
   */
  private Vector vlgsCq_;
  /**
   * Format de page.
   */
  //private PageFormat pf_;
  /**
   * Position suivant x de la légende à créer. Incrémenté à chaque légende
   * créée.
   */
  //  private double xPos_=0;
  /**
   * Création d'un modèle de legendes.
   */
  public RefondeModeleLegendes() {
    vlgs_= new Vector();
    vlgsGeo_= new Vector();
    vlgsCqLg_= new Vector();
    vlgsCq_= new Vector();
    //pf_= RefondeMiseEnPage.pageDefaut;
  }
  /**
   * Définition du format de page dans lequel la legende doit être ajoutée. Par
   * défaut le format de page vaut PrinterJob.getPrinterJob().defautPage().
   */
  public void setPageFormat(PageFormat _pf) {
    //pf_= _pf;
  }
  /**
   * Ajout d'une légende.
   *
   * @param _lg   Légende.
   * @param _cqLg Le calque d'affichage de légendes associé.
   * @param _cq   Le calque associé à la légende.
   */
  //  public void ajoute(JPanel _lg, BCalqueLegende _cqLg, BCalqueCarte _cq) {
  //    double prtX=pf_.getImageableX();
  //    double prtY=pf_.getImageableY();
  //    double prtW=pf_.getImageableWidth();
  //    double prtH=pf_.getImageableHeight();
  //
  //    double lgPrtX=prtX+xPos_;
  //    double lgPrtY=prtY;
  //    double lgPrtW=_lg.getWidth()/4;
  //    double lgPrtH=_lg.getHeight()/4;
  //
  //    xPos_+=lgPrtW;
  //
  //    ajoute(_lg,_cqLg,_cq,prtX,prtY,prtW,prtH);
  //  }
  /**
   * Ajout d'une légende en donnant ses dimensions.
   *
   * @param _lg   Légende.
   * @param _cqLg Le calque d'affichage de légendes associé.
   * @param _cq   Le calque associé à la légende.
   * @param _x  Position x de la legende.
   * @param _y  Position y de la legende.
   * @param _w  Largeur de la legende.
   * @param _h  Hauteur de la legende.
   */
  public void ajoute(
    JPanel _lg,
    BCalqueLegende _cqLg,
    BCalqueCarte _cq,
    double _x,
    double _y,
    double _w,
    double _h) {
    GrPolygone pg= new GrPolygone();
    pg.sommets_.ajoute(new GrPoint(_x, _y, 0));
    pg.sommets_.ajoute(new GrPoint(_x + _w, _y, 0));
    pg.sommets_.ajoute(new GrPoint(_x + _w, _y + _h, 0));
    pg.sommets_.ajoute(new GrPoint(_x, _y + _h, 0));
    vlgs_.add(_lg);
    vlgsGeo_.add(pg);
    vlgsCqLg_.add(_cqLg);
    vlgsCq_.add(_cq);
  }
  /**
   * Suppression d'une légende du calque.
   *
   * @param _lg Légende graphique.
   */
  public void enleve(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    vlgs_.remove(i);
    vlgsGeo_.remove(i);
    vlgsCqLg_.remove(i);
    vlgsCq_.remove(i);
  }
  /**
   * Supprime tous les objets du modèle.
   */
  public void enleveTout() {
    vlgs_.clear();
    vlgsGeo_.clear();
    vlgsCqLg_.clear();
    vlgsCq_.clear();
    //    xPos_=0;
  }
  /**
   * Retourne les légendes du modèle sous forme de vecteur.
   */
  public Vector getObjets() {
    return vlgs_;
  }
  /**
   * Retourne la géométrie associée à une legende.
   * @return La géométrie ou <i>null</i> si la legende n'est pas dans le modèle.
   */
  public GrPolygone getGeometrie(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (GrPolygone)vlgsGeo_.get(i) : null;
  }
  /**
   * Retourne le calque d'affichage des légendes associé (pour savoir si la
   * légende est visible).
   * @return Le calque ou <i>null</i> si la legende n'est pas dans le modèle.
   */
  public BCalqueLegende getCalqueLegende(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (BCalqueLegende)vlgsCqLg_.get(i) : null;
  }
  /**
   * Retourne le calque représenté par la légende.
   * @return Le calque ou <i>null</i> si la legende n'est pas dans le modèle.
   */
  public BCalqueCarte getCalque(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (BCalqueCarte)vlgsCq_.get(i) : null;
  }
  /**
   * Boite englobante des objets contenus dans le modèle.
   * @return Boite englobante. Si le modèle est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getBoite() {
    GrBoite r= null;
    if (vlgsGeo_.size() > 0) {
      if (r == null)
        r= new GrBoite();
      for (int i= 0; i < vlgsGeo_.size(); i++)
        if (((BCalqueLegende)vlgsCqLg_.get(i)).isVisible()
          && ((BCalqueAffichage)vlgsCq_.get(i)).isVisible())
          r.ajuste(((GrPolygone)vlgsGeo_.get(i)).boite());
    }
    return r;
  }
  /**
   * Renvoi la liste des éléments sélectionnables, soit tous les objets.
   * <p>
   * Cette liste est retournée dans le sens inverse de création des objets, de
   * façon que le dernier créé soit le premier à être testé lors de la sélection.
   *
   * @return La liste des formes.
   */
  public VecteurGrContour getSelectionnables() {
    VecteurGrContour r= new VecteurGrContour();
    for (int i= vlgsGeo_.size() - 1; i >= 0; i--)
      if (((BCalqueLegende)vlgsCqLg_.get(i)).isVisible()
        && ((BCalqueAffichage)vlgsCq_.get(i)).isVisible())
        r.ajoute((GrPolygone)vlgsGeo_.get(i));
    return r;
  }
}