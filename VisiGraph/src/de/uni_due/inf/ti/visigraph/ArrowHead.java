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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * Instances of this class represent an arrow head.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class ArrowHead
{
    /** Flag which specifies whether or not this arrow head should be filled. */
    private boolean filled;
    
    
    /**
     * Creates a new arrow head which is not filled.
     */
    protected ArrowHead() 
    {
        filled = false;
    }
    
    
    /**
     * Creates a new arrow head.
     * 
     * @param filled
     *      flag which specifies whether or not this arrow head's shape should
     *      be filled or only drawn
     */
    protected ArrowHead(boolean filled)
    {
        this.filled = filled;
    }

    
    /**
     * Returns a shape which has the form of this arrow head.
     * 
     * @param x
     *      x-coordinate of the end point of the arrow head
     * @param y
     *      y-coordinate of the end point of the arrow head
     * @param angle
     *      direction that the arrow head points in
     * @return 
     *      the shape of this arrow head at the specified angle, or {@code null}
     *      if this arrow head doesn't have a shape (that is, it isn't 
     *      visible)
     */
    public abstract Shape getShape(double x, double y, double angle);
    
    
    /**
     * Returns a shape which has the form of this arrow head.
     * 
     * @param pos
     *      position of the end point of the arrow head
     * @param angle
     *      direction that the arrow head points in
     * @return 
     *      the shape of this arrow head at the specified angle, or {@code null}
     *      if this arrow head doesn't have a shape (that is, it isn't 
     *      visible)
     */
    public Shape getShape(Point2D pos, double angle) 
    {
        return getShape(pos.getX(), pos.getY(), angle);
    }
    
    
    /**
     * Returns whether or not the shape of this arrow head should be filled.
     */
    public boolean isFilled()
    {
        return filled;
    }
    
    
    /**
     * Draws this arrow head to a graphics context.
     * 
     * @param g
     *      graphics context to draw on
     * @param x
     *      x-coordinate of the arrow head's point
     * @param y
     *      y-coordinate of the arrow head's point
     * @param angle
     *      direction the arrow head points in
     */
    public void draw(Graphics2D g, double x, double y, double angle)
    {
        Shape shape = getShape(x, y, angle);
        if (shape != null) {
            g.draw(shape);
            if (filled) g.fill(shape);
        }
    }
    
    
    /**
     * Draws this arrow head to a graphics context.
     * 
     * @param g
     *      the graphics context to draw to
     * @param pos
     *      the position of the arrow head's point
     * @param angle
     *      direction the arrow head points in
     */
    public void draw(Graphics2D g, Point2D pos, double angle)
    {
        draw(g, pos.getX(), pos.getY(), angle);
    }
        
    
    /**
     * Class which implements a default arrow head style.
     */
    public static class TriangleArrowHead extends ArrowHead
    {
        private final boolean open;
        private final double sideLength;
        private final double sideAngle;

        public boolean isOpen() {
            return open;
        }
        public double getSideLength() {
            return sideLength;
        }
        public double getSideAngle() {
            return sideAngle;
        }
        public double getWidth() {
            return Math.sin(sideAngle / 2.) * sideLength * 2.;
        }
        public double getHeight() {
            return Math.cos(sideAngle / 2.) * sideLength;
        }

        TriangleArrowHead(boolean open, double width, double height) 
        {
            super(!open);
            this.open = open;
            
            sideLength = Math.hypot(width / 2, height);
            sideAngle = Math.atan2(width / 2, height) * 2;
        }

        @Override
        public Shape getShape(double x, double y, double angle) 
        {
            double mid = GraphicsUtils.normalizeAngle(angle + Math.PI);
            double left = mid + sideAngle / 2;
            double right = mid - sideAngle / 2;
            double lx = x + sideLength * Math.cos(left);
            double ly = y + sideLength * Math.sin(left);
            double rx = x + sideLength * Math.cos(right);
            double ry = y + sideLength * Math.sin(right);

            Path2D path = new Path2D.Double();
            path.moveTo(rx, ry);
            path.lineTo(x, y);
            path.lineTo(lx, ly);
            if (!open) path.closePath();
            
            return path;
        }
    }
    
    
    /**
     * Class which represents a arrow head in the form of a circle.
     */
    public static class CircleArrowHead extends ArrowHead
    {
        private final double radius;
        public double getRadius() {
            return radius;
        }
        
        public CircleArrowHead(boolean filled, double radius)
        {
            super(filled);
            this.radius = radius;
        }
        
        @Override
        public Shape getShape(double x, double y, double angle)
        {
            double backAngle = GraphicsUtils.normalizeAngle(angle + Math.PI);
            double midX = x + Math.cos(backAngle) * radius;
            double midY = y + Math.sin(backAngle) * radius;
            return new Ellipse2D.Double(midX - radius, midY - radius, 
                    2 * radius, 2 * radius);
        }
    }

	/**
	 * Class which represents an arrow head that is not drawn.
	 */
	public static class EmptyArrowHead extends ArrowHead {
		@Override
		public Shape getShape(double x, double y, double angle) {
			return null;
		}
	}

    /**
     * Returns an empty arrow head. This is an arrow head which is not drawn.
     * Such arrow heads are mainly used as a placeholder in places where
     * {@code null} already has a different meaning.
     * 
     * @return
     *      an empty arrow head
     */
    public static ArrowHead newEmptyArrowHead()
    {
        return new EmptyArrowHead();
    }

    /**
     * Creates a new triangle arrow head. A triangle arrow head consists of a
     * triangle.
     * 
     * @param width
     *      width of the new arrow head
     * @param height
     *      height of the new arrow head
     * @return
     *      a new triangle arrow head with the specified width and height
     */
    public static ArrowHead newTriangleArrowHead(double width, double height)
    {
        return new TriangleArrowHead(false, width, height);
    }
    
    
    /**
     * Creates a new line arrow head. A line arrow head consists of two lines
     * which touch each other in the point of the arrow head.
     * 
     * @param width
     *      width of the arrow head
     * @param height
     *      height of the arrow head
     * @return
     *      a line arrow head with the specified width and height
     */
    public static ArrowHead newLineArrowHead(double width, double height)
    {
        return new TriangleArrowHead(true, width, height);
    }
    
    
    /**
     * Creates a new arrow head in the form of a filled circle.
     * 
     * @param radius
     *      radius of the circle
     * @return
     *      a bullet arrow head
     */
    public static ArrowHead newBulletArrowHead(double diameter)
    {
        return new CircleArrowHead(true, diameter / 2);
    }
}
