/*
 * @file         RefondeGeom.java
 * @creation     2000-03-22
 * @modification $Date: 2005-08-16 14:21:33 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
/**
 * Une interface pour les objets geometriques (Domaines, contours,
 * polyligne, etc.).
 *
 * @version      $Id: RefondeGeom.java,v 1.4 2005-08-16 14:21:33 deniger Exp $
 * @author       Bertrand Marchand
 */
public interface RefondeGeom {
  /**
   * Retourne les parents
   */
  RefondeGeom[] getParents();
  /**
   * Ajoute un parent
   */
  void addParent(RefondeGeom _geom);
  /**
   * Supprime un parent
   */
  void removeParent(RefondeGeom _geom);
  /**
   * Retourne la sc�ne (ou null si l'objet n'est rattach� a aucune sc�ne)
   */
  RefondeScene getScene();
  /**
   * d�finit la sc�ne d'attachement
   */
  void setScene(RefondeScene _scene);
}
