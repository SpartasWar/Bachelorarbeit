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

import java.awt.geom.Dimension2D;

/**
 * Dimension2D subclass which stores its data in double precision.
 * 
 * @author H.J. Sander Bruggink
 */
class DoubleDimension extends Dimension2D
{
    double width;
    double height;

    /**
     * Creates a new DoubleDimension objects with zero width and height.
     */
    public DoubleDimension()
    {
        width = 0;
        height = 0;
    }
    
    /**
     * Create a new DoubleDimension with user-supplied width and height.
     * 
     * @param width
     *      width of the new DoubleDimension object
     * @param height
     *      height of the new DoubleDimension object
     */
    public DoubleDimension(double width, double height)
    {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Creates a new DoubleDimension which contains the same information as
     * an existing Dimension2D object.
     * 
     * @param dim
     *      the dimension object that the new object will copy its information
     *      from
     */
    public DoubleDimension(Dimension2D dim)
    {
        width = dim.getWidth();
        height = dim.getHeight();
    }
    
    /**
     * Returns a new DoubleDimension object with the same dimension as this
     * one.
     */
    public Object clone()
    {
        return new DoubleDimension(width, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWidth()
    {
        return width;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeight()
    {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(double width, double height)
    {
        this.width = width;
        this.height = height;
    }
}
