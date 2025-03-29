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
 * A collection in which elements are collected as a 2D grid.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <E>
 *      element type of this Grid
 */
public interface Grid<E> extends Collection<E> 
{
    /**
     * Returns the item at the specified position in the grid.
     * 
     * @param row         
     *      row (y-coordinate) of the item
     * @param column      
     *      column (x-coordinate) of the item
     * @return            
     *      the item at the specified row and column
     * @throws IndexOutOfBoundsException 
     *      if the row or column index is out of range 
     */
    public E get(int row, int column);

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
     *      if the row or column index is out of bounds
     * @throws UnsupportedOperationException 
     *      if this operation is not supported by this Grid
     * @throws ClassCastException 
     *      if the class of the specified item prevents it from being added to
     *      this Grid
     * @throws IllegalArgumentException 
     *      if some property of the added item prevents it from being added to
     *      this Grid
     */
    public E set(int row, int column, E item);

    /**
     * Returns the number of rows in this grid.
     * 
     * @return           
     *      the number of rows in this grid
     */
    public int getRows();

    /**
     * Returns the number of columns in this grid.
     * 
     * @return
     *      the number of columns in this grid
     */
    public int getColumns();

    /**
     * Returns a view of a single row in the grid.
     * 
     * @param row
     *      the row number of the required row, where 
     *      {@code 0 <= row < getRows()}
     * @return
     *      the specified row as a list
     */
    public List<E> getRow(int row);

    /**
     * Returns a view of a single column in the grid.
     * 
     * @param col
     *      the column number of the required row, where 
     *      {@code 0 <= col < getColumns()}
     * @return
     *      the specified column as a list
     */
    public List<E> getColumn(int col);

    /**
     * Returns a view of a subgrid of this grid.
     * 
     * @param top         
     *      top row in the subgrid
     * @param left        
     *      left column in the subgrid
     * @param rows        
     *      number of rows in the subgrid
     * @param cols       
     *      number of columns in the subgrid
     * @return            
     *      the subgrid of this grid with the specified properties
     */
    public Grid<E> subgrid(int top, int left, int rows, int cols);

    /**
     * Resizes this grid.
     * This is an optional operation. If the grid does not support
     * resizing, a {@code UnsupportedOperationException} is thrown.
     * 
     * @param rows        
     *      the new number of rows of this grid
     * @param columns     
     *      the new number of columns of this grid
     * @throws UnsupportedOperationException 
     *      if this operation is not supported by this Grid
     */
    public void resize(int rows, int columns);

    /**
     * Returns an iterator which iterates through the elements of 
     * this grid, starting with the element at a specified 
     * coordinate.
     * 
     * @param row         
     *      row number of the start element
     * @param column      
     *      column number of the start element
     * @return
     *      an iterator which iterates over all elements of this Grid
     */
    public Iterator<E> iterator(int row, int column);
}
