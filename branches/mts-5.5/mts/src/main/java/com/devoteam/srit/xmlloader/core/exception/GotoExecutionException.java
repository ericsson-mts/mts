package com.devoteam.srit.xmlloader.core.exception;

public class GotoExecutionException extends Exception
{
    private String label;

    public GotoExecutionException(String label)
    {
        this.label = label;
    }
    
    public String getLabel()
    {
        return this.label;
    }
}