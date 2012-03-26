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

package com.devoteam.srit.xmlloader.gui;

import java.awt.Font;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author gpasquiers
 */
public abstract class MyTableCellRenderer implements TableCellRenderer
{
    public JTextArea getJTextPane()
    {
        return this.getJTextArea(false);
    }

    public JTextArea getJTextArea(boolean selected)
    {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setMargin(new Insets(0,0,0,0));
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(false);
        jTextArea.setFont(Font.decode("Monospaced"));
        jTextArea.setToolTipText("Click to expand");
        return jTextArea;
    }
}
