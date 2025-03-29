/* 
 * Copyright (c) -2010, Jerry Huxtable (JHLabs)
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

import java.awt.*;

/**
 * Layout manager which layout components in a grid.
 * 
 * @author Jerry Huxtable, H.J. Sander Bruggink
 */
public class BasicGridLayout extends ConstraintLayout
{
    private int hGap, vGap;
    private int rows, cols, reqRows, reqCols;
    private int[] rowHeights, colWidths;
    private Alignment alignment = Alignment.NORTH_WEST;
    private Fill fill = Fill.FILL_BOTH;
    private int colWeight = 0;
    private int rowWeight = 0;

    
    /**
     * Creates a new BasicGridLayout with one column, unlimited rows and
     * default gaps sizes between rows and columns. 
     */
    public BasicGridLayout()
    {
        this(0, 1, 2, 2);
    }

    
    /**
     * Creates a new BasicGridLayout with an empty margin and default 
     * gaps.
     * 
     * @param rows
     *      number of rows in the layout; 0 means unlimited
     * @param cols
     *      number of column in the layout; 0 means unlimited
     */
    public BasicGridLayout(int rows, int cols)
    {
        this(rows, cols, 2, 2);
    }

    
    /**
     * Creates a new BasicGridLayout with an empty margin.
     * 
     * @param rows
     *      number of rows in the layout; 0 means unlimited
     * @param cols
     *      number of column in the layout; 0 means unlimited
     * @param hGap
     *      number of pixels between columns in the layout
     * @param vGap
     *      number of pixels between rows in the layout
     */
    public BasicGridLayout(int rows, int cols, int hGap, int vGap)
    {
        this(rows, cols, hGap, vGap, 0, 0);
    }

    
    /**
     * Creates a new BasicGridLayout.
     * 
     * @param rows
     *      number of rows in the layout; 0 means unlimited
     * @param cols
     *      number of column in the layout; 0 means unlimited
     * @param hGap
     *      number of pixels between columns in the layout
     * @param vGap
     *      number of pixels between rows in the layout
     * @param hMargin
     *      horizontal margin
     * @param vMargin
     *      vertical margin
     */
    public BasicGridLayout(int rows, int cols, int hGap, int vGap, int hMargin,
            int vMargin)
    {
        this.reqRows = this.rows = rows;
        this.reqCols = this.cols = cols;
        this.hGap = hGap;
        this.vGap = vGap;
        this.hMargin = hMargin;
        this.vMargin = vMargin;
    }

    
    /**
     * Sets the alignment setting of this BasicGridLayout.
     */
    public void setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
    }

    
    /**
     * Returns the alignment setting of this BasicGridLayout.
     */
    public Alignment getAlignment()
    {
        return alignment;
    }

    
    /**
     * Sets the fill setting of this BasicGridLayout.
     * The fill setting specifies how the sizes of cells are changed when
     * the size of the cell and the component in it are different.
     */
    public void setFill(Fill fill)
    {
        this.fill = fill;
    }

    
    /**
     * Returns the fill setting of this BasicGridLayout.
     */
    public Fill getFill()
    {
        return fill;
    }

    
    /**
     * Returns the number of rows in this layout.
     */
    public int getRows()
    {
        return rows;
    }

    
    /**
     * Returns the number of columns in this layout.
     */
    public int getColumns()
    {
        return cols;
    }

    
    public void setColWeight(int colWeight)
    {
        this.colWeight = colWeight;
    }

    public int getColWeight()
    {
        return colWeight;
    }

    public void setRowWeight(int rowWeight)
    {
        this.rowWeight = rowWeight;
    }

    public int getRowWeight()
    {
        return rowWeight;
    }

    
    /**
     * Override this to set alignment on a per-component basis.
     */
    protected Alignment alignmentFor(Component c, int row, int col)
    {
        return alignment;
    }

    
    /**
     * Override this to set fill on a per-component basis.
     */
    protected Fill fillFor(Component c, int row, int col)
    {
        return fill;
    }

    
    /**
     * Override this to set weights on a per-row basis.
     */
    protected int getRowWeight(int row)
    {
        return getRowWeight();
    }

    
    /**
     * Override this to set weights on a per-column basis.
     */
    protected int getColWeight(int col)
    {
        return getColWeight();
    }

    
    protected static int sumArray(int[] array, int spacing, int size)
    {
        int i, total = 0;

        for (i = 0; i < size; i++)
            total += array[i];
        if (size > 1) total += (size - 1) * spacing;
        return total;
    }

    
    protected void calcCellSizes(Container target, SizeType type)
    {
        int i;
        int count = target.getComponentCount();

        if (reqCols <= 0) {
            rows = reqRows;
            cols = (count + reqRows - 1) / reqRows;
        }
        else {
            rows = (count + reqCols - 1) / reqCols;
            cols = reqCols;
        }

        colWidths = new int[cols];
        for (i = 0; i < cols; i++)
            colWidths[i] = 0;
        rowHeights = new int[rows];
        for (i = 0; i < rows; i++)
            rowHeights[i] = 0;

        int index = 0;
        for (i = 0; i < count; i++) {
            Component c = target.getComponent(i);
            if (includeComponent(c)) {
                Dimension size = getComponentSize(c, type);
                int row = index / cols;
                int col = index % cols;
                if (colWidths[col] < size.width) colWidths[col] = size.width;
                if (rowHeights[row] < size.height)
                    rowHeights[row] = size.height;
                index++;
            }
        }

        Dimension size = target.getSize();
        Insets insets = target.getInsets();
        int totalWeight, totalSize, remainder;

        size.width -= insets.left + insets.right + 2 * hMargin;
        size.height -= insets.top + insets.bottom + 2 * vMargin;
        totalWeight = totalSize = 0;
        for (i = 0; i < cols; i++) {
            totalWeight += getColWeight(i);
            totalSize += colWidths[i];
            if (i != 0) totalSize += hGap;
        }
        if (totalWeight != 0) {
            remainder = size.width - totalSize;
            for (i = 0; i < cols; i++)
                colWidths[i] += remainder * getColWeight(i) / totalWeight;
        }

        totalWeight = totalSize = 0;
        for (i = 0; i < rows; i++) {
            totalWeight += getRowWeight(i);
            totalSize += rowHeights[i];
            if (i != 0) totalSize += vGap;
        }
        if (totalWeight != 0) {
            remainder = size.height - totalSize;
            for (i = 0; i < rows; i++)
                rowHeights[i] += remainder * getRowWeight(i) / totalWeight;
        }
    }

    protected void measureLayout(Container target, Dimension dimension,
            SizeType type)
    {
        if (dimension != null) {
            calcCellSizes(target, type);
            dimension.width = sumArray(colWidths, hGap, cols);
            dimension.height = sumArray(rowHeights, vGap, rows);
            rowHeights = colWidths = null;
        }
        else {
            int count = target.getComponentCount();
            if (count > 0) {
                Insets insets = target.getInsets();
                int index = 0;

                calcCellSizes(target, type);

                for (int i = 0; i < count; i++) {
                    Component c = target.getComponent(i);
                    if (includeComponent(c)) {
                        Dimension d = getComponentSize(c, type);
                        Rectangle r = new Rectangle(0, 0, d.width, d.height);
                        int row = index / cols;
                        int col = index % cols;
                        int x, y;

                        x = insets.left + sumArray(colWidths, hGap, col)
                                + hMargin;
                        y = insets.top + sumArray(rowHeights, vGap, row)
                                + vMargin;
                        if (col > 0) x += hGap;
                        if (row > 0) y += vGap;
                        Rectangle cell = new Rectangle(x, y, colWidths[col],
                                rowHeights[row]);
                        alignInCell(r, cell, alignmentFor(c, row, col),
                                fillFor(c, row, col));
                        c.setBounds(r.x, r.y, r.width, r.height);
                        index++;
                    }
                }
            }
            rowHeights = colWidths = null;
        }
    }
}
