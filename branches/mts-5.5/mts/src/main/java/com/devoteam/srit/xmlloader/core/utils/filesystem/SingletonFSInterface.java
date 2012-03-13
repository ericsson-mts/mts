/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.filesystem;

/**
 *
 * @author gpasquiers
 */
public class SingletonFSInterface
{
    static private FSInterface instance = null;
    
    static public FSInterface instance()
    {
        if(null == instance)
        {
            throw new RuntimeException("FSInterface instance not set");
        }
        
        return instance;
    }
    
    static public void setInstance(FSInterface _instance)
    {
        instance = _instance;
    }
}
