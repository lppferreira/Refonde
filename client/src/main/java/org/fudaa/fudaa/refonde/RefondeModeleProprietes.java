/*
 * @file         RefondeModeleProprietes.java
 * @creation     1999-08-12
 * @modification $Date: 2006-09-08 16:04:28 $
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
import java.util.Vector;

import org.fudaa.ebli.geometrie.GrPoint;
/**
 * Classe pour la gestion du modèle de propriété du projet.
 *
 * @version      $Id: RefondeModeleProprietes.java,v 1.11 2006-09-08 16:04:28 opasteur Exp $
 * @author       Bertrand Marchand
 */
public class RefondeModeleProprietes {
  // Pour la clareté du code
  //  private static final int EOF   =StreamTokenizer.TT_EOF;
  //  private static final int EOL   =StreamTokenizer.TT_EOL;
  //  private static final int NUMBER=StreamTokenizer.TT_NUMBER;
  private static final int WORD= StreamTokenizer.TT_WORD;
  //  private Hashtable bord2Gp=new Hashtable();
  boolean modifie;

  /** Domaines poreux */
  private Vector poreux_= new Vector();

  /**
   * Création d'un modele de proprietés par défaut depuis le projet
   * @param _tpModele Le type du modele de données
   *                    (RefondeModeleCalcul.MODELE_SEICHE,
   *                     RefondeModeleCalcul.MODELE_HOULE)
   */
  public static RefondeModeleProprietes defaut(RefondeProjet _projet, int _tpModele) {
    RefondeModeleProprietes mdlPrp= new RefondeModeleProprietes();
    RefondeGroupeProprietes gp;
    RefondePolyligne pl;

    Vector vpls= _projet.getGeometrie().scene_.getPolylignes();
    for (int i= 0; i < vpls.size(); i++) {
      pl= (RefondePolyligne)vpls.get(i);

      // Pas de frontière associée => Pas de groupe de propriétés
      if (_projet.getGeometrie().frontiere(pl) == null)
        pl.setGroupeProprietes(null);

      else {
        if (pl.getGroupeProprietes() == null) {

          // Construction des propriétés par défaut
          if (_tpModele==RefondeModeleCalcul.MODELE_HOULE)
            gp=new RefondeGroupeProprietes(
                RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT);
          else
            gp=new RefondeGroupeProprietes(
                RefondeGroupeProprietes.SEICHE_BORD_FERME);

          pl.setGroupeProprietes(gp);
        }
      }
    }
    mdlPrp.modifie= true;
    return mdlPrp;
  }

  /**
   * Retourne le groupe de proprietes associé au bord
   * @return Groupe de propriété associé au bord.
   * @deprecated
   */
  public RefondeGroupeProprietes getGroupeProprietes(RefondeSupporteGroupeProprietes _bord) {
    RefondeGroupeProprietes r= null;
    //    if ((r=(RefondeGroupeProprietes)bord2Gp.get(_bord))==null)
    if ((r= _bord.getGroupeProprietes()) == null)
      new IllegalArgumentException("Le bord n'a pas de groupe de propriétés")
        .printStackTrace();
    return r;
  }
  /**
   * Affecte le groupe de propriétés au bord
   * @deprecated Gardé pour compatibilité
   */
  public void setGroupeProprietes(
    RefondeSupporteGroupeProprietes _bord,
    RefondeGroupeProprietes _groupe) {
    //    bord2Gp.put(_bord,_groupe);
    _bord.setGroupeProprietes(_groupe);
    modifie= true;
  }

  /**
   * Ajoute d'un domaine poreux.
   */
  public void addDomainePoreux(RefondeDomainePoreux _dm) {
    poreux_.add(_dm);
    modifie= true;
  }

  /**
   * Suppression d'un domaine poreux.
   */
  public void removeDomainePoreux(RefondeDomainePoreux _dm) {
    poreux_.remove(_dm);
    modifie= true;
  }

  /**
   * Suppression de tous les domaines poreux
   */
  public void removeAllDomainesPoreux() {
    poreux_.clear();
    modifie=true;
  }

  /**
   * Retourne les domaines poreux.
   */
  public RefondeDomainePoreux[] getDomainesPoreux() {
    return (RefondeDomainePoreux[])poreux_.toArray(new RefondeDomainePoreux[0]);
  }

  /**
   * Lecture d'un modele de propriétés.
   * @param _fc Nom du fichier modele de propriétés.
   * @param _projet  Projet contenant la géométrie sur laquelle on affecte les
   *                 propriétés de bord.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public static RefondeModeleProprietes ouvrir(RefondeProjet _projet, File _fc)
    throws IOException {
    RefondeModeleProprietes prp= new RefondeModeleProprietes();
    StreamTokenizer file= null;
    Reader rf= null;
    //    double val=0;
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
      if (file.nextToken() != WORD || !file.sval.equals("modele_proprietes"))
        throw new RefondeIOException("Le fichier n'est pas un fichier modèle de propriétés");
      // Lecture par version
      if (version.compareTo("5.06") <= 0)
        prp.lire506(_projet, file);
      else if (version.compareTo("5.14") <= 0)
        prp.lire514(_projet, file);
      // Ajout des domaines poreux
      else
        prp.lire(_projet, file);
    } catch (NumberFormatException _exc) {
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
    prp.modifie= false;
    return prp;
  }
  /**
   * Enregistrement d'un modele de propriétés.
   * @param _projet  Projet contenant la géométrie sur laquelle sont affectées
   *                 les propriétés de bord.
   * @param _fichier Nom du fichier modele de propriétés.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public void enregistrer(RefondeProjet _projet, File _fichier)
    throws IOException {
    ecrire(_projet, _fichier);
    modifie= false;
  }
  /**
   * Lecture des informations sur version 506 et moins
   */
  private void lire506(RefondeProjet _projet, StreamTokenizer _file)
    throws IOException {
    RefondePropriete[] prps;
    RefondeGroupeProprietes gPrp;
    int tpPrp;
    int tpGprp;
    double valPrp;
    RefondePolyligne[] bords;
    int nbBords;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector cntrs= geo.frontieres();
    Vector cntr;
    Vector vdms= geo.getDomaines();
    // Bords
    nbBords= 0;
    for (int i= 0; i < cntrs.size(); i++)
      nbBords += ((Vector)cntrs.get(i)).size();
    bords= new RefondePolyligne[nbBords];
    nbBords= 0;
    for (int i= 0; i < cntrs.size(); i++) {
      cntr= (Vector)cntrs.get(i);
      for (int j= 0; j < cntr.size(); j++)
        bords[nbBords++]= (RefondePolyligne)cntr.get(j);
    }
    // Groupes de propriétés
    nbBords= 0;
    //    while (_file.nextToken()!=WORD || !_file.sval.equals("<fin>")) {
    while (_file.nextToken() == WORD && !_file.sval.equals("<fin>")) {
      if (nbBords == bords.length)
        throw new IOException();
      // Type de groupe
      if (_file.ttype != WORD)
        throw new IOException();
      if (_file.sval.equals("ouvert_entree"))
        tpGprp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE;
      else if (_file.sval.equals("ouvert_sortie"))
        tpGprp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE;
      else if (_file.sval.equals("semi_reflechissant"))
        tpGprp= RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT;
      else
        throw new IOException();
      // Pour chaque propriété
      prps=
        new RefondePropriete[RefondeGroupeProprietes
          .typesProprietes[tpGprp]
          .length];
      for (int i= 0; i < prps.length; i++) {
        // Type de propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("reflexion"))
          tpPrp= RefondePropriete.REFLEXION;
        else
          throw new IOException();
        if (tpPrp != RefondeGroupeProprietes.typesProprietes[tpGprp][i])
          throw new IOException();
        // Comportement (stationnaire uniquement autorisé)
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (!_file.sval.equals("stationnaire"))
          throw new IOException();
        // Valeur de la propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        valPrp= Double.parseDouble(_file.sval);
        prps[i]= new RefondePropriete(tpPrp, valPrp);
      }
      if (_file.nextToken() != WORD || !_file.sval.equals("<fin>"))
        throw new IOException();
      gPrp= new RefondeGroupeProprietes(tpGprp, prps);
      //      bord2Gp.put(bords[nbBords++],gPrp);
      geo.polyligne(bords[nbBords++]).setGroupeProprietes(gPrp);
    }
    // controle que le nombre de groupes lus est = au nombre de bords
    if (nbBords != bords.length)
      throw new RefondeIOException("Le fichier n'est pas cohérent avec la géométrie du projet");
    // Création de groupes de propriétés de fond pour les domaines digues
    for (int i= 0; i < vdms.size(); i++) {
      if (vdms.get(i) instanceof RefondeDomaineDigue) {
        RefondeGroupeProprietes gp=
          new RefondeGroupeProprietes(
            RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE);
        ((RefondeDomaine)vdms.get(i)).setGroupeProprietes(gp);
      }
    }
  }
  /**
   * Lecture des informations sur version 514 et moins
   */
  private void lire514(RefondeProjet _projet, StreamTokenizer _file)
    throws IOException {
    RefondePropriete[] prs;
    RefondeGroupeProprietes gp;
    RefondePolyligne pl;
    RefondeDomaineDigue dm;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vdms= geo.getDomaines();
    Vector vpls= geo.getPolylignes();
    int tpPr;
    int tpGp;
    double valPr;
    int nbGp;
    int ival;
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    // Groupes de propriétés de bord
    while (_file.nextToken() == WORD && !_file.sval.equals("<fin>")) {
      // Numéro de polyligne
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size() - 1)
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Type de groupe
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("ouvert_entree"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE;
      else if (_file.sval.equals("ouvert_sortie"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE;
      else if (_file.sval.equals("semi_reflechissant"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT;
      else
        throw new IOException();
      // Pour chaque propriété
      prs=
        new RefondePropriete[RefondeGroupeProprietes
          .typesProprietes[tpGp]
          .length];
      for (int i= 0; i < prs.length; i++) {
        // Type de propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("reflexion"))
          tpPr= RefondePropriete.REFLEXION;
        else
          throw new IOException();
        if (tpPr != RefondeGroupeProprietes.typesProprietes[tpGp][i])
          throw new IOException();
        // Comportement (stationnaire uniquement autorisé)
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (!_file.sval.equals("stationnaire"))
          throw new IOException();
        // Valeur de la propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        valPr= Double.parseDouble(_file.sval);
        prs[i]= new RefondePropriete(tpPr, valPr);
      }
      if (_file.nextToken() != WORD || !_file.sval.equals("<fin>"))
        throw new IOException();
      gp= new RefondeGroupeProprietes(tpGp, prs);
      pl.setGroupeProprietes(gp);
    }
    // Groupes de propriétés de fond
    // Nombre de groupes de propriétés de fond
    if (_file.nextToken() != WORD || !_file.sval.equals("<proprietes_fond>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbGp= Integer.parseInt(_file.sval);
    // Groupes de propriétés
    for (int i= 0; i < nbGp; i++) {
      // Numéro de domaine
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size() - 1)
        throw new IOException();
      if (!(vdms.get(ival) instanceof RefondeDomaineDigue))
        throw new IOException();
      dm= (RefondeDomaineDigue)vdms.get(ival);
      // Type du groupe de propriété
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("digue_transmissible"))
        tpGp= RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE;
      else if (_file.sval.equals("digue_perforee"))
        tpGp= RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE;
      else
        throw new IOException();
      // Pour chaque propriété
      prs=
        new RefondePropriete[RefondeGroupeProprietes
          .typesProprietes[tpGp]
          .length];
      for (int j= 0; j < prs.length; j++) {
        // Type de propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("perte_charge_i"))
          tpPr= RefondePropriete.PERTE_CHARGE_I;
        else if (_file.sval.equals("perte_charge_r"))
          tpPr= RefondePropriete.PERTE_CHARGE_R;
        else if (_file.sval.equals("porosite"))
          tpPr= RefondePropriete.POROSITE;
        else if (_file.sval.equals("contraction"))
          tpPr= RefondePropriete.CONTRACTION;
        else if (_file.sval.equals("intg_verticale"))
          tpPr= RefondePropriete.INTG_VERTICALE;
        else if (_file.sval.equals("reflexion"))
          tpPr= RefondePropriete.REFLEXION;
        else if (_file.sval.equals("transmission"))
          tpPr= RefondePropriete.TRANSMISSION;
        else if (_file.sval.equals("dp_reflexion"))
          tpPr= RefondePropriete.DP_REFLEXION;
        else if (_file.sval.equals("dp_transmission"))
          tpPr= RefondePropriete.DP_TRANSMISSION;
        else
          throw new IOException();
        if (tpPr != RefondeGroupeProprietes.typesProprietes[tpGp][j])
          throw new IOException();
        // Comportement (stationnaire uniquement autorisé)
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (!_file.sval.equals("stationnaire"))
          throw new IOException();
        // Valeur de la propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        valPr= Double.parseDouble(_file.sval);
        prs[j]= new RefondePropriete(tpPr, valPr);
      }
      gp= new RefondeGroupeProprietes(tpGp, prs);
      dm.setGroupeProprietes(gp);
    }
  }
  /**
   * Lecture des informations sur version courante
   */
  private void lire(RefondeProjet _projet, StreamTokenizer _file)
    throws IOException {
    RefondePropriete[] prs;
    RefondeGroupeProprietes gp;
    RefondePolyligne pl;
    RefondeDomaineDigue dm;
    RefondeGeometrie geo= _projet.getGeometrie();
    Vector vdms= geo.getDomaines();
    Vector vpls= geo.getPolylignes();
    int tpPr;
    int tpGp;
    double valPr;
    int nbGp;
    int ival;
    // Identifiant de géométrie
    if (_file.nextToken() != WORD
      || !_file.sval.equals("<identifiant_geometrie>"))
      throw new IOException();
    if (_file.nextToken() != WORD
      || Integer.parseInt(_file.sval) != geo.identifiant())
      throw new RefondeIOException("Le numéro de version de la géométrie n'est pas compatible avec le projet");
    //---  Groupes de propriétés de bord  --------------------------------------
    while (_file.nextToken() == WORD && !_file.sval.equals("<fin>")) {
      // Numéro de polyligne
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size() - 1)
        throw new IOException();
      pl= (RefondePolyligne)vpls.get(ival);
      // Type de groupe
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("ouvert_entree"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE;
      else if (_file.sval.equals("ouvert_sortie"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE;
      else if (_file.sval.equals("semi_reflechissant"))
        tpGp= RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT;
      else if (_file.sval.equals("seiche_ouvert"))
        tpGp= RefondeGroupeProprietes.SEICHE_BORD_OUVERT;
      else if (_file.sval.equals("seiche_ferme"))
        tpGp= RefondeGroupeProprietes.SEICHE_BORD_FERME;
      else
        throw new IOException();
      // Pour chaque propriété
      prs=
        new RefondePropriete[RefondeGroupeProprietes
          .typesProprietes[tpGp]
          .length];
      for (int i= 0; i < prs.length; i++) {
        // Type de propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("reflexion"))
          tpPr= RefondePropriete.REFLEXION;
        else
          throw new IOException();
        if (tpPr != RefondeGroupeProprietes.typesProprietes[tpGp][i])
          throw new IOException();
        // Comportement (stationnaire uniquement autorisé)
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (!_file.sval.equals("stationnaire"))
          throw new IOException();
        // Valeur de la propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        valPr= Double.parseDouble(_file.sval);
        prs[i]= new RefondePropriete(tpPr, valPr);
      }
      if (_file.nextToken() != WORD || !_file.sval.equals("<fin>"))
        throw new IOException();
      gp= new RefondeGroupeProprietes(tpGp, prs);
      pl.setGroupeProprietes(gp);
    }
    //---  Groupes de propriétés de fond  --------------------------------------
    // Nombre de groupes de propriétés de fond
    if (_file.nextToken() != WORD || !_file.sval.equals("<proprietes_fond>"))
      throw new IOException();
    if (_file.nextToken() != WORD)
      throw new IOException();
    nbGp= Integer.parseInt(_file.sval);
    // Groupes de propriétés
    for (int i= 0; i < nbGp; i++) {
      // Numéro de domaine
      if (_file.nextToken() != WORD)
        throw new IOException();
      ival= Integer.parseInt(_file.sval);
      if (ival < 0 || ival > vpls.size() - 1)
        throw new IOException();
      if (!(vdms.get(ival) instanceof RefondeDomaineDigue))
        throw new IOException();
      dm= (RefondeDomaineDigue)vdms.get(ival);
      // Type du groupe de propriété
      if (_file.nextToken() != WORD)
        throw new IOException();
      if (_file.sval.equals("digue_transmissible"))
        tpGp= RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE;
      else if (_file.sval.equals("digue_perforee"))
        tpGp= RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE;
      else
        throw new IOException();
      // Pour chaque propriété
      prs=
        new RefondePropriete[RefondeGroupeProprietes
          .typesProprietes[tpGp]
          .length];
      for (int j= 0; j < prs.length; j++) {
        // Type de propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (_file.sval.equals("perte_charge_i"))
          tpPr= RefondePropriete.PERTE_CHARGE_I;
        else if (_file.sval.equals("perte_charge_r"))
          tpPr= RefondePropriete.PERTE_CHARGE_R;
        else if (_file.sval.equals("porosite"))
          tpPr= RefondePropriete.POROSITE;
        else if (_file.sval.equals("contraction"))
          tpPr= RefondePropriete.CONTRACTION;
        else if (_file.sval.equals("intg_verticale"))
          tpPr= RefondePropriete.INTG_VERTICALE;
        else if (_file.sval.equals("reflexion"))
          tpPr= RefondePropriete.REFLEXION;
        else if (_file.sval.equals("transmission"))
          tpPr= RefondePropriete.TRANSMISSION;
        else if (_file.sval.equals("dp_reflexion"))
          tpPr= RefondePropriete.DP_REFLEXION;
        else if (_file.sval.equals("dp_transmission"))
          tpPr= RefondePropriete.DP_TRANSMISSION;
        else
          throw new IOException();
        if (tpPr != RefondeGroupeProprietes.typesProprietes[tpGp][j])
          throw new IOException();
        // Comportement (stationnaire uniquement autorisé)
        if (_file.nextToken() != WORD)
          throw new IOException();
        if (!_file.sval.equals("stationnaire"))
          throw new IOException();
        // Valeur de la propriété
        if (_file.nextToken() != WORD)
          throw new IOException();
        valPr= Double.parseDouble(_file.sval);
        prs[j]= new RefondePropriete(tpPr, valPr);
      }
      gp= new RefondeGroupeProprietes(tpGp, prs);
      dm.setGroupeProprietes(gp);
    }
    //--- Domaines poreux  -----------------------------------------------------
    {
      // Nombre de domaines poreux
      if (_file.nextToken() != WORD || !_file.sval.equals("<domaines_poreux>"))
        throw new IOException();
      if (_file.nextToken() != WORD)
        throw new IOException();
      int nb= Integer.parseInt(_file.sval);
      // Domaines poreux
      for (int i= 0; i < nb; i++) {
        RefondeDomainePoreux dmp= new RefondeDomainePoreux();
        GrPoint[] pts= dmp.getPoints();
        GrPoint[] ptsPlan= dmp.getPointsPlan();
        // Limites xmin, xmax, ymin, ymax
        if (_file.nextToken() != WORD)
          throw new IOException();
        pts[0].x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        pts[1].x_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        pts[0].y_= Double.parseDouble(_file.sval);
        if (_file.nextToken() != WORD)
          throw new IOException();
        pts[1].y_= Double.parseDouble(_file.sval);
        // Points du plan
        for (int j= 0; j < ptsPlan.length; j++) {
          //... x
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptsPlan[j].x_= Double.parseDouble(_file.sval);
          //... y
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptsPlan[j].y_= Double.parseDouble(_file.sval);
          //... z
          if (_file.nextToken() != WORD)
            throw new IOException();
          ptsPlan[j].z_= Double.parseDouble(_file.sval);
          //... z automatique ou manuel
          if (_file.nextToken() != WORD)
            throw new IOException();
          if (_file.sval.equals("z_automatique"))
            dmp.setZAutomatique(true, j);
          else if (_file.sval.equals("z_manuel"))
            dmp.setZAutomatique(false, j);
          else
            throw new IOException();
        }
        //... Porosité
        if (_file.nextToken() != WORD)
          throw new IOException();
        dmp.porosite_= Double.parseDouble(_file.sval);
        //... Coefficient de frottement
        if (_file.nextToken() != WORD)
          throw new IOException();
        dmp.coefFrottement_= Double.parseDouble(_file.sval);
        //... Perméabilité
        if (_file.nextToken() != WORD)
          throw new IOException();
        dmp.permeabilite_= Double.parseDouble(_file.sval);
        //... Coefficient de masse virtuelle
        if (_file.nextToken() != WORD)
          throw new IOException();
        dmp.coefMasse_= Double.parseDouble(_file.sval);
        dmp.setPoints(pts);
        addDomainePoreux(dmp);
      }
    }
  }
  /**
   * Ecriture des informations sur le fichier associé
   */
  private void ecrire(RefondeProjet _projet, File _fcProprietes)
    throws IOException {
    RefondeGeometrie geo= _projet.getGeometrie();
    //    Vector            cntrs=geo.frontieres();
    //    Vector cntr;
    RefondeGroupeProprietes gp;
    RefondePropriete[] prs;
    RefondePolyligne pl;
    RefondeDomaineDigue dm;
    Vector vpls= geo.getPolylignes();
    Vector vdms= geo.getDomaines();
    PrintWriter file= null;
    try {
      // Ouverture du fichier
      file= new PrintWriter(new FileWriter(_fcProprietes));
      // Entète du fichier
      file.print("refonde ; ");
      file.print(RefondeImplementation.informationsSoftware().version + " ; ");
      file.print("modele_proprietes");
      file.println();
      file.println();
      // Identifiant de géométrie
      file.println("<identifiant_geometrie> ; " + geo.identifiant());
      file.println();
      //---  Groupes de propriétés de bord  ------------------------------------
      for (int i= 0; i < vpls.size(); i++) {
        pl= ((RefondePolyligne)vpls.get(i));
        if (geo.frontiere(pl) == null)
          continue;
        //        cntr=(Vector)cntrs.get(i);
        //        for (int j=0; j<cntr.size(); j++) {
        //          gPrp=geo.polyligne((RefondePolyligne)cntr.get(i)).getGroupeProprietes();
        //          gPrp=getGroupeProprietes((RefondePolyligne)cntr.get(j));
        // Numéro de polyligne
        file.print(i + " ; ");
        // Type de groupe
        gp= pl.getGroupeProprietes();
        if (gp.getType() == RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE)
          file.println("ouvert_entree");
        else if (gp.getType()==RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE)
          file.println("ouvert_sortie");
        else if (gp.getType()==RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT)
          file.println("semi_reflechissant");
        else if (gp.getType()==RefondeGroupeProprietes.SEICHE_BORD_OUVERT)
          file.println("seiche_ouvert");
        else if (gp.getType()==RefondeGroupeProprietes.SEICHE_BORD_FERME)
          file.println("seiche_ferme");
        else
          throw new IOException();
        // Propriétés
        prs= gp.getProprietes();
        for (int j= 0; j < prs.length; j++) {
          // Type
          if (prs[j].getType() == RefondePropriete.REFLEXION)
            file.print("reflexion ; ");
          // Comportement
          file.print("stationnaire ; ");
          // Valeur
          file.print(prs[j].getValeur());
          file.println();
        }
        file.println("<fin>");
      }
      file.println("<fin>");
      file.println();
      //---  Groupes de propriétés de fond  ------------------------------------
      // Nombre de groupes de propriétés de fond
      int nbGp= 0;
      for (int i= 0; i < vdms.size(); i++)
        if (((RefondeDomaine)vdms.get(i)).getGroupeProprietes() != null)
          nbGp++;
      file.println("<proprietes_fond> ; " + nbGp);
      // Groupes de propriétés
      for (int i= 0; i < vdms.size(); i++) {
        if (vdms.get(i) instanceof RefondeDomaineFond)
          continue;
        dm= (RefondeDomaineDigue)vdms.get(i);
        gp= dm.getGroupeProprietes();
        prs= gp.getProprietes();
        // Numéro de domaine
        file.print(i + " ; ");
        // Type du groupe de propriété
        if (gp.getType()
          == RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE)
          file.println("digue_transmissible ; ");
        else
          file.println("digue_perforee ; ");
        // Propriétés à la suite
        for (int j= 0; j < prs.length; j++) {
          // Type de la propriété
          switch (prs[j].getType()) {
            case RefondePropriete.PERTE_CHARGE_I :
              file.print("perte_charge_i ; ");
              break;
            case RefondePropriete.PERTE_CHARGE_R :
              file.print("perte_charge_r ; ");
              break;
            case RefondePropriete.POROSITE :
              file.print("porosite ; ");
              break;
            case RefondePropriete.CONTRACTION :
              file.print("contraction ; ");
              break;
            case RefondePropriete.INTG_VERTICALE :
              file.print("intg_verticale ; ");
              break;
            case RefondePropriete.REFLEXION :
              file.print("reflexion ; ");
              break;
            case RefondePropriete.TRANSMISSION :
              file.print("transmission ; ");
              break;
            case RefondePropriete.DP_REFLEXION :
              file.print("dp_reflexion ; ");
              break;
            case RefondePropriete.DP_TRANSMISSION :
              file.print("dp_transmission ; ");
              break;
            default :
              throw new IOException();
          }
          // Comportement
          file.print("stationnaire ; ");
          // Valeur
          file.println(prs[j].getValeur());
        }
      }
      file.println();
      //--- Domaines poreux  ---------------------------------------------------
      // Nombre de domaines poreux
      file.println("<domaines_poreux> ; " + poreux_.size());
      // Domaines poreux
      for (int i= 0; i < poreux_.size(); i++) {
        RefondeDomainePoreux dmp= (RefondeDomainePoreux)poreux_.get(i);
        GrPoint[] pts= dmp.getPoints();
        GrPoint[] ptsPlan= dmp.getPointsPlan();
        // Limites xmin, xmax, ymin, ymax
        file.print(pts[0].x_ + " ; ");
        file.print(pts[1].x_ + " ; ");
        file.print(pts[0].y_ + " ; ");
        file.println(pts[1].y_);
        // Points du plan
        for (int j= 0; j < ptsPlan.length; j++) {
          //... x
          file.print(ptsPlan[j].x_ + " ; ");
          //... y
          file.print(ptsPlan[j].y_ + " ; ");
          //... z
          file.print(ptsPlan[j].z_ + " ; ");
          //... z automatique ou manuel
          if (dmp.isZAutomatique(j))
            file.println("z_automatique");
          else
            file.println("z_manuel");
        }
        //... Porosité
        file.print(dmp.porosite_ + " ; ");
        //... Coefficient de frottement
        file.print(dmp.coefFrottement_ + " ; ");
        //... Perméabilité
        file.print(dmp.permeabilite_ + " ; ");
        //... Coefficient de masse virtuelle
        file.println(dmp.coefMasse_);
      }
    } catch (IOException _exc) {
      throw new IOException("Erreur d'écriture sur " + _fcProprietes);
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
   * Change la structure du modèle en fonction du modèle de données.
   * @param _typeModele Le type du modele de données
   *                    (RefondeModeleCalcul.MODELE_SEICHE,
   *                     RefondeModeleCalcul.MODELE_HOULE)
   * @param _prj Le projet.
   */
  public void transmute(int _typeModele, RefondeProjet _prj) {
    // Aucun changement si le modèle de données est le même
    if (_typeModele==_prj.getModeleCalcul().typeModele()) return;

    poreux_.clear();

    // Changement des groupes propriétés s'il y a lieu.

    Vector vpls= _prj.getGeometrie().scene_.getPolylignes();
    for (int i= 0; i < vpls.size(); i++) {
      RefondePolyligne pl= (RefondePolyligne)vpls.get(i);
      RefondeGroupeProprietes gp=pl.getGroupeProprietes();

      // Modèle de seiche
      if (_typeModele==RefondeModeleCalcul.MODELE_SEICHE) {
        if (gp==null) {}
        else if (gp.getType()==RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE) {
          gp=new RefondeGroupeProprietes(RefondeGroupeProprietes.SEICHE_BORD_OUVERT);
        }
        else if (gp.getType()==RefondeGroupeProprietes.HOULE_BORD_OUVERT_SORTIE) {
          gp=new RefondeGroupeProprietes(RefondeGroupeProprietes.SEICHE_BORD_OUVERT);
        }
        else if (gp.getType()==RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT) {
          gp=new RefondeGroupeProprietes(RefondeGroupeProprietes.SEICHE_BORD_FERME);
        }
      }

      // Modèle de houle
      else {
        if (gp==null) {}
        else if (gp.getType()==RefondeGroupeProprietes.SEICHE_BORD_OUVERT) {
          gp=new RefondeGroupeProprietes(RefondeGroupeProprietes.HOULE_BORD_OUVERT_ENTREE);
        }
        else if (gp.getType()==RefondeGroupeProprietes.SEICHE_BORD_FERME) {
          gp=new RefondeGroupeProprietes(RefondeGroupeProprietes.HOULE_BORD_SEMI_REFLECHISSANT);
        }
      }
      pl.setGroupeProprietes(gp);
    }
  }
}
