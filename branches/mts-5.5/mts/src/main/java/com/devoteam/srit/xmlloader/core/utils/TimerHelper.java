/*
 * TimerHelper.java
 *
 * Created on 5 juin 2007, 14:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import java.util.HashMap;
import java.util.Timer;

/**
 *
 * @author gpasquiers
 */
public class TimerHelper
{
    private static HashMap<String, Timer> timerById = new HashMap<String, Timer>();
    
    public static Timer getTimer(String id)
    {
        Timer timer = timerById.get(id);
        
        if(null == timer)
        {
            timer = new Timer(true);
            timerById.put(id, timer);
        }
        
        return timer;
    }
}
