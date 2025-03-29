/* 
 * Copyright (c) 2010-2014, H.J. Sander Bruggink, 
 * Copyright (c) 2010-2014, University of Duisburg-Essen
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

import java.util.NoSuchElementException;


/**
 * This class represents a stack with a limited capacity.
 * This stack removes references to object which lie deeper on the stack than the
 * given capacity.
 * 
 * @author H.J. Sander Bruggink
 *
 */
public class CapacityStack<E> 
{
    /**
     * Private class which holds the respective elements of this CapacityStack. 
     */
    private static class CapacityStackElement<F> {
        CapacityStackElement<F> prev, next;
        F element;
    }

    private CapacityStackElement<E> first, last;
    private int capacity;
    private int size;


    /**
     * Creates a new CapacityStack with no capacity limit.
     */
    public CapacityStack() 
    {
        this(-1);
    }


    /**
     * Creates a new CapacityStack with a given capacity limit.
     * If {@code capacity < 0}, the stack has no capacity limit.
     * @param capacity
     */
    public CapacityStack(int capacity) 
    {
        this.capacity = capacity;
        this.size = 0;
    }


    /**
     * Removes the last element from this CapacityStack.
     */
    private void removeLastElement() 
    {
        assert last != null;
        last = last.prev;
        if (last != null) {
            last.next = null;
        }
        size--;    
    }


    /**
     * Pushes an element to the stack. If the stack becomes bigger than the
     * capacity of this CapacityStack, elements on the bottom of this
     * stack are removed.
     * 
     * @param element     the element to be pushed on top of the stack
     */
    public void push(E element) 
    {
        if (capacity > 0 && size >= capacity && last != null) {
            removeLastElement();
        }
        if (size == 0) {
            first = new CapacityStackElement<E>();
            first.element = element;
            first.prev = null;
            first.next = null;
            last = first;
        }
        else {
            CapacityStackElement<E> nse = new CapacityStackElement<E>();
            nse.element = element;
            nse.next = first;
            nse.prev = null;
            first.prev = nse;
            first = nse;
        }
        size++;
    }


    /**
     * Returns the element which is currently on the top of the stack.
     * 
     * @return            the top element of the stack, or {@code null}
     *                    if the stack is empty.
     */
    public E peek() 
    {
        if (first == null) 
            return null;
        else
            return first.element;
    }


    /**
     * Returns the element which is currently on the top of the stack and removes it.
     * 
     * @return            the element on the top of the stack
     * @throws            NoSuchElementException if the stack is empty
     */
    public E pop() 
    {
        if (first == null)
            throw new NoSuchElementException();
        E result = first.element;
        if (first.next == null) {
            first = null;
            last = null;
        }
        else {
            first = first.next;
            first.prev.next = null;
            first.prev = null;
        }
        size--;
        return result;
    }


    /**
     * Sets the capacity of this CapacityStack.
     * If the new capacity is positive but smaller than the current capacity,
     * elements are removed from the bottom of the stack until the stack has
     * reached the given capacity.
     */
    public void setCapacity(int capacity) 
    {
        this.capacity = capacity;
        if (capacity >= 0) {
            while (size > capacity) 
                removeLastElement();
        }
    }


    /**
     * Returns the capacity of this stack.
     */
    public int getCapacity() 
    {
        return capacity;
    }


    /**
     * Returns the number of elements on this CapacityStack.
     */
    public int getSize() 
    {
        return size;
    }


    /**
     * Determines whether this CapacityStack is empty.
     */
    public boolean isEmpty() 
    {
        return size == 0;
    }


    /**
     * Removes all elements from this CapacityStack.
     */
    public void clear() 
    {
        first = null;
        last = null;
        size = 0;
    }
}
