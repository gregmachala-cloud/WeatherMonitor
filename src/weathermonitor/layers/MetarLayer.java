package weathermonitor.layers;


import weathermonitor.listeners.MetarHistoryListener;
import weathermonitor.dataobjects.Metar;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import static java.lang.Thread.sleep;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import mappingtools.Mapping;
import weathermonitor.listeners.MetarLocationListener;
import weathermonitor.dataobjects.MetarStationInfo;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.threads.MetarPlotter;
import weathermonitor.listeners.MetarPlotterListener;
import utilities.MetarViewer;
import weathermonitor.listeners.NetworkListener;
import weathermonitor.threads.Timer;
import weathermonitor.listeners.TimerListener;
import weathermonitor.threads.WMTimer;
import weathermonitor.WeatherMonitorRun;
import weathermonitor.dataobjects.MetarLocationObject;
import weathermonitor.settings.WeatherMonitorSettings;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class MetarLayer extends JPanel implements  MetarLocationListener, TimerListener, NetworkListener, MetarPlotterListener  {
    private int width;
    private int numStationsPlotted;
    private boolean showMetarSites= false;
    private boolean showMetarTimeStamp = true;
    private boolean metarTimeStampCurrent = true;
    private final long updateAge = 1800;
    private long lastUpdate = 0;
    private long latestMetarUpdateTime = 0;
    private int zoomLevel = 1;
    private boolean stop24HrDownload = false;
    private boolean downloading24HrMetarData= false;
    public static boolean showWindSpeedsOnArrows = true;
    public static  int showWindSpeedsOnArrowsValues = WeatherMonitorSettings.ALL_WIND_SPEEDS;
    //private static double id = Math.random();
    private boolean networkDown = false;
    public static ArrayList<MetarStationInfo> stationData = new ArrayList<>();
    private static final ArrayList<MetarPlotter> metarPlotters = new ArrayList<>();
    private int height;
    private BufferedImage image;
    public static int fontNumber = 3;
    private static final ArrayList<MetarStationInfo> downloadedMetars = new ArrayList<>();
    private double[] bbox = {0,0,0,0};
    public static Color fallingTemps = new Color(0,0,207);
    public static Color fallingTemps2 = new Color(0,125,250);
    public static Color risingTemps = new Color(207,0,0);//0 125 65F
    public static Color risingTemps2 = new Color(250,0,0);
    private final ArrayList<String> stateIds = new ArrayList<>();
    public boolean loadMetarSites = true;
    private String metarTimeStamp = null;
    private final Font metarStationFont = new Font("Monospaced",Font.BOLD,18);
    private Font metarTimeStampFont = new Font("Monospaced",Font.BOLD,18);
    private final WMTimer updateTimer;
    private MetarHistoryListener metarHistoryListener;
    private BufferedImage metarSiteImg;
    private static final ArrayList<MetarLocationObject> metarLocationsToPlot = new ArrayList<>();
    private static MetarLayer me = null;
    public static double lowObDistance = 5.0;
    public static double midObDistance = 7.0;
    public static double highObDistance = 9.0;
    private boolean purged = false;
    private WMTimer purgeTimer;
    
    public static MetarLayer getInstance() {
        if (me == null) me = new MetarLayer();
        return me;
    }
    private  MetarLayer() {
        
        updateTimer = new WMTimer(500,"MetarLayer", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateThePlot();
            }
        });
        updateTimer.setRepeats(false);
        updateTimer.start();
        
         purgeTimer = new WMTimer(5000,"MetarLayer", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                purgeStationData();
            }
        });
        purgeTimer.setRepeats(false);
        purgeTimer.start();
    }
   
    public void initLayer(int w, int h) {
        this.width = w;
        this.height = h;
       
        this.setOpaque(false);
        this.setSize(w,h);
         GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
       
        
        metarHistoryListener = MetarViewer.getInstance();
        
    }
    public void setTimestampFont(Font f) {
        this.metarTimeStampFont = f;
    }
    public void setMetarDistanceBuffer(double low, double mid, double high) {
        this.lowObDistance = low;
        this.midObDistance = mid;
        this.highObDistance = high;
      
    }
    
    public void showWindSpeedsOnArrows(boolean v) {
        this.showWindSpeedsOnArrows = v;
       
    }
    public void showWindSpeedsOnArrowsValues(int v) {
        this.showWindSpeedsOnArrowsValues = v;
        
    }
    public void setMapBgColor(Color c) {
        //this.mapBgColor = c;
      
    }
    public void setFontNumber(int num) {//1 to 4
       //System.out.println("SetFontNumber("+num+")");
        if (num >= 0 && num <= 4) {
            fontNumber = num+1;
        } else {
            fontNumber = 3;
        }
       
    }

    private ArrayList<MetarStationInfo> getStationData(ArrayList<String> stateIds) {
        purged = false;
        downloadedMetars.clear();
        if (stateIds == null) return null;
        ArrayList<MetarStationInfo> msi = new ArrayList<>();
        ArrayList<MetarStationInfo> retList = new ArrayList<>();
        for (int i=0; i<stateIds.size(); i++) {
            ArrayList<MetarStationInfo> list = Utilities.downloadStationData(stateIds.get(i));
            msi.addAll(list);
            }
        for (int i=0; i<msi.size(); i++) {
            if (isInMapBounds(msi.get(i).getLat(),msi.get(i).getLon())) {
                retList.add(msi.get(i));
            }
        }
        return retList; 
    }
    private boolean isInMapBounds(double lat , double lon) {
        return Mapping.getInstance().isInMapBounds(lat,lon);

    }
   
    
//    private boolean isTopOfHour() {
//      
//        SimpleDateFormat sfd = new SimpleDateFormat("mm");
//        Date d= new Date();
//        String mm = sfd.format(d);
//       // System.out.println("Current minute is "+mm);
//        if (mm.equals("00") || mm.equals("15") || mm.equals("30") || mm.equals("45")) {
//            System.out.println("Is top of hour");
//            return true;
//        }
//        //System.out.println("Is not the top of the hour");
//        return false;
//    }

    public MetarStationInfo getMetarSampleDataAtPoint(int x, int y) {
       // System.out.println("There are "+downloadedMetars.size()+" downloaded metars");
        for (int i=0; i<downloadedMetars.size(); i++) {
//            if (bbox == null) {
//                System.out.println("bbox is null");
//            }
            try {
              
            java.awt.Point p = downloadedMetars.get(i).getPlotXYLocation(bbox,width,height);
            if (p == null) return null;
            java.awt.Rectangle rect = new java.awt.Rectangle(p.x-7,p.y+10,50,34);
            if (rect.contains(x,y)) {
               
                return downloadedMetars.get(i);
            }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    private void purgeStationData() {
          
          // On first load NEED TO BE SURE INTERNET WAS UP FIRST TOO
          System.out.println("Purge station data with downloaded metars size:"+downloadedMetars.size()+" and numPlotted="+numStationsPlotted);
          if (downloadedMetars.size() > 0 && numStationsPlotted > 0) {
            if (purged == false && Timer.getInstance().isInternetUp() && numStationsPlotted >= downloadedMetars.size()) {
                purged = true;
                System.out.println("All stations plotted now purge unsed stations from the metar stations data (size before:" + stationData.size() + ")");
                stationData.clear();
                stationData.addAll(downloadedMetars);
                System.out.println("StationData size after purge:" + stationData.size());
                //System.out.println("Setting purged to true");

            }
            numStationsPlotted = 0;
        }
    }
    public void clearMap() {
       
               
        Graphics2D g2d = image.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0,0, width,height);
        g2d.dispose();
            //g2d.setComposite(AlphaComposite.SrcOver);
    }
    private boolean isDownloadTime() {
        if (latestMetarUpdateTime == 0) {
            //System.out.println("Dam there are no obs at all yet...update it");
            return true;
        }
        
        long curSecs = new Date().getTime()/1000;
        long diff = curSecs - lastUpdate;
        if (diff <= 900) return false;
        diff = curSecs - latestMetarUpdateTime;
        //System.out.println("Observation age is "+diff+" seconds");
        if (diff >= updateAge) {
          //  System.out.println("Obs are old need to update them ");
            lastUpdate = curSecs;
            return true;
        }
       // System.out.println("Obs are not old yet...passed");
        return false;
    }
    
    public void updateThePlot() {
        //downloadedMetars.clear();
        metarPlotters.clear();
        final boolean finalPurged = purged;
       
        if (stateIds == null) {
            System.out.println("MetarLayer:StateIds is null in MetarLayer cannot plot");
        
            return;
        } 
        if (loadMetarSites == true)  loadMetarSites();
        if (stationData != null) {
            
            clearMap();
            //metarLocationToPlot is a city on the map there may or may not be a metar
            // site nearest to all the cities so some of the metar plotters will return
            // null value
            final int count = metarLocationsToPlot.size();
            numStationsPlotted = 0;
           // System.out.println("There are "+count + " metar sites");
            for (int i = 0; i < count; i++) {
               // System.out.println("Start metar plotter with purged = "+finalPurged);
                
                MetarPlotter plotter = new MetarPlotter(image, metarLocationsToPlot.get(i), i, count, finalPurged);
                plotter.addMetarPlotterListener(this);
                metarPlotters.add(plotter);
                plotter.start();
            }
        }   else {
            System.out.println("MetarLayer station data is null bad");
        }  
        //System.out.println("MetarLayer:Done with metar plot update");
    }
    

   public void download24HrsMetarData(final String stationId) {
       stop24HrDownload = true;
       
      // viewingMetarId = stationId;
       
       Thread t = new Thread() {
           public void run() {
               int timeout = 0;
               while (downloading24HrMetarData == true) {
                   timeout++;
                   if (timeout > 300) break;
                   try {
                       sleep(200);
                   } catch (InterruptedException ex) {
                       ex.printStackTrace();
                   }
               }
               downloading24HrMetarData = true;
               stop24HrDownload = false;
               try {
                   boolean plottedLatestMetar = false;
                   String id = stationId;
                   String dlink = "https://api.weather.gov/stations/" + id + "/observations?limit=100";
                   String nextSet = dlink;
                   int numberDownloaded = 0;
                   int numberOfAttempts = 0;
                   while (numberDownloaded < 500) {
                       if (numberOfAttempts >= 6 || stop24HrDownload == true) {
                           downloading24HrMetarData = false;
                           break;
                       }
                       numberOfAttempts++;
                       String json = Utilities.downloadJsonData(nextSet,10000);
                       if (json != null && json.equals("") == false) {

                           JSONObject obj = new JSONObject(json);
                           JSONArray features = obj.getJSONArray("features");
                           ArrayList<Metar> list = new ArrayList<Metar>();
                           try {
                               nextSet = obj.getJSONObject("pagination").getString("next") + "&limit=72";
                           } catch (Exception ex) {
                               nextSet = "";
                           }

                           // System.out.println("nextSet=" + nextSet);
                           for (int j = 0; j < features.length(); j++) {
                               String name = features.getJSONObject(j).getJSONObject("properties").getString("stationName");
                               //msi.setSiteName(name);
                               String temp = features.getJSONObject(j).getJSONObject("properties").getJSONObject("temperature").get("value").toString();
                               String dwpt = features.getJSONObject(j).getJSONObject("properties").getJSONObject("dewpoint").get("value").toString();
                               String wspd = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windSpeed").get("value").toString();
                               String wgst = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windGust").get("value").toString();
                               String wdir = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windDirection").get("value").toString();
                               String oneHrPcpn = "null";
                            try {
                                oneHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLastHour").get("value").toString();
                            } catch (Exception ex) {
                                //System.out.println("precipitationLastHour not found for "+id);
                            }
                            String threeHrPcpn = "null";
                            try {
                                threeHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLast3Hours").get("value").toString();
                            } catch (Exception ex) {
                                 //System.out.println("precipitationLast3Hours not found for "+id);
                            }
                            String sixHrPcpn = "null";
                            try {
                                sixHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLast6Hours").get("value").toString();
                            } catch (Exception ex) {
                                 //System.out.println("precipitationLast6Hours not found for "+id);
                            } String vsby = features.getJSONObject(j).getJSONObject("properties").getJSONObject("visibility").get("value").toString();
                               String maxT = features.getJSONObject(j).getJSONObject("properties").getJSONObject("maxTemperatureLast24Hours").get("value").toString();

                               String minT = features.getJSONObject(j).getJSONObject("properties").getJSONObject("minTemperatureLast24Hours").get("value").toString();
                               String timestamp = features.getJSONObject(j).getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");

                               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                               DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d hh:mma");
                               LocalDateTime localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                               TimeZone tz = TimeZone.getTimeZone("America/Chicago");
                               ZoneId zoneId = ZoneId.of("Z");
                               long epochSeconds = localDateTime.atZone(zoneId).toEpochSecond();
                               int hours = tz.getOffset(new Date().getTime()) / 1000 / 60 / 60 * -1;
                               localDateTime = localDateTime.minusHours(hours);
                               String updatedTime = localDateTime.format(outputFormat);
                               JSONArray wx = null;
                               try {
                                   wx = features.getJSONObject(j).getJSONObject("properties").getJSONArray("presentWeather");
                               } catch (Exception ex) {
                                   ex.printStackTrace();
                               }
                               JSONArray sky = null;
                               try {
                                   sky = features.getJSONObject(j).getJSONObject("properties").getJSONArray("cloudLayers");
                               } catch (Exception ex) {
                               }
                               int tempF = MetarStationInfo.MISSING_TEMP;
                               try {
                                   double tempC = Double.parseDouble(temp);
                                   tempF = Utilities.tempCtoF(tempC);
                               } catch (Exception ex) {
                               }
                               int dwptF = MetarStationInfo.MISSING_TEMP;
                               try {
                                   double dwptC = Double.parseDouble(dwpt);
                                   dwptF = Utilities.tempCtoF(dwptC);
                               } catch (Exception ex) {
                               }
                               int windDir = 0;
                               try {
                                   windDir = Integer.valueOf(wdir);
                               } catch (Exception ex) {
                               }
                               int windSpeed = MetarStationInfo.MISSING_WIND;
                               try {
                                   windSpeed = (int) Math.round(Double.valueOf(wspd) / 1.609344);
                               } catch (Exception ex) {
                               }
                               int windGust = MetarStationInfo.MISSING_WIND;
                               try {
                                   windGust = (int) Math.round(Double.valueOf(wgst) / 1.609344);
                               } catch (Exception ex) {
                               }
                             
                               Integer max = null;
                               try {
                                   max = Integer.valueOf(maxT);
                               } catch (Exception ex) {
                               }
                               Integer min = null;
                               try {
                                   min = Integer.valueOf(minT);
                               } catch (Exception ex) {
                               }
                               String weatherString = "";
                               try {

                                   if (wx != null) {
                                       for (int k = 0; k < wx.length(); k++) {
                                           weatherString = weatherString + wx.getJSONObject(k).getString("rawString");
                                       }
                                   }
                               } catch (Exception ex) {
                                   ex.printStackTrace();
                               }
                               String wxIcon = null;
                               try {
                                   wxIcon = getPresentWeatherIcon(wx, sky, windSpeed, windGust);
                               } catch (Exception ex) {
                                   ex.printStackTrace();
                               }
                            double oneHrPrecip = -1.0;
                            try {
                                oneHrPrecip = Double.parseDouble(oneHrPcpn);
                                oneHrPrecip = Utilities.mmToIn(oneHrPrecip);
                               
                            } catch (Exception ex) {}
                           
                            double threeHrPrecip = -1.0;
                            try {
                                threeHrPrecip = Double.parseDouble(threeHrPcpn);
                                threeHrPrecip = Utilities.mmToIn(threeHrPrecip);
                               
                            } catch (Exception ex) {}
                            
                             double sixHrPrecip = -1.0;
                            try {
                                sixHrPrecip = Double.parseDouble(sixHrPcpn);
                                sixHrPrecip = Utilities.mmToIn(sixHrPrecip);
                               
                            } catch (Exception ex) {}
                            
                              double viz = -1.0;
                            try {
                                viz = Double.parseDouble(vsby);
                                viz = Utilities.mToMiles(viz);
                               
                            } catch (Exception ex) {}
                             
                               String skyString = getSkyIcon(sky);
                               Metar metar = new Metar(id, name, skyString, tempF, dwptF, windDir, windSpeed, windGust, weatherString, wxIcon, max, min, oneHrPrecip, threeHrPrecip, sixHrPrecip, viz, epochSeconds, updatedTime);
                               if (metar.getTemp() != 9999 && plottedLatestMetar == false) {
                                   //updatePlotValue(metar);
                                   plottedLatestMetar = true;
                               }
                               metarHistoryListener.addedMetar(metar);
                               list.add(metar);
                           }

                           numberDownloaded = numberDownloaded + list.size();

                       } else {
                           nextSet = "";
                           //System.out.println("Nothing fournd for "+dlink);
                           metarHistoryListener.addedMetar(null);
                       }
                   }
               } catch (Exception ex) {
               }
               downloading24HrMetarData = false;
           }

       };
       t.start();
   }
   
   
   
    public MetarStationInfo getMetarSiteDataAtPoint(int x, int y) {
        if (showMetarSites == false || stationData == null) return null;
        for (int i=0; i<stationData.size(); i++) {
            try {
                String id = stationData.get(i).getStationId();
                if (zoomLevel < 3 && id.startsWith("K")) {
                    java.awt.Point p = stationData.get(i).getPlotXYLocation(bbox,width,height);
                    java.awt.Rectangle rect = new java.awt.Rectangle(p.x - 10, p.y - 10, 20, 20);
                    if (rect.contains(x, y)) return stationData.get(i);
                    
                } else if (zoomLevel >= 3) {
                    java.awt.Point p = stationData.get(i).getPlotXYLocation(bbox,width,height);
                    java.awt.Rectangle rect = new java.awt.Rectangle(p.x - 10, p.y - 10, 20, 20);
                    if (rect.contains(x, y)) return stationData.get(i);
                    
                }

            } catch (Exception ex) {

            }
        }
        return null;
    }
    public boolean getShowWindSpeedsOnArrows() {
        return showWindSpeedsOnArrows;
    }
    public int getShowWindSpeedsOnArrowsValues() {
        return showWindSpeedsOnArrowsValues;
    }
    public void setShowWindSpeedsOnArrowsValues(int nu) {
        this.showWindSpeedsOnArrowsValues = nu;
    }
    public void setShowWindSpeedsOnArrows(boolean value) {
        this.showWindSpeedsOnArrows = value;
    }
    public void setShowMetarSites(boolean value) {
        this.showMetarSites = value;
        repaint();
    }
    private void loadMetarSites() {
        Thread t= new Thread() {
            public void run() {
                loadMetarSitesThread();
            }
        };
        t.start();
    }
    // tiny little ids on map
   private void loadMetarSitesThread() {
      //System.out.println("ZoomLevel="+zoomLevel+" Create metar site img "+width+","+height);
       this.metarSiteImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
       Graphics2D g2d = metarSiteImg.createGraphics();
          if (stationData != null ) {
              g2d.setColor(Color.darkGray);
              g2d.setFont(metarStationFont);
              for (int i=0; i<stationData.size(); i++) {
                  
                  String id = stationData.get(i).getStationId();
                  double lat = stationData.get(i).getLat();
                      double lon = stationData.get(i).getLon();
                      int[] xy = Mapping.getInstance().worldToPixel(lon, lat, width, height);
                      java.awt.Point p = new java.awt.Point(xy[0], xy[1]);
                      stationData.get(i).setPlotXYLocation(p);
                  if (zoomLevel < 3 && id.startsWith("K")) {
                      //System.out.println("Draw metar site id "+metarStationInfo.get(i).getStationId()+" at "+xy[0]+","+xy[1]);
                      g2d.drawString("+ " + stationData.get(i).getStationId(), xy[0], xy[1]);
                  } else if (zoomLevel >= 3) {
                     // System.out.println("Draw metar site id "+metarStationInfo.get(i).getStationId()+" at "+xy[0]+","+xy[1]);
                     
                      g2d.drawString("+ " + stationData.get(i).getStationId(), xy[0], xy[1]);
                  }
              }
          }
          g2d.dispose();
      }
   
   public static String getPresentWeatherIcon(JSONArray wx, JSONArray sky, Integer spd, Integer gst) {
//       int wind_gust = gst;// convert to  MPH
//        int wind_speed = (int) Math.round(spd);// convert to  MPH
       
       String weather = ""; 
       if (wx != null) {
       for (int i=0; i<wx.length(); i++) {
           weather = weather + wx.getJSONObject(i).getString("rawString");
       }
       }
       //System.out.println("Weather "  +weather);
       Pattern p = Pattern.compile("RA");
       Matcher ra = p.matcher(weather);
        p = Pattern.compile("FG");
         Matcher fg = p.matcher(weather);
        p = Pattern.compile("TS");
         Matcher ts = p.matcher(weather);
        p = Pattern.compile("HA");
         Matcher ha = p.matcher(weather);
        p = Pattern.compile("SN");
         Matcher sn = p.matcher(weather);
        p = Pattern.compile("IP");
         Matcher ip = p.matcher(weather);
        p = Pattern.compile("FZ");
         Matcher fz = p.matcher(weather);
        p = Pattern.compile("DZ");
         Matcher dz = p.matcher(weather);
        p = Pattern.compile("BR");
         Matcher br = p.matcher(weather);
       if (weather.equals("") == false) {
           if (ts.find()) {
               return "ThunderStorm.png";
           } else if (ra.find()) {
               return "Rain.png";
           } else if (fg.find()) {
               return "Fog.png";
           } else if (ha.find()) {
               return "Hail.phg";
           } else if (sn.find() || ip.find() || fz.find()) {
               return "Snow_Ice.png";
           } else if (dz.find()) {
               return "Mist_Drizzle.png";
           } else if (br.find()) {
               return "Mist_Drizzle.png";
           }
       } else if (spd >= 58 || gst >= 58) {
          // System.out.println("speed:"+wind_speed+">30");
           return "Windy.png";
       } else {
           String cover = "";
           if (sky != null) {
               for (int i = 0; i < sky.length(); i++) {
                   cover = cover + sky.getJSONObject(i).getString("amount");
               }
           }
           if (cover.equals("") == false) {
               p = Pattern.compile("OVC");
               Matcher ovc = p.matcher(cover);
               p = Pattern.compile("BKN");
               Matcher bkn = p.matcher(cover);
               p = Pattern.compile("SCT");
               Matcher sct = p.matcher(cover);
               p = Pattern.compile("FEW");
               Matcher few = p.matcher(cover);
               if (ovc.find()) {
                   return "Cloudy.png";
               } else if (bkn.find()) {
                   return "Mostly_Cloudy.png";
               } else if (sct.find()) {
                   return "Partly_Cloudy.png";
               } else if (few.find()) {
                   return "Mostly_Clear.png";
               } else {
                   return "Clear.png";
               }
       }
       }
       return null;
   }
   public static String getSkyIcon(JSONArray sky) {
       String cover = "";
           if (sky != null) {
               for (int i = 0; i < sky.length(); i++) {
                   cover = cover + sky.getJSONObject(i).getString("amount");
               }
           }
           Pattern p;
           if (cover.equals("") == false) {
               p = Pattern.compile("OVC");
               Matcher ovc = p.matcher(cover);
               p = Pattern.compile("BKN");
               Matcher bkn = p.matcher(cover);
               p = Pattern.compile("SCT");
               Matcher sct = p.matcher(cover);
               p = Pattern.compile("FEW");
               Matcher few = p.matcher(cover);
               if (ovc.find()) {
                   return "Cloudy.png";
               } else if (bkn.find()) {
                   return "Mostly_Cloudy.png";
               } else if (sct.find()) {
                   return "Partly_Cloudy.png";
               } else if (few.find()) {
                   return "Mostly_Clear.png";
               } else {
                   return "Clear.png";
               }
           }
           return "";
   }
//   private MetarStationInfo getDownloadedMetar(String metarId) {
//       for (int i=0 ; i<downloadedMetars.size(); i++) {
//           if (downloadedMetars.get(i).getStationId().equals(metarId)) {
//               return downloadedMetars.get(i);
//           }
//       }
//       return null;
//   }
   
   public void showMetarTimeStamp(boolean value) {
       if (value == true && showMetarTimeStamp == false) {
           showMetarTimeStamp = true;
           repaint();
       } else if (value == false && showMetarTimeStamp == true) {
           showMetarTimeStamp = false;
           repaint();
       }
       
   }
    public void setMetarImage(BufferedImage img) {
        this.image = img;
        repaint();
    }
     @Override
    public void paint(Graphics g) {
        //super.paint(g);
       
        if (image != null) {
            //painted = true;
            g.drawImage(image,0,0,this);
            if (loadMetarSites == true && showMetarSites == true) {
                g.drawImage(metarSiteImg, 0, 0, this);
            }
        }
        if (showMetarTimeStamp == true) {
        if (metarTimeStamp != null) {
            String s  = "Obs:"+metarTimeStamp;
           
            g.setFont(metarTimeStampFont);
            FontMetrics m = g.getFontMetrics(metarTimeStampFont);
            Rectangle2D r = m.getStringBounds(s, g);
            int offset = 110;
            if (ForecastTable.getInstance().isVisible() == false) {
                offset = 0;   
            }
            int x = this.width-(int)r.getWidth()- offset;
            int y = this.height-(int)r.getHeight() +12;
            int w = (int) r.getWidth();
            int h = (int) r.getHeight();
            g.setColor(WeatherMonitorRun.bottomPanel.getBackground());
            g.fillRect(x-5,y-h,w+10,h+10);
             if (metarTimeStampCurrent == false) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawString(s, x,y);
        } else {
             String s  = "...Loading Observations...";
           
            g.setFont(metarTimeStampFont);
            FontMetrics m = g.getFontMetrics(metarTimeStampFont);
            Rectangle2D r = m.getStringBounds(s, g);
            int offset = 110;
            if (ForecastTable.getInstance().isVisible() == false) {
                offset = 0;   
            }
            int x = this.width-(int)r.getWidth()- offset;
            int y = this.height-(int)r.getHeight() +12;
            int w = (int) r.getWidth();
            int h = (int) r.getHeight();
            g.setColor(Color.white);
            g.fillRect(x-5,y-h,w+10,h+10);
            g.setColor(Color.BLACK);
            g.drawString(s, x,y);
        }
        }
    }
   
    private void updateDownloadedMetars(MetarStationInfo msi) {
        //System.out.println("Update downloaded metars size="+downloadedMetars.size()+ " for "+msi.getSiteName()+" "+msi.getStationId());
        if (msi == null) return;
        try {
        String uid = msi.getUniqueId();
        //System.out.println("Incoming metar has uid="+uid);
        boolean addIt = true;
        for (int i=0; i<downloadedMetars.size(); i++) {
            String testuid = downloadedMetars.get(i).getUniqueId();
           // System.out.println("testuid="+testuid +" for "+downloadedMetars.get(i).getStationId());
            if (uid.equals(testuid)) {
                System.out.println("downloaded metars does contain "+msi.getStationId());
                    addIt = false;    
                    downloadedMetars.get(i).updateDataValues(msi);
                
            } 
             
        }
        if (addIt == true) {
            //System.out.println("Adding to downloaded metars did not contain "+msi.getStationId());
            downloadedMetars.add(msi);
        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
   
    
    public void updateMetarTimeStamp(String s, boolean isCurrent) {
        //System.out.println("Set metar time stamp to "+s);
        metarTimeStamp = s ;
        metarTimeStampCurrent = isCurrent;
        repaint();
    }
 
    

   
    @Override
    public void runTask() {
      //  System.out.println("MetarLayer: Run task in MEtarLayer");
      if (isDownloadTime() == true) {
           //  System.out.println("MetarLayer:Update the plot");
             
            
             updateThePlot();
            // System.out.println("MetarLayer:Done with update plot");
        }
       // System.out.println("MetarLayer:Done with task in metarLAyer");
    }

    @Override
    public void setCountdownValue(int number) {
        
    }
  
  
    
    @Override
    public void setMetarPlotList(
            
            final ArrayList<MetarLocationObject> flist,
            final  Point2D fyourLocation,
            final ArrayList<String> fstateIds,
            int w, int h,
            final double[] fbbox,
            final int fzoomLevel) {
         latestMetarUpdateTime = 0;
       metarTimeStamp = null;
         this.width = w;
        this.height = h;
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        //if (bbox[0] != fbbox[0] || bbox[1] != fbbox[1] || bbox[2] != fbbox[2] || bbox[3] != fbbox[3]) {
         //   this.downloadedMetars.clear();
        //}
        bbox = fbbox;
        zoomLevel = fzoomLevel;
        stateIds.clear();
        stateIds.addAll(fstateIds);
       // System.out.println("There are "+stateIds.size()+ " state Ids in MetarLAyer");
        stationData.clear();
        //metarPlotters.clear();
        stationData.addAll ( getStationData(fstateIds) );
        metarLocationsToPlot.clear();
        metarLocationsToPlot.addAll(flist);
       // System.out.println("There are "+metarLocationsToPlot.size()+ " metarsLocationToPlot Ids in MetarLAyer");
        
      
        if (updateTimer != null && stateIds != null) {
            //System.out.println("Restart pdateTimer in MEtarLAyer with stateIds size:"+stateIds.size());
            updateTimer.restart();
        }
        
       
    }

    @Override
    public void networkUp(boolean value) {
        if (value == true) {
            networkDown = false;
            System.out.println("MetarLayer:Network restored = MetarLayer");
           // networkRestored = true;
            if (stateIds != null && stateIds.size() > 0) {

                stationData.clear();

                stationData.addAll(getStationData(stateIds));

                getStationData(stateIds);
                updateThePlot();
            } else {
                 stateIds.clear();
                 stateIds.addAll( Mapping.getInstance().getStateIdsList() );
                 if (stateIds != null) {
                     getStationData(stateIds);
                     updateThePlot();
                 }
            }
            
            
        } else if (value == false) {
            //System.out.println("Network down in Metar Layer");
            networkDown = true;
        }
    }
//    @Override
//    public void setStateIds(final ArrayList<String> fStateIds, final double[] fBbox, final int fzoomLevel) {
//        Thread t = new Thread() {
//            public void run() {
//                stopPlotting = true;
//                while (plotting == true) {
//                    try {
//                        sleep(100);
//                    } catch (InterruptedException ex) {
//                        System.getLogger(MetarLayer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
//                    }
//                }
//                metarStationInfo.clear();
//                bbox = fBbox;
//                zoomLevel = fzoomLevel;
//                stateIds = fStateIds;
//                downloadedMetars.clear();
//                downloadStationData(stateIds);
//                metarStationInfoForBounds = getStationsDataForBounds();
//                loadMetarSites();
//            }
//        };
//        t.start();
//
//    }

//    @Override
//    public void bboxChanged(String bbox, int w, int h) {
//         // double lowerLeftLat = bbox[0];
//        //  double lowerLeftLon = bbox[1];
//        //  double upperRightLat = bbox[2];
//        //  double upperRightLon = bbox[3];
//        this.bbox = bbox;
//        //loadMetarSites();
//    }
  
    @Override
    public void metarPlotterFinished(MetarStationInfo msi, int id, int count) {
       
        numStationsPlotted++;
       // System.out.println("MetarLayer metarplotterfinished numStationsPlotted="+numStationsPlotted+" of "+count);
        if (msi != null) {
       
            updateDownloadedMetars( msi);
            //msi.setIsPlotted(true);
            long updateTime = msi.getObservationTime();
            if (updateTime > latestMetarUpdateTime) {
                latestMetarUpdateTime = updateTime;
                metarTimeStamp = msi.getObservationTimeString();
            }
            this.revalidate();
             this.repaint();
        } 
        if (Timer.getInstance().isInternetUp() && purged == false) {
           // System.out.println("Restart purge timer");
            purgeTimer.restart();
               
         }
        
       
    }

   

 
   
}
