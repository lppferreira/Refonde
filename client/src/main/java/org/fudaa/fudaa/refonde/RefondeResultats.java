/*
 * @file         RefondeResultats.java
 * @creation     1999-10-28
 * @modification $Date: 2006-12-05 10:18:13 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.fudaa.dodico.corba.refonde.SResultatsEtapeRefonde;
import org.fudaa.dodico.corba.refonde.SResultatsLigneRefonde;
import org.fudaa.dodico.corba.refonde.SResultatsRefonde;

import org.fudaa.dodico.refonde.DResultatsRefonde;
/**
 * Une classe de résultats du projet. Contient les résultats aux noeuds pour le
 * projet.
 *
 * @version      $Id: RefondeResultats.java,v 1.8 2006-12-05 10:18:13 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeResultats {
  /** @todo A mettre plutot dans CResultatsRefonde dés que possible */
  public static final int PHASE= 0;
  public static final int HAUTEUR_HOULE= 1;
  public static final int BATHYMETRIE= 2;
  public static final int SXX= 3;
  public static final int SXY= 4;
  public static final int SYY= 5;
  public static final int DIR_MOYENNE= 6;
  public static final int DIFF_HAUTEUR= 7;
  public static final int DIFF_SXX= 8;
  public static final int DIFF_SXY= 9;
  public static final int DIFF_SYY= 10;

  public static final String[] nomResultats=
  {
      "Phase",
      "Hauteur_de_houle",
      "Bathymétrie",
      "Contrainte_Sxx",
      "Contrainte_Sxy",
      "Contrainte_Syy",
      "Direction_moyenne",
      "Diff_de_hauteurs_de_houle",
      "Diff_de_contraintes_Sxx",
      "Diff_de_contraintes_Sxy",
      "Diff_de_contraintes_Syy"
  };

  /** Vecteur<String> des noms des colonnes */
  private Vector vnomRes_= new Vector();
  /** Vecteur<double> des instants d'étapes */
  private Vector vinstants_=new Vector();
  /** Vecteur"Etapes"<Vecteur"Colonnes"<double[]"Valeurs_noeuds">> contenant les
      valeurs de résultats. */
  private Vector vres_= new Vector();

  boolean modifie;

  /**
   * Lecture des résultats depuis un fichier.
   *
   * @param _fichier Nom du fichier de résultats.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public static RefondeResultats ouvrir(File _fichier, int _nbNoeuds)
    throws IOException {

    RefondeResultats p= new RefondeResultats();
    String fichier= _fichier.getPath();
    fichier= fichier.substring(0, fichier.lastIndexOf(".sol"));
    SResultatsRefonde res= DResultatsRefonde.litSurFichiers(fichier, _nbNoeuds);
    p.fromStruct(res);

    return p;
  }

  /**
   * Enregistrement des résultats.
   * @param _fichier Nom du fichier des résultats.
   */
  public void enregistrer(RefondeProjet _projet, File _fichier) {
    DResultatsRefonde.ecritSurFichiers(_fichier.getPath(), toStruct());
  }

  /**
   * Initialisation des résultats depuis une structure IDL.
   * @param _res La structure IDL en provenance du serveur.
   */
  public void fromStruct(SResultatsRefonde _res) {
    supprimeTout();

    for (int i=0; i<_res.etapes.length; i++) {
      double instant=_res.etapes[i].instant;
      double[][] vals=new double[_res.etapes[i].lignes[0].valeurs.length]
                                [_res.etapes[i].lignes.length];

      for (int j=0; j<_res.etapes[i].lignes.length; j++) {
        for (int k=0; k<_res.etapes[i].lignes[j].valeurs.length; k++) {
          vals[k][j]=_res.etapes[i].lignes[j].valeurs[k];
        }
      }
      ajouteEtape(instant,vals);
    }

    setNomsColonnes(_res.noms);
  }

  /**
   * Initialisation de la strucute IDL equivalente.
   * @return La structure IDL au format serveur.
   */
  public SResultatsRefonde toStruct() {
    SResultatsRefonde r=new SResultatsRefonde();
    r.noms=getNomsColonnes();

    r.etapes=new SResultatsEtapeRefonde[getNbEtapes()];
    for (int i=0; i<getNbEtapes(); i++) {
      double[][] vals=getEtape(i);
      r.etapes[i]=new SResultatsEtapeRefonde();

      r.etapes[i].lignes=new SResultatsLigneRefonde[vals[0].length];
      for (int j=0; j<vals[0].length; j++) {
        r.etapes[i].lignes[j]=new SResultatsLigneRefonde();

        r.etapes[i].lignes[j].valeurs=new double[vals.length];
        for (int k=0; k<vals.length; k++) {
          r.etapes[i].lignes[j].valeurs[k]=vals[k][j];
        }
      }
      r.etapes[i].instant=getInstantEtape(i);
    }

    return r;
  }

  /**
   * Ajout d'un résultat. Ajoute un résultat à la suite des résultats existants.
   * @param _valeurs Le nombre de valeurs est considéré comme étant exact.
   * @deprecated Pour le cas ou plusieurs pas de temps existent, les valeurs
   *             sont introduites à chaque fois.
   */
  public void ajouteResultat(String _nom, double[] _valeurs) {
    vnomRes_.add(_nom);
    for (int i=0; i<vres_.size(); i++) {
      ((Vector)vres_.get(i)).add(_valeurs);
    }
//    vres_.add(_valeurs);
  }

  /**
   * Suppression d'un résultat. Supprime le résultat de nom désigné. Ceci
   * implique que le nom du résultat est unique.
   * @param _nom Nom du résultat à supprimer. Si le résultat n'existe pas,
   *             la méthode ne fait rien.
   * @deprecated Utiliser supprimeColonne à la place
   */
  public void supprimeResultat(String _nom) {
    supprimeColonne(indexOfColonne(_nom));
/*    int ind= vnomRes_.indexOf(_nom);
    if (ind >= 0) {
      vnomRes_.remove(ind);
      vres_.remove(ind);
    }*/
  }

  /**
   * Retourne les valeurs de résultat de nom donné. On considère un seul pas de
   * temps.
   *
   * @param _nom Le nom de résultats pour lequel on désire avoir les valeurs.
   * @return Les valeurs de résultats ou <i>null</i> si aucun résultat pour le
   *         nom.
   * @deprecated
   */
  public double[] getResultat(String _nom) {
    int ind= vnomRes_.indexOf(_nom);
    return ind >= 0 ? (double[])((Vector)vres_.get(0)).get(ind) : null;
  }

  /**
   * Retourne les noms de résultats existants.
   * @deprecated
   */
  public String[] getNomsResultats() {
    return (String[])vnomRes_.toArray(new String[0]);
  }

  // Nouvelle API --------------------------------------------------------------

  /**
   * Ajoute une étape aux résultats. L'étape est ajoutée en fin sans respect
   * d'un ordre croissant des instants.
   * @param _t L'instant de l'étape.
   * @param _v Les valeurs pour cette étapes. _v[iColonne][iNoeud];
   */
  public void ajouteEtape(double _t, double[][] _v) {
    // Première fois : Ajout des noms de colonne par défaut.
    if (vres_.size()==0) {
      for (int i=0; i<_v.length; i++) vnomRes_.add("Col"+i);
    }

    Vector vcols=new Vector();
    vcols.addAll(Arrays.asList(_v));
    vres_.add(vcols);
    vinstants_.add(new Double(_t));
  }

  /**
   * Ajoute une colonne aux résultats.
   * @param _s Le nom de la colonne.
   * @param _v  Les valeurs pour cette colonne. _v[iEtape][iNoeud];
   */
  public void ajouteColonne(String _s, double[][] _v) {
    // Première fois : Ajout des étapes avec des instants par défaut.
    if (vres_.size()==0) {
      for (int i=0; i<_v.length; i++) {
        vres_.add(new Vector());
        vinstants_.add(new Double(i));
      }
    }

    for (int i=0; i<_v.length; i++) ((Vector)vres_.get(i)).add(_v[i]);
    vnomRes_.add(_s);
  }

  /**
   * Supprime une étape. Si l'indice est hors de la table, ne fait rien.
   * @param _i L'indice de l'étape dans la table.
   */
  public void supprimeEtape(int _i) {
    if (_i<0 || _i>=vres_.size()) return;

    vres_.remove(_i);
    vinstants_.remove(_i);

    synchoniseVecteurs();
  }

  /**
   * Supprime une colonne. Si l'indice est hors de la table, ne fait rien.
   * @param _i L'indice de la colonne dans la table.
   */
  public void supprimeColonne(int _i) {
    if (vres_.size()==0) return;

    for (int i=0; i<vres_.size(); i++) {
      Vector vcols=(Vector)vres_.get(i);
      if (_i<0 || _i>=vcols.size()) return;
      vcols.remove(_i);
    }
    vnomRes_.remove(_i);

    synchoniseVecteurs();
  }

  /**
   * Retourne le nombre d'étapes des résultats.
   * @return Le nombre d'étapes.
   */
  public int getNbEtapes() {
    return vinstants_.size();
  }

  /**
   * Retourne le nombre de colonnes des résultats.
   * @return Le nombre de colonnes.
   */
  public int getNbColonnes() {
    return vnomRes_.size();
  }

  /**
   * Définit le nom de toutes les colonnes. Le nombre de noms doit correspondre
   * au nombre de colonnes déjà stockées.
   * @param _n Le nom des colonnes dans l'ordre. Ce nombre doit corrspandre
   */
  public void setNomsColonnes(String[] _n) {
    vnomRes_.clear();
    vnomRes_.addAll(Arrays.asList(_n));
  }

  /**
   * Retourne le nom de toutes les colonnes.
   * @return Le nom des colonnes dans l'ordre.
   */
  public String[] getNomsColonnes() {
    return (String[])vnomRes_.toArray(new String[0]);
  }

  /**
   * Définit les instants pour toutes les étapes. Le nombre d'instants doit
   * correspondre au nombre d'étapes déjà stockées.
   * @param _n Le nom des colonnes dans l'ordre.
   */
  public void setInstantsEtapes(double[] _n) {
    vinstants_.clear();
    for (int i=0; i<_n.length; i++) vinstants_.add(new Double(_n[i]));
  }

  /**
   * Retourne les instants pour toutes les étapes.
   * @return Les instants des étapes.
   */
  public double[] getInstantsEtapes() {
    double[] r=new double[vinstants_.size()];
    for (int i=0; i<vinstants_.size(); i++)
     r[i]=((Double)vinstants_.get(i)).doubleValue();

    return r;
  }

  /**
   * Supprime tous les résultats.
   */
  public void supprimeTout() {
    vres_.clear();
    synchoniseVecteurs();
  }

  /**
   * Retourne la valeur de l'instant de l'étape. -1 si l'indice est hors tableau.
   * @param _i L'indice de l'étape dans la table.
   * @return La valeur de l'instant.
   */
  public double getInstantEtape(int _i) {
    if (_i<0 || _i>=vinstants_.size()) return -1;
    return ((Double)vinstants_.get(_i)).doubleValue();
  }

  /**
   * Retourne le nom de la colonne. null si l'indice est hors tableau.
   * @param _i L'indice de la colonne dans la table.
   * @return Le nom de la colonne.
   */
  public String getNomColonne(int _i) {
    if (_i<0 || _i>=vnomRes_.size()) return null;
    return (String)vnomRes_.get(_i);
  }

  /**
   * Retourne les valeurs d'une étape. null si l'indice est hors tableau.
   * @param _i L'indice de l'étape dans la table.
   * @return Les valeurs pour cette étape. double[iColonne][iNoeud];
   */
  public double[][] getEtape(int _i) {
    double[][] r=null;
    if (_i<0 || _i>=vinstants_.size()) return null;

    Vector vcols=(Vector)vres_.get(_i);
    r=(double[][])vcols.toArray(new double[0][]);

    return r;
  }

  /**
   * Retourne les valeurs d'une colonne.
   * @param _i L'indice de la colonne dans la table.
   * @return Les valeurs pour cette colonne. double[iEtape][iNoeud];
   */
  public double[][] getColonne(int _i) {
    double[][] r=null;
    if (_i<0 || _i>=vnomRes_.size()) return null;

    r=new double[vres_.size()][];
    for (int i=0; i<vres_.size(); i++) {
      Vector vcols=(Vector)vres_.get(i);
      r[i]=((double[])vcols.get(_i));
    }
    return r;
  }

  /**
   * Retourne l'index de la première colonne correspondant au nom donné. -1 si
   * la colonne n'existe pas.
   * @param _s Le nom de la colonne
   * @return L'indice de la colonne dans la table.
   */
  public int indexOfColonne(String _s) {
    return vnomRes_.indexOf(_s);

//    for (int i=0; i<vnomRes_.size(); i++) {
//      if (((String)vnomRes_.get(i)).equals(_s)) return i;
//    }
//    return -1;
  }

  /**
   * Retourne l'index de la première étape correspondant à l'instant donné. -1
   * si l'étape n'existe pas.
   * @param _t L'instant de l'étape.
   * @return L'indice de l'étape dans la table.
   */
  public int indexOfEtape(double _t) {
    return vinstants_.indexOf(new Double(_t));

//    for (int i=0; i<vinstants_.size(); i++) {
//      if (((Double)vinstants_.get(i)).doubleValue()==_t) return i;
//    }
//    return -1;
  }

  /**
   * Pour effacer le contenu des vecteurs d'instant et de nom de colonne si le
   * vecteur de valeurs ne contient plus rien.
   */
  private void synchoniseVecteurs() {
    if (vres_.size()==0 || ((Vector)vres_.get(0)).size()==0) {
      vres_.clear();
      vinstants_.clear();
      vnomRes_.clear();
    }
  }
}
