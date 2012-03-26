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
