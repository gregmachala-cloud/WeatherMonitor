/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.threads;

import weathermonitor.listeners.TimerListener;
import weathermonitor.listeners.NetworkListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import mappingtools.BboxListener;
import mappingtools.Mapping;
import utilities.Utilities;

/**
 *
 * @author greg
 */
public class Timer implements BboxListener {
    private static int n = 60;
    private static final boolean runIt = true;
    private boolean internetUp  = false;
    
    public static int NETWORK_DOWN = 0;
    public static int NETWORK_UP = 1;
    private int internetState = -1;
    
    private static Timer timer = null;
    private static boolean restartTimer = false;
    private ArrayList<NetworkListener> networkListeners = new ArrayList<>();
   private static final double id = Math.random();
    private static ArrayList<TimerListener> listeners = new ArrayList<>();
    private boolean startingUp = true;
    
    private Timer() {
        this.internetUp = Utilities.isInternetUp();
        
        
         
    }
    
    public static Timer getInstance() {
        if  (timer == null) {
            timer = new Timer();
            
        }
        return timer;
    }
    
        public void addNetworkListener(NetworkListener l) {
        this.networkListeners.add(l);
    }
        
    public  void startTimer() {
        Thread t = new Thread() {
            public void run() {
                if (startingUp == true && internetUp == true) {
                    try {
                        sleep(60000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    startingUp = false;
                   
                    //System.out.println("Version in ProtonDrive:"+p.getProperty("Version"));
                }
                int timerSeconds = 60;
                 n = 60; int diff = 0;
                 
                if (internetUp == false) {

                    timerSeconds = 5;
                }
                fireNetworkUp(internetUp);
                while (runIt == true) {
                    if (n <= 0) {
                       System.out.println("n <= 0 so check internet connectioni");
                       new CheckNetwork();
                    }
                    try {
                        System.out.println("sleep in timer with n="+n+" ");
                        sleep(1000-diff);
                    } catch (InterruptedException ex) {
                       // System.getLogger(Timer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                   
                    try {
                       
                    
                         
                         if (internetUp == false) {
                             //System.out.println("Timer is running ith no internet");
                             timerSeconds = 5;
                         }
                         fireNetworkUp(internetUp);
                      
                        if (n <= 0 && restartTimer == false) {
                            
                            if (internetUp == false) {
                                timerSeconds = 5;
                            } else {
                                timerSeconds = 60;
                            }
                            fireNetworkUp(internetUp);
                            if (internetUp == true) {
                                Date d = new Date();
                                SimpleDateFormat sdf = new SimpleDateFormat("s");
                                int secs = Integer.parseInt(sdf.format(d));
                                System.out.println("Current sec " + secs + "secs");
                                int sleepTime = secs - 30;
                                if (sleepTime < 0) {
                                    sleepTime = -1 * sleepTime * 1000;

                                    try {
                                        //System.out.println("Sleep additional" + sleepTime + " secondsto get to 30s");
                                        sleep(sleepTime);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }

                                } else {
                                    diff = (sleepTime * 1000) / 60 + 15;
                                     System.out.println("Speed up timer by "+diff+" ms");
                                }
                                Date d2 = new Date();
                                SimpleDateFormat sdf2 = new SimpleDateFormat("s");
                                secs = Integer.parseInt(sdf2.format(d2));
                                System.out.println("Running tasks at " + secs + "secs");
                                for (int i = 0; i < listeners.size(); i++) {
                                    System.out.println("Running timer task for "+listeners.get(i).getClass()+" listeners");
                                    final TimerListener l = listeners.get(i);
                                    Thread t = new Thread() {
                                        public void run()  {
                                            l.runTask();
                                        }
                                    };
                                    t.start();
                                    
                                }
                                System.out.println("Done with runtasks");
                            }
                            n = timerSeconds;

                        } else if (restartTimer == true) {
                       // System.out.println("Timer is restarted");
                        n = timerSeconds;
                        restartTimer = false;
                    } else if (internetUp == false) {
                        
                        if (n > timerSeconds) {
                            n = timerSeconds;
                        }
                        n = n - 1;
                        if (n < 0) n = 5;
//                        for (int i = 0; i < listeners.size(); i++) {
//                            listeners.get(i).setCountdownValue(-1);
//                        }
                      // System.out.println("Internet is down in Timer "+n);
                    } else {
                       
                        n = n - 1;
                        if (n < 0) {
                            n = timerSeconds;
                            
                            if (internetUp == false) {
                                timerSeconds = 5;
                            } else {
                                timerSeconds = 60;
                            }
                            fireNetworkUp(internetUp);
                        }
//                        for (int i = 0; i < listeners.size(); i++) {
//                            listeners.get(i).setCountdownValue(n);
//                        }
                        
                    }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }
    public boolean isInternetUp() {
       // System.out.println("isInternetUp "+internetUp);
        return internetUp;
    }
   private void fireNetworkUp(boolean value) {
       boolean networkStateChanged = false;
       this.internetUp = value;
       if (internetState == NETWORK_DOWN && value == true) {
           //internet goes up
           internetState = NETWORK_UP;
           networkStateChanged = true;
           Mapping.getInstance().getCWA();
       } else if (internetState == NETWORK_UP && value == false) {
           // internet goes down
           networkStateChanged = true;
           internetState = NETWORK_DOWN;
       } else if (internetState == -1) {
           // on startup 
            value = Utilities.isInternetUp();
            networkStateChanged = true;
            if (internetUp) {
                internetState = NETWORK_UP;
            } else {
                internetState = NETWORK_DOWN;
            }
       }
       if (networkStateChanged) {
           System.out.println("Network state changed to "+internetState);
           for (int i = 0; i < networkListeners.size(); i++) {
               networkListeners.get(i).networkUp(value);
           }
       }
   }
   
   public int getCountdownValue() {
       return n;
   }
    
   public void restartTimer() {
       restartTimer = true;
       
   }
    public void addTimerListener(TimerListener l) {
        this.listeners.add(l);
    }

    @Override
    public void bboxChanged(String bbox, int w, int h,double lat, double lon, int zoomLevel, ArrayList<String> stateIds,String nearestCity) {
        restartTimer();
    }

    

    @Override
    public double getID() {
        return id;
    }

    private class CheckNetwork {
        private CheckNetwork() {
            Thread t = new Thread() {
                public void run() {
                    internetUp = Utilities.isInternetUp();
                }
            };
            t.start();
        }
    }
   
}
