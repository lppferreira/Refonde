/*
 * @file         RefondeCalqueAngleInteraction.java
 * @creation     1999-10-25
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import com.memoire.bu.BuDialogError;

import org.fudaa.ebli.calque.BArbreCalque;
import org.fudaa.ebli.calque.BCalqueInteraction;
import org.fudaa.ebli.calque.BVueCalque;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.geometrie.GrSegment;
import org.fudaa.ebli.geometrie.GrSymbole;
import org.fudaa.ebli.trace.TraceGeometrie;

import org.fudaa.fudaa.commun.impl.FudaaDialog;
/**
 * Un calque de saisie d'un segment de contour.
 *
 * @version      $Id: RefondeCalqueAngleInteraction.java,v 1.11 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeCalqueAngleInteraction
  extends BCalqueInteraction
  implements MouseListener {
  private RefondeProjet projet_;
  private RefondeCalqueAngle clAngles_;
  private BVueCalque vc_;
  private BArbreCalque ac_;
  private int icntr1_; // Contour sur lequel le 1er point a été saisi
//  private int iseg1_; // Segment sur lequel le 1er point a été saisi
  private GrPoint p1_; // 1er point saisi
  private boolean debut_; // Vrai si aucun point saisi
  /**
   * Création d'un calque
   */
  public RefondeCalqueAngleInteraction(
    RefondeCalqueAngle _calque,
    BVueCalque _vc,
    BArbreCalque _ac) {
    super();
    setDestructible(false);
    debut_= true;
    clAngles_= _calque;
    vc_= _vc;
    ac_= _ac;
  }
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
    Color fg= (isVisible() ? Color.black : Color.lightGray);
    int w= getIconWidth();
    int h= getIconHeight();
    //    _g.setColor(Color.white);
    //    _g.fillRect(_x,_y,w,h);
    _g.setColor(fg);
    _g.drawRect(_x, _y, w, h);
    TraceGeometrie tg= new TraceGeometrie( GrMorphisme.identite());
    tg.setForeground(fg);
    GrSymbole s;
    //    s=new GrSymbole(GrSymbole.FLECHE_LIEE,new GrPoint(_x+9.,_y+5.,0.),0.,Math.min(w,h)-5.);
    //    tg.dessineSymbole(s,false,false);
    s=
      new GrSymbole(
        GrSymbole.FLECHE_DIFFRACTEE,
        new GrPoint(_x + w - 5., _y + 5., 0.),
        0.,
        Math.min(w, h) - 5.);
    tg.dessineSymbole( (Graphics2D) _g,s, false, false);
    _g.drawLine(_x + 4, (_y + h) / 2 + 4, _x + 8, (_y + h) / 2 + 4);
    _g.drawLine(_x + 6, (_y + h) / 2 + 2, _x + 6, (_y + h) / 2 + 6);
    _g.drawRect(_x + 3, (_y + h) / 2 + 1, 7, 7);
  }
  public void initialise(RefondeProjet _projet) {
    debut_= true;
    projet_= _projet;
  }
  /* ---------------------------------------------------------------------------
   * -------  Evènements souris  -----------------------------------------------
   * ---------------------------------------------------------------------------
   *
   * RELEASED : Saisie d'un point
   */
  /**
   * <I>Sans objet</I>
   */
  public void mousePressed(MouseEvent _evt) {}
  // ---------------------------------------------------------------------------
  // Methode invoquee quand on lâche un bouton de la souris.
  // => Création de point
  // ---------------------------------------------------------------------------
  public void mouseReleased(MouseEvent _evt) {
    if (isGele())
      return;
    Vector cntrs= projet_.getGeometrie().frontieres();
    Vector cntr;
    GrPolyligne[] plTots= new GrPolyligne[cntrs.size()];
    GrPoint ppp= null;
    double dmin= Double.MAX_VALUE;
    int icntr= 0;
    //int iseg= 0;
    GrPoint ptClic= new GrPoint(_evt.getX(), _evt.getY(), 0.);
    // Boucle sur les segments de la géométrie pour recherche du point le plus
    // proche
    for (int i= 0; i < cntrs.size(); i++) {
      GrPolyligne pl= null;
      GrPoint pt;
      cntr= (Vector)cntrs.get(i);
      plTots[i]= new GrPolyligne();
      for (int j= 0; j < cntr.size(); j++) {
        pl= (GrPolyligne)cntr.get(j);
        pt= pl.sommet(0);
        plTots[i].sommets_.ajoute(pt);
      }
      pt= pl.sommet(1);
      plTots[i].sommets_.ajoute(pt);
      plTots[i]= plTots[i].applique(getVersEcran());
      for (int j= 0; j < plTots[i].nombre() - 1; j++) {
        GrSegment s;
        GrPoint pp;
        double d;
        s= plTots[i].segment(j);
        pp= s.pointPlusProcheXY(ptClic);
        d= pp.distanceXY(ptClic);
        if (d < dmin) {
          dmin= d;
          ppp= pp;
          icntr= i;
          //iseg= j;
          if (dmin == 0.)
            break;
        }
      }
    }
    // Premier point saisi => Stockage
    if (debut_) {
      System.out.println("1er point saisi : " + ppp.x_ + ", " + ppp.y_);
      debut_= false;
      p1_= ppp.applique(getVersReel());
      icntr1_= icntr;
//      iseg1_= iseg;
    }
    // Deuxieme point saisi => Création d'un angle d'incidence
    else {
      System.out.println("2ieme point saisi : " + ppp.x_ + ", " + ppp.y_);
      GrPoint p2= ppp.applique(getVersReel());
      int icntr2= icntr;
      //      int      iseg2 =iseg;
      double sDeb;
      double sFin;
      int iai1= 0;
      int iai2= 0;
      int iai= 0;
      Vector ais;
      Vector[] vais;
      RefondeAngle aiSaisi;
      RefondeAngle ai;
      RefondeAngle ai1;
      RefondeAngle ai2;
      debut_= true;
      setGele(true);
      ac_.refresh();
      if (icntr2 != icntr1_) {
        new BuDialogError(
          null,
          RefondeImplementation.informationsSoftware(),
          "Les points saisis ne sont pas sur le même contour.")
          .activate();
        return;
      }
      // Insertion des points
      //      plTots[icntr].sommets.insere(p1_.applique(getVersEcran()),iseg1_+1);
      //      if (iseg>iseg1_) iseg2++;
      //      else             iseg1_++;
      //      plTots[icntr].sommets.insere(p2.applique(getVersEcran()),iseg2+1);
      // Création d'un angle d'incidence relatif par défaut
      sDeb= plTots[icntr].abscisseDe(p1_.applique(getVersEcran()));
      sFin= plTots[icntr].abscisseDe(p2.applique(getVersEcran()));
      //      sDeb=plTots[icntr].abscisseDe(iseg1_+1);
      //      sFin=plTots[icntr].abscisseDe(iseg2+1);
      if (sDeb == sFin) {
        new BuDialogError(
          null,
          RefondeImplementation.informationsSoftware(),
          "Les points saisis sont identiques.")
          .activate();
        return;
      }
      aiSaisi= new RefondeAngle();
      aiSaisi.setRelatif(sDeb, sFin, 0.);
      // Modification du type et des parametres de l'angle
      RefondeDialogAngle di= new RefondeDialogAngle();
      di.setOption(FudaaDialog.OK_CANCEL_OPTION);
      di.initialise(projet_, new Object[] { aiSaisi });
      di.show();
      if (di.reponse == di.btCancel_)
        return;
      // Insertion de l'angle dans la table des angles
      vais= projet_.getModeleCalcul().angles();
      ais= vais[icntr];
      // Recherche de l'angle existant contenant l'abscisse du premier point
      // de l'angle saisi.
      for (int i= 0; i < ais.size(); i++) {
        ai= (RefondeAngle)ais.get(i);
        if (sDeb <= ai.getSFin() && sDeb > ai.getSDebut()) {
          iai1= i;
          break;
        }
      }
      // Recherche de l'angle existant contenant l'abscisse du 2ieme point
      // de l'angle saisi.
      for (int i= 0; i < ais.size(); i++) {
        ai= (RefondeAngle)ais.get(i);
        if (sFin <= ai.getSFin() && sFin > ai.getSDebut()) {
          iai2= i;
          break;
        }
      }
      ai1= (RefondeAngle)ais.get(iai1);
      ai2= (RefondeAngle)ais.get(iai2);
      // Pas de passage par 0.
      if (sDeb < sFin) {
        // Même angle contenant l'abscisse de début et de fin => Dédoublement
        if (ai1 == ai2) {
          ai2= new RefondeAngle(ai2);
          ai1.setSFin(sDeb);
          ai2.setSDebut(sFin);
          ais.add(iai1 + 1, aiSaisi);
          ais.add(iai1 + 2, ai2);
        }
        // Suppression de tous les angles entre les 2 abscisses
        else {
          iai= iai1 + 1;
          while (ais.get(iai %= ais.size()) != ai2)
            ais.remove(iai);
          ai1.setSFin(sDeb);
          ai2.setSDebut(sFin);
          ais.add(iai, aiSaisi);
        }
      }
      // Passage par 0 => Suppression de tous les angles entre les 2, création
      // De 2 angles.
      else {
        iai= iai1 + 1;
        while (ais.get(iai %= ais.size()) != ai2)
          ais.remove(iai);
        ai1.setSFin(sDeb);
        ai2.setSDebut(sFin);
        ai= new RefondeAngle(aiSaisi);
        ai.setSDebut(sDeb);
        ai.setSFin(1);
        ais.add(ais.size(), ai);
        aiSaisi.setSDebut(0);
        ais.add(0, aiSaisi);
      }
      // Même angle contenant l'abscisse de début et de fin => Dédoublement
      //      if (ai1==ai2) {
      //        if (sDeb<sFin) {
      //          ai2=new RefondeAngle(ai2);
      //          ai1.setSFin(sDeb);
      //          ai2.setSDebut(sFin);
      //          ais.add(iai1+1,aiSaisi);
      //          ais.add(iai1+2,ai2);
      //        }
      //        else {
      //        }
      //      }
      // Deux angles différents => Suppression des angles entre les 2
      //      else {
      //        iai=iai1+1;
      //        while (ais.get(iai%=ais.size())!=ai2) ais.remove(iai);
      //
      //        ai1.setSFin(sDeb);
      //        ai2.setSDebut(sFin);
      //        ais.add(iai,aiSaisi);
      //      }
      //      double lg;
      //      if (sDeb>sFin) lg=sFin+1-sDeb; else lg=sFin-sDeb;
      //      double sMil=(sDeb+lg/2.)%1;
      //      for (int i=0; i<ais.size(); i++) {
      //        ai=(RefondeAngle)ais.get(i);
      //        if (ai.getSFin()>ai.getSDebut()) {
      //          if (sMil<ai.getSFin() && sMil>ai.getSDebut()) { iai=i; break; }
      //        }
      //        else {
      //          if (sMil>ai.getSDebut() || sMil<ai.getSFin()) { iai=i; break; }
      //        }
      //      }
      //      ai1=(RefondeAngle)ais.get(iai);
      //      ai2=new RefondeAngle(ai1);
      //      ai1.setSFin(sMil);
      //      ai2.setSDebut(sMil);
      //      ais.add(iai+1,ai2);
      //
      //      for (int i=0; i<ais.size(); i++) {
      //        ai=(RefondeAngle)ais.get(i);
      //        if (ai.getSFin()>ai.getSDebut()) {
      //          if (sDeb<ai.getSFin() && sDeb>ai.getSDebut()) { iai1=i; break; }
      //        }
      //        else {
      //          if (sDeb>ai.getSDebut() || sDeb<ai.getSFin()) { iai1=i; break; }
      //        }
      //      }
      //
      //      for (int i=0; i<ais.size(); i++) {
      //        ai=(RefondeAngle)ais.get(i);
      //        if (ai.getSFin()>ai.getSDebut()) {
      //          if (sFin<ai.getSFin() && sFin>ai.getSDebut()) { iai2=i; break; }
      //        }
      //        else {
      //          if (sFin>ai.getSDebut() || sFin<ai.getSFin()) { iai2=i; break; }
      //        }
      //      }
      //
      //      // Autrement, suppression des angles intermédiaires
      //      ai1=(RefondeAngle)ais.get(iai1);
      //      ai2=(RefondeAngle)ais.get(iai2);
      //      ai1.setSFin(sDeb);
      //      ai2.setSDebut(sFin);
      //
      //      ais.add(iai1+1,aiSaisi);
      ////        int iai=iai1+1;
      ////        while (ais.get((iai=(iai+1)%ais.size()))!=ai2) ais.remove(iai);
      //      iai=(iai1+2)%ais.size();
      //      while (ais.get(iai)!=ai2) {
      //        ais.remove(iai);
      //        iai=iai%ais.size();
      //      }
      // Suppression des angles éventuels de longueur 0.
      vais[icntr]= new Vector();
      for (int i= 0; i < ais.size(); i++) {
        ai= (RefondeAngle)ais.get(i);
        if (ai.getSDebut() != ai.getSFin())
          vais[icntr].add(ai);
      }
      //      vais[icntr]=ais;
      projet_.getModeleCalcul().angles(vais);
      clAngles_.initialise(projet_);
      vc_.repaint();
    }
  }
  /**
   * <I>Sans objet</I>
   */
  public void mouseClicked(MouseEvent _evt) {}
  /**
   * <I>Sans objet</I>
   */
  public void mouseEntered(MouseEvent _evt) {}
  /**
   * <I>Sans objet</I>
   */
  public void mouseExited(MouseEvent _evt) {}
}
