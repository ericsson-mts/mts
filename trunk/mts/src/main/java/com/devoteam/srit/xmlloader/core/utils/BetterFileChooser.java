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

package com.devoteam.srit.xmlloader.core.utils;

import java.io.File;
import java.util.concurrent.Callable;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * This class provides a workaround for bugs in the Windows implementation of
 * {@link JFileChooser} causing certain operations to deadlock.<br />
 * The deadlock pattern is always as follows:
 * <ul>
 * <li>The operation begins on the AWT EventQueue thread
 * <li>It launches another operation on the Swing-Shell daemon thread
 * <li>It waits for the Swing-Shell thread to finish the requested operation
 * <li>The Swing-Shell thread blocks waiting for a monitor that was locked earlier by
 * the AWT EventQueue thread.
 * </ul>
 * To work around this, these deadlock-prone operations are performed on a separate
 * thread and polled to detect the deadlock. If the deadlock occurs, the thread is
 * interrupted allowing the Swing-Shell thread to finish whatever it was doing, then
 * the operation is retried. Experience shows that in most cases, a retry is successful
 * - presumably due to the Swing-Shell thread executing repeated requests faster, or
 * without requiring the same locks again.<br />
 * Note that this code is entirely built upon experimental evidence and therefore there
 * can be no guarantee that it does what it is intended to do. On platforms other than
 * Windows, it behaves identically to JFileChooser.<br />
 * See Sun bugs 6741890, 6744953, and 6789084.
 */
public class BetterFileChooser extends JFileChooser {

    /** A reference to the Swing-Shell system thread, needed for deadlock detection. */
    private static Thread s_SwingShellThread = null;
    private static final boolean WINDOWS;
    static {
        String osName = System.getProperty("os.name");
        WINDOWS = osName.contains("Windows") || osName.contains("windows");
    }

    /**
     * A worker thread that executes the Callable passed to it in the constructor and
     * swallows all exceptions in case it is interrupted at any time.
     */
    protected static class DeadlockWorker<ReturnType> extends Thread {
        private volatile boolean m_SwallowAllExceptions = false;
        private final Callable<ReturnType> m_Callable;
        private ReturnType m_ReturnValue = null;

        public DeadlockWorker(Callable<ReturnType> operation) {
            super("DeadlockWorker");
            m_Callable = operation;
        }

        @Override
        public void interrupt() {
            m_SwallowAllExceptions = true;
            super.interrupt();
        }
 
        @Override
        public void run() {
            try {
                m_ReturnValue = m_Callable.call();
            } catch (Exception ex) {
                if (!m_SwallowAllExceptions) {
                    if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    }
                    throw new RuntimeException(ex);
                }
            }
        }

        public ReturnType getReturnValue() {
            return m_ReturnValue;
        }
    }

       /**
     * A watchdog object that wraps the Callable passed to it in a {@link DeadlockWorker}
     * which it polls periodically to see if it has deadlocked with the Swing-Shell
     * thread. If it finds that the deadlock has occurred, then it interrupts the worker
     * to break out of the deadlock.
     */
    protected static class DeadlockWatchdog<ReturnType> {

        /** The delay in milliseconds between two attempts to detect the deadlock. */
        private static final int POLL_DELAY_MS = 100;

        /** The deadlock-prone operation. */
        private final Callable<ReturnType> m_Operation;

        /** Whether our worker thread was finished without us interrupting it. */
        private boolean m_Completed = false;

        /** The value returned by the watched operation. */
        private ReturnType m_ReturnValue = null;

        public DeadlockWatchdog(Callable<ReturnType> callable) {
            m_Operation = callable;
        }

        /** A single attempt at performing the deadlock-prone operation. */
        public void runOnce() {
            DeadlockWorker<ReturnType> worker = new DeadlockWorker<ReturnType>(m_Operation);
            worker.start();

            // Ensure we have an up-to-date reference to the Swing-Shell thread.
            updateSwingShell();

            boolean deadlock = false;
            while (!deadlock) {

                // Give the worker some time to do its job.
                try {
                    worker.join(POLL_DELAY_MS);
                }
                catch (InterruptedException e) {
                    // ignore
                }

                // Finished? All good.
                if (!worker.isAlive()) {
                    m_Completed = true;
                    m_ReturnValue = worker.getReturnValue();
                    return;
                }

                // Not finished - detect deadlock between worker and Swing-Shell.
                if (s_SwingShellThread != null &&
                        s_SwingShellThread.getState() == Thread.State.BLOCKED &&
                        worker.getState() == Thread.State.WAITING) {
                    //Debug.log("Deadlock detected");
                    deadlock = true;
                }
            }

            // Deadlock! Terminate the worker so it can release the Swing-Shell thread.
            // The current operation may be retried later.
            worker.interrupt();

            // Wait for Swing-Shell to finish its job. Most of the time it is in WAITING
            // state, so it's fine to just wait until it gets into that state. (It might
            // do some more work meanwhile than strictly necessary, but never mind.)
            // This step is required so that whatever speeds up the Swing-Shell for a
            // possible next invocation is finished, increasing the chance for a
            // successful retry.
            while (s_SwingShellThread.getState() != Thread.State.WAITING) {
                try {
                    Thread.sleep(POLL_DELAY_MS);
                }
                catch (InterruptedException e) {
                }
            }
        }

        /** Attempt to perform the deadlock-prone operation at most nbRetries times. */
        public void runRetries(int nbRetries) {
            int count = 0;
            while (!m_Completed && ++count <= nbRetries) {
                runOnce();
            }
        }

        /** @return whether the operation was completed without deadlock. */
        public boolean completed() {
            return m_Completed;
        }

        /** @return the result of the operation, or null if it wasn't completed. */
        public ReturnType getReturnValue() {
            return m_ReturnValue;
        }
    }

    //---- Superclass constructors

    public BetterFileChooser() {
    }

    public BetterFileChooser(String s) {
        super(s);
    }

    public BetterFileChooser(File file) {
        super(file);
    }

    public BetterFileChooser(FileSystemView filesystemview) {
        super(filesystemview);
    }

    public BetterFileChooser(File file, FileSystemView filesystemview) {
        super(file, filesystemview);
    }

    public BetterFileChooser(String s, FileSystemView filesystemview) {
        super(s, filesystemview);
    }

    /**
     * Update the reference to the Swing-Shell thread.
     */
    private static void updateSwingShell() {
        if (s_SwingShellThread != null && s_SwingShellThread.isAlive()) {
            return;
        }
        s_SwingShellThread = null;
        for (Thread thread: Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals("Swing-Shell")) {
                s_SwingShellThread = thread;
                break;
            }
        }
    }

    //---- Operations wrapped in DeadlockWatchdogs

    @Override
    public void setSelectedFile(final File file) {
        if (!WINDOWS) {
            super.setSelectedFile(file);
            return;
        }

        Callable<Void> watched = new Callable<Void>() {
            // SRIT @Override
            public Void call() throws Exception {
                BetterFileChooser.super.setSelectedFile(file);
                return null;
            }
        };

        DeadlockWatchdog<Void> watchdog = new DeadlockWatchdog<Void>(watched);
        watchdog.runRetries(2);
        if (!watchdog.completed()) {
            //Debug.log("setSelectedFile failed");
        }
    }

    @Override
    public String getName(final File file) {
        if (!WINDOWS) {
            return super.getName(file);
        }

        Callable<String> watched = new Callable<String>() {
            // SRIT @Override
            public String call() throws Exception {
                return BetterFileChooser.super.getName(file);
            }
        };

        DeadlockWatchdog<String> watchdog = new DeadlockWatchdog<String>(watched);
        watchdog.runRetries(2);
        if (!watchdog.completed()) {
            //Debug.log("getName failed");
            return "???";
        }
        return watchdog.getReturnValue();
    }
}

