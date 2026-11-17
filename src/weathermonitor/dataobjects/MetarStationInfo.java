package weathermonitor.dataobjects;


import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Date;
import utilities.Utilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class MetarStationInfo {
    public static int MISSING_TEMP = 9999;
    public static int MISSING_WIND = -1;
    private int dwpt = 0;
    private String wind_dir = "";
    private int wind_speed = 0;
    private double oneHrPcpn = 0;
    private double threeHrPcpn = 0;
    private double sixHrPcpn = 0;
    private double vsby =10.0;
    private int windDirDeg = 0;
    private Font plotFont = new Font("Monospaced",Font.BOLD,20);
    private long ob_time = 0;
    private long updateTime = 0;
    private String ob_time_string = "";
    private String name = "";
    private String id = "";
    private double lat = 0;
    private double lon = 0;
    private Point point;
    private int tempF = MISSING_TEMP;
    private int oneHourTemp = MISSING_TEMP;
    private String wxIcon = null;
    private Rectangle rect;
    public boolean isPlotted = false;
    private boolean skip = false;
    private int wind_gust = 0;
    private boolean hasNoData = false;
    private Point2D nearestCityLatLon;
    private String uniqueId = "";
    
    public MetarStationInfo(String id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        
    }
    public void updateDataValues(Metar m) {
        this.tempF = m.getTemp();
        this.dwpt = m.getDwpt();
        this.windDirDeg = m.getWindDir();
        this.wind_dir = m.getWindDirString();
        
        this.sixHrPcpn = m.getSixHrPcpn();
        this.oneHrPcpn = m.getOneHrPcpn();
        this.threeHrPcpn = m.getThreeHrPcpn();
        this.vsby = m.getVsby();
        double value = m.getWindGustMPH();
        if (value >= 0) {
            try {
               wind_gust = (int) Math.round(value);
            } catch (Exception ex) {
                wind_gust = MISSING_WIND;
            }
        } else {
            wind_gust = MISSING_WIND;
        }
        value  = m.getWindSpeedMPH();
        if (value >= 0) {
            try {
               wind_speed = (int) Math.round(value);
            } catch (Exception ex) {
                wind_speed = MISSING_WIND;
            }
        } else {
            wind_speed = MISSING_WIND;
        }
        this.ob_time = m.getTime();
        this.ob_time_string = m.getTimeString();
        this.wxIcon = m.getWeatherIcon();
    }
    public void updateDataValues(MetarStationInfo msi) {
        this.tempF = msi.getTemp();
        
        this.dwpt = msi.getDwpt();
        this.windDirDeg = msi.getWindDirDeg();
        this.wind_dir = msi.getWindDirString();
        //System.out.println("old windDir for "+id+" was "+windDirDeg+" new dir="+msi.getWindDirDeg());
        this.wind_gust = msi.getWindGustMPH();
        this.wind_speed = msi.getWindSpeedMPH();
       // System.out.println("old wind speed for "+id+" was "+wind_speed+" new speed="+msi.getWindSpeedMPH());;
        //this.oneHourTemp = msi.getOneHourTemp();
        this.ob_time = msi.getObservationTime();
        this.ob_time_string = msi.getObservationTimeString();
        this.wxIcon = msi.getWeatherIcon();
        this.vsby = msi.getVsby();
        this.oneHrPcpn = msi.getOneHrPcpn();
        this.threeHrPcpn = msi.getThreeHrPcpn();
        this.sixHrPcpn = msi.getSixHrPcpn();
    }
    public void setNearestCityLatLon(Point2D p2d) {
        this.nearestCityLatLon = p2d;
    }
    public Point2D getNearestCityLatLon() {
        return this.nearestCityLatLon;
    }
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }
    public void setIsPlotted(boolean value) {
        this.isPlotted = value;
    }
    public String getWeatherIcon() {
        return wxIcon;
    }
    public void setWeatherIcon(String icon) {
        this.wxIcon = icon;
    }
    public void setSkip(boolean value) {
        this.skip = value;
    }
    public boolean getSkip() {
        return skip;
    }
    public boolean hasNoData() {
        return hasNoData;
    }
    public void setHasNoData(boolean value) {
        this.hasNoData = value;
    }
    public void setPlotFont(Font f) {
        //System.out.println("Setting plot font to "+f.toString());
        this.plotFont = f;
    }
    
    public Font getPlotFont() {
        //System.out.println("Geeting plot font "+plotFont.toString());
        return plotFont;
    }
    public void setSiteID(String id) {
        this.id = id;
    }
    public int getTemp() {
        long curTime = new Date().getTime()/1000;
        long age = curTime-ob_time;
        //System.out.println("Ob age is "+age+" seconds ob time="+ this.ob_time_string);
        if (ob_time > 0 && age > 4500) {
            return tempF;
        }
        return tempF;
    }
    public void setWindDir(int value) {
        
        this.windDirDeg = value;
        this.wind_dir = Utilities.getWindDir(value);
    }
    public int getWindDirDeg() {
        
        return windDirDeg;
    }
    public String getWindDirString() {
        if (wind_dir == null || wind_dir.equals("null")) {
            return "";
        }
        return wind_dir;
    }
    public void setWindGust(int value) {
       
        if (value >= 0) {
           wind_gust = value;
        } else {
            wind_gust = MISSING_WIND;
        }
    }
    
    public void setWindSpeed(int value) {
       
        if (value >= 0) {
           wind_speed = value;
        } else {
            wind_speed = MISSING_WIND;
        }
    }
//    public double getWindSpeedKmHr() {
//        return windSpeedKmhr;
//    }
//    public double getWindGustKmHr() {
//        return windGustKmhr;
//    }
     public int getWindGustMPH() {
        return wind_gust;
    }
    public int getWindSpeedMPH() {
        return wind_speed;
    }
    public int getDwpt() {
        return dwpt;
    }
    public void setDwpt(int value) {
        //System.out.println("Setting dwpt for "+getStationName()+" to "+value);
        this.dwpt = value;
    }
//    public void setTempHistoryIndexes(ArrayList<Long> values) {
//        this.tempHistoryIndex = values;
//    }
//    public void setTempHistoryValues(ArrayList<Integer> values) {
//        this.tempHistory = values;
//    }
//    public ArrayList<Long> getTempHistoryIndexes() {
//        return this.tempHistoryIndex;
//    }
//    public ArrayList<Integer> getTempHistoryValues() {
//        return this.tempHistory;
//    }
//    public void setOneHourTemp(int value) {
//        this.oneHourTemp = value;
//    }
//    public int getOneHourTemp() {
//       
//        return oneHourTemp;
//    }
    public void setOneHrPcpn( double value) {
        this.oneHrPcpn = value;
    }
    public void setThreeHrPcpn(double value) {
        this.threeHrPcpn = value;
    }
    public void setSixHrPcpn(double value) {
        this.sixHrPcpn = value;
    }
    public void setVsby(double value) {
        this.vsby = value;
    }
    public double getOneHrPcpn() {
        return this.oneHrPcpn;
    }
    public double getThreeHrPcpn() {
        return this.threeHrPcpn;
    }
    public double getSixHrPcpn() {
        return this.sixHrPcpn;
    }
    public double getVsby() {
        return this.vsby;
    }
    public void setTemp(int tempe, long time) {
        this.tempF = tempe;
       
//        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d hh:mma");
//        Instant instantFromSecond = Instant.ofEpochSecond(time);
//        ZoneId systemZone = ZoneId.systemDefault();
//        ZonedDateTime zonedFromSecond = instantFromSecond.atZone(systemZone);
//        LocalDateTime localDateTime = zonedFromSecond.toLocalDateTime();
//       
//         String updatedTimeString = localDateTime.format(outputFormat);
//        
//        this.setObservationTimeString(updatedTimeString);
        this.setObservationTime(time);
        
    }
//    public ArrayList<Metar> getMetarHistory() {
//        return metarHistory;
//    }
//    public void clearMetarHistory() {
//        this.metarHistory.clear();
//    }
//    public void appendToMetarHistory(ArrayList<Metar> metarHistory) {
//        this.metarHistory.addAll(metarHistory);
//    }
    public String getSiteName() {
        return name;
    }
    public void setSiteName(String name) {
        this.name = name;
    }
    public long getObservationTime() {
        return ob_time;
    }
    public long getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(long time) {
        updateTime = time;
    }
    public void setObservationTime(long time) {
        this.ob_time = time;
    }
    public String getObservationTimeString() {
        return ob_time_string;
    }
    public void setObservationTimeString(String s) {
        this.ob_time_string = s;
    }
    public String getStationId(String stationName) {//stationName is like San Angelo TX   returns KSJT
        return "";
    }
    public String getStationId() {
        return id;
    }
    public String getStationName() {
        return name;
    }
   public double getLat() {
       return lat;
   }
   public double getLon() {
       return lon;
   }
    public Point getPlotXYLocation() {
       
        return this.point;
    }
   
    public Point getPlotXYLocation(double[] bbox,int w, int h) {
        if (point == null) {
           // System.out.println("Point is null so use "+bbox+"lat:"+lat+" lon:"+lon+" w:"+w+" h"+h+" to find it");
            int[] xy = worldToPixel(bbox,lon,lat,w,h);
            //System.out.println("The point is :"+xy[0]+" "+xy[1]);
            return new Point(xy[0],xy[1]);
            
        }
        return this.point;
    }
   
    public void setPlotXYLocation(Point p) {
        this.uniqueId = this.id+p.x+":"+p.y;
        this.point = p;
    }
    public Rectangle getPlotBoundingRectangle() {
        return rect;
    }
    public void setBoundingRectangle(Rectangle rect) {
        this.rect = rect;
    }
    private static int[] worldToPixel(double[] bbox, double t_lon,double t_lat,int w,int h) {
            // double lowerLeftLat = bbox[0];
        //  double lowerLeftLon = bbox[1];
        //  double upperRightLat = bbox[2];
        //  double upperRightLon = bbox[3];
            
            double lowerLeftLat = bbox[0];
            double lowerLeftLon = bbox[1];
            double upperRightLat = bbox[2];
            double upperRightLon = bbox[3];
            double dppX = (upperRightLon - lowerLeftLon)/w;
            double dppY = (upperRightLat - lowerLeftLat)/h;
            double latDiff = upperRightLat - t_lat;
            double lonDiff = t_lon - lowerLeftLon;
            
            int x = Double.valueOf(lonDiff / dppX).intValue();
            int y = Double.valueOf(latDiff / dppY).intValue();
         
            int[] retVal = {x,y};
            return retVal;
      }
}
