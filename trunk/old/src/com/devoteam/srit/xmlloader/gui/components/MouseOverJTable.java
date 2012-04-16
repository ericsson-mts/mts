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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.gui.components;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author gpasquiers
 */
public class MouseOverJTable extends JTable
{
    private class FullMouseListener implements MouseMotionListener, MouseListener
    {

        public void mouseDragged(MouseEvent e)
        {
            //possiblySwitchEditors(e);
        }

        public void mouseMoved(MouseEvent e)
        {
            possiblySwitchEditors(e);
        }

        public void mouseClicked(MouseEvent e)
        {
            possiblySwitchEditors(e);
        }

        public void mousePressed(MouseEvent e)
        {
            //possiblySwitchEditors(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            //possiblySwitchEditors(e);
        }

        public void mouseEntered(MouseEvent e)
        {
            possiblySwitchEditors(e);
        }

        public void mouseExited(MouseEvent e)
        {
            possiblySwitchEditors(e);
        }
        
    }
    
    private final FullMouseListener twoStageEditingListener = new FullMouseListener();

    
    public MouseOverJTable(TableModel dm)
    {
        super(dm);
        
        this.addMouseListener(twoStageEditingListener);
        this.addMouseMotionListener(twoStageEditingListener);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component c = super.prepareRenderer(renderer, row, column);
        return prepareEditorRenderer(c, row, column);
    }

    public Component prepareEditor(TableCellEditor editor, int row, int column)
    {
        Component c = super.prepareEditor(editor, row, column);
        return prepareEditorRenderer(c, row, column);
    }

    private Component prepareEditorRenderer(Component stamp, int row, int column)
    {
        return stamp;
    }
    
    private void possiblySwitchEditors(MouseEvent e)
    {
        try
        {
            Point p = e.getPoint();
            if (p != null)
            {
                int row = rowAtPoint(p);
                int col = columnAtPoint(p);
                if (row != getEditingRow() || col != getEditingColumn())
                {
                    if (isEditing())
                    {
                        TableCellEditor editor = getCellEditor();
                        if (editor instanceof TwoStageTableCellEditor && !((TwoStageTableCellEditor) editor).isFullyEngaged())
                        {
                            try
                            {
                                if (!editor.stopCellEditing())
                                {
                                    editor.cancelCellEditing();
                                }
                            }
                            catch(Exception eee)
                            {
                                editor.cancelCellEditing();
                            }
                        }
                    }

                    if (!isEditing())
                    {
                        if (row != -1 && isCellEditable(row, col))
                        {
                            editCellAt(row, col);
                        }
                    }
                }
            }
        }
        catch(Exception ee)
        {

        }
    }
}
