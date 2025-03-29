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

public class VxExplicitNode extends VxNode
{
    // Properties //
    
    /** X-coordinate of the center of this node. */
    private double midX;
    
    /** Y-coordinate of the center of this node. */
    private double midY;
    
    // Cached objects //
    
    /** Point instance which is a view of {@code midX} and {@code midY} and
        sends change notifications. */
    private Point2D center = null;
    
    /**
     * View on the center point of this node.
     */
    private class CenterPoint extends Point2D 
    {
        @Override
        public double getX()
        {
            return midX;
        }

        @Override
        public double getY()
        {
            return midY;
        }

        @Override
        public void setLocation(double x, double y)
        {
            if (x == midX && y == midY) return; // if nothing is changed, do nothing
            
            midX = x;
            midY = y;
            
            for (VxEdge edge : getGraph().getEdges()) {
                if (edge.getSource() == VxExplicitNode.this || 
                    edge.getTarget() == VxExplicitNode.this)
                    edge.notifyModification();
            }
            
            getGraph().fireElementMoved(VxExplicitNode.this);
        }
        
        public String toString()
        {
            return String.format("(%f,%f)", midX, midY);
        }
    }
    
    /**
     * Creates a new node with a given center point, shape and label.
     * 
     * @param midX          
     *      x-coordinate of the new node
     * @param midY          
     *      y-coordinate of the new node
     * @param label 
     *      label of the new node
     * @param style         
     *      style of the new node
     */
    VxExplicitNode(VxGraph graph, double midX, double midY, String label, String style) 
    {
        super(graph, label, style);
        this.midX = midX;
        this.midY = midY;
    }
    
    /**
     * Returns the position of this node. Changing the coordinates of the
     * returned object will move the node.
     */
    public Point2D position()
    {
        if (center == null) {
            center = new CenterPoint();
        }
        return center;
    }
    
    /**
     * Moves this node to a new location without sending a change event to 
     * listeners. 
     * 
     * @param x
     *      new x-coordinate of the node
     * @param y
     *      new y-coordinate of the node
     */
    void moveTo(double x, double y)
    {
        midX = x;
        midY = y;
    }
    
    /**
     * Moves this node to a new location without sending change event to 
     * listeners. 
     * 
     * @param pos
     *      new position of this node
     */
    void moveTo(Point2D pos)
    {
        midX = pos.getX();
        midY = pos.getY();
    }
}
