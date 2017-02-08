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

package com.devoteam.srit.xmlloader.core.report;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;

import org.dom4j.Element;

/**
 * @author pn007888
 */
public class ReportGenerator implements NotificationSender<Notification<String, ReportStatus>>
{
    
    private final static String REPORT_TEMPLATE = "../conf/stats/reportTemplate.html";
    private final static String STYLESHEET_FILE = "../conf/stats/stylesheet.css";
    private final static String STR_DATA_REPORT = "#DATA_REPORT#";
    
    private String reportDirectory;
    private String reportIndex;   
    
    private SectionReportGenerator sectionRP;
 
    private ReportStatus reportStatus = new ReportStatus();
    
    public ReportGenerator(String reportDirectory) throws Exception
    {
        // add timestamp to the report
        reportDirectory.replaceAll("[/]*\\z", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm'm'ss's'");
        reportDirectory += "_" + df.format(Calendar.getInstance().getTime());

        this.reportDirectory = reportDirectory;
        Utils.deleteRecursively(this.reportDirectory);
        this.reportIndex = "_report.html";       
        
    	// clean expires entries in stack list  
        StackFactory.cleanStackLists();
    }

    /**
     * @return the path of the report file
     */
    public String getReportFileName()
    {
        return this.reportIndex;
    }

    public String getReportDirectory()
    {
        return this.reportDirectory;
    }
    
    /**
     * 
     */
    public void setReportDirPath(String reportDirPath)
    {
        this.reportDirectory = reportDirPath.trim();
        File f = new File(reportDirPath + "/_report.html");
        try
        {
            this.reportIndex = f.getCanonicalPath();
        }
        catch (IOException e)
        {
            this.reportIndex = f.getAbsolutePath();
        }
    }

    /**
     * Generate the report. Report is based on stats and data received since
     * last reset() call.
     */
    public void generateReport()
    {    	
    	generateReport(StatPool.getInstance().clone(), StatPool.getInstance().getLastTimestamp());
    }

    /**
     * Generate the report. Report is based on stats and data received since
     * last reset() call.
     */
    public void generateReport(StatPool statPool, long zeroTimestamp)
    {
    	PrintWriter printWriter = null;
        try
        {
            statPool.setUpdateLastTimestamp(false);
            
            this.sectionRP = new SectionReportGenerator(statPool, this.reportDirectory);
            if (Tester.getInstance() != null && Tester.getInstance().getTest() != null)            
            {
            	statPool.addStatsStaticTestParameters(Tester.getInstance().getTest());
            }
            
        	// build the main report file name and create the report home directory
            File report = new File(this.reportDirectory + "/" + this.reportIndex);
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generating report. Please wait...");
            if (!report.getParentFile().exists())
            {
                report.getParentFile().mkdirs();

            }    

            // copy stylesheet
            Utils.copyFile(new URI(STYLESHEET_FILE), new File(this.reportDirectory + "/stylesheet.css").toURI());

            // create presentation data for counters into the "Config" section
            prepareReportConfiguration();          
            
            // build all report files using the template
            printWriter = new PrintWriter(new FileWriter(report));
            generateReportFromTemplate(printWriter);

            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Report ready..., see  file://", report.getAbsolutePath());
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generating protocol reports");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Unable to generate report :", e);
        }
        finally
        {
            printWriter.flush();
            printWriter.close();        	
        }
        
        this.reportStatus.setProgression(100);
        this.reportStatus.setDescription("report done");
        this.notifyAll(new Notification<String, ReportStatus>("reportGenerator", this.reportStatus));
    }

    private void generateReportFromTemplate(PrintWriter printWriter) throws Exception
    {       
        String reportListSections = Config.getConfigByName("tester.properties").getString("stats.LIST_SECTIONS", "");
        String[] listSections = reportListSections.split(",");

      // For each section
      int i = 0;
      for(String section : listSections)
      {
          // We convert the section to lower case
          section = section.toLowerCase();

          // If we have a space before the name of the section, we remove it
          if(section.charAt(0)==' ')
          {
              section = section.substring(1, section.length());
          }
          listSections[i] = section;
          i++;
      }

	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(new URI(REPORT_TEMPLATE))));
	    String line = bufferedReader.readLine();
	    while (line != null)
	    {    		
	        if (line.indexOf(STR_DATA_REPORT) >= 0)
	        {
	        	line = generateSections(listSections);
	        }
	        printWriter.println(line);
	        line = bufferedReader.readLine();
	    }
	    bufferedReader.close();
    }
    
    private void prepareReportConfiguration() throws Exception
    {        
    	/*if (Tester.instance() != null)
    	{
    		List<Element> list = Tester.instance().getTest().getEditableParameters();
        
	        for(Element elementEditable:list)
	        {
	            String name = elementEditable.attributeValue("name");
	            String paramName = name.substring(1, name.length() - 1);
	            String description = elementEditable.attributeValue("description");
	            String value = elementEditable.attributeValue("value");
	            StatKey statKey = new StatKey(StatPool.PREFIX_CONFIG, "_" + paramName);
	            this.statPool.addValue(statKey, value + " : " + description);
	            // create the corresponding template
	            CounterReportTemplate template = new CounterReportTemplate("<text>", statKey, null, "_" + name, paramName, description);
	            StatCounterConfigManager.getInstance().addTemplateList(new StatKey(StatPool.PREFIX_CONFIG), template);
	        }
    	}*/
    }

    private String generateTabs(String[] listSection, int index) throws Exception
    {
    	String ret = "<A name='";
    	ret += listSection[index];
    	ret += "'>";
    	ret += "<table bgcolor='#ffffff' width='100%' border='0' cellpadding='2' cellspacing='1'>\n";
    	ret += "<tr bgcolor='#ffffff' align='center'>\n";
    	
        Integer tabWidth = 100/listSection.length;

    	for (int i = 0; i < listSection.length; i++)
    	{
    		if (index == i)
    		{
    			ret += "<td width='"+tabWidth.toString()+"%' bgcolor='#C3740D'><font color='#ffffff'>";
    			ret += listSection[i]; 
    			ret += "</font></td>\n";
    		}
    		else
    		{
    			ret += "<td width='"+tabWidth.toString()+"%' bgcolor='#93A685'><a class='menuItem' href='#";
    			ret += listSection[i];
    			ret += "'><font color='#ffffff'>";    			
    			ret += listSection[i];
    			ret += "</font></a></td>\n";
    		}
    	}
    	ret += "</tr>\n";
    	ret += "</table>\n";
    	ret += "</A>\n";
    	return ret;
    }

    private String generateSections(String[] listSection) throws Exception
    {
    	String  ret = "";
		for (int i = 0; i < listSection.length; i++)
		{
			ret += generateTabs(listSection, i);
			ret += this.sectionRP.generateReport(new StatKey(listSection[i].trim().toLowerCase()));
			refreshProgressBar(listSection[i], listSection.length);
		}
		return ret;
    }
    
    private void refreshProgressBar(String section, int number)
    {
        this.reportStatus.setProgression(reportStatus.getProgress() + 100 / number);
        this.reportStatus.setDescription(section);
        this.notifyAll(new Notification<String, ReportStatus>("reportGenerator", this.reportStatus));
    }
        
    /**
     * Show the report
     */
    static public void showReport(URI reportFileName)
    {
        try
        {
            // Mantis 0000112
            String browser = Config.getConfigByName("tester.properties").getString("stats.BROWSER_PATH");
            browser = Utils.normalizePath(browser);
            if (browser != null)
        	{
	            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Opening web browser with command :\n", browser, " ", reportFileName);
	            Runtime.getRuntime().exec(browser + " " + reportFileName);
        	}
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Unable to start web browser");
        }
    }
    
    private DefaultNotificationSender defaultNotificationSender = new DefaultNotificationSender<Notification<String, ReportStatus>>();
    
    public void addListener(NotificationListener<Notification<String, ReportStatus>> listener)
    {
        this.defaultNotificationSender.addListener(listener);
        listener.notificationReceived(new Notification<String, ReportStatus>("reportGenerator", this.reportStatus));
    }

    public void removeListener(NotificationListener<Notification<String, ReportStatus>> listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, ReportStatus> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }
}
