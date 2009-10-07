/*
 * @file         RefondeTaskView.java
 * @creation     1999-06-25
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.memoire.bu.BuPopupMenu;
import com.memoire.bu.BuTask;
import com.memoire.bu.BuTaskView;
/**
 * Une vue pour les tâches en cours. Les tâches en cours peuvent être stoppées à
 * l'aide d'un popUpMenu.
 *
 * @version      $Id: RefondeTaskView.java,v 1.5 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeTaskView
  extends BuTaskView
  implements MouseListener, ActionListener {
  BuTask tacheSel_; // Tache sélectionnée lors de l'apparition du popupMenu.
  public RefondeTaskView() {
    super();
    addMouseListener(this);
  }
  // >>> Interface MouseListener -----------------------------------------------
  public void mousePressed(MouseEvent _evt) {
    if (_evt.isPopupTrigger()
      || ((_evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0))
      popupMenu(_evt);
  }
  public void mouseReleased(MouseEvent _evt) {}
  public void mouseClicked(MouseEvent _evt) {}
  public void mouseExited(MouseEvent _evt) {}
  public void mouseEntered(MouseEvent _evt) {}
  // <<< Interface MouseListener -----------------------------------------------
  // >>> Interface ActionListener ----------------------------------------------
  public void actionPerformed(ActionEvent _evt) {
    if (tacheSel_ instanceof RefondeTacheOperation)
       ((RefondeTacheOperation)tacheSel_).stopWhenReady();
    //    else {
    //      tacheSel_.stop();
    //      removeTask(tacheSel_);
    //    }
  }
  // >>> Interface ActionListener ----------------------------------------------
  /**
   * Affiche le menu contextuel pour la tâche selectionnée.
   */
  public void popupMenu(MouseEvent _evt) {
    int xs= _evt.getX();
    int ys= _evt.getY();
    int ind= locationToIndex(new Point(xs, ys));
    if (ind != -1) {
      tacheSel_= (BuTask)getModel().getElementAt(ind);
      BuPopupMenu mn= new BuPopupMenu();
      if (tacheSel_ instanceof RefondeTacheOperation
        && ((RefondeTacheOperation)tacheSel_).isStoppable())
        mn.addMenuItem(
          "Arrêter '" + tacheSel_.getName() + "'",
          "ARRETER",
          true);
      else
        mn.addMenuItem(
          "Interruption impossible sur '" + tacheSel_.getName() + "'",
          "DUMMY",
          false);
      // Position d'affichage du menu.
      Dimension mnSize= mn.getPreferredSize();
      Dimension scSize= Toolkit.getDefaultToolkit().getScreenSize();
      int xmn= xs;
      int ymn= ys;
      Point ps= new Point(xs, ys);
      SwingUtilities.convertPointToScreen(ps, (JComponent)_evt.getSource());
      if (ps.x + mnSize.width > scSize.width)
        xmn -= mnSize.width;
      if (ps.y + mnSize.height > scSize.height)
        ymn -= mnSize.height;
      mn.show((JComponent)_evt.getSource(), xmn, ymn);
    }
  }
  /**
   * Test
   */
  public static void main(String[] args) {
    RefondeTaskView tv= new RefondeTaskView();
    tv.addTask(new RefondeTacheOperation(null, "Tache 1"));
    tv.addTask(new RefondeTacheOperation(null, "Tache 2"));
    tv.setPreferredSize(new Dimension(100, 150));
    JFrame fr= new JFrame("Task view");
    fr.getContentPane().add(tv);
    fr.pack();
    fr.show();
  }
}
