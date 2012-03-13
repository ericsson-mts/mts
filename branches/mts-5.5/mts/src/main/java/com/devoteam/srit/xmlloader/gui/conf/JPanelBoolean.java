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