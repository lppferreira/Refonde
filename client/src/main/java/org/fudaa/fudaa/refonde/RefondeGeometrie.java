/*
 * @file         RefondeGeometrie.java
 * @creation     1999-06-28
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.fudaa.dodico.fortran.FortranReader;

import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrVecteur;
import org.fudaa.ebli.geometrie.VecteurGrPoint;

import org.fudaa.fudaa.commun.conversion.FudaaInterpolateurMaillage;
/**
 * Classe pour la gestion de la géometrie du projet.
 *
 * @version      $Id: RefondeGeometrie.java,v 1.10 2007-01-19 13:14:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeGeometrie {
  public Vector pointsTopo;
  protected boolean modifie_;
  protected int idGeo_; // Identifiant de géométrie
  protected RefondeScene scene_= new RefondeScene();
  protected Hashtable poly2Bord_;
  protected FudaaInterpolateurMaillage it_= new FudaaInterpolateurMaillage();
  protected Vector frontieres_= null;
  /**
   * Lecture d'une géometrie depuis les fichiers Vag.
   * @param _fichier Nom du fichier geometrie. La géométrie est contenue
   *                 dans les _fichier.10 et _fichier.12
   * @exception FileNotFoundException Un fichier de géométrie n'est pas trouvé
   */
  public static RefondeGeometrie ouvrirVag(File _fichier) throws IOException {
    RefondeGeometrie geo= new RefondeGeometrie();
    //    String        ext;
    String fcName;
    double x;
    double y;
    double z;
    int nbLignes;
    int indic;
    int size;
    //  int           nbctrs =0;
    Vector vpls= null;
    Vector ptsTopo= new Vector();
    GrPoint pt;
    GrPoint pt1= null;
    GrPoint pt2= null;
    RefondePolyligne pl= null;
    FortranReader file= null;
    //    ListeGrPoint  pts;
    VecteurGrPoint pts;
    Hashtable hcoor2pt= new Hashtable();
    // Points stockées (pour controle de points non confondus)
    fcName= _fichier.getPath();
    //--------------------------------------------------------------------------
    //---  Lecture du fichier des fonds (.10)  ---------------------------------
    //--------------------------------------------------------------------------
    try {
      // Ouverture du fichier
      file= new FortranReader(new FileReader(fcName + ".10"));
      // Lecture des points
      try {
        String key;
        while (true) {
          file.readFields();
          x= file.doubleField(0);
          y= file.doubleField(1);
          z= file.doubleField(2);
          pt= new GrPoint(x, y, z);
          ptsTopo.addElement(pt);
          if (hcoor2pt.get(key= x + ";" + y) != null)
            throw new RefondeIOException("Un point de topo de mêmes coordonnées existe déjà");
          hcoor2pt.put(key, pt);
        }
      } catch (EOFException _exc) {} // Fin du fichier => On ferme
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".10 ligne "
          + file.getLineNumber()
          + "\n"
          + _exc.getMessage());
    } catch (FileNotFoundException _exc) {
      throw new IOException("Erreur d'ouverture de " + fcName + ".10");
    } catch (IOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".10 ligne "
          + file.getLineNumber());
    } catch (NumberFormatException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".10 ligne "
          + file.getLineNumber());
    } finally {
      // Fermeture du fichier
      if (file != null)
        file.close();
    }
    //--------------------------------------------------------------------------
    //---  Lecture du fichier des contours (.12)  ------------------------------
    //--------------------------------------------------------------------------
    try {
      // Ouverture du fichier
      file= new FortranReader(new FileReader(fcName + ".12"));
      // Nombre de lignes
      file.readFields();
      nbLignes= file.intField(0);
      // Lecture des contours
      for (int i= 0; i < nbLignes; i++) {
        if (i != nbLignes - 1) {
          file.readFields();
          indic= file.intField(0);
          x= file.doubleField(1);
          y= file.doubleField(2);
          z= file.doubleField(3);
        }
        // Fin du fichier
        else {
          indic= 0;
          x= 0;
          y= 0;
          z= 0;
        }
        pt= new GrPoint(x, y, z);
        if (indic == 0) {
          // Indicateur de fin du contour => Controle que les premier et dernier
          // points du contour précédent ont mêmes coordonnées
          if (i > 0) {
            size= vpls.size();
            if (size < 2)
              throw new RefondeIOException("Le contour possède moins de 2 polylignes");
            pt1= ((RefondePolyligne)vpls.elementAt(0)).sommet(0);
            pt2= ((RefondePolyligne)vpls.elementAt(size - 2)).sommet(1);
            if (pt1.x_ != pt2.x_ || pt1.y_ != pt2.y_ || pt1.z_ != pt2.z_)
              throw new RefondeIOException("Le contour n'est pas fermé");
            pts= ((RefondePolyligne)vpls.elementAt(size - 2)).sommets_;
            pts.remplace(pt1, 1);
            vpls.remove(size - 1);
            // Controle que 2 points du contour ne sont pas confondus
            RefondePolyligne[] pls= new RefondePolyligne[vpls.size()];
            vpls.toArray(pls);
            for (int j= 0; j < pls.length; j++) {
              GrPoint p1= pls[j].sommet(0);
              for (int k= j + 1; k < pls.length; k++) {
                GrPoint p2= pls[k].sommet(0);
                if (new GrVecteur(p2.x_ - p1.x_, p2.y_ - p1.y_, 0.).norme() == 0)
                  throw new RefondeIOException("2 points du contour sont confondus.");
              }
            }
            // Création du contour, ajout des polylignes dans la liste
            for (int j= 0; j < pls.length; j++)
              geo.scene_.addPolyligne(pls[j]);
            geo.scene_.addContour(new RefondeContour(pls));
          }
          vpls= new Vector();
        } else
          pl.sommets_.ajoute(pt);
        pl= new RefondePolyligne();
        pl.sommets_.ajoute(pt);
        vpls.add(pl);
      }
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".12 ligne "
          + file.getLineNumber()
          + "\n"
          + _exc.getMessage());
    } catch (FileNotFoundException _exc) {
      throw new IOException("Erreur d'ouverture de " + fcName + ".12");
    } catch (IOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".12 ligne "
          + file.getLineNumber());
    } catch (NumberFormatException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + fcName
          + ".12 ligne "
          + file.getLineNumber());
    } finally {
      // Fermeture du fichier
      if (file != null)
        file.close();
    }
    //--------------------------------------------------------------------------
    //---  Traitements avant retour  -------------------------------------------
    //--------------------------------------------------------------------------
    RefondeContour[] cntrs= new RefondeContour[geo.scene_.cntrs_.size()];
    geo.scene_.cntrs_.toArray(cntrs);
    // Controle que les contours sont corrects (pas de points doublés).
    for (int i= 0; i < cntrs.length; i++) {
      RefondePolyligne[] pls= cntrs[i].getPolylignes();
      for (int j= 0; j < pls.length; j++) {
        GrPoint p1= pls[j].sommet(0);
        GrPoint p2= pls[j].sommet(1);
        if (new GrVecteur(p2.x_ - p1.x_, p2.y_ - p1.y_, 0.).norme() == 0)
          throw new IOException(
            "Erreur sur "
              + fcName
              + ".12:\n"
              + "2 points consécutifs du contour sont confondus.");
      }
    }
    // Suppression des lignes en double (mêmes points ordonnés avec mêmes
    // coordonnées)
    geo.colLignes();
    // Contour dont la boite englobante est la + grande => DomaineFond
    Vector vctsFond= new Vector();
    double lgMax= Double.NEGATIVE_INFINITY;
    int cext= 0;
    for (int i= 0; i < cntrs.length; i++) {
      GrBoite b= cntrs[i].boite();
      double lg= b.e_.x_ - b.o_.x_;
      if (lg > lgMax) {
        lgMax= lg;
        cext= i;
      }
    }
    vctsFond.add(cntrs[cext]);
    // Recherche des contours internes au contour extérieur du DomaineFond
    for (int i= 0; i < cntrs.length; i++) {
      if (i != cext && cntrs[cext].isPointInterne(cntrs[i].getPointInterne()))
        vctsFond.add(cntrs[i]);
    }
    // Création du domaine fond
    RefondeContour[] ctsFond= new RefondeContour[vctsFond.size()];
    vctsFond.toArray(ctsFond);
    RefondeDomaineFond doma= new RefondeDomaineFond(ctsFond);
    GrPoint[] ptsT= new GrPoint[ptsTopo.size()];
    ptsTopo.toArray(ptsT);
    // Objet en retour
    //    geo.idGeo_=geo.genereId();
    //    geo.modifie_=true;
    geo.setModifie();
    geo.pointsTopo= ptsTopo;
    geo.scene_.addDomaine(doma);
    geo.it_.maillage(RefondeTriangulation.trianguler(doma, ptsT));
    return geo;
  }
  /**
   * Lecture d'une géometrie
   * @param _fichier Nom du fichier de géométrie.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public static RefondeGeometrie ouvrir(File _fichier) throws IOException {
    RefondeGeometrie geo= new RefondeGeometrie();
    geo.lire(_fichier);
    geo.modifie_= false;
    return geo;
  }
  /**
   * Lecture des informations depuis le fichier associé
   */
  private void lire(File _fcGeom) throws IOException {
    //    int EOF   =StreamTokenizer.TT_EOF;
    //    int EOL   =StreamTokenizer.TT_EOL;
    //    int NUMBER=StreamTokenizer.TT_NUMBER;
    int WORD= StreamTokenizer.TT_WORD;
    StreamTokenizer file= null;
    Reader rf= null;
    int ival= 0;
    //    double dval=0;
    String version= "??";
    int nbPtsTopo;
    int nbPts;
    int nbPls;
    int nbCts;
    int nbDms;
    Vector vptsTopo;
    Vector vpts;
    Vector vpls;
    Vector vcts;
    Vector vdms;
    try {
      // Ouverture du fichier
      file= new StreamTokenizer(rf= new FileReader(_fcGeom));
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
      // Entète du fichier
      if (file.nextToken() != WORD
        || (!file.sval.equals("refonde") && !file.sval.equals("prefonde")))
        throw new RefondeIOException("Le fichier n'est pas un fichier Refonde");
      if (file.nextToken() != WORD
        || (version= file.sval).compareTo("5.01") < 0)
        throw new RefondeIOException(
          "Le format du fichier est de version "
            + version
            + ".\n"
            + "Seules les versions > à 5.07 sont autorisées");
      if (file.nextToken() != WORD || !file.sval.equals("modele_geometrique"))
        throw new RefondeIOException("Le fichier n'est pas un fichier de geometrie");
      // Identifiant de géométrie
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<identifiant_geometrie>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      idGeo_= Integer.parseInt(file.sval);
      // Nombre de points de topo
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<points_topo>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      nbPtsTopo= Integer.parseInt(file.sval);
      // Points de topo
      vptsTopo= new Vector(nbPtsTopo);
      for (int i= 0; i < nbPtsTopo; i++) {
        GrPoint pt= new GrPoint();
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.x_= Double.parseDouble(file.sval);
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.y_= Double.parseDouble(file.sval);
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.z_= Double.parseDouble(file.sval);
        vptsTopo.add(pt);
      }
      // Nombre de points
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<points>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      nbPts= Integer.parseInt(file.sval);
      // Points
      vpts= new Vector(nbPts);
      for (int i= 0; i < nbPts; i++) {
        GrPoint pt= new GrPoint();
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.x_= Double.parseDouble(file.sval);
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.y_= Double.parseDouble(file.sval);
        if (file.nextToken() != WORD)
          throw new IOException();
        pt.z_= Double.parseDouble(file.sval);
        vpts.add(pt);
      }
      // Nombre de lignes
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<lignes>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      nbPls= Integer.parseInt(file.sval);
      // Lignes
      vpls= new Vector(nbPls);
      for (int i= 0; i < nbPls; i++) {
        RefondePolyligne pl= new RefondePolyligne();
        for (int j= 0; j < 2; j++) {
          if (file.nextToken() != WORD)
            throw new IOException();
          ival= Integer.parseInt(file.sval);
          if (ival < 0 || ival > nbPts)
            throw new IOException();
          pl.sommets_.ajoute((GrPoint)vpts.get(ival));
        }
        vpls.add(pl);
      }
      // Nombre de contours
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<contours>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      nbCts= Integer.parseInt(file.sval);
      // Contours
      vcts= new Vector(nbCts);
      for (int i= 0; i < nbCts; i++) {
        //... Nombre de lignes
        if (file.nextToken() != WORD)
          throw new IOException();
        ival= Integer.parseInt(file.sval);
        //... Lignes
        RefondePolyligne[] pls= new RefondePolyligne[ival];
        for (int j= 0; j < pls.length; j++) {
          if (file.nextToken() != WORD)
            throw new IOException();
          ival= Integer.parseInt(file.sval);
          if (ival < 0 || ival > nbPls)
            throw new IOException();
          pls[j]= (RefondePolyligne)vpls.get(ival);
        }
        vcts.add(new RefondeContour(pls));
      }
      // Nombre de domaines
      if (file.nextToken() != WORD)
        throw new IOException();
      if (!file.sval.equals("<domaines>"))
        throw new IOException();
      if (file.nextToken() != WORD)
        throw new IOException();
      nbDms= Integer.parseInt(file.sval);
      // Domaines
      vdms= new Vector(nbDms);
      for (int i= 0; i < nbDms; i++) {
        //... Nombre de Contours
        if (file.nextToken() != WORD)
          throw new IOException();
        ival= Integer.parseInt(file.sval);
        //... Contour
        RefondeContour[] cts= new RefondeContour[ival];
        for (int j= 0; j < cts.length; j++) {
          if (file.nextToken() != WORD)
            throw new IOException();
          ival= Integer.parseInt(file.sval);
          if (ival < 0 || ival > nbCts)
            throw new IOException();
          cts[j]= (RefondeContour)vcts.get(ival);
        }
        //... Type
        if (file.nextToken() != WORD)
          throw new IOException();
        if (file.sval.equals("fond")) {
          vdms.add(new RefondeDomaineFond(cts));
        } else {
          RefondeDomaineDigue dm;
          RefondeGroupeProprietes gp;
          RefondePolyligne plExt;
          if (file.sval.equals("digue_perforee"))
            gp=
              new RefondeGroupeProprietes(
                RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE);
          else if (file.sval.equals("digue_transmissible"))
            gp=
              new RefondeGroupeProprietes(
                RefondeGroupeProprietes.HOULE_FOND_DIGUE_TRANSMISSIBLE);
          else
            throw new IOException();
          //... Extremité de digue
          if (file.nextToken() != WORD)
            throw new IOException();
          ival= Integer.parseInt(file.sval);
          if (ival < 0 || ival > nbPls)
            throw new IOException();
          plExt= (RefondePolyligne)vpls.get(ival);
          dm= new RefondeDomaineDigue(cts[0], plExt);
          dm.setGroupeProprietes(gp);
          vdms.add(dm);
        }
      }
    } catch (NumberFormatException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fcGeom + " ligne " + file.lineno());
    } catch (RefondeIOException _exc) {
      throw new IOException(
        "Erreur de lecture sur "
          + _fcGeom
          + " ligne "
          + file.lineno()
          + "\n"
          + _exc.getMessage());
    } catch (FileNotFoundException _exc) {
      throw new IOException("Erreur d'ouverture de " + _fcGeom);
    } catch (IOException _exc) {
      throw new IOException(
        "Erreur de lecture sur " + _fcGeom + " ligne " + file.lineno());
    }
    //    catch (Exception _exc) {
    //      throw new IOException(_exc.getMessage());
    //    }
    finally {
      if (rf != null)
        rf.close();
    }
    //--------------------------------------------------------------------------
    //---  Traitements avant retour  -------------------------------------------
    //--------------------------------------------------------------------------
    // Triangulation sur le domaine fond uniquement et initialisation de
    // l'interpolateur
    GrPoint[] ptsTopo= new GrPoint[vptsTopo.size()];
    vptsTopo.toArray(ptsTopo);
    for (int i= 0; i < vdms.size(); i++) {
      if (vdms.get(i) instanceof RefondeDomaineFond) {
        RefondeDomaineFond dm= (RefondeDomaineFond)vdms.get(i);
        it_.maillage(RefondeTriangulation.trianguler(dm, ptsTopo));
        break;
      }
    }
    // Remplissage de la scene
    for (int i= 0; i < vpts.size(); i++)
      scene_.addPoint((GrPoint)vpts.get(i));
    for (int i= 0; i < vpls.size(); i++)
      scene_.addPolyligne((RefondePolyligne)vpls.get(i));
    for (int i= 0; i < vcts.size(); i++)
      scene_.addContour((RefondeContour)vcts.get(i));
    for (int i= 0; i < vdms.size(); i++)
      scene_.addDomaine((RefondeDomaine)vdms.get(i));
    pointsTopo= vptsTopo;
  }
  /**
   * Enregistrement d'une géométrie
   * @param _fichier Nom du fichier de géométrie.
   * @exception FileNotFoundException Le fichier n'est pas trouvé
   */
  public void enregistrer(File _fichier) throws IOException {
    ecrire(_fichier);
    modifie_= false;
  }
  /**
   * Ecriture des informations sur le fichier associé
   */
  private void ecrire(File _fcGeom) throws IOException {
    Vector vpls= scene_.getPolylignes();
    Vector vcts= scene_.getContours();
    Vector vdms= scene_.getDomaines();
    Vector vpts= scene_.getPoints();
    // Correspondance objet -> numéro
    Hashtable hpts;
    Hashtable hpls;
    Hashtable hcts;
    Hashtable hdms;
    //... Points
    hpts= new Hashtable(vpts.size());
    for (int i= 0; i < vpts.size(); i++)
      hpts.put(vpts.get(i), new Integer(i));
    //... Lignes
    hpls= new Hashtable(vpls.size());
    for (int i= 0; i < vpls.size(); i++)
      hpls.put(vpls.get(i), new Integer(i));
    //... Contours
    hcts= new Hashtable(vcts.size());
    for (int i= 0; i < vcts.size(); i++)
      hcts.put(vcts.get(i), new Integer(i));
    //... Domaines
    hdms= new Hashtable(vdms.size());
    for (int i= 0; i < vdms.size(); i++)
      hdms.put(vdms.get(i), new Integer(i));
    PrintWriter file= null;
    try {
      // Ouverture du fichier
      file= new PrintWriter(new FileWriter(_fcGeom));
      // Entète du fichier
      file.print("refonde ; ");
      file.print(RefondeImplementation.informationsSoftware().version + " ; ");
      file.print("modele_geometrique");
      file.println();
      file.println();
      // Identifiant de géométrie
      file.println("<identifiant_geometrie> ; " + idGeo_);
      file.println();
      // Nombre de points de topo
      file.println("<points_topo> ; " + pointsTopo.size());
      // Points de topo
      for (int i= 0; i < pointsTopo.size(); i++) {
        GrPoint pt= (GrPoint)pointsTopo.get(i);
        file.println(pt.x_ + " ; " + pt.y_ + " ; " + pt.z_);
      }
      file.println();
      // Nombre de points
      file.println("<points> ; " + vpts.size());
      // Points
      for (int i= 0; i < vpts.size(); i++) {
        GrPoint pt= (GrPoint)vpts.get(i);
        file.println(pt.x_ + " ; " + pt.y_ + " ; " + pt.z_);
      }
      file.println();
      // Nombre de lignes
      file.println("<lignes> ; " + vpls.size());
      // Lignes
      for (int i= 0; i < vpls.size(); i++) {
        RefondePolyligne pl= (RefondePolyligne)vpls.get(i);
        GrPoint p1= pl.sommet(0);
        GrPoint p2= pl.sommet(1);
        file.println(hpts.get(p1) + " ; " + hpts.get(p2));
      }
      file.println();
      // Nombre de contours
      file.println("<contours> ; " + vcts.size());
      // Contours
      for (int i= 0; i < vcts.size(); i++) {
        RefondeContour ct= (RefondeContour)vcts.get(i);
        RefondePolyligne[] pls= ct.getPolylignes();
        file.print(pls.length);
        for (int j= 0; j < pls.length; j++)
          file.print(" ; " + hpls.get(pls[j]));
        file.println();
      }
      file.println();
      // Nombre de domaines
      file.println("<domaines> ; " + vdms.size());
      // Domaines
      for (int i= 0; i < vdms.size(); i++) {
        RefondeDomaine dm= (RefondeDomaine)vdms.get(i);
        RefondeContour[] cts= dm.getContours();
        file.print(cts.length);
        for (int j= 0; j < cts.length; j++)
          file.print(" ; " + hcts.get(cts[j]));
        if (dm instanceof RefondeDomaineFond) {
          file.print(" ; fond");
        } else {
          RefondeDomaineDigue dmd= (RefondeDomaineDigue)dm;
          if (dmd.getGroupeProprietes().getType()
            == RefondeGroupeProprietes.HOULE_FOND_PAROI_PERFOREE)
            file.print(" ; digue_perforee");
          else
            file.print(" ; digue_transmissible");
          file.print("; " + hpls.get(dmd.getExtremiteDigue()));
        }
        file.println();
      }
      file.println();
    } catch (IOException _exc) {
      throw new IOException("Erreur d'écriture sur " + _fcGeom);
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
   * Attribut identifiant de géométrie. Cet identifiant est modifié à chaque
   * sauvegarde.
   */
  public int identifiant() {
    return idGeo_;
  }
  /**
   * Attribut géométrie modifiée
   */
  public void setModifie() {
    if (!modifie_) {
      idGeo_= genereId();
      modifie_= true;
    }
  }
  public boolean estModifiee() {
    return modifie_;
  }
  /*
   * Retourne un identificateur sur 6 chiffres
   */
  private int genereId() {
    String id= String.valueOf(System.currentTimeMillis());
    return Integer.parseInt(id.substring(id.length() - 6));
  }
  /**
   * Interpolateur
   */
  public FudaaInterpolateurMaillage interpolateur() {
    return it_;
  }
  /**
   * Attribut points
   */
  public void add(GrPoint _pt) {
    scene_.addPoint(_pt);
    setModifie();
  }
  public void remove(GrPoint _pt) {
    scene_.removePoint(_pt);
    setModifie();
  }
  public Vector getPoints() {
    return scene_.getPoints();
  }
  /**
   * Attribut polylignes
   */
  public void add(RefondePolyligne _pl) {
    scene_.addPolyligne(_pl);
    setModifie();
  }
  public void remove(RefondePolyligne _pl) {
    scene_.removePolyligne(_pl);
    setModifie();
  }
  public Vector getPolylignes() {
    return scene_.getPolylignes();
  }
  /**
   * Attribut contours
   */
  public void add(RefondeContour _ct) {
    scene_.addContour(_ct);
    setModifie();
  }
  public void remove(RefondeContour _ct) {
    scene_.removeContour(_ct);
    setModifie();
  }
  public Vector getContours() {
    return scene_.getContours();
  }
  /**
   * Attribut domaines
   */
  public void add(RefondeDomaine _dm) {
    scene_.addDomaine(_dm);
    initFrontieres();
    setModifie();
  }
  public void remove(RefondeDomaine _dm) {
    scene_.removeDomaine(_dm);
    initFrontieres();
    setModifie();
  }
  public Vector getDomaines() {
    return scene_.getDomaines();
  }
  /**
   * Réinitialisation des frontieres avec les domaines existants. En fait,
   * les frontières sont calculées à l'appel de frontieres() suivant
   */
  private void initFrontieres() {
    frontieres_= null;
  }
  /**
   * Retourne la frontière correspondant à une polyligne.
   *
   * @param _pl La polyligne non orientée d'un domaine géométrique.
   * @return La frontière, ou <i>null</i> si la polyligne
   *         n'appartient pas a une frontiere.
   */
  public RefondePolyligne frontiere(RefondePolyligne _pl) {
    frontieres();
    return (RefondePolyligne)poly2Bord_.get(_pl);
  }
  /**
   * Retourne la polyligne associée à une frontière.
   *
   * @param _frontiere Une frontière du domaine d'étude.
   * @return La polyligne géométrique associée.
   */
  public RefondePolyligne polyligne(RefondePolyligne _frontiere) {
    frontieres();
    return (RefondePolyligne)poly2Bord_.get(_frontiere);
  }
  /**
   * Retourne vrai si la géometrie contient le point 2D donné. Retourne vrai
   * si au moins 1 domaine de la géométrie contient le point.
   */
  public boolean contient(GrPoint _pt) {
    Vector dms= scene_.getDomaines();
    for (int i= 0; i < dms.size(); i++) {
      if (((RefondeDomaine)dms.get(i)).contient(_pt))
        return true;
    }
    return false;
  }
  /**
   * Retourne les frontières du domaine d'étude.
   * <p>
   * Le domaine d'étude est défini par l'union de tous les domaines
   * géométriques. Il est délimité par un contour externe, orienté dans le sens
   * trigo, et éventuellement des contours internes, orientés dans le sens
   * horaire. Chaque contour est défini par des polylignes appelées
   * <code>frontières</code>.
   * <p>
   * Les frontières, contrairement aux polylignes des domaines géométriques,
   * sont orientées, dans le même sens que le contour qui les contient.
   *
   * @return Les frontières sous forme de Vector de Vector de RefondePolyligne,
   *         par contour. Le contour extérieur est le premier, les contours
   *         internes suivent.
   */
  public Vector frontieres() {
    if (frontieres_ != null)
      return frontieres_;
    frontieres_= new Vector();
    poly2Bord_= new Hashtable();
    RefondeContour[] cts;
    RefondeContour ct;
    RefondePolyligne[] pls;
    RefondePolyligne pl;
    Vector vdms;
    Vector vcts;
    Vector vpls;
    // Contour exterieur => Celui du domaine fond, éventuellement modifié s'il
    // existe un ou plusieurs domaines digues en frontière du domaine
    vdms= scene_.getDomaines();
    vcts= new Vector();
    RefondeDomaine doma= null;
    for (int i= 0; i < vdms.size(); i++) {
      if (vdms.get(i) instanceof RefondeDomaineFond) {
        doma= (RefondeDomaine)vdms.get(i);
        break;
      }
    }
    if (doma == null) {
      System.out.println("Erreur : aucun domaine fond trouve");
      return frontieres_;
    }
    vpls= new Vector();
    ct= doma.getContours()[0];
    pls= ct.getPolylignes();
    int ipl= 0;
    while (ipl < pls.length) {
      RefondeGeom[] parents;
      // On continue le contour exterieur sur le contour adjacent
      if (getDomaines(pls[ipl]).size() > 1) {
        parents= pls[ipl].getParents();
        //      if ((parents=pls[ipl].getParents()).length>1) {
        RefondeContour ctAdj;
        RefondePolyligne[] plsCtAdj;
        int decal;
        int sens;
        if (parents[0] == ct)
          ctAdj= (RefondeContour)parents[1];
        else
          ctAdj= (RefondeContour)parents[0];
        decal= ctAdj.indice(pls[ipl]);
        plsCtAdj= ctAdj.getPolylignes();
        //        for (decal=0; decal<plsCtAdj.length; decal++)
        //         if (plsCtAdj[decal]==pls[ipl]) break;
        if (plsCtAdj[(decal + 1) % plsCtAdj.length].getParents().length == 1)
          sens= 1;
        else
          sens= -1;
        for (int j= 0; j < plsCtAdj.length; j++) {
          //          pl=plsCtAdj[(j*sens+decal+1+plsCtAdj.length)%plsCtAdj.length];
          pl=
            plsCtAdj[((j + 1) * sens + decal + plsCtAdj.length)
              % plsCtAdj.length];
          if (pl.getParents().length == 1)
            vpls.add(pl);
          else
            break;
        }
        // Saut de toutes les polylignes du contour appartenant à 2 contours
        //        while (pls[ipl].getParents().length>1) ipl=(ipl+1)%pls.length;
        while (ipl < pls.length && pls[ipl].getParents().length > 1)
          ipl++;
      } else {
        vpls.add(pls[ipl]);
        ipl++;
      }
    }
    pls= new RefondePolyligne[vpls.size()];
    vpls.toArray(pls);
    vcts.add(new RefondeContour(pls));
    // Les contours internes
    cts= doma.getContours();
    for (int i= 1; i < cts.length; i++)
      if (cts[i].getParents().length == 1)
        vcts.add(new RefondeContour(cts[i].getPolylignes()));
    // Pour tous les contours temporaires, orientation des lignes et des contours
    for (int i= 0; i < vcts.size(); i++) {
      RefondeContour ctCopy;
      RefondePolyligne[] plsCopy;
      ct= (RefondeContour)vcts.get(i);
      ctCopy= ct.copie();
      // Correspondance polyligne -> frontière : Le contour copié n'a pas
      // encore été orienté
      pls= ct.getPolylignes();
      plsCopy= ctCopy.getPolylignes();
      for (int j= 0; j < pls.length; j++) {
        poly2Bord_.put(pls[j], plsCopy[j]);
        poly2Bord_.put(plsCopy[j], pls[j]);
      }
      // Orientation du contour copié
      ctCopy.orienteTrigo(i == 0);
      plsCopy= ctCopy.getPolylignes();
      ctCopy.setPolylignes(null); // Suppression des références à ce contour
      ct.setPolylignes(null); // Suppression des références à ce contour
      frontieres_.add(new Vector(Arrays.asList(plsCopy)));
    }
    return frontieres_;
  }
  /*
   * Domaines parents pour la polyligne spécifiée ou 0
   */
  private Vector getDomaines(RefondePolyligne _pl) {
    Vector vdms= new Vector();
    RefondeGeom[] cts= _pl.getParents();
    for (int i= 0; i < cts.length; i++) {
      vdms.addAll(Arrays.asList(cts[i].getParents()));
    }
    return vdms;
  }
  /*
   * Collage des lignes des contours pour non redondance
   */
  private void colLignes() {
    // Collage des points de même coordonnées
    colPoints();
    Vector vpls= scene_.getPolylignes();
    RefondePolyligne[] pls= new RefondePolyligne[vpls.size()];
    vpls.toArray(pls);
    for (int i= 0; i < pls.length; i++) {
      RefondePolyligne pl1= pls[i];
      for (int j= i + 1; j < pls.length; j++) {
        RefondePolyligne pl2= pls[j];
        if (ontMemesPoints(pl1, pl2)) {
          RefondeGeom[] parents= pl1.getParents();
          for (int k= 0; k < parents.length; k++) {
            RefondeContour pere= (RefondeContour)parents[k];
            RefondePolyligne[] plsCntr= pere.getPolylignes();
            for (int l= 0; l < plsCntr.length; l++)
              if (plsCntr[l] == pl1) {
                plsCntr[l]= pl2;
                break;
              }
            pere.setPolylignes(plsCntr);
          }
          //          pl2.addParent(parents[k]);
          scene_.removePolyligne(pl1);
          break;
        }
      }
    }
  }
  /*
   * Collage des points des lignes pour non redondance
   */
  private void colPoints() {
    Hashtable coor2pts= new Hashtable();
    Vector vpls= scene_.getPolylignes();
    for (int i= 0; i < vpls.size(); i++) {
      RefondePolyligne pl= (RefondePolyligne)vpls.get(i);
      for (int j= 0; j < pl.sommets_.nombre(); j++) {
        GrPoint ptExst;
        GrPoint pt= pl.sommet(j);
        String coors= pt.x_ + " " + pt.y_;
        if ((ptExst= (GrPoint)coor2pts.get(coors)) != null)
          pl.sommets_.remplace(ptExst, j);
        else
          coor2pts.put(coors, pt);
      }
    }
    // Stockage des points dans la scène
    for (Enumeration e= coor2pts.elements(); e.hasMoreElements();)
      scene_.addPoint((GrPoint)e.nextElement());
  }
  /*
   * Les 2 polylignes ont les memes points ordonnes
   */
  private boolean ontMemesPoints(
    RefondePolyligne _pl1,
    RefondePolyligne _pl2) {
    int nombre;
    if ((nombre= _pl1.sommets_.nombre()) != _pl2.sommets_.nombre())
      return false;
    if (nombre == 0)
      return true;
    if (_pl1.sommet(0) == _pl2.sommet(0)) {
      for (int i= 1; i < nombre; i++)
        if (_pl1.sommet(i) != _pl2.sommet(i))
          return false;
      return true;
    } else if (_pl1.sommet(0) == _pl2.sommet(nombre - 1)) {
      for (int i= 1; i < nombre; i++)
        if (_pl1.sommet(i) != _pl2.sommet(nombre - 1 - i))
          return false;
      return true;
    }
    return false;
  }
}