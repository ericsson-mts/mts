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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatCounterConfigManager;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.derived.DerivedCounter;
import com.devoteam.srit.xmlloader.core.utils.Helper;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class SectionReportGenerator
{
    private static int filenameIndex = 1000;
    private static String imageFileName = "img";
    private StatPool statPool;

    private String directory;
    
    public SectionReportGenerator(StatPool statPool, String directory) throws Exception
    {
        this.directory = directory; 
        this.statPool = statPool;     
    }
    
    public static synchronized String getImageFilename()
    {
        filenameIndex++;
        return filenameIndex + imageFileName + ".png";
    }

    public static synchronized String getUniqueFilename(String filename, String ext)
    {
        return filename + filenameIndex++ + ext;
    }

    public String getCurrentReportDir()
    {
        return this.directory;
    }
        
    public String generateReport(StatKey prefixKey) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<br/>");

        // We add the path of the current displayed section
        String[] path = prefixKey.getAllAttributes();
        StringBuilder railWay = new StringBuilder();

        int i = 0;

        if(path.length > 1)
        {

            for(String pathElement : path)
            {
                // If we are not on the last element of the rail way
                if(i != path.length-1)
                {
                    railWay.append(" &gt; ");
                    railWay.append("<a href=\"");
                    if(i == 0)
                    {
                        // If this a section (eg: protocol session etc...)
                        railWay.append("./_report.html");

                        // We add an anchor
                        railWay.append("#");
                        railWay.append(pathElement);
                    }
                    else
                    {
                        int j = 0;
                        for(String pathElement2 : path)
                        {
                            // While we are not more deeper than the name of the link
                            if(j <= i)
                            {
                                // If we are at the last part of the link
                                if(j == i)
                                {
                                    railWay.append("_");
                                }
                                // We remove special chars
                                pathElement2 = Utils.replaceFileName(pathElement2);
                                railWay.append(pathElement2);
                            }
                            j++;
                        }
                        railWay.append(".html");
                    }
                    railWay.append("\">");
                    railWay.append(pathElement.replace("<BR>","+"));
                    railWay.append("</a>");
                }
                else
                {
                    // If we are on the last element of the rail way
                    railWay.append(" &gt; ");
                    railWay.append(pathElement.replace("<BR>","+"));
                }
                i++;
            }

            sb.append(HTML.div(null, "path", railWay.toString()));

            sb.append("<br/>");

        }

        List<CounterReportTemplate> templateList = StatCounterConfigManager.getInstance().getTemplateList(prefixKey);

        String[] descendentSelect = CounterReportTemplate.concat(prefixKey.getAllAttributes(), "^[^_].*");
        List<StatKey> descendentsList = statPool.findMatchingKeyStrict(new StatKey(descendentSelect));
        Collections.sort(descendentsList);

        sb.append(HTML.div(null, "smalltitle", ""));
        sb.append(HTML.tableO("", "detailTable"));
        if (templateList.size() > 0)
        {
            sb.append("<tr>");
            String temp = HTML.table("", "",
                    HTML.tableRow("", "",
                    HTML.tableCell("", "title", "Summary")));

            sb.append("<td>" + temp + "</td>");
            for (CounterReportTemplate template : templateList)
            {
                temp = HTML.table("", "",
                        HTML.tableRow("", "",
                        HTML.tableCell("", "title", "<a href=\"#\" class=\"info\">"+template.summary+"<span>"+template.name+"</span></a>")));

                sb.append("<td>" + temp + "</td>");

            }
            sb.append("</tr>");
        }
        // ------------------------------------------------

        // for each descendant
        for (StatKey descendent : descendentsList)
        {
            String descName = prefixKey + "_" + descendent.getLastAttribute();

            // Avoid strange character in the filename because of OS restriction            
            descName = Utils.replaceFileName(descName);

            sb.append("<tr>");
            // link to descendent level report file
            sb.append("<td>");
            sb.append(HTML.a("descLink", "./" + descName + ".html", descendent.getLastAttribute()));
            sb.append("</td>");

            // general information about the descendent
            for (CounterReportTemplate template : templateList)
            {
                sb.append("<td>");
                DerivedCounter derivedCounter = template.getDerivedCounter(this.statPool, descendent);
                if (derivedCounter != null)
                {
                    String anchor = prefixKey.toString().replaceAll("/", "_")+"_"+descendent.getLastAttribute()+"_"+template.summary.replaceAll("\\s", "");
                    anchor = anchor.replaceAll("/", "_");
                    sb.append(derivedCounter.generateShortReport("./"+ descName + ".html"+"#"+anchor));
                    
                }
                else
                {
                    sb.append("-");
                }
                sb.append("</td>");

            }
            sb.append("</tr>");

            String generatedDescendentReport = this.generateReport(descendent);
            NSwriteReportToFile(descName, generatedDescendentReport);
        }

        // If there a table with short information
        if(templateList.size() > 0)
        {
            // We add the total line
            sb.append("<tr>");
            sb.append(HTML.tableCell("", "totalRow", "TOTAL"));
            for (CounterReportTemplate template : templateList)
            {
                sb.append("<td class=\"totalRow\">");
                DerivedCounter derivedCounter = template.getDerivedCounter(this.statPool, prefixKey);
                if (derivedCounter != null)
                {
                    String anchor = prefixKey.toString().replaceAll("/", "_")+"_"+template.summary.replaceAll("\\s", "");
                    anchor = anchor.replaceAll("/", "_");
                    sb.append(derivedCounter.generateShortReport("#"+anchor));
                }
                else
                {
                    sb.append("-");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");

        }
        
        sb.append(HTML.tableC());

        sb.append("<br/><br/>");

        // Long part of report
        for (CounterReportTemplate template : templateList)
        {
            DerivedCounter derivedCounter = template.getDerivedCounter(this.statPool, prefixKey);
            if (derivedCounter != null)
            {
                String anchor = prefixKey.toString().replaceAll("/", "_")+"_"+template.summary.replaceAll("\\s", "");
                sb.append(HTML.div(anchor, "", derivedCounter.generateLongReport(this)));
            }
        }

        return sb.toString();
    }

    public void NSwriteReportToFile(String name, String content)
    {
        PrintWriter pw;
        try
        {
            File reportFile = new File(this.directory + "/" + name + ".html");

            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generating report (", this.directory, "/", ".html) Please wait...");
            if (!reportFile.getParentFile().exists())
            {
                reportFile.getParentFile().mkdirs();
            }
            this.directory = reportFile.getParentFile().getCanonicalPath().replace('\\', '/');
            pw = new PrintWriter(new FileWriter(reportFile));

            pw.println("<html>");
            pw.println("<header>");
            pw.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"stylesheet.css\" />\n");

            pw.println("</header>");


            pw.println(content);
            pw.println("<BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR>");
            pw.println("</html>");
            pw.flush();
            pw.close();
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Report ready..., see  file://", reportFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Unable to generate report :", e);
        }
    }
}
