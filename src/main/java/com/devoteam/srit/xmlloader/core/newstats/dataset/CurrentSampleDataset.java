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
