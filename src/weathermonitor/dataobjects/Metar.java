package weathermonitor.dataobjects;

import java.awt.Font;
import javax.swing.ImageIcon;
import utilities.Utilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class Metar {
    private int tempF;
    private long time;
    private String id;
    private int dir = 0;
    private int speed = -1;
    private int gust = -1;
    private int dwpt;
    private String weatherString;
    private Integer maxTemp;
    private Integer minTemp;
    private Font font;
    private String name;
    private String timeString = null;
    private String skyString = "";
    private ImageIcon skyIcon = null;
    private String wxIcon = "";
    private double sixHrPcpn = 0;
    private double threeHrPcpn = 0;
    private double oneHrPcpn = 0;
    private double vsby = 0;
    // speed mph and dir deg
    // Used to 24 hour metar sample
     public Metar(String id, int temp, int dwpt, int dir, int speed,int gust, long time,double oneHrPcpn, double threeHrPcpn, double sixHrPcpn, double vsby, Font mapFont) {
         this.tempF = temp;
         this.dwpt = dwpt;
         this.dir = dir;
         this.speed = speed;
         this.gust = gust;
         this.time = time;
         this.id = id;
         this.sixHrPcpn = sixHrPcpn;
         this.oneHrPcpn = oneHrPcpn;
         this.threeHrPcpn = threeHrPcpn;
         this.vsby = vsby;
         this.font = mapFont;
     }
     // USed for metar plots
     public Metar(String id, String name, String skyIconString, Integer temp, Integer dwpt, Integer dir, Integer speed,Integer gust,String weather,String wxIcon, Integer max, Integer min, double oneHrPcpn, double threeHrPcpn, double sixHrPcpn, double vsby,long time,String timeString) {
         this.tempF = temp;
         this.maxTemp = max;
         this.minTemp = min;
         this.skyIcon = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+skyIconString));
         this.sixHrPcpn = sixHrPcpn;
         this.oneHrPcpn = oneHrPcpn;
         this.threeHrPcpn = threeHrPcpn;
         this.vsby = vsby;
         this.weatherString = weather;
         this.wxIcon = wxIcon;
         this.dwpt = dwpt;
         this.dir = dir;
         this.name = name;
         this.skyString = skyIconString;
         this.speed = speed;
         this.gust = gust;
         this.time = time;
         this.id = id;
         
         this.timeString = timeString;
         
     }
     public String getOneHrPcpnString() {
         if (this.oneHrPcpn < 0) {
             return "";
         }
         return Double.toString(oneHrPcpn);
     }
      public String getThreeHrPcpnString() {
         if (this.threeHrPcpn < 0) {
             return "";
         }
         return Double.toString(threeHrPcpn);
     }
       public String getSixHrPcpnString() {
         if (this.sixHrPcpn < 0) {
             return "";
         }
         return Double.toString(sixHrPcpn);
     }
       public String getVsbyString() {
           if (this.vsby < 0) {
               return "";
           } 
           return Double.toString(vsby);
       }
     public String getWeatherIcon() {
         return wxIcon;
     }
     public double getWindSpeedMPH() {
         return speed;
     }
     public double getWindGustMPH() {
         return gust;
     }
     public ImageIcon getSkyIcon() {
         return skyIcon;
     }
     public String getSkyString() {
         return skyString;
     }
     public String getSiteName() {
         return name;
     }
     public String getTimeString() {
         return timeString;
     }
     public String getId() {
         return id;
     }
     public long getTime() {
         return time;
     }
     public void setTime(long time) {
         this.time = time;
     }
     public void setTemp(int temp) {
         this.tempF = temp;
     }
     public int getTemp() {
         return tempF;
     }
     public double getOneHrPcpn() {
         return this.oneHrPcpn;
     }
     public double getSixHrPcpn() {
         return this.sixHrPcpn;
     }
     public double getThreeHrPcpn() {
         return this.threeHrPcpn;
     }
     public double getVsby() {
         return this.vsby;
     }
      public String getMaxTempString() {
          if(maxTemp != null) {
              if (maxTemp > 140 || maxTemp < -100) {
                  return "";
              } else {
                  return Integer.toString(maxTemp);
              }
          }
          return "";
     }
      public String getMinTempString() {
          if(minTemp != null) {
              if (minTemp > 140 || minTemp < -100) {
                  return "";
              } else {
                  return Integer.toString(minTemp);
              }
          }
          return "";
     }
     public String getTempString() {
         if (tempF > 140 || tempF < -100) {
             return "M";
         }
         return Integer.toString(tempF);
     }
     public void setDwpt(int dwpt) {
         
         this.dwpt = dwpt;
     }
     public int getDwpt() {
         return dwpt;
     }
     public String getDwptString() {
         if (dwpt > 100) {
             return "M";
         }
         return Integer.toString(dwpt);
     }
     public void setWeatherString (String s) {
         this.weatherString = s;
     }
     public String getWeatherString() {
         return weatherString;
     }
     public int getWindDir() {
         return dir;
     }
     public void setWindDir(int dir) {
         this.dir = dir;
     }
     public double getWindGust() {
         return gust;
     }
     public double getWindSpeed() {
         return speed;
     }
     public int getWindGustInt() {
         if (gust > 0) {
            try {
            return Integer.parseInt(Utilities.roundDouble(gust, 0));
         } catch (Exception ex) {
            int v = (int) Math.round(gust);
            return v;
         }
         }
         return 0;
     }
     public int getWindSpeedInt() {
         try {
            return Integer.parseInt(Utilities.roundDouble(speed, 0));
         } catch (Exception ex) {
            int v = (int) Math.round(speed);
            return v;
         }
     }
     public String getWindDirString() {
         return Utilities.getWindDir(dir);
     }
//     public String getThreeHrPrecipString() {
//         if (threeHrPrecip != null) {
//             return Utilities.roundDouble(threeHrPrecip, 2);
//         }
//         return "";
//     }
     
     public String getWindGustString() {
         if (gust <= 0) return "";
         return Utilities.roundDouble(gust, 0);
     }
     public String getWindSpeedString() {
         if (speed <= 0) return "";
         return Utilities.roundDouble(speed, 0);
     }
     public void setWindSpeed(int value ) {
         if (value > 0) {
             speed = value;
         } else {
             speed = -1;
         }
     }
     public void setWindGust(int value ) {
         if  (value > 0) {
            gust = value;
         } else {
             gust = -1;
         }
     }
     public void setMapFont(Font f) {
         this.font = f;
     }
     public Font getMapFont() {
         return font;
     }
}
