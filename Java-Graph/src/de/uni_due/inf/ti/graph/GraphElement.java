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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * This is the common superclass of parts of graphs, that is, of Node and
 * Edge.
 */
public abstract class GraphElement extends AttributeObject
{
    /**
     * Graph to which this GraphElement belongs.
     */
    private @Nullable Graph graph;
    
    /**
     * Creates a new GraphElement.
     * 
     * @param graph
     *      graph to which the new GraphElement belong.
     */
    @SideEffectFree
    protected GraphElement(Graph graph)
    {
        if (graph == null) 
            throw new NullPointerException("Graph cannot be null.");
        this.graph = graph;
    }

    /**
     * Performs action that must occur when this GraphElement is removed from
     * a graph. Classes that override this method must make sure to call the
     * overridden method.
     */
    void removeFromGraph() 
    {
        graph = null;
    }
    
    /**
     * Returns the graph to which this GraphElement belongs.
     * If this method returns {@code null}, this GraphElement has been removed
     * from the Graph it belonged to.
     * 
     * @return
     *      graph to which this GraphElement belongs
     */
    @Pure
    public final @Nullable Graph getGraph()
    {
        return graph;
    }
    
    /**
     * Returns the isomorphism hash code of this GraphElement.
     */
    @Pure
    abstract int getIsoHash();
}
