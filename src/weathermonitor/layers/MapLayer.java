/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.layers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import mappingtools.BboxListener;
import mappingtools.Mapping;

/**
 *
 * @author greg
 */
public class MapLayer extends JPanel implements BboxListener {
    private BufferedImage mapImage;
    private final Mapping map;
    private int zoomLevel = 2;
    private double lat;
    private double lon;

    private final double id = Math.random();
 
    
    public MapLayer (MetarLayer metarLayer, int zoomLevel, int w, int h, double centerLat, double centerLon, Properties props) {
        
        this.setLayout(new GridLayout());
        this.setOpaque(false);
        this.setSize(w, h);
        this.lat = centerLat;
        this.lon = centerLon;
        this.zoomLevel = zoomLevel;
       
        
        
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        mapImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        this.map = Mapping.getInstance();
        map.initMapping(mapImage,metarLayer, zoomLevel, lat, lon, w, h,props);
        map.addBboxListener(this);
       
    }
     public void recenterMap(int zoomLevel, double newLat, double newLon) {
      
        this.zoomLevel = zoomLevel;
        this.lon = newLon;
        this.lat = newLat;
        //mapImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        map.recenterMap(newLat,newLon);
          map.resizeMap(mapImage, getWidth(), getHeight(), lat, lon);
        
    }
    public void recenterMap(double newLat, double newLon,boolean useNearestCityForLocation) {
      
        this.lon = newLon;
        this.lat = newLat;
        //mapImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        map.recenterMap(newLat,newLon);
          map.resizeMap(mapImage, getWidth(), getHeight(), lat, lon);
           if (mapImage != null) mapImage.flush();
           mapImage = map.getMapImage();
         
    }
    public Color getMapBgColor()  {
        return map.getMapBgColor();
    }
    public int getZoomLevel() {
        return zoomLevel;
    }
    public void setZoomLevel(int zoomLevel) {
        //System.out.println("SetZoomLevel in maplayer to "+zoomLevel);
        //setReScalingMap(true);
       
        this.zoomLevel = zoomLevel;
        map.setZoomLevel(zoomLevel);
        //setReScalingMap(false);
       
    }
    public Mapping getMap() {
        return map;
    }
    @Override
    public void paint(Graphics g) {
       // System.out.println("Painting in map layer");
      
            if (mapImage != null) {
                // System.out.println("MapImage is not  null");
                 g.drawImage(mapImage, 0,0, this);
            } else {
                System.out.println("mapImage is null");
            }
        
        
    }
    private BufferedImage smoothImage(BufferedImage img) {
         float[] filter = new float[]{
                0.0f / 121.0f, 1.0f / 121.0f, 0.0f / 121.0f,
                1.0f / 121.0f, 50.0f / 121.0f, 1.0f / 121.0f,
                0.0f / 121.0f, 1.0f / 121.0f,0.0f / 121.0f
                };

            Kernel kernel = new Kernel(3, 3, filter);
            ConvolveOp cop = new ConvolveOp(kernel);
            return cop.filter(img, null);
    }
    

    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        this.lat = lat; this.lon = lon;
       
        
        repaint();
       
    }


    @Override
    public double getID() {
        return id;
        
    }

}
