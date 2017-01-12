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

package com.devoteam.srit.xmlloader.core.newstats.dataset;

import com.devoteam.srit.xmlloader.core.newstats.parameter.GraphParameters;
import java.io.Serializable;

public class GraphDataset implements Serializable, Cloneable
{
    private double[] graphArray;
    
    public GraphParameters graphParameters;

    public GraphDataset(GraphParameters graphParameters)
    {
        super();
        this.graphParameters = graphParameters;
        this.graphArray = new double[graphParameters.graphArrayLength];
    }

    private GraphDataset(GraphDataset other)
    {
        this.graphArray      = other.graphArray.clone();
        this.graphParameters = other.graphParameters.clone();
    }

    @Override
    public GraphDataset clone()
    {
        return new GraphDataset(this);
    }
    
    public void compressGraphArray(long currentTimestamp)
    {

        if (currentTimestamp >= graphParameters.graphPeriod * graphParameters.graphArrayLength)
        {
            //how many times do we have to compress the array?
            int compressionFactor = (int) Math.floor((double) (currentTimestamp / (double) (graphParameters.graphPeriod * graphParameters.graphArrayLength)) + 1);

            // get the closer (but greater) power of 2
            boolean isGreater = false;
            int power = 0;
            while((compressionFactor >> 1) != 0)
            {
                power++;
                if(false == isGreater && (compressionFactor & 0x01) == 1)
                {
                    power++;
                    isGreater = true;
                }
                compressionFactor = compressionFactor >> 1;
            }
            compressionFactor = (int) Math.floor(Math.pow(2, power));
            
            graphParameters.graphPeriod *= compressionFactor;
 
            
            // compress the array
            int readingIndex = 0;
            for (int writingIndex=0; writingIndex < graphArray.length; writingIndex++)
            {
                double sum = 0;
                for(int j=0; readingIndex<graphArray.length && j<compressionFactor; j++)
                {
                    sum += graphArray[readingIndex];
                    readingIndex++;
                }
                graphArray[writingIndex] = sum;
            }
        }
    }

    public void addValue(double value, long currentTimestamp)
    {
        // compress the report array if we're out of its bounds
        compressGraphArray(currentTimestamp);

        // get the current sample index to know where to put the value
        int currentSampleIndex = (int) Math.floor(currentTimestamp / (double) graphParameters.graphPeriod);

        // put the value into the reportArray
        graphArray[currentSampleIndex] += value;

    }

    public void sum(GraphDataset other)
    {
        long maxTimestamp = Math.max(this.graphArray.length * this.graphParameters.graphPeriod - 1, other.graphArray.length * other.graphParameters.graphPeriod - 1);

        this.compressGraphArray(maxTimestamp);
        other.compressGraphArray(maxTimestamp);

        if (this.graphArray.length != other.graphArray.length)
        {
            throw new RuntimeException("Two arrays have different lengths - this case is not implemented yet");
        }
        if (this.graphParameters.graphPeriod != other.graphParameters.graphPeriod)
        {
            throw new RuntimeException("Periods of two graph dataset aren't the same - this case is not implemented yet");
        }
        
        for (int i = 0; i < graphArray.length; i++)
        {
            this.graphArray[i] += other.graphArray[i];
        }
    }

    public void divide(double factor)
    {
        for (int i = 0; i < graphArray.length; i++)
        {
            graphArray[i] = graphArray[i] / factor;
        }
    }

    public void divide(GraphDataset other)
    {
        long maxTimestamp = Math.max(this.graphArray.length * this.graphParameters.graphPeriod - 1, other.graphArray.length * other.graphParameters.graphPeriod - 1);

        this.compressGraphArray(maxTimestamp);
        other.compressGraphArray(maxTimestamp);

        if (this.graphArray.length != other.graphArray.length)
        {
            throw new RuntimeException("Two arrays have different lengths - this case is not implemented yet");
        }
        if (this.graphParameters.graphPeriod != other.graphParameters.graphPeriod)
        {
            throw new RuntimeException("Periods of two graph datasets aren't the same - this case is not implemented yet");
        }

        for (int i = 0; i < graphArray.length; i++)
        {
            if(other.graphArray[i] != 0)
            {
                graphArray[i] = graphArray[i] / other.graphArray[i];
            }
        }
    }

    public void percentage(GraphDataset other)
    {
        long maxTimestamp = Math.max(this.graphArray.length * this.graphParameters.graphPeriod - 1, other.graphArray.length * other.graphParameters.graphPeriod - 1);

        this.compressGraphArray(maxTimestamp);
        other.compressGraphArray(maxTimestamp);

        if (this.graphArray.length != other.graphArray.length)
        {
            throw new RuntimeException("Two arrays have different lengths - this case is not implemented yet");
        }
        if (this.graphParameters.graphPeriod != other.graphParameters.graphPeriod)
        {
            throw new RuntimeException("Periods of two graph dataset aren't the same - this case is not implemented yet");
        }

        for (int i = 0; i < graphArray.length; i++)
        {
            try
            {
                graphArray[i] = (graphArray[i] / (other.graphArray[i])) * 100;
            }
            catch (ArithmeticException e)
            {

            }
        }
    }

    public double[] getGraphArray()
    {
        return graphArray;
    }
}
