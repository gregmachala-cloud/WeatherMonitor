/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.dataobjects;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author greg
 */
public class RadarImageObject {
    private double lat;
    private double lon;
    private BufferedImage img;
    private String timeStamp = "";
    private int id = 0;
    private double[] bbox;
    private Point2D centerPoint;
    private long epochSecs;
    
    public RadarImageObject(int id,Point2D centerPoint, Image radarImg, String timestamp, long epoch, double[] bbox) {
        this.timeStamp = timestamp;
        this.centerPoint = centerPoint;
        this.epochSecs = epoch;
        this.id = id;
        int w = radarImg.getWidth(null);
        int h = radarImg.getHeight(null);
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        //img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.drawImage(radarImg,0,0,null);
        //g.drawImage(radarImg.getScaledInstance(w, h, Image.SCALE_FAST), 0, 0, null);
        g.dispose();
        
        this.bbox = bbox;
    }
    public void flush() {
        img.flush();
    }
    public Point2D getCenterPoint() {
        return centerPoint;
    }
    public int getId() {
        return id;
    }
    public long getEpochSecs(){
        return this.epochSecs;
    }
    public String getTimestamp() {
        return timeStamp;
    }
    
    public Image getRadarImage() {
        return img;
    }
    
    public double[] getBbox() {
        return bbox;
    }
}
