package com.devoteam.srit.xmlloader.core.newstats.parameter;

import java.io.Serializable;

public class GraphParameters implements Serializable, Cloneable
{

    public int graphArrayLength;
    public long graphPeriod;

    public GraphParameters(int graphArrayLength, long graphPeriod)
    {
        this.graphArrayLength = graphArrayLength;
        this.graphPeriod = graphPeriod;
    }

    @Override
    public GraphParameters clone()
    {
        return new GraphParameters(this.graphArrayLength, this.graphPeriod);
    }
}
