package weathermonitor.listeners;

import java.awt.Image;
import java.awt.geom.Point2D;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
/**
 *
 * @author greg
 */
public interface RadarImageListener {
    public void radarLayersChanged(String[] radarLayerNames);
    public void radarImageAdded(int rid, Point2D centerPoint, boolean flush, Image img, String time, long epochSecs, int layerNumber,int numberOfRadarImages);
    public void radarLengendAdded(Image img, String layer_name);
}
