/* 
 * Copyright (c) 2010-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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

package de.uni_due.inf.ti.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class PropertyMap implements Map<String, Object>
{
    private Object owner;
    private List<PropertyChangeListener> listeners;
    private Map<String, Object> properties;

    /**
     * Creates a new PropertyMap.
     * 
     * @param owner
     *        object of which this property map manages the properties
     */
    public PropertyMap(Object owner)
    {
        this.owner = owner;
        properties = new HashMap<String, Object>();
    }

    /**
     * Creates a new PropertyMap without an owner.
     */
    public PropertyMap()
    {
        this(null);
    }

    /**
     * Registers a new PropertyChangeListener. The property change listener's
     * propertyChange method will be called each time a property of this
     * HexamObject changes.
     * 
     * @param list
     *        the new PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener list)
    {
        if (listeners == null)
            listeners = new ArrayList<PropertyChangeListener>();
        listeners.add(list);
    }

    /**
     * Remove a PropertyChangeListener from the registered listeners
     * 
     * @param the
     *        PropertyChangeListener which is to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener list)
    {
        listeners.remove(list);
    }

    /**
     * Fires a PropertyChangeEvent to all registered PropertyChangeListeners.
     * 
     * @param name
     *        name of the changed property
     * @param oldValue
     *        old value of the property
     * @param newValue
     *        new value of the property
     */
    public void firePropertyChange(String name, Object oldValue, Object newValue)
    {
        if (listeners == null)
            return;
        Object source = (owner == null ? this : owner);
        PropertyChangeEvent e = new PropertyChangeEvent(source, name, oldValue,
                newValue);
        ListIterator<PropertyChangeListener> it = listeners
                .listIterator(listeners.size());
        while (it.hasPrevious()) {
            PropertyChangeListener l = it.previous();
            l.propertyChange(e);
        }
    }

    /**
     * Changes the property {@code key} to {@code value}. If
     * {@code value == null}, the {@code key} is removed from the property list.
     * 
     * If the new value is not equal to the old value, an PropertyChange event
     * is sent to all registered PropertyChangeListeners.
     * 
     * @param key
     *        the name of the property which is changed
     * @param value
     *        the new value of the property
     * @return the old value of the property
     */
    @Override
    public Object put(String key, Object value)
    {
        if (value == null) {
            Object oldValue = properties.get(key);
            properties.remove(key);
            if (oldValue != null)
                firePropertyChange(key, oldValue, value);
            return oldValue;
        } else {
            Object oldValue = properties.put(key, value);
            if (oldValue != null && !oldValue.equals(value))
                firePropertyChange(key, oldValue, value);
            return oldValue;
        }
    }

    /**
     * Changes the boolean property {@code key} to {@code value}.
     */
    public void putBoolean(String key, boolean b)
    {
        put(key, Boolean.valueOf(b));
    }

    /**
     * Returns the value associated with the property {@code key}, or
     * {@code null} if no value is associated to the property.
     * 
     * @param key
     *        the name of the property
     * @return the value of the specified property, or {@code null} if no value
     *         is associated with the property
     * @throws ClassCastException
     *         , if {@code key} is not an instance of {@code String}
     */
    @Override
    public Object get(Object key)
    {
        if (!(key instanceof String))
            throw new ClassCastException();
        return properties.get(key);
    }

    /**
     * Returns the value associated with the property {@code key}, or a default
     * value if no value is associated with the property.
     * 
     * @param key
     *        the name of the property
     * @param def
     *        the default value
     * @return the value of the property named {@code key}, or {@code null} if
     *         {@code key} has no value
     */
    public Object get(String key, Object def)
    {
        Object val = properties.get(key);
        if (val == null)
            return def;
        return val;
    }

    public boolean getBoolean(String key)
    {
        Object o = get(key);
        if (!(o instanceof Boolean))
            throw new ClassCastException();
        return (Boolean) o;
    }

    public boolean getBoolean(String key, boolean def)
    {
        Object o = get(key);
        if (o == null)
            return def;
        if (!(o instanceof Boolean))
            throw new ClassCastException();
        return (Boolean) o;
    }

    @Override
    public void clear()
    {
        Map<String, Object> oldProps = properties;
        properties = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : oldProps.entrySet()) {
            firePropertyChange(entry.getKey(), entry.getValue(), null);
        }
    }

    /**
     * Returns {@code true} if the specified property has a value.
     * 
     * @param key
     *        the name of the property
     * @return {@code true} if the property has value, {@code false} if not
     * @throws ClassCastException
     *         if {@code key} is not an instance of String
     */
    @Override
    public boolean containsKey(Object key)
    {
        return properties.containsKey(key);
    }

    /**
     * Returns {@code true} if one of the properties has the specified value as
     * value.
     */
    @Override
    public boolean containsValue(Object value)
    {
        return properties.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
        return properties.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }

    /**
     * Returns a set of al set properties.
     */
    @Override
    public Set<String> keySet()
    {
        return properties.keySet();
    }

    @Override
    public void putAll(Map<? extends String, ?> m)
    {
        for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the given property from this PropertyMap.
     * 
     * @param key
     *        the name of the property to remove
     * @return the old value of the property
     * @throws ClassCastException
     *         if {@code key} is not an instance of {@code String}
     */
    @Override
    public Object remove(Object key)
    {
        if (!(key instanceof String))
            throw new ClassCastException();
        Object oldValue = properties.remove(key);
        if (oldValue != null)
            firePropertyChange((String) key, oldValue, null);
        return oldValue;
    }

    /**
     * Returns the number of key-value pairs in this PropertyMap.
     */
    @Override
    public int size()
    {
        return properties.size();
    }

    /**
     * Returns a view of the PropertyMap's values as a {@code Collection}.
     */
    @Override
    public Collection<Object> values()
    {
        return properties.values();
    }

}
