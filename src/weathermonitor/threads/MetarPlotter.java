/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.threads;

import utilities.WindBarb;
import weathermonitor.listeners.MetarPlotterListener;
import weathermonitor.layers.MetarLayer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import mappingtools.Mapping;
import weathermonitor.dataobjects.MetarStationInfo;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.dataobjects.MetarLocationObject;
import static weathermonitor.layers.MetarLayer.getPresentWeatherIcon;

/**
 *
 * @author greg
 */
public class MetarPlotter extends Thread {
    //private String metarId;
    private MetarStationInfo metar;
    private MetarLocationObject metarLoc;
//    private final double lat;
//    private final double lon;
    private final BufferedImage image;
    private static final ArrayList<MetarStationInfo> metarStationInfoForBounds = MetarLayer.stationData;
    private double metarDistanceBuffer;
    private final double lowObDistance = MetarLayer.lowObDistance;
    private final  double highObDistance = MetarLayer.highObDistance;
    private final  double midObDistance = MetarLayer.midObDistance;
    private int count;
    private final int id;
    private MetarPlotterListener listener;
    private boolean stationListPurged = false;
   
    
    public MetarPlotter( BufferedImage img, 
                         MetarLocationObject metarLoc,
                         int id, int count, boolean stationListPurged) {
        this.image = img;
        this.id = id;
        this.metarLoc = metarLoc;
//        this.lat = metarLoc.metarSiteLonLat.getY();
//        this.lon = metarLoc.metarSiteLonLat.getX();
//        this.preferredMetarId = metarLoc.preferredId;
        this.count = count;
        this.stationListPurged = stationListPurged;
    }
    
    public void addMetarPlotterListener(MetarPlotterListener l) {
        this.listener = l;
    }
    @Override
    public void run() {
       long curSecs = new Date().getTime()/1000;
        plotMetar( metarLoc.metarSiteLonLat.getY(),  metarLoc.metarSiteLonLat.getX());
       long diff = new Date().getTime()/1000 - curSecs;
       //System.out.println("PlotMetar id:"+id+" finished in "+diff+" seconds");
       if (listener != null) {
           listener.metarPlotterFinished(metar, id, count);
       }
    }
    
    private void plotMetar(MetarStationInfo msi) {
                     this.metar = msi;
                   // System.out.println("Plot Metar "+msi.getSiteName() +" "+msi.getStationId());
                    String name = msi.getSiteName();
                    Font f = msi.getPlotFont();
                    boolean obIsCurrent = isCurrent(msi.getObservationTime());
                    //if (msi == null) {System.out.println("The msi for "+name+" is null");}
                    if (msi != null) {
                       // System.out.println("Plotting "+msi.getStationId()+" on map now ");
                        
                        Graphics2D g2d = image.createGraphics();
                        g2d.setRenderingHint(
                                RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        
                        g2d.setFont(f);
                                              
                        //Metar metar = new Metar(msi.getStationId(),msi.getTemp(),msi.getDwpt(),msi.getWindDirDeg(),msi.getWindSpeedKmHr(),msi.getWindGustKmHr(),msi.getObservationTime(),msi.getPlotFont());
                        //metarCache.put(msi.getStationId(),metar);
                        
                         String text = "M";
                        if (msi.getTemp() != MetarStationInfo.MISSING_TEMP) {
                            text = Integer.toString(msi.getTemp()) + "F";
                        }
                        
                        //String text = Integer.toString(msi.getTemp()) + "F";
                        if (obIsCurrent == false) {
                            text = "OLD";
                            //g2d.setFont(metarTimeStampFont);
                        }
                        
                        msi.setUpdateTime(new Date().getTime()/1000);
//                        if (setToolTipText == false && msi.getObservationTime() > 0) {
//                            setToolTipText = true;
//                            WeatherMonitorRun.obsCheckbox.setToolTipText(msi.getObservationTimeString());
//                        }
                    
                    Point p = msi.getPlotXYLocation();
//                    // clear previous wind barb
//                    g2d.setComposite(AlphaComposite.Clear);
//                    Rectangle windRect = new Rectangle(p.x-70,p.y-40,50,50);
//                    g2d.fillRect((int) windRect.getX(), (int) windRect.getY(), (int) windRect.getWidth(), (int) windRect.getHeight());
//                    // clear previous temperature plot
//                    Rectangle temperatureRect = msi.getPlotBoundingRectangle();
//                    if (temperatureRect != null) g2d.fillRect((int) temperatureRect.getX() - 5, (int) temperatureRect.getY() - 5, (int) temperatureRect.getWidth() + 10, (int) temperatureRect.getHeight() + 10);
//                    g2d.setComposite(AlphaComposite.SrcOver); 
//                    // done with clear
                   
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(text, p.x+2, p.y+2 + 25);
                    g2d.drawString(text, p.x+2, p.y-2 + 25);
                    g2d.drawString(text, p.x-2, p.y+2 + 25);
                    g2d.drawString(text, p.x-2, p.y-2 + 25);
                    g2d.setColor(Color.BLACK);
                    if (obIsCurrent == false) {
                        g2d.setColor(Color.RED);
                    }
                    //System.out.println("Draw String for "+msi.getSiteName() +" text "+p.x+" "+p.y);
                        g2d.drawString(text, p.x, p.y + 25);
                        if (msi.getWeatherIcon() != null && obIsCurrent == true) {
                            String wxIcon = msi.getWeatherIcon();
                            try {
                               ImageIcon weatherIcon = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+wxIcon));
                               g2d.drawImage(weatherIcon.getImage(),p.x-28,p.y+15,null);
                               
                            } catch (Exception ex) {
                                System.out.println("Cannot get wxIcon for /weathermonitor/icons/"+wxIcon);
                                ex.printStackTrace();
                            }
                            
                         
                        }
                        int gust = msi.getWindGustMPH();
                        int speed = msi.getWindSpeedMPH();
                        if (gust > speed) {
                            speed = gust;
                        }
//                        if (speed == MetarStationInfo.MISSING_WIND || speed == 0) {
//                            speed = msi.getWindSpeedMPH();
//                        }
                        if (obIsCurrent == true) {
                            WindBarb wb = new WindBarb(MetarLayer.showWindSpeedsOnArrows,MetarLayer.showWindSpeedsOnArrowsValues);
                            Image img = wb.getWindBarb(speed,msi.getWindDirDeg());
                            g2d.drawImage(img,p.x-80,p.y-30,null);
                            img.flush();
                        }
                       //////////  updateDownloadedMetars(msi);
                       
                        g2d.dispose();
                    } else if (msi != null) {
                        //////////  updateDownloadedMetars(msi);
                        msi.setUpdateTime(0);
                       
                    }
                   
        
    }
    
     private MetarStationInfo plotMetar( double lat, double lon){
         if (metarLoc.preferredId != null ) {
             try {
             MetarStationInfo m = loadMetarData(metarLoc.preferredId,metarLoc.cityName,lat,lon);
             System.err.println("MetarPlotter : Use "+metarLoc.preferredId+" for city "+metarLoc.cityName);
             System.err.println("             : " + m.getStationId() + ":" + m.getTemp() + "F");
             // prepare m 
             Font f = Mapping.cityFont[MetarLayer.fontNumber];
             m.setPlotFont(f);
             Graphics g = image.getGraphics();
             FontMetrics met;
             met = g.getFontMetrics(f);// 1 to 4
             String text = "   F";
             Rectangle2D rect = met.getStringBounds(text, g);
             g.dispose();
             int[] xy = Mapping.getInstance().worldToPixel(lon,lat,image.getWidth(),image.getHeight());
             Point p = new Point(xy[0],xy[1]);
             m.setPlotXYLocation(p);
             Rectangle r = new Rectangle(p.x, p.y + 15, (int) rect.getWidth(), (int) rect.getHeight());
             m.setBoundingRectangle(r);
            
             plotMetar(m);
             return m;
             } catch (Exception ex) {
                 ex.printStackTrace();
                 return null;
             }
         }
         if (stationListPurged == true) {
             // all the staions in metarStationDataForBounds will be plotted
             // it is a short list
             MetarStationInfo msi = getNearestMetarSite(lat,lon);
             if (msi != null) {
                 if (msi.getPlotXYLocation() != null) {
                   
                         plotMetar(msi);
                     
                 } else {
                     System.out.println("plot xy location null for "+msi.getSiteName()+" "+msi.getStationId());
                 }
             } else {
                 System.out.println("cannot plot metar cause msi is null");
             }
             return msi;
         }
        MetarStationInfo msi = null; 
        try {
        
                    for (int j=0; j<=5; j++) {// 4 attempts for K type metars
                         msi = getNearestMetarSite(lat,lon,true);
                        // System.out.println("Get nearest Metar site for "+lat+" "+lon);
                         //if (msi == null) System.out.println("FAILED");
                         if (1 == 2 && msi != null && msi.getTemp() == 9999) {
                            // System.out.println("temp was missing Retry metar location");
                             
                             msi = getNearestMetarSite(lat,lon,true);
                         
                            
                         } else if (msi == null) {
                             //System.out.println("msi was null Retry metar location");
                             
                             msi = getNearestMetarSite(lat,lon,true);
                         } else {
                             
                             break;
                         }
                    }
                    //if (msi != null) System.out.println("Found neareset site for "+msi.getStationName()+" is "+ msi.getStationId());
                    if (msi == null && Mapping.zoomLevel >= 3) {// try 4 more attempts with other than starts with K
                        for (int j=0; j<=3; j++) {
                         msi = getNearestMetarSite(lat,lon,false);
                         if (1 == 2 && msi != null && msi.getTemp() == 9999) {
                            // System.out.println("temp was missing Retry metar location");
                             
                             msi = getNearestMetarSite(lat,lon,false);
                         
                            
                         } else if (msi == null) {
                           //  System.out.println("msi was null Retry metar location");
                             
                             msi = getNearestMetarSite(lat,lon,false);
                         } else {
                             
                             break;
                         }
                    }
                    }
                    String id = "";
                    if (msi != null) id = msi.getStationId();
                    if (msi != null) {// && plottedIds.contains(id)  == false) {
                        //plottedIds.add(msi.getStationId());
                        String name = msi.getSiteName();
                        Font f = Mapping.cityFont[MetarLayer.fontNumber];
                        msi.setPlotFont(msi.getPlotFont());
                        Graphics g = image.getGraphics();
                        FontMetrics m;
                        m = g.getFontMetrics(f);// 1 to 4
                        String text = "   F";
                        Rectangle2D rect = m.getStringBounds(text, g);
                        g.dispose();
                        int[] xy = Mapping.getInstance().worldToPixel(lon,lat,image.getWidth(),image.getHeight());
                        Point p = new Point(xy[0],xy[1]);
                        msi.setPlotXYLocation(p);
                        Rectangle r = new Rectangle(p.x, p.y + 15, (int) rect.getWidth(), (int) rect.getHeight());
                        msi.setBoundingRectangle(r);
                        msi.setPlotFont(f);
//                        if (latestMsi == null) latestMsi = msi;
//                        //System.out.println("plotMetar for "+name+" "+msi.getStationId());
//                        if  (msi.getObservationTime() > latestMsi.getObservationTime()) {
//                            latestMsi = msi;
//                            updatMetarPlotTimeStamp(latestMsi.getObservationTimeString());
//                        }
                        plotMetar(msi);
                        
                    } else {
                        
                        //System.out.println("No remaining metar locations within "+metarDistanceBuffer+" for "+lon+" "+lat);
                    }
        } catch(Exception ex) {
            ex.printStackTrace();
            msi = null;
        }
        return msi;
        
    }
    private MetarStationInfo getNearestMetarSite(double lat, double lon) {
        try {
           // System.out.println("Metar station list is purged " + metarStationInfoForBounds.size() + " things so use the short method ");

            for (int i = 0; i < metarStationInfoForBounds.size(); i++) {
                MetarStationInfo testMsi = metarStationInfoForBounds.get(i);
                // System.out.println("testMsi has plot point "+testMsi.getPlotXYLocation());
                Point2D p2d = testMsi.getNearestCityLatLon();
                if (p2d.getX() == lon && p2d.getY() == lat) {
                    //  System.out.println("Found a matching city for lat:"+lat+" lon:"+lon+" city="+testMsi.getSiteName());

                    if (Timer.getInstance().isInternetUp() == true) {
                        MetarStationInfo m = loadMetarData(testMsi.getStationId(), testMsi.getSiteName(), lat, lon);
                        testMsi.updateDataValues(m);
                        // System.out.println("update testMsi has plot point "+testMsi.getPlotXYLocation());
                        return testMsi;
                    } else {
                        return testMsi;
                    }
                    // return m;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private MetarStationInfo getNearestMetarSite(Double lat, Double lon, boolean startsWithK) {
        //if (isInMapBounds(lat,lon)) {
        long start = new Date().getTime()/1000;
            Pattern pat = Pattern.compile("^K[A-Z,0-9][A-Z,0-9][A-Z,0-9]");
        String  test = "";//-100.6,30.86
//        if (lon == -100.6 && lat == 30.86) {
//             test = "Eldorado";
//            System.out.println("Working on Eldorados lat lon");
//        }
        boolean isYourLocation = false;
        Point2D.Double testPoint = new Point2D.Double(lon,lat);
//        if (yourLocation != null && testPoint.distance(yourLocation) <= 0.05) {
//            //System.out.println("Found Your Location lat/lon" +lat+" "+lon);
//            isYourLocation = true;
//            startsWithK = false;
//        }
        double distance = 1000;
        String metarId = ""; String siteName = "";
        MetarStationInfo nearestMsi = null;
        //System.out.println("metarstationiinfoforBound.size()="+metarStationInfoForBounds.size());
        for (int i=0; i<metarStationInfoForBounds.size(); i++) {
            String stationId = metarStationInfoForBounds.get(i).getStationId();
            Matcher kStation = pat.matcher(stationId);
           if (isYourLocation == false && startsWithK == true && kStation.find() && stationId.length() == 4 && metarStationInfoForBounds.get(i).getSkip() == false && metarStationInfoForBounds.get(i).hasNoData()== false) {
           // System.out.println("KKKKKKKK");
            double testLon = metarStationInfoForBounds.get(i).getLon();
            double testLat = metarStationInfoForBounds.get(i).getLat();
            double dist = Utilities.getDistanceBetweenTwoPoints(testLon,testLat,lon,lat);
             
               if (dist < distance) {
                    distance = dist;
                    metarId = stationId;
                    siteName = metarStationInfoForBounds.get(i).getStationName();
                    nearestMsi = metarStationInfoForBounds.get(i);
               }
            } else if (startsWithK == false && metarStationInfoForBounds.get(i).getSkip() == false && metarStationInfoForBounds.get(i).hasNoData()== false) {
                double testLon = metarStationInfoForBounds.get(i).getLon();
                double testLat = metarStationInfoForBounds.get(i).getLat();
                double dist = Utilities.getDistanceBetweenTwoPoints(testLon,testLat,lon,lat);
             //if (test.equals("") == false) System.out.println(stationId+" has distande "+dist );
               if (dist < distance) {
                    distance = dist;
                    metarId = stationId;
                    siteName = metarStationInfoForBounds.get(i).getStationName();
                    nearestMsi = metarStationInfoForBounds.get(i);
               }
            //}
        }
        //metarStationInfoForBounds.clear();
        double distInMiles = distance*60;
       if (stationId.startsWith("K")) {
           if  (Mapping.zoomLevel <= 3) {
               metarDistanceBuffer = highObDistance;//9
           } else {
              metarDistanceBuffer = midObDistance;//7
           }
       } else {
           metarDistanceBuffer = lowObDistance;//5
       }
      // System.out.println("metar distance buffer="+metarDistanceBuffer);
     //System.out.println("Nearest Metar for Kstatioin "+siteName+" id "+metarId+" at distance "+distInMiles+"miles");
        if (distInMiles <= metarDistanceBuffer && metarId.equals("") == false) {
            MetarStationInfo m = null;
               // System.out.println("load metar data for id:"+metarId+ " siteName:"+siteName+" lat="+lat+" lon="+lon);
                m = loadMetarData(metarId,siteName,lat,lon);
              
            if (m == null && Timer.getInstance().isInternetUp() == true) {
                nearestMsi.setSkip(true);
                
            } else if (m != null && m.getTemp() == 9999) {
             
                //nearestMsi.setSkip(true);
            } else if (m != null && isCurrent(m.getObservationTime()) == false) {
               // nearestMsi.setSkip(true);
            }
            //long end = new Date().getTime()/1000;
            //long diff = end - start;
            //System.out.print("getNearestMetar took "+diff+" seconds");
            //if (m != null) System.out.println("Return nearest station is "+m.getSiteName()+" "+m.getTemp());
            return m;
            
        }
    }
        return null;
    }
    
      private boolean isCurrent(long time) {
        //return true;
        long diff = new Date().getTime()/1000 - time;
        //System.out.println("isCurrent "+diff);
        if (diff <= 9000) return true;
        return false;
    }
    
     private MetarStationInfo loadMetarData(String metarId, String cityNameOnMap, double lat, double lon) {
        if (Timer.getInstance().isInternetUp() == false) {
           // System.out.println("MetarLayer internet is not up");
            return null;
        }
        long start = new Date().getTime()/1000;
            String dlink = "https://api.weather.gov/stations/" + metarId + "/observations/?limit=20";
           
            String json = "";

            MetarStationInfo msi = null;
               // System.out.println("Download metar for " + metarId + " plotter id is "+id);
                json = Utilities.downloadJsonData(dlink,5000);
               // System.out.println("Download finished for metar:"+metarId);
                ZoneId zoneId = ZoneId.of("Z");
                if (json != null && json.equals("") == false) {
                   // System.out.println("Got json data for " + metarId);
                    try {
                        JSONObject obj = new JSONObject(json);
                        JSONArray features = obj.getJSONArray("features");
                        if (features.length() > 0) {
                            int index = 0;
                            long minTime = start - 3600;
                            //
                            String timestamp = features.getJSONObject(0).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            LocalDateTime localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                            
                            long testTime = localDateTime.atZone(zoneId).toEpochSecond();
                          
                            JSONArray coords = features.getJSONObject(index).getJSONObject("geometry").getJSONArray("coordinates");
                            msi = new MetarStationInfo(metarId, coords.getDouble(1), coords.getDouble(0));
                            msi.setNearestCityLatLon(new Point2D.Double(lon,lat));
                            String temperature = features.getJSONObject(index).getJSONObject("properties").getJSONObject("temperature").get("value").toString();
                            if (metarId.equals("KAQO")) {
                                System.out.println("Temperature for "+metarId+" = "+temperature);
                            }
                            if (temperature.equals("M") || temperature.equals("null") || temperature.equals("")) {
                                for (int k = 0; k < 4; k++) {
                                    timestamp = features.getJSONObject(k).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");
                                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                    localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                                    testTime = localDateTime.atZone(zoneId).toEpochSecond();
                                    if (testTime >= minTime) {
                                         if (metarId.equals("KAQO")) {
                                             System.out.println("Temperature for time "+k+" = "+metarId+" = "+temperature);
                                          }
                                        try {
                                            temperature =  features.getJSONObject(k).getJSONObject("properties").getJSONObject("temperature").get("value").toString();
                                        } catch (Exception ex) { }
                                        if (temperature.equals("M") == false &&
                                            temperature.equals("null") == false &&
                                            temperature.equals("") == false) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (metarId.equals("KAQO")) {
                                System.out.println("Temperature for "+metarId+" = "+temperature);
                            }
                            String oneHrPcpn = "null";
                            try {
                                oneHrPcpn = features.getJSONObject(index).getJSONObject("properties").getJSONObject("precipitationLastHour").get("value").toString();
                            } catch (Exception ex) {
                               // System.out.println("precipitationLastHour not found for "+metarId);
                            }
                            String threeHrPcpn = "null";
                            try {
                                threeHrPcpn = features.getJSONObject(index).getJSONObject("properties").getJSONObject("precipitationLast3Hours").get("value").toString();
                            } catch (Exception ex) {
                                // System.out.println("precipitationLast3Hours not found for "+metarId);
                            }
                            String sixHrPcpn = "null";
                            try {
                                sixHrPcpn = features.getJSONObject(index).getJSONObject("properties").getJSONObject("precipitationLast6Hours").get("value").toString();
                            } catch (Exception ex) {
                                // System.out.println("precipitationLast6Hours not found for "+metarId);
                            }
                                String vsby = features.getJSONObject(index).getJSONObject("properties").getJSONObject("visibility").get("value").toString();
                          
                            String dwpt = features.getJSONObject(index).getJSONObject("properties").getJSONObject("dewpoint").get("value").toString();
                            String wspd = features.getJSONObject(index).getJSONObject("properties").getJSONObject("windSpeed").get("value").toString();
                            String wgst = features.getJSONObject(index).getJSONObject("properties").getJSONObject("windGust").get("value").toString();
                            String wdir = features.getJSONObject(index).getJSONObject("properties").getJSONObject("windDirection").get("value").toString();
                            JSONArray wx = null;
                            for (int k = 0; k < 4; k++) {
                                timestamp = features.getJSONObject(k).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");
                                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                                testTime = localDateTime.atZone(zoneId).toEpochSecond();
                                if (testTime >= minTime) {
                                try {
                                    wx = features.getJSONObject(k).getJSONObject("properties").getJSONArray("presentWeather");
                                } catch (Exception ex) {
                                }
                                if (wx != null && wx.length() > 0) {
                                    break;
                                }
                                } else {
                                    break;
                                }
                            }
                        
                            JSONArray sky = null;
                            for (int k = 0; k < 4; k++) {
                                 timestamp = features.getJSONObject(k).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");
                                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                                testTime = localDateTime.atZone(zoneId).toEpochSecond();
                                if (testTime >= minTime) {
                                    //System.out.println("testTime >= minTime");
                                    try {
                                        sky = features.getJSONObject(k).getJSONObject("properties").getJSONArray("cloudLayers");
                                    } catch (Exception ex) {
                                    }

                                    if (sky != null && sky.length() > 0) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                          
                           
                            int windSpeed = MetarStationInfo.MISSING_WIND;
                            try {
                                
                                windSpeed = (int) Math.round(Double.valueOf(wspd)/1.609344);
                            } catch (Exception ex) {
                                windSpeed = MetarStationInfo.MISSING_WIND;
                                //ex.printStackTrace();
                            }
                            int windGust = MetarStationInfo.MISSING_WIND;
                            try {
                                windGust = (int) Math.round(Double.valueOf(wgst)/1.609344);
                            } catch (Exception ex) {
                                windGust = MetarStationInfo.MISSING_WIND;
                                //ex.printStackTrace();
                            }
                             String wxIcon = null;
                            try {
                                 wxIcon = getPresentWeatherIcon(wx,sky,windSpeed,windGust);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                           // System.out.println("wxIcon="+wxIcon);
                            int windDir = 0;
                            try {
                                windDir = Integer.valueOf(wdir);
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                            }
//"timestamp": "2026-09-23T21:25:00+00:00",
                            timestamp = features.getJSONObject(index).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");

                             formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d hh:mma");
                             localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                            TimeZone tz = TimeZone.getTimeZone("America/Chicago");
                           
                            long epochSeconds = localDateTime.atZone(zoneId).toEpochSecond();
                            int hours = tz.getOffset(new Date().getTime()) / 1000 / 60 / 60 * -1;
                            localDateTime = localDateTime.minusHours(hours);
                           
                            String updatedTime = localDateTime.format(outputFormat);
                            
                            long curTime = new Date().getTime()/1000;
                            long diff = curTime - epochSeconds;
                            msi.setHasNoData(false);
                            //System.out.println("Metar "+metarId+" updated "+updatedTime+" updated "+diff+" seconds ago");
                            msi.setUpdateTime(curTime);
                            msi.setObservationTime(epochSeconds);
                           // updateAllObservationTimes(epochSeconds, updatedTime, msi.getStationId());
                            msi.setObservationTimeString(updatedTime);
                            msi.setSiteName(cityNameOnMap);
                            //msi.setPlotFont(cachedMetarFont);
                            msi.setWeatherIcon(wxIcon);
                            msi.setWindDir(windDir);
                            msi.setWindSpeed(windSpeed);
                            msi.setWindGust(windGust);
                            double oneHrPrecip = -1.0;
                            try {
                                oneHrPrecip = Double.parseDouble(oneHrPcpn);
                                oneHrPrecip = Utilities.mmToIn(oneHrPrecip);
                               
                            } catch (Exception ex) {}
                             msi.setOneHrPcpn(oneHrPrecip);
                            double threeHrPrecip = -1.0;
                            try {
                                threeHrPrecip = Double.parseDouble(threeHrPcpn);
                                threeHrPrecip = Utilities.mmToIn(threeHrPrecip);
                               
                            } catch (Exception ex) {}
                             msi.setOneHrPcpn(oneHrPrecip);
                             double sixHrPrecip = -1.0;
                            try {
                                sixHrPrecip = Double.parseDouble(sixHrPcpn);
                                sixHrPrecip = Utilities.mmToIn(sixHrPrecip);
                               
                            } catch (Exception ex) {}
                             msi.setOneHrPcpn(oneHrPrecip);
                              double viz = -1.0;
                            try {
                                viz = Double.parseDouble(vsby);
                                viz = Utilities.mToMiles(viz);
                               
                            } catch (Exception ex) {}
                             msi.setVsby(viz);
                             
                            int tempF = MetarStationInfo.MISSING_TEMP;

                            int dwptF = MetarStationInfo.MISSING_TEMP;
                            try {
                                double dwptC = Double.parseDouble(dwpt);
                                dwptF = Utilities.tempCtoF(dwptC);
                                msi.setDwpt(dwptF);

                            } catch (Exception ex) {
                                msi.setDwpt(MetarStationInfo.MISSING_TEMP);

                            }
                            //System.out.println("temp for " + msi.getStationId() + " = " + temperature);
                            try {
                                if (temperature.equals("null") == false) {
                                    double tempC = Double.parseDouble(temperature.trim());
                                    tempF = Utilities.tempCtoF(tempC);
                                    msi.setTemp(tempF, epochSeconds);
                                } else {
                                    msi.setTemp(MetarStationInfo.MISSING_TEMP, epochSeconds);

                                }
                                //System.out.println("set tempF:"+tempF+" got temp "+msi.getTemp());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                msi.setTemp(MetarStationInfo.MISSING_TEMP, epochSeconds);

                            }
                            
                        }
                      
                       } catch (Exception ex) {
                           
                        ex.printStackTrace();
                    }
                    
                   
                } else {
                    //System.out.println("Should not be here must have no data at all");
                    msi = new MetarStationInfo(metarId, lat, lon);
                    msi.setHasNoData(true);
                    msi.setSiteName(cityNameOnMap);
                }
              
             long end = new Date().getTime()/1000;
             long diff = end - start;
       // System.out.println("loadMetarData took "+diff+" seconds");
        return msi;
    }
}
