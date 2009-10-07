/*
 * @file         RefondeOLB.java
 * @creation     2000-05-15
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.ArrayList;
import java.util.List;

import org.fudaa.dodico.corba.geometrie.SMaillage;
import org.fudaa.dodico.corba.olb.ICalculOlb;
import org.fudaa.dodico.corba.olb.IParametresOlb;
import org.fudaa.dodico.corba.olb.IParametresOlbHelper;
import org.fudaa.dodico.corba.olb.IResultatsOlb;
import org.fudaa.dodico.corba.olb.IResultatsOlbHelper;

import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;

import org.fudaa.fudaa.commun.conversion.FudaaMaillageElement;
/**
 * Optimisation de largeur de bande pour un maillage.
 *
 * @version      $Id: RefondeOLB.java,v 1.12 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeOLB {
  /**
   * Optimisation de la largeur de bande pour le maillage donné. Seul le numéro
   * interne des éléments et noeuds est modifié
   */
  public static void optimiser(GrMaillageElement _mail) {
//    GrNoeud[] nds;
  //  GrElement[] els;
    GrMaillageElement mailInit;
    //GrNoeud[] ndsInit;
    //GrElement[] elsInit;

    RefondeNoeudData ndData;
    ICalculOlb olbCal;
    IResultatsOlb olbRes;
    IParametresOlb olbPar;
    // Mise a 0 des numéros de noeuds
    int ndsTaille=_mail.getNbNoeud();
    for (int i= 0; i < ndsTaille; i++)
       ((RefondeNoeudData)_mail.noeud(i).data()).numero= 0;
    // Pour l'instant, seuls les éléments T3 sont autorisés, on n'utilise que
    // ces éléments pour l'OLB
    {
      int elsT=_mail.getNbElement();
//      els= _mail.elements();
      List vels= new ArrayList(elsT);
      for (int i= 0; i < elsT; i++) {
        GrElement eli=_mail.element(i);
        if (eli.type_ == GrElement.T3)
          vels.add(eli);
      }
      mailInit=
        new GrMaillageElement((GrElement[])vels.toArray(new GrElement[vels.size()]));
    }
    //ndsInit= mailInit.noeuds();
    RefondeImplementation.statusBar.setProgression(20);
    if (mailInit.getNbNoeud() > 0) {
      olbCal= RefondeImplementation.SERVEUR_OLB;
      olbPar=
        IParametresOlbHelper.narrow(olbCal.parametres(RefondeImplementation.CONNEXION_OLB));
      olbPar.maillage(FudaaMaillageElement.gr2S(mailInit));
      RefondeImplementation.statusBar.setProgression(60);
      olbCal.calcul(RefondeImplementation.CONNEXION_OLB);
      // Si interruption ou résultats inexistants.
      if (!olbCal.estOK())
        return;
      RefondeImplementation.statusBar.setProgression(70);
      olbRes=
        IResultatsOlbHelper.narrow(olbCal.resultats(RefondeImplementation.CONNEXION_OLB));

      //Fred
      //old
      //GrMaillageElement mailFinal= FudaaMaillageElement.s2Gr(olbRes.maillage());
      //new
      SMaillage mailFinal= olbRes.maillage();
      RefondeImplementation.statusBar.setProgression(80);
      // Construction noeud maillage final => numéro optimisé
      //Fred
      //old
      /*ndsFinal= mailFinal.noeuds();
      nd2Num= new Hashtable(ndsFinal.length);
      for (int i= 0; i < ndsFinal.length; i++)
        nd2Num.put(ndsFinal[i], new Integer(i + 1));*/
      // Mise a jour du numéro d'optimisation des noeuds
//      elsInit= mailInit.elements();
      //Fred
      //old
      //elsFinal= mailFinal.elements();
      int nbEltIni=mailInit.getNbElement();
      for (int i= 0; i < nbEltIni; i++) {
        GrNoeud[] ndsEleInit= mailInit.element(i).noeuds_;
//      Fred
        //old
        //GrNoeud[] ndsEleFinal= elsFinal[i].noeuds;
        //new
        int[] ndsEleFinal=mailFinal.elements[i].noeudsIdx;
        for (int j= 0; j < ndsEleInit.length; j++) {
          ndData= (RefondeNoeudData)ndsEleInit[j].data();
//        Fred
          //old
//          if (ndData.numero == 0)
  //          ndData.numero= ((Integer)nd2Num.get(ndsEleFinal[j])).intValue();
          //new
          if (ndData.numero == 0)
                      ndData.numero= ndsEleFinal[j]+1;
        }
      }
    }
    RefondeImplementation.statusBar.setProgression(85);
    // Mise à jour des numéro de noeuds non rattachés à un élément T3
    {
      int nbOpt= mailInit.getNbNoeud();
      for (int i= 0; i < ndsTaille; i++) {
        ndData= (RefondeNoeudData)_mail.noeud(i).data();
        if (ndData.numero == 0)
          ndData.numero= ++nbOpt;
      }
    }
    int nbElt=_mail.getNbElement();
    // Mise a jour du numéro des éléments
    for (int i= 0; i < nbElt; i++)
       ((RefondeElementData)_mail.element(i).data()).numero= i + 1;
    RefondeImplementation.statusBar.setProgression(90);
  }
}
