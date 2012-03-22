/*
 * Created on Oct 19, 2004
 */
package com.devoteam.srit.xmlloader.gui;

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import java.awt.Color;

/**
 * @author pn007888 A class that contains all icons
 */
public final class GuiHelper
{
    // Text colors
    public final static Color DEBUG       = new Color(150, 150, 150);
    public final static Color INFO        = new Color( 10,  10, 200);
    public final static Color WARN        = new Color(190, 100,   0);
    public final static Color ERROR       = new Color(200,   0,   0);
    public final static Color WHITE       = new Color(255, 255, 255);
    public final static Color GREY        = new Color(240, 240, 240);
    public final static Color LIGHT_GREY  = new Color(245, 245, 245);
    
    
    public final static Color getColorForTopic(TextEvent.Topic topic)
    {
        if(null == topic)
        {
            return WHITE;
        }
        
        switch(topic)
        {
            case PARAM      : return new Color(0xEAFFEA);
            case CORE       : return new Color(0xF0F0F0);
            case USER       : return new Color(0xFFEAC0);
            case PROTOCOL   : return new Color(0xFFEAEA);
            case CALLFLOW   : return new Color(0xFFD0D0);
            default         : return WHITE;
        }
    }
    
    /**
     * @param e the text to display
     * @return the color used to display a String according to its level.
     */
    public final static Color getColorForLevel(int level)
    {
        switch (level)
        {
            case TextEvent.DEBUG: return GuiHelper.DEBUG;
            case TextEvent.INFO:  return GuiHelper.INFO;
            case TextEvent.WARN:  return GuiHelper.WARN;
            default:              return GuiHelper.ERROR;
        }
        
    }
    
}