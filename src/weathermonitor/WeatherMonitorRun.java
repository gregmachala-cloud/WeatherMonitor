package weathermonitor;

import weathermonitor.threads.Timer;
import weathermonitor.layers.WxAlertsTable;
import weathermonitor.dataobjects.WmsRadarLayer;
import weathermonitor.settings.WeatherMonitorSettings;
import weathermonitor.layers.WarningLayer;
import weathermonitor.dataobjects.WarningData;
import utilities.Viewer;
import weathermonitor.layers.StormReportLayer;
import weathermonitor.dataobjects.StormReportInfo;
import weathermonitor.listeners.SettingsListener;
import spcmap.SPCMapViewer;
import weathermonitor.layers.RadarLayer;
import weathermonitor.listeners.RadarImageListener;
import weathermonitor.layers.PanLayer;
import weathermonitor.listeners.NetworkListener;
import utilities.MetarViewer;
import weathermonitor.layers.MetarLayer;
import weathermonitor.layers.MapLayer;
import weathermonitor.layers.ForecastTable;
import weathermonitor.layers.AlertLayer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import weathermonitor.dataobjects.MetarStationInfo;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author greg
 */
public class WeatherMonitorRun extends javax.swing.JFrame implements SettingsListener,NetworkListener,RadarImageListener,BboxListener{
    public static RadarLayer radarLayer;
    public static String OPEN_GL = System.getProperty("sun.java2d.opengl");
    private Point clickPoint;
    
    private boolean useOpenGL = false;
    private Point releasePoint;
    private static PanLayer panLayer;
    private BufferedImage panImg;
    private static MapLayer mapLayer;
    public static  MetarLayer metarLayer;
    private boolean dragging = false;
    private int zoomLevel = 0;
    private double lat = 32.09;
    private double lon = -84.24;
    private static Border lineBorder =  BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0),2);
    private static Color bottomPanelBgColor = new Color(242,242,242);
     private JButton[] zoomButtons = new JButton[7];
    private ImageIcon[] zoomButtonsUnsel = new ImageIcon[7];
    private ImageIcon[] zoomButtonsSel = new ImageIcon[7];
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WeatherMonitorRun.class.getName());
private String[] comboBoxLabels = new String[5];
private String[] favAddress = new String[5];
private MetarStationInfo nearestMetar = null;
    private String[] comboBoxLats = new String[5];
    private String[] comboBoxLons = new String[5];
    private boolean showWindSpeedsOnArrows = true;
    private int showWindSpeedsOnArrowsValues = WeatherMonitorSettings.ALL_WIND_SPEEDS;
    public static Color mapBgColor = Color.WHITE;
    public static Color outBgColor = Color.lightGray;
     private Font bigFont = new Font("Monospaced",Font.PLAIN,18);
    private Font hugeFont = new Font("Monospaced",Font.PLAIN,24);
    private int mouseMoving = 0;
    private boolean mouseExited = false;
    private String bbox = null;
    private long mouseLastMovedTime = 0;
    private int radarLoopDelay = 500;
    public static AlertLayer alertLayer;
    public static WarningLayer warningLayer;
    public static StormReportLayer stormReportLayer;
    private int scrollAmount = 0;
    private javax.swing.Timer scrollTimer;
    private int scrollWheelX;
    private int scrollWheelY;
    private WxAlertsTable wxAlertsTable;
    private int tableLocation =0;
    Properties props ;
    private ArrayList<StormReportInfo> stormReports = null;
    private Color prodTableBg = Color.white;
    private Font prodTableFont = new Font("SansSerif",Font.BOLD,18);
    private Font applicationFont = new Font("SansSerif",Font.BOLD,18);
    private boolean loopRadar = false;
    private boolean uniformObsFont = false;
    public static boolean enabledTooltips;
    private int obsFontNumber = 4;
    javax.swing.Timer resizeTimer;
    private Hashtable<Color,String>  radarLegendHash;
    public static ForecastTable fcstTable;
    private int forecastPeriods = 6;
    private boolean networkUp = true;
    private double lowObDistance = 5.0;
    private double midObDistance = 7.0;
    private double highObDistance = 9.0;
    private static final double id = Math.random();
    private static final boolean zoomToScrollWheelLoction = false;
    public static int[] STORM_REPORT_OFFSET_SECS = {12*3600,24*3600,48*3600,72*3600,Integer.MAX_VALUE};
    /**
     * Creates new form TestMapLayer
     */
    public WeatherMonitorRun() {
//        try {
//            
//            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
//            //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//                   
//        } catch (Exception ex) {
//            logger.log(java.util.logging.Level.SEVERE, null, ex);
//        }
        UIManager.put("TabbedPane.selected", new Color(255,255,113));
        UIManager.put("ToolTip.background", new ColorUIResource(255, 255, 255)); 
        System.out.println("OpenGL="+System.getProperty("sun.java2d.opengl"));
        UIManager.put("ToolTip.font", new Font("SansSerif", Font.BOLD, 20));
        resizeTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                radarLegendLabel.setLocation(0,backgroundPanel.getHeight()- 41);
                if (mapLayer != null) mapLayer.recenterMap(lat, lon, false);
            }
        });
        resizeTimer.setRepeats(false);
         props = Utilities.readPrefs();
         if (props == null) {
             JOptionPane.showMessageDialog(this, "Error reading setup file radar.properties...exiting");
             System.exit(1);
         }
        
        if (props.containsKey("X") && props.containsKey("Y")) {
            try {
                Integer x = Integer.valueOf(props.getProperty("X"));
                Integer y = Integer.valueOf(props.getProperty("Y"));
                this.setLocation(x, y);
            } catch (Exception ex) {
                this.setLocation(0, 0);
            }
        } else {
            this.setLocation(0, 0);
        }
        if (props.containsKey("WIDTH") && props.containsKey("HEIGHT")) {
            try {
                Integer w = Integer.valueOf(props.getProperty("WIDTH"));
                Integer h = Integer.valueOf(props.getProperty("HEIGHT"));
                System.out.println("Setting size to "+w+"x"+h);
                this.setSize(new Dimension(w, h));
                this.setPreferredSize(new Dimension(w, h));
            } catch (Exception ex) {
                this.setSize(new Dimension(1280, 1024));
                this.setPreferredSize(new Dimension(1280, 1024));
            }
        } else {
            this.setSize(new Dimension(1280, 1024));
            this.setPreferredSize(new Dimension(1280, 1024));
        }
        
        
        initComponents();
         OPEN_GL = System.getProperty("sun.java2d.opengl");
        if (OPEN_GL != null && OPEN_GL.equals("true")) {
             openGLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/GL_on.png")));
             useOpenGL = true;
        } else {
            openGLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/GL_off.png")));
             useOpenGL = false;
        }
        stormReportHoursCb.addItemListener(Viewer.getInstance());
        waitIcon.setVisible(false);
        infoLabel.setVisible(false);
        nearestMetar = null;
        stormReports = null;
        scrollTimer = new javax.swing.Timer(500, new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
            zoomToScollAmount();
        }
        });
        scrollTimer.setRepeats(false);
       // countdownField.setBackground(bottomPanelBgColor);
        jPanel4.setBackground(bottomPanelBgColor);
        bottomPanel.setBackground(bottomPanelBgColor);
        this.jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(comboBoxLabels));
        //Properties props = Utilities.readPrefs();
        updateSettings(props,true);
        declareButtonArrays();
        zoomButtons[zoomLevel].setIcon(zoomButtonsSel[zoomLevel]);
        this.setVisible(true);
        
        radarLegendLabel.setLocation(0,backgroundPanel.getHeight()- 41);
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                   ex.printStackTrace();
                }
                loadMaps();
            }
        };
        t.start();
        //java.awt.EventQueue.invokeLater(() -> loadMaps());
    }
    private boolean updateSettings(Properties props, boolean okToChangeZoomLevel) {
        comboBoxLabels[0] = "Choose Location";
        comboBoxLats[0] = "";
        comboBoxLons[0] = "";
        favAddress[0] = "";
        boolean needsRepaint = false;
        if (props != null) {
             if (props.containsKey("ToolTipsEnabled")) {
                String value = props.getProperty("ToolTipsEnabled");
                if (value.equals("true") || value.equals("1")) {
                    enabledTooltips = true;
                } else {
                    enabledTooltips = false;
                    radarLegendLabel.setToolTipText(null);
                }
                
            }
            if (props.containsKey("LowObDistance")
                    && props.containsKey("MidObDistance")
                    && props.containsKey("HighObDistance")) {
                try {
                    this.lowObDistance = Double.valueOf(props.getProperty("LowObDistance"));
                    this.midObDistance = Double.valueOf(props.getProperty("MidObDistance"));
                    this.highObDistance = Double.valueOf(props.getProperty("HighObDistance"));
                } catch (Exception ex) {
                    this.lowObDistance = 5.0;
                    this.midObDistance = 7.0;
                    this.highObDistance = 9.0;
                }
                if (metarLayer != null) {
                    metarLayer.setMetarDistanceBuffer(lowObDistance,midObDistance,highObDistance);
                }
            }
            if (props.containsKey("ShowWindSpeedsOnArrowsValues")) {
                String value = props.getProperty("ShowWindSpeedsOnArrowsValues");
                
                showWindSpeedsOnArrowsValues = Integer.parseInt(value);
               if (metarLayer != null) {
                    int v = metarLayer.getShowWindSpeedsOnArrowsValues();
                    if (this.showWindSpeedsOnArrowsValues != v) {
                        needsRepaint = true;
                    }
                }
                
                if (metarLayer != null) metarLayer.setShowWindSpeedsOnArrowsValues(showWindSpeedsOnArrowsValues);
                
            }
            if (props.containsKey("ShowWindSpeedsOnArrows")) {
                 String value = props.getProperty("ShowWindSpeedsOnArrows");
                if (value.equals("0") || value.equals("false")) {
                    this.showWindSpeedsOnArrows = false;
                } else {
                    this.showWindSpeedsOnArrows = true;
                }
                if (metarLayer != null) {
                    boolean v = metarLayer.getShowWindSpeedsOnArrows();
                    if (this.showWindSpeedsOnArrows != v) {
                        needsRepaint = true;
                    }
                }
               
                if (metarLayer != null) {
                  
                    metarLayer.setShowWindSpeedsOnArrows(showWindSpeedsOnArrows);
                }
                
            }
            if (props.containsKey("LoopRadar")) {
            String value = props.getProperty("LoopRadar");
            if (value.equals("1")) {
                this.radarLoopCb.setSelected(true);
                loopRadar = true;
            } else {
                this.radarLoopCb.setSelected(false);
                loopRadar = false;
            }
            if (radarLayer != null) radarLayer.setLoop(radarLoopCb.isSelected());
          }
            if (props.containsKey("ForecastPeriods")) {
             String v = props.getProperty("ForecastPeriods");
            
             forecastPeriods = Integer.parseInt(props.getProperty("ForecastPeriods"));
         }
         
           
            uniformObsFont = false; 
            obsFontNumber = 0;
          
                if (props.containsKey("ObsFont")) {
                    int index = Integer.parseInt(props.getProperty("ObsFont"));
                    if (index >= 1) {
                        
                        obsFontNumber = index;
                    }
                }
            
            if (metarLayer != null) {
                metarLayer.setFontNumber(obsFontNumber);
                
            }
          
          if (props.containsKey("MapBgColor")) {
               String[] arr = props.getProperty("MapBgColor").split(",");
               mapBgColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
              
               if (mapLayer != null) {
                   Color c = mapLayer.getMapBgColor();
                   int r = c.getRed();
                   int g = c.getGreen();
                   int b = c.getBlue();
                   int tr = mapBgColor.getRed();
                   int tg = mapBgColor.getGreen();
                   int tb = mapBgColor.getBlue();
                   if (r!=tr || g!=tg || b != tb) {
                       needsRepaint = true;
                   }
               }
               if (metarLayer != null) {
                   metarLayer.setMapBgColor(mapBgColor);
               }
               if (mapLayer != null) {
                   mapLayer.getMap().setMapBgColor(mapBgColor);
                   
               }
          }
          if (props.containsKey("ProdTableFont")) {
               String[] arr = props.getProperty("ProdTableFont").split(",");
               int bold = Integer.valueOf(arr[2]);
               int fontSize = Integer.valueOf(arr[1]);
               prodTableFont = new Font(arr[0],bold,fontSize);
               if (this.wxAlertsTable != null) {
                   wxAlertsTable.setTableFont(prodTableFont);
               }
              
               
          }
          if (props.containsKey("ApplicationFont")) {
              String[] arr = props.getProperty("ApplicationFont").split(",");
               int bold = Integer.valueOf(arr[2]);
               int fontSize = Integer.valueOf(arr[1]);
               applicationFont = new Font(arr[0],bold,fontSize);
            
               advisoriesCb.setFont(applicationFont);
               fcstCb.setFont(applicationFont);
               radarLoopCb.setFont(applicationFont);
               warningsCb.setFont(applicationFont);
               obsCheckbox.setFont(applicationFont);
               samplingCheckbox.setFont(applicationFont);
               metarSiteCb.setFont(applicationFont);
               stormReportsCb.setFont(applicationFont);
               hideRadarCheckbox.setFont(applicationFont);
               stormReportHoursCb.setFont(applicationFont);
               RadarChooser.setFont(applicationFont);
               timestamp.setFont(applicationFont);
               MetarLayer.getInstance().setTimestampFont(applicationFont);
               layerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
          }
         if (props.containsKey("ProdTableBg")) {
            String[] arr = props.getProperty("ProdTableBg").split(",");
             prodTableBg = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            if  (wxAlertsTable != null) {
                wxAlertsTable.setTableBgColor(prodTableBg);
                
              
            }      
             
        }
            if (props.containsKey("YourLocation")) {
               double testLat = Double.parseDouble(props.getProperty("Lat"));
               double testLon = Double.parseDouble(props.getProperty("Lon"));
               if (testLat != lat || testLon != lon) {
                   needsRepaint = true;
               }
                      
                String newLocation = props.getProperty("YourLocation").trim();
                String currentLocation = this.LocationEntry.getText().trim();
               // System.out.println("currentLocation=" + currentLocation);
                if (currentLocation.equals("") || currentLocation.equals("Your Location")) {
                    this.LocationEntry.setText(newLocation);
                    if (props.containsKey("Lat") && props.containsKey("Lon")) {
                        this.lat = Double.parseDouble(props.getProperty("Lat"));
                        this.lon = Double.parseDouble(props.getProperty("Lon"));
                        
                    }
                    if (okToChangeZoomLevel && props.containsKey("ZoomLevel")) {
                        int newZoomLevel = Integer.parseInt(props.getProperty("ZoomLevel")) - 1;
                        if (newZoomLevel != this.zoomLevel) {
                            this.zoomLevel = newZoomLevel;
                            
                        }

                   }
                }
            }
//            if (props.containsKey("SmoothRadar")) {
//                String b = props.getProperty("SmoothRadar");
//                if (b.equals("true") || b.equals("1")) {
//                    this.smoothCheckbox.setSelected(true);
//                } else {
//                    this.smoothCheckbox.setSelected(false);
//                }
//            }
            if (props.containsKey("FPS")) {
                String fps = props.getProperty("FPS").trim();
                System.out.println("radar delay fps="+fps);
                try {
                    Integer v = Integer.parseInt(fps);
                    if (OPEN_GL == null || OPEN_GL.equals("false")) {
                        System.out.println("Open gl is false");
                        v = 2;
                    }
                    int ms = 1000/v;
                    radarLoopDelay = ms;
                    if (radarLayer != null) {
                        System.out.println("Set radar animation delay "+radarLoopDelay);
                        radarLayer.setAnimationDelay(radarLoopDelay);
                    } else {
                        System.out.println("Radar layer is null cannot set loop delay");
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
           
            if (props.containsKey("Lat") && props.containsKey("Lon")) {
                
                comboBoxLabels[1] = "Your Location";
                comboBoxLats[1] = props.getProperty("Lat");
                comboBoxLons[1] = props.getProperty("Lon");
            }

            if (props.containsKey("OutBgColor")) {
                String[] arr = props.getProperty("OutBgColor").split(",");
                 outBgColor = new Color(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]));
                this.backgroundPanel.setBackground(outBgColor);
                //this.mapBg.setIcon(new ImageIcon(mapLayer.getMap().getMapBgImage()));
            }
          if  (props.containsKey("YourLocation")) favAddress[1] = props.getProperty("YourLocation");
           
            if  (props.containsKey("Fav1Address")) favAddress[2] = props.getProperty("Fav1Address");
              if  (props.containsKey("Fav2Address")) favAddress[3] = props.getProperty("Fav2Address");
            if  (props.containsKey("Fav3Address")) favAddress[4] = props.getProperty("Fav3Address");
             
            
            
            if (props.containsKey("Fav1Name") && props.containsKey("Fav1Lat") && props.containsKey("Fav1Lon")) {
                comboBoxLabels[2] = props.getProperty("Fav1Name");
                comboBoxLats[2] = props.getProperty("Fav1Lat");
                comboBoxLons[2] = props.getProperty("Fav1Lon");
            }
            if (props.containsKey("Fav2Name") && props.containsKey("Fav2Lat") && props.containsKey("Fav2Lon")) {
                comboBoxLabels[3] = props.getProperty("Fav2Name");
                comboBoxLats[3] = props.getProperty("Fav2Lat");
                comboBoxLons[3] = props.getProperty("Fav2Lon");
            }
            if (props.containsKey("Fav3Name") && props.containsKey("Fav3Lat") && props.containsKey("Fav3Lon")) {
                comboBoxLabels[4] = props.getProperty("Fav3Name");
                comboBoxLats[4] = props.getProperty("Fav3Lat");
                comboBoxLons[4] = props.getProperty("Fav3Lon");
            }
           
            
        }
      
        this.jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(comboBoxLabels));
        return needsRepaint;
    }
    private void declareButtonArrays() {
        zoomButtons[0] = zoom1Button;
      zoomButtons[1] = zoom2Button;
      zoomButtons[2] = zoom3Button;
      zoomButtons[3] = zoom4Button;
      zoomButtons[4] = zoom5Button;
       zoomButtons[5] = zoom6Button;
      zoomButtons[6] = zoom7Button;
      zoomButtonsUnsel[0] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/1.png"));
      zoomButtonsUnsel[1] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/2.png"));
      zoomButtonsUnsel[2] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/3.png"));
      zoomButtonsUnsel[3] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/4.png"));
      zoomButtonsUnsel[4] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/5.png"));
      zoomButtonsUnsel[5] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/6.png"));
      zoomButtonsUnsel[6] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/7.png"));
      zoomButtonsSel[0] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/1_sel.png"));
      zoomButtonsSel[1] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/2_sel.png"));
      zoomButtonsSel[2] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/3_sel.png"));
      zoomButtonsSel[3] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/4_sel.png"));
      zoomButtonsSel[4] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/5_sel.png"));
      zoomButtonsSel[5] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/6_sel.png"));
      zoomButtonsSel[6] = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/7_sel.png"));
      
    }
    public static void showAllLayers() {
            radarLegendLabel.setVisible(true);
            mapLayer.setVisible(true);
            radarLayer.setVisible(hideRadarCheckbox.isSelected());
            metarLayer.setVisible(obsCheckbox.isSelected());
            stormReportLayer.setVisible(stormReportsCb.isSelected());
            alertLayer.setVisible(advisoriesCb.isSelected());
            warningLayer.setVisible(warningsCb.isSelected());
            fcstTable.setVisible(fcstCb.isSelected());
            
    }
    
    public static void hideAllLayers() {
            if (mapLayer != null) {
                try {
            infoLabel.setVisible(false);
            radarLegendLabel.setVisible(false);
            mapLayer.setVisible(false);
            radarLayer.setVisible(false);
           
            metarLayer.setVisible(false);
            stormReportLayer.setVisible(false);
            fcstTable.setVisible(false);
            
            alertLayer.setVisible(false);
          
            warningLayer.setVisible(false);
                } catch (Exception ex) {}
            }
    }
    
    public static void  blankScreen() {
         alertLayer.setAlertImage(Utilities.getEmptyImage());
         warningLayer.setWarningImage(Utilities.getEmptyImage());
         metarLayer.setMetarImage(Utilities.getEmptyImage());
         stormReportLayer.setReportImage(Utilities.getEmptyImage());
    }
    private void loadMaps() {
        int w = layeredPane.getWidth();
        int h = layeredPane.getHeight();
        System.out.println("width="+w+" height="+h);
      
        
        metarLayer = MetarLayer.getInstance();
        metarLayer.initLayer(w,h);
        metarLayer.setFontNumber(obsFontNumber);
        metarLayer.setMetarDistanceBuffer(lowObDistance,midObDistance,highObDistance);
        metarLayer.setShowWindSpeedsOnArrows(showWindSpeedsOnArrows);
        metarLayer.setShowWindSpeedsOnArrowsValues(showWindSpeedsOnArrowsValues);
        mapLayer = new MapLayer(metarLayer,zoomLevel,w, h,lat,lon,props);
        
        
        //this.mapBgLabel.setIcon(new ImageIcon(mapLayer.getMap().getMapBgImage()));
        this.zoomLevel = mapLayer.getZoomLevel();
         wxAlertsTable = new WxAlertsTable(lat,lon,layeredPane,this);
         wxAlertsTable.setTableBgColor(prodTableBg);
         wxAlertsTable.setTableFont(prodTableFont);
         stormReportHoursCb.addItemListener(wxAlertsTable);
        backgroundPanel.setSize(w,h);
        
        mapLayer.getMap().addBboxListener(wxAlertsTable);
        radarLayer = new RadarLayer(mapLayer.getMap(),waitIcon, w, h,lat,lon);
        
        mapLayer.getMap().addBboxListener(radarLayer);
        mapLayer.getMap().addBboxListener(Viewer.getInstance());
        radarLayer.setTimestamp(timestamp);
        radarLayer.setAnimationDelay(radarLoopDelay);
        radarLayer.setLoop(loopRadar);
        //radarLayer.setCountdownField(countdownField);
        
       // radarLayer.getRadar().addNetworkListener(this);
        radarLayer.getRadar().addRadarImageListener(this);
        //radarLayer.getRadar().addRadarImageListener(mapLayer);
        
        mapLayer.getMap().addBboxListener(radarLayer.getRadar());
        //radarLayer.getRadar().addNetworkListener(Timer.getInstance());
        String[] radars = radarLayer.getRadar().getDescrRadarNames();
        
        this.RadarChooser.setModel(new javax.swing.DefaultComboBoxModel<>(radars));
       // RadarChooser.addItem("Edit Settings");
        warningLayer = new WarningLayer(mapLayer.getMap(),w,h);
        //warningLayer.addNetworkListener(this);
        alertLayer = new AlertLayer(mapLayer.getMap(),w,h);
        
        mapLayer.getMap().addBboxListener(alertLayer);
        //alertLayer.addNetworkListener(this);
        stormReportLayer = StormReportLayer.getInstance();
        stormReportLayer.initStormReportLayer(mapLayer.getMap(),lat,lon,w,h);
        mapLayer.getMap().addBboxListener(stormReportLayer);
        fcstTable = ForecastTable.getInstance();
        fcstTable.initForecastTable(lat,lon,layeredPane,forecastPeriods, this);
        
        mapLayer.getMap().addBboxListener(fcstTable);
        panLayer = new PanLayer();
        infoPanel.add(panLayer);
        infoPanel.add(wxAlertsTable);
        infoPanel.add(fcstTable);
        //layeredPane.add(wxAlerts,1);
        layeredPane.add(stormReportLayer, 1);
        layeredPane.add(metarLayer, 2);
        layeredPane.add(warningLayer, 3);
        layeredPane.add(mapLayer, 4);
        layeredPane.add(radarLayer, 5);
        layeredPane.add(alertLayer, 6);// products like sps heat advy etc at bottom
        mapLayer.getMap().addBboxListener(this);
        radarLayer.getRadar().loadRadar(mapLayer.getWidth(),mapLayer.getHeight());
        java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
             mapBgLabel.setIcon(new ImageIcon(mapLayer.getMap().getMapBgImage()));
        }
        
        });

        Timer.getInstance().addNetworkListener(metarLayer);
        Timer.getInstance().addNetworkListener(radarLayer);
        Timer.getInstance().addNetworkListener(this);
        Timer.getInstance().addNetworkListener(fcstTable);
        Timer.getInstance().addTimerListener(alertLayer);
        Timer.getInstance().addTimerListener(radarLayer.getRadar());
        Timer.getInstance().addTimerListener(fcstTable);
        Timer.getInstance().addTimerListener(metarLayer);
        Timer.getInstance().addTimerListener(stormReportLayer);
        Timer.getInstance().addTimerListener(warningLayer);
        Timer.getInstance().addTimerListener(wxAlertsTable);
        Timer.getInstance().startTimer();
    }
    
     private void zoomOut() {
          int zoom = zoomLevel - 1;
           //System.out.println("zoomLevel="+zoomLevel);
           if (zoom < 0) {
               // dont do shit
           } else {
             setZoomLevel(zoom);
           }
    }

     private void zoomToScollAmount() {
         //System.out.println("Zoom to scroll amount:"+scrollAmount);
         int delta = zoomLevel - (scrollAmount/2);
         if (delta < 0) delta = 0;
         if (delta > 6) delta = 6;
         scrollAmount = 0;
         if (zoomToScrollWheelLoction == true) {
             setZoomLevel(delta,new Point(scrollWheelX,scrollWheelY));
         } else {
             setZoomLevel(delta);
         }
     }
     private void setZoomLevel(int index) {
         setZoomLevel(index,null);
     }
    private void setZoomLevel(int index, Point center) {
        if (index == zoomLevel) return;
        hideAllLayers();
       // this.warningLayer.setVisible(false);
      //  this.alertLayer.setVisible(false);
        if (zoomLevel == index) return;
        zoomLevel = index;
        if (center != null) mapLayer.getMap().setMapCenter(center);
        mapLayer.setZoomLevel(zoomLevel);
        
        for (int i=0; i<zoomButtons.length; i++) {
            zoomButtons[i].setIcon(zoomButtonsUnsel[i]);
        }
        zoomButtons[index].setIcon(zoomButtonsSel[index]);
        //mapLayer.repaint();
    }
    private void zoomIn() {
        int zoom = zoomLevel + 1;
     
           if (zoom > 6) {
               // Dont do shit
           } else {
               setZoomLevel(zoom);
           }
    }
    private void panMap(int dX, int dY) {
        this.LocationEntry.setText("");
        mapLayer.getMap().panMap(dX,dY);
    }

     private void recenterMap(double newLat, double newLon, boolean useNearestCityForLocation) {
        //blanking the location entry will force the listner to fill
        // in the location entry with the nearesst city not the address
         if (useNearestCityForLocation == true) this.LocationEntry.setText("");
        hideAllLayers();
        mapLayer.recenterMap(newLat,newLon,useNearestCityForLocation);
       
    }
//    private void loadRadar() {
//        Thread t = new Thread() {
//            public void run() {
//                radarLayer.getRadar().loadRadar();
//                
//            }
//        };
//        t.start();
//        
//    }
    public String[] getStateIds() {
        return mapLayer.getMap().getStateIdsArray();
    
    }
    
    private String getNearestLegendValue(Color color) {
        int r = color.getRed(); int g = color.getGreen(); int b = color.getBlue();
        Enumeration en = radarLegendHash.keys();
        String retVal = "";
        int diff = Integer.MAX_VALUE; Color nearestColor = null;
        while (en.hasMoreElements()) {
            Color testColor = (Color) en.nextElement();
            int tr = testColor.getRed(); int tg =  testColor.getGreen(); int tb = testColor.getBlue();
            int tsum = Math.abs(tr-r)+Math.abs(tg-g)+Math.abs(tb-b);
            //int test = Math.abs(rgb-key);
            //System.out.println("diff="+test);
            if (tsum < diff) {
                diff = tsum;
                nearestColor = testColor;
                retVal = radarLegendHash.get(testColor);
            }
        }
       // System.out.println("lowest diff is "+diff+ " nearest color is "+nearestColor.toString());
        return retVal;
    }
    private ImageIcon combineStormIcon(ImageIcon[] icons, int imgHeight) {
        if (icons.length == 1) {
            return icons[0];
        }
        //int totalHeight = icons.length*20;
        int incr = imgHeight/icons.length;
        BufferedImage img = new BufferedImage(30,imgHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        int y=0;
        for (int i=0; i<icons.length; i++) {
            g.drawImage(icons[i].getImage(), 0, y, null);
            y = y +incr;
        }
        g.dispose();
        return new ImageIcon(img);
    }
    public void refresh() {
        mapLayer.getMap().refresh();
    }
     private boolean getMapFeatures(int x, int y) {
        try {
         if (mouseExited == true) {
             infoLabel.setVisible(false);
             
             return false;
         }
         stormReports = null;
           nearestMetar = null;
         double[] point = mapLayer.getMap().pixelToWorld(x, y);
         Font f = infoLabel.getFont();
          String text = "";
         Image radarImg = radarLayer.getLatestRadarImage();
         if (radarLayer.isLooping() == false && radarImg != null && radarLegendHash != null) {
             //System.out.println("Radar image is not null and legen hash is not null");
             BufferedImage bufImg = new BufferedImage(radarImg.getWidth(null), radarImg.getWidth(null), BufferedImage.TYPE_INT_ARGB);
             Graphics g = bufImg.getGraphics();
             g.drawImage(radarImg, 0, 0, null);
             g.dispose();
             int rgb = bufImg.getRGB(x+2, y+2);
            // System.out.println("rgb="+rgb);
             if (rgb == 0) {
                 text = "";
             } else {
                 Color color = new Color(rgb);
                 text = getNearestLegendValue(color);
                 
             
            // System.out.println("text="+text);
             BufferedImage img = new BufferedImage(750, 200, BufferedImage.TYPE_INT_ARGB);

             Graphics2D g2d = img.createGraphics();
             g2d.setFont(f);
             FontMetrics m = g2d.getFontMetrics(f);
             Rectangle2D rect = m.getStringBounds(text, g2d);
             double height = rect.getHeight();
             double width = rect.getWidth();

             g2d.dispose();
             int textWidth = Double.valueOf(width).intValue() + 10;
             int textHeight = Double.valueOf(height).intValue() + 1;
             //System.out.println("size="+textWidth+"x"+textHeight);
             infoLabel.setSize(new Dimension(textWidth, textHeight));
             infoLabel.setIcon(null);
             infoLabel.setText(text);
             }
         }
         if (stormReportLayer.isVisible()) {
             stormReports = stormReportLayer.getStormReportsAtPoint(x, y,mapLayer.getMap());
             if (stormReports != null && stormReports.size() > 0) {
                 ImageIcon[] icons = new ImageIcon[stormReports.size()];
                 text = "<html>";
                 for (int i = 0; i < stormReports.size(); i++) {
                     icons[i] = stormReports.get(i).getIcon();
                     String line = stormReports.get(i).getDate() + " " + stormReports.get(i).getTime() + " " + stormReports.get(i).getCity()+", "+stormReports.get(i).getState() + " " + stormReports.get(i).getEventType() + " " + stormReports.get(i).getEventMag();
                     text = text + line + "<br>";

                 }
                 text = text + "<center><font color=yellow>Click For Details</font></center></html>";
                 infoLabel.setText(text);
                 ImageIcon combined = combineStormIcon(icons,infoLabel.getPreferredSize().height-20);
                 infoLabel.setIcon(combined);
                 infoLabel.setSize(infoLabel.getPreferredSize());
                 nearestMetar = null;
             } else {
                 stormReports = null;
             }
         }
         // System.out.println("Get metar data at x"+x+" y"+y);
         if (metarLayer.isVisible()) {
         MetarStationInfo msi = metarLayer.getMetarSampleDataAtPoint(x, y);
         String vsby = "";
         if (text.equals("") && msi != null) {
             String oneHourAgo = "";
             if (msi.getVsby() >= 0 && msi.getVsby() <= 5.0) {
                 vsby = " "+Double.toString(msi.getVsby())+"Mile";
             }
             String t = msi.getTemp()+"F";
             if (msi.getTemp() == MetarStationInfo.MISSING_TEMP) {
                 t = "M";
             }
             String d = msi.getDwpt()+"F";
             if (msi.getDwpt() == MetarStationInfo.MISSING_TEMP) {
                 d = "M";
             }
             String wind = "";
            
             int windSpeed = msi.getWindSpeedMPH();
             if (windSpeed != MetarStationInfo.MISSING_WIND && windSpeed > 0) {
                 String windDir = msi.getWindDirString();
                 wind = windDir+windSpeed+"MPH";
             }
             int windGust = msi.getWindGustMPH();
             if (windGust != MetarStationInfo.MISSING_WIND && windGust > 0) {
                 String windDir = msi.getWindDirString();
                 wind = windDir+windGust+"MPH";
             }
             String wxIcon = "";
             String wx = ":";
             if (msi.getWeatherIcon() != null && msi.getWeatherIcon().equals("") == false) {
                  wxIcon = msi.getWeatherIcon();
                  wx = " "+wxIcon.replaceAll(".png", "").replaceAll("_"," ")+" ";
             }
             
             if (msi.getObservationTimeString().length() == 0) {
                text = msi.getStationId()+" "+msi.getObservationTimeString()+vsby+wx+t+"/"+d+" "+wind;
             } else {
                text = msi.getStationId()+" "+msi.getObservationTimeString().substring(10).trim()+vsby+wx+t+"/"+d+" "+wind+" "+oneHourAgo;
             }
             BufferedImage img = new BufferedImage(750,100,BufferedImage.TYPE_INT_ARGB);
                       
                Graphics2D g2d = img.createGraphics();
                g2d.setFont(f);
                FontMetrics m = g2d.getFontMetrics(f);
                Rectangle2D rect = m.getStringBounds(text, g2d);
                double height = rect.getHeight();
                double width = rect.getWidth()+5;
               
                g2d.dispose();
                
                int textWidth = Double.valueOf(width).intValue();
                int textHeight = Double.valueOf(height).intValue()+1;
                //System.out.println("size="+textWidth+"x"+textHeight);
                
                infoLabel.setText("<html><center>"+text+"<br><font size=3 color=yellow>Click For Details</font></center></html>");
                if (wxIcon != "") {
                    System.out.println("Get weatehr icon for "+wxIcon);
                    ImageIcon weatherIcon = new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/"+wxIcon));
                    infoLabel.setIcon(weatherIcon);
                    Dimension dim = infoLabel.getPreferredSize();
                    infoLabel.setSize(dim);
                } else {
                    infoLabel.setIcon(null);
                    infoLabel.setSize(new Dimension(textWidth,textHeight+18));
                }
                nearestMetar = msi;
         } 
         }
         
         if (text.equals("")) {
             MetarStationInfo msi1 =metarLayer.getMetarSiteDataAtPoint(x, y);
             if(msi1 != null) {
             text = msi1.getStationId() +" - "+ msi1.getStationName();
             
             
             
             BufferedImage img = new BufferedImage(250,50,BufferedImage.TYPE_INT_ARGB);
                       
                Graphics2D g2d = img.createGraphics();
                g2d.setFont(f);
                FontMetrics m = g2d.getFontMetrics(f);
                Rectangle2D rect = m.getStringBounds(text, g2d);
                double height = rect.getHeight();
                double width = rect.getWidth();
               
                g2d.dispose();
                
                int textWidth = Double.valueOf(width).intValue()+10;
                int textHeight = Double.valueOf(height).intValue()+1;
                //System.out.println("size="+textWidth+"x"+textHeight);
                infoLabel.setSize(new Dimension(textWidth,textHeight+18));
             
             infoLabel.setIcon(null);
             infoLabel.setText("<html><center>"+text+"<br><font color=yellow>Click For Details</font></html>");
             nearestMetar = msi1;
             }
         }
         
         
         if (text.equals("") == true) {
               
         
         
             String event = alertLayer.getProductAtPoint(point[0], point[1]);

             if (event != null && event.equals("") == false) {
                 text = "<html><center>" + event.toUpperCase() + "<br><font color=yellow>Click For Details</font></center></html>";
                 BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

                 Graphics2D g2d = img.createGraphics();
                 g2d.setFont(f);
                 FontMetrics m = g2d.getFontMetrics(f);
                 Rectangle2D rect1 = m.getStringBounds("Click For Details", g2d);
                 Rectangle2D rect2 = m.getStringBounds(event.toUpperCase(), g2d);
                 double height1 = rect1.getHeight();
                 double width1 = rect1.getWidth();
                 double height2 = rect2.getHeight();
                 double width2 = rect2.getWidth();

                 g2d.dispose();
                 int width = (int) width1;
                 if (width2 > width1) {
                     width = (int) width2;
                 }
                 int textWidth = Double.valueOf(width).intValue() + 10;
                 int textHeight = Double.valueOf(height2).intValue() * 2 + 2;
                 //System.out.println("size="+textWidth+"x"+textHeight);
                 infoLabel.setSize(new Dimension(textWidth, textHeight));
                 infoLabel.setIcon(null);
                 infoLabel.setText(text);
             }
                
                
        if (text.equals("")) {
                Rectangle re = new Rectangle(x-5,y-5,10,10);
                String roadName = mapLayer.getMap().getRoadNameInBboxFast(re).trim();
                String countyName = mapLayer.getMap().getCountyNameInBboxFast(x,y).trim();
                if (countyName.equals("") == false) countyName = countyName+" County";
                String title = mapLayer.getMap().getStateNameInBboxFast(x, y).trim().toUpperCase();
                  
                int maxLength = roadName.length();
                String stringToMeasure = roadName;
                if (countyName.length() > maxLength) {
                    maxLength = countyName.length();
                    stringToMeasure = countyName;
                }
                if (title.length() > maxLength) {
                    maxLength = title.length();
                    stringToMeasure = title;
                }
                 text = "<html><center>";
                int numberOfLines = 0; 
                if (title.equals("") == false) {
                    numberOfLines = numberOfLines+1;
                    text = text+"<b>"+title +"</b><br>";
                } 
                if (countyName.equals("") == false) {
                    numberOfLines = numberOfLines+1;
                    text = text+countyName +"<br>";
                } 
                if (roadName.equals("") == false) {
                    numberOfLines = numberOfLines+1;
                    text = text+roadName +"<br>";
                    
                } 
                text  = text+"</center></html>";
                //System.out.println("text="+text);
               // System.out.println("Found road with name :" + roadName + " county:" + countyName);
               // text = "<html>"+stateName+"<br>"+countyName +"<br>"+roadName+"<br></html>";
                BufferedImage img = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
                       
                Graphics2D g2d = img.createGraphics();
                g2d.setFont(f);
                FontMetrics m = g2d.getFontMetrics(f);
                Rectangle2D rect = m.getStringBounds(stringToMeasure, g2d);
                double height = rect.getHeight();
                double width = rect.getWidth();
               
                g2d.dispose();
                
                int textWidth = Double.valueOf(width).intValue()+10;
                int textHeight = Double.valueOf(height).intValue() * numberOfLines + numberOfLines;
                //System.out.println("size="+textWidth+"x"+textHeight);
                infoLabel.setSize(new Dimension(textWidth,textHeight));
                infoLabel.setIcon(null);
                infoLabel.setText(text);
               
        }
         
         
         }
         
         
            if (text.length() >= 2) {
             int infoLabelWidth = infoLabel.getWidth();
             int infoLabelHeight = infoLabel.getHeight();
             //int latLonReadoutHeight = latLonReadout.getHeight();
             
             int w = layeredPane.getWidth();
             if (fcstTable.isVisible()) {
                 w = w - fcstTable.getWidth();
             }
             int h = layeredPane.getHeight();
             int maxX = w - infoLabelWidth - 5;
             int maxY = h - infoLabelHeight - 5;
             int minY = infoLabelHeight + 5;
             if (x > maxX) {
                 x = maxX;
             }
             if (y > maxY) {
                 y = maxY;
             } else if (y < minY) {
                 y= minY;
             }
             
             infoLabel.setLocation(x + 5, y - infoLabelHeight - 5);
             infoLabel.setVisible(true);
             mouseLastMovedTime = new Date().getTime();
             Thread t= new Thread() {
                 public void run() {
                     try {
                         this.sleep(10100);
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
                      long curTime = new Date().getTime();
                      long diff = curTime - mouseLastMovedTime;
                      if (diff >= 10000) {
                          infoLabel.setIcon(null);
                          infoLabel.setVisible(false);
                          nearestMetar= null;
                          stormReports = null;
                          //metarLayer.clearNearestMetarSiteList();
                      }
                 }
             };
             t.start();
         } else {
                infoLabel.setIcon(null);
                infoLabel.setVisible(false);
                nearestMetar = null;
                stormReports = null;
                return false;
         }
         return true;
     } catch (Exception ex) {
        ex.printStackTrace();
     }
     return false;
     }
    
     private String getSevereWeatherAlerts(ArrayList<String> eventNames) {
         
         if  (eventNames == null) return null;
         if (eventNames.size() == 0) return null;
         for (int i=0; i<eventNames.size(); i++) {
             //System.out.println("Test is event "+list.get(i).getEvent()+" is severe");
             if (eventNames.get(i).equals("Severe Thunderstorm Warning")) {
                 return "Severe Thunderstorm Warning";
             }
             if (eventNames.get(i).equals("Flash Flood Warning")) {
                 return "Flash Flood Warning";
             }
             if (eventNames.get(i).equals("Flood Warning")) {
                 return "Flood Warning";
             }
             if (eventNames.get(i).equals("Special Marine Warning")) {
                 return "Special Marine Warning";
             }
             if (eventNames.get(i).equals("Tornado Warning")) {
                 return "Tornado Warning";
             }
             
         }
         return eventNames.get(0);
         
     }
    private void viewStormReports(ArrayList<StormReportInfo> list) {
        
       int numHrs = STORM_REPORT_OFFSET_SECS[stormReportHoursCb.getSelectedIndex()]/60/60;
        
        Viewer.getInstance().viewStormReports("View Storm Reports", numHrs, list, stormReportLayer.getAllStormReports(), this);
    }
    private void viewAllStormReports() {
//        ArrayList<StormReportInfo> allStormReports = stormReportLayer.getAllStormReports();
//        long curSecs = new Date().getTime()/1000;
       int numHrs = STORM_REPORT_OFFSET_SECS[stormReportHoursCb.getSelectedIndex()]/60/60;
//       int numReports = 0;
//       String text = "<html>";
//        long minTime = curSecs - numHrs*60*60;
//        for (int i=0; i<allStormReports.size(); i++) {
//             if (allStormReports.get(i).getEpoch() >= minTime) {
//                String line = allStormReports.get(i).getDate() + " " + allStormReports.get(i).getTime() + " " + allStormReports.get(i).getCity() +", "+ allStormReports.get(i).getState()+" <font color=red><b>" + allStormReports.get(i).getEventType() + " " + allStormReports.get(i).getEventMag()+"</b></font>";
//                text = text + line + "<br>&nbsp;&nbsp;&nbsp;&nbsp;"+allStormReports.get(i).getText()+"<br><br>";
//                numReports++;
//             }
//        }
//        if (numReports == 0) {
//            text = text +"No New Storm Reports In Last "+numHrs+" Hours";
//        }
//        text = text+"</html>";
        Viewer.getInstance().viewAllStormReports(stormReportLayer.getAllStormReports(), numHrs, this);
        //Viewer.getInstance().showAllStormReports();
    }
    private WarningData getSevereWeatherAlert(ArrayList<WarningData> list) {
         
         if  (list == null) return null;
         if (list.size() == 0) return null;
         for (int i=0; i<list.size(); i++) {
             //System.out.println("Test is event "+list.get(i).getEvent()+" is severe");
             if (list.get(i).getEvent().equals("Severe Thunderstorm Warning")) {
                 return list.get(i);
             }
             if (list.get(i).getEvent().equals("Flash Flood Warning")) {
                 return list.get(i);
             }
             if (list.get(i).getEvent().equals("Flood Warning")) {
                 return list.get(i);
             }
             if (list.get(i).getEvent().equals("Special Marine Warning")) {
                 return list.get(i);
             }
             if (list.get(i).getEvent().equals("Tornado Warning")) {
                 return list.get(i);
             }
             
         }
         return list.get(0);
         
     }
private void lookupAddress(JTextField addr) {
        double[] lonlat = Utilities.isLatLon(addr.getText());
         if (lonlat != null) {
             lat = lonlat[1];
             lon = lonlat[0];
             recenterMap(lat,lon,false);
             jComboBox1.setSelectedIndex(0);
         } else {
         Thread t = new Thread() {
             public void run() {
                  String dlink = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?SingleLine=";
                 dlink += addr.getText().replaceAll(" ", "+");
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
                 System.out.println("json="+json);
                 JSONObject obj = new JSONObject(json);
                 JSONArray candidates = obj.getJSONArray("candidates");
                 String x = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("x").toString();
                 String y = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("y").toString();
                 lat = Double.valueOf(y);
                 lon = Double.valueOf(x);
                 recenterMap(lat,lon,false);
                 jComboBox1.setSelectedIndex(0);
                 } catch (Exception ex) {
                     //ex.printStackTrace();
                     JOptionPane.showMessageDialog(WeatherMonitorRun.this,"<html><center><font size=5>Address lookup failed for<br><b>"+addr.getText()+"</b><br>Check internet connection!</font></center></html>");
                 }
                 //addr.setText("");
             }
         };
         t.start();
         }
     }

public boolean isValidDrag(Point releasePoint, int delta) {
    if (clickPoint == null) return false;
    double dist = Utilities.getDistanceBetweenTwoPoints(releasePoint.x,releasePoint.y,clickPoint.x,clickPoint.y);
    if (dist > delta) return true;
    return false;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        openGLButton = new javax.swing.JButton();
        viewStormReportsButton2 = new javax.swing.JButton();
        spcButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        zoom1Button = new javax.swing.JButton();
        zoom2Button = new javax.swing.JButton();
        zoom3Button = new javax.swing.JButton();
        zoom4Button = new javax.swing.JButton();
        zoom5Button = new javax.swing.JButton();
        zoom6Button = new javax.swing.JButton();
        zoom7Button = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        LocationEntry = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        stormReportsCb = new javax.swing.JCheckBox();
        stormReportHoursCb = new javax.swing.JComboBox<>();
        hideRadarCheckbox = new javax.swing.JCheckBox();
        RadarChooser = new javax.swing.JComboBox<>();
        timestamp = new javax.swing.JLabel();
        layerScrollPane = new javax.swing.JScrollPane();
        bottomPanel = new javax.swing.JPanel();
        advisoriesCb = new javax.swing.JCheckBox();
        radarLoopCb = new javax.swing.JCheckBox();
        fcstCb = new javax.swing.JCheckBox();
        obsCheckbox = new javax.swing.JCheckBox();
        warningsCb = new javax.swing.JCheckBox();
        samplingCheckbox = new javax.swing.JCheckBox();
        metarSiteCb = new javax.swing.JCheckBox();
        layeredPane = new javax.swing.JLayeredPane();
        infoPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        radarLegendLabel = new javax.swing.JLabel();
        waitIcon = new javax.swing.JLabel();
        backgroundPanel = new javax.swing.JPanel();
        mapBgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Weather Monitor v1.0");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jLabel3.setText("   ");
        jPanel2.add(jLabel3);

        jPanel3.add(jPanel2, java.awt.BorderLayout.EAST);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/format-justify-fill.png"))); // NOI18N
        jButton2.setToolTipText("");
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.setMaximumSize(new java.awt.Dimension(48, 48));
        jButton2.setMinimumSize(new java.awt.Dimension(48, 48));
        jButton2.setPreferredSize(new java.awt.Dimension(48, 48));
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel5.add(jButton2);

        openGLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/GL_off.png"))); // NOI18N
        openGLButton.setToolTipText("Enable Graphics Acceleration");
        openGLButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        openGLButton.setMaximumSize(new java.awt.Dimension(48, 48));
        openGLButton.setMinimumSize(new java.awt.Dimension(48, 48));
        openGLButton.setPreferredSize(new java.awt.Dimension(48, 48));
        openGLButton.addActionListener(this::openGLButtonActionPerformed);
        jPanel5.add(openGLButton);

        viewStormReportsButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/quetion.png"))); // NOI18N
        viewStormReportsButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        viewStormReportsButton2.setMaximumSize(new java.awt.Dimension(48, 48));
        viewStormReportsButton2.setMinimumSize(new java.awt.Dimension(48, 48));
        viewStormReportsButton2.setPreferredSize(new java.awt.Dimension(48, 48));
        viewStormReportsButton2.addActionListener(this::viewStormReportsButton2ActionPerformed);
        jPanel5.add(viewStormReportsButton2);

        spcButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/ThunderStorm.png"))); // NOI18N
        spcButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        spcButton.setMaximumSize(new java.awt.Dimension(48, 48));
        spcButton.setMinimumSize(new java.awt.Dimension(48, 48));
        spcButton.setPreferredSize(new java.awt.Dimension(48, 48));
        spcButton.addActionListener(this::spcButtonActionPerformed);
        jPanel5.add(spcButton);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/refresh.png"))); // NOI18N
        refreshButton.setToolTipText("Force Data Refresh");
        refreshButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        refreshButton.setMaximumSize(new java.awt.Dimension(48, 48));
        refreshButton.setMinimumSize(new java.awt.Dimension(48, 48));
        refreshButton.setPreferredSize(new java.awt.Dimension(48, 48));
        refreshButton.addActionListener(this::refreshButtonActionPerformed);
        jPanel5.add(refreshButton);

        jLabel5.setText("   ");
        jPanel5.add(jLabel5);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/zoom-out.png"))); // NOI18N
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton3.setMaximumSize(new java.awt.Dimension(48, 48));
        jButton3.setMinimumSize(new java.awt.Dimension(48, 48));
        jButton3.setPreferredSize(new java.awt.Dimension(48, 48));
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel5.add(jButton3);

        zoom1Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom1Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/1.png"))); // NOI18N
        zoom1Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom1Button.addActionListener(this::zoom1ButtonActionPerformed);
        jPanel5.add(zoom1Button);

        zoom2Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom2Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/2.png"))); // NOI18N
        zoom2Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom2Button.addActionListener(this::zoom2ButtonActionPerformed);
        jPanel5.add(zoom2Button);

        zoom3Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/3.png"))); // NOI18N
        zoom3Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom3Button.addActionListener(this::zoom3ButtonActionPerformed);
        jPanel5.add(zoom3Button);

        zoom4Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/4.png"))); // NOI18N
        zoom4Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom4Button.addActionListener(this::zoom4ButtonActionPerformed);
        jPanel5.add(zoom4Button);

        zoom5Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom5Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/5.png"))); // NOI18N
        zoom5Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom5Button.addActionListener(this::zoom5ButtonActionPerformed);
        jPanel5.add(zoom5Button);

        zoom6Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom6Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/6.png"))); // NOI18N
        zoom6Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom6Button.addActionListener(this::zoom6ButtonActionPerformed);
        jPanel5.add(zoom6Button);

        zoom7Button.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        zoom7Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/7.png"))); // NOI18N
        zoom7Button.setPreferredSize(new java.awt.Dimension(15, 40));
        zoom7Button.addActionListener(this::zoom7ButtonActionPerformed);
        jPanel5.add(zoom7Button);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/zoom-in.png"))); // NOI18N
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton4.setMaximumSize(new java.awt.Dimension(48, 48));
        jButton4.setMinimumSize(new java.awt.Dimension(48, 48));
        jButton4.setPreferredSize(new java.awt.Dimension(48, 48));
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel5.add(jButton4);

        jLabel2.setText("   ");
        jPanel5.add(jLabel2);

        jPanel3.add(jPanel5, java.awt.BorderLayout.WEST);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new java.awt.BorderLayout());

        LocationEntry.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        LocationEntry.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        LocationEntry.setText("Your Location");
        LocationEntry.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        LocationEntry.setMaximumSize(new java.awt.Dimension(174, 35));
        LocationEntry.setMinimumSize(new java.awt.Dimension(174, 35));
        LocationEntry.setPreferredSize(new java.awt.Dimension(174, 35));
        LocationEntry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LocationEntryMouseClicked(evt);
            }
        });
        LocationEntry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LocationEntryKeyPressed(evt);
            }
        });
        jPanel6.add(LocationEntry, java.awt.BorderLayout.CENTER);

        jComboBox1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setFocusable(false);
        jComboBox1.addItemListener(this::jComboBox1ItemStateChanged);
        jPanel6.add(jComboBox1, java.awt.BorderLayout.WEST);

        jPanel3.add(jPanel6, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel1.setMaximumSize(new java.awt.Dimension(2147483647, 58));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 5));

        stormReportsCb.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        stormReportsCb.setSelected(true);
        stormReportsCb.setText("Storm Reports:");
        stormReportsCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        stormReportsCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        stormReportsCb.addActionListener(this::stormReportsCbActionPerformed);
        jPanel4.add(stormReportsCb);

        stormReportHoursCb.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        stormReportHoursCb.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "12Hr", "24Hr", "48Hr", "72Hr", "All " }));
        stormReportHoursCb.addItemListener(this::stormReportHoursCbItemStateChanged);
        jPanel4.add(stormReportHoursCb);

        hideRadarCheckbox.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        hideRadarCheckbox.setSelected(true);
        hideRadarCheckbox.setText("Radar:");
        hideRadarCheckbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        hideRadarCheckbox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        hideRadarCheckbox.addItemListener(this::hideRadarCheckboxItemStateChanged);
        jPanel4.add(hideRadarCheckbox);

        RadarChooser.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        RadarChooser.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        RadarChooser.addItemListener(this::RadarChooserItemStateChanged);
        RadarChooser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                RadarChooserMouseEntered(evt);
            }
        });
        jPanel4.add(RadarChooser);

        timestamp.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        timestamp.setText("...Loading Radar...");
        jPanel4.add(timestamp);

        jPanel1.add(jPanel4, java.awt.BorderLayout.EAST);

        bottomPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bottomPanelMouseExited(evt);
            }
        });
        bottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        advisoriesCb.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        advisoriesCb.setSelected(true);
        advisoriesCb.setText("Advisories");
        advisoriesCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        advisoriesCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        advisoriesCb.addActionListener(this::advisoriesCbActionPerformed);
        bottomPanel.add(advisoriesCb);

        radarLoopCb.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        radarLoopCb.setSelected(true);
        radarLoopCb.setText("Loop");
        radarLoopCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        radarLoopCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        radarLoopCb.addActionListener(this::radarLoopCbActionPerformed);
        bottomPanel.add(radarLoopCb);

        fcstCb.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        fcstCb.setSelected(true);
        fcstCb.setText("Forecast");
        fcstCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        fcstCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        fcstCb.addItemListener(this::fcstCbItemStateChanged);
        bottomPanel.add(fcstCb);

        obsCheckbox.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        obsCheckbox.setSelected(true);
        obsCheckbox.setText("Obs");
        obsCheckbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        obsCheckbox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        obsCheckbox.addActionListener(this::obsCheckboxActionPerformed);
        bottomPanel.add(obsCheckbox);

        warningsCb.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        warningsCb.setSelected(true);
        warningsCb.setText("Warnings");
        warningsCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        warningsCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        warningsCb.addActionListener(this::warningsCbActionPerformed);
        bottomPanel.add(warningsCb);

        samplingCheckbox.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        samplingCheckbox.setSelected(true);
        samplingCheckbox.setText("Sampling");
        samplingCheckbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        samplingCheckbox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        bottomPanel.add(samplingCheckbox);

        metarSiteCb.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        metarSiteCb.setText("METAR");
        metarSiteCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        metarSiteCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        metarSiteCb.addActionListener(this::metarSiteCbActionPerformed);
        bottomPanel.add(metarSiteCb);

        layerScrollPane.setViewportView(bottomPanel);

        jPanel1.add(layerScrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        layeredPane.setLayout(new javax.swing.OverlayLayout(layeredPane));

        infoPanel.setBackground(new java.awt.Color(255, 255, 255));
        infoPanel.setFocusable(false);
        infoPanel.setOpaque(false);
        infoPanel.setPreferredSize(new java.awt.Dimension(2000, 2000));
        infoPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                infoPanelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                infoPanelMouseMoved(evt);
            }
        });
        infoPanel.addMouseWheelListener(this::infoPanelMouseWheelMoved);
        infoPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                infoPanelMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                infoPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                infoPanelMouseReleased(evt);
            }
        });
        infoPanel.setLayout(null);

        infoLabel.setBackground(new java.awt.Color(0, 0, 0));
        infoLabel.setFont(new java.awt.Font("DejaVu Sans Mono", 1, 18)); // NOI18N
        infoLabel.setForeground(new java.awt.Color(255, 255, 255));
        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setText("jLabel2");
        infoLabel.setAlignmentX(0.5F);
        infoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        infoLabel.setFocusable(false);
        infoLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        infoLabel.setIconTextGap(2);
        infoLabel.setOpaque(true);
        infoLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        infoPanel.add(infoLabel);
        infoLabel.setBounds(0, 0, 120, 60);

        radarLegendLabel.setMaximumSize(new java.awt.Dimension(750, 45));
        radarLegendLabel.setMinimumSize(new java.awt.Dimension(750, 45));
        radarLegendLabel.setPreferredSize(new java.awt.Dimension(750, 45));
        infoPanel.add(radarLegendLabel);
        radarLegendLabel.setBounds(0, 0, 750, 45);

        waitIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/wait.png"))); // NOI18N
        infoPanel.add(waitIcon);
        waitIcon.setBounds(550, 60, 100, 100);

        layeredPane.add(infoPanel);

        backgroundPanel.setLayout(new java.awt.GridLayout(1, 0));
        backgroundPanel.add(mapBgLabel);

        layeredPane.add(backgroundPanel);

        getContentPane().add(layeredPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        WeatherMonitorSettings rp = WeatherMonitorSettings.getInstance();
        rp.setSettingsListener(this);
        rp.showDialog(this.lat, this.lon, this.LocationEntry.getText(),this);
    }//GEN-LAST:event_jButton2ActionPerformed
     
    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
 if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            //System.out.println("Combo box changed to index:"+this.jComboBox1.getSelectedIndex());
            int index = jComboBox1.getSelectedIndex();
            if (index > 0) {
                try {
                     this.LocationEntry.setText(favAddress[index]);
                    double longi = Double.valueOf(comboBoxLons[index]);
                    double lati = Double.valueOf(comboBoxLats[index]);
                    recenterMap(lati, longi,false);
                   
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(WeatherMonitorRun.this, "Error in setup check settings to ensure lat lon values are correct",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
       
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        zoomOut();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void zoom1ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom1ButtonActionPerformed
        setZoomLevel(0);
    }//GEN-LAST:event_zoom1ButtonActionPerformed

    private void zoom2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom2ButtonActionPerformed
        setZoomLevel(1);
    }//GEN-LAST:event_zoom2ButtonActionPerformed

    private void zoom3ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom3ButtonActionPerformed
        setZoomLevel(2);
    }//GEN-LAST:event_zoom3ButtonActionPerformed

    private void zoom4ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom4ButtonActionPerformed
        setZoomLevel(3);
    }//GEN-LAST:event_zoom4ButtonActionPerformed

    private void zoom5ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom5ButtonActionPerformed
        setZoomLevel(4);
    }//GEN-LAST:event_zoom5ButtonActionPerformed

    private void zoom6ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom6ButtonActionPerformed
        setZoomLevel(5);
    }//GEN-LAST:event_zoom6ButtonActionPerformed

    private void zoom7ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoom7ButtonActionPerformed
        setZoomLevel(6);
    }//GEN-LAST:event_zoom7ButtonActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        zoomIn();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void LocationEntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LocationEntryMouseClicked
        LocationEntry.selectAll();
    }//GEN-LAST:event_LocationEntryMouseClicked

    private void LocationEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LocationEntryKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            
            lookupAddress(LocationEntry);
        }
    }//GEN-LAST:event_LocationEntryKeyPressed
     private void mouseMoved(final int value,final int x, final int y) {
         
         if (this.samplingCheckbox.isSelected()) {
             Thread t = new Thread() {
                 public void run() {
                     try {
                         this.sleep(250);
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
                     if (mouseMoving == value) {
                         
                         getMapFeatures(x, y);

                     }
                 }
             };
             t.start();
         }
    }
    private void hideRadarCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hideRadarCheckboxItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            radarLayer.setVisible(true);
            radarLegendLabel.setVisible(true);
        } else {
           radarLayer.setVisible(false);
           radarLegendLabel.setVisible(false);
        }
    }//GEN-LAST:event_hideRadarCheckboxItemStateChanged
    
    private void infoPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoPanelMouseMoved
        if (fcstTable == null || wxAlertsTable == null) return;
        if (dragging ) return;
        int  x = evt.getX();
        int y = evt.getY();
        //System.out.println("Alert Table bounds="+this.wxAlertsTable.getBounds().toString());
        if (fcstTable.isVisible()) {
            int maxX = layeredPane.getWidth() - fcstTable.getWidth();
            //System.out.println("layeredPAne width="+layeredPane.getWidth()+" table w="+fcstTable.getWidth()+" maxX="+maxX);
            if (x > maxX) {
                mouseExited = true;
                infoLabel.setVisible(false);
                stormReports = null;
                nearestMetar = null;
                return;
            }
        }
        Rectangle rect = this.wxAlertsTable.getBounds();
        if (rect.contains(x,y)) {
            mouseExited = true;
            infoLabel.setVisible(false);
            stormReports = null;
                nearestMetar = null;
            return;
        }
        mouseExited = false;
        mouseMoving = mouseMoving + 1;
        mouseMoved(mouseMoving,x,y);
        infoLabel.setVisible(false);
        stormReports = null;
                nearestMetar = null;
    }//GEN-LAST:event_infoPanelMouseMoved

    private void infoPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoPanelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
           
            if (nearestMetar != null) {
                //nearestMetar.getStationName();
                MetarViewer.getInstance().centerViewer(nearestMetar.getStationName(), mapBgColor, this);
                metarLayer.download24HrsMetarData(nearestMetar.getStationId());
               
            } else if (stormReports != null) {
                viewStormReports(stormReports);
            } else {
                //lat lon returns
                double[] point = mapLayer.getMap().pixelToWorld(evt.getX(), evt.getY());
                ArrayList<WarningData> list = WxAlertsTable.getWeatherAlertsAt(point[0], point[1]);
                if (list != null) {
                    WarningData d = getSevereWeatherAlert(list);
                    System.out.println("There are " + list.size() + " alerts");
                    if (d == null) {

                        return;
                    }
                    Viewer v = Viewer.getInstance();
                    System.out.println("text="+d.getText());
                    String title = d.getEvent() + " Until " + d.getExpires();
                    String maxHailSize = d.getMaxHailSize();
                    String maxWindGust = d.getMaxWindGust();
                    v.viewWWA(title, d.getText(), maxHailSize, maxWindGust, this);
                } else {
                    System.out.println("WxAlert list is null");
                }
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
             double[] point = mapLayer.getMap().pixelToWorld(evt.getX(), evt.getY());
             blankScreen();
             recenterMap(point[0],point[1],true);
             //double p1 = Utilities.roundDouble(point[0]);
             //double p2 = Utilities.roundDouble(point[1]);
             //this.LocationEntry.setText(Double.toString(p1)+ " "+Double.toString(p2));
        }
    }//GEN-LAST:event_infoPanelMouseClicked

    private void infoPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_infoPanelMouseWheelMoved
        scrollAmount = scrollAmount + evt.getWheelRotation();
        scrollTimer.restart();
        scrollWheelX = evt.getX();
        scrollWheelY = evt.getY();
    }//GEN-LAST:event_infoPanelMouseWheelMoved

    private void bottomPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottomPanelMouseExited
        infoLabel.setVisible(false);
        stormReports = null;
        nearestMetar = null;
    }//GEN-LAST:event_bottomPanelMouseExited

    private void RadarChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_RadarChooserItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
//            if (RadarChooser.getSelectedItem().toString().equals("Edit Settings")) {
//                RadarSettings.getInstance().showDialog(this);
//            } else {
                int radarWmsUrlNumber = RadarChooser.getSelectedIndex();

                radarLayer.setRadarWmsTypeNumber(radarWmsUrlNumber);
               
            //}
        }
        
    }//GEN-LAST:event_RadarChooserItemStateChanged

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        resizeTimer.restart();
    }//GEN-LAST:event_formComponentResized

    private void fcstCbItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fcstCbItemStateChanged
        fcstTable.showForecastTable(fcstCb.isSelected());
        
    }//GEN-LAST:event_fcstCbItemStateChanged

    private void advisoriesCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advisoriesCbActionPerformed
        //this.defaultProductLayerVisible = advisoriesCheckbox.isSelected();
        this.alertLayer.setVisible(advisoriesCb.isSelected());
    }//GEN-LAST:event_advisoriesCbActionPerformed

    private void stormReportHoursCbItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stormReportHoursCbItemStateChanged
        int index = stormReportHoursCb.getSelectedIndex();
        //long time = new Date().getTime()/1000;
        stormReportLayer.setMinimumReportTimeOffsetSecs(STORM_REPORT_OFFSET_SECS[stormReportHoursCb.getSelectedIndex()]);

    }//GEN-LAST:event_stormReportHoursCbItemStateChanged

    private void stormReportsCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stormReportsCbActionPerformed
       if (stormReportsCb.isSelected()) {
           stormReportLayer.setVisible(true);
       } else {
           stormReportLayer.setVisible(false);
       }
    }//GEN-LAST:event_stormReportsCbActionPerformed

    private void warningsCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningsCbActionPerformed
        if (warningsCb.isSelected()) {
           warningLayer.setVisible(true);
       } else {
           warningLayer.setVisible(false);
       }
    }//GEN-LAST:event_warningsCbActionPerformed

    private void spcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spcButtonActionPerformed
        
        java.awt.EventQueue.invokeLater(() -> new SPCMapViewer(this).setVisible(true));
    }//GEN-LAST:event_spcButtonActionPerformed

    private void radarLoopCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radarLoopCbActionPerformed
        radarLayer.setLoop(radarLoopCb.isSelected());
    }//GEN-LAST:event_radarLoopCbActionPerformed

    private void obsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_obsCheckboxActionPerformed
        if (metarLayer != null) {
//            if (obsCheckbox.isSelected()) {
//                Timer.getInstance().restartTimer();
//                metarLayer.updateThePlot();
//            }
            metarLayer.setVisible(obsCheckbox.isSelected());
        }
    }//GEN-LAST:event_obsCheckboxActionPerformed

    private void infoPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoPanelMouseDragged
        //System.out.println("Mouse dragged from "+clickPoint.toString()+" to "+evt.getPoint().toString());
       if (clickPoint == null) return;
        
        if (dragging == false && isValidDrag(evt.getPoint(), 20)) {
             dragging = true;
             blankScreen();
            hideAllLayers();
            radarLayer.stopLoop();
            radarLayer.hideRadar(true);
            
            backgroundPanel.setBackground(mapBgColor);
            mapBgLabel.setIcon(null);
            BufferedImage panMapImage = Mapping.getInstance().getPanMapImage();
            int w = panMapImage.getWidth();
            int h = panMapImage.getHeight();
            int lw = layeredPane.getWidth();
            int lh = layeredPane.getHeight();
            int x = (w - lw) / 2;
            int y = (h - lh) / 2;

            panLayer.setBackground(outBgColor);
            panLayer.setXY(clickPoint);
            panLayer.setOffset(new Point(x, y));
            Graphics panGraphics = panMapImage.getGraphics();

            //panImg = new BufferedImage(layeredPane.getWidth(), layeredPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
            //Graphics g = panImg.getGraphics();
            BufferedImage mapFill = mapLayer.getMap().getMapBgImage();
            panGraphics.drawImage(mapFill, x, y, null);
            //AffineTransform translate = new AffineTransform(); 
            panGraphics.translate(x, y);

            if (alertLayer.isVisible()) {
                alertLayer.paint(panGraphics);
            }
            if (radarLayer.isVisible()) {
                Image radarImg = radarLayer.getLatestRadarImage();
                panGraphics.drawImage(radarImg, 0, 0, null);
            }
            mapLayer.paint(panGraphics);
            if (warningLayer.isVisible()) {
                warningLayer.paint(panGraphics);
            }
            if (metarLayer.isVisible()) {
                metarLayer.showMetarTimeStamp(false);
                metarLayer.paint(panGraphics);
                metarLayer.showMetarTimeStamp(true);
            }
            if (stormReportLayer.isVisible()) {
                stormReportLayer.paint(panGraphics);
            }
            panGraphics.dispose();
            panLayer.setPanImage(panMapImage);

            //mapBgLabel.setIcon(null);
           
           
            panLayer.setVisible(true);

        } else if (dragging == true) {
        backgroundPanel.repaint();
        //panLayer.setV();
       Point p = evt.getPoint();
      
       //mapLayer.setVisible(false);
       int x = p.x - clickPoint.x;
       int y = p.y-clickPoint.y;
       //int w= panImg.getWidth();
      // int h = panImg.getHeight();
       //mapBgLabel.setIcon(null);
       
        panLayer.setXY(new Point(x,y));
        }
        
       //mapBgLabel.getGraphics().drawImage(panImg,x,y,w,h,null);
    }//GEN-LAST:event_infoPanelMouseDragged

    private void infoPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoPanelMousePressed
        if (evt.getButton() == MouseEvent.BUTTON1)  {
         
           clickPoint = evt.getPoint();
         
        } else {
            clickPoint = null;
        }
    }//GEN-LAST:event_infoPanelMousePressed

    private void infoPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_infoPanelMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON1) {
            
             releasePoint = evt.getPoint();
            if (isValidDrag(releasePoint,20)) {
                
                radarLayer.setLoop(false);
                radarLayer.hideRadar(true);
                radarLayer.clearRadar();
                int dX = releasePoint.x - clickPoint.x;
                int dY = releasePoint.y - clickPoint.y;
                panMap(dX, dY);
                 infoLabel.setVisible(true);
            showAllLayers();
            radarLayer.setLoop(radarLoopCb.isSelected());
            panLayer.setVisible(false);
            panLayer.flush();
            clickPoint = null;
                
            } else {
                //backgroundPanel.setBackground(outBgColor);
                //mapBgLabel.setIcon(new ImageIcon(mapLayer.getMap().getMapBgImage()));
                //mapBgLabel.setBackground(outBgColor);
               // radarLayer.hideRadar(false);
              
                if (dragging == false) infoPanelMouseClicked(evt);
            }
         
           dragging = false;
        
        } 
        
    }//GEN-LAST:event_infoPanelMouseReleased

    private void openGLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openGLButtonActionPerformed
        String value = "OFF";
        if (useOpenGL == true) {
            useOpenGL = false;
            openGLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/GL_off.png")));
            Properties props = Utilities.readPrefs();
            props.put("OpenGL","0");
            Utilities.savePrefs(props, "/Maps/WeatherMonitorSettings.properties");
        } else {
            useOpenGL = true;
            value = "ON";
            openGLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/GL_on.png")));
            Properties props = Utilities.readPrefs();
            props.put("OpenGL","1");
            Utilities.savePrefs(props, "/Maps/WeatherMonitorSettings.properties");
        }
         JOptionPane.showMessageDialog(this,"<html><center><font size=5>Graphics hardware acceleration is set to<b> "+value+"</b><br>you must restart WeatherMonitor when changing graphics acceleration settings</font></center></html>");
    }//GEN-LAST:event_openGLButtonActionPerformed

    private void RadarChooserMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RadarChooserMouseEntered
        if (RadarChooser.getItemCount() == 1) {
            // refresh it something went wrong like lost internet and
            //  the menu was unable to populate
            System.out.println("Repopulate RadarChooser pulldown");
            radarLayer.getRadar().loadWmsRadarLayers(lat, lon);
        }
    }//GEN-LAST:event_RadarChooserMouseEntered

    private void viewStormReportsButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewStormReportsButton2ActionPerformed
        
        String text = "WeatherMonitor V"+Utilities.getCurrentVersion()+ "(Updated:"+Utilities.getLastUpdated()+")\n";
        text+=Utilities.readPropFileFromJar("/weathermonitor/help/help.txt", this);
        text+="\nChange Log\n"+Utilities.getChangeLog();
        Viewer.getInstance().viewText("Help", text, this);
    }//GEN-LAST:event_viewStormReportsButton2ActionPerformed

    private void metarSiteCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metarSiteCbActionPerformed
        
             metarLayer.setShowMetarSites(metarSiteCb.isSelected());
         
    }//GEN-LAST:event_metarSiteCbActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refresh();
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        //</editor-fold>

        /* Create and display the form */
        Properties props = Utilities.readPrefs();
        if (props.containsKey("OpenGL")) {
            String v= props.getProperty("OpenGL");
            if (v.equals("true") || v.equals("1")) {
                System.setProperty("sun.java2d.opengl", "true");
            } 
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WeatherMonitorRun wmr = new WeatherMonitorRun();
                String sImageFileName = "/weathermonitor/icons/WeatherMonitor.png";
                URL urlImageFileName = getClass().getResource(sImageFileName);

                if (urlImageFileName == null) {
                    System.out.println("urlImageFileName: " + urlImageFileName + " Not Found.");
                } else {
                    ImageIcon oImageIcon = new ImageIcon(getClass().getResource(sImageFileName));
                    System.out.println("urlImageFileName: " + urlImageFileName);
                    wmr.setIconImage(oImageIcon.getImage());
                }
                
                        
                //Image img = new javax.swing.ImageIcon("/home/greg/Programs/JackStop/StopJack.png").getImage();
                //this.setIconImage(img);
                // this.setIconImage(Toolkit.getDefaultToolkit().getImage("/home/greg/Programs/JackStop/StopJack.png"));
                wmr.setTitle("Weather Monitor V"+Utilities.getCurrentVersion());
                wmr.setVisible(true);
            }
        });
        
        
        
        
        
        
//        
//        Properties props = Utilities.readPrefs();
//        if (props.containsKey("OpenGL")) {
//            String v= props.getProperty("OpenGL");
//            if (v.equals("true") || v.equals("1")) {
//                System.setProperty("sun.java2d.opengl", "true");
//            } 
//        }
//        
//        WeatherMonitorRun tml = new WeatherMonitorRun();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField LocationEntry;
    private javax.swing.JComboBox<String> RadarChooser;
    public static javax.swing.JCheckBox advisoriesCb;
    public static javax.swing.JPanel backgroundPanel;
    public static javax.swing.JPanel bottomPanel;
    public static javax.swing.JCheckBox fcstCb;
    public static javax.swing.JCheckBox hideRadarCheckbox;
    public static javax.swing.JLabel infoLabel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane layerScrollPane;
    private javax.swing.JLayeredPane layeredPane;
    public static javax.swing.JLabel mapBgLabel;
    private javax.swing.JCheckBox metarSiteCb;
    public static javax.swing.JCheckBox obsCheckbox;
    private javax.swing.JButton openGLButton;
    public static javax.swing.JLabel radarLegendLabel;
    public static javax.swing.JCheckBox radarLoopCb;
    public static javax.swing.JButton refreshButton;
    private javax.swing.JCheckBox samplingCheckbox;
    public static javax.swing.JButton spcButton;
    private javax.swing.JComboBox<String> stormReportHoursCb;
    public static javax.swing.JCheckBox stormReportsCb;
    private javax.swing.JLabel timestamp;
    public static javax.swing.JButton viewStormReportsButton2;
    private javax.swing.JLabel waitIcon;
    public static javax.swing.JCheckBox warningsCb;
    private javax.swing.JButton zoom1Button;
    private javax.swing.JButton zoom2Button;
    private javax.swing.JButton zoom3Button;
    private javax.swing.JButton zoom4Button;
    private javax.swing.JButton zoom5Button;
    private javax.swing.JButton zoom6Button;
    private javax.swing.JButton zoom7Button;
    // End of variables declaration//GEN-END:variables

    public void radarSettingsChanged() {
        RadarChooser.removeAllItems();
        WmsRadarLayer wrl = radarLayer.getRadar().readDefaultMosaicRadarSettings();
        String[] radars = radarLayer.getRadar().getDescrRadarNames();
        
        this.RadarChooser.setModel(new javax.swing.DefaultComboBoxModel<>(radars));
        //this.RadarChooser.setSelectedIndex(radarLayer.getRadar().getRadarWmsTypeNumber());
        this.radarLayer.getRadar().setRadarWmsTypeNumber(0);
//        radarLayer.getRadar().readRadarSettings();
//        String[] radars = radarLayer.getRadar().getDescrRadarNames();
//        
//        this.RadarChooser.setModel(new javax.swing.DefaultComboBoxModel<>(radars));
//        RadarChooser.addItem("Edit Settings");
        
    }
    
    @Override
    public void settingsChanged(Properties props) {
        //metarLayer.setUseCache(true);
        if (props.containsKey("InterstateColor")) {
            String[] arr = props.getProperty("InterstateColor").split(",");
            Color interstateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            mapLayer.getMap().setInterstateColor(interstateColor);
        }
        if (props.containsKey("StateColor")) {
            String[] arr = props.getProperty("StateColor").split(",");
            Color stateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            mapLayer.getMap().setStateColor(stateColor);
        }
        if (props.containsKey("RoadColor")) {
            String[] arr = props.getProperty("RoadColor").split(",");
            Color roadColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            mapLayer.getMap().setRoadColor(roadColor);
        }
        if (props.containsKey("OutBgColor")) {
            String[] arr = props.getProperty("OutBgColor").split(",");
             outBgColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            this.backgroundPanel.setBackground(outBgColor);            
             //this.mapBg.setIcon(new ImageIcon(mapLayer.getMap().getMapBgImage()));
        }
        
        if (props.containsKey("City1Font") 
                && props.containsKey("City2Font") 
                && props.containsKey("City3Font")
                && props.containsKey("City4Font")) {
            String[] arr = props.getProperty("City1Font").split(",");
             int bold = Integer.valueOf(arr[2]);
            Font city1Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City2Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city2Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City3Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city3Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            arr = props.getProperty("City4Font").split(",");
            bold = Integer.valueOf(arr[2]);
            Font city4Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            Font[] fontArr = {city1Font,city2Font,city3Font,city4Font};
            mapLayer.getMap().setCityFonts(fontArr);
        }
        
        boolean needsRepaint = updateSettings(props,false);
        if  (needsRepaint == true) {
           
             refresh();
             
        }
        
        
    }

    @Override
    public void networkUp(boolean value) {
        
        if (value == false) {
            networkUp = false;
            this.timestamp.setText("Internet Down ");
            this.timestamp.setForeground(Color.RED);
        } else if (value == true) {
            
            networkUp = true;
            this.timestamp.setText("...Loading Radar... ");
            this.timestamp.setForeground(Color.BLACK);
           
        }
    }


    @Override
    public void radarImageAdded(int rid, Point2D centerPoint, boolean flush, Image img, String time, long epochSecs, int layerNumber, int num) {
        
    }

    @Override
    public void radarLengendAdded(Image img, String layer_name) {//750 x 45
        if (img != null) {
              radarLegendLabel.setIcon(new ImageIcon(img));
              String layer = layer_name;
              if (layer_name.matches("k[a-z][a-z][a-z]_bdsa")) {
                  layer="bdsa";
              }
              if (layer_name.matches("k[a-z][a-z][a-z]_boha")) {
                  layer="boha";
              }
              if (layer_name.matches("k[a-z][a-z][a-z]_sr_bvel")) {
                  layer="sr_bvel";
              }
               if (layer_name.matches("k[a-z][a-z][a-z]_bdhc")) {
                  layer="bdhc";
                  if (enabledTooltips) radarLegendLabel.setToolTipText("<html>BI:Insects<br>GC:Clutter<br>IC:Ice Crystals<br>DS:Dry Snow<br>WS:Wet Snow<br>RA:Rain<br>HR:Heavy Rain<br>BD:Big Drops<br>GR:Ice Pellets<br>HA:Hail<br>LH:Large Hail<br>GH:Giant Hail</html>");
              }
              //System.out.println("get radar legend sampling for layer:"+layer);
              Properties p = Utilities.getRadarLegendScale(layer);
             if (p != null) {
                 try {
                     String[] values = p.getProperty("scale").split(",");
                    
                     
                     int startPixel = 0;
                     if (p.containsKey("startPixel")) {
                         startPixel = Integer.valueOf(p.getProperty("startPixel"));
                     }
                     String unit = p.getProperty("unit").toString();
                    // System.out.println("unit for layer "+layer+" = "+unit);
                     if (unit.contains("AbsoluteValues")) {
                         //System.out.println("Radar legend scale is type AbsoluteValues");
                          ArrayList<String> scale = new ArrayList<>();
                          for (int i = 0; i < values.length; i++) {
                              //System.out.println("scale.add"+values[i]);
                             scale.add(values[i]);
                          }
                          radarLegendHash = Utilities.scanRadarLegend( img,scale);
                     } else {
                          Double maxValue = Double.parseDouble(p.getProperty("maxValue"));
                   
                          Integer decimalPlaces = Integer.valueOf(p.getProperty("decimalPlaces"));
                          ArrayList<Double> scale = new ArrayList<>();
                          for (int i = 0; i < values.length; i++) {
                             scale.add(Double.parseDouble(values[i]));
                          }
                          radarLegendHash = Utilities.scanRadarLegend(img, scale, maxValue,decimalPlaces,unit,startPixel);
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     radarLegendHash = null;
                 }
            } else {
                 radarLegendHash = null;
             }
          } else {
            radarLegendHash = null;
        }
    }
    
    @Override
    public void radarLayersChanged(String[] radarNames) {
        if (radarNames.length > 0) {
           this.RadarChooser.setModel(new javax.swing.DefaultComboBoxModel<>(radarNames));
           this.RadarChooser.setSelectedIndex(radarLayer.getRadar().getRadarWmsTypeNumber());
        }
    }
    
    @Override
    public void bboxChanged(String bbox, int w, int h, double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
       System.out.println("Bbox changed in WeatherMontirRun");
        //if (this.bbox == null || this.bbox.equals(bbox) == false) {
            System.out.println("Nearest city1="+nearestCity);
            if (this.lat != lat || this.lon != lon) {
                //String nearestCity = Mapping.getInstance().getNearestCity();
                System.out.println("Nearest city2="+nearestCity);
                if (nearestCity != null && Utilities.isAnAddress(LocationEntry.getText()) == false) {
                    
                     LocationEntry.setText(nearestCity);
                } 
             } 
             
             this.timestamp.setText("...Loading Radar...");
             
             
        //}
        this.bbox = bbox;
        this.lat = lat;
        this.lon = lon;
        System.out.println("map layer visible="+mapLayer.isVisible());
        mapLayer.setVisible(true);
        mapLayer.repaint();
        BufferedImage img = mapLayer.getMap().getMapBgImage();
        if (img != null) {
          mapBgLabel.setSize(new Dimension(w,h));
          System.out.println("Setting map bg image now");
          mapBgLabel.setIcon(new ImageIcon(img));
          mapBgLabel.setVisible(true);
          backgroundPanel.setBackground(outBgColor);
        }
          // dont remove the following showAllLayers       
          showAllLayers();
    }

    

    @Override
    public double getID() {
        return id;
    }
}
