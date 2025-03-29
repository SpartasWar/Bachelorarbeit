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

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

/**
 * A VxNode subclass which implements nodes that act as the label of an edge.
 * 
 * @author H.J. Sander Bruggink
 */
public class VxLabelNode extends VxNode
{
    public enum LabelSide {
        OUTSIDE,
        INSIDE,
        LEFT,
        RIGHT,
        ONTOP
    }    
    
    private VxEdge edge;
    private double pos;
    private double distance;
    private @Nullable LabelSide side;
    
    Point2D lastPoint;

    /**
     * Creates a new VxLabelNode.
     * 
     * @param edge
     *      edge of which the new VxLabelNode is a label
     * @param label
     *      string label of the new node
     * @param style
     *      node style of the new node
     * @param pos
     *      position on the edge; this can be any double between 0.0 and 1.0,
     *      where 0.0 = at source and 1.0 = at target
     * @param distance
     *      distance of the node center to the edge line
     * @param side
     *      side of the edge line on which the label is placed
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     */
    VxLabelNode(
            @NonNull VxEdge edge, 
            @Nullable String label, 
            @Nullable String style, 
            double pos, 
            double distance, 
            @NonNull LabelSide side)
    {
        super(edge.getGraph(), label, style);
        
        this.edge = edge;
        this.pos = pos;
        this.distance = distance;
        this.side = side;
    }
    
    /**
     * Returns the edge of which this node is a label.
     */
    public @NonNull VxEdge getEdge()
    {
        return edge;
    }
    
    /**
     * Returns the position on the edge of this node. 
     * The returned value lies between 0.0 and 1.0, where 0.0 means that the
     * node is placed at the source of the edge and 1.0 means that the node
     * is placed at the target of the edge.
     * 
     * @return
     *      position on the edge of this node
     */
    public double getPositionOnEdge()
    {
        return pos;
    }
    
    /**
     * Sets the position on the edge of this node. 
     * The position is a value between 0.0 and 1.0, where 0.0 means that the
     * node is placed at the source of the edge and 1.0 means that the node
     * is placed at the target of the edge. If a number outside of this interval
     * is given, the position is obtained from the edge's style.
     * 
     * @param pos
     *      position on the edge of this node
     * @throws IllegalArgumentException
     *      if {@code pos} is not a number between 0.0 and 1.0 (inclusive)
     */
    public void setPositionOnEdge(double pos)
    {
        if (pos < 0.0 || pos > 1.0) throw new IllegalArgumentException();
        this.pos = pos;
        lastPoint = null;
        getGraph().fireElementMoved(this);
    }
    
    /**
     * Returns the distance of the node center to the edge line.
     */
    public double getDistance()
    {
        return distance;
    }
    
    /**
     * Sets the distance of this node's center to the edge line.
     * 
     * @param distance
     *      new distance of this label's center to the edge line
     * @throws IllegalArgumentException
     *      if {@code distance < 0}
     */
    public void setDistance(double distance)
    {
        if (distance < 0) throw new IllegalArgumentException();
        this.distance = distance;
        lastPoint = null;
        getGraph().fireElementMoved(this);
    }
    
    /**
     * Returns the side on which this label is placed on the edge.
     */
    public @NonNull LabelSide getSide()
    {
        return side;
    }
    
    /**
     * Sets the side on which this label is placed on the edge.
     */
    public void setSide(@NonNull LabelSide side)
    {
        this.side = side;
        lastPoint = null;
        getGraph().fireElementMoved(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyModification()
    {
        getGraph().fireElementMoved(this);
    }
}
