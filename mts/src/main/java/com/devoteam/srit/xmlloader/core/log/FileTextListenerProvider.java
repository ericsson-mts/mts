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

import com.devoteam.srit.xmlloader.core.TestcaseRunner;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.util.HashMap;

/**
 * @author gpasquiers
 */
public class FileTextListenerProvider implements TextListenerProvider {

    HashMap<Object, FileTextListener> registry = new HashMap<Object, FileTextListener>();

    public FileTextListenerProvider() {
    }

    public TextListener provide(TextListenerKey key) {
        if (GlobalLogger.instance().getLogStorage() != GlobalLogger.LOG_STORAGE_FILE) {
            return null;
        }

        if (null != key && key instanceof TestcaseRunner) {
            return null;
        }

        if (null != key && key instanceof ScenarioRunner) {
            if (((ScenarioRunner) key).getState().isFinished()){
                return null;
            }
        }

        FileTextListener listener = registry.get(key);

        boolean logFileFormatCSV = Config.getConfigByName("tester.properties").getBoolean("logs.FILE_FORMAT_CSV", false);
        String extensionFile;
        if (logFileFormatCSV) {
            extensionFile = ".csv";
        }
        else {
            extensionFile = ".log";
        }

        if (null == listener) {
            String filename;
            if (null == key) {
                filename = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs") + "application" + extensionFile;
                listener = new FileTextListener(filename, true);
            }
            else {
                filename = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs") + ((ScenarioRunner) key).getParent().getRunId() + "/" + ((ScenarioRunner) key).getScenarioReference().getName() + extensionFile;
                listener = new FileTextListener(filename, false);
            }
            registry.put(key, listener);
        }

        return listener;
    }

    public void dispose(TextListenerKey key) {
        FileTextListener listener = registry.get(key);

        if (null != listener) {
            registry.remove(key);
            listener.dispose();
        }
    }
}
