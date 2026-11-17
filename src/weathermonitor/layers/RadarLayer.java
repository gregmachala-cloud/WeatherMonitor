/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.layers;

import weathermonitor.dataobjects.RadarImageObject;
import weathermonitor.listeners.RadarImageListener;
import weathermonitor.dataproviders.Radar;
import weathermonitor.listeners.NetworkListener;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;
import weathermonitor.threads.RadarLooper;
import weathermonitor.listeners.RadarLooperListener;
import weathermonitor.WeatherMonitorRun;

/**
 *
 * @author greg
 */
public class RadarLayer extends JPanel implements RadarImageListener,RadarLooperListener,BboxListener,NetworkListener {
    //private BufferedImage mapImage;
    private final Radar radar;
    //private BufferedImage radarImage;
    private final Mapping map;
    private static final ArrayList<RadarImageObject> radarImageObjects = new ArrayList<>();
    private int radarImageNumber = 0;
   private final boolean smoothRadar;
   private int numberOfRadarImages = 13;
   private JLabel timestamp = null;
   //private boolean networkUp = true;
   private final JLabel waitIcon;
   private boolean showWaitImage = false;
   //private boolean painting = false;
   private String bbox = "";
   //  private int width;
   //  private int height;
     private final double id = Math.random();
     private int radarLayerNumber = 0;
     private boolean loop = true;
     //private javax.swing.Timer radarLooper;
     private final RadarLooper looper;
     private boolean hideRadar = false;
    
    private double lat = 0;
    private double lon = 0;
    
    public RadarLayer(Mapping map, JLabel waitIcon, int w, int h,double lat, double lon) {
        this.smoothRadar = false;
//        this.width = w;
//        this.height = h;
        this.map = map;
        this.lat = map.getLat();
        this.lon = map.getLon();
        this.waitIcon = waitIcon;
        this.setLayout(new GridLayout());
        this.setOpaque(false);
        this.setSize(w, h);
        this.radar = new Radar(map.getWmsBbox(), this,lat,lon);
       
        looper = new RadarLooper(this, numberOfRadarImages);
        looper.setLoop(true);
    }
     public void setAnimationDelay(int value) {
         System.out.println("Set radar animation delay:"+value+"ms");
         
         looper.setDelay(value);
         //this.radarLooper.setDelay(value);
     }
     private void flushRadarObjects() {
           if (radarImageObjects != null) {
               for (int i=0; i<radarImageObjects.size(); i++) {
                   radarImageObjects.get(i).flush();
               }
               radarImageObjects.clear();
           }
           
     }
     public void setRadarWmsTypeNumber(int number ){
        this.radarLayerNumber = number;
        
         showWaitImage = true;
         this.repaint();
         Thread t = new Thread() {
             public void run() {
                 int timeout = 0;
                 while (radar.isDownloadingRadar()) {
                     System.out.println("Downloading radar in RadarLayer 1");
                     timeout++;
                     if (timeout > 120) break;
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException ex) {
                        // System.getLogger(RadarLayer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                     }
                 }
                 flushRadarObjects();
                
                 
                 radar.setRadarWmsTypeNumber(number);
             }
         };
         t.start();
         
      }
//      public void setCountdownField(JLabel f) {
//          radar.setCountdownField(f);
//      }
     public void setTimestamp(JLabel l) {
         this.timestamp = l;
     }
     public Radar getRadar() {
         return radar;
     }
    @Override
    public void paint(Graphics g) {
       
        //super.paint(g);
        try {
            if (hideRadar) {
                g.drawImage(Utilities.getEmptyImage(),0,0,null);
                System.out.println("Draw null radar image");
                
               //  g.drawImage(new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_ARGB),0,0,null);
            } else {

                int size = radarImageObjects.size();
                //System.out.println("show radar number"+radarImageNumber);
                if (radarImageObjects.size() > radarImageNumber) {
                    //System.out.println("Draw radar imageof width:"+radarImageObjects.get(radarImageNumber).getRadarImage().getWidth(this));
                    g.drawImage(radarImageObjects.get(radarImageNumber).getRadarImage(), 0, 0, this);

                } else {
                    int last = size - 1;
                    if (last >= 0) {
                        g.drawImage(radarImageObjects.get(last).getRadarImage(), 0, 0, this);

                    }

                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    // tempo hide radar until images arrive in radarImageAdded
    public void hideRadar(boolean value ) {
        this.hideRadar = value;
        if (value == true) {
            repaint();
            //setVisible(false);
            
        }
       
        repaint();
    }
    
//    public Image getLatestRadarImage() {
//        if (looper.isLooping() == true) return null;
//        int last = radarImageObjects.size() - 1;
//        if (last >= 0) {
//            return radarImageObjects.get(last).getRadarImage();
//
//        }
//        return null;
//    }

    public String formatRadarTimestamp(String time) {
         time = time.substring(0,19).replace("T"," ");
       // System.out.println(time);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d hh:mma");
        LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
        TimeZone tz = TimeZone.getTimeZone("America/Chicago");
        int hours = tz.getOffset(new Date().getTime())/1000/60/60*-1;
        //System.out.println("offset is "+hours);
        localDateTime = localDateTime.minusHours(hours);
        //timestamp.setText(localDateTime.format(outputFormat)+" ");
        return localDateTime.format(outputFormat);
    }
    @Override
    public void radarImageAdded(int rid,Point2D centerPoint, boolean flush, Image img, String time, long epochSecs, int layerNumber, int numberOfRadarImages) {
        //System.out.println("There are "+numberOfRadarImages+" radar images to loop");
        
        if (flush == true) flushRadarObjects();
        if (rid != radar.getRadarId()) return;
      //  System.out.println("New radar imiage added for "+centerPoint.getX()+" "+centerPoint.getY()+" with lat="+lat+" lon="+lon);
        if (centerPoint.getX() != lon || centerPoint.getY() != lat) {
          //  System.out.println("Toss extrandous radar image lat="+centerPoint.getY());
            return;
        }
        this.numberOfRadarImages = numberOfRadarImages;
        this.looper.setNumberOfImages(numberOfRadarImages);
        int w = img.getWidth(this);
        int h = img.getHeight(this);
//        this.width = w;
//        this.height = h;
        if (smoothRadar == true && map.getZoomLevel() >= 4) {
            BufferedImage bufImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufImg.createGraphics();
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
            float[] filter = new float[]{
                1.0f / 121.0f, 2.0f / 121.0f, 3.0f / 121.0f, 2.0f / 121.0f, 1.0f / 121.0f,
                2.0f / 121.0f, 7.0f / 121.0f, 11.0f / 121.0f, 7.0f / 121.0f, 2.0f / 121.0f,
                3.0f / 121.0f, 11.0f / 121.0f, 15.0f / 121.0f, 11.0f / 121.0f, 3.0f / 121.0f,
                2.0f / 121.0f, 7.0f / 121.0f, 11.0f / 121.0f, 7.0f / 121.0f, 2.0f / 121.0f,
                1.0f / 121.0f, 2.0f / 121.0f, 3.0f / 121.0f, 2.0f / 121.0f, 1.0f / 121.0f};

            Kernel kernel = new Kernel(5, 5, filter);
            ConvolveOp cop = new ConvolveOp(kernel);
            BufferedImage smoothedImage = cop.filter(bufImg, null);
            RadarImageObject rdo = new RadarImageObject(rid,centerPoint,smoothedImage, time,epochSecs, map.getBbox());
            radarImageObjects.add(rdo);

        } else {

           // System.out.println("Adding radar img with time "+time);
                             
            RadarImageObject rdo = new RadarImageObject(rid,centerPoint,img, time,epochSecs, map.getBbox());
            if (radarImageObjects.size() == 0) {
               radarImageObjects.add(rdo);
              // System.out.println("rid="+rid+" insert radar0 id="+rdo.getId());
            } else {
                boolean inserted = false;
                for (int k=0; k<radarImageObjects.size(); k++){
                    long t = radarImageObjects.get(k).getEpochSecs();
                    int nrid = radarImageObjects.get(k).getId();
                    //System.out.println("rid="+rid+" insert radar rid="+nrid);
                     if (epochSecs < t) {
                       //  System.out.println("Insert radar1 image at index "+k+" id="+rdo.getId());
                         radarImageObjects.add(k, rdo);
                         inserted = true;
                         break;
                     }
                }
                if (inserted == false) {
                    
                   // System.out.println("rid="+rid+" insert radar2 id="+rdo.getId());
                    radarImageObjects.add(rdo);
                }
            }
        }
        while (radarImageObjects.size() > numberOfRadarImages) {
            radarImageObjects.remove(0);
        }
        
         int lastOne = radarImageObjects.size()-1;
         String ts = radarImageObjects.get(lastOne).getTimestamp();
        if (timestamp != null) timestamp.setText(formatRadarTimestamp(ts));
        //System.out.println("The radar coming in has number "+layerNumber+" the requested layer number in pull down is"+radarLayerNumber);
        if (showWaitImage == true && layerNumber == radarLayerNumber) {
            showWaitImage = false;
            waitIcon.setVisible(false);
            repaint();}
        if  (hideRadar == false && looper.isLooping() == false) {
            repaint();
        }
        int minLoad = numberOfRadarImages/4;
        int maxLoad = numberOfRadarImages - 1;
        if (hideRadar == true && radarImageObjects.size() == 1) {
            Point2D testPoint = radarImageObjects.get(0).getCenterPoint();
            if (testPoint.equals(centerPoint) == false) {
                System.out.println("RadarLayer image has old centerPoint");
            }
            System.out.println("Setting radar visible true in RadarLayer");
            Thread t = new Thread() {
                public void run() {
                    try {
                        sleep(2500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    hideRadar = false;
                    repaint();
                }
            };
            t.start();
            repaint();
            //setLoop(true);
            //hideRadar = false;
        } else if (radarImageObjects.size() < numberOfRadarImages-1) {
            repaint();
        } else if(radarImageObjects.size() == numberOfRadarImages-1) {
            
           
            
            if (WeatherMonitorRun.radarLoopCb.isSelected()) {
                //System.out.println("Start looping radar again");
                setLoop(true);
                looper.setLoop(true);
            } else {
                repaint();
            }
        }
    }
    public Image getLatestRadarImage() {
         int size = radarImageObjects.size();
            int last = size - 1;
            if (last >= 0) {
                
                return radarImageObjects.get(last).getRadarImage();
            }
            return null;
    }
    public void stopLoop() {
        this.looper.setLoop(false);
    }
    public void startLoop() {
        this.looper.setLoop(true);
    }
    public void clearRadar() {
        flushRadarObjects();
        //radarImageObjects.clear();
    }
    public boolean isLooping() {
        return loop;
    }
    public void setLoop(final boolean value) {
        this.loop = value;
        if (value == false) {
            int size = radarImageObjects.size();
            int last = size - 1;
            if (last >= 0) {
                radarImageNumber = last;
                repaint();
            }
        }
        Thread t = new Thread() {
            public void run() {
                int timeout = 0;
                while (radar.isDownloadingRadar()) {
                    //System.out.println("RadarLAyer waiting on radar to stop downloading...");
                    timeout++;
                    if (timeout > 300) break;
                    try {
                        sleep(200);
                    } catch (InterruptedException ex) {

                    }}
                    looper.setLoop(value);
                    if (value == false) {
                        int size = radarImageObjects.size();
                        int last = size - 1;
                        if (last >= 0) {
                            radarImageNumber = last;
                            repaint();
                        }
                    }
                
            }
        };
        t.start();
         
         
    }
    @Override
    public void loopNext(int imageNumber) {
        if (loop == false && hideRadar == false) return;
       // Thread t = new Thread() {
         //   public void run() {
                radarImageNumber = imageNumber;
                repaint();
        //    }
       // };
       // t.start();
    }

    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        if (this.bbox.equals(bbox)) return;
        this.bbox = bbox;
        this.lat = lat;
        this.lon = lon;
        flushRadarObjects();
        //radarImageObjects.clear();
        
        
        showWaitImage = true;
        // Wait for images to come in from Radar.java
    }

    @Override
    public double getID() {
        return id;
    }

    @Override
    public void radarLengendAdded(Image img,String layerName) {
        
    }

    @Override
    public void radarLayersChanged(String[] radarNames) {
        
    }

    @Override
    public void networkUp(boolean value) {
        if (value == true) radar.updateRadar();
    }
   

   
    
    
}
