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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates edges in a VxGraph.
 * 
 * @author H.J. Sander Bruggink
 */
public class VxEdge extends VxComponent
{
    // Constants
    
    /** Default distance of label nodes to the edge line. */
    private static final double LABEL_DISTANCE = 8;
    
    // Properties //

    /** Source node of this edge. */
    private VxNode source;
    
    /** Target node of this edge. */
    private VxNode target;
    
    /** If this edge is a loop, this member specifies the angle the center of
        the loop has with respect to the node. */
    private double loopAngle = 0.0;
        
    /** Relative angle of the outgoing curve control point. */
    private double curveOutAngle;
    
    /** Relative angle of the incoming curve control point. */
    private double curveInAngle;

    /** The relative distance to the center of the source node of the outgoing
        curve control point. 1.0 = straight line distance between source and
        target. */
    private double curveOutStrength;

    /** The relative distance to the center of the source node of the incoming
        curve control point. 1.0 = straight line distance between source and
        target. */
    private double curveInStrength;
    
    /** List of labels of this edge. */
    private List<VxLabelNode> labels;
    
    
    /**
     * Creates a VxEdge with a label.
     * If <i>source</i> and <i>target</i> are the same node, the edge is 
     * initially curved, otherwise it is initially straight.
     *  
     * @param source
     *      source of the new VxEdge
     * @param target
     *      target of the new VxEdge
     * @param curveOutAngle
     *      relative angle of the first control point of the new edge's curve
     * @param curveOutDistance
     *      relative distance of the first control point of the new edge's curve
     * @param curveInAngle
     *      relative angle of the second control point of the new edge's curve
     * @param curveInDistance
     *      relative distance of the second control point of the new edge's
     *      curve
     * @param loopAngle
     *      base angle if the new edge is a loop
     * @throws IllegalArgumentException
     *      if <i>source</i> and <i>target</i> belong to a different 
     *      VxGraph
     * @throws NullPointerException
     *      if <i>style</i> is <i>null</i>
     */
    VxEdge(VxNode source, VxNode target, double curveOutAngle, 
            double curveOutDistance, double curveInAngle, 
            double curveInDistance, double loopAngle, String style)
    {
        super(source.getGraph(), style);
        if (source.getGraph() != target.getGraph()) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.target = target;
        this.curveInAngle = curveInAngle;
        this.curveInStrength = curveInDistance;
        this.curveOutAngle = curveOutAngle;
        this.curveOutStrength = curveOutDistance;
        this.loopAngle = loopAngle;
    }  
    
    /**
     * Returns the source node of this edge.
     */
    public VxNode getSource()
    {
        return source;
    }   
    
    /**
     * Returns the target node of this edge.
     */
    public VxNode getTarget()
    {
        return target;
    }   
    
    /**
     * Returns the labels of this edge. 
     * This method always a returns an non-null, unmodifiable collection.
     *  
     * @return
     *      the collection of labels of this edge
     */
    public Collection<VxLabelNode> getLabels()
    {
        if (labels == null) labels = new ArrayList<>();
        return Collections.unmodifiableCollection(labels);
    }   
    
    /**
     * Sets the  first control point of this edge's curve.
     * <p>
     * In order to make the curve more or less robust with respect to moving
     * nodes, the control point is set relative to the straight line segment 
     * from the source node to the target node, that is, the angle specified is
     * relative to this line, and the distance is relative to this line
     * segment's length.
     * <p>
     * If this edge is a loop, the base angle relative to which the control
     * points angle is set is specified by the user with the 
     * {@link #setLoopAngle(double) setLoopAngle} method, and a distance of
     * 1.0 corresponds to 100. 
     * 
     * @param relAngle
     *      angle of the first control point of this edge's curve
     * @param relDist
     *      distance of the first control point from the source node's center
     *      point
     */
    public void setCurveOut(double relAngle, double relDist)
    {
        curveOutAngle = relAngle;
        curveOutStrength = relDist;
        getGraph().fireElementMoved(this);
    }   
    
    /**
     * Returns the relative angle of the first control point of this edge's 
     * curve.
     */
    public double getCurveOutAngle() 
    {
        return curveOutAngle;
    }
       
    /**
     * Sets the relative angle of the first control point of this edge's 
     * curve.
     */    
    public void setCurveOutAngle(double angle)
    {
        curveOutAngle = angle;
        getGraph().fireElementMoved(this);
    }   
    
    /**
     * Returns the relative distance of the first control point of this 
     * edge's curve.
     */
    public double getCurveOutStrength()
    {
        return curveOutStrength;
    }   
    
    /**
     * Sets the relative distance of the first control point of this 
     * edge's curve.
     */
    public void setCurveOutStrength(double strength)
    {
        curveOutStrength = strength;
        getGraph().fireElementMoved(this);
    }
        
    /**
     * Sets the second control point of this edge's curve.
     * <p>
     * In order to make the curve more or less robust with respect to moving
     * nodes, the control point is set relative to the straight line segment 
     * from the target node to the source node, that is, the angle specified is
     * relative to this line, and the distance is relative to this line
     * segment's length.
     * <p>
     * If this edge is a loop, the base angle relative to which the control
     * points angle is set is specified by the user with the 
     * {@link #setLoopAngle(double) setLoopAngle} method, and a distance of
     * 1.0 corresponds to 100. 
     * 
     * @param relAngle
     *      angle of the first control point of this edge's curve
     * @param relDist
     *      distance of the first control point from the target node's center
     *      point
     */
    public void setCurveIn(double relAngle, double relDist)
    {
        curveInAngle = relAngle;
        curveInStrength = relDist;
        getGraph().fireElementMoved(this);
    }
     
    /**
     * Returns the relative angle of the second control point of this edge's 
     * curve.
     */
    public double getCurveInAngle() 
    {
        return curveInAngle;
    }   
    
    /**
     * Sets the angle of the second control point of this edge's curve.
     */
    public void setCurveInAngle(double angle)
    {
        curveInAngle = angle;
        getGraph().fireElementMoved(this);
    }   
    
    /**
     * Returns the relative distance of the second control point of this 
     * edge's curve.
     */
    public double getCurveInStrength()
    {
        return curveInStrength;
    }
       
    /**
     * Sets the relative distance of the second control point of this 
     * edge's curve.
     */
    public void setCurveInStrength(double strength)
    {
        curveInStrength = strength;
        getGraph().fireElementMoved(this);
    }
    
    /**
     * Sets the loop angle of this edge. If this edge is not a loop, the
     * loop angle has no effect.
     * 
     * @param angle
     *      the new loop angle of this edge
     */
    public void setLoopAngle(double angle)
    {
        loopAngle = angle;
        if (isLoop()) {
            getGraph().fireElementMoved(this);
        }
    }   

    /**
     * Returns the loop angle of this edge.
     */
    public double getLoopAngle()
    {
        return loopAngle;
    }   
    
    /**
     * Determines if this edge is a loop. An edge is a loop if the source and
     * target are the same node.
     * 
     * @return
     *      {@code true} if this edge is a loop
     */
    public boolean isLoop()
    {
        return source == target;
    }
       
    /**
     * This method is called when one of the incident nodes of this edge
     * changed position.
     */
    public void notifyModification()
    {
        clearDelegateCaches();
        
        VxGraph graph = getGraph();
        graph.startModification();
        graph.fireElementMoved(this);
        if (labels != null) {
            for (VxLabelNode label : labels) {
                label.notifyModification();
            }
        }
        graph.endModification();
    }
    
    /**
     * Adds a label to this edge. A label is a node whose position is 
     * determined by the edge. 
     * <p>
     * Additionally, all label nodes on this edge are repositioned so that
     * they have uniform distances to each other. Consequently, any custom
     * positioning of label nodes must be done after the last call to this
     * method.
     * <p>
     * The created VxLabelNode instance is returned, and can be used to change
     * properties of the label, such as the distance to the edge, the node
     * style, etc.
     *  
     * @param label
     *      string of the new label
     * @return
     *      the VxLabelNode instance of the added label
     */
    public VxLabelNode addLabel(String label)
    {
        VxLabelNode labelNode = new VxLabelNode(
                this, 
                label, 
                null, 
                -1, 
                LABEL_DISTANCE, 
                VxLabelNode.LabelSide.OUTSIDE);
        if (labels == null) labels = new ArrayList<>();
        labels.add(labelNode);
        
        double perLabel = 1.0 / (double)(labels.size() + 1);
        
        for (int i = 0; i < labels.size(); i++) {
            VxLabelNode node = labels.get(i);
            node.setPositionOnEdge((double)(i + 1) * perLabel);
        }
        
        return labelNode;
    }
       
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("(%s->%s)", 
                source.getLabel(), target.getLabel());
    }   
}
