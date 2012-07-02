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

package com.devoteam.srit.xmlloader.core.log;

import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that receive text events and save them to a file.
 * @author pn007888
 */
public class FileTextListener implements TextListener {

    private BufferedWriter bufferedWriter;
    private String fileName;
    private boolean appendFile;
    private boolean firstUse;

    /**
     * Create the class. If backup is not activate, then the file is not
     * activated (nor the parent directory structure).
     * @param name the name of the file to create
     * @throws IOException
     */
    public FileTextListener(String name, boolean append) {
        firstUse = true;
        fileName = name;
        bufferedWriter = null;
        appendFile = append;
    }

    /**
     * Print a text with a log level.
     * @param e a text event containing text + logLevel
     */
    public void printText(TextEvent e) {
        if (bufferedWriter == null) {
            try {
                if (firstUse == false) {
                    appendFile = true;
                }

                firstUse = false;
                File file = new File(fileName);
                if (!file.getParentFile().exists()) {
                    // Parent directory does not exists ! create it
                    file.getParentFile().mkdirs();
                }

                bufferedWriter = new BufferedWriter(new FileWriter(file, appendFile));
            }
            catch (IOException ex) {
                dispose();
                ex.printStackTrace();
            }
        }

        if (bufferedWriter != null) {
            try {
                try {
                    bufferedWriter.write(LogHelper.getText(e));
                    bufferedWriter.newLine();
                }
                catch (Exception ex) {
                    GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "print ", e.getText() , " in file : ", bufferedWriter);
                }
                bufferedWriter.flush();
            }
            catch (IOException ex) {
                dispose();
                ex.printStackTrace();
            }
        }
    }

    /**
     * Free resources. Close file.
     */
    public void dispose() {
        try {
            if (bufferedWriter != null) {
                BufferedWriter _bufferedWriter = bufferedWriter;
                bufferedWriter = null;
                _bufferedWriter.close();
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Closed log file :", fileName);
            }
        }
        catch (IOException e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Unable to close log file ", fileName);
        }
    }
}
