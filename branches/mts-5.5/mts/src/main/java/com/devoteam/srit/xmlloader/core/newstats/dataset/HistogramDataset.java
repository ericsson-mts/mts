package com.devoteam.srit.xmlloader.core.newstats.dataset;

import com.devoteam.srit.xmlloader.core.newstats.parameter.HistogramParameters;
import java.io.Serializable;

public class HistogramDataset implements Serializable, Cloneable
{

    public HistogramParameters histogramParameters;
    private double[] histogramArray;
    public double hits;
    
    public HistogramDataset(HistogramParameters histogramParameters)
    {
        this.histogramParameters = histogramParameters;
        this.histogramArray = new double[histogramParameters.histogramIntervals.length];
        this.hits = 0;
    }

    @Override
    public HistogramDataset clone()
    {
        HistogramDataset clone = new HistogramDataset(this.histogramParameters.clone());
        clone.histogramArray = this.histogramArray.clone();
        clone.hits = this.hits;
        return clone;
    }

    public void addValue(double value)
    {
        for (int i = 0; i < histogramParameters.histogramIntervals.length - 1; i++)
        {
            if(value >= histogramParameters.histogramIntervals[i] && value < histogramParameters.histogramIntervals[i+1])
            {
                this.hits++;
                this.histogramArray[i]++;
                break;
            }
        }
    }

    public double[] getHistogramArray()
    {
        return histogramArray;
    }
    
    /**
     * Sums two histograms, we suppose that they have the same intervals..
     * 
     * @param otherHistogramDataset
     */
    public void sum(HistogramDataset otherHistogramDataset)
    {
        if(otherHistogramDataset.histogramParameters.histogramIntervals.length != this.histogramParameters.histogramIntervals.length)
        {
            throw new RuntimeException("Can't sum two histograms with different intervals");
        }
        
        for (int i = 0; i < histogramArray.length; i++)
        {
            if(otherHistogramDataset.histogramParameters.histogramIntervals[i] != this.histogramParameters.histogramIntervals[i])
            {
                throw new RuntimeException("Can't sum two histograms with different intervals");
            }
            else
            {
                histogramArray[i] = histogramArray[i] + otherHistogramDataset.histogramArray[i];
            }
        }

        this.hits += otherHistogramDataset.hits;
    }

    public void divide(double factor)
    {
        for (int i = 0; i < histogramArray.length; i++)
        {
            histogramArray[i] /= factor;
        }
    }

}
