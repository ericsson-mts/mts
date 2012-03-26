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

package com.devoteam.srit.xmlloader.core.report;

/**
 *
 * @author gege
 */
public class ReportStatus
{
    private String description;
    private int progress;
    private boolean changed;
    
    public ReportStatus()
    {
        this.changed = true;
        this.description = "";
        this.progress = 0;
    }
    
    public boolean changed()
    {
        if(true == this.changed)
        {
            this.changed = false;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void setProgression(int progress)
    {
        this.progress = progress;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public int getProgress()
    {
        return this.progress;
    }
    
    @Override
    public String toString()
    {
        return progress + "% : " + description;
    }
}
