/**
 *  @creation     2 juil. 2004
 *  @modification $Date: 2006-09-19 14:45:59 $
 *  @license      GNU General Public License 2
 *  @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
* @mail devel@fudaa.fr
 */
package org.fudaa.dodico.refonde;

import org.fudaa.dodico.corba.refonde.IParametresRefondeINP;
import org.fudaa.dodico.corba.refonde.IParametresRefondeINPOperations;
import org.fudaa.dodico.corba.refonde.SParametresRefondeConnectivite;
import org.fudaa.dodico.corba.refonde.SParametresRefondeDomainePoreux;
import org.fudaa.dodico.corba.refonde.SParametresRefondeINPBase;
import org.fudaa.dodico.corba.refonde.SParametresRefondeLignePE;
import org.fudaa.dodico.corba.refonde.SParametresRefondeLigneXYZ;
import org.fudaa.dodico.corba.refonde.SParametresRefondeSeiche;

import org.fudaa.dodico.objet.DObjet;


/**
 * @author Fred Deniger
 * @version $Id: DParametresRefondeINP.java,v 1.3 2006-09-19 14:45:59 deniger Exp $
 */
public class DParametresRefondeINP extends DObjet implements IParametresRefondeINPOperations,IParametresRefondeINP {
  
  /**
   * Par def.
   */
  public DParametresRefondeINP() {
    super();
  }

  SParametresRefondeLigneXYZ[] xyz_;
  public SParametresRefondeLigneXYZ[] xyz(){
    return xyz_;
  }

  public void xyz(final SParametresRefondeLigneXYZ[] _newXyz){
  xyz_=_newXyz;  
  }
  
  SParametresRefondeConnectivite[] c_;
  public SParametresRefondeConnectivite[] connectivites(){
    return c_;
  }

  public void connectivites(final SParametresRefondeConnectivite[] _newConnectivites){
  c_=_newConnectivites;  
  }
  
  int[] type_;

  /**
   * @see org.fudaa.dodico.corba.refonde.IParametresRefondeINPOperations#type()
   */
  public int[] type(){
    return type_;
  }

  /**
   * @see org.fudaa.dodico.corba.refonde.IParametresRefondeINPOperations#type(int[])
   */
  public void type(final int[] _newType){
    type_=_newType;
    }
  int[] groupePE_;
  public int[] groupePE(){
    return groupePE_;
  }

  /**
   * @see org.fudaa.dodico.corba.refonde.IParametresRefondeINPOperations#groupePE(int[])
   */
  public void groupePE(final int[] _newGroupePE){
  groupePE_=_newGroupePE;  
  }
  SParametresRefondeLignePE[] valeurPE_;
  public SParametresRefondeLignePE[] valeurPE(){
    return valeurPE_;
  }

  public void valeurPE(final SParametresRefondeLignePE[] _newValeurPE){
  valeurPE_=_newValeurPE;
  }
  SParametresRefondeDomainePoreux[] domainePoreux_;
  public SParametresRefondeDomainePoreux[] domainePoreux(){
    return domainePoreux_;
  }

  public void domainePoreux(final SParametresRefondeDomainePoreux[] _newDomainePoreux){
  domainePoreux_=_newDomainePoreux;  
  }
  SParametresRefondeSeiche seiche_;
  public SParametresRefondeSeiche seiche(){
    return seiche_;
  }

  public void seiche(final SParametresRefondeSeiche _newSeiche){}

  SParametresRefondeINPBase donneesBase_;
  public SParametresRefondeINPBase donneesBase(){
    return donneesBase_;
  }

  public void donneesBase(final SParametresRefondeINPBase _newDonneesBase){
  donneesBase_=_newDonneesBase;  
  }


}
