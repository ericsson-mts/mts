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

package com.devoteam.srit.xmlloader.core.log;

import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.List;

public class GenericLogger
{
    
    public void debug(TextEvent.Topic topic, Object... objects)
    {
        print(null, topic, TextEvent.DEBUG, null, objects);
    }

    public void info(TextEvent.Topic topic, Object... objects)
    {
        print(null, topic, TextEvent.INFO, null, objects);
    }

    public void warn(TextEvent.Topic topic, Object... objects)
    {
        print(null, topic, TextEvent.WARN, null, objects);
    }

    public void warn(TextEvent.Topic topic, Throwable e, Object... objects)
    {
        print(null, topic, TextEvent.WARN, e, objects);
    }

    public void error(TextEvent.Topic topic, Object... objects)
    {
        print(null, null, TextEvent.ERROR, null, objects);
    }

    public void error(TextEvent.Topic topic, Throwable e, Object... objects)
    {
        print(null, null, TextEvent.ERROR, e, objects);
    }
    
    public void debug(TextListenerKey key, TextEvent.Topic topic, Object... objects)
    {
        print(key, topic, TextEvent.DEBUG, null, objects);
    }

    public void info(TextListenerKey key, TextEvent.Topic topic, Object... objects)
    {
        print(key, topic, TextEvent.INFO, null, objects);
    }
    
    public void warn(TextListenerKey key, TextEvent.Topic topic, Object... objects)
    {
        print(key, topic, TextEvent.WARN, null, objects);
    }
        
    public void warn(TextListenerKey key, TextEvent.Topic topic, Throwable e, Object... objects)
    {
        print(key, topic, TextEvent.WARN, e, objects);
    }
    
    public void error(TextListenerKey key, TextEvent.Topic topic, Throwable e, Object... objects)
    {
        print(key, null, TextEvent.ERROR, e, objects);
    }
    
    public void error(TextListenerKey key, TextEvent.Topic topic, Object... objects)
    {
        print(key, null, TextEvent.ERROR, null, objects);
    }
    
    private synchronized void print(TextListenerKey key, TextEvent.Topic topic, int l, Throwable e, Object... objects)
    {
        //
        // Get logging level
        //
        int level = GlobalLogger.instance().getLogLevel();
        if (l >= level)
        {
            List<TextListener> list = TextListenerProviderRegistry.instance().provide(key);
            
            if(null != list && list.size() > 0)
            {
                StringBuilder message = new StringBuilder();

                for(Object object:objects)
                {
                    if(null != object)
                    {
                        message.append(object.toString());
                    }
                }

                if(null != e)
                {
                    message.append("\n");
                    message.append(Utils.printStackTrace(e));
                }

                String string = message.toString();

                for(TextListener textListener:list)
                {
                    textListener.printText(new TextEvent(string, l, topic));
                }
            }
        }
    }
}