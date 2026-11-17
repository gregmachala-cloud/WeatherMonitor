/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package utilities;

import weathermonitor.layers.StormReportLayer;
import weathermonitor.dataobjects.StormReportInfo;
import utilities.FontChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JRadioButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;
import weathermonitor.WeatherMonitorRun;
//import static weathermonitor.WeatherMonitorRun.STORM_REPORT_OFFSET_SECS;

/**
 *
 * @author greg
 */
public class Viewer extends javax.swing.JFrame implements ItemListener,BboxListener {
    private static Viewer viewer = null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Viewer.class.getName());
    private Font font = new Font("SansSerif",Font.BOLD,18);
    private ArrayList<StormReportInfo> allStormReports = null;
    private boolean showAllStormReports = false;
    private String[] officeIds = new String[0];
   private double lat = 31.1;
   private double lon = -100.1;
   private static double id = Math.random();
   private String selectedOffice = "SJT";
   private ArrayList<JRadioButton> oidCbs = new ArrayList<>();
  
   private static final Properties stormReportOfficeIds = Utilities.getInstance().officeIdsProps;
   
   private int numStormReportHours;
   private Hashtable<String,String> iconHash = new Hashtable<>();
   
    public static Viewer getInstance() {
       if (viewer == null) viewer = new Viewer();
       return viewer;
   }
    /**
     * Creates new form Viewer
     */
    private Viewer() {
        initComponents();
        String[] iconNames = StormReportInfo.iconNames;
        for (int i=0; i<iconNames.length; i++) {
            String src = getClass().getResource("/weathermonitor/icons/"+iconNames[i]).toString();
            iconHash.put(iconNames[i],src);
        }
        this.setBackground(Color.WHITE);
        this.jEditorPane1.setBackground(Color.WHITE);
        jEditorPane1.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if  (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                System.out.println("User clicked the link");
                if (selectedOffice != null) {
                    showAllStormReports(selectedOffice);
                } else {
                    showAllStormReports();
                }
            }
         }
        });
//         StyleSheet styleSheet = new StyleSheet();
//        styleSheet.addRule("td { vertical-align:bottom; } ");
//        HTMLEditorKit messageEditorPaneHTMLEditorKit = new HTMLEditorKit();
//        messageEditorPaneHTMLEditorKit.setStyleSheet(styleSheet);
//        HTMLDocument document = (HTMLDocument) messageEditorPaneHTMLEditorKit.createDefaultDocument();
//        jEditorPane1.setEditorKit(messageEditorPaneHTMLEditorKit);
//        jEditorPane1.setDocument(document);
        
        readProps();
        //jTextPane1.setContentType("text/html");
    }
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
    public void setVisible(boolean v) {
        super.setVisible(v);
        if (v == false) {
            allStormReports = null;
            showAllStormReports = false;
            topPanel.removeAll();
            oidCbs.clear();
        }
    }
    public void setViewerFont(Font f) {
        System.out.println("Font is "+f.toString());
        this.font = f;
        this.jEditorPane1.setFont(f);
        this.jEditorPane1.repaint();
    }
      public void viewForecast(String title, String html,  Component parent) {
         showAllStormReports = false;
          topPanel.removeAll();
        oidCbs.clear();
        topPanel.setVisible(false);
        jEditorPane1.setContentType("text/html");
          jEditorPane1.setText(html);
        
        jEditorPane1.setCaretPosition(0);
        setTitle(title);
       
        int w = 1000;
        int h = 600;
        this.setSize(w,h);
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
          java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
       
    }
//     private String[] getOfficeIds(ArrayList<StormReportInfo> list) {
//         ArrayList<String> oids= new ArrayList<String>();
//         for (int i=0; i<list.size(); i++) {
//             if (oids.contains(list.get(i).getOffice()) == false) {
//                 oids.add(list.get(i).getOffice());
//             }
//         }
//         String[] oidsArray = new String[oids.size()];
//         for (int i=0; i<oids.size(); i++) {
//             oidsArray[i] = oids.get(i);
//         }
//         return oidsArray;
//     }
    public boolean isShowingAllStormReports() {
        return showAllStormReports;
    }
    //used to update the storm reports in the viewr when show all storm reports
    // is selected in the window
    public void setAllStormReports(ArrayList<StormReportInfo> reports) {
        
        allStormReports = reports;
        if (this.isVisible() && showAllStormReports == true) {
            System.out.println("Updating storm reports in teh Viewer");
            if (isFiltered() == false) showAllStormReports();
        }
    }
    private boolean isFiltered() {
        for (int i=0; i<oidCbs.size(); i++) {
            if (oidCbs.get(i).isSelected()) {
                return true;
            }
        }
        return false;
    }
    public void viewAllStormReports(ArrayList<StormReportInfo> list, int numHrs, Component parent) {
        this.allStormReports = list;
        this.numStormReportHours = numHrs;
        int w = 1000;
        int h = 600;
        this.setSize(w,h);
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
        showAllStormReports();
         java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
    }
    public void viewStormReports(String title,int numHrs,ArrayList<StormReportInfo> list, ArrayList<StormReportInfo> allReports, Component parent) {
        showAllStormReports = false;
        
        
        
        String text = "<html>"; String office="";
        for (int i=0; i<list.size(); i++) {
            //double miles = Utilities.getDistanceBetweenTwoPoints(list.get(i).getLon(), list.get(i).getLat(),lon,lat)*60.0;
            //String milesString = Utilities.roundDouble(miles, 0);
            String src = "";
            if (iconHash.containsKey(list.get(i).getIconName())) {
                 src = iconHash.get(list.get(i).getIconName());
            }
            String milesString = Utilities.roundDouble(list.get(i).getDistance(), 0);
            String line = "<img src=\""+src+"\"></img>&nbsp;<font color=red><b>" + list.get(i).getEventType() + " " + list.get(i).getEventMag()+"</b></font> - "+list.get(i).getDate() + " " + list.get(i).getTime() + " " + list.get(i).getCity() + ", " + list.get(i).getState()+" ("+milesString+" miles away)";
            office = list.get(i).getOffice();
            // String line = list.get(i).getDate() + " " + list.get(i).getTime() + " " + list.get(i).getCity() + ", "+list.get(i).getState()+"<font color=red><b> " + list.get(i).getEventType() + " " + list.get(i).getEventMag()+"</b></font>";
             text = text + line + "<br>"+list.get(i).getText()+"<br>";
        }
         String officeString = Utilities.getInstance().getShortOfficeName(office);
              
        if (numHrs > 72) {
            text = text+"<center><font color=blue><b><a href=\"http://weather.gov\">View All Storm Reports Issued By:"+officeString+"</a></b></font></center>";
      
        } else {
              text = text+"<center><font color=blue><b><a href=\"http://weather.gov\">View All Storm Reports Issued By: "+officeString+" From Last "+numHrs+" Hours</a></b></font></center>";
      
         }
         text = text+"</html>";
        
        
        
        
        topPanel.removeAll();
        oidCbs.clear();
        topPanel.setVisible(false);
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setText(text);
        this.allStormReports = allReports;
        this.numStormReportHours = numHrs;
        this.selectedOffice = office;
        jEditorPane1.setCaretPosition(0);
        setTitle(title);
       
        int w = 1000;
        int h = 600;
        this.setSize(w,h);
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
    
         java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
         
    }
//   
     public void viewWWA(String title, String text, String maxHailSize, String maxWindGust, Component parent) {
         showAllStormReports = false;
          topPanel.removeAll();
        oidCbs.clear();
        topPanel.setVisible(false);
         if (maxHailSize.trim().equals("") || maxHailSize.trim().equals("0.00")) {
            maxHailSize = "";
        } 
         String extraText = "";
         if (maxHailSize.equals("") == false) {
            extraText = " "+maxHailSize+"in Hail";
        }
        if (maxWindGust.equals("") == false) {
            extraText = extraText + " "+maxWindGust +" Wind";
        }
        text = "<html><b><font color=\"red\">"+title+extraText+"</font><b><br><br>"+text.replaceAll("\\n","<br>")+"</html>";
        System.out.println(text);
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setText(text);
        //jTextPane1.setFont(font);
        jEditorPane1.setCaretPosition(0);
        setTitle(title);
     
        int w = 1000;
        int h = 600;
        this.setSize(w,h);
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
         java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
    }
     public void viewText(String title, String text, Component parent) {
         showAllStormReports = false;
          topPanel.removeAll();
        oidCbs.clear();
        topPanel.setVisible(false);
     
        jEditorPane1.setContentType("text/plain");
        jEditorPane1.setText(text);
        //jTextPane1.setFont(font);
        jEditorPane1.setCaretPosition(0);
        setTitle(title);
     
        int w = 1000;
        int h = 600;
        this.setSize(w,h);
        int x = (parent.getWidth() - w)/2 + parent.getX();
        int y = (parent.getHeight() - h)/2 + parent.getY();
        this.setLocation(x, y);
         java.awt.EventQueue.invokeLater(new Runnable() {
              public void run() {
                  setVisible(true);
                  
              }
          });
    }
     
    private ArrayList<StormReportInfo> sortStormReportsByTime(ArrayList<StormReportInfo> list) {
        Hashtable<String, StormReportInfo> ht = new Hashtable<>();
        long[] arr = new long[list.size()];
        int suffix = 0;
        for (int i=0; i<list.size(); i++) {
            
            long time = list.get(i).getEventTime();
            String timeString = Long.toString(time);
            time = (time*1000)+suffix;
            
            ht.put(Long.toString(time),list.get(i));
            arr[i] = time;
            suffix++;
        }
        Arrays.sort(arr);
        ArrayList<StormReportInfo> sortedList = new ArrayList<StormReportInfo>();
        for (int i=arr.length-1; i>=0; i--) {
            String v = Long.toString(arr[i]);
            
//            int end = v.length()-3;
//            v = v.substring(0,end);
           
            if (ht.containsKey(v)) {
                sortedList.add(ht.get(v));
            } 
        }
       
       // System.out.println("input storm report list has size:"+list.size()+" sorted list has size:"+sortedList.size());
        return sortedList;
    }
    private String getToolTipText(String oid) {
        String toolTip = "";
        if (stormReportOfficeIds.containsKey(oid)) {
            String data = stormReportOfficeIds.get(oid).toString();
            String[] arr = data.split("\\|");
            String city = arr[0];
            String state = arr[1];
            toolTip = oid+"-"+city+", "+state;
            
        }
        return toolTip;
    }
    
    private String[] sortOidsByPreferredOffices(String[] oids) {
        String oidsString = Utilities.arrayToString(oids);
        if (oidsString.equals("")) {
            return new String[0];
        }
        System.out.println("unsorted oids:"+oidsString);
        ArrayList<String> sortedOids = new ArrayList<>();
        ArrayList<String> preferredOffices = StormReportLayer.getInstance().getPreferredOffices();
        for (int i=0; i<preferredOffices.size(); i++) {
            if (preferredOffices.get(i) != null && oidsString.contains(preferredOffices.get(i))) {
                sortedOids.add(preferredOffices.get(i));
            }
        }
        for (int i=0; i<oids.length; i++) {
            if (sortedOids.contains(oids[i]) == false) {
                sortedOids.add(oids[i]);
            }
        }
        String[] retVal = new String[sortedOids.size()];
        for (int i=0; i<sortedOids.size(); i++) {
            retVal[i] = sortedOids.get(i);
        }
        System.out.println("sorted by preferred oids:"+Utilities.arrayToString(retVal));
        return retVal;
    }
    private boolean populateCheckBoxPanel(String[] oids) {
        ButtonGroup bg = new ButtonGroup();
        topPanel.removeAll();
        //ArrayList<String> preferredOffices = StormReportLayer.getInstance().getPreferredOffices();
        oids = sortOidsByPreferredOffices(oids);
        oidCbs.clear();
        boolean selected = false;
        for (int i=0; i<oids.length; i++) {
            String toolTip = getToolTipText(oids[i]);
            String cbText = oids[i];
            if (oids.length <= 3) {
                cbText = toolTip;  
            } 
            JRadioButton cb = new JRadioButton(cbText);
            bg.add(cb);
            cb.setVisible(true);
            cb.setFont(font);
            if (oids[i].equals(selectedOffice)) {
                cb.setSelected(true);
                selected = true;
            }        
            cb.setName(oids[i]);
            cb.setToolTipText(toolTip);
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    filterStormReports();
                }
            });
            oidCbs.add(cb);
           
            topPanel.add(cb);
            
       } 
       return selected;
        
    }
    private void filterStormReports() {
        
        for (int i =0; i<oidCbs.size(); i++) {
            if (oidCbs.get(i).isSelected()) {
                selectedOffice = oidCbs.get(i).getName();
            }
        }
        
        showAllStormReports(selectedOffice);
       
    }
//    private void showAllStormReports(String office) {
//        ArrayList<String> oid = new ArrayList<String>();
//        oid.add(selectedOffice);
//        showAllStormReports(oid);
//    }
    private void showAllStormReports(String selectedOffice) {
        //String nearestOffice = StormReports.getInstance().nearestReportOffice;
        showAllStormReports = true;
          String text = "<html>";
          int numReports = 0;
        long curSecs = new Date().getTime()/1000;
        long minTime = curSecs - this.numStormReportHours*60*60;
        ArrayList<StormReportInfo> sorted = sortStormReportsByTime(allStormReports);
        for (int i=0; i<sorted.size(); i++) {
             if (sorted.get(i).getOffice().equals(selectedOffice) && sorted.get(i).getEpoch() >= minTime) {
                //double miles = Utilities.getDistanceBetweenTwoPoints(sorted.get(i).getLon(), sorted.get(i).getLat(),lon,lat)*60.0;
                //String milesString = Utilities.roundDouble(miles, 0);
                String src = "";
                if (iconHash.containsKey(sorted.get(i).getIconName())) {
                     src = iconHash.get(sorted.get(i).getIconName());
                }
                String milesString = Utilities.roundDouble(sorted.get(i).getDistance(), 0);
                String line = "<img src=\""+src+"\"></img><font color=red><b>&nbsp;" + sorted.get(i).getEventType() + " " + sorted.get(i).getEventMag()+"</b></font> - "+sorted.get(i).getDate() + " " + sorted.get(i).getTime() + " " + sorted.get(i).getCity() +" ,"+ sorted.get(i).getState()+" ("+milesString+" miles away)";
                text = text + line + "<br>"+sorted.get(i).getText()+"<br>";
                numReports++;
             }
        }
        if (numReports == 0) {
            text = text +"No Storm For Selected Offices In Last "+numStormReportHours+" Hours";
        }
        text = text+"</html>";
        String officeName = "";
        for (int i=0; i<oidCbs.size(); i++) {
            if (oidCbs.get(i).isSelected()) {
                officeName = oidCbs.get(i).getToolTipText();
                text = "<center><b>Storm Reports From: "+officeName+"</b></center><hr>"+text;
            }
        }
        jEditorPane1.setContentType("text/html");
        jEditorPane1.setText(text);
        String title = "";
        if (numStormReportHours > 72) {
            //if (selectedOffices.size() == 1) {
                String officeString = Utilities.getInstance().getLongOfficeName(selectedOffice);
               title = "Viewing All Storm Reports Issued By:"+officeString; 
           // }
        } else {
            // if (selectedOffices.size() == 1) {
               String officeString = Utilities.getInstance().getShortOfficeName(selectedOffice);
               title = "All Storm Reports Issued By:"+officeString+" From Last "+numStormReportHours +" Hours";
           // } else {
           //    title = "All Storm Reports From Last "+numStormReportHours +" Hours";
           // }
            
        }
        this.setTitle(title);
        jEditorPane1.setCaretPosition(0);
    }
    
//    public String getNearestActiveReportOffice(long minSeconds) {
//        return StormReports.getNearestActiveReportOffice(minSeconds);
//    }
//        public String getNearestActiveReportOffice(ArrayList<StormReportInfo> lsrData, long minSeconds) {
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
    private void showAllStormReports() {
        
        topPanel.setVisible(true);
        showAllStormReports = true;
          String text = "<html>";
          int numReports = 0;
        long curSecs = new Date().getTime()/1000;
        long minTime = curSecs - this.numStormReportHours*60*60;
        
        ArrayList<StormReportInfo> sorted = sortStormReportsByTime(allStormReports);
        this.selectedOffice = Mapping.cwa;
        System.out.println("nearest active report is from office "+selectedOffice);
        ArrayList<String> oids = new ArrayList<>();
        
        for (int i=0; i<sorted.size(); i++) {
             if (sorted.get(i).getEpoch() >= minTime) {
                 if (oids.contains(sorted.get(i).getOffice()) == false) {
                     oids.add(sorted.get(i).getOffice());
                 }
                double miles = sorted.get(i).getDistance();
                String milesString = Utilities.roundDouble(miles, 0);
                 String src = "";
                if (iconHash.containsKey(sorted.get(i).getIconName())) {
                     src = iconHash.get(sorted.get(i).getIconName());
                }
                 if (selectedOffice != null) {
                     if (sorted.get(i).getOffice().equals(selectedOffice)) {
                         String line = "<img src=\"" + src + "\"></img><font color=red><b>&nbsp;" + sorted.get(i).getEventType() + " " + sorted.get(i).getEventMag() + "</b></font> - " + sorted.get(i).getDate() + " " + sorted.get(i).getTime() + " " + sorted.get(i).getCity() + ", " + sorted.get(i).getState() + " (" + milesString + " miles away)";
                         text = text + line + "<br>" + sorted.get(i).getText() + "<br>";
                     }
                 } else {
                     String line = "<img src=\"" + src + "\"></img><font color=red><b>&nbsp;" + sorted.get(i).getEventType() + " " + sorted.get(i).getEventMag() + "</b></font> - " + sorted.get(i).getDate() + " " + sorted.get(i).getTime() + " " + sorted.get(i).getCity() + ", " + sorted.get(i).getState() + " (" + milesString + " miles away)";
                     text = text + line + "<br>" + sorted.get(i).getText() + "<br>";

                 }
                numReports++;
             }
        }
        officeIds = new String[oids.size()];
        for (int i=0; i<oids.size(); i++) {
            officeIds[i] = oids.get(i);
        }
        boolean checkBoxSelected = populateCheckBoxPanel(officeIds);
        if (numReports == 0) {
            text = text +"No New Storm Reports In Last "+numStormReportHours+" Hours";
        }
        text = text+"</html>";
        if (checkBoxSelected) {
            jEditorPane1.setContentType("text/html");
            jEditorPane1.setText(text);
            String title = "";
            if (numStormReportHours > 72) {
                title = "Viewing All Storm Reports";
            } else {
                title = "All Storm Reports From Last " + numStormReportHours + " Hours";
            }
            this.setTitle(title);
            jEditorPane1.setCaretPosition(0);
//         int w = 1000;
//        int h = 600;
//        this.setSize(w,h);
            topPanel.revalidate();
            topPanel.repaint();
            topPanel.setVisible(true);
        } else if (officeIds.length >= 1) {
            
            selectedOffice = officeIds[0];
            this.filterStormReports();
            oidCbs.get(0).setSelected(true);
        }
        //java.awt.EventQueue.invokeLater(() -> repaint());
    }
//    public void showViewer(String title, String text, String maxHailSize, String maxWindGust, Component parent) {
//        if (maxHailSize.trim().equals("") || maxHailSize.trim().equals("0.00")) {
//            maxHailSize = "";
//        } 
//         String extraText = "";
//         if (maxHailSize.equals("") == false) {
//            extraText = " "+maxHailSize+"in Hail";
//        }
//        if (maxWindGust.equals("") == false) {
//            extraText = extraText + " "+maxWindGust +" Wind";
//        }
//        text = "<html><b><font color=\"red\">"+title+extraText+"</font><b><br><br>"+text.replaceAll("\\n","<br>")+"</html>";
//        
//        //text = "<html><b><font color=\"red\">"+title+" "+maxHailSize+"in Hail "+maxWindGust+" Wind</font></b><br><br>"+text.replaceAll("\\n","<br>")+"</html>";
//       System.out.println(text);
//        jEditorPane1.setText(text);
//        jEditorPane1.setFont(font);
//        jEditorPane1.setCaretPosition(0);
//        setTitle(title);
//        pack();
//         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
//        int scrW = d.width;
//        int scrH = d.height;
//        
//        int w = 950;
//        int h = 550;
//        this.setSize(w,h);
//        int x = parent.getX() + (scrW/2) - (w/2);
//        int y = parent.getY() + (scrH/2) - (h/2);
//        this.setLocation(x, y);
//        this.setContentPane(this.jPanel3);
//        //this.pack();
//        setVisible(true);
//        java.awt.EventQueue.invokeLater(() -> repaint());
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Text Product Viewer");

        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Filter Storm Reports By Office", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 18))); // NOI18N
        topPanel.setMinimumSize(new java.awt.Dimension(22, 65));
        topPanel.setLayout(new java.awt.GridLayout(1, 0));
        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton2.setText("Font");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, java.awt.BorderLayout.WEST);

        jButton1.setText("Close");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, java.awt.BorderLayout.CENTER);

        jButton3.setText("Print");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel1.add(jButton3, java.awt.BorderLayout.EAST);

        jPanel3.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        jPanel2.add(jScrollPane1);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        FontChooser.getInstance().showDialog(this,jEditorPane1.getFont());
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            jEditorPane1.print();
        } catch (PrinterException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            //System.out.println("Storm Report hours combo changed");
            JComboBox cb = (JComboBox) e.getSource();
            int index = cb.getSelectedIndex();
           // System.out.println("selected index="+index);
            this.numStormReportHours = WeatherMonitorRun.STORM_REPORT_OFFSET_SECS[index]/60/60;
            //System.out.println("numHrs="+numStormReportHours);
            if (this.isVisible() && showAllStormReports == true) {
                showAllStormReports();
            } 
        }
    }

    @Override
    public void bboxChanged(String bbox, int w, int h, double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public double getID() {
        return id;
    }
}
