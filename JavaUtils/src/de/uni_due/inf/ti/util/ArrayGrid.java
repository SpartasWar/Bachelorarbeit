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

/**
 * This class represents a list which uses a two-dimensional array as storage.
 * All optional operations are supported, in particular the grid is modifiable
 * and resizable.
 * <p>
 * Instances of this class offer O(1) get and set operations. Resizing, however,
 * is an O(n) operation.
 * 
 * @author H.J. Sander Bruggink
 * 
 * @param <E>
 *        type of the elements stored by this array.
 */
public class ArrayGrid<E> extends AbstractGrid<E>
{
    private Object[][] items;
    private int rows;
    private int columns;
    private int hash;
    private boolean calcHash = true;


    /**
     * Creates a new ArrayGrid of the specified size.
     * 
     * @param rows
     *        number of rows in the new Grid
     * @param columns
     *        number of columns in the new Grid
     * @throws IllegalArgumentException
     *         if either {@code rows < 0} or {@code columns < 0}.
     */
    public ArrayGrid(int rows, int columns)
    {
        checkSize(rows, columns);
        this.items = new Object[rows][columns];
        this.rows = rows;
        this.columns = columns;
    }

    
    /**
     * Creates a new ArrayGrid which is a copy of a specified other one.
     */
    public ArrayGrid(Grid<E> original)
    {
        rows = original.getRows();
        columns = original.getColumns();
        items = new Object[rows][columns];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < columns; c++)
                items[r][c] = original.get(r, c);
    }

    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public E get(int row, int column)
    {
        return (E) items[row][column];
    }

    
    /**
     * Returns the number of columns in this grid.
     */
    @Override
    public int getColumns()
    {
        return columns;
    }

    
    /**
     * Returns the number of rows in this grid.
     */
    @Override
    
    public int getRows()
    {
        return rows;
    }

    
    /**
     * Replaces the element at the specified position in this grid with the
     * the specified element.
     * 
     * @param row
     *      row (y-coordinate) of the item
     * @param column
     *      column (x-coordinate) of the item
     * @param item
     *      the item to be stored at the specified position
     * @return 
     *      the item previously at the specified position
     * @throws IndexOutOfBoundsException
     *       if the row or column index is out of
     *       bounds
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(int row, int column, E item)
    {
        E oldItem = (E) items[row][column];
        items[row][column] = item;
        calcHash = true;
        return oldItem;
    }

    
    /**
     * Sets the number of rows and columns in this grid.
     * 
     * @param rows
     *      the new number of rows of this grid
     * @param columns
     *      the new number of columns of this grid
     * @throws IllegalArgumentException
     *      if either {@code row < 0} or {@code column < 0}
     */
    @Override
    public void resize(int rows, int columns)
    {
        checkSize(rows, columns);
        Object[][] oldItems = items;
        int oldRows = this.rows;
        int oldCols = this.columns;
        this.rows = rows;
        this.columns = columns;
        items = new Object[rows][columns];
        for (int r = 0; r < Math.min(rows, oldRows); r++)
            for (int c = 0; c < Math.min(columns, oldCols); c++)
                items[r][c] = oldItems[r][c];
        calcHash = true;
    }

    
    /**
     * Calculates the hash code of this grid.
     */
    @Override
    public int hashCode()
    {
        if (calcHash) {
            hash = super.hashCode();
            calcHash = false;
        }
        return hash;
    }

    
    /**
     * Convenience method which throws an IllegalArgumentException if either
     * of its two parameters is negative.
     */
    private static void checkSize(int rows, int columns)
    {
        if ((rows < 0) || (columns < 0))
            throw new IllegalArgumentException("Illegal grid size -- rows: "
                    + rows + ", columns: " + columns);
    }

}
