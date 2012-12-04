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

package com.devoteam.srit.xmlloader.core.api;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListener;
import com.devoteam.srit.xmlloader.core.log.TextListenerKey;
import com.devoteam.srit.xmlloader.core.log.TextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.io.File;
import java.net.URI;
import java.util.concurrent.Semaphore;

/**
 *
 * @author gpasquiers
 */
public class TestAPI {

    static NotificationListener listener = new NotificationListener<Notification<String, RunnerState>>() {

        public void notificationReceived(Notification<String, RunnerState> notification) {
            System.err.println(notification.getSource() + " ==> " + notification.getData());
        }
    };

    static public void main(String... args) {


        /*
         * Set the MTS BIN directory
         */
        try {
            // URIRegistry.MTS_BIN_HOME = new URI("file:/D:/ws_gpasquiers_mts/gpasquiers_view2/srit_tools/ngn_tools/xmlSipLoader/bin/");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Create, initialize, then register the HashMap file system.
         */
        try {
            HashMapFileSystem fs = new HashMapFileSystem();
            fs.loadDirFromdisk(URIRegistry.MTS_BIN_HOME.resolve("../conf/"));
            fs.loadDirFromdisk(URIRegistry.MTS_BIN_HOME.resolve("../tutorial/"));
            SingletonFSInterface.setInstance(fs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        /*
         * Add our test.
         */
        try {
            HashMapFileSystem fs = (HashMapFileSystem) SingletonFSInterface.instance();
            String testFile =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<test name=\"test\">"
                    + "<testcase name=\"testcase\">"
                    + "<scenario name=\"scenario\">scenario.xml</scenario>"
                    + "</testcase>"
                    + "</test>";
            fs.addFile(new URI("test.xml"), testFile);

            String scenarioFile =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<scenario>"
                    + "<log>MYTAG\n"
                    + "toto=tutu\n"
                    + "titi=grominet\n"
                    + "\n"
                    + "Blablabla"
                    + "</log>"
                    + "</scenario>";
            fs.addFile(new URI("scenario.xml"), scenarioFile);

            fs.dumpToDisk(new File("d:/mts virtualdisk_dump/").toURI());
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        /*
         * Register the File logger provider
         */
        //TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());


        /*
         * Register our custom logger
         */
        try {
            final SerializingTextListener serializingTextListener = new SerializingTextListener(new URI("file:/D:/temp/temp.txt"));

            TextListenerProviderRegistry.instance().register(new TextListenerProvider() {

                TextListener textListener = new TextListener() {

                    public void printText(TextEvent e) {
                        if (e.getText().startsWith("MYTAG")) {
                            System.out.println(e.getText());
                        }
                        serializingTextListener.printText(e);
                    }

                    public void dispose() {
                        // nothing to do
                    }
                };

                public TextListener provide(TextListenerKey key) {
                    return textListener;
                }

                public void dispose(TextListenerKey key) {
                    // nothing to do
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        try {

            APISingleton.instance("test_1").openTest(new URI("../tutorial/core/test.xml"), TestRunnerSequential.class);
            APISingleton.instance("test_1").getTestRunner().addListener(listener);
            APISingleton.instance("test_1").getTestRunner().start();

            final Semaphore semaphore1 = new Semaphore(0);
            APISingleton.instance("test_1").getTestRunner().addListener(new NotificationListener<Notification<String, RunnerState>>() {
                boolean _couldBeFinished = false;

                @Override
                public void notificationReceived(Notification<String, RunnerState> notification) {
                    if (_couldBeFinished && (notification.getData().isFinished() || notification.getData().couldNotStart())) {
                        APISingleton.instance("test_1").getTestRunner().removeListener(this);
                        semaphore1.release();
                    }
                    else {
                        _couldBeFinished = true;
                    }
                }
            });
            semaphore1.acquire();

            // Does not work in parallel because core access to a properties file
            APISingleton.instance("test_2").openTest(new URI("../tutorial/http/test.xml"), TestRunnerSequential.class);
            APISingleton.instance("test_2").getTestRunner().addListener(listener);
            APISingleton.instance("test_2").getTestRunner().start();

            final Semaphore semaphore2 = new Semaphore(0);
            APISingleton.instance("test_2").getTestRunner().addListener(new NotificationListener<Notification<String, RunnerState>>() {
                boolean _couldBeFinished = false;

                @Override
                public void notificationReceived(Notification<String, RunnerState> notification) {
                    if (_couldBeFinished && (notification.getData().isFinished() || notification.getData().couldNotStart())) {
                        APISingleton.instance("test_2").getTestRunner().removeListener(this);
                        semaphore2.release();
                    }
                    else {
                        _couldBeFinished = true;
                    }
                }
            });
            semaphore2.acquire();

            // generate stat report
            APISingleton.getReportGenerator("/test/TEST_API_").generateReport();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
