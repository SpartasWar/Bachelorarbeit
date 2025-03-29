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

/**
 * Abstract superclass of shapes which have a width and a height, and
 * optionally fix the aspect ratio.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class AspectRatioShape extends Shape
{
    private double width, height;
    private boolean fixedRatio;
    
    /**
     * Creates a new AspectRatioShape instance.
     * 
     * @param width
     *      width of the shape
     * @param height
     *      height of the shape
     */
    public AspectRatioShape(double width, double height)
    {
        this(width, height, false);
    }
    
    
    /**
     * Creates a new RectangleShape instance.
     * 
     * @param width
     *      width of the shape
     * @param height
     *      height of the shape
     * @param fixedRatio
     *      whether this shape has a fixed aspect ratio
     */
    public AspectRatioShape(double width, double height, boolean fixedRatio)
    {
        this.width = width;
        this.height = height;
        this.fixedRatio = fixedRatio;
    }
    
    
    /**
     * Returns whether this shape has a fixed aspect ratio. This property
     * is used when creating derived shapes.
     */
    public boolean hasFixedAspectRatio()
    {
        return fixedRatio;
    }
    
    
    /**
     * Returns the width of this rectangle.
     */
    public double getWidth()
    {
        return width;
    }
    
    
    /**
     * Returns the height of this rectangle.
     */
    public double getHeight()
    {
        return height;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Shape getEnlargement(double w, double h)
    {
        if (w < width && h < height) {
            return this;
        }
        else if (fixedRatio) {
            double ar = width / height;
            double newAr = w / h;
            if (ar > newAr) {
                h = w / ar;
            }
            else {
                w = h * ar;
            }
            return createCopy(w, h);
        }
        else {
            if (w < width) w = width;
            if (h < height) h = height;
            return createCopy(w, h);
        }
    }
    
    
    /**
     * Creates a clone this object, in which only the width and height
     * properties are different. The returned shape should have the same 
     * runtime type as this object.
     */
    protected abstract Shape createCopy(double w, double h); 
}
