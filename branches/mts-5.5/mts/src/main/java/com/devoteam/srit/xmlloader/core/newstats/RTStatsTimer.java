/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.newstats;


import com.devoteam.srit.xmlloader.core.TestRunnerCounter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRTStats;
import com.devoteam.srit.xmlloader.gui.model.ModelTreeRTStats;
import java.awt.event.ActionListener;

/**
 * Timer for refresh Real times Statistiques
 * @author bthou
 */
public class RTStatsTimer implements ActionListener{

    // Instance of this RTStats
    private static RTStatsTimer instance = null;

    // Timer
    private Timer clock = null;

    // A flag for say if we need to refresh RTStats window
    // (example: if a test during less than the interval of an automatic refresh)
    private static boolean shouldRefresh = false;
    
    // Constructer for RTStatsTimer
    public RTStatsTimer(){

        // We call the constructer of the parent-class
        super();

        // We get the configuration of GUI_REFRESH_INTERVAL
        double interval = Config.getConfigByName("tester.properties").getDouble("stats.GUI_REFRESH_INTERVAL", 1);

        // We create a new timer with this config value as interval
        clock = new Timer((int)(interval * 1000), this);

        // We want an action repeating
        clock.setRepeats(true);

        // We start the timer
        clock.start();

        // We record this in the logger
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Refresh interval for real-times statistics is ", interval, "s");
    }

    // Method for get the current version of the RTStatsTimer
    public final static RTStatsTimer getInstance()
    {
        return instance;
    }

    // Method for get the current version of the RTStatsTimer
    // and create an instance if it doesn't exist
    public final static RTStatsTimer instance()
    {
        if (instance == null)
        {
            instance = new RTStatsTimer();
        }
        return instance;
    }

    // Method for paused the timer
    public void pause()
    {
        synchronized(this)
        {
            // We stop the timer
            clock.stop();

            // We unreference it
            instance=null;
        }
    }

    // Method for wake up the timer
    public void unpause()
    {
        synchronized(this)
        {
            // We restart the timer
            clock.start();
        }
    }

    // Method for init a flag if we nedd to refresh the RTStats window
    public static void shouldRefresh()
    {
        shouldRefresh = true;
    }

    // Actions performed by the timer
    public void actionPerformed(ActionEvent e) {

        // If there is more than one test currently running or if we need to refresh
        if(shouldRefresh || TestRunnerCounter.instance().runningTestsCount() > 0)
        {
            if(ModelTreeRTStats.instance() != null && JFrameRTStats.instance() != null)
            {
                // We demand the regeneration of the tree
                ModelTreeRTStats.instance().reGenerateTree();

                // We demand to update the panel of stats
                JFrameRTStats.instance().updatePanel();
            }

            // We use the flag for say "we have refresh the RTStats window"
            shouldRefresh = false;
        }

    }

}
