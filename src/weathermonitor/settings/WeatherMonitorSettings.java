/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package weathermonitor.settings;

import weathermonitor.listeners.SettingsListener;
import weathermonitor.settings.RadarSettings;
import utilities.FontChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.WeatherMonitorRun;
import weathermonitor.layers.WxAlertsTable;

/**
 *
 * @author greg
 */
public class WeatherMonitorSettings extends javax.swing.JFrame {
    private String[] revert1 = {"31.2404","-100.4733","2750 Venado Dr., Christoval, TX"};
    private String[] revert2 = {"Cabinet Shop","31.44008","-100.4452","710 Knickerbocker Rd., San Angelo, TX"};
    private String[] revert3=  {"Garys House","31.19143","-100.4820","19895 Cougar Trl., Christoval, TX"};
    private String[] revert4 = {"The Farm","29.96456","-96.92465","4428 Egypt Rd., La Grange, TX"};
    private SettingsListener settingsListener = null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WeatherMonitorSettings.class.getName());
    private Properties props = null;
    private String currentLocation = "San Angelo, TX";
    public static int ALL_WIND_SPEEDS = 0;
    public static int WIND_SPEEDS_GT_10 = 1;
    public static int WIND_SPEEDS_GT_20 = 2;
    private int sliderValue = 50;
    public static String lowObDist = "5";
    private String midObDist = "7";
    private String highObDist = "9";
    private double currentLat = 31.0;
    private double currentLon = -101.0;
    private WeatherMonitorRun program = null;
    private static WeatherMonitorSettings rp = null;
   /**
     * Creates new form RadarPreferences
     */
    public static WeatherMonitorSettings getInstance() {
        if (rp == null) rp = new WeatherMonitorSettings();
        return rp;
    }
    private WeatherMonitorSettings() {
        
        initComponents();
        if (WeatherMonitorRun.OPEN_GL == null || WeatherMonitorRun.OPEN_GL.equals("false")) {
            this.fpsCombo.setSelectedIndex(1);
            this.fpsCombo.setEnabled(true);
            this.fpsCombo.setToolTipText("Without OpenGL enabled 2fps is the limit for radar");
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilyNames = ge.getAvailableFontFamilyNames();
        this.cityFont1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(fontFamilyNames));
        this.cityFont2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(fontFamilyNames));
        this.cityFont3Combo.setModel(new javax.swing.DefaultComboBoxModel<>(fontFamilyNames));
        this.cityFont4Combo.setModel(new javax.swing.DefaultComboBoxModel<>(fontFamilyNames));
        this.prodTableFontButton.setFont(WxAlertsTable.tableFont);
        readProps();
    }
    public void showDialog(double currentLat, double currentLon, String currentLocation, WeatherMonitorRun parent) {
        this.currentLon  = currentLon;
        this.currentLat = currentLat;
        this.currentLocation = currentLocation;
        this.program = parent;
        readProps();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.getWidth();
        int h = this.getHeight();
        
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
         pack();
          java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
    }
    public void setSettingsListener(SettingsListener l) {
        this.settingsListener = l;
    }
    private void readProps() {
        //boolean openGlEnabled = Utilities.getOpenGL();
        //this.openGLCb.setSelected(openGlEnabled);
        try {
            this.props = Utilities.readPrefs();
            if (props == null) {
                return;
            }
            if (props.containsKey("ToolTipsEnabled")) {
                String value = props.getProperty("ToolTipsEnabled");
                if (value.equals("true") || value.equals("1")) {
                    tooltipsCb.setSelected(true);
                } else {
                    tooltipsCb.setSelected(false);
                }
            }
            if (props.containsKey("ShowWindSpeedsOnArrows")) {
                 String value = props.getProperty("ShowWindSpeedsOnArrows");
                if (value.equals("true") || value.equals("1")) {
                    windSpeedsOnArrowsCb.setSelected(true);
                    windSpeedOnArrowsCombo.setEnabled(true);
      
                } else {
                    windSpeedsOnArrowsCb.setSelected(false);
                    windSpeedOnArrowsCombo.setEnabled(false);
                }
            }
             if (props.containsKey("ShowWindSpeedsOnArrowsValues")) {
                String value = props.getProperty("ShowWindSpeedsOnArrowsValues");
                int index = Integer.parseInt(value);
                this.windSpeedOnArrowsCombo.setSelectedIndex(index);
            }
            if (props.containsKey("SliderValue")) {
                String value = props.getProperty("SliderValue");
                int v = Integer.parseInt(value);
                jSlider1.setValue(v);
                if (props.containsKey("LowObDistance") &&
                    props.containsKey("MidObDistance") &&
                    props.containsKey("HighObDistance")) {
                    this.lowObDist = props.getProperty("LowObDistance");
                    this.midObDist = props.getProperty("MidObDistance");
                    this.highObDist = props.getProperty("HighObDistance");
                }
            }
            
               
                 if (props.containsKey("ObsFont")) {
                    int index = Integer.parseInt(props.getProperty("ObsFont"));
                    if (index >=0 && index <= 3) {
                        obsFontCombo.setSelectedIndex(index);
                    } else {
                        obsFontCombo.setSelectedIndex(3);
                    }
                 } else {
                     obsFontCombo.setSelectedIndex(3);
                 }
            
        
         
        if (props.containsKey("X")) this.startUpX.setText(props.getProperty("X"));
        if (props.containsKey("Y")) this.startUpY.setText(props.getProperty("Y"));
        if (props.containsKey("WIDTH")) this.startUpWidth.setText(props.getProperty("WIDTH"));
        if (props.containsKey("HEIGHT")) this.startUpHeight.setText(props.getProperty("HEIGHT"));
       
        if (props.containsKey("FPS")) this.fpsCombo.setSelectedItem(props.getProperty("FPS"));
        if (props.containsKey("Lat")) latField.setText(props.getProperty("Lat"));
        if (props.containsKey("Lon"))this.lonField.setText(props.getProperty("Lon"));
       if (props.containsKey("ZoomLevel")) this.jComboBox1.setSelectedItem(props.getProperty("ZoomLevel"));
        if (props.containsKey("Fav1Name"))this.fav1NameEntry.setText(props.getProperty("Fav1Name"));
        if (props.containsKey("Fav2Name")) this.fav2NameEntry.setText(props.getProperty("Fav2Name"));
        if (props.containsKey("Fav3Name")) this.fav3NameEntry.setText(props.getProperty("Fav3Name"));
         if (props.containsKey("Fav1Lat"))this.lat1Entry.setText(props.getProperty("Fav1Lat"));
         if (props.containsKey("Fav2Lat")) this.lat2Entry.setText(props.getProperty("Fav2Lat"));
        if (props.containsKey("Fav3Lat")) this.lat3Entry.setText(props.getProperty("Fav3Lat"));
        if (props.containsKey("Fav1Lon"))  this.lon1Entry.setText(props.getProperty("Fav1Lon"));
        if (props.containsKey("Fav2Lon"))  this.lon2Entry.setText(props.getProperty("Fav2Lon"));
       if (props.containsKey("Fav3Lon"))  this.lon3Entry.setText(props.getProperty("Fav3Lon"));
       if (props.containsKey("Fav1Address"))this.address1Entry.setText(props.getProperty("Fav1Address"));
       if (props.containsKey("Fav2Address"))this.address2Entry.setText(props.getProperty("Fav2Address"));
       if (props.containsKey("Fav3Address"))this.address3Entry.setText(props.getProperty("Fav3Address"));
        if (props.containsKey("YourLocation"))this.addressEntry.setText(props.getProperty("YourLocation"));
        if (props.containsKey("RoadColor")) {
            String[] arr = props.getProperty("RoadColor").split(",");
            Color roadColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            roadLabel.setForeground(Utilities.getFgColor(roadColor));
            roadLabel.setBackground(roadColor);
        }
        if (props.containsKey("StateColor")) {
            String[] arr = props.getProperty("StateColor").split(",");
            Color stateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            stateLabel.setForeground(Utilities.getFgColor(stateColor));
            stateLabel.setBackground(stateColor);
        }
        if (props.containsKey("InterstateColor")) {
            String[] arr = props.getProperty("InterstateColor").split(",");
            Color interstateColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            interstateLabel.setForeground(Utilities.getFgColor(interstateColor));
            interstateLabel.setBackground(interstateColor);
        }
         if (props.containsKey("OutBgColor")) {
            String[] arr = props.getProperty("OutBgColor").split(",");
            Color outColor = new Color(Integer.valueOf(arr[0]),Integer.valueOf(arr[1]),Integer.valueOf(arr[2]));
            outBgColor.setForeground(Utilities.getFgColor(outColor));
            outBgColor.setBackground(outColor);
        }
          if  (props.containsKey("ApplicationFont")) {
            String[] arr = props.getProperty("ApplicationFont").split(",");
            int bold = Integer.valueOf(arr[2]);
           
            Font f = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.applicationFont.setFont(f);
         }
         if  (props.containsKey("ProdTableFont")) {
              String[] arr = props.getProperty("ProdTableFont").split(",");
            int bold = Integer.valueOf(arr[2]);
           
            Font f = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.prodTableFontButton.setFont(f);
         }
            if (props.containsKey("ProdTableBg")) {
                String[] arr = props.getProperty("ProdTableBg").split(",");
                Color color = new Color(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]));
                prodTableBg.setForeground(Utilities.getFgColor(color));
                prodTableBg.setBackground(color);
                prodTableFontButton.setBackground(color);
            }
            if (props.containsKey("MapBgColor")) {
                String[] arr = props.getProperty("MapBgColor").split(",");
                Color mapBgColor = new Color(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]));
                mapBgColorLabel.setForeground(Utilities.getFgColor(mapBgColor));
                mapBgColorLabel.setBackground(mapBgColor);
            }
        if (props.containsKey("City1Font")) {
            String[] arr = props.getProperty("City1Font").split(",");
            int bold = Integer.valueOf(arr[2]);
            if (bold == Font.BOLD) {
                cityFont1Bold.setSelected(true);
            } else {
                cityFont1Bold.setSelected(false);
            }
            Font city1Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.cityFont1Combo.setSelectedItem(arr[0]);
            this.cityFont1SizeCombo.setSelectedItem(arr[1]);
            this.cityFont1Label.setFont(city1Font);
        }
         if (props.containsKey("City2Font")) {
            String[] arr = props.getProperty("City2Font").split(",");
            int bold = Integer.valueOf(arr[2]);
            if (bold == Font.BOLD) {
                this.cityFont2Bold.setSelected(true);
            } else {
                this.cityFont2Bold.setSelected(false);
            }
            Font city2Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.cityFont2Combo.setSelectedItem(arr[0]);
            this.cityFont2SizeCombo.setSelectedItem(arr[1]);
           this.cityFont2Label.setFont(city2Font);
        }
          if (props.containsKey("City3Font")) {
            String[] arr = props.getProperty("City3Font").split(",");
            int bold = Integer.valueOf(arr[2]);
            if (bold == Font.BOLD) {
                this.cityFont3Bold.setSelected(true);
            } else {
                this.cityFont3Bold.setSelected(false);
            }
            Font city3Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.cityFont3Combo.setSelectedItem(arr[0]);
            this.cityFont3SizeCombo.setSelectedItem(arr[1]);
            this.cityFont3Label.setFont(city3Font);
        }
           if (props.containsKey("City4Font")) {
            String[] arr = props.getProperty("City4Font").split(",");
            int bold = Integer.valueOf(arr[2]);
            if (bold == Font.BOLD) {
                this.cityFont4Bold.setSelected(true);
            } else {
                this.cityFont4Bold.setSelected(false);
            }
            Font city4Font = new Font(arr[0],bold,Integer.valueOf(arr[1]));
            this.cityFont4Combo.setSelectedItem(arr[0]);
            this.cityFont4SizeCombo.setSelectedItem(arr[1]);
            
            this.cityFont4Label.setFont(city4Font);
        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
private void restoreFactorySettings() {
    String[] filesToRestore = {
        "AK_MetarData.properties",
        "AL_MetarData.properties",
        "AR_MetarData.properties",
        "AZ_MetarData.properties",
        "bdhc.properties",
        "bdsa.properties",
        "boha.properties",
        "CA_MetarData.properties",
        "CO_MetarData.properties",
        "conus_bref_qcd.properties",
        "CT_MetarData.properties",
        "DC_MetarData.properties",
        "DE_MetarData.properties",
        "FL_MetarData.properties",
        "GA_MetarData.properties",
        "HI_MetarData.properties",
        "IA_MetarData.properties",
        "ID_MetarData.properties",
        "IL_MetarData.properties",
        "IN_MetarData.properties",
        "KS_MetarData.properties",
        "KY_MetarData.properties",
        "LA_MetarData.properties",
        "MA_MetarData.properties",
        "MD_MetarData.properties",
        "ME_MetarData.properties",
        "MI_MetarData.properties",
        "MN_MetarData.properties",
        "MO_MetarData.properties",
        "MS_MetarData.properties",
        "MT_MetarData.properties",
        "NC_MetarData.properties",
        "ND_MetarData.properties",
        "NE_MetarData.properties",
        "NH_MetarData.properties",
        "NJ_MetarData.properties",
        "NM_MetarData.properties",
        "NV_MetarData.properties",
        "NY_MetarData.properties",
        "OfficeIds.properties",
        "OH_MetarData.properties",
        "OK_MetarData.properties",
        "PA_MetarData.properties",
        "PR_MetarData.properties",
        "product.properties",
        "radarSettings.properties",
        "RadarStations.properties",
        "RI_MetarData.properties",
        "SC_MetarData.properties",
        "SD_MetarData.properties",
        "SPCMapSettings.properties",
        "sr_bvel.properties",
        "StormReportSettings.properties",
        "TN_MetarData.properties",
        "TX_MetarData.properties",
        "UT_MetarData.properties",
        "VA_MetarData.properties",
        "VT_MetarData.properties",
        "WA_MetarData.properties",
        "WeatherMonitorSettings.properties",
        "WI_MetarData.properties",
        "WV_MetarData.properties",
        "WY_MetarData.properties"
    };
        for (int i=0; i<filesToRestore.length; i++) {
            String text = Utilities.readPropFileFromJar("/weathermonitor/props/"+filesToRestore[i],this);
            String filename = "Maps"+Utilities.SEP+filesToRestore[i];
            Utilities.writeStringToFile(text,Utilities.DIR+Utilities.SEP+"Maps"+Utilities.SEP+filesToRestore[i]);
            System.out.println("Writing to "+filename);
        }
        JOptionPane.showMessageDialog(this,"You Must Restart WeatherMonitor");
}
    private void save() {
        props.put("FPS", fpsCombo.getSelectedItem());
        props.put("ZoomLevel",this.jComboBox1.getSelectedItem().toString());
//        if (loopRadarCb.isSelected()) {
//            props.put("LoopRadar","1");
//        } else {
//            props.put("LoopRadar","0");
//        }
        if (tooltipsCb.isSelected()) {
            props.put("ToolTipsEnabled", "1");
        } else {
            props.put("ToolTipsEnabled", "0");
        }
        if (windSpeedsOnArrowsCb.isSelected()) {
            props.put("ShowWindSpeedsOnArrows", "1");
        } else {
            props.put("ShowWindSpeedsOnArrows", "0");
        }
        props.put("ShowWindSpeedsOnArrowsValues", Integer.toString(windSpeedOnArrowsCombo.getSelectedIndex()));
        
        
        props.put("ObsFont", Integer.toString(obsFontCombo.getSelectedIndex()));
        
        props.put("SliderValue",Integer.toString(sliderValue));
        props.put("HighObDistance",highObDist);
        props.put("LowObDistance", lowObDist);
        props.put("MidObDistance", midObDist);
        props.put("Lat",latField.getText());
        props.put("Lon",lonField.getText());
        props.put("Fav1Lat",lat1Entry.getText());
        props.put("Fav2Lat",lat2Entry.getText());
        props.put("Fav3Lat",lat3Entry.getText());
        props.put("Fav1Lon",lon1Entry.getText());
        props.put("Fav2Lon",lon2Entry.getText());
        props.put("Fav3Lon",lon3Entry.getText());
        props.put("Fav1Name",this.fav1NameEntry.getText());
        props.put("Fav2Name",this.fav2NameEntry.getText());
        props.put("Fav3Name",this.fav3NameEntry.getText());
        props.put("YourLocation",this.addressEntry.getText());
        props.put("Fav1Address",this.address1Entry.getText());
        props.put("Fav2Address",this.address2Entry.getText());
        props.put("Fav3Address",this.address3Entry.getText());
        props.put("X",this.startUpX.getText());
        props.put("Y",this.startUpY.getText());
        props.put("WIDTH",this.startUpWidth.getText());
        props.put("HEIGHT",this.startUpHeight.getText());
        Color rgb = this.interstateLabel.getBackground();
        props.put("InterstateColor",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
        rgb = this.stateLabel.getBackground();
        props.put("StateColor",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
        rgb = this.roadLabel.getBackground();
        props.put("RoadColor",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
        rgb = this.mapBgColorLabel.getBackground();
        props.put("MapBgColor",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
        rgb = this.outBgColor.getBackground();
        props.put("OutBgColor",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
        rgb = this.prodTableBg.getBackground();
        props.put("ProdTableBg",Integer.toString(rgb.getRed())+","+Integer.toString(rgb.getGreen())+","+Integer.toString(rgb.getBlue()));
         Font f = prodTableFontButton.getFont();
        String family = f.getFamily();
        boolean v = f.isBold();
        int bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
        String size = Integer.toString(f.getSize());
        props.put("ProdTableFont", family+","+size+","+bold);
          f = applicationFont.getFont();
         family = f.getFamily();
         v = f.isBold();
         bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
         size = Integer.toString(f.getSize());
        props.put("ApplicationFont", family+","+size+","+bold);
        f = cityFont1Label.getFont();
        family = f.getFamily();
        v = f.isBold();
        bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
        size = Integer.toString(f.getSize());
        props.put("City1Font", family+","+size+","+bold);
        f = cityFont2Label.getFont();
        family = f.getFamily();
        v = f.isBold();
        bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
        size = Integer.toString(f.getSize());
        props.put("City2Font", family+","+size+","+bold);
        f = cityFont3Label.getFont();
        family = f.getFamily();
          v = f.isBold();
        bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
        size = Integer.toString(f.getSize());
        props.put("City3Font", family+","+size+","+bold);
        f = cityFont4Label.getFont();
        family = f.getFamily();
          v = f.isBold();
        bold = Font.PLAIN;
        if (v == true) {
            bold = Font.BOLD;
        }
        size = Integer.toString(f.getSize());
        props.put("City4Font", family+","+size+","+bold);
        
        System.out.println("Saving prefs:"+Utilities.DIR+Utilities.SEP+"WeatherMonitor.properties");
        Utilities.saveMonitorSettingsPrefs(props);
        if (this.settingsListener != null) {
            settingsListener.settingsChanged(props);
        }
    }
    private void getInfoFromCurrentMapLocation(int num) {
        if (num == 0) {
            this.latField.setText(Double.toString(Utilities.roundDouble(currentLat)));
             this.lonField.setText(Double.toString(Utilities.roundDouble(currentLon)));
             this.addressEntry.setText(currentLocation);
        }
         if (num == 1) {
            this.fav1NameEntry.setText("Favorite #1");
             this.lat1Entry.setText(Double.toString(Utilities.roundDouble(currentLat)));
             this.lon1Entry.setText(Double.toString(Utilities.roundDouble(currentLon)));
             this.address1Entry.setText(currentLocation);
        }
          if (num == 2) {
           this.fav2NameEntry.setText("Favorite #2");
             this.lat2Entry.setText(Double.toString(Utilities.roundDouble(currentLat)));
             this.lon2Entry.setText(Double.toString(Utilities.roundDouble(currentLon)));
             this.address2Entry.setText(currentLocation);
        }
           if (num == 3) {
            this.fav3NameEntry.setText("Favorite #3");
             this.lat3Entry.setText(Double.toString(Utilities.roundDouble(currentLat)));
             this.lon3Entry.setText(Double.toString(Utilities.roundDouble(currentLon)));
             this.address3Entry.setText(currentLocation);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        latField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        lonField = new javax.swing.JTextField();
        addressEntry = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        fav1NameEntry = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        lat1Entry = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lon1Entry = new javax.swing.JTextField();
        address1Entry = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        fav2NameEntry = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        lat2Entry = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        lon2Entry = new javax.swing.JTextField();
        address2Entry = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        fav3NameEntry = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        lat3Entry = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        lon3Entry = new javax.swing.JTextField();
        address3Entry = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        interstateLabel = new javax.swing.JLabel();
        stateLabel = new javax.swing.JLabel();
        roadLabel = new javax.swing.JLabel();
        mapBgColorLabel = new javax.swing.JLabel();
        outBgColor = new javax.swing.JLabel();
        prodTableBg = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        cityFont1Combo = new javax.swing.JComboBox<>();
        cityFont1Bold = new javax.swing.JCheckBox();
        cityFont1SizeCombo = new javax.swing.JComboBox<>();
        cityFont1Label = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        cityFont2Combo = new javax.swing.JComboBox<>();
        cityFont2Bold = new javax.swing.JCheckBox();
        cityFont2SizeCombo = new javax.swing.JComboBox<>();
        cityFont2Label = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        cityFont3Combo = new javax.swing.JComboBox<>();
        cityFont3Bold = new javax.swing.JCheckBox();
        cityFont3SizeCombo = new javax.swing.JComboBox<>();
        cityFont3Label = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        cityFont4Combo = new javax.swing.JComboBox<>();
        cityFont4Bold = new javax.swing.JCheckBox();
        cityFont4SizeCombo = new javax.swing.JComboBox<>();
        cityFont4Label = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        prodTableFontButton = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        applicationFont = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        windSpeedsOnArrowsCb = new javax.swing.JCheckBox();
        windSpeedOnArrowsCombo = new javax.swing.JComboBox<>();
        jPanel11 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        startUpX = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        startUpY = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        startUpWidth = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        startUpHeight = new javax.swing.JTextField();
        jButton11 = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        fpsCombo = new javax.swing.JComboBox<>();
        jPanel12 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jButton12 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        sliderValueLabel = new javax.swing.JLabel();
        obsFontCombo = new javax.swing.JComboBox<>();
        jPanel22 = new javax.swing.JPanel();
        tooltipsCb = new javax.swing.JCheckBox();

        setTitle("Weather Monitor Settings");
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Favorite Location Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N
        jPanel2.setLayout(new java.awt.GridLayout(4, 1));

        jPanel15.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel2.setText("Your Location");
        jLabel2.setMaximumSize(new java.awt.Dimension(250, 30));
        jLabel2.setMinimumSize(new java.awt.Dimension(175, 30));
        jLabel2.setPreferredSize(new java.awt.Dimension(175, 30));
        jPanel15.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel3.setText("Lat:");
        jPanel15.add(jLabel3);

        latField.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        latField.setText("31.2404");
        latField.setMaximumSize(new java.awt.Dimension(100, 30));
        latField.setMinimumSize(new java.awt.Dimension(100, 30));
        latField.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel15.add(latField);

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel4.setText("Lon:");
        jPanel15.add(jLabel4);

        lonField.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lonField.setText("-100.4733");
        lonField.setMaximumSize(new java.awt.Dimension(100, 30));
        lonField.setMinimumSize(new java.awt.Dimension(100, 30));
        lonField.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel15.add(lonField);

        addressEntry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        addressEntry.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        addressEntry.setText("Type address here and hit Enter key");
        addressEntry.setToolTipText("Type an address <Hit Enter Key>");
        addressEntry.setMaximumSize(new java.awt.Dimension(325, 30));
        addressEntry.setMinimumSize(new java.awt.Dimension(325, 30));
        addressEntry.setPreferredSize(new java.awt.Dimension(325, 30));
        addressEntry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addressEntryMouseClicked(evt);
            }
        });
        addressEntry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                addressEntryKeyPressed(evt);
            }
        });
        jPanel15.add(addressEntry);

        jButton7.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton7.setText("Use Current Location");
        jButton7.addActionListener(this::jButton7ActionPerformed);
        jPanel15.add(jButton7);

        jPanel2.add(jPanel15);

        jPanel16.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        fav1NameEntry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        fav1NameEntry.setText("Favorite1");
        fav1NameEntry.setMaximumSize(new java.awt.Dimension(250, 30));
        fav1NameEntry.setMinimumSize(new java.awt.Dimension(175, 30));
        fav1NameEntry.setPreferredSize(new java.awt.Dimension(175, 30));
        jPanel16.add(fav1NameEntry);

        jLabel6.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel6.setText("Lat:");
        jPanel16.add(jLabel6);

        lat1Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lat1Entry.setText("-------");
        lat1Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lat1Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lat1Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel16.add(lat1Entry);

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel7.setText("Lon:");
        jPanel16.add(jLabel7);

        lon1Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lon1Entry.setText("--------");
        lon1Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lon1Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lon1Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel16.add(lon1Entry);

        address1Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        address1Entry.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        address1Entry.setText("Type a favorite here and hit Enter key");
        address1Entry.setToolTipText("Type an address <Hit Enter Key>");
        address1Entry.setMaximumSize(new java.awt.Dimension(325, 30));
        address1Entry.setMinimumSize(new java.awt.Dimension(325, 30));
        address1Entry.setPreferredSize(new java.awt.Dimension(325, 30));
        address1Entry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                address1EntryMouseClicked(evt);
            }
        });
        address1Entry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                address1EntryKeyPressed(evt);
            }
        });
        jPanel16.add(address1Entry);

        jButton8.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton8.setText("Use Current Location");
        jButton8.addActionListener(this::jButton8ActionPerformed);
        jPanel16.add(jButton8);

        jPanel2.add(jPanel16);

        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        fav2NameEntry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        fav2NameEntry.setText("Favorite2");
        fav2NameEntry.setMaximumSize(new java.awt.Dimension(250, 30));
        fav2NameEntry.setMinimumSize(new java.awt.Dimension(175, 30));
        fav2NameEntry.setPreferredSize(new java.awt.Dimension(175, 30));
        fav2NameEntry.addActionListener(this::fav2NameEntryActionPerformed);
        jPanel17.add(fav2NameEntry);

        jLabel9.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel9.setText("Lat:");
        jPanel17.add(jLabel9);

        lat2Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lat2Entry.setText("-------");
        lat2Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lat2Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lat2Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel17.add(lat2Entry);

        jLabel10.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel10.setText("Lon:");
        jPanel17.add(jLabel10);

        lon2Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lon2Entry.setText("--------");
        lon2Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lon2Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lon2Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel17.add(lon2Entry);

        address2Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        address2Entry.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        address2Entry.setText("Type a favorite here and hit Enter key");
        address2Entry.setToolTipText("Type an address <Hit Enter Key>");
        address2Entry.setMaximumSize(new java.awt.Dimension(325, 30));
        address2Entry.setMinimumSize(new java.awt.Dimension(325, 30));
        address2Entry.setPreferredSize(new java.awt.Dimension(325, 30));
        address2Entry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                address2EntryMouseClicked(evt);
            }
        });
        address2Entry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                address2EntryKeyPressed(evt);
            }
        });
        jPanel17.add(address2Entry);

        jButton9.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton9.setText("Use Current Location");
        jButton9.addActionListener(this::jButton9ActionPerformed);
        jPanel17.add(jButton9);

        jPanel2.add(jPanel17);

        jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        fav3NameEntry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        fav3NameEntry.setText("Favorite3");
        fav3NameEntry.setMaximumSize(new java.awt.Dimension(250, 30));
        fav3NameEntry.setMinimumSize(new java.awt.Dimension(175, 30));
        fav3NameEntry.setPreferredSize(new java.awt.Dimension(175, 30));
        jPanel18.add(fav3NameEntry);

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel12.setText("Lat:");
        jPanel18.add(jLabel12);

        lat3Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lat3Entry.setText("-------");
        lat3Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lat3Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lat3Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel18.add(lat3Entry);

        jLabel13.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel13.setText("Lon:");
        jPanel18.add(jLabel13);

        lon3Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        lon3Entry.setText("--------");
        lon3Entry.setMaximumSize(new java.awt.Dimension(100, 30));
        lon3Entry.setMinimumSize(new java.awt.Dimension(100, 30));
        lon3Entry.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel18.add(lon3Entry);

        address3Entry.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        address3Entry.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        address3Entry.setText("Type a favorite here and hit Enter key");
        address3Entry.setToolTipText("Type an address <Hit Enter Key>");
        address3Entry.setMaximumSize(new java.awt.Dimension(325, 30));
        address3Entry.setMinimumSize(new java.awt.Dimension(325, 30));
        address3Entry.setPreferredSize(new java.awt.Dimension(325, 30));
        address3Entry.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                address3EntryMouseClicked(evt);
            }
        });
        address3Entry.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                address3EntryKeyPressed(evt);
            }
        });
        jPanel18.add(address3Entry);

        jButton10.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton10.setText("Use Current Location");
        jButton10.addActionListener(this::jButton10ActionPerformed);
        jPanel18.add(jButton10);

        jPanel2.add(jPanel18);

        jPanel7.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Colors And Font", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N
        jPanel5.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new java.awt.GridLayout(1, 7, 10, 2));

        interstateLabel.setBackground(new java.awt.Color(0, 0, 0));
        interstateLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        interstateLabel.setForeground(new java.awt.Color(255, 255, 255));
        interstateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        interstateLabel.setText("Interstate");
        interstateLabel.setToolTipText("Interstate Line Color");
        interstateLabel.setOpaque(true);
        interstateLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                interstateLabelMouseClicked(evt);
            }
        });
        jPanel6.add(interstateLabel);

        stateLabel.setBackground(new java.awt.Color(0, 0, 0));
        stateLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        stateLabel.setForeground(new java.awt.Color(255, 255, 255));
        stateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stateLabel.setText("State");
        stateLabel.setToolTipText("State Boundary Color");
        stateLabel.setOpaque(true);
        stateLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stateLabelMouseClicked(evt);
            }
        });
        jPanel6.add(stateLabel);

        roadLabel.setBackground(new java.awt.Color(0, 0, 0));
        roadLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        roadLabel.setForeground(new java.awt.Color(255, 255, 255));
        roadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        roadLabel.setText("Roads");
        roadLabel.setToolTipText("Road Color");
        roadLabel.setOpaque(true);
        roadLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                roadLabelMouseClicked(evt);
            }
        });
        jPanel6.add(roadLabel);

        mapBgColorLabel.setBackground(new java.awt.Color(255, 255, 255));
        mapBgColorLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        mapBgColorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mapBgColorLabel.setText("Map Bg");
        mapBgColorLabel.setToolTipText("Map Background Color");
        mapBgColorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mapBgColorLabel.setOpaque(true);
        mapBgColorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mapBgColorLabelMouseClicked(evt);
            }
        });
        jPanel6.add(mapBgColorLabel);

        outBgColor.setBackground(new java.awt.Color(255, 255, 255));
        outBgColor.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        outBgColor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        outBgColor.setText("Ocean Bg");
        outBgColor.setToolTipText("Ocean color");
        outBgColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        outBgColor.setOpaque(true);
        outBgColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                outBgColorMouseClicked(evt);
            }
        });
        jPanel6.add(outBgColor);

        prodTableBg.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        prodTableBg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        prodTableBg.setText("Alert Table Bg");
        prodTableBg.setToolTipText("Product table background color");
        prodTableBg.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        prodTableBg.setOpaque(true);
        prodTableBg.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                prodTableBgMouseClicked(evt);
            }
        });
        jPanel6.add(prodTableBg);

        jPanel5.add(jPanel6);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setOpaque(false);
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));

        cityFont1Combo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cityFont1Combo.setFocusable(false);
        cityFont1Combo.setMaximumSize(new java.awt.Dimension(400, 30));
        cityFont1Combo.setMinimumSize(new java.awt.Dimension(400, 30));
        cityFont1Combo.setPreferredSize(new java.awt.Dimension(400, 30));
        cityFont1Combo.addItemListener(this::cityFont1ComboItemStateChanged);
        jPanel4.add(cityFont1Combo);

        cityFont1Bold.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont1Bold.setText("Bold");
        cityFont1Bold.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        cityFont1Bold.setFocusable(false);
        cityFont1Bold.setMaximumSize(new java.awt.Dimension(65, 30));
        cityFont1Bold.setMinimumSize(new java.awt.Dimension(65, 30));
        cityFont1Bold.setOpaque(true);
        cityFont1Bold.setPreferredSize(new java.awt.Dimension(65, 30));
        cityFont1Bold.addActionListener(this::cityFont1BoldActionPerformed);
        jPanel4.add(cityFont1Bold);

        cityFont1SizeCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont1SizeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        cityFont1SizeCombo.setFocusable(false);
        cityFont1SizeCombo.setMaximumSize(new java.awt.Dimension(90, 30));
        cityFont1SizeCombo.setMinimumSize(new java.awt.Dimension(70, 30));
        cityFont1SizeCombo.setPreferredSize(new java.awt.Dimension(80, 30));
        cityFont1SizeCombo.addItemListener(this::cityFont1SizeComboItemStateChanged);
        jPanel4.add(cityFont1SizeCombo);

        cityFont1Label.setBackground(new java.awt.Color(255, 255, 255));
        cityFont1Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        cityFont1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cityFont1Label.setText("Small City Fonts");
        cityFont1Label.setFocusable(false);
        cityFont1Label.setOpaque(true);
        jPanel4.add(cityFont1Label);

        jLabel5.setText("     ");
        jPanel4.add(jLabel5);

        jPanel5.add(jPanel4);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));

        cityFont2Combo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cityFont2Combo.setFocusable(false);
        cityFont2Combo.setMaximumSize(new java.awt.Dimension(400, 30));
        cityFont2Combo.setMinimumSize(new java.awt.Dimension(400, 30));
        cityFont2Combo.setPreferredSize(new java.awt.Dimension(400, 30));
        cityFont2Combo.addItemListener(this::cityFont2ComboItemStateChanged);
        jPanel3.add(cityFont2Combo);

        cityFont2Bold.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont2Bold.setText("Bold");
        cityFont2Bold.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        cityFont2Bold.setFocusable(false);
        cityFont2Bold.setMaximumSize(new java.awt.Dimension(65, 30));
        cityFont2Bold.setMinimumSize(new java.awt.Dimension(65, 30));
        cityFont2Bold.setOpaque(true);
        cityFont2Bold.setPreferredSize(new java.awt.Dimension(65, 30));
        cityFont2Bold.addActionListener(this::cityFont2BoldActionPerformed);
        jPanel3.add(cityFont2Bold);

        cityFont2SizeCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont2SizeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        cityFont2SizeCombo.setFocusable(false);
        cityFont2SizeCombo.setMaximumSize(new java.awt.Dimension(90, 30));
        cityFont2SizeCombo.setMinimumSize(new java.awt.Dimension(70, 30));
        cityFont2SizeCombo.setPreferredSize(new java.awt.Dimension(80, 30));
        cityFont2SizeCombo.addItemListener(this::cityFont2SizeComboItemStateChanged);
        jPanel3.add(cityFont2SizeCombo);

        cityFont2Label.setBackground(new java.awt.Color(255, 255, 255));
        cityFont2Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        cityFont2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cityFont2Label.setText("Medium City Fonts");
        cityFont2Label.setFocusable(false);
        cityFont2Label.setOpaque(true);
        jPanel3.add(cityFont2Label);

        jPanel5.add(jPanel3);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setOpaque(false);
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));

        cityFont3Combo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont3Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cityFont3Combo.setFocusable(false);
        cityFont3Combo.setMaximumSize(new java.awt.Dimension(400, 30));
        cityFont3Combo.setMinimumSize(new java.awt.Dimension(400, 30));
        cityFont3Combo.setPreferredSize(new java.awt.Dimension(400, 30));
        cityFont3Combo.addItemListener(this::cityFont3ComboItemStateChanged);
        jPanel8.add(cityFont3Combo);

        cityFont3Bold.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont3Bold.setText("Bold");
        cityFont3Bold.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        cityFont3Bold.setFocusable(false);
        cityFont3Bold.setMaximumSize(new java.awt.Dimension(65, 30));
        cityFont3Bold.setMinimumSize(new java.awt.Dimension(65, 30));
        cityFont3Bold.setOpaque(true);
        cityFont3Bold.setPreferredSize(new java.awt.Dimension(65, 30));
        cityFont3Bold.addActionListener(this::cityFont3BoldActionPerformed);
        jPanel8.add(cityFont3Bold);

        cityFont3SizeCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont3SizeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        cityFont3SizeCombo.setFocusable(false);
        cityFont3SizeCombo.setMaximumSize(new java.awt.Dimension(90, 30));
        cityFont3SizeCombo.setMinimumSize(new java.awt.Dimension(70, 30));
        cityFont3SizeCombo.setPreferredSize(new java.awt.Dimension(80, 30));
        cityFont3SizeCombo.addItemListener(this::cityFont3SizeComboItemStateChanged);
        jPanel8.add(cityFont3SizeCombo);

        cityFont3Label.setBackground(new java.awt.Color(255, 255, 255));
        cityFont3Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        cityFont3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cityFont3Label.setText("Large City Fonts");
        cityFont3Label.setFocusable(false);
        cityFont3Label.setOpaque(true);
        jPanel8.add(cityFont3Label);

        jPanel5.add(jPanel8);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setOpaque(false);
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));

        cityFont4Combo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont4Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cityFont4Combo.setFocusable(false);
        cityFont4Combo.setMaximumSize(new java.awt.Dimension(400, 30));
        cityFont4Combo.setMinimumSize(new java.awt.Dimension(400, 30));
        cityFont4Combo.setPreferredSize(new java.awt.Dimension(400, 30));
        cityFont4Combo.addItemListener(this::cityFont4ComboItemStateChanged);
        jPanel9.add(cityFont4Combo);

        cityFont4Bold.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont4Bold.setText("Bold");
        cityFont4Bold.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        cityFont4Bold.setFocusable(false);
        cityFont4Bold.setMaximumSize(new java.awt.Dimension(65, 30));
        cityFont4Bold.setMinimumSize(new java.awt.Dimension(65, 30));
        cityFont4Bold.setOpaque(true);
        cityFont4Bold.setPreferredSize(new java.awt.Dimension(65, 30));
        cityFont4Bold.addActionListener(this::cityFont4BoldActionPerformed);
        jPanel9.add(cityFont4Bold);

        cityFont4SizeCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        cityFont4SizeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        cityFont4SizeCombo.setFocusable(false);
        cityFont4SizeCombo.setMaximumSize(new java.awt.Dimension(90, 30));
        cityFont4SizeCombo.setMinimumSize(new java.awt.Dimension(70, 30));
        cityFont4SizeCombo.setPreferredSize(new java.awt.Dimension(80, 30));
        cityFont4SizeCombo.addItemListener(this::cityFont4SizeComboItemStateChanged);
        jPanel9.add(cityFont4SizeCombo);

        cityFont4Label.setBackground(new java.awt.Color(255, 255, 255));
        cityFont4Label.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        cityFont4Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cityFont4Label.setText("XLarge City Fonts");
        cityFont4Label.setFocusable(false);
        cityFont4Label.setOpaque(true);
        jPanel9.add(cityFont4Label);

        jPanel5.add(jPanel9);

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jLabel21.setText("  ");
        jPanel19.add(jLabel21);

        prodTableFontButton.setBackground(new java.awt.Color(51, 51, 51));
        prodTableFontButton.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        prodTableFontButton.setForeground(new java.awt.Color(255, 255, 0));
        prodTableFontButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        prodTableFontButton.setText("Product Table Font");
        prodTableFontButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        prodTableFontButton.setOpaque(true);
        prodTableFontButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                prodTableFontButtonMouseClicked(evt);
            }
        });
        jPanel19.add(prodTableFontButton);

        jLabel20.setText("  ");
        jPanel19.add(jLabel20);

        jLabel19.setText("  ");
        jPanel19.add(jLabel19);

        applicationFont.setBackground(new java.awt.Color(51, 51, 51));
        applicationFont.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        applicationFont.setForeground(new java.awt.Color(255, 255, 0));
        applicationFont.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        applicationFont.setText("Application Font");
        applicationFont.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        applicationFont.setOpaque(true);
        applicationFont.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                applicationFontMouseClicked(evt);
            }
        });
        jPanel19.add(applicationFont);

        jLabel22.setText("    ");
        jPanel19.add(jLabel22);

        windSpeedsOnArrowsCb.setBackground(new java.awt.Color(244, 244, 244));
        windSpeedsOnArrowsCb.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        windSpeedsOnArrowsCb.setSelected(true);
        windSpeedsOnArrowsCb.setText("Show Wind Speeds On Arrows");
        windSpeedsOnArrowsCb.setBorder(null);
        windSpeedsOnArrowsCb.setFocusable(false);
        windSpeedsOnArrowsCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        windSpeedsOnArrowsCb.setMaximumSize(new java.awt.Dimension(291, 34));
        windSpeedsOnArrowsCb.setMinimumSize(new java.awt.Dimension(291, 34));
        windSpeedsOnArrowsCb.setPreferredSize(new java.awt.Dimension(291, 34));
        windSpeedsOnArrowsCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        windSpeedsOnArrowsCb.addActionListener(this::windSpeedsOnArrowsCbActionPerformed);
        jPanel19.add(windSpeedsOnArrowsCb);

        windSpeedOnArrowsCombo.setBackground(new java.awt.Color(244, 244, 244));
        windSpeedOnArrowsCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        windSpeedOnArrowsCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "For All Wind Speeds", "For Wind Speeds Above 10", "For Wind Speeds Above 20" }));
        windSpeedOnArrowsCombo.setBorder(null);
        windSpeedOnArrowsCombo.setFocusable(false);
        windSpeedOnArrowsCombo.setMaximumSize(new java.awt.Dimension(32767, 34));
        jPanel19.add(windSpeedOnArrowsCombo);

        jPanel5.add(jPanel19);

        jPanel7.add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 5));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Default Window Size & Location", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N

        jLabel8.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel8.setText("Location:");
        jPanel1.add(jLabel8);

        jLabel18.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel18.setText("X:");
        jPanel1.add(jLabel18);

        startUpX.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        startUpX.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startUpX.setText(" 0");
        jPanel1.add(startUpX);

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel11.setText("Y:");
        jPanel1.add(jLabel11);

        startUpY.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        startUpY.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startUpY.setText("0");
        jPanel1.add(startUpY);

        jLabel14.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel14.setText("Width:");
        jPanel1.add(jLabel14);

        startUpWidth.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        startUpWidth.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startUpWidth.setText("1280");
        jPanel1.add(startUpWidth);

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel17.setText("Height:");
        jPanel1.add(jLabel17);

        startUpHeight.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        startUpHeight.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startUpHeight.setText("1024");
        jPanel1.add(startUpHeight);

        jButton11.setFont(new java.awt.Font("SansSerif", 0, 17)); // NOI18N
        jButton11.setText("<<<Get Current");
        jButton11.setToolTipText("Get the current Weather Monitor position and size");
        jButton11.addActionListener(this::jButton11ActionPerformed);
        jPanel1.add(jButton11);

        jPanel10.add(jPanel1);

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Other Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel1.setText("Zoom Level");
        jLabel1.setToolTipText("Default Zoom Level");
        jPanel13.add(jLabel1);

        jComboBox1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7" }));
        jComboBox1.setSelectedItem(5);
        jComboBox1.setMaximumSize(new java.awt.Dimension(55, 34));
        jComboBox1.setMinimumSize(new java.awt.Dimension(55, 34));
        jComboBox1.setPreferredSize(new java.awt.Dimension(55, 34));
        jPanel13.add(jComboBox1);

        jLabel23.setText("   ");
        jPanel13.add(jLabel23);

        jLabel15.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel15.setText("Loop Speed");
        jLabel15.setToolTipText("Radar Frames Per Second");
        jPanel13.add(jLabel15);

        fpsCombo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        fpsCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "20", "30", "40", "50" }));
        fpsCombo.setMaximumSize(new java.awt.Dimension(55, 34));
        fpsCombo.setMinimumSize(new java.awt.Dimension(55, 34));
        fpsCombo.setPreferredSize(new java.awt.Dimension(55, 34));
        jPanel13.add(fpsCombo);

        jPanel10.add(jPanel13);

        jPanel11.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setLayout(new java.awt.BorderLayout());

        jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton12.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jButton12.setText("Radar Settings");
        jButton12.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton12.addActionListener(this::jButton12ActionPerformed);
        jPanel14.add(jButton12);

        jButton1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 153, 0));
        jButton1.setText("Save");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel14.add(jButton1);

        jButton2.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jButton2.setText("Close");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel14.add(jButton2);

        jPanel12.add(jPanel14, java.awt.BorderLayout.SOUTH);

        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Observation Plot Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N

        jLabel16.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel16.setText("Plot Density:");
        jPanel21.add(jLabel16);

        jSlider1.addChangeListener(this::jSlider1StateChanged);
        jPanel21.add(jSlider1);

        sliderValueLabel.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        sliderValueLabel.setText("   ");
        jPanel21.add(sliderValueLabel);

        obsFontCombo.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        obsFontCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Use Small Font For Temperature Plot", "Use Medium Font For Temperature Plot", "Use Large Font For Temperature Plot", "Use XLarge Font For Temperature Plot" }));
        jPanel21.add(obsFontCombo);

        jPanel20.add(jPanel21, java.awt.BorderLayout.WEST);

        jPanel22.setLayout(new java.awt.GridLayout(1, 0));

        tooltipsCb.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        tooltipsCb.setSelected(true);
        tooltipsCb.setText("Show Tooltips");
        tooltipsCb.setFocusable(false);
        tooltipsCb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_unsel.png"))); // NOI18N
        tooltipsCb.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/weathermonitor/icons/checkbox_sel.png"))); // NOI18N
        tooltipsCb.addActionListener(this::tooltipsCbActionPerformed);
        jPanel22.add(tooltipsCb);

        jPanel20.add(jPanel22, java.awt.BorderLayout.CENTER);

        jPanel12.add(jPanel20, java.awt.BorderLayout.NORTH);

        jPanel11.add(jPanel12, java.awt.BorderLayout.SOUTH);

        jPanel7.add(jPanel11, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel7);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        save();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        this.startUpX.setText(Integer.toString(program.getX()));
        this.startUpY.setText(Integer.toString(program.getY()));
        this.startUpWidth.setText(Integer.toString(program.getWidth()));
        this.startUpHeight.setText(Integer.toString(program.getHeight()));
    }//GEN-LAST:event_jButton11ActionPerformed

    private void cityFont1ComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont1ComboItemStateChanged
        int bold;
        if (cityFont1Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont1Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont1SizeCombo.getSelectedItem().toString()));
        this.cityFont1Label.setFont(f);
    }//GEN-LAST:event_cityFont1ComboItemStateChanged

    private void cityFont1SizeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont1SizeComboItemStateChanged
        int bold;
        if (cityFont1Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont1Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont1SizeCombo.getSelectedItem().toString()));
        this.cityFont1Label.setFont(f);
    }//GEN-LAST:event_cityFont1SizeComboItemStateChanged

    private void cityFont2ComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont2ComboItemStateChanged
        int bold;
        if (cityFont2Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont2Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont2SizeCombo.getSelectedItem().toString()));
        this.cityFont2Label.setFont(f);
    }//GEN-LAST:event_cityFont2ComboItemStateChanged

    private void cityFont2SizeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont2SizeComboItemStateChanged
        int bold;
        if (cityFont2Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont2Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont2SizeCombo.getSelectedItem().toString()));
        this.cityFont2Label.setFont(f);
    }//GEN-LAST:event_cityFont2SizeComboItemStateChanged

    private void cityFont3ComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont3ComboItemStateChanged
        int bold;
        if (cityFont3Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }Font f = new Font(this.cityFont3Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont3SizeCombo.getSelectedItem().toString()));
       this.cityFont3Label.setFont(f);
    }//GEN-LAST:event_cityFont3ComboItemStateChanged

    private void cityFont3SizeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont3SizeComboItemStateChanged
        int bold;
        if (cityFont3Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont3Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont3SizeCombo.getSelectedItem().toString()));
       this.cityFont3Label.setFont(f);
    }//GEN-LAST:event_cityFont3SizeComboItemStateChanged

    private void cityFont4ComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont4ComboItemStateChanged
        int bold;
        if (cityFont4Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont4Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont4SizeCombo.getSelectedItem().toString()));
        this.cityFont4Label.setFont(f);
    }//GEN-LAST:event_cityFont4ComboItemStateChanged

    private void cityFont4SizeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cityFont4SizeComboItemStateChanged
        int bold;
        if (cityFont4Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        } 
        Font f = new Font(this.cityFont4Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont4SizeCombo.getSelectedItem().toString()));
        this.cityFont4Label.setFont(f);
    }//GEN-LAST:event_cityFont4SizeComboItemStateChanged

    private void cityFont1BoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cityFont1BoldActionPerformed
        int bold;
        if (cityFont1Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont1Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont1SizeCombo.getSelectedItem().toString()));
       this.cityFont1Label.setFont(f);
    }//GEN-LAST:event_cityFont1BoldActionPerformed

    private void cityFont2BoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cityFont2BoldActionPerformed
         int bold;
        if (cityFont2Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont2Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont2SizeCombo.getSelectedItem().toString()));
       this.cityFont2Label.setFont(f);
    }//GEN-LAST:event_cityFont2BoldActionPerformed

    private void cityFont3BoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cityFont3BoldActionPerformed
        int bold;
        if (cityFont3Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont3Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont3SizeCombo.getSelectedItem().toString()));
       this.cityFont3Label.setFont(f);
    }//GEN-LAST:event_cityFont3BoldActionPerformed

    private void cityFont4BoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cityFont4BoldActionPerformed
        int bold;
        if (cityFont4Bold.isSelected()) {
            bold = Font.BOLD;
        } else {
            bold = Font.PLAIN;
        }
        Font f = new Font(this.cityFont4Combo.getSelectedItem().toString(),bold,Integer.valueOf(cityFont4SizeCombo.getSelectedItem().toString()));
       this.cityFont4Label.setFont(f);
    }//GEN-LAST:event_cityFont4BoldActionPerformed

    private void mapBgColorLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapBgColorLabelMouseClicked
          Color initialColor = mapBgColorLabel.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
        if (color != null) {
            mapBgColorLabel.setForeground(Utilities.getFgColor(color));
            mapBgColorLabel.setBackground(color);
        }
    }//GEN-LAST:event_mapBgColorLabelMouseClicked

    private void interstateLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_interstateLabelMouseClicked
        Color initialColor = interstateLabel.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
        if (color != null) {
            interstateLabel.setForeground(Utilities.getFgColor(color));
            interstateLabel.setBackground(color);
        }
    }//GEN-LAST:event_interstateLabelMouseClicked

    private void stateLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stateLabelMouseClicked
        Color initialColor = stateLabel.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
         if (color != null) {
        stateLabel.setForeground(Utilities.getFgColor(color));
        stateLabel.setBackground(color);
         }
    }//GEN-LAST:event_stateLabelMouseClicked

    private void roadLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_roadLabelMouseClicked
        Color initialColor = roadLabel.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
         if (color != null) {
        roadLabel.setForeground(Utilities.getFgColor(color));
        roadLabel.setBackground(color);
         }
    }//GEN-LAST:event_roadLabelMouseClicked

    private void outBgColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outBgColorMouseClicked
        Color initialColor = outBgColor.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
         if (color != null) {
        outBgColor.setForeground(Utilities.getFgColor(color));
        outBgColor.setBackground(color);
         }
    }//GEN-LAST:event_outBgColorMouseClicked

    private void prodTableBgMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_prodTableBgMouseClicked
        Color initialColor = prodTableBg.getBackground();
        Color color = JColorChooser.showDialog(this, "Select a color", initialColor);
         if (color != null) {
        prodTableFontButton.setBackground(color);
        prodTableBg.setForeground(Utilities.getFgColor(color));
        prodTableBg.setBackground(color);
        prodTableFontButton.setBackground(color);
         }
    }//GEN-LAST:event_prodTableBgMouseClicked

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        getInfoFromCurrentMapLocation(3);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        getInfoFromCurrentMapLocation(2);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        getInfoFromCurrentMapLocation(1);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        getInfoFromCurrentMapLocation(0);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void fav2NameEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fav2NameEntryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fav2NameEntryActionPerformed

    private void address3EntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_address3EntryKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            lookupAddress(lat3Entry,lon3Entry,address3Entry);
        }
    }//GEN-LAST:event_address3EntryKeyPressed

    private void address3EntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_address3EntryMouseClicked
        address3Entry.selectAll();
    }//GEN-LAST:event_address3EntryMouseClicked

    private void address2EntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_address2EntryKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            lookupAddress(lat2Entry,lon2Entry,address2Entry);
        }
    }//GEN-LAST:event_address2EntryKeyPressed

    private void address2EntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_address2EntryMouseClicked
        address2Entry.selectAll();
    }//GEN-LAST:event_address2EntryMouseClicked

    private void address1EntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_address1EntryKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            lookupAddress(lat1Entry,lon1Entry,address1Entry);
        }
    }//GEN-LAST:event_address1EntryKeyPressed

    private void address1EntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_address1EntryMouseClicked
        address1Entry.selectAll();
    }//GEN-LAST:event_address1EntryMouseClicked

    private void addressEntryKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_addressEntryKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            lookupAddress(latField,lonField,addressEntry);
        }
    }//GEN-LAST:event_addressEntryKeyPressed

    private void addressEntryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addressEntryMouseClicked
        this.addressEntry.selectAll();
    }//GEN-LAST:event_addressEntryMouseClicked

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        RadarSettings.getInstance().showDialog(this,program);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void prodTableFontButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_prodTableFontButtonMouseClicked
         Font f = FontChooser.getInstance().showDialog(this,prodTableFontButton.getFont());
        this.prodTableFontButton.setFont(f);
    }//GEN-LAST:event_prodTableFontButtonMouseClicked

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        System.out.println("Metar distance pct ="+jSlider1.getValue());
        sliderValue = jSlider1.getValue();
        double pct = 2.0*sliderValue/100.0;
        lowObDist = Utilities.roundDouble(5.0*pct,2);
        midObDist = Utilities.roundDouble(7.0*pct,2);
        highObDist = Utilities.roundDouble(9.*pct,2);
        this.sliderValueLabel.setText(Integer.toString(sliderValue));
        System.out.println("low="+lowObDist+" mid="+midObDist+" high="+highObDist);
    }//GEN-LAST:event_jSlider1StateChanged

    private void windSpeedsOnArrowsCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windSpeedsOnArrowsCbActionPerformed
        if (windSpeedsOnArrowsCb.isSelected()) {
            windSpeedOnArrowsCombo.setEnabled(true);
        } else {
            windSpeedOnArrowsCombo.setEnabled(false);
        }
    }//GEN-LAST:event_windSpeedsOnArrowsCbActionPerformed

    private void tooltipsCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tooltipsCbActionPerformed
        
    }//GEN-LAST:event_tooltipsCbActionPerformed

    private void applicationFontMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_applicationFontMouseClicked
        Font f = FontChooser.getInstance().showDialog(this,applicationFont.getFont());
        this.applicationFont.setFont(f);
    }//GEN-LAST:event_applicationFontMouseClicked
     private void lookupAddress(JTextField lat, JTextField lon, JTextField addr) {
         revert1[0] = lat.getText();
         revert1[1] = lon.getText();
         lat.setText("-----");
         lon.setText("-----");
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
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 JSONObject obj = new JSONObject(json);
                 JSONArray candidates = obj.getJSONArray("candidates");
                 String x = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("x").toString();
                 String y = candidates.getJSONObject(0).getJSONObject("location").getBigDecimal("y").toString();
                 lat.setText(y.substring(0,8));
                 lon.setText(x.substring(0,9));
                 //addr.setText("");
             }
         };
         t.start();
        
     }
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
//            logger.log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(() -> new RadarPreferences1().setVisible(true));
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address1Entry;
    private javax.swing.JTextField address2Entry;
    private javax.swing.JTextField address3Entry;
    private javax.swing.JTextField addressEntry;
    private javax.swing.JLabel applicationFont;
    private javax.swing.JCheckBox cityFont1Bold;
    private javax.swing.JComboBox<String> cityFont1Combo;
    private javax.swing.JLabel cityFont1Label;
    private javax.swing.JComboBox<String> cityFont1SizeCombo;
    private javax.swing.JCheckBox cityFont2Bold;
    private javax.swing.JComboBox<String> cityFont2Combo;
    private javax.swing.JLabel cityFont2Label;
    private javax.swing.JComboBox<String> cityFont2SizeCombo;
    private javax.swing.JCheckBox cityFont3Bold;
    private javax.swing.JComboBox<String> cityFont3Combo;
    private javax.swing.JLabel cityFont3Label;
    private javax.swing.JComboBox<String> cityFont3SizeCombo;
    private javax.swing.JCheckBox cityFont4Bold;
    private javax.swing.JComboBox<String> cityFont4Combo;
    private javax.swing.JLabel cityFont4Label;
    private javax.swing.JComboBox<String> cityFont4SizeCombo;
    private javax.swing.JTextField fav1NameEntry;
    private javax.swing.JTextField fav2NameEntry;
    private javax.swing.JTextField fav3NameEntry;
    private javax.swing.JComboBox<String> fpsCombo;
    private javax.swing.JLabel interstateLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField lat1Entry;
    private javax.swing.JTextField lat2Entry;
    private javax.swing.JTextField lat3Entry;
    private javax.swing.JTextField latField;
    private javax.swing.JTextField lon1Entry;
    private javax.swing.JTextField lon2Entry;
    private javax.swing.JTextField lon3Entry;
    private javax.swing.JTextField lonField;
    private javax.swing.JLabel mapBgColorLabel;
    private javax.swing.JComboBox<String> obsFontCombo;
    private javax.swing.JLabel outBgColor;
    private javax.swing.JLabel prodTableBg;
    private javax.swing.JLabel prodTableFontButton;
    private javax.swing.JLabel roadLabel;
    private javax.swing.JLabel sliderValueLabel;
    private javax.swing.JTextField startUpHeight;
    private javax.swing.JTextField startUpWidth;
    private javax.swing.JTextField startUpX;
    private javax.swing.JTextField startUpY;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JCheckBox tooltipsCb;
    private javax.swing.JComboBox<String> windSpeedOnArrowsCombo;
    private javax.swing.JCheckBox windSpeedsOnArrowsCb;
    // End of variables declaration//GEN-END:variables
}
