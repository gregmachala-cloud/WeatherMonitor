package weathermonitor.layers;

import weathermonitor.dataobjects.ForecastData;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.json.JSONArray;
import org.json.JSONObject;
import mappingtools.BboxListener;
import utilities.Utilities;
import weathermonitor.listeners.NetworkListener;
import weathermonitor.threads.Timer;
import weathermonitor.listeners.TimerListener;
import utilities.Viewer;
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
public class ForecastTable extends JPanel implements BboxListener,TimerListener,NetworkListener {
    private boolean locationChanged = true;
    private static Font font = new Font("SansSerif",Font.BOLD,16);
    private double lat = 31.1;
    private double lon = -101.1;
    private boolean noForecast = true;
    private ArrayList<ForecastData> forecastData = new ArrayList<>();
    private static int iconWidth = 90;
    private int numPeriods = 5;
    public static double id = Math.random();
    private JLabel[] fcstLabel = new JLabel[12];
    private TitledBorder[] titledBorder = new TitledBorder[12];
    private JLayeredPane container;
    private WeatherMonitorRun parent;
    private WMTimer updateTimer;
    private int container_height = 0;
    private int container_width = 0;
    private static ForecastTable me = null;
    private boolean networkUp = true;
    private boolean updating = false;
    
    public static ForecastTable getInstance() {
        if  (me == null) me = new ForecastTable();
        return me;
    }
    private ForecastTable() {
        
    }
    
    public void initForecastTable(double lat, double lon, JLayeredPane container,int periods, WeatherMonitorRun parent) {
        this.lat = lat;
        this.lon = lon;
        
        numPeriods =  container.getHeight()/125;
        this.container = container; 
        this.container_width = container.getWidth();
        this.container_height = container.getHeight();
       // System.out.println("Create forecast table with size w="+container_width+" heihgt="+container_height);
        int x = container.getWidth()-iconWidth-14;
        int y = 0;
        setLocation(x,y);
        //this.width = iconWidth+2;
        //this.height = container.getHeight();
        Dimension d = new Dimension(iconWidth+14,container_height);
        Dimension dl = new Dimension(iconWidth+10,iconWidth+20);
        setPreferredSize(d);
        setMaximumSize(d);
        setMinimumSize(d);
        setSize(d);
       // System.out.println("Set forecast table location "+x+" "+y);
        //System.out.println("Set forecast suize "+width+" "+height);
        
        this.parent = parent;
        this.setOpaque(true);
        setBackground(new Color(245,245,245));
        this.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        
        this.setLayout(new GridLayout(0,1,0,0));
        
          for (int i=0; i<12; i++){
            fcstLabel[i] = new JLabel();
            fcstLabel[i].setHorizontalAlignment(JLabel.CENTER);
            fcstLabel[i].setHorizontalTextPosition(JLabel.CENTER);
            fcstLabel[i].setPreferredSize(dl);
            fcstLabel[i].setSize(dl);
            fcstLabel[i].setMinimumSize(dl);
            fcstLabel[i].setMaximumSize(dl);
            fcstLabel[i].setVerticalTextPosition(JLabel.BOTTOM);
            titledBorder[i] = javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new Color(50,50,50), 1), "Loading", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", Font.BOLD, 14));
            fcstLabel[i].setBorder(titledBorder[i]);
            if (i < numPeriods) this.add(fcstLabel[i]);
          }
        updateTimer = new WMTimer(500,"ForecastTable",new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Update forecast table");
                update();
            }
        });
        
        updateTimer.setRepeats(false);
        updateTimer.start();
        this.setVisible(true);
        
    }
    @Override
    public int getWidth() {
        return iconWidth+14;
    }
    public void setNumPeriods(int value) {
        this.numPeriods = value;
        Dimension dl = new Dimension(iconWidth+10,iconWidth+20);
        this.removeAll();
       for (int i=0; i<12; i++){
            fcstLabel[i] = new JLabel();
           
            fcstLabel[i].setPreferredSize(dl);
            fcstLabel[i].setSize(dl);
            fcstLabel[i].setMinimumSize(dl);
            fcstLabel[i].setMaximumSize(dl);
            fcstLabel[i].setHorizontalAlignment(JLabel.CENTER);
            fcstLabel[i].setHorizontalTextPosition(JLabel.CENTER);
            fcstLabel[i].setVerticalTextPosition(JLabel.BOTTOM);
            titledBorder[i] = javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new Color(50,50,50), 1), "Loading", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 1, 12));
            fcstLabel[i].setBorder(titledBorder[i]);
            if (i < numPeriods) this.add(fcstLabel[i]);
          }
        
        //System.out.println("set num fcst periods "+numPeriods);
        updateTimer.restart();
    }
    private void clearForecastTable() {
         for (int i=0; i<numPeriods; i++){
            try {
            titledBorder[i].setTitle("");
            fcstLabel[i].setIcon(null);
            fcstLabel[i].setText("");
            noForecast = true;
            } catch (Exception ex ) {}
          }
    }
    public void showForecastTable(boolean value) {
        setVisible(value);
    }
    private String shortenDays(String day) {
       String thing =  day.replaceAll("Friday Night", "Fri Night").
            replaceAll("Saturday Night", "Sat Night").
            replaceAll("Sunday Night", "Sun Night").   
            replaceAll("Monday Night", "Mon Night").
            replaceAll("Tuesday Night", "Tue Night").
            replaceAll("Wednesday Night", "Wed Night").
            replaceAll("Thursday Night", "Thu Night").
            replaceAll("This Afternoon", "Today");
       return thing;
        
    }
    private Color getTextColor(String loworhigh) {
        if (loworhigh.toLowerCase().contains("low")) {
            return Color.BLUE;
        } else if (loworhigh.toLowerCase().contains("high")) {
            return Color.RED;
        }
        return Color.black;
    }
    public void update() {
       
        Thread t = new Thread() {
            public void run() {
                try {
                    int timeout = 0;
                    while (updating == true && timeout <= 30 && Timer.getInstance().isInternetUp() == true) {
                        System.out.println("Waiting for ForecastTable to load");
                        sleep(500);
                        timeout++;
                    }
                    if (Timer.getInstance().isInternetUp() == true) {

                        updating = true;

                        System.out.println("Doing update in ForecastTable");
                        if (forecastData != null) {
                            forecastData.clear();
                        } else {
                            forecastData = new ArrayList<>();
                        }

                        forecastData.addAll(getTheForecastAt(lat, lon));
                        if (forecastData != null && forecastData.size() > 0) {

                            for (int i = 0; i < numPeriods; i++) {
                                final int index = i;
                                fcstLabel[i].setText(forecastData.get(i).getLabel() + " " + forecastData.get(i).getTemp());
                                if (forecastData.get(i).getPop().equals("") == false) {
                                    if (WeatherMonitorRun.enabledTooltips) fcstLabel[i].setToolTipText("<html><center><font size=5>Chance of precipitation "+forecastData.get(i).getPeriodName()+": "+forecastData.get(i).getPop()+"%<br><font size=2>Click For Details</font></center></html>");
                                } else if (i < numPeriods-1) {
                                    if (WeatherMonitorRun.enabledTooltips) fcstLabel[i].setToolTipText("Click For Details");
                                } else if (i == numPeriods-1) {
                                    if (WeatherMonitorRun.enabledTooltips) fcstLabel[i].setToolTipText("Click For Remainder Of The Forecast");
                                }
                                fcstLabel[i].setFont(font);
                                fcstLabel[i].setHorizontalAlignment(JLabel.CENTER);
                                fcstLabel[i].setHorizontalTextPosition(JLabel.CENTER);
                                fcstLabel[i].setForeground(getTextColor(forecastData.get(i).getLabel()));
                                fcstLabel[i].setIcon(downloadIcon(forecastData.get(i).getIconLink()));
                                String title = shortenDays(forecastData.get(i).getPeriodName());
                                titledBorder[i].setTitle(title);
                                fcstLabel[i].addMouseListener(new java.awt.event.MouseAdapter() {
                                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                                        getForecastText(index);
                                    }
                                });
                            }
                            noForecast = false;
                        } else {
                            noForecast = true;
                            System.out.println("Forecast data is null...");
                        }

                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
                updating = false;

            }
        };
        t.start();

    }
    private void getForecastText(int index) {
        if  (index < numPeriods-1) {
            Viewer.getInstance().viewForecast("Forecast For "+forecastData.get(index).getPeriodName(),"<b>"+forecastData.get(index).getPeriodName()+"...</b>"+forecastData.get(index).getHighlightedFcstTextHtml()+"</html>",parent);
        } else {
            /// show whole forecast
            String text = "<html>";
           
            String[] bg = new String[2];
             bg[0] = "#ffffdd";
             bg[1] = "#ffffff";
             int n =0;
             int thePeriod = numPeriods - 1;
             text = text + "<div bgcolor=white><b>"+forecastData.get(thePeriod).getPeriodName()+"...</b>"+forecastData.get(thePeriod).getHighlightedFcstTextHtml()+"</div><div bgcolor=white>&nbsp;</div><div style=\"border:2px solid black\"><div bgcolor=white><center><b>---Remainder Of The Forecast ---</b></center></div>";
             //Draw the icons
             text = text+"<table border=1 width=100% ><tr>";
             for (int i=numPeriods; i<forecastData.size(); i++) {
                 String label = forecastData.get(i).getLabel();
                  String c = "blue";
                  if (label.toLowerCase().equals("high")) {
                      c = "red";
                  }
                 text = text +"<td><center><font size=2>"+shortenDays(forecastData.get(i).getPeriodName())+"</font></center><br>";
                 text = text +"<center><img src=\""+forecastData.get(i).getIconLink()+"\"></img></center><br>";
                 text=text+"<center><font color="+c+"><b>"+label+" "+forecastData.get(i).getTemp()+"</b></font></center></td>";
                 
             }
//            
              text = text +"</tr></center></table><br>";
             //Show forecast text below icon
            for (int i=numPeriods; i<forecastData.size(); i++) {
                
                text = text + "<div bgcolor="+bg[n]+"><b>"+forecastData.get(i).getPeriodName()+"...</b>"+forecastData.get(i).getHighlightedFcstTextHtml()+"</div>";
                if (n == 0) {
                    n = 1;
                } else {
                    n = 0;
                }
            }
            text = text + "</div></html>";
            Viewer.getInstance().viewForecast("Forecast For "+forecastData.get(thePeriod).getPeriodName(),text,parent);
        }
    }
    private ImageIcon makePopBgWhite(ImageIcon icon) {
        int h = icon.getIconHeight();
        int w = icon.getIconWidth();
        BufferedImage img = new BufferedImage (w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.drawImage(icon.getImage(),0,0,null);
        g.dispose();
        int white = new Color(255,255,255).getRGB();
        for (int i=0; i<w; i++) {
            for (int j=71; j<h; j++) {
                int rgb = img.getRGB(i, j);
                Color testColor = new Color(rgb);
                int red = testColor.getRed();
                int grn =  testColor.getGreen();
                int blu = testColor.getBlue();
                if (red > 200 && grn > 200 && blu > 200) {
                    img.setRGB(i, j, white);
                }
            }
        }
        
        icon = new ImageIcon(img);
        img.flush();
        return icon;
    }
    private ImageIcon downloadIcon(String url) {
        if (Timer.getInstance().isInternetUp() == false) return null;
          try {
                URL link = new URL(url);
                Image image = ImageIO.read(link);
               // System.out.println("Download forecast icon "+url);
                ImageIcon icon = new ImageIcon(image); 
                icon = makePopBgWhite(icon);
                image.flush();
                return icon;
            } catch (Exception e) {
                //e.printStackTrace();
            }
          return null;
    }
    
    private  ArrayList<ForecastData> getTheForecastAt(double lat, double lon) {
        if (Timer.getInstance().isInternetUp() == false) return null; 
        String dlink = "https://forecast.weather.gov/MapClick.php?lat="+lat+"&lon="+lon+"&FcstType=json";
       System.out.println("Download Forecast Data:"+dlink);
        String json = "";
        ArrayList<ForecastData> list = new ArrayList<ForecastData>();
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
            //System.out.println("There are "+lines+" lines in the forecast json");
        } catch (Exception ex) {
            System.out.println("No internet connection do download forecast data...");
            // ex.printStackTrace();
            return null;
        }
        try {
        JSONObject obj = new JSONObject(json);
        JSONArray perNames = obj.getJSONObject("time").getJSONArray("startPeriodName");
        JSONArray tempLabels = obj.getJSONObject("time").getJSONArray("tempLabel");
        JSONArray temps = obj.getJSONObject("data").getJSONArray("temperature");
        JSONArray pops = obj.getJSONObject("data").getJSONArray("pop");
        JSONArray icons = obj.getJSONObject("data").getJSONArray("iconLink");
        JSONArray text = obj.getJSONObject("data").getJSONArray("text");
        for (int i=0; i<temps.length(); i++) {
            ForecastData d = new ForecastData(perNames.getString(i),
                                               tempLabels.getString(i),
                                               temps.getString(i),pops.get(i).toString(),
                                               icons.getString(i),
                                               text.getString(i));
            list.add(d);
        }
        } catch (Exception ex) {
            ex.printStackTrace();
            for (int i = 0; i < titledBorder.length; i++) {
                titledBorder[i].setTitle("No Data");

            }
            JOptionPane.showMessageDialog(parent, "<html><font size=5>Forecast data server is down!</font></html>");
            return null;
        }
        return list;
        
    }
    
   
    
     @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
       // System.out.println("Bbox changed in forecast table w="+w+" h="+h+" ");
        if (this.lat != lat || this.lon != lon) {
            locationChanged = true;
            this.lat = lat;
            this.lon = lon;
            
        } else {
            locationChanged = false;
        }
         if (container_height != h || container_width != w) {
             
             //System.out.println("Set num periods to "+numPeriods);
             
             container_height = h;
             container_width = w;

             clearForecastTable();
             int x = container_width-iconWidth-14;
             int y = 0;
             setLocation(x, y);

             Dimension d = new Dimension(iconWidth+14, h);
             this.setPreferredSize(d);
             setMaximumSize(d);
             setMinimumSize(d);
             setSize(d);
             this.numPeriods = h/125;
             setNumPeriods(numPeriods);
         }
         updateTimer.restart();
    }
    
//    @Override
//    public void locationChanged(double lat, double lon, int zoomLevel) {
//        
//        if (this.lat != lat || this.lon != lon) {
//            locationChanged = true;
//            this.lat = lat;
//            this.lon = lon;
//            updateTimer.restart();
//        } else {
//            locationChanged = false;
//        }
//        
//    }

    @Override
    public double getID() {
        return id;
    }

    @Override
    public void runTask() {
        if (Utilities.isTopOfHour()) {
            System.out.println("Is top of hour so update the forecast");
            updateTimer.restart();
        } else {
            //System.out.println("Skip download forecast");
        }
    }

    @Override
    public void setCountdownValue(int number) {
        
    }

//    @Override
//    public void networkDown() {
//        clearForecastTable();
//        for (int i=0; i<numPeriods; i++) {
//                fcstLabel[i].setHorizontalAlignment(JLabel.CENTER);
//                fcstLabel[i].setHorizontalTextPosition(JLabel.CENTER);
//                fcstLabel[i].setText("<html><center>Internet<br>Down</center></html>");
//                fcstLabel[i].setFont(new Font("Monospaced",Font.PLAIN,14));
//                fcstLabel[i].setForeground(Color.BLACK);
//                
//            }
//    }

    @Override
    public void networkUp(boolean value) {
        if (value == true) {
            networkUp = true;
            if (noForecast == true) updateTimer.restart();
        } else {
           
                networkUp = false;
                clearForecastTable();
                for (int i = 0; i < numPeriods; i++) {
                    fcstLabel[i].setHorizontalAlignment(JLabel.CENTER);
                    fcstLabel[i].setHorizontalTextPosition(JLabel.CENTER);
                    fcstLabel[i].setText("<html><center>Internet<br>Down</center></html>");
                    fcstLabel[i].setFont(new Font("Monospaced", Font.PLAIN, 14));
                    fcstLabel[i].setForeground(Color.BLACK);

                }
            
        }
    }

   
}
