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

// 2014-12-01 - Created

package de.uni_due.inf.ti.graph.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.NamedObject;
import de.uni_due.inf.ti.util.Pair;

/**
 * A collection of objects all of which have a name. 
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      the type of the elements in this NamedCollection
 */
public class NamedCollection<T> implements Iterable<T>
{
    /** Map which associates the maximum index for each prefix. */
    private Map<String,Integer> prefixes;
    
    /** Storage of the objects. */
    private Map<String,T> objects;

    /**
     * Creates a new NamedCollection instance.
     */
    public NamedCollection()
    {
        objects = new HashMap<>();
        prefixes = new HashMap<>();
    }
    
    /**
     * Creates a new NamedCollection which is a copy of another one.
     */
    public NamedCollection(NamedCollection<? extends T> orig)
    {
        objects = new HashMap<>(orig.objects);
        prefixes = new HashMap<>(orig.prefixes);
    }
    
    /**
     * Returns an iterator which iterates over the objects in this 
     * NamedCollection.
     */
    public Iterator<T> iterator()
    {
        return objects.values().iterator();
    }
    
    /**
     * Returns a set of all the names in this NamedCollection.
     */
    public Set<String> nameSet()
    {
        return objects.keySet();
    }
    
    /**
     * Returns the object with the specified name.
     * 
     * @param name
     *      name of the object to get
     * @return
     *      object with the specified name, or {@code null} if this 
     *      NamedCollection does not contain an object with the given name
     * @throws NullPointerException
     *      if {@code name} is {@code null}
     */
    public @Nullable T get(String name)
    {
        return objects.get(name);
    }
    
    /** 
     * Adds an object to this NamedCollection under a generated unique name.
     * 
     * @param object
     *      object to be added to this NamedCollection
     * @return
     *      name under which the object was stored in this NamedCollection
     * @throws NullPointerException
     *      if {@code object} is {@code null}      
     */
    public String add(T object)
    {
        return add(null, object);
    }
    
    /**
     * Adds an object to this NamedCollection and suggests a name under which
     * the object is to be added. If this NamedCollection does not yet contain
     * an objected with the suggested name, the object will be stored under the
     * suggested name. Otherwise a new unique name will be generated and the
     * object will be stored under the generated name.
     * 
     * @param name
     *      name suggestion for the object; if {@code null}, a unique name
     *      will be generated
     * @param object
     *      object to be added to this NamedCollection
     * @return
     *      name under which the object was added to this NamedCollection
     * @throws NullPointerException
     *      if {@code object == null}
     */
    public String add(@Nullable String name, T object)
    {
        if (name == null) name = getNameSuggestion(object);
        name = getUniqueName(name);
        
        objects.put(name, object);
        return name;
    }

    /**
     * Returns a name which does not occur yet in this NamedCollection.
     * 
     * @param name
     *      name suggestion
     * @return
     *      a name which does not occur yet in this NamedCollection
     */
    private String getUniqueName(String name)
    {
        if (objects.keySet().contains(name)) {
            String prefix = name;
            Pair<String,Integer> prin = getPrefixAndIndex(name);
            if (prin != null) {
                prefix = prin.getFirst();
            }
            Integer max = prefixes.get(prefix);
            if (max == null) {
                max = 0;
                name = String.format("%s0", prefix);
            }
            else {
                name = String.format("%s%d", prefix, ++max);
            }
            prefixes.put(prefix, max);
        }
        else {   
            Pair<String,Integer> prin = getPrefixAndIndex(name);
            if (prin != null) {
                String prefix = prin.getFirst();
                int index = prin.getSecond();
                Integer oldMax = prefixes.get(prin.getFirst());
                if (oldMax != null) { 
                    if (index > oldMax) {
                        prefixes.put(prefix, index);
                    }
                }
                else{
                	prefixes.put(prefix, 0);
                }
                	
            }
        }
        
        return name;
    }
    
    /**
     * Returns a suggestion for the name of an object.
     * 
     * @param object
     *      object to suggest a name for
     * @return
     *      name suggestion for {@code object}
     * @throws NullPointerException
     *      if {@code object} is {@code null}
     */
    private static String getNameSuggestion(Object object)
    {
        if (object instanceof NamedObject) {
            return ((NamedObject)object).getName();
        }
        else {
            return object.getClass().getName() + "0";
        }
    }
        
    /** 
     * Divides a name into a prefix and a number. This method checks if 
     * the input string is a prefix followed by a number consisting only of
     * digits, and returns the prefix and index if it does. 
     * If it doesn't, this method returns {@code null}.
     * 
     * @param s
     *      string to subdivide
     * @returns
     *      prefix and index of the string, or {@code null} if {@code s} is
     *      not a prefix followed by an index
     * @throws NullPointerException
     *      if {@code s} is {@code null}
     */
    private static @Nullable Pair<String,Integer> getPrefixAndIndex(String s)
    {
        int pos = s.length();
        while (pos >= 0 && Character.isDigit(s.charAt(pos - 1))) {
            pos--;
        }
        if (pos >= 0 && pos < s.length()) {
            String prefix = s.substring(0, pos);
            int index = 0;
            try {
                index = Integer.parseInt(s.substring(pos));
            }
            catch (NumberFormatException e) {
                assert false; // cannot happen, because all chars are digits
            }
            return Pair.create(prefix, index);
        }
        return null;
    }
}
