package com.devoteam.srit.xmlloader.core.newstats.parameter;

import java.io.Serializable;

public class HistogramParameters implements Serializable, Cloneable
{

    public double[] histogramIntervals;

    public HistogramParameters(double[] histogramIntervals)
    {
        this.histogramIntervals = histogramIntervals;
    }

    @Override
    public HistogramParameters clone()
    {
        return new HistogramParameters(this.histogramIntervals.clone());
    }
}
