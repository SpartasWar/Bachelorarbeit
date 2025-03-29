/* 
 * Copyright (c) 2013-2015, H.J. Sander Bruggink
 * Copyright (c) 2013-2014, University of Duisburg-Essen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the names of its copyright holder(s) nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.uni_due.inf.ti.visigraph;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A map which associates strings with styles. The difference of this class
 * with HashMap is that the methods {@link #styleAdded(Style)},
 * {@link #styleRemoved(Style)} or {@link #nameModified(String)} are called
 * each time a style is associated with a string or a style name is removed
 * from the map. Subclasses that need to perform custom operations on
 * changes, need only override these methods.
 *
 * @author H.J. Sander Bruggink
 */
class HashStyleMap implements StyleMap
{
    private Map<String,Style> storage;
    private Set<Map.Entry<String,Style>> entrySet;
    private Collection<Style> valueCollection;
    private Set<String> keySet;

    HashStyleMap(Map<String,Style> storage)
    {
        this.storage = storage;
        
        entrySet = null;
        valueCollection = null;
        keySet = null;
    }
    
    /**
     * Called when a style is added to this StyleMap.
     */
    protected void styleAdded(Style style)
    {
        // do nothing
    }
    
    /**
     * Called when a style is removed from this StyleMap.
     */
    protected void styleRemoved(Style style)
    {
        // do nothing
    }

    /**
     * Called when the association of a style name is changed.
     */
    protected void nameModified(String name)
    {
        // do nothing
    }
    
    @Override
    public Style get(Object key)
    {
        return storage.get(key);
    }

    @Override
    public Style put(String key, Style style)
    {
        if (style == null) {
            return remove(key);
        }
        else {
            Style old = storage.put(key, style);
            nameModified(key);
            if (old != null) styleRemoved(old);
            styleAdded(style);
            return old;
        }
    }

    @Override
    public Style remove(Object key)
    {
        Style old = storage.remove(key);
        if (key instanceof String) nameModified((String)key);
        if (old != null) styleRemoved(old);
        return old;
    }

    @Override
    public void clear()
    {
        for (Entry<String,Style> entry : storage.entrySet()) {
            nameModified(entry.getKey());
            styleRemoved(entry.getValue());
        }
        storage.clear();
    }
    
    @Override
    public boolean containsKey(Object key)
    {
        return storage.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return storage.containsValue(value);
    }

    @Override
    public Set<Map.Entry<String,Style>> entrySet()
    {
        if (entrySet == null) entrySet = new EntrySet();
        return entrySet;
    }

    @Override
    public boolean isEmpty()
    {
        return storage.isEmpty();
    }

    @Override
    public Set<String> keySet()
    {
        if (keySet == null) keySet = new KeySet();
        return keySet;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Style> map)
    {
        for (Map.Entry<? extends String,? extends Style> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size()
    {
        return storage.size();
    }

    @Override
    public Collection<Style> values()
    {
        if (valueCollection == null) valueCollection = new ValueCollection();
        return valueCollection;
    }
    
    private class ValueCollection extends AbstractCollection<Style>
    {
        @Override
        public Iterator<Style> iterator()
        {
            final Iterator<Style> it = storage.values().iterator();

            return new Iterator<Style>() {
                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public Style next()
                {
                    return it.next();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }                
            };
        }

        @Override
        public int size()
        {
            return storage.size();
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<String,Style>>
    {
        @Override
        public Iterator<Map.Entry<String,Style>> iterator()
        {
            final Iterator<Map.Entry<String,Style>> it = storage.entrySet().iterator();
            
            return new Iterator<Map.Entry<String,Style>>() {
                private String lastKey = null;
                private Style lastValue = null;
                
                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public Map.Entry<String,Style> next()
                {
                    Entry<String,Style> entry = it.next();
                    lastKey = entry.getKey();
                    lastValue = entry.getValue();
                    return entry;
                }

                @Override
                public void remove()
                {
                    it.remove();
                    if (lastKey != null) {
                        nameModified(lastKey);
                        styleRemoved(lastValue);
                        lastKey = null;
                        lastValue = null;
                    }
                }
            };
        }

        @Override
        public int size()
        {
            return storage.size();
        }    
    }
    
    private class KeySet extends AbstractSet<String>
    {
        @Override
        public boolean add(String e)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends String> c)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            HashStyleMap.this.clear();
        }

        @Override
        public boolean contains(Object o)
        {
            Style style = get(o);
            return (style != null);
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            for (Object o : c) {
                if (!contains(o)) return false;
            }
            return true;
        }

        @Override
        public boolean isEmpty()
        {
            return HashStyleMap.this.size() == 0;
        }

        @Override
        public Iterator<String> iterator()
        {
            final Iterator<String> it = storage.keySet().iterator();
            
            return new Iterator<String>() {
                private String lastKey = null;
                
                @Override
                public boolean hasNext()
                {
                    return it.hasNext();
                }

                @Override
                public String next()
                {
                    lastKey = it.next();
                    return lastKey;
                }

                @Override
                public void remove()
                {
                    it.remove();
                    if (lastKey != null) {
                        nameModified(lastKey);
                        lastKey = null;
                    }
                }
            };
        }

        @Override
        public boolean remove(Object o)
        {
            Style old = HashStyleMap.this.remove(o);
            return old != null;
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            boolean ret = false;
            for (Object o : c) {
                ret |= remove(o);
            }
            return ret;
        }

        @Override
        public int size()
        {
            return HashStyleMap.this.size();
        }
    }
}
