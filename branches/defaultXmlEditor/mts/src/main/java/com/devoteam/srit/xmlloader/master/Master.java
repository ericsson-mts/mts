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

package com.devoteam.srit.xmlloader.master;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterTestRegistry;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.SwingExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import com.devoteam.srit.xmlloader.master.master.gui.JFrameMasterCtrl;
import com.devoteam.srit.xmlloader.master.master.gui.JFrameMasterView;
import java.io.File;
import java.io.PrintStream;
import javax.swing.UIManager;

/**
 *
 * @author
 * gpasquiers
 */
public class Master {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Redirect the output in a file but only when starting the master GUI
        try {
            File file = new File("../logs/stdout_master.log");
            PrintStream print = new PrintStream(file);
            System.setOut(print);
            System.setErr(print);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        init();
        
        JFrameMasterView jFrameMasterView = new JFrameMasterView();

        ExceptionHandlerSingleton.setInstance(new SwingExceptionHandler(jFrameMasterView));

        JFrameMasterCtrl jFrameMasterCtrl = new JFrameMasterCtrl(jFrameMasterView);
    }

    public static void init() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Init FSInterface
        SingletonFSInterface.setInstance(new LocalFSInterface());

        // Override tester.properties/logs.STORAGE_DIRECTORY for the master filename = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY","../logs") + "/application.log";
        PropertiesEnhanced properties = new PropertiesEnhanced();
        properties.addPropertiesEnhancedComplete("logs.STORAGE_DIRECTORY", Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs") + "/master");
        Config.overrideProperties("tester.properties", properties);

        // Register the GUI logger provider
        TextListenerProviderRegistry.instance().register(GUITextListenerProvider.instance());

        // Register the File logger provider
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());

        // Initialize the pluggable components
        ParameterOperatorRegistry.initialize();
        ParameterTestRegistry.initialize();

        // Initialize the Statistics to automatically generate periodically statistics report
        StatPool.initialize("master");
    }
}
