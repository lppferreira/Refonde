/*
 * @file         RefondeMiseEnPageHelper.java
 * @creation     2001-10-26
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import javax.swing.JDesktopPane;

import org.fudaa.ebli.geometrie.GrMorphisme;
/**
 * Outils d'aide à la mise en page.
 *
 * @version      $Id: RefondeMiseEnPageHelper.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeMiseEnPageHelper {
  /**
   * La valeur d'un inch en mm.
   */
  private static final double UN_INCH_EN_MM= 25.4;
  /**
   * Pour passer des coordonnées imprimante en coordonnées millimètres. Une
   * unité d'impression représente 1/72ieme d'inch.
   */
  private static final double ImpToMM= UN_INCH_EN_MM / 72;
  /**
   * Conversion d'un format en 1/72ieme d'inches en mm.
   * @param _pf Le format de page en 1/72ieme d'inches.
   * @return Un nouveau format de page en mm.
   */
  public static PageFormat convert2MM(PageFormat _pf) {
    PageFormat r= (PageFormat)_pf.clone();
    Paper pp= _pf.getPaper();
    Paper ppmm= new Paper();
    ppmm.setSize(pp.getWidth() * ImpToMM, pp.getHeight() * ImpToMM);
    ppmm.setImageableArea(
      pp.getImageableX() * ImpToMM,
      pp.getImageableY() * ImpToMM,
      pp.getImageableWidth() * ImpToMM,
      pp.getImageableHeight() * ImpToMM);
    r.setPaper(ppmm);
    return r;
  }
  /**
   * Conversion d'un format mm en 1/72ieme d'inches.
   * @param _pf Le format de page en mm.
   * @return Un nouveau format de page en 1/72ieme d'inches.
   */
  public static PageFormat convert2Imp(PageFormat _pf) {
    PageFormat r= (PageFormat)_pf.clone();
    Paper pp= _pf.getPaper();
    Paper ppmm= new Paper();
    ppmm.setSize(pp.getWidth() / ImpToMM, pp.getHeight() / ImpToMM);
    ppmm.setImageableArea(
      pp.getImageableX() / ImpToMM,
      pp.getImageableY() / ImpToMM,
      pp.getImageableWidth() / ImpToMM,
      pp.getImageableHeight() / ImpToMM);
    r.setPaper(ppmm);
    return r;
  }
  /**
   * Retourne un morphisme permettant la conversion de la position d'une fenetre
   * de post-traitement depuis l'espace Desktop vers l'espace MiseEnPage.
   */
  public static GrMorphisme convertEcran2MEP(
    PageFormat _pf,
    JDesktopPane _pn) {
    GrMorphisme m;
    int dskW= _pn.getWidth();
    int dskH= _pn.getHeight();
    double prtX= _pf.getImageableX();
    double prtY= _pf.getImageableY();
    double prtW= _pf.getImageableWidth();
    double prtH= _pf.getImageableHeight();
    double ech= Math.min(prtW / dskW, prtH / dskH);
    m= GrMorphisme.identite();
    m.composition(GrMorphisme.translation(0, -dskH, 0));
    m.composition(GrMorphisme.dilatation(ech, -ech, 1));
    m.composition(GrMorphisme.translation(prtX, prtY, 0));
    m.composition(
      GrMorphisme.translation(
        (prtW - dskW * ech) / 2,
        (prtH - dskH * ech) / 2,
        0));
    return m;
  }
  /**
   * Retourne un morphisme permettant la conversion de la position d'une fenetre
   * de post-traitement depuis l'espace Desktop vers l'espace MiseEnPage.
   */
  public static GrMorphisme convertMEP2Ecran(
    PageFormat _pf,
    JDesktopPane _pn) {
    GrMorphisme m;
    int dskW= _pn.getWidth();
    int dskH= _pn.getHeight();
    double prtX= _pf.getImageableX();
    double prtY= _pf.getImageableY();
    double prtW= _pf.getImageableWidth();
    double prtH= _pf.getImageableHeight();
    double ech= Math.min(prtW / dskW, prtH / dskH);
    m= GrMorphisme.identite();
    m.composition(
      GrMorphisme.translation(
        - (prtW - dskW * ech) / 2,
        - (prtH - dskH * ech) / 2,
        0));
    m.composition(GrMorphisme.translation(-prtX, -prtY, 0));
    m.composition(GrMorphisme.dilatation(1 / ech, -1 / ech, 1));
    m.composition(GrMorphisme.translation(0, dskH, 0));
    return m;
  }
}
