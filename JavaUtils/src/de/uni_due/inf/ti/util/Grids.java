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

/**
 * This class contains static utility methods for working with grids.
 * 
 * @author H.J. Sander Bruggink
 * 
 */
public class Grids
{
    // prohibit instantiation
    private Grids() { }

    private static class UnmodifiableGrid<F> implements Grid<F>
    {
        private Grid<F> orig;

        public UnmodifiableGrid(Grid<F> orig)
        {
            this.orig = orig;
        }

        @Override
        public F get(int row, int column)
        {
            return orig.get(row, column);
        }

        @Override
        public List<F> getColumn(int col)
        {
            return orig.getColumn(col);
        }

        @Override
        public int getColumns()
        {
            return orig.getColumns();
        }

        @Override
        public List<F> getRow(int row)
        {
            return orig.getRow(row);
        }

        @Override
        public int getRows()
        {
            return orig.getRows();
        }

        @Override
        public void resize(int rows, int columns)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: resize() not supported.");
        }

        @Override
        public F set(int row, int column, F item)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: set(int,int,F) not supported.");
        }

        @Override
        public Grid<F> subgrid(int top, int left, int rows, int cols)
        {
            return orig.subgrid(top, left, rows, cols);
        }

        @Override
        public boolean add(F e)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: add(F) not supported.");
        }

        @Override
        public boolean addAll(Collection<? extends F> c)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: addAll(Collection) not supported.");
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: clear() not supported.");
        }

        @Override
        public boolean contains(Object o)
        {
            return orig.contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            return orig.containsAll(c);
        }

        @Override
        public boolean isEmpty()
        {
            return orig.isEmpty();
        }

        @Override
        public Iterator<F> iterator()
        {
            return orig.iterator();
        }

        @Override
        public Iterator<F> iterator(int row, int col)
        {
            return orig.iterator(row, col);
        }

        @Override
        public boolean remove(Object o)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: remove(Object) not supported.");
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: removeAll(Collection) not supported.");
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException(
                    "Unmodifiable grid: retainAll(Collection) not supported.");
        }

        @Override
        public int size()
        {
            return orig.size();
        }

        @Override
        public Object[] toArray()
        {
            return orig.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            return orig.toArray(a);
        }

    }

    public static <E> Grid<E> unmodifiableGrid(Grid<E> e)
    {
        return new UnmodifiableGrid<E>(e);
    }
}
