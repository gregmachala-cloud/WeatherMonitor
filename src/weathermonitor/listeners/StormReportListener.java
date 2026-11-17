/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package weathermonitor.listeners;

import weathermonitor.dataobjects.StormReportInfo;
import java.util.ArrayList;

/**
 *
 * @author greg
 */
public interface StormReportListener {
    public void stormReportsUpdated(ArrayList<StormReportInfo> list, long minimumReportTimeOffsetSecs);
}
