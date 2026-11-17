/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package weathermonitor.dataproviders;

import weathermonitor.dataobjects.StormReportInfo;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mappingtools.Mapping;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.threads.Timer;

/**
 *
 * @author greg
 */
public class StormReports {
    public static ArrayList<StormReportInfo>  lsrData = new ArrayList<>();
    private   String[] officeIds = {"SJT"};
    private static StormReports me = null;
    
    private final ArrayList<String> decodedStormReports = new ArrayList<>();
     
    public static StormReports getInstance() {
        if (me == null) me = new StormReports();
        return me;
    }
    
    public StormReports() {
  }
   
    public void setOfficeIds(String[] ids) {
    
        this.officeIds = ids;

        lsrData.clear();
        decodedStormReports.clear();
    }
    public String[] getOfficeIds() {
        return officeIds;
    }
//    public static String getNearestActiveReportOffice(long minSeconds) {
//        double dist = Double.MAX_VALUE;
//        String nearestOffice = null;
//        for (int i=0; i<lsrData.size(); i++) {
//            if (lsrData.get(i).getEpoch() > minSeconds) {
//                double distance = Utilities.getDistanceBetweenTwoPoints(Mapping.getInstance().getLon(), 
//                                                                    Mapping.getInstance().getLat(),
//                                                                    lsrData.get(i).getLon(),lsrData.get(i).getLat())*60.0;
//                if (distance < dist) {
//                    dist = distance;
//                    nearestOffice = lsrData.get(i).getOffice();
//                }
//            }
//        }
//        return nearestOffice;
//    }
    public ArrayList<StormReportInfo> getStormReports() {
        //System.out.println("Get storm reports for "+officeIds.length+" offices");
        return getStormReports(officeIds);
    }
    public ArrayList<StormReportInfo> getStormReports(String[] oids) {
        boolean success = false;
      
        if (Timer.getInstance().isInternetUp() == false) {
            return null;
        }
      //  System.out.println("Getting storm reports for "+oids.length +" offices");
        for (int i=0; i<oids.length; i++) {
            String dlink = "https://api.weather.gov/products?office=K"+oids[i]+"&type=LSR&limit=50";
          // System.out.println("Download storm reports:"+dlink);
            String json = Utilities.downloadJsonData(dlink,10000);
            if (Timer.getInstance().isInternetUp() == true && json != null && json.equals("") == false) {
                success = true;
                JSONObject obj = new JSONObject(json);
                JSONArray reports = obj.getJSONArray("@graph");
              //  System.out.println("There are "+reports.length()+" LSRs for "+oids[i]);
                for (int j = 0; j < reports.length(); j++) {
                    String lsrDataLink = reports.getJSONObject(j).get("@id").toString();
                    String[] n = lsrDataLink.split("/");
                    int last = n.length-1;
                    String fname = n[last];
                    if (decodedStormReports.contains(fname) == false) {
                       ArrayList<StormReportInfo> list = decodeLSR(oids[i],downloadLSR(lsrDataLink),fname);
                       lsrData.addAll(list);
                     //  System.out.println("lsrData has size:"+lsrData.size());
                       decodedStormReports.add(fname);
                    } else {
                       
                    }
                }
            } 
        }
//        if (lsrData.size() > 0) {
//            WeatherMonitorRun.viewStormReportsButton.setVisible(true);
//        } else {
//            WeatherMonitorRun.viewStormReportsButton.setVisible(false);
//        }
        //System.out.println("lsrData has size :"+lsrData.size());
        if (success == false) return null;
       
        return lsrData;
    }
    
    private ArrayList<StormReportInfo> decodeLSR(String office,String lsrText,String fname) {
        if (lsrText != null && lsrText.length() > 10) {
            Pattern pat = Pattern.compile("[0,1][0-9][0-9][0-9] [A,P]M     ");
            Matcher m = pat.matcher(lsrText);
//            Pattern endPat = Pattern.compile("&&");
//            Matcher m2 = endPat.matcher(lsrText);
//            if (m2.find()) {System.out.println("Found end pattern && ");}
            if (m.find()) {
                lsrText = lsrText.substring(m.start());
                Pattern endPat = Pattern.compile("&&");
                Matcher m2 = endPat.matcher(lsrText);
                if (m2.find()) {
                    lsrText = lsrText.substring(0,m2.start());
                }
                //System.out.println(lsrText);
                return decodeCleanLSR(office,lsrText.trim(),fname);
            }
        }
        return null;
    }
    private boolean isValidStateAbbr(String value) {
        if (value.length() != 2) return false;
        Pattern pat = Pattern.compile("[A-Z][A-Z]");
        Matcher m = pat.matcher(value);
        if (m.find()) { return true; }
        return false;
    }
    private ArrayList<StormReportInfo> decodeCleanLSR(String office, String text, String fname) {
        ArrayList<StormReportInfo> list = new ArrayList<>();
        Pattern timePat = Pattern.compile("^[0,1][0-9][0-9][0-9] [A,P]M");
        Pattern datePat = Pattern.compile("^[0,1][0-9]/[0-9][0-9]/20[0-9][0-9]");
        String [] parts = text.split("\\\n");
       // System.out.println("There are "+parts.length+" lines in the lsr");
        String rawText = "";
        String time = "";
        String event = "";
        String county = "";
        String mag = "";
        String date = "";
        String lat = "";
        String lon = "";
        String city = "";
        String state = "";
        boolean parsed = false;
        for (int i=0; i<parts.length; i++) {
            //System.out.println("Working on line:"+parts[i]);
            String line = parts[i];
            Matcher timeMatcher = timePat.matcher(line);
            Matcher dateMatcher = datePat.matcher(line);
            if (timeMatcher.find()) {
                if (time.equals("") == false) {
                    StormReportInfo l = parseLsrData(fname,office,date,time,event,mag,county, city,state,lat,lon,rawText);
                    if (l != null) list.add(l);
                    parsed = true;
                }
                rawText = "";
                try {
                 time = line.substring(0,7).trim();
                 event = line.substring(12,28).trim();
                 city = line.substring(29,52).trim();
                 Pattern pat = Pattern.compile(", [A-Z][A-Z]$");
                 Matcher m = pat.matcher(city);
                 if (m.find()) {
                    state = city.substring(m.start()+1).trim();
                 }
                 lat = line.substring(53,58).trim();
                 lon = "-"+line.substring(60).replace("W","").trim();
                } catch (Exception ex) {
                    ex.printStackTrace();
                   System.out.println("BAD LINE:"+line+" is not long enuf");
                }
            } else if (dateMatcher.find()) {
                try {
                 date = line.substring(0,10).trim();
                 mag = line.substring(13,28).trim();
                 county = line.substring(29,47).trim();
                 if (state.equals("")) state = line.substring(48,50);
                 if (isValidStateAbbr(state) == false) {
                     state = "";
                 }
                } catch (Exception ex) {
                    System.out.println("BAD LINE:"+line+" is not long enuf");
                }
            } else if (line.startsWith("          ")) {
                rawText = rawText +"&nbsp;&nbsp;&nbsp;&nbsp;"+ line.trim()+"<br>";
            }
        }
        if (parsed == false) {
             StormReportInfo l = parseLsrData(fname,office,date,time,event,mag,county, city,state,lat,lon,rawText);
             if (l != null) list.add(l);
            // list.add(parseLsrData(         fname,office,date,time,event,mag,county, city,state,lat,lon,rawText));
                   
        }
        return list;
    }
//    public String getNearestActiveReportOffice(long minSeconds) {
//        double dist = Double.MAX_VALUE;
//        String nearestOffice = null;
//        for (int i=0; i<this.lsrData.size(); i++) {
//            if (lsrData.get(i).getEpoch() > minSeconds) {
//                double distance = Utilities.getDistanceBetweenTwoPoints(Mapping.getInstance().getLon(), 
//                                                                    Mapping.getInstance().getLat(),
//                                                                    lsrData.get(i).getLon(),lsrData.get(i).getLat())*60.0;
//                if (distance < dist) {
//                    dist = distance;
//                    nearestOffice = lsrData.get(i).getOffice();
//                }
//            }
//        }
//        return nearestOffice;
//    }
    public void testLsr() {
        Date date = new Date();
         
        long eventTimeSecs = date.getTime()/1000; 
        eventTimeSecs = eventTimeSecs - 5*60*60; 
      
        lsrData.add( new  StormReportInfo("","SJT",0, eventTimeSecs ,"Test City","TX","Hail","2.5In","Test Event",29.6,-99.44));
        lsrData.add( new  StormReportInfo("","SJT",1, eventTimeSecs ,"Test City","TX","Fire","","Test Event",29.8,-99.44));
        lsrData.add( new  StormReportInfo("","SJT",2, eventTimeSecs ,"Test City","TX","Snow","2.5In","Test Event",30.0,-99.44));
        lsrData.add( new  StormReportInfo("","SJT",3, eventTimeSecs ,"Test City","TX","Sleet","2In","Test Event",30.2,-99.44));
        lsrData.add( new  StormReportInfo("","SJT",4, eventTimeSecs ,"Test City","TX","Freezing Rain","0.5in","Test Event",30.4,-99.44));
        lsrData.add( new  StormReportInfo("","SJT",5, eventTimeSecs ,"Test City","TX","Tornado","F5","Test Event",30.6,-99.44));
       
    }
    private StormReportInfo parseLsrData(String fname, String office, String date, String time, String event, String mag, String county, String city, String state, String lat, String lon, String text) {
        //System.out.println("Date="+date+" time="+time+" event="+event+" county="+county+" lat="+lat+" lon="+lon);
        //System.out.println("RawText="+text);
        try {
            double latD = Double.parseDouble(lat);
            double lonD = Double.parseDouble(lon);
            if (Mapping.getInstance().isInMapBounds(latD, lonD) == false) {
                return null;
            }
            String timestamp = date + " " + time;
            ZoneId zoneId = ZoneId.of("America/Chicago");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hhmm a");
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);
            long epoch = localDateTime.atZone(zoneId).toEpochSecond();
            double distance = Utilities.getDistanceBetweenTwoPoints(Mapping.getInstance().getLon(), 
                                                                    Mapping.getInstance().getLat(),
                                                                    lonD,latD)*60.0;
               
            
            return new  StormReportInfo(fname,office,distance,epoch,city,state,event,mag,text,latD,lonD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private String downloadLSR(String link) {
        String path = Utilities.DIR + Utilities.SEP + "LSR";
        String[] n = link.split("/");
        int last = n.length-1;
        String fname = n[last]+".txt";
        File test = new File(path+Utilities.SEP+fname);
        String lsr = null;
        if (test.exists()) {
           // System.out.println("Retrieving "+fname+" from LSR folder");
            lsr = Utilities.readFile(path+Utilities.SEP+fname);
        } else {
            //System.out.println("Downloading "+link);
            String json  = Utilities.downloadJsonData(link,5000);
            if (Timer.getInstance().isInternetUp() == true && json != null && json.equals("") == false) {
                JSONObject obj = new JSONObject(json);
                lsr = obj.getString("productText");
                try {
                    FileWriter writer = new FileWriter(path + Utilities.SEP + fname);
                    writer.write(lsr);
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
           
        }
        return lsr;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String[] officeIds = {"SJT","EWX","MAF","FWD"};
        //StormReports lsr = new StormReports(officeIds);
    }
    
}
