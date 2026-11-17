/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package weathermonitor.settings;

import spcmap.SPCMapViewer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.JTextField;
import utilities.Utilities;

/**
 *
 * @author greg
 */
public class SPCMapSettings extends javax.swing.JFrame {
    private String[] outlooks = {"https://www.spc.noaa.gov/products/outlook/day1otlk_cat.nolyr.geojson",
                                       "https://www.spc.noaa.gov/products/outlook/day2otlk_cat.nolyr.geojson",
                                       "https://www.spc.noaa.gov/products/outlook/day3otlk_cat.nolyr.geojson"};
    private  String[] outlookNames = {"Day 1","Day 2","Day 3"};
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SPCMapSettings.class.getName());
    private static Font font = new Font("Monospaced",Font.PLAIN,18);
    private ArrayList<JTextField> nameTFs = new ArrayList<>();
    private ArrayList<JTextField> linkTFs = new ArrayList<>();
    private Properties props = null;
    private static SPCMapSettings me = null;
    private SPCMapViewer listener = null;
    
    public static SPCMapSettings getInstance() {
        if (me == null) me = new SPCMapSettings();
        return me;
    }
    
    private SPCMapSettings() {
        initComponents();
        this.props = readProps();
        
        for (int i=0; i<20; i++) {
            JTextField tf1 = new JTextField(25);
            JTextField tf2 = new JTextField(80);
            int v = i+1;
            if (outlooks.length >= v) {
               tf1.setText(outlookNames[i]);
               tf2.setText(outlooks[i]);
            }
            tf2.setFont(font);
            tf2.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                   // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton()==MouseEvent.BUTTON3) {
                        try {
                            Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
                            Transferable data = board.getContents(null);
                            String text = data.getTransferData(DataFlavor.stringFlavor).toString();
                            JTextField tf = (JTextField) e.getSource();
                            tf.setText(text);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                   // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                   // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            });
            tf1.setFont(font);
            linkTFs.add(tf2);
            nameTFs.add(tf1);
            JPanel p = new JPanel(new BorderLayout());
            p.add(tf1,BorderLayout.WEST);
            p.add(tf2,BorderLayout.CENTER);
            centerPanel.add(p);
        }
    }
    
    public void setSettingsListener(SPCMapViewer viewer) {
        this.listener = viewer;
    }
    private void saveProps() {
        String n = "";
        String l = "";
        for (int i=0; i<20; i++) {
            String name = nameTFs.get(i).getText().trim();
            String link = linkTFs.get(i).getText().trim();
            if (name.equals("") == false && link.equals("") == false) {
                n = n + "," + name;
                l = l + "," + link;
            }
        }
        props.put("names",n.substring(1));
        props.put("links",l.substring(1));
        outlooks = props.getProperty("links").split(",");
        outlookNames = props.getProperty("names").split(",");
        Utilities.savePrefs(props, "Maps"+Utilities.SEP+"SPCMapSettings.properties");
        if (listener != null) listener.readSettings();
    }
    
    public String[] getOutlookLinks() {
        return outlooks;
    }
    public String[] getOutlookNames() {
        return outlookNames;
    }
    private Properties readProps() {
        Properties props = Utilities.readPrefs("Maps"+Utilities.SEP+"SPCMapSettings.properties");
        if (props == null) {
            props = new Properties();
            String links = ""; String names = "";
            for (int i=0; i<outlooks.length; i++) {
                links = links+","+outlooks[i];
                names = names+","+outlookNames[i];
            }
            props.put("names",names.substring(1));
            props.put("links",links.substring(1));
        } 
        if (props.containsKey("names") && props.containsKey("links")) {
            try {
                outlooks = props.getProperty("links").split(",");
                outlookNames = props.getProperty("names").split(",");
                System.out.println("Got SPC Map Settings from properties file");
            } catch (Exception ex) {
                System.out.println("Failed to get links from properties");
            }
        }
        return props;
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
        centerPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SPC Map Settings");

        jButton1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1);

        jButton2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jButton2.setText("Close");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        centerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        centerPanel.setMaximumSize(new java.awt.Dimension(1200, 900));
        centerPanel.setMinimumSize(new java.awt.Dimension(900, 850));
        centerPanel.setPreferredSize(new java.awt.Dimension(1000, 850));
        centerPanel.setLayout(new java.awt.GridLayout(0, 1));
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveProps();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new SPCMapSettings().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
