/*
 * @file         RefondeTacheOperation.java
 * @creation     1999-07-01
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import com.memoire.bu.BuCommonInterface;
import com.memoire.bu.BuTaskOperation;
/**
 * Une classe permettant d'éxécuter une tache et d'afficher l'état
 * d'avancement de cette tache en même temps qu'elle se déroule.
 *
 * @version      $Id: RefondeTacheOperation.java,v 1.5 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeTacheOperation extends BuTaskOperation {
  //  public String operation  ="";
  //  public int    progression=0;
  private boolean arretDemande_= false;
  private boolean stoppable_= false;
  /**
   * Création d'une tache non stoppable.
   */
  public RefondeTacheOperation(BuCommonInterface _app, String _nom) {
    this(_app, _nom, false);
  }
  /**
   * Création d'une tâche en précisant si elle est stoppable.
   */
  public RefondeTacheOperation(
    BuCommonInterface _app,
    String _nom,
    boolean _stoppable) {
    super(_app, _nom);
    stoppable_= _stoppable;
    //    new ThreadMessage(_app,this).start();
  }
  //  public void   setOperation(String _operation) { operation=_operation; }
  //
  //  public void   setProgression(int _progression) { progression=_progression; }
  //
  //  public void   setMessage(String _operation, int _progression) {
  //    operation=_operation;
  //    progression=_progression;
  //  }
  /**
   * La procédure ne s'arrètera que lorsque l'état du thread le permettra.
   */
  public void stopWhenReady() {
    if (isStoppable()) {
      arretDemande_= true;
      if (RefondeResource.DEBUG)
        System.out.println("Arrêt de la tâche " + getName() + " demandé");
      arretDemande();
    }
  }
  /**
   * Procédure lancée quand l'arret est demandé. A implémenter.
   */
  protected void arretDemande() {}
  /**
   * Retourne si un arret a été demandé pour le thread.
   */
  public boolean isArretDemande() {
    return arretDemande_;
  }
  /**
   * Retourne si un arret a été demandé pour le thread courant.
   */
  public static boolean isTacheCouranteArretDemande() {
    if (!(Thread.currentThread() instanceof RefondeTacheOperation))
      return false;
    return ((RefondeTacheOperation)Thread.currentThread()).isArretDemande();
  }
  /**
   * Lancement d'une exception lors de l'appel à cette tâche si un arret a été
   * demandé.
   */
  public static void notifieArretDemande()
    throws RefondeTacheInterruptionException {
    if (RefondeTacheOperation.isTacheCouranteArretDemande())
      throw new RefondeTacheInterruptionException("*** Interruption utilisateur **");
  }
  /**
   * Définit si le Thread peut être arrété.
   */
  public void setStoppable(boolean _stoppable) {
    stoppable_= _stoppable;
  }
  /**
   * Retourne si le Thread peut être interrompu
   */
  public boolean isStoppable() {
    return stoppable_;
  }
}
/*class ThreadMessage extends Thread {
  RefondeTacheOperation op_;
  BuCommonInterface    app_;

  public ThreadMessage(BuCommonInterface _app, RefondeTacheOperation _op) {
    super();
    op_ =_op;
    app_=_app;
  }

  public void run() {
    while (!op_.isAlive())
     try { Thread.sleep(100); } catch( InterruptedException e ) {}
    while ( op_.isAlive() ) {
      app_.getMainPanel().setMessage(op_.operation);
      app_.getMainPanel().setProgression(op_.progression);
      try { Thread.sleep(100); } catch( InterruptedException e ) {}
    }

    app_.getMainPanel().setMessage("");
    app_.getMainPanel().setProgression(0);
  }
}*/
