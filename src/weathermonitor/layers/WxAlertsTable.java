package weathermonitor.layers;

import weathermonitor.dataobjects.WarningData;
import utilities.Viewer;
import weathermonitor.listeners.TimerListener;
import weathermonitor.listeners.StormReportListener;
import weathermonitor.layers.StormReportLayer;
import weathermonitor.dataobjects.StormReportInfo;
import utilities.Sounds;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import org.json.JSONArray;
import org.json.JSONObject;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;
import weathermonitor.threads.Timer;
import weathermonitor.threads.WMTimer;
import weathermonitor.WeatherMonitorRun;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class WxAlertsTable extends JPanel implements BboxListener,TimerListener,StormReportListener,ItemListener {
    public static int UL = 0;
    public static int UR = 1;
    public static int LR = 2;
    public static int LL = 3;
    private final boolean soundIsOn = true;
    private boolean playWarningSound = false;
    private boolean playedWarningSound = false;
    public static int ALERTS_BY_LOCATION = 2;
    public static int ALERTS_BY_CWA = 1;
    public int alertType = ALERTS_BY_LOCATION;
    public String CWA = "TAE";
    private int numActiveReports = -1;
    private final int tablePosition = UL;
    private Color tableBgColor = new Color(250,250,250);
    private final Color advisoryTableFgColor = Color.YELLOW;
    //private JLabel[] expireLabels = new JLabel[5];
    private final JLabel[] eventLabels = new JLabel[6];
    //private JLabel[] extrasLabels = new JLabel[5];
    private final Hashtable<String, Color> severityHash = new Hashtable<String, Color>();
    private final Hashtable<String, Color> bgColorHash = new Hashtable<String, Color>();
    private int numRows = 0;
    private static final ArrayList<StormReportInfo> stormReports = new ArrayList<>();
    private  ArrayList<StormReportInfo> activeStormReports =  new ArrayList<>();
    private long minimumStormReportTimeOffsetSecs =  WeatherMonitorRun.STORM_REPORT_OFFSET_SECS[0];
    public static Font tableFont = new Font("Monospaced",Font.BOLD,18);
    //private static Font font = new Font("Monospaced",Font.PLAIN,16);
    private double lat = 26.84;
    private double lon = -98.38;
    //26.84°N 98.38°26.84°N 98.38°
    private ArrayList<WarningData> list;
    private final WMTimer updateTimer;
    public static double id = Math.random();
    private final JPanel[] p = new JPanel[6];
    private static final JPanel contentPanel = new JPanel(new BorderLayout());
    private static final JPanel wwaPanel = new JPanel(new GridLayout(0,1,0,0));
    private int largestWidth = 450;
    private final JLayeredPane container;
    private final WeatherMonitorRun parent;
    private boolean updating = false;
    public static Font titleFont = new Font("Monospaced",Font.BOLD,16);
    public static JLabel titleLabel = new JLabel("");
    private boolean alertTableLoaded = false;
    private int width = 0;
    private int height = 0;
    
    public WxAlertsTable(double lat, double lon, JLayeredPane container,WeatherMonitorRun parent) {
        this.lat = lat;
        this.lon = lon;
        this.container = container;  
        this.parent = parent;
        this.setOpaque(false);
        
        StormReportLayer.getInstance().addStormReportListener(this);
        updateTimer = new WMTimer(1000,"WxAlerts Table", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                update();
            }
        });
        updateTimer.setRepeats(false);
        titleLabel.setText("Your Location");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(titleFont);
        
        contentPanel.setBackground(Color.WHITE);
       
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(wwaPanel, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setLayout(new GridLayout());
        this.add(contentPanel);
          p[0] = new JPanel(new FlowLayout());
          p[0].setOpaque(false);
        //this.add(p[0]);
         p[1] = new JPanel(new FlowLayout());
         p[1].setOpaque(false);
        //this.add(p[1]);
          p[2] = new JPanel(new FlowLayout());
           p[2].setOpaque(false);
       // this.add(p[2]);
          p[3] = new JPanel(new FlowLayout());
           p[3].setOpaque(false);
       // this.add(p[3]);
          p[4] = new JPanel(new FlowLayout());
           p[4].setOpaque(false);
        //Storm Report panel
        p[5] = new JPanel(new FlowLayout());
           p[5].setOpaque(false);
  
        severityHash.put("Severe", new Color(255,255,0));
        severityHash.put("Moderate", new Color(0,100,0));
        severityHash.put("Extreme", new Color(255,0,255));
        severityHash.put("Emergency", new Color(255,0,255));
        severityHash.put("Minor", Color.WHITE);
   
        bgColorHash.put("Flash Flood Warning", new Color(255,0,0));
        bgColorHash.put("Tornado Warning", new Color(255,0,0));
        bgColorHash.put("Severe Thunderstorm Warning", new Color(255,0,0));
        bgColorHash.put("Special Marine Warning", new Color(250,0,0));
        createTheLabels();
        
        //updateTimer.restart();
        this.setVisible(true);
        
        updateTimer.start();
    }
    
    public void setTableBgColor(Color c) {
        this.tableBgColor = c;
        Utilities.getFgColor(c);//gets opposite color
        updateTimer.restart();
        
    }
    
    public int getNumberOfRows() {
        return numRows;
    }
    public void setTableFont(Font f) {
        this.tableFont = f;
        updateTheTable();
    }
//    public void setTablePosition(int position) {
//        if (container == null) return;
//        tablePosition = position;
//        System.out.println("Set table position "+position);
//        int height = 33*numRows;
//        if  (position == UL) {
//            System.out.println("set location 0 0");
//            this.setLocation(0,0);
//        } else if (position == UR) {
//            int x = container.getWidth()-largestWidth-2;
//            System.out.println("set location "+x+" 0");
//            this.setLocation(x,0);
//        } else if (position == LR) {
//            int x = container.getWidth()-largestWidth-2;
//            int y = container.getHeight()-height;
//            System.out.println("set location "+x+" "+y);
//            this.setLocation(x,y);
//        } else if (position == LL) {
//            int y = container.getHeight()-height;
//            System.out.println("set location 0 "+y);
//            this.setLocation(0,y);
//        }
//        this.repaint();
//    }
    private void setTableSize(int numRows, int width, int numReports) {
        if (numActiveReports == -1) {
            System.out.println("numActivbeReports == -1 so set it to  "+numReports);
            this.numActiveReports = numReports;
        }
        
        Dimension d = wwaPanel.getSize();
        if (playWarningSound == true && playedWarningSound == false) {
            playedWarningSound = true;
            System.out.println("Play thunder in 1");
            if  (soundIsOn == true) Sounds.getInstance().playSound(Sounds.THUNDER);
            
        } else if (numRows > this.numRows) {
            System.out.println("wxAlertTable rows increasing with playWarningSound="+playWarningSound);
          
            if (playWarningSound == false && numActiveReports == 0 && numReports > 0) {

                System.out.println("wxAlertTable storm report count increasing play NEW sound");
                if (soundIsOn == true) {
                    Sounds.getInstance().playSound(Sounds.NEW);
                }

                numActiveReports = numReports;
            } else if (playWarningSound && playedWarningSound == false) {
                playedWarningSound = true;
                System.out.println("wxAlertTable playWarningSound is true and playedWarningSound is false");
                if  (soundIsOn == true)Sounds.getInstance().playSound(Sounds.THUNDER);
            } else {
                System.out.println("wxAlertsTable no new warnings and no new LSR but got some new product so play NEW sound");
                if  (soundIsOn == true)Sounds.getInstance().playSound(Sounds.NEW); 
            }
        } else if ( numRows < this.numRows) {
           System.out.println("wxAlertTable rows decreasing play EXPIRE sound");
           if  (soundIsOn == true) Sounds.getInstance().playSound(Sounds.EXPIRE);
        } else {
            System.out.println("wxAlertTable rows stayed the same check if storm reports changed");
            if (numReports > this.numActiveReports) {
                System.out.println("   numActiveReports ="+numActiveReports+" < "+numReports+" play NEW");
                
                if  (soundIsOn == true)Sounds.getInstance().playSound(Sounds.NEW);
            } 
            this.numActiveReports = numReports;
        }
        this.numRows = numRows;
        int h = 33*numRows;
        //Dimension dd = 20;//titleLabel.getPreferredSize();
        //titleLabel.setText("XXXXX");
        wwaPanel.setSize(width+10,h);
        this.setSize(width+10,h+20);
        
      
        
        if  (container == null || tablePosition == UL ) {
           // Dimension dd = titleLabel.getPreferredSize();
            this.setLocation(0,0);
        } else if (tablePosition == UR) {
            this.setLocation(container.getWidth()-largestWidth-2,0);
        } else if (tablePosition == LR) {
            this.setLocation(container.getWidth()-largestWidth-2,container.getHeight()-height);
        } else if (tablePosition == LL) {
            this.setLocation(0,container.getHeight()-height);
        }
//        this.setMaximumSize(new Dimension(450,height));
//        this.setPreferredSize(new Dimension(450,height));
    }
    
    public void setTableLocation(int pos) {
        if (pos == UL) {
            
        } else if (pos == UR) {
            
        } else if (pos == LR) {
            
        } else if (pos == LL) {
        }
    }
   
    private void clearTable() {
        wwaPanel.removeAll();
        for (int i=0; i<6; i++) {
            p[i].removeAll();
            if (eventLabels[i] != null) {
            this.eventLabels[i].setText("");
            this.eventLabels[i].setOpaque(false);
            }

        }
    }
    
   
    public void showWeatherAlerts(double lat, double lon) {
        clearTable();
       
        this.lat = lat;
        this.lon = lon;
        updateTimer.restart();
       
    }
    public void updateTheTable() {
        updateTimer.restart();
    }
    private void update() {
        Thread t = new Thread() {
            public void run() {
                int timeout = 0;
                while (updating == true && timeout < 60) {
                    try {
                        System.out.println("sleeping");
                        sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    timeout++;
                }
                System.out.println("Update wx alerts table");
                updating = true;
                try {
                    if (alertType == ALERTS_BY_CWA) {
                        list = getAllWeatherAlerts(CWA);
                    } else if (alertType == ALERTS_BY_LOCATION) {
                        list = getWeatherAlertsAt(lat, lon);
                    }
                    
                    if (list != null && list.size() > 0) {

                        System.out.println("Weather alert updated for lat/lon  " + lat + " " + lon + " with " + list.size() + " datas");
                        populateTable(list, stormReports);
                        setVisible(true);
                    } else if (stormReports != null && stormReports.size() > 0) {
                        populateTable(list, stormReports);
                        setVisible(true);
                    } else {
                        clearTable();
                        setVisible(false);
                        //System.out.println("The list is null in WxAlerts");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                alertTableLoaded = true;
                updating = false;
            }

        };
        t.start();
        
       // allWeatherAlerts = getAllWeatherAlerts();
       // System.out.println("all weather alerts has size "+allWeatherAlerts.size());
    }
    
    private void viewAlertData(int index) {
        if (index == 5) {// storm report data
           
            Viewer.getInstance().viewAllStormReports(stormReports, (int)minimumStormReportTimeOffsetSecs/60/60, parent);
        } else {
            if (list.size() < index) {
                return;
            }
            if (list != null) {
                try {
                    String text = list.get(index).getText();
                    String title = list.get(index).getEvent() + " Until " + list.get(index).getExpires();
                    String maxWindGust = list.get(index).getMaxWindGust();
                    String maxHailSize = list.get(index).getMaxHailSize();
                    Viewer v = Viewer.getInstance();
                    //v.showViewer(title,text, this, mediumFont);
                    v.viewWWA(title, text, maxHailSize, maxWindGust, parent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void setTheLocation(double lat, double lon) {
        this.lat= lat;
        this.lon = lon;
        updateTimer.restart();
    }
    public static boolean isSevereEvent(String event) {
        if (event.trim().toLowerCase().equals("severe thunderstorm warning")) {
            return true;
        }
        if (event.trim().toLowerCase().equals("flash flood warning")) {
            return true;
        }
        if (event.trim().toLowerCase().equals("tornado warning")) {
            return true;
        }
        if (event.trim().toLowerCase().equals("special marine warning")) {
            return true;
        }
         if (event.trim().toLowerCase().equals("special weather statement")) {
            return true;
        }
          
        return false;
    }
    
    public static ArrayList<WarningData> getWeatherAlertsAt(double theLat, double theLon) {
        String dlink = "https://api.weather.gov/alerts?point=" + Double.toString(theLat) + "," + Double.toString(theLon) + "&active=true";
       // System.out.println("Download WxAlerts:"+dlink);
        return getWeatherAlerts(dlink, null);
    }
    public  ArrayList<WarningData> getAllWeatherAlerts(String oid) {
        String dlink = "https://api.weather.gov/alerts?&active=true";
        return getWeatherAlerts(dlink,CWA);
    }
    private static ArrayList<WarningData> getWeatherAlerts(String dlink) {
        return getWeatherAlerts(dlink, null);
    }
    private static ArrayList<WarningData> getWeatherAlerts(String dlink,String oid) {
        String xml = "";
        String text = "";String json  = "";
        ArrayList<WarningData> list = new ArrayList<WarningData>();
        try {
            
            URL url = new URL(dlink);
            url.openConnection().setReadTimeout(5000);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(url.openStream()));
        
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            json = json + inputLine;
            
        }
        in.close();
       // System.out.println("There are "+lines+" lines of data for "+dlink);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }
        JSONObject obj = new JSONObject(json);
        JSONArray features = obj.getJSONArray("features");
        for (int i = 0; i < features.length(); i++) {
            String officeId = "";
            try {
                JSONArray awipsId = features.getJSONObject(i).getJSONObject("properties").getJSONObject("parameters").getJSONArray("AWIPSidentifier");
                 officeId = awipsId.getString(0).substring(3);
                //System.out.println("Office id for product is "+officeId);
                } catch (Exception ex) {ex.printStackTrace();}
            if (oid == null || officeId.equals(oid)) {
            
            
            
            String event = "";
            WarningData data = new WarningData();
            GeneralPath path = new GeneralPath();
            String type = "";
            try {
                event = features.getJSONObject(i).getJSONObject("properties").getString("event");
                data.setEvent(event);
            } catch (Exception ex) {ex.printStackTrace();}
            try {

                String severity = features.getJSONObject(i).getJSONObject("properties").getString("severity");
                //System.out.println("Severity=" + severity);
                data.setSeverity(severity);
            } catch (Exception ex) {ex.printStackTrace();}
            try {
                if (isSevereEvent(event)) {
                    //System.out.println("is a severe event "+event);
                     try {
                           String maxHailSize = features.getJSONObject(i).getJSONObject("properties").getJSONObject("parameters").getJSONArray("maxHailSize").get(0).toString();
                           data.setMaxHailSize(maxHailSize);
                          // System.out.println("Got maxHailSize="+maxHailSize);
                    } catch (Exception ex) {}
                      try {
                           String maxWindGust = features.getJSONObject(i).getJSONObject("properties").getJSONObject("parameters").getJSONArray("maxWindGust").get(0).toString();
                           // System.out.println("Got maxWindGust="+maxWindGust);
                           data.setMaxWindGust(maxWindGust);
                    } catch (Exception ex) {}

                }
            } catch (Exception ex) {
               
            }
                try {
                String headline = features.getJSONObject(i).getJSONObject("properties").getString("headline");
                data.setHeadline(headline);
                } catch (Exception ex) {}
                
                try {
                String descr = features.getJSONObject(i).getJSONObject("properties").getString("description");
                data.setText(descr);
                } catch (Exception ex) {ex.printStackTrace();}
                
                
                try {
                    String expires = features.getJSONObject(i).getJSONObject("properties").getString("expires");
                    //2026-09-08T17:30:00-05:00
                    expires = expires.substring(0, 16).replace("T", " ");
                    // System.out.println(time);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("hh:mma");
                    LocalDateTime localDateTime = LocalDateTime.parse(expires, formatter);
                    expires = localDateTime.format(outputFormat);
                    data.setExpires(expires);
                } catch (Exception ex) {ex.printStackTrace();}
                
            list.add(data);
        }
        }
        return sortWarningAlertsBySeverity(list);
        
    }
    
   
    private static ArrayList<WarningData> sortWarningAlertsBySeverity(ArrayList<WarningData> list) {
        String added = "";
        if (list == null) {
            return null;
        }

        ArrayList<WarningData> retList = new ArrayList<WarningData>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().equals("Tornado Warning")) {
                if (added.contains("Tornado Warning")) {
                    for (int j = 0; j < retList.size(); j++) {
                        String ev = retList.get(j).getEvent();
                        if (ev.equals("Tornado Warning")) {
                            retList.remove(j);
                            break;
                        }
                    }
                }
                retList.add(list.get(i));
                list.set(i, null);
                added = added + ",Tornado Warning";
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().equals("Severe Thunderstorm Warning")) {
                if (added.contains("Severe Thunderstorm Warning")) {
                    for (int j = 0; j < retList.size(); j++) {
                        String ev = retList.get(j).getEvent();
                        if (ev.equals("Severe Thunderstorm Warning")) {
                            retList.remove(j);
                            break;
                        }
                    }
                }
                retList.add(list.get(i));
                list.set(i, null);
                added = added + ",Severe Thunderstorm Warning";
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().contains("Flash Flood Warning")) {
                if (added.contains("Flash Flood Warning")) {
                    for (int j = 0; j < retList.size(); j++) {
                        String ev = retList.get(j).getEvent();
                        if (ev.equals("Flash Flood Warning")) {
                            retList.remove(j);
                            break;
                        }
                    }
                }
                retList.add(list.get(i));
                list.set(i, null);
                added = added + ",Flash Flood Warning";
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().contains("Flood Warning")) {
                if (added.contains("Flood Warning")) {
                    for (int j = 0; j < retList.size(); j++) {
                        String ev = retList.get(j).getEvent();
                        if (ev.equals("Flood Warning")) {
                            retList.remove(j);
                            break;
                        }
                    }
                }
                retList.add(list.get(i));
                list.set(i, null);
                added = added + ",Flood Warning";
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().contains("Watch")) {
                if (added.contains(list.get(i).getEvent()) == false) {
                    retList.add(list.get(i));
                    
                    added = added +","+list.get(i).getEvent();
                    list.set(i, null);
                }
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).getEvent().contains("Flood Advisory")) {
                if (added.contains("Flood Advisory")) {
                    for (int j = 0; j < retList.size(); j++) {
                        String ev = retList.get(j).getEvent();
                        if (ev.equals("Flood Advisory")) {
                            retList.remove(j);
                            break;
                        }
                    }
                }
                retList.add(list.get(i));
                list.set(i, null);
                added = added + ",Flood Advisory";
            }
        }
        
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null) {
                 if (added.contains(list.get(i).getEvent()) == false) {
                    retList.add(list.get(i));
                    
                    added = added +","+list.get(i).getEvent();
                    list.set(i, null);
                }
                
            }
        }
        
        return retList;
    }
    
    private String shortenEvent(String event) {
        String ret = "";
        ret = event.replaceAll("Severe Thunderstorm Warning", "Severe Thunderstorm").
        replaceAll("Tornado Warning","TORNADO WARNING").
        replaceAll("Special Weather Statement","Weather Alert").
        replaceAll("Special Marine Warning","Severe Thunderstorm");
       
        
        return ret;
    }
    
    private void createTheLabels() {
         for (int i=0; i<6; i++) {
            final int index = i;
            p[i].setBorder(BorderFactory.createLineBorder(Color.black, 1));
            p[i].setLayout(new GridLayout(1,1));
            eventLabels[i] = new JLabel("");
            eventLabels[i].setOpaque(false);
            eventLabels[i].setBackground(tableBgColor);
            if (WeatherMonitorRun.enabledTooltips) eventLabels[i].setToolTipText("Click For Details");
            //eventLabels[i].setBorder(BorderFactory.createLineBorder(Color.black, 1));
            eventLabels[i].setForeground(advisoryTableFgColor);
            eventLabels[i].setHorizontalTextPosition(JLabel.CENTER);
            eventLabels[i].setHorizontalAlignment(JLabel.CENTER);
            eventLabels[i].addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent me) {viewAlertData(index);}
                @Override
                public void mousePressed(MouseEvent me) {}
                @Override
                public void mouseReleased(MouseEvent me) {}
                @Override
                public void mouseEntered(MouseEvent me) {}
                @Override
                public void mouseExited(MouseEvent me) {}
            });
        }
    }
    private int getActiveStormReportsForNearestOffice(ArrayList<StormReportInfo> list) {
        int num = 0;
        long curSecs = new Date().getTime()/1000;
        long minTime = curSecs - minimumStormReportTimeOffsetSecs;
       // String oid = StormReports.getNearestActiveReportOffice(minTime);
        String oid = Mapping.cwa;
        System.out.println("getActiveStormReportsForNearestOffice :"+oid);
        for (int i=0; i<list.size(); i++) {
            if (list.get(i).getOffice().equals(oid)) {
                num++;
            }
        }
        return num;
    }
    public void  populateTable(ArrayList<WarningData> list,ArrayList<StormReportInfo> lsrData) {
         int numRows = 0;
         boolean activeWarning = false;
         clearTable();
         largestWidth = 0;
        titleLabel.setText(Mapping.getInstance().getNearestCity());
        System.out.println("Neareset city="+Mapping.getInstance().getNearestCity());
        if (list != null) {
            numRows = list.size();
            if (numRows > 5) {
                numRows = 5;
            }

            for (int i = 0; i < numRows; i++) {

                String event = list.get(i).getEvent();
                if (event.contains("Warning")) {
                    p[i].setBackground(Color.RED);
                    activeWarning = true;
                    eventLabels[i].setForeground(Color.WHITE);
                    eventLabels[i].setFont(tableFont);
                } else if (event.contains("Watch")) {
                    p[i].setBackground(Color.YELLOW);
                    eventLabels[i].setForeground(Color.BLACK);
                    eventLabels[i].setFont(tableFont);
                } else {
                    p[i].setBackground(tableBgColor);
                    eventLabels[i].setForeground(advisoryTableFgColor);
                    //Font f = new Font(tableFont.getFamily(),Font.PLAIN,tableFont.getSize()-2);
                    eventLabels[i].setFont(tableFont);
                }

                String theText = list.get(i).getExpires() + " ";
                theText = theText + shortenEvent(event);
                String extraText = "";
                String hailSize = list.get(i).getMaxHailSize();
                String maxWind = list.get(i).getMaxWindGust();
                if (hailSize.equals("") == false && hailSize.equals("0.00") == false) {
                    extraText = hailSize + "In Hail ";
                }
                if (maxWind.equals("") == false) {
                    extraText = extraText + maxWind + " Wind";
                }
                theText = theText + " " + extraText;
                System.out.println(" tex=" + theText);
                eventLabels[i].setText(theText);
                Dimension d = eventLabels[i].getPreferredSize();
                if (d.width > largestWidth) {
                    largestWidth = d.width;
                }
                p[i].setOpaque(true);

                p[i].add(eventLabels[i]);
                wwaPanel.add(p[i]);
                
            }
            
        } else {
            activeWarning = false;
        }
        if (activeWarning == false) {
            playWarningSound = false;
            playedWarningSound = false;
        } else {
            if (playedWarningSound == false) {
                playWarningSound = true;
            } else {
                playWarningSound = false;
            }
        }
        int numLsrs = 0;
        activeStormReports.clear();
        activeStormReports = new ArrayList<>();
        long curTime = new Date().getTime()/1000;
        long minTime = curTime - minimumStormReportTimeOffsetSecs;
        long hours = minimumStormReportTimeOffsetSecs/60/60;
        String hrsString = "";
        if (hours <= 72) {
            hrsString = "Last "+Long.toString(hours)+"Hrs ";
        } else {
            hrsString = "";
        }
        String states = "(";
        for (int i=0; i<lsrData.size(); i++) {
            long eventTime = lsrData.get(i).getEpoch();
            if (eventTime >= minTime) {
                if (isInMapBounds(lsrData.get(i).getLat(), lsrData.get(i).getLon())) {
                    activeStormReports.add(lsrData.get(i));
                    if (states.contains(lsrData.get(i).getState()) == false) {
                        if (lsrData.get(i).getState().equals("") == false) {
                            states = states + lsrData.get(i).getState() + ",";
                        }
                    }
                }
            }
            
        }
        if (states.length() > 2) {
            int end = states.length() -1 ;
            states = states.substring(0,end)+")";
        } else {
            states = "";
        }
        //int numActiveStormReports = activeStormReports.size();
        int numActiveStormReports = getActiveStormReportsForNearestOffice(activeStormReports);
        System.out.println("There are "+activeStormReports.size()+" acive storm reports on the map and "+numActiveStormReports+" from the nearest office");
        if (activeStormReports.size() > 0) {
             p[5].setBackground(Color.WHITE);
             eventLabels[5].setForeground(Color.BLACK);
             eventLabels[5].setFont(tableFont);  
             if (WeatherMonitorRun.enabledTooltips) eventLabels[5].setToolTipText(activeStormReports.size()+" storm reports on the map click to read all available reports");
             eventLabels[5].setText(activeStormReports.size()+" Storm Reports-"+hrsString+states);
             int stormReportLabelWidth = eventLabels[5].getPreferredSize().width;
             if (largestWidth == 0) {
                 largestWidth = eventLabels[5].getPreferredSize().width;
             } else if (stormReportLabelWidth > largestWidth) {
                 largestWidth = stormReportLabelWidth;
             }
             p[5].setOpaque(true);
             p[5].add(eventLabels[5]);
             wwaPanel.add(p[5]);
             numRows = numRows + 1;
        }
        
        setTableSize(numRows, largestWidth, numActiveStormReports);
        revalidate();
        repaint();
    }
     private boolean isInMapBounds(double lat , double lon) {
          return Mapping.getInstance().isInMapBounds(lat,lon);
     }
     @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        if (this.width != w || this.height != h) {
            this.width = w;
            this.height = h;
            playedWarningSound = false;
        }
        
        this.container.setSize(w,h);
        this.numActiveReports = -1;
        setTheLocation(lat,lon);
        
    }
    
//    @Override
//    public void locationChanged(double lat, double lon, int zoomLevel) {
//        // This is used by WxAlerts only to update the table
//        setTheLocation(lat,lon);
//    }

    @Override
    public double getID() {
        return id;
    }

    @Override
    public void runTask() {
        
        updateTimer.restart();
    }

    @Override
    public void setCountdownValue(int number) {
        
    }

    @Override
    public void stormReportsUpdated(ArrayList<StormReportInfo> list, long minTime) {
        stormReports.clear();
        stormReports.addAll(list);
        //this.stormReports = list;
        this.minimumStormReportTimeOffsetSecs = minTime;
       // System.out.println("STorm reports updated in WxAlertsTable with mintim="+minTime+ " size:"+list.size());
        updateTimer.restart();
    }
    
     @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
           System.out.println("Storm Report hours combo changed in wxAlertsTable");
            JComboBox cb = (JComboBox) e.getSource();
            int index = cb.getSelectedIndex();
          //  System.out.println("selected index="+index);
            this.minimumStormReportTimeOffsetSecs = WeatherMonitorRun.STORM_REPORT_OFFSET_SECS[index];
            //System.out.println("numHrs="+numStormReportHours);
            if (alertTableLoaded) update();
        }
    }
}
