/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
