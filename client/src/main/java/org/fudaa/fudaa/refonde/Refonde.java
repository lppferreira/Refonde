/*
 * @file         Refonde.java
 * @creation     1999-06-25
 * @modification $Date: 2007-01-19 13:14:15 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import javax.swing.JFrame;

import com.diaam.lgpl.ts.TerminalStandard;

import com.memoire.bu.BuLib;
import com.memoire.bu.BuPreferences;
import com.memoire.bu.BuSplashScreen;

import org.fudaa.fudaa.commun.impl.FudaaCommandLineParser;
/**
 * Point d'entrée de l'application Refonde.
 *
 * @version      $Id: Refonde.java,v 1.11 2007-01-19 13:14:15 deniger Exp $
 * @author       Bertrand Marchand
 */
public final class Refonde {
  
  private Refonde() {}
  final public static String[][] SOFTWARE_CLASSES=
    new String[][] { BuLib.SWING_CLASSES, BuLib.BU_CLASSES };
  /**
   * Exécution de l'application.
   */
  public static void main(String[] _args) {
    System.out.println("Client Refonde");
    // Lecture des flags
    FudaaCommandLineParser flags= new FudaaCommandLineParser();
    _args= flags.parse(_args);
    if (_args.length > 0) {
      System.err.println("The flag " + _args[0] + " is unknown");
      System.err.println("Flags: " + flags.flagTotalText());
      System.exit(1);
    }
    // Console système
    if (!flags.noLog_) {
      String ver= RefondeImplementation.informationsSoftware().version;
      TerminalStandard ts= new TerminalStandard();
      ts.activeStandards();
      JFrame fts= new JFrame("Console système");
      fts.setContentPane(ts);
      fts.pack();
      fts.show();
      RefondeImplementation.ts= fts;
      String wlcmsg=
        "******************************************************************************\n"
          + "*                             Bienvenue dans Refonde "
          + ver
          + "                   *\n"
          + "*                             -----------------------                        *\n"
          + "* Ceci est la console texte. Elle affiche tous les messages systeme:         *\n"
          + "* erreurs, taches en cours. Consultez-la regulierement pour savoir           *\n"
          + "* si le programme est actif, si une erreur s'est produite, ...               *\n"
          + "* En cas d'erreur, joignez son contenu (enregistre dans le fichier ts.log)   *\n"
          + "* au mail de notification de bogue, ceci nous aidera a comprendre.           *\n"
          + "******************************************************************************\n\n";
      System.out.println(wlcmsg);
    }
    // Preferences
    /*BuInformationsSoftware il= */RefondeImplementation.informationsSoftware();
    BuPreferences.BU.applyLookAndFeel();
    // Network Check
    //    FudaaNetworkChecker nc=new FudaaNetworkChecker(il);
    //    nc.check();
    // Update
    //    if( !flags.no_update ) {
    //      FudaaUpdate update=new FudaaUpdate(RefondeImplementation.informationsSoftware());
    //      update.setInstallMode(flags.jar_update?FudaaUpdate.JAR:FudaaUpdate.CLASS);
    //      if( update.scanForUpdate() ) {
    //        if( update.startUpdate() )
    //          System.exit(0);
    //      } else System.err.println("Refonde : no update");
    //    }
    // Splash screen
    BuSplashScreen ss=
      new BuSplashScreen(
        RefondeImplementation.informationsSoftware(),
        3000,
        Refonde.SOFTWARE_CLASSES);
    if (!flags.noSplash_)
      ss.start();
    ss.setProgression(0);
    /*
        ss.setText("Recherche d'un serveur Refonde distant...");

        if(!flags.no_corba&&!flags.no_server)
        // Recherche du serveur
          RefondeImplementation.SERVEUR_REFONDE=ICalculRefondeHelper.narrow(
    //       CDodico.findServerByName("un-serveur-refonde",50));
           CDodico.findServerByInterface("::refonde::ICalculRefonde",4000));

        ss.setProgression(20);

        if(!flags.no_server)
        if(RefondeImplementation.SERVEUR_REFONDE==null)
        {
    //      String OS=System.getProperty("os.name");
    //      String FS=System.getProperty("file.separator");
    //      String PS="serveurs"+FS+"refonde"+FS+"refonde";
    //
    //      if(OS.startsWith("Win")) OS="Win95";
    //      System.err.println("OS="+OS);
    //      System.err.println("FS="+FS);
    //      System.err.println("PS="+PS);

          ss.setText("Lancement d'un serveur Refonde local...");
          System.err.print("Lancement d'un serveur Refonde local...");

          {
    	ss.setProgression(30);
    	ss.setText("Initialisation du serveur local...");
            // "un-serveur-refonde-local"
    	RefondeImplementation.SERVEUR_REFONDE=new CCalculRefonde();
    //	CDodico.getBOA().obj_is_ready(RefondeImplementation.SERVEUR_REFONDE);
    	System.err.println("OK");
          }
          // else System.err.println("# introuvable.");
        }

        // Serveur dunes (obligatoire pour l'application).
        RefondeServeurDunes.initialiser(!flags.no_corba);
    //    RefondeImplementation.SERVEUR_DUNES=new CCalculDunes();

        // Serveur locaux (pas de nécessité qu'il soit à distance)
        RefondeImplementation.SERVEUR_OLB  =new CCalculOLB();
    */
    ss.setProgression(40);
    ss.setText("Création de l'application...");
    RefondeApplication app= new RefondeApplication();
    ss.setProgression(60);
    // new AlmaScreenCopy(app);
    // ss.setProgression(70);
    ss.setText("Initialisation de l'application...");
    app.init();
    ss.setProgression(100);
    try {
      Thread.sleep(500);
    } catch (Exception ex) {}
    ss.setVisible(false);
    app.start();
  }
}
