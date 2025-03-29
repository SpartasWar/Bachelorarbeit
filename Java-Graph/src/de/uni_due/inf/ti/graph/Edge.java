/* 
 * Copyright (c) 2012-2014, H.J. Sander Bruggink
 * Copyright (c) 2012-2014, University of Duisburg-Essen
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

package de.uni_due.inf.ti.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.StringUtils;

/**
 * This class represents a hyperedge in a graph.
 * <p>
 * The edge class does not contain a public constructor; edges are created
 * with the {@link Graph#addEdge} method.
 */
public final class Edge extends GraphElement
{
    // private int isoHash;
    
    private Label label;
    private List<Node> nodes;
    
    private static final String EXC_ARITY_MISMATCH      = "Arity mismatch (arity=%d, number of nodes=%d";
    private static final String EXC_INCIDENT_NULL       = "Incident nodes cannot be null.";
    private static final String EXC_INCIDENT_FOREIGN    = "One of the nodes does not belong to this graph.";
    private static final String EXC_NOT_BINARY          = "Method only available for binary edges.";
    
    
    /** 
     * Creates an edge.
     * The caller must disregard all references to the attached node array.
     * 
     * @param graph
     *      graph to which the new edge belongs
     * @param label
     *      label of the new edge
     * @param nodes
     *      incident nodes of the the new edge
     * @throws IllegalArgumentException
     *      if the label's arity different than the size of {@code nodes}, 
     *      or if one or more of the nodes in {@code nodes} belongs to a
     *      different graph than the new edge
     * @throws NullPointerException
     *      if {@code graph}, {@code label} or {@code nodes} is null or if
     *      {@code nodes} contains a {@code null} reference
     */
    public Edge(Graph graph, Label label, List<Node> nodes)
    {
        super(graph);
        
        if (nodes.size() != label.getArity()) {
            throw new IllegalArgumentException(
                    String.format(EXC_ARITY_MISMATCH, label.getArity(), nodes.size()));
        }
        
        for (Node n : nodes) {
            if (n == null)
                throw new NullPointerException(EXC_INCIDENT_NULL);
            if (n.getGraph() != graph)
                throw new IllegalArgumentException(EXC_INCIDENT_FOREIGN);
        }
        
        for (Node n : nodes) {
            n.incidentEdges++;
        }
        
        this.label = label;
        this.nodes = nodes;
    }
    
    /**
     * Returns the label of this edge.
     */
    @Pure
    public Label getLabel() 
    {
        return label;
    }
    
    /**
     * Returns the list of incident nodes of this edge.
     * The returned list unmodifiable.
     * 
     * @return
     *      an unmodifiable list of the nodes which are incident to this edge
     */
    @SideEffectFree
    public List<Node> getNodes()
    {
        return Collections.unmodifiableList(nodes);
    }
    
    /**
     * Returns the source node of this binary edge. The source node is the 
     * node connected to port 0.
     * 
     * @return
     *      the source node of this edge
     * @throws UnsupportedOperationException
     *      if this edge is not binary
     */
    @Pure
    public Node getSource()
    {
        if (nodes.size() != 2) {
            throw new UnsupportedOperationException(EXC_NOT_BINARY);
        }
        return nodes.get(0);
    }
    
    /**
     * Returns the target node of this binary edge. The target node is the 
     * node connected to port 1.
     * 
     * @return
     *      the target node of this edge
     * @throws UnsupportedOperationException
     *      if this edge is not binary
     */
    @Pure
    public Node getTarget()
    {
        if (nodes.size() != 2) {
            throw new UnsupportedOperationException(EXC_NOT_BINARY);
        }
        return nodes.get(1);
    }
    
    /**
     * Returns the arity of this edge. The arity of an edge is the number of
     * nodes which are incident to this edge. 
     * 
     * @return
     *      arity of this edge
     */
    @Pure
    public int getArity()
    {
        return nodes.size();
    }
    
    /**
     * Returns whether this is a binary edge. An edge is binary if it has
     * exactly two incident nodes. 
     * 
     * @return
     *      whether or not this is a binary edge
     */
    @Pure
    public boolean isBinary()
    {
        return nodes.size() == 2;
    }
    
    /**
     * {@inheritDoc}
     */
    @Pure
    public String toString() 
    {
        return String.format("%s(%s)", 
                label.getName(), 
                StringUtils.listString(nodes, ", "));
    }

    /**
     * Performs actions that must occur when this edge is removed from a graph.
     */
    @Override
    void removeFromGraph()
    {
        Graph graph = getGraph();
        if (graph != null) { // if graph == null, edge was already removed
            for (Node n : nodes) {
                assert (n.incidentEdges > 0);
                n.incidentEdges--;
            }
            /*
            for (Edge e : graph.edges) {
                if (e == this || !Collections.disjoint(e.nodes, nodes)) {
                    e.isoHash = 0;
                }
            }
            */
        
            super.removeFromGraph();
        }
    }
    
    /**
     * Returns the "isomorphism hash code" for this edge.
     * If two edges {@code e1} and {@code e2} have different isomorphism hash
     * codes, then they cannot be related by an isomorphism. 
     * (The converse does not hold: edges that cannot be related by an 
     * isomorphism may have the same isomorphism hash code.)
     * 
     * @return              
     *      the isomorphism hash code of this edge
     */
    @Override@Pure
    int getIsoHash() 
    {
        /* TODO: perhaps reenable caching of the isomorphism hash value.
         * Just caching it may result in it being outdated (it depends on the number
         * of incident edges on the connected nodes; so an edge's hash depends on
         * when it was created).
         * A direct consequence was that the code thought there was no isomorphism
         * between a graph and its .clone()
         */
        // if (isoHash == 0) {
        // do NOT overwrite the field! otherwise the isoHash gets corrupted if two threads try
        // to compute it at the same time.
        int isoHash;
            isoHash = 31 * nodes.size() + label.hashCode();
            for (Node n : nodes) {
                isoHash = 31 * isoHash + n.getIsoHash();
            }
        //}
        
        return isoHash;
    }
    
    // Note: Because there can be two or more *different* edges with the
    // same label and attached nodes, Object's equal and hashCode methods are
    // not overridden.
    
    /* ---------------------------------------------------------------------- *
     *                        Static utility functions                        *
     * ---------------------------------------------------------------------- */
    
    /**
     * Returns the border of a collection of edges. The border of a collection
     * of edges is the set of all nodes which are incident to one or more
     * edges in the collection.
     * 
     * @param edges         
     *      collection of edges of which the border is to be determined
     * @return
     *      the set of nodes which are incident to one or more edges in 
     *      {@code edges}
     * @throws NullPointerException 
     *      if {@code edges} is {@code null}
     */
    @Pure
    public static Set<Node> getBorder(Collection<Edge> edges)
    {
        Set<Node> border = new HashSet<>();
        for (Edge edge : edges) {
            border.addAll(edge.nodes);
        }
        return border;
    }
}
