package weathermonitor.threads;


import java.awt.event.ActionListener;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class WMTimer extends Thread {
    private final ActionListener listener;
    private boolean restart = true;
    private int delay = 0;
    private String name = "";
    private boolean repeats = false;
    private boolean stop = false;
    private boolean runIt = true;
    
    
    public WMTimer(int delay, String name, ActionListener listener) {
        this.delay = delay;
        this.listener = listener;
        this.name = name;
    }
    public void setDelay(int value) {
        this.delay = value;
    }
    public void restart() {
        //System.out.println("Restart timer in "+name);
        this.restart = true;
    }
    public void setRepeats(boolean value) {
        this.repeats = value;
    }
    public void setStop(boolean value) {
        this.stop = value;
    }
    @Override
    public void run() {
        while (runIt == true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            while (restart == true) {
                restart = false;
                try {
                    Thread.sleep(delay-10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (restart == false) {
//                    if (name.equals("RadarLooper") == false) {
//                              //System.out.println("Restart is false for "+name+" continue ");
//                  
//                        }
                    if (stop == false) {
                        
//                        if (name.equals("RadarLooper") == false) {
//                            //System.out.println("Timer " + name + " action performed");
//                        }
                        listener.actionPerformed(null);
                    } 
                    if (repeats == true) {
                       
                        //System.out.println("Timer "+name +" is  repeat");
                        restart = true;
                    }
                } else {
                    stop = false;
                    restart = true;
                   // System.out.println("Timer " + name + " has been restarted");
                }
            }
        }
    }
}
