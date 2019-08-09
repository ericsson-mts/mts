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

import au.com.bytecode.opencsv.CSVWriter;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.report.SectionReportGenerator;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.util.SortOrder;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRTStats;
import com.devoteam.srit.xmlloader.gui.model.ModelTreeRTStats;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.SimpleTimeZone;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;

public abstract class DerivedCounter
{

    public StatKey id;
    public CounterReportTemplate template;
    protected StatCounter counter;
    protected long reportEndTimestamp;
    protected long reportZeroTimestamp;
    private boolean createCharts;
    private boolean createCSVs;
    private char csvSeparator;
    protected Double mean;
    protected Double std_dv;
    private JFreeChart timeChart = null;
    private JFreeChart histogramChart = null;

    // Color (in hexa) for background of the main tab
    String bgColor = "#F8F8F8";

    // Color (in hexa) for titles
    String bgColorTitle = "#E2E7DE";

    public DerivedCounter(long endTimestamp, long zeroTimestamp, StatCounter counter) throws ParsingException
    {
        this.counter = counter.clone();
        mean = std_dv = new Double(0);
        this.reportEndTimestamp = endTimestamp;
        this.reportZeroTimestamp = zeroTimestamp;
        this.createCharts = Config.getConfigByName("tester.properties").getBoolean("stats.GENERATE_CHARTS_PICTURES");
        this.createCSVs = Config.getConfigByName("tester.properties").getBoolean("stats.GENERATE_CHARTS_CSVS");
        this.csvSeparator = ';';
    }

    // Methods for generation of HTML report Statistics
    abstract public String generateShortReport(String link);
    abstract public String generateLongReport(SectionReportGenerator protocolReportGenerator);
    abstract public String generateLongReportText(Boolean isToolTip);

    // Methods for generation of JPanel for Real Times Statistics
    abstract public JPanel generateShortRTStats();
    abstract public JPanel generateLongRTStats();
    abstract public String generateLongStringHTML();

    // Methods for creation of Parameter for "statCounter" <parameter> operation
    abstract public void addParameterStats(Runner runner, Parameter param, String resultant) throws Exception;

    protected void init()
    {
        long timestamp;
        double[] graphTable = this.counter.graphDataset.getGraphArray();

        double[] weightTable = null;
        if (this instanceof StatValue)
        {
            weightTable = ((StatValue) this).eventCounter.graphDataset.getGraphArray();
        }

        double sum = 0;
        double square_sum = 0;
        double hits = 0;

        for (int i = 0; i < graphTable.length; i++)
        {
            timestamp = i * this.counter.graphDataset.graphParameters.graphPeriod;

            long nextTimestamp = (i + 1) * this.counter.graphDataset.graphParameters.graphPeriod;


            double weight = 1;
            if (null != weightTable)
            {
                weight = weightTable[i];
            }

            if (nextTimestamp <= this.reportEndTimestamp &&
                    timestamp <= this.reportEndTimestamp)
            {
                hits += weight;
            }
            else
            {
                if (nextTimestamp >= this.reportEndTimestamp &&
                        timestamp <= this.reportEndTimestamp)
                {
                    if (null != weightTable)
                    {
                        hits += weight;
                    }
                    else
                    {
                        hits += ((double) (this.reportEndTimestamp % this.counter.graphDataset.graphParameters.graphPeriod)) / ((double) this.counter.graphDataset.graphParameters.graphPeriod);
                    }
                }
                else
                {
                    if (timestamp > this.reportEndTimestamp)
                    {
                        break;
                    }
                }
            }

            sum += weight * graphTable[i];

            square_sum += weight * graphTable[i] * graphTable[i];
        }
        if (0 != hits)
        {
            mean = sum / hits;
            double diff = square_sum / hits - mean * mean;
            if (diff >= 0)
            {
            	std_dv = Math.sqrt(diff);
            }
        }
    }

    protected void writeChartToFile(String path, JFreeChart chart)
    {
        try
        {
            ChartUtilities.saveChartAsPNG(new File(path), chart, 800, 400);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public JFreeChart getTimeChart()
    {
        double[] graphTable = this.counter.graphDataset.getGraphArray();

        double[] weightTable = null;
        if (this instanceof StatValue)
        {
            weightTable = ((StatValue) this).eventCounter.graphDataset.getGraphArray();
        }

        TimeSeries timeSeries = new TimeSeries("", FixedMillisecond.class);

        double hits = 0;

        long offset;
        boolean absoluteDate = Config.getConfigByName("tester.properties").getBoolean("stats.CHARTS_ABSOLUTE_DATE", false);
        if(absoluteDate){
            offset = reportZeroTimestamp;
        }
        else{
            offset = 0;
        }
        for (int i = 0; i < graphTable.length; i++)
        {
            long timestamp = offset + i * this.counter.graphDataset.graphParameters.graphPeriod;
            timeSeries.add(new FixedMillisecond(timestamp), graphTable[i], false);
        }

        JFreeChart chart = ChartFactory.createXYAreaChart(
                null, // chart title
                null, // domain axis label
                null, // range axis label
                new TimeSeriesCollection(timeSeries), // data
                PlotOrientation.VERTICAL, // orientation
                false, // include legend
                false, // tooltips
                false // urls
                );
        if (null != mean && null != std_dv)
        {
            chart.getXYPlot().addRangeMarker(new ValueMarker(mean));
            IntervalMarker intervalMarker = new IntervalMarker(mean - std_dv / 2, mean + std_dv / 2);
            intervalMarker.setAlpha(0.3f);
            chart.getXYPlot().addRangeMarker(intervalMarker);
        }

        chart.getXYPlot().setDomainAxis(new DateAxis());
        DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
        DateFormat dateFormat;
        
        if(absoluteDate){
            dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            axis.setVerticalTickLabels(true);
        }
        else{
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
        }
        axis.setDateFormatOverride(dateFormat);

        chart.setBackgroundPaint(Color.WHITE);
        return chart;
    }

    public void writeTimeCSV(String path) throws IOException
    {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(path)), this.csvSeparator);
        double[] graphTable = this.counter.graphDataset.getGraphArray();

        csvWriter.writeNext(new String[]
                {
                    "timestamp", "value"
                });

        for (int i = 0; i < graphTable.length; i++)
        {
            long timestamp = i * this.counter.graphDataset.graphParameters.graphPeriod;

            if (timestamp > this.reportEndTimestamp)
            {
                break;
            }

            csvWriter.writeNext(new String[]
                    {
                        Long.toString(timestamp), Double.toString(graphTable[i])
                    });
        }

        csvWriter.close();
    }

    public JFreeChart getHistogramChart()
    {

        double[] hits = this.counter.histogramDataset.getHistogramArray();
        double[] intervals = this.counter.histogramDataset.histogramParameters.histogramIntervals;
        double hitsCount = this.counter.histogramDataset.hits;
        DefaultCategoryDataset datasetNormal = new DefaultCategoryDataset();

        datasetNormal.addValue(0, "a", "          -INF");
        datasetNormal.addValue(0, "b", "          -INF");

        double cumul = 0;
        for (int i = 0; i < intervals.length - 1; i++)
        {
            String intervalName;

            if (i == intervals.length - 2)
            {
                intervalName = "          +INF";
            }
            else
            {
                intervalName = "          " + Double.toString(intervals[i + 1]);
            }

            cumul += hits[i];
            datasetNormal.addValue((100 * hits[i]) / hitsCount, "a", intervalName);
            datasetNormal.addValue((100 * cumul) / hitsCount, "b", intervalName);
        }


        JFreeChart jFreeChart = ChartFactory.createBarChart("", "", "%", datasetNormal, PlotOrientation.VERTICAL, false, false, false);

        CategoryPlot plot = jFreeChart.getCategoryPlot();
        CategoryAxis axis = plot.getDomainAxis();
        LayeredBarRenderer layeredBarRenderer = new LayeredBarRenderer();
        layeredBarRenderer.setSeriesPaint(0, new Color(0, 0, 255, 85));
        layeredBarRenderer.setSeriesPaint(1, new Color(255, 0, 0, 85));
        plot.setRenderer(layeredBarRenderer);

        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        axis.setMaximumCategoryLabelWidthRatio(1);

        jFreeChart.setBackgroundPaint(Color.WHITE);
        return jFreeChart;

    }

    public void writeHistogramCSV(String path) throws IOException
    {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(path)), this.csvSeparator);

        csvWriter.writeNext(new String[]
                {
                    "interval_min", "interval_max", "interval_hits", "cumulated_hits"
                });

        double[] hits = this.counter.histogramDataset.getHistogramArray();
        double[] intervals = this.counter.histogramDataset.histogramParameters.histogramIntervals;

        double cumul = 0;
        for (int i = 0; i < intervals.length - 1; i++)
        {
            cumul += hits[i];

            csvWriter.writeNext(new String[]
                    {
                        Double.toString(intervals[i]), Double.toString(intervals[i + 1]), Double.toString(hits[i]), Double.toString(cumul)
                    });
        }

        csvWriter.close();
    }

    public boolean isCreateCharts()
    {
        return createCharts;
    }

    public boolean isCreateCSV()
    {
        return createCSVs;
    }

    // Method return a Panel with the chart of this counter
    public ChartPanel getChartPanel()
    {
        ChartPanel chartPanel = new ChartPanel(null);
        chartPanel.setLayout(new javax.swing.BoxLayout(chartPanel, javax.swing.BoxLayout.Y_AXIS));
        chartPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        chartPanel.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);

        if(timeChart == null)
        {
            timeChart = getTimeChart();
        }
        chartPanel.add(new ChartPanel(timeChart));

        if(this instanceof StatValue)
        {
            if(histogramChart == null)
            {
                histogramChart = getHistogramChart();                
            }
            chartPanel.add(new ChartPanel(histogramChart));
        }
        
        return chartPanel;
    }

    // Method for add mouselistener for have graph on click on Real-Time Stats
    public void addMouseListenerForGraph(JPanel panel)
    {
        if (/*this instanceof StatAverage || this instanceof StatFlow*/ true)
        {
            panel.addMouseListener(new MouseListener() {

                java.awt.Color lastColor;

                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                    JFrameRTStats.instance().getJPanelBottom().removeAll();

                    JPanel longpanel = generateLongRTStats();
                    longpanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
                    longpanel.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);
                    JFrameRTStats.instance().getJPanelBottom().add(longpanel);
                    
                    // We add graphics to jPanelBottom
                    JFrameRTStats.instance().getJPanelBottom().add(getChartPanel());
                    // We save the StatKey selected
                    ModelTreeRTStats.instance().setStatSelected(id);
                    // We update the panel for display the graph
                    JFrameRTStats.instance().updatePanel();
                }

                public void mouseReleased(MouseEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                public void mouseEntered(MouseEvent e) {
                    lastColor = e.getComponent().getBackground();
                    e.getComponent().setBackground(ModelTreeRTStats.instance().getColorByString("selectForGraph"));
                }

                public void mouseExited(MouseEvent e) {
                    e.getComponent().setBackground(lastColor);
                }
            });
        }
    }

    // Method for create the short report and a tooltip
    public String createToolTip(String[] values, String link)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<table>");

        for(String current : values)
        {
            sb.append("<tr>");
                sb.append("<td class=\"value\">");
                    sb.append("<a href=\""+link+"\" class=\"info\">");
                        sb.append(current);
                        // Tooltip information between <span>
                        sb.append("<span>");
                        sb.append(generateLongReportText(true));
                        sb.append("</span>");
                    sb.append("</a>");
                sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    public String generateRTStatsToolTip()
    {
        StringBuilder sb = new StringBuilder();

        // For the tooltip, we add help message "Click to view graphs"

        if(!(this instanceof StatText))
        {
            sb.append("<html>");
            sb.append("<table width=\"350px\" bgcolor=\""+bgColor+"\">");
            sb.append("<tr bgcolor=\""+bgColor+"\"><td colspan=2>"+"Click to view graphs"+"</td></tr>");
            sb.append("</table>");
        }

        // We add long information about this counter
        sb.append(generateLongStringHTML());

        sb.append("</html>");

        return sb.toString();
    }

}
