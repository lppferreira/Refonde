/*
 * @file         RefondeModeleDessin.java
 * @creation     2001-09-28
 * @modification $Date: 2006-09-08 16:04:28 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.Vector;

import org.fudaa.ebli.calque.dessin.DeForme;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrContour;
import org.fudaa.ebli.geometrie.VecteurGrContour;
/**
 * Un modele de dessin. Il renferme des objets de type DeForme.
 *
 * @version      $Id: RefondeModeleDessin.java,v 1.5 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleDessin {
  /**
   * La liste des dessins. Le vecteur contient des objets de type forme.
   */
  private Vector dessins_;
  /**
   * Création d'un modèle de dessin.
   */
  public RefondeModeleDessin() {
    dessins_= new Vector();
  }
  /**
   * Ajoute une forme au modèle.
   */
  public void ajoute(DeForme _forme) {
    if (!dessins_.contains(_forme))
      dessins_.add(_forme);
  }
  /**
   * Enlève une forme au modèle.
   */
  public void enleve(DeForme _forme) {
    dessins_.remove(_forme);
  }
  /**
   * Supprime tous les objets du modèle.
   */
  public void enleveTout() {
    dessins_.clear();
  }
  /**
   * Retourne les formes du modèle sous forme de vecteur.
   */
  public Vector getObjets() {
    return dessins_;
  }
  /**
   * Boite englobante des objets contenus dans le modèle.
   * @return Boite englobante. Si le modèle est vide,
   * la boite englobante retournée est <I>null</I>
   */
  public GrBoite getBoite() {
    GrBoite r= null;
    if (dessins_.size() > 0) {
      if (r == null)
        r= new GrBoite();
      for (int i= 0; i < dessins_.size(); i++)
        r.ajuste(((DeForme)dessins_.get(i)).boite());
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
    for (int i= dessins_.size() - 1; i >= 0; i--)
      r.ajoute((GrContour)dessins_.get(i));
    return r;
  }
}