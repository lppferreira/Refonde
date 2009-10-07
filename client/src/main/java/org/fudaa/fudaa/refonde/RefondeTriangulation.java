/*
 * @file         RefondeTriangulation.java
 * @creation     1999-07-01
 * @modification $Date: 2006-09-19 15:10:21 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.util.Arrays;
import java.util.Vector;

import org.fudaa.dodico.corba.dunes.ICalculDunes;
import org.fudaa.dodico.corba.dunes.IParametresDunes;
import org.fudaa.dodico.corba.dunes.IParametresDunesHelper;
import org.fudaa.dodico.corba.dunes.IResultatsDunes;
import org.fudaa.dodico.corba.dunes.IResultatsDunesHelper;
import org.fudaa.dodico.corba.geometrie.SPoint;
import org.fudaa.dodico.corba.geometrie.SPolyligne;
import org.fudaa.dodico.corba.geometrie.SRegion;
import org.fudaa.dodico.corba.geometrie.STrou;

import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrPoint;

import org.fudaa.fudaa.commun.conversion.FudaaMaillageElement;
import org.fudaa.fudaa.commun.conversion.FudaaPoint;
import org.fudaa.fudaa.commun.conversion.FudaaPolyligne;
/**
 * Classe de triangulation à partir d'un domaine et d'un nuage de points.
 *
 * @version      $Id: RefondeTriangulation.java,v 1.8 2006-09-19 15:10:21 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeTriangulation {
  /**
   * Triangulation d'un nuage de points avec polylignes de contours
   * @param _doma Domaine limitant la triangulation (les points doivent inclure
   *              une cote z
   * @param _pts  Points de topo (avec cote z)
   * @return <code>null</code> si une interruption ou une erreur a eu lieu sur
   *         le serveur de calcul.
   */
  public static GrMaillageElement trianguler(
    RefondeDomaine _doma,
    GrPoint[] _pts) {
    ICalculDunes duCal;
    IResultatsDunes duRes;
    IParametresDunes duPar;
    Vector vSPls= new Vector();
    RefondeContour[] cntrs= _doma.getContours();
    STrou[] sTrous= new STrou[cntrs.length - 1];
    SPoint[] sPts;
    SPolyligne[] sPls;
    int nbTrous= 0;
    //-------  Remplissage des parametres pour dunes  --------------------------
    System.out.println("Triangulation");
    //    _op.setMessage("Triangulation...",0);
    //    duCal=RefondeImplementation.SERVEUR_DUNES;
    duCal= RefondeImplementation.SERVEUR_DUNES;
    duPar=
      IParametresDunesHelper.narrow(
        duCal.parametres(RefondeImplementation.CONNEXION_DUNES));
    //    sPls  =new SPolyligne[nbLignes];
    //    sTrous=new STrou[nbCntr-1];
    //    nbCntr=0;
    //    nbTrous=0;
    //    nbLignes=0;
    for (int i= 0; i < cntrs.length; i++) {
      //    for (Iterator i=contours.iterator(); i.hasNext();) {
      SPoint sPt= null;
      RefondePolyligne[] pls= cntrs[i].getPolylignes();
      SPolyligne[] sPlsCntr= new SPolyligne[pls.length];
      for (int j= 0; j < pls.length; j++) {
        //      cntr=(Vector)i.next();
        //      sPlsCntr=new SPolyligne[cntr.size()];
        //      for (int j=0; j<cntr.size(); j++) {
        //        pl=(GrPolyligne)cntr.get(j);
        sPlsCntr[j]= FudaaPolyligne.gr2S(pls[j]);
        sPlsCntr[j].points[0]= sPt;
        sPt= sPlsCntr[j].points[sPlsCntr[j].points.length - 1];
      }
      sPlsCntr[0].points[0]= sPt;
      if (i != 0)
        sTrous[nbTrous++]=
          new STrou(FudaaPoint.gr2S(cntrs[i].getPointInterne()));
      vSPls.addAll(Arrays.asList(sPlsCntr));
      //      System.arraycopy(sPlsCntr,0,sPls,nbLignes,sPlsCntr.length);
      //      nbLignes+=sPlsCntr.length;
      //      nbCntr++;
    }
    sPts= new SPoint[_pts.length];
    for (int i= 0; i < _pts.length; i++)
      sPts[i]= FudaaPoint.gr2S(_pts[i]);
    sPls= new SPolyligne[vSPls.size()];
    vSPls.toArray(sPls);
    //-------  Triangulation 3D  -----------------------------------------------
    System.out.println("Nombre de points de topo : " + sPts.length);
    duPar.points(sPts);
    duPar.polylignes(sPls);
    duPar.regions(new SRegion[0]);
    duPar.trous(sTrous);
    duCal.optionC(true);
    duCal.optionQ(false);
    duCal.optionA(false);
    duCal.optionO(false);
    //    _op.setMessage("Triangulation 3D...",10);
    System.out.println("Triangulation 3D...");
    if(RefondeImplementation.CONNEXION_DUNES==null) {
      System.err.println("pas de connexion pour dunes");
    }

    duCal.calcul(RefondeImplementation.CONNEXION_DUNES);
    if (!duCal.estOK())
      return null;
    duRes=
      IResultatsDunesHelper.narrow(
        duCal.resultats(RefondeImplementation.CONNEXION_DUNES));
    return FudaaMaillageElement.s2Gr(duRes.maillage());
  }
}
