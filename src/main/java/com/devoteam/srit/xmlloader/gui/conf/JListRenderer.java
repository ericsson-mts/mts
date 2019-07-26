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

package com.devoteam.srit.xmlloader.gui.conf;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.awt.Component;
import java.awt.Font;
import java.net.URI;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author jbor
 */
public class JListRenderer extends JLabel implements ListCellRenderer {

//    private ListCellRenderer    defaultRenderer     = new DefaultListCellRenderer();
    private Font                f                   = new Font("Tahoma", Font.PLAIN, 14);
    private final Font          bold                = new Font("Tahoma", Font.BOLD, 14);
    private final Font          plain               = new Font("Tahoma", Font.PLAIN, 14);
    private URI                 filePathInputConf;

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String val = (String) value;        
        this.filePathInputConf = URIRegistry.MTS_TEST_HOME.resolve(val);
        try{
            //on verifie si le fichier est présent en local et si oui, on met la font en gras
            if (SingletonFSInterface.instance().exists(filePathInputConf)){
                this.f = this.bold;
            }
            else{
                this.f = this.plain;
            }
        }
        catch (Exception e){
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Problem in the list renderer");
        }
        //on supprimer le ".properties" s'il est present
        if (val.endsWith(".properties")){
            val = val.substring(0, val.lastIndexOf("."));
        }
        //on recreer le comportement de la liste par defaut sur un jlabel
        this.setText(val);
        if (isSelected){
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        }
        else{
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        this.setEnabled(list.isEnabled());
        this.setOpaque(true);
        this.setFont(this.f);
//        return defaultRenderer.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
        return this;
    }
}