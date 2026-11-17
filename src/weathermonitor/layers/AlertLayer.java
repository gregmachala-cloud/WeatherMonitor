/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.layers;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;
import weathermonitor.threads.Timer;
import weathermonitor.listeners.TimerListener;
import weathermonitor.dataobjects.WarningData;
import weathermonitor.WeatherMonitorRun;

/**
 *
 * @author greg
 */
public class AlertLayer extends JPanel implements BboxListener, TimerListener {
  
    private final String wms_warnings = "https://opengeo.ncep.noaa.gov/geoserver/wwa/hazards/ows?";
    private final String wms_hazards_layer = "hazards";
    private BufferedImage alertImage;
    private final Mapping map;
    private String wmsbbox;
    private String lastUpdateTime = "";
    private int width;
    private boolean updating = false;
    private static final double id = Math.random();
    private int height;
    
    private Properties props = null;
    private Hashtable<Integer,String> products = new Hashtable<Integer, String>();
    //private ArrayList<NetworkListener> networkListeners = new ArrayList<NetworkListener>();

     public AlertLayer (Mapping map, int w, int h) {
         this.map = map;
         this.width = w;
         this.height = h;
         this.wmsbbox = map.getWmsBbox();
         this.products = getProductHash();
         //System.out.println("Starting alert layer with product color hash of size:"+props.size());
         
        this.setLayout(new GridLayout());
         this.setOpaque(false);
           this.setSize(w,h);
       
       updateAlerts();
        
    }
//     
//       public void addNetworkListener(NetworkListener l) {
//         networkListeners.add(l);
//     }
   
      private void updateAlerts() {
       
        updating = true;
        Thread t = new Thread() {
            public void run() {
                boolean success = loadAlerts();
                if (success == false) {
                    //networkListener.networkDown();
                    System.out.println("Alert layer has not updated");
                } else {
                    //networkListener.networkUp();
                    if (WeatherMonitorRun.advisoriesCb.isSelected()) setVisible(true);
                   // System.out.println("Alert layer was downloaded with latest time " + lastUpdateTime);
                }
                updating = false;
                repaint();
            }
            
        };
        t.start();

    }

      
    public boolean loadAlerts() {
        boolean success = false;
        String xml = "";
        long start = new Date().getTime()/1000;
       String  dlink = wms_warnings+"service=wms&request=GetCapabilities";
      // System.out.println("load Alerts "+dlink);
       try {
            URL url = new URL(dlink);
            url.openConnection().setReadTimeout(10000);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            
            while ((inputLine = in.readLine()) != null) {
                //System.out.println("reading input lihe");
                xml = xml +"\n"+ inputLine;
                
            }
            in.close();
           // System.out.println("There are "+lines+" lines of xml for alerts");
            String[] times = getWarningTimes(xml);
            
            int latest = times.length - 1;
           // System.out.println("There are "+times.length+" warning times with first value of "+times[latest]);
            if (times[latest].substring(0,16).equals(lastUpdateTime) == false) {
              //  System.out.println("lastUpdateTime="+lastUpdateTime);
                downloadLatestWarningImage(times[latest]);
                lastUpdateTime = times[latest].substring(0,16);
            } 
            long diff = new Date().getTime()/1000 - start;
           // System.out.println("Download alert layer image took "+diff+" secs");
            success = true;
        } catch (Exception ex) {
       //     fireNetworkDown();
            
            success = false;
            
        }
       //if (updateTimerRunning == false && autoUpdate == true) startUpdateTimer();
       
      // if (success == true) fireNetworkUp();
       return success;
    }

    private void downloadLatestWarningImage(String time) {
        if (Timer.getInstance().isInternetUp() == false) {
            System.out.println("NEtwork up on download latest warnign image");
            return;
        }
        String url = wms_warnings + "request=GetMap&service=WMS&version=1.3.0&layers=" + wms_hazards_layer + "&styles=wwa_hazards&crs=EPSG:4326&";
               url += "bbox=" + wmsbbox + "&width=" + width + "&height=" + height + "&transparent=true&format=image/png&time=" + time;
            //System.out.println("download alerts:"+url);
            try {
                URL link = new URL(url);
                Image image = ImageIO.read(link);
                alertImageAdded(image,time);
                              
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
     private String[] getWarningTimes(String xml) {
        String times = "";
        Pattern pat = Pattern.compile("<Dimension");
        Matcher m = pat.matcher(xml);
        if (m.find()) {
                times = xml.substring(m.start());
                pat = Pattern.compile("</Dimension>");
                m = pat.matcher(times);
                if (m.find()) {
                    times = times.substring(0,m.start());
                    pat = Pattern.compile(">");
                    m = pat.matcher(times);
                    if (m.find()) {
                        times = times.substring(m.end());
                    }
                }
            }
        return times.split(",");
       
    }
    private void alertImageAdded(Image image,String time) {
        
         int w = image.getWidth(this);
         int h = image.getHeight(this);
         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
         if (alertImage != null) {
             alertImage.flush();
         }
         alertImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
         Graphics g = alertImage.getGraphics();
         g.drawImage(image,0,0,null);
         g.dispose();
         this.lastUpdateTime = time;
        // System.out.println("Alert image added ");
         this.revalidate();
         this.repaint();
    }
    private String getHighestPriorityEvent(ArrayList<WarningData> list) {
        if (list == null) return "";
        if (list.size() == 0) return "";
        for (int i=0;i<list.size();i++) {
            if (list.get(i).getSeverity().equals("Severe")) {
                return list.get(i).getEvent();
            }
            
        }
        for (int i=0;i<list.size();i++) {
            if (list.get(i).getSeverity().equals("Moderate")) {
                return list.get(i).getEvent();
            }
            
        }
        return list.get(0).getEvent();
    }
    
    private boolean isSingleEvent(ArrayList<WarningData> list) {
        if (list == null || list.size() == 0) return false;
        String event = list.get(0).getEvent();
        for (int i=1; i<list.size(); i++) {
            if (list.get(i).getEvent().equals(event) == false) {
                return false;
            }
        }
        return true;
    }
    public  String getProductAtPoint(double lat, double lon)  {
        if (alertImage == null) return "";
        String event = "";
        int w = alertImage.getWidth();
        int h = alertImage.getHeight();
        int[] xy = map.worldToPixel(lon, lat, w, h);
       // System.out.println("Get rgb value for alert layer at point "+xy[0]+" "+xy[1]);
        int rgb = 0;
        try {
             rgb = alertImage.getRGB(xy[0], xy[1]);
        } catch (Exception ex) {}
        
        if (rgb == 0) return "";
       // System.out.println("Color at point is "+rgb);
        if (products.containsKey(rgb)) {
            
            //System.out.println("Hashtable does contain "+rgb+" so dont have to ping alert server");
            return products.get(rgb);
            
       
        } else {
            ArrayList<WarningData> list = WxAlertsTable.getWeatherAlertsAt( lat,  lon);
            if (list != null && list.size() > 0) {
                if (isSingleEvent(list) == true) {
                    if (props == null) {
                        props = new Properties();
                    }
                    props.put(Integer.valueOf(rgb).toString(), list.get(0).getEvent().trim());
                    storeProductProps();
                    //storeProductHash(rgb, list.get(0).getEvent().trim());
                }
                event = getHighestPriorityEvent(list);
                if (event.equals("") == false && products.containsKey(rgb) == false) {
                    // Take a guess on the color you clicked on and put highest priority
                    // product into the hash for now
                    products.put(rgb, event);

                }
            }
        }
        return event;
    }
    private Properties getProductProps() {
         Properties props = new Properties();
      
        try {
            FileInputStream os = new FileInputStream(Utilities.DIR+Utilities.SEP+"Maps"+Utilities.SEP+"product.properties");
            props.load(os);
            os.close();
          
        } catch (FileNotFoundException ex) {
            //System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            //System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return props;
    }
    private Hashtable<Integer,String> getProductHash() {
        props = getProductProps();
        if  (props == null) props = new Properties();
        Hashtable<Integer,String> hash = new Hashtable<Integer,String>();
        Enumeration en = props.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                String value = props.getProperty(key);
                hash.put(Integer.valueOf(key), value);
            }
       
        return hash;
    }
    
    public  void storeProductProps() {

       //System.out.println("Storeing product props");
        try {
            FileOutputStream os = new FileOutputStream(Utilities.DIR+Utilities.SEP+"Maps"+Utilities.DIR+"product.properties");
            props.store(os, "");
            os.close();
        } catch (FileNotFoundException ex) {
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    public void setAlertImage(BufferedImage img) {
        this.alertImage = img;
        repaint();
    }
    @Override
    public void paint(Graphics g) {
       // super.paint(g);
       // System.out.println("Repainting AlertLayer at "+new Date().getTime());
        if (alertImage != null) {
          
            g.drawImage(alertImage,0,0,this);
        } 
    }
    



    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        
        if (wmsbbox.equals(bbox) == true) {
            if (WeatherMonitorRun.advisoriesCb.isSelected()) setVisible(true);
            return;
        }
        if (alertImage != null) this.alertImage.flush();
        this.alertImage = null;
        setVisible(false);
        //System.out.println("Repaint requested at "+new Date().getTime());
        
        //System.out.println("Repaint done at "+new Date().getTime());
        this.wmsbbox = map.getWmsBbox();
        this.width = w;
        this.height = h;
        lastUpdateTime = "";
        updateAlerts();
        
    }

 

    @Override
    public double getID() {
        return id;
    }

    @Override
    public void runTask() {
         if (updating == false)  updateAlerts();
    }

    @Override
    public void setCountdownValue(int number) {
        
    }

}
