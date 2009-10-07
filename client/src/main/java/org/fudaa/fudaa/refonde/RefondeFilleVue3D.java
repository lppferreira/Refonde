/*
 * @file         RefondeFilleVue3D.java
 * @creation     2000-02-18
 * @modification $Date: 2007-01-19 13:14:14 $
 * @license      GNU General Public License 2
 * @copyright    (c)1998-2001 CETMEF 2 bd Gambetta F-60231 Compiegne
 * @mail         devel@fudaa.org
 */
package org.fudaa.fudaa.refonde;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.memoire.bu.BuDesktop;
import com.memoire.bu.BuInformationsDocument;
import com.memoire.bu.BuInternalFrame;
import com.memoire.bu.BuMenu;
import com.memoire.bu.BuPopupButton;
import com.memoire.bu.BuPrintable;
import com.memoire.bu.BuPrinter;
import com.memoire.bu.BuResource;
import com.memoire.bu.BuVerticalLayout;

import org.fudaa.ebli.palette.BPaletteSelecteurReduitCouleur;
import org.fudaa.ebli.ressource.EbliResource;
import org.fudaa.ebli.volume.BCartouche;
import org.fudaa.ebli.volume.BGroupeStandard;
import org.fudaa.ebli.volume.BImportVolume;
import org.fudaa.ebli.volume.BUnivers;
import org.fudaa.ebli.volume.controles.BArbreVolume;
import org.fudaa.ebli.volume.controles.BControleEchelle;
import org.fudaa.ebli.volume.controles.BControleLumiere;
import org.fudaa.ebli.volume.controles.BControleVolume;
import org.fudaa.ebli.volume.controles.BPas;
import org.fudaa.ebli.volume.controles.BSelecteurTexture;
import org.fudaa.ebli.volume.controles.BUniversInteraction;
import org.fudaa.ebli.volume.controles.BVolumeTransformation;
import org.fudaa.ebli.volume.controles.TransformTypeIn;

import org.fudaa.fudaa.commun.FudaaLib;

/**
 * Une fenetre pour la visualisation des résultats en 3D.
 *
 * @version $Id: RefondeFilleVue3D.java,v 1.11 2007-01-19 13:14:14 deniger Exp $
 * @author Christophe Delhorbe
 */
public class RefondeFilleVue3D extends BuInternalFrame implements InternalFrameListener, WindowListener, BuPrintable {
  private BUnivers u_;
  private BArbreVolume av_;
  private BuMenu mnVolumes;
  private BuPopupButton pbPalette;
  private BuPopupButton pbTexture;
  private BuPopupButton pbLumiere;
  private BuPopupButton pbEchelle;
  private BuPopupButton pbTransfo;
  private BuPopupButton pbImport;
  private MyFrame frame;
  // private Transform3D[] t;
  private BGroupeStandard gs;
  private boolean hide = false;
  private BuInformationsDocument idRefonde_;
  BCartouche cart;
  private BGroupeStandard objets2d;
  private BControleLumiere controle_lumiere;
  private BControleEchelle controle_echelle;
  private BVolumeTransformation transfo_volume;
  private BImportVolume import_volume;
  boolean anim = false;

  public BUnivers getUnivers() {
    return u_;
  }

  public BArbreVolume getArbreVolume() {
    return av_;
  }

  public void setArbreVolume(BArbreVolume _av) {
    av_ = _av;
    controle_lumiere.addPropertyChangeListener(av_);
  }

  public RefondeFilleVue3D(BuInformationsDocument _idRefonde) {
    super("Vue 3D", true, false, true, true);
    addInternalFrameListener(this);
    av_ = new BArbreVolume();
    mnVolumes = av_.buildNormalMenu();
    idRefonde_ = _idRefonde;
    build();
  }

  public void build() {
    frame = new MyFrame("Vue 3D");
    u_ = new BUnivers();
    u_.addPropertyChangeListener(frame);
    frame.addPropertyChangeListener(av_);
    // Panel Sud : controle deplacement
    JPanel jp = new JPanel();
    jp.setLayout(new FlowLayout());
    BUniversInteraction uiInteraction = new BUniversInteraction(u_);
    JButton init = new JButton("Init");
    init.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent _e) {
        u_.init();

      }

    });
    BControleVolume cv = new BControleVolume(u_);
    cv.addRepereEventListener(uiInteraction);
    cv.addPropertyChangeListener(uiInteraction);
    BPas coef = new BPas(cv);
    // BPosition pos=new BPosition();
    // u_.addPropertyChangeListener("position",pos);
    TransformTypeIn transf = new TransformTypeIn();
    transf.addRepereEventListener(uiInteraction);
    transf.addItemListener(uiInteraction);
    u_.addPropertyChangeListener("position", transf);
    JCheckBox orbital = new JCheckBox("Orbital");
    orbital.addItemListener(uiInteraction);
    orbital.setSelected(false);
    JPanel boutons = new JPanel();
    boutons.setLayout(new BuVerticalLayout());
    boutons.add(init);
    boutons.add(orbital);
    cart = new BCartouche();
    cv.addPropertyChangeListener(cart);
    cart.setName("Cartouche");
    cart.setFont(new Font("SansSerif", Font.PLAIN, 10));
    cart.setInformations(idRefonde_);
    cart.setForeground(Color.black);
    cart.setBackground(new Color(255, 255, 224));
    cart.setVisible(false);
    objets2d = new BGroupeStandard();
    objets2d.setName("2D");
    objets2d.add(cart);
    // jp.add(pos);
    jp.add(transf);
    jp.add(cv);
    // jp.add(coef);
    jp.add(boutons);
    jp.add(cart);
    BPaletteSelecteurReduitCouleur palette = new BPaletteSelecteurReduitCouleur();
    palette.addPropertyChangeListener(av_);
    pbPalette = new BuPopupButton(FudaaLib.getS("Palette"), palette);
    pbPalette.setToolTipText(FudaaLib.getS("Couleurs du volume"));
    pbPalette.setIcon(EbliResource.EBLI.getIcon("palettecouleur"));
    BSelecteurTexture stex = new BSelecteurTexture();
    stex.addPropertyChangeListener(av_);
    pbTexture = new BuPopupButton(FudaaLib.getS("Texture"), stex);
    pbTexture.setToolTipText(FudaaLib.getS("Texture du volume"));
    pbTexture.setIcon(EbliResource.EBLI.getIcon("texture"));
    controle_lumiere = new BControleLumiere();
    controle_lumiere.addPropertyChangeListener(av_);
    pbLumiere = new BuPopupButton(FudaaLib.getS("Lumiere"), controle_lumiere);
    pbLumiere.setToolTipText(FudaaLib.getS("Réglage de la lumiere"));
    pbLumiere.setIcon(EbliResource.EBLI.getIcon("lumiere"));
    controle_echelle = new BControleEchelle();
    controle_echelle.addPropertyChangeListener(av_);
    pbEchelle = new BuPopupButton(FudaaLib.getS("Echelle"), controle_echelle);
    pbEchelle.setToolTipText(FudaaLib.getS("Deformation suivant les Z"));
    pbEchelle.setIcon(EbliResource.EBLI.getIcon("echellez"));
    transfo_volume = new BVolumeTransformation();
    transfo_volume.addPropertyChangeListener(av_);
    pbTransfo = new BuPopupButton(FudaaLib.getS("Transfo"), transfo_volume);
    pbTransfo.setToolTipText(FudaaLib.getS("Transformation"));
    pbTransfo.setIcon(EbliResource.EBLI.getIcon("transform"));
    import_volume = new BImportVolume();
    import_volume.addPropertyChangeListener(av_);
    pbImport = new BuPopupButton(FudaaLib.getS("Import"), import_volume);
    pbImport.setToolTipText(FudaaLib.getS("Importer un Objet VRML"));
    pbImport.setIcon(BuResource.BU.getIcon("importer"));
    // JButton animbu=new JButton("Anim");
    // anim.addPropertyChangeListener(av_);
    // animbu.addActionListener(new AL());
    JPanel est = new JPanel(new BuVerticalLayout());
    est.add(av_);
    // est.add(animbu);
    // est.add(controle_echelle);
    JComponent content_ = (JComponent) frame.getContentPane();
    content_.setLayout(new BorderLayout());
    content_.add("Center", u_.getCanvas3D());
    content_.add("South", jp);
    content_.add("East", est);
    if (gs != null) u_.setRoot(gs);
    /*
     * if (t != null) u_.setUniversTransforms(t);
     */
    u_.getCanvas3D().freeze(false);
    setVisible(false);
    frame.addWindowListener(this);
    frame.setSize(new Dimension(600, 600));
    frame.setVisible(true);
  }

  // BuInternalFrame
  public void cache() {
    hide = true;
    frame.setVisible(false);
  }

  public void montre() {
    frame.setVisible(true);
  }

  public JMenu[] getSpecificMenus() {
    JMenu[] r = new JMenu[1];
    r[0] = mnVolumes;
    return r;
  }

  public JComponent[] getSpecificTools() {
    pbPalette.setDesktop((BuDesktop) getDesktopPane());
    pbTexture.setDesktop((BuDesktop) getDesktopPane());
    pbLumiere.setDesktop((BuDesktop) getDesktopPane());
    pbEchelle.setDesktop((BuDesktop) getDesktopPane());
    pbTransfo.setDesktop((BuDesktop) getDesktopPane());
    pbImport.setDesktop((BuDesktop) getDesktopPane());
    JComponent[] r = new JComponent[6];
    r[0] = pbPalette;
    r[1] = pbTexture;
    r[2] = pbLumiere;
    r[3] = pbEchelle;
    r[4] = pbTransfo;
    r[5] = pbImport;
    return r;
  }

  public String[] getEnabledActions() {
    String[] r = new String[] { "IMPRIMER" };
    return r;
  }

  public void setRoot(BGroupeStandard _root) {
    gs = _root;
    gs.add(objets2d);
    u_.setRoot(gs);
    av_.setVolume(u_.getRoot());
  }

  // InternalFrameListener
  public void internalFrameActivated(InternalFrameEvent e) {}

  public void internalFrameClosed(InternalFrameEvent e) {}

  public void internalFrameClosing(InternalFrameEvent e) {}

  public void internalFrameDeactivated(InternalFrameEvent e) {}

  public void internalFrameDeiconified(InternalFrameEvent e) {}

  public void internalFrameIconified(InternalFrameEvent e) {}

  public void internalFrameOpened(InternalFrameEvent e) {}

  // WindowListener
  public void windowActivated(WindowEvent e) {
    try {
      setSelected(true);
    } catch (PropertyVetoException ex) {}
  }

  public void windowClosed(WindowEvent e) {}

  public void windowClosing(WindowEvent e) {
    cache();
    u_.animate(false);
    try {
      setClosed(true);
    } catch (PropertyVetoException ex) {}
  }

  public void windowDeactivated(WindowEvent e) {
    if (!hide ) frame.toFront();
  }

  public void windowDeiconified(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowOpened(WindowEvent e) {}

  // BuPrintable
  public void print(PrintJob _job, Graphics _g) {
    BuPrinter.INFO_DOC = new BuInformationsDocument();
    BuPrinter.INFO_DOC.name = getTitle();
    BuPrinter.INFO_DOC.logo = BuResource.BU.getIcon("calque");
    u_.setBackground(Color.white);
    /*
     * try{ Thread.currentThread().sleep(1000); } catch(InterruptedException ex) {System.out.println(ex);}
     */
    Image i = u_.getCanvas3D().print();
    BuPrinter.printImage(_job, _g, i);
    u_.setBackground(Color.black);
  }
  class MyFrame extends JFrame implements PropertyChangeListener {
    public MyFrame(String s) {
      super(s);
    }
    int indice = 0;

    synchronized public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName() == "swap") {
        Graphics g = getUnivers().getCanvas3D().getGraphics();
        if (cart != null) {
          Rectangle rect = getUnivers().getCanvas3D().getBounds();
          Dimension size = cart.getPreferredSize();
          if (size.equals(new Dimension(0, 0))) size = new Dimension(300, 150);
          int x = rect.x + rect.width - size.width;
          int y = rect.y + rect.height - size.height;
          cart.setLocation(x, y);
          cart.paint(g);
        }
        /*
         * if (anim==true) { System.out.println ("Anim frame "+indice); this.firePropertyChange("temps",new
         * Long((indice-1)),new Long(indice)); indice++; if (indice==200) indice=0; }
         */
      }
    }
  }
  class AL implements ActionListener {
    public double t = 0;

    public void actionPerformed(ActionEvent e) {
      anim = !anim;
      getUnivers().animate(anim);
      // ((JButton)e.getSource()).firePropertyChange("temps",0d,0.1d);
      // u_.startRenderer();
    }
  }
}
