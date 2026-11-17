/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mappingtools;

/**
 *
 * @author greg
 */
public class City {
    String name = "?";
    double lat = 0.0;
    double lon = 0.0;
    String state = "";
    int vis = 1;
    int x = 0;
    int y = 0;
    
    int major = 1;
    // var arr = [xy[0], xy[1], name, 1, major]; // lon lat name visibile major
    public City(double Lon, double Lat, int x, int y, String name, String state, int vis, int major) {
        this.lon = Lon;
        this.lat = Lat;
        this.name = name;
        this.state = state;
        this.major = major;
        this.vis = vis;
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public String getState() {
        return state;
    }
    public String getCityName() {
        return name;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public int getMajorCity() {
        return major;
    }
    public int isVisible() {
        return vis;
    }
    public void setVisible(int value) {
        this.vis = value;
    }
}
