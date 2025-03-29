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
import java.util.Collections;
import java.util.List;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

/**
 * Instances of this class represent a node a VxGraph.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class VxNode extends VxComponent
{    
    // Properties
    
    /** Label of this node. */
    private @Nullable String label;
    
    // Internal information
    
    /** List of edges having this node as target. */
    List<VxEdge> inEdges;
    
    /** List of edges having this node as source. */
    List<VxEdge> outEdges;
    
    /**
     * Creates a new node with a given center point, shape and label.
     * 
     * @param graph
     *      graph to which the new node belongs
     * @param label 
     *      label of the new node, or {@code null} if the new node does not
     *      have a label
     * @param style         
     *      style of the new node, or {@code null} if the new node uses the
     *      default style
     */
    VxNode( @NonNull VxGraph graph, 
            @Nullable String label, 
            @Nullable String style) 
    {
        super(graph, style);
        this.label = label;
        
        inEdges = new ArrayList<>();
        outEdges = new ArrayList<>();
    }
    
    /**
     * Returns the label of this node.
     */
    public final @Nullable String getLabel()
    {
        return label;
    }
    
    /**
     * Changes the label of this node.
     * If the label is set to {@code null}, this node will not have a label.
     * 
     * @param label
     *      new label of this VxNode, or {@code null} for no label
     */
    public final void setLabel(@Nullable String label) 
    {
        this.label = label;
        clearDelegateCaches();
        graph.fireStyleChanged(this);
    }
    
    /**
     * Returns the list of edges which have this node as target.
     */
    public final @NonNull List<VxEdge> getInEdges()
    {
        return Collections.unmodifiableList(inEdges);
    }

    /**
     * Returns the list of edges which have this node as source.
     */
    public final @NonNull List<VxEdge> getOutEdges()
    {
        return Collections.unmodifiableList(outEdges);
    }
}
