/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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

package de.uni_due.inf.ti.graph;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * This class represents a node in a graph.
 */
public final class Node extends GraphElement
{
    /** The number of edges that are incident to this node. */
    int incidentEdges;
    
    /** This node's unique id. This field is primarily used for user 
        interaction. */
    private int id;

    
    /**
     * Creates a new Node object with a given id number.
     * The caller must make sure each id is unique within a graph.
     * 
     * @param graph
     *      graph to which the new node belongs
     * @param id
     *      id number of the new node
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @SideEffectFree
	public Node(Graph graph, int id) 
    {
        super(graph);
        this.incidentEdges = 0;
        this.id = id;
    }
    
    /**
     * Returns this Node's id.
     */
    @Pure
    public int getId()
    {
        return id;
    }
    
    /**
     * Returns the isomorphism certificate of this node.
     * If two nodes {@code n1} and {@code n2} have different isomorphism hash
     * codes, then they cannot be related by an isomorphism. 
     * (The converse does not hold: nodes that cannot be related by an 
     * isomorphism may have the same isomorphism hash code.)
     * 
     * @return
     *      the isomorphism certificate of this node   
     */
    @Override@Pure
    int getIsoHash()
    {
        return incidentEdges * 11;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override@Pure
    public String toString()
    {
        return "" + id;
    }
}
