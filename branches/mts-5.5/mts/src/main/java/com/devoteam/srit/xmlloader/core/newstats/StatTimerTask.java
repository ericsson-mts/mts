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

package com.devoteam.srit.xmlloader.core.newstats;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Config;
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
