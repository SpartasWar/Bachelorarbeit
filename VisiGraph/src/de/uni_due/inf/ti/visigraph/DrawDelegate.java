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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Instances of this graph draw VxElement instances. 
 * Each VxElement can have multiple GraphDelegates (one for each component
 * that displays it).
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      subclass of VxElement that is drawn by this DrawDelegate
 */
abstract class DrawDelegate<T extends VxElement>
{
    /** The element which is drawn by this DrawDelegate. */
    private T element;
    
    /** GraphDrawer which uses this DrawDelegate. */
    private GraphDrawer drawer; 
    
    
    /**
     * Creates a new DrawDelegate instance.
     * 
     * @param element
     *      element which is drawn by this DrawDelegate
     * @param drawer
     *      GraphDrawer which own this DrawDelegate
     * @throws NullPointerException
     *      if {@code element} or {@code drawer} is {@code null}
     */
    public DrawDelegate(T element, GraphDrawer drawer)
    {
        if (element == null || drawer == null) throw new NullPointerException();
        this.element = element;
        this.drawer = drawer;
        element.addDelegate(this);
    }
    
    /**
     * Returns the VxElement which is drawn by this DrawDelegate.
     */
    public T getElement()
    {
        return element;
    }
    
    /**
     * Returns the GraphDrawer which uses this DrawDelegate.
     */
    public GraphDrawer getDrawer()
    {
        return drawer;
    }
        
    /**
     * Returns the style information of this DrawDelegate's element.
     */
    public Style getStyle()
    {
        return drawer.getStyle(element);
    }
    
    /**
     * Draw this DrawDelegate's element.
     * 
     * @param g
     *      graphics context to draw to
     * @throws NullPointerException
     *      if {@code g} is {@code null}
     */
    public abstract void draw(Graphics2D g, boolean selected);
    
    /**
     * Returns a rectangle which completely encompasses this DrawDelegate's
     * element.
     * 
     * @param buf
     *      if {@code buf} is non-null, it is set to the resulting rectangle
     *      and returned; otherwise, a new {@link Rectangle2D.Double} object
     *      is created and returned
     * @return
     *      a rectangle which completely encompasses this DrawDelegate's
     *      element
     */
    public abstract Rectangle2D getBounds(Rectangle2D buf);
    
    /**
     * Returns a rectangle which completely encompasses this DrawDelegate's
     * element.
     */
    public Rectangle2D getBounds()
    {
        return getBounds(null);
    }

    /**
     * Determines whether a given point lies on this DrawDelegate's element.
     */
    public boolean contains(Point2D point)
    {
        return getBounds().contains(point);
    }
    
    /**
     * Clear cached information for this DrawDelegate.
     */
    void clearCache()
    {
        // do nothing 
    }
    
    /**
     * Disables this DrawDelegate. After disabling, this DrawDelegate cannot
     * be used anymore and any attempt to call any of its methods will
     * cause unpredictable behavior. In particular, it cannot be re-enabled.
     */
    public void disable()
    {
        element.removeDelegate(this);
        element = null;
    }
}
