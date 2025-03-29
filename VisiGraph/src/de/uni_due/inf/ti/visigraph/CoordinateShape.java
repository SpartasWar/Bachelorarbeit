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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Shape which has no dimensions.
 * 
 * @author H.J. Sander Bruggink
 */
public class CoordinateShape extends Shape
{
    /**
     * {@inheritDoc}
     * <p>
     * CoordinateShapes have no dimension. This method always returns
     * the point (0,0).
     */
    @Override
    public Point2D getBorderPoint(double direction, double outset,
            Point2D result)
    {
        if (result == null) {
            result = new Point2D.Double(0, 0);
        }
        else {
            result.setLocation(0, 0);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * CoordinateShapes are not drawn. This method always returns {@code null}.
     */
    @Override
    protected java.awt.Shape getAwtShape(boolean inner)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * CoordinateShapes have no dimensions. This method just returns 
     * {@code this}.
     */
    @Override
    public Shape getEnlargement(double width, double height)
    {
        return this;
    }
    
    /**
     * {@inheritDoc}.
     * <p>
     * CoordinateShapes are not drawn, so this method does nothing.
     */
    @Override
    public void draw(Graphics2D g, Point2D center, Color line, Color fill, boolean dbl)
    {
        // do nothing
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * CoordinateShapes have no dimensions; this method returns a rectangle
     * with size 0.
     */
    @Override
    public Rectangle2D getBounds()
    {
        return new Rectangle2D.Double(0, 0, 0, 0);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * CoordinateShapes have no dimension; this method returns a Dimension2D of
     * size 0.
     */
    @Override
    public Dimension2D getSize(Dimension2D buffer)
    {
        if (buffer == null) {
            buffer = new DoubleDimension(0, 0);
        }
        else {
            buffer.setSize(0, 0);
        }
        return buffer;
    }
}
