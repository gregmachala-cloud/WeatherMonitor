/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package weathermonitor.settings;

import weathermonitor.layers.StormReportLayer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import utilities.Utilities;
import org.json.JSONObject;
import weathermonitor.dataproviders.StormReports;

/**
 *
 * @author greg
 */
public class StormReportSettings1 extends javax.swing.JFrame {
    private  String[] officeIds = {"AKQ","ALY","BGM","BOX","BTV","BUF","CAE","CAR","CHS","CLE","CTP","GSP","GYX","ILM","ILN","LWX","MHX","OKX","PBZ","PHI","RAH","RLX","RNK","ABQ","AMA","BMX","BRO","CRP","EPZ","EWX","FFC","FWD","HGX","HUN","JAN","JAX","KEY","LCH","LIX","LUB","LZK","MAF","MEG","MFL","MLB","MOB","MRX","OHX","OUN","SHV","SJT","SJU","TAE","TBW","TSA","ABR","APX","ARX","BIS","BOU","CYS","DDC","DLH","DMX","DTX","DVN","EAX","FGF","FSD","GID","GJT","GLD","GRB","GRR","ICT","ILX","IND","IWX","JKL","LBF","LMK","LOT","LSX","MKX","MPX","MQT","OAX","PAH","PUB","RIW","SGF","TOP","UNR","BOI","BYZ","EKA","FGZ","GGW","HNX","LKN","LOX","MFR","MSO","MTR","OTX","PDT","PIH","PQR","PSR","REV","SEW","SGX","SLC","STO","TFX","TWC","VEF","AER","AFC","AFG","AJK","ALU","GUM","HPA","HFO","PPG","PQE","PQW","STU","NH1","NH2","ONA","ONP"};
    private String[] active_states = {"TX"};
    private Hashtable<String, JPanel> panelHash = new Hashtable<>();
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StormReportSettings1.class.getName());
    private ArrayList<JCheckBox> officeCbs = new ArrayList<>();
    private static Font font = new Font("Monospaced",Font.BOLD,18);
    private static String propFile = "Maps"+Utilities.SEP+"OfficeIds.properties";
    private static String setupFile = "Maps"+Utilities.SEP+"StormReportSettings.properties";
    private Properties stormReportOfficeIds = Utilities.readPrefs(setupFile);
    private static StormReportSettings1 me = null;
    
    
    /**
     * Creates new form StormReportSettings
     */
    private StormReportSettings1() {
        initComponents();
         String v = stormReportOfficeIds.getProperty("StormReportOfficeIds").toString();
            String[] arr;
            if (v.contains(",")) {
                arr = v.split(",");
            } else {
                 arr = new String[1];
                arr[0] = v;
            }
           this.officeIds = arr;
           String vv = "0";
//        if (this.stormReportOfficeIds.containsKey("AutomaticOfficeIds")) {
//             vv = stormReportOfficeIds.getProperty("AutomaticOfficeIds");
//            
//        }
        setupComponents();
//        if (vv.equals("1")) {
//            this.jCheckBox1.setSelected(true);
//            setTabsEnabled(false);
//
//        } else {
//            this.jCheckBox1.setSelected(false);
//            setTabsEnabled(true);
//
//        }
    }
    public static StormReportSettings1 getInstance() {
        if (me == null) me = new StormReportSettings1();
        return me;
    }
    public void centerDialog(Component parent,String[] active_states) {
        
        this.active_states = active_states;
        setupComponents();
        this.setLocation(parent.getX(),parent.getY());
        this.setVisible(true);
    }
    
    private void setTabsEnabled(boolean value) {
        Color c = Color.WHITE;
        if (value == false) {
           jTabbedPane1.setEnabled(false);
           backgroundPanel.setBackground(Color.lightGray);
           c = Color.lightGray;
        } else {
            jTabbedPane1.setEnabled(true);
            backgroundPanel.setBackground(Color.white);
            c = Color.white;
        }
        for (int i=0; i<officeCbs.size(); i++) {
            officeCbs.get(i).setBackground(c);
            if (value == false) {
                officeCbs.get(i).setEnabled(false);
            } else {
                officeCbs.get(i).setEnabled(true);
            }
        }
        repaint();
    }
    private void setupComponents() {
        for (int i=0; i<Utilities.states.length; i++) {
            JPanel p = new JPanel(new GridLayout(0,1));
            p.setOpaque(true);
            panelHash.put(Utilities.states[i], p);
        }
        boolean saveProps = false;
        Properties props = getOfficeIdsFromProperties();
        String city = "";
        String st_abbr = "";
        if (props == null) {
            props = new Properties();
            saveProps = true;
        }
        for (int i=0; i<officeIds.length; i++) {
            if (props.containsKey(officeIds[i])) {

            } else {
                String dlink = "https://api.weather.gov/offices/" + officeIds[i];
                System.out.println(dlink);
                String json = Utilities.downloadJsonData(dlink,5000);
                if (json != null && json.equals("") == false) {
                    JSONObject obj = new JSONObject(json);
                    
                     city = obj.getJSONObject("address").getString("addressLocality");
                     st_abbr = obj.getJSONObject("address").getString("addressRegion");
                    props.put(officeIds[i], city+"|"+st_abbr);
                } else {
                    city = "";
                    st_abbr = "";
                    props.put(officeIds[i],"");
                }
                
            }
          
        }
        if (saveProps == true) Utilities.savePrefs(props, propFile);
        populateTabbedPane(props);
        
    }
    private boolean isActiveState(String id) {
        for (int i=0; i<active_states.length; i++) {
            if (active_states[i].equals(id)) {
                return true;
            }
        }
        return false;
    }
    private boolean isHighlightedState(String id, ArrayList<String> list) {
         for (int i=0; i<list.size(); i++) {
            if (list.get(i).equals(id)) {
                return true;
            }
        }
        return false;
    }
    private void populateTabbedPane(Properties props) {
        ArrayList<NwsOffice> list = sortOfficeProps(props);
        ArrayList<String> highlightedStates = new ArrayList<>();
        for (int i=0; i<active_states.length; i++) {
            highlightedStates.add(active_states[i]);
        }
       officeCbs.clear();
        for (int i=0; i<list.size(); i++) {
          String city = list.get(i).getCityName();
          String st_abbr = list.get(i).getStateAbbr();
          String off_id = list.get(i).getOfficeId();
          if (city.equals("") == false && st_abbr.equals("") == false) {
                JCheckBox cb = new JCheckBox(city+", "+st_abbr);
                cb.setFont(font);
                cb.setName(off_id);
                cb.setOpaque(true);
//                if (jCheckBox1.isSelected()) {
//                    cb.setEnabled(false);
//                } else {
//                    cb.setEnabled(true);
//                }
                if (isConfigured(off_id)) {
                    System.out.println(off_id+" isConfigured for "+st_abbr);
                    if (highlightedStates.contains(st_abbr) == false) {
                        System.out.println("Add "+st_abbr+" to highlighted states");
                        highlightedStates.add(st_abbr);
                    } else {
                        System.out.println("ERROR");
                    }
                    cb.setSelected(true);
                } else {
                    cb.setSelected(false);
                }
                officeCbs.add(cb);
                panelHash.get(st_abbr).add(cb);
            }
        }
       jTabbedPane1.removeAll();
        for (int i=0; i<highlightedStates.size(); i++) {
            String st = highlightedStates.get(i);
            //System.out.println("")
            JPanel p = panelHash.get(st);
            //p.setBackground(Color.white);
            p.setOpaque(false);
            jTabbedPane1.add(p);
            jTabbedPane1.setTitleAt(i, st);
            jTabbedPane1.setBackgroundAt(i,Color.white);
        }
        
        
        for (int i=0; i<list.size(); i++) {
            String st = list.get(i).getStateAbbr();
            String city = list.get(i).getCityName();
            if (st.equals("") == false && city.equals("") == false && isHighlightedState(st,highlightedStates) == false) {
                JPanel p = panelHash.get(st);
                //p.setBackground(Color.lightGray);
                p.setOpaque(false);
                jTabbedPane1.add(p);
               int v = jTabbedPane1.getTabCount()-1;
               jTabbedPane1.setTitleAt(v, st);
               jTabbedPane1.setBackgroundAt(v,Color.lightGray);
            }
        }
    }
    
    private ArrayList<NwsOffice> sortOfficeProps(Properties p) {
        Enumeration en = p.keys();
        String[] sortableArray = new String[p.size()];
        int index = 0;
        while (en.hasMoreElements()) {
            String id = en.nextElement().toString();
            String v = p.get(id).toString();
            //System.out.println(v);
            String city = "M";
            String st = "M";
            if (id.equals("") == false && v.equals("") == false) {
                
                String[] arr = v.split("\\|");
               // System.out.println("split "+v+" at | "+arr[0] +" "+arr[1]);
                 city = arr[0];
                 st = arr[1];
            }
            sortableArray[index] = st + "|" + city + "|" + id;
            //System.out.println(sortableArray[index]);
            index++;
            
        }
        Arrays.sort(sortableArray);
        ArrayList<NwsOffice> list = new ArrayList<>();
        for (int i=0; i<sortableArray.length; i++) {
            //System.out.println(sortableArray[i]);
            String[] arr = sortableArray[i].split("\\|");
            if (arr[0].equals("M") == false) {
                NwsOffice nwsOffice = new NwsOffice(arr[2],arr[0],arr[1]);
                list.add(nwsOffice);
            }
        }
        return list;
    }
    
    private void saveSettings() {
        if (stormReportOfficeIds == null) {
            stormReportOfficeIds = new Properties();
        } else {
            stormReportOfficeIds.clear();
        }
        String ids = "";
        for (int i=0; i<officeCbs.size(); i++) {
            if (officeCbs.get(i).isSelected()) {
                System.out.println("officeCbs:"+officeCbs.get(i).getName()+" isSelected");
                ids = ids + officeCbs.get(i).getName()+",";
            }
        }
        System.out.println("check boxs are checked for "+ids);
        int end = ids.length() -1 ;
        if (ids.length() > 2) {
           stormReportOfficeIds.put("StormReportOfficeIds", ids.substring(0,end));
        } else {
            stormReportOfficeIds.put("StormReportOfficeIds", "");
        }
        boolean auto = false;
//        if (jCheckBox1.isSelected()) {
//            stormReportOfficeIds.put("AutomaticOfficeIds","1");
//            auto = true;
//        } else {
            stormReportOfficeIds.put("AutomaticOfficeIds","0");
//            auto = false;
//        }
        Utilities.savePrefs(stormReportOfficeIds, setupFile);
//        if (jCheckBox1.isSelected() == false) {
           StormReportLayer.getInstance().setOfficeIds(ids.split(","));
//        }
    }
    
    private boolean isConfigured(String officeId) {
        if (stormReportOfficeIds != null && stormReportOfficeIds.containsKey("StormReportOfficeIds")) {
            String v = stormReportOfficeIds.getProperty("StormReportOfficeIds").toString();
            String[] arr;
            if (v.contains(",")) {
                arr = v.split(",");
            } else {
                 arr = new String[1];
                arr[0] = v;
            }
            for (int i=0; i<arr.length; i++) {
                if (arr[i].equals(officeId)) {
                    return true;
                }
            }
                
        } else {
            String[] ids = StormReports.getInstance().getOfficeIds();
            for (int i=0; i<ids.length; i++) {
                if (ids[i].equals(officeId)) {
                    return true;
                }
            }
        }
        return false;
    }
    private Properties getOfficeIdsFromProperties() {
         return Utilities.readPrefs(propFile);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        backgroundPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setTitle("Storm Report Reporting Stations");
        setMinimumSize(new java.awt.Dimension(750, 500));
        setSize(new java.awt.Dimension(350, 750));

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));
        backgroundPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        backgroundPanel.setLayout(new java.awt.GridLayout(1, 0));

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        backgroundPanel.add(jTabbedPane1);

        getContentPane().add(backgroundPanel, java.awt.BorderLayout.CENTER);

        jButton1.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1);

        jButton2.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jButton2.setText("Close");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        setVisible(false);
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveSettings();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new StormReportSettings1().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    private class NwsOffice {
        String cityname;
        String state_abbr;
        String office_id;
        
        public NwsOffice(String id, String state, String city) {
            cityname = city;
            state_abbr = state;
            office_id = id;
        }
        
        public String getOfficeId() {
            return office_id;
        }
        
        public String getStateAbbr() {
            return state_abbr;
        }
        
        public String getCityName() {
            return cityname;
        }
    }
    
}
