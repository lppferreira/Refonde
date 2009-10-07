/*
 * @file         ServeurRefonde.java
 * @creation     2000-02-16
 * @modification $Date: 2006-09-19 14:45:59 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.dodico.refonde;
import java.util.Date;

import org.fudaa.dodico.objet.CDodico;
import org.fudaa.dodico.objet.UsineLib;
/**
 * Une classe serveur pour Refonde.
 *
 * @version      $Revision: 1.8 $ $Date: 2006-09-19 14:45:59 $ by $Author: deniger $
 * @author       Guillaume Desnoix 
 */
public final class ServeurRefonde {
  private ServeurRefonde(){}
  public static void main(final String[] _args) {
    final String nom=
      (_args.length > 0
        ? _args[0]
        : CDodico.generateName("::refonde::ICalculRefonde"));
    //Cas particulier : il s'agit de creer un serveur de calcul dans une jvm donne
    //Cette Méthode n'est pas a imiter. If faut utiliser Boony pour creer des objet corba.
    CDodico.rebind(nom, UsineLib.createService(DCalculRefonde.class));
    System.out.println("Refonde server running... ");
    System.out.println("Name: " + nom);
    System.out.println("Date: " + new Date());
  }
}
