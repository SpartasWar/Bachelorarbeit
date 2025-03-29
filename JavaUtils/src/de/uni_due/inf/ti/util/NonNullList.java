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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ForwardingList;

/**
 * An {@link ArrayList} which cannot contain {@code null} elements.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <E>
 *      the type of the elements of this NonNullArrayList
 */
public class NonNullList<E> extends ForwardingList<E>
{
    private static final String EXC_NULL_ELEMENT = "This list does not accept null elements.";
    
    /**
     * Backing list in which the elements are stored.
     */
    List<E> delegate;
    
    public NonNullList(List<E> backingList) 
    {
        delegate = backingList;
    }
    
    
    /**
     * Returns the backing list of this NonNullList.
     */
    @Override
    protected List<E> delegate()
    {
        return delegate;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override 
    public boolean add(E ob)
    {
        if (ob == null)
            throw new NullPointerException(EXC_NULL_ELEMENT);
        
        return delegate.add(ob);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, E ob) 
    {
        if (ob == null)
            throw new NullPointerException(EXC_NULL_ELEMENT);

        delegate.add(index, ob);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> col)
    {
        if (col.contains(null))
            throw new NullPointerException(EXC_NULL_ELEMENT);
        
        return delegate.addAll(col);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> col) 
    {
        if (col.contains(null))
            throw new NullPointerException(EXC_NULL_ELEMENT);
        
        return delegate.addAll(index, col);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator()
    {
        return standardIterator();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator()
    {
        return standardListIterator();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(int start)
    {
        return standardListIterator(start);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return standardSubList(fromIndex, toIndex);
    }

}
