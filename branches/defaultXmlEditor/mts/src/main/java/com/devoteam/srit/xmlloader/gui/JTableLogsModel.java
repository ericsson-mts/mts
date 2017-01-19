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

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import com.devoteam.srit.xmlloader.core.log.FileTextListener;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import javax.swing.JTable;
import javax.swing.JViewport;

/**
 *
 * @author gpasquiers
 */
public class JTableLogsModel {

    private DefaultTableModel completeDefaultTableModel;
    private DefaultTableModel filteredDefaultTableModel;
    private LinkedList<TextEvent.Topic> topicsToDisplay;
    private int minLevel;
    private int columnCount;
    private int rowCount;
    private String scenarioName;
    private String testcaseName;
    private FileTextListener listener;
    private boolean open = false;
    private boolean searchFindValue = false;
    private static boolean savePurgeFile = Config.getConfigByName("tester.properties").getBoolean("logs.SAVE_BEFORE_PURGE", false);

    /**
     * Creates a new instance of JTableLogsModel
     * This class contains two DefaultTableModels,
     * One contains all the lines added (in the limit of the "size" argument).
     * the other contains only the lines satisfying the criteras of 
     * Topic and Level.
     */
    public JTableLogsModel(String[] columns, int size, String testcaseName, String scenarioName) {
        this.columnCount = columns.length;
        this.rowCount = size;
        this.testcaseName = testcaseName;
        this.scenarioName = scenarioName;

        this.completeDefaultTableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.filteredDefaultTableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.topicsToDisplay = new LinkedList<TextEvent.Topic>();

        for (TextEvent.Topic topic : TextEvent.Topic.values()) {
            this.topicsToDisplay.add(topic);

        }

        this.minLevel = TextEvent.DEBUG;
    }

    /**
     * Add a row to the complete model and try to add it to the filtered model.
     */
    synchronized public void addRow(TextEvent textEvent, String title, boolean open) {
        this.open = open;
        Vector line = new Vector();

        line.add(textEvent);
        line.add(textEvent);
        line.add(textEvent);
        for (int i = 3; i < completeDefaultTableModel.getColumnCount(); i++) {
            if (null == title || title.equals(completeDefaultTableModel.getColumnName(i))) {
                line.add(textEvent);
            }
            else {
                line.add(null);
            }
        }
        this.completeDefaultTableModel.addRow(line);
        if (!open) {
            if (this.completeDefaultTableModel.getRowCount() > this.rowCount) {
                if (savePurgeFile) {
                    if (this.listener == null) {
                        String logApplicationPathName = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs/");
                        boolean logFileFormatCSV = Config.getConfigByName("tester.properties").getBoolean("logs.FILE_FORMAT_CSV", false);
                        String extensionFile;
                        String fileName;

                        if (logFileFormatCSV) {
                            extensionFile = ".csv";
                        }
                        else {
                            extensionFile = ".log";
                        }

                        if (!scenarioName.contains("(callflow)")) {
                            if (testcaseName == null) {
                                fileName = logApplicationPathName + "application" + extensionFile;
                            }
                            else {
                                fileName = logApplicationPathName + testcaseName + "/" + scenarioName + extensionFile;
                            }
                            this.listener = new FileTextListener(fileName, false);
                        }
                        else {
                            this.listener = null;
                        }
                    }
                    if (this.listener != null) {
                        // only save if it's not an open log
                        if (!((TextEvent) this.completeDefaultTableModel.getValueAt(0, 1)).getOpen()) {
                            this.listener.printText((TextEvent) this.completeDefaultTableModel.getValueAt(0, 1));
                        }
                    }
                }
                this.completeDefaultTableModel.removeRow(0);
            }
        }
        this.addFilteredRow(textEvent, line, open);
    }

    /**
     * Add a line to the filtered model (if it matches the filter)
     */
    private void addFilteredRow(TextEvent textEvent, Vector row, boolean open) {
        this.open = open;
        TextEvent.Topic topic = textEvent.getTopic();

        if (textEvent.getLevel() < this.minLevel || (null != topic && !this.topicsToDisplay.contains(topic))) {
            return;
        }

        this.filteredDefaultTableModel.addRow(row);
        if (!open) {
            if (this.filteredDefaultTableModel.getRowCount() > this.rowCount) {

                this.filteredDefaultTableModel.removeRow(0);
            }
        }
    }

    /**
     * Clean the filtered model and then try to add all lines from
     * the complete model (applying the filter).
     */
    synchronized public void refreshFilteredModel() {
        //
        // Empty Model
        this.clear();

        Vector<Vector> vector = this.completeDefaultTableModel.getDataVector();

        for (Vector line : vector) {
            TextEvent textEvent = null;

            int size = line.size();
            for (int i = 1; i < size; i++) {
                if (null != line.get(i)) {
                    textEvent = (TextEvent) line.get(i);
                }
            }
            this.addFilteredRow(textEvent, line, this.open);
        }
        this.filteredDefaultTableModel.fireTableDataChanged();
    }

    /**
     * Clear the filtered model.
     */
    synchronized public void clear() {
        while (this.filteredDefaultTableModel.getRowCount() > 0) {
            filteredDefaultTableModel.removeRow(0);
        }
    }

    synchronized public void hideTopic(TextEvent.Topic topic) {
        if (topicsToDisplay.contains(topic)) {
            topicsToDisplay.remove(topic);
            this.refreshFilteredModel();
        }
    }

    synchronized public void showTopic(TextEvent.Topic topic) {
        if (!topicsToDisplay.contains(topic)) {
            topicsToDisplay.add(topic);
            this.refreshFilteredModel();
        }
    }

    synchronized public void setMinLevel(int level) {
        if (level != this.minLevel) {
            this.minLevel = level;
            this.refreshFilteredModel();
        }
    }

    public DefaultTableModel getCompleteDefaultTableModel() {
        return completeDefaultTableModel;
    }

    public DefaultTableModel getFilteredDefaultTableModel() {
        return filteredDefaultTableModel;
    }

    public void save(String logApplicationPathName) {

        boolean logFileFormatCSV = Config.getConfigByName("tester.properties").getBoolean("logs.FILE_FORMAT_CSV", false);
        String extensionFile;
        String fileName;

        if (logFileFormatCSV) {
            extensionFile = ".csv";
        }
        else {
            extensionFile = ".log";
        }

        if (!this.scenarioName.contains("(callflow)")) {
            if (testcaseName == null) {
                fileName = logApplicationPathName + "application" + extensionFile;
            }
            else {
                fileName = logApplicationPathName + testcaseName + "/" + scenarioName + extensionFile;
            }

            //create a new file if it's a save from Save
            if ((this.listener == null) || (!(logApplicationPathName.equals(Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs/"))))) {
                this.listener = new FileTextListener(fileName, true);
            }

            for (int idx = 0; idx < this.completeDefaultTableModel.getRowCount(); idx++) {
                String name = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getText());
                int level = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getLevel());
                Topic topic = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getTopic());
                long timestamp = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getTimestamp());
                long index = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getIndex());
                boolean openTextEvent = (((TextEvent) this.completeDefaultTableModel.getValueAt(idx, 1)).getOpen());
                // only save if it's not a open log
                if (!openTextEvent) {
                    TextEvent e = new TextEvent(name, level, topic, timestamp, index, openTextEvent);
                    this.listener.printText(e);
                }
            }
        }
    }

    public int search(String search, int idx, JTable table) {
        int indexFor = 0;
        int indexColumn = 3;

        if (search == null) {
            return -1;
        }

        // when the last element of the table is a searched one, permit to restart the loop
        if ((idx == this.filteredDefaultTableModel.getRowCount()) && (searchFindValue)) {
            idx = 0;
            searchFindValue = false;
        }

        for (indexFor = idx; indexFor < this.filteredDefaultTableModel.getRowCount(); indexFor++) {
            if (((TextEvent) this.filteredDefaultTableModel.getValueAt(indexFor, 0)).getText().toLowerCase().contains(search)) {
                searchFindValue = true;
                for (indexColumn = 3; indexColumn < this.filteredDefaultTableModel.getColumnCount(); indexColumn++) {
                    if (((TextEvent) this.filteredDefaultTableModel.getValueAt(indexFor, indexColumn)) != null) {
                        JViewport viewport = (JViewport) table.getParent();
                        Rectangle rect = table.getCellRect(indexFor, indexColumn, true);
                        Point pt = viewport.getViewPosition();
                        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
                        viewport.scrollRectToVisible(rect);
                    }
                 }
                    return indexFor;
                }

                //restart the loop when the last element of the table is not a searched one
                if ((indexFor == this.filteredDefaultTableModel.getRowCount() - 1) && (searchFindValue)) {
                    indexFor = -1;
                    searchFindValue = false;
                }
            }

            //nothing found
            return -1;
        }
    }
