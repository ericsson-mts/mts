/*
 * JFrameEditableParameters.java
 *
 * Created on 6 novembre 2007, 16:38
 */

package com.devoteam.srit.xmlloader.gui.model;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.dom4j.Element;

/**
 *
 * @author  fhenry
 */
public class ModelEditableParameters extends javax.swing.table.DefaultTableModel
{
    
    private List<Element> modelElements;
        
    public ModelEditableParameters()
    {    	
    	super(
        new Object [][]
			{
			},
			new String []
			{
        		"Name", "Description", "Value"
			}
    	);
    }

    public ModelEditableParameters(List<Element> elts)
    {    	
    	this();
    	this.modelElements = elts;
    }

    Class[] types = new Class []
    {
        java.lang.String.class, java.lang.String.class, java.lang.String.class
    };
    boolean[] canEdit = new boolean []
    {
        false, false, true
    };

    public Class getColumnClass(int columnIndex)
    {
        return types [columnIndex];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return canEdit [columnIndex];
    }

    public void clear()
    {
       /* if(null != this.modelElements)
        {
            this.modelElements.clear();
        }*/
        
        while(getRowCount() > 0)
        {
            removeRow(0);
        }
    }
        
    public void fill(List<Element> elts)
    {
        this.clear();
        this.modelElements = elts;
        
        for(Element element:elts)
        {
            String[] line = new String[3];
            
            //TODO: check name is unique in jTable column 0
            line[0] = element.attributeValue("name");
            if(null != element.attributeValue("description"))
            {
                line[1] = element.attributeValue("description");
            }
            line[2] = element.attributeValue("value");
            
            addRow(line);
        }        
    }
    
    public void apply()
    {
    	apply(this.modelElements);
    }

    public void apply(List<Element> elts)
    {       
        Vector dataVector = this.getDataVector();
        for(Element element:elts)
        {
            Iterator<Vector> iterator = (Iterator<Vector>) dataVector.iterator();
            while(iterator.hasNext())
            {
                Vector pair = iterator.next();
                if(element.attributeValue("name").equals((String) pair.get(0)))
                {
                    element.attribute("value").setValue((String) pair.get(2));
                }
            }
        }
    }

    public Vector getElements() {
        return this.getDataVector();
    }
    
}
