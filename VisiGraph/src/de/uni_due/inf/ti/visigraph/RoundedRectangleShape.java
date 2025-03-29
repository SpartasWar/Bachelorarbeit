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
import java.awt.geom.RoundRectangle2D;

import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * VisiGraph shape for rounded rectangles.
 * 
 * @author H.J. Sander Bruggink
 */
public class RoundedRectangleShape extends RectangleShape
{
    private double arcRadius;
    
    /**
     * Creates a new RoundedRectangleShape instance.
     * 
     * @param width
     *      width of the rectangle
     * @param height
     *      height of the rectangle
     * @param arcRadius
     *      radius of arcs used to draw the corners
     * @param fixedRatio
     *      whether the shape has a fixed aspect ratio; this property is used
     *      when derived shapes are created
     */
    public RoundedRectangleShape(double width, double height, double arcRadius, boolean fixedRatio)
    {
        super(width, height, fixedRatio);
        this.arcRadius = arcRadius;
    }

    
    /**
     * Creates a new RoundedRectangleShape instance.
     * 
     * @param width
     *      width of the rectangle
     * @param height
     *      height of the rectangle
     * @param arcRadius
     *      radius of arcs used to draw the corners
     */
    public RoundedRectangleShape(double width, double height, double arcRadius)
    {
        this(width, height, arcRadius, false);
    }
    
    
    /**
     * Returns the radius of the corners.
     */
    public double getCornerRadius()
    {
        return arcRadius;
    }
    
    
    /**
     * Calculate intersection points of a line and a circle. This method returns
     * a double array of length 0, 2, or 4, where the components are the
     * x- and y-coordinates of the intersection points. 
     * 
     * @param cx
     *      x-coordinate of the center of the circle
     * @param cy
     *      y-coordinate of the center of the circle
     * @param cr
     *      radius of the circle
     * @param la
     *      angle of the line (the line goes through (0,0) and has this angle)
     * @return
     *      intersection points of the line and a circle as a double array
     */
    private static double[] getCirclePoints(
            double cx, 
            double cy, 
            double r, 
            double lx1,
            double ly1,
            double lx2,
            double ly2)
    {
        // translate so that the circle is at (0,0) 
        lx1 -= cx; ly1 -= cy;
        lx2 -= cx; ly2 -= cy;
        
        double[] coeff = ShapeUtils.getCoefficients(lx1, ly1, lx2, ly2);
        assert coeff != null;
            // Vertical lines can't occur here. If this method is moved
            // somewhere else and made public, calculate intersections for this
            // special case also.
        assert coeff.length == 2;
       
        double p = coeff[0];
        double q = coeff[1];
        
        // line: y = px + q
        // circle: x^2 + y^2 = r^2
        // that is:
        //   x^2 + (px + q)^2 - r^2 = 0
        //   x^2 + p^2x^2 + 2pqx + q^2 - r^2 = 0
        //   (p^2+1)x^2 + (2pq)x + (q^2-r^2) = 0
        
        double a = p * p + 1;
        double b = 2 * p * q;
        double c = q * q - r * r;
        double b2 = b * b;
        double ac4 = 4 * a * c;
        if (b2 < ac4) {
            return new double[] {};
        }
        else {
            double x1 = (-b - Math.sqrt(b2 - ac4)) / (2 * a);
            double x2 = (-b + Math.sqrt(b2 - ac4)) / (2 * a);
            if (x1 == x2) {
                double y1 = p * x1 + q;
                return new double[] { x1 + cx, y1 + cy } ;
            }
            else {
                double y1 = p * x1 + q;
                double y2 = p * x2 + q;
                return new double[] { x1 + cx, y1 + cy, x2 + cx, y2 + cy } ;
            }
        }
    }
    

    @Override
    public Point2D getBorderPoint(double direction, double outset,
            Point2D result)
    {
        direction = GraphicsUtils.normalizeRelativeAngle(direction);
        
        // pre-calculate values
        double cosa = Math.cos(direction);
        double sina = Math.sin(direction);
        double halfWidth = getWidth() / 2 + outset;
        double halfHeight = getHeight() / 2 + outset;
        double radius = arcRadius + outset;
        double x, y;
        
        if (sina == 0) {
            // special case: left or right border
            x = Math.abs(direction) < 1 ? halfWidth : -halfWidth;
            y = 0;
        }
        else if (cosa == 0) {
            // special case: top or bottom border
            x = 0;
            y = direction > 0 ? halfHeight : -halfHeight;
        }
        else {
            // first calculate border point as if this is a normal rectangle 
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
            
            double cx = halfWidth - radius;
            double cy = halfHeight - radius;
                        
            // is the point near one of the angles? then update for the circle
            if (x < -cx) {
                double[] cp = null;
                if (y < -cy) {
                    cp = getCirclePoints(-cx, -cy, radius, 0, 0, x, y);
                }
                else if (y > cy) {
                    cp = getCirclePoints(-cx, cy, radius, 0, 0, x, y);
                }
                if (cp != null && cp.length == 4) {
                    if (cp[0] < cp[2]) {
                        x = cp[0];
                        y = cp[1];
                    }
                    else {
                        x = cp[2];
                        y = cp[3];
                    }
                }
            }
            else if (x > cx) {
                double[] cp = null;
                if (y < -cy) {
                    cp = getCirclePoints(cx, -cy, radius, 0, 0, x, y);
                }
                else if (y > cy) {
                    cp = getCirclePoints(cx, cy, radius, 0, 0, x, y);
                }
                if (cp != null && cp.length == 4) {
                    if (cp[0] < cp[2]) {
                        x = cp[2];
                        y = cp[3];
                    }
                    else {
                        x = cp[0];
                        y = cp[1];
                    }
                }
            }
        }
        
        if (result == null) {
            result = new Point2D.Double(x, y);
        }
        else {
            result.setLocation(x, y);
        }
        return result;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected java.awt.Shape getAwtShape(boolean inner)
    {
        double w, h, r;
        if (inner) {
            double dd = DOUBLE_DISTANCE * 2;
            w = getWidth() - dd;
            h = getHeight() - dd;
            r = arcRadius - DOUBLE_DISTANCE;
        }
        else {
            w = getWidth();
            h = getHeight();
            r = arcRadius;
        }
        if (w < 0) w = 0;
        if (h < 0) h = 0;
        if (r < 0) r = 0;
        return new RoundRectangle2D.Double(-w/2, -h/2, w, h, r * 2, r * 2);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Shape createCopy(double width, double height)
    {
        return new RoundedRectangleShape(
                width, 
                height, 
                arcRadius, 
                hasFixedAspectRatio());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        long w = Double.doubleToLongBits(getWidth());
        long h = Double.doubleToLongBits(getHeight());
        long r = Double.doubleToLongBits(arcRadius);
        int hash = 1;
        hash = 37 * hash + (int)(w ^ (w >>> 32));
        hash = 37 * hash + (int)(h ^ (h >>> 32));
        hash = 37 * hash + (int)(r ^ (r >>> 32));
        hash = 37 * hash + (hasFixedAspectRatio() ? 1 : 0);
        return hash;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof RoundedRectangleShape)) return false;
        RoundedRectangleShape rs = (RoundedRectangleShape)o;
        return (rs.getWidth() == getWidth() && 
                rs.getHeight() == getHeight() &&
                rs.arcRadius == arcRadius &&
                rs.hasFixedAspectRatio() == hasFixedAspectRatio());
    }

}
