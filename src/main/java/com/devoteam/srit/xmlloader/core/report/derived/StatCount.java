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

package com.devoteam.srit.xmlloader.core.report.derived;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.report.HTML;
import com.devoteam.srit.xmlloader.core.report.SectionReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;

public class StatCount extends DerivedCounter
{
    public StatCount(long timestamp, long zeroTimestamp, StatKey id, StatCounter statCounter, CounterReportTemplate template) throws ParsingException
    {
        super(timestamp, zeroTimestamp, statCounter);
        this.id = id;

        double[] graphArray = this.counter.graphDataset.getGraphArray();
        double previous = 0;

        for (int i = 0; i < graphArray.length; i++)
        {
            previous += graphArray[i];
            graphArray[i] = previous;
        }
        
        this.template = template;
        init();
    }

    // Methods for generation of HTML report Statistics
    @Override
    public String generateShortReport(String link)
    {
        StringBuilder sb = new StringBuilder();

        String[] values = {Utils.formatdouble(this.counter.globalDataset.getValue())
                          };

        sb.append(createToolTip(values, link));

        return sb.toString();
    }

    @Override
    public String generateLongReport(SectionReportGenerator protocolReportGenerator)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML.divO("", "detailedCounter"));
        {
            sb.append(generateLongReportText(false));

            sb.append(generateLongReportGraph(protocolReportGenerator));
        }
        sb.append(HTML.divC());
        return sb.toString();
    }

    @Override
    public String generateLongReportText(Boolean isToolTip)
    {
        StringBuilder sb = new StringBuilder();

            sb.append(HTML.divO("", "detailedCounterTextWrapper"));
            {
            sb.append(HTML.divO("", "name"));
            {
                    StringBuilder counterName = new StringBuilder();
                    if(!isToolTip)
                    {
                        counterName.append("<a href=\"#\" class=\"info\">");
                    }

                    counterName.append("<b>"+template.name+"</b>");

                    if(!isToolTip)
                    {
                        counterName.append("<span>"+template.complete+"</span></a>");
                    }

                    sb.append(
                        HTML.table("", "",
                            HTML.tableRow("", "",
                                HTML.tableCell("", "title", counterName.toString())
                            )
                        )
                    );
            }
            sb.append(HTML.divC());

            sb.append(HTML.divO("", "global"));
                {
                    sb.append(
                        HTML.table("", "",
                            HTML.tableRow("", "",
                                HTML.tableCell("", "key", "Count") +
                                HTML.tableCell("", "value", Utils.formatdouble(this.counter.globalDataset.getValue()))
                            )
                        )
                    );
                }
                sb.append(HTML.divC());
            }
            sb.append(HTML.divC());

        return sb.toString();
    }

    // Methods for generation of JPanel for Real Times Statistics
    @Override
    public JPanel generateShortRTStats()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.add(new JLabel(Utils.formatdouble(this.counter.globalDataset.getValue())));
        panel.setToolTipText(generateRTStatsToolTip());

        addMouseListenerForGraph(panel);

        return panel;
    }

    @Override
    public JPanel generateLongRTStats()
    {
        // Panel we will return with all information of this counter
        JPanel panel = new JPanel();

        // Layout for this panel
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        // Color of background for this panel
        panel.setBackground(new java.awt.Color(248, 248, 248));

        // We add as a Tooltip the long description of this counter
        panel.setToolTipText(template.complete);
        
        // We add this html code as a JLabel in the panel
        panel.add(new JLabel(generateLongStringHTML()));
        
        // We return the panel
        return panel;
    }
    
    @Override
    public String generateLongStringHTML()
    {
        // String for the construction of html code
        String htmlPanel;

        // Html code for discribe how is display this counter
        htmlPanel = "<html>";

        htmlPanel += "<table width=\"350px\" bgcolor=\""+bgColor+"\">";

        // Main title of this counter
        htmlPanel += "<tr bgcolor=\""+bgColorTitle+"\"><td colspan=2><b>"+template.name+"</b></td></tr>";

        // All parameters of this counter
        htmlPanel += "<tr><td width=\"50%\" bgcolor=\""+bgColorTitle+"\">"+"Count"+"</td><td>"+Utils.formatdouble(this.counter.globalDataset.getValue()) + ""+"</td></tr>";

        htmlPanel += "</table>";

        htmlPanel += "</html>";

        return htmlPanel;
    }

    @Override
    public JFreeChart getHistogramChart()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Methods for creation of Parameter for "statCounter" <parameter> operation
    @Override
    public void addParameterStats(Runner runner, Parameter param, String resultant) throws Exception
    {   
    	String name = resultant + ".Count"; 
		param.add(name);
		runner.getParameterPool().createSimple(name, this.counter.globalDataset.getValue());
    }
    
    private String generateLongReportGraph(SectionReportGenerator protocolReportGenerator)
    {
        StringBuilder sb = new StringBuilder();

            sb.append(HTML.divO("", "graph"));
            {
                try
                {
                    if(this.isCreateCSV())
                    {
                        String csvFileName = SectionReportGenerator.getUniqueFilename("time_csv", ".csv");
                        this.writeTimeCSV(protocolReportGenerator.getCurrentReportDir() + "/" + csvFileName);
                        sb.append(HTML.a("", csvFileName, "chart CSV"));
                        sb.append(HTML.br());
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                if(this.isCreateCharts())
                {
                    String graphFilename = SectionReportGenerator.getImageFilename();
                    sb.append(HTML.a("", graphFilename, HTML.img(null, "imgGraph", graphFilename)));
                    writeChartToFile(protocolReportGenerator.getCurrentReportDir() + "/" + graphFilename, this.getTimeChart());
                }
            }
            sb.append(HTML.divC());

        return sb.toString();
    }
}
