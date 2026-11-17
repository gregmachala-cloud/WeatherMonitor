/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.dataobjects;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;

/**
 *
 * @author greg
 */
public class StormReportInfo {
    private long epoch;
    private String eventType;
    private String eventMag;
    private double lat;
    private double lon;
    private String text;
    private String city;
    private String time;
    private String date;
   private String uuid;
   private String state;
   private String office;
   private double distance;
   private String iconName = "";
   private ImageIcon icon = new ImageIcon(getClass().getResource("/weathermonitor/icons/Unknown.png"));
   public static String[] iconNames = {"Hail.png","Wind.png","Tornado.png","Flood.png","Funnel.png","Snow.png","FreezingRain.png","Sleet.png","HeavyRain.png","Lightning.png","Fire.png"};  
     
    public StormReportInfo(String uuid, String office, double distance, long eventTimeSecs, String city,String state, String eventType, String eventMag, String text, double lat, double lon) {
        this.epoch = eventTimeSecs;
         SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mma");    
        Date d = new Date(eventTimeSecs * 1000);
        this.time = sdf2.format(d);
        this.office = office;
        this.distance = distance;
        this.date = sdf1.format(d);
        this.eventType  = eventType;
        this.icon = getIconFor(eventType);
        this.eventMag = eventMag;
        this.city = city;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
        this.text = text;
        this.date = date;
        this.time = time;
        this.uuid = uuid;
    }
    public ImageIcon getIcon() {
        return this.icon;
    }
    public double getDistance() {
        return distance;
    }
    public String getOffice() {
        return office;
    }
    public String getUuid() {
        return uuid;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public long getEpoch() {
        return epoch;
    }
    public String getText() {
        return text;
    }
    public String getCity() {
        return city;
    }
    public String getState() {
        return state;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public String getEventType() {
        return eventType;
    }
    public String getEventMag() {
        return eventMag;
    }
    public long getEventTime() {
        return epoch;
    }
    public String getIconName() {
        return iconName;
    }
     private ImageIcon getIconFor(String event) {
          //This should be global at top
          //String[] iconNames = {"Hail.png","Wind.png","Tornado.png","Flood.png","Funnel.png","Snow.png","FreezingRain.png","Sleet.png","HeavyRain.png","Lightning.png","Fire.png"};
          String ev = event.toLowerCase();
          
          if (ev.contains("hail")) {
              iconName = "Hail.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Hail.png"));
          } else if (ev.contains("wnd") || ev.contains("wind")) {
              iconName = "Wind.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Wind.png"));
          } else if (ev.contains("flood")) {
              iconName = "Flood.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Flood.png"));
          } else if (ev.contains("tornado")) {
              iconName = "Tornado.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Tornado.png"));
          } else if (ev.contains("funnel")) {
              iconName = "Funnel.png";
             return new ImageIcon(getClass().getResource("/weathermonitor/icons/Funnel.png")); 
          } else if (ev.contains("snow")) {
              iconName = "Snow.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Snow.png"));
          } else if (ev.contains("freezing rain")) {
              iconName = "FreezingRain.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/FreezingRain.png"));
          } else if (ev.contains("sleet")) {
              iconName = "Sleet.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Sleet.png"));
          } else if (ev.contains("rain")) {
              iconName = "HeavyRain.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/HeavyRain.png"));
          } else if (ev.contains("lightning")) {
              iconName = "Lightning.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Lightning.png"));
          } else if (ev.contains("landspout")) {
              iconName = "Tornado.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Tornado.png"));
          } else if (ev.contains("fire")) {
              iconName = "Fire.png";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Fire.png"));
         
          } else {
              iconName = "";
              return new ImageIcon(getClass().getResource("/weathermonitor/icons/Unknown.png"));
          }
      }
}
