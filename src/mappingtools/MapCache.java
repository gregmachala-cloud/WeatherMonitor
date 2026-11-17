/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mappingtools;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import weathermonitor.dataobjects.MetarLocationObject;

/**
 *
 * @author greg
 */
public class MapCache {
    private int zoomLevel;
    private ArrayList<String> stateIds;
    private BufferedImage img = null;
    private BufferedImage mapBgImage = null;
    private BufferedImage panMapImage = null;
    private Hashtable<String, GeneralPath> countyHash = new Hashtable<String, GeneralPath>();
    private Hashtable<String, GeneralPath> roadHash = new Hashtable<String, GeneralPath>();
     private Hashtable<String, GeneralPath> stateHash = new Hashtable<String, GeneralPath>();
    private ArrayList<MetarLocationObject> metarStationList;

    public MapCache(int zoomLevel) {
        
        this.zoomLevel = zoomLevel;
    }
    public void flush() {
        if (img != null) img.flush();
        if (mapBgImage != null) flush();
        if (panMapImage != null) flush();
        if (stateIds != null) stateIds.clear();
         countyHash.clear();
        roadHash.clear();
        if (metarStationList != null) metarStationList.clear();
    }
    public void setStateIds(ArrayList<String> ids) {
        this.stateIds = ids;
    }
    public ArrayList<String> getStateIds() {
        return stateIds;
    }
    public void setMetarStationList(ArrayList<MetarLocationObject> list) {
        this.metarStationList = list;
    }
    public ArrayList<MetarLocationObject> getMetarStationList() {
        return this.metarStationList;
    }
    public void setCachedImage(BufferedImage mapImg) {
        img = new BufferedImage(mapImg.getWidth(),mapImg.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.drawImage(mapImg,0,0,null);
        g.dispose();
        
    }

    public void setCountyPathHash(Hashtable<String, GeneralPath> pathHash) {
        this.countyHash = pathHash;
    }

    public void setRoadPathHash(Hashtable<String, GeneralPath> pathHash) {
        this.roadHash = pathHash;
    }
    
    public void setStatePathHash(Hashtable<String, GeneralPath> pathHash) {
        this.stateHash = pathHash;
    }

    public BufferedImage getImage() {
        return img;
    }
    public BufferedImage getPanMapImage() {
        return this.panMapImage;
    }
    public void setPanMapImage(BufferedImage img) {
        this.panMapImage = img;
    }
    public BufferedImage getMapBgImage() {
        return this.mapBgImage;
    }
    public void setMapBgImage(BufferedImage img) {
        this.mapBgImage = img;
    }
    
     public String getCountyNameAtPoint(int x, int y) {
         //System.out.println("County hash has size:"+countyHash.size());
            Enumeration en = countyHash.keys();
            Point2D.Double point = new Point2D.Double(x,y);
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                //System.out.println("Tesing county "+key);
                GeneralPath testPath = countyHash.get(key);
              
                if (testPath.contains(point)) {
                    String[] arr = key.split(":");
                    
                    return arr[0].trim();
                }
            }
            return "";
        }
     
      public String getStateNameAtPoint(int x, int y) {
         //System.out.println("County hash has size:"+countyHash.size());
            Enumeration en = stateHash.keys();
            Point2D.Double point = new Point2D.Double(x,y);
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                //System.out.println("Tesing county contains "+point.toString()+" "+key);
                GeneralPath testPath = stateHash.get(key);
              
                if (testPath.contains(point)) {
                     String[] arr = key.split(":");
                    
                    return arr[0].trim();
                }
            }
            return "";
        }
     
     public String getRoadNameInRectangle(Rectangle rect) {
            Enumeration en = roadHash.keys();
            String v = "";
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                //System.out.println("Test if "+key+" intersects "+rect.toString());
                GeneralPath testPath = roadHash.get(key);
                //System.out.println("bounds="+testPath.getBounds());
        
                if (testPath.intersects(rect)) {
                    String[] thing = key.split(":");
                    //System.out.println("Got road name "+thing[0]);
                    v = thing[0].trim();
                    
                }
                
            }
            return v;
        }
}
