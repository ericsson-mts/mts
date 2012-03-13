package com.devoteam.srit.xmlloader.core.newstats;

import com.devoteam.srit.xmlloader.core.newstats.dataset.CurrentSampleDataset;
import com.devoteam.srit.xmlloader.core.newstats.dataset.GlobalDataset;
import com.devoteam.srit.xmlloader.core.newstats.dataset.GraphDataset;
import com.devoteam.srit.xmlloader.core.newstats.dataset.HistogramDataset;
import com.devoteam.srit.xmlloader.core.newstats.parameter.StatCounterParameters;

public class StatCounter implements IStatCounter, Cloneable
{
    // id
    private StatKey id;
    
    //Datasets
    public GraphDataset graphDataset;
    public HistogramDataset histogramDataset;
    public CurrentSampleDataset currentSampleDataset;
    public GlobalDataset globalDataset;

    public StatCounter(StatKey id, StatCounterParameters statCounterParameters)
    {
        this.id = id;

        this.graphDataset = null;
        this.histogramDataset = null;
        this.currentSampleDataset = new CurrentSampleDataset();
        this.globalDataset = new GlobalDataset();
        
        if (statCounterParameters.graphParameters != null)
        {
            graphDataset = new GraphDataset(statCounterParameters.graphParameters);
        }

        if (statCounterParameters.histogramParameters != null)
        {
            histogramDataset = new HistogramDataset(statCounterParameters.histogramParameters);
        }
    }

    private StatCounter(StatCounter other)
    {
        synchronized(other)
        {
            this.id = other.id;

            if(null != other.graphDataset)  this.graphDataset = other.graphDataset.clone();
            else                            this.graphDataset = null;
            
            if(null != other.histogramDataset)  this.histogramDataset = other.histogramDataset.clone();
            else                                this.histogramDataset = null;
            
            this.currentSampleDataset = other.currentSampleDataset.clone();
            this.globalDataset = other.globalDataset.clone();
        }
    }

    @Override
    public StatCounter clone()
    {
        return new StatCounter(this);
    }
    
    /**
     * Adds a new value to the counter
     */
    public synchronized void addValue(Object value)
    {
        //long currentTimestamp = StatPool.getInstance().relativeTimeMillis();

        addValue(value, System.currentTimeMillis());
    }

    public void addValue(Object value, long currentTimestamp)
    {
        currentTimestamp -= StatPool.getInstance().getZeroTimestamp();
        if(null != globalDataset)
        {
            globalDataset.addValue(value, currentTimestamp);
        }

        if(value instanceof Number)
        {
            double doubleValue = ((Number) value).doubleValue();
            if(null != graphDataset)
            {
                graphDataset.addValue(doubleValue, currentTimestamp);
            }
            if(null != histogramDataset)
            {
                histogramDataset.addValue(doubleValue);
            }
            if(null != currentSampleDataset)
            {
                currentSampleDataset.addValue(doubleValue);
            }
        }
    }


    public synchronized void sum(IStatCounter operand)
    {
        StatCounter otherCounter = (StatCounter) operand;
        if (currentSampleDataset != null && otherCounter.currentSampleDataset != null)
        {
            currentSampleDataset.sum(otherCounter.currentSampleDataset);
        }
        if (graphDataset != null && otherCounter.graphDataset != null)
        {
            graphDataset.sum(otherCounter.graphDataset);
        }
        if (histogramDataset != null && otherCounter.histogramDataset != null)
        {
            histogramDataset.sum(otherCounter.histogramDataset);
        }
        if (globalDataset != null && otherCounter.globalDataset != null)
        {
            globalDataset.sum(otherCounter.globalDataset);
        }
    }

    // IStatCounter implementation finished
    public StatKey getId()
    {
        return id;
    }

    public void setId(StatKey id)
    {
        this.id = id;
    }

    public void divide(int factor)
    {

        if (currentSampleDataset != null)
        {
            currentSampleDataset.divide(factor);
        }
        if (graphDataset != null)
        {
            graphDataset.divide(factor);
        }
        if (histogramDataset != null)
        {
            histogramDataset.divide(factor);
        }
        if (globalDataset != null)
        {
            globalDataset.divide(factor);
        }
    }
}
