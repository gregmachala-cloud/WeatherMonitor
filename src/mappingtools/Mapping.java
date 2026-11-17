
package mappingtools;

import utilities.Utilities;
import weathermonitor.listeners.MetarLocationListener;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import org.json.*;
import weathermonitor.WeatherMonitorRun;
import weathermonitor.dataobjects.MetarLocationObject;
import weathermonitor.listeners.NetworkListener;


public class Mapping implements NetworkListener {
    public static int COUNTY_MAP = 0;
    public static int STATE_MAP = 1;
    private boolean networkUp = true;
    private Properties cityToMetarProps;
    private ArrayList<MetarLocationListener> metarLocationListeners = new ArrayList<MetarLocationListener>();
    //private static Color stateColor = new Color(100,100,110);
        Point2D yourLocation;
        private String nearestCityName = "Your Location";
        private double nearestCityDistance = Double.MAX_VALUE;
        private double refreshTweak = 0.0001;
        private static Color countyLineColorZ4 = new Color(100,100,100);
        private Color mapBgColor = Color.WHITE;
        private static Color countyLineColorZ6 = new Color(50,50,50);
        private static Color countyLineColorZ7 = new Color(20,20,20);
        private Color stateColor = new Color(12,17,112);
        private Color roadColor = new Color(200,125,50);
        private Color interstateColor = new Color(255,0,0);
        public static Font metarStationFont = new Font("Monospaced",Font.PLAIN,16);
        public static Font[] cityFont = {new Font("Monospaced",Font.PLAIN,15),
                new Font("Monospaced",Font.BOLD,17),
                new Font("Monospaced",Font.BOLD,18),
                new Font("Monospaced",Font.BOLD,19),
                new Font("Monospaced",Font.BOLD,20),
                new Font("Monospaced",Font.BOLD,22)
        };
        //private static Font yourLocationFont = new Font("SansSerif",Font.BOLD,18);
        private static String state_id = "TX";
        private int hashIndex = 0;
      public static int zoomLevel = 2;  
      //public String MapDir = Utilities.DIR+Utilities.SEP+"Maps";
      public static double[]  zoomFactors = {7.0,4.25,3.25,2.25,1.0,0.5,0.25};
      public double lat;
      public double lon;
      private double bboxChangeId;
      private double locationChangeId;
      private ArrayList<String> stateIds = new ArrayList<String>();
      //private Hashtable<String,GeneralPath> countyPathHash = new Hashtable<String, GeneralPath>();
      //private Hashtable<String,GeneralPath> roadPathHash = new Hashtable<String, GeneralPath>();
      private MapCache[] mapCache = new MapCache[7];
      public int container_width = 2000;
      public int container_height = 1000;
      public double dppX = 0.0;
      public double dppY = 0.0;
      private BufferedImage img = null;
      public double[] bbox = null;
      private boolean paintUserCity = false;
      public static String cwa;
      private Graphics2D g2d;
      private String countiesJson;
      private String citiesJson;
      private String roadsJson;
      private String statesJson;
      //private boolean showYourLocationText = false;
      private ArrayList plottedCityX = new ArrayList();
      private ArrayList plottedCityY = new ArrayList();
      private ArrayList<BboxListener> bboxListeners = new ArrayList<BboxListener>();
      private BufferedImage mapBgImage = null;
      private BufferedImage panMapImage = null;
      private Graphics2D panMapGraphics;
      private ArrayList<City> panMapCities;
      private int panMapWidth;
      private int panMapHeight;
      private double panMapLowerLeftLat;
      private double panMapLowerLeftLon;
      private double panMapUpperRightLat;
      private double panMapUpperRightLon;
      private ArrayList<MetarLocationObject> metarStationLocations = new ArrayList<>();
      private javax.swing.Timer metarListenerTimer;
    
      private static Mapping me = null;
      private boolean useCache = false;
      
      public static Mapping getInstance() {
          if (me == null) me = new Mapping();
          return me;
      }
      
      public void initMapping(BufferedImage img,MetarLocationListener listener, int zoomLevel, double theLat, double theLon,int w, int h,Properties props) {
           readSettings(props);
           this.container_width = w;
           this.container_height = h;
           this.metarLocationListeners.add(listener);
           this.lat = theLat; this.lon = theLon;
           getCWA(lat,lon);
           this.img = img;
           this.zoomLevel = zoomLevel;
           bbox = getBbox();

           drawMap(img);
           
       }
      public double getLat() {
          return lat;
      }
      public double getLon() {
          return lon;
      }
      public void getCWA() {
          System.out.println("Get CWA in mapping");
          getCWA(lat,lon);
      }
      private void getCWA(double lat, double lon) {
          String dlink = "https://api.weather.gov/points/"+lat+","+lon;
          String json = Utilities.downloadJsonData(dlink, 5000);
          if (json != null && json.equals("") == false) {
              try {
              JSONObject obj = new JSONObject(json);
                  this.cwa = obj.getJSONObject("properties").getString("cwa");
                  System.out.println("Set CWA to "+cwa);
              } catch (Exception ex) {
                  this.cwa = "SJT";
              }
          }
      }
      public double[] getBoundingBox() {
          return this.bbox;
      }
      public boolean isInPanMapBounds(double lat, double lon) {
         
          if (lat > panMapLowerLeftLat && lat < panMapUpperRightLat && lon > panMapLowerLeftLon && lon < panMapUpperRightLon) {
              return true;
          }
        return false;
      }
       public boolean isInMapBounds(double lat , double lon) {
          double lowerLeftLat = bbox[0];
          double lowerLeftLon = bbox[1];
          double upperRightLat = bbox[2];
          double upperRightLon = bbox[3];
          if (lat > lowerLeftLat && lat < upperRightLat && lon > lowerLeftLon && lon < upperRightLon) {
              return true;
          }
        return false;
    }
       public void addMetarLocationListener(MetarLocationListener listener) {
           this.metarLocationListeners.add(listener);
       }
       //used for recenter on mouse wheel
       public void setMapCenter(Point p) {
           double[] latlon = this.pixelToWorld(p.x, p.y);
           this.lat = latlon[0];
           this.lon = latlon[1];
           this.nearestCityDistance = Double.MAX_VALUE;
           //this.nearestCityName = "";
           //getCWA(lat, lon);
           recenterMap(lat,lon);
       }
//       public void recenterMap(double newLat, double newLon) {
//           
//           recenterMap(newLat,newLon,true);
//       }
       public void recenterMap(double newLat, double newLon) {
           //this.useNearestCityForLocation = useNearestCityForLocation;
           //System.out.println("RecenterMap in mapping with useNearestCity="+useNearestCityForLocation);
           this.clearMapCache();
           this.lat = newLat;
           this.lon = newLon;
           this.nearestCityDistance = Double.MAX_VALUE;
          // this.nearestCityName = "";
           getCWA(lat, lon);
           setZoomLevel(this.zoomLevel);
           // fires this for wx alerts to get new location shouldnt be used elsewhere
          // fireLocationChanged(newLat,newLon);
    }

       public void resizeMap(BufferedImage img, int w, int h,double newLat, double newLon) {
           //System.out.println("Resize map "+w+" "+h);
           if (w != container_width || h != container_height) clearMapCache();
           this.lat = newLat;
           this.lon = newLon;
             this.nearestCityDistance = Double.MAX_VALUE;
           //this.nearestCityName = "";
           getCWA(lat,lon);
           if (container_width != w || container_height != h) {
                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
               this.img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
               
               this.container_height = h;
               this.container_width = w;
              // System.out.println("Resize map in mapping");
               setZoomLevel(this.zoomLevel);
           }
       }
       
       public void addBboxListener(BboxListener l) {
           if (l == null) return;
           for (int i=0; i<bboxListeners.size(); i++) {
               if (bboxListeners.get(i).getID() == l.getID()) {
                  // System.out.println("Already added a listener for bbox");
                   return;
               }
           }
           this.bboxListeners.add(l);
       }

        public int getZoomLevel() {
            return this.zoomLevel;
        }
       
        public void refresh() {
            this.lat = lat + refreshTweak;
            this.lon = lon + refreshTweak;
            refreshTweak = refreshTweak * -1.0;
            setZoomLevel(zoomLevel);
        }
     public void setZoomLevel(int num) {
        // System.out.println("Set zoom level in mapping to "+num);
       // mapBgLabel.setVisible(false);
       // backgroundPanel.setBackground(WeatherMonitorRun.mapBgColor);
        
      
       
        this.zoomLevel = num;
        if (img == null) {
           // System.out.println("Image is nulll at setzoomlevel in jamapoping");
            return;
        }
        
        BufferedImage testImg = getCachedMap(zoomLevel);
       Graphics2D g2d = img.createGraphics();
        if (useCache && testImg != null) {
            
            System.out.println("Using cached map");
            clearMap(g2d);
            g2d.drawImage(testImg,0,0,null);
            g2d.dispose();
            this.bbox = getBbox();
            this.metarStationLocations = getCachedMetarList(num);
            this.stateIds = getCachedStateList(num);
           // System.out.println("Restart timer at "+new Date().getTime());
            for (int i=0; i<metarLocationListeners.size(); i++) {
                 metarLocationListeners.get(i).setMetarPlotList(metarStationLocations,new Point2D.Double(lon,lat),stateIds,this.container_width, this.container_height,bbox,zoomLevel);
            }
        } else {
        System.out.println("Clear map");
        clearMap(g2d);
        if (useCache == false) clearMapCache();
        System.out.println("Redraw Map for zoomLevel " + zoomLevel+" lat="+lat+" lon="+lon);
        this.bbox = getBbox();
        
        loadMapLayers(g2d);
        //ImageViewer.getInstance().showImage(img);
        g2d.dispose();

        }
        //System.out.println("Fire map changed with nearesetcity="+this.nearestCityName+ " and use nearest city="+useNearestCityForLocation);
         fireBboxChanged(bbox[0],bbox[1],bbox[2],bbox[3],container_width,container_height,stateIds,nearestCityName);
        
        
//        for (int i=0; i<bboxListeners.size(); i++) {
//            bboxListeners.get(i).bboxChanged(bbox[0]+","+bbox[1]+","+bbox[2]+","+bbox[3],container_width,container_height);
//           // bboxListeners.get(i).locationChanged(lat, lon);
//        }
    }
//      private void fireDownloadMetarDataForPoint(String name,int x,int y,double lat,double lon) {
//          if (metarLocationListeners == null) return;
//          for (int i=0; i<this.metarLocationListeners.size(); i++) {
//              metarLocationListeners.get(i).plotMetar(name, x, y, lat, lon);
//          }
//      }
//      private void fireLocationChanged(double newLat, double newLon) {
//         locationChangeId = Math.random();
//         final double id = locationChangeId;
//         Thread t = new Thread() {
//             public void run() {
//                 try {
//                     sleep(500);
//                 } catch (InterruptedException ex){
//                     ex.printStackTrace();
//                    // System.getLogger(Mapping.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
//                 }
//                 if (locationChangeId == id) {
//                     for (int i = 0; i < bboxListeners.size(); i++) {
//                          System.out.println("firebboxchanged with nearest city="+nearestCityName);
//                          bboxListeners.get(i).bboxChanged(bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3],container_width, container_height,lat,lon,zoomLevel,stateIds,nearestCityName);
//                     }
//                 } else {
//                     System.out.println("Not firing bbox changed in mapping");
//                 }
//             }
//         };
//         t.start();
//     }
             
     private void fireBboxChanged(double lowerLeftLat, double lowerLeftLon, double upperRightLat, double upperRightLon,int w,int h,ArrayList<String> st_ids,String city) {
         bboxChangeId = Math.random();
         
         final double id = bboxChangeId;
         final String cityN = city;
         Thread t = new Thread() {
             public void run() {
                 try {
                     sleep(500);
                 } catch (InterruptedException ex) {
                     //System.getLogger(Mapping.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                 }
                 if (bboxChangeId == id) {
                     for (int i = 0; i < bboxListeners.size(); i++) {
                         //System.out.println("Bbox changed :"+bboxListeners.get(i).getClass().getName()+" with nearest city="+nearestCityName);
                         bboxListeners.get(i).bboxChanged(bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3], container_width, container_height,lat,lon,zoomLevel,st_ids,cityN);
                         // bboxListeners.get(i).locationChanged(lat, lon);
                     }
                 } 
//                 for (int i=0; i<metarLocationListeners.size(); i++) {
//                final int index = i;
//                Thread t = new Thread() {
//                    public void run() {
//                         metarLocationListeners.get(index).setMapSize(w, h);
//                    }
//                };
//                t.start();
//                
//            }
             }
         };
         t.start();
     }
     
     private void clearMapCache() {
         for (int i=0; i<mapCache.length; i++) {
             if (mapCache[i] != null) mapCache[i].flush();
         }
          mapCache = new MapCache[7];
     }
     public ArrayList<String> getCachedStateList(int zoomLevel) {
        if (mapCache[zoomLevel] == null) {
           //  System.out.println("There is no map cache for zoom Level"+zoomLevel);
             return null;
         }
         try {
             return mapCache[zoomLevel].getStateIds();
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
     
     public ArrayList<MetarLocationObject> getCachedMetarList(int zoomLevel) {
         if (mapCache[zoomLevel] == null) {
           //  System.out.println("There is no map cache for zoom Level"+zoomLevel);
             return null;
         }
         try {
             return mapCache[zoomLevel].getMetarStationList();
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
     public BufferedImage getCachedMap(int zoomLevel) {
        // System.out.println("Get cached map for zoomLevel="+zoomLevel);
         if (mapCache[zoomLevel] == null) {
           //  System.out.println("There is no map cache for zoom Level"+zoomLevel);
             return null;
         }
         try {
             return mapCache[zoomLevel].getImage();
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
         
     }
     
     public void clearMap(Graphics2D g2d) {
         //this.roadPathHash.clear();
         //this.countyPathHash.clear();
         g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, (container_width), (container_height));
        g2d.setComposite(AlphaComposite.SrcOver);
     }
       public void drawMap(BufferedImage img) {
           this.container_width = img.getWidth();
           this.container_height = img.getHeight();
           
           this.g2d = img.createGraphics();
           g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
           g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
           g2d.setColor(new Color(255,255,255,0));
           g2d.fillRect(0,0,container_width,container_height);
           loadMapLayers(g2d);
           g2d.dispose();
       }
       public String getWmsBbox() {
           return bbox[0]+","+bbox[1]+","+bbox[2]+","+bbox[3];
       }
       public ArrayList<String> getStateIdsList() {
           return this.stateIds;
       }
       public String getNearestCity() {
           return this.nearestCityName;
       }
       public String[] getStateIdsArray() {
           String[] retVAl = new String[stateIds.size()];
           for (int i=0; i<stateIds.size(); i++) {
               retVAl[i] = stateIds.get(i);
           }
           return retVAl;
       }
  // Lower LEft lat / lon upper right lat lon
        public double[]  getBbox() {
            //System.out.println("Get Bbox");
            
            int w = container_width;
            int h = container_height;
            //System.out.println("container width="+w+" height="+container_height);
            
            double zoomFactor = zoomFactors[zoomLevel];
            //var aspect = w/h;
            double aspect = Double.valueOf(w)/Double.valueOf(h)*1.2;
            
            double tweekLat = zoomFactor/aspect;
            double tweekLon = zoomFactor;
            
            //System.out.println("WmsRadarViwer: tweekLat="+tweekLat+ " tweekLon="+tweekLon);

            double a = lat-tweekLat;
            double b = lon-tweekLon;
            double c = lat+tweekLat;
            double d = lon+tweekLon;
            dppX = Math.abs((b-d) / w);
            dppY = Math.abs((a-c) / h);
            double retVal[] = {a,b,c,d};
            // System.out.println("Bbox="+a+" "+b+" "+c+" "+d);
            return retVal;
        }
      public Color getMapBgColor() {
          return mapBgColor;
      }
         public void setMapBgColor(Color color) {
          this.mapBgColor = color;
          //refreshMap();
      }
      public void setRoadColor(Color color) {
          this.roadColor = color;
         // refreshMap();
      }
      public void setInterstateColor(Color color) {
          this.interstateColor = color;
          //refreshMap();
      }
      public void setStateColor(Color color) {
          this.stateColor = color;
          //refreshMap();
      }
      public void setCityFonts(Font[] fourCityFontsArray) {
          cityFont[0] = fourCityFontsArray[0];
          cityFont[1] = fourCityFontsArray[1];
          cityFont[2] = fourCityFontsArray[2];
          cityFont[3] = fourCityFontsArray[3];
          String family = cityFont[3].getFamily();
         
          int size = cityFont[3].getSize();
          Font f = new Font(family,Font.BOLD,size+1);
          cityFont[4] = f;
          f = new Font(family,Font.BOLD,size+3);
          cityFont[5] = f;
          //refreshMap();
      }
      private void refreshMap() {
        if (img == null)return;
        Graphics2D g2d = img.createGraphics();
        clearMap(g2d);
        loadMapLayers(g2d);
        g2d.dispose();
      }
      private void readSettings(Properties props) {
          this.cityToMetarProps = Utilities.readPrefs(Utilities.SEP+"Maps"+Utilities.SEP+"CityToMetar.properties");
          
          if (props.containsKey("InterstateColor")) {
            String[] arr = props.getProperty("InterstateColor").split(",");
            interstateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            
        }
        if (props.containsKey("StateColor")) {
            String[] arr = props.getProperty("StateColor").split(",");
            stateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            
        }
        if (props.containsKey("RoadColor")) {
            String[] arr = props.getProperty("RoadColor").split(",");
            roadColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            
        }
         if (props.containsKey("MapBgColor")) {
            String[] arr = props.getProperty("MapBgColor").split(",");
            mapBgColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            
        }
        if (props.containsKey("City1Font") 
                && props.containsKey("City2Font") 
                && props.containsKey("City3Font")
                && props.containsKey("City4Font")) {
            String[] arr = props.getProperty("City1Font").split(",");
            int bold = Integer.valueOf(arr[2]);
            Font city1Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City2Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city2Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City3Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city3Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City4Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city4Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            cityFont[0] = city1Font;
            cityFont[1] = city2Font;
            cityFont[2] = city3Font;
            cityFont[3] = city4Font;
            int size = cityFont[3].getSize();
            String family = cityFont[3].getFamily();
            Font f = new Font(family,Font.BOLD,size+1);
            cityFont[4] = f;
            f = new Font(family,Font.BOLD,size+3);
            cityFont[5] = f;
        }
      }
      
      private void loadMapLayers(Graphics2D g2d) {
           
//        for (int i=0; i<metarLocationListeners.size(); i++) {
//             this.metarLocationListeners.get(i).boundingBoxChanged(Math.random(),this.container_width,this.container_height);
//         }
         double aspect = (double)container_width/(double)container_height;
        panMapHeight = container_height+1000;
        panMapWidth = (int) (panMapHeight * aspect);
        int translateX = (panMapWidth-container_width)/2;
        int translateY= (panMapHeight-container_height)/2;
        if (panMapImage != null) panMapImage.flush();
        panMapImage = new BufferedImage(panMapWidth,panMapHeight,BufferedImage.TYPE_INT_ARGB);
        panMapCities = new ArrayList<City>();
        panMapGraphics = panMapImage.createGraphics();             
        
        panMapLowerLeftLat = bbox[0]-(translateY*dppY);
        panMapLowerLeftLon = bbox[1]-(translateX*dppX);
        panMapUpperRightLat = bbox[2]+(translateY*dppY);
        panMapUpperRightLon = bbox[3]+(translateX*dppX);
        
        
       
        MapCache cache = new MapCache(zoomLevel);
        mapCache[zoomLevel] = cache;
        
        // Have to load states first
        panMapGraphics.translate(translateX,translateY);
        loadStates(g2d, container_width, container_height);
        panMapGraphics.translate(-1*translateX,-1*translateY);
        loadTheCounties(g2d);
        loadTheRoads(g2d, container_width, container_height);

        BufferedImage newBuf = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newBuf.createGraphics();
        newBuf = smoothImage(img);
        g.dispose();
        clearMap(g2d);
        g2d.drawImage(newBuf, 0, 0, null);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        loadCities(g2d, container_width, container_height);
        
        newBuf = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        g = newBuf.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        if (useCache == true) {
        mapCache[zoomLevel].setCachedImage(newBuf);
        mapCache[zoomLevel].setMetarStationList(metarStationLocations);
        }        
        g2d.dispose();
        if (useCache == true) {
        Thread t = new Thread() {
            public void run() {
                loadPanMapCities();
                try {
                    if (useCache == true) {
                        mapCache[zoomLevel].setPanMapImage(panMapImage);
                    }
                } catch (Exception ex) {}
                panMapGraphics.dispose();
            }
        };
        t.start();
        }
    }

    private BufferedImage smoothImage(BufferedImage inputImage) {
//            float[] filter2 = new float[]{
//                1.0f / 121.0f, 2.0f / 121.0f, 3.0f / 121.0f, 2.0f / 121.0f, 1.0f / 121.0f,
//                2.0f / 121.0f, 7.0f / 121.0f, 11.0f / 121.0f, 7.0f / 121.0f, 2.0f / 121.0f,
//                3.0f / 121.0f, 11.0f / 121.0f, 50.0f / 121.0f, 11.0f / 121.0f, 3.0f / 121.0f,
//                2.0f / 121.0f, 7.0f / 121.0f, 11.0f / 121.0f, 7.0f / 121.0f, 2.0f / 121.0f,
//                1.0f / 121.0f, 2.0f / 121.0f, 3.0f / 121.0f, 2.0f / 121.0f, 1.0f / 121.0f};
        float[] filter1 = new float[]{
            3.0f / 121.0f, 0.0f / 121.0f, 3.0f / 121.0f,
            0.0f / 121.0f, 1.0f, 0.0f / 121.0f,
            3.0f / 121.0f, 0.0f / 121.0f, 3.0f / 121.0f
        };

        Kernel kernel = new Kernel(3, 3, filter1);
        ConvolveOp cop = new ConvolveOp(kernel);
        return cop.filter(inputImage, null);
    }
        private void loadTheRoads (Graphics2D g2d,int w,int h) {
//            if (states_layer_loaded == true) {
//                for (var i = 0; i < includedStateIDs.length; i++) {
//                    var st = includedStateIDs[i];
                    loadRoads(g2d, w, h);
//                }
//            } else {
//                setTimeout(function () {loadTheRoads(ctx,w,h);},100);
//            }
        }
        
       
   private void loadRoads(Graphics2D g2d, int w, int h) {
        if (zoomLevel < 0) {

            return;// dont show roads at zoomed out distances
        }
        Hashtable<String, GeneralPath> roadPathHash = new Hashtable<String, GeneralPath>();
        hashIndex = 0;
        for (int i = 0; i < stateIds.size(); i++) {
            String path = "/mappingtools/maps/" + stateIds.get(i) + "_Roads.json";
            roadsJson = Utilities.getInstance().readFromJar(path);
            if (roadsJson != null && roadsJson.equals("") == false) {
                loadRoadsRunner(roadsJson, roadPathHash, g2d, w, h);
            }
            
        }
     

    }
   
        public String getStateNameInBboxFast(int x, int y) {
            if (mapCache[zoomLevel] != null) {
                return mapCache[zoomLevel].getStateNameAtPoint(x, y);
            }
            return "";
        }
        public String getCountyNameInBboxFast(int x, int y) {
            if (mapCache[zoomLevel] != null) {
                return mapCache[zoomLevel].getCountyNameAtPoint(x, y);
            }
            return "";
        }
        
        public String getRoadNameInBboxFast(Rectangle rect) {
            if (mapCache[zoomLevel] != null) {
                return mapCache[zoomLevel].getRoadNameInRectangle(rect);
            }
            return "";
        }

         
         private void loadRoadsRunner(String data,Hashtable<String,GeneralPath> roadPathHash,Graphics2D g2d,int w,int h) {
            
            try {
                
                JSONObject obj = new JSONObject(data);
                JSONArray features = obj.getJSONArray("features");
                //System.out.println("WmsRadarViewer: There are " + features.length() + " road features");
                for (int i = 0; i < features.length(); i++) {
                    JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coord");
                     int numCoords = coords.length();
                     //System.out.println("There are "+numCoords+" coordinates");
                    //double testLon = Double.valueOf(coords.get(0).toString());
                    //double testLat = Double.valueOf(coords.get(1).toString());
                    String type = "Unknown";
                    try {
                           type = features.getJSONObject(i).getJSONObject("props").getString("ROUTE");
                    } catch (Exception ex) {
                        
                    }
                    //System.out.println("Working on Road "+type);
                    boolean skip = false;
                    try {
                        if (type.substring(0, 1).equals("I")) {
                            g2d.setColor(interstateColor);
                           
                            if (zoomLevel <= 3) {
                                g2d.setStroke(new BasicStroke(3.0f));
                            } else {
                                g2d.setStroke(new BasicStroke(5.0f));
                            }
                        } else if (zoomLevel == 3) {
                          
                            g2d.setColor(roadColor);
                            if (type.substring(0,2).equals("US")) {
                                g2d.setColor(Color.WHITE);
                                g2d.setStroke(new BasicStroke(4.0f));
                                g2d.setColor(roadColor);
                                g2d.setStroke(new BasicStroke(2.0f));
                                //System.out.println("US Road");
                            } else {
                                g2d.setColor(roadColor);
                                g2d.setStroke(new BasicStroke(1.0f));
                                skip = true;
                            }
                        } else if (zoomLevel >= 4) {
                            if (type.substring(0,1).equals("I")) {
                                g2d.setColor(interstateColor);
                                g2d.setStroke(new BasicStroke(5.0f));
                            } else if (type.substring(0,2).equals("US")) {
                                 g2d.setColor(roadColor);
                                g2d.setStroke(new BasicStroke(3.0f));
                            } else  {
                               g2d.setColor(roadColor);
                               g2d.setStroke(new BasicStroke(2.0f));
                            }
                        } else {
                            skip = true;
                        }
                    } catch (Exception ex) {
                        skip = true;
                        g2d.setColor(roadColor);
                        g2d.setStroke(new BasicStroke(1.0f));
                    }
                    if (skip == false) {
                        
                    boolean start = false;
                    GeneralPath path = new GeneralPath();
                    for (int j = 0; j < numCoords; j++) {
                        double testLon = 0;
                        double testLat = 0;
                        
                        try {
                         testLon = coords.getJSONArray(j).getFloat(0);
                         testLat = coords.getJSONArray(j).getFloat(1);
                        // System.out.println("Road "+type+" has coords "+testLon+","+testLat);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (testLon != 0 && testLat != 0) {
                            
                        //double testLon = Double.valueOf(coords.get(0).toString());
                        //double testLat = Double.valueOf(coords.get(1).toString());
                        if (bboxContainsPoint(testLon, testLat)) {
                             
                             if (start == false) {
                                start = true;
                                int[] xy = null;
                                if (j > 0) {
                                    int n = j - 1;
                                    xy = worldToPixel(testLon, testLat, w, h);
                                } else {
                                    xy = worldToPixel(testLon, testLat, w, h);
                                }
                                
                                
                                path.moveTo(xy[0], xy[1]);
                            } else {
                                int[] xy = worldToPixel(testLon,testLat, w, h);
                                path.lineTo(xy[0], xy[1]);
                            }
                        } else {
                            if (start == true) {
                                try {
                                    //var n = j + 1;
                                    int[] xy = worldToPixel(testLon,testLat, w, h);
                                    path.lineTo(xy[0], xy[1]);
                                } catch (Exception ex) {ex.printStackTrace();
                                }
                                roadPathHash.put(type+":"+hashIndex, path);
                                hashIndex++;
                                BasicStroke bsi = (BasicStroke) g2d.getStroke();
                                float lw = bsi.getLineWidth();
                                lw = lw + 1.5f;
                                Color c = g2d.getColor();
                                g2d.setColor(WeatherMonitorRun.mapBgColor);
                                g2d.setStroke(new BasicStroke(lw));
                                g2d.draw(path);
                                g2d.setStroke(bsi);
                                g2d.setColor(c);
                                g2d.draw(path);
                                //path.stroke();
                                start = false;
                            }
                        }
                    }
                    }
                    if (start == true) {
                        BasicStroke bsi = (BasicStroke) g2d.getStroke();
                        float lw = bsi.getLineWidth();
                        lw = lw + 1.5f;
                        Color c = g2d.getColor();
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(lw));
                        g2d.draw(path);
                        g2d.setStroke(bsi);
                        g2d.setColor(c);
                        g2d.draw(path);
                        //g2d.draw(path);
                        roadPathHash.put(type + ":" + hashIndex, path);
                        hashIndex++;
                        start = false;
                    }
                }
            }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            mapCache[zoomLevel].setRoadPathHash(roadPathHash);
            
      }

        private void loadCities(Graphics2D g2d, int w, int h) {
            
            
                paintUserCity = false;

                plottedCityX = new ArrayList();
                plottedCityY = new ArrayList();
                if (citiesJson != null) {
                    System.out.println("ParseCities from json");
                     parseCities(citiesJson,g2d, w, h);
                } else {
                    //System.out.println("Read City Json");
                    String path = "/mappingtools/maps/Cities.json";
                    citiesJson = Utilities.getInstance().readFromJar(path);
                   
                    parseCities(citiesJson, g2d, w,h);
                   
                }
            
        }
        
       
        public void parseCities(String data,Graphics2D g2d,int w,int h) {
           // System.out.println("parse cityes");
          // Properties cityProps = new Properties();
            JSONObject obj = new JSONObject(data);
            ArrayList<City> parsed = new ArrayList<City>();
            int[] xyl = worldToPixel(lon,lat,w,h);
            JSONArray features = obj.getJSONArray("features");
                //System.out.println("WmsRadarViewer: There are " + features.length() + " county features");
                for (int i = 0; i < features.length(); i++) {
                    String name = features.getJSONObject(i).getJSONObject("prop").getString("N");
                    String state = "";
                    try {
                        state = features.getJSONObject(i).getJSONObject("prop").getString("ST");
//                        if (state.equals("TX")) {
//                            cityProps.put(name+","+state,"");
//                        }
                    } catch (Exception ex) {
                        System.out.println("City name "+name+" has bad state");
                    }
                    Integer major = features.getJSONObject(i).getJSONObject("prop").getInt("M");
                    JSONArray coords = features.getJSONObject(i).getJSONObject("geom").getJSONArray("coord");
                    double testLon = Double.valueOf(coords.get(0).toString());
                    double testLat = Double.valueOf(coords.get(1).toString());
                    int[] xy = bboxContains(testLon,testLat,major,w,h,name,parsed);
                    
                    if (xy != null) {

                        City city = new City(testLon, testLat, xy[0], xy[1], name, state, 1, major);
                        // System.out.println("Workjing on city "+city.getCityName());
                        double distMiles = Utilities.getDistanceBetweenTwoPoints(lon, lat, city.getLon(), city.getLat()) * 60.0;
                        if (distMiles < nearestCityDistance) {
                            // System.out.println("Neareset city to lat:"+lat+" lon:"+lon+" is "+city.getCityName());
                            nearestCityName = city.getCityName() + ", " + city.getState();
                            nearestCityDistance = distMiles;
                        }
                        if (major > 0) {

                            ArrayList<City> l = new ArrayList<>();
                            l.add(city);
                            for (int k = 0; k < parsed.size(); k++) {
                                l.add(parsed.get(k));
                            }
                            parsed.clear();
                            parsed.addAll(l);

                        } else {
                            parsed.add(city);
                        }

                    }
                    xy = panBboxContainsPoint(testLon,testLat,major);
                    if (xy != null) {
                        City city = new City(testLon, testLat, xy[0], xy[1], name, state, 1, major);
                        panMapCities.add(city);
                    }
                }
                //sort the cities
                ArrayList<City> sorted = new ArrayList<City>();
                for (int i =0 ; i<parsed.size(); i++) {
                    if (parsed.get(i).getMajorCity() == 3) {
                        sorted.add(parsed.get(i));
                    }
                }
                for (int i =0 ; i<parsed.size(); i++) {
                    if (parsed.get(i).getMajorCity() == 2) {
                        sorted.add(parsed.get(i));
                    }
                }
                for (int i =0 ; i<parsed.size(); i++) {
                    if (parsed.get(i).getMajorCity() == 1) {
                        sorted.add(parsed.get(i));
                    }
                }
                for (int i =0 ; i<parsed.size(); i++) {
                    if (parsed.get(i).getMajorCity() == 0) {
                        sorted.add(parsed.get(i));
                    }
                }

                ArrayList<City> l = new ArrayList<>();
                //l.add(city);
                for (int i=0; i<sorted.size(); i++) {
                    l.add(sorted.get(i));
                }
                sorted.clear();
                sorted.addAll(l);
                //Utilities.savePrefs(cityProps, "/Maps/CityToMetar.properties");
                loadCitiesRunner(sorted,g2d,w,h);
                
        }

        private void loadCitiesRunner(ArrayList<City> parsedCities,Graphics2D g2d, int w,int h) { // data is parsed cities
            
            ArrayList<City> plottedCities = new ArrayList<City>();
            metarStationLocations = new ArrayList<MetarLocationObject>();
          // System.out.println("Adding "+parsedCities.get(0).getCityName());
           plottedCities.add(parsedCities.get(0));
            try {
                //System.out.println("WmsRadarViewer: There are " + parsedCities.size() + " city features");
                for (int i = 0; i < parsedCities.size(); i++) {
                    City city =  parsedCities.get(i);
                    String preferredMetarId = null;
                    int x = city.getX();
                    int y = city.getY();
                    
                    String name = city.getCityName();
                    int vis = city.isVisible();
                    int major = city.getMajorCity();
                    String citystate = name+","+city.state;
                    if (cityToMetarProps != null && cityToMetarProps.containsKey(citystate)) {
                         preferredMetarId = cityToMetarProps.getProperty(citystate);
                         System.err.println("Use "+preferredMetarId +" for city "+name);
                    }
                    if (vis == 1 && cityNameOverlaps(g2d,plottedCities,x,y,name,major) == false) {
                        if (g2d != null) {
                            g2d.setStroke(new BasicStroke(1f));
                        }
                       
                        int diam = 10;

                        if (g2d != null) {
                            g2d.setColor(Color.YELLOW);
                        }
                        
                        if (g2d != null) {
                            Ellipse2D.Double circle = new Ellipse2D.Double(x - (diam / 2), y - (diam / 2), diam, diam);
                            x = x - 10;
                            y = y - 10;
                            g2d.fill(circle);
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.draw(circle);
                           
                                g2d.setFont(cityFont[major]);
                                g2d.setColor(Color.WHITE);
                                g2d.drawString(name, x + 2, y + 2-5);
                                g2d.drawString(name, x + 2, y - 2-5);
                                g2d.drawString(name, x - 2, y + 2-5);
                                g2d.drawString(name, x - 2, y - 2-5);
                                g2d.setColor(Color.BLACK);
                                g2d.drawString(name, x, y-5);
                           // }

                            Point2D.Double p2d = new Point2D.Double(city.getLon(), city.getLat());
                            MetarLocationObject mlo = new MetarLocationObject(p2d,city.name,city.state);
                            if (preferredMetarId != null) mlo.setPreferredStationId(preferredMetarId);
                            metarStationLocations.add(mlo);
                            
                        }
                      
                        plottedCities.add(city);
                    }
                }
                yourLocation = new Point2D.Double(lon,lat);
                int[] xy = worldToPixel(lon,lat,container_width,container_height);
                g2d.setColor(Color.RED);
                Ellipse2D.Double circle = new Ellipse2D.Double(xy[0] - (14 / 2), xy[1] - (14 / 2), 14, 14);
                g2d.fill(circle);
                g2d.setColor(Color.DARK_GRAY);
                g2d.draw(circle);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            for (int i=0; i<metarLocationListeners.size(); i++) {
                        metarLocationListeners.get(i).setMetarPlotList(metarStationLocations,new Point2D.Double(lon,lat),stateIds,this.container_width, this.container_height,bbox,zoomLevel);
            }
        }
       private void loadPanMapCities() {
       int diam = 10;    
          if (panMapGraphics == null) {
            return;
        }
        for (int i = 0; i < panMapCities.size(); i++) {
            City city = panMapCities.get(i);
            int x = city.x;
            int y = city.y;
            String name = city.name;
            int major = city.major;
            Ellipse2D.Double circle = new Ellipse2D.Double(x - (diam / 2), y - (diam / 2), diam, diam);
            x = x - 10;
            y = y - 10;
            panMapGraphics.setColor(Color.YELLOW);
            panMapGraphics.fill(circle);
            panMapGraphics.setColor(Color.DARK_GRAY);
            panMapGraphics.setStroke(new BasicStroke(1f));
            panMapGraphics.draw(circle);

            panMapGraphics.setFont(cityFont[major]);
            panMapGraphics.setColor(Color.WHITE);
            panMapGraphics.drawString(name, x + 2, y + 2 - 5);
            panMapGraphics.drawString(name, x + 2, y - 2 - 5);
            panMapGraphics.drawString(name, x - 2, y + 2 - 5);
            panMapGraphics.drawString(name, x - 2, y - 2 - 5);
            panMapGraphics.setColor(Color.BLACK);
            panMapGraphics.drawString(name, x, y - 5);

            

        }
    }
       private boolean cityNameOverlaps(Graphics2D g2d,ArrayList<City> parsedCities,int x,int y,String name,int maj) {
            //if (name.equals("Your Location")) return false;
//     
            if (maj < 1 && zoomLevel <= 2) {
                return true;
      
            }
            boolean disposeG2d = false;
            if (g2d == null) {
                disposeG2d = true;
                BufferedImage testImg = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
                g2d = testImg.createGraphics();
            }
            FontMetrics metrics = g2d.getFontMetrics(cityFont[maj]);
                    Rectangle2D rect = metrics.getStringBounds(name, g2d);
                    
                    double Width = rect.getWidth();
                    
                    double Height = rect.getHeight();
                    int buffer = 35;
                    Rectangle2D testRect2 = new Rectangle2D.Double(x-buffer,y-buffer,Width+buffer*2,Height+buffer*2);
                        
            if (maj <= 3) {
                for (int i=0; i<parsedCities.size(); i++) {
                    int testX = parsedCities.get(i).getX();
                    int testY = parsedCities.get(i).getY();
                    String testName = parsedCities.get(i).getCityName();
                    int testMaj = parsedCities.get(i).getMajorCity();
                    int vis = parsedCities.get(i).isVisible();
                    
                    
                     
                     //var testName = data[i][2];
                     if ( vis == 1 && testName.equals(name) == false) { 
                         metrics = g2d.getFontMetrics(cityFont[testMaj]);
                        Rectangle2D rect2 = metrics.getStringBounds(testName, g2d);
                        
                        double testWidth = rect2.getWidth();
                        
                        double testHeight = rect2.getHeight();
                        Rectangle2D testRect1 = new Rectangle2D.Double(testX,testY,testWidth,testHeight);
                       
                        if (testRect1.intersects(testRect2)) { // do not paint cities whose beginning text overlaps the end of a previously plotted city
                            if (testMaj < maj) {
                            //System.out.println("Is looks like lesser city with name "+name+" and major="+maj+" overlaps the end of previously plotted city "+testName); 
                                parsedCities.get(i).setVisible(0);
                            } else {
                                if (disposeG2d == true) g2d.dispose();
                                return true;
                            }
                        }
                     }
                }
            }
            if (disposeG2d == true) g2d.dispose();
            return false;
        }
      
        private void loadStates(Graphics2D g2d, int w, int h) {
            stateIds.clear();        
            int hashIndex = 0;
            if (statesJson != null) {
                loadStatesRunner(g2d,statesJson,w,h);
            } else {
                String path = "/mappingtools/maps/States.json";
                statesJson = Utilities.getInstance().readFromJar(path);
                loadStatesRunner(g2d,statesJson,w,h);
            }
            //downloadStationData(stateIds);
//            for (int i=0; i<stateIds.size(); i++) {
//                System.out.println("State id "+stateIds.get(i));
//            }
             mapCache[zoomLevel].setStateIds(stateIds);
        }
        

        public BufferedImage getMapImage() {
            return img;
        }
        public BufferedImage getPanMapImage() {
            if (useCache == true) {
                BufferedImage cPanMapImage = null;
                try {
                    cPanMapImage = mapCache[zoomLevel].getPanMapImage();
                } catch (Exception ex) {
                    return null;
                }
                if (cPanMapImage != null) {
                    return cPanMapImage;
                }
            }
            return panMapImage;
        }
        public BufferedImage getMapBgImage() {
            if (useCache == true) {
                BufferedImage cmapBgImage = null;
                try {
                    cmapBgImage = mapCache[zoomLevel].getMapBgImage();
                } catch (Exception ex) {
                    return null;
                }
                if (cmapBgImage != null) {
                    return cmapBgImage;
                }
            }
            return mapBgImage;
        }
        private void loadStatesRunner(Graphics2D g2d, String jsonData, int w, int h) {
            Hashtable<String,GeneralPath> statePathHash = new Hashtable<String, GeneralPath>();
            String name = "";
            if (zoomLevel >= 5) {
                g2d.setStroke(new BasicStroke(8.0f));
            } else if (zoomLevel >= 4) {
                g2d.setStroke(new BasicStroke(7.0f));
            } else if (zoomLevel >= 3) {
                g2d.setStroke(new BasicStroke(6.0f));
            } else {
                g2d.setStroke(new BasicStroke(4.0f));
            }
        

        g2d.setColor (stateColor);
        JSONObject obj = new JSONObject(jsonData);
        JSONArray features = obj.getJSONArray("features");
        if (mapBgImage != null) mapBgImage.flush();
        mapBgImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D mapBgGraphics = mapBgImage.createGraphics();
       
               
              for (int i = 0; i < features.length(); i++) {
                name = features.getJSONObject(i).getJSONObject("properties").getString("NAME");
                String id = features.getJSONObject(i).getJSONObject("properties").getString("STATE");
                if (state_id.equals(state_id)) {
                    GeneralPath path = new GeneralPath();
                    JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                    //System.out.println("Working on state:"+id);
                    processCoordinates(STATE_MAP,statePathHash, name,id, coords, g2d, mapBgGraphics,w, h);
                  
                }
            }
               mapCache[zoomLevel].setStatePathHash(statePathHash);
              mapBgGraphics.dispose();  
              if (useCache == true) {
                mapCache[zoomLevel].setMapBgImage(mapBgImage);
              }
}

        
private void processCoordinates(int mapType, Hashtable<String,GeneralPath> pathHash, String name, String stateId, JSONArray arr, Graphics2D g2d, Graphics2D mapBgGraphics, int w, int h) {
    //System.out.println("There are "+arr.length()+" arrays in the coords");
    
    int numArrays = arr.length();
    for (int i=0; i<arr.length(); i++) {
        try {
            int len1 = arr.getJSONArray(i).getJSONArray(0).length();
            int len = len1;
            int len2 = arr.getJSONArray(i).length();
            if(len2 > len1) len = len2;
            
            //System.out.println("Array "+i+" has len1="+len1+" len2="+len2);
            GeneralPath path = new GeneralPath();
            
            double testLon; double testLat;
            if (numArrays == 1) {
                testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).get(0).toString());
                testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).get(1).toString());
            } else {
                try {
                    testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).getJSONArray(0).get(0).toString());
                    testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).getJSONArray(0).get(1).toString());
                } catch (Exception ex) {
                    testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).get(0).toString());
                    testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).get(1).toString());
                }
            }
            int[] XY = worldToPixel(testLon,testLat,w,h);
            path.moveTo(XY[0],XY[1]);
            for (int j=1; j<len; j++) {
                if (numArrays == 1) {
                     testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(j).get(0).toString());
                     testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(j).get(1).toString());
                } else {
                    try {
                        testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).getJSONArray(j).get(0).toString());
                        testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(0).getJSONArray(j).get(1).toString());
                    } catch (Exception ex) {
                        testLon = Double.parseDouble(arr.getJSONArray(i).getJSONArray(j).get(0).toString());
                        testLat = Double.parseDouble(arr.getJSONArray(i).getJSONArray(j).get(1).toString());
                    }
                }
                 XY = worldToPixel(testLon,testLat,w,h);
                 path.lineTo(XY[0],XY[1]);
            }
            path.closePath();
            pathHash.put(name+":"+Integer.toString(hashIndex), path);
                hashIndex++;
            if (mapType == COUNTY_MAP) {
                g2d.draw(path);
                pathHash.put(name+":"+Integer.toString(hashIndex), path);
                hashIndex++;
            } else {
               
                //Working on a state path
                pathHash.put(name+":"+Integer.toString(hashIndex), path);
                hashIndex++;
                 if (path.intersects(0,0,w,h)) {
                     mapBgGraphics.setColor(mapBgColor);
                     
                     mapBgGraphics.fill(path);
                     g2d.setColor(stateColor);
                     
                     g2d.draw(path);
                    //System.out.println("State "+name+" is in bounds");
                    if (stateIds.contains(name) == false) {
                        if  (stateIds.contains(stateId) == false) stateIds.add(stateId);
                    }
                }
                if (panMapGraphics != null) {
                   panMapGraphics.setColor(mapBgColor);
                   panMapGraphics.fill(path);
                   panMapGraphics.setColor(g2d.getColor());
                   panMapGraphics.setStroke(g2d.getStroke());
                   panMapGraphics.draw(path);
                }
            }
            //g2d.setStroke(new BasicStroke(4.0f));
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
   
}


        
       private void loadTheCounties(Graphics2D g2d) {
           
           loadCounties(g2d, container_width, container_height);
              
       }
        
      private void loadCounties(Graphics2D g2d,int w,int h) {
          Hashtable<String,GeneralPath> countyPathHash = new Hashtable<String, GeneralPath>();
           //System.out.println("WmsRadarViewer: load counties w="+w+" h="+h);
          for (int i = 0; i < stateIds.size(); i++) {
              //System.out.println("Loading couinties for state " + stateIds.get(i));
              String path = "/mappingtools/maps/" + stateIds.get(i) + "_Counties.json";

              countiesJson = Utilities.getInstance().readFromJar(path);
              if (countiesJson != null && countiesJson.equals("") == false) {
                  loadCountiesRunner(countiesJson, countyPathHash, g2d, w, h);
              }
          }
            
      }
        private void loadCountiesRunner(String data,Hashtable<String,GeneralPath> countyPathHash, Graphics2D g2d, int w, int h) {
            int hashIndex = 0;
            String name = "";
            try {
                
                 if (zoomLevel >= 5) {
                     g2d.setStroke(new BasicStroke(3f));
                     g2d.setPaint(countyLineColorZ7);
                 }  else if (zoomLevel >= 4) {
                    g2d.setStroke(new BasicStroke(2f));
                     g2d.setPaint(countyLineColorZ6);
               
                
                } else if (zoomLevel >= 3) {
                    g2d.setStroke(new BasicStroke(1f));
                     g2d.setPaint(countyLineColorZ4);
               
                } else {
                    return;// dont show at zoomed out levels
                }
                 GeneralPath path = null;
                 
                 JSONObject obj = new JSONObject(data);
                 JSONArray features = obj.getJSONArray("features");
                
                for (int i = 0; i < features.length(); i++) {
                      name = features.getJSONObject(i).getJSONObject("properties").getString("NAME");
                     
                     JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                     processCoordinates(COUNTY_MAP,countyPathHash,name,name,coords,g2d,null,w,h);
                   
                 }  
                


            } catch (Exception ex) {
                System.out.println("Error for county "+name);
                ex.printStackTrace();
            }
           
            mapCache[zoomLevel].setCountyPathHash(countyPathHash);
        }
      

        private boolean bboxIntersectsShape(double testLat, double testLon) {
             if (bboxContainsPoint(testLon,testLat)) {
                           // System.out.println("bbox contains "+testLon+" , "+testLat);
                            return true;
                        } 
             return false;
        }
        private boolean bboxIntersectsShape_3(JSONArray coords) {
            
           
                for (int i = 0; i < coords.length(); i++) {
                     double testLon = Double.valueOf(coords.getJSONArray(0).getJSONArray(0).get(0).toString());
                    double testLat = Double.valueOf(coords.getJSONArray(0).getJSONArray(0).get(1).toString());
                    //double testLon = coords.getJSONArray(i).getJSONArray(0).getDouble(0);
                    //double testLat = coords.getJSONArray(i).getJSONArray(0).getDouble(1);
                    
                        if (bboxContainsPoint(testLon,testLat)) {
                           // System.out.println("bbox contains "+testLon+" , "+testLat);
                            return true;
                        } 
                    
                }
//            } else if (dim == 4) {
//                for (int x = 0; x < coords.length; x++) {
//                    for (int y = 0; y < coords[x].length; y++) {
//                        for (int z = 0; z < coords[x][y].length; z++) {
//                            if (bboxContainsPoint(coords[x][y][z][0], coords[x][y][z][1])) {
//                                return true;
//                            }
//                        }
//
//                    }
//                }
//            } else {
 //               return false;
  //          }
//            if (bboxContainsPoint(bboxGetBounds_3(coords),lon,lat)) {
//                return true;
//            }
            return false;
        }
        private int[] panBboxContainsPoint(double lon, double lat, int major) {
            if (major < 1) return null;
            if (isInPanMapBounds(lat,lon) && isInMapBounds(lat,lon) == false) {
                return panMapWorldToPixel(lon,lat,panMapWidth,panMapHeight);
            }
            return null;
        }
      private boolean bboxContainsPoint(double lowerLeftLat, double lowerLeftLon, double upperRightLat, double upperRightLon, double testLat, double testLon) {
          if (testLat >  lowerLeftLat &&  testLat < upperRightLat && testLon > lowerLeftLon && testLon < upperRightLon) {
              return true;
          }
          return false;
      }
      private boolean bboxContainsPoint(double testLon,double testLat) {
       
          double lowerLeftLat = bbox[0];
          double lowerLeftLon = bbox[1];
          double upperRightLat = bbox[2];
          double upperRightLon = bbox[3];
          //var testLat = parseFloat(t_lat);
         // var testLon = parseFloat(t_lon);
          if (testLat >  lowerLeftLat &&  testLat < upperRightLat && testLon > lowerLeftLon && testLon < upperRightLon) {
              return true;
          }
          return false;
      }
      
      private int[] bboxContains(double testLon,double testLat,int major,int w,int h,String name,ArrayList<City> parsed) {
          if (name.equals("Your Location")) return null;
         
          try {
          
          //var arr = bbox.split(","); 
          //var arr = getBbox();
          double lowerLeftLat = bbox[0];
          double lowerLeftLon = bbox[1];
          double upperRightLat = bbox[2];
          double upperRightLon = bbox[3];
          //var testLat = parseFloat(t_lat);
          //var testLon = parseFloat(t_lon);
          int maxX = w-12; int maxY = h-12;
          if (testLat >  lowerLeftLat &&  testLat < upperRightLat && testLon > lowerLeftLon && testLon < upperRightLon) {
            
            int[] xy = worldToPixel(testLon,testLat,w,h);
            if (xy[0] <= maxX && xy[0] >= 5 && xy[1] >= 12 && xy[1] <= maxY) {
              int[] my = worldToPixel(lon,lat,w,h);
              double d2 = getDistanceBetweenTwoPoints(xy[0], xy[1],my[0],my[1]);
            
//              if (d2 <= 20) {
//                    
//                  paintUserCity = true;
//                  return null;
//              } else if (d2 <= 21) {// do not paint cities near map center
//                 
//                  return null;
//              }
              //console.log("major is "+major+" for city "+name);
                    if (major == 2) { // erase everything near it and plot this instead
                        for (int i = 0; i < parsed.size(); i++) {
                            double testX = parsed.get(i).getLon();
                            double testY = parsed.get(i).getLat();
                            //var testX = parsed[i][0];
                            //var testY = parsed[i][1];
                            double d = getDistanceBetweenTwoPoints(xy[0], xy[1], testX, testY);
                            if (d <= 60) {
                                parsed.get(i).setVisible(0);
                                //  parsed[i][3] = 0; // change to hidden
                                  //console.log("hiding city "+parsed[i][2]+" to make room for major city 2:"+name);
                            }
                        }
                        return xy;
                    } else if (major == 1) {
                        
                        // find nearest city to the major city already parsed
                        for (int i = 0; i < parsed.size(); i++) {
                            if (parsed.get(i).isVisible() == 1) {  //  only test those sites that are visible
                              double testX = parsed.get(i).getLon();
                              double testY = parsed.get(i).getLat();
                              double d = getDistanceBetweenTwoPoints(xy[0], xy[1], testX, testY);
                              if (d <= 60 && parsed.get(i).getMajorCity() != 2) {
                                    parsed.get(i).setVisible(0); // change to hidden if its not a super major city
                                   // console.log("hiding city "+parsed[i][2]+" to make room for major city 1: " +name);
                              } else if (d <=60 && major == 1) {  // already plotted a major = 1 city
                                    return null;
                              } else if (d <= 60 && major == 2) { // too close to major city =2
                                    return null;
                              }
                            }
                        }
                        return xy;
                    } else {

                        double minDist = 500;
                        
                        for (int i = 0; i < parsed.size(); i++) {
                           if (parsed.get(i).isVisible() == 1) { // only test those sites that are visible
                               double testX = parsed.get(i).getLon();
                               double testY = parsed.get(i).getLat();
                               double d = getDistanceBetweenTwoPoints(xy[0], xy[1], testX, testY);
                               if (d <= minDist) minDist = d;
                           }
                        }
                        if (minDist >= 60) {
                            
                            return xy;
                        }
                    }
              }
          }
      } catch (Exception ex) {
          System.out.println("WmsRadarViewer: Error int bboxContains() : "+ex.getMessage());
      }
          return null;
      }
      
//    public boolean pointInPolygon(point, polygon) {// array of point in array of polygon points
//        // Check if the point sits exactly on a vertex
//        if (pointOnVertex(point, polygon)) {
//               return true;
//        }
//        
//        // Check if the point is inside the polygon or on the boundary
//        var intersections = 0; 
//        var vertices_count = polygon.length;
//
//        for (var i=1; i < vertices_count; i++) {
//            var vertex1 = polygon[i-1];
//            var vertex2 = polygon[i];
//            var minX = vertex1[0];
//            if (vertex2[0] < minX) minX = vertex2[0];
//            var maxX = vertex1[0];
//            if (vertex2[0] > maxX) maxX = vertex2[0];
//            var minY = vertex1[1];
//            if (vertex2[1] < minY) minY = vertex2[1];
//            var maxY = vertex1[1];
//            if (vertex2[1] > maxY) maxY = vertex2[1];
//
//            if (vertex1[1] == vertex2[1] && vertex1[1] == point[1] && point[0] > minX && point[0] < maxX) { 
//                return true; // on the boundary
//            }
//            if (point[1] > minY && point[1] <= maxY && point[0] <= maxX && vertex1[1] != vertex2[1]) { 
//                var xinters = (point[1] - vertex1[1]) * (vertex2[0] - vertex1[0]) / (vertex2[1] - vertex1[1]) + vertex1[0]; 
//                if (xinters == point[0]) { // Check if point is on the polygon boundary (other than horizontal)
//                    return true; // boundary
//                }
//                if (vertex1[0] == vertex2[0] || point[0] <= xinters) {
//                    intersections++; 
//                }
//            } 
//        } 
//        // If the number of edges we passed through is odd, then it's in the polygon. 
//        if (intersections % 2 != 0) {
//            return true;
//        } else {
//            return false;
//        }
//    }

//    function pointOnVertex(point, vertices) {
//        for (var i=0; i<vertices.length; i++) {
//             if (point[0] == vertices[0] && point[1] == vertices[1]) return true;
//        }
//        return false;
//    }
      public void panMap(int deltaXpix, int deltaYpix) {
          System.out.println("deltaXpix="+deltaXpix+" deltaYpix="+deltaYpix);
          double dX = dppX * deltaXpix;
          double dY = dppY * deltaYpix;
           
          double newLat = lat + dY;
          double newLon = lon - dX;
          recenterMap(newLat, newLon);
          
      }
      public double[] pixelToWorld(int x, int y) {
          double upperLeftLat = bbox[2];
          double upperLeftLon = bbox[1];
          double deltaX = dppX * x;
          double deltaY = dppY * y;
          double newLat = upperLeftLat - deltaY;
          double newLon = upperLeftLon + deltaX;
          return new double[] {newLat, newLon};
      }
      public int[] panMapWorldToPixel(double t_lon,double t_lat,int w,int h) {
           
            double latDiff = panMapUpperRightLat - t_lat;
            double lonDiff = t_lon - panMapLowerLeftLon;
            
            int x = Double.valueOf(lonDiff / dppX).intValue();
            int y = Double.valueOf(latDiff / dppY).intValue();
         
            int[] retVal = {x,y};
            return retVal;
      }
      public int[] worldToPixel(double t_lon,double t_lat,int w,int h) {
           
            double latDiff = bbox[2] - t_lat;
            double lonDiff = t_lon - bbox[1];
            
            int x = Double.valueOf(lonDiff / dppX).intValue();
            int y = Double.valueOf(latDiff / dppY).intValue();
         
            int[] retVal = {x,y};
            return retVal;
      }
      
       
      private String expandBbox(String t_bbox, int pixels,int w,int h) {
           String[] a = t_bbox.split(",");
           double[] arr = {Double.valueOf(a[0]), Double.valueOf(a[1]), Double.valueOf(a[2]), Double.valueOf(a[3])};
            double tdppx = Math.abs((arr[1] - arr[3]) / w);
            double tdppy = Math.abs((arr[0] - arr[2]) / h);
            double newLLLat = arr[0]-(pixels*tdppy);
            double newLLLon = arr[1]-(pixels*tdppx);
            double newURLat = arr[2]+(pixels*tdppy);
            double newURLon = arr[3]+(pixels*tdppx);
            return newLLLat+","+newLLLon+","+newURLat+","+newURLon;
      }
      
      
      private double getDegreesAtPixelDist(int pixels) {
          int w = container_height;
          //double[] arr = getBbox();
          //var arr = bbox.split(",");
          double dppy = Math.abs((bbox[0] - bbox[2]) / w);
          return dppy * pixels;
          
      }
      
        private double getDistance(double t_lat, double t_lon) {
            return getDistanceBetweenTwoPoints(lat,lon,t_lat,t_lon);
        }
        
        private double getDistanceBetweenTwoPoints(double t_lat1, double t_lon1, double t_lat2,double t_lon2) {
            double d1 = t_lon2 - t_lon1;
            double d2 = t_lat2 - t_lat1;
            return Math.sqrt(d1*d1 + d2*d2);
        }

    @Override
    public void networkUp(boolean value) {
       
       networkUp = value;
    }

    
      
       
        
      
}