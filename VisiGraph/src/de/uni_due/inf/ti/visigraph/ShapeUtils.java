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

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Class with utility methods for graphical shapes and curves.
 * 
 * @author H.J. Sander Bruggink
 */
public class ShapeUtils
{
    private static final double PRECISION = 1.0;
    private static final int REC_BOUND = 64;
    
    
    /**
     * Returns a point on a cubic curve.
     * 
     * @param t
     *      distance of the point from the origin of the curve
     * @param x1
     *      x-coordinate of the origin of the curve
     * @param y1
     *      y-coordinate of the origin of the curve
     * @param cp1x
     *      x-coordinate of the first control point of the curve
     * @param cp1y
     *      y-coordinate of the first control point of the curve
     * @param cp2x
     *      x-coordinate of the second control point of the curve
     * @param cp2y
     *      y-coordinate of the second control point of the curve
     * @param x2
     *      x-coordinate of the target of the curve
     * @param y2
     *      y-coordinate of the target of the curve
     * @return
     *      coordinates of the point which at position t on the given curve
     */
    public static Point2D getPointOnCurve(
            double t,
            double x1, double y1, 
            double cp1x, double cp1y,
            double cp2x, double cp2y,
            double x2, double y2)
    {
            double trev = 1 - t;
            double trev2 = trev * trev;
            double trev3 = trev2 * trev;
            double t2 = t * t;
            double t3 = t2 * t;
            
            double x = (x1 * trev3)
                     + (3 * cp1x * t * trev2)
                     + (3 * cp2x * t2 * trev)
                     + (x2 * t3);
            double y = (y1 * trev3) 
                     + (3 * cp1y * t * trev2)
                     + (3 * cp2y * t2 * trev)
                     + (y2 * t3);
            
            return new Point2D.Double(x, y);
    }
    
    
    /**
     * Returns a point on a cubic curve.
     * 
     * @param t
     *      position on the curve
     * @param curve
     *      cubic curve
     * @return
     *      coordinates of the point which is at position t on the given curve
     */
    public static Point2D getPointOnCurve(double t, CubicCurve2D curve)
    {
        return getPointOnCurve(t, 
                curve.getX1(), curve.getY1(),
                curve.getCtrlX1(), curve.getCtrlY1(),
                curve.getCtrlX2(), curve.getCtrlY2(),
                curve.getX2(), curve.getY2());
    }
    
    
    /**
     * Returns a point on a line.
     * 
     * @param t
     *      position on the line
     * @param x1
     *      x-coordinate of the first end point of the line
     * @param y1
     *      y-coordinate of the first end point of the line
     * @param x2
     *      x-coordinate of the second end point of the line
     * @param y2
     *      y-coordinate of the second end point of the line
     * @return
     *      point on the line at the given relative position
     */
    public static Point2D getPointOnCurve(double t, double x1, double y1, double x2, double y2)
    {
        return new Point2D.Double(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t);
    }
    

    /**
     * Returns a point on a line.
     * 
     * @param t
     *      relative position on the line
     * @param p1
     *      first end point of the line
     * @param p2
     *      second end point of the line
     * @return
     *      point on the line from {@code p1} to {@code p2} on the given
     *      relative position {@code t}
     */
    public static Point2D getPointOnCurve(double t, Point2D p1, Point2D p2)
    {
        return getPointOnCurve(t, p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    
    /**
     * Returns a point on a line.
     * 
     * @param t
     *      relative position on the line
     * @param curve
     *      the line
     * @return
     *      point on the line {@code curve} on the given relative position
     *      {@code t}
     */
    public static Point2D getPointOnCurve(double t, Line2D curve) 
    {
        return getPointOnCurve(t, curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2());
    }
    

    /**
     * Returns a relative point on a curve. The relative position 0.0 is the
     * start point of the curve, while the position 1.0 is the end point of
     * the curve. Currently, only instances of Line2D and CubicCurve2D are
     * supported.
     * 
     * @param t
     *      relative position on the curve
     * @param curve
     *      the curve to find a point on
     * @return
     *      the coordinates of the given relative position on the curve
     * @throws IllegalArgumentException
     *      if {@code curve} is not a {@code CubicCurve2D} or {@code Line2D}
     *      instance
     */
    public static Point2D getPointOnCurve(double t, java.awt.Shape curve)
    {
        if (curve instanceof CubicCurve2D) {
            return getPointOnCurve(t, (CubicCurve2D)curve);
        }
        else if (curve instanceof Line2D) {
            return getPointOnCurve(t, (Line2D)curve);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    

    /**
     * Private worker method for calculating the distance of a point to a
     * cubic curve. 
     */
    private static double distanceToCurve(Point2D p, CubicCurve2D curve, int rec)
    {
        if (rec >= REC_BOUND || curve.getFlatness() < PRECISION) {
            return new Line2D.Double(curve.getP1(), curve.getP2()).ptSegDist(p);
        }
        else {
            CubicCurve2D right = new CubicCurve2D.Double();
            curve.subdivide(curve, right);
            double leftDist = distanceToCurve(p, curve, rec+1);
            double rightDist = distanceToCurve(p, right, rec+1);
            return Math.min(leftDist, rightDist);
        }
    }


    /**
     * Approximates the distance of a point to a cubic curve.
     * 
     * @param p
     *      the point to approximate the distance of
     * @param curve
     *      the curve
     * @return
     *      approximated minimal distance of the point {@code p} to the given 
     *      curve
     */
    public static double distanceToCurve(Point2D p, CubicCurve2D curve)
    {
        CubicCurve2D left = new CubicCurve2D.Double();
        CubicCurve2D right = new CubicCurve2D.Double();
        curve.subdivide(left, right);
        double leftDist = distanceToCurve(p, left, 1);
        double rightDist = distanceToCurve(p, right, 1);
        return Math.min(leftDist, rightDist);
    }
    

    /**
     * Determines the distance of a point to line.
     * 
     * @param p
     *      the point to determine the distance of
     * @param curve
     *      the line to which to determine the distance
     * @return
     *      the distance of the point {@code p} to the given line
     */
    public static double distanceToCurve(Point2D p, Line2D curve) 
    {
        return curve.ptSegDist(p);
    }
    
    
    /**
     * Determines the distance of a point to a curve. This method calls one of
     * the {@code distanceToCurve} methods based on the runtime type of its
     * argument, and throws an {@code IllegalArgumentException} if the 
     * argument is not of a type that can be handled by this method.
     * 
     * @param p
     *      the point to determine the distance of
     * @param curve
     *      the curve to which to determine the distance
     * @return
     *      the (approximate) distance of the point {@code p} to the given
     *      curve.
     * @throws IllegalArgumentException
     *      if {@code shape} is not of a type for which this method known
     *      how to calculate a distance
     */
    public static double distanceToCurve(Point2D p, Shape curve)
    {
        if (curve instanceof Line2D) {
            return distanceToCurve(p, (Line2D)curve);
        }
        else if (curve instanceof CubicCurve2D) {
            return distanceToCurve(p, (CubicCurve2D)curve);
        }
        else {
            throw new IllegalArgumentException("Shape class " + curve.getClass().getSimpleName() + " not supported.");
        }
    }


    /**
     * Private worker method for determining whether a cubic curve intersects
     * a given rectangle.
     */
    private static boolean intersects(CubicCurve2D curve, Rectangle2D rect, int rec)
    {
        if (rec >= REC_BOUND || curve.getFlatness() <= PRECISION) {
            return new Line2D.Double(curve.getP1(), curve.getP2()).intersects(rect);
        }
        else {
            CubicCurve2D right = new CubicCurve2D.Double();
            curve.subdivide(curve, right);
            return intersects(curve, rect, rec+1) || intersects(right, rect, rec+1);
        }
    }


    /**
     * Determines whether a cubic curve intersects a given rectangle.
     * 
     * @param curve
     *      the curve of which to determine whether it intersects the rectangle
     * @param rect
     *      the rectangle of which to determine whether the curve intersects
     *      it
     * @return
     *      {@code true} if {@code curve} intersects {@code rect}
     */
    public static boolean intersects(CubicCurve2D curve, Rectangle2D rect)
    {
        CubicCurve2D right = new CubicCurve2D.Double();
        CubicCurve2D left = new CubicCurve2D.Double();
        curve.subdivide(left, right);
        return intersects(left, rect, 1) || intersects(right, rect, 1);
    }
    
    
    /**
     * Determines whether a line intersects a given rectangle.
     * 
     * @param curve
     *      the line of which to determine whether it intersects the rectangle
     * @param rect
     *      the rectangle of which to determine whether the line intersects
     *      it
     * @return
     *      {@code true} if {@code curve} intersects {@code rect}
     */
    public static boolean intersects(Line2D curve, Rectangle2D rect)
    {
        return curve.intersects(rect);
    }
    
    
    /**
     * Determines whether a curve intersects a given rectangle. This method
     * calls on of the {@code intersects} methods based on the runtime type
     * of the argument, and throws an {@code IllegalArgumentException} if the
     * type of the argument is not known.
     * 
     * @param curve
     *      the curve of which to determine whether it intersects the rectangle
     * @param rect
     *      the rectangle of which to determine whether the curve intersects
     *      it
     * @return
     *      {@code true} if {@code curve} intersects {@code rect}
     * @throw IllegalArgumentException
     *      if the runtime type of {@code curve} cannot be handled by this
     *      method
     */
   public static boolean intersects(Shape curve, Rectangle2D rect)
    {
        if (curve instanceof Line2D) {
            return intersects((Line2D)curve, rect);
        }
        else if (curve instanceof CubicCurve2D) {
            return intersects((CubicCurve2D)curve, rect);
        }
        else {
        	throw new IllegalArgumentException("Shape class " + curve.getClass().getSimpleName() + " not supported.");
        }        
    }
   
   
   /**
    * Returns the coefficients of the line which runs through two given points.
    * The return value is an length-2 array <code>{a,b}</code> such that the
    * formula <i>f</i>(<i>x</i>) = <i>ax</i> + <i>b</i> describes the line
    * going through the two given points.
    * 
    * @param x1
    *       x-coordinate of the first point on the line
    * @param y1
    *       y-coordinate of the first point on the line
    * @param x2
    *       x-coordinate of the second point on the line
    * @param y2
    *       y-coordinate of the second point on the line
    * @return
    *       the coefficients of the formula of the line going through
    *       {@code (x1,y1)} and {@code (x2,y2)}, or {@code null} if the line
    *       is vertical
    * @throws IllegalArgumentException
    *       if {@code x1 == x2} and {@code y1 == y2}
    */
   public static double[] getCoefficients(double x1, double y1, double x2, double y2)
   {
       double[] ret = new double[2];
       double dx = x2 - x1;
       double dy = y2 - y1;
       if (dx == 0) {
           if (dy == 0) throw new IllegalArgumentException();
           return null;
       }
       ret[0] = dy / dx;
       ret[1] = y1 - (x1 * ret[0]);
       return ret;
   }
   

   /**
    * Returns the coefficients of the line which runs through two given points.
    * The return value is an length-2 array <code>{a,b}</code> such that the
    * formula <i>f</i>(<i>x</i>) = <i>ax</i> + <i>b</i> describes the line
    * going through the two given points.
    * 
    * @param p1
    *       first point on the line
    * @param p2
    *       second point on the line
    * @return
    *       the coefficients of the formula of the line going through
    *       {@code p1} and {@code p2}, or {@code null} if the line is vertical
    * @throws IllegalArgumentException
    *       if {@code p1} and {@code p2} have the same coordinates
    */
   public static double[] getCoefficients(Point2D p1, Point2D p2)
   {
       return getCoefficients(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }
   
   
   /**
    * Returns the coefficients of the line of a line segment.
    * The return value is an length-2 array <code>{a,b}</code> such that the
    * formula <i>f</i>(<i>x</i>) = <i>ax</i> + <i>b</i> describes the line
    * of the line segment. 
    * 
    * @param
    *       line to determine the coefficients of
    * @return
    *       coefficient of the line of the given line segment, or {@code null}
    *       if the line is vertical
    * @throws IllegalArgumentException
    *       if the end points of {@code line} are identical
    */
   public static double[] getCoefficients(Line2D line)
   {
       return getCoefficients(line.getX1(), line.getY1(), line.getX2(), line.getY2());
   }
   
   
   /**
    * Returns the intersection of two lines.
    * 
    * @param p1
    *       first point of the first line
    * @param p2
    *       second point of the first line
    * @param q1
    *       first point of the second line
    * @param q2
    *       second point of the second line
    * @param result
    *       if non-null, the result will be stored in this object and returned; 
    *       otherwise, a new {@link Point2D.Double} instance with the result
    *       will be created 
    * @return
    *       intersection point of the two given lines, or {@code null} if the
    *       two lines are parallel (or the same)
    */
   public static Point2D getIntersection(
           Point2D p1, Point2D p2, 
           Point2D q1, Point2D q2, 
           Point2D result) 
   {
       double[] pCoeff = getCoefficients(p1, p2);
       double[] qCoeff = getCoefficients(q1, q2);
       if (pCoeff == null && qCoeff == null) { // both vertical so parallel 
           return null;                        // or the same           
       }
       double x = 0;
       double y = 0;
       if (pCoeff == null) {
           x = p1.getX();
           y = qCoeff[0] * x + qCoeff[1];
       }
       else if (qCoeff == null) {
           x = q1.getX();
           y = pCoeff[0] * x + pCoeff[1];
       }
       else if (pCoeff[0] == qCoeff[0]) { // parallel
           return null;
       }
       else {
           x = (qCoeff[1] - pCoeff[1]) / (pCoeff[0] - qCoeff[0]);
           y = pCoeff[0] * x + pCoeff[1];
       }
       if (result == null) {
           return new Point2D.Double(x, y);
       }
       else {
           result.setLocation(x, y);
           return result;
       }
   }
   
   
   /**
    * Returns the intersection of two lines.
    * 
    * @param p1
    *       first point of the first line
    * @param p2
    *       second point of the first line
    * @param q1
    *       first point of the second line
    * @param q2
    *       second point of the second line
    * @return
    *       intersection point of the two given lines, or {@code null} if the
    *       two lines are parallel (or the same)
    */
   public static Point2D getIntersection(
           Point2D p1, Point2D p2, 
           Point2D q1, Point2D q2)
   {
       return getIntersection(p1, p2, q1, q2, null);
   }

}
