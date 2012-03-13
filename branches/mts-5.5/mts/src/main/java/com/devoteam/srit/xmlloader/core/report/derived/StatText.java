package com.devoteam.srit.xmlloader.core.report.derived;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.report.HTML;
import com.devoteam.srit.xmlloader.core.report.SectionReportGenerator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;

public class StatText extends DerivedCounter
{

    public StatText(long timestamp, StatKey id, StatCounter statCounter, CounterReportTemplate template) throws ParsingException
    {
        super(timestamp, statCounter);
        this.id = id;
        this.template = template;
    }

    // Methods for generation of HTML report Statistics
    @Override
    public String generateShortReport(String link)
    {
        StringBuilder sb = new StringBuilder();

        String[] values = {this.counter.globalDataset.getText()
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
                sb.append(HTML.divO("", "global"));
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
                            HTML.tableCell("", "key", counterName.toString()) +
                            HTML.tableCell("", "value", this.counter.globalDataset.getText()))));
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
        panel.add(new JLabel(this.counter.globalDataset.getText()));
        panel.setToolTipText(generateRTStatsToolTip());
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

        // All parameters of this counter
        htmlPanel += "<tr><td width=\"50%\" bgcolor=\""+bgColorTitle+"\"><b>"+template.name+"</b></td><td>"+this.counter.globalDataset.getText()+"</td></tr>";

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
    	String name = resultant + ".Text"; 
		param.add(name);
		runner.getParameterPool().createSimple(name, this.counter.globalDataset.getText());
    }

}
