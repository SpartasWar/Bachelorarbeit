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

package de.uni_due.inf.ti.gui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A ListModel which adheres to the List interface of the Collections
 * framework.
 * 
 * @author H.J. Sander Bruggink
 */
public class CollectionListModel<E> extends AbstractList<E> implements List<E>,
        ListModel<E>
{
    private List<E> backingList;
    private ArrayList<ListDataListener> listeners;

    /**
     * Creates a new CollectionListModel.
     */
    public CollectionListModel()
    {
        backingList = new ArrayList<E>();
        listeners = new ArrayList<ListDataListener>();
    }

    /**
     * Creates a new CollectionListModel which contains the elements of the
     * given collection.
     */
    public CollectionListModel(final Collection<E> orig)
    {
        backingList = new ArrayList<E>(orig);
        listeners = new ArrayList<ListDataListener>();
    }

    @Override
    public boolean add(E e)
    {
        backingList.add(e);
        int size = backingList.size();
        sendIntervalAdded(size, size);
        return true;
    }

    @Override
    public void add(int index, E element)
    {
        backingList.add(index, element);
        sendIntervalAdded(index, index);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        int size = backingList.size();
        boolean result = backingList.addAll(c);
        if (result) sendIntervalAdded(size, backingList.size() - 1);
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        boolean result = backingList.addAll(index, c);
        if (result) sendIntervalAdded(index, index + c.size() - 1);
        return result;
    }

    @Override
    public void clear()
    {
        int size = backingList.size();
        if (size > 0) {
            backingList.clear();
            sendIntervalRemoved(0, size - 1);
        }
    }

    @Override
    public boolean contains(Object o)
    {
        return backingList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return backingList.containsAll(c);
    }

    @Override
    public E get(int index)
    {
        return backingList.get(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return backingList.indexOf(o);
    }

    @Override
    public boolean isEmpty()
    {
        return backingList.isEmpty();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return backingList.lastIndexOf(o);
    }

    @Override
    public boolean remove(Object o)
    {
        int index = backingList.indexOf(o);
        if (index >= 0) {
            backingList.remove(index);
            sendIntervalRemoved(index, index);
            return true;
        }
        else
            return false;
    }

    @Override
    public E remove(int index)
    {
        E ob = backingList.remove(index);
        sendIntervalRemoved(index, index);
        return ob;
    }

    @Override
    public E set(int index, E element)
    {
        E ob = backingList.set(index, element);
        sendContentsChanged(index, index);
        return ob;
    }

    @Override
    public int size()
    {
        return backingList.size();
    }

    @Override
    public E getElementAt(int index)
    {
        return backingList.get(index);
    }

    @Override
    public int getSize()
    {
        return backingList.size();
    }

    @Override
    public void addListDataListener(ListDataListener l)
    {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l)
    {
        listeners.remove(l);
    }

    private void sendIntervalAdded(int index1, int index2)
    {
        ListDataEvent e = null;
        ListIterator<ListDataListener> it = listeners.listIterator(listeners
                .size());
        while (it.hasPrevious()) {
            ListDataListener l = it.previous();
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED,
                        index1, index2);
            }
            l.intervalAdded(e);
        }
    }

    private void sendIntervalRemoved(int index1, int index2)
    {
        ListDataEvent e = null;
        ListIterator<ListDataListener> it = listeners.listIterator(listeners
                .size());
        while (it.hasPrevious()) {
            ListDataListener l = it.previous();
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,
                        index1, index2);
            }
            l.intervalRemoved(e);
        }
    }

    private void sendContentsChanged(int index1, int index2)
    {
        ListDataEvent e = null;
        ListIterator<ListDataListener> it = listeners.listIterator(listeners
                .size());
        while (it.hasPrevious()) {
            ListDataListener l = it.previous();
            if (e == null) {
                e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
                        index1, index2);
            }
            l.contentsChanged(e);
        }
    }

}
