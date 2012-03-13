package com.devoteam.srit.xmlloader.core.newstats.dataset;

import java.io.Serializable;

public class GlobalDataset implements Serializable, Cloneable
{
    private double min;
    private double max;
    private double cumulativeValue;
    private long cumulativeHitCount;
    private String text;

    public GlobalDataset()
    {
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
        this.cumulativeHitCount = 0;
        this.cumulativeValue = 0;
        this.text = "";
    }

    @Override
    public GlobalDataset clone()
    {
        GlobalDataset clone = new GlobalDataset();
        clone.cumulativeHitCount = this.cumulativeHitCount;
        clone.cumulativeValue = this.cumulativeValue;
        clone.max = this.max;
        clone.min = this.min;
        clone.text = this.text;
        return clone;
    }
    
    public void addValue(Object value, long currentTimestamp)
    {
        if(value instanceof Number)
        {
            double doubleValue = ((Number) value).doubleValue();
            cumulativeValue += doubleValue;
            cumulativeHitCount++;
            if (doubleValue > max)
            {
                max = doubleValue;
            }

            if (doubleValue < min)
            {
                min = doubleValue;
            }
        }
        else if(value instanceof String)
        {
            this.text = (String) value;
        }
    }

    public double getValue()
    {
        return cumulativeValue;
    }

    public String getText()
    {
        return this.text;
    }
    
    public double getAvg()
    {
        return cumulativeValue / cumulativeHitCount;
    }

    public double getMax()
    {
        return max;
    }

    public double getMin()
    {
        return min;
    }

    public void sum(GlobalDataset otherGlobalDataset)
    {
        cumulativeValue = cumulativeValue + otherGlobalDataset.cumulativeValue;
        cumulativeHitCount = cumulativeHitCount + otherGlobalDataset.cumulativeHitCount;
        this.min = Math.min(this.min, otherGlobalDataset.min);
        this.max = Math.max(this.max, otherGlobalDataset.max);
        this.text = otherGlobalDataset.text;
    }

    public void divide(int factor)
    {
        this.cumulativeValue = cumulativeValue / factor;

    }

    public void divide(GlobalDataset dataset)
    {
        try
        {
            this.cumulativeValue = this.cumulativeValue / dataset.cumulativeValue;
        }
        catch (ArithmeticException e)
        {

        }
    }

    public void percentage(GlobalDataset dataset)
    {

        //TODO What about the Max and Min ?
        try
        {
            this.cumulativeValue = 100 * this.cumulativeValue / (dataset.cumulativeValue);
        }
        catch (ArithmeticException e)
        {

        }
    }
}
