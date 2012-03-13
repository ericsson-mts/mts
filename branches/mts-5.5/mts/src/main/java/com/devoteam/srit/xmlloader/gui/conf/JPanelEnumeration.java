/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.gui.conf;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import java.awt.Component;
import javax.swing.JComboBox;


/**
 *
 * @author jbor
 */
public class JPanelEnumeration extends JPanelGeneric implements JPanelConfInterface {

    private JComboBox jComboBoxEnumeration;

    public JPanelEnumeration (PropertiesEnhanced config, String name){
        super(config, name);
        this.jComboBoxEnumeration = new JComboBox(config.getConfig().get(name).getPossibilities());
        this.add(this.jComboBoxEnumeration);
        this.jComboBoxEnumeration.setEditable(false);
        this.jComboBoxEnumeration.setVisible(true);
        this.validate();
    }

    @Override
    public Component getSpecificComponent() {
        return jComboBoxEnumeration;
    }

    public void resetSpecific() {
        this.newPropertiesParameterStructure = this.propertiesParameterStructure.clone();
        refresh();
    }

    public void save() {
        propertiesParameterStructure.setLocaleValue(newPropertiesParameterStructure.getLocaleValue());
        refresh();
    }

    public void refreshSpecific() {
        String locale = this.newPropertiesParameterStructure.getLocaleValue();
        String globale = this.newPropertiesParameterStructure.getDefaultValue();

        this.jComboBoxEnumeration.removeActionListener(this);
        if(null != locale){
            this.jComboBoxEnumeration.setSelectedItem(locale);
        }
        else{
            this.jComboBoxEnumeration.setSelectedItem(globale);
        }
        this.jComboBoxEnumeration.addActionListener(this);
    }

    @Override
    public void touchedSpecific() {
        newPropertiesParameterStructure.setLocaleValue((String) this.jComboBoxEnumeration.getSelectedItem());
        refresh();
    }

    @Override
    public void initSpecific() {
    }

    @Override
    public boolean isDisplayedValueTheInitOne(){
        String oldValue = this.newPropertiesParameterStructure.getLocaleValue();
        String localeNew = (String) this.jComboBoxEnumeration.getSelectedItem();       
        if (oldValue == null){
            oldValue = this.newPropertiesParameterStructure.getDefaultValue();
        }
        return localeNew.equals(oldValue);
    }
}