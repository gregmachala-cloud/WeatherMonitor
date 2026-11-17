/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mappingtools;

import utilities.Utilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author greg
 */
public class GeocodeOfficeIds {
    
    public GeocodeOfficeIds() {
        Properties props = Utilities.readPrefs("/Maps" + Utilities.SEP + "OfficeIds.properties");
        Properties newProps = new Properties();
//FSD=Sioux Falls|SD
        Enumeration en = props.keys();
        while (en.hasMoreElements()) {
            String office = en.nextElement().toString();
            String data = props.get(office).toString();
            String[] city_st = data.split("\\|");
            String city = city_st[0];
            String st = city_st[1];
            double[] lonlat = lookupAddress(city + ", " + st);
            if (lonlat[0] != 0 && lonlat[1] != 0) {
                newProps.put(office, city + "|" + st + "|" + lonlat[0] + "|" + lonlat[1]);
            } else {
                newProps.put(office, city + "|" + st + " | | ");
            }
            System.out.println( office +" - "+city + "|" + st + "|" + lonlat[0] + "|" + lonlat[1]);
        }
        Utilities.savePrefs(newProps, "/Maps/OfficeIds_new.properties");
    }
    public double[] lookupAddress(String addr) {
        double[] retVal = new double[2];
        double lon = 0;
        double lat = 0;
        retVal[0] = 0;
        retVal[1] = 0;
         String dlink = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?SingleLine=";
                 dlink += addr.replaceAll(" ", "+");
                 dlink += "&forStorage=false&f=pjson";
                 String json = "";
                 try {
                     URL url = new URL(dlink);
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(url.openStream()));
                     String inputLine;

                     while ((inputLine = in.readLine()) != null) {
                         json = json + "\n" + inputLine;
                     }
                     in.close();
                 
                 JSONObject obj = new JSONObject(json);
                 JSONArray candidates = obj.getJSONArray("candidates");
                 String x = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("x").toString();
                 String y = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("y").toString();
                
                 
                 try {
                 if (y.length() >= 8) {
                     lat = Double.valueOf(y.substring(0,8));
                 } else {
                     lat = Double.valueOf(y);
                 }
                 
                 if (x.length() >= 9) {
                     lon = Double.valueOf(x.substring(0,9));
                 } else {
                     lon = Double.valueOf(x);
                 }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 retVal[0] = lon;
                 retVal[1] = lat;
                 return retVal;
                 
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 return retVal;
    }
    public static void main(String[] args) {
        GeocodeOfficeIds gid = new GeocodeOfficeIds();
    }
}
