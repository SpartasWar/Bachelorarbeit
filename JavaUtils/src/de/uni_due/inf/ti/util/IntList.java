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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;

/**
 * List which stores its contents as an array of ints.
 * 
 * @author H.J. Sander Bruggink
 */
public class IntList extends AbstractList<Integer>
{
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAX_RESIZE_AMOUNT = 64;
    
    private int[] store;
    private int size;
    private boolean modifiable;
    
    
    /**
     * Creates a new, empty IntList.
     */
    public IntList() 
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }
    
    
    /**
     * Creates an IntList which is backed by the specified {@code int} array.
     */
    public IntList(int[] store) 
    {
        this.store = store;
        this.size = store.length;
    }
    
    
    /**
     * Creates an IntList with an user-specified initial capacity.
     */
    public IntList(int initialCapacity)
    {
        store = new int[initialCapacity];
        size = 0;
        modifiable = true;
    }
    

    /**
     * Creates an IntList which is a copy of a given IntList.
     */
    public IntList(IntList col) 
    {
        this(col.size());
        size = col.size();
        for (int i = 0; i < size; i++)
            store[i] = col.store[i];
    }
    
    
    /**
     * Creates an IntList which contains the same elements as a given
     * collection.
     */
    public IntList(Collection<Integer> col) 
    {
        this(col.size());
        size = col.size();
        int index = 0;
        for (int el : col) 
            store[index++] = el;
    }
    
    
    /**
     * Resizes the storage of this IntList.
     */
    private void resize(int reqSize) 
    {
        int newSize = store.length;
        while (newSize < reqSize && newSize < MAX_RESIZE_AMOUNT) {
            newSize *= 2;
        }
        
        int[] newStore = Arrays.copyOf(store, newSize);
        store = newStore;        
    }
    
    
    private void shiftRight(int index, int len) 
    {
        for (int i = store.length - 1; i > index + len; i--) {
            store[i] = store[i - len];
        }
    }
    
    
    void shiftLeft(int index, int len) 
    {
        for (int i = index; i < store.length - len - 1; i++) {
            store[i] = store[i + len];
        }
    }
    
    
    public boolean add(int el) 
    {
        add(size, el);
        return true;
    }

    
    @Override
    public boolean add(Integer el)
    {
        if (el == null) {
            throw new NullPointerException("This list does not support " +
            		"null elements.");
        }
        
        return add(el.intValue());
    }
    
    
    public void add(int index, int el)
    {
        if (!modifiable) {
            throw new UnsupportedOperationException("This list is read-only.");
        }
        if (index > size) {
            throw new IndexOutOfBoundsException("Size: " + size + 
                    "; Index: " + index);
        }
        
        ensureCapacity(size + 1);        
        shiftRight(index, 1);
        store[index] = el;
        size++;
    }

    
    @Override
    public void add(int index, Integer el)
    {
        add(index, el.intValue());
    }

    
    @Override
    public boolean addAll(Collection<? extends Integer> col)
    {
        return addAll(size, col);
    }

    
    @Override
    public boolean addAll(int index, Collection<? extends Integer> col)
    {
        if (!modifiable)
            throw new UnsupportedOperationException("This list is read-only.");

        if (col.isEmpty())
            return false;
        else { 
            ensureCapacity(size + col.size());
            shiftRight(index, col.size());
            for (Integer el : col) {
                store[index] = el;
                index++;
            }
            size += col.size();
            return true;
        }
    }

    
    @Override
    public void clear()
    {
        if (!modifiable)
            throw new UnsupportedOperationException("This list is read-only.");

        size = 0;
    }
    
    
    public boolean contains(int el)
    {
        return indexOf(el) >= 0;
    }

    
    public void ensureCapacity(int minCapacity)
    {
        if (!modifiable)
            throw new UnsupportedOperationException("This list is read-only.");

        if (store.length < minCapacity)
            resize(minCapacity);
    }
    
    
    public int getInt(int index)
    {
        return store[index];
    }
    

    @Override
    public Integer get(int index)
    {
        return Integer.valueOf(store[index]);
    }
    
    
    public int indexOf(int el) 
    {
        for (int i = 0; i < store.length; i++) {
            if (store[i] == el)
                return i;
        }
        
        return -1;        
    }

    

    public int lastIndexOf(int el)
    {
        for (int i = store.length - 1; i >= 0; i--) {
            if (store[i] == el)
                return i;
        }
        
        return -1;
    }
    
    
    public boolean removeInt(int el)
    {
        int index = indexOf(el);
        if (index >= 0) {
            shiftLeft(index, 1);
            size--;
            return true;
        }
        else return false;
    }

    @Override
    public boolean remove(Object el)
    {
        int index = indexOf(el);
        if (index >= 0) {
            shiftLeft(index, 1);
            size--;
            return true;
        }
        else return false;
    }
    

    @Override
    public Integer remove(int index)
    {
        Integer old = Integer.valueOf(store[index]);
        shiftLeft(index, 1);
        size--;
        return old;
    }

    
    @Override
    public boolean removeAll(Collection<?> col)
    {
        int removed = 0;
        int index = 0;
        for (int i = 0; i < size; i++) {
            int el = store[i];
            if (col.contains(Integer.valueOf(el))) {
                store[index] = el;
                index++;
            }
            else removed++;
        }
        shiftLeft(index, removed);
        return (removed > 0);
    }
    

    @Override
    public boolean retainAll(Collection<?> col)
    {
        int removed = 0;
        int index = 0;
        for (int i = 0; i < size; i++) {
            int el = store[i];
            if (col.contains(Integer.valueOf(el))) {
                store[index] = el;
                index++;
            }
            else removed++;
        }
        shiftLeft(index, removed);
        return (removed > 0);
    }
    
    
    public int set(int index, int el) 
    {
        int result = store[index];
        store[index] = el;
        return result;
    }
    

    @Override
    public Integer set(int index, Integer el)
    {
        return set(index, el.intValue());
    }

    
    @Override
    public int size()
    {
        return size;
    }

    
    public int[] toIntArray()
    {
        return Arrays.copyOf(store, store.length);        
    }
    
    
    public static IntList unmodifiableArrayView(int[] arr) 
    {
        IntList result = new IntList(arr);
        result.modifiable = false;
        return result;
    }
}
