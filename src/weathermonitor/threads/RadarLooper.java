/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.threads;

import weathermonitor.listeners.RadarLooperListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author greg
 */
public class RadarLooper  {
    private boolean loop = true;
    public int numberOfImages = 0;
    private int delay = 200;
    private int num = 0;
    public int lastOne = 0;
    private WMTimer timer;
    
    public RadarLooper(RadarLooperListener listener, int numImages) {
        numberOfImages = numImages;
        lastOne = numberOfImages-1;
        //lastImageNumber = numberOfImages-1;
        timer = new WMTimer(delay,"RadarLooper", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // System.out.println("Loop Next img "+num+" lastOne="+lastOne);
                 listener.loopNext(num);
                
                 num++;
                 if (num > lastOne) {
                     num = 0;
                     timer.setDelay(delay*4);
   
                 } else {
                     timer.setDelay(delay);
                 }

            }
        });
        timer.setRepeats(true);
        //timer.start();
    }
    public void setNumberOfImages(int numImages) {
        //System.out.println("SetNumberOfImages in looper to "+numImages);
        numberOfImages = numImages;
        lastOne = numImages-1;
    }
    public void setLoop(boolean value) {
       // System.out.println("setLoop "+value);
        this.loop = value;
        if (loop == true) {
            if (timer.isAlive() == false) {
                timer.start();
            } else {
                timer.restart();
            }
        } else {
            timer.setStop(true);
        }
    }
    public void setDelay(int value) {
       // System.out.println("Set radar looper delay="+value);
        this.delay = value;
        timer.setDelay(value);
    }
    public boolean isLooping() {
        return loop;
    }
    
    
    
}
