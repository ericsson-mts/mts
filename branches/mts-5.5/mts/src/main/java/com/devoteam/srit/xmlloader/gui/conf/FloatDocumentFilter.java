/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.gui.conf;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author jbor
 */
public class FloatDocumentFilter extends DocumentFilter{

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        String intToInsert;
        if (!Utils.isInteger(text) && !text.equals(".")){
            intToInsert = "";
        }
        else{
            intToInsert = text;
        }
        super.insertString(fb, offset, intToInsert, attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String intToReplacet;
        if (!Utils.isInteger(text) && !text.equals(".")){
            intToReplacet = "";
        }
        else{
            intToReplacet = text;
        }
        super.insertString(fb, offset, intToReplacet, attrs);
    }
}
