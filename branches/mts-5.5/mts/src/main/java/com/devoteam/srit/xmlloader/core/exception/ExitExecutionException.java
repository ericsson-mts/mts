package com.devoteam.srit.xmlloader.core.exception;

public class ExitExecutionException extends Exception
{
    private boolean failed;

    public ExitExecutionException(boolean failed, String message)
    {
        super(message);
        this.failed = failed;
    }
    
    public boolean getFailed()
    {
        return this.failed;
    }
}