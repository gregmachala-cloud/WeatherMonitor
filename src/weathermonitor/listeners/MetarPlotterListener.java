/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package weathermonitor.listeners;

import weathermonitor.dataobjects.MetarStationInfo;

/**
 *
 * @author greg
 */
public interface MetarPlotterListener {
    
    public void metarPlotterFinished(MetarStationInfo msi, int id, int count);
}
