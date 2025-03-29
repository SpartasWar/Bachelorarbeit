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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * A class with utility methods for writing layouters.
 * 
 * @author H.J. Sander Bruggink
 */
class LayouterUtils
{
    private LayouterUtils() { } // prohibit instantiation
    
    private static final double[] angles = { 0.0, 0.26, -0.26, 0.74, -0.74, 1.57, -1.57, 2.39, -2.39 };
    /*
    static {
        final int NUM_OF_ANGLES = 10;
        double step = (Math.PI / 2) / (double)NUM_OF_ANGLES;
        angles = new double[(NUM_OF_ANGLES * 2) - 1];
        angles[0] = 0;
        for (int i = 1; i < angles.length; i += 2) {
            double b = step + step * (double)(i / 2);
            angles[i] = b;
            angles[i+1] = -b; 
        }
    }
     */
    
    /** 
     * (Approximately) count the number of nodes in a collection which touch
     * the given curve.
     */
    private static int countTouches(
            GraphDrawer drawer, 
            Shape curve, 
            Collection<? extends VxElement> obstacles)
    {
        int result = 0;
        for (VxElement el : obstacles) {
            DrawDelegate<?> elDD = drawer.getDelegate(el);
            if (ShapeUtils.intersects(curve, elDD.getBounds())) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Returns the angles at which an edge is attached to a node. If the 
     * second parameter is non-null, only edges which are contained in it are
     * considered.
     * 
     * @param GraphDrawer
     *      graphical information about the objects
     * @param node
     *      the node of which the connection angles are to be determined
     * @param placed
     *      restriction to the considered edges
     * @return
     *      a sorted list of the angles at which edges are connected to the node
     */
    static List<Double> getAngles(
            GraphDrawer drawer, 
            VxNode node, 
            VxGroup placed)
    {
        List<Double> result = new ArrayList<>();
        for (VxEdge edge : node.outEdges) {
            EdgeDrawDelegate edgeDD = drawer.getDelegate(edge);
            if (placed == null || placed.getElements().contains(edge)) {
                result.add(edgeDD.getOutAngle());
            }
        }
        for (VxEdge edge : node.inEdges) {
            EdgeDrawDelegate edgeDD = drawer.getDelegate(edge);
            if (placed == null || placed.getElements().contains(edge)) {
                result.add(edgeDD.getInAngle());
            }
        }
        Collections.sort(result);
        return result;
    }  
    
    /**
     * Returns the largest angle interval where no edges are connected to the
     * given node. If the second parameter is non-null, only edges which 
     * have end points in it (including the given node) are considered.
     *
     * @param drawer
     *      graphical information about the objects
     * @param node
     *      the node of which to return the largest free block
     * @param placed
     *      restriction to the considered edges
     * @return
     *      the angle in the middle of the largest free interval without
     *      connected edges
     */
    static double largestFreeBlock(
            GraphDrawer drawer, 
            VxNode node, 
            VxGroup placed)
    {
        List<Double> cons = getAngles(drawer, node, placed);
        
        switch (cons.size()) {
            case 0:
                return 0;
            case 1:
                return GraphicsUtils.normalizeAngle(cons.get(0) + Math.PI);
            default:
                double start = cons.get(cons.size() - 1) - (Math.PI * 2);
                double end = cons.get(0);
                double size = -start + end;
                for (int i = 1; i < cons.size(); i++) {
                    double newStart = cons.get(i - 1);
                    double newEnd = cons.get(i);
                    double newSize = newEnd - newStart;
                    if (newSize > size) {
                        size = newSize;
                        start = newStart;
                        end = newEnd;
                    }
                }
                return (end + start) / 2;
        }
    }    
    
    static double nearestEdgeDistance(
            GraphDrawer drawer, 
            VxNode node, 
            double angle, 
            VxGroup placed)
    {
        List<Double> angles = getAngles(drawer, node, placed);
        double dist = Double.MAX_VALUE;
        for (double a : angles) {
            double ad = angleDistance(angle, a);
            if (Math.abs(ad) < Math.abs(dist)) {
                dist = ad;
            }
        }
        return dist;
    }    
    
    static double nearestFreeAngle(
            GraphDrawer drawer, 
            VxNode node, 
            double baseAngle, 
            double minDist, 
            VxGroup placed)
    {
        List<Double> angles = getAngles(drawer, node, placed);
        
        switch (angles.size()) {
            case 0: {
                return baseAngle;
            }
            case 1: {
                double dist = angleDistance(angles.get(0), baseAngle);
                if (Math.abs(dist) >= minDist) {
                    return baseAngle;
                }
                if (dist == 0) {
                    return GraphicsUtils.normalizeAngle(angles.get(0) + minDist);
                }
                else {
                    return GraphicsUtils.normalizeAngle(angles.get(0) + 
                            (Math.signum(dist) * minDist));
                }
            }
            default: {
                double nearestDist = Double.MAX_VALUE;
                double bestAngle = 0;

                for (int i = 0; i < angles.size(); i++) {
                    double d1 = (i == 0) ? 
                            angles.get(angles.size() - 1) - (Math.PI * 2): 
                            angles.get(i - 1);
                    double d2 = angles.get(i);
                    if ((d2 - d1) >= (2 * minDist)) {
                        double a1 = d1 + minDist;
                        double a2 = d2 - minDist;
                        if (baseAngle >= a1 && baseAngle <= a2) {
                            return baseAngle;
                        }
                        double dist1 = angleDistance(a1, baseAngle);
                        if (dist1 < nearestDist) {
                            bestAngle = a1;
                        }
                        double dist2 = angleDistance(a2, baseAngle);
                        if (dist2 < nearestDist) {
                            bestAngle = a2;
                        }
                    }
                }
                
                return bestAngle;
            }
        }
    }

	static boolean lineIntersectsEdges(
			GraphDrawer drawer,
			Line2D line,
			Collection<VxEdge> edges
	) {
		// FIXME: This is an approximation
		double[] coords = new double[6];
		for (VxEdge e : edges) {
			PathIterator pi = drawer.getDelegate(e).getLineShape().getPathIterator(null, 1.0);
			Point2D p1 = new Point2D.Double();
			Point2D p2 = p1;
			Point2D lastMove = p1;
			while (!pi.isDone()) {
				int segType = pi.currentSegment(coords);
				switch (segType) {
				case PathIterator.SEG_MOVETO:
					p2 = new Point2D.Double(coords[0], coords[1]);
					lastMove = p2;
					break;
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_CLOSE:
					p1 = p2;
					p2 = segType == PathIterator.SEG_LINETO
							? new Point2D.Double(coords[0], coords[1])
							: lastMove;
					Line2D seg = new Line2D.Double(p1, p2);
					if (seg.intersectsLine(line)) {
						return true;
					}
					break;
				}
				pi.next();
			}
		}
		return false;
	}

    /**
     * Tries to set the parameters of an edge in such a way, that the edge
     * runs through as little as possible nodes of the graph. The second
     * parameter can be used to restrict the part of the graph under
     * consideration.
     *
     * @param drawer
     *      graphical information about objects
     * @param edge
     *      the edge to change the parameters of
     * @param placed
     *      parts of the graph which have already been placed, or {@code null}
     *      if all of the graph's nodes and edges are to be considered.
     */
    static void setToOptimalPath(
            GraphDrawer drawer, 
            VxEdge edge, 
            VxGroup placed)
    {
        final double MIN_DIST = (Math.PI / 2) / (double)(angles.length + 2);
        final double RELATIVE_CP_DIST = 0.3;
        
        if (edge.isLoop()) {
            double angle = largestFreeBlock(drawer, edge.getSource(), placed);
            edge.setLoopAngle(angle);
            edge.setCurveIn(Math.PI / 18, .4);
            edge.setCurveOut(-Math.PI / 18, .4);
        }
        else {
            Collection<VxExplicitNode> obstacles = placed == null ? 
                    new ArrayList<>(edge.getGraph().getNodes()) :
                    new ArrayList<>(placed.getNodes());
            obstacles.remove(edge.getSource());
            obstacles.remove(edge.getTarget());
            
            Point2D srcPos = drawer.getDelegate(edge.getSource()).position();
            Point2D tgtPos = drawer.getDelegate(edge.getTarget()).position();

			// HACK XXX let's see if a straight line works and if yes, do a straight line
			Line2D straightCurve = new Line2D.Double(srcPos, tgtPos);
			int straightTouches = countTouches(drawer, straightCurve, obstacles);
			if (straightTouches == 0) {
				// next, check if any already-placed edge would collide with this line
				Collection<VxEdge> collEdges = placed == null ? edge.getGraph().getEdges() : placed.getEdges();
				if (!lineIntersectsEdges(drawer, straightCurve, collEdges)) {
					edge.setCurveInAngle(0.0);
					edge.setCurveOutAngle(0.0);
					return;
				}
			}

            double dx = tgtPos.getX() - srcPos.getX();
            double dy = tgtPos.getY() - srcPos.getY();
            double cpDist = RELATIVE_CP_DIST * srcPos.distance(tgtPos);
            double baseOut = Math.atan2(dy, dx);
            double baseIn = GraphicsUtils.normalizeAngle(baseOut + Math.PI);
            
            int minTouches = Integer.MAX_VALUE;
            double optOut = 0;
            double optIn = 0;
            
            for (double in : angles) {
                double out = -in;
                
                double absOut = GraphicsUtils.normalizeAngle(baseOut + out);
                double distOut = nearestEdgeDistance(drawer, edge.getSource(), absOut, placed);
                if (Math.abs(distOut) < MIN_DIST) continue;

                double absIn = GraphicsUtils.normalizeAngle(baseIn + in);
                double distIn = nearestEdgeDistance(drawer, edge.getTarget(), absIn, placed);
                if (Math.abs(distIn) < MIN_DIST) continue;

                double cp1x = srcPos.getX() + Math.cos(absOut) * cpDist;
                double cp1y = srcPos.getY() + Math.sin(absOut) * cpDist;
                double cp2x = tgtPos.getX() + Math.cos(absIn) * cpDist;
                double cp2y = tgtPos.getY() + Math.sin(absIn) * cpDist;

                CubicCurve2D curve = new CubicCurve2D.Double(
                        srcPos.getX(), srcPos.getY(),
                        cp1x, cp1y,
                        cp2x, cp2y,
                        tgtPos.getX(), tgtPos.getY());
                int touches = countTouches(drawer, curve, obstacles);

                if (touches < minTouches) {
                    minTouches = touches;
                    optOut = out;
                    optIn = in;
                    if (minTouches == 0) break; // good enough
                }
            }
            
            if (minTouches == Integer.MAX_VALUE) { // no free places found
                optOut = (Math.random() * Math.PI) - (Math.PI / 2);
                optIn = -optOut;
            }
            
            edge.setCurveIn(optIn, RELATIVE_CP_DIST * Math.max(0.8, Math.abs(optIn)));
            edge.setCurveOut(optOut, RELATIVE_CP_DIST * Math.max(0.8, Math.abs(optOut)));
        }
    }
    
    // ---------------------------------------------------------------------- //
    //                          Private utility methods                       // 
    // ---------------------------------------------------------------------- //
    
    private static double angleDistance(double a1, double a2)  
    {
        a1 = GraphicsUtils.normalizeAngle(a1);
        a2 = GraphicsUtils.normalizeAngle(a2);
        double dist = a2 - a1;
        if (Math.abs(dist) > Math.PI) {
            dist = -((Math.PI * 2) - dist);
        }
        return dist;
    }
}
