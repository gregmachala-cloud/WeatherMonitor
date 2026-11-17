package weathermonitor.dataobjects;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author greg
 */
public class WmsRadarLayer {
    private final String layerNames;
    private final String layerDescr;
    private final String wmsUrl;
    private boolean isDefaultLayer = false;
    
    public WmsRadarLayer(String url, String layerNames, String layerDescr, boolean defaultLayer) {
        this.wmsUrl = url;
        this.layerNames = layerNames;
        this.layerDescr = layerDescr;
        this.isDefaultLayer = defaultLayer;
    }
    
    public String getBaseWmsUrl() {
        return wmsUrl;
    }
    public String getLayerName() {
        return layerNames;
    }
    
    public String getLayerDescr() {
        return layerDescr;
    }
    
    public String getShortLayerDescr() {
        if (isDefaultLayer) return layerDescr;
        String shortDescr = layerDescr.replace("NEXRAD Level 3","").replace("Dual-Pol ","").replace("One Hour","1Hr").replace("Digital ","").replace("Surface ","").replace("Accumulation","Accum").replace(" Running Total","");
        shortDescr = shortDescr.replace("Super Resolution ", "HiRez").replace("Radar","").replace("Radial Velocity","Vel");
        Pattern pat = Pattern.compile("\\. ");
        Matcher m = pat.matcher(shortDescr);
        if (m.find()) {
            return shortDescr.substring(0,m.start());
        }
        return shortDescr;
    }
}
