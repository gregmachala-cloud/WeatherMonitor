/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mappingtools;

import java.util.ArrayList;

/**
 *
 * @author greg
 */
public interface BboxListener {
    
    
    public void bboxChanged(String bbox, int w, int h,double lat, double lon,int zoomLevel,ArrayList<String> stateIds, String nearestCity);
    //public void locationChanged(double lat, double lon,int zoomLevel);
    public double getID();
}
