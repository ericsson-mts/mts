package com.devoteam.srit.xmlloader.core.utils.expireshashmap;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ExpireHashMap<K, V extends Removable> implements Map<K, V>
{
	private String name;
    private long lifetime;
    
    private LinkedHashMap<K, ExpirableValue<V>> map;
    
    /**
     * Constructs an empty <tt>HashMap</tt> with the default parameters 
     * and a given expirationTime one.
     *
     * @param  expirationTime the expiration time.
     */
    public ExpireHashMap(final String name, long lifetime)
    {
        super();
        this.name = name;
        this.lifetime = lifetime;
        this.map = new LinkedHashMap<K, ExpirableValue<V>>()
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, ExpirableValue<V>> eldest)
            {
            	boolean result = eldest.getValue().isExpired();
            	if (result)
            	{
       				Removable group = eldest.getValue().getValue();
       				try
       				{
           				group.onRemove();
       				}
       				catch (Exception e)
       				{
       		            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Stack : remove expires entries ", name, e);
       				}
            	}
                return result;
            }
        };
    }

    public synchronized V put(K key, V value)
    {
        ExpirableValue<V> returnValue = this.map.put(key, new ExpirableValue(value, System.currentTimeMillis() + this.lifetime));
        if (this.map.size() % 10000 == 9999)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Stack : List ", name, " : size = ", this.map.size());
        }
        
        if(null == returnValue)
        {
            return null;
        }
        else
        {
            return returnValue.getValue();
        }
    }
    
    public synchronized V get(Object key)
    {
        ExpirableValue<V> value = this.map.get(key);
        
        if(null == value)
        {
            return null;
        }
        else
        {
            return value.getValue();    
        }
    }
    
    public synchronized V remove(Object key)
    {
        ExpirableValue<V> value = this.map.remove(key);
        
        if(null == value)
        {
            return null;
        }
        else
        {
            return value.getValue();    
        }
    }
    
    public synchronized int size()
    {
        return this.map.size();
    }

    public synchronized boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    public synchronized boolean containsKey(Object key)
    {
        return this.map.containsKey(key);
    }

    public synchronized boolean containsValue(Object value)
    {
        return this.map.containsValue(new ExpirableValue((V) value,0));
    }

    public synchronized void clear()
    {
        this.map.clear();
    }

    public synchronized Set<K> keySet()
    {
        return this.map.keySet();
    }

    // unsupported operations that need some heavier coding
    public synchronized void putAll(Map<? extends K, ? extends V> t)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Collection<V> values()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized Set<Entry<K, V>> entrySet()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public synchronized void cleanEldestEntries(){
        // Remove eldest entry if instructed, else grow capacity if appropriate
    	this.map.cleaneldestEntries();
    }
}


