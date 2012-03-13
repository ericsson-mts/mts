/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author indiaye
 */
public class DictionnaryStun {

    public static HashMap readProperties() {
        String fichier = "../conf/stun/typeStun.properties";
        HashMap hash = new HashMap();
        try {
            InputStream ips = new FileInputStream(fichier);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (!ligne.trim().startsWith("#") && !ligne.trim().isEmpty()) {
                    String[] tab = ligne.split("=");
                    hash.put(tab[0], tab[1]);
                    hash.put(tab[1], tab[0]);
                }

            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }
  
}
