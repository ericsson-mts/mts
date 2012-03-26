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

import java.util.LinkedList;
import java.util.List;

public class TextListenerProviderRegistry
{
    private static TextListenerProviderRegistry instance = new TextListenerProviderRegistry();

    public static TextListenerProviderRegistry instance()
    {
        return instance;
    }


    private List<TextListenerProvider> registry = new LinkedList();

    public void register(TextListenerProvider textListenerProvider)
    {
        if(!registry.contains(textListenerProvider))
        {
            registry.add(textListenerProvider);
        }
    }

    public void unregister(TextListenerProvider textListenerProvider)
    {
        if(registry.contains(textListenerProvider))
        {
            registry.remove(textListenerProvider);
        }
    }

    public List<TextListener> provide(TextListenerKey key)
    {
        List<TextListener> result = new LinkedList();
        for(TextListenerProvider textListenerProvider:registry)
        {
            try
            {
                TextListener textListener = textListenerProvider.provide(key);
                if(null != textListener)
                {
                    result.add(textListener);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void dispose(TextListenerKey key)
    {
        for(TextListenerProvider textListenerProvider:this.registry)
        {
            textListenerProvider.dispose(key);
        }
    }

    public int getTextListenerProviderCount()
    {
        return this.registry.size();
    }
}
