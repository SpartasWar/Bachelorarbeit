/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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

package de.uni_due.inf.ti.graph;

import java.util.Collection;
import java.util.WeakHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents an object with a name.
 * 
 * @author H.J.Sander Bruggink
 */
public class NamedObject extends AttributeObject
{
    /** Storage for the last number of each name prefix. */
    private static WeakHashMap<String,Integer> names = new WeakHashMap<>();
    
    /** Name of this named object. */
    private @Nullable String name;

    /**
     * Creates a new NamedObject instance with initial null name.
     */
    public NamedObject()
    {
        this.name = null;
    }
    
    /**
     * Creates a new NamedObject instance.
     * 
     * @param name
     *      name of the new NamedObject, or {@code null} if it doesn't have a
     *      name
     */
    public NamedObject(@Nullable String name)
    {
        this.name = name;
        if (name != null) addName(name);
    }
    
    /**
     * Creates a new NamedObject which has the same name as a given
     * NamedObject.
     * 
     * @param orig
     *      object of which the new NamedObject will be a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    public NamedObject(NamedObject orig) 
    {
        super(orig);
        this.name = orig.name;
    }

    /**
     * Sets the name of this NamedObject.
     * 
     * @param name
     *      new name of this NamedObject, or {@code null} to remove the current
     *      name of this NamedObject 
     */
    public void setName(@Nullable String name)
    {
        this.name = name;
        if (name != null) addName(name);
    }
    
    /**
     * Gives this NamedObject a new, unique name. The unique name is obtained
     * by calling {@link #createUniqueName} with the name of this
     * object's runtime class as prefix.
     * 
     * @return 
     *      the new name of this NamedObject instance
     */
    public String giveUniqueName()
    {
        String prefix = getClass().getName();
        name = createUniqueName(prefix);
        return name;
    }

    /**
     * Returns the name of this NamedObject.
     * 
     * @return 
     *      name of this NamedObject, or {@code null} if this NamedObject 
     *      does not have a name
     */
    public @Nullable String getName()
    {
        return name;
    }
    
    /**
     * Determines whether this NamedObject has a name.
     */
    public boolean hasName()
    {
        return name != null;
    }
    
    /**
     * Creates a unique name starting with a given prefix. The returned name
     * is unique for the current application instance.
     * <p>
     * This method is thread-safe.
     * 
     * @param prefix
     *      prefix of the unique name, or {@code null} if a default prefix is
     *      to be used
     * @return
     *      unique name starting with the given prefix
     */
    public static String createUniqueName(@Nullable String prefix)
    {
        if (prefix == null) prefix = "Object";
        int index = 0;
        synchronized (names) {
            Integer lastNr = names.get(prefix);
            if (lastNr != null) {
                if (lastNr == Integer.MAX_VALUE) return createUniqueName(prefix + 'A');
                index = lastNr + 1;
            }
            names.put(prefix, index);
        }
        return String.format("%s-%d", prefix, index);
    }
    
    /**
     * Adds a name to the name cache. This method will make sure that 
     * subsequent calls to {@link #createUniqueName(String)} will not return
     * the same name as long as the name is used. (If all references to the
     * name string are lost, the name is removed from the name cache and may
     * be re-used.)
     * <p>
     * This method is thread safe.
     * 
     * @param name
     *      name to add to the name cache
     * @throws NullPointerException
     *      if {@code name} is {@code null}
     */
    public static void addName(String name)
    {
        int pos = name.length();
        while (pos >= 0 && Character.isDigit(name.charAt(pos - 1))) {
            pos--;
        }
        if (pos > 0 && pos < name.length() && name.charAt(pos - 1) == '-') {
            String prefix = name.substring(0, pos - 1);
            int index = 0;
            try {
                index = Integer.parseInt(name.substring(pos));
            }
            catch (NumberFormatException e) {
                assert false; // shouldn't happen, because all chars are digits
            }
            synchronized (names) {
                Integer curIndex = names.get(prefix);
                if (curIndex == null || index > curIndex) {
                    names.put(prefix, index);
                }
            }
        }
    }
    
    /**
     * Makes sure that all elements in a collection of NamedObject instances
     * have a name. The new names are created by calling the
     * {@link NamedObject#giveUniqueName() giveUniqueName()} method on 
     * objects that have no name.
     * 
     * @param col
     *      collection of NamedObjects of which all are to be given a 
     *      name
     */
    public static <T extends NamedObject> void nameAll(Collection<T> col) 
    {
        for (T el : col) {
            String name = el.getName();
            if (name == null) {
                el.giveUniqueName();
            }
        }
    }
}
