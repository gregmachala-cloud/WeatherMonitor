/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

import weathermonitor.settings.WeatherMonitorSettings;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 *
 * @author greg
 */
public class WindBarb {
    private static Image barbImg;
    private Font font = new Font("Monospaced",Font.BOLD,16);
    //private ImageIcon weatherIcon = null;
    private int forWhatWindSpeeds = WeatherMonitorSettings.ALL_WIND_SPEEDS;
    private boolean showWindSpeedsOnArrows = true;
    
    public  WindBarb(boolean showWindSpeedsOnBarbs, int how) {
        this.showWindSpeedsOnArrows = showWindSpeedsOnArrows;
        this.forWhatWindSpeeds = how;
    }
    private Point getWindSpeedLocation(int deg) {
        
        if (deg >= 180 && deg <=270) {
            //plt in q2
           int alpha = deg-180;
           double radians = ((2*3.14)/360.0)*alpha;
           int x = (int) (16*Math.cos(radians));
           int y = (int) (16*Math.sin(radians));
           int deltaX = 32+x-11;
           int deltaY = 32+y-11;
           System.out.println("deg="+deg+" x="+deltaX+" y="+deltaY);
           Point p = new Point(deltaX, deltaY);
           return p;
        } else if (deg >= 270 && deg <= 360) {
           //plot in q3
             int alpha = 360-deg;
           double radians = ((2*3.14)/360.0)*alpha;
           int x = (int) (16*Math.cos(radians));
           int y = (int) (16*Math.sin(radians));
           int deltaX = 32-x-11;
           int deltaY = 32+y-11;
           Point p = new Point(deltaX, deltaY);
           return p;
        } else if (deg >=0 && deg <=90) {
            //plot 4
           int alpha = deg;
           double radians = ((2*3.14)/360.0)*alpha;
           int x = (int) (16*Math.cos(radians));
           int y = (int) (16*Math.sin(radians));
           int deltaX = 32-x-11;
           int deltaY = 32-y-11;
           Point p = new Point(deltaX, deltaY);
           return p;
           
        } else if (deg >= 90 && deg <=180) {
            //plot in q1
             int alpha = 180-deg;
             double radians = ((2*3.14)/360.0)*alpha;
           int x = (int) (16*Math.cos(radians));
           int y = (int) (16*Math.sin(radians));
           int deltaX = 32+x-11;
           int deltaY = 32-y-11;
           Point p = new Point(deltaX, deltaY);
           return p;
        }
        return new Point(0,0);
    }
    public Image getWindBarb(int speed, int deg) {
        String barb = "arrow.png";
        if (speed >= 45) {
            barb = "arrow_strong.png";
        } else if (speed >= 20) {
            barb = "arrow_moderate.png";
        }
        try {
        ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+barb)); 
        barbImg = icon.getImage();
        } catch (Exception ex) {
            System.out.println("CANNOT GET WIND BARB FOR "+barb);
        }
//        if (wxIcon != null) {
//            weatherIcon = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+wxIcon));
//        } else {
//            weatherIcon = null;
//        }
        BufferedImage img = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        if (speed > 0) g2d.drawImage(barbImg, 0,0,null);
         g2d.dispose();
       // Image n = img;
        if (speed > 0 && speed < 20) {
            img =  colorizeBarb(img, Color.gray);
        } else if (speed >= 20 && speed < 35) {
             img =  colorizeBarb(img, new Color(0,132,250));
           
        } else if (speed >= 35 && speed < 45) {
           img = colorizeBarb(img, new Color(220,0,0));
        } else if (speed >= 45 && speed < 58) {
           img = colorizeBarb(img, new Color(220,0,0));
        } else if (speed >= 58) {
            img = colorizeBarb(img, new Color(220,0,250));
        }
        
        if (img != null && speed > 0) {
            img =  rotateImage(img, deg);
        } 
        if (speed > 0 && showWindSpeedsOnArrows == true) {
            boolean okToPlot = false;
            if (forWhatWindSpeeds == WeatherMonitorSettings.ALL_WIND_SPEEDS) {
                okToPlot = true;
            } else if (forWhatWindSpeeds == WeatherMonitorSettings.WIND_SPEEDS_GT_10 && speed >= 10) {
                okToPlot = true;
            } else if (forWhatWindSpeeds == WeatherMonitorSettings.WIND_SPEEDS_GT_20 && speed >= 20) {
                okToPlot = true;
            }
            if (okToPlot == true) {
                g2d = img.createGraphics();
                g2d.setFont(font);
                
                Point p = getWindSpeedLocation(deg);
                g2d.setColor(Color.WHITE);
                g2d.drawString(Integer.toString(speed), p.x - 6-1, p.y + 10+1);
                g2d.drawString(Integer.toString(speed), p.x - 6-1, p.y + 10-1);
                g2d.drawString(Integer.toString(speed), p.x - 6+1, p.y + 10-1);
                g2d.drawString(Integer.toString(speed), p.x - 6+1, p.y + 10+1);
               g2d.setColor(Color.BLACK);
               g2d.drawString(Integer.toString(speed), p.x - 6, p.y + 10);
                g2d.dispose();
            }
        }
//        if (weatherIcon != null) {
//            g2d = img.createGraphics();
//            Point p = new Point(32,16);
//            if (speed > 0) {
//                 p = getWeatherIconLocation(deg);
//            }
//            g2d.drawImage(weatherIcon.getImage(),p.x,p.y, null);
//            g2d.dispose();
//        }
        return img;
    }
    
    public BufferedImage colorizeBarb(BufferedImage img, Color c) {
         int w = img.getWidth(null);
        int h = img.getHeight(null);
        int black = Color.BLACK.getRGB();
        int newRgb = c.getRGB();
        BufferedImage buf = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buf.createGraphics();
        g2d.drawImage(img,0,0,null);
        g2d.dispose();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int rgb = buf.getRGB(i, j);
                if (rgb == black) {
                         buf.setRGB(i, j, newRgb);
                }
            }
        }
        return buf;
    }
    
    public BufferedImage rotateImage(Image img, int deg) {
        double theta = ((2*3.14)/360.0)*deg;
        //double theta = 1.57;
        //System.out.println("rotate "+theta+" radians");
        int w = 50;
        int h = 50;
        BufferedImage rotatedImage = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
       
        AffineTransform rotationTransform = new AffineTransform();
        rotationTransform.translate(25,25);
        rotationTransform.rotate(theta);
        rotationTransform.translate(-25,-25);
        
        g2d.setTransform(rotationTransform);
        g2d.drawImage(img, 0, 0, null);
        
        
        g2d.dispose(); 
         
       
        return rotatedImage;
    }
}
