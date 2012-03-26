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
