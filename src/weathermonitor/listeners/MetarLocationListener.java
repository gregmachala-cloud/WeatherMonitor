/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package weathermonitor.listeners;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import weathermonitor.dataobjects.MetarLocationObject;

/**
 *
 * @author greg
 */
public interface MetarLocationListener {
    //public void plotMetar(String name,int x,int y,double lat,double lon);
   // public void boundingBoxChanged(double threadId, int w, int h);
    public void setMetarPlotList(ArrayList<MetarLocationObject> list, Point2D yourLocation,ArrayList<String> stateIds,int width, int height, double[] bbox,int zoomLevel);
    //public void setStateIds(ArrayList<String> stateIds, double[] bbox,int zoomLevel);
    //public void setMapSize(int width, int height);
}
