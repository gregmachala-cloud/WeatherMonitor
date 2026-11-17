/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package utilities;

import weathermonitor.layers.MetarLayer;
import weathermonitor.listeners.MetarHistoryListener;
import weathermonitor.dataobjects.Metar;
import utilities.FontChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import weathermonitor.dataobjects.MetarStationInfo;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author greg
 */
public class MetarViewer extends javax.swing.JFrame implements MetarHistoryListener {
    private static MetarViewer viewer = null;
    //private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MetarViewer.class.getName());
    private final Font font = new Font("SansSerif",Font.BOLD,18);
    private final JEditorPane jep = null;
    private int row = 0;
    //private Hashtable<String, JTable> tableHash = new Hashtable<>();
    private JTable activeMetarTable;
    private Metar latestMetar;
    private int todaysHigh;
    private int todaysLow;
    private String todaysDate;
    private int yesterdaysHigh;
    private int yesterdaysLow;
    private int todaysMaxWind;
    private int yesterdaysMaxWind;
    private boolean yesterday;
    private boolean today;
    private String yesterdaysDate;
    //private javax.swing.Timer timer ;
    public static MetarViewer getInstance() {
       if (viewer == null) viewer = new MetarViewer();
       return viewer;
   }
    /**
     * Creates new form Viewer
     */
    private MetarViewer() {
        initComponents();
        this.setBackground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        //metarTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(6).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(7).setCellRenderer( centerRenderer );
         metarTable.getColumnModel().getColumn(8).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(9).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(10).setCellRenderer( centerRenderer );
        metarTable.getColumnModel().getColumn(11).setCellRenderer( centerRenderer );
       
        metarTable.getColumnModel().getColumn(1).setCellRenderer( new ImageRenderer());
        metarTable.getColumnModel().getColumn(0).setPreferredWidth(175);
        metarTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        
        metarTable.getTableHeader().setFont(font);
        readProps();
//        timer = new javax.swing.Timer(60000, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                update();
//            }
//        });
//        timer.setRepeats(true);
       
    }
    
//    private void setupMetarDataView(MetarStationInfo msi) {
//        if  (msi == null) {
//            System.out.println("The metar data is null");
//            return;
//        }
//        text = "";
//        tabs.removeAll();
//        //for (int i=0; i<list.size();i++) {
//             jep = new JEditorPane();
//             jep.setEditable(false);
//            jep.setFont(font);
//            JScrollPane sp = new JScrollPane();
//            JPanel p  = new JPanel();
//            p.setBackground(new java.awt.Color(255, 255, 255));
//            p.setLayout(new java.awt.GridLayout(1, 0));
//            jep.setContentType("text/html"); 
//            jep.setText(getInitialHtmlText());
//            sp.setViewportView(jep);
//            p.add(sp);
//            tabs.add(p);
//            tabs.setTitleAt(0, msi.getStationId());
//            tabs.setToolTipTextAt(0, msi.getStationName());
//            
//        //}
//    }
    
    private void readProps() {
        try {
        Properties props = Utilities.readPrefs();
        if (props == null) return;
        if (props.containsKey("ViewerFont")) {
            String[] arr = props.getProperty("ViewerFont").split(",");
            int bold = Integer.valueOf(arr[1]);
            if (bold == 1) {
                setViewerFont( new Font(arr[0],Font.BOLD,Integer.valueOf(arr[2])));
            } else {
                setViewerFont( new Font(arr[0],Font.PLAIN,Integer.valueOf(arr[2])));
            }
           
        }
        } catch (Exception ex) {ex.printStackTrace();}
    }
    public void updateData(String id, JSONArray features) {
        ArrayList<Metar> list = new ArrayList<>();

                      for (int j = 0; j < features.length(); j++) {

                          String name = features.getJSONObject(j).getJSONObject("properties").getString("stationName");
                          String temp = features.getJSONObject(j).getJSONObject("properties").getJSONObject("temperature").get("value").toString();
                          String dwpt = features.getJSONObject(j).getJSONObject("properties").getJSONObject("dewpoint").get("value").toString();
                          String wspd = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windSpeed").get("value").toString();
                          String wgst = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windGust").get("value").toString();
                          String wdir = features.getJSONObject(j).getJSONObject("properties").getJSONObject("windDirection").get("value").toString();
                          String oneHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLastHour").get("value").toString();
                          String threeHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLast3Hours").get("value").toString();
                          String sixHrPcpn = features.getJSONObject(j).getJSONObject("properties").getJSONObject("precipitationLast6Hours").get("value").toString();
                          String vsby = features.getJSONObject(j).getJSONObject("properties").getJSONObject("visibility").get("value").toString();
                               
                          JSONArray wx = features.getJSONObject(j).getJSONObject("properties").getJSONArray("presentWeather");
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
                          JSONArray sky = null; 
                           try {
                                 sky = features.getJSONObject(j).getJSONObject("properties").getJSONArray("cloudLayers");
                            } catch (Exception ex) {}
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
                            
                          String skyString = MetarLayer.getSkyIcon(sky);// bkn.png etc
                          String weatherString = "";
                          for (int i = 0; i < wx.length(); i++) {
                              weatherString = weatherString + wx.getJSONObject(i).getString("rawString");
                          }
                          Metar metar = new Metar(id, name, skyString, tempF, dwptF, windDir, windSpeed, windGust, weatherString,"", 9999, 9999,oneHrPrecip, threeHrPrecip, sixHrPrecip, viz, epochSeconds, updatedTime);
                          //updateTable(metar);
                          list.add(metar);
                      }
                      for (int i = list.size()-1; i >= 0; i--) {

                          updateTable(list.get(i));
                      }
        
    }
//    private void update() {
//        if (latestMetar == null) return;
//        System.out.println("update the table");
//        downloadLatestMetar(latestMetar.getId());
//    }
//    public void setVisible(boolean value) {
//        if (value == false) {
//            timer.stop();
//        } else {
//            timer.restart();
//        }
//        super.setVisible(value);
//    }
    private void updateTable(Metar metar) {
        if (metar != null && metar.getTime() > latestMetar.getTime()) {
             latestMetar = metar;
            System.out.println("Updating metar viewer for "+metar.getId());
          
            for (int i=metarTable.getRowCount()-2; i>=0; i--) {
                for (int j=0; j<metarTable.getColumnCount(); j++) {
                    
                    Object v = metarTable.getValueAt(i,j);
                    metarTable.setValueAt(v, i+1,j);
                }
                
            }
            metarTable.setValueAt(metar.getTimeString(), 0, 0);
            metarTable.setValueAt(metar.getSkyIcon(), 0, 1);
            metarTable.setValueAt(metar.getTempString(), 0, 2);
            metarTable.setValueAt(metar.getDwptString(), 0, 3);
            metarTable.setValueAt(metar.getWindDirString(), 0, 4);
            metarTable.setValueAt(metar.getWindSpeedString(), 0, 5);
            metarTable.setValueAt(metar.getWindGustString(), 0, 6);
            metarTable.setValueAt(metar.getWeatherString(), 0, 7);
            metarTable.setValueAt(metar.getWeatherString(), 0, 8);
            metarTable.setValueAt(metar.getWeatherString(), 0, 9);
            metarTable.setValueAt(metar.getWeatherString(), 0, 10);
            metarTable.setValueAt(metar.getWeatherString(), 0, 11);
            metarTable.repaint();
        } 
    }
      public void downloadLatestMetar(final String stationId) {
       Thread t = new Thread() {
           public void run() {
                  long latestTime=latestMetar.getTime();
                  String id = stationId;
                  String dlink = "https://api.weather.gov/stations/" + id + "/observations/?limit=5";
                  System.out.println("download:" + dlink);
                  String json = Utilities.downloadJsonData(dlink,5000);
                  if (json != null && json.equals("") == false) {

                      JSONObject obj = new JSONObject(json);
                      JSONArray features = obj.getJSONArray("features");
                      updateData(id , features);
                    
                      
                  }
              }

          };
       t.start();
   }
   
    public void setViewerFont(Font f) {
       
        metarTable.setFont(font);
        
    }
    private void clearTable(JTable t) {
        for (int i=0; i<t.getRowCount(); i++) {
            for (int j=0; j<t.getColumnCount(); j++) {
                if (j == 1) {
                    t.setValueAt(null,i,j);
                } else {
                    t.setValueAt("", i, j);
                }
            }
        }
        t.repaint();
    }
    public void centerViewer(String title, Color bgColor, Component parent) {
        //text = "<html><b><font color=\"red\">"+title+" "+maxHailSize+" "+maxWindGust+"</font><b><br><br>"+text.replaceAll("\\n","<br>")+"</html>";
        row = 0;
//        if  (this.appendModeCb.isSelected()) {
//            activeMetarTable = addTab();
//            int last = tabs.getTabCount()-1;
//            tabs.setSelectedIndex(last);
//        } else {  
            clearTable(metarTable);
            metarTable.setBackground(bgColor);
            metarTable.repaint();
            while (tabs.getTabCount() > 1) {
                int last = tabs.getTabCount()-1;
                tabs.remove(last);
            }
            this.titleLabel.setText(title);
            //setTitle(title);
            tabs.setTitleAt(0,"Loading");
            //tabs.setToolTipTextAt(0, msi.getSiteName());
            activeMetarTable = metarTable;
       // }
        if (this.isVisible() == false) {
            int w = 1050;
            int h = 550;
            this.setSize(w, h);
            int x = (parent.getWidth() - w) / 2 + parent.getX();
            int y = (parent.getHeight() - h) / 2 + parent.getY();
            this.setLocation(x, y);
        }
         java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        highTodayLabel = new javax.swing.JLabel();
        lowTodayLabel = new javax.swing.JLabel();
        todaysMaxWindLabel = new javax.swing.JLabel();
        highYesterdayLabel = new javax.swing.JLabel();
        lowYesterdayLabel = new javax.swing.JLabel();
        yesterdaysMaxWindLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        tabs = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        metarTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("24Hr Observation Viewer");

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 3, 24)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 51, 255));
        jPanel5.add(titleLabel);

        jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        highTodayLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        jPanel2.add(highTodayLabel);

        lowTodayLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        jPanel2.add(lowTodayLabel);

        todaysMaxWindLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        todaysMaxWindLabel.setToolTipText("");
        jPanel2.add(todaysMaxWindLabel);

        highYesterdayLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        jPanel2.add(highYesterdayLabel);

        lowYesterdayLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        jPanel2.add(lowYesterdayLabel);

        yesterdaysMaxWindLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        yesterdaysMaxWindLabel.setToolTipText("");
        jPanel2.add(yesterdaysMaxWindLabel);

        jPanel4.add(jPanel2, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.BorderLayout());

        tabs.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tabs.setFont(new java.awt.Font("DejaVu Sans", 1, 20)); // NOI18N

        metarTable.setBackground(new java.awt.Color(255, 245, 224));
        metarTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Time", "Sky", "Temp", "Dwpt", "Dir", "Speed", "Gust", "Weather", "Vsby", "1Hr Pcpn", "3Hr Pcpn", "6Hr Pcpn"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        metarTable.setGridColor(new java.awt.Color(102, 102, 102));
        metarTable.setRowHeight(30);
        metarTable.setShowHorizontalLines(true);
        jScrollPane1.setViewportView(metarTable);

        tabs.addTab("tab1", jScrollPane1);

        jPanel3.add(tabs, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton2.setText("Font");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, java.awt.BorderLayout.WEST);

        jButton1.setText("Close");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       dispose();
      
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FontChooser.getInstance().showDialog(this,font);
    }//GEN-LAST:event_jButton2ActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel highTodayLabel;
    private javax.swing.JLabel highYesterdayLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lowTodayLabel;
    private javax.swing.JLabel lowYesterdayLabel;
    private javax.swing.JTable metarTable;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel todaysMaxWindLabel;
    private javax.swing.JLabel yesterdaysMaxWindLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addedMetar(Metar metar) {
        //System.out.print("Wx="+metar.getWeatherString());
        if (metar == null && row == 0) {
            int last = tabs.getTabCount()-1;
            tabs.setTitleAt(last, "No Data");
            tabs.setToolTipTextAt(last,"");
        } else {
        if (row == 0) {
            latestMetar = metar;
            jScrollPane1.getVerticalScrollBar().setValue(0);
            highTodayLabel.setText("");
            highYesterdayLabel.setText("");
            lowYesterdayLabel.setText("");
            lowTodayLabel.setText("");
            int last = tabs.getTabCount()-1;
            tabs.setTitleAt(last, metar.getId());
            tabs.setToolTipTextAt(last,metar.getSiteName());
            todaysHigh = metar.getTemp();
            todaysLow = metar.getTemp();
            todaysMaxWind = 0;
            todaysDate = metar.getTimeString().substring(8,10);
            yesterdaysDate = todaysDate;
            yesterdaysHigh = -9999;
            yesterdaysLow = 9999;
            yesterdaysMaxWind = 0;
            yesterday = false;
            today = true;
        } 
        //timer.restart();
        int temp = metar.getTemp();
        int windSpeed = metar.getWindSpeedInt();
        int windGust = metar.getWindGustInt();
        if (windGust > windSpeed) {
            windSpeed = windGust;
        }
        String testDate = metar.getTimeString().substring(8,10);
        
       // System.out.println("testData = "+testDate);
        if (testDate.equals(todaysDate) == false) {
            yesterday = true;
            today = false;
            yesterdaysDate = testDate;
        } else if (testDate.equals(yesterdaysDate) == false) {
          
            yesterday = false;
            today = false;
        }
       
        if (temp != 9999 && today == true && yesterday == false && temp >= todaysHigh) {
            todaysHigh = temp; 
            highTodayLabel.setText("<html>Today: <font color=red>High "+temp+"F</font></html>");
        } else if (temp != 9999 && today == false && yesterday == true && temp > yesterdaysHigh) {
            yesterdaysHigh = temp;
            highYesterdayLabel.setText("Yesterday: High "+temp+"F");
        }
         if (windSpeed > 0 && today == true && yesterday == false && windSpeed > todaysMaxWind) {
            todaysMaxWind = windSpeed; 
            todaysMaxWindLabel.setText("Max Wind "+todaysMaxWind+"MPH");
        } else if (windSpeed > 0 && today == false && yesterday == true && windSpeed > yesterdaysMaxWind) {
            yesterdaysMaxWind = windSpeed;
            yesterdaysMaxWindLabel.setText("Max Wind "+yesterdaysMaxWind+"MPH");
        }
        if (today == true && yesterday == false && temp <= todaysLow) {
            todaysLow = temp;
            lowTodayLabel.setText("<html><font color=blue>Low "+temp+"F</font></html>");
        } else if (today == false && yesterday == true && temp < yesterdaysLow) {
            yesterdaysLow = temp;
            lowYesterdayLabel.setText("Low "+temp+"F");
        }
            if (row < 500) {
                activeMetarTable.setValueAt(metar.getTimeString(), row, 0);
                activeMetarTable.setValueAt(metar.getSkyIcon(), row, 1);
                activeMetarTable.setValueAt(metar.getTempString(), row, 2);
                activeMetarTable.setValueAt(metar.getDwptString(), row, 3);
                activeMetarTable.setValueAt(metar.getWindDirString(), row, 4);
                activeMetarTable.setValueAt(metar.getWindSpeedString(), row, 5);
                activeMetarTable.setValueAt(metar.getWindGustString(), row, 6);
                activeMetarTable.setValueAt(metar.getWeatherString(), row, 7);
                activeMetarTable.setValueAt(metar.getVsbyString(), row, 8);
                activeMetarTable.setValueAt(metar.getOneHrPcpnString(), row, 9);
                activeMetarTable.setValueAt(metar.getThreeHrPcpnString(), row, 10);
                activeMetarTable.setValueAt(metar.getSixHrPcpnString(), row, 11);
            }
        
        row++;
        }
    }
    
    private JTable addTab() {
        JTable mTable = null;
        JScrollPane jsp = new JScrollPane();

         mTable = new JTable();
         mTable.setFont(font);
        
         Object[][] obj = new Object[500][8];
        
         mTable.setModel(new javax.swing.table.DefaultTableModel(
            obj,
           new String [] {
                "Time","Sky", "Temp", "Dwpt", "Dir", "Speed", "Gust", "Weather"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class,ImageIcon.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false,false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mTable.setShowHorizontalLines(true);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        mTable.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(6).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(7).setCellRenderer( centerRenderer );
         mTable.getColumnModel().getColumn(8).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(9).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(10).setCellRenderer( centerRenderer );
        mTable.getColumnModel().getColumn(11).setCellRenderer( centerRenderer );
       
        mTable.getColumnModel().getColumn(0).setPreferredWidth(175);
        mTable.getTableHeader().setFont(font);
        
    
        jsp.setViewportView(mTable);
        int index = tabs.getTabCount();
        tabs.addTab("Loading", jsp);
        //tabs.setToolTipTextAt(index,name);
        return mTable;

    }
    
     class ImageRenderer extends DefaultTableCellRenderer {
    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,col);
        if (value instanceof ImageIcon) {
            label.setIcon((ImageIcon) value);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setText("");
        } else {
             label.setIcon(null);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setText("");
        }
        return label;
    }
}
}
