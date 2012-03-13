/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
