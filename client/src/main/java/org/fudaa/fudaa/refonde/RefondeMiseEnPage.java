/*
 * @file         RefondeMiseEnPage.java
 * @creation     2001-10-26
 * @modification $Date: 2006-12-05 10:18:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import org.fudaa.ebli.calque.BCalque;
import org.fudaa.ebli.calque.BCalqueAffichage;
import org.fudaa.ebli.calque.BCalqueCarte;
import org.fudaa.ebli.calque.BCalqueLegende;
import org.fudaa.ebli.calque.BVueCalque;
import org.fudaa.ebli.calque.dessin.*;
import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrMorphisme;
import org.fudaa.ebli.geometrie.GrPoint;
import org.fudaa.ebli.geometrie.GrPolygone;
import org.fudaa.ebli.geometrie.GrPolyligne;
import org.fudaa.ebli.geometrie.GrSegment;

import org.fudaa.fudaa.commun.trace2d.BPaletteCouleurPlage;
/**
 * Classe de sauvegarde/récupération de la mise en page.
 *
 * @version      $Id: RefondeMiseEnPage.java,v 1.13 2006-12-05 10:18:14 deniger Exp $
 * @author       Bertrand Marchand
 */
public class RefondeMiseEnPage {
  /**
   * Noms d'attributs ou d'éléments
   */
  public static final String NM_FENETRES= "fenetres";
  public static final String NM_FENETRE= "fenetre";
  public static final String NM_ID= "id";
  public static final String NM_GEOMETRIE= "geometrie";
  public static final String NM_POS_X= "pos_x";
  public static final String NM_POS_Y= "pos_y";
  public static final String NM_LARGEUR= "largeur";
  public static final String NM_HAUTEUR= "hauteur";
  public static final String NM_VUE= "vue";
  public static final String NM_CALQUE= "calque";
  public static final String NM_TYPE= "type";
  public static final String NM_PAS= "pas";
  public static final String NM_COL_FOND= "couleur_fond";
  public static final String NM_COL_TRACE= "couleur_trace";
  public static final String NM_ROUGE= "rouge";
  public static final String NM_VERT= "vert";
  public static final String NM_BLEU= "bleu";
  public static final String NM_AVEC_CTR= "avec_contours";
  public static final String NM_AVEC_ISL= "avec_isolignes";
  public static final String NM_AVEC_ISS= "avec_isosurfaces";
  //  public static final String NM_ECART="ecart";
  public static final String NM_BORNES= "bornes";
  public static final String NM_MIN= "min";
  public static final String NM_MAX= "max";
  public static final String NM_PALETTE= "palette";
  public static final String NM_PLAGES= "plages";
  public static final String NM_PLAGE= "plage";
  //  public static final String NM_ESPACE="espace";
  //  public static final String NM_ESPACE_RVB="rvb";
  //  public static final String NM_ESPACE_TDL="tdl";
  //  public static final String NM_NB_PALIERS="nb_paliers";
  //  public static final String NM_NB_CYCLES="nb_cycles";
  //  public static final String NM_COL_MIN="couleur_min";
  //  public static final String NM_COL_MAX="couleur_max";
  public static final String NM_MEP= "mise_en_page";
  public static final String NM_VERSION= "version";
  public static final String NM_LOGICIEL= "logiciel";
  public static final String NM_REFONDE= "refonde";
  public static final String NM_FORMAT= "format";
  public static final String NM_MARGES= "marges";
  public static final String NM_GAUCHE= "gauche";
  public static final String NM_DROITE= "droite";
  public static final String NM_BAS= "bas";
  public static final String NM_HAUT= "haut";
  public static final String NM_LEGENDES= "legendes";
  public static final String NM_LEGENDE= "legende";
  public static final String NM_POLICE= "police";
  public static final String NM_NOM= "nom";
  public static final String NM_TAILLE= "taille";
  public static final String NM_STYLE= "style";
  public static final String NM_STYLE_N= "normal";
  public static final String NM_STYLE_I= "italique";
  public static final String NM_STYLE_G= "gras";
  public static final String NM_ID_FENETRE= "id_fenetre";
  public static final String NM_ID_CALQUE= "id_calque";
  public static final String NM_DESSINS= "dessins";
  public static final String NM_DESSIN= "dessin";
  public static final String NM_TYPE_TR= "trait";
  public static final String NM_TYPE_TX= "texte";
  public static final String NM_TYPE_CA= "carre";
  public static final String NM_TYPE_RE= "rectangle";
  public static final String NM_TYPE_CF= "courbe_fermee";
  public static final String NM_TYPE_PG= "polygone";
  public static final String NM_TYPE_CO= "courbe";
  public static final String NM_TYPE_PL= "polyligne";
  public static final String NM_TYPE_CE= "cercle";
  public static final String NM_TYPE_EL= "Ellipse";
  public static final String NM_TEXTE= "texte";
  public static final String NM_POINT= "point";
  /**
   * Implémentation.
   *
   * B.M. 04/10/2001
   * Elle sert a créer les fenetres de post lors de la relecture de la mise
   * en page. En effet, la solution qui consiste à créer des fenetres de post
   * temporaires (non dimensionnées - donc non affichables dans le desktop)
   * pour les retoucher plus tard quand la valeur du desktop est accessible
   * semble quasi impossible à mettre au point.
   * On choisi donc de créer ces fenetres dans l'interface en même temps qu'on
   * lit le projet, ce qui est loin d'être propre.
   *
   * Ce champ est initialisé au tout début de l'application.
   */
  public static RefondeImplementation imp= null;
  /**
   * La valeur d'un inch en mm.
   */
  //private static final double UN_INCH_EN_MM= 25.4;
  /**
   * Pour passer des coordonnées imprimante en coordonnées millimètres. Une
   * unité d'impression représente 1/72ieme d'inch.
   */
  //private static final double ImpToMM=UN_INCH_EN_MM/72;
  /**
   * Format d'impression par défaut en mm.
   */
  public static final PageFormat pageDefaut=
    RefondeMiseEnPageHelper.convert2MM(
      PrinterJob.getPrinterJob().defaultPage());
  /**
   * Format de mise en page.
   */
  private PageFormat pageFormat_;
  /**
   * Formes de dessin.
   */
  private RefondeModeleDessin mdlDes_;
  /**
   * Fenêtres
   */
  private RefondeModeleFenetres mdlFns_;
  /**
   * Légendes
   */
  private RefondeModeleLegendes mdlLgs_;
  /**
   * Etat de modification ou non de la mise en page.
   */
  //private boolean modifie_;
  //----------------------------------------------------------------------------
  /**
   * Création d'une mise en page par défaut.
   */
  public static RefondeMiseEnPage defaut(RefondeProjet _prj) {
    return new RefondeMiseEnPage();
  }
  /**
   * Création d'une mise en page.
   */
  public RefondeMiseEnPage() {
    //modifie_= false;
    pageFormat_= pageDefaut;
    mdlDes_= new RefondeModeleDessin();
    mdlFns_= new RefondeModeleFenetres();
    mdlLgs_= new RefondeModeleLegendes();
  }
  /**
   * Accesseur à la propriété modele de dessin.
   */
  public RefondeModeleDessin getModeleDessin() {
    return mdlDes_;
  }
  /**
   * Accesseur à la propriété modèle de fenetres.
   */
  public RefondeModeleFenetres getModeleFenetres() {
    return mdlFns_;
  }
  /**
   * Accesseur à la propriété modèle de légendes.
   */
  public RefondeModeleLegendes getModeleLegendes() {
    return mdlLgs_;
  }
  /**
   * Accesseur à la propriété format de page en mm.
   */
  public PageFormat getPageFormat() {
    return pageFormat_;
  }
  /**
   * Vide la mise en page.
   */
  public void vide() {
    mdlDes_.enleveTout();
    mdlFns_.enleveTout();
    mdlLgs_.enleveTout();
  }
  /**
   * Retourne si la mise en page en vide.
   * @return <i>true</i> si pas de fenetres, pas de dessins et pas de légendes.
   */
  public boolean isVide() {
    return mdlDes_.getObjets().size() == 0
      && mdlFns_.getObjets().size() == 0
      && mdlLgs_.getObjets().size() == 0;
  }
  /**
   * Lecture d'une mise en page
   * @param _prj Le projet.
   * @param _fichier Nom du fichier de mise en page.
   */
  public static RefondeMiseEnPage ouvrir(RefondeProjet _prj, File _fichier)
    throws IOException {
    RefondeMiseEnPage mep= new RefondeMiseEnPage();
    mep.lire(_prj, _fichier);
    //mep.modifie_= false;
    return mep;
  }
  /**
   * Lecture des informations depuis le fichier associé
   */
  private void lire(RefondeProjet _prj, File _fc) throws IOException {
    Document document;
    DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
    //    NodeList els;
    Hashtable hidFn2Fn= new Hashtable(); // Table id fenetre -> Fenetre
    Hashtable hfn2IdCq2Cq= new Hashtable();
    // Table fenetre -> Hashtable hidCq2Cq
    Hashtable hidCq2Cq; // Table id calque -> Calque
    try {
      // Lecture et parse du document.
      DocumentBuilder builder= factory.newDocumentBuilder();
      document= builder.parse(_fc);
      // <mise_en_page> (version, logiciel)
      Element root= document.getDocumentElement();
      if (!root.getTagName().equals(NM_MEP))
        throw new SAXException(
          "Erreur de lecture sur "
            + _fc
            + "\nLe fichier n'est pas un fichier de mise en page");
      if (!getStringAtt(root, NM_LOGICIEL).equals(NM_REFONDE))
        throw new SAXException(
          "Erreur de lecture sur "
            + _fc
            + "\nLe fichier n'est pas un fichier Refonde");

      // <Format> (largeur, hauteur)
      Element format= getSousElement(root, NM_FORMAT);
      double w= getDoubleAtt(format, NM_LARGEUR);
      double h= getDoubleAtt(format, NM_HAUTEUR);

      // <Marges> (gauche,droite,haut,bas)
      Element marges= getSousElement(format, NM_MARGES);
      double mrgg= getDoubleAtt(marges, NM_GAUCHE);
      double mrgd= getDoubleAtt(marges, NM_DROITE);
      double mrgh= getDoubleAtt(marges, NM_HAUT);
      double mrgb= getDoubleAtt(marges, NM_BAS);
      double imx= mrgg;
      double imy= mrgb;
      double imw= w - mrgg - mrgd;
      double imh= h - mrgh - mrgb;
      pageFormat_= new PageFormat();

      // On renverse la page, et on change l'orientation
      if (w > h) {
        Paper pp= pageFormat_.getPaper();
        pp.setSize(h, w);
        pp.setImageableArea(h - imh - imy, imx, imh, imw);
        pageFormat_.setPaper(pp);
        pageFormat_.setOrientation(PageFormat.LANDSCAPE);
      }

      // On laisse tel quel
      else {
        Paper pp= pageFormat_.getPaper();
        pp.setSize(w, h);
        pp.setImageableArea(imx, imy, imw, imh);
        pageFormat_.setPaper(pp);
        pageFormat_.setOrientation(PageFormat.PORTRAIT);
      }

      //---  Fenetres  ---------------------------------------------------------

      // Morphisme de transformation Mise ne page -> Ecran
      JDesktopPane dsk= imp.getMainPanel().getDesktop();
      GrMorphisme m= RefondeMiseEnPageHelper.convertMEP2Ecran(pageFormat_, dsk);
      Element fenetres= getSousElement(root, NM_FENETRES);
      Element[] fenetre= getSousElements(fenetres, NM_FENETRE);

      for (int i= 0; i < fenetre.length; i++) {

        // La fenetre construite ici est définitive.
        RefondeFillePost fnPost= new RefondeFillePost(imp.getArbreCalque());
        fnPost.initialise(_prj);

        BVueCalque vc= fnPost.getVueCalque();
        vc.getCalque().getCalqueParNom("cqLegende").setVisible(false);

        // Identifiant
        hidFn2Fn.put(new Integer(getIntAtt(fenetre[i], NM_ID)), fnPost);
        hidCq2Cq= new Hashtable();
        hfn2IdCq2Cq.put(fnPost, hidCq2Cq);

        // Geometrie
        Element geometrie= getSousElement(fenetre[i], NM_GEOMETRIE);
        double xw= getDoubleAtt(geometrie, NM_POS_X);
        double yw= getDoubleAtt(geometrie, NM_POS_Y);
        double ww= getDoubleAtt(geometrie, NM_LARGEUR);
        double hw= getDoubleAtt(geometrie, NM_HAUTEUR);
        GrPoint po= new GrPoint(xw, yw + hw, 0);
        GrPoint pe= new GrPoint(xw + ww, yw, 0);
        po.autoApplique(m);
        pe.autoApplique(m);
        vc.setPreferredSize(new Dimension((int)(pe.x_-po.x_),(int)(pe.y_-po.y_)));
        vc.setSize(vc.getPreferredSize());

        // Vue
        Element vue= getSousElement(fenetre[i], NM_VUE);
        double xv= getDoubleAtt(vue, NM_POS_X);
        double yv= getDoubleAtt(vue, NM_POS_Y);
        double wv= getDoubleAtt(vue, NM_LARGEUR);
        double hv= getDoubleAtt(vue, NM_HAUTEUR);

        GrBoite bt= new GrBoite();
        bt.ajuste(new GrPoint(xv, yv, 0));
        bt.ajuste(new GrPoint(xv + wv, yv + hv, 0));
        vc.changeRepere(this, bt, 0);

        // Calques
        Element[] calques= getSousElements(fenetre[i], NM_CALQUE);
        for (int j= 0; j < calques.length; j++) {
          String type= getStringAtt(calques[j], NM_TYPE);
          Double valPas=new Double(0.0);
          try { valPas=new Double(getDoubleAtt(calques[j],NM_PAS)); }
          catch (SAXException _exc) {} // => Attribut non présent autorisé (versions < 5.14)

          RefondeCalqueCarte cqRes=(RefondeCalqueCarte)vc.getCalque().getCalqueParTitre(type);

          // Aucun calque correspondant existant => On passe
          if (cqRes == null) continue;

          // Valeur du temps
          if (cqRes.getModeleValeur() instanceof RefondeModeleVisuResultats) {
            ((RefondeModeleVisuResultats)cqRes.getModeleValeur()).setSelectedStep(valPas);
          }

          BPaletteCouleurPlage pal= cqRes.getPalette();

          // Identifiant
          hidCq2Cq.put(new Integer(getIntAtt(calques[j], NM_ID)), cqRes);

          int rouge;
          int vert;
          int bleu;

          // Couleur de fond
          Element colFond= getSousElement(calques[j], NM_COL_FOND);
          rouge= getIntAtt(colFond, NM_ROUGE);
          vert= getIntAtt(colFond, NM_VERT);
          bleu= getIntAtt(colFond, NM_BLEU);
          cqRes.setBackground(new Color(rouge, vert, bleu));

          // Couleur de tracé
          Element colTrace= getSousElement(calques[j], NM_COL_TRACE);
          rouge= getIntAtt(colTrace, NM_ROUGE);
          vert= getIntAtt(colTrace, NM_VERT);
          bleu= getIntAtt(colTrace, NM_BLEU);
          cqRes.setForeground(new Color(rouge, vert, bleu));
          // Avec contours ?
          Element avecCtr= null;
          try {
            avecCtr= getSousElement(calques[j], NM_AVEC_CTR);
          } catch (SAXException _exc) {} // => Champ non présent autorisé
          cqRes.setContour(avecCtr != null);
          // Avec isolignes ?
          Element avecIsl= null;
          try {
            avecIsl= getSousElement(calques[j], NM_AVEC_ISL);
          } catch (SAXException _exc) {} // => Champ non présent autorisé
          cqRes.setIsolignes(avecIsl != null);
          // Avec isosurfaces ?
          Element avecIss= null;
          try {
            avecIss= getSousElement(calques[j], NM_AVEC_ISS);
          } catch (SAXException _exc) {} // // => Champ non présent autorisé
          cqRes.setIsosurfaces(avecIss != null);
          cqRes.setIsolignes(avecIsl != null);
          //          // Ecart
          //          Element ecart=getSousElement(calques[j],NM_ECART);
          //          cqRes.setEcart(getDoubleValeur(ecart));
          //          // Bornes
          //          Element bornes=null;
          //          try { bornes=getSousElement(calques[j],NM_BORNES); }
          //          catch (SAXException _exc) {} // => Champ non présent autorisé
          //          if (bornes!=null) {
          //            try { cqRes.setMinValeur(getDoubleAtt(bornes,NM_MIN)); }
          //            catch (SAXException _exc) {} // => Champ non présent autorisé
          //            try { cqRes.setMaxValeur(getDoubleAtt(bornes,NM_MAX)); }
          //            catch (SAXException _exc) {} // => Champ non présent autorisé
          //          }
          // Palette
          //          BPaletteCouleurSimple pal=new BPaletteCouleurSimple();
          Element palette= getSousElement(calques[j], NM_PALETTE);
          // Plages
          Element plages= getSousElement(palette, NM_PLAGES);
          Element[] plage= getSousElements(plages, NM_PLAGE);
          pal.setNbPlages(plage.length);
          for (int k= 0; k < plage.length; k++) {
            // Couleur de tracé
            colTrace= getSousElement(plage[k], NM_COL_TRACE);
            rouge= getIntAtt(colTrace, NM_ROUGE);
            vert= getIntAtt(colTrace, NM_VERT);
            bleu= getIntAtt(colTrace, NM_BLEU);
            pal.setCouleurPlage(k, new Color(rouge, vert, bleu));
            // Bornes
            Element bornes= getSousElement(plage[k], NM_BORNES);
            pal.setMinPlage(k, getDoubleAtt(bornes, NM_MIN));
            pal.setMaxPlage(k, getDoubleAtt(bornes, NM_MAX));
          }
          //          pal.setEspace(getStringAtt(palette,NM_ESPACE).equals(NM_ESPACE_RVB));
          //
          //          // Nombre de paliers
          //          Element nbPaliers=getSousElement(palette,NM_NB_PALIERS);
          //          pal.setPaliers(getIntValeur(nbPaliers));
          //
          //          // Nombre de cycles
          //          Element nbCycles=getSousElement(palette,NM_NB_CYCLES);
          //          pal.setCycles(getIntValeur(nbCycles));
          //
          //          // Couleur min
          //          Element colMin=getSousElement(palette,NM_COL_MIN);
          //          rouge=getIntAtt(colMin,NM_ROUGE);
          //          vert =getIntAtt(colMin,NM_VERT);
          //          bleu =getIntAtt(colMin,NM_BLEU);
          //          pal.setCouleurMin(new Color(rouge,vert,bleu));
          //
          //          // Couleur max
          //          Element colMax=getSousElement(palette,NM_COL_MAX);
          //          rouge=getIntAtt(colMax,NM_ROUGE);
          //          vert =getIntAtt(colMax,NM_VERT);
          //          bleu =getIntAtt(colMax,NM_BLEU);
          //          pal.setCouleurMax(new Color(rouge,vert,bleu));
          //
          //          cqRes.setPaletteCouleur(pal);
          cqRes.setVisible(true);
        }
        mdlFns_.ajoute(vc, xw, yw, ww, hw);
        fnPost.setLocation((int)po.x_, (int)po.y_);
        fnPost.getVueCalque().setTaskView(imp.getTaskView());
        fnPost.pack();
        // L'ajout effectif de la fenetre sera effectué plus tard.
        class AddPost implements Runnable {
          RefondeFillePost fn_;
          RefondeImplementation imp_;
          public AddPost(RefondeFillePost _fn, RefondeImplementation _imp) {
            fn_= _fn;
            imp_= _imp;
          }
          public void run() {
            imp_.addInternalFrame(fn_);
          }
        }
        SwingUtilities.invokeLater(new AddPost(fnPost, imp));
      }
      //---  Légendes  ---------------------------------------------------------
      Element legendes= getSousElement(root, NM_LEGENDES);
      Element[] legende= getSousElements(legendes, NM_LEGENDE);
      for (int i= 0; i < legende.length; i++) {
        // Geometrie
        Element geometrie= getSousElement(legende[i], NM_GEOMETRIE);
        double xw= getDoubleAtt(geometrie, NM_POS_X);
        double yw= getDoubleAtt(geometrie, NM_POS_Y);
        double ww= getDoubleAtt(geometrie, NM_LARGEUR);
        double hw= getDoubleAtt(geometrie, NM_HAUTEUR);
        int rouge;
        int vert;
        int bleu;
        // Couleur de fond
        Element colFond= getSousElement(legende[i], NM_COL_FOND);
        rouge= getIntAtt(colFond, NM_ROUGE);
        vert= getIntAtt(colFond, NM_VERT);
        bleu= getIntAtt(colFond, NM_BLEU);
        Color clBg= new Color(rouge, vert, bleu);
        // Couleur de tracé
        Element colTrace= getSousElement(legende[i], NM_COL_TRACE);
        rouge= getIntAtt(colTrace, NM_ROUGE);
        vert= getIntAtt(colTrace, NM_VERT);
        bleu= getIntAtt(colTrace, NM_BLEU);
        Color clFg= new Color(rouge, vert, bleu);
        // Police
        Element police= getSousElement(legende[i], NM_POLICE);
        String nom= getStringAtt(police, NM_NOM);
        int size= getIntAtt(police, NM_TAILLE);
        String stl= getStringAtt(police, NM_STYLE);
        int style;
        if (stl.equals(NM_STYLE_N))
          style= Font.PLAIN;
        else if (stl.equals(NM_STYLE_G))
          style= Font.BOLD;
        else if (stl.equals(NM_STYLE_I))
          style= Font.ITALIC;
        else
          throw new SAXException(
            "Erreur de lecture sur "
              + _fc
              + "\nLe style de police est invalide");
        Font plc= new Font(nom, style, size);
        // Identifiant fenetre
        int idFn= getIntValeur(getSousElement(legende[i], NM_ID_FENETRE));
        // Identifiant calque
        Element idCalque= getSousElement(legende[i], NM_ID_CALQUE);
        int idCq= getIntValeur(idCalque);
        // Récupération de la légende et ajout dans le modèle.
        RefondeFillePost fn= (RefondeFillePost)hidFn2Fn.get(new Integer(idFn));
        hidCq2Cq= (Hashtable)hfn2IdCq2Cq.get(fn);
        BCalqueCarte cq= (BCalqueCarte)hidCq2Cq.get(new Integer(idCq));
        BCalqueLegende cqLg=
          (BCalqueLegende)fn.getVueCalque().getCalque().getCalqueParNom(
            "cqLegende");
        cqLg.setBackground(cq, clBg);
        cqLg.setForeground(cq, clFg);
        cqLg.setFont(plc);
        JPanel pnLg= cqLg.getLegende(cq);
        mdlLgs_.ajoute(pnLg, cqLg, cq, xw, yw, ww, hw);
        // Mise à jour de l'interface.
        //        SwingUtilities.invokeLater(new Runnable() {
        //          public void run() {
        //          }
        //        });
        cqLg.setVisible(true);
      }
      //---  Dessins  ----------------------------------------------------------
      Element dessins= getSousElement(root, NM_DESSINS);
      Element[] dessin= getSousElements(dessins, NM_DESSIN);
      for (int i= 0; i < dessin.length; i++) {
        String type= getStringAtt(dessin[i], NM_TYPE);
        // Géométrie
        Element geometrie= getSousElement(dessin[i], NM_GEOMETRIE);
        Element[] points= getSousElements(geometrie, NM_POINT);
        GrPoint[] ptGeom= new GrPoint[points.length];
        for (int j= 0; j < points.length; j++) {
          double x= getDoubleAtt(points[j], NM_POS_X);
          double y= getDoubleAtt(points[j], NM_POS_Y);
          ptGeom[j]= new GrPoint(x, y, 0);
        }
        DeForme des;
        // Trait
        if (type.equals(NM_TYPE_TR)) {
          if (ptGeom.length != 2)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          des= new DeTrait(ptGeom[0], ptGeom[1]);
        }
        // Texte
        else if (type.equals(NM_TYPE_TX)) {
          if (ptGeom.length != 1)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          Element texte= getSousElement(dessin[i], NM_TEXTE);
          String txt= getTexteValeur(texte);
          Element police= getSousElement(dessin[i], NM_POLICE);
          String nom= getStringAtt(police, NM_NOM);
          int size= getIntAtt(police, NM_TAILLE);
          String stl= getStringAtt(police, NM_STYLE);
          int style;
          if (stl.equals(NM_STYLE_N))
            style= Font.PLAIN;
          else if (stl.equals(NM_STYLE_G))
            style= Font.BOLD;
          else if (stl.equals(NM_STYLE_I))
            style= Font.ITALIC;
          else
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe style de police est invalide");
          des= new DeTexte(txt, ptGeom[0]);
          ((DeTexte)des).setFont(new Font(nom, style, size));
        }
        // Carré
        else if (type.equals(NM_TYPE_CA)) {
          if (ptGeom.length != 4)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DeCarre(pl);
        }
        // Rectangle
        else if (type.equals(NM_TYPE_RE)) {
          if (ptGeom.length != 4)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DeRectangle(pl);
        }
        // Courbe fermée
        else if (type.equals(NM_TYPE_CF)) {
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DeCourbeFermee(new DeLigneBrisee(pl));
        }
        // Polygone
        else if (type.equals(NM_TYPE_PG)) {
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DePolygone(pl);
        }
        // Courbe
        else if (type.equals(NM_TYPE_CO)) {
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DeMainLevee(new DeLigneBrisee(pl));
        }
        // Polyligne
        else if (type.equals(NM_TYPE_PL)) {
          GrPolyligne pl= new GrPolyligne();
          pl.sommets_.tableau(ptGeom);
          des= new DeLigneBrisee(pl);
        }
        // Cercle
        else if (type.equals(NM_TYPE_CE)) {
          if (ptGeom.length != 4)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          des= new DeCercle(ptGeom);
        }
        // Ellipse
        else if (type.equals(NM_TYPE_EL)) {
          if (ptGeom.length != 4)
            throw new SAXException(
              "Erreur de lecture sur "
                + _fc
                + "\nLe nombre de points n'est pas cohérent avec le type de dessin");
          des= new DeEllipse(ptGeom);
        } else
          throw new SAXException(
            "Erreur de lecture sur "
              + _fc
              + "\nLe type de dessin est invalide");
        int rouge;
        int vert;
        int bleu;
        // Couleur de fond (peut être nulle)
        Element colFond= null;
        try {
          colFond= getSousElement(dessin[i], NM_COL_FOND);
        } catch (SAXException _exc) {} // => Champ non présent autorisé
        if (colFond != null) {
          rouge= getIntAtt(colFond, NM_ROUGE);
          vert= getIntAtt(colFond, NM_VERT);
          bleu= getIntAtt(colFond, NM_BLEU);
          des.setBackground(new Color(rouge, vert, bleu));
        } else
          des.setBackground(null);
        // Couleur de tracé
        Element colTrace= getSousElement(dessin[i], NM_COL_TRACE);
        rouge= getIntAtt(colTrace, NM_ROUGE);
        vert= getIntAtt(colTrace, NM_VERT);
        bleu= getIntAtt(colTrace, NM_BLEU);
        des.setForeground(new Color(rouge, vert, bleu));
        mdlDes_.ajoute(des);
      }
    } catch (SAXException sxe) {
      // Error generated during parsing)
      Exception x= sxe;
      if (sxe.getException() != null)
        x= sxe.getException();
      x.printStackTrace();
    } catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();
    } catch (IOException ioe) {
      // I/O error
      ioe.printStackTrace();
    }
  }
  /**
   * Retourne l'attribut d'élément sous forme de double.
   *
   * @param _el L'élément
   * @param _att L'attribut sous forme de String.
   * @exception SAXException Si l'attribut n'est pas trouvé ou n'est pas un
   *                         double.
   * @return La valeur de l'attribut
   */
  private double getDoubleAtt(Element _el, String _att) throws SAXException {
    double r;
    try {
      r= Double.parseDouble(_el.getAttribute(_att));
    } catch (NumberFormatException _exc) {
      throw new SAXException(
        "Tag "
          + _el.getTagName()
          + ": Attribut "
          + _att
          + " manquant ou invalide");
    }
    return r;
  }
  /**
   * Retourne l'attribut d'élément sous forme d'integer.
   *
   * @param _el L'élément
   * @param _att L'attribut sous forme de String.
   * @exception SAXException Si l'attribut n'est pas trouvé ou n'est pas un
   *                         integer.
   * @return La valeur de l'attribut
   */
  private int getIntAtt(Element _el, String _att) throws SAXException {
    int r;
    try {
      r= Integer.parseInt(_el.getAttribute(_att));
    } catch (NumberFormatException _exc) {
      throw new SAXException(
        "Tag "
          + _el.getTagName()
          + ": Attribut "
          + _att
          + " manquant ou invalide");
    }
    return r;
  }
  /**
   * Retourne l'attribut d'élément sous forme de String.
   *
   * @param _el L'élément
   * @param _att L'attribut sous forme de String.
   * @exception SAXException Si l'attribut n'est pas trouvé ou n'est pas un
   *                         String.
   * @return La valeur de l'attribut
   */
  private String getStringAtt(Element _el, String _att) throws SAXException {
    String r;
    if ((r= _el.getAttribute(_att)).equals(""))
      throw new SAXException(
        "Tag "
          + _el.getTagName()
          + ": Attribut "
          + _att
          + " manquant ou invalide");
    return r;
  }
  /**
   * Retourne le texte pour l'élément donné.
   *
   * @param _el L'élément
   * @exception SAXException Si le texte n'est pas trouvé.
   * @return Le texte.
   */
  private String getTexteValeur(Element _el) throws SAXException {
    NodeList nls= _el.getChildNodes();
    for (int i= 0; i < nls.getLength(); i++) {
      if (nls.item(i) instanceof Text)
        return ((Text)nls.item(i)).getNodeValue();
    }
    throw new SAXException("Tag " + _el.getTagName() + ": Valeur manquante");
  }
  /**
   * Retourne la valeur pour l'élément donné sous forme de int.
   *
   * @param _el L'élément
   * @exception SAXException Si la valeur n'est pas trouvée ou invalide.
   * @return La valeur.
   */
  private int getIntValeur(Element _el) throws SAXException {
    NodeList nls= _el.getChildNodes();
    for (int i= 0; i < nls.getLength(); i++) {
      if (nls.item(i) instanceof Text) {
        try {
          return Integer.parseInt(((Text)nls.item(i)).getNodeValue());
        } catch (NumberFormatException _exc) {
          throw new SAXException(
            "Tag " + _el.getTagName() + ": Valeur invalide");
        }
      }
    }
    throw new SAXException("Tag " + _el.getTagName() + ": Valeur manquante");
  }
  /**
   * Retourne la valeur pour l'élément donné sous forme de double.
   *
   * @param _el L'élément
   * @exception SAXException Si la valeur n'est pas trouvée ou invalide.
   * @return La valeur.
   *//*
  private double getDoubleValeur(Element _el) throws SAXException {
    NodeList nls= _el.getChildNodes();
    for (int i= 0; i < nls.getLength(); i++) {
      if (nls.item(i) instanceof Text) {
        try {
          return Double.parseDouble(((Text)nls.item(i)).getNodeValue());
        } catch (NumberFormatException _exc) {
          throw new SAXException(
            "Tag " + _el.getTagName() + ": Valeur invalide");
        }
      }
    }
    throw new SAXException("Tag " + _el.getTagName() + ": Valeur manquante");
  }*/
  /**
   * Retourne le sous élément de nom donné.
   *
   * @param _el L'élément
   * @param _nom Le nom du sous élément.
   * @exception SAXException Si le sous élément n'est pas trouvé ou si l'élément
   *                         comporte plus d'un sous élément de nom donné.
   * @return Le sous élément
   */
  private Element getSousElement(Element _el, String _nom)
    throws SAXException {
    Element r= null;
    NodeList nls= _el.getChildNodes();
    for (int i= 0; i < nls.getLength(); i++) {
      if (nls.item(i) instanceof Element
        && ((Element)nls.item(i)).getTagName().equals(_nom)) {
        if (r == null)
          r= (Element)nls.item(i);
        else
          throw new SAXException(
            "Tag "
              + _el.getTagName()
              + ": Un seul sous-tag "
              + _nom
              + " attendu");
      }
    }
    if (r == null)
      throw new SAXException(
        "Tag "
          + _el.getTagName()
          + ": Sous-tag "
          + _nom
          + " manquant ou invalide");
    return r;
  }
  /**
   * Retourne les sous-éléments de nom donné.
   *
   * @param _el L'élément
   * @param _nom Le nom des sous-éléments.
   * @return Les sous-éléments
   */
  private Element[] getSousElements(Element _el, String _nom)
    throws SAXException {
    Element[] r;
    Vector vr= new Vector();
    NodeList nls= _el.getChildNodes();
    for (int i= 0; i < nls.getLength(); i++) {
      if (nls.item(i) instanceof Element
        && ((Element)nls.item(i)).getTagName().equals(_nom)) {
        vr.add(nls.item(i));
      }
    }
    r= new Element[vr.size()];
    vr.toArray(r);
    return r;
  }
  /**
   * Enregistrement d'une mise en page.
   *
   * @param _projet  Le projet courant.
   * @param _fichier Nom du fichier de mise en page.
   * @exception FileNotFoundException Le fichier n'est pas trouvé.
   */
  public void enregistrer(RefondeProjet _projet, File _fichier)
    throws IOException {
    ecrire(_projet, _fichier);
//    modifie_= false;
  }

  /**
   * Ecriture des informations sur le fichier associé.
   *
   * @param _projet  Le projet courant.
   * @param _fichier Nom du fichier de mise en page.
   * @exception FileNotFoundException Le fichier n'est pas trouvé.
   */
  private void ecrire(RefondeProjet _projet, File _fc) throws IOException {
    Document document;
    DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder builder= factory.newDocumentBuilder();
      document= builder.newDocument();

      // Table de calque vers leur identifiant.
      Hashtable hcq2IdCq= new Hashtable();
      // Table de calque vers l'identifiant fenetre.
      Hashtable hcq2IdFn= new Hashtable();

      // Mise en page (version=<ver> logiciel="refonde")
      Element root= document.createElement(NM_MEP);
      root.setAttribute(
        NM_VERSION,
        RefondeImplementation.informationsSoftware().version);
      root.setAttribute(NM_LOGICIEL, NM_REFONDE);
      document.appendChild(root);

      // Format
      Element format= document.createElement(NM_FORMAT);
      format.setAttribute(NM_LARGEUR, "" + pageFormat_.getWidth());
      format.setAttribute(NM_HAUTEUR, "" + pageFormat_.getHeight());
      root.appendChild(format);

      // Marges du format
      Element marges= document.createElement(NM_MARGES);
      marges.setAttribute(NM_GAUCHE, "" + pageFormat_.getImageableX());
      marges.setAttribute(NM_DROITE,""+(pageFormat_.getWidth()-
                          pageFormat_.getImageableWidth()-
                          pageFormat_.getImageableX()));
      marges.setAttribute(NM_BAS, "" + pageFormat_.getImageableY());
      marges.setAttribute(NM_HAUT,""+(pageFormat_.getHeight()-
                          pageFormat_.getImageableHeight()-
                          pageFormat_.getImageableY()));
      format.appendChild(marges);

      // Fenetres
      Element fenetres= document.createElement(NM_FENETRES);
      root.appendChild(fenetres);
      {
        Vector vfns= mdlFns_.getObjets();

        for (int i= 0; i < vfns.size(); i++) {
          //          RefondeFillePost fn=(RefondeFillePost)vfns.get(i);
          //          GrPolygone pg=mdlFns_.getGeometrie(fn);
          //          BVueCalque vc=fn.getVueCalque();
          //          GrPolygone pg=mdlFns_.getGeometrie(fn);
          BVueCalque vc= (BVueCalque)vfns.get(i);
          GrPolygone pg= mdlFns_.getGeometrie(vc);

          Element fenetre= document.createElement(NM_FENETRE);
          fenetre.setAttribute(NM_ID, "" + i);

          Element geometrie= document.createElement(NM_GEOMETRIE);
          geometrie.setAttribute(NM_POS_X,""+pg.sommet(0).x_);
          geometrie.setAttribute(NM_POS_Y,""+ pg.sommet(0).y_);
          geometrie.setAttribute(NM_LARGEUR,""+(pg.sommet(2).x_-pg.sommet(0).x_));
          geometrie.setAttribute(NM_HAUTEUR,""+(pg.sommet(2).y_-pg.sommet(0).y_));
          fenetre.appendChild(geometrie);

          GrPoint o=new GrPoint(0, vc.getHeight(), 0).applique(
                                vc.getCalque().getVersReel());
          GrPoint e=new GrPoint(vc.getWidth(), 0, 0).applique(
                                vc.getCalque().getVersReel());

          Element vue= document.createElement(NM_VUE);
          vue.setAttribute(NM_POS_X, "" + o.x_);
          vue.setAttribute(NM_POS_Y, "" + o.y_);
          vue.setAttribute(NM_LARGEUR, "" + (e.x_ - o.x_));
          vue.setAttribute(NM_HAUTEUR, "" + (e.y_ - o.y_));
          fenetre.appendChild(vue);

          BCalque[] cqs= vc.getCalque().getTousCalques();
          int idCalque= 0;

          for (int j= 0; j < cqs.length; j++) {
            if (!cqs[j].isVisible()) continue;
            if (!(cqs[j] instanceof RefondeCalqueCarte)) continue;

            Double valPas;
            if (((RefondeCalqueCarte)cqs[j]).getModeleValeur() instanceof
                RefondeModeleVisuResultats) {
              valPas=((RefondeModeleVisuResultats)
               ((RefondeCalqueCarte)cqs[j]).getModeleValeur()).getSelectedStep();
            }
            else {
              valPas=new Double(0.0);
            }
            BPaletteCouleurPlage pal=((RefondeCalqueCarte)cqs[j]).getPalette();


            Element calque= document.createElement(NM_CALQUE);
            calque.setAttribute(NM_ID,""+idCalque);
            calque.setAttribute(NM_TYPE,""+cqs[j].getTitle());
            calque.setAttribute(NM_PAS,""+valPas);

            Element colFond= document.createElement(NM_COL_FOND);
            colFond.setAttribute(NM_ROUGE,""+cqs[j].getBackground().getRed());
            colFond.setAttribute(NM_VERT,""+cqs[j].getBackground().getGreen());
            colFond.setAttribute(NM_BLEU,""+cqs[j].getBackground().getBlue());
            calque.appendChild(colFond);

            Element colTrace= document.createElement(NM_COL_TRACE);
            colTrace.setAttribute(NM_ROUGE,""+cqs[j].getForeground().getRed());
            colTrace.setAttribute(NM_VERT,""+cqs[j].getForeground().getGreen());
            colTrace.setAttribute(NM_BLEU,""+cqs[j].getForeground().getBlue());
            calque.appendChild(colTrace);

            if (((BCalqueCarte)cqs[j]).getContour()) {
              Element avecCtr= document.createElement(NM_AVEC_CTR);
              calque.appendChild(avecCtr);
            }
            if (((BCalqueCarte)cqs[j]).getIsolignes()) {
              Element avecIsl= document.createElement(NM_AVEC_ISL);
              calque.appendChild(avecIsl);
            }
            if (((BCalqueCarte)cqs[j]).getIsosurfaces()) {
              Element avecIss= document.createElement(NM_AVEC_ISS);
              calque.appendChild(avecIss);
            }
            //            Element ecart=document.createElement(NM_ECART);
            //            ecart.appendChild(document.createTextNode(""+((BCalqueCarte)cqs[j]).getEcart()));
            //            calque.appendChild(ecart);
            //
            //            double bmin=((BCalqueCarte)cqs[j]).getMinValeur();
            //            double bmax=((BCalqueCarte)cqs[j]).getMaxValeur();
            //
            //            if (!Double.isNaN(bmin) || !Double.isNaN(bmax)) {
            //              Element bornes=document.createElement(NM_BORNES);
            //              if (!Double.isNaN(bmin)) bornes.setAttribute(NM_MIN,""+bmin);
            //              if (!Double.isNaN(bmax)) bornes.setAttribute(NM_MAX,""+bmax);
            //              calque.appendChild(bornes);
            //            }

            // Palette
            Element palette= document.createElement(NM_PALETTE);
            Element plages= document.createElement(NM_PLAGES);
            for (int k= 0; k < pal.getNbPlages(); k++) {
              Element plage= document.createElement(NM_PLAGE);
              colTrace= document.createElement(NM_COL_TRACE);
              colTrace.setAttribute(
                NM_ROUGE,
                "" + pal.getCouleurPlage(k).getRed());
              colTrace.setAttribute(
                NM_VERT,
                "" + pal.getCouleurPlage(k).getGreen());
              colTrace.setAttribute(
                NM_BLEU,
                "" + pal.getCouleurPlage(k).getBlue());
              plage.appendChild(colTrace);
              Element bornes= document.createElement(NM_BORNES);
              bornes.setAttribute(NM_MIN, "" + pal.getMinPlage(k));
              bornes.setAttribute(NM_MAX, "" + pal.getMaxPlage(k));
              plage.appendChild(bornes);
              plages.appendChild(plage);
            }
            palette.appendChild(plages);
            //            if (pal.getEspace()) palette.setAttribute(NM_ESPACE,NM_ESPACE_RVB);
            //            else                 palette.setAttribute(NM_ESPACE,NM_ESPACE_TDL);
            //
            //            Element nbPaliers=document.createElement(NM_NB_PALIERS);
            //            nbPaliers.appendChild(document.createTextNode(""+pal.getPaliers()));
            //            palette.appendChild(nbPaliers);
            //
            //            Element nbCycles=document.createElement(NM_NB_CYCLES);
            //            nbCycles.appendChild(document.createTextNode(""+pal.getCycles()));
            //            palette.appendChild(nbCycles);
            //
            //            Element colMin=document.createElement(NM_COL_MIN);
            //            colMin.setAttribute(NM_ROUGE,""+pal.getCouleurMin().getRed());
            //            colMin.setAttribute(NM_VERT ,""+pal.getCouleurMin().getGreen());
            //            colMin.setAttribute(NM_BLEU ,""+pal.getCouleurMin().getBlue());
            //            palette.appendChild(colMin);
            //
            //            Element colMax=document.createElement(NM_COL_MAX);
            //            colMax.setAttribute(NM_ROUGE,""+pal.getCouleurMax().getRed());
            //            colMax.setAttribute(NM_VERT ,""+pal.getCouleurMax().getGreen());
            //            colMax.setAttribute(NM_BLEU ,""+pal.getCouleurMax().getBlue());
            //            palette.appendChild(colMax);
            calque.appendChild(palette);
            fenetre.appendChild(calque);
            hcq2IdCq.put(cqs[j], new Integer(idCalque));
            hcq2IdFn.put(cqs[j], new Integer(i));
            idCalque++;
          }
          fenetres.appendChild(fenetre);
        }
      }

      // Légendes
      Element legendes= document.createElement(NM_LEGENDES);
      root.appendChild(legendes);
      {
        Vector vlgs= mdlLgs_.getObjets();
        int idLg= 0;
        for (int i= 0; i < vlgs.size(); i++) {
          JPanel lg= (JPanel)vlgs.get(i);
          GrPolygone pg= mdlLgs_.getGeometrie(lg);
          BCalqueLegende cqLg= mdlLgs_.getCalqueLegende(lg);
          BCalqueAffichage cq= mdlLgs_.getCalque(lg);
          if (!cq.isVisible() || !cqLg.isVisible())
            continue;
          Element legende= document.createElement(NM_LEGENDE);
          legende.setAttribute(NM_ID, "" + idLg);
          Element geometrie= document.createElement(NM_GEOMETRIE);
          geometrie.setAttribute(NM_POS_X, "" + pg.sommet(0).x_);
          geometrie.setAttribute(NM_POS_Y, "" + pg.sommet(0).y_);
          geometrie.setAttribute(
            NM_LARGEUR,
            "" + (pg.sommet(2).x_ - pg.sommet(0).x_));
          geometrie.setAttribute(
            NM_HAUTEUR,
            "" + (pg.sommet(2).y_ - pg.sommet(0).y_));
          legende.appendChild(geometrie);
          Element colFond= document.createElement(NM_COL_FOND);
          colFond.setAttribute(NM_ROUGE, "" + lg.getBackground().getRed());
          colFond.setAttribute(NM_VERT, "" + lg.getBackground().getGreen());
          colFond.setAttribute(NM_BLEU, "" + lg.getBackground().getBlue());
          legende.appendChild(colFond);
          Element colTrace= document.createElement(NM_COL_TRACE);
          colTrace.setAttribute(NM_ROUGE, "" + lg.getForeground().getRed());
          colTrace.setAttribute(NM_VERT, "" + lg.getForeground().getGreen());
          colTrace.setAttribute(NM_BLEU, "" + lg.getForeground().getBlue());
          legende.appendChild(colTrace);
          Element police= document.createElement(NM_POLICE);
          police.setAttribute(NM_NOM, lg.getFont().getName());
          police.setAttribute(NM_TAILLE, "" + lg.getFont().getSize());
          if (lg.getFont().isPlain())
            police.setAttribute(NM_STYLE, NM_STYLE_N);
          else if (lg.getFont().isBold())
            police.setAttribute(NM_STYLE, NM_STYLE_G);
          else
            police.setAttribute(NM_STYLE, NM_STYLE_I);
          legende.appendChild(police);
          Element idFenetre= document.createElement(NM_ID_FENETRE);
          idFenetre.appendChild(
            document.createTextNode(
              "" + ((Integer)hcq2IdFn.get(cq)).intValue()));
          legende.appendChild(idFenetre);
          Element idCalque= document.createElement(NM_ID_CALQUE);
          idCalque.appendChild(
            document.createTextNode(
              "" + ((Integer)hcq2IdCq.get(cq)).intValue()));
          legende.appendChild(idCalque);
          idLg++;
          legendes.appendChild(legende);
        }
      }
      // Dessins
      Element dessins= document.createElement(NM_DESSINS);
      root.appendChild(dessins);
      {
        Vector vdess= mdlDes_.getObjets();
        for (int i= 0; i < vdess.size(); i++) {
          DeForme des= (DeForme)vdess.get(i);
          Element dessin= document.createElement(NM_DESSIN);
          dessin.setAttribute(NM_ID, "" + i);
          GrPoint[] ptGeom= null;
          // Attention : Test dans le sens inverse de l'arborescence des classes
          // En effet, un carre est instanceof d'un rectangle, etc.
          if (des instanceof DeTrait) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_TR);
            ptGeom=
              new GrPoint[] {
                ((GrSegment)des.getGeometrie()).o_,
                ((GrSegment)des.getGeometrie()).e_ };
          } else if (des instanceof DeTexte) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_TX);
            ptGeom= new GrPoint[] {(GrPoint)des.getGeometrie()};
            Element texte= document.createElement(NM_TEXTE);
            texte.appendChild(
              document.createTextNode(((DeTexte)des).getText()));
            dessin.appendChild(texte);
            Element police= document.createElement(NM_POLICE);
            police.setAttribute(NM_NOM, ((DeTexte)des).getFont().getName());
            police.setAttribute(
              NM_TAILLE,
              "" + ((DeTexte)des).getFont().getSize());
            if (((DeTexte)des).getFont().isPlain())
              police.setAttribute(NM_STYLE, NM_STYLE_N);
            else if (((DeTexte)des).getFont().isBold())
              police.setAttribute(NM_STYLE, NM_STYLE_G);
            else
              police.setAttribute(NM_STYLE, NM_STYLE_I);
            dessin.appendChild(police);
          } else if (des instanceof DeCarre) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_CA);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeRectangle) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_RE);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeCourbeFermee) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_CF);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DePolygone) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_PG);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeMainLevee) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_CO);
            ptGeom= ((GrPolyligne)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeLigneBrisee) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_PL);
            ptGeom= ((GrPolyligne)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeCercle) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_CE);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          } else if (des instanceof DeEllipse) {
            dessin.setAttribute(NM_TYPE, NM_TYPE_EL);
            ptGeom= ((GrPolygone)des.getGeometrie()).sommets_.tableau();
          }
          // La couleur de fond peut être vide.
          if (des.getBackground() != null) {
            Element colFond= document.createElement(NM_COL_FOND);
            colFond.setAttribute(NM_ROUGE, "" + des.getBackground().getRed());
            colFond.setAttribute(NM_VERT, "" + des.getBackground().getGreen());
            colFond.setAttribute(NM_BLEU, "" + des.getBackground().getBlue());
            dessin.appendChild(colFond);
          }
          Element colTrace= document.createElement(NM_COL_TRACE);
          colTrace.setAttribute(NM_ROUGE, "" + des.getForeground().getRed());
          colTrace.setAttribute(NM_VERT, "" + des.getForeground().getGreen());
          colTrace.setAttribute(NM_BLEU, "" + des.getForeground().getBlue());
          dessin.appendChild(colTrace);
          Element geometrie= document.createElement(NM_GEOMETRIE);
          for (int j= 0; j < ptGeom.length; j++) {
            Element point= document.createElement(NM_POINT);
            point.setAttribute(NM_POS_X, "" + ptGeom[j].x_);
            point.setAttribute(NM_POS_Y, "" + ptGeom[j].y_);
            geometrie.appendChild(point);
          }
          dessin.appendChild(geometrie);
          dessins.appendChild(dessin);
        }
      }
      document.normalize();
      // Stockage sur fichier
      // Use a Transformer for output
      TransformerFactory tFactory= TransformerFactory.newInstance();
      Transformer transformer= tFactory.newTransformer();
      DOMSource source= new DOMSource(document);
      StreamResult result= new StreamResult(new FileWriter(_fc));
      transformer.transform(source, result);
    } catch (TransformerConfigurationException tce) {
      // Error generated by the parser
      System.out.println("\n** Transformer Factory error");
      System.out.println("   " + tce.getMessage());
      // Use the contained exception, if any
      Throwable x= tce;
      if (tce.getException() != null)
        x= tce.getException();
      x.printStackTrace();
    } catch (TransformerException te) {
      // Error generated by the parser
      System.out.println("\n** Transformation error");
      System.out.println("   " + te.getMessage());
      // Use the contained exception, if any
      Throwable x= te;
      if (te.getException() != null)
        x= te.getException();
      x.printStackTrace();
    }
    //    catch (SAXException sxe) {
    //      // Error generated by this application
    //      // (or a parser-initialization error)
    //      Exception  x = sxe;
    //      if (sxe.getException() != null)
    //        x = sxe.getException();
    //      x.printStackTrace();
    //    }
    catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();
    }
    //    catch (IOException ioe) {
    //      // I/O error
    //      ioe.printStackTrace();
    //    }
  }
  /**
   * Initialisation depuis les fenetres courantes et le format d'impression.
   * @param _pf Le format de pages en mm.
   */
  public void initialise(RefondeImplementation _imp, PageFormat _pf) {
    //    DeRectangle r;
    // Conversion des dimensions en 1/72 inches en mm.
    //    pageFormat_=(PageFormat)_pf.clone();
    //    Paper pp=_pf.getPaper();
    //    Paper ppmm=new Paper();
    //    ppmm.setSize(pp.getWidth() *ImpToMM,
    //                 pp.getHeight()*ImpToMM);
    //
    //    ppmm.setImageableArea(pp.getImageableX()     *ImpToMM,
    //                          pp.getImageableY()     *ImpToMM,
    //                          pp.getImageableWidth() *ImpToMM,
    //                          pp.getImageableHeight()*ImpToMM);
    //    pageFormat_.setPaper(ppmm);
    pageFormat_= _pf;
    // Initialisation des modèles
    vide();
    mdlFns_.setPageFormat(pageFormat_);
    mdlLgs_.setPageFormat(pageFormat_);
    // Affectation du modèle de dessin
    //    cqDes_.setModele(_prj.getMiseEnPage().getModeleDessin());
    // Affectation du modèle de fenetres
    //    RefondeModeleFenetres mdlFns=_prj.getMiseEnPage().getModeleFenetres();
    //    mdlFns_.setPageFormat(pgmm_);
    //    cqFns_.setModele(mdlFns);
    // Affectation du modèle de légendes
    //    RefondeModeleLegendes mdlLgs=_prj.getMiseEnPage().getModeleLegendes();
    //    mdlLgs_.setPageFormat(pgmm_);
    //    cqLgs_.setModele(mdlLgs);
    // Morphisme de transformation ecran -> Mise en page
    JDesktopPane dsk= _imp.getMainPanel().getDesktop();
    GrMorphisme m= RefondeMiseEnPageHelper.convertEcran2MEP(_pf, dsk);
    // Création des fenêtres et des légendes
    JInternalFrame[] frames= _imp.getAllInternalFrames();
    for (int i= frames.length - 1; i >= 0; i--) {
      if (frames[i] instanceof RefondeFillePost) {
        RefondeFillePost fnPost= (RefondeFillePost)frames[i];
        BVueCalque vc= fnPost.getVueCalque();
        GrPoint p1=
          new GrPoint(
            fnPost.getX(),
            fnPost.getY() + vc.getHeight(),
            0).applique(
            m);
        GrPoint p2=
          new GrPoint(
            fnPost.getX() + vc.getWidth(),
            fnPost.getY(),
            0).applique(
            m);
        mdlFns_.ajoute(vc, p1.x_, p1.y_, p2.x_ - p1.x_, p2.y_ - p1.y_);
        BCalque[] cqs= fnPost.getVueCalque().getCalque().getTousCalques();
        for (int j= 0; j < cqs.length; j++) {
          if (cqs[j] instanceof BCalqueCarte) {
            BCalqueLegende cqLg= ((BCalqueCarte)cqs[j]).getLegende();
            if (cqLg != null) {
              JPanel pnLg= cqLg.getLegende((BCalqueCarte)cqs[j]);
              if (pnLg != null)
                mdlLgs_.ajoute(
                  pnLg,
                  cqLg,
                  (BCalqueCarte)cqs[j],
                  p1.x_,
                  p1.y_,
                  pnLg.getWidth() / 4,
                  pnLg.getHeight() / 4);
              //              if (pnLg!=null) mdlLgs_.ajoute(pnLg,cqLg,(BCalqueCarte)cqs[j]);
            }
          }
        }
      }
    }
  }
}
