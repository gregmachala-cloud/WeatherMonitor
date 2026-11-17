/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package weathermonitor.dataobjects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author greg
 */
public class ForecastData {
    private String periodName;
    private String tempLabel;
    private String temperature;
    private String iconLink;
    private String text;
    private String pop;
    
    public ForecastData (String periodName, String tempLabel, String temperature, Object pop, String iconLink,String text) {
        this.periodName = periodName;
        this.tempLabel = tempLabel;
        if (pop == null || pop.equals("null")) {
            this.pop = "0";
        } else {
            this.pop = pop.toString();
        }
        
        this.temperature = temperature;
        this.iconLink = iconLink;
        this.text = text;
   }
    public String getLabel() {
        return tempLabel;
    }
    public String getTemp() {
        return temperature;
    }
    public String getPop() {
        if (pop.equals("0") == false) {
            return pop;
        } else {
            return "";
        }
    }
    public String getPeriodName() {
        return periodName;
    }
    public String getIconLink() {
        return iconLink;
    }
    public String getFcstText() {
        return text;
    }
    public String getHighlightedFcstTextHtml() {
        Pattern pat = Pattern.compile("[ ,1][0-9][0-9]\\.");
        Matcher m = pat.matcher(text);
        String html = text;
        String c = "red";
        if (tempLabel.toLowerCase().contains("low")) {
            c = "blue";
        }
        if  (m.find()) {
            html = text.substring(0,m.start())+"<font color="+c+"><b>"+text.substring(m.start(),m.end()-1)+"</b></font>"+text.substring(m.end()-1);        
        }
        pat = Pattern.compile("[ ,1][0-9][0-9] percent");
        m = pat.matcher(html);
       
        c="#23b404";
        if  (m.find()) {
            html = html.substring(0,m.start())+"<font color="+c+"><b>"+html.substring(m.start(),m.end())+"</b></font>"+html.substring(m.end());        
        }
        pat = Pattern.compile("Chance of precipitation is 100%");
        
        m = pat.matcher(html);
       
        c="#23b404";
        if  (m.find()) {
            html = html.substring(0,m.start())+"<font color="+c+"><b>"+html.substring(m.start(),m.end())+"</b></font>"+html.substring(m.end());        
        }
        pat = Pattern.compile("Chance of precipitation is [0-9][0-9]%");
        
        m = pat.matcher(html);
       
        c="#23b404";
        if  (m.find()) {
            html = html.substring(0,m.start())+"<font color="+c+"><b>"+html.substring(m.start(),m.end())+"</b></font>"+html.substring(m.end());        
        }
        return html;
    }
}
