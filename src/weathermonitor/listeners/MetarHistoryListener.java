/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package weathermonitor.listeners;

import weathermonitor.dataobjects.Metar;
import java.util.ArrayList;

/**
 *
 * @author greg
 */
public interface MetarHistoryListener {
    public void addedMetar(Metar metar);
}
