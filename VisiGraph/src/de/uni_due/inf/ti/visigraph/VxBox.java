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

/**
 * Class which can visually group nodes and edges of a VxGraph.
 * 
 * @author H.J. Sander Bruggink
 */
public final class VxBox extends VxElement
{    
    /** Label of this box. */
    private String label;

    /** Graph elements which belong to this box. */
    private VxGroup elements;
    
    /** Public view of the above group. */
    private VxGroup pubElements;
    
    /**
     * Creates a new VxBox instance. The caller must dispose of all references
     * to the object referenced by {@code elements}.  
     * 
     * @param graph
     *      the graph to which the new box belongs
     * @param label
     *      the label of the new box, or {@code null} if the box doesn't have
     *      a label
     * @param style
     *      the style of the new box
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    VxBox(VxGroup elements, String label, String style)
    {
        super(elements.getGraph(), style);
        if (style == null || label == null) {
            throw new NullPointerException();
        }
        this.label = label;
        this.pubElements = null;
        this.elements = elements;
    }
    
    
    /**
     * Returns the label of this box.
     */
    public String getLabel()
    {
        return label;
    }

    
    /**
     * Sets the label of this box.
     */
    public void setLabel(String label)
    {
        this.label = label;
        graph.fireStyleChanged(this);
    }
    
    
    /**
     * Adds a node to this box. If the node already belongs to a different 
     * box, it is first removed from it.
     * 
     * @param node
     *      node to be added to this box
     * @throws NullPointerException
     *      if {@code node} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code node} belongs to a different graph than this box
     */
    public void add(VxNode node)
    {
        if (node.graph != graph) {
            throw new IllegalArgumentException();
        }
        if (node.box != null) {
            node.box.elements.remove(node);
        }
        node.box = this;
        elements.add(node); 
    }
    
    
    /**
     * Adds a node to this box. If the edge already belongs to a different
     * box, it is first removed from it.
     * 
     * @param edge
     *      edge to be added to this box
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code edge} belongs to a different graph than this box
     */
    public void add(VxEdge edge)
    {
        if (edge.graph != graph) {
            throw new IllegalArgumentException();
        }
        if (edge.box != null) {
            edge.box.elements.remove(edge);
        }
        edge.box = this;
        elements.add(edge); 
    }
    
    
    /**
     * Adds a component to this box. If the component already belongs to a
     * different box, it is first removed from it.
     * 
     * @param el
     *      el to be added to this box
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code el} belongs to a different graph than this box
     */
    public void add(VxComponent el)
    {
        getGroup().add(el);
    }
    

    /**
     * Removes a node from this box.
     * 
     * @param node
     *      node to remove from this box
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code edge} belongs to a different graph than this box
     */
    public void remove(VxNode node)
    {
        if (node.graph != graph) {
            throw new IllegalArgumentException();
        }
        node.box = null;
        elements.remove(node);
    }

    
    /**
     * Removes an edge from this box.
     * 
     * @param edge
     *      edge to remove from this box
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code edge} belongs to a different graph than this box
     */
    public void remove(VxEdge edge)
    {
        if (edge.graph != graph) {
            throw new IllegalArgumentException();
        }
        edge.box = null;
        elements.remove(edge);
    }
    
    
    /**
     * Removes a component from this box.
     * 
     * @param el
     *      edge to remove from this box
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code el} belongs to a different graph than this box
     */
    public void remove(VxComponent el)
    {
        getGroup().remove(el);
    }

    
    /**
     * Returns the group which contains all nodes and edges in this box.
     * This group can be used to add nodes and edges to this box, and to remove
     * nodes and edges from this box.
     * 
     * @return
     *      group which contains all elements of this box
     */
    public VxGroup getGroup() 
    {
        if (pubElements == null) {
            pubElements = new VxGroup() {
                @Override
                public VxGraph getGraph()
                {
                    return graph;
                }

                @Override
                public Collection<VxExplicitNode> getNodes()
                {
                    return elements.getNodes();
                }

                @Override
                public Collection<VxEdge> getEdges()
                {
                    return elements.getEdges();
                }

                @Override
                public void clear()
                {
                    for (VxNode node : elements.getNodes()) {
                        node.box = null;
                    }
                    for (VxEdge edge : elements.getEdges()) {
                        edge.box = null;
                    }
                    elements.clear();
                }

                @Override
                public void add(VxExplicitNode node)
                {
                    VxBox.this.add(node);
                }

                @Override
                public void add(VxEdge edge)
                {
                    VxBox.this.add(edge);
                }

                @Override
                public void remove(VxExplicitNode el)
                {
                    VxBox.this.remove(el);
                }

                @Override
                public void remove(VxEdge el)
                {
                    VxBox.this.remove(el);
                }
            };
        }
        return pubElements;
    }
}
