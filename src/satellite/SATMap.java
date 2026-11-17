/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package satellite;

import utilities.Utilities;
import weathermonitor.settings.SPCMapSettings;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import javax.swing.ImageIcon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author greg
 */
public class SATMap {
    public  String[] outlooks = {"https://www.spc.noaa.gov/products/outlook/day1otlk_cat.nolyr.geojson",
                                       "https://www.spc.noaa.gov/products/outlook/day2otlk_cat.nolyr.geojson",
                                       "https://www.spc.noaa.gov/products/outlook/day3otlk_cat.nolyr.geojson"};
    public  String[] names = {"Day 1 Convective Outlook",
                                    "Day 2 Convective Outlook",
                                    "Day 3 Convective Outlook"};
    
    private String statesJson = null;
    private static Color stateOutlineColor = Color.BLACK;
    private static Color stateFillColor = Color.WHITE;
    private static Color bgColor = Color.WHITE;
    private static float stateStroke = 2.0f;
    public double dppX = 0.0;
    public double dppY = 0.0;
    private int width;
    private int height;
    public double[] bbox = null;
    public int zoomLevel = 0;  
    private static double lat = 37.0;
    private static double lon = -96.0;
    private BufferedImage mapImg ;
    public double[]  zoomFactors = {30.0,23.25,12.25,7.25,3.25,1.75,0.5};
    //public static String[] dayStrings = {"Day1","Day2","Day3"};
    public static Font titleFont = new Font("Monospaced",Font.BOLD,24);
    public static Font bigBoldFont = new Font("Monospaced",Font.BOLD,34);
    
    public SATMap(int w, int h, int number) {
        this.outlooks = SPCMapSettings.getInstance().getOutlookLinks();
        this.names = SPCMapSettings.getInstance().getOutlookNames();
        this.width = w;
        this.height = h;
        this.bbox = getBbox();
        this.mapImg = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapImg.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0,0, width, height);
        String title = "SPC "+names[number];
        FontMetrics metrics = g2d.getFontMetrics(titleFont);
        Rectangle2D rect = metrics.getStringBounds(title, g2d);
        int textWidth = (int) rect.getWidth();
        int x = (width-textWidth)/2;
        g2d.setColor(Color.BLACK);
        g2d.setFont(titleFont);
        g2d.drawString(title, x, 30);
        
        loadOutlook(g2d, number);
        loadStates(g2d, width, height);
        g2d.dispose();
    }
    
    private void drawValidTime(Graphics2D g2d, String validTimeString) {
        validTimeString = "Valid: "+validTimeString;
        FontMetrics metrics = g2d.getFontMetrics(titleFont);
       
        Rectangle2D rect = metrics.getStringBounds(validTimeString, g2d);
        int textWidth = (int) rect.getWidth();
        int x = (width-textWidth)/2;
        g2d.setColor(Color.BLACK);
        g2d.setFont(titleFont);
        g2d.drawString(validTimeString, x, 60);
    }
    
    private void loadOutlook(Graphics2D g2d, int number) {
        String json = Utilities.downloadJsonData(outlooks[number],5000);
        if (json != null && json.equals("") == false) {
             boolean noCoords = false;
             JSONObject obj = new JSONObject(json);
             JSONArray features = obj.getJSONArray("features");
             GeneralPath[] p = new GeneralPath[features.length()];
              for (int i = 0; i < features.length(); i++) {
                   p[i] = new GeneralPath();
                  String type = features.getJSONObject(i).getJSONObject("geometry").getString("type");
                  String stroke = features.getJSONObject(i).getJSONObject("properties").getString("stroke");
                  String fill = features.getJSONObject(i).getJSONObject("properties").getString("fill");
                 
                  String label = features.getJSONObject(i).getJSONObject("properties").getString("LABEL2");
                  //"VALID_ISO": "2026-10-25T13:00:00+00:00",
                  String valid = features.getJSONObject(i).getJSONObject("properties").getString("VALID_ISO").replace("T", " ");
                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                  DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d");
                  LocalDateTime localDateTime = LocalDateTime.parse(valid.substring(0, 16), formatter);
                  TimeZone tz = TimeZone.getTimeZone("America/Chicago");
                  ZoneId zoneId = ZoneId.of("Z");
                  String validTimeString = localDateTime.format(outputFormat);
                  System.out.println("validTimeString="+validTimeString);
                  if (i == 0) {
                      drawValidTime(g2d,validTimeString);
                      
                  }
                  JSONArray coords = null;
                  try {
                      coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                  } catch (Exception ex) {
                      noCoords = true;
                      coords = new JSONArray();
                      break;
                  }
                  if (coords == null || coords.length() == 0) {
                      noCoords = true;
                      break;
                      
                  }
                  System.out.println("feature "+i+" is type "+type+" with "+coords.length()+" coords");
                  for (int j=0; j< coords.length(); j++) {
                     // System.out.println("Working on feature "+i+" type="+type+" label="+label+" stroke="+stroke);
                      
                       Object o = coords.get(j);
                      // System.out.println("coords "+j+" has class "+o.getClass().toString());
                       JSONArray coords2 = coords.getJSONArray(j);
                      // System.out.println("coords2 has "+coords2.length()+" coordinates");
                       for (int k=0; k<coords2.length(); k++) {
                           o = coords2.get(k);
                           //System.out.println("coords "+k+" has class "+o.getClass().toString());
                           JSONArray coords3 = coords2.getJSONArray(k);
                          // System.out.println("coords3 has "+coords3.length()+" coordinates");
                           for (int l=0; l<coords3.length(); l++) {
                               
                                o = coords3.get(l);
                                //System.out.println("coords3 "+l+" has class "+o.getClass().toString());
                               if (o.getClass().getName().equals("java.math.BigDecimal")) {
                                   if (l == 1) {
                                       drawPolygon(coords3.get(0), coords3.get(1), stroke, label);
                                       double x = coords3.getBigDecimal(0).doubleValue();
                                       double y = coords3.getBigDecimal(1).doubleValue();
                                       int[] xy = worldToPixel(x, y);
                                       if (k == 0) {
                                          // System.out.println("p" + i + " moveTo");
                                           p[i].moveTo(xy[0], xy[1]);
                                       } else {
                                           //System.out.println("p" + i + " lineto");
                                           p[i].lineTo(xy[0], xy[1]);
                                       }
                                       //System.out.println("draw path from coords3");
                                   }
                               } else if (o.getClass().getName().equals("org.json.JSONArray")) {
                                    JSONArray coords4 = coords3.getJSONArray(l);
                                    //System.out.println("coords4 has "+coords4.length()+" coordinates");
                                    for (int m=0; m<coords4.length(); m++) {
                                        
                                        o = coords4.get(m);
                                        //System.out.println("coords4 "+m+" has class "+o.getClass().toString());
                                        if (o.getClass().getName().equals("java.math.BigDecimal")) {
                                            if (m == 1) {
                                                drawPolygon(coords4.get(0), coords4.get(1), stroke, label);
                                                double x = coords4.getBigDecimal(0).doubleValue();
                                                double y = coords4.getBigDecimal(1).doubleValue();
                                                int[] xy = worldToPixel(x, y);
                                                if (l == 0) {
                                                  //  System.out.println("p" + i + " moveTo");
                                                    p[i].moveTo(xy[0], xy[1]);
                                                } else {
                                                   // System.out.println("p" + i + " lineto");
                                                    p[i].lineTo(xy[0], xy[1]);
                                                }
                                               // System.out.println("draw path from coords4");
                                            }
                                        }
                                    }
                                }
                           }
                       }
                       
                          //p.closePath();
                          //drawPath(g2d, p,stroke,label);
                     
                  }
                  
                     p[i].closePath();
                     drawPath(g2d,p[i],stroke,fill,label);
                  
              }
              if (noCoords == true) {
                  System.out.println("no coords");
                  String text = "No Threats At This Time";
                  FontMetrics metrics = g2d.getFontMetrics(bigBoldFont);
                  Rectangle2D rect = metrics.getStringBounds(text, g2d);
                  int textWidth = (int) rect.getWidth();
                  int x = (width-textWidth)/2;
                  g2d.setColor(new Color(0,200,0));
                  g2d.setFont(bigBoldFont);
                  g2d.drawString(text, x, height/2);
              }
        }
        String icon = "spc.png";
        if (outlooks[number].contains("torn")) {
            icon = "spc_tornado.png";
        } else if (outlooks[number].contains("wind")) {
            icon = "spc_wind.png";
        } else if (outlooks[number].contains("hail")) {
            icon = "spc_hail.png";
        }
        ImageIcon legend = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+icon)); 
        int legendWidth = legend.getIconWidth();
        int legendHeight = legend.getIconHeight();
        int x = width-legendWidth;
        int y = height-legendHeight;
        g2d.drawImage(legend.getImage(),x,y,null);
    }
    private void drawPath(Graphics2D g2d, GeneralPath p, String strokeColor, String fillColor, String label) {
       // System.out.println("draw Path with color="+stroke+" label="+label);
        Color stroke = Color.decode(strokeColor);
         Color fill = Color.decode(fillColor);
        g2d.setColor(stroke);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(p);
        g2d.setColor(fill);
        g2d.fill(p);
    }
    private void drawPolygon(Object lon, Object lat, String stroke, String label) {
      //  System.out.println("Draw color:"+stroke+" label:"+label+" lon="+lon.toString()+" lat="+lat.toString());
    }
    public BufferedImage getImage() {
        return mapImg;
    }
    
    private void loadStates(Graphics2D g2d, int w, int h) {
            
            int hashIndex = 0;
            if (statesJson != null) {
                loadStatesRunner(g2d,statesJson,w,h);
            } else {
                String path = "/mappingtools/maps/States.json";
                statesJson = Utilities.getInstance().readFromJar(path);
                loadStatesRunner(g2d,statesJson,w,h);
            }
            
        }
         private void loadStatesRunner(Graphics2D g2d, String jsonData, int w, int h) {
            Hashtable<String,GeneralPath> statePathHash = new Hashtable<String, GeneralPath>();
            String name = "";
            g2d.setStroke(new BasicStroke(stateStroke));
       
       
        JSONObject obj = new JSONObject(jsonData);
        JSONArray features = obj.getJSONArray("features");
                
               
              for (int i = 0; i < features.length(); i++) {
                name = features.getJSONObject(i).getJSONObject("properties").getString("NAME");
                String id = features.getJSONObject(i).getJSONObject("properties").getString("STATE");
                
                    GeneralPath path = new GeneralPath();
                    JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                    //System.out.println("Working on state:"+id);
                    processCoordinates(name, coords, g2d,w, h);
                
            }
             
}

        
private void processCoordinates(String name, JSONArray arr, Graphics2D g2d, int w, int h) {
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
            int[] XY = worldToPixel(testLon,testLat);
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
                 XY = worldToPixel(testLon,testLat);
                 path.lineTo(XY[0],XY[1]);
            }
            path.closePath();
            
               
               
                 if (path.intersects(0,0,w,h)) {
//                     g2d.setColor(stateFillColor);
//                     g2d.fill(path);
                     g2d.setColor(stateOutlineColor);
                     g2d.draw(path);
                     
                }
            
        
           
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
   
}
 public double[]  getBbox() {
            
            double zoomFactor = zoomFactors[zoomLevel];
            //var aspect = w/h;
            double aspect = Double.valueOf(width)/Double.valueOf(height)*1.2;
            
            double tweekLat = zoomFactor/aspect;
            double tweekLon = zoomFactor;
            
            //System.out.println("WmsRadarViwer: tweekLat="+tweekLat+ " tweekLon="+tweekLon);

            double a = lat-tweekLat;
            double b = lon-tweekLon;
            double c = lat+tweekLat;
            double d = lon+tweekLon;
            dppX = Math.abs((b-d) / width);
            dppY = Math.abs((a-c) / height);
            double retVal[] = {a,b,c,d};
            // System.out.println("Bbox="+a+" "+b+" "+c+" "+d);
            return retVal;
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
  
   public int[] worldToPixel(double t_lon,double t_lat) {
           
            double latDiff = bbox[2] - t_lat;
            double lonDiff = t_lon - bbox[1];
            
            int x = Double.valueOf(lonDiff / dppX).intValue();
            int y = Double.valueOf(latDiff / dppY).intValue();
         
            int[] retVal = {x,y};
            return retVal;
      }
      
}
