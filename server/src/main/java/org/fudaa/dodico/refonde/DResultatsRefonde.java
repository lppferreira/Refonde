/**
 * @file         DResultatsRefonde.java
 * @creation     1998-04-16
 * @modification $Date: 2006-09-19 14:45:59 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.dodico.refonde;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.fudaa.ctulu.CtuluLibString;

import org.fudaa.dodico.corba.refonde.IResultatsRefonde;
import org.fudaa.dodico.corba.refonde.IResultatsRefondeOperations;
import org.fudaa.dodico.corba.refonde.SResultatsEtapeRefonde;
import org.fudaa.dodico.corba.refonde.SResultatsLigneRefonde;
import org.fudaa.dodico.corba.refonde.SResultatsRefonde;

import org.fudaa.dodico.calcul.DResultats;
import org.fudaa.dodico.fortran.FortranReader;
import org.fudaa.dodico.fortran.FortranWriter;
import org.fudaa.dodico.objet.CDodico;

/**
 * Les Resultats Refonde.
 *
 * @version      $Revision: 1.9 $ $Date: 2006-09-19 14:45:59 $ by $Author: deniger $
 * @author       Philippe Sergent , Guillaume Desnoix, Bertrand Marchand
 */
public class DResultatsRefonde
  extends DResultats
  implements IResultatsRefonde,IResultatsRefondeOperations {

   String fichier_= "";
  private int nbNoeuds_;
//  private boolean hreg_= true;
  private SResultatsRefonde results_;
  /** Vector of steps : rendu non privee pour etre acceder depuis les inner classes sans artifact.*/
  Vector vsteps_;
  /** Vector of columns names. */
  Vector vnames_;
  /** Results reader */
  //private ResultsReader resReader_= null;
  /** Index in steps for iterator */
  //private int icptSteps_;
  /** If false, there is no more step to come  */
  //private boolean iteratorFilled_= true;

  /** Noms des r�sultats de houle. */
  public static final String[] NOM_RESULTS_HOULE={
    "Phase",
    "Hauteur_de_houle",
    "Bathym�trie",
    "Contrainte_Sxx",
    "Contrainte_Sxy",
    "Contrainte_Syy",
  };

  /** Noms des r�sultats de seiche. */
  public static final String[] NOM_RESULTS_SEICHE={
    "Coordonn�e_X",
    "Coordonn�e_Y",
    "Bathym�trie",
    "Niveau_d_eau"
  };

  public DResultatsRefonde() {
    super();
  }

  public Object clone() {
    final DResultatsRefonde r= new DResultatsRefonde();
//    r.setFichier(fichier_, nbNoeuds_, hreg_);
    r.setFichier(fichier_, nbNoeuds_);
    return r;
  }

  public String toString() {
    return "DResultatsRefonde("
      + fichier_
      + CtuluLibString.VIR
      + nbNoeuds_
//      + (hreg_ ? ",houle_r�guli�re" : ",houle_al�atoire")
      +")";
  }

  //--- Interface IResultatsRefonde { -----------------------------------------

  /**
   * @param _fichier Racine du fichier r�sultats � lire.
   * @param _nbNoeuds Nombre de noeuds du probl�me.
   * @param _hreg <i>true</i> La houle est r�guli�re, les r�sultats sont lus sur
   *              _fichier+"1.sol".<p>
   *              <i>false</i> La houle est al�atoire, les r�sultats sont lus
   *              sur _fichier+".sol".
   */
  public void setFichier(final String _fichier, final int _nbNoeuds) {
    fichier_= _fichier;
    nbNoeuds_= _nbNoeuds;
//    hreg_= _hreg;
    results_= null;
  }

  /**
   * Retourne les r�sultats calcul�s sous forme de structure.
   * @return La structure contenant les valeurs pour tous les noeuds � toutes
   *         les �tapes
   */
  public SResultatsRefonde resultatsRefonde() {
    if (results_ == null) {
      try {
//        results= litResultatsRefonde(new File(fichier_ + (hreg_ ? "1" : "")), nbNoeuds_);
        results_= litResultatsRefonde(new File(fichier_), nbNoeuds_);
        clearFichiers(fichier_);
      }
      catch (final IOException _exc) {}
    }
    return results_;
  }

  /**
   * Retourne les r�sultats calcul�s � une �tape sous forme de structure.
   * @param _instant Valeur du temps pour lequel on demande les r�sultats
   * @return La structure contenant les valeurs pour tous les noeuds � un
   *         instant donn� (null si aucune valeur n'a �t� calcul�e � cet instant)
   */
  public SResultatsEtapeRefonde resultatsEtapeRefonde(final long _instant) {
    resultatsRefonde();

    // Recherche du pas de temps
    for (int i= 0; i < results_.etapes.length; i++) {
      final SResultatsEtapeRefonde step= results_.etapes[i];
      if (step.instant == _instant) {
        return step; // Pas de temps trouv�
      }
    }
    // Pas de temps non trouv�
    CDodico.exception(
      this,
      "Etape non trouv�e pour le pas de temps : " + _instant);
    return null;
  }

  /**
   * Retourne les r�sultats calcul�s pour un noeud � une �tape sous forme de
   * structure.
   *
   * @param _instant Valeur du pas de temps
   * @param _n Numero du noeud pour lequel on demande les r�sultats
   * @return La structure contenant les valeurs pour un noeud � un instant
   *         donn� (null si aucune valeur n'a �t� calcul�e � cet instant)
   */
  public SResultatsLigneRefonde resultatsLigneRefonde(final long _instant, final int _n) {
    SResultatsLigneRefonde r= null;
    // R�cup�ration de l'�tape
    final SResultatsEtapeRefonde etape=resultatsEtapeRefonde(_instant);

    if (etape!=null) {
      try {
        r= etape.lignes[_n];
      } catch (final Exception ex) {
        CDodico.exception(
          this,"Noeud " + _n + " non trouv� au pas de temps " + _instant);
      }
    }
    return r;
  }

  //--- } Interface IResultatsRefonde -----------------------------------------

  // M�thodes locales
  /**
   * Ecriture des r�sultats sur fichiers.
   *
   * ATTENTION : Le format du fichier diff�re l�g�rement du fichier .SOL issu
   * de Refonde (noms des r�sultats en +), et ajout �ventuel de colonnes de
   * r�sultats.
   *
   * @param _fichiers Nom des fichiers.
   * @param _res R�sultats � �crire.
   */
  public static void ecritSurFichiers(
    final String _fichiers,
    final SResultatsRefonde _res) {
    ecritResultatsRefondeSOL(_fichiers, _res);
  }
  /**
   * Lecture des r�sultats sur fichiers.
   *
   * @param _fichier Nom du fichier r�sultats .sol sans extension
   * @param _nbNoeuds Nombre de noeuds du fichier.
   * @exception FileNotFoundException Fichier non trouv�.
   * @exception IOException           Erreur de lecture sur le fichier.
   * @return La structure contenant tous les valeurs � tous les noeuds de tous
   *         les pas de temps.
   */
  public static SResultatsRefonde litSurFichiers(final String _fichier,final int _nbNoeuds)
    throws IOException {
    return litResultatsRefonde(new File(_fichier), _nbNoeuds);
  }
  /**
   * Suppression des fichiers de r�sultats du calcul de nom donn�.
   *
   * @param _fichiers Nom des fichiers du projet sans l'extension.
   */
  static void clearFichiers(final String _fichiers) {
    new File(_fichiers + ".out").delete();
    new File(_fichiers + "1.sol").delete(); // Fichier houle r�guli�re
    new File(_fichiers + ".sol").delete(); // Fichier houle al�atoire
  }
  /**
   * Suppression des fichiers de reprise du calcul de nom donn�.
   *
   * @param _fichiers Nom des fichiers du projet sans l'extension.
   */
  static void clearFichiersReprise(final String _fichiers) {
    final FilenameFilter fltSolRep= new FilenameFilter() {
      public boolean accept(File _dir, String _name) {
        boolean r;
        String racine= new File(_fichiers).getName();
        r=
          _name.startsWith(racine)
            && _name.endsWith(".sol")
            && _name.length() == racine.length() + 10
            && _name.charAt(racine.length()) == 't'
            && _name.charAt(racine.length() + 3) == 'd';
        return r;
      }
    };
    final File[] fics= new File(_fichiers).getParentFile().listFiles(fltSolRep);
    for (int i= 0; i < fics.length; i++) {
      fics[i].delete();
    }
    new File(_fichiers + ".don").delete();
  }
  /**
   * Ecriture des r�sultats sur fichier .sol.
   *
   * @param _fichier Nom du fichier .spe
   * @param _res Parametres � �crire.
   */
  private static void ecritResultatsRefondeSOL(
    final String _fichier,
    final SResultatsRefonde _res) {

    int[] fmt;

    try {
      final FortranWriter file= new FortranWriter(new FileWriter(_fichier));

      // Pas de chaines limit�es par des apostrophes en format libre
      file.setStringQuoted(false);

      final SResultatsEtapeRefonde[] etapes=_res.etapes;
      for (int ipas=0; ipas<etapes.length; ipas++) {

        // " -999"
        file.stringField(0, " -999");
        file.writeFields();

        // " ===== PAS NUMERO :",<pas>," ITERATION NUMERO :",<iter=1>," TEMPS ",<temps>
        fmt= new int[] { 19, 5, 19, 5, 19, 12 };
        file.stringField(0, " ===== PAS NUMERO :");
        file.intField(1, ipas+1);
        file.stringField(2, " ITERATION NUMERO :");
        file.intField(3, 1);
        file.stringField(4, "    TEMPS          ");
        file.floatField(5, etapes[ipas].instant);
        file.writeFields(fmt);

        // "   -1",<nombre de r�sultats>
        fmt= new int[] { 5, 5 };
        file.intField(0, -1);
        file.intField(1, _res.noms.length);
        file.writeFields(fmt);

        // Noms des r�sultats ("<noms>",<noms des r�sultats>) (si 1er pas de temps)
        if (ipas==0) {
          file.stringField(0, "<noms>");
          for (int i= 0; i < _res.noms.length; i++) {
            file.stringField(i + 1, _res.noms[i]);
          }
          file.writeFields();
        }

        // Valeurs des r�sultats
        fmt= new int[1 + _res.noms.length];
        fmt[0]= 10; for (int i= 0; i < _res.noms.length; i++) {
          fmt[i + 1]= 12;
        }

        for (int i= 0; i <etapes[ipas].lignes.length; i++) {
          file.intField(0, i + 1);

          for (int j= 0; j < etapes[ipas].lignes[i].valeurs.length; j++) {
            file.doubleField(j + 1, etapes[ipas].lignes[i].valeurs[j]);
          }
          file.writeFields(fmt);
        }
      }
      file.flush();
      file.close();
    } catch (final IOException ex) {
      CDodico.exception(DResultatsRefonde.class, ex);
    }
  }

/*  private static SResultatsRefonde litResultatsRefonde(
    String fichier,
    int nbNoeuds) {

    try {
      int i, j;
      int[] fmt;
      int nbCols;
      int formatCol1;
      // System.out.println("nombre de noeuds: "+nbNoeuds);

      FortranReader fsol= new FortranReader(new FileReader(fichier + ".sol"));
      SResultatsRefonde r= new SResultatsRefonde();
      r.lignes= new SResultatsLigneRefonde[nbNoeuds];
      fmt= new int[] { 5 };
      fsol.readFields(fmt);
      j= fsol.intField(0);
      if (j != -999)
        throw new Exception("SOL Debut de fichier attendu (-999)");
      fmt= new int[] { 6, 13, 5, 19, 5 };
      fsol.readFields(fmt);
      j= fsol.intField(2);
      System.out.println("SOL Pas       : " + j);
      j= fsol.intField(4);
      System.out.println("SOL Iteration : " + j);
      fmt= new int[] { 5, 5 };
      fsol.readFields(fmt);
      j= fsol.intField(0);
      if (j != -1)
        System.out.println("SOL -1 attendu et non " + j);
      nbCols= fsol.intField(1);
      System.out.println("SOL Colonnes  : " + nbCols);
      // @todo Pour l'instant, on suppose qu'on est toujours en houle r�guli�re
      // B.M. 10/08/2001 D�tection des noms de colonnes
      fsol.mark(1000);
      fsol.readFields();
      if (fsol.stringField(0).equals("<noms>")) {
        r.noms= new String[nbCols];
        for (i= 0; i < nbCols; i++)
          r.noms[i]= fsol.stringField(i + 1);
      }
      // D�finition des noms par d�faut + lecture des r�sultats int�ressants
      else {
        nbCols= nomResultats.length;
        r.noms= new String[nbCols];
        for (i= 0; i < nbCols; i++)
          r.noms[i]= nomResultats[i];
        fsol.reset();
      }
      // B.M. 25/04/2001 D�tection de la longueur des champs des num�ros
      // de noeuds (5 ou 10)
      fsol.mark(1000);
      String ligne1= fsol.readLine();
      if (ligne1.startsWith("     "))
        formatCol1= 10;
      else
        formatCol1= 5;
      fsol.reset();
      fmt= new int[nbCols + 1];
      fmt[0]= formatCol1;
      for (j= 1; j < fmt.length; j++)
        fmt[j]= 12;
      //	fsol.readFields(fmt);
      //      if( formatCol1==10 ) {
      //        String ligne1=fsol.getLine();
      //        if( ((ligne1.charAt(5)==' ')||(ligne1.charAt(5)=='-'))&&(ligne1.charAt(6)!=' ') ) {
      //          formatCol1=5;
      //          fsol.close();
      //          return litResultatsRefonde(fichier, nbNoeuds);
      //        }
      //      }
      System.err.println("### Format col 1 SOL: " + formatCol1);
      for (i= 0; i < nbNoeuds; i++) {
        fsol.readFields(fmt);
        //      for(i=0;i<nbNoeuds-1;i++) {
        j= fsol.intField(0);
        if (j != (i + 1))
          throw new Exception("SOL Decalage en ligne : " + (i + 1));
        // System.out.print("SOL "+i+": ");
        r.lignes[i]= new SResultatsLigneRefonde();
        r.lignes[i].n= fsol.intField(0);
        r.lignes[i].valeurs= new double[nbCols];
        for (j= 0; j < nbCols; j++)
          r.lignes[i].valeurs[j]= fsol.doubleField(j + 1);
        //        r.lignes[i].sinusPhaseHauteur=fsol.doubleField(1);
        //        r.lignes[i].moduleHauteur    =fsol.doubleField(2);
        //        r.lignes[i].profondeur       =fsol.doubleField(3);
        //        r.lignes[i].x1               =fsol.doubleField(4);
        //        r.lignes[i].phaseHauteur     =fsol.doubleField(5);
        //        r.lignes[i].x2               =fsol.doubleField(6);
        //        r.lignes[i].x3               =fsol.doubleField(7);
        //        r.lignes[i].x4               =fsol.doubleField(8);
        //        fsol.readFields(fmt);
      }
//            fmt = new int[] { 5 };
//            fsol.readFields(fmt);
//
//            j=fsol.intField(0);
//            if(j!=-1)
//      	throw new Exception("Fin de fichier attendue (-1)");

      //      fsol=null;
      fsol.close();
      return r;
    } catch (Exception ex) {
      CDodico.exception(DResultatsRefonde.class, ex);
    }
    return null;
  }*/


  /**
   * Lit les resultats du calcul par Refonde ou Seiche. Cette m�thode peut �tre
   * appel�e aussi bien par le serveur de calcul que par l'application cliente
   * (pour lire des fichiers r�sultats locaux).
   *
   * @param _file  Le fichier de solutions contenant les r�sultats. Le fichier
   *                 a comme extension .sol
   * @param _nbNds Nombre de noeuds sur le fichier des solutions. Ce nombre
   *                 devrait correspondre au nombre de noeuds du maillage du
   *                 projet.
   *
   * @return La structure contenant les valeurs pour tous les noeuds � toutes
   *         les �tapes.
   * @exception FileNotFoundException Fichier non trouv�.
   * @exception IOException           Erreur de lecture sur le fichier.
   */
  public static SResultatsRefonde litResultatsRefonde(final File _file, final int _nbNds)
    throws IOException {

    final Vector vsteps=new Vector();
    final Vector vnames=new Vector();

    readSteps(new File(_file.getPath()+".sol"), _nbNds, vsteps, vnames, new ResultsReader());

    // Fin du fichier => Stockage des �tapes dans la structure ad�quate
    final SResultatsRefonde resultats=
      new SResultatsRefonde(
        new String[vnames.size()],new SResultatsEtapeRefonde[vsteps.size()]);
    vsteps.toArray(resultats.etapes);
    vnames.toArray(resultats.noms);

    return resultats;
  }

  /**
   * Read step synchronously or asynchronously.
   *
   * @param _file   Le fichier de solutions contenant les r�sultats. Le fichier
   *                peut avoir n'importe quelle extension.
   * @param _nbNds  Nombre de noeuds sur le fichier des solutions. Ce nombre
   *                devrait correspondre au nombre de noeuds du maillage du
   *                projet.
   * @param _vsteps Vector of steps to fill.
   * @param _vnames Vector of columns names.
   */
   static void readSteps(
    final File _file,
    final int _nbNds,
    final Vector _vsteps,
    final Vector _vnames,
    final ResultsReader _reader)

    throws IOException {

    // Fichier de lecture
    FortranReader file= null;
    _vsteps.clear();
    _vnames.clear();

    try {
      // Ouverture du fichier des solutions
      file= new FortranReader(new FileReader(_file));
      file.setBlankZero(true);

      // Valeur du pas de temps lu sur fichier
      //      double temps;
      // Nombre de valeurs � lire sur le fichier
      int nbValeurs= 0;
      // Formats
      int[] fmt;
      // Num�ros sur 5 ou 10 colonnes
      int formatCol1=5;

      // Lecture des �tapes (-999)
      for (;;) {
        try {
          while (!file.ready() && _reader.waitForResults_) {
            try {
              Thread.sleep(100);
            } catch (final InterruptedException e) {}
          }
          fmt= new int[] { 5 };
          file.readFields(fmt);
        }
        // Fin du fichier => Plus d'�tapes
        catch (final EOFException ex) {
          break;
        }
        if (file.intField(0) != -999) {
          throw new IOException();
        }

        // Cr�ation d'une nouvelle �tape
        final SResultatsEtapeRefonde etape= new SResultatsEtapeRefonde();
        etape.lignes= new SResultatsLigneRefonde[_nbNds];

        // (' ===== PAS NUMERO :',<pas de temps>,' ITERATION NUMERO :',
        //  <numero d'it�ration>,'TEMPS',<valeur de temps>)
        fmt= new int[] { 19, 5, 19, 5, 19, 12 };
        while (!file.ready() && _reader.waitForResults_) {
          try {
            Thread.sleep(100);
          } catch (final InterruptedException e) {}
        }

        file.readFields(fmt);
        etape.instant= file.doubleField(5);

        DCalculRefonde.message("Fichier r�sultats Pas: "+file.intField(1)+
                               " Iteration: "           +file.intField(3)+
                               " Temps: "               +file.doubleField(5));

        /** @todo Voir si possibilit� de corriger la sortie de Seiche */
        // Position du -1 (si sortie de l'exe de seiche, -1 en I6, si refonde,
        // -1 en I5 !!!)
        file.mark(1000);
        fmt=new int[]{4,2};
        file.readFields(fmt);
        final boolean bmodseiche=file.stringField(1).equals("-1");
        file.reset();

        // (-1,<nombre de valeurs>)
        fmt= new int[] { bmodseiche ? 6:5, 5 };
        while (!file.ready() && _reader.waitForResults_) {
          try {
            Thread.sleep(100);
          } catch (final InterruptedException e) {}
        }

        file.readFields(fmt);
        if (file.intField(0) != -1) {
          throw new IOException();
        }
        nbValeurs= file.intField(1);

        // Seulement lors de la premi�re �tape
        if (_vsteps.size() < 1) {
          DCalculRefonde.message("Fichier r�sultats Colonnes : " + nbValeurs);

          // @todo Pour l'instant, on suppose qu'on est toujours en houle r�guli�re
          // B.M. 10/08/2001 D�tection des noms de colonnes
          file.mark(1000);
          file.readFields();
          if (file.stringField(0).equals("<noms>")) {
            for (int i=0; i < nbValeurs; i++) {
              _vnames.add(file.stringField(i+1));
            }
          }

          // fichier .sol issu du serveur : Les noms ne sont pas donn�es.
          // D�finition des noms par d�faut + lecture des r�sultats int�ressants
          else {
            if (bmodseiche) {
              nbValeurs=4;
              _vnames.addAll(Arrays.asList(NOM_RESULTS_SEICHE));
            }
            else {
              nbValeurs=6;
              _vnames.addAll(Arrays.asList(NOM_RESULTS_HOULE)); // Les colonnes 7->9 n'ont pas d'interet.
            }
            file.reset();
          }

          // B.M. 25/04/2001 D�tection de la longueur des champs des num�ros
          // de noeuds (5 ou 10)
          file.mark(1000);
          final String ligne1= file.readLine();
          if (ligne1.startsWith("     ")) {
            formatCol1= 10;
          } else {
            formatCol1= 5;
          }
          file.reset();

          DCalculRefonde.message("Fichier r�sultats format : I"+formatCol1);
        }

        // Cr�ation des lignes de l'�tape
        for (int i= 0; i < _nbNds; i++) {
          etape.lignes[i]= new SResultatsLigneRefonde();
          etape.lignes[i].valeurs= new double[nbValeurs];
        }

        // Lecture des lignes (<numero de noeud>,<valeurs>)
        fmt=new int[nbValeurs+1];
        fmt[0]=formatCol1; for (int i=1; i<nbValeurs+1; i++) {
          fmt[i]=12;
        }

        for (int i=0; i<_nbNds; i++) {
          while (!file.ready() && _reader.waitForResults_) {
            try {
              Thread.sleep(100);
            } catch (final InterruptedException e) {}
          }

          file.readFields(fmt);
          etape.lignes[i].n=file.intField(0);
          for (int j=0; j<nbValeurs; j++) {
            etape.lignes[i].valeurs[j]=file.doubleField(j+1);
          }
        }
        _vsteps.add(etape);
      }

      // Fermeture du fichier
      file.close();
    }
    catch (final FileNotFoundException _exc) {
      throw new FileNotFoundException("Erreur d'ouverture du fichier " + _file);
    }
    catch (final IOException e) {
      throw new IOException(
        "Erreur de lecture sur fichier "
          + _file
          + " ligne "
          + (file==null?-1:file.getLineNumber()));
    }
  }

  /**
   * A reader for asynchronous read the results. When a new step is written
   * in results file, it is load in vector of steps.
   */
  private static class ResultsReader extends Thread {

    /** Dernier caract�re des extensions pour les fichiers de calcul. */
    private static final String[] EXTENSIONS= { "v", "v", "t" };

    private DResultatsRefonde results_;
     boolean waitForResults_;

    /** Nombre de noeuds du domaine calcul� (nombre d'enregistrements pour chaque �tape). */
    private int nbNds_;

    /** Type de probl�me (sert notamment pour l'extension du fichier de r�sultat). */
    private int tpPb_;

    public ResultsReader(final DResultatsRefonde _res) {
      results_= _res;
    }

    public ResultsReader() {}

    public void run() {
      waitForResults_= true;
      //results_.iteratorFilled_= false;
      final String racine= results_.fichier_;
      final String path=
        System.getProperty("user.dir")
          + File.separator
          + "serveurs"
          + File.separator
          + "reflux"
          + File.separator;
      try {
        // Read informations in file .dat
        litData(new File(path + racine + ".dat"));
      } catch (final IOException _exc) {
        System.out.println(
          "Erreur de lecture du fichier " + new File(path + racine + ".dat"));
      }
      // Wait for result file exist.
      final File fsol= new File(path + racine + ".so" + EXTENSIONS[tpPb_]);
      while (!fsol.exists() && waitForResults_) {
        try {
          sleep(100);
        } catch (final InterruptedException e) {}
      }
      try {
        readSteps(fsol, nbNds_, results_.vsteps_, results_.vnames_, this);
      } catch (final IOException _exc) {
        System.out.println(_exc.getMessage());
      }
      // Notify the is no more results to come
      //results_.iteratorFilled_= true;
    }
    /**
     * Lit les donn�es non stock�es sur les fichiers r�sultats mais n�cessaires
     * � la lecture des fichiers r�sultats.
     * @param _file Le nom du fichier .dat
     */
    private void litData(final File _file) throws IOException {
      FileInputStream file= null;
      // Ouverture du fichier
      file= new FileInputStream(_file);
      // Propri�t�s
      final Properties prs= new Properties();
      prs.load(file);
      file.close();
      // Remplissage des variables
      nbNds_= Integer.parseInt(prs.getProperty("nbNoeuds", "0"));
      tpPb_= Integer.parseInt(prs.getProperty("tpProbleme", "0"));
    }
    /**
     * Ask the reader to stop when analysis is finished.
     */
    public void stopNeeded() {
      waitForResults_= false;
    }
  }
}
