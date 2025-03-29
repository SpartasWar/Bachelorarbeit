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

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractGrid<E> 
    extends AbstractCollection<E> 
    implements Grid<E>
{
    private static final String EXC_COL_OUT_OF_BOUNDS_FMT = "col: %d, number of columns: %d";
    private static final String EXC_ROW_OUT_OF_BOUNDS_FMT = "row: %d, number of rows: %d";
    
    private GridDirection direction = GridDirections.newHorizontalGridDirection(1, 1);
    private GridPosition addPos = new GridPosition();
    
    
    /**
     * This abstract class is the superclass of all <em>List views</view> into
     * the grid.
     * 
     * @param <F>
     *        type of the elements of the list view
     */
    private abstract static class ViewList<F> extends AbstractList<F>
    {
        protected Grid<F> grid;
        private int savedRows, savedCols;

        ViewList(Grid<F> grid)
        {
            this.grid = grid;
            savedRows = grid.getRows();
            savedCols = grid.getColumns();
        }

        protected void checkSize()
        {
            if ((grid.getRows() != savedRows)
                    || (grid.getColumns() != savedCols))
                throw new ConcurrentModificationException("Grid size changed.");
        }
    }

    
    /**
     * This class provides a column view into the grid.
     * 
     * @param <F>
     *        type of the elements of the column view
     */
    private static class ColumnViewList<F> extends ViewList<F>
    {

        Grid<F> grid;
        int column;

        ColumnViewList(Grid<F> grid, int column)
        {
            super(grid);
            this.column = column;
        }

        @Override
        public F get(int index)
        {
            checkSize();
            return grid.get(index, column);
        }

        @Override
        public int size()
        {
            checkSize();
            return grid.getRows();
        }
    }

    
    /**
     * This class provides a row view into a grid.
     * 
     * @param <F>
     *        type of the elements of the row view
     */
    private static class RowViewList<F> extends ViewList<F>
    {

        Grid<F> grid;
        int row;

        RowViewList(Grid<F> grid, int row)
        {
            super(grid);
            this.row = row;
        }

        @Override
        public F get(int index)
        {
            checkSize();
            return grid.get(row, index);
        }

        @Override
        public int size()
        {
            checkSize();
            return grid.getColumns();
        }
    }

    
    /**
     * This class represents a subgrid of another grid.
     * 
     * @param <F>
     *        type of the elements of the subgrid
     */
    private static class Subgrid<F> extends AbstractGrid<F>
    {

        Grid<F> grid;
        int top, left, rows, cols;
        int gridRows, gridCols;

        Subgrid(Grid<F> grid, int top, int left, int rows, int cols)
        {
            this.grid = grid;
            this.top = top;
            this.left = left;
            this.rows = rows;
            this.cols = cols;
            gridRows = grid.getRows();
            gridCols = grid.getColumns();
        }

        @Override
        public F get(int row, int column)
        {
            checkSize();
            checkPosition(row, column);
            return grid.get(row + top, column + left);
        }

        @Override
        public F set(int row, int column, F element)
        {
            checkSize();
            checkPosition(row, column);
            return grid.set(row + top, column + left, element);
        }

        @Override
        public int getRows()
        {
            return rows;
        }

        @Override
        public int getColumns()
        {
            return cols;
        }

        private void checkSize()
        {
            if ((grid.getRows() != gridRows) || (grid.getColumns() != gridCols))
                throw new ConcurrentModificationException("Grid size changed.");
        }
    }

    
    /**
     * This class represents an iterator over the grid's contents.
     */
    private class GridIterator implements Iterator<E>
    {

        private int gridRows, gridColumns;
        private GridPosition pos;
        private GridDirection dir;

        public GridIterator(GridDirection dir)
        {
            this(dir, 0, 0);
        }

        public GridIterator(GridDirection dir, int startRow, int startCol)
        {
            gridRows = getRows();
            gridColumns = getColumns();
            pos = new GridPosition(startRow, startCol);
            this.dir = dir;
        }

        public E next()
        {
            checkSize();
            if ((pos.row >= getRows()) || (pos.col >= getColumns()))
                throw new NoSuchElementException(
                        "Iterator has passed end of grid.");
            E item = get(pos.row, pos.col);
            dir.nextPosition(pos, getRows(), getColumns());
            return item;
        }

        public boolean hasNext()
        {
            checkSize();
            return (pos.row < getRows() && pos.col < getColumns());
        }

        public void remove()
        {
            throw new UnsupportedOperationException(
                    "Remove not supported on Grids.");
        }

        private void checkSize()
        {
            if ((getRows() != gridRows) || (getColumns() != gridColumns))
                throw new ConcurrentModificationException("Grid size changed.");
        }
    }

    public void setGridDirection(GridDirection dir)
    {
        direction = dir;
    }

    public GridDirection getGridDirection()
    {
        return direction;
    }

    
    /**
     * Returns an iterator which iterates over the grid's elements.
     */
    @Override
    public Iterator<E> iterator()
    {
        return new GridIterator(direction);
    }

    
    /**
     * Returns an iterator which iterates over the grid's elements with
     * a custom direction.
     * 
     * @param dir
     *      direction whith which to iterator over the grid's elements
     * @return
     *      iterator which iterates over the grids elements
     */
    public Iterator<E> iterator(GridDirection dir)
    {
        return new GridIterator(dir);
    }

    
    /**
     * Returns an iterator which iterates over the grid's elements 
     * starting at a user-specified position.
     * 
     * @param row
     *      starting row of the new iterator
     * @param col
     *      starting column of the new iterator
     * @return
     *      iterator which iterates over the grids elements
     */
    @Override
    public Iterator<E> iterator(int row, int col)
    {
        return new GridIterator(direction, row, col);
    }

    
    /**
     * Returns an iterator which iterates over the grid's elements 
     * starting at a user-specified position with a custom direction.
     * 
     * @param row
     *      starting row of the new iterator
     * @param col
     *      starting column of the new iterator
     * @param dir
     *      direction whith which to iterator over the grid's elements
     * @return
     *      iterator which iterates over the grids elements
     */
    public Iterator<E> iterator(GridDirection dir, int row, int col)
    {
        return new GridIterator(dir, row, col);
    }

    
    /**
     * Returns the number of elements of this grid.
     * This default implementation returns the number of rows multiplied by the
     * number of columns.
     * 
     * @return {@inheritDoc}
     */
    @Override
    public int size()
    {
        return getRows() * getColumns();
    }

    
    /**
     * Returns the item at the specified position in the grid.
     * 
     * @param row
     *        row (y-coordinate) of the item
     * @param column
     *        column (x-coordinate) of the item
     * @return the item at the specified row and column
     * @throws IndexOutOfBoundsException
     *         if the row or column index is out of
     *         range
     */
    @Override
    public abstract E get(int row, int column);


    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> getColumn(int col)
    {
        if (col < 0 || col >= getColumns())
            throw new IndexOutOfBoundsException(String.format(
                    EXC_COL_OUT_OF_BOUNDS_FMT, col, getColumns()));
        return new ColumnViewList<E>(this, col);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> getRow(int row)
    {
        if (row < 0 || row >= getRows())
            throw new IndexOutOfBoundsException(String.format(
                    EXC_ROW_OUT_OF_BOUNDS_FMT, row, getColumns()));
        return new RowViewList<E>(this, row);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getColumns();

    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getRows();

    
    /**
     * Returns a view of a subgrid of this grid.
     * 
     * @param top
     *        top row in the subgrid
     * @param left
     *        left column in the subgrid
     * @param rows
     *        number of rows in the subgrid
     * @param cols
     *        number of columns in the subgrid
     * @return the subgrid of this grid with the specified properties
     */
    @Override
    public Grid<E> subgrid(int top, int left, int rows, int cols)
    {
        return new Subgrid<E>(this, top, left, rows, cols);
    }
    

    /**
     * Checks if the specified position is within the grid bounds.
     * This is a convenience method which throws an
     * {@code IndexOutOfBoundsException} if the specified position is out of
     * bounds. Otherwise, it just returns to the caller.
     * 
     * The bounds of the grid are obtained by calling {@link getRows()} and
     * {@link getColumns()}.
     * 
     * @param row
     *        row (y-coordinate) of the position
     * @param col
     *        column (x-coordinate) of the position
     */
    protected void checkPosition(int row, int col)
    {
        if (col < 0 || col >= getColumns())
            throw new IndexOutOfBoundsException(String.format(
                    EXC_COL_OUT_OF_BOUNDS_FMT, col, getColumns()));
        if (row < 0 || row >= getRows())
            throw new IndexOutOfBoundsException(String.format(
                    EXC_ROW_OUT_OF_BOUNDS_FMT, row, getColumns()));
    }

    
    /**
     * Returns the order in which the {@code add} operation fills the locations
     * in the grid.
     */
    public GridDirection getDirection()
    {
        return direction;
    }

    
    /**
     * Sets the order in which the {@code add} operation fills the locations
     * in the grid.
     */
    public void setDirection(GridDirection dir)
    {
        direction = dir;
    }

    
    /**
     * Adds an element to this Grid.
     * This default implementation calls {@code set} to set the element at the
     * current position and updates the current position according to the
     * current direction. If the Grid is full (i.e.
     * {@code getRows() * getColumns()} elements have been added already),
     * an {@code IllegalStateException} is thrown.
     * 
     * This method calls the object's {@code getRows()} and {@code getColumns()}
     * method in order to obtain the bounds of the grid.
     * 
     * @param e
     *        the element to add to this Grid
     * @return this method always returns {@code true}
     * @throws IllegalStateException
     *         if this Grid is full
     */
    @Override
    public boolean add(E e)
    {
        if ((addPos.row >= getRows()) || (addPos.col >= getColumns()))
            throw new IllegalStateException("Grid is full.");
        set(addPos.row, addPos.col, e);
        direction.nextPosition(addPos, getRows(), getColumns());
        return true;
    }

    
    /**
     * Resizes this grid.
     * This default implementation throws an
     * {@code UnsupportedOperationException}.
     */
    @Override
    public void resize(int rows, int columns)
    {
        throw new UnsupportedOperationException(
                "Resize not supported on this Grid.");
    }

    
    /**
     * Compares the specified object with this grid for equality.
     * This method returns {@code true} if and only if the specified object is
     * a grid of the same size, and all corresponding pairs of objects in the
     * grid are equal.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (!(o instanceof Grid<?>)) return false;

        Grid<?> og = (Grid<?>) o;
        if ((og.getColumns() != getColumns()) || (og.getRows() != getRows()))
            return false;
        for (int row = 0; row < getRows(); row++)
            for (int column = 0; column < getColumns(); column++) {
                E o1 = get(row, column);
                Object o2 = og.get(row, column);
                if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
            }
        return true;
    }

    
    /**
     * Returns the hash code of this grid.
     */
    @Override
    public int hashCode()
    {
        int hash = 1;
        for (int row = 0; row < getRows(); row++)
            for (int column = 0; column < getRows(); column++) {
                E o = get(row, column);
                hash = 31 * hash + (o == null ? 0 : o.hashCode());
            }
        return hash;
    }
}
