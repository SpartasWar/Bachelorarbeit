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

package de.uni_due.inf.ti.util;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingMultiset;
import com.google.common.collect.Multiset;

public class ValidatingMultiset<E> extends ForwardingMultiset<E>
{
    private Multiset<E> delegate;
    private Predicate<E> predicate;
    
    
    private class EntryPredicate implements Predicate<Multiset.Entry<E>>
    {
        @Override
        public boolean apply(Multiset.Entry<E> el)
        {
            return predicate.apply(el.getElement());
        }
    }
    
    
    /**
     * Creates a new ValidatingMultiset.
     * 
     * @param delegate      the backing ValidatingMultiset
     * @param predicate     the validation predicate to be applied to all
     *                      elements being added
     */
    public ValidatingMultiset(Multiset<E> delegate, Predicate<E> predicate)
    {
        for (E el : delegate) {
            if (!predicate.apply(el))
                throw new IllegalArgumentException("Element " + el.toString() + 
                        " does not satisfy predicate.");
        }

        this.delegate = delegate;
        this.predicate = predicate;
    }
    
    
    /**
     * Creates a new ValidatingMultiset.
     * 
     * @param delegate      the backing ValidatingMultiset
     * @param predicate     the validation predicate to be applied to all
     *                      elements being added
     * @return              a new ValidatingMultiset backed by
     *                      {@code delegate}
     */
    public static <T> ValidatingMultiset<T> create(Multiset<T> delegate, Predicate<T> predicate)
    {
        return new ValidatingMultiset<T>(delegate, predicate);
    }

    
    @Override
    protected Multiset<E> delegate()
    {
        return delegate;
    }
    
    
    @Override
    public int add(E el, int count) 
    {
        if (!predicate.apply(el))
            throw new IllegalArgumentException("Added element does not satisfy " +
            		"the predicate.");
        
        return delegate.add(el, count);
    }
    
    
    @Override
    public boolean add(E el)
    {
        return standardAdd(el);
    }
    
    
    @Override
    public boolean addAll(Collection<? extends E> col)
    {
        for (E el : col) {
            if (!predicate.apply(el))
                throw new IllegalArgumentException("One of the added elements " +
                		"does not satisfy the predicate.");
        }
        
        return delegate.addAll(col);
    }
    
    
    @Override
    public int setCount(E el, int count) 
    {
        if (count != 0 && !predicate.apply(el))
            throw new IllegalArgumentException("Added element does not satisfy " +
            		"the predicate.");
        
        return delegate.setCount(el, count);
    }
    
    
    @Override
    public boolean setCount(E el, int oldCount, int newCount)
    {
        if (newCount != 0 && !predicate.apply(el))
            throw new IllegalArgumentException("Added element does not satisfy the predicate.");
        
        return delegate.setCount(el, oldCount, newCount);
    }
    
    
    @Override
    public Set<E> elementSet()
    {
        return ValidatingSet.create(delegate.elementSet(), predicate);
    }
    
    
    @Override
    public Set<Multiset.Entry<E>> entrySet()
    {
        return ValidatingSet.create(delegate.entrySet(), new EntryPredicate());
    }
}
