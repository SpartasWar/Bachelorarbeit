/* 
 * Copyright (c) 2013-2015, H.J. Sander Bruggink
 * Copyright (c) 2013-2014, University of Duisburg-Essen
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

package de.uni_due.inf.ti.visigraph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * Shape for rectangles.
 */
public class RectangleShape extends AspectRatioShape
{
    /**
     * Creates a new RectangleShape instance.
     * 
     * @param width
     *      width of the rectangle
     * @param height
     *      height of the rectangle
     */
    public RectangleShape(double width, double height)
    {
        this(width, height, false);
    }
    
    
    /**
     * Creates a new RectangleShape instance.
     * 
     * @param width
     *      width of the rectangle
     * @param height
     *      height of the rectangle
     * @param fixedRatio
     *      whether this rectangle has a fixed aspect ratio
     */
    public RectangleShape(double width, double height, boolean fixedRatio)
    {
        super(width, height, fixedRatio);
    }
    
    
    /**
     * Returns the AWT shape used to draw this Shape.
     */
    @Override
    protected java.awt.Shape getAwtShape(boolean inner)
    {
        double w;
        double h;
        if (inner) {
            double dd = DOUBLE_DISTANCE * 2;
            w = getWidth() - dd;
            h = getHeight() - dd;
        }
        else {
            w = getWidth();
            h = getHeight();
        }
        if (w < 0) w = 0;
        if (h < 0) h = 0;
        return new Rectangle2D.Double(-w/2, -h/2, w, h);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getBorderPoint(double direction, double outset, Point2D result)
    {
        direction = GraphicsUtils.normalizeRelativeAngle(direction);
        
        // pre-calculate values
        double cosa = Math.cos(direction);
        double sina = Math.sin(direction);
        double halfWidth = getWidth() / 2 + outset;
        double halfHeight = getHeight() / 2 + outset;
        double x, y;
        
        // calculate border point
        if (sina == 0) {
            x = Math.abs(direction) < 1 ? halfWidth : -halfWidth;
            y = 0;
        }
        else if (cosa == 0) {
            x = 0;
            y = direction > 0 ? halfHeight : -halfHeight;
        }
        else {
            x = (halfHeight * cosa) / sina;
            if (x < -halfWidth || x > halfWidth) {
                double sign = Math.abs(direction) < Math.PI / 2 ? 1 : -1;
                y = ((halfWidth * sina) / cosa) * sign;
                x = halfWidth * sign;
            }
            else {
                double sign = Math.signum(direction);
                x *= sign;
                y = halfHeight * sign;
            }
        }
        
        if (result == null) {
            return new Point2D.Double(x,y);
        }
        else {
            result.setLocation(x, y);
            return result;
        }
    }
    
    
    /**
     * Creates an instance of the type of this object which has a given
     * width and height, and all other properties are the same as in this 
     * object.
     */
    @Override
    protected Shape createCopy(double w, double h) 
    {
        return new RectangleShape(w, h, hasFixedAspectRatio());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        long w = Double.doubleToLongBits(getWidth());
        long h = Double.doubleToLongBits(getHeight());
        int hash = 1;
        hash = 37 * hash + (int)(w ^ (w >>> 32));
        hash = 37 * hash + (int)(h ^ (h >>> 32));
        hash = 37 * hash + (hasFixedAspectRatio() ? 1 : 0);
        return hash;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof RectangleShape)) return false;
        RectangleShape rs = (RectangleShape)o;
        return (rs.getWidth() == getWidth() && 
                rs.getHeight() == getHeight() &&
                rs.hasFixedAspectRatio() == hasFixedAspectRatio());
    }
    
}