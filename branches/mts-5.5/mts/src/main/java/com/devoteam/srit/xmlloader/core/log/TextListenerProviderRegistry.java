/*
 * Created on Oct 20, 2004
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
