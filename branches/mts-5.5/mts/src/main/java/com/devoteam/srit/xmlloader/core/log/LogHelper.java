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

import java.text.SimpleDateFormat;
import java.util.Date;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 * @author pn007888
 */
public class LogHelper {

    private final static SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss SSS");

    /**
     * Build a String containing the current date, the debug level and the text to print
     * @param e object TextEvent containing the log level, the topic and the text of the event
     * @return the concatened String
     */
    public static String getText(TextEvent e) throws Exception {

        boolean logFileFormatCSV = Config.getConfigByName("tester.properties").getBoolean("logs.FILE_FORMAT_CSV");

        String separator = ";";
        StringBuilder buffer = new StringBuilder();

        buffer.append(e.getIndex());
        buffer.append(separator);

        //Date date = new Date(System.currentTimeMillis());
        Date date = new Date(e.getTimestamp());

        buffer.append(dateformat.format(date));
        buffer.append(separator);
        buffer.append(TextEvent.level2String(e.getLevel()));
        buffer.append(separator);
        buffer.append(e.getTopic());
        buffer.append(separator);

        String s = e.getText();
        s = Utils.replaceNoRegex(s, "\r\n", "\n");

        String nameOS = System.getProperty("os.name");
        if (nameOS.contains("Win")) {
            s = Utils.replaceNoRegex(s, "\n", "\r\n");
        }


        if (!logFileFormatCSV) {
            if (s.indexOf("\n") != -1) {
                String espaces = "";
                for (int i = 0; i < buffer.length() + 1; i++) {
                    espaces += " ";
                }
                s = Utils.replaceNoRegex(s, "\n", "\n" + espaces);
            }
        }
        else {
            s = Utils.replaceNoRegex(s, "\t", "");
            s = Utils.replaceNoRegex(s, "\"", "\"\"");
            buffer.append("\"");
        }

        buffer.append(s);

        if (logFileFormatCSV) {
            buffer.append("\"");
        }

        buffer.append(separator);

        //buffer.append("\n");
        return buffer.toString();
    }
}
