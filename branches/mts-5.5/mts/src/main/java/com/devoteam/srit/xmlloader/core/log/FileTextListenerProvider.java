/*
 * Created on Nov 9, 2004
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
                filename = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs") + ((ScenarioRunner) key).getParent().getRunId() + "/" + ((ScenarioRunner) key).getScenario().getName() + extensionFile;
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
