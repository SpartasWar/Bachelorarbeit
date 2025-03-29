/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, 
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

// 2014-09-26 - Created (as EnumeratorFactory)
// 2014-10-21 - Renamed to Enumerators

package de.uni_due.inf.ti.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

import de.uni_due.inf.ti.annotation.NonNull;

/**
 * Factory class for Enumerator objects of various kinds.
 * 
 * @author H.J. Sander Bruggink
 */
public class Enumerators
{
    private Enumerators() // prohibit instantiation
    { }
    
    private static @MonotonicNonNull Enumerator<?> EMPTY_ENUM = null;

    // ---------------------------------------------------------------------- //
    //              Nested classes that implement the enumerators             //
    // ---------------------------------------------------------------------- //
    
    /**
     * Enumerator which enumerates, in order, the elements of a list of other
     * Enumerators.
     */
    private static class ConcatEnumerator<T> extends Enumerator<T>
    {
        private List<Enumerator<? extends T>> enums;
        int currentIdx;
        
        ConcatEnumerator(List<Enumerator<? extends T>> enums)
        {
            this.enums = enums;
            this.currentIdx = 0;
        }
        
        @Override
        public @Nullable T next()
        {
            while (currentIdx < enums.size()) {
                @Nullable T el = enums.get(currentIdx).next();
                if (el != null) return el;
                currentIdx++;
            }
            return null;
        }
    }
    
    /**
     * Class which implements an Enumeration of all maps from a collection of 
     * keys to a collection of values.
     */
    private static class MapEnumerator<K,V> extends Enumerator<HashMap<K,V>>
    {
        private K[] keys;
        private V[] values;
        private int[] curMap;
        boolean overflow;
        
        @SuppressWarnings("unchecked")
        MapEnumerator(final Collection<K> ks, final Collection<V> vs) 
        {
            keys = (K[])ks.toArray();
            values = (V[])vs.toArray();
            curMap = new int[keys.length];
            overflow = false;
        }
        
        @Override
        public int characteristics()
        {
            return IMMUTABLE | NONNULL | DISTINCT;
        }
        
        @Override
        public @Nullable HashMap<K,V> next()
        {
            if (overflow) {
                return null;
            }
            else {
                assert curMap.length == keys.length;
                
                HashMap<K,V> map = new HashMap<>();
                for (int i = 0; i < curMap.length; i++) {
                    map.put(keys[i], values[curMap[i]]);
                }
                
                for (int i = 0; i < curMap.length; i++) {
                    curMap[i]++;
                    if (curMap[i] >= values.length) {
                        curMap[i] = 0;
                    }
                    else {
                        return map;
                    }
                }
                
                overflow = true;
                return map;
            }
        }
    }
    
    /** 
     * Enumerator which enumerates the elements of a collection.
     */
    private static class IteratorEnumerator<T> extends Enumerator<T>
    {
        private Iterator<T> iterator;
        
        IteratorEnumerator(Iterator<T> iterator)
        {
            if (iterator == null) throw new NullPointerException();
            this.iterator = iterator;
        }
        
        @Override
        public @Nullable T next()
        {
            if (iterator.hasNext()) {
                return iterator.next();
            }
            else {
                return null;
            }
        }
    }
    
    private static class SingletonEnumerator<T> extends Enumerator<T> 
    {
        private @Nullable T element;
        
        SingletonEnumerator(T element)
        {
            assert element != null;
            this.element = element;
        }
        
        @Override
        public int characteristics()
        {
            return IMMUTABLE | NONNULL | DISTINCT;
        }

        @Override
        public @Nullable T next()
        {
            @Nullable T result = element;
            element = null;
            return result;
        }
    }
    
    /**
     * Enumerator which enumerates the subsets of a collection that have a
     * given size.
     */
    private static class FixedSizeSubsetEnumerator<E> extends Enumerator<Set<E>>
    {
        private int[] current;
        private boolean done;
        List<E> elements;
        
        FixedSizeSubsetEnumerator(List<E> elements, int size)
        {
            assert size > 0;
            
            this.elements = elements;
            
            current = new int[size];
            for (int i = 0; i < size; i++) 
                current[i] = i;
            
            done = size > elements.size();
        }

        @Override
        public int characteristics()
        {
            return IMMUTABLE | NONNULL | DISTINCT;
        }

        @Override@Pure
        public @Nullable Set<E> next()
        {
            if (done) return null;
            
            Set<E> set = new HashSet<>();
            for (int i = 0; i < current.length; i++) {
                set.add(elements.get(current[i]));
            }
            
            done = nextFixedSizeSubset(current, elements.size());
            
            return set;
        }
    }

    /**
     * Enumerator which enumerates the subsets of a collection that have
     * a size within a given range.
     */
    private static class RangeSubsetEnumerator<E> extends Enumerator<Set<E>>
    {
        private int current;
        private int max;
        private Enumerator<Set<E>> fse;
        private List<E> elements;
        
        RangeSubsetEnumerator(List<E> elements, int min, int max)
        {
            this.elements = elements;
            this.max = Math.min(max, elements.size());
            
            current = min;
            fse = createSubsetEnumerator(elements, current);
        }
        
        @Override
        public int characteristics()
        {
            return IMMUTABLE | NONNULL | DISTINCT;
        }
        
        @Override@Pure
        public @Nullable Set<E> next()
        {
            while (current <= max) {
                Set<E> n = fse.next();
                if (n != null) return n;
                current++;
                fse = new FixedSizeSubsetEnumerator<>(elements, current);
            }
            return null;
        }
    }

    
    // ---------------------------------------------------------------------- //
    //                             Factory methods                            //  
    // ---------------------------------------------------------------------- //
    
    /**
     * Returns an Enumerator which enumerates all maps from a collection 
     * of keys to a collection of values.
     * 
     * @param keys
     *      the collection of keys
     * @param values
     *      the collection of values
     * @return
     *      an Enumerator which enumerates all maps from {@code keys} to
     *      {@code values}
     * @throws NullPointerException
     *      when either {@code keys} or {@code values} is {@code null}
     */
    @Pure
    public static <K,V> Enumerator<HashMap<K,V>> createMapEnumerator(
            final Collection<K> keys, 
            final Collection<V> values)
    {
        return new MapEnumerator<>(keys, values);    
    }
    
    /**
     * Returns an Enumerator which enumerates all elements of a collection.
     * The returned Enumerator's {@link Enumerator#next()} method throws
     * {@link ConcurrentModificationException}s when the collection changed
     * between calls.
     * 
     * @param collection
     *      collection of which the elements are enumerated
     * @return
     *      Enumerator which enumerates the elements of a collection
     * @throws NullPointerException
     *      when {@code collection} is {@code null} 
     */
    @Pure
    public static <T> Enumerator<T> createCollectionEnumerator(
            final Collection<T> collection)
    {
        return new IteratorEnumerator<T>(collection.iterator());
    }

    /**
     * Returns an Enumerator which enumerates all elements of a collection.
     * The returned Enumerator's {@link Enumerator#next()} method throws
     * {@link ConcurrentModificationException}s when the collection changed
     * between calls.
     *
     * @param collection
     *      collection of which the elements are enumerated
     * @return
     *      Enumerator which enumerates the elements of a collection
     * @throws NullPointerException
     *      when {@code collection} is {@code null}
     */
    @Pure
    public static <T> Enumerator<T> enumerate(
            final Collection<T> collection)
    {
        return new IteratorEnumerator<T>(collection.iterator());
    }

    /**
     * Returns an Enumerator which enumerates an iterator.
     *
     * @param iterator
     *      iterator to enumerate
     * @return
     *      Enumerator which enumerates the elements of a collection
     * @throws NullPointerException
     *      when {@code collection} is {@code null}
     */
    @Pure
    public static <T> Enumerator<T> enumerate(
            final Iterator<T> iterator)
    {
        return new IteratorEnumerator<T>(iterator);
    }

	/**
	 * Returns an enumerator-based stream for the given collection.
	 */
	public static <T> Stream<T> stream(final Collection<T> collection) {
		return enumerate(collection).stream();
	}

	/**
	 * Returns an enumerator-based stream for the given iterator.
	 */
	public static <T> Stream<T> stream(final Iterator<T> iterator) {
		return enumerate(iterator).stream();
	}

    public static <T> @NonNull Enumerator<T> of(T el) {
        return new SingletonEnumerator<T>(el);
    }

    /**
     * Returns an Enumerator which enumerates the elements of a collection
     * of Enumerators. The order of the Enumerators is given by the 
     * order in which the collection's iterator returns them.
     * 
     * @param enums
     *      Enumerators to be concatenated
     * @return
     *      Enumerator which concatenates the given Enumerators
     */
    @Pure
    public static <T> Enumerator<T> concatenate(Collection<Enumerator<? extends T>> enums)
    {
        List<Enumerator<? extends T>> enumsCopy = new ArrayList<>(enums);
        return new ConcatEnumerator<T>(enumsCopy);
    }

    /**
     * Returns an Enumerator which enumerates the elements of an array
     * of Enumerators. The order of the Enumerators is given by the array. 
     * 
     * @param enums
     *      Enumerators to be concatenated
     * @return
     *      Enumerator which concatenates the given Enumerators
     */
    @SafeVarargs@Pure
    public static <T> Enumerator<T> concatenate(Enumerator<? extends T>... enums)
    {
        return concatenate(Arrays.asList(enums));
    }

    /**
     * Returns the empty enumerator.
     */
    @SuppressWarnings("unchecked")
    @Pure
    public static <T> Enumerator<T> emptyEnumerator()
    {
        if (EMPTY_ENUM == null) {
            EMPTY_ENUM = new Enumerator<Object>() {
                @Override 
                public int characteristics()
                {
                    return IMMUTABLE | NONNULL | DISTINCT;
                }
                
                @Override 
                public @Nullable Object next() 
                {
                    return null;
                }
            };
        }
        return (Enumerator<T>)EMPTY_ENUM;
    } 
    
    /**
     * Returns an enumerator which enumerates the subsets of a collection that
     * have a given size.
     * 
     * @param items
     *      collection of which the enumerated sets are subsets
     * @param size
     *      size of the enumerated subsets
     * @return
     *      an enumerator which enumerates all {@code items}'s subsets of size 
     *      {@code size}
     * @throws NullPointerException
     *      if {@code items} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code size} is negative
     */
    public static <T> Enumerator<Set<T>> createSubsetEnumerator(
            Collection<T> items,
            int size)
    {
        if (size < 0 || size > items.size()) {
            return emptyEnumerator();
        }
        else if (size == 0) {
            return new SingletonEnumerator<>(Collections.emptySet());
        }
        else {
            return new FixedSizeSubsetEnumerator<T>(new ArrayList<>(items), size);
        }
    }

    /**
     * Returns an enumerator which enumerates the subsets of a collection that
     * have a size within a given range
     *       
     * @param items
     *      collection of which the enumerated sets are subsets
     * @param minSize
     *      minimum size of the enumerated subsets (inclusive)
     * @param maxSize
     *      maximum size of the enumerated subsets (inclusive)
     * @return
     *      an enumerator which enumerates all {@code items}'s subsets of a 
     *      size within the specified range
     * @throws NullPointerException
     *      if {@code items} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code minSize} or {@code maxSize} is negative, or if 
     *      {@code maxSize} is smaller than {@code minSize}
     */
    public static <T> Enumerator<Set<T>> createSubsetEnumerator(
            Collection<T> items, 
            int minSize,
            int maxSize)
    {
        if (items == null) throw new NullPointerException();
        if (minSize < 0 || maxSize < minSize) throw new IllegalArgumentException();
        
        if (minSize > items.size()) {
            return emptyEnumerator();
        }
        if (minSize == maxSize) {
            return createSubsetEnumerator(items, minSize);
        }
        else {
            return new RangeSubsetEnumerator<T>(new ArrayList<>(items), minSize, maxSize);
        }
    }

    /** 
     * Returns an Enumerator which enumerates all subsets of a collection.
     * 
     * @param items
     *      collection to enumerate the subsets of
     * @return
     *      an Enumerator which enumerates all subsets of {@code items}
     * @throws NullPointerException
     *      if {@code items} is {@code null}
     */
    public static <T> Enumerator<Set<T>> createSubsetEnumerator(
            Collection<T> items)
    {
        if (items == null) throw new NullPointerException();
        return new RangeSubsetEnumerator<T>(new ArrayList<>(items), 0, items.size());
    }

    
    // ---------------------------------------------------------------------- //
    //                        Private utility methods                         //
    // ---------------------------------------------------------------------- //
    
    /**
     * Changes a subset to the next subset. 
     * The {@code subset} parameter is an array which contains the indices of
     * the elements.
     * <p>
     * <strong>Precondition:<strong> 
     *   {@code subset} is strictly monotonously increasing.
     *   That is, for each <tt>i</tt> &lt; <tt>j</tt> it must hold that 
     *   <tt>subset[i] &lt; subset[j]</tt>. 
     * 
     * @param subset
     *      subset to modify
     * @param k
     *      number of elements
     * @returns 
     *      {@code true} if {@code subset} is already the last one
     */
    private static boolean nextFixedSizeSubset(int[] subset, int k)
    {
        if (subset.length == 0) return false;
        
        int idx = subset.length - 1;
        
        while (idx >= 0) {
            int last = k - (subset.length - idx); 
            if (subset[idx] < last) {
                subset[idx]++;
                for (int i = idx + 1; i < subset.length; i++) {
                    subset[i] = subset[idx] + (i - idx);
                }
                return false;
            }
            else {
                idx--;
            }
        }
        
        return true;
    }
}
