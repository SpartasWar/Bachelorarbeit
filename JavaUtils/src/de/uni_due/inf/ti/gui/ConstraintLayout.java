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
import java.util.HashMap;

/**
 * A base class for layouts which simplifies the business of building new
 * layouts with constraints.
 * 
 * @author Jerry Huxtable, H.J. Sander Bruggink
 */
public abstract class ConstraintLayout implements LayoutManager2
{

    public enum Fill {
        FILL_NONE, 
        FILL_HORIZONTAL, 
        FILL_VERTICAL, 
        FILL_BOTH
    }

    public enum Alignment {
        CENTER, 
        NORTH, 
        NORTH_EAST, 
        EAST, 
        SOUTH_EAST, 
        SOUTH, 
        SOUTH_WEST, 
        WEST, 
        NORTH_WEST
    }

    protected enum SizeType {
        PREFERRED, MINIMUM, MAXIMUM
    }

    protected int hMargin = 0;
    protected int vMargin = 0;
    private HashMap<Component, Object> constraints;
    protected boolean includeInvisible = false;

    @Override
    public void addLayoutComponent(String constraint, Component c)
    {
        setConstraint(c, constraint);
    }

    @Override
    public void addLayoutComponent(Component c, Object constraint)
    {
        setConstraint(c, constraint);
    }

    @Override
    public void removeLayoutComponent(Component c)
    {
        if (constraints != null) constraints.remove(c);
    }

    protected void setConstraint(Component c, Object constraint)
    {
        if (constraint != null) {
            if (constraints == null)
                constraints = new HashMap<Component, Object>();
            constraints.put(c, constraint);
        }
        else if (constraints != null) constraints.remove(c);
    }

    protected Object getConstraint(Component c)
    {
        if (constraints != null) return constraints.get(c);
        return null;
    }

    public void setIncludeInvisible(boolean includeInvisible)
    {
        this.includeInvisible = includeInvisible;
    }

    public boolean getIncludeInvisible()
    {
        return includeInvisible;
    }

    protected boolean includeComponent(Component c)
    {
        return includeInvisible || c.isVisible();
    }

    public Dimension minimumLayoutSize(Container target)
    {
        return calcLayoutSize(target, SizeType.MINIMUM);
    }

    public Dimension maximumLayoutSize(Container target)
    {
        return calcLayoutSize(target, SizeType.MAXIMUM);
    }

    public Dimension preferredLayoutSize(Container target)
    {
        return calcLayoutSize(target, SizeType.PREFERRED);
    }

    private Dimension calcLayoutSize(Container target, SizeType type)
    {
        Dimension dim = new Dimension(0, 0);
        measureLayout(target, dim, type);
        Insets insets = target.getInsets();
        dim.width += insets.left + insets.right + 2 * hMargin;
        dim.height += insets.top + insets.bottom + 2 * vMargin;
        return dim;
    }

    public void invalidateLayout(Container target)
    {
    }

    public float getLayoutAlignmentX(Container parent)
    {
        return 0.5f;
    }

    public float getLayoutAlignmentY(Container parent)
    {
        return 0.5f;
    }

    public void layoutContainer(Container target)
    {
        measureLayout(target, null, SizeType.PREFERRED);
    }

    protected abstract void measureLayout(Container target,
            Dimension dimension, SizeType type);

    protected static Dimension getComponentSize(Component c, SizeType type)
    {
        switch (type) {
            case MINIMUM:
                return c.getMinimumSize();
            case MAXIMUM:
                return c.getMaximumSize();
            default: // PREFERRED
                return c.getPreferredSize();
        }
    }

    /**
     * Utility method to align a rectangle inside a cell.
     */
    static void alignInCell(Rectangle r, Rectangle cell, Alignment alignment,
            Fill fill)
    {
        r.x = cell.x;
        r.y = cell.y;

        /* Horizontal fill */
        if (fill == Fill.FILL_BOTH || fill == Fill.FILL_HORIZONTAL)
            r.width = cell.width;

        /* Vertical fill */
        if (fill == Fill.FILL_BOTH || fill == Fill.FILL_VERTICAL)
            r.height = cell.height;

        /* Horizontal alignment */
        switch (alignment) {
            case CENTER:
            case NORTH:
            case SOUTH:
                r.x += (cell.width - r.width) / 2;
                break;
            case EAST:
            case NORTH_EAST:
            case SOUTH_EAST:
                r.x += cell.width - r.width;
            default:
                // do nothing
        }

        /* Vertical alignment */
        switch (alignment) {
            case CENTER:
            case WEST:
            case EAST:
                r.y += (cell.height - r.height) / 2;
                break;
            case SOUTH:
            case SOUTH_WEST:
            case SOUTH_EAST:
                r.y += cell.height - r.height;
            default:
                // do nothing
        }
    }

}
