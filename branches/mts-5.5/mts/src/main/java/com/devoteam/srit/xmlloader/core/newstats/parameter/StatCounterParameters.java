package com.devoteam.srit.xmlloader.core.newstats.parameter;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import java.io.Serializable;

public class StatCounterParameters implements Serializable, Cloneable
{

    //pattern that matches this counter
    public final StatKey pattern;
    
    //counter parameters
    public GraphParameters graphParameters;
    public HistogramParameters histogramParameters;

    //report parameters
    public String descriptionLong;
    public String descriptionShort;

    public StatCounterParameters(StatKey pattern, GraphParameters graphParameters, HistogramParameters histogramParameters, String descriptionShort, String descriptionLong)
    {
        this.pattern = pattern;

        this.graphParameters = graphParameters;
        this.histogramParameters = histogramParameters;

        this.descriptionLong = descriptionLong;
        this.descriptionShort = descriptionShort;
    }

    @Override
    public StatCounterParameters clone()
    {
        GraphParameters otherGraphParameters = null;
        HistogramParameters otherHistogramParameters = null;
        
        if(null != this.graphParameters)
        {
            otherGraphParameters = this.graphParameters.clone();
        }

        if(null != this.histogramParameters)
        {
            otherHistogramParameters = this.histogramParameters.clone();
        }
        
        return new StatCounterParameters(this.pattern, otherGraphParameters, otherHistogramParameters, this.descriptionShort, this.descriptionLong);
    }
}
