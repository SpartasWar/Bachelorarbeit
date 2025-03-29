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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents a (visual representation of a) graph.
 * 
 * @author H.J. Sander Bruggink
 */
public class VxGraph
{
    private static final double INITIAL_CP_DISTANCE = 0.4;
    private static final double RANDOM_POS_WIDTH    = 500;
    private static final double RANDOM_POS_HEIGHT   = 500;
    
    private long delNodeCount = 0;
    private long delEdgeCount = 0;
    
    private Set<VxExplicitNode> nodes;
    private Set<VxEdge> edges;
    private List<VxBox> boxes;
    
    private List<GraphListener> graphListeners = null;
    private VxGroup allGroup = null;
    
    private int modificationLevel = 0;
    
    private List<VxElement> addedElements = null;
    private List<VxElement> removedElements = null;
    private List<VxElement> movedElements = null;
    private List<VxElement> changedElements = null;
    
    private Object reference = null;
    
    /**
     * Creates a new VxGraph instance.
     */
    public VxGraph()
    {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        boxes = new ArrayList<>();
    }
    
    
    /**
     * Returns the reference object of this graph. The reference object is
     * a user-defined object which is used to establish a connection between
     * the graph and the underlying data. It is not used by any of the 
     * VisiGraph classes.
     * 
     * @return
     *      the reference object of this graph; or {@code null} if this 
     *      graph doesn't have an associated reference object
     */
    public Object getReference()
    {
        return reference;
    }
    
    
    /**
     * Sets the reference object of this graph. The reference object is
     * a user-defined object which is used to establish a connection between
     * the graph and the underlying data. It is not used by any of the 
     * VisiGraph classes.
     * 
     * @param ref
     *      the reference object of this graph
     */
    public void setReference(Object ref)
    {
        reference = ref;
    }
    
    
    /**
     * Returns the number of nodes that were deleted from this graph since it
     * was created. This statistic is used by groups to lazily update their 
     * element collection.
     */
    long getDeletedNodesCount()
    {
        return delNodeCount;
    }
    
    
    /**
     * Returns the number of edge that were deleted from this graph since it
     * was created. This statistic is used by groups to lazily update their
     * element collection.
     */
    long getDeletedEdgesCount()
    {
        return delEdgeCount;
    }
    

    /**
     * Returns whether this graph is empty. A graph empty if it doesn't contain
     * any nodes and edges.
     * 
     * @return
     *      {@code true} if this graph is empty, {@code false} otherwise
     */
    public boolean isEmpty()
    {
        return nodes.isEmpty() && edges.isEmpty();
    }
    
    
    /**
     * Adds a new node with a label and a custom style to this graph.
     * 
     * @param x
     *      x-coordinate of the new node
     * @param y
     *      y-coordinate of the new node
     * @param label
     *      label of the new node
     * @param style
     *      style of the new node
     * @return
     *      node which was added to this graph
     */
    public VxExplicitNode addNode(double x, double y, String label, String style)
    {
        VxExplicitNode node = new VxExplicitNode(this, x, y, label, style);
        nodes.add(node);
        fireElementAdded(node);
        return node;
    }

    
    /**
     * Adds a new node with a label and a custom style to this graph.
     * 
     * @param pos
     *      position of the new node
     * @param label
     *      label of the new node
     * @param style
     *      style of the new node
     * @return
     *      node which was added to this graph
     * @throws NullPointerException
     *      when {@code pos} is {@code null}
     */
    public VxExplicitNode addNode(Point2D pos, String label, String style)
    {
        return this.addNode(pos.getX(), pos.getY(), label, style);
    }

    
    /**
     * Adds a new node to this graph. The new node has no label and the 
     * default node style.
     * 
     * @param x
     *      x-coordinate of the new node
     * @param y
     *      y-coordinate of the new node
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode(double x, double y)
    {
        return addNode(x, y, null, null);
    }

    
    /**
     * Adds a new node to this graph. The new node has no label and the 
     * default node style.
     * 
     * @param pos
     *      position of the new node
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode(Point2D pos)
    {
        return addNode(pos.getX(), pos.getY(), null, null);
    }

    
    /**
     * Adds a new node with a label to this graph. The new node has the
     * default node style.
     * 
     * @param x
     *      x-coordinate of the new node
     * @param y
     *      y-coordinate of the new node
     * @param label
     *      label of the new node
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode(double x, double y, String label)
    {
        return addNode(x, y, label, null);
    }

    
    /**
     * Adds a new node with a label to this graph. The new node has the
     * default node style.
     * 
     * @param pos
     *      position of the new node
     * @param label
     *      label of the new node
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode(Point2D pos, String label)
    {
        return addNode(pos.getX(), pos.getY(), label, null);
    }

    
    /**
     * Adds a new node with a label and a custom style to this graph at a 
     * random position.
     * 
     * @param label
     *      label of the new node
     * @param style
     *      style of the new node
     * @return
     *      node which was added to this graph
     * @throws NullPointerException
     *      when <em>style</em> or <em>pos</em> is <em>null</em>
     */
    public VxExplicitNode addNode(String label, String style)
    {
        return addNode(
                Math.random() * RANDOM_POS_WIDTH, 
                Math.random() * RANDOM_POS_HEIGHT,
                label, 
                style);
    }

    
    /**
     * Adds a new node to this graph at a random position. 
     * The new node has no label and the default node style.
     *
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode()
    {
        return addNode(
                Math.random() * RANDOM_POS_WIDTH, 
                Math.random() * RANDOM_POS_HEIGHT,
                null, 
                null);
    }

    
    /**
     * Adds a new node with a label to this graph at a random location. 
     * The new node has the default node style.
     * 
     * @param label
     *      label of the new node
     * @return
     *      the node which was added to this graph
     * @see GraphDrawer#setDefaultNodeStyle(Style)
     */
    public VxExplicitNode addNode(String label)
    {
        return addNode(
                Math.random() * RANDOM_POS_WIDTH, 
                Math.random() * RANDOM_POS_HEIGHT,
                label, 
                null);
    }

    
    /**
     * Removes a node from this graph. Before the node is removed, all edges
     * which are incident to the node are also removed.
     * 
     * @param node
     *      node which is to be removed from this graph
     * @throws NullPointerException
     *      if <em>node</em> is null
     * @throws IllegalArgumentException
     *      if <em>node</em> doesn't belong to this graph
     */
    public void removeNode(VxNode node) 
    {
        if (node.graph == null) return; // node was already removed;
        
        if (node.getGraph() != this) {
            throw new IllegalArgumentException();
        }
        
        startModification();
        try {
            LinkedList<VxEdge> removedEdges = new LinkedList<>();
            removedEdges.addAll(node.inEdges);
            removedEdges.addAll(node.outEdges);
            for (VxEdge edge : removedEdges) {
                removeEdge(edge);
            }
            nodes.remove(node);
            node.graph = null;
            delNodeCount++;
            fireElementRemoved(node);
        }
        finally {
            endModification();
        }
    }
    
    
    /**
     * Adds an edge with a label and a user-specified style to this graph.
     * 
     * @param source
     *      source node of the new edge
     * @param target
     *      target node of the new edge
     * @param style
     *      style of the new edge
     * @return
     *      the edge which was added to this graph
     * @throws IllegalArgumentException
     *      if {@code source} or {@code target} does not belong to this graph
     * @throws NullPointerException
     *      if {@code source} or {@code target} is {@code null}
     */
    public VxEdge addEdge(VxNode source, VxNode target, String style) 
    {
        if (source.getGraph() != this || target.getGraph() != this) {
            throw new IllegalArgumentException();
        }
        if (style == null) {
            throw new NullPointerException();
        }
        
        if (source == target) {
            double loopAngle = LayouterUtils.largestFreeBlock(new GraphDrawer(), source, null);
            double relAngle = Math.PI / 18;
            VxEdge edge = new VxEdge(source, target, relAngle, .5, 
                    -relAngle, .5, loopAngle, style);
            source.outEdges.add(edge);
            target.inEdges.add(edge);
            edges.add(edge);
            fireElementAdded(edge);
            return edge;
        }
        else {            
            Collection<VxEdge> fwdEdges = getEdges(source, target);
            Collection<VxEdge> bwdEdges = getEdges(target, source);
            double angle = 0.0;
            
            if (fwdEdges.size() > 0 || bwdEdges.size() > 0) {
                boolean hasStraight = false;
                for (VxEdge edge : edges) {
                    if (edge.getCurveOutAngle() == 0.0 || edge.getCurveInAngle() == 0.0) { 
                        hasStraight = true;
                        break;
                    }
                }
                int num = hasStraight ? edges.size() : edges.size() + 1;
                angle = (double)num * (Math.PI / 14);
            }
            
            VxEdge newEdge = new VxEdge(source, target, angle, 
                    INITIAL_CP_DISTANCE, -angle, INITIAL_CP_DISTANCE, 0, style);
            source.outEdges.add(newEdge);
            target.inEdges.add(newEdge);
            edges.add(newEdge);
            fireElementAdded(newEdge);
            return newEdge;
        }
    }
    
    
    /**
     * Adds an edge to this graph. The new edge doesn't have a label and uses
     * the default edge style.
     * 
     * @param source
     *      source node of the new edge
     * @param target
     *      target node of the new edge
     * @return
     *      the edge which was added to this graph
     * @throws IllegalArgumentException
     *      if {@code source} or {@code target} does not belong to this graph
     * @throws NullPointerException
     *      if {@code source} or {@code target} is {@code null}
     */
    public VxEdge addEdge(VxNode source, VxNode target)
    {
        return addEdge(source, target, null);
    }

    
    /**
     * Removes an edge from this graph.
     * 
     * @param edge
     *      the edge to be removed from this graph
     * @throws NullPointerException
     *      if {@code edge} is null
     * @throws IllegalArgumentException
     *      if {@code edge} doesn't belong to this graph
     */
    public void removeEdge(VxEdge edge) 
    {
        if (edge.getGraph() != this) {
            throw new IllegalArgumentException();
        }
        edge.getSource().outEdges.remove(edge);
        edge.getTarget().inEdges.remove(edge);
        edges.remove(edge);
        edge.graph = null;
        delEdgeCount++;
        fireElementRemoved(edge);
    }
    
    
    /**
     * Adds a box to this graph.
     * 
     * @param elements
     *      group containing the initial elements of the box
     * @param label
     *      label of the box, or {@code null} if the box doesn't have a label
     * @param style
     *      style of the box
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code elements} or {@code style} is {@code null}
     */
    public VxBox addBox(VxGroup elements, String label, String style)
    {
        if (elements == null || style == null) {
            throw new NullPointerException();
        }
        VxBox box = new VxBox(elements.copy(), label, style);
        boxes.add(box);
        return box;
    }

    
    /**
     * Adds an empty box to this graph.
     * 
     * @param label
     *      label of the box, or {@code null} if the box doesn't have a label
     * @param style
     *      style of the box
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    public VxBox addBox(String label, String style)
    {
        return addBox(createGroup(), label, style);
    }

    
    /**
     * Adds a box with the default style to this graph.
     * 
     * @param elements
     *      group containting the initial elements of the box
     * @param label
     *      label of the box, or {@code null} if the box doesn't have a label
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code elements} is {@code null}
     */
    public VxBox addBox(VxGroup elements, String label)
    {
        return addBox(elements, label, null);
    }

    
    /**
     * Adds an empty box with the default style to this graph.
     * 
     * @param label
     *      label of the box, or {@code null} if the box doesn't have a label
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    public VxBox addBox(String label)
    {
        return addBox(createGroup(), label, null);
    }

    
    /**
     * Adds an empty box without a label and with the default style to this 
     * graph.
     * 
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    public VxBox addBox()
    {
        return addBox(createGroup(), null, null);
    }
    
    
    /**
     * Adds a box without a label and with the default style to this graph.
     * 
     * @param elements
     *      group containting the initial elements of the box
     * @return
     *      a new box with the above characteristics
     * @throws NullPointerException
     *      if {@code elements} or {@code style} is {@code null}
     */
    public VxBox addBox(VxGroup elements)
    {
        return addBox(elements, null, null);
    }

    
    /**
     * Removes all nodes and edges from this graph.
     */
    public void clear()
    {
        startModification(); // cache events until end of method
        try {
            fireElementsRemoved(nodes);
            fireElementsRemoved(edges);
            
            delNodeCount += nodes.size();
            delEdgeCount += edges.size();
        
            for (VxEdge edge : edges) {
                edge.graph = null;
            }
            edges.clear();
        
            for (VxNode node : nodes) {
                node.graph = null;
            }
            nodes.clear();
        }
        finally {
            endModification();
        }
    }
    

    /**
     * Returns the collection of (top-level) nodes in this graph.
     * The returned collection is unmodifiable.
     * <p>
     * Note that the returned collection only contains the top-level nodes of 
     * this graph. Nodes which are labels of edges are not in the collection.
     */
    public Collection<VxExplicitNode> getNodes()
    {
        return Collections.unmodifiableSet(nodes);
    }

    
    /**
     * Returns the collection of edges in this graph. The returned collection is
     * unmodifiable.
     * 
     * @return
     *      list of all edges in this graph
     */
    public Collection<VxEdge> getEdges()
    {
        return Collections.unmodifiableSet(edges);
    }
    
    
    /**
     * Returns the list of boxes in this graph. The returned list is
     * unmodifiable.
     * 
     * @return
     *      list of all boxes in this graph
     */
    public List<VxBox> getBoxes()
    {
        return Collections.unmodifiableList(boxes);
    }

    
    /**
     * Returns the list of edges from a source node to a target node. 
     * The returned list may be modified by the caller; modifications are not
     * reflected back to the graph.
     * 
     * @param source
     *      source node of the edges
     * @param target
     *      target node of the edges
     * @return
     *      list of edges from <em>source</em> to <em>target</em>
     */
    public Collection<VxEdge> getEdges(VxNode source, VxNode target) 
    {   
        List<VxEdge> result = new ArrayList<>();
        for (VxEdge edge : source.outEdges) {
            if (edge.getTarget() == target) result.add(edge);
        }
        return result;
    }
    
    
    /**
     * Returns a list of all elements in this graph.
     */
    public Collection<VxComponent> getElements() 
    {
        Set<VxComponent> els = new HashSet<>(nodes.size() + edges.size());
        els.addAll(nodes);
        els.addAll(edges);
        return els;
    }
    
    
    /**
     * Adds a graph listener to this graph.
     */
    public void addGraphListener(GraphListener l) 
    {
        if (graphListeners == null) graphListeners = new LinkedList<>();
        graphListeners.add(l);
    }
    
    
    /**
     * Starts a modification of this graph. Until {@link #endModification()}
     * is called, all events delayed and combined. This method is reentrant:
     * it is possible to call it twice; in that case, endModification() must
     * be called twice to enable the sending of events again.
     */
    public void startModification() 
    {
        modificationLevel++;
    }
    
    
    /**
     * Ends a modification of this graph. If there are no modifications to the
     * graph going on, this method sends all delayed events to the listeners.
     * 
     * @see #startModification()
     */
    public void endModification()
    {
        if (modificationLevel == 0) {
            throw new IllegalStateException("Modification level already 0.");
        }
        modificationLevel--;
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                if (addedElements != null) {
                    GraphEvent e = new GraphEvent(this, addedElements);
                    for (GraphListener l : graphListeners)
                        l.elementAdded(e);
                }
                if (removedElements != null) {
                    GraphEvent e = new GraphEvent(this, removedElements);
                    for (GraphListener l : graphListeners) 
                        l.elementRemoved(e);
                }
                if (movedElements != null) {
                    GraphEvent e = new GraphEvent(this, movedElements);
                    for (GraphListener l : graphListeners)
                        l.elementMoved(e);
                }
                if (changedElements != null) {
                    GraphEvent e = new GraphEvent(this, changedElements);
                    for (GraphListener l : graphListeners)
                        l.styleChanged(e);
                }
            }
            addedElements = null;
            removedElements = null;
            movedElements = null;
            changedElements = null;
        }
    }
    
    
    /**
     * Removes a graph listener from this graph.
     */
    public void removeGraphListener(GraphListener l)
    {
        if (graphListeners != null) {
            graphListeners.remove(l);
        }
    }
    
    
    /**
     * Notifies all graph listeners of an element having been added to this
     * graph.
     */
    void fireElementAdded(VxElement element)
    {
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                GraphEvent e = new GraphEvent(this, element);
                for (GraphListener l : graphListeners) {
                    l.elementAdded(e);
                }
            }
        }
        else {
            if (addedElements == null) addedElements = new ArrayList<>();
            addedElements.add(element);
        }
    }
    
    
    /**
     * Notifies all graph listeners of an element having been removed from this
     * graph.
     */
    void fireElementRemoved(VxElement element)
    {
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                GraphEvent e = new GraphEvent(this, element);
                for (GraphListener l : graphListeners) {
                    l.elementRemoved(e);
                }
            }
        }
        else {
            if (movedElements != null) {
                movedElements.remove(element);
            }
            if (changedElements != null) {
                changedElements.remove(element);
            }
            if (addedElements != null && addedElements.contains(element)) {
                addedElements.remove(element);
            }
            else {
                if (removedElements == null) removedElements = new ArrayList<>();
                removedElements.add(element);
            }
        }
    }
    
    
    /**
     * Notifies all graph listeners of a collection of elements having been
     * removed from this graph. The collection is not modified nor leaked to
     * other objects.
     */
    void fireElementsRemoved(Collection<? extends VxElement> elements)
    {
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                GraphEvent e = new GraphEvent(this, new ArrayList<>(elements));
                for (int i = graphListeners.size() - 1; i >= 0; i--) {
                    GraphListener l = graphListeners.get(i);
                    l.elementRemoved(e);
                }
            }
        }
        else {
            if (movedElements != null) {
                movedElements.removeAll(elements);
            }
            if (changedElements != null) {
                changedElements.removeAll(elements);
            }
            if (addedElements != null) {
                addedElements.removeAll(elements);
            }
            else {
                if (removedElements == null) removedElements = new ArrayList<>();
                removedElements.addAll(elements);
            }
        }
    }
    
    
    /**
     * Notifies all graph listeners of an element having been moved.
     */
    void fireElementMoved(VxElement element)
    {
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                GraphEvent e = new GraphEvent(this, element);
                for (GraphListener l : graphListeners) {
                    l.elementMoved(e);
                }
            }
        }
        else {
            if (movedElements == null) {
                movedElements = new ArrayList<>();
            }
            if (!movedElements.contains(element)) {
                movedElements.add(element);
            }
        }
    }
    
    
    /**
     * Notifies all graph listeners of an element's style having been changed.
     */
    void fireStyleChanged(VxElement element)
    {
        if (modificationLevel == 0) {
            if (graphListeners != null && !graphListeners.isEmpty()) {
                GraphEvent e = new GraphEvent(this, element);
                for (GraphListener l : graphListeners) {
                    l.styleChanged(e);
                }
            }
        }
        else {
            if (changedElements == null) {
                changedElements = new ArrayList<>();
            }
            if (!changedElements.contains(element)) {
                changedElements.add(element);
            }
        }
    }
    
    
    /**
     * Returns a group which contains all nodes and edges of this graph. The
     * group provides a <em>view</em> on the elements of this graph. That is,
     * when nodes or edges are added to this graph, the group reflects these
     * changes.
     * 
     * @return
     *      a group which contains all nodes and edges of this graph
     */
    public VxGroup getAllGroup()
    {
        if (allGroup == null) {
            allGroup = new AllGroup();
        }
        return allGroup;
    }
    
    
    /**
     * Creates a new, modifiable, empty group for elements of this graph.
     */
    public VxGroup createGroup()
    {
        return new GeneralGroup(this);
    }
    
    
    /**
     * Creates a new, modifiable group containing the given elements.
     * 
     * @param els
     *      initial elements of the new group
     * @return
     *      a VxGroup for this graph with the specified elements
     * @throw IllegalArgumentException
     *      if one of the elements is not an element of this graph
     */
    public VxGroup createGroup(VxComponent... els)
    {
        ArrayList<VxComponent> elements = new ArrayList<>(els.length);
        for (VxComponent el : els) {
            if (el.getGraph() != this) {
                throw new IllegalArgumentException();
            }
            elements.add(el);
        }
        VxGroup group = new GeneralGroup(this);
        group.addAll(elements);
        return group;
    }

    
    /**
     * Creates a new group containing the elements from a given
     * collection.
     * 
     * @param els
     *      initial elements of the new group
     * @return
     *      a VxGroup for this graph with the specified elements
     * @throw IllegalArgumentException
     *      if one of the elements of {@code els} is not an element of this
     *      graph
     */
    public VxGroup createGroup(Collection<? extends VxComponent> els)
    {
        for (VxComponent el : els) {
            if (el.getGraph() != this) {
                throw new IllegalArgumentException();
            }
        }
        VxGroup group = new GeneralGroup(this);
        group.addAll(els);
        return group;
    }
    
    
    /**
     * A group which contains all elements of this graph.
     */
    private class AllGroup extends VxGroup
    {
        @Override
        public Collection<VxComponent> getElements()
        {
            return VxGraph.this.getElements();
        }
        
        @Override
        public Collection<VxExplicitNode> getNodes()
        {
            return VxGraph.this.getNodes();
        }
        
        @Override
        public Collection<VxEdge> getEdges()
        {
            return VxGraph.this.getEdges();
        }
        
        @Override
        public boolean contains(VxComponent el) 
        {
            return el.graph == VxGraph.this;
        }
        
        @Override
        public void clear()
        {
            VxGraph.this.clear();
        }

        @Override
        public VxGraph getGraph()
        {
            return VxGraph.this;
        }

        @Override
        public void add(VxExplicitNode node)
        {
            if (node.graph != VxGraph.this) {
                throw new IllegalArgumentException();
            }
            // else do nothing, because the node already belongs to this graph
        }

        @Override
        public void add(VxEdge edge)
        {
            if (edge.graph != VxGraph.this) {
                throw new IllegalArgumentException();
            }
            // else do nothing, because the edge already belongs to this graph
        }

        @Override
        public void setTo(Collection<VxComponent> els)
        {
            if (els == null) {
                throw new NullPointerException();
            }
            Collection<VxComponent> complement = getElements();
            complement.removeAll(els);
            for (VxComponent c : complement) {
                remove(c);
            }
        }

        @Override
        public void remove(VxExplicitNode node)
        {
            removeNode(node);
        }
        
        @Override
        public void remove(VxEdge edge)
        {
            removeEdge(edge);
        }
    }

}
