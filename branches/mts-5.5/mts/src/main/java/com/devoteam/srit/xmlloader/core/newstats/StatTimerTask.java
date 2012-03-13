/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.newstats;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Config;
//import com.devoteam.srit.xmlloader.master.mastergui.JFrameMaster;
//import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.master.gui.JFrameMasterCtrl;
import java.util.TimerTask;

/**
 *
 * @author bbouvier
 */
public class StatTimerTask extends TimerTask
{
    private String mode = null;

    /** Creates a new instance of StatTimerTask */
    public StatTimerTask(String aMode)
    {
        mode = aMode;
    }

    //execute the task
    public void run()
    {
    	try
    	{
            StatPool stat = null;
            long zeroTimestamp = -1;
            String name = null;

            if(mode.equalsIgnoreCase("standalone"))
            {
                name = "/TEST_" + Tester.instance().getTest().getName();
                stat = StatPool.getInstance().clone();
                zeroTimestamp = StatPool.getInstance().getZeroTimestamp();
                StatPool.getInstance().reset();
            }
            else if(mode.equalsIgnoreCase("master"))
            {
                JFrameMasterCtrl.getInstance().doClickReport();
            }

            // generate stat report
        	String dirName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY","../reports/");
        	String fileName = dirName + name;
        	ReportGenerator reportGen = new ReportGenerator(fileName);
        	reportGen.generateReport(stat, zeroTimestamp);
        }
    	catch (Exception e) {
    		GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error while trying to generate automatically report ");
    	}
    }
}
