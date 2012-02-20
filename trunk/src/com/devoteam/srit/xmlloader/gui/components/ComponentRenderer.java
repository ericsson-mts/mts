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
*/package com.devoteam.srit.xmlloader.gui.components;

import com.devoteam.srit.xmlloader.gui.GuiHelper;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComponentRenderer extends AbstractCellEditor implements TableCellRenderer, TwoStageTableCellEditor
{
    private Component component;

    public ComponentRenderer()
    {
        super();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        this.component = (Component) value;
    
        if(0 == (row%2)) component.setBackground(GuiHelper.GREY);
        else             component.setBackground(GuiHelper.WHITE);
        
        return this.component;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        this.component = (Component) value;
    
        if(0 == (row%2)) component.setBackground(GuiHelper.GREY);
        else             component.setBackground(GuiHelper.WHITE);
        
        return this.component;
    }

    public Object getCellEditorValue()
    {
        return this.component;
    }
    
    public boolean isFullyEngaged()
    {
        return false;
    }
}
