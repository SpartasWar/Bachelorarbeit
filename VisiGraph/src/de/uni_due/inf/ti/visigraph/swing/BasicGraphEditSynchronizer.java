/* 
 * Copyright (c) 2014-2015, H.J. Sander Bruggink
 * Copyright (c) 2014, University of Duisburg-Essen
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

// 2014-05-?? - Initial version

package de.uni_due.inf.ti.visigraph.swing;

import java.awt.geom.Point2D;

import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxLabelNode;
import de.uni_due.inf.ti.visigraph.VxNode;

/**
 * GraphEditSynchronizer which uses {@link VxGraph} as the underlying model.
 * 
 * @author H.J. Sander Bruggink
 */
public class BasicGraphEditSynchronizer implements GraphEditSynchronizer
{
    // Graph characteristics
    private boolean edgesToLabels;
    private int maxLabels;
    
    // Behavior
    private boolean autoAddLabels;
    
    // Styles
    private String newNodeStyleName;
    private String newLabelStyleName;
    private String newEdgeStyleName;
        
    /**
     * Creates a new BasicGraphEditSynchronizer. 
     * The new BasicGraphEditSynchronizer does not allow edges to have edge
     * labels as source or target, and allows a single label per edge.
     */
    public BasicGraphEditSynchronizer()
    {
        this(false, 1);
    }
    
    /**
     * Creates a new BasicGraphEditSynchronizer with custom behavior.
     * 
     * @param edgesToLabels
     *      whether the new BasicGraphEditSynchronizer allows edges to have
     *      edge labels as source or target
     * @param maxLabels
     *      maximum number of label nodes on an edge; -1 means unlimited
     * @throws IllegalArgumentException
     *      if {@code maxLabels < -1}
     */
    public BasicGraphEditSynchronizer(boolean edgesToLabels, int maxLabels)
    {
        if (maxLabels < -1) throw new IllegalArgumentException("maxLabels must be >= -1");
        this.edgesToLabels = edgesToLabels;
        this.maxLabels = maxLabels;
        this.autoAddLabels = false;
    }
    
    /**
     * Returns the name of the style of nodes that are added to the graph.
     */
    public String getNewNodeStyleName()
    {
        return newNodeStyleName;
    }
    
    /**
     * Sets the name of the style of nodes that are added to the graph. 
     * 
     * @param style
     *      style of nodes added to the graph
     */
    public void setNewNodeStyleName(String style)
    {
        newNodeStyleName = style;
    }
    
    /**
     * Returns the name of the style of labels that are added to the graph.
     */
    public String getNewLabelStyleName()
    {
        return newLabelStyleName;
    }
    
    /**
     * Sets the style of labels that are added to the graph by the user.
     */
    public void setNewLabelStyleName(String style)
    {
        newLabelStyleName = style;
    }   
    
    /**
     * Returns the name of the style of edges that are added to the graph.
     */
    public String getNewEdgeStyleName()
    {
        return newEdgeStyleName;
    }
    
    /**
     * Sets the name of the style of edges that are added to the graph by this 
     * GraphEditor.
     */
    public void setNewEdgeStyleName(String style)
    {
        newEdgeStyleName = style;
    }   
    
    /**
     * Returns whether labels are automatically added to new edges.
     */
    public boolean getAutoAddLabels()
    {
        return autoAddLabels;
    }
    
    /**
     * Sets whether labels are automatically added to new edges.
     */
    public void setAutoAddLabels(boolean b)
    {
        autoAddLabels = b;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPossibleSourceNode(VxNode source)
    {
        if (edgesToLabels) {
            return true;
        }
        else {
            return !(source instanceof VxLabelNode);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation simply returns {@code true}.
     */
    @Override
    public boolean canAddNode(VxGraph graph)
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddEdge(VxNode source, VxNode target)
    {
        if (!edgesToLabels) {
            if (source instanceof VxLabelNode) return false;
            if (target instanceof VxLabelNode) return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddLabel(VxEdge edge)
    {
        int numOfLabels = edge.getLabels().size(); // throws NPE when edge == null
        return (maxLabels < 0 || numOfLabels + 1 <= maxLabels);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canEditLabel(VxNode node)
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(VxGraph graph, Point2D pt)
    {
        graph.addNode(pt).setStyle(newNodeStyleName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdge(VxNode source, VxNode target)
    {
        VxGraph graph = source.getGraph();
        VxEdge edge = graph.addEdge(source, target, newEdgeStyleName);
        if (autoAddLabels) {
            addLabel(edge, "");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addLabel(VxEdge edge, String label)
    {
        VxLabelNode ln = edge.addLabel(label);
        if (newLabelStyleName != null) {
            ln.setStyle(newLabelStyleName);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void editLabel(VxNode node, String newLabel)
    {
        node.setLabel(newLabel);
    }
}
