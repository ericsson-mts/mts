/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
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
