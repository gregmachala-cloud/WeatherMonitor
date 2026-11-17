/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.layers;

import weathermonitor.dataobjects.StormReportInfo;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;
import weathermonitor.listeners.StormReportListener;
import weathermonitor.dataproviders.StormReports;
import weathermonitor.threads.Timer;
import weathermonitor.listeners.TimerListener;
import weathermonitor.threads.WMTimer;
import weathermonitor.WeatherMonitorRun;

/**
 *
 * @author greg
 */
public class StormReportLayer extends JPanel implements BboxListener, TimerListener {
   // private static ArrayList<StormReportInfo> stormReports = new ArrayList<>();
    private final ArrayList<StormReportListener> stormReportListeners = new ArrayList<>();
    private long minimumReportTimeOffset = WeatherMonitorRun.STORM_REPORT_OFFSET_SECS[0];
    private BufferedImage reportImage;
    private Mapping map;
    private WMTimer plotStormReportsTimer ;
    private int container_width;
    private int container_height;
    private boolean stopPlotting = false;
    private boolean plotting = false;
    private static final ArrayList<StormReportInfo> stormReports = StormReports.lsrData;
    private static StormReportLayer me = null;
   private static final double id = Math.random();
    private double locationLat = 0;
    private double locationLon = 0;
    private ArrayList<String> preferredOffices = new ArrayList<>();
    
    public static StormReportLayer getInstance() {
        if (me == null) me = new StormReportLayer();
        return me;
    }
    
    private StormReportLayer() {
        
        StormReports.getInstance().getStormReports();
    }
   
    public ArrayList<String> getPreferredOffices() {
        return this.preferredOffices;
    }
    public void addStormReportListener(StormReportListener l) {
        stormReportListeners.add(l);
    }
    public void initStormReportLayer(Mapping map,double lat, double lon, int w, int h) {
        this.map = map;
        this.locationLat = lat;
        this.locationLon = lon;
          this.container_width = w;
        this.container_height = h;
       // getStormReportOfficeIds(lat,lon, false);
       map.getBbox();
        this.setOpaque(false);
        //this.setBorder(BorderFactory.createLineBorder(Color.RED));
        
         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        reportImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        
        this.setLayout(new GridLayout());
        
        this.setSize(w, h);
       
        plotStormReportsTimer = new WMTimer(500, "StormReports", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Timer.getInstance().restartTimer();
                String[] ids = getStormReportOfficeIds(locationLat,locationLon,true);
                StormReports.getInstance().setOfficeIds(ids);
                update();
                
            }
        });
        plotStormReportsTimer.setRepeats(false);
        plotStormReportsTimer.start();
    }
     
    private void clearMap() {
                 
        Graphics2D g2d = reportImage.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0,0, container_width,container_height);
        g2d.dispose();
    }
     
      public void setMinimumReportTimeOffsetSecs(long t) {
          minimumReportTimeOffset = t;
          clearMap();
          plotStormReports();
      }
      private ArrayList<StormReportInfo> sortStormReports(ArrayList<StormReportInfo> list) {
          ArrayList<StormReportInfo> sortedList = new ArrayList<>();
          for (int i = 0; i < stormReports.size(); i++) {
              if (stormReports.get(i).getEventType().toLowerCase().contains("tornado")) {
                  sortedList.add(stormReports.get(i));
              }
          }
          for (int i = 0; i < stormReports.size(); i++) {
              if (stormReports.get(i).getEventType().toLowerCase().contains("funnel")) {
                  sortedList.add(0,stormReports.get(i));
              }
          }
          for (int i = 0; i < stormReports.size(); i++) {
              if (stormReports.get(i).getEventType().toLowerCase().contains("hail")) {
                  sortedList.add(0,stormReports.get(i));
              }
          }
          for (int i = 0; i < stormReports.size(); i++) {
              if (stormReports.get(i).getEventType().toLowerCase().contains(" wnd ")) {
                  sortedList.add(0,stormReports.get(i));
              }
          }
          for (int i = 0; i < stormReports.size(); i++) {
              if (stormReports.get(i).getEventType().toLowerCase().contains("funnel") == false &&
                  stormReports.get(i).getEventType().toLowerCase().contains(" wnd ") == false &&
                  stormReports.get(i).getEventType().toLowerCase().contains("tornado") == false &&
                  stormReports.get(i).getEventType().toLowerCase().contains("hail") == false) {
                     sortedList.add(0,stormReports.get(i));
               }
          }
          return sortedList;
      }
    private void plotStormReports() {
       // System.out.println("Plot Storm reports");
        Thread t = new Thread() {
            public void run() {
                stopPlotting = true;
                int timeout = 0;
                while (plotting == true) {
                    //System.out.println("Waiting for StormReportLayer plot to finish");
                    timeout++;
                    if (timeout > 300) break;
                    try {
                        sleep(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                stopPlotting = false;
                plotting = true;
                try {
                    long curSecs = new Date().getTime() / 1000;
                    long minimumReportTime = curSecs - minimumReportTimeOffset;
//                    if (stormReports == null) {
//                       
//                        stormReports = StormReports.getInstance().getStormReports();
//                       
//                    }
                    if (stormReports != null && reportImage != null) {
                         System.out.println("There are "+stormReports.size()+" reports to plot");
                    
                        Graphics2D g2d = reportImage.createGraphics();
                        ArrayList<StormReportInfo> l = sortStormReports(stormReports);
                        for (int i = 0; i < l.size(); i++) {
                            if (stopPlotting == true) {
                                plotting = false;
                                break;
                            }
                            long eventTime = l.get(i).getEventTime();
                            // System.out.println("EventTime="+eventTime+" minTime="+minimumReportTime);
                            if (eventTime >= minimumReportTime) {
                                // System.out.println("Plot storm report"+stormReports.get(i).getCity());
                                String event = l.get(i).getEventType();
                                ImageIcon icon = l.get(i).getIcon();
                                double lat = l.get(i).getLat();
                                double lon = l.get(i).getLon();

                                // System.out.println(stormReports.get(i).getCity()+" lat="+lat+" lon="+lon);
                                int[] xy = map.worldToPixel(lon, lat, container_width, container_height);
                                g2d.drawImage(icon.getImage(), xy[0] - 10, xy[1] - 10, null);
                            }
                        }
                        g2d.dispose();
                    }
                    plotting = false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    plotting = false;
                }
                fireStormReportsUpdated(stormReports);
            }
            
        };
        t.start();

    }

    private void fireStormReportsUpdated(ArrayList<StormReportInfo> list) {
        if (list != null) {
            //System.out.println("Fire storm reports added for " + list.size() + " reports");
            for (int i = 0; i < stormReportListeners.size(); i++) {
                //System.out.println("Sending storm reports to listener " + i);
                stormReportListeners.get(i).stormReportsUpdated(list, minimumReportTimeOffset);
            }
        }
    }
      
    private void update() {
        long curSecs = new Date().getTime() / 1000;
        //long minimumReportTime = curSecs - minimumReportTimeOffset;
        ///if (stormReports != null) stormReports.clear();
        StormReports.getInstance().getStormReports();
        //System.out.println("Therea are "+stormReports.size()+" storm report on update");
      
        clearMap();
        plotStormReports();
//        if (Viewer.getInstance().isVisible() && Viewer.getInstance().isShowingAllStormReports()) {
//            Viewer.getInstance().setAllStormReports(stormReports);
//            
//        }
    }
     public double[]  getBbox(double lat, double lon) {
            
            int w = 800;
            int h = 800;
             double zoomFactor =7.0;
            //var aspect = w/h;
            double aspect = Double.valueOf(w)/Double.valueOf(h)*1.2;
            
            double tweekLat = zoomFactor/aspect;
            double tweekLon = zoomFactor;
            
            //System.out.println("WmsRadarViwer: tweekLat="+tweekLat+ " tweekLon="+tweekLon);

            double a = lat-tweekLat;
            double b = lon-tweekLon;
            double c = lat+tweekLat;
            double d = lon+tweekLon;
//            double dppX = Math.abs((b-d) / w);
//            double dppY = Math.abs((a-c) / h);
            double retVal[] = {a,b,c,d};
            // System.out.println("Bbox="+a+" "+b+" "+c+" "+d);
            return retVal;
        }
    private String[] getStormReportOfficeIds(double lat, double lon,boolean update) {
      // System.out.println("GetStormReportOfficIds for "+lat+" "+lon);
        double[] bbox = getBbox(lat,lon);
        preferredOffices = new ArrayList<>();
        if (Mapping.cwa != null) {
            preferredOffices.add(Mapping.cwa);
        }
        double nearest = 1000;
        String nearestOffice = "";
        Enumeration en = Utilities.getInstance().officeIdsProps.keys();
        ArrayList<String> stateList = new ArrayList<>();
        while (en.hasMoreElements()) {
             String office = en.nextElement().toString();
            String data = Utilities.getInstance().officeIdsProps.get(office).toString();
            String[] city_st = data.split("\\|");
            String city = city_st[0];
            String st = city_st[1];
            double tlon = Double.parseDouble(city_st[2]);
            double tlat = Double.parseDouble(city_st[3]);
            double dist = Utilities.getDistanceBetweenTwoPoints(tlon,tlat,lon,lat);
            double distMiles = dist*60.0;
            if (distMiles <= 150) {
                if (preferredOffices.contains(office) == false) {
                   preferredOffices.add(office);
                }
            }
            if (distMiles < nearest) {
                nearest = distMiles;
                nearestOffice = office;
            }
            if (isInMapBounds(bbox, tlat, tlon)) {
                if (stateList.contains(st) == false) {
                   // System.out.println("Storm Report State ="+st);
                    stateList.add(st);
                }
            }


        }
        if (preferredOffices.size() == 0) {
            preferredOffices.add(nearestOffice);
        }
        ArrayList<String> stormReportOids = new ArrayList<>();
        en = Utilities.getInstance().officeIdsProps.keys();
        while (en.hasMoreElements()) {
             String office = en.nextElement().toString();
            String data = Utilities.getInstance().officeIdsProps.get(office).toString();
            String[] city_st = data.split("\\|");
            String st = city_st[1];
            if (stateList.contains(st)) {
                stormReportOids.add(office);
                //System.out.println("Storm report office add :"+office);
            }
        }
        String[] oid = new String[stormReportOids.size()];
        for (int i=0; i<stormReportOids.size(); i++) {
            oid[i] = stormReportOids.get(i);
        }
        return oid;
        //setOfficeIds(oid,update);
    }
    private boolean isInMapBounds(double[] bbox, double lat , double lon) {
          double lowerLeftLat = bbox[0];
          double lowerLeftLon = bbox[1];
          double upperRightLat = bbox[2];
          double upperRightLon = bbox[3];
          if (lat > lowerLeftLat && lat < upperRightLat && lon > lowerLeftLon && lon < upperRightLon) {
              return true;
          }
        return false;
    }
//    private void setOfficeIds(ArrayList<String> stateIds) {
//        if (automaticOfficeIds == false) {
//           // System.out.println("Automatic Storm Report Office ids is off");
//            return;
//        }
////        System.out.println("Automatic Office Ids is ON : SetOfficeIds in StormReport to "+stateIds.toString());
////        for (int i=0; i<stateIds.size(); i++) {
////            System.out.println("     "+stateIds.get(i));
////        }
//        ArrayList<String> list = new ArrayList<>();
//        Properties p = Utilities.readPrefs(officeStateProps);
//        Enumeration en = p.keys();
//        while (en.hasMoreElements()) {
//            String key = en.nextElement().toString();
//            String v = p.get(key).toString();
//            if (v.equals("") == false) {
//                String[] arr = v.split("\\|");
//                if (arr.length == 2) {
//                    String state = arr[1];
//                    if (stateIds.contains(state)) {
//                        list.add(key);
//                    }
//                }
//            }
//            
//        }
//        String[] officeIds = new String[list.size()];
//        for (int i=0; i<officeIds.length; i++) {
//           // System.out.println("StormReport office "+i+"="+list.get(i));
//            officeIds[i] = list.get(i);
//        }
//        StormReports.getInstance().setOfficeIds(officeIds);
//        
//    }
 
    public void setOfficeIds(String[] ids) {
       //this.automaticOfficeIds = auto;
       Timer.getInstance().restartTimer();
       StormReports.getInstance().setOfficeIds(ids);
       update();
    }
    
    public ArrayList<StormReportInfo> getStormReportsAtPoint(int x, int y, Mapping map) {
        int minX = x - 10;
        int minY = y - 10;
        int maxX = x + 10;
        int maxY = y + 10;
        double[] ul = map.pixelToWorld(minX, minY);// lat,lon
        double[] lr = map.pixelToWorld(maxX, maxY);
        return getStormReportsAtPoint(ul[1],lr[0],lr[1],ul[0]);
    }
    
    public ArrayList<StormReportInfo> getAllStormReports() {
        return stormReports;
    }
    
    private ArrayList<StormReportInfo> getStormReportsAtPoint(double minX, double minY, double maxX, double maxY)  {
        if (stormReports == null) {
           // System.out.println("There are no storm reports ");
            return null;
        }
        ArrayList<StormReportInfo> list = new ArrayList<>();
        //System.out.println("minX="+minX+" minY="+minY+" maxX="+maxX+" maxY="+maxY);
//        double minX = lon -0.025;
//        double minY = lat -0.025;
//        double maxX = lon + 0.025;
//        double maxY = lat + 0.025;
       long curSecs = new Date().getTime() / 1000;
        long minimumReportTime = curSecs - minimumReportTimeOffset;
        for (int i=0; i<stormReports.size(); i++) {
           double testLat = stormReports.get(i).getLat();
           double testLon = stormReports.get(i).getLon();
           //System.out.println("see if testLat:"+testLat+" testLon:"+testLon+" is near "+lat+" "+lon);
           if (testLat >= minY && testLat <= maxY &&
               testLon >= minX && testLon <= maxX) {
               if  (stormReports.get(i).getEventTime() >= minimumReportTime ) {
                   list.add(stormReports.get(i));
                }
           }
          
        
        }
        return list;
    }
    
//    public int[] worldToPixel(double t_lon,double t_lat,int w,int h) {
//        
//            double lowerLeftLat = bbox[0];
//            double lowerLeftLon = bbox[1];
//            double upperRightLat = bbox[2];
//            double upperRightLon = bbox[3];
//            double dppX = (upperRightLon - lowerLeftLon)/w;
//            double dppY = (upperRightLat - lowerLeftLat)/h;
//            double latDiff = upperRightLat - t_lat;
//            double lonDiff = t_lon - lowerLeftLon;
//            
//            int x = Double.valueOf(lonDiff / dppX).intValue();
//            int y = Double.valueOf(latDiff / dppY).intValue();
//         
//            int[] retVal = {x,y};
//            return retVal;
//      }
    
    public void setReportImage(BufferedImage img) {
        this.reportImage = img;
        repaint();
    }
    @Override
    public void paint(Graphics g) {
        //super.paint(g);
       // System.out.println("Repainting AlertLayer at "+new Date().getTime());
        if (reportImage != null) {
          // System.out.println("Paint with reportImage size="+reportImage.getHeight()+ " this height="+container_height);
            g.drawImage(reportImage,0,0,null);
        } 
    }
    



    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList stateIds,String nearestCity) {
        //setOfficeIds(stateIds);
//        if (lat != locationLat || lon != locationLon) {
//            locationLat = lat;
//            locationLon = lon;
//            getStormReportOfficeIds = true;
//            
//        } else {
//            getStormReportOfficeIds = false;
//        }
       // System.out.println("BBox changed in StormReportLayer");
        this.locationLat = lat;
        this.locationLon = lon;
        this.container_width = w;
        this.container_height = h;
        this.setSize(w, h);
//        if (width != w || height != h) {
          
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            reportImage = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        //} else {
            clearMap();
            if (stormReports != null) stormReports.clear();
        //}
        plotStormReportsTimer.restart();
        
    }

 

    @Override
    public double getID() {
       return id;
    }

    @Override
    public void runTask() {
         update();
    }

    @Override
    public void setCountdownValue(int number) {
        
    }

}
