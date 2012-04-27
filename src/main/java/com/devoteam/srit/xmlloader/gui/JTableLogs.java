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

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import java.awt.Component;
import java.text.ParseException;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.log.TextListener;
import com.devoteam.srit.xmlloader.core.utils.*;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextArea;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author pn007888
 */
public class JTableLogs extends JTable implements TextListener {

    private String title;
    private JTableLogsModel jTableLogsModel;
    private JTableLogs testcaseTableLogs;
    private String lineTextEvent = null;
    private String lineTextEventReturn = null;
    private String lineTemp = "";
    private int sizeLine = 0;
    private String search = null;
    private int index = -1;

    public JTableLogs(String title, String[] columns, String testCaseName) {
        super();

        if (null == columns) {
            this.jTableLogsModel = new JTableLogsModel(new String[]{"Date", "Level", "Topic", "Message"}, Config.getConfigByName("tester.properties").getInteger("logs.LINES_NUMBER", 1000), testCaseName, title);
        }
        else {
            this.jTableLogsModel = new JTableLogsModel(columns, Config.getConfigByName("tester.properties").getInteger("logs.LINES_NUMBER", 1000), testCaseName, title);
            this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        this.setModel(this.jTableLogsModel.getFilteredDefaultTableModel());

        this.setEnabled(true);
        init(title);
        addMouseListener(new java.awt.event.MouseAdapter() {
            //int lastSelected = -1;

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                collapseCell(((JTable) evt.getSource()).getSelectedRow());
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
//                JTable jTable = (JTable) e.getSource();
//                int itsRow = jTable.rowAtPoint(e.getPoint());
//                int itsColumn = jTable.columnAtPoint(e.getPoint());
//                jTable.getCellEditor(itsRow, itsColumn)
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
            }
        });

        super.setRowHeight(18);
    }

    public void collapseCell(int selected) {
        final JTable _this = this;
        //if(lastSelected == selected)
        {
            if (_this.getRowHeight(selected) != _this.getRowHeight()) {
                _this.setRowHeight(selected, _this.getRowHeight());
            }
            else {
                int maxHeight = _this.getRowHeight();
                for (int i = 0; i < _this.getColumnCount(); i++) {
                    Component component = _this.getCellRenderer(selected, i).getTableCellRendererComponent(_this, _this.getValueAt(selected, i), false, true, selected, i);
                    int height = (int) component.getPreferredSize().getHeight();

                    maxHeight = height > maxHeight ? height : maxHeight;
                }
                _this.setRowHeight(selected, maxHeight);
            }
        }

        //lastSelected = selected;
    }

    public void clean() {
        while (this.jTableLogsModel.getCompleteDefaultTableModel().getRowCount() > 0) {
            this.jTableLogsModel.getCompleteDefaultTableModel().removeRow(0);
        }

        while (this.jTableLogsModel.getFilteredDefaultTableModel().getRowCount() > 0) {
            this.jTableLogsModel.getFilteredDefaultTableModel().removeRow(0);
        }
    }

    public void setTestcaseTableLogs(JTableLogs table) {
        this.testcaseTableLogs = table;
    }

    private void init(String title) {
        this.title = title;

        setShowGrid(false);
        TableColumn column = null;
        for (int i = 0; i < getColumnCount(); i++) {
            column = getColumnModel().getColumn(i);
            switch (i) {
                case 0:
                    column.setMinWidth(90);
                    column.setMaxWidth(90);
                    column.setCellRenderer(new DateCellRenderer());
                    break;
                case 1:
                    column.setMinWidth(50);
                    column.setMaxWidth(50);
                    column.setCellRenderer(new LevelRenderer());
                    break;
                case 2:
                    column.setMinWidth(70);
                    column.setMaxWidth(70);
                    column.setCellRenderer(new TopicCellRenderer());
                    break;
                default:
                    column.setPreferredWidth(500);
                    column.setCellRenderer(new MessageRenderer());
                    break;
            }
        }
    }

    public void dispose() {
    }

    /**
     * Print a text with a log level.
     * Application log
     * @param e a text event containing text + logLevel
     */
    public void printText(TextEvent e) {
        this.jTableLogsModel.addRow(e, null, false);

        if (null != this.testcaseTableLogs) {

            this.testcaseTableLogs.printText(e, this.title, false);

        }
    }

    public void printText(TextEvent e, boolean open) {
        this.jTableLogsModel.addRow(e, null, open);

        if (null != this.testcaseTableLogs) {

            this.testcaseTableLogs.printText(e, this.title, open);

        }
    }

    /**
     * Prints the text passing the title with the event. This method should only be used
     * on a testcase-JTableLogs
     * Scenario Log
     */
    public void printText(TextEvent e, String title, boolean open) {
        this.jTableLogsModel.addRow(e, title, open);
    }

    /**
     * Reset the table. Remove all lines from the table.
     */
    public void reset() {
        this.jTableLogsModel.clear();
    }

    public void hide(TextEvent.Topic topic) {
        this.jTableLogsModel.hideTopic(topic);
    }

    public void show(TextEvent.Topic topic) {
        this.jTableLogsModel.showTopic(topic);
    }

    public void setLevel(int level) {
        this.jTableLogsModel.setMinLevel(level);
    }

    private class DateCellRenderer extends MyTableCellRenderer {

        private final SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss SSS");
        private final SimpleDateFormat completeDateformat = new SimpleDateFormat("dd.MM.yy HH:mm:ss SSS");

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Date date = new Date(((TextEvent) value).getTimestamp());
            JTextArea jTextArea = super.getJTextArea(isSelected);
            jTextArea.setText(dateformat.format(date));
            TextEvent textEvent = (TextEvent) table.getValueAt(row, 1);
            jTextArea.setForeground(GuiHelper.getColorForLevel(textEvent.getLevel()));
            jTextArea.setBackground(GuiHelper.getColorForTopic(textEvent.getTopic()));
            return jTextArea;
        }
    }

    private class TopicCellRenderer extends MyTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TextEvent textEvent = (TextEvent) value;
            JTextArea jTextArea = super.getJTextArea(isSelected);

            if (null != textEvent.getTopic()) {
                jTextArea.setText(textEvent.getTopic().toString());
            }
            else {
                jTextArea.setText("");
            }

            jTextArea.setForeground(GuiHelper.getColorForLevel(textEvent.getLevel()));
            jTextArea.setBackground(GuiHelper.getColorForTopic(textEvent.getTopic()));
            return jTextArea;
        }
    }

    private class LevelRenderer extends MyTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TextEvent textEvent = (TextEvent) value;
            JTextArea jTextArea = super.getJTextArea(isSelected);

            jTextArea.setText(textEvent.getLevelStr());
            jTextArea.setForeground(GuiHelper.getColorForLevel(textEvent.getLevel()));
            jTextArea.setBackground(GuiHelper.getColorForTopic(textEvent.getTopic()));
            return jTextArea;
        }
    }

    private class MessageRenderer extends MyTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JTextArea jTextArea = super.getJTextArea(isSelected);
            TextEvent textEvent = (TextEvent) table.getModel().getValueAt(row, column);

            if (null == value) {
                jTextArea.setText("");
                jTextArea.setBackground(Color.WHITE);
                jTextArea.setForeground(Color.BLACK);
            }
            else {
                String text = textEvent.getText();
                jTextArea.setText(text);

                jTextArea.setBackground(GuiHelper.getColorForTopic(textEvent.getTopic()));
                jTextArea.setForeground(GuiHelper.getColorForLevel(textEvent.getLevel()));

                jTextArea.setMinimumSize(new Dimension(table.getRowHeight(), table.getRowHeight()));

                // to get the prefered height
                jTextArea.setSize(table.getColumnModel().getColumn(column).getWidth(), -1);
                jTextArea.setSize(table.getColumnModel().getColumn(column).getWidth(), (int) jTextArea.getPreferredSize().getHeight());
                int preferedHeight = (int) Math.round(jTextArea.getPreferredSize().getHeight());

                // set the prefered height if is different
                if (table.getRowHeight(row) != preferedHeight && table.getRowHeight(row) != table.getRowHeight()) {
                    table.setRowHeight(row, preferedHeight);
                }
                if (search != null) {
                    if (text.toLowerCase().contains(search)) {
                        jTextArea.setBackground(Color.ORANGE);
                        jTextArea.setForeground(Color.BLACK);
                    }
                }
                if ((row == index) && (index > -1)) {
                    jTextArea.setBackground(Color.YELLOW);
                    jTextArea.setForeground(Color.BLACK);

                  //  table.setRowHeight(row, (int) jTextArea.getPreferredSize().getHeight());
                }

            }

            if (table.getRowHeight(row) != table.getRowHeight()) {
                jTextArea.setToolTipText("Click to collapse");
            }

            return jTextArea;
        }
    }

    public String getTitle() {
        return title;
    }

    public void save(URI logApplicationPathName) {
        this.jTableLogsModel.save(logApplicationPathName.getPath());
    }

    public void open(String testcaseIteration, String scenarioName, URI logApplicationPathName) throws Exception {

        File logApplicationPath = new File(logApplicationPathName);
        String[] listFile;
        boolean logFormatCSV;
        int i;
        TextEvent textEventReturn = null;
        BufferedReader bufferedFile;

        // Application Log
        if (testcaseIteration == null && scenarioName == null) {
            this.clean();
            boolean applicationLogFound = false;
            listFile = logApplicationPath.list();

            for (i = 0; i < listFile.length; i++) {

                if (listFile[i].contains("application")) {
                    File application = new File(logApplicationPathName.getPath() + listFile[i]);
                    applicationLogFound = true;
                    String nameFile = application.getName();
                    bufferedFile = new BufferedReader(new FileReader(application));
                    if (bufferedFile == null) {
                        throw new ParsingException("File not found : " + application);
                    }

                    if (nameFile.endsWith(".csv") || (nameFile.endsWith(".log"))) {
                        if (nameFile.endsWith(".csv")) {
                            logFormatCSV = true;
                        }
                        else {
                            logFormatCSV = false;
                        }
                        do {
                            textEventReturn = readFile(bufferedFile, logFormatCSV);
                            if (textEventReturn != null) {
                                printText(textEventReturn, true);
                            }
                        }
                        while (textEventReturn != null);
                        bufferedFile.close();
                    }
                }
            }
            if (!applicationLogFound) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, "File application (.log | .csv) not found in " + logApplicationPathName.getPath());
            }
        }
    }

    public TextEvent readFile(BufferedReader bufferedFile, boolean logFormatCSV) throws ParsingException, IOException, ParseException {

        String line = "";
        try {
            // CSV
            if (logFormatCSV) {
                while (line != null) {
                    line = bufferedFile.readLine();
                    if (line != null) {
                        if (!line.endsWith("\";")) {
                            this.lineTemp = this.lineTemp + "\n" + line;
                        }
                        else {
                            line = this.lineTemp + "\n" + line;
                            line = Utils.replaceNoRegex(line, "\"\"", "\"");
                            //to delete carrier return
                            line = line.substring(1);
                            this.lineTemp = "";
                            return lineToTextEvent(line);
                        }
                    }
                }
                return null;
            }
            // LOG
            else {
                while (line != null) {
                    int sizeLineTemp = 0;
                    String lineSizeLine = "";
                    line = bufferedFile.readLine();
                    if (line != null) {
                        if (line.startsWith(" ")) {
                            line = line.substring(this.sizeLine + 1);
                            this.lineTemp = this.lineTemp + "\n" + line;
                            this.lineTextEvent = this.lineTemp;
                        }
                        else {
                            lineSizeLine = line;
                            for (int idxSize = 0; idxSize < 4; idxSize++) {
                                sizeLineTemp = lineSizeLine.indexOf(";");
                                lineSizeLine = lineSizeLine.substring(sizeLineTemp + 1);
                                this.sizeLine = (line.length()) - (lineSizeLine.length());
                            }
                            this.lineTemp = line;
                            if (this.lineTextEvent != null) {
                                this.lineTextEventReturn = this.lineTextEvent;
                                this.lineTextEvent = line;
                                return lineToTextEvent(this.lineTextEventReturn);
                            }
                            this.lineTextEvent = line;
                        }
                    }
                    else {
                        if (this.lineTextEvent != null) {
                            this.lineTextEventReturn = this.lineTextEvent;
                            this.lineTextEvent = null;
                            return lineToTextEvent(this.lineTextEventReturn);
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            throw new ParsingException(e.getMessage());
        }
        catch (IOException e) {
            throw new ParsingException(e.getMessage());
        }
        return null;
    }

    public TextEvent lineToTextEvent(String line) throws ParseException {
        int idx = line.indexOf(";");
        long index = Long.parseLong(line.substring(0, idx));
        line = line.substring(idx + 1);

        //to obtain timestamp
        idx = line.indexOf(";");
        String date = line.substring(0, idx);
        SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss SSS");
        Date time = dateformat.parse(date);
        long timestamp = time.getTime();

        //to obtain level
        line = line.substring(idx + 1);
        idx = line.indexOf(";");
        int level = TextEvent.string2Level(line.substring(0, idx));

        //to obtain topic
        line = line.substring(idx + 1);
        idx = line.indexOf(";");
        Topic topic = null;
        String topicStr = line.substring(0, idx);
        if (topicStr.equalsIgnoreCase("callflow")) {
            topic = Topic.CALLFLOW;
        }
        if (topicStr.equalsIgnoreCase("core")) {
            topic = Topic.CORE;
        }
        if (topicStr.equalsIgnoreCase("master")) {
            topic = Topic.MASTER;
        }
        if (topicStr.equalsIgnoreCase("param")) {
            topic = Topic.PARAM;
        }
        if (topicStr.equalsIgnoreCase("protocol")) {
            topic = Topic.PROTOCOL;
        }
        if (topicStr.equalsIgnoreCase("user")) {
            topic = Topic.USER;
        }

        //to obtain text
        String text;
        line = line.substring(idx + 1);
        text = line.substring(0, line.length() - 1);
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }

        boolean open = true;
        TextEvent textEvent = new TextEvent(text, level, topic, timestamp, index, open);
        return textEvent;
    }

    public int search(String search, int idx) {
        JTable _this = this;
        _this.clearSelection();
        this.search = search;
        this.repaint();

        this.index = this.jTableLogsModel.search(search, idx, this);

        return index;
    }
}
