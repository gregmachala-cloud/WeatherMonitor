/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathermonitor.dataobjects;

import java.util.ArrayList;

/**
 *
 * @author harriet
 */
public class RadarStation {
    private String id = "";
    private String name = "";
    private double lat;
    private double lon;
   
    
    public RadarStation(String id, String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }
    public double getLat(){
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
}
