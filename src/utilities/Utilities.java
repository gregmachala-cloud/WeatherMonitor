/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

import weathermonitor.dataobjects.MetarStationInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mappingtools.ImageViewer;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.threads.Timer;

/**
 *
 * @author greg
 */
public class Utilities {
    public static  String DIR = System.getProperty("user.dir");
    public static String SEP = System.getProperty("file.separator");
    private Hashtable<String, String> jsonMapCache = new Hashtable<>();
    public static String[] states = {"AK","HI","GU","PR","MN","NJ","DC","VA","TX","MS","LA","AL","FL","GA","SC","NC","TN","AR","MO","OK","CO","NM","AZ","NV","CA","WY","UT","ND","SD","NE","KS","IA","IL","OH","ID","WA","MT","IN","KY","WI","MI","NY","VT","NH","ME","DE","RI","MA","MD","PA","WV","CT","OR"};
    //public static String[] states = {"DC"};
    private static Hashtable<Integer,String> windDirHash = new Hashtable<Integer,String>();
    private static Utilities me = null;
    private static final String setupFile = "/weathermonitor/settings/props/OfficeIds.properties";
    public final Properties officeIdsProps;
    public static  BufferedImage nullImage = null;
     private static Properties versionProps = null;
    
    public static Utilities getInstance() {
        if (me == null) me = new Utilities();
        return me;
    }
    private Utilities() {
       officeIdsProps  = readPropFileFromJar(setupFile);
    }
    
   
    
    public static ArrayList<MetarStationInfo> downloadStationData(String stateId) {
        return getMetarStationInfo(stateId);
//        File test = new File(Utilities.DIR+Utilities.SEP+"Maps"+Utilities.SEP+stateId+"_MetarData.properties");
//        if (test.exists()) {
//           // System.out.println("Using "+test.toString());
//            return getMetarStationInfo(stateId);
//        }
//        Properties props = new Properties();
//        ArrayList<MetarStationInfo> list = new ArrayList<MetarStationInfo>();
//        String dlink = "https://api.weather.gov/stations?state="+stateId+"&limit=250";
//        System.out.println("metar station dlink="+dlink);
//        String json = downloadJsonData(dlink,10000);
//        boolean keepGoing = true;
//        int totalFeatures = 0;
//        while (keepGoing == true) {
//            
//            String nextSet = "";
//        if (json != null && json.equals("") == false) {
//            JSONObject obj = new JSONObject(json);
//            JSONArray features = obj.getJSONArray("features");
//            System.out.println("There are "+features.length()+" metar stations in "+stateId);
//            if (features.length() == 0 || totalFeatures >= 10000) {
//                nextSet = "";
//            } else {
//                nextSet =  obj.getJSONObject("pagination").getString("next");
//            }
//            totalFeatures = totalFeatures + features.length();
//            for (int i = 0; i < features.length(); i++) {
//                
//                String stationId = features.getJSONObject(i).getJSONObject("properties").getString("stationIdentifier");
//                String name = features.getJSONObject(i).getJSONObject("properties").getString("name").replaceAll(",","");
//               
//                if (stationId.contains("COOP")== false) {
//                    JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
//                    String lon = coords.get(0).toString();
//                    String lat = coords.get(1).toString();
//                    props.put(stationId, lat+","+lon+","+name+" , , ,");//lat lon cityname temp time
//                            
//                }
//            }
//            
//        }
//        if (nextSet.equals("") ) {
//                System.out.println("Done with station data nextSet=>"+nextSet+"<");
//                keepGoing = false;
//            } else {
//                keepGoing = true;
//                json = downloadJsonData(nextSet,10000);
//            }
//          System.out.println("next page is "+nextSet);
//            
//            
//        }
//        System.out.println("There are "+props.size()+" metar locations to save");
//        Utilities.saveMetarData(props,stateId);
//        return getMetarStationInfo(stateId);
    }
    public static int getRandomIntegerForColors() {
         int randomNum = ThreadLocalRandom.current().nextInt(0, 254 + 1);
         return randomNum;
    }
    public static String arrayToString(String[] arr) {
        String retVal = "";
        for (int i=0; i< arr.length; i++) {
            retVal = retVal+","+arr[i];
        }
        if (retVal.length() >= 2) {
             return retVal.substring(1);
        } else {
             return "";
        }
    }
     public String getOfficeCity(String oid) {
         if (officeIdsProps.containsKey(oid)) {
            String[] arr = officeIdsProps.get(oid).toString().split("\\|");
            
            return arr[0]+", "+arr[1];
        }
         return oid;
    }
    
    public  String getLongOfficeName(String oid) {
        if (officeIdsProps.containsKey(oid)) {
            String[] arr = officeIdsProps.get(oid).toString().split("\\|");
            
            return "NWS-"+oid+" "+arr[0]+", "+arr[1];
        }
        return oid;
    }
     public  String getShortOfficeName(String oid) {
        if (officeIdsProps.containsKey(oid)) {
            String[] arr = officeIdsProps.get(oid).toString().split("\\|");
            
            return "NWS-"+arr[0]+", "+arr[1];
        }
        return oid;
    }
     public  double[] getOfficeLatLon(String oid) {
           String data = officeIdsProps.get(oid).toString();
            String[] city_st = data.split("\\|");
           
            double tlon = Double.parseDouble(city_st[2]);
            double tlat = Double.parseDouble(city_st[3]);
            double[] retVal = new double[2];
            retVal[0] = tlat;
            retVal[1] = tlon;
            return retVal;
     }
    public static int getRandomInteger(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 1000 + 1);
         return randomNum;
    }
    public static boolean isAnAddress(String text) {
        Pattern pat = Pattern.compile("^[0-9]")   ;
        Matcher m = pat.matcher(text);
        return m.find();
    }
    private static void forceInternetTimeout(final HttpURLConnection conn) {
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                   conn.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.start();
                
    }
    private static String getAllChanges(Properties p) {
        String changeLog = "";
        int index = 1;
       String key = "ChangeLog"+index;
       while (p.containsKey(key)) {
           String cl = p.getProperty(key).toString();
           changeLog+=index+":"+cl+"\n";
           index++;
           key = "ChangeLog"+index;
       }
       return changeLog;
    }
    public static String getChangeLog() {
        if (versionProps != null) {
            if (versionProps.containsKey("ChangeLog1")) {
                return getAllChanges(versionProps);
            } else {
                return "?";
            }
        }
        File test = new File(Utilities.DIR + Utilities.SEP + "VERSION.properties");
        if (test.exists()) {
            try {
                  versionProps = new Properties();
                FileInputStream is = new FileInputStream(test.getPath());
                versionProps.load(is);
                is.close();
               if (versionProps.containsKey("ChangeLog1")) {
                return getAllChanges(versionProps);
            } else {
                return "?";
            }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "?";
    }
    public static String getLastUpdated() {
         if (versionProps != null) {
            if (versionProps.containsKey("LastUpdated")) {
                return versionProps.getProperty("LastUpdated").toString();
            } else {
                return "?";
            }
        }
         File test = new File(Utilities.DIR + Utilities.SEP + "VERSION.properties");
        if (test.exists()) {
            try {
                versionProps = new Properties();
                FileInputStream is = new FileInputStream(test.getPath());
                versionProps.load(is);
                is.close();
                if (versionProps.containsKey("LastUpdated")) {
                    return versionProps.getProperty("LastUpdated").toString();
                } else {
                    return "?";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "?";
    }
    public static String getCurrentVersion() {
        if (versionProps != null) {
            if (versionProps.containsKey("Version")) {
                return versionProps.getProperty("Version").toString();
            } else {
                return "?";
            }
        }
        File test = new File(Utilities.DIR + Utilities.SEP + "VERSION.properties");
        if (test.exists()) {
            try {
                  versionProps = new Properties();
                FileInputStream is = new FileInputStream(test.getPath());
                versionProps.load(is);
                is.close();
                if (versionProps.containsKey("Version")) {
                    return versionProps.getProperty("Version").toString();
                } else {
                    return "?";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "?";
    }
    
    public static boolean isInternetUp() {
        String dlink = "https://api.weather.gov/offices/SJT";
        HttpURLConnection connection = null;
         try {
            System.out.println("Check if internet is up");
            URL url = new URL(dlink);
            System.out.println("    new URL("+dlink+")");
            connection = (HttpURLConnection) url.openConnection();
            System.out.println("    url connection opened");
            forceInternetTimeout(connection);
            connection.setConnectTimeout(5000); // 5 seconds connectTimeout
            System.out.println("    setConnectTimeout to 5000");
            connection.setReadTimeout(5000 ); // 5 seconds socketTimeout
            System.out.println("    setReadTimeout to 5000");
            connection.connect();
            System.out.println("    connection.connect() finished");
        } catch (Exception e) {
            System.out.println("Internet down in Utilities.isInternetUp()");
           return false;
        } finally {
             try {
                 connection.disconnect();
             } catch (Exception ex) {
                 ex.printStackTrace();
                 return false;
             }
        }
         System.out.println("   connected to internet");
        return true;
    }
    
    public static String downloadJsonData(String dlink, int timeout) {
        if (Timer.getInstance().isInternetUp() == false) return null;
        
        String json = "";
        try {
              HttpURLConnection connection = null;
                URL url = new URL(dlink);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 seconds connectTimeout
            connection.setReadTimeout(5000 ); // 5 seconds socketTimeout
            connection.connect();
           
            int bufferSize = 1000*1024;
            char[] buffer = new char[bufferSize];
             StringBuilder out = new StringBuilder();
            
             Reader ins = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
             for (int numRead; (numRead = ins.read(buffer, 0, buffer.length)) > 0; ) {
                 
                 out.append(buffer, 0, numRead);
                 
             }
            // System.out.println("There are "+lines+" lines of json data for "+dlink);
                
              connection.disconnect();
              ins.close();
              return out.toString();
        } catch (Exception ex) {
            
            return null;
        }
    }
    
    public static MetarStationInfo getMetarData(String metarId) {
        System.out.println("Get metar data for "+metarId);
        MetarStationInfo msi = null;
        String json = downloadJsonData("https://api.weather.gov/stations/" + metarId + "/observations/latest?require_qc=false",5000);
        //MetarStationInfo msi = new MetarStationInfo();
        if (json != null && json.equals("") == false) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray coords = obj.getJSONObject("geometry").getJSONArray("coordinates");
                msi = new MetarStationInfo(metarId, coords.getDouble(1), coords.getDouble(0));
                String name = obj.getJSONObject("properties").getString("name").replaceAll(",","");
               
                String temp = obj.getJSONObject("properties").getJSONObject("temperature").get("value").toString();
                String dwpt = obj.getJSONObject("properties").getJSONObject("dewpoint").get("value").toString();
                String wspd = obj.getJSONObject("properties").getJSONObject("windSpeed").get("value").toString();
                String wdir = obj.getJSONObject("properties").getJSONObject("windDirection").get("value").toString();
                String wgst = obj.getJSONObject("properties").getJSONObject("windGust").get("value").toString();
                
                int windSpeed = MetarStationInfo.MISSING_WIND;
                try {
                    windSpeed = (int) Math.round(Double.valueOf(wspd) / 1.609344);
                } catch (Exception ex) {
                    windSpeed = MetarStationInfo.MISSING_WIND;
                    //ex.printStackTrace();
                }
                int windGust = MetarStationInfo.MISSING_WIND;
                try {
                    windGust = (int) Math.round(Double.valueOf(wgst) / 1.609344);
                } catch (Exception ex) {
                    windGust = MetarStationInfo.MISSING_WIND;
                    //ex.printStackTrace();
                }
                int windDir = 0;
                try {
                    windDir = Integer.valueOf(wdir);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
//"timestamp": "2026-09-23T21:25:00+00:00",
                String timestamp = obj.getJSONObject("properties").get("timestamp").toString().replaceAll("T", " ");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("EEE MMM d hh:mma");
                LocalDateTime localDateTime = LocalDateTime.parse(timestamp.substring(0, 16), formatter);
                TimeZone tz = TimeZone.getTimeZone("America/Chicago");
                ZoneId zoneId = ZoneId.of("Z");
                long epochSeconds = localDateTime.atZone(zoneId).toEpochSecond();
                int hours = tz.getOffset(new Date().getTime()) / 1000 / 60 / 60 * -1;
                localDateTime = localDateTime.minusHours(hours);
                String updatedTime = localDateTime.format(outputFormat);

                
                msi.setObservationTime(epochSeconds);
                
                msi.setObservationTimeString(updatedTime);
                msi.setSiteName(name);
                
                msi.setWindDir(windDir);
                msi.setWindSpeed(windSpeed);
                msi.setWindGust(windGust);
                int tempF = MetarStationInfo.MISSING_TEMP;
                try {
                    double tempC = Double.parseDouble(temp);
                    tempF = Utilities.tempCtoF(tempC);
                    msi.setTemp(tempF, epochSeconds);

                } catch (Exception ex) {
                    msi.setTemp(MetarStationInfo.MISSING_TEMP, epochSeconds);

                }
                int dwptF = MetarStationInfo.MISSING_TEMP;
                try {
                    double dwptC = Double.parseDouble(dwpt);
                    dwptF = Utilities.tempCtoF(dwptC);
                    msi.setDwpt(dwptF);

                } catch (Exception ex) {
                    msi.setDwpt(MetarStationInfo.MISSING_TEMP);

                }
            } catch (Exception ex) {
            }
        }
        return msi;
    }
     
    public static ArrayList<MetarStationInfo> getMetarStationInfo(String stateId) {
       // System.out.println("Get metar station info for "+stateId);
        ArrayList<MetarStationInfo> list = new ArrayList<MetarStationInfo>();
        Properties p = Utilities.getInstance().readMetarData(stateId);
        if (p == null) return null;
        Enumeration en = p.keys();
        while (en.hasMoreElements()) {
           String id = en.nextElement().toString();
           String[] arr = p.get(id).toString().split(",");
           Double lat = Double.valueOf(arr[0]);
           Double lon = Double.valueOf(arr[1]);
           String name = arr[2];
          
//           String temp = arr[3];
//           int tempF = MetarStationInfo.MISSING_TEMP;
//           if (temp.trim().equals("") == false) {
//               
//               double tempC = Double.valueOf(temp);
//               tempF = Utilities.tempCtoF(tempC);
//           }
//           String time = arr[4];
//           long observationTime = 0;
//           if (time.trim().equals("") == false) {
//                observationTime = Long.valueOf(time);
//           }
           MetarStationInfo msi = new MetarStationInfo(id,lat,lon);
//           msi.setObservationTime(observationTime);
           msi.setSiteName(name);
//           msi.setTemp(tempF);
           list.add(msi);
        }
        return list;
    }
    public static void downloadMaps(String type) {//Road or Counties
        ///source/sjt/GeoMaps/" + st + "_Roads.json
        ///
        System.out.println("statest size=" + states.length);
        for (int i = 0; i < states.length; i++) {
            URL url;
            try {
                ///source/sjt/GeoMaps/LA_Roads.json
                File test = new File("/home/greg/Maps/"+states[i]+"_"+type+".json");
                if (test.exists() == false) {
                FileWriter os = new FileWriter("/home/greg/Maps/" + states[i] + "_"+type+".json");
                url = new URL("https://www.weather.gov/source/sjt/GeoMaps/" + states[i] + "_"+type+".json");
                //System.out.println("Download "+url);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                int lines = 0;
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    os.write(inputLine+"\n");
                }
                in.close();
                os.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
              //  System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }
    }
    public static BufferedImage getEmptyImage() {
       if (nullImage == null) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        nullImage = new BufferedImage((int)d.getWidth(),(int)d.getHeight(), BufferedImage.TYPE_INT_ARGB);
       }
       return nullImage;
    }
    public static Color getFgColor(Color bgColor) {
         Color fgColor = Color.WHITE;
         if (bgColor != null) {
            try {
                int r = bgColor.getRed();
                int g = bgColor.getGreen();
                int b = bgColor.getBlue();

                if ((r * 0.299 + g * 0.587 + b * 0.114) > 186) {
                    fgColor = Color.BLACK;
                }
            } catch (Exception ex) {
            }
        }
        
        return fgColor;
    }
    private static double exprapolateBetween(Double sVal, Double eVal, int progress, int incrWidth) {
        if (progress == 0) return sVal;
        if (progress == incrWidth) return eVal;
        Double diff = eVal - sVal;
        return sVal + ((diff/incrWidth) * progress);
    }
    public static Properties getRadarLegendScale(String layer_name) {
        return readPrefs("Maps"+SEP+layer_name+".properties");
    }
    private static Color getHtKeyAtValue(Hashtable<Color,String> ht, String value) {
        Enumeration en = ht.keys();
        int j = 0;
        while (en.hasMoreElements()) {
             Color c = (Color) en.nextElement();
//             if (j >= 10 && j < 25) {
//                System.out.println("see if ht.get(c):"+ht.get(c)+" == "+value);
//             }
             if (ht.get(c).equals(value)) {
                 //System.out.println("Got color "+c.toString()+" for value "+value);
                 return c;
             }
             j++;
        }
        return null;
    }
    public static Hashtable<Color,String> scanRadarLegend(Image img, ArrayList<String> scale) {
         Hashtable<Color,String> ht = new Hashtable<Color,String>();
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        BufferedImage bufImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g = bufImg.getGraphics();
        g.drawImage(img,0,0,null);
        g.dispose();
        int numOfValues = scale.size();
       // System.out.println("There are "+numOfValues+" values in the scale");
        int incr = width/numOfValues;
        int pixel = incr / 2;
        //System.out.println("The start pix is "+pixel+" with incr="+incr);
        
        for (int i=0; i<numOfValues; i++) {
            //System.out.println("scan radar legend pixel:"+i);
             int rgb = bufImg.getRGB(pixel, 2);
             Color c = new Color(rgb);
            // System.out.println("color="+c.toString());
             ht.put(c,scale.get(i));
             pixel = pixel + incr;
        }
        return ht;
    }
    
    public static Hashtable<Color,String> scanRadarLegend(Image img, ArrayList<Double> scale, Double maxValue, int decimalPlaces, String unit, int startPixel) {
        
        Hashtable<Color,String> ht = new Hashtable<Color,String>();
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        BufferedImage bufImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g = bufImg.getGraphics();
        g.drawImage(img,0,0,null);
        g.dispose();
        int numOfValues = scale.size();
        int splitAt = numOfValues - 1;
        int incr = 0; int index=0; int progress =0;
        scale.add(maxValue);
        for (int i=startPixel; i<width; i++) {
             int rgb = bufImg.getRGB(i, 2);
            // System.out.println(i);
             try {
             Color c = new Color(rgb);
             //System.out.println("i="+i+" rgb="+c.getRed()+","+c.getGreen()+","+c.getBlue()+ "rgb="+rgb);
             int sum = c.getRed()+c.getBlue()+c.getGreen();
             if (sum >= 0 && incr == 0) {
                 
                 int diff = width - i;
                 incr = diff/splitAt;
                 //System.out.println("start scanning legend colors at "+i+" with incr="+incr);
             } else if (incr > 0 && progress < incr) {
                 double v = exprapolateBetween(scale.get(index),scale.get(index+1),progress,incr);
                // System.out.println("Extrap between "+scale.get(index) +" and "+scale.get(index+1));                 
//System.out.println("Color at x="+i+" = "+rgb+" amt="+v);
                 if (sum > 0) ht.put(c,roundDouble(v,decimalPlaces)+unit);
                 //System.out.println("color="+c.toString()+" string="+roundDouble(v,decimalPlaces));
                 progress = progress + 1;
             } else if (incr > 0) {
                 progress = 0; index = index + 1;
                 try {
                 double v = exprapolateBetween(scale.get(index),scale.get(index+1),progress,incr);
                 //System.out.println("Color at x="+i+" = "+rgb+" amt="+v);
                 if (sum > 0) ht.put(c,roundDouble(v,decimalPlaces)+unit);
                 } catch (Exception ex) {
                     //System.out.println("Reaced the end of legend at "+i);
                     break;
                 }
                 progress = progress + 1;
             }} catch (Exception ex) {
                 ex.printStackTrace();
             }
        }
        //savePrefs(props, "OHP.properties");
        
        return ht;
    }
    private void testLegendScale(Hashtable<Color,String> ht, String unit) {
        BufferedImage buf = new BufferedImage(2000,200,BufferedImage.TYPE_INT_RGB);
        Graphics gf = buf.getGraphics();int x = 0; int y = 0;
        gf.setColor(Color.WHITE);
        gf.setFont(new Font("Monospaced",Font.PLAIN,12));
        gf.fillRect(0,0,2000,200);
        int pixCount = 1;
        //double i = 0.01;  
        int end = 1700; double nextVal = 1.0;
        for (int i=0; i<end; i++) {
            
            double t = roundDoubleTwoPlaces(i*0.01);
            String testVal = Double.toString(t).trim();
            
            System.out.println(testVal);
            //System.out.println("See if ht contains "+testVal);
            //if (ht.containsValue(testVal+unit)) {
                Color c = getHtKeyAtValue(ht,testVal+unit);
                if (c != null) {
                gf.setColor(c);
                gf.fillRect(x,y,1,10);
                if (t >= nextVal) {
                    gf.setColor(Color.BLACK);
                    System.out.println("Draw tick mark for "+testVal);
                    gf.fillRect(x,y+10,1,10);
                    nextVal = nextVal+1.0;
                }
//                if (pixCount >= 100) {
//                    pixCount = 1;
//                    gf.setColor(Color.BLACK);
//                    gf.drawString(testVal, x-5, 20);
//                }
                x = x + 1;
                }
                pixCount = pixCount+1;
                        
            //}
            //i = i+0.01;
            
        }
        gf.dispose();
        ImageViewer.getInstance().showImage(buf);
    }
     public static boolean isTopOfHour() {
        SimpleDateFormat sfd = new SimpleDateFormat("mm");
        Date d= new Date();
        String mm = sfd.format(d);
        //System.out.println("Current minute is "+mm);
        if (mm.equals("00") || mm.equals("15") || mm.equals("30") || mm.equals("45")) {
            //System.out.println("Is top of hour");
            return true;
        }
        //System.out.println("Is not the top of the hour");
        return false;
    }
    public static String getWindDir(int n) {
        if (windDirHash.size() == 0) {
            for (int i=0; i<22; i++) {
                windDirHash.put(i,"N");
            }
            for (int i=23; i<67; i++) {
                windDirHash.put(i,"NE");
            }
            for (int i=68; i<112; i++) {
                windDirHash.put(i,"E");
            }
            for (int i=113; i<158; i++) {
                windDirHash.put(i,"SE");
            }
            for (int i=159; i<202; i++) {
                windDirHash.put(i,"S");
            }
            for (int i=203; i<247; i++) {
                windDirHash.put(i,"SW");
            }
            for (int i=248; i<292; i++) {
                windDirHash.put(i,"W");
            }
            for (int i=293; i<338; i++) {
                windDirHash.put(i,"NW");
            }
            for (int i=339; i<=360; i++) {
                windDirHash.put(i,"N");
            }
        }
        if (n >=0 && n<=360 && windDirHash.containsKey(n)) {
            return windDirHash.get(n);
        } else {
            return "";
        }
    }
    public static String roundDouble(double v, int places) {
        if (places == 0 ) {
            return Long.toString((long) (Math.round(v*10.0)/10.0));
        }
        //System.out.println("Round "+v+" to "+places+" places");
        double pl = Math.pow(10.0, places);
        double newValues = Math.round(v*pl) / pl;
        
        return Double.toString(newValues);
    }
    public static double roundDouble(double v) {
        double newValues = Math.round(v*10000.0) / 10000.0;
        return newValues;
    }
     public static double roundDoubleTwoPlaces(double v) {
         
        double newValues = Math.round(v*100.0) / 100.0;
        return newValues;
    }
    public static void saveRadarPositionAndSize(int zoomLevel,Component c,int w, int h) {
        Properties props = readPrefs();
        if (zoomLevel >= 0) {
            props.put("ZoomLevel", Integer.toString(zoomLevel));
        }
        props.put("LocalRadar",c.getX()+","+c.getY()+","+ w+","+h+","+c.getWidth()+","+c.getHeight());
        saveMonitorSettingsPrefs(props);
    }
    public static void saveAlertPositionAndSize(Component c) {
        Properties props = readPrefs();
        if (c.getWidth() > 200) {
           props.put("WeatherAlerts", c.getX()+","+c.getY()+","+c.getWidth()+","+c.getHeight());
           saveMonitorSettingsPrefs(props);
        }
    }
    
    public  Properties readMetarData(String stateId) {
        return readPropFileFromJar("/weathermonitor/settings/props/"+stateId+"_MetarData.properties");
        //return readPrefs("Maps"+Utilities.SEP+stateId+"_MetarData.properties");
    }
    
    public static Properties readPrefs() {
        return readPrefs("Maps"+Utilities.SEP+"WeatherMonitorSettings.properties");
    }
    public static  boolean getOpenGL() {
        File test = new File(System.getProperty("user.home")+SEP+"WeatherMonitorLauncher.properties");
         if (test.exists() == false) return false;
         Properties props = new Properties() ;
         try{
           FileInputStream is = new FileInputStream(test.getPath());  
           props.load(is);
           is.close();
           if (props.containsKey("OpenGL")) {
               String v = props.getProperty("OpenGL");
               if (v.equals("true")) {
                   return true;
               } else {
                   return false;
               }
              
           } else {
              return false;
           }
          
         } catch (Exception ex) {
            ex.printStackTrace();
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
         return false;
    }
    
    public static void setOpenGL(boolean v) {
        File test = new File(System.getProperty("user.home")+SEP+"WeatherMonitorLauncher.properties");
         if (test.exists() == false) return;
         Properties props = new Properties() ;
         try{
           FileInputStream is = new FileInputStream(test.getPath());  
           props.load(is);
           is.close();
           if (v == false) {
              props.put("OpenGL", "false");
           } else {
               props.put("OpenGL", "true");
           }
           FileOutputStream os = new FileOutputStream(test.getPath());
           props.store(os,"Modified by WeatherMontior");
           os.close();
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
            //System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
         
    }
    public static Properties readRadarSettingsPrefs() {
        return readPrefs("Maps"+Utilities.SEP+"radarSettings.properties");
    }
     
    public static Properties readPrefs(String profFileName) {
        //System.out.println("read prefs");
         String path = Utilities.DIR+Utilities.SEP+profFileName;
         File test = new File(path);
         if (test.exists() == false) {
             System.out.println("No such file:"+path);
             return null;
         }
         Properties props = new Properties() ;
         try{
           FileInputStream is = new FileInputStream(path);  
           props.load(is);
           is.close();
           return props;
           
        } catch (Exception ex) {
            ex.printStackTrace();
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
     }
    
    
    public static void saveMonitorSettingsPrefs(Properties props) {
        savePrefs(props,"Maps"+Utilities.SEP+"WeatherMonitorSettings.properties");
    }
    public static void saveMetarData(Properties props, String stateId) {
        savePrefs(props,"Maps"+Utilities.SEP+stateId+"_MetarData.properties");
    }
    public static void saveRadarSettingsProps(Properties props) {
        savePrefs(props,"Maps"+Utilities.SEP+"radarSettings.properties");
    }
    public static void savePrefs(Properties props, String profFileName) {
        
        System.out.println("Saving prefs:"+Utilities.DIR+Utilities.SEP+profFileName);
        try {
            FileOutputStream os = new FileOutputStream(Utilities.DIR+Utilities.SEP+profFileName);
            props.store(os, "");
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
           // System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
         }
        
    }
    public static double getDistanceBetweenTwoPoints(double X1, double Y1, double X2, double Y2) {
        return Math.sqrt((X2-X1)*(X2-X1) + (Y2-Y1)*(Y2-Y1));
    }
    public static double mmToIn(double value) {
        double inchValue = value * 0.03937008;
        inchValue = Utilities.roundDoubleTwoPlaces(inchValue);
        return inchValue;
    }
    public static double mToMiles(double value) {
        double milesValue = value * 0.0006213712;
        milesValue = Utilities.roundDoubleTwoPlaces(milesValue);
        return milesValue;
    }
    public static int tempCtoF(double tempC) {
        double value = (9.0/5.0 * tempC) + 32.0;
        return (int) (Math.round(value*10.0)/10);
        
    }
    public static double[] isLatLon(String text) {
        double[] retVal = new double[2];
        String[] values = text.trim().split(" ");
        if (values.length == 2) {
            try {
                String v1 = values[0].replaceAll("°N", "");
                String v2 = values[1].replaceAll("°W", "");
                double v11= Double.valueOf(v1);
                double v22 = Double.valueOf(v2)*-1;
                if (v11 >= 20 && v11 <= 40 && v22 <= -80 && v22 >= -150) {
                    retVal[0] = v22; retVal[1] = v11;
                } else  {
                    return null;
                }
                return retVal;
            } catch (Exception ex) {
                
            }
            try {
                double v1= Double.valueOf(values[0]);
                double v2 = Double.valueOf(values[1]);
                if (v1 >= 20 && v1 <= 40 && v2 <= -80 && v2 >= -150) {
                    retVal[0] = v2; retVal[1] = v1;
                } else if (v1 <= -80 && v1 >= -150 && v2 >= 20 && v2 <=40) {
                    retVal[0] = v1; retVal[1] = v2;
                }
                return retVal;
            } catch (Exception ex) {
                return null;
            }
        } else {
            values = text.trim().split(",");
            if (values.length == 2) {
                try {
                double v1= Double.valueOf(values[0]);
                double v2 = Double.valueOf(values[1]);
                if (v1 >= 20 && v1 <= 40 && v2 <= -80 && v2 >= -150) {
                    retVal[0] = v2; retVal[1] = v1;
                } else if (v1 <= -80 && v1 >= -150 && v2 >= 20 && v2 <=40) {
                    retVal[0] = v1; retVal[1] = v2;
                }
                return retVal;
            } catch (Exception ex) {
                return null;
            }
            }
        }
        return null;
    }
    public static void writeStringToFile(String text, String filename) {
        FileWriter os = null;
        try {
            os = new FileWriter( filename);
           
            String[] arr = text.split("\\\\n");
            for (int i = 0; i < arr.length; i++) {
                
                os.write(arr[i] + "\n");
            }
            try {
                os.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
               ex.printStackTrace();
            }
        }
    }
    private Properties readPropFileFromJar(String path) {
        try {
            Properties props = new Properties();

            InputStream in = this.getClass().getResourceAsStream(path);
            props.load(in);
             in.close();
            return props;
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("Error reading "+path +" from JAR");
            return null;
        }

    
    }
    
    public static String readPropFileFromJar(String filename, Component parent) {
        try {
            String str = "";

            InputStream stream = parent.getClass().getResourceAsStream(filename);
            int bufferSize = 1000*1024;
            char[] buffer = new char[bufferSize];
             StringBuilder out = new StringBuilder();
             Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
             for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                  out.append(buffer, 0, numRead);
             }
           
             in.close();
            return out.toString();
           // return str;
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("Error reading "+filename +" from JAR");
            return null;
        }

    }
    
    // this is used to REads the maps from the jar file
   public String readFromJar(String filename) {
       if (this.jsonMapCache.containsKey(filename)) {
           //System.out.println("Got map "+filename+" from cache");
           return jsonMapCache.get(filename);
       }
        long start = new Date().getTime();
       
        try {
            String str = "";

            InputStream stream = getClass().getResourceAsStream(filename);
            int bufferSize = 1000*1024;
            char[] buffer = new char[bufferSize];
             StringBuilder out = new StringBuilder();
             Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
             for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                  out.append(buffer, 0, numRead);
             }
             long end = new Date().getTime();
             long diff = (end-start)/1000;
            // System.out.print("Read "+filename+" took "+diff+" sconds");
             this.jsonMapCache.put(filename,out.toString());
             in.close();
            return out.toString();
           // return str;
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("Error reading "+filename +" from JAR");
            return null;
        }

    }
        
        
        
    
    
    public static String readFile(String filename) {
        
        String text = "";
        try {
            
            text = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException ex) {
            System.out.println("No map for "+filename);
            //System.getLogger(Utilities.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
       
        return text;
}

}
