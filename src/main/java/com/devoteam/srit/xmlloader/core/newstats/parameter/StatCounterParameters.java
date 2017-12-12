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
