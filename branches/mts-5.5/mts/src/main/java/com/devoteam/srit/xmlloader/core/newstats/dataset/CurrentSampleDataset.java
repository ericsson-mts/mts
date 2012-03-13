package com.devoteam.srit.xmlloader.core.newstats.dataset;

import java.io.Serializable;

public class CurrentSampleDataset implements Serializable, Cloneable
{
    // Instant data params and data sets
    private double currentSampleValue;
    private int currentSampleHitCount;

    public CurrentSampleDataset()
    {
        this.currentSampleHitCount = 0;
        this.currentSampleValue = 0;
    }

    @Override
    public CurrentSampleDataset clone()
    {
        CurrentSampleDataset clone = new CurrentSampleDataset();
        clone.currentSampleValue = this.currentSampleValue;
        clone.currentSampleHitCount = this.currentSampleHitCount;
        return clone;
    }
    
    public void addValue(double value)
    {
        currentSampleValue += value;
        currentSampleHitCount++;
    }

    public double getValue()
    {
        double currentSampleValueCopy = currentSampleValue;
        currentSampleValue = 0;
        currentSampleHitCount = 0;
        return currentSampleValueCopy;
    }

    public double getAvgValue()
    {
        double currentSampleAverageValue = currentSampleValue / currentSampleHitCount;
        currentSampleValue = 0;
        currentSampleHitCount = 0;
        return currentSampleAverageValue;
    }

    public void sum(CurrentSampleDataset currentSampleDataset)
    {
        currentSampleValue = currentSampleValue + currentSampleDataset.currentSampleValue;
        currentSampleHitCount = currentSampleHitCount + currentSampleDataset.currentSampleHitCount;
    }

    public void divide(int factor)
    {
        currentSampleValue = currentSampleValue / factor;
        currentSampleHitCount = currentSampleHitCount / factor;
    }

    public void divide(CurrentSampleDataset dataset)
    {
        currentSampleValue = currentSampleValue / dataset.currentSampleValue;
    }

    public void percentage(CurrentSampleDataset dataset)
    {
        this.currentSampleValue = 100 * this.currentSampleValue / (dataset.currentSampleValue);
    }
}
