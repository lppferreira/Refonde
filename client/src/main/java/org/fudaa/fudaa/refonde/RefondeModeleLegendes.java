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
   * Vecteur des g�om�tries des legendes.
   */
  private Vector vlgsGeo_;
  /**
   * Vecteur des BCalqueLegende pour la visibilit� des l�gendes.
   */
  private Vector vlgsCqLg_;
  /**
   * Vecteur des BCalqueAffichage pour la visibilit� des l�gendes.
   */
  private Vector vlgsCq_;
  /**
   * Format de page.
   */
  //private PageFormat pf_;
  /**
   * Position suivant x de la l�gende � cr�er. Incr�ment� � chaque l�gende
   * cr��e.
   */
  //  private double xPos_=0;
  /**
   * Cr�ation d'un mod�le de legendes.
   */
  public RefondeModeleLegendes() {
    vlgs_= new Vector();
    vlgsGeo_= new Vector();
    vlgsCqLg_= new Vector();
    vlgsCq_= new Vector();
    //pf_= RefondeMiseEnPage.pageDefaut;
  }
  /**
   * D�finition du format de page dans lequel la legende doit �tre ajout�e. Par
   * d�faut le format de page vaut PrinterJob.getPrinterJob().defautPage().
   */
  public void setPageFormat(PageFormat _pf) {
    //pf_= _pf;
  }
  /**
   * Ajout d'une l�gende.
   *
   * @param _lg   L�gende.
   * @param _cqLg Le calque d'affichage de l�gendes associ�.
   * @param _cq   Le calque associ� � la l�gende.
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
   * Ajout d'une l�gende en donnant ses dimensions.
   *
   * @param _lg   L�gende.
   * @param _cqLg Le calque d'affichage de l�gendes associ�.
   * @param _cq   Le calque associ� � la l�gende.
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
   * Suppression d'une l�gende du calque.
   *
   * @param _lg L�gende graphique.
   */
  public void enleve(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    vlgs_.remove(i);
    vlgsGeo_.remove(i);
    vlgsCqLg_.remove(i);
    vlgsCq_.remove(i);
  }
  /**
   * Supprime tous les objets du mod�le.
   */
  public void enleveTout() {
    vlgs_.clear();
    vlgsGeo_.clear();
    vlgsCqLg_.clear();
    vlgsCq_.clear();
    //    xPos_=0;
  }
  /**
   * Retourne les l�gendes du mod�le sous forme de vecteur.
   */
  public Vector getObjets() {
    return vlgs_;
  }
  /**
   * Retourne la g�om�trie associ�e � une legende.
   * @return La g�om�trie ou <i>null</i> si la legende n'est pas dans le mod�le.
   */
  public GrPolygone getGeometrie(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (GrPolygone)vlgsGeo_.get(i) : null;
  }
  /**
   * Retourne le calque d'affichage des l�gendes associ� (pour savoir si la
   * l�gende est visible).
   * @return Le calque ou <i>null</i> si la legende n'est pas dans le mod�le.
   */
  public BCalqueLegende getCalqueLegende(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (BCalqueLegende)vlgsCqLg_.get(i) : null;
  }
  /**
   * Retourne le calque repr�sent� par la l�gende.
   * @return Le calque ou <i>null</i> si la legende n'est pas dans le mod�le.
   */
  public BCalqueCarte getCalque(JPanel _lg) {
    int i= vlgs_.indexOf(_lg);
    return i != -1 ? (BCalqueCarte)vlgsCq_.get(i) : null;
  }
  /**
   * Boite englobante des objets contenus dans le mod�le.
   * @return Boite englobante. Si le mod�le est vide,
   * la boite englobante retourn�e est <I>null</I>
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
   * Renvoi la liste des �l�ments s�lectionnables, soit tous les objets.
   * <p>
   * Cette liste est retourn�e dans le sens inverse de cr�ation des objets, de
   * fa�on que le dernier cr�� soit le premier � �tre test� lors de la s�lection.
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