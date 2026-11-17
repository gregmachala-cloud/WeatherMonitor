/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mappingtools;

import utilities.Utilities;

/**
 *
 * @author greg
 */
public class DownloadMaps {
    
    
    
public static void main(String[] args) {
    //Roads or Counties
    Utilities.downloadMaps("Counties");
    Utilities.downloadMaps("Roads");
}
}
