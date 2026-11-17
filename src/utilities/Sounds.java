/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author greg
 */
public class Sounds {
    public static String EXPIRE = "/weathermonitor/sounds/expire.wav";
    public static String NEW = "/weathermonitor/sounds/new.wav";
    public static String UPDATE = "/weathermonitor/sounds/update.wav";
    public static String THUNDER = "/weathermonitor/sounds/thunder.wav";
    public AudioInputStream audioInputStream;
    private Clip clip = null;
    private static Sounds me = null;
    private long lastSoundPlayedTime = 0;
   
    public static Sounds getInstance() {
        if (me == null) me = new Sounds();
        return me;
    }
    
    private Sounds() {
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException ex) {
           ex.printStackTrace();
        }
       
    }
    
    public void playSound(String soundPath) {
        long curSecs= new Date().getTime()/1000;
        if (lastSoundPlayedTime > 0) {
            
            long diff = curSecs - lastSoundPlayedTime;
            if (diff < 15) {
                System.out.println("Skip sound byte "+soundPath +" only "+diff+" seconds since last sound" );
                return;
            }
            
        }
        lastSoundPlayedTime = curSecs;
        if (clip == null || clip.isActive()) {
            System.out.println("Audio clip is active or unavailable...return");
            return;
        }
        final String path = soundPath;

        Thread t = new Thread() {

            public void run() {
                long s = new Date().getTime()/1000;
               // System.out.println("Start plyaing sound now");
                playSoundThread(path);
                long s2 = new Date().getTime()/1000;
                long diff = s2-s;
                //System.out.println("Play took "+diff+" secs");
                try {
                    sleep(6000);
                } catch (InterruptedException ex) {
                   ex.printStackTrace();
                }
                clip.close();
            }
        };
        t.start();
    }
    public void playSoundThread(String path) {
       
          // create AudioInputStream object
       InputStream is = null;
       is = getClass().getResourceAsStream(path);
       
       BufferedInputStream bufferedStream = new BufferedInputStream(is);
       System.out.println("play sound "+path);
        if (is != null) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);
                
                clip.open(audioInputStream);
                clip.setFramePosition(0);
                clip.start();
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.print("Cannot find "+path);
        }
    }
}
