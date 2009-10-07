/*
 * @file         RefondeModeleCalcul.java
 * @creation     1999-08-06
 * @modification $Date: 2006-09-19 15:10:22 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.dodico.refonde.DParametresRefonde;

import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolyligne;

/**
 * Classe pour la gestion du modèle de calcul du projet.
 *
 * @version      $Id: RefondeModeleCalcul.java,v 1.12 2006-09-19 15:10:22 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleCalcul {
  public static final int DEFER_SANS= 0;
  public static final int DEFER_ITERATIF= 1;
  public static final int DEFER_ECRETAGE= 2;
  public static final int BORD_COND_ORDRE_1= 0;
  public static final int BORD_FORM_ANA_ILE= 1;
  public static final int BORD_FORM_ANA_PORT= 2;
  public static final int BORD_COND_ORDRE_2= 3;
  public static final int FORMULE_GODA_SANS_PENTE= 0;
  public static final int FORMULE_GODA_AVEC_PENTE= 1;
  public static final int FORMULE_MICHE_MODIFIEE= 2;
  public static final int FORMULE_MUNK= 3;
  /** Type de houle régulière */
  public static final int HOULE_REG= DParametresRefonde.HOULE_REG;
  /** Type de houle aléatoire */
  public static final int HOULE_ALEA= DParametresRefonde.HOULE_ALEA;

  /** Type de modèle de données houle */
  public static final int MODELE_HOULE=0;
  /** Type de modèle de données seiche */
  public static final int MODELE_SEICHE=1;

  // Pour la clareté du code
  private static final int EOF= StreamTokenizer.TT_EOF;
  //  private static final int EOL   =StreamTokenizer.TT_EOL;
  //  private static final int NUMBER=StreamTokenizer.TT_NUMBER;
  private static final int WORD= StreamTokenizer.TT_WORD;

  // Paramètres généraux

  protected double hauteurMer_= 0;
  // A la fois periode de houle régulière et période de pic de houle aléatoire
  protected double periodeHoule_= 10;
  // A la fois angle de houle régulière et angle de houle principal aléatoire
  // Angle en degrés entre [0,360[ dans le sens trigo.
  protected double angleHoule_= 270;
  protected double hauteurHoule_= 1;
//  protected double   profondeurOrigine_  =0;
  protected int casBordOuvert_= BORD_COND_ORDRE_1;
  protected int ordreMax_= 0;
  protected int typeHoule_= HOULE_REG;
  /** Type de modèle de données */
  private int typeModele_=MODELE_HOULE;
  /** Fonds poreux */
  protected boolean fondsPoreux_= false;
  /** Déferlement */
  protected int deferlement_= DEFER_SANS;
  //  protected int      nbIterations_     =1;
  protected int formule_= FORMULE_GODA_SANS_PENTE;

  // Houle régulière

  protected int nbPeriodes_= 1;
  protected double periodeHouleMin_= 5;
  protected double periodeHouleMax_= 15;
  protected int nbAnglesHoule_= 1;
  // Angle en degrés entre [0,360[ dans le sens trigo.
  protected double angleHouleMin_= 270;
  // Angle en degrés entre [0,360[ dans le sens trigo.
  protected double angleHouleMax_= 270;

  // Houle aléatoire

  //  protected double   periodePic_         =100;
  protected double rehaussementPic_= 1;
  //  protected double   angleHouleHA_       =270;
  protected double repartitionAngle_= 50;

  // Seiches

  /** Nombre de valeurs propres : Le premier mode donne un plan d'eau immobile */
  protected int nbValPropres_=2;
  /** Nombre d'itération max */
  protected int nbIterMax_=200;
  /** Décalage des valeurs propres */
  protected double decalValPropres_=0.;
  /** Précision de convergence */
  protected double precision_=0.00001;

  /**
   * Les angles d'incidence sur les bord. D'abord les angles sur le contour
   * extérieur, puis les angles sur les contours internes. Les angles sont
   * stockés ordonnés suivant leurs abscisses initiaux, le premier angle étant
   * celui d'angle initial=0. Le dernier angle a pour abscisse final 1.
   */
  protected Vector[] angles_=new Vector[0];
  protected boolean modifie_;
  private Hashtable ai2pl_= new Hashtable();
  private Hashtable at2pl_= new Hashtable();

  /**
   * Création d'un modele de calcul par défaut depuis le projet
   */
  public static RefondeModeleCalcul defaut(RefondeProjet _projet) {
    RefondeModeleCalcul cal= new RefondeModeleCalcul();
    // Calcul des angles d'incidence sur les segments à partir de l'angle de houle
    cal.calculAngles(_projet);
    cal.setModifie();
    return cal;
  }

  /**
   * Accesseur hauteur de mer
   */
  public void hauteurMer(double _ht) {
    setModifie();
    hauteurMer_= _ht;
  }

  public double hauteurMer() {
    return hauteurMer_;
  }

  /**
   * Accesseur periode de houle
   */
  public void periodeHoule(double _periode) {
    setModifie();
    periodeHoule_= _periode;
  }

  public double periodeHoule() {
    return periodeHoule_;
  }

  /**
   * Accesseur angle de houle
   */
  public void angleHoule(double _angle) {
    setModifie();
    angleHoule_= _angle;
  }

  public double angleHoule() {
    return angleHoule_;
  }

  /**
   * Accesseur hauteur de houle
   */
  public void hauteurHoule(double _ht) {
    setModifie();
    hauteurHoule_= _ht;
  }

  public double hauteurHoule() {
    return hauteurHoule_;
  }

  /**
   * Accesseur profondeur d'origine
   */
  //  public void profondeurOrigine(double _prof) { setModifie(); profondeurOrigine_=_prof; }
  //  public double profondeurOrigine() { return profondeurOrigine_; }
  /**
   * Accesseur cas de bord ouvert
   */
  public void casBordOuvert(int _cas) {
    setModifie();
    casBordOuvert_= _cas;
  }
  public int casBordOuvert() {
    return casBordOuvert_;
  }
  /**
   * Accesseur ordre max
   */
  public void ordreMax(int _ordre) {
    setModifie();
    ordreMax_= _ordre;
  }
  public int ordreMax() {
    return ordreMax_;
  }
  /**
   * Accesseur angles
   */
  public void angles(Vector[] _angles) {
    setModifie();
    angles_= _angles;
  }
  public Vector[] angles() {
    return angles_;
  }
  /**
   * Accesseur cas déferlement
   */
  public void deferHoule(int _cas) {
    setModifie();
    deferlement_= _cas;
  }
  public int deferHoule() {
    return deferlement_;
  }
  /**
   * Accesseur nb d'itérations houle
   */
  //  public void nbIterDeferHoule(int _nb) { nbIterations_=_nb; }
  //  public int  nbIterDeferHoule()        { return nbIterations_; }
  /**
   * Accesseur formule houle
   */
  public void formuleDeferHoule(int _formule) {
    setModifie();
    formule_= _formule;
  }
  public int formuleDeferHoule() {
    return formule_;
  }
  /**
   * Accesseur nombre de périodes
   */
  public void nbPeriodesHoule(int _nb) {
    setModifie();
    nbPeriodes_= _nb;
  }
  public int nbPeriodesHoule() {
    return nbPeriodes_;
  }
  /**
   * Accesseur valeur de période mini
   */
  public void periodeHouleMini(double _periode) {
    setModifie();
    periodeHouleMin_= _periode;
  }
  public double periodeHouleMini() {
    return periodeHouleMin_;
  }
  /**
   * Accesseur valeur de période maxi
   */
  public void periodeHouleMaxi(double _periode) {
    setModifie();
    periodeHouleMax_= _periode;
  }
  public double periodeHouleMaxi() {
    return periodeHouleMax_;
  }
  /**
   * Accesseur nombre d'angles
   */
  public void nbAnglesHoule(int _nb) {
    setModifie();
    nbAnglesHoule_= _nb;
  }
  public int nbAnglesHoule() {
    return nbAnglesHoule_;
  }
  /**
   * Accesseur valeur d'angle mini
   */
  public void angleHouleMini(double _angle) {
    setModifie();
    angleHouleMin_= _angle;
  }
  public double angleHouleMini() {
    return angleHouleMin_;
  }
  /**
   * Accesseur valeur d'angle maxi
   */
  public void angleHouleMaxi(double _angle) {
    setModifie();
    angleHouleMax_= _angle;
  }
  public double angleHouleMaxi() {
    return angleHouleMax_;
  }
  /**
   * Accesseur periode de pic
   */
  //  public void   periodePic(double _periode) { periodePic_=_periode; }
  //  public double periodePic()                { return periodePic_; }
  /**
   * Accesseur facteur de rehaussement du pic
   */
  public void rehaussementPic(double _coef) {
    setModifie();
    rehaussementPic_= _coef;
  }
  public double rehaussementPic() {
    return rehaussementPic_;
  }
  /**
   * Accesseur angle de houle principal
   */
  //  public void   angleHouleActif(double _angle) { angleHouleHA_=_angle; }
  //  public double angleHouleActif()              { return angleHouleHA_; }
  /**
   * Accesseur répartition angulaire
   */
  public void repartitionAngle(double _coef) {
    setModifie();
    repartitionAngle_= _coef;
  }
  public double repartitionAngle() {
    return repartitionAngle_;
  }
  /**
   * Prise en compte ou non des fonds poreux.
   * @param _b <code>true</code> Les fonds sont poreux, <code>false</code> sinon.
   */
  public void setFondsPoreux(boolean _b) {
    setModifie();
    fondsPoreux_= _b;
  }
  /**
   * Les fonds sont-ils poreux ?
   * @return <code>true</code> Les fonds sont poreux, <code>false</code> sinon.
   */
  public boolean isFondsPoreux() {
    return fondsPoreux_;
  }
  /**
   * Attribut Modèle modifié
   */
  public void setModifie() {
    modifie_= true;
  }
  public boolean estModifie() {
    return modifie_;
  }
  /**
   * Retourne le type de houle.
   *
   * @return Le type de houle.
   * @see #HOULE_REG
   * @see #HOULE_ALEA
   */
  public int typeHoule() {
    return typeHoule_;
  }
  /**
   * Définit le type de houle.
   *
   * @param _type Le type. (HOULE_REG, HOULE_ALEA)
   */
  public void typeHoule(int _type) {
    typeHoule_= _type;
  }

  /**
   * Type de modele de données.
   * @param _type Le type de modele de données (MODELE_SEICHE, MODELE_HOULE)
   */
  public void typeModele(int _type) {
    typeModele_=_type;
  }

  /**
   * Retourne le type de modele de données.
   * @return Le type de modele de données (MODELE_SEICHE, MODELE_HOULE);
   */
  public int typeModele() {
    return typeModele_;
  }

  /**
   * Modèle seiche : Défini le nombre d'iteration max
   * @param _n Nombre d'itération max.
   */
  public void nbIterMax(int _n) {
    nbIterMax_=_n;
  }

  /**
   * Modèle seiche : Retourne le nombre d'iteration max
   * @return Nombre d'itération max.
   */
  public int nbIterMax() {
    return nbIterMax_;
  }

  /**
   * Modèle seiche : Défini le nombre de valeurs propres
   * @param _n nombre de valeurs propres
   */
  public void nbValPropres(int _n) {
    nbValPropres_=_n;
  }

  /**
   * Modèle seiche : Retourne le nombre de valeurs propres
   * @return nombre de valeurs propres
   */
  public int nbValPropres() {
    return nbValPropres_;
  }

  /**
   * Modèle seiche : defini le décalage des valeurs propres.
   * @param _d Décalage des valeurs propres
   */
  public void decalValPropres(double _d) {
    decalValPropres_=_d;
  }

  /**
   * Modèle seiche : Retourne le décalage des valeurs propres.
   * @return Décalage des valeurs propres
   */
  public double decalValPropres() {
    return decalValPropres_;
  }

  /**
   * Modèle seiche : defini la précision de convergence.
   * @param _p Précision de convergence.
   */
  public void precision(double _p) {
    precision_=_p;
  }

  /**
   * Modèle seiche : Retourne la précision de convergence.
   * @return Précision de convergence.
   */
  public double precision() {
    return precision_;
  }

  /**
   * Lecture d'un modele de calcul.
   * @param _fc Nom du fichier modele de calcul.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public static RefondeModeleCalcul ouvrir(RefondeProjet _projet, File _fc)
    throws IOException {
    RefondeModeleCalcul cal= new RefondeModeleCalcul();
    StreamTokenizer file= null;
    Reader rf= null;
    String version= "??";
    // Entête et numéro de version
    try {
      // Ouverture du fichier
      file= new StreamTokenizer(rf= new FileReader(_fc));
      file.lowerCaseMode(true);
      file.wordChars('<', '<');
      file.wordChars('>', '>');
      file.wordChars('_', '_');
      // Inhibition du mode parseNumber instauré par défaut. Il ne sait pas
      // traiter des float ou double avec exposant. Le traitement se fera donc
      // dans la classe courante, et non dans StreamTokenizer
      file.ordinaryChars('0', '9');
      file.ordinaryChar('.');
      file.ordinaryChar('-');
      file.wordChars('0', '9');
      file.wordChars('.', '.');
      file.wordChars('-', '-');
      file.whitespaceChars(';', ';');
      // Entète du fichier et numéro de version
      if (file.nextToken() != WORD
        || (!file.sval.equals("refonde") && !file.sval.equals("prefonde")))
        throw new RefondeIOException("Le fichier n'est pas un fichier Refonde");
      if (file.nextToken() != WORD
        || (version= file.sval).compareTo("5.01") < 0)
        throw new RefondeIOException(
          "Le format du fichier est de version "
            + version
            + ".\nSeules les versions > à 5.01 sont autorisées");
      if (file.nextToken() != WORD || !file.sval.equals("modele_calcul"))
        throw new RefondeIOException("Le fichier n'est pas un fichier modele calcul");

      // Lecture par version
      if (version.compareTo("5.06") <= 0)
        cal.lire506(_projet, file);
      else if (version.compareTo("5.08") <= 0)
        cal.lire508(_projet, file);
      else if (version.compareTo("5.12") <= 0)
        cal.lire512(_projet, file);
      // Ajout des angles supplémentaires sur les contours
      else if (version.compareTo("5.14") <= 0)
        cal.lire514(_projet, file);
      // Suppression longueur onde
      // Ajout prise en compte fonds poreux
      // Ajout condition absorbante ordre 2
      else if (version.compareTo("5.14a") <= 0)
        cal.lire514a(_projet, file);
      // A partir de 5.15 : Ajout du modèle de seiche
      // => Paramètres de seiche, pas d'angles.
      else
        cal.lire(_projet, file);
    }
    catch (NumberFormatException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fc + " ligne " + file.lineno());
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + _fc
          + " ligne "
          + file.lineno()
          + "\n"
          + _exc.getMessage());
    } catch (FileNotFoundException _exc) {
      throw new IOException("Erreur d'ouverture de " + _fc);
    } catch (IOException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fc + " ligne " + file.lineno());
    }
    //    catch (Exception _exc) {
    //      throw new IOException("Erreur de lecture sur "+_fc+" ligne "+file.lineno()+
    //                            "\n"+_exc.getMessage());
    //    }
    finally {
      if (rf != null)
        rf.close();
    }
    cal.modifie_= false;
    return cal;
  }
  /**
   * Enregistrement d'un modele de calcul.
   * @param _fichier Nom du fichier modele de calcul.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public void enregistrer(RefondeProjet _projet, File _fichier)
    throws IOException {
    ecrire(_projet, _fichier);
    modifie_= false;
  }
  /**
   * Lecture des informations sur version 506 et moins
   */
  private void lire506(RefondeProjet _projet, StreamTokenizer _file)
    throws IOException {
    RefondeAngle ai;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();
    Vector vdms= geo.getDomaines();
    // Correspondance objet -> numéro
    Hashtable hpls;
    //... Lignes
    hpls= new Hashtable(vpls.size());
    for (int i= 0; i < vpls.size(); i++)
      hpls.put(vpls.get(i), new Integer(i));
    // Hauteur de mer
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurMer_= Double.parseDouble(_file.sval);
    // Periode de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    periodeHoule_= Double.parseDouble(_file.sval);
    // Angle de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    angleHoule_= Double.parseDouble(_file.sval);
    // Hauteur de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurHoule_= Double.parseDouble(_file.sval);
    // Profondeur d'origine (inutilisé à partir de la version 5.12)
    if (_file.nextToken() != WORD)
      throw new IOException();
    //    profondeurOrigine_=Double.parseDouble(_file.sval);
    //double dummy=Double.parseDouble(_file.sval);
    // Cas de bord ouvert
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("cond_absorbante"))
      casBordOuvert_= 0;
    else if (_file.sval.equals("fa_ile"))
      casBordOuvert_= 1;
    else if (_file.sval.equals("fa_port"))
      casBordOuvert_= 2;
    else
      throw new IOException();
    // Ordre max de troncature
    if (_file.nextToken() != WORD)
      throw new IOException();
    ordreMax_= (int)Double.parseDouble(_file.sval);
    // Houle (non lu sur fichier) : régulière
    typeHoule_= HOULE_REG;
    // Déferlement (non lu sur fichier) : Pas de déferlement
    deferlement_= DEFER_SANS;
    // Angles d'incidence sur les frontières
    aiss= new Vector();
    while (_file.nextToken() != WORD || !_file.sval.equals("<fin>")) {
      // Pour chaque contour
      ais= new Vector();
      while (_file.ttype != WORD || !_file.sval.equals("<fin>")) {
        if (_file.ttype == EOF)
          throw new IOException();
        // Abscisse de début de segment
        if (_file.ttype != WORD)
          throw new IOException();
        sDeb= Double.parseDouble(_file.sval);
        // Abscisse de fin du segment
        if (_file.nextToken() != WORD)
          throw new IOException();
        sFin= Double.parseDouble(_file.sval);
        // Type de l'angle
        if (_file.nextToken() != WORD)
          throw new IOException();
        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }
        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }
        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff= new GrPoint();
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.x_= Double.parseDouble(_file.sval);
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.y_= Double.parseDouble(_file.sval);
          ptDiff.z_= 0.;
          ai= new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        } else
          throw new IOException();
        _file.nextToken();
        ais.add(ai);
      }
      aiss.add(ais);
    }
    Object[] objs= aiss.toArray();
    angles_= new Vector[objs.length];
    for (int i= 0; i < angles_.length; i++)
      angles_[i]= (Vector)objs[i];
    // Correction des angles
    corrigeAngles();
    // Traitement des angles sur les digues
    for (int i= 0; i < vdms.size(); i++) {
      if (vdms.get(i) instanceof RefondeDomaineDigue) {
        RefondeDomaineDigue dm= (RefondeDomaineDigue)vdms.get(i);
        if (dm.getGroupeProprietes().getType()
          == RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
          creerAngles(dm);
      }
    }
  }

  /**
   * Lecture des informations sur version 508 et moins
   */
  private void lire508(RefondeProjet _projet, StreamTokenizer _file)
    throws RefondeIOException, IOException {
    int nbAiDigue;
    int nbAtDigue;
    int ival;
    RefondeAngle ai;
    RefondePolyligne pl;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    // Hauteur de mer
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurMer_= Double.parseDouble(_file.sval);
    // Periode de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    periodeHoule_= Double.parseDouble(_file.sval);
    // Angle de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    angleHoule_= Double.parseDouble(_file.sval);
    // Hauteur de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurHoule_= Double.parseDouble(_file.sval);
    // Profondeur d'origine (inutilisé à partir de version 5.12).
    if (_file.nextToken() != WORD)
      throw new IOException();
    //    profondeurOrigine_=Double.parseDouble(_file.sval);
    //    double dummy=Double.parseDouble(_file.sval);
    // Cas de bord ouvert
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("cond_absorbante"))
      casBordOuvert_= 0;
    else if (_file.sval.equals("fa_ile"))
      casBordOuvert_= 1;
    else if (_file.sval.equals("fa_port"))
      casBordOuvert_= 2;
    else
      throw new IOException();
    // Ordre max de troncature
    if (_file.nextToken() != WORD)
      throw new IOException();
    ordreMax_= (int)Double.parseDouble(_file.sval);
    // Houle (non lu sur fichier) : régulière
    typeHoule_= HOULE_REG;
    // Déferlement (non lu sur fichier) : Pas de déferlement
    deferlement_= DEFER_SANS;
    // Angles d'incidence sur les frontières
    aiss= new Vector();
    while (_file.nextToken() != WORD || !_file.sval.equals("<fin>")) {
      // Pour chaque contour
      ais= new Vector();
      while (_file.ttype != WORD || !_file.sval.equals("<fin>")) {
        if (_file.ttype == EOF)
          throw new IOException();
        // Abscisse de début de segment
        if (_file.ttype != WORD)
          throw new IOException();
        sDeb= Double.parseDouble(_file.sval);
        // Abscisse de fin du segment
        if (_file.nextToken() != WORD)
          throw new IOException();
        sFin= Double.parseDouble(_file.sval);
        // Type de l'angle
        if (_file.nextToken() != WORD)
          throw new IOException();
        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }
        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }
        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff= new GrPoint();
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.x_= Double.parseDouble(_file.sval);
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.y_= Double.parseDouble(_file.sval);
          ptDiff.z_= 0.;
          ai= new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        } else
          throw new IOException();
        _file.nextToken();
        ais.add(ai);
      }
      aiss.add(ais);
    }
    Object[] objs= aiss.toArray();
    angles_= new Vector[objs.length];
    for (int i= 0; i < angles_.length; i++)
      angles_[i]= (Vector)objs[i];
    // Correction des angles
    corrigeAngles();
    // Angles d'incidence sur les digues
    // Nombre d'angles d'incidence sur les digues
    if (_file.nextToken() != WORD || !_file.sval.equals("<angles_incidence>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAiDigue= Integer.parseInt(_file.sval);
    // Angles d'incidence sur les digues
    //    ai2pl_=new Hashtable(nbAiDigue);
    for (int i= 0; i < nbAiDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      ai2pl_.put(ai, pl);
    }
    // Angles de transmission sur les digues
    // Nombre d'angles de transmission sur les digues
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<angles_transmission>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAtDigue= Integer.parseInt(_file.sval);
    // Angles dde transmission sur les digues
    at2pl_= new Hashtable(nbAtDigue);
    for (int i= 0; i < nbAtDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      at2pl_.put(ai, pl);
    }
  }

  /**
   * Lecture des informations sur version 512 et moins
   */
  private void lire512(RefondeProjet _projet, StreamTokenizer _file)
    throws RefondeIOException, IOException {
    int nbAiDigue;
    int nbAtDigue;
    int ival;
    RefondeAngle ai;
    RefondePolyligne pl;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    // Paramètres généraux
    if (_file.nextToken() != WORD || !_file.sval.equals("<parametres>"))
      throw new IOException();
    // Hauteur de mer
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurMer_= Double.parseDouble(_file.sval);
    // Profondeur d'origine (inutilisée à partir de version 5.12. A supprimer).
    if (_file.nextToken() != WORD)
      throw new IOException();
    //    profondeurOrigine_=Double.parseDouble(_file.sval);
    //double dummy= Double.parseDouble(_file.sval);
    // Cas de bord ouvert
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("cond_absorbante"))
      casBordOuvert_= 0;
    else if (_file.sval.equals("fa_ile"))
      casBordOuvert_= 1;
    else if (_file.sval.equals("fa_port"))
      casBordOuvert_= 2;
    else
      throw new IOException();
    // Ordre max de troncature
    if (_file.nextToken() != WORD)
      throw new IOException();
    ordreMax_= (int)Double.parseDouble(_file.sval);
    // Paramètres de houle
    if (_file.nextToken() != WORD || !_file.sval.equals("houle"))
      throw new IOException();
    // Cas de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("reguliere"))
      typeHoule(HOULE_REG);
    else if (_file.sval.equals("aleatoire"))
      typeHoule(HOULE_ALEA);
    else
      throw new IOException();
    // Houle régulière
    if (typeHoule() == HOULE_REG) {
      // Hauteur
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);
      // Période
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);
      // Direction
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);
    }
    // Houle aléatoire
    else {
      // Nombre de periodes
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbPeriodes_= (int)Double.parseDouble(_file.sval);
      // Periode min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMin_= Double.parseDouble(_file.sval);
      // Periode max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMax_= Double.parseDouble(_file.sval);
      // Nombre de directions
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbAnglesHoule_= (int)Double.parseDouble(_file.sval);
      // Direction min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMin_= Double.parseDouble(_file.sval);
      // Direction max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMax_= Double.parseDouble(_file.sval);
      // Hauteur significative
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);
      // Periode de pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);
      // Rehaussement du pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      rehaussementPic_= Double.parseDouble(_file.sval);
      // Direction principale
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);
      // Répartition angulaire
      if (_file.nextToken() != WORD)
        throw new IOException();
      repartitionAngle_= Double.parseDouble(_file.sval);
    }
    // Paramètres de déferlement
    if (_file.nextToken() != WORD || !_file.sval.equals("deferlement"))
      throw new IOException();
    // Cas de déferlement
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("sans"))
      deferlement_= DEFER_SANS;
    else if (_file.sval.equals("iteratif"))
      deferlement_= DEFER_ITERATIF;
    else if (_file.sval.equals("ecretage"))
      deferlement_= DEFER_ECRETAGE;
    else
      throw new IOException();
    // Formule pour l'ecretage
    if (deferlement_ == DEFER_ECRETAGE) {
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("goda_sans"))
        formule_= FORMULE_GODA_SANS_PENTE;
      else if (_file.sval.equals("goda_avec"))
        formule_= FORMULE_GODA_AVEC_PENTE;
      else if (_file.sval.equals("miche"))
        formule_= FORMULE_MICHE_MODIFIEE;
      else if (_file.sval.equals("munk"))
        formule_= FORMULE_MUNK;
      else
        throw new IOException();
    }
    // Angles d'incidence sur les frontières
    aiss= new Vector();
    while (_file.nextToken() != WORD || !_file.sval.equals("<fin>")) {
      // Pour chaque contour
      ais= new Vector();
      while (_file.ttype != WORD || !_file.sval.equals("<fin>")) {
        if (_file.ttype == EOF)
          throw new IOException();
        // Abscisse de début de segment
        if (_file.ttype != WORD)
          throw new IOException();
        sDeb= Double.parseDouble(_file.sval);
        // Abscisse de fin du segment
        if (_file.nextToken() != WORD)
          throw new IOException();
        sFin= Double.parseDouble(_file.sval);
        // Type de l'angle
        if (_file.nextToken() != WORD)
          throw new IOException();
        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }
        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }
        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff= new GrPoint();
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.x_= Double.parseDouble(_file.sval);
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.y_= Double.parseDouble(_file.sval);
          ptDiff.z_= 0.;
          ai= new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        } else
          throw new IOException();
        _file.nextToken();
        ais.add(ai);
      }
      aiss.add(ais);
    }
    Object[] objs= aiss.toArray();
    angles_= new Vector[objs.length];
    for (int i= 0; i < angles_.length; i++)
      angles_[i]= (Vector)objs[i];
    // Correction des angles
    corrigeAngles();
    // Angles d'incidence sur les digues
    // Nombre d'angles d'incidence sur les digues
    if (_file.nextToken() != WORD || !_file.sval.equals("<angles_incidence>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAiDigue= Integer.parseInt(_file.sval);
    // Angles d'incidence sur les digues
    //    ai2pl_=new Hashtable(nbAiDigue);
    for (int i= 0; i < nbAiDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      ai2pl_.put(ai, pl);
    }
    // Angles de transmission sur les digues
    // Nombre d'angles de transmission sur les digues
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<angles_transmission>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAtDigue= Integer.parseInt(_file.sval);
    // Angles dde transmission sur les digues
    at2pl_= new Hashtable(nbAtDigue);
    for (int i= 0; i < nbAtDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      at2pl_.put(ai, pl);
    }
  }

  /**
   * Lecture des informations sur version 5.14 et moins
   */
  private void lire514(RefondeProjet _projet, StreamTokenizer _file)
    throws RefondeIOException, IOException {
    int nbAiDigue;
    int nbAtDigue;
    int ival;
    RefondeAngle ai;
    RefondePolyligne pl;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    // Paramètres généraux
    if (_file.nextToken() != WORD || !_file.sval.equals("<parametres>"))
      throw new IOException();
    // Hauteur de mer
    if (_file.nextToken() != WORD)
      throw new IOException();
    hauteurMer_= Double.parseDouble(_file.sval);
    // Profondeur d'origine (inutilisée à partir de version 5.12. A supprimer).
    if (_file.nextToken() != WORD)
      throw new IOException();
    //    profondeurOrigine_=Double.parseDouble(_file.sval);
    //    double dummy=Double.parseDouble(_file.sval);
    // Cas de bord ouvert
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("cond_absorbante"))
      casBordOuvert_= BORD_COND_ORDRE_1;
    else if (_file.sval.equals("fa_ile"))
      casBordOuvert_= BORD_FORM_ANA_ILE;
    else if (_file.sval.equals("fa_port"))
      casBordOuvert_= BORD_FORM_ANA_PORT;
    else
      throw new IOException();
    // Ordre max de troncature
    if (_file.nextToken() != WORD)
      throw new IOException();
    ordreMax_= (int)Double.parseDouble(_file.sval);
    // Paramètres de houle
    if (_file.nextToken() != WORD || !_file.sval.equals("houle"))
      throw new IOException();
    // Cas de houle
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("reguliere"))
      typeHoule(HOULE_REG);
    else if (_file.sval.equals("aleatoire"))
      typeHoule(HOULE_ALEA);
    else
      throw new IOException();
    // Houle régulière
    if (typeHoule() == HOULE_REG) {
      // Hauteur
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);
      // Période
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);
      // Direction
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);
    }
    // Houle aléatoire
    else {
      // Nombre de periodes
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbPeriodes_= (int)Double.parseDouble(_file.sval);
      // Periode min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMin_= Double.parseDouble(_file.sval);
      // Periode max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMax_= Double.parseDouble(_file.sval);
      // Nombre de directions
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbAnglesHoule_= (int)Double.parseDouble(_file.sval);
      // Direction min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMin_= Double.parseDouble(_file.sval);
      // Direction max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMax_= Double.parseDouble(_file.sval);
      // Hauteur significative
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);
      // Periode de pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);
      // Rehaussement du pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      rehaussementPic_= Double.parseDouble(_file.sval);
      // Direction principale
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);
      // Répartition angulaire
      if (_file.nextToken() != WORD)
        throw new IOException();
      repartitionAngle_= Double.parseDouble(_file.sval);
    }
    // Paramètres de déferlement
    if (_file.nextToken() != WORD || !_file.sval.equals("deferlement"))
      throw new IOException();
    // Cas de déferlement
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("sans"))
      deferlement_= DEFER_SANS;
    else if (_file.sval.equals("iteratif"))
      deferlement_= DEFER_ITERATIF;
    else if (_file.sval.equals("ecretage"))
      deferlement_= DEFER_ECRETAGE;
    else
      throw new IOException();
    // Formule pour l'ecretage
    if (deferlement_ == DEFER_ECRETAGE) {
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("goda_sans"))
        formule_= FORMULE_GODA_SANS_PENTE;
      else if (_file.sval.equals("goda_avec"))
        formule_= FORMULE_GODA_AVEC_PENTE;
      else if (_file.sval.equals("miche"))
        formule_= FORMULE_MICHE_MODIFIEE;
      else if (_file.sval.equals("munk"))
        formule_= FORMULE_MUNK;
      else
        throw new IOException();
    }
    // Angles d'incidence sur les frontières
    aiss= new Vector();
    while (_file.nextToken() != WORD || !_file.sval.equals("<fin>")) {
      // Pour chaque contour
      ais= new Vector();
      while (_file.ttype != WORD || !_file.sval.equals("<fin>")) {
        if (_file.ttype == EOF)
          throw new IOException();
        // Abscisse de début de segment
        if (_file.ttype != WORD)
          throw new IOException();
        sDeb= Double.parseDouble(_file.sval);
        // Abscisse de fin du segment
        if (_file.nextToken() != WORD)
          throw new IOException();
        sFin= Double.parseDouble(_file.sval);
        // Type de l'angle
        if (_file.nextToken() != WORD)
          throw new IOException();
        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }
        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }
        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff= new GrPoint();
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.x_= Double.parseDouble(_file.sval);
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.y_= Double.parseDouble(_file.sval);
          ptDiff.z_= 0.;
          ai= new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        } else
          throw new IOException();
        // Angle supplémentaire associé.
        // Type de l'angle supplémentaire
        if (_file.nextToken() != WORD)
          throw new IOException();
        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai.setSupAbsolu(angle);
        }
        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai.setSupRelatif(angle);
        } else
          throw new IOException();
        _file.nextToken();
        ais.add(ai);
      }
      aiss.add(ais);
    }
    Object[] objs= aiss.toArray();
    angles_= new Vector[objs.length];
    for (int i= 0; i < angles_.length; i++)
      angles_[i]= (Vector)objs[i];
    // Correction des angles
    corrigeAngles();
    // Angles d'incidence sur les digues
    // Nombre d'angles d'incidence sur les digues
    if (_file.nextToken() != WORD || !_file.sval.equals("<angles_incidence>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAiDigue= Integer.parseInt(_file.sval);
    // Angles d'incidence sur les digues
    //    ai2pl_=new Hashtable(nbAiDigue);
    for (int i= 0; i < nbAiDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      ai2pl_.put(ai, pl);
    }
    // Angles de transmission sur les digues
    // Nombre d'angles de transmission sur les digues
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<angles_transmission>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAtDigue= Integer.parseInt(_file.sval);
    // Angles dde transmission sur les digues
    at2pl_= new Hashtable(nbAtDigue);
    for (int i= 0; i < nbAtDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);
      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);
      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();
      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }
      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }
      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      at2pl_.put(ai, pl);
    }
  }

  /**
   * Lecture des informations sur version inferieur a 5.14a et moins
   */
  private void lire514a(RefondeProjet _projet, StreamTokenizer _file)
    throws RefondeIOException, IOException {
    int nbAiDigue;
    int nbAtDigue;
    int ival;
    RefondeAngle ai;
    RefondePolyligne pl;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();

    // Identifiant de géométrie
    if (_file.nextToken()!=WORD || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();

    if (_file.nextToken()!=WORD || Integer.parseInt(_file.sval)!=geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");

    // Paramètres généraux
    if (_file.nextToken()!=WORD || !_file.sval.equals("<parametres>"))
      throw new IOException();

    // Hauteur de mer
    if (_file.nextToken()!=WORD)
      throw new IOException();
    hauteurMer_=Double.parseDouble(_file.sval);

    // Cas de bord ouvert
    if (_file.nextToken()!=WORD)
      throw new IOException();

    if (_file.sval.equals("cond_abs_ordre1"))
      casBordOuvert_= BORD_COND_ORDRE_1;
    else if (_file.sval.equals("cond_abs_ordre2"))
      casBordOuvert_= BORD_COND_ORDRE_2;
    else if (_file.sval.equals("fa_ile"))
      casBordOuvert_= BORD_FORM_ANA_ILE;
    else if (_file.sval.equals("fa_port"))
      casBordOuvert_= BORD_FORM_ANA_PORT;
    else
      throw new IOException();

    // Ordre max de troncature
    if (_file.nextToken() != WORD)
      throw new IOException();
    ordreMax_= (int)Double.parseDouble(_file.sval);

    // Fonds poreux
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("poreux"))
      fondsPoreux_= true;
    else if (_file.sval.equals("non_poreux"))
      fondsPoreux_= false;
    else
      throw new IOException();

    // Paramètres de houle
    if (_file.nextToken() != WORD || !_file.sval.equals("houle"))
      throw new IOException();

    // Cas de houle
    if (_file.nextToken() != WORD)
      throw new IOException();

    if (_file.sval.equals("reguliere"))
      typeHoule(HOULE_REG);
    else if (_file.sval.equals("aleatoire"))
      typeHoule(HOULE_ALEA);
    else
      throw new IOException();

    // Houle régulière

    if (typeHoule() == HOULE_REG) {

      // Hauteur
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);

      // Période
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);

      // Direction
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);
    }

    // Houle aléatoire

    else {
      // Nombre de periodes
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbPeriodes_= (int)Double.parseDouble(_file.sval);

      // Periode min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMin_= Double.parseDouble(_file.sval);

      // Periode max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHouleMax_= Double.parseDouble(_file.sval);

      // Nombre de directions
      if (_file.nextToken() != WORD)
        throw new IOException();
      nbAnglesHoule_= (int)Double.parseDouble(_file.sval);

      // Direction min.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMin_= Double.parseDouble(_file.sval);

      // Direction max.
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHouleMax_= Double.parseDouble(_file.sval);

      // Hauteur significative
      if (_file.nextToken() != WORD)
        throw new IOException();
      hauteurHoule_= Double.parseDouble(_file.sval);

      // Periode de pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      periodeHoule_= Double.parseDouble(_file.sval);

      // Rehaussement du pic
      if (_file.nextToken() != WORD)
        throw new IOException();
      rehaussementPic_= Double.parseDouble(_file.sval);

      // Direction principale
      if (_file.nextToken() != WORD)
        throw new IOException();
      angleHoule_= Double.parseDouble(_file.sval);

      // Répartition angulaire
      if (_file.nextToken() != WORD)
        throw new IOException();
      repartitionAngle_= Double.parseDouble(_file.sval);
    }

    // Paramètres de déferlement

    if (_file.nextToken() != WORD || !_file.sval.equals("deferlement"))
      throw new IOException();

    // Cas de déferlement
    if (_file.nextToken() != WORD)
      throw new IOException();
    if (_file.sval.equals("sans"))
      deferlement_= DEFER_SANS;
    else if (_file.sval.equals("iteratif"))
      deferlement_= DEFER_ITERATIF;
    else if (_file.sval.equals("ecretage"))
      deferlement_= DEFER_ECRETAGE;
    else
      throw new IOException();

    // Formule pour l'ecretage
    if (deferlement_ == DEFER_ECRETAGE) {
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("goda_sans"))
        formule_= FORMULE_GODA_SANS_PENTE;
      else if (_file.sval.equals("goda_avec"))
        formule_= FORMULE_GODA_AVEC_PENTE;
      else if (_file.sval.equals("miche"))
        formule_= FORMULE_MICHE_MODIFIEE;
      else if (_file.sval.equals("munk"))
        formule_= FORMULE_MUNK;
      else
        throw new IOException();
    }

    // Angles d'incidence sur les frontières

    aiss= new Vector();
    while (_file.nextToken() != WORD || !_file.sval.equals("<fin>")) {

      // Pour chaque contour
      ais= new Vector();
      while (_file.ttype != WORD || !_file.sval.equals("<fin>")) {
        if (_file.ttype == EOF)
          throw new IOException();

        // Abscisse de début de segment
        if (_file.ttype != WORD)
          throw new IOException();
        sDeb= Double.parseDouble(_file.sval);

        // Abscisse de fin du segment
        if (_file.nextToken() != WORD)
          throw new IOException();
        sFin= Double.parseDouble(_file.sval);

        // Type de l'angle
        if (_file.nextToken() != WORD)
          throw new IOException();

        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }

        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }

        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff= new GrPoint();
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.x_= Double.parseDouble(_file.sval);
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptDiff.y_= Double.parseDouble(_file.sval);
          ptDiff.z_= 0.;
          ai= new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        }
        else
          throw new IOException();

        // Angle supplémentaire associé.

        // Type de l'angle supplémentaire
        if (_file.nextToken() != WORD)
          throw new IOException();

        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai.setSupAbsolu(angle);
        }

        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken() != WORD)
            throw new IOException();
          angle= Double.parseDouble(_file.sval);
          ai.setSupRelatif(angle);
        }
        else
          throw new IOException();
        _file.nextToken();
        ais.add(ai);
      }
      aiss.add(ais);
    }

    Object[] objs= aiss.toArray();
    angles_= new Vector[objs.length];

    for (int i= 0; i < angles_.length; i++)
      angles_[i]= (Vector)objs[i];

    // Correction des angles
    corrigeAngles();

    // Angles d'incidence sur les digues

    // Nombre d'angles d'incidence sur les digues
    if (_file.nextToken() != WORD || !_file.sval.equals("<angles_incidence>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAiDigue= Integer.parseInt(_file.sval);

    // Angles d'incidence sur les digues
    //    ai2pl_=new Hashtable(nbAiDigue);
    for (int i= 0; i < nbAiDigue; i++) {
      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);

      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);

      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);

      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();

      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }

      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }

      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      } else
        throw new IOException();
      ai2pl_.put(ai, pl);
    }

    // Angles de transmission sur les digues

    // Nombre d'angles de transmission sur les digues
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<angles_transmission>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbAtDigue= Integer.parseInt(_file.sval);

    // Angles dde transmission sur les digues
    at2pl_= new Hashtable(nbAtDigue);
    for (int i= 0; i < nbAtDigue; i++) {

      // Polyligne support
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size())
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);

      // Abscisse de début
      if (_file.nextToken() != WORD)
        throw new IOException();
      sDeb= Double.parseDouble(_file.sval);

      // Abscisse de fin
      if (_file.nextToken() != WORD)
        throw new IOException();
      sFin= Double.parseDouble(_file.sval);

      // Type de l'angle
      if (_file.nextToken() != WORD)
        throw new IOException();

      // Absolu => Angle absolu
      if (_file.sval.equals("absolu")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angle);
      }

      // Relatif => Angle par rapport a la normale sur le bord
      else if (_file.sval.equals("relatif")) {
        if (_file.nextToken() != WORD)
          throw new IOException();
        angle= Double.parseDouble(_file.sval);
        ai= new RefondeAngle();
        ai.setRelatif(sDeb, sFin, angle);
      }

      // Diffracté => Position x, y du point de diffraction
      else if (_file.sval.equals("diffracte")) {
        ptDiff= new GrPoint();
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        ptDiff.y_= Double.parseDouble(_file.sval);
        ptDiff.z_= 0.;
        ai= new RefondeAngle();
        ai.setDiffracte(sDeb, sFin, ptDiff);
      }
      else
        throw new IOException();

      at2pl_.put(ai, pl);
    }
  }

  /**
   * Lecture des informations sur version courante
   */
  private void lire(RefondeProjet _projet, StreamTokenizer _file)
    throws RefondeIOException, IOException {
    int nbAiDigue;
    int nbAtDigue;
    int ival;
    RefondeAngle ai;
    RefondePolyligne pl;
    Vector ais;
    Vector aiss;
    double sDeb;
    double sFin;
    double angle;
    GrPoint ptDiff;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();

    // Identifiant de géométrie
    if (_file.nextToken()!=WORD || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();

    if (_file.nextToken()!=WORD || Integer.parseInt(_file.sval)!=geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");

    // Type du modèle
    if (_file.nextToken()!=WORD || !_file.sval.equals("<type_modele>"))
      throw new IOException();

    if (_file.nextToken()!=WORD) throw new IOException();
    if (_file.sval.equals("houle"))       typeModele_=MODELE_HOULE;
    else if (_file.sval.equals("seiche")) typeModele_=MODELE_SEICHE;
    else                                  throw new IOException();

    //--- Paramètres généraux --------------------------------------------------

    if (_file.nextToken()!=WORD || !_file.sval.equals("<parametres>"))
      throw new IOException();

    // Modèle de houle

    if (typeModele_==MODELE_HOULE) {

      // Hauteur de mer
      if (_file.nextToken()!=WORD)
        throw new IOException();
      hauteurMer_=Double.parseDouble(_file.sval);

      // Cas de bord ouvert
      if (_file.nextToken()!=WORD)
        throw new IOException();

      if (_file.sval.equals("cond_abs_ordre1"))
        casBordOuvert_=BORD_COND_ORDRE_1;
      else if (_file.sval.equals("cond_abs_ordre2"))
        casBordOuvert_=BORD_COND_ORDRE_2;
      else if (_file.sval.equals("fa_ile"))
        casBordOuvert_=BORD_FORM_ANA_ILE;
      else if (_file.sval.equals("fa_port"))
        casBordOuvert_=BORD_FORM_ANA_PORT;
      else
        throw new IOException();

      // Ordre max de troncature
      if (_file.nextToken()!=WORD)
        throw new IOException();
      ordreMax_=(int)Double.parseDouble(_file.sval);

      // Fonds poreux
      if (_file.nextToken()!=WORD)
        throw new IOException();
      if (_file.sval.equals("poreux"))
        fondsPoreux_=true;
      else if (_file.sval.equals("non_poreux"))
        fondsPoreux_=false;
      else
        throw new IOException();

      // Paramètres de houle
      if (_file.nextToken()!=WORD||!_file.sval.equals("houle"))
        throw new IOException();

      // Cas de houle
      if (_file.nextToken()!=WORD)
        throw new IOException();

      if (_file.sval.equals("reguliere"))
        typeHoule(HOULE_REG);
      else if (_file.sval.equals("aleatoire"))
        typeHoule(HOULE_ALEA);
      else
        throw new IOException();

      // Houle régulière

      if (typeHoule()==HOULE_REG) {

        // Hauteur
        if (_file.nextToken()!=WORD)
          throw new IOException();
        hauteurHoule_=Double.parseDouble(_file.sval);

        // Période
        if (_file.nextToken()!=WORD)
          throw new IOException();
        periodeHoule_=Double.parseDouble(_file.sval);

        // Direction
        if (_file.nextToken()!=WORD)
          throw new IOException();
        angleHoule_=Double.parseDouble(_file.sval);
      }

      // Houle aléatoire

      else {
        // Nombre de periodes
        if (_file.nextToken()!=WORD)
          throw new IOException();
        nbPeriodes_=(int)Double.parseDouble(_file.sval);

        // Periode min.
        if (_file.nextToken()!=WORD)
          throw new IOException();
        periodeHouleMin_=Double.parseDouble(_file.sval);

        // Periode max.
        if (_file.nextToken()!=WORD)
          throw new IOException();
        periodeHouleMax_=Double.parseDouble(_file.sval);

        // Nombre de directions
        if (_file.nextToken()!=WORD)
          throw new IOException();
        nbAnglesHoule_=(int)Double.parseDouble(_file.sval);

        // Direction min.
        if (_file.nextToken()!=WORD)
          throw new IOException();
        angleHouleMin_=Double.parseDouble(_file.sval);

        // Direction max.
        if (_file.nextToken()!=WORD)
          throw new IOException();
        angleHouleMax_=Double.parseDouble(_file.sval);

        // Hauteur significative
        if (_file.nextToken()!=WORD)
          throw new IOException();
        hauteurHoule_=Double.parseDouble(_file.sval);

        // Periode de pic
        if (_file.nextToken()!=WORD)
          throw new IOException();
        periodeHoule_=Double.parseDouble(_file.sval);

        // Rehaussement du pic
        if (_file.nextToken()!=WORD)
          throw new IOException();
        rehaussementPic_=Double.parseDouble(_file.sval);

        // Direction principale
        if (_file.nextToken()!=WORD)
          throw new IOException();
        angleHoule_=Double.parseDouble(_file.sval);

        // Répartition angulaire
        if (_file.nextToken()!=WORD)
          throw new IOException();
        repartitionAngle_=Double.parseDouble(_file.sval);
      }

      // Paramètres de déferlement

      if (_file.nextToken()!=WORD||!_file.sval.equals("deferlement"))
        throw new IOException();

      // Cas de déferlement
      if (_file.nextToken()!=WORD)
        throw new IOException();
      if (_file.sval.equals("sans"))
        deferlement_=DEFER_SANS;
      else if (_file.sval.equals("iteratif"))
        deferlement_=DEFER_ITERATIF;
      else if (_file.sval.equals("ecretage"))
        deferlement_=DEFER_ECRETAGE;
      else
        throw new IOException();

      // Formule pour l'ecretage
      if (deferlement_==DEFER_ECRETAGE) {
        if (_file.nextToken()!=WORD)
          throw new IOException();
        if (_file.sval.equals("goda_sans"))
          formule_=FORMULE_GODA_SANS_PENTE;
        else if (_file.sval.equals("goda_avec"))
          formule_=FORMULE_GODA_AVEC_PENTE;
        else if (_file.sval.equals("miche"))
          formule_=FORMULE_MICHE_MODIFIEE;
        else if (_file.sval.equals("munk"))
          formule_=FORMULE_MUNK;
        else
          throw new IOException();
      }
    }

    // Modèle de seiche

    else {
      // Hauteur de mer
      if (_file.nextToken()!=WORD) throw new IOException();
      hauteurMer_=Double.parseDouble(_file.sval);
      // Nombre de valeurs propres
      if (_file.nextToken()!=WORD) throw new IOException();
      nbValPropres_=Integer.parseInt(_file.sval);
      // Nombre d'itération max
      if (_file.nextToken()!=WORD) throw new IOException();
      nbIterMax_=Integer.parseInt(_file.sval);
      // Décalage des valeurs propres
      if (_file.nextToken()!=WORD) throw new IOException();
      decalValPropres_=Double.parseDouble(_file.sval);
      // Précision de convergence
      if (_file.nextToken()!=WORD) throw new IOException();
      precision_=Double.parseDouble(_file.sval);
    }

    //--- Angles d'incidence sur les frontières --------------------------------

    // Modèle de houle

    if (typeModele_==MODELE_HOULE) {

      aiss=new Vector();
      while (_file.nextToken()!=WORD||!_file.sval.equals("<fin>")) {

        // Pour chaque contour
        ais=new Vector();
        while (_file.ttype!=WORD||!_file.sval.equals("<fin>")) {
          if (_file.ttype==EOF)
            throw new IOException();

          // Abscisse de début de segment
          if (_file.ttype!=WORD)
            throw new IOException();
          sDeb=Double.parseDouble(_file.sval);

          // Abscisse de fin du segment
          if (_file.nextToken()!=WORD)
            throw new IOException();
          sFin=Double.parseDouble(_file.sval);

          // Type de l'angle
          if (_file.nextToken()!=WORD)
            throw new IOException();

          // Absolu => Angle absolu
          if (_file.sval.equals("absolu")) {
            if (_file.nextToken()!=WORD)
              throw new IOException();
            angle=Double.parseDouble(_file.sval);
            ai=new RefondeAngle();
            ai.setAbsolu(sDeb, sFin, angle);
          }

          // Relatif => Angle par rapport a la normale sur le bord
          else if (_file.sval.equals("relatif")) {
            if (_file.nextToken()!=WORD)
              throw new IOException();
            angle=Double.parseDouble(_file.sval);
            ai=new RefondeAngle();
            ai.setRelatif(sDeb, sFin, angle);
          }

          // Diffracté => Position x, y du point de diffraction
          else if (_file.sval.equals("diffracte")) {
            ptDiff=new GrPoint();
            if (_file.nextToken()!=WORD)
              throw new IOException();
            ptDiff.x_=Double.parseDouble(_file.sval);
            if (_file.nextToken()!=WORD)
              throw new IOException();
            ptDiff.y_=Double.parseDouble(_file.sval);
            ptDiff.z_=0.;
            ai=new RefondeAngle();
            ai.setDiffracte(sDeb, sFin, ptDiff);
          }
          else
            throw new IOException();

          // Angle supplémentaire associé.

          // Type de l'angle supplémentaire
          if (_file.nextToken()!=WORD)
            throw new IOException();

          // Absolu => Angle absolu
          if (_file.sval.equals("absolu")) {
            if (_file.nextToken()!=WORD)
              throw new IOException();
            angle=Double.parseDouble(_file.sval);
            ai.setSupAbsolu(angle);
          }

          // Relatif => Angle par rapport a la normale sur le bord
          else if (_file.sval.equals("relatif")) {
            if (_file.nextToken()!=WORD)
              throw new IOException();
            angle=Double.parseDouble(_file.sval);
            ai.setSupRelatif(angle);
          }
          else
            throw new IOException();
          _file.nextToken();
          ais.add(ai);
        }
        aiss.add(ais);
      }

      Object[] objs=aiss.toArray();
      angles_=new Vector[objs.length];

      for (int i=0; i<angles_.length; i++)
        angles_[i]=(Vector)objs[i];

        // Correction des angles
      corrigeAngles();

      // Angles d'incidence sur les digues

      // Nombre d'angles d'incidence sur les digues
      if (_file.nextToken()!=WORD||!_file.sval.equals("<angles_incidence>"))
        throw new IOException();
      if (_file.nextToken()!=WORD)
        throw new IOException();
      nbAiDigue=Integer.parseInt(_file.sval);

      // Angles d'incidence sur les digues
      //    ai2pl_=new Hashtable(nbAiDigue);
      for (int i=0; i<nbAiDigue; i++) {
        // Polyligne support
        if (_file.nextToken()!=WORD)
          throw new IOException();
        ival=Integer.parseInt(_file.sval);
        if (ival<0||ival>vpls.size())
          throw new IOException();
        pl=(RefondePolyligne)vpls.get(ival);

        // Abscisse de début
        if (_file.nextToken()!=WORD)
          throw new IOException();
        sDeb=Double.parseDouble(_file.sval);

        // Abscisse de fin
        if (_file.nextToken()!=WORD)
          throw new IOException();
        sFin=Double.parseDouble(_file.sval);

        // Type de l'angle
        if (_file.nextToken()!=WORD)
          throw new IOException();

        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken()!=WORD)
            throw new IOException();
          angle=Double.parseDouble(_file.sval);
          ai=new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }

        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken()!=WORD)
            throw new IOException();
          angle=Double.parseDouble(_file.sval);
          ai=new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }

        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff=new GrPoint();
          if (_file.nextToken()!=WORD)
            throw new IOException();
          ptDiff.x_=Double.parseDouble(_file.sval);
          if (_file.nextToken()!=WORD)
            throw new IOException();
          ptDiff.y_=Double.parseDouble(_file.sval);
          ptDiff.z_=0.;
          ai=new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        }
        else
          throw new IOException();
        ai2pl_.put(ai, pl);
      }

      // Angles de transmission sur les digues

      // Nombre d'angles de transmission sur les digues
      if (_file.nextToken()!=WORD
          ||!_file.sval.equals("<angles_transmission>"))
        throw new IOException();
      if (_file.nextToken()!=WORD)
        throw new IOException();
      nbAtDigue=Integer.parseInt(_file.sval);

      // Angles dde transmission sur les digues
      at2pl_=new Hashtable(nbAtDigue);
      for (int i=0; i<nbAtDigue; i++) {

        // Polyligne support
        if (_file.nextToken()!=WORD)
          throw new IOException();
        ival=Integer.parseInt(_file.sval);
        if (ival<0||ival>vpls.size())
          throw new IOException();
        pl=(RefondePolyligne)vpls.get(ival);

        // Abscisse de début
        if (_file.nextToken()!=WORD)
          throw new IOException();
        sDeb=Double.parseDouble(_file.sval);

        // Abscisse de fin
        if (_file.nextToken()!=WORD)
          throw new IOException();
        sFin=Double.parseDouble(_file.sval);

        // Type de l'angle
        if (_file.nextToken()!=WORD)
          throw new IOException();

        // Absolu => Angle absolu
        if (_file.sval.equals("absolu")) {
          if (_file.nextToken()!=WORD)
            throw new IOException();
          angle=Double.parseDouble(_file.sval);
          ai=new RefondeAngle();
          ai.setAbsolu(sDeb, sFin, angle);
        }

        // Relatif => Angle par rapport a la normale sur le bord
        else if (_file.sval.equals("relatif")) {
          if (_file.nextToken()!=WORD)
            throw new IOException();
          angle=Double.parseDouble(_file.sval);
          ai=new RefondeAngle();
          ai.setRelatif(sDeb, sFin, angle);
        }

        // Diffracté => Position x, y du point de diffraction
        else if (_file.sval.equals("diffracte")) {
          ptDiff=new GrPoint();
          if (_file.nextToken()!=WORD)
            throw new IOException();
          ptDiff.x_=Double.parseDouble(_file.sval);
          if (_file.nextToken()!=WORD)
            throw new IOException();
          ptDiff.y_=Double.parseDouble(_file.sval);
          ptDiff.z_=0.;
          ai=new RefondeAngle();
          ai.setDiffracte(sDeb, sFin, ptDiff);
        }
        else
          throw new IOException();

        at2pl_.put(ai, pl);
      }
    }

    // Modèle de seiche => Initialisation des angles pour éviter les effets de
    // bord du fait d'une non initialisation, mais s'ils ne sont pas utilisés
    // dans l'ihm.

    else {
      calculAngles(_projet);
    }
  }

  /**
   * Ecriture des informations sur le fichier associé
   */
  private void ecrire(RefondeProjet _projet, File _fcCalcul)
    throws IOException {
    RefondeAngle ai;
    RefondePolyligne pl;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vpls= geo.getPolylignes();
    // Correspondance objet -> numéro
    Hashtable hpls;
    //... Lignes
    hpls= new Hashtable(vpls.size());
    for (int i= 0; i < vpls.size(); i++)
      hpls.put(vpls.get(i), new Integer(i));

    PrintWriter file= null;
    try {
      // Ouverture du fichier
      file= new PrintWriter(new FileWriter(_fcCalcul));

      // Entète du fichier
      file.print("refonde ; ");
      file.print(RefondeImplementation.informationsSoftware().version + " ; ");
      file.print("modele_calcul");
      file.println();
      file.println();

      // Identifiant de géométrie
      file.println("<identifiant_geometrie> ; " + geo.identifiant());
      file.println();

      // Type du modèle
      file.print("<type_modele> ; ");
      if (typeModele_==MODELE_HOULE) file.println("houle");
      else                           file.println("seiche");
      file.println();

      //--- Paramètres généraux ------------------------------------------------

      file.println("<parametres>");

      // Modèle de houle

      if (typeModele_==MODELE_HOULE) {

        // Hauteur de mer
        file.print(hauteurMer_+" ; ");
        // Profondeur d'origine (inutilisé, à supprimer du format).
        //      file.print(profondeurOrigine_+" ; ");
        //      file.print("0 ; ");
        // Cas de bord ouvert
        switch (casBordOuvert_) {
          case BORD_COND_ORDRE_1:
            file.print("cond_abs_ordre1 ; ");
            break;
          case BORD_COND_ORDRE_2:
            file.print("cond_abs_ordre2 ; ");
            break;
          case BORD_FORM_ANA_ILE:
            file.print("fa_ile ; ");
            break;
          case BORD_FORM_ANA_PORT:
            file.print("fa_port ; ");
            break;
        }
        // Ordre max de troncature
        file.print(ordreMax_+" ; ");
        // Fonds poreux
        file.print(fondsPoreux_?"poreux":"non_poreux");
        file.println();

        // Paramètres de houle

        file.print("houle ; ");

        // Houle régulière
        if (typeHoule()==HOULE_REG) {
          file.print("reguliere ; ");
          // Hauteur
          file.print(hauteurHoule_+" ; ");
          // Période
          file.print(periodeHoule_+" ; ");
          // Direction
          file.println(angleHoule_);
        }

        // Houle aléatoire
        else {
          file.print("aleatoire ; ");
          // Nombre de periodes
          file.print(nbPeriodes_+" ; ");
          // Periode min.
          file.print(periodeHouleMin_+" ; ");
          // Periode max.
          file.print(periodeHouleMax_+" ; ");
          // Nombre de directions
          file.print(nbAnglesHoule_+" ; ");
          // Direction min.
          file.print(angleHouleMin_+" ; ");
          // Direction max.
          file.print(angleHouleMax_+" ; ");
          // Hauteur significative
          file.print(hauteurHoule_+" ; ");
          // Periode de pic
          file.print(periodeHoule_+" ; ");
          // Rehaussement du pic
          file.print(rehaussementPic_+" ; ");
          // Direction principale
          file.print(angleHoule_+" ; ");
          // Répartition angulaire
          file.println(repartitionAngle_);
        }

        // Paramètres de déferlement
        file.print("deferlement ; ");
        switch (deferlement_) {
          case DEFER_SANS:
            file.println("sans");
            break;
          case DEFER_ITERATIF:
            file.println("iteratif");
            break;
          case DEFER_ECRETAGE:
            file.print("ecretage ; ");
            switch (formule_) {
              case FORMULE_GODA_SANS_PENTE:
                file.println("goda_sans");
                break;
              case FORMULE_GODA_AVEC_PENTE:
                file.println("goda_avec");
                break;
              case FORMULE_MICHE_MODIFIEE:
                file.println("miche");
                break;
              case FORMULE_MUNK:
                file.println("munk");
                break;
            }
            break;
        }
        file.println();
      }

      // Modèle de seiche

      else {
        // Hauteur de mer
        file.print(hauteurMer_+" ; ");
        // Nombre de valeurs propres
        file.print(nbValPropres_+" ; ");
        // Nombre d'itération max
        file.print(nbIterMax_+" ; ");
        // Décalage des valeurs propres
        file.print(decalValPropres_+" ; ");
        // Précision de convergence
        file.println(precision_);
      }

      //--- Angles d'incidence sur les frontières ( et angles supplémentaires) -

      if (typeModele_==MODELE_HOULE) {

        for (int i=0; i<angles_.length; i++) {
          for (int j=0; j<angles_[i].size(); j++) {

            ai=(RefondeAngle)angles_[i].get(j);
            // Abscisse de début de segment
            file.print(ai.getSDebut()+" ; ");
            // Abscisse de fin du segment
            file.print(ai.getSFin()+" ; ");

            // Suivant le type de l'angle d'incidence

            // Absolu
            if (ai.getType()==RefondeAngle.ABSOLU) {
              file.print("absolu ; ");
              file.print(ai.getAngle()+" ; ");
            }

            // Relatif
            else if (ai.getType()==RefondeAngle.RELATIF) {
              file.print("relatif ; ");
              file.print(ai.getAngle()+" ; ");
            }

            // Diffracté
            else if (ai.getType()==RefondeAngle.DIFFRACTE) {
              file.print("diffracte ; ");
              file.print(ai.getPointDiffraction().x_+" ; ");
              file.print(ai.getPointDiffraction().y_+" ; ");
            }

            // Suivant le type de l'angle supplémentaire

            // Absolu
            if (ai.getTypeSup()==RefondeAngle.ABSOLU) {
              file.print("absolu ; ");
              file.print(ai.getAngleSup()+" ; ");
            }

            // Relatif
            else if (ai.getTypeSup()==RefondeAngle.RELATIF) {
              file.print("relatif ; ");
              file.print(ai.getAngleSup()+" ; ");
            }
            file.println();
          }
          file.println("<fin>");
          file.println();
        }
        file.println("<fin>");
        file.println();

        // Nombre d'angles d'incidence sur les digues
        file.println("<angles_incidence> ; "+ai2pl_.size());

        // Angles d'incidence sur les digues
        for (Enumeration e=ai2pl_.keys(); e.hasMoreElements(); ) {
          ai=(RefondeAngle)e.nextElement();
          pl=(RefondePolyligne)ai2pl_.get(ai);
          file.print(hpls.get(pl)+" ; ");
          file.print(ai.getSDebut()+" ; ");
          file.print(ai.getSFin()+" ; ");

          // Absolu
          if (ai.getType()==RefondeAngle.ABSOLU) {
            file.print("absolu ; ");
            file.print(ai.getAngle());
          }

          // Relatif
          else if (ai.getType()==RefondeAngle.RELATIF) {
            file.print("relatif ; ");
            file.print(ai.getAngle());
          }

          // Diffracté
          else if (ai.getType()==RefondeAngle.DIFFRACTE) {
            file.print("diffracte ; ");
            file.print(ai.getPointDiffraction().x_+" ; ");
            file.print(ai.getPointDiffraction().y_);
          }
          file.println();
        }
        file.println();

        // Nombre d'angles de transmission sur les digues
        file.println("<angles_transmission> ; "+at2pl_.size());

        // Angles de transmission sur les digues
        for (Enumeration e=at2pl_.keys(); e.hasMoreElements(); ) {
          ai=(RefondeAngle)e.nextElement();
          pl=(RefondePolyligne)at2pl_.get(ai);
          file.print(hpls.get(pl)+" ; ");
          file.print(ai.getSDebut()+" ; ");
          file.print(ai.getSFin()+" ; ");

          // Absolu
          if (ai.getType()==RefondeAngle.ABSOLU) {
            file.print("absolu ; ");
            file.print(ai.getAngle());
          }

          // Relatif
          else if (ai.getType()==RefondeAngle.RELATIF) {
            file.print("relatif ; ");
            file.print(ai.getAngle());
          }

          // Diffracté
          else if (ai.getType()==RefondeAngle.DIFFRACTE) {
            file.print("diffracte ; ");
            file.print(ai.getPointDiffraction().x_+" ; ");
            file.print(ai.getPointDiffraction().y_);
          }
          file.println();
        }
        file.println();
      }
    }
    catch (IOException _exc) {
      throw new IOException("Erreur d'écriture sur " + _fcCalcul);
    }
    //    catch (Exception _exc) {
    //      throw new IOException(_exc.getMessage());
    //    }
    finally {
      if (file!=null) file.close();
    }
  }
  /**
   * Création des angles (incidence/transmission) sur un domaine digue de
   * type transmissible uniquement
   */
  public void creerAngles(RefondeDomaineDigue _doma) {
    if (_doma.getGroupeProprietes().getType()
      != RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
      return;
    RefondePolyligne[] pls= _doma.getContour().getPolylignes();
    RefondeAngle ai;
    for (int i= 0; i < pls.length; i++) {
      if (pls[i] != _doma.getExtremiteDigue()
        && pls[i] != _doma.getExtremiteFinDigue()) {
        ai= new RefondeAngle();
        ai.setRelatif(0, 0.9999999, 0); // Sinon, pb d'affichage 1. -> 0.
        ai2pl_.put(ai, pls[i]);
        ai= new RefondeAngle();
        ai.setRelatif(0, 0.9999999, 0); // Sinon, pb d'affichage 1. -> 0.
        at2pl_.put(ai, pls[i]);
      }
    }
  }
  /**
   * Suppression des angles (incidence/transmission) sur un domaine digue de
   * type transmissible uniquement
   */
  public void supprimerAngles(RefondeDomaineDigue _doma) {
    //    if (_doma.getGroupeProprietes().getType()!=
    //        RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE) return;
    Hashtable pl2ai= new Hashtable(ai2pl_.size());
    for (Enumeration e= ai2pl_.keys(); e.hasMoreElements();) {
      Object key= e.nextElement();
      pl2ai.put(ai2pl_.get(key), key);
    }
    Hashtable pl2at= new Hashtable(at2pl_.size());
    for (Enumeration e= at2pl_.keys(); e.hasMoreElements();) {
      Object key= e.nextElement();
      pl2at.put(at2pl_.get(key), key);
    }
    RefondePolyligne[] pls= _doma.getContour().getPolylignes();
    for (int i= 0; i < pls.length; i++) {
      Object key;
      if ((key= pl2ai.get(pls[i])) != null)
        ai2pl_.remove(key);
      if ((key= pl2at.get(pls[i])) != null)
        at2pl_.remove(key);
    }
  }
  /**
   * Retourne tous les angles d'incidence dans un ordre quelconque
   */
  public RefondeAngle[] getAnglesIncidenceDigues() {
    RefondeAngle[] ais;
    ais= new RefondeAngle[ai2pl_.size()];
    ai2pl_.keySet().toArray(ais);
    return ais;
  }
  /**
   * Retourne la polyligne associée à l'angle d'incidence
   */
  public RefondePolyligne getPolyligneAI(RefondeAngle _ai) {
    return (RefondePolyligne)ai2pl_.get(_ai);
  }
  /**
   * Retourne tous les angles de transmission dans un ordre quelconque
   */
  public RefondeAngle[] getAnglesTransmissionDigues() {
    RefondeAngle[] ats;
    ats= new RefondeAngle[at2pl_.size()];
    at2pl_.keySet().toArray(ats);
    return ats;
  }
  /**
   * Retourne la polyligne associée à l'angle de transmission
   */
  public RefondePolyligne getPolyligneAT(RefondeAngle _at) {
    return (RefondePolyligne)at2pl_.get(_at);
  }
  /**
   * Calcul des angles d'incidence depuis le projet
   * @param _projet Le projet pour lequel on calcul les angles d'incidence
   */
  public void calculAngles(RefondeProjet _projet) {
    setModifie();
    //    RefondeModeleProprietes mdlPrp=_projet.getModeleProprietes();
    RefondeAngle ai;
    //    RefondeContour[] cts=_projet.getGeometrie().cntrsPeau();
    Vector cntrs= _projet.getGeometrie().frontieres();
    Vector cext= (Vector)cntrs.get(0);
    Vector cntr;
    GrPoint pt01= null;
    GrPoint pt02= null;
    GrPoint ptD0;
    GrPoint pt;
    RefondePolyligne pl= null;
    GrPolyligne plFerm;
    GrPolyligne plTot;
    int tpGPrp;
    int i;
    int j;
    int n;
    double sDeb;
    double sFin;
    boolean ouvert= true;
    // Création de la polyligne 2D totale à partir des polylignes de bord
    n= cext.size();
    plTot= new GrPolyligne();
    for (i= 0; i < n; i++) {
      pl= (RefondePolyligne)cext.get(i);
      pt= pl.sommet(0);
      plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
    }
    pt= pl.sommet(1);
    plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
    // Recherche du bord ouvert en entrée
    i= 0;
    j= 0;
    while (i < n + 1) {
      pl= (RefondePolyligne)cext.get(j % n);
      //      tpGPrp=mdlPrp.getGroupeProprietes(pl).getType();
      tpGPrp=
        _projet.getGeometrie().polyligne(pl).getGroupeProprietes().getType();
      if (tpGPrp == RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE) {
        if (!ouvert) {
          pt02= plTot.sommet(j % n);
          ouvert= true;
          i= 0;
        }
      } else {
        ouvert= false;
        if (pt02 != null) {
          pt01= plTot.sommet(j % n);
          break;
        }
      }
      i++;
      j++;
    }
    try {
      //--- Le bord extérieur est totalement fermé -----------------------------
      if (pt01 == null && !ouvert) {
        angles_= new Vector[cntrs.size()];
        angles_[0]= new Vector();
        // Initialisation des angles d'incidence sur la frontière fermée
        ai= new RefondeAngle();
        // B.M. 27/04/2001        ai.setRelatif(0,0,0);
        ai.setRelatif(0, 1, 0);
        angles_[0].add(ai);
      }
      //--- Le bord extérieur est totalement ouvert ----------------------------
      // => On traite de manière globale
      else if (pt01 == null && ouvert) {
        angles_= new Vector[cntrs.size()];
        angles_[0]= new Vector();
        // Initialisation des angles d'incidence sur la frontière ouverte
        ai= new RefondeAngle();
        // B.M. 27/04/2001        ai.setAbsolu(0,0,angleHoule_);
        ai.setAbsolu(0, 1, angleHoule_);
        angles_[0].add(ai);
      }
      //--- Le bord extérieur possède une frontière ouverte, une autre fermée --
      else {
        // Point de diffraction initial
        ptD0= calculPtDiffrInitial(_projet);
        // Prise en compte du cas ou au moins 1 des 2 points de la frontière
        // ouverte n'est pas éclairé directement :
        // On tente de savoir par quel coté la houle attaque les extremités de
        // la frontière. Pour cela, on considère que le premier point "attaqué"
        // est le point le + proche du point de diffraction initial (ptD0).
        // On définit alors la polyligne 2D globale de la frontière fermée
        // orientée dans le sens ptD0->ptExtremiteFrontierePlusProche. Cette
        // polyligne inclut également le premier point de diffraction.
        plFerm= new GrPolyligne();
        plFerm.sommets_.ajoute(ptD0);
        boolean sensTrigo;
        // Point + proche : pt01
        if (ptD0.distanceXY(pt02) > ptD0.distanceXY(pt01)) {
          plFerm.sommets_.ajoute(pt01);
          i= 0;
          while (plTot.sommet(i % n) != pt01)
            i++;
          i++; // Pour les frontières totalement fermées
          while ((pt= plTot.sommet(i % n)) != pt02) {
            plFerm.sommets_.ajoute(pt);
            i++;
          }
          plFerm.sommets_.ajoute(pt02);
          sensTrigo= true;
        }
        // Point + proche : pt02
        else {
          plFerm.sommets_.ajoute(pt02);
          i= 10 * n; // Pour être sûr de ne pas se retrouver avec un i négatif.
          while (plTot.sommet(i % n) != pt02)
            i--;
          i--; // Pour les frontières totalement fermées
          while ((pt= plTot.sommet(i % n)) != pt01) {
            plFerm.sommets_.ajoute(pt);
            i--;
          }
          plFerm.sommets_.ajoute(pt01);
          sensTrigo= false;
        }
        // Calcul récursif des angles d'incidence.
        angles_= new Vector[cntrs.size()];
        angles_[0]= new Vector();
        calcul(angles_[0], plFerm, plTot, 0, 1, plFerm.nombre() - 1, sensTrigo);
        // Remplacement sur les segments dont les angles d'incidence sont diffractés
        // depuis le point ptD par des angles d'incidence absolus.
        for (i= 0; i < angles_[0].size(); i++) {
          ai= (RefondeAngle)angles_[0].get(i);
          if (ai.getPointDiffraction() == ptD0) {
            ai.setAbsolu(ai.getSDebut(), ai.getSFin(), angleHoule_);
          }
        }
        // Initialisation des angles d'incidence sur la frontière ouverte
        sDeb= plTot.abscisseDe(plTot.sommets_.indice(pt02));
        sFin= plTot.abscisseDe(plTot.sommets_.indice(pt01));
        ai= new RefondeAngle();
        ai.setAbsolu(sDeb, sFin, angleHoule_);
        angles_[0].add(ai);
        // Réordonnancement inverse des angles si contour fermé orienté inverse trigo.
        if (!sensTrigo) {
          Vector vtmp= new Vector();
          for (i= angles_[0].size() - 1; i >= 0; i--) {
            vtmp.add(angles_[0].get(i));
          }
          angles_[0]= vtmp;
        }
      }
    } catch (IllegalArgumentException _exc) {
      angles_[0]= new Vector();
      // Initialisation des angles d'incidence comme une frontière fermée
      ai= new RefondeAngle();
      ai.setRelatif(0, 0, 0);
      angles_[0].add(ai);
      throw _exc;
    }
    //    catch (Exception _exc) {
    //       _exc.printStackTrace();
    //    }
    finally {
      // Initialisation des angles d'incidence sur les contours intérieurs
      for (i= 1; i < cntrs.size(); i++) {
        angles_[i]= new Vector();
        cntr= (Vector)cntrs.get(i);
        n= cntr.size();
        plTot= new GrPolyligne();
        for (j= 0; j < n; j++) {
          pl= (RefondePolyligne)cntr.get(j);
          pt= pl.sommet(0);
          plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
        }
        pt= pl.sommet(1);
        plTot.sommets_.ajoute(new GrPoint(pt.x_, pt.y_, 0.));
        for (j= 0; j < n; j++) {
          sDeb= plTot.abscisseDe(j);
          sFin= plTot.abscisseDe(j + 1);
          ai= new RefondeAngle();
          ai.setRelatif(sDeb, sFin, 0.);
          angles_[i].add(ai);
        }
      }
      // B.M. 30/04/2001 L'algorithme serait sans doute à revoir de manière à
      // ce que le vecteur des angles retournés commence toujours par l'angle
      // d'abscisse initial=0 et finisse par l'angle d'abscisse final=1. Pour
      // ne pas avoir à retoucher l'algo, on modifie les angles après coup.
      corrigeAngles();
      for (i= 0; i < angles_[0].size(); i++) {
        ai= (RefondeAngle)angles_[0].get(i);
        System.out.println(
          "Angle "
            + i
            + " Type : "
            + (ai.getType() == RefondeAngle.ABSOLU
              ? "Absolu"
              : (ai.getType() == RefondeAngle.DIFFRACTE
                ? "Diffracté"
                : "Relatif"))
            + " Deb : "
            + ai.getSDebut()
            + " Fin : "
            + ai.getSFin());
      }
    }
    return;
  }
  /**
   * Correction des angles des contours de manière que le premier angle du
   * contour ait toujours pour abscisse initial 0 et le dernier pour abscisse
   * final 1.
   */
  private void corrigeAngles() {
    Vector angles;
    RefondeAngle ai1;
    RefondeAngle ai2;
    // Recherche de l'angle qui contient le 0.
    for (int i= 0; i < angles_.length; i++) {
      int decal= 0;
      for (int j= 0; j < angles_[i].size(); j++) {
        ai1= (RefondeAngle)angles_[i].get(j);
        // Cas particulier d'un angle unique sur le contour.
        if (angles_[i].size() == 1 && ai1.getSFin() == 0) {
          decal= j;
          ai1.setSFin(1);
          break;
        } else if (ai1.getSDebut() > ai1.getSFin()) {
          decal= j + 1;
          // Pas de scindement, on ne modifie que l'abscisse final de l'angle.
          if (ai1.getSFin() == 0) {
            ai1.setSFin(1);
            break;
          }
          ai2= new RefondeAngle(ai1);
          ai1.setSFin(1);
          ai2.setSDebut(0);
          angles_[i].add(j + 1, ai2);
          break;
        }
      }
      if (decal == 0)
        continue;
      // Ordonnancement à partir de l'angle d'abscisse initial=0
      angles= new Vector();
      for (int j= 0; j < angles_[i].size(); j++)
        angles.add(angles_[i].get((decal + j) % angles_[i].size()));
      angles_[i]= angles;
    }
  }
  //----------------------------------------------------------------------------
  // Calcul du premier point de diffraction pour algorithme récursif
  // Le 1er point se trouve a une distance de 100xtaille du domaine
  // pour obtenir un angle de houle de depart quasi-constant
  //----------------------------------------------------------------------------
  private GrPoint calculPtDiffrInitial(RefondeProjet _projet) {
    GrPoint r= null;
    GrBoite bt;
    Vector cext;
    double taille;
    // Boite du contour exterieur
    bt= new GrBoite();
    cext= (Vector)_projet.getGeometrie().frontieres().get(0);
    for (int i= 0; i < cext.size(); i++)
      bt.ajuste(((GrPolyligne)cext.get(i)).boite());
    taille= Math.max(bt.e_.x_ - bt.o_.x_, bt.e_.y_ - bt.o_.y_);
    r= bt.barycentre();
    r.x_ -= Math.cos(angleHoule_ * Math.PI / 180.) * taille * 100.;
    r.y_ -= Math.sin(angleHoule_ * Math.PI / 180.) * taille * 100.;
    r.z_= 0.;
    return r;
  }
  //----------------------------------------------------------------------------
  // Recherche d'un point de diffraction pour le segment
  // @param _cext  Segment fermé du contour exterieur
  // @param _ctot  Polyligne totale du contour
  // @param _ais   Vecteur des RefondeAngle
  // @param _ipD   Indice sur le contour du point de diffraction
  // @param _ip1   Indice sur le contour du point de debut du segment à traiter
  // @param _ip2   Indice sur le contour du point de fin   du segment à traiter
  // @param _trigo Le contour tourne dans le sens trigo si true, horaire sinon.
  // @return Le nombre de points rajoutés sur la polyligne. Utile pour le
  //         décalage pour les calculs suivants
  //----------------------------------------------------------------------------
  private void calcul(
    Vector _ais,
    GrPolyligne _cext,
    GrPolyligne _ctot,
    int _ipD0,
    int _ip01,
    int _ip02,
    boolean _trigo) {
    double thetaCur;
    double theta;
    double theta0;
    GrPoint pC;
    GrPoint pD0;
    GrPoint pI;
    GrPoint pIC;
    GrPoint p02;
    GrPoint p12;
    RefondeDroite d;
    int ipD1;
    int ip11;
    int ip12;
    int ipC;
    int ipI;
    int ip0C;
    pD0= _cext.sommets_.renvoie(_ipD0);
    //    GrPoint p1=_cext.sommets.renvoie(_ip01);
    //    GrPoint p2=_cext.sommets.renvoie(_ip02);
    ipC= _ip01;
    theta0= 10; // Valeur hors limites calculées possibles pour indiquer que le
    // theta0 n'a pas encore été calculé.
    theta= 0; // Pas nécessaire, mais la compilation ne passe pas sinon
    while (ipC <= _ip02) {
      // On passe le point s'il est le point de diffraction du segment
      if (ipC == _ipD0) {
        ipC++;
        continue;
      }
      pC= _cext.sommets_.renvoie(ipC);
      // Theta 0 calculé
      if (theta0 == 10) {
        theta0= Math.atan2(pC.y_ - pD0.y_, pC.x_ - pD0.x_);
        theta= 0;
        ipC++;
        continue;
      }
      thetaCur=
        (Math.atan2(pC.y_ - pD0.y_, pC.x_ - pD0.x_) - theta0 + 3 * Math.PI)
          % (2 * Math.PI)
          - Math.PI;
      if ((_trigo && thetaCur >= theta) || (!_trigo && thetaCur <= theta)) {
        theta= thetaCur;
        ipC++;
      }
      // Le theta diminue : on se trouve en présence d'un point de diffraction
      // potentiel
      else {
        ipD1= ipC - 1;
        ip0C= ipD1 - 1;
        pI= null; // Pas de point d'intersection défini
        ipI= 0;
        // Recherche d'un point de diffraction cachant ipD1
        for (int i= ipC; i <= _ip02; i++) {
          if (i == _ipD0)
            continue;
          pC= _cext.sommets_.renvoie(i);
          thetaCur=
            (Math.atan2(pC.y_ - pD0.y_, pC.x_ - pD0.x_) - theta0 + 3 * Math.PI)
              % (2 * Math.PI)
              - Math.PI;
          if ((_trigo && thetaCur < theta) || (!_trigo && thetaCur > theta)) {
            // Calcul de l'intersection entre la droite (_ipD0,ipC) et le segment (_ip01,ip0C)
            d= new RefondeDroite(pD0, pC);
            for (int j= _ip01; j <= ip0C; j++) {
              pIC= d.intersectionSegmentXY(_cext.segment(j));
              // Intersection trouvée. Si dist (pD0,pC) < dist (pD0,pIC), alors
              // le point i est bien un nouveau point de diffraction
              if (pIC != null) {
                if (pD0.distance(pC) < pD0.distance(pIC)) {
                  theta= thetaCur;
                  ipD1= i;
                  ip0C= j;
                  pI= pIC;
                  ipI= j + 1;
                  break;
                }
              }
            }
          }
        }
        // On n'a pas trouvé de point de diffraction cachant ipD1 (pas de point
        // d'intersection). La zone d'ombre se situe après le point de
        // diffraction ipD1. Recherche de l'intersection
        if (pI == null) {
          // Recherche du point intersectant le plus proche de pD1 dont la norme
          // (pD0,pI) est supérieure à (pD0,pD1).
          d= new RefondeDroite(pD0, _cext.sommets_.renvoie(ipD1));
          for (int i= ipD1 + 1; i < _ip02; i++) {
            pIC= d.intersectionSegmentXY(_cext.segment(i));
            // Intersection trouvée. Si dist (pD0,pIC) < dist (pD0,pI), alors
            // le point i est bien un nouveau point de diffraction
            if (pIC != null) {
              //              if ((pI==null || pD0.distance(pIC)<pD0.distance(pI))) {
              if ((pI == null || pD0.distance(pIC) < pD0.distance(pI))
                && pD0.distance(pIC)
                  > pD0.distance(_cext.sommets_.renvoie(ipD1))) {
                pI= pIC;
                ipI= i + 1;
              }
            }
          }
          // B.M. 23/05/2002 Ce cas peut se produire lorsqu'un point extrémité de
          // frontière ouverte n'est pas éclairé directement => Pas de point
          // d'intersection. On est en présence d'une zone d'ombre après le point de
          // diffraction ipD1. On prend comme point d'intersection le dernier point du
          // segment.
          //          if (pI==null) throw new IllegalArgumentException(
          //           "L'angle d'incidence choisi ne permet pas de faire un calcul "+
          //           "d'angles automatique.\nInitialisation par défaut.");
          if (pI == null) {
            ipI= _ip02;
          }
          // Insertion du point d'intersection sur le contour extérieur et le
          //          // contour total
          else {
            //            _ctot.sommets.insere(pI,_ctot.sommets.indice(_cext.sommet(ipI)));
            _cext.sommets_.insere(pI, ipI);
            _ip02++;
            if (_ipD0 >= ipI)
              _ipD0++;
          }
          ip11= ipD1;
          ip12= ipI;
        }
        // La zone d'ombre se situe avant le point de diffraction
        else {
          // Insertion du point d'intersection sur le contour extérieur et le
          //          // contour total
          //          _ctot.sommets.insere(pI,_ctot.sommets.indice(_cext.sommet(ipI)));
          _cext.sommets_.insere(pI, ipI);
          _ip02++;
          if (_ipD0 >= ipI)
            _ipD0++;
          ipD1++;
          ip11= ipI;
          ip12= ipD1;
        }
        // Création de l'angle d'incidence pour le segment défini si le segment
        // n'est pas de longueur nulle
        if (_ip01 < ip11)
          _ais.add(creeAngle(_cext, _ctot, _ipD0, _ip01, ip11, _trigo));
        // Calcul pour le segment déterminé ipD1,ip11,ip12
        p02= _cext.sommets_.renvoie(_ip02);
        p12= _cext.sommets_.renvoie(ip12);
        pD0= _cext.sommets_.renvoie(_ipD0);
        calcul(_ais, _cext, _ctot, ipD1, ip11, ip12, _trigo);
        // Passage a la portion suivante du segment
        _ip02= _cext.sommets_.indice(p02);
        _ip01= _cext.sommets_.indice(p12);
        _ipD0= _cext.sommets_.indice(pD0);
        ipC= _ip01;
        theta0= 10;
      }
    }
    if (_ip01 < _ip02)
      _ais.add(creeAngle(_cext, _ctot, _ipD0, _ip01, _ip02, _trigo));
    return;
  }
  //----------------------------------------------------------------------------
  // Création d'un angle diffracté
  // @param _cext Polyligne contour exterieur
  // @param _ctot Polyligne totale du contour
  // @param _ip01 Indice sur _cext du point de debut du segment
  // @param _ip02 Indice sur _cext du point de fin   du segment
  // @param _ipD0 Indice sur _cext du point de diffraction
  // @param _trigo Le contour tourne dans le sens trigo si true, horaire sinon.
  // @return Angle d'incidence diffracté
  //----------------------------------------------------------------------------
  private RefondeAngle creeAngle(
    GrPolyligne _cext,
    GrPolyligne _ctot,
    int _ipD0,
    int _ip01,
    int _ip02,
    boolean _trigo) {
    RefondeAngle r= new RefondeAngle();
    GrPoint pt01;
    GrPoint pt02;
    double sDeb;
    double sFin;
    pt01= _cext.sommet(_ip01);
    pt02= _cext.sommet(_ip02);
    if (_trigo) {
      sDeb= _ctot.abscisseDe(pt01);
      sFin= _ctot.abscisseDe(pt02);
    } else {
      sDeb= _ctot.abscisseDe(pt02);
      sFin= _ctot.abscisseDe(pt01);
    }
    //    sDeb=_ctot.abscisseDe(_ctot.sommets.indice(pt01));
    //    sFin=_ctot.abscisseDe(_ctot.sommets.indice(pt02));
    r.setDiffracte(sDeb, sFin, _cext.sommet(_ipD0));
    return r;
  }
}
