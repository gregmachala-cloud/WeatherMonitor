package weathermonitor.settings;


import weathermonitor.dataproviders.Radar;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import weathermonitor.dataobjects.RadarStation;
import weathermonitor.WeatherMonitorRun;
import weathermonitor.dataobjects.WmsRadarLayer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author greg
 */
public class RadarSettings extends javax.swing.JFrame {
    private Properties props = null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RadarSettings.class.getName());
//    private JTextField[] prodNameFields = new JTextField[5];
//    private JTextField[] wmsUrlFields = new JTextField[5];
//    private JTextField[] layerNameFields = new JTextField[5];
    private static RadarSettings settings = null;
    private WeatherMonitorSettings weatherMonitorSettings;
    private WeatherMonitorRun program;
    private static ArrayList<RadarStation> radarStations = null;
    private static String wmsBaseUrl = "https://opengeo.ncep.noaa.gov/geoserver/kxxx/ows?";
    //service=wms&request=GetCapabilities&r=1.0";
    
    public static RadarSettings getInstance() {
        if (settings == null) settings = new RadarSettings();
        return settings;
    }
    /**
     * Creates new form RadarSettings
     */
    private RadarSettings() {
        
        initComponents();
       
        
        readProps();
       
        
    }

    public void showDialog(WeatherMonitorSettings parent, WeatherMonitorRun program) {
        this.weatherMonitorSettings = parent;
        this.program = program;
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.getWidth();
        int h = this.getHeight();
        
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
        this.setVisible(true);
    }
    public static String getNearestRadarStation(double lat, double lon) {
        if (radarStations == null) {
            radarStations = getRadarStations();
        }
        double dist = Double.MAX_VALUE;
        //Point2D.Double p2d = new Point2D.Double(lon,lat);
        String id = null;
        for (int i=0; i<radarStations.size(); i++) {
            //Exclude TDWR radars
            if (radarStations.get(i).getId().startsWith("T") == false) {
                double testDist = Utilities.getDistanceBetweenTwoPoints(lon, lat, radarStations.get(i).getLon(), radarStations.get(i).getLat());
                if (testDist < dist) {
                    id = radarStations.get(i).getId();
                    dist = testDist;
                }
            }
        }
        return id;
    }
    public static ArrayList<RadarStation> getRadarStations() {
        ArrayList<RadarStation> list = new ArrayList<>();
        Properties p = Utilities.readPrefs("Maps"+Utilities.SEP+"RadarStations.properties");
        if (p != null) {
            Enumeration en = p.keys();
            while(en.hasMoreElements()) {
                String id = en.nextElement().toString();
                String[] arr = p.get(id).toString().split(",");
                double lat = Double.valueOf(arr[1]);
                double lon = Double.valueOf(arr[2]);
                RadarStation rs = new RadarStation(id,arr[0],lat,lon);
                list.add(rs);
            }
            //System.out.println("Returning radar stations from prop file");
            //System.out.println("SJT RADAR : "+list.get(12).getId()+" "+list.get(12).getName()+ " "+list.get(12).getLat()+" "+list.get(12).getLon());
            return list;
        }
        p = new Properties();
        System.out.println("Downloading...https://api.weather.gov/radar/stations");
        String json = Utilities.downloadJsonData("https://api.weather.gov/radar/stations",5000);
        if (json == null) return null;
        JSONObject obj = new JSONObject(json);
            JSONArray features = obj.getJSONArray("features");
           
            for (int i = 0; i < features.length(); i++) {
                JSONArray coords = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                String name = features.getJSONObject(i).getJSONObject("properties").getString("name");
                String id = features.getJSONObject(i).getJSONObject("properties").getString("id");
                p.put(id, name.replaceAll(",", "")+","+coords.getDouble(1)+","+coords.getDouble(0));
                RadarStation rs = new RadarStation(id,name,coords.getDouble(1),coords.getDouble(0));
                list.add(rs);
            }
            Utilities.savePrefs(p, "Maps"+Utilities.SEP+"RadarStations.properties");
            return list;
    }
    public static ArrayList<WmsRadarLayer> getAllRadarLayersFor(String id) {
        id = id.toLowerCase();
        ArrayList<WmsRadarLayer> list = new ArrayList<>();
        String dlink = wmsBaseUrl.replace("kxxx", id);
        long start = new Date().getTime();
        //System.out.println("Download:"+dlink);
        String xml = Utilities.downloadJsonData(dlink+"service=wms&request=GetCapabilities&r=1.0",5000);
        if (xml == null) return null;
        long end = new Date().getTime();
        long diff = (end-start)/1000;
        //System.out.println("Download took "+diff+" seconds ");
        Pattern pat1 = Pattern.compile("<Layer");
        Pattern pat2 = Pattern.compile("</Abstract>");
        Matcher m1 = pat1.matcher(xml);
        String added = "";
        while (m1.find()) {
            String substring = xml.substring(m1.start());
            Matcher m2 = pat2.matcher(substring);
            if (m2.find()) {

                String snippet = substring.substring(0, m2.end());
                //System.out.println("Found snippet" + snippet);
                Pattern pat3 = Pattern.compile("<Name>");
                Pattern pat5 = Pattern.compile("</Name>");
                Pattern pat6 = Pattern.compile("<Abstract>");
                Pattern pat7 = Pattern.compile("</Abstract>");
                Matcher m3 = pat3.matcher(snippet);
                Matcher m4 = pat5.matcher(snippet);
                Matcher m6 = pat6.matcher(snippet);
                Matcher m7 = pat7.matcher(snippet);
                if (m3.find() && m4.find() && m6.find() && m7.find()) {
                    String layerName = snippet.substring(m3.end(), m4.start());
                    String layerDescr = snippet.substring(m6.end(),m7.start());
                    //System.out.println("layerName=" + layerName);
                    if (added.contains(layerName) == false) {
                        WmsRadarLayer l = new WmsRadarLayer(dlink,layerName,layerDescr,false);
                        list.add(l);
                        added = added + ","+layerName;
                    }
                }

            }
        }
       // System.out.print("Radar layers for "+id+"="+added);
        return list;
    }
    private void readProps() {
        this.props = Utilities.readRadarSettingsPrefs();
        if (props == null) {
            //System.out.println("Radar settings is null")
            props = new Properties();
            String url = Radar.mosaicUrl;
            props.put("MosaicUrl",url);
            String name = Radar.mosaicDescrName;
            props.put("MosaicName", name);
            String layerName = Radar.mosaicLayerName;
            props.put("MosaicLayerName",layerName);
        } 
            if (props.containsKey("MosaicUrl")) this.wmsUrl1.setText(props.getProperty("MosaicUrl"));
            if (props.containsKey("MosaicName")) this.radarProd1.setText(props.getProperty("MosaicName"));
            if (props.containsKey("MosaicLayerName")) this.layerName1.setText(props.getProperty("MosaicLayerName"));
           
        
        
        
    }
    
    private void savePrefs() {
        for (int i=0; i<5; i++) {
            int j = i+1;
            props.put("MosaicUrl", this.wmsUrl1.getText());
            props.put("MosaicName", this.radarProd1.getText());
            props.put("MosaicLayerName", this.layerName1.getText());
        }
        Utilities.saveRadarSettingsProps(props);
        program.radarSettingsChanged();
        //JOptionPane.showMessageDialog(weatherMonitorSettings,"Restart to see changes to radar settings!");
        setVisible(false);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        wmsUrl1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        radarProd1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        layerName1 = new javax.swing.JTextField();

        setTitle("Default Mosaic Radar Settings");

        jButton1.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1);

        jButton2.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N
        jButton2.setText("Close");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel6.setPreferredSize(new java.awt.Dimension(505, 46));

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Url:");

        wmsUrl1.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        wmsUrl1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        wmsUrl1.setMinimumSize(new java.awt.Dimension(500, 36));
        wmsUrl1.setPreferredSize(new java.awt.Dimension(500, 36));

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Name:");

        radarProd1.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        radarProd1.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Layer:");

        layerName1.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        layerName1.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radarProd1, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(layerName1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wmsUrl1, javax.swing.GroupLayout.DEFAULT_SIZE, 1087, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(radarProd1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(wmsUrl1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(layerName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel6, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        savePrefs();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String id = getNearestRadarStation(31.0,-101.1);
        System.out.println("Nearest Radar Stattion is "+id);
        ArrayList<WmsRadarLayer> l = getAllRadarLayersFor(id);
        if (l != null) {
        for (int i=0; i<l.size(); i++) {
            System.out.println(l.get(i).getLayerName() +" : "+l.get(i).getShortLayerDescr()+" : "+l.get(i).getBaseWmsUrl());
        
        }
        } else {
            System.out.println("Internet appears to be down");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField layerName1;
    private javax.swing.JTextField radarProd1;
    private javax.swing.JTextField wmsUrl1;
    // End of variables declaration//GEN-END:variables
}
