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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Shape class for circles.
 */
public class CircleShape extends Shape
{
    private double radius;

    /**
     * Creates a new circular shape instance.
     *  
     * @param radius
     *      radius of the new circular shape
     */
    public CircleShape(double radius)
    {
        this.radius = radius;
    }
    
    /**
     * Returns whether this shape has a fixed aspect ratio. Since circles always
     * have a fixed aspect ratio, this method just returns {@code true}.
     */
    @Override
    public boolean hasFixedAspectRatio()
    {
        return true;
    }    
    
    /**
     * Returns the radius of this circular shape.
     */
    public double getRadius()
    {
        return radius;
    }   
    
    /**
     * Returns the AWT shape which displays this shape.
     */
    @Override
    protected java.awt.Shape getAwtShape(boolean inner)
    {
        double r = inner ? radius - DOUBLE_DISTANCE : radius;
        return new Ellipse2D.Double(-r, -r, r * 2, r * 2);
    }   
    
    /**
     * Returns a point on the border of this shape.
     */
    @Override
    public Point2D getBorderPoint(double direction, double outset, Point2D result)
    {
        double r = radius + outset;
        double x = Math.cos(direction) * r;
        double y = Math.sin(direction) * r;
        if (result == null) {
            return new Point2D.Double(x,y);
        }
        else {
            result.setLocation(x, y);
            return result;
        }
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Shape getEnlargement(double width, double height)
    {
        double r = Math.max(width, height) / 2;
        if (r < radius) {
            return this;
        }
        else {
            return new CircleShape(r / 2); // XXX why /2 here?
        }
    }   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        long r = Double.doubleToLongBits(radius);
        return (int)(r ^ (r >>> 32));
    }   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof CircleShape)) return false;
        CircleShape cs = (CircleShape)o;
        return cs.radius == radius;
    }
}