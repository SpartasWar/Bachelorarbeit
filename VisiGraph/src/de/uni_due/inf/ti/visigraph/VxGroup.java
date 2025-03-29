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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;
import de.uni_due.inf.ti.gui.GraphicsUtils;

/**
 * This class represents a group of objects within a single graph. This 
 * allows some operations to be applied to many objects at the same time.
 *
 * @author H.J. Sander Bruggink
 */
public abstract class VxGroup
{
    /**
     * Returns the graph to which the elements of this group belong.
     * This method may return {@code null} if the group has been removed from
     * the graph.
     */
    public abstract @Nullable VxGraph getGraph();
    
    /**
     * Returns the elements of this group. The returned collection is a newly 
     * created collection containing exactly all the nodes and edges in this 
     * group.
     */
    public @NonNull Collection<VxComponent> getElements()
    {
        Set<VxComponent> elements = new HashSet<>();
        elements.addAll(getNodes());
        elements.addAll(getEdges());
        return elements;
    }
    
    /**
     * Returns the number of elements in this group.
     * The number of elements is the number of nodes plus the number of edges.
     * 
     * @return
     *      the number of elements in this group
     */
    public int size()
    {
        return getNodes().size() + getEdges().size();
    }
    
    /**
     * Returns an arbitrary element of this group. This is a convenience method
     * for situations in which it is known that the group contains only a
     * single element, or when an arbitrary element of the group is required.
     * 
     * @return
     *      the first element returned by this group's backing collection's
     *      iterator, or {@code null} if this group doesn't have any elements
     */
    public @Nullable VxComponent getArbitraryElement()
    {
        VxComponent el = getArbitraryNode();
        if (el == null) {
            el = getArbitraryEdge();
        }
        return el;
    }
    
    /**
     * Returns an arbitrary node of this group. This is a convenience method
     * for situations in which it is known that the group contains only a
     * single node, or when an arbitrary node of the group is required.
     * 
     * @return
     *      the first element returned by this group's backing collection's
     *      iterator, or {@code null} if this group doesn't have any elements
     */
    public @Nullable VxExplicitNode getArbitraryNode()
    {
        Iterator<? extends VxExplicitNode> it = getNodes().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns an arbitrary edge of this group. This is a convenience method
     * for situations in which it is known that the group contains only a
     * single edge, or when an arbitrary edge of the group is required.
     * 
     * @return
     *      the first element returned by this group's backing collection's
     *      iterator, or {@code null} if this group doesn't have any elements
     */
    public @Nullable VxEdge getArbitraryEdge()
    {
        Iterator<VxEdge> it = getEdges().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the nodes contained in this group.
     */
    public abstract @NonNull Collection<VxExplicitNode> getNodes(); 
    
    /**
     * Returns the nodes contained in this group.
     */
    public abstract @NonNull Collection<VxEdge> getEdges(); 
    
    /**
     * Returns whether this group is empty. A group is empty if it doesn't 
     * contain any nodes and edges.
     * 
     * @return
     *      {@code true} when this group is empty, {@code false} otherwise
     */
    public boolean isEmpty()
    {
        return getElements().isEmpty();
    }
    
    /**
     * Removes all objects from this group.
     */
    public abstract void clear();
    
    /**
     * Adds an element to this group. This is a convenience method which calls
     * {@link #add(VxNode)} or {@link #add(VxEdge)} based on the runtime type
     * of its argument.
     * 
     * @param el
     *      the object which is added to this group
     * @throws IllegalArgumentException
     *      if {@code el} is not an element of the same graph as the other
     *      elements of this group
     * @throws NullPointerException 
     *      if {@code el} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code el} belongs to a different graph than this group
     */
    public void add(@NonNull VxComponent el)
    {
        if (el == null) {
            throw new NullPointerException();
        }
        if (el instanceof VxExplicitNode) {
            add((VxExplicitNode)el);
        }
        else {
            add((VxEdge)el);
        }
    }
    
    /**
     * Adds a node to this group.
     * 
     * @param node
     *      the node which is added to this group
     * @throws IllegalArgumentException
     *      if {@code el} is not an element of the same graph as the other
     *      elements of this group
     * @throws NullPointerException 
     *      if {@code node} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code node} belongs to a different graph than this group
     */
    public abstract void add(@NonNull VxExplicitNode node);
    
    /**
     * Adds an edge to this group.
     * 
     * @param edge
     *      the edge which is added to this group
     * @throws IllegalArgumentException
     *      if {@code el} is not an element of the same graph as the other
     *      elements of this group
     * @throws NullPointerException 
     *      if {@code edge} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code edge} belongs to a different graph than this group
     */
    public abstract void add(@NonNull VxEdge edge);
    
    /**
     * Adds a collection of objects to this group.
     * 
     * @param els
     *      the objects which are to be added to this group
     * @throws IllegalArgumentException
     *      if one of the elements of {@code els} does not belong to the same
     *      graph as this group
     * @throws NullPointerException
     *      if {@code els} is {@code null}
     */
    public void addAll(@NonNull Collection<? extends VxComponent> els)
    {
        for (VxComponent el : els) {
            add(el);
        }
    }
    
    /**
     * Changes this group so that it only contains a single, specified element.
     * 
     * @param el
     *      the element this group will contain
     * @throws IllegalArgumentException
     *      if {@code el} does not belong to the same graph as this group
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     */
    public void setTo(@NonNull VxComponent el)
    {
        if (el == null) {
            throw new NullPointerException();
        }
        setTo(Collections.singleton(el));
    }

    /**
     * Changes this group so that it exactly contains the elements of a 
     * given collection.
     *  
     * @param els
     *      the new elements of this group
     * @throws IllegalArgumentException
     *      if one of the elements of {@code els} does not belong to the same
     *      graph as this group
     * @throws NullPointerException
     *      if {@code els} is {@code null}
     */
    public void setTo(@NonNull Collection<VxComponent> els) 
    {
        if (els == null) {
            throw new NullPointerException();
        }
        
        for (VxComponent el : els) {
            if (el.getGraph() != getGraph()) {
                throw new IllegalArgumentException();
            }
        }
        clear();
        for (VxComponent el : els) {
            add(el);
        }
    }
    
    /**
     * Removes an node from this group. If the given node does not belong to 
     * this group (but to the same graph) this method does nothing.
     * 
     * @param el
     *      the element which is removed from this group
     * @throws IllegalArgumentException 
     *      if {@code el} belongs to a different graph than this group
     */
    public abstract void remove(@NonNull VxExplicitNode el);

    /**
     * Removes an edge from this group. If the given edge does not belong to
     * this group (but to the same graph) this method does nothing.
     * 
     * @param el
     *      the element which is removed from this group
     * @throws IllegalArgumentException 
     *      if {@code el} belongs to a different graph than this group
     */
    public abstract void remove(@NonNull VxEdge el);
    
    /**
     * Removes an element from this group. This is a convenience method which
     * calls {@link VxGroup#remove(VxNode) remove(VxNode)} or 
     * {@link VxGroup#remove(VxEdge) remove(VxEdge)} based on the runtime type
     * of the argument.
     * 
     * @param el
     *      the element which is removed from this group
     * @throws IllegalArgumentException 
     *      if {@code el} belongs to a different graph than this group
     */
    public void remove(@NonNull VxComponent el)
    {
        if (el instanceof VxNode) {
            remove((VxNode)el);
        }
        else {
            remove((VxEdge)el);
        }
    }
    
    /**
     * Removes the elements of a collection from this group.
     * 
     * @param els
     *      the objects which are to be removed from this group
     * @throw NullPointerException
     *      if {@code els} is {@code null}
     */
    public void removeAll(@NonNull Collection<? extends VxComponent> els)
    {
        for (VxComponent el : els) {
            remove(el);
        }
    }
    
    /**
     * Determines whether this group contains an element.
     * 
     * @param el
     *      the element of which it is to be determined whether or not it is
     *      in this group
     * @return
     *      {@code true} if {@code el} is contained in this group, 
     *      {@code false} if {@code el} is {@code null} or not contained in 
     *      this group
     */
    public boolean contains(@Nullable VxComponent el)
    {
        if (el instanceof VxNode) {
            return getNodes().contains(el);
        }
        else {
            return getEdges().contains(el);
        }
    }
    
    /**
     * Determines whether this group contains a node.
     * 
     * @param el
     *      the node of which it is to be determined whether or not it is
     *      in this group
     * @return
     *      {@code true} if {@code el} is contained in this group, 
     *      {@code false} if {@code el} is {@code null} or not contained in
     *      this group
     */
    public boolean contains(@Nullable VxNode el) 
    {
        return getNodes().contains(el);
    }

    /**
     * Determines whether this group contains an edge.
     * 
     * @param el
     *      the edge of which it is to be determined whether or not it is
     *      in this group
     * @return
     *      {@code true} if {@code el} is contained in this group,
     *      {@code false} if {@code el} is {@code null} or not contained in
     *      this group
     */
    public boolean contains(@Nullable VxEdge el) 
    {
        return getEdges().contains(el);
    }

    /**
     * Returns the complement of this group. The complement contains exactly
     * those nodes and edges of the graph which are not contained in this 
     * group.
     * <p>
     * The returned complement is a newly created snap-shot of the complement 
     * of this group. Modifications to this group are not reflected in the
     * returned group and vice versa.
     * 
     * @return
     *      a group which is the complement of this group
     */
    public @NonNull VxGroup getComplement()
    {
        Set<VxExplicitNode> nodes = new HashSet<>(getGraph().getNodes());
        nodes.removeAll(getNodes());
        
        Set<VxEdge> edges = new HashSet<>(getGraph().getEdges());
        edges.removeAll(getEdges());
        
        return new GeneralGroup(getGraph(), nodes, edges);
    }
    
    /**
     * Move all nodes in this group a specified number of pixels to the right
     * and down.
     * 
     * @param dx
     *      number of pixels to move to the right
     * @param dx
     *      number of pixels to move down
     */
    public void move(double dx, double dy)
    {
        getGraph().startModification();
        try {
            for (VxExplicitNode node : getNodes()) {
                Point2D pt = node.position();
                pt.setLocation(pt.getX() + dx, pt.getY() + dy);
            }
        }
        finally {
            getGraph().endModification();
        }
    }
    
    /**
     * Rotate the nodes in this group by a specified angle around a given
     * anchor point.
     * 
     * @param angle
     *      angle to rotate the nodes by
     * @param anchorX
     *      x-coordinate of the anchor point
     * @param anchorY
     *      y-coordinate of the anchor point
     */
    public void rotate(double angle, double anchorX, double anchorY)
    {
        getGraph().startModification();
        try {
            angle = -angle;
            double sina = Math.sin(angle);
            double cosa = Math.cos(angle);
            double msina = -sina;
            for (VxExplicitNode node : getNodes()) {
                double x = node.position().getX() - anchorX;
                double y = node.position().getY() - anchorY;
                node.position().setLocation(
                        x * cosa + y * sina + anchorX,
                        x * msina + y * cosa + anchorY
                        );
            }
            for (VxEdge edge : getEdges()) {
                if (edge.isLoop()) {
                    double a = edge.getLoopAngle();
                    edge.setLoopAngle(GraphicsUtils.normalizeAngle(a - angle));
                }
            }
        }
        finally {
            getGraph().endModification();
        }
    }
    
    /**
     * Rotate the nodes in this group by a specified angle around a given
     * anchor point.
     * 
     * @param angle
     *      angle to rotate the nodes by
     * @param anchor
     *      anchor point around which the nodes are rotated
     * @throws NullPointerException
     *      if {@code anchor} is {@code null}
     */
    public void rotate(double angle, @NonNull Point2D anchor)
    {
        rotate(angle, anchor.getX(), anchor.getY());
    }
    
    /**
     * Creates a new group which contains the same elements as this group.
     */
    public @NonNull VxGroup copy()
    {
        Set<VxExplicitNode> nodes = new HashSet<>(getNodes());
        Set<VxEdge> edges = new HashSet<>(getEdges());
        return new GeneralGroup(getGraph(), nodes, edges);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "VxGroup: " + getElements().toString();
    }
}
