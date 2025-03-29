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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.uni_due.inf.ti.visigraph.VxLabelNode.LabelSide;

/**
 * Draw delegate for nodes. This class draws nodes and calculates their 
 * dimensions.
 * 
 * @author H.J. Sander Bruggink
 */
public class NodeDrawDelegate extends DrawDelegate<VxNode>
{
    /** For label nodes: temporary storage of the side of the label for
        "non-curved" edges. */
    private int realSide = 0;
    
    /** Cached shape. */
    private Shape realShape = null;
    
    /** Label used when cached shape was formed. */
    private String realShapeLabel = null;

    
    /**
     * Creates a new NodeDrawDelegate.
     */
    public NodeDrawDelegate(VxNode node, GraphDrawer drawer)
    {
        super(node, drawer);
    }
    
    
    /**
     * Returns the position of this NodeDrawDelegate's node.
     */
    public Point2D position()
    {
        VxNode node = getElement();
        
        if (node instanceof VxExplicitNode) {
            return ((VxExplicitNode)node).position();
        }
        else {
            assert node instanceof VxLabelNode;
            VxLabelNode label = (VxLabelNode)node;
            VxEdge edge = label.getEdge();
            EdgeDrawDelegate edgeDD = getDrawer().getDelegate(edge);
            LabelSide side = label.getSide();
            
            Point2D result = null;
            
            switch (side) {
                case ONTOP: {
                    result = edgeDD.getEdgePoint(label.getPositionOnEdge());
                    break;
                }
                case LEFT:
                case RIGHT: {
                    double rpos = label.getPositionOnEdge();
                    if (rpos < .05) rpos = .05;
                    if (rpos > .95) rpos = .95;
                    Point2D mid  = edgeDD.getEdgePoint(rpos);
                    Point2D p1   = edgeDD.getEdgePoint(rpos - .05);
                    Point2D p2   = edgeDD.getEdgePoint(rpos + .05);
                    double mang = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
                    if (side == LabelSide.LEFT) {
                        mang -= Math.PI / 2;
                    }
                    else {
                        mang += Math.PI / 2;
                    }
                    double d = label.getDistance();
                    result = new Point2D.Double(
                                    mid.getX() + Math.cos(mang) * d,
                                    mid.getY() + Math.sin(mang) * d);
                    break;
                }
                case INSIDE:
                case OUTSIDE: {
                    double rpos = label.getPositionOnEdge();
                    if (rpos < .05) rpos = .05;
                    if (rpos > .95) rpos = .95;
                    Point2D mid  = edgeDD.getEdgePoint(rpos);
                    Point2D p1   = edgeDD.getEdgePoint(rpos - .05);
                    Point2D p2   = edgeDD.getEdgePoint(rpos + .05);
                    Point2D imid = new Point2D.Double(
                        p1.getX() + (p2.getX() - p1.getX()) / 2,
                        p1.getY() + (p2.getY() - p1.getY()) / 2
                    );
                    double mang;
                    if (Math.hypot(imid.getX() - mid.getX(), imid.getY() - mid.getY()) < 0.001) {
                        mang = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
                        if (realSide == 0) {
                            realSide = (Math.random() > 0.5) ? 1 : -1;
                        }
                        if (realSide == 1) mang -= Math.PI / 2;
                                      else mang += Math.PI / 2;
                    }
                    else {
                        mang = Math.atan2(mid.getY() - imid.getY(), mid.getX() - imid.getX());
                    }
                    double d = label.getDistance();
                    result = new Point2D.Double(
                            mid.getX() + Math.cos(mang) * d,
                            mid.getY() + Math.sin(mang) * d);
                    break;
                }
                default:
                    assert false;
                    return null;
            }
            
            assert result != null;
            label.lastPoint = result;
            return result;
        }
    }
    

    /**
     * Returns the shape of the node drawn by this NodeDrawDelegate.
     * Different than the shape returned by {@code getStyle().getShape()},
     * the shape has been enlarged to completely encompass the label if 
     * necessary.
     * 
     * @return
     *      the shape of the node drawn by this NodeDrawDelegate
     */
    public Shape getRealShape()
    {
        Style style = getStyle();
        String label = getElement().getLabel();
        
        if (!style.hasFlexibleShape() || label == null || label.isEmpty()) {
            realShape = null;
            realShapeLabel = null;
            return style.getShape();
        }
        else {
            if (realShape == null || !label.equals(realShapeLabel)) {
                Font font = style.getLabelFont();
                Shape shape = style.getShape();
                FontRenderContext frc = new FontRenderContext(font.getTransform(), true, true);
                Rectangle2D labelBounds = font.getStringBounds(label, frc);
                realShape = shape.getEnlargement(labelBounds.getWidth() + 5, labelBounds.getHeight() + 5);
                realShapeLabel = label;
            }                        
            return realShape;
        }
    }
    
    
    /**
     * Returns a point on the border of the node drawn by this DrawDelegate. 
     * 
     * @param direction
     *      direction of the border point relative to the center of the node,
     *      in radians
     * @param result
     *      buffer object for storing the result
     * @return
     *      point on the border of the node
     * @throws NullPointerException
     *      if {@code node} is {@code null} 
     */
    public Point2D getBorderPoint(double direction, Point2D result) 
    {
        Style style = getStyle();
        double lineWidth = style.getStroke().getLineWidth();
        Shape shape = getRealShape();
        Point2D border = shape.getBorderPoint(direction, lineWidth / 2, result);
        double x = border.getX();
        double y = border.getY();
        Point2D center = position();
        border.setLocation(center.getX() + x, center.getY() + y);
        return border;       
    }

    
    /**
     * Returns a point on the border of the node drawn by this DrawDelegate. 
     * 
     * @param direction
     *      direction of the border point relative to the center of the node,
     *      in radians
     * @return
     *      point on the border of the node
     * @throws NullPointerException
     *      if {@code node} is {@code null} 
     */
    public Point2D getBorderPoint(double direction) 
    {
        return getBorderPoint(direction, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(Graphics2D g, boolean selected)
    {
        VxNode node = getElement();
        Style style = getStyle();
        Shape shape = getRealShape();
        
        Color lineColor = style.isDrawn(selected) ? style.getLineColor(selected) : null;
        Color fillColor = style.isFilled(selected) ? style.getFillColor(selected) : null;
        Stroke stroke = style.getStroke(selected);
        boolean doubleLine = style.getDoubleLine(selected);

        Stroke oldStroke = g.getStroke();
        g.setStroke(stroke);
        shape.draw(g, position(), lineColor, fillColor, doubleLine);
        g.setStroke(oldStroke);
        
        String label = node.getLabel();
        if (label != null && !label.isEmpty()) {
            Color oldColor = g.getColor();
            Font oldFont = g.getFont();
            g.setColor(style.getLabelColor(selected));
            g.setFont(style.getLabelFont());
            Utils.drawLabel(g, position(), label);
            g.setColor(oldColor);
            g.setFont(oldFont);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getBounds(Rectangle2D buf)
    {
        Shape shape = getRealShape();
        Rectangle2D rect = shape.getBounds(position());
        
        if (buf == null) {
            return rect;
        }
        else {
            buf.setRect(rect);
            return buf;
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override 
    public Rectangle2D getBounds() 
    {
        Shape shape = getRealShape();
        return shape.getBounds(position());        
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Point2D point)
    {
        Shape shape = getRealShape();
        Point2D np = position();
        return shape.contains(point.getX() - np.getX(), point.getY() - np.getY());
    }
}
