/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.dataobjects;

import java.awt.geom.Point2D;

/**
 *
 * @author greg
 */
public class MetarLocationObject {
    public Point2D metarSiteLonLat;
    public String preferredId = null;
    public String cityName;
    public String state;
    
    public MetarLocationObject(Point2D metarSiteLonLat, String cityName, String state) {
        this.metarSiteLonLat = metarSiteLonLat;
        this.state = state;
        this.cityName = cityName;
    }
    
    public void setPreferredStationId(String id) {
        this.preferredId = id;
    }
}
