/*
 * @file         RefondeMaillable.java
 * @creation     2000-03-22
 * @modification $Date: 2006-09-08 16:04:26 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
/**
 * Une interface pour les objets maillable (Domaines, contours,
 * polyligne, etc.).
 *
 * @version      $Id: RefondeMaillable.java,v 1.5 2006-09-08 16:04:26 opasteur Exp $
 * @author       Bertrand Marchand
 */
public interface RefondeMaillable {
  /**
   * L'objet est-il associ� � un maillage (true si getMaillage()!=null)
   */
  boolean hasMaillage();
  /**
   * L'objet poss�de-t-il des noeuds (true si getNoeuds()!=null)
   */
  boolean hasNoeuds();
  /**
   * Affecte le maillage � l'objet.
   */
  void setMaillage(GrMaillageElement _mail);
  /**
   * Retourne le maillage associ� � l'objet (null si aucun maillage)
   */
  GrMaillageElement getMaillage();
  /**
   * Retourne les noeuds de l'objet (il en poss�de si lui m�me est maill� ou si
   * son parent est maill�). null si aucun noeud
   *
   * Attention : L'objet n'est pas consid�r� poss�der des noeuds si un de ses
   * descendants en poss�de. Exemple : la fonction retournera null pour un
   * domaine non maill� m�me si une des polylignes de ses contours poss�de des
   * noeuds
   */
  GrNoeud[] getNoeuds();
}
