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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;

public class ValidatingList<T> extends ForwardingList<T>
{
    private List<T> delegate;
    private Predicate<T> predicate;
    
    public ValidatingList(List<T> delegate, Predicate<T> predicate) 
    {
        for (T el : delegate) {
            if (!predicate.apply(el))
                throw new IllegalArgumentException("Element " + el.toString() + 
                        " does not satisfy predicate.");
        }
        
        this.delegate = delegate;
        this.predicate = predicate;
    }
    
    
    public static <T> ValidatingList<T> create(List<T> delegate, Predicate<T> predicate)
    {
        return new ValidatingList<T>(delegate, predicate);
    }


    @Override
    protected List<T> delegate()
    {
        return delegate;
    }
    
    
    @Override
    public boolean add(T el)
    {
        if (!predicate.apply(el))
            throw new IllegalArgumentException("Added element does not satisfy the predicate.");
        
        return delegate.add(el);
    }
    
    
    @Override
    public void add(int index, T el)
    {
        if (!predicate.apply(el))
            throw new IllegalArgumentException("Added element does not satisfy the predicate.");

        delegate.add(index, el);
    }
    
    
    @Override
    public boolean addAll(Collection<? extends T> col)
    {
        for (T el : col) {
            if (!predicate.apply(el))
                throw new IllegalArgumentException("Added element does not satisfy the predicate.");
        }
        
        return delegate.addAll(col);
    }
    
    
    @Override
    public boolean addAll(int index, Collection<? extends T> col) 
    {
        for (T el : col) {
            if (!predicate.apply(el))
                throw new IllegalArgumentException("Added element does not satisfy the predicate.");
        }
        
        return delegate.addAll(index, col);
    }
    
    
    @Override
    public Iterator<T> iterator()
    {
        return standardIterator();
    }
    
    
    @Override
    public ListIterator<T> listIterator()
    {
        return standardListIterator();
    }
    
    
    @Override
    public ListIterator<T> listIterator(int start)
    {
        return standardListIterator(start);
    }
    
    
    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        return standardSubList(fromIndex, toIndex);
    }
}
