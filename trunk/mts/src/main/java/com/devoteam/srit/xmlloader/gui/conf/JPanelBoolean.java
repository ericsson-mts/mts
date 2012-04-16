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

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import java.awt.Component;
import javax.swing.JComboBox;

/**
 *
 * @author jbor
 */
public class JPanelBoolean extends JPanelGeneric implements JPanelConfInterface {

    private JComboBox jComboBoxBoolean;

    public JPanelBoolean (PropertiesEnhanced config, String name){
        super(config, name);        
        this.jComboBoxBoolean = new JComboBox(new String[] {"true", "false"});
        this.add(this.jComboBoxBoolean);
        this.jComboBoxBoolean.setEditable(false);
        this.jComboBoxBoolean.setVisible(true);
        this.validate();
    }

    @Override
    public Component getSpecificComponent() {
        return jComboBoxBoolean;
    }

    public void resetSpecific() {
        this.newPropertiesParameterStructure = this.propertiesParameterStructure.clone();
        refresh();        
    }

    public void save() {
        propertiesParameterStructure.setLocaleValue(newPropertiesParameterStructure.getLocaleValue());
        refresh();
    }

    @Override
    public void refreshSpecific() {
        String locale = this.newPropertiesParameterStructure.getLocaleValue();
        String globale = this.newPropertiesParameterStructure.getDefaultValue();

        this.jComboBoxBoolean.removeActionListener(this);
        if(null != locale){
            this.jComboBoxBoolean.setSelectedItem(locale);
        }
        else{
            this.jComboBoxBoolean.setSelectedItem(globale);
        }
        this.jComboBoxBoolean.addActionListener(this);
    }

    @Override
    public void touchedSpecific() {
        newPropertiesParameterStructure.setLocaleValue((String) this.jComboBoxBoolean.getSelectedItem());
        refresh();
    }

    @Override
    public void initSpecific() {
    }

    @Override
    public boolean isDisplayedValueTheInitOne(){
        String oldValue = this.newPropertiesParameterStructure.getLocaleValue();
        String localeNew = (String) this.jComboBoxBoolean.getSelectedItem();
        if (oldValue == null){
            oldValue = this.newPropertiesParameterStructure.getDefaultValue();
        }
        return localeNew.equals(oldValue);
    }
}