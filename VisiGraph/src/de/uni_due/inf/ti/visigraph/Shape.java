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
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Superclass for node shapes.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class Shape
{    
    protected static final double DOUBLE_DISTANCE = 3.0;
    
    // AWT shapes of this shape
    private java.awt.Shape outer = null;
    private java.awt.Shape inner = null;
    
    
    /**
     * Returns the coordinate of a point of the border of this node shape,
     * including an additional distance from the shape edge to the returned 
     * point. This additional distance can be used to accommodate the line width
     * in which the shape is drawn.
     * <p>
     * If the third argument is {@code null}, a new object containing the
     * result is returned; otherwise, the specified object is changed to 
     * contain the result and returned.
     * 
     * @param direction     
     *      direction in radians, where 0 is right, pi/2 is below, etc.
     * @param outset
     *      extra distance between the shape border and the returned point;
     *      this should usually be half of the line width in which the shape
     *      is drawn
     * @param result
     *      buffer object to store the result
     * @return              
     *      the coordinate of this shape at the given direction
     */
    public abstract Point2D getBorderPoint(
            double direction, 
            double outset, 
            Point2D result);
    
    
    /**
     * Returns whether this shape has a fixed aspect ratio. This property is
     * used when creating derived shapes with the {@link #getEnlargement} 
     * methods: if {@code true}, these will always return a larger shape with
     * the same aspect ratio.
     * <p>
     * This default implementation just returns {@code false}. Subclasses which
     * fix the aspect ratio of the shape should override this method.
     */
    public boolean hasFixedAspectRatio()
    {
        return false;
    }
    
    
    /**
     * Returns the coordinate of a point of the border of this node shape,
     * including an additional distance from the shape edge to the returned 
     * point. This additional distance can be used to accommodate the line width
     * in which the shape is drawn, or to leave an empty space between the
     * shape and connected edges.
     * 
     * @param direction     
     *      direction in radians, where 0 is right, pi/2 is below, etc.
     * @param outset
     *      extra distance between the shape border and the returned point;
     *      this should usually be half of the line width in which the shape
     *      is drawn
     * @return              
     *      the coordinate of this shape at the given direction
     */
    public Point2D getBorderPoint(double direction, double outset)
    {
        return getBorderPoint(direction, outset, null);
    }
    
    
    /**
     * Returns the AWT shape which is used to draw this node shape.
     * 
     * @param inner         
     *      {@code true} when the returned shape must be the inner shape (used 
     *      when drawing double lines)
     * @return
     *      the AWT shape which is used to draw this shape
     */
    protected abstract java.awt.Shape getAwtShape(boolean inner);
    
    
    /**
     * Determines whether a coordinate is within this shape.
     * 
     * @param x
     *      x coordinate
     * @param y
     *      y coordinate
     * @return
     *      {@code true} if the coordinate {@code (x,y)} is within this shape,
     *      {@code false} otherwise
     */
    public boolean contains(double x, double y)
    {
        if (outer == null) outer = getAwtShape(false);
        return outer == null ? false : outer.contains(x, y);
    }

    
    /**
     * Draw this shape on a graphics context.
     * 
     * @param g             
     *      graphics context to draw to
     * @param center        
     *      location of the center of the shape
     * @param line          
     *      line color of the shape, or {@code null} if the exterior of the
     *      shape is not to be drawn
     * @param fill          
     *      fill color of the shape, or {@code null} if the shape is not to
     *      be filled
     * @param dbl           
     *      if true, the border is drawn as double line
     * @throws NullPointerException
     *      if <em>g</em> or <em>center</em> is {@code null}  
     */
    public void draw(Graphics2D g, Point2D center, Color line, Color fill, boolean dbl) 
    {
        if (g == null || center == null) throw new NullPointerException();
        
        Color oldColor = g.getColor();
        AffineTransform oldTrans = g.getTransform();
        g.translate(center.getX(), center.getY());
        
        if (fill != null) {
            g.setColor(fill);
            if (outer == null) outer = getAwtShape(false);
            g.fill(outer);
        }
        if (line != null) {
            g.setColor(line);
            if (outer == null) outer = getAwtShape(false);
            g.draw(outer);
            if (dbl) {
                if (inner == null) inner = getAwtShape(true);
                g.draw(inner);
            }
        }
        
        g.setColor(oldColor);
        g.setTransform(oldTrans);
    }
    
    
    /**
     * Returns a rectangle which completely encompasses this shape.
     */
    public Rectangle2D getBounds()
    {
        if (outer == null) outer = getAwtShape(false);
        return outer.getBounds2D();
    }
    

    /** 
     * Returns the size of the rectangle which completely encompasses this 
     * shape. If the {@code buffer} parameter is non-null, that object will
     * be used to store the size and returned; otherwise, a new Dimension2D
     * object will be created.
     * 
     * @param buffer
     *      buffer where the answer is stored, or {@code null} of a new 
     *      Dimension2D object is to be created 
     * @return
     *      Dimension2D object which holds the size of this shape
     */
    public Dimension2D getSize(Dimension2D buffer)
    {
        if (buffer == null) buffer = new DoubleDimension();
        if (outer == null) outer = getAwtShape(false);
        Rectangle2D bounds = outer.getBounds2D();
        buffer.setSize(bounds.getWidth(), bounds.getHeight());
        return buffer;
    }
    
    
    /** 
     * Returns the size of the rectangle which completely encompasses this 
     * shape. 
     *  
     * @return
     *      Dimension2D object which holds the size of this shape
     */
    public Dimension2D getSize()
    {
        return getSize(null);
    }
    
    
    /**
     * Returns a rectangle which completely encompasses this shape if its 
     * center is at a given position.
     * 
     * @param center
     *      the center of the shape
     * @return
     *      a rectangle which completely encompasses this shape if drawn at the
     *      given position
     */
    public Rectangle2D getBounds(Point2D center)
    {
        Rectangle2D bounds = getBounds();
        double dx = center.getX() - bounds.getCenterX();
        double dy = center.getY() - bounds.getCenterY();
        bounds.setRect(
                bounds.getX() + dx, 
                bounds.getY() + dy,
                bounds.getWidth(),
                bounds.getHeight());
        return bounds;
    }
    
    
    /**
     * Returns a larger shape which has the same form as this one, 
     * but is large enough to hold a rectangle of the given dimensions.
     * 
     * @param width
     *      width of the rectangle
     * @param height
     *      height of the rectangle
     * @return
     *      a shape of the same form which is large enough to hold a rectangle
     *      of the given width and height
     */
    public abstract Shape getEnlargement(double width, double height);

    
    /**
     * Returns a larger shape which has the same form as this one, 
     * but is large enough to hold a rectangle of the given dimensions. 
     * 
     * @param size
     *      dimensions of the inner rectangle
     * @return
     *      a shape of the same form which is large enough to hold a rectangle
     *      of the given width and height
     */
    public Shape getEnlargement(Dimension2D size) 
    {
        return getEnlargement(size.getWidth(), size.getHeight());
    }

    
    /**
     * Returns a larger rounded rectangle which has the same form as this one, 
     * but is large enough to hold a rectangle which has the same dimensions as
     * a given rectangle. 
     * 
     * @param rect
     *      rectangle of the size determines the size of the new shape; the
     *      x- and y-coordinates of this rectangle are ignored by this method
     * @return
     *      a shape of the same form which is large enough to hold a rectangle
     *      of the given width and height
     */
    public Shape getEnlargement(Rectangle2D rect) 
    {
        return getEnlargement(rect.getWidth(), rect.getHeight());
    }
    

    /**
     * Creates a circular shape.
     */
    public static CircleShape createCircle(double radius)
    {
        return new CircleShape(radius);
    }
    
    
    /**
     * Creates a rectangular shape.
     */
    public static RectangleShape createRectangle(double width, double height)
    {
        return new RectangleShape(width, height);
    }
    
    
    /**
     * Creates a rounded rectangle shape.
     * 
     * @param width
     *      width of the rounded rectangle
     * @param height
     *      height of the rounded rectangle
     * @param radius
     *      radius of the circles used to draw the corners
     * @return
     *      a rounded rectangle shape with the given properties
     */
    public static RoundedRectangleShape createRoundedRectangle(double width, double height, double radius)
    {
        return new RoundedRectangleShape(width, height, radius);
    }
    

    /**
     * Creates a square shape.
     */
    public static RectangleShape createSquare(double size)
    {
        return new RectangleShape(size, size);
    }
    
    
    /**
     * Creates a rounded square shape.
     * 
     * @param size
     *      width and height of the rounded square
     * @param radius
     *      radius of the circles used to draw the corners
     * @return
     *      a rounded rectangle shape with the given properties
     */
    public static RoundedRectangleShape createRoundedSquare(double size, double radius)
    {
        return new RoundedRectangleShape(size, size, radius);
    }

    
    /**
     * Creates a coordinate shape.
     * A coordinate shape is an invisible, zero-sized shape.
     */
    public static CoordinateShape createCoordinate()
    {
        return new CoordinateShape();
    }
}
