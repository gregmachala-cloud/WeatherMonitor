/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.layers;

import weathermonitor.listeners.TimerListener;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import weathermonitor.threads.Timer;

/**
 *
 * @author greg
 */
public class WarningLayer extends JPanel implements BboxListener, TimerListener {
  
    private final String wms_warnings = "https://opengeo.ncep.noaa.gov/geoserver/wwa/warnings/ows?";
    private final String wms_warnings_layer = "warnings";
    private BufferedImage warningImage;
    private final Mapping map;
    private String wmsbbox;
    private String lastUpdateTime = "";
    private int width;
    private boolean updating = false;
    private static final double id = Math.random();
    private int height;
    //private ArrayList<NetworkListener> networkListeners = new ArrayList<NetworkListener>();

     public WarningLayer (Mapping map, int w, int h) {
         this.map = map;
         this.width = w;
         this.height = h;
         this.wmsbbox = map.getWmsBbox();
        
        this.setLayout(new GridLayout());
         this.setOpaque(false);
           this.setSize(w,h);
       map.addBboxListener(this);
       //setBorder(new LineBorder())
        updateWarnings();
    }
     
//       public void addNetworkListener(NetworkListener l) {
//         networkListeners.add(l);
//     }
   
      private void updateWarnings() {
       
        updating = true;
        Thread t = new Thread() {
            public void run() {
                boolean success = loadWarnings();
                if (success == false) {
                    //networkListener.networkDown();
                   // System.out.println("Warnings layer has not updated");
                } else {
                    setVisible(true);
                    //System.out.println("Warning layer was downloaded with latest time " + lastUpdateTime);
                }
                updating = false;
                repaint();
            }
            
        };
        t.start();

    }

      
    public boolean loadWarnings() {
        boolean success = false;
        String xml = "";
       String dlink = wms_warnings+"service=wms&request=GetCapabilities";
       try {
            
              URL url = new URL(dlink);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            xml = xml + inputLine;
        in.close();
            String[] times = getWarningTimes(xml);
            
            int latest = times.length - 1;
            //System.out.println("There are "+times.length+" warning times with first value of "+times[latest]);
            if (times[latest].substring(0,16).equals(lastUpdateTime) == false) {
               // System.out.println("lastUpdateTime="+lastUpdateTime);
                success = downloadLatestWarningImage(times[latest]);
                lastUpdateTime = times[latest].substring(0,16);
            }
            //success = true;
        } catch (Exception ex) {
          //  fireNetworkDown();
            
            success = false;
            
        }
       //if (updateTimerRunning == false && autoUpdate == true) startUpdateTimer();
       
      // if (success == true) fireNetworkUp();
       return success;
    }
//    private void fireNetworkDown() {
//        for (int i=0; i<networkListeners.size(); i++) {
//            networkListeners.get(i).networkDown();
//        }
//    }
//     private void fireNetworkUp() {
//        for (int i=0; i<networkListeners.size(); i++) {
//            networkListeners.get(i).networkUp();
//        }
//    }
    private boolean downloadLatestWarningImage(String time) {
        if (Timer.getInstance().isInternetUp() == false) return false;
        String url = wms_warnings + "request=GetMap&service=WMS&version=1.3.0&layers=" + wms_warnings_layer + "&styles=wwa_warnings&crs=EPSG:4326&";
               url += "bbox=" + wmsbbox + "&width=" + width + "&height=" + height + "&transparent=true&format=image/png&time=" + time;
          //  System.out.println("download warnings:"+url);
            try {
                URL link = new URL(url);
                Image image = ImageIO.read(link);
                warningImageAdded(image,time);
                return true;              
            } catch (Exception e) {
                //fireNetworkDown();
                System.out.println("Error downloading latest warning image");
            }
            return false;
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
    private void warningImageAdded(Image image,String time) {
        
         int w = image.getWidth(this);
         int h = image.getHeight(this);
         this.lastUpdateTime = time;
         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
         warningImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
         Graphics g = warningImage.getGraphics();
         g.drawImage(image,0,0,null);
         g.dispose();
         this.revalidate();
         this.repaint();
    }
    public void setWarningImage(BufferedImage img) {
        this.warningImage = img;
        repaint();
    }
    @Override
    public void paint(Graphics g) {
        //super.paint(g);
       
        if (warningImage != null) {
            //painted = true;
            g.drawImage(warningImage,0,0,this);
        }
    }
    



    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        
        if (wmsbbox.equals(bbox) == true) {
            setVisible(true);
            return;
        }
        setVisible(false);
        if (warningImage != null) warningImage.flush();
        warningImage = null;
        repaint();
        this.wmsbbox = map.getWmsBbox();
        this.width = w;
        this.height = h;
        lastUpdateTime = "";
        updateWarnings();
        
    }


    @Override
    public double getID() {
        return id;
    }

    @Override
    public void runTask() {
         if (updating == false)  updateWarnings();
    }

    @Override
    public void setCountdownValue(int number) {
        
    }

}
