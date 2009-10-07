/**
 * @file         DCalculRefonde.java
 * @creation     1998-04-16
 * @modification $Date: 2006-09-19 14:45:59 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 */
package org.fudaa.dodico.refonde;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.fudaa.ctulu.CtuluLibMessage;

import org.fudaa.dodico.corba.objet.IConnexion;
import org.fudaa.dodico.corba.refonde.ICalculRefonde;
import org.fudaa.dodico.corba.refonde.ICalculRefondeOperations;
import org.fudaa.dodico.corba.refonde.IParametresRefonde;
import org.fudaa.dodico.corba.refonde.IParametresRefondeHelper;
import org.fudaa.dodico.corba.refonde.IResultatsRefonde;
import org.fudaa.dodico.corba.refonde.IResultatsRefondeHelper;
import org.fudaa.dodico.corba.refonde.SParametresRefondeINP;

import org.fudaa.dodico.calcul.DCalcul;
import org.fudaa.dodico.objet.CDodico;
/**
 * Une encapsulation du programme Refonde ecrit en Fortran.
 *
 * @version      $Revision: 1.15 $ $Date: 2006-09-19 14:45:59 $ by $Author: deniger $
 * @author       Bertrand Marchand
 */
public class DCalculRefonde
  extends DCalcul
  implements ICalculRefonde,ICalculRefondeOperations {
  private StringBuffer traceBuffer_= new StringBuffer();
  private boolean succes_;
  private boolean arretDemande_;
  private Process proc_; // Sous process d'exécution du programme.
  private boolean saveReprise_;

  private final static int CAL_H_REG         =DParametresRefonde.HOULE_REG;
  private final static int CAL_H_ALEA        =DParametresRefonde.HOULE_ALEA;
  private final static int CAL_H_ALEA_REPRISE=DParametresRefonde.HOULE_ALEA_REPRISE;
  private final static int CAL_SEICHE        =DParametresRefonde.MODULE_SEICHE;


  public DCalculRefonde() {
    super();
  }
  public int dureeEstimee(final IConnexion _c) {
    int t= 0;
    final IParametresRefonde params= IParametresRefondeHelper.narrow(parametres(_c));
    if (params != null) {
      final SParametresRefondeINP p= params.parametresINP();
      t= p.nbElements / 333;
    }
    return t;
  }
  public String description() {
    return "Refonde, serveur de calcul pour la houle: " + super.description();
  }

  /**
   * Calcul par l'intermédiaire des exe refonde1 et refonde2 ou seiche.
   */
  public void calcul(final IConnexion _c) {
    succes_= false;
    if (!verifieConnexion(_c)) {
      return;
    }

    final IParametresRefonde params= IParametresRefondeHelper.narrow(parametres(_c));
    if (params == null) {
      CDodico.exceptionAxel(this, new Exception("params non definis (null)"));
      return;
    }
    final IResultatsRefonde results= IResultatsRefondeHelper.narrow(resultats(_c));
    if (results == null) {
      CDodico.exceptionAxel(this, new Exception("results non definis (null)"));  
      return;
    }

    //fred
    if(params.isNewVersion()) {
      params.parametresINP(DParametresRefonde.createInpForInterface(params.parametresINPNewVersion()));
      params.clearRefondeNewVersion();
    }

    log(_c, "lancement du calcul");

    traceBuffer_= new StringBuffer();
    final String user= _c.utilisateur().nom().toLowerCase();
    final String organisme= _c.utilisateur().organisme().sigle().toLowerCase();
    final String pathExe= cheminServeur();
    final String pathFic=
      pathExe
        + "projets"
        + File.separator
        + organisme
        + File.separator
        + user
        + File.separator;

    // Création du répertoire pour ce user et cet organisme s'il n'existe pas
    new File(pathFic).mkdirs();
    final String fichier= params.racine();

    final int tpCal= params.typeCalcul();

    try {
      if (arretDemande_) {
        return;
      }

      // Suppression des fichiers de résultats avant calcul (en principe, ils
      // ne sont plus là).
      DResultatsRefonde.clearFichiers(pathFic + fichier);

      // Ecriture des fichiers
      DParametresRefonde.ecritSurFichiers(
        pathFic + fichier,
        params.typeCalcul(),
        params);

      // Calcul par le module seiche
      if (tpCal==CAL_SEICHE) {
        executeSeiche(fichier,pathFic);
      // Calcul par refonde standard
      } else {
        executeRefonde(fichier,pathFic,_c);
      }

      // Récupération des résultats (pour l'instant pas en cas de reprise de
      // calcul, car l'attribut nbNoeuds n'est pas initialisé).

      if (tpCal != DParametresRefonde.HOULE_ALEA_REPRISE) {
        //        results.setFichier(pathFic+fichier,params.parametresINP().nbNoeuds,
//                           tpCal==DParametresRefonde.HOULE_REG);
        results.setFichier(pathFic+fichier,params.parametresINP().nbNoeuds);
      }

      message("Fin du calcul");
      log(_c, "calcul terminé");
    }
    catch (final Exception ex) {
      message("Il y a eu une exception");
      log(_c, "erreur du calcul");
      System.out.println(ex);
      CDodico.exception(this, ex);
    }
    finally {
      DParametresRefonde.clearFichiers(pathFic + fichier);

      // Suppression des fichiers de résultats si interruption.
      if (arretDemande_) {
        DResultatsRefonde.clearFichiers(pathFic+fichier);
      }

      // Suppression des fichiers de reprise si interruption ou si demandé.
      if (arretDemande_ || !saveReprise_) {
        DResultatsRefonde.clearFichiersReprise(pathFic + fichier);
      }

      if (arretDemande_) {
        traceBuffer_.append("\n*** Interruption utilisateur ***\n");
        arretDemande_= false;
      }
    }
  }

  /**
   * Lancement des exécutables refonde1 et refonde2.
   * @param _fichier String
   * @param _pathFic String
   * @throws IOException
   * @throws InterruptedException
   */
  private void executeRefonde(final String _fichier, final String _pathFic, final IConnexion _c)
   throws IOException, InterruptedException {
    if (!verifieConnexion(_c)) {
      return;
    }
    final IParametresRefonde params= IParametresRefondeHelper.narrow(parametres(_c));

    final int tpCal= params.typeCalcul();
    final String os= System.getProperty("os.name");
    final String pathExe=cheminServeur();

    try {
      // Houle aléatoire avec terme dissipatif : On boucle 2 fois sur les exe
      int nbExec = 1;
      if (tpCal == CAL_H_ALEA && params.parametresINP().deferlement == 1) {
        nbExec = 2;
      }

      int iexec = 0;

      while (iexec < nbExec) {

        // Lancement de l'exécutable houle régulière.

        if (tpCal != CAL_H_ALEA_REPRISE) {
          succes_ = false;

          // Ecriture du fichier refonde1.5
          if (iexec == 0) {
            final PrintWriter fref5 =
                new PrintWriter(new FileWriter(_pathFic + "refonde1.5"));
            fref5.println(_fichier + ".inp");
            fref5.println(_fichier + ".out");
            fref5.close();
          }

          String exe;
          if (os.startsWith("Windows")) {
            exe = "refonde1.exe";
          } else {
            exe = "refonde1-" + os + ".x";
          }

          message("Exécution de refonde houle régulière (passage " + (iexec + 1) +
                  ") pour " + _fichier);

          if (arretDemande_) {
            return;
          }

          final String[] cmd = {
              pathExe + exe};
          if(CtuluLibMessage.DEBUG) {
            CtuluLibMessage.debug("proc demarré");
          }
          proc_ = Runtime.getRuntime().exec(cmd, null, new File(_pathFic));
          if (arretDemande_) {
            proc_.destroy();
          } else {
            proc_.waitFor();
          }
          if(CtuluLibMessage.DEBUG) {
            CtuluLibMessage.debug("proc fini");
          }

          if (arretDemande_) {
            return;
          }

          // Trace d'exécution pour houle régulière
          if (new File(_pathFic + _fichier + ".out").exists()) {
            String line;
            LineNumberReader fout;
            appendSep();
            traceBuffer_.append("========================================\n");
            traceBuffer_.append("Exécution refonde 1 (passage " + (iexec + 1) +
                                ")\n");
            appendSep();
            traceBuffer_.append("========================================\n\n");

            fout = new LineNumberReader(new FileReader(_pathFic + _fichier +
                ".out"));
            while ( (line = fout.readLine()) != null) {
              traceBuffer_.append(line +
                  "\n");
            }
            fout.close();
          }

          // Erreurs sur stream Error
          final InputStreamReader is = new InputStreamReader(proc_.getErrorStream());
          String sResult = "";
          int ch;
          while ( (ch = is.read()) != -1) {
            sResult += (char) ch;
          }
          traceBuffer_.append(sResult);

          final String fsol = _pathFic + _fichier;


          // Succès du calcul si aucune erreur et fichiers résultats existants.
          succes_ = proc_.exitValue() == 0 &&
              (tpCal == CAL_H_REG && new File(fsol + "1.sol").length() != 0) ||
              accepteRepriseHouleAleatoire(_c, _fichier,
                                           params.parametresINP().
                                           nbPeriodesHoule,
                                           params.parametresINP().nbAnglesHoule);

          // Le calcul s'est mal déroulé.
          if (!succes_) {
            message("Erreurs :" + sResult);
            return;
          }
        }

        // Lancement de l'exécutable houle aléatoire.

        if (tpCal != CAL_H_REG) {
          succes_ = false;

          // Ecriture du fichier refonde2.5
          if (iexec == 0) {
            final PrintWriter fref5 =
                new PrintWriter(new FileWriter(_pathFic + "refonde2.5"));
            fref5.println(_fichier + ".spe");
            fref5.println(_fichier + ".out");
            fref5.close();
          }

          String exe;
          if (os.startsWith("Windows")) {
            exe = "refonde2.exe";
          } else {
            exe = "refonde2-" + os + ".x";
          }

          message("Exécution de refonde houle aléatoire (passage " + (iexec + 1) +
                  ") pour " + _fichier);

          if (arretDemande_) {
            return;
          }

          final String[] cmd={pathExe+exe};
          proc_ = Runtime.getRuntime().exec(cmd, null, new File(_pathFic));
          if (arretDemande_) {
            proc_.destroy();
          } else {
            proc_.waitFor();
          }

          if (arretDemande_) {
            return;
          }

          // Trace d'exécution pour houle aléatoire
          if (new File(_pathFic + _fichier + ".out").exists()) {
            String line;
            LineNumberReader fout;
            appendSep();
            traceBuffer_.append("========================================\n");
            traceBuffer_.append("Exécution refonde 2 (passage "+(iexec+1)+")\n");
            appendSep();
            traceBuffer_.append("========================================\n\n");

            fout=new LineNumberReader(new FileReader(_pathFic+_fichier+".out"));
            while ((line=fout.readLine())!=null) {
              traceBuffer_.append(line+"\n");
            }
            fout.close();
          }

          // Erreurs sur stream Error
          final InputStreamReader is = new InputStreamReader(proc_.getErrorStream());
          String sResult = "";
          int ch;
          while ( (ch = is.read()) != -1) {
            sResult += (char) ch;
          }
          traceBuffer_.append(sResult);

          final String fsol = _pathFic + _fichier;

          // Succès du calcul si aucune erreur et fichier résultats existant.
          succes_=proc_.exitValue()==0 && new File(fsol+".sol").length()!=0;

          // Le calcul s'est mal déroulé.
          if (!succes_) {
            message("Erreurs :" + sResult);
            return;
          }
        }
        iexec++;
      }
    }
    // Dans tous les cas, on renomme le fichier de sortie.
    finally {
      new File(_pathFic+_fichier+"1.sol").renameTo(new File(_pathFic+_fichier+".sol"));
    }
  }
  private void appendSep() {
    traceBuffer_.append("========================================");
  }

  /**
   * Lancement de l'exécutable seiche.
   * Les fichiers entrée/sortie sont renommés le temps de l'exécution en
   * ___tmp.* suite à la limitation de la longueur des noms à 6 caractères.
   *
   * @param _fichier String Le nom du fichier sans extension.
   * @param _pathFic String Le chemin jusqu'au fichier.
   * @throws IOException En cas d'impossibilité d'ouvrir un fichier
   * @throws InterruptedException En cas d'interruption de l'exe seiche.
   */
  private void executeSeiche(final String _fichier, final String _pathFic)
   throws IOException, InterruptedException {

    // Lecteur du buffer d'output standard process.
    OutputProcessReader pout=null;
    // Lecteur du buffer d'output erreur process.
    OutputProcessReader perr=null;

    String string = "___tmp.out";
    try {
      // Suppression des fichiers d'exécution s'ils existent
      new File(_pathFic+"seiche.sol").delete();
      new File(_pathFic+"___tmp.inp").delete();
      new File(_pathFic+string).delete();

      // On renomme le fichier d'entrée.
      new File(_pathFic+_fichier+".inp").renameTo(new File(_pathFic+"___tmp.inp"));

      succes_=false;
      final String os=System.getProperty("os.name");
      final String pathExe=cheminServeur();

      String exe;
      if (os.startsWith("Windows")) {
        exe="seiche.exe";
      } else {
        exe="seiche1-"+os+".x";
      }

      message("Exécution de seiche pour "+_fichier);

      final String[] cmd={pathExe + exe};
      if (arretDemande_) {
        return;
      }

      proc_=Runtime.getRuntime().exec(cmd, null, new File(_pathFic));

      final PrintWriter pinp=new PrintWriter(proc_.getOutputStream());
//      String linp=(_fichier+".inp      ").substring(0, 10)+_fichier+".out";
//      pinp.println(linp);
      pinp.println("___tmp.inp___tmp.out");
      pinp.flush();
      pinp.close();

      // Lecteur du buffer d'output standard process.
      // Ne contient aucune information interessante, mais doit être lu pour ne
      // pas bloquer le process.
      pout=new OutputProcessReader(proc_.getInputStream());
      pout.start();
      // Lecteur du buffer d'output erreur process.
      perr=new OutputProcessReader(proc_.getErrorStream());
      perr.start();

      if (arretDemande_) {
        proc_.destroy();
      } else {
        proc_.waitFor();
      }

      if (arretDemande_) {
        return;
      }

      // Trace d'exécution pour seiche
//      if (new File(_pathFic+_fichier+".out").exists()) {
      if (new File(_pathFic+string).exists()) {
        String line;
        LineNumberReader fout;
        appendSep();
        traceBuffer_.append("========================================\n");
        traceBuffer_.append("Exécution seiche\n");
        appendSep();
        traceBuffer_.append("========================================\n\n");

//        fout=new LineNumberReader(new FileReader(_pathFic+_fichier+".out"));
        fout=new LineNumberReader(new FileReader(_pathFic+string));
        while ((line=fout.readLine())!=null) {
          traceBuffer_.append(line+"\n");
        }
        fout.close();
      }

      // Erreurs sur stream Error
      final String sResult=perr.getBuffer().toString();
      traceBuffer_.append(sResult);

      // Succès du calcul si aucune erreur et fichiers résultats existants.
      succes_=proc_.exitValue()==0 && new File(_pathFic+"seiche.sol").length()!=0;

      // Le calcul s'est mal déroulé.
      if (!succes_) {
        message("Erreurs :"+sResult);
        return;
      }
    }
    finally {
      // Dans tous les cas, on renomme les fichiers d'entrée/sortie.
      new File(_pathFic+"seiche.sol").renameTo(new File(_pathFic+_fichier+".sol"));
      new File(_pathFic+"___tmp.inp").renameTo(new File(_pathFic+_fichier+".inp"));
      new File(_pathFic+string).renameTo(new File(_pathFic+_fichier+".out"));

      // Arret des lecteurs.
      if (pout!=null) {
        pout.stopReading();
      }
      if (perr!=null) {
        perr.stopReading();
      }
    }
  }

  // >>> Interface ICalculRefonde  ---------------------------------------------
  /**
   * Arret demandé du calcul en cours.
   */
  public void setArretDemande() {
    arretDemande_= true;
    // Arret du sous process s'il est en cours.
    if (proc_ != null) {
      proc_.destroy();
    }
  }
  /**
   * Annulation de l'arret demandé.
   */
  public void clearArretDemande() {
    arretDemande_= false;
  }
  /**
   * Retourne la trace d'exécution sous forme de chaine. Les lignes sont
   * séparées par des caractères de fin de ligne "\n".
   *
   * @return La trace d'exécution.
   */
  public String traceExecution() {
    return traceBuffer_.toString();
  }
  /**
   * Retourne l'état de sortie du calcul. Le calcul s'est correctement déroulé
   * si des résultats existent.
   *
   * @return <i>true</i> Le calcul s'est bien déroulé. <i>false</i> sinon.
   */
  public boolean estOK() {
    return succes_;
  }
  /**
   * Retourne si une reprise de calcul en houle aléatoire est possible. Ceci
   * implique que les fichiers résultats de houle régulière pour chaque
   * direction et chaque période existent.
   *
   * @param _c      Connexion
   * @param _racine Racine des fichiers calcul sans path.
   * @param _nbPer  Nombre de périodes. Necessaire pour controler l'existence
   *                des fichiers solutions.
   * @param _nbAng  Nombre de directions. Necessaire pour controler l'existence
   *                des fichiers solutions.
   */
  public boolean accepteRepriseHouleAleatoire(
    final IConnexion _c,
    final String _racine,
    final int _nbPer,
    final int _nbAng) {
    if (!verifieConnexion(_c)) {
      return false;
    }
    final String path= cheminServeur();
    final String user= _c.utilisateur().nom().toLowerCase();
    final String organisme= _c.utilisateur().organisme().sigle().toLowerCase();
    final String pathFic=
      path
        + "projets"
        + File.separator
        + organisme
        + File.separator
        + user
        + File.separator;
    final DecimalFormat df= new DecimalFormat("00");
    File fTest;
    // Controle sur le fichier de données.
    fTest= new File(pathFic + _racine + ".don");
    if (!fTest.exists()) {
      message("Fichier " + fTest.getPath() + " inexistant");
      return false;
    }
    // Controle sur les fichiers résultats.
    for (int i= 0; i < _nbPer; i++) {
      for (int j= 0; j < _nbAng; j++) {
        fTest=
          new File(
            pathFic
              + _racine
              + "t"
              + df.format(i + 1)
              + "d"
              + df.format(j + 1)
              + ".sol");
        if (!fTest.exists()) {
          message("Fichier " + fTest.getPath() + " inexistant");
          return false;
        }
      }
    }
    return true;
  }
  /**
   * Conservation des fichiers de reprise lors du calcul en houle aléatoire.
   */
  public void conserveFichiersReprise(final boolean _saveReprise) {
    saveReprise_= _saveReprise;
  }
  /**
   * Les fichiers de reprise de calcul en houle aléatoire sont ils conservés.
   */
  public boolean conserveFichiersReprise() {
    return saveReprise_;
  }
  // <<< Interface ICalculRefonde  ---------------------------------------------

  /**
   * Affichage d'un message du serveur.
   *
   * @param _m Message.
   */
  public static void message(final String _m) {
    System.out.println("ServeurRefonde : " + _m);
  }

  /**
   * Renomme tous les fichiers de même racine situés dans un répertoire
   * dans une autre racine.
   *
   * @param _dir       Repertoire d'emplacement des fichiers
   * @param _racineOld Ancienne racine.
   * @param _racineNew Nouvelle racine.
   */
  /*  private void renommeTousFichiers
     (String _dir, final String _racineOld, String _racineNew) {

      File dSearch;

      if (!(dSearch=new File(_dir)).isDirectory()) return;

      // Filtre de recherche de tous les fichiers à même racine.
      FileFilter filtre=new FileFilter() {
        public boolean accept(File _f) {
          return _f.getName().startsWith(_racineOld);
        }
      };

      // Renommage de tous les fichiers trouvés.
      message("_racineOld "+_racineOld);
      message("_racineNew "+_racineNew);
      File[] files=dSearch.listFiles(filtre);
      for (int i=0; i<files.length; i++) {
        File nf;
        nf=new File(_dir+_racineNew+files[i].getName().substring(_racineOld.length()));
        message("Renomme : "+files[i].getPath()+" en "+nf.getPath());
        files[i].renameTo(nf);
      }
    }*/
}

/**
 * Une classe pour lire et vider les buffer sortie du process.
 */
class OutputProcessReader extends Thread {
  private InputStreamReader is_;
  private final StringBuffer sb_=new StringBuffer();
  private boolean stopReading_;

  public OutputProcessReader(final InputStream _is) {
    is_=new InputStreamReader(_is);
  }

  public void run() {
    int ch;
    while (!stopReading_) {
      try { while ((ch=is_.read())!=-1) {
        sb_.append((char)ch);
      } }
      catch (final IOException _exc) { stopReading_=true; }

      try { Thread.sleep(100); } catch (final InterruptedException _exc) {}
    }

    try { is_.close(); } catch (final IOException _exc) {}
  }

  public StringBuffer getBuffer() { return sb_; }
  public void stopReading() { stopReading_=true; }
}
