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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A group which contains an arbitrary number of elements.
 * 
 * @author H.J. Sander Bruggink
 */
class GeneralGroup extends VxGroup
{
    private VxGraph graph;
    private Set<VxExplicitNode> nodes;
    private Set<VxEdge> edges;
    private long lastDelNodes;
    private long lastDelEdges;
    
    /**
     * Creates a new empty GeneralGroup instance.
     */
    GeneralGroup(VxGraph graph)
    {
        this(graph, new HashSet<VxExplicitNode>(), new HashSet<VxEdge>());
    }
    
    /**
     * Creates a new GeneralGroup instance. The caller must throw away all 
     * references to the {@code nodes} and {@code edges} objects.
     */
    GeneralGroup(VxGraph graph, Set<VxExplicitNode> nodes, Set<VxEdge> edges)
    {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        
        lastDelNodes = graph.getDeletedNodesCount();
        lastDelEdges = graph.getDeletedEdgesCount();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VxGraph getGraph()
    {
        return graph;
    }

    /**
     * Returns the elements of this group. The returned collection is a newly 
     * created collection containing exactly all the nodes and edges in this 
     * group.
     */
    @Override
    public Collection<VxComponent> getElements()
    {
        Set<VxComponent> elements = new HashSet<>();
        elements.addAll(nodes);
        elements.addAll(edges);
        return elements;
    }
    
    /**
     * Returns the nodes of this group. The returned collection is unmodifiable.
     */
    public Collection<VxExplicitNode> getNodes()
    {
        if (graph.getDeletedNodesCount() > lastDelNodes) {
            for (Iterator<VxExplicitNode> it = nodes.iterator(); it.hasNext(); ) {
                VxNode node = it.next();
                if (node.graph == null) { // node was removed from the graph
                    it.remove();          // remove it from this group also
                }   
            }
        }
        return Collections.unmodifiableSet(nodes);
    }
    
    /**
     * Returns the edges of this group. The returned collection is unmodifiable.
     */
    public Collection<VxEdge> getEdges()
    {
        if (graph.getDeletedEdgesCount() > lastDelEdges) {
            for (Iterator<VxEdge> it = edges.iterator(); it.hasNext(); ) {
                VxEdge edge = it.next();
                if (edge.graph == null) { // edge was removed from the graph
                    it.remove();          // remove it from this group also
                }   
            }
        }
        return Collections.unmodifiableSet(edges);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        nodes.clear();
        edges.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(VxExplicitNode node) 
    {
        if (node.getGraph() != graph) {
            throw new IllegalArgumentException();
        }
        nodes.add((VxExplicitNode)node);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(VxEdge edge) 
    {
        if (edge.getGraph() != graph) {
            throw new IllegalArgumentException();
        }
        edges.add((VxEdge)edge);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(VxExplicitNode el) 
    {
        nodes.remove(el);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(VxEdge el) 
    {
        edges.remove(el);
    }
}
