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
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * Draw delegate for edges. This class draws edges and calculates their
 * dimensions.
 * 
 * @author H.J. Sander Bruggink
 */
class EdgeDrawDelegate extends DrawDelegate<VxEdge>
{
    private static final double EDGE_CLICK_SENSITIVITY = 5.0;

    /** Cached shape of the edge line. */
    private java.awt.Shape lineShape;
    
    /** Cached shape of the front arrow head. */
    private java.awt.Shape frontArrowShape;
    
    /** Cached shape of the back arrow head. */
    private java.awt.Shape backArrowShape;
    
    /**
     * Creates a new EdgeDrawDelegate instance.
     * 
     * @param edge
     *      edge of which is drawn by the new EdgeDrawDelegate
     * @param drawer
     *      GraphDrawer to which the new EdgeDrawDelegate belongs
     * @throws NullPointerException
     *      if {@code edge} or {@code drawer} is {@code null}
     */
    public EdgeDrawDelegate(VxEdge edge, GraphDrawer drawer)
    {
        super(edge, drawer);
        lineShape = null;
        frontArrowShape = null;
        backArrowShape = null;
    }
    
    /**
     * Returns the AWT shape which draw the edge line.
     */
    public java.awt.Shape getLineShape()
    {
        constructShapes();
        return lineShape;
    }
    
    /**
     * Returns the angle at which this edge leaves the source node.
     */
    public double getOutAngle()
    {
        VxEdge edge = getElement();
        double angle;
        if (edge.isLoop()) {
            angle = edge.getLoopAngle();
        }
        else {
            Point2D srcPos = getDrawer().getDelegate(edge.getSource()).position();
            Point2D tgtPos = getDrawer().getDelegate(edge.getTarget()).position();
            angle = Math.atan2(tgtPos.getY() - srcPos.getY(), tgtPos.getX() - srcPos.getX());
        }
        return GraphicsUtils.normalizeAngle(angle + edge.getCurveOutAngle());
    }
    
    /**
     * Returns the angle at which this edge enters the target node, relative to
     * the target node.
     */
    public double getInAngle()
    {
        VxEdge edge = getElement();
        double angle;
        if (edge.isLoop()) {
            angle = edge.getLoopAngle();
        }
        else {
            Point2D srcPos = getDrawer().getDelegate(edge.getSource()).position();
            Point2D tgtPos = getDrawer().getDelegate(edge.getTarget()).position();
            angle = Math.atan2(srcPos.getY() - tgtPos.getY(), srcPos.getX() - tgtPos.getX());
        }
        return GraphicsUtils.normalizeAngle(angle + edge.getCurveInAngle());
    }
    
    /**
     * Construct the shapes used to draw this arrow.
     */
    private void constructShapes()
    {
        if (lineShape != null) return; // shapes already constructed
       
        VxEdge edge = getElement();
        VxNode source = edge.getSource();
        NodeDrawDelegate sourceDD = getDrawer().getDelegate(source);
        VxNode target = edge.getTarget();
        NodeDrawDelegate targetDD = getDrawer().getDelegate(target);
        
        double dist;
        double coa = edge.getCurveOutAngle();
        double cia = edge.getCurveInAngle();
        
        if (edge.isLoop()) {
            if (Math.abs(coa) < Math.PI / 18) {
                coa = Math.signum(coa) * Math.PI / 18;
                edge.setCurveOut(coa, edge.getCurveOutStrength());
            }
            if (Math.abs(cia) < Math.PI / 18) {
                cia = Math.signum(cia) * Math.PI / 18;
                edge.setCurveIn(cia, edge.getCurveInStrength());
            }
            dist = 100;
        }
        else {
            dist = sourceDD.position().distance(targetDD.position());
        }

        double outAngle = getOutAngle();
        double inAngle = getInAngle();
        Point2D srcPos = sourceDD.getBorderPoint(outAngle);
        Point2D tgtPos = targetDD.getBorderPoint(inAngle);
        
        // create line shape
        if (cia == 0 && coa == 0) {
            lineShape = new Line2D.Double(srcPos.getX(), srcPos.getY(), tgtPos.getX(), tgtPos.getY());
        }
        else {
            double outStrength = edge.getCurveOutStrength() * dist;
            double cp1x = srcPos.getX() + Math.cos(outAngle) * outStrength;
            double cp1y = srcPos.getY() + Math.sin(outAngle) * outStrength;
            double inStrength = edge.getCurveInStrength() * dist;
            double cp2x = tgtPos.getX() + Math.cos(inAngle) * inStrength;
            double cp2y = tgtPos.getY() + Math.sin(inAngle) * inStrength;
            lineShape = new CubicCurve2D.Double(
                    srcPos.getX(), srcPos.getY(),
                    cp1x, cp1y,
                    cp2x, cp2y,
                    tgtPos.getX(), tgtPos.getY());
        }
        
        Style style = getStyle();
        
        ArrowHead fwdHead = style.getFrontArrow();
        if (fwdHead != null) {
            frontArrowShape = fwdHead.getShape(tgtPos, 
                    GraphicsUtils.normalizeAngle(inAngle + Math.PI));
        }
        
        ArrowHead bwdHead = style.getBackArrow();
        if (bwdHead != null) {
            backArrowShape = bwdHead.getShape(srcPos,                     
                    GraphicsUtils.normalizeAngle(outAngle + Math.PI));
        }

    }
    
    /**
     * Get a point on the edge at a given relative distance.
     * The relative distance 0 is closest to the source and 1 is closest to the
     * target.
     *
     * @param t
     *      the relative distance of the required point on the edge.
     * @return
     *      the point on the edge at relative distance t
     * @throws IllegalArgumentException
     *      if {@code t < 0.0} or {@code t > 1.0}
     */
    public Point2D getEdgePoint(double t) 
    {
        if (t < 0.0 || t > 1.0) { 
            throw new IllegalArgumentException();
        }

        constructShapes();
        return ShapeUtils.getPointOnCurve(t, lineShape);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(Graphics2D g, boolean selected)
    {
        Style style = getStyle();
        constructShapes();
        
        Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();

        g.setColor(style.getLineColor(selected));
        g.setStroke(style.getStroke(selected));
        g.draw(lineShape);
        g.setStroke(oldStroke);
        
        if (frontArrowShape != null) {
            if (style.getFrontArrow().isFilled()) g.fill(frontArrowShape);
            g.draw(frontArrowShape);
        }
        
        if (backArrowShape != null) {
            if (style.getBackArrow().isFilled()) g.fill(backArrowShape);
            g.draw(backArrowShape);
        }
        
        g.setColor(oldColor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getBounds(Rectangle2D buf)
    {
        if (buf == null) buf = new Rectangle2D.Double();
        
        constructShapes();
        buf.setRect(lineShape.getBounds2D());
        if (frontArrowShape != null) buf.add(frontArrowShape.getBounds2D());
        if (backArrowShape != null) buf.add(backArrowShape.getBounds2D());
        
        for (VxLabelNode label : getElement().getLabels()) {
            NodeDrawDelegate nodeDD = getDrawer().getDelegate(label);
            buf.add(nodeDD.getBounds());
        }
        
        return buf;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Point2D point)
    {
        constructShapes();
        
        if (ShapeUtils.distanceToCurve(point, lineShape) < EDGE_CLICK_SENSITIVITY) {
            return true;
        }

        for (VxLabelNode label : getElement().getLabels()) {
            NodeDrawDelegate labelDD = getDrawer().getDelegate(label);
            if (labelDD.contains(point)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    void clearCache()
    {
        lineShape = null;
        frontArrowShape = null;
        backArrowShape = null;
    }
}
