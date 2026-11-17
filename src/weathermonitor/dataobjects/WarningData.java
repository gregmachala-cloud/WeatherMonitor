/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.dataobjects;

import java.awt.geom.GeneralPath;

/**
 *
 * @author greg
 */
public class WarningData {
    private String event;
    private String severity;
    private String text;
    private String headline;
    private String expires;
    private GeneralPath polygonPath = null;
    private String maxWindGust = "";
    private String maxHailSize = "";
    
    public WarningData() {
        
    }
    public WarningData(String event, String severity, String text, String headline) {
        
    }
    public String getEvent() {
        return event;
    }
    
    public void setMaxHailSize(String value) {
        this.maxHailSize = value;
    }
    
    public void setMaxWindGust(String value) {
        this.maxWindGust = value;
    }
    
    public String getMaxHailSize() {
        return this.maxHailSize;
    }
    
    public String getMaxWindGust() {
        return this.maxWindGust;
    }
    public void setEvent(String event) {
        this.event  = event;
    }
    public String getSeverity() {
        return severity;
        
    }
    public GeneralPath getPolygonPath() {
        return polygonPath;
    }
            
    public void setPolygonPath(GeneralPath polygonPath) {
        //System.out.println("Setting polygon path:"+polygonPath.toString());
        
        this.polygonPath = polygonPath;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    public String getHeadline() {
        return headline;
    }
    
    public void setHeadline(String headline) {
        this.headline = headline;
    }
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getExpires() {
        return expires;
    }
    
    public void setExpires(String expires) {
        this.expires = expires;
    }
}
