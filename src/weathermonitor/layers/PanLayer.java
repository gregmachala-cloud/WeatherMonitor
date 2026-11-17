package weathermonitor.layers;


import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class PanLayer extends JPanel {
   BufferedImage img;
   int x = 0;
   int y = 0;
    int offsetX = 0;
    int offsetY = 0;
    
   public PanLayer() {
       setVisible(false);
   }
   public void setPanImage(BufferedImage panImg) {
         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
         this.img = gc.createCompatibleImage(panImg.getWidth(),panImg.getHeight(), Transparency.TRANSLUCENT);
         Graphics g = img.getGraphics();
         g.drawImage(panImg,0,0,this);
         g.dispose();
       this.setSize(panImg.getWidth(),panImg.getHeight());
      
   }
   public void flush() {
       img.flush();
   }
//   public void setVisible(boolean value) {
//      
////       if (img != null && value == false) img.flush();
////       this.img = null;
//       super.setVisible(value);
//   }
   public void setXY(Point p) {
       this.x = p.x;
       this.y = p.y;
       java.awt.EventQueue.invokeLater(() -> repaint());
       //repaint();
   }
   public void setOffset(Point p) {
       this.offsetX = p.x;
       this.offsetY = p.y;
   }
   @Override
    public void paint (Graphics g) {
       // super.paint(g);
       // System.out.println("Paint pan img x:"+x+"y="+y);
        g.drawImage(img,x-offsetX,y-offsetY,this);
    }
}
