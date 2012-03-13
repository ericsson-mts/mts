package com.devoteam.srit.xmlloader.gui.components;

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
