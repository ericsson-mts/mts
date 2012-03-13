/*
 * StringPadder.java
 *
 * Created on 27 septembre 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

/**
 *
 * @author gpasquiers
 */
public class StringPadder
{
 
    public static enum Side
    {
        RIGHT,
        LEFT
    }
    
    public static String pad(Side side, String string, String padder, int length)
    {
        if(string.length()>length)
        {
            switch(side)
            {
                case RIGHT:
                {
                    string = string.substring(0, length);
                    break;
                }
                case LEFT:
                {
                    string = string.substring(string.length() - length);
                    break;
                }
            }
        }
        else
        {
            while(string.length()<length)
            {
                switch(side)
                {
                    case RIGHT:
                    {
                        string = string.concat(padder);
                        break;
                    }
                    case LEFT:
                    {
                        string = padder.concat(string);
                        break;
                    }
                }
            }        
        }
        
        return string;
    }
}
