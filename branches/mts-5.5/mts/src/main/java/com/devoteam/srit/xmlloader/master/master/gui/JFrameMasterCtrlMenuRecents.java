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

package com.devoteam.srit.xmlloader.master.master.gui;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

/**
 *
 * @author Gwenhael
 */
public class JFrameMasterCtrlMenuRecents {

    private JMenu _jMenuRecents;
    private JMenuItem _jMenuItemClear;
    private JMenuItem _jMenuItemEmpty;
    private JSeparator _jSeparator;
    private ActionListener _jMenuItemActionPerformed;
    private LinkedList<String> _recents = new LinkedList<String>();

    public JFrameMasterCtrlMenuRecents(JMenu jMenuRecents, JMenuItem jMenuItemClear, JMenuItem jMenuItemEmpty, JSeparator jSeparator, ActionListener jMenuItemActionPerformed) {
        _jMenuRecents = jMenuRecents;
        _jMenuItemClear = jMenuItemClear;
        _jMenuItemActionPerformed = jMenuItemActionPerformed;
        _jMenuItemEmpty = jMenuItemEmpty;
        _jSeparator = jSeparator;
        
        
        _jMenuItemClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearRecents();
                updateRecents();
            }
        });
        
        readRecentsFromFile();
        updateRecents();
    }

    public void addToRecents(String path) {
        if (_recents.contains(path)) {
            _recents.remove(path);
        }
        _recents.addFirst(path);
        dumpRecentsToFile();
        updateRecents();
    }
    
    public void openLatest() {
        if(!_jMenuRecents.getItem(0).equals(_jMenuItemEmpty)){
            _jMenuRecents.getItem(0).doClick();
        }
    }

    private void clearRecents() {
        _recents.clear();
        dumpRecentsToFile();
        updateRecents();
    }
    
    private void updateRecents() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _jMenuRecents.removeAll();

                if (_recents.isEmpty()) {
                    _jMenuRecents.add(_jMenuItemEmpty);
                }
                else {
                    for (String path : _recents) {
                        javax.swing.JMenuItem jMenuItem = new javax.swing.JMenuItem();
                        jMenuItem.addActionListener(_jMenuItemActionPerformed);

                        jMenuItem.setText(path);

                        jMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/page_white_code.png")));

                        _jMenuRecents.add(jMenuItem);
                    }
                }

                _jMenuRecents.add(_jSeparator);
                _jMenuRecents.add(_jMenuItemClear);
                _jMenuRecents.revalidate();

            }
        });
    }


    private void dumpRecentsToFile() {
        try {
            PrintStream printStream = new PrintStream(new FileOutputStream("../conf/master.recents"));//BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("../conf/master.recents"));
            for (String path : _recents) {
                printStream.println(path);
            }
            printStream.close();
        }
        catch (IOException e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Unable to save recent files");
        }
    }

    private void readRecentsFromFile() {
        _recents.clear();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("../conf/master.recents"));
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (line.length() != 0) {
                    _recents.add(line);
                }
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException e) {
            // this is not an error
        }
        catch (IOException e) {
            Utils.showError(_jMenuRecents, "Error in readRecentsFromFile()", e);
        }
    }
}
