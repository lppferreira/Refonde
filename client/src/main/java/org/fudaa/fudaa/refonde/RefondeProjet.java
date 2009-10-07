/*
 * @file         RefondeProjet.java
 * @creation     1999-06-28
 * @modification $Date: 2006-12-05 10:18:13 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.fudaa.ctulu.CtuluLibFile;

import org.fudaa.ebli.geometrie.GrMaillageElement;
/**
 * Classe projet.
 *
 * @version      $Id: RefondeProjet.java,v 1.14 2006-12-05 10:18:13 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeProjet {
  private File fcProjet_;
  private File fcGeometrie_;
  private File fcMaillage_;
  private File fcModele_;
  private File fcCalcul_;
  private File fcResultats_;
  private File fcMEP_;
//  private boolean  existeMaillage;
  private RefondeGeometrie geometrie_;
  private RefondeMaillage maillage_;
  private RefondeModeleProprietes modele_;
  private RefondeModeleCalcul calcul_;
  private RefondeResultats resultats_;
  private RefondeMiseEnPage mep_;
  private boolean modifie;
  /** Auditeurs d'événement message. */
  private HashSet messageListeners_= new HashSet();

  protected boolean isModifie(){
    return modifie;
  }

  //  private RefondeProjet() {
//    fcGeometrie_=null;
//    modifie=false;
//  }
  /**
   * Création d'un projet à partir d'une géométrie au format Vag.
   * @param _projet Nom du projet. La géométrie est contenue dans les fichiers
   *                _projet.10 et _projet.12
   * @param _tpModele Le type de modèle (HOULE ou SEICHE).
   * @exception FileNotFoundException Un fichier de géométrie n'est pas trouvé
   */
//  public static RefondeProjet nouveau(File _projet) throws IOException {
  public void nouveau(File _projet, int _tpModele) throws IOException {
//    RefondeProjet prj=new RefondeProjet();
    RefondeProjet prj= this;
    String ext;
    String fcName;
    RefondeGeometrie geo;
//    File              fcGeo;
    if (_projet.getName().endsWith(".10"))
      ext= ".10";
    else if (_projet.getName().endsWith(".12"))
      ext= ".12";
    else
      throw new IOException("Impossible de créer un projet depuis " + _projet);
    fcName= _projet.getPath().substring(0, _projet.getPath().lastIndexOf(ext));
    prj.fireMessageEvent(new MessageEvent(prj, null, 20));
//    RefondeImplementation.statusBar.setProgression(20);

    // Lecture/Création de la géométrie depuis les fichiers Vag
    prj.fcGeometrie_= new File(fcName + ".geo");
    geo= RefondeGeometrie.ouvrirVag(new File(fcName));
    prj.geometrie_= geo;
    prj.fireMessageEvent(new MessageEvent(prj, null, 50));
//    RefondeImplementation.statusBar.setProgression(50);

    // Maillage
    prj.fcMaillage_= new File(fcName + ".mai");
    prj.maillage_= RefondeMaillage.defaut(prj);
    prj.fireMessageEvent(new MessageEvent(prj, null, 60));
//    RefondeImplementation.statusBar.setProgression(60);

    // Modèle de propriétés
    prj.fcModele_= new File(fcName + ".prp");
    prj.modele_= RefondeModeleProprietes.defaut(prj,_tpModele);
    prj.fireMessageEvent(new MessageEvent(prj, null, 70));
//    RefondeImplementation.statusBar.setProgression(70);

    // Modèle de calcul
    prj.fcCalcul_= new File(fcName + ".cal");
    prj.calcul_= RefondeModeleCalcul.defaut(prj);
    prj.calcul_.typeModele(_tpModele);
    prj.fireMessageEvent(new MessageEvent(prj, null, 80));
//    RefondeImplementation.statusBar.setProgression(80);

    // Résultats
    prj.fcResultats_= new File(fcName + ".sol");
    prj.resultats_= null;

    // Mise en page
    prj.fcMEP_= new File(fcName + ".mep");
    prj.mep_= RefondeMiseEnPage.defaut(prj);

    // Objet en retour
    prj.modifie= true;
    prj.fcProjet_= new File(fcName + ".prf");
//    return prj;
  }

  /**
   * Ouverture d'un projet. Les fichiers associés sont aussi ouverts
   * @param _projet Nom du projet
   */
  //  public static RefondeProjet ouvrir(File _projet)
  public void ouvrir(File _projet)
    throws IOException, RefondeTacheInterruptionException {
    //    RefondeProjet prj=new RefondeProjet();
    RefondeProjet prj= this;
    String ext;
    String fcName;
    if (_projet.getName().endsWith(".prf"))
      ext= ".prf";
    else
      throw new IOException("Impossible d'ouvrir un projet depuis " + _projet);
    fcName= _projet.getPath().substring(0, _projet.getPath().lastIndexOf(ext));
    prj.fcProjet_= new File(fcName + ".prf");
    RefondeTacheOperation.notifieArretDemande();
    // Lecture du projet
    prj.lire();
    RefondeTacheOperation.notifieArretDemande();
    // B.M. A faire : controler que les noms de fichiers sont valides
    // Lecture/Création de la géométrie
    prj.fireMessageEvent(
      new MessageEvent(
        prj,
        "Lecture du fichier de géométrie " + prj.fcGeometrie_,
        20));
    if (prj.fcGeometrie_.getName().endsWith(".geo")) {
      prj.geometrie_= RefondeGeometrie.ouvrir(prj.fcGeometrie_);
    } else {
      prj.geometrie_= RefondeGeometrie.ouvrirVag(prj.fcGeometrie_);
      prj.fcGeometrie_= new File(prj.fcGeometrie_.getPath() + ".geo");
    }
    RefondeTacheOperation.notifieArretDemande();
    // Lecture/Création du maillage si maillage existant
    if (prj.fcMaillage_.getPath().equals("")) {
      prj.fcMaillage_= new File(fcName + ".mai");
      prj.maillage_= RefondeMaillage.defaut(prj);
    } else if (prj.fcMaillage_.getName().endsWith(".mai")) {
      prj.fireMessageEvent(
        new MessageEvent(
          prj,
          "Lecture du fichier de maillage " + prj.fcMaillage_,
          30));
      prj.maillage_= RefondeMaillage.ouvrir(prj, prj.fcMaillage_);
    } else {
      prj.fireMessageEvent(
        new MessageEvent(
          prj,
          "Lecture du fichier de maillage " + prj.fcMaillage_,
          30));
      prj.maillage_= RefondeMaillage.ouvrirPreflux(prj, prj.fcMaillage_);
      prj.fcMaillage_= new File(prj.fcMaillage_.getPath() + ".mai");
    }
    RefondeTacheOperation.notifieArretDemande();
    // Lecture des résultats si existants
    if (prj.fcResultats_.getPath().equals("")) {
      prj.resultats_= null;
      prj.fcResultats_= new File(fcName + ".sol");
    } else {
      prj.fireMessageEvent(
        new MessageEvent(
          prj,
          "Lecture du fichier de résultats " + prj.fcResultats_,
          60));
      prj.resultats_=
        RefondeResultats.ouvrir(
          prj.fcResultats_,
          RefondeMaillage.creeSuperMaillage(prj).noeuds().length);
    }
    //    if (prj.estEntierementMaille()) {
    //      if (new File(fcName+"1.sol").exists()) {
    //        prj.resultats_=RefondeResultats.ouvrir(fcName+"1",prj.maillage_.creeSuperMaillage(prj).noeuds().length);
    //        System.out.println("Resultats: "+prj.resultats_.resultats_.lignes.length+" lignes");
    //      }
    //      else prj.resultats_=null;
    //    }
    RefondeTacheOperation.notifieArretDemande();
    // Modèle de propriétés
    prj.fireMessageEvent(
      new MessageEvent(
        prj,
        "Lecture du modèle de propriétés " + prj.fcModele_,
        70));
    prj.modele_= RefondeModeleProprietes.ouvrir(prj, prj.fcModele_);
    RefondeTacheOperation.notifieArretDemande();
    // Modèle de calcul
    prj.fireMessageEvent(
      new MessageEvent(
        prj,
        "Lecture du modèle de calcul " + prj.fcCalcul_,
        75));
    prj.calcul_= RefondeModeleCalcul.ouvrir(prj, prj.fcCalcul_);
    RefondeTacheOperation.notifieArretDemande();
    // Mise en page (lecture si existante)
    if (prj.fcMEP_.getPath().equals("")) {
      prj.mep_= RefondeMiseEnPage.defaut(prj);
      prj.fcMEP_= new File(fcName + ".mep");
    } else {
      prj.fireMessageEvent(
        new MessageEvent(
          prj,
          "Lecture du fichier de mise en page " + prj.fcMEP_,
          80));
      prj.mep_= RefondeMiseEnPage.ouvrir(prj, prj.fcMEP_);
    }
    // Objet en retour
    prj.modifie= false;
    //    return prj;
  }
  /**
   * Enregistrement d'un projet et de ses fichiers associés sur les fichiers
   * correspondants
   * @exception FileNotFoundException Un fichier ne peut être ouvert
   * @exception IOException Une erreur d'écriture s'est produite
   */
  public void enregistrer() throws IOException {
    // Enregistrement de la géométrie si elle a été modifiée
    //    if (geometrie_.estModifiee()) geometrie_.enregistrer(fcGeometrie_);
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement de la géométrie... " + fcGeometrie_,
        20));
    //    RefondeImplementation.statusBar.setMessage("Enregistrement de la géométrie... "+fcGeometrie_);
    //    RefondeImplementation.statusBar.setProgression(20);
    geometrie_.enregistrer(fcGeometrie_);
    // Enregistrement du maillage s'il a été modifié
    //    if (hasMaillage() && maillage_.modifie) maillage_.enregistrer(fcMaillage_);
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement du maillage... " + fcMaillage_,
        40));
    //    RefondeImplementation.statusBar.setMessage("Enregistrement du maillage... "+fcMaillage_);
    //    RefondeImplementation.statusBar.setProgression(40);
    maillage_.enregistrer(this, fcMaillage_);
    // Enregistrement du modele de calcul s'il a été modifié
    //    if (calcul_.modifie) calcul_.enregistrer(this,fcCalcul_);
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement du modèle de calcul... " + fcCalcul_,
        50));
    //    RefondeImplementation.statusBar.setMessage("Enregistrement du modèle de calcul... "+fcCalcul_);
    //    RefondeImplementation.statusBar.setProgression(50);
    calcul_.enregistrer(this, fcCalcul_);
    // Enregistrement du modele de propriétés s'il a été modifié
    //    if (modele_.modifie) modele_.enregistrer(this,fcModele_);
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement du modèle de propriétés... " + fcModele_,
        60));
    //    RefondeImplementation.statusBar.setMessage("Enregistrement du modèle de propriétés... "+fcModele_);
    //    RefondeImplementation.statusBar.setProgression(60);
    modele_.enregistrer(this, fcModele_);
    // Enregistrement du fichier des résultats
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement des résultats..." + fcResultats_,
        65));
    if (hasResultats())
      resultats_.enregistrer(this, fcResultats_);
    // Enregistrement du fichier de mise en page
    fireMessageEvent(
      new MessageEvent(
        this,
        "Enregistrement de la mise en page..." + fcMEP_,
        80));
    mep_.enregistrer(this, fcMEP_);
    // Enregistrer le fichier projet s'il a été modifié
    //    if (modifie) ecrire();
    ecrire();
    modifie= false;
  }
  /**
   * Ajout d'un auditeur d'événement message.
   */
  public void addMessageListener(MessageListener _listener) {
    messageListeners_.add(_listener);
  }
  /**
   * Suppression d'un auditeur de d'événement message.
   */
  public void removeMessageListener(MessageListener _listener) {
    messageListeners_.remove(_listener);
  }
  /**
   * Envoi aux auditeurs qu'un événement <I>MessageEvent</I> s'est
   * produit.
   */
  public void fireMessageEvent(MessageEvent _evt) {
    for (Iterator i= messageListeners_.iterator(); i.hasNext();)
       ((MessageListener)i.next()).messageEnvoye(_evt);
  }
  /**
   * Accesseurs à la propriété fichierProjet.
   */
  public void setFichierProjet(File _fichier) {
    fcProjet_= _fichier;
    modifie= true;
  }
  public File getFichierProjet() {
    return fcProjet_;
  }
  /**
   * Accesseurs à la propriété fichierGeometrie.
   */
  public void setFichierGeometrie(File _fichier) {
    fcGeometrie_= _fichier;
    modifie= true;
  }
  public File getFichierGeometrie() {
    return fcGeometrie_;
  }
  /**
   * Accesseurs à la propriété fichierMaillage.
   */
  public void setFichierMaillage(File _fichier) {
    fcMaillage_= _fichier;
    modifie= true;
  }
  public File getFichierMaillage() {
    return fcMaillage_;
  }
  /**
   * Accesseurs à la propriété fichierModele.
   */
  public void setFichierModele(File _fichier) {
    fcModele_= _fichier;
    modifie= true;
  }
  public File getFichierModele() {
    return fcModele_;
  }
  /**
   * Accesseurs à la propriété fichierCalcul.
   */
  public void setFichierCalcul(File _fichier) {
    fcCalcul_= _fichier;
    modifie= true;
  }
  public File getFichierCalcul() {
    return fcCalcul_;
  }
  /**
   * Accesseurs à la propriété geometrie
   */
  public void setGeometrie(RefondeGeometrie _geometrie) {
    geometrie_= _geometrie;
    modifie= true;
  }
  public RefondeGeometrie getGeometrie() {
    return geometrie_;
  }
  /**
   * Accesseurs à la propriété maillage
   */
  public void setMaillage(RefondeMaillage _maillage) {
    maillage_= _maillage;
    modifie= true;
  }
  public RefondeMaillage getMaillage() {
    return maillage_;
  }
  /**
   * Accesseurs à la propriété modele de propriétés
   */
  public void setModeleProprietes(RefondeModeleProprietes _modele) {
    modele_= _modele;
    modifie= true;
  }
  public RefondeModeleProprietes getModeleProprietes() {
    return modele_;
  }
  /**
   * Accesseurs à la propriété modele de calcul
   */
  public void setModeleCalcul(RefondeModeleCalcul _calcul) {
    calcul_= _calcul;
    modifie= true;
  }
  public RefondeModeleCalcul getModeleCalcul() {
    return calcul_;
  }
  /**
   * Accesseurs à la propriété resultats
   */
  public void setResultats(RefondeResultats _resultats) {
    resultats_= _resultats;
    modifie= true;
  }
  public RefondeResultats getResultats() {
    return resultats_;
  }
  /**
   * Accesseurs à la propriété miseEnPage
   */
  public void setMiseEnPage(RefondeMiseEnPage _mep) {
    mep_= _mep;
    modifie= true;
  }
  public RefondeMiseEnPage getMiseEnPage() {
    return mep_;
  }
  /**
   * Retourne le super maillage (union de tous les maillages) du projet.
   * Le super maillage est recréé si au moins un des maillages est différent
   * de lors de l'appel précédent.
   */
  public GrMaillageElement getSuperMaillage() {
    return RefondeMaillage.creeSuperMaillage(this);
  }
  /**
   * Le projet a-t-il un maillage
   */
  //  public boolean hasMaillage() { return maillage_!=null; }
  /**
   * Le projet contient-il des résultats.
   */
  public boolean hasResultats() {
    return resultats_ != null;
  }
  /**
   * Le projet contient-il une mise en page ?
   */
  public boolean hasMiseEnPage() {
    return mep_ != null;
  }
  /**
   * Le projet est-il entièrement maillé. true si tous les domaines du projet
   * possèdent un maillage. false sinon
   */
  public boolean estEntierementMaille() {
    Vector vdms= geometrie_.scene_.getDomaines();
    for (int i= 0; i < vdms.size(); i++)
      if (!((RefondeDomaine)vdms.get(i)).hasMaillage())
        return false;
    return true;
  }
  /**
   * Permet de savoir s'il faut lancer une optimisation en meme temps que le maillage
   * @param _d le domaine a ne pas tester
   * @return si tous les domaine sauf d sont mailles
   */
  public boolean estEntierementMailleSauf(RefondeDomaine _d) {
    List vdms= geometrie_.scene_.getDomaines();
    for (int i= vdms.size()-1; i >=0 ; i--) {
      RefondeDomaine di=(RefondeDomaine)vdms.get(i);
      if ((di!=_d) && (!di.hasMaillage()))
        return false;
    }
    return true;
  }
  /**
   * Retourne la racine des noms des fichiers calcul associée au projet. La
   * racine est déterminée depuis le nom du projet sans le path ni l'extension.
   *
   * @return La racine des noms.
   */
  public String getRacineFichiersCalcul() {
    String r;
    r= getFichierProjet().getName();
    return r.substring(0, r.lastIndexOf(".prf"));
  }
  /**
   * Lecture des informations depuis le fichier associé
   */
  private void lire() throws IOException {
    Properties prs;
    String logiciel;
    String version;
    String format;
    String pr;
    FileInputStream file= null;
    try {
      // Ouverture du fichier
      file= new FileInputStream(fcProjet_);
      //----------------------------------------------------------------------
      //--- Lecture des propriétés
      //----------------------------------------------------------------------
      prs= new Properties();
      prs.load(file);
      // Entête
      if ((logiciel= prs.getProperty("logiciel")) == null
        || (version= prs.getProperty("version")) == null
        || (format= prs.getProperty("format")) == null)
        throw new IOException(
          "Erreur de lecture sur " + fcProjet_ + "\nEntête incorrect");
      // Logiciel
      if (!logiciel.toLowerCase().equals("refonde")
        && !logiciel.toLowerCase().equals(
          "prefonde")) // On conserve la possibilité pour les anciens fichiers.
        throw new RefondeIOException("Le fichier n'est pas un fichier Refonde");
      // Version
      if (version.compareTo("5.01") < 0)
        throw new RefondeIOException(
          "Le format du fichier est de version "
            + version
            + ".\nSeules les versions > à 5.01 sont autorisées");
      // Format
      if (!format.toLowerCase().equals("projet"))
        throw new RefondeIOException("Le fichier n'est pas un fichier projet");
      // Fichier geométrie
      pr= prs.getProperty("projet.geometrie");
      if (pr == null)
        throw new IOException();
      fcGeometrie_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
      // Fichier maillage
      pr= prs.getProperty("projet.maillage");
      if (pr == null)
        throw new IOException();
      fcMaillage_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
      // Modèle de propriétés
      pr= prs.getProperty("projet.modele");
      if (pr == null)
        throw new IOException();
      fcModele_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
      // Modèle de calcul
      pr= prs.getProperty("projet.parametrescalcul");
      if (pr == null)
        throw new IOException();
      fcCalcul_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
      // Résultats
      pr= prs.getProperty("projet.resultats");
      if (pr == null)
        fcResultats_= new File("");
      else
        fcResultats_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
      // Mise en page
      pr= prs.getProperty("projet.miseenpage");
      if (pr == null)
        fcMEP_= new File("");
      else
        fcMEP_= CtuluLibFile.getAbsolutePathnameTo(fcProjet_, new File(pr));
    } catch (IOException _exc) {
      throw new IOException("Erreur de lecture sur " + fcProjet_);
    }
    //    catch (Exception _exc) {
    //      throw new IOException(_exc.getMessage());
    //    }
    finally {
      if (file != null)
        file.close();
    }
  }
  /**
   * Ecriture des informations sur le fichier projet associé
   */
  private void ecrire() throws IOException {
    Properties prs;
    //    String      logiciel;
    //    String      version;
    //    String      format;
    //    String      pr;
    File fc;
    FileOutputStream file= null;
    try {
      // Ouverture du fichier
      file= new FileOutputStream(fcProjet_);
      //----------------------------------------------------------------------
      //--- Ecriture des propriétés
      //----------------------------------------------------------------------
      prs= new Properties();
      // Entête
      prs.setProperty("logiciel", "refonde");
      prs.setProperty(
        "version",
        RefondeImplementation.informationsSoftware().version);
      prs.setProperty("format", "projet");
      // Fichiers relatifs associés
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcGeometrie_);
      prs.setProperty("projet.geometrie", fc.getPath());
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcMaillage_);
      prs.setProperty("projet.maillage", fc.getPath());
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcModele_);
      prs.setProperty("projet.modele", fc.getPath());
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcCalcul_);
      prs.setProperty("projet.parametrescalcul", fc.getPath());
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcResultats_);
      if (hasResultats())
        prs.setProperty("projet.resultats", fc.getPath());
      fc= CtuluLibFile.getRelativePathnameTo(fcProjet_, fcMEP_);
      prs.setProperty("projet.miseenpage", fc.getPath());
      prs.store(file, null);
    } catch (IOException _exc) {
      throw new IOException("Erreur d'écriture sur " + fcProjet_);
    }
    //    catch (Exception _exc) {
    //      throw new IOException(_exc.getMessage());
    //    }
    finally {
      if (file != null)
        file.close();
    }
  }

  /**
   * Change la structure du projet en fonction du modèle de données.
   * @param _typeModele Le type du modele de données
   *                    (RefondeModeleCalcul.MODELE_SEICHE,
   *                     RefondeModeleCalcul.MODELE_HOULE)
   */
  public void transmute(int _typeModele) {
    // Aucun changement si le modèle de données est le même
    if (_typeModele==this.calcul_.typeModele()) return;

    modifie=true;

    this.modele_.transmute(_typeModele,this);
    this.calcul_.typeModele(_typeModele);
    this.calcul_.calculAngles(this);
    this.resultats_=null;
  }
}
