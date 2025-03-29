/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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

package de.uni_due.inf.ti.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.annotation.NonNullByDefault;

/**
 * This class is an implementation of a find-union structure.
 *  
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      type of the elements of this partition
 */
@NonNullByDefault
public class Partition<T> 
{
    private static class PartitionElement<E>
    {
        E element;
        PartitionElement<E> parent;
        int rank;
    }
    
    
    private Map<T,PartitionElement<T>> elements;
    

    /**
     * Creates a new, empty partition.
     */
    public Partition()
    {
        elements = new HashMap<T,PartitionElement<T>>();
    }
    
    
    /**
     * Creates a new partition which contains the elements of a given
     * collection. Initially, all the elements form their own equivalence
     * class.
     * 
     * @param els
     *      initial elements of the partition
     */
    public Partition(Collection<T> els)
    {
        elements = new HashMap<T,PartitionElement<T>>();
        for (T el : els) {
            makeSet(el);
        }
    }
    
    
    /**
     * Find the representative of a the equivalence class to which an element
     * belongs.
     * 
     * @param el
     *      element data
     * @return
     *      element data of the representative
     */
    private PartitionElement<T> find(PartitionElement<T> el) 
    {
        if (el.parent == el) {
            return el;
        }
        else { // unwind once so that recursion is not necessary in the typical case
            if (el.parent.parent == el.parent) {
                return el.parent;
            }
            else {
                PartitionElement<T> pel = find(el.parent.parent);
                el.parent.parent = pel;
                el.parent = pel;
                return pel;
            }
        }
    }
    
    
    /**
     * Finds the representative of the equivalence class of an element.
     * If two elements {@code el1} and {@code el2} are in the same equivalence
     * class, it will hold that {@code find(el1) == find(el2)}. 
     * If the given element does not belong to the domain of this partition,
     * {@code null} is returned.
     * 
     * @param el
     *      the element of which to find the representative
     * @return 
     *      representative of the equivalence class to which {@code el} belongs,
     *      or {@code null} if {@code el} does not belong to the domain of this
     *      partition
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     */
    public @Nullable T find(T el) 
    {
        if (el == null) throw new NullPointerException();
        
        PartitionElement<T> pel = elements.get(el);
        if (pel == null) {
            return null;
        }
        else {
            return find(pel).element;
        }
    }
    

    /**
     * Unite two equivalence classes.
     * 
     * @param el1
     *      arbitrary element data of the first equivalence class
     * @param el2
     *      arbitrary element data of the second equivalence class
     * @return
     *      element data of the representative of the new equivalence class
     */
    private PartitionElement<T> union(PartitionElement<T> el1, PartitionElement<T> el2) 
    {
        PartitionElement<T> rep1 = find(el1);
        PartitionElement<T> rep2 = find(el2);
        if (rep1.rank < rep2.rank) {
            rep1.parent = rep2;
            return rep2;
        }
        else if (rep2.rank < rep1.rank) {
            rep2.parent = rep1;
            return rep1;
        }
        else { // ranks are equal
            rep1.parent = rep2;
            rep2.rank = rep1.rank + 1;
            return rep2;
        }
    }
    
    
    /**
     * Unite two equivalence classes. 
     * 
     * @param el1
     *      arbitrary element of the first equivalence class
     * @param el2
     *      arbitrary element of the second equivalence class
     * @return
     *      representative of the union of the two equivalence classes
     * @throws NullPointerException
     *      if {@code el1} or {@code el2} is {@code null}
     * @throws IllegalArgumentException
     *      if either {@code el1} or {@code el2} does not belong to the domain
     *      of this partition
     */
    public T union(T el1, T el2)
    {
        if (el1 == null || el2 == null) throw new NullPointerException();
        
        PartitionElement<T> pel1 = elements.get(el1);
        if (pel1 == null) throw new IllegalArgumentException();
        
        PartitionElement<T> pel2 = elements.get(el2);
        if (pel2 == null) throw new IllegalArgumentException();
        
        return union(pel1, pel2).element;
    }
    
    
    /**
     * Adds a new element to the partition as a singleton equivalence class.
     * 
     * @param el
     *      element to add to the partition
     * @return
     *      the representative of the new equivalence class, in this case
     *      {@code el} itself
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code el} is already in this partition
     */
    public T makeSet(T el)
    {
        if (el == null) throw new NullPointerException();
        if (elements.get(el) != null) throw new IllegalArgumentException("Element is already contained in the partition");
        PartitionElement<T> pel = new PartitionElement<T>();
        pel.element = el;
        pel.parent = pel;
        pel.rank = 0;
        elements.put(el, pel);
        return el;
    }
    
    
    /**
     * Returns a set containing the equivalence classes of this partition.
     * The returned set is newly created and can can be modified by the caller;
     * modifications does not reflect back to the partition.
     * 
     * @return
     *      set of equivalence classes of this partition
     */
    public Set<Set<T>> getEquivalenceClasses()
    {
        Map<T,Set<T>> classMap = new HashMap<T,Set<T>>();
        
        for (T el : elements.keySet()) {
            @Nullable T rep = find(el);
            assert rep != null;
            Set<T> cl = classMap.get(rep);
            if (cl == null) {
                cl = new HashSet<T>();
                classMap.put(rep, cl);
            }
            cl.add(el);
        }
        
        return new HashSet<Set<T>>(classMap.values());
    }
}
