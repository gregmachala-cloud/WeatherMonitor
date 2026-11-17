/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package weathermonitor.dataproviders;

import weathermonitor.listeners.NetworkListener;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import mappingtools.BboxListener;
import utilities.Utilities;
import weathermonitor.listeners.RadarImageListener;
import weathermonitor.settings.RadarSettings;
import weathermonitor.threads.Timer;
import weathermonitor.listeners.TimerListener;
import weathermonitor.threads.WMTimer;
import weathermonitor.dataobjects.WmsRadarLayer;

/**
 *
 * @author greg
 */
public class Radar implements BboxListener,TimerListener{
    private final ArrayList<RadarImageListener> radarImageListeners = new ArrayList<>();
    private boolean downloadingRadar = false;
    public String bbox = "28.98,-102.72,33.48,-98.22";
    private final ArrayList<WmsRadarLayer> wmsRadarLayers;
    public static String mosaicUrl = "https://opengeo.ncep.noaa.gov/geoserver/conus/conus_bref_qcd/ows?";
    public static String mosaicLayerName = "conus_bref_qcd";
    public static String mosaicDescrName = "USA Reflectivity";
    private int radarWmsUrlNumber = 0;
    private String lastRadarLayerNameUsed ;
    private int width = 1000;
    private int height = 800;
    private boolean reScaling = false;
    private int rid = 0;
    private final ArrayList<String> downloadedRadarTimestamps;
    private boolean stopDownloading = false;
    private final double id = Math.random();
  
    private final WMTimer loadRadarTimer;
    private boolean networkUp = true;
    private boolean loadedRadarLayers = false;
    private int numberOfRadarImages = 15;
    private double lat;
    private double lon;

     
    public Radar(String bbox, RadarImageListener imageListener,double lat,double lon) {
        this.downloadedRadarTimestamps = new ArrayList<>();
        this.wmsRadarLayers = new ArrayList<>();
        this.lat = lat;
        this.lon = lon;
        boolean success = loadWmsRadarLayers(lat,lon);
        
        this.bbox = bbox;
       radarImageListeners.add(imageListener);
        
        loadRadarTimer = new WMTimer(500,"Radar",new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              
                stopDownloading = false;
                loadRadarThread();
                
            }
        });
        loadRadarTimer.setRepeats(false);
        loadRadarTimer.start();
    }
    public boolean loadWmsRadarLayers(double lat, double lon) {
        lastRadarLayerNameUsed = this.getWmsLayerName(this.radarWmsUrlNumber);
        wmsRadarLayers.clear();
        String radarId = RadarSettings.getNearestRadarStation(lat, lon);
        WmsRadarLayer defaultLayer = readDefaultMosaicRadarSettings();
        if (radarId != null) {
            
            ArrayList<WmsRadarLayer> list = RadarSettings.getAllRadarLayersFor(radarId);
            
            if (list != null) {
                
                wmsRadarLayers.add(defaultLayer);
                wmsRadarLayers.addAll(list);
                loadedRadarLayers = true;
            } else {
                //WmsRadarLayer defaultLayer = readDefaultMosaicRadarSettings();
                wmsRadarLayers.add(defaultLayer);
                loadedRadarLayers = false;
            }
        }
        if (lastRadarLayerNameUsed.equals(defaultLayer.getLayerName()) == false) {
            String[] arr = lastRadarLayerNameUsed.split("_");
            int last = arr.length-1;
            String shortLayerName = arr[last];
            this.radarWmsUrlNumber = 0;
            for (int i=0; i<wmsRadarLayers.size(); i++) {
                arr = wmsRadarLayers.get(i).getLayerName().split("_");
                last = arr.length-1;
                String testName = arr[last];
              //System.out.println("Does shortLayerName:"+shortLayerName+" == "+testName);
                if (shortLayerName.equals(testName)) {
                    //System.err.print("   YES");
                    this.radarWmsUrlNumber = i;
                    break;
                } 
            }
        } else {
            this.radarWmsUrlNumber = 0;
        }
        
        fireRadarLayersChanged();
        return loadedRadarLayers;
    }
    public void addRadarImageListener(RadarImageListener listener) {
        this.radarImageListeners.add(listener);
    }
    
    public WmsRadarLayer  readDefaultMosaicRadarSettings() {
        String url = mosaicUrl;
        String layerName = mosaicLayerName;
        String name = mosaicDescrName;
        Properties props = Utilities.readRadarSettingsPrefs();
        if (props != null) {
             if (props.containsKey("MosaicUrl")) {
                 url=props.getProperty("MosaicUrl");
                 mosaicUrl = url;
             }
            if (props.containsKey("MosaicLayerName")) {
                layerName=props.getProperty("MosaicLayerName");
                mosaicLayerName = layerName;
            }
            if (props.containsKey("MosaicName")) {
                name=props.getProperty("MosaicName");
                mosaicDescrName = name;
            }
           
        }
        WmsRadarLayer wrl = new WmsRadarLayer(url,layerName,name,true);
//        if (wmsRadarLayers.size() == 0) {
//            wmsRadarLayers.add(wrl);
//        } else {
//            wmsRadarLayers.set(0, wrl);
//        }
        return wrl;
    }
    
    public  boolean isDownloadingRadar() {
        return this.downloadingRadar;
    }
//    public boolean isRadarUpdating() {
//        return updatingRadar;
//    }
//    public void addNetworkListener(NetworkListener l) {
//        this.networkListeners.add(l);
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Radar radar = new Radar(bbox,null);
        //radar.loadRadar();
    }
  
    public void reScaleRadar(String bbox, int w, int h) {
        
        if (reScaling == true) return;
        reScaling = true;
        
        this.width  = w;
        this.height = h;
        this.bbox = bbox;
     
         loadRadarTimer.restart();
        
    }
    public int getRadarWmsTypeNumber() {
        return this.radarWmsUrlNumber;
    }
    public void setRadarWmsTypeNumber(int number) {
        Timer.getInstance().restartTimer();
        this.radarWmsUrlNumber=number;
        if (downloadingRadar == true) stopDownloading = true;
        int timeout = 0;
        while(downloadingRadar == true) {
            timeout++;
           // System.out.println("Wait for active radar downloads to stop 2");
          if (timeout >= 300) break;
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
               // System.getLogger(Radar.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        //System.out.println("clear downloadedRadarTimestamps array at A");
        downloadedRadarTimestamps.clear();
       //  System.out.println("Restar radar" +new Date().getTime());
        loadRadarTimer.restart();
        //loadRadar();
    }
    public String[] getDescrRadarNames() {
        if (wmsRadarLayers.size() == 0) return new String[0];
        ArrayList<String> l = new ArrayList<>();
        //String[] arr = new String[wmsRadarLayers.size()];
        for (int i=0; i<wmsRadarLayers.size(); i++) {
            try {
               l.add(wmsRadarLayers.get(i).getShortLayerDescr());
            } catch (Exception ex) {
                ex.printStackTrace();
                
            }
        }
        String[] arr = new String[l.size()];
        for (int i=0; i<arr.length; i++) {
            arr[i] = l.get(i);
        }
        return arr;
    }
    public String getWmsLayerName(int number) {
        if (wmsRadarLayers.size() == 0) return "";
        return wmsRadarLayers.get(number).getLayerName();
    }
    public String getWmsUrl(int number) {
         if (wmsRadarLayers.size() == 0) return "";
        return  wmsRadarLayers.get(number).getBaseWmsUrl();
    }
    public void updateRadar() {
        updateRadarThread();
    }
    private void updateRadarThread() {
        Thread t= new Thread() {
            public void run() {
                updateRadarT();
            }
        };
        t.start();
    }
    private void updateRadarT() {
       // updatingRadar = true;
        boolean success = false;
        trimDownloadTimesArray(20);
        String dlink = getWmsUrl(radarWmsUrlNumber)+"service=wms&request=GetCapabilities&r="+Math.random();
        //System.out.println("Download WmsRadar Link:"+dlink);
        String xml = "";
        try {
            URL url = new URL(dlink);
            url.openConnection().setReadTimeout(5000);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            boolean start=false;
            int lines = 0;
            while ((inputLine = in.readLine()) != null) {
                //System.out.println("Reading xml lines in RAdar");
                if  (inputLine.contains("<Name>"+getWmsLayerName(radarWmsUrlNumber)+"</Name>")) {
                     start = true;   
                }
                if (start == true) {
                xml = xml +"\n"+ inputLine;
                }
                
            }
            //System.out.println("There are "+lines+" lines of data from "+dlink);
            in.close();
            success = true;
        } catch (Exception ex) {
            System.out.println("Error no network on radar download");
            
            success = false;
        }
        if (success == true) {
            xml = xml.trim().replace("?xml", "xml");
            String[] times = getRadarTimes(xml);
            downloadRadarImages(times);
        } 
        
    }
    private void fireRadarLayersChanged() {
        
        if (radarImageListeners == null) return;
        for (int i=0; i<radarImageListeners.size(); i++) {
            radarImageListeners.get(i).radarLayersChanged(getDescrRadarNames());
        }
    }
    private void fireRadarLegendAdded(Image img) {
        
        if (radarImageListeners == null) return;
        for (int i=0; i<radarImageListeners.size(); i++) {
            radarImageListeners.get(i).radarLengendAdded(img,getWmsLayerName(radarWmsUrlNumber));
        }
    }
    private void fireRadarImageAdded(int rid, Point2D centerPoint, boolean flush, Image img, String time, long epochSecs, int layerNumber,int num) {
        //System.out.println("Radar Bbox="+bbox);
        if (radarImageListeners == null) return;
        for (int i=0; i<radarImageListeners.size(); i++) {
            radarImageListeners.get(i).radarImageAdded(rid,centerPoint,flush, img,time,epochSecs,layerNumber,num);
        }
    }

     public void loadRadar(int w, int h) {
         // System.out.println("Restar radar" +new Date().getTime());
         width = w;
         height = h;
         loadRadarTimer.restart();
     }
     private void loadRadarThread() {
         Thread t = new Thread() {
             public void run() {
                 loadRadarT();
             }
         };
         t.start();
     }
    private void loadRadarT(){
      
        boolean success = false; 
        String dlink = getWmsUrl(radarWmsUrlNumber)+"service=wms&request=GetCapabilities&r="+Math.random();
       // System.out.println("Download WmsRadar Link:"+dlink);
        String xml = "";
        try {
            URL url = new URL(dlink);
            url.openConnection().setReadTimeout(5000);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            boolean start = false;
           
            while ((inputLine = in.readLine()) != null) {
               // System.out.println("downloadin radar data");
                 if  (inputLine.contains("<Name>"+getWmsLayerName(radarWmsUrlNumber)+"</Name>")) {
                     start = true;   
                }
                 if (start == true) {
                     xml = xml +"\n"+ inputLine;
                 }
                 
            }
           // System.out.println("There are "+lines+" lines in "+dlink);
            in.close();
            success = true;
        } catch (Exception ex) {
          
            System.out.println("Not connected to internet");
        }
        if (success == true) {
            //if (networkListeners != null) fireNetworkUp();
            xml = xml.trim().replace("?xml", "xml");

            //System.out.println(xml);
            String[] times = getRadarTimes(xml);
            String radarLegendUrl = getRadarLegend(xml);
            
            
            
             Image radarLegendImage = null;
            try {
                URL link = new URL(radarLegendUrl);
                radarLegendImage = ImageIO.read(link);
                if (radarLegendImage != null) {
                    fireRadarLegendAdded(getScaledRadarLegendImage(radarLegendImage));
                }
             } catch (Exception ex) {}
            
            
            
            //System.out.println("There are "+times.length+" times with first value of "+times[0]);
           
            downloadRadarImages(times);
        }
        loadRadarTimer.setDelay(500);
        reScaling = false;
        //return success;
    }
    
   
    
    private void trimDownloadTimesArray(int size) {
        while (downloadedRadarTimestamps.size() >= size) {
            //System.out.println("Trim:"+downloadedRadarTimestamps.get(0)+" from downloadedRadarTimestamps");
            downloadedRadarTimestamps.remove(0);
        }
        //System.out.println("downloaded array="+downloadedRadarTimestamps.toString());
    }
    private boolean isDownloaded(String time) {
       // System.out.println("Check is downloaded:"+time);
       // System.out.println("downloaded array="+downloadedRadarTimestamps.toString());
        trimDownloadTimesArray(20);
        for (int i=0; i<this.downloadedRadarTimestamps.size(); i++) {
            
            if (downloadedRadarTimestamps.get(i).equals(time)) {
                //System.out.println("Well  "+downloadedRadarTimestamps.get(i)+" eq "+time);
                return true;
            }
        }
        
        return false;
    }
    private void downloadRadarImages(final String[] times) {
        
        final Point2D centerPoint = new Point2D.Double(lon,lat);
        Thread t = new Thread() {
        public void run() {
            downloadRadarImagesThreaded(times,centerPoint);
        } 
        };
        t.start();
    }
    
    private void downloadRadarImagesThreaded(String[] times,final Point2D centerPoint) {
        //System.out.println("Start radar download thread");
        stopDownloading = true;
        
        int timeout = 0;
        boolean flush = false;
        try {
        while (downloadingRadar && timeout <= 120000 && Timer.getInstance().isInternetUp() == true) {
            
            //System.out.println("....Waiting for radar downloaded to stop");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            timeout = timeout + 500;
        }
        if (Timer.getInstance().isInternetUp() == false) {
            return;
        }
        
        stopDownloading = false;        
       
        downloadingRadar = true;
         // download legend
         numberOfRadarImages = 15;
        if (times.length < 15) {
            numberOfRadarImages = times.length;
        }
        //radarImage = new Image[numberOfRadarImages];
        //System.out.println("downloadRadarImages");
        int index = times.length - numberOfRadarImages; //-1;//load 15 images
        //System.out.println("time.length="+times.length+" starting to download radars at index="+index);
        String startingBbox = this.bbox;
        for (int i=0; i<numberOfRadarImages; i++) {
            if (stopDownloading == true) {
                //System.out.println("STIOP DOWNLAODING NOW BREAK");
                break;
            }
            if (startingBbox.equals(bbox) == false) {
               // System.out.println("Bbox changed on radar download");
                break;
            }
             String url = "";
            try {
                int v = index;
                boolean missingRadars = false;
                if (index < 0) {
                    missingRadars = true;
                    v = 0;
                }
               // System.out.println("index="+v);
                if (missingRadars == true || isDownloaded(times[v].substring(0,16)) == false) {
                    url = getWmsUrl(radarWmsUrlNumber)+ "request=GetMap&service=WMS&version=1.3.0&layers=" + getWmsLayerName(radarWmsUrlNumber) + "&crs=EPSG:4326&";
                    url += "bbox=" + bbox + "&width=" + width + "&height=" + height + "&transparent=true&format=image/png&time=" + times[v];

                    //System.out.println("Download radar:"+url);
                    URL link = new URL(url);
                    link.openConnection().setReadTimeout(5000);
                    Image image = ImageIO.read(link);
                    if (downloadedRadarTimestamps.size() == 0) {
                        flush = true;
                    } else {
                        flush = false;
                    }
                    downloadedRadarTimestamps.add(times[v].substring(0,16));
                    ZoneId zoneId = ZoneId.of("Z");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime localDateTime = LocalDateTime.parse(times[v].replace("T"," ").substring(0, 16), formatter);
                    long epochSecs = localDateTime.atZone(zoneId).toEpochSecond();
                    
                    fireRadarImageAdded(rid, centerPoint, flush, image, times[v],epochSecs,radarWmsUrlNumber,numberOfRadarImages);
                    //if (i == 14) {//latest image
                    //    latestRadarTime = times[v].substring(0, 16);
                    //}

                } else {
                    //System.out.println("Already downloaded radar with time "+times[v]);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("Can't get radar image"+url);
                
            }
            index++;
        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        downloadingRadar = false;
        
    }
    
    private Image getScaledRadarLegendImage(Image img) {
        if (img != null) {
          return img.getScaledInstance(750, 45, Image.SCALE_FAST);
          
        } else {
            return null;
        }
        //return this.radarLegendImage;
    }
    private String getRadarLegend(String xml) {
        String url = ""; int start = -1; int end = -1;
        Pattern pat = Pattern.compile("<LegendURL");
        Matcher m = pat.matcher(xml);
        if (m.find()) start = m.start();
        pat = Pattern.compile("</LegendURL");
        m = pat.matcher(xml);
        if (m.find()) end = m.start();
        if (start > 0 && end > 0) {
            String text = xml.substring(start,end);
            pat = Pattern.compile("href=\"");
            m = pat.matcher(text);
            if (m.find()) start = m.end();
            pat = Pattern.compile("\"/>");
            m = pat.matcher(text);
            if (m.find()) end = m.start();
            url = text.substring(start,end).replaceAll("amp;","").replaceAll("%2F", "/");
            //System.out.println("legend url = "+url);
        }
        return url;
    }
    private String[] getRadarTimes(String xml) {
        // Grab the layer data first
        
        String times = "";
        Pattern pat = Pattern.compile("Dimension name=\"time");
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

//    public void setCountdownField(JLabel field) {
//        this.countdownField = field;
//    }
    private void reloadNewBbox(String bbox, int w, int h) {
        
        this.bbox = bbox;
        
        if (downloadingRadar == true) stopDownloading = true;
        int timeout = 0;
        while(downloadingRadar == true) {
           // System.out.println("Wait for active downloads to stop");
            timeout++;
            if (timeout >=300) break;
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
               // System.getLogger(Radar.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        // System.out.println("clear downloadedRadarTimestamps array at B");
        downloadedRadarTimestamps.clear();
        Timer.getInstance().restartTimer();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
         //   System.getLogger(Radar.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
      //  System.out.println("Rescale radar with timer value ="+Timer.getInstance().getCountdownValue());
        reScaleRadar(bbox,w, h);
    }
    @Override
    public void bboxChanged(String bbox, final int w, final int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        
        if (this.bbox.equals(bbox)) return;
       // System.out.println("Radar Bbox Changed from "+this.bbox+" to "+bbox);
        //System.out.println("bbox changed in radar");
        rid++;
        this.bbox = bbox;
        this.width = w;
        this.height = h;
        this.lat = lat;
        this.lon = lon;
        //loadWmsRadarLayers(lat,lon);
        Thread t = new Thread() {
            public void run() {
                loadWmsRadarLayers(lat,lon);
                reloadNewBbox(bbox,w,h);
            }
        };
        t.start();
        
    }

//    @Override
//    public void locationChanged(double lat, double lon, int zoomLevel) {
//        this.lat = lat;
//        this.lon = lon;
//        loadWmsRadarLayers(lat,lon);
//
//    }

    public int getRadarId() {
        return rid;
    }
    @Override
    public double getID() {
        return id;
    }

    @Override
    public void runTask() {
        if (reScaling == false && stopDownloading == false) {
            updateRadarThread();
        } else {
            System.out.println("Cannot runTask to updateRadar because reScaling=" + reScaling + " is true or stopDownloading =" + stopDownloading + " is true");
        }
    }

    @Override
    public void setCountdownValue(int number) {
//        if (countdownField != null) {
//            if (number >= 10 ) {
//               countdownField.setText(Integer.toString(number));
//            } else {
//                if (number < 0) {
//                    countdownField.setBackground(Color.RED);
//                    countdownField.setForeground(Color.RED);
//                } else {
//                    countdownField.setText("0" + Integer.toString(number));
//                }
//            }
//            
//        }
    }

//    @Override
//    public void networkUp(boolean value) {
//         if (value == true) {
//            
//             networkUp = true;            
//            
//            updateRadarThread();
//         } else if (value == false) {
//             networkUp = false;
//         }
//    }
}
