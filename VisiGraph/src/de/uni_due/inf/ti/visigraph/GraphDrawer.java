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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A GraphDrawer instance assigns concrete style to style names and draws 
 * VxGraph objects to a graphics context.
 *
 * @author H.J. Sander Bruggink
 */
public class GraphDrawer
{
    public static final double LOOP_DISTANCE = 100.0;
    
    // Properties
    
    /** Storage for the styles. */
    private HashStyleMap styles;
    
    /** Default style for nodes. */
    private Style defaultNodeStyle = null;
    
    /** Default style for edges. */
    private Style defaultEdgeStyle = null;
    
    /** Default style for label nodes. */
    private Style defaultLabelStyle = null;
    
    /** Default style for boxes. */
    private Style defaultBoxStyle = null;
    
    // Internal state
    
    /** Draw delegates of the nodes. */
    private IdentityHashMap<VxNode,NodeDrawDelegate> nodeDelegates;
    
    /** Draw delegates of the edges. */
    private IdentityHashMap<VxEdge,EdgeDrawDelegate> edgeDelegates;
    
    /** Draw delegates of the boxes. */
    private IdentityHashMap<VxBox,BoxDrawDelegate> boxDelegates;
    
    /** PropertyListener to listen to style changes. */
    private PropertyChangeListener changeListener;
    
    
    /**
     * Private class which holds the style information of this GraphDrawer.
     */
    private class MyStyleMap extends HashStyleMap
    {
        MyStyleMap()
        {
            super(new HashMap<String,Style>());
        }

        @Override
        protected void styleAdded(Style style)
        {
            style.addPropertyChangeListener(changeListener);
        }

        @Override
        protected void styleRemoved(Style style)
        {
            style.removePropertyChangeListener(changeListener);
        }
        
        @Override
        protected void nameModified(String name)
        {
            clearState();
        }

        @Override
        public void clear()
        {
            super.clear();
            clearState();
        }
    }

    
    /**
     * Listener for property change events of styles.
     */
    private class ChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent arg0)
        {
            clearState();
        }
    }
    
    
    /**
     * Creates a new StyleMap instance.
     */
    public GraphDrawer()
    {
        styles = new MyStyleMap();
        nodeDelegates = new IdentityHashMap<>();
        edgeDelegates = new IdentityHashMap<>();
        boxDelegates = new IdentityHashMap<>();
        changeListener = new ChangeListener();
    }
    
    /**
     * Returns the DrawDelegate of a node.
     * 
     * @param node
     *      the node to get the DrawDelegate of
     * @return 
     *      DrawDelegate of {@code node}
     * @throws NullPointerException
     *      if {@code node} is {@code null}
     */
    NodeDrawDelegate getDelegate(VxNode node)
    {
        NodeDrawDelegate delegate = nodeDelegates.get(node);
        if (delegate == null) {
            delegate = new NodeDrawDelegate(node, this);
            nodeDelegates.put(node, delegate);
        }
        return delegate;
    }

    /**
     * Returns the DrawDelegate of an edge.
     * 
     * @param edge
     *      the edge to get the DrawDelegate of
     * @return 
     *      DrawDelegate of {@code edge}
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     */
    EdgeDrawDelegate getDelegate(VxEdge edge)
    {
        EdgeDrawDelegate delegate = edgeDelegates.get(edge);
        if (delegate == null) {
            delegate = new EdgeDrawDelegate(edge, this);
            edgeDelegates.put(edge, delegate);
        }
        return delegate;
    }
    
    /** 
     * Returns the DrawDelegate of a box.
     * 
     * @param box
     *      box of which the drawing delegate is to be returned
     * @return
     *      drawing delegate of {@code box}
     * @throws NullPointerException
     *      when {@code box} is {@code null}
     */
    BoxDrawDelegate getDelegate(VxBox box)
    {
        BoxDrawDelegate delegate = boxDelegates.get(box);
        if (delegate == null) {
            delegate = new BoxDrawDelegate(box, this);
            boxDelegates.put(box, delegate);
        }
        return delegate;
    }
    
    /**
     * Returns the DrawDelegate of an VxElement of arbitrary type.
     * 
     * @param el
     *      element of which the DrawDelegate is to be returned
     * @return
     *      DrawDelegate of {@code el}
     * @throws NullPointerException
     *      if {@code el} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code el} is of an unknown VxElement subclass
     */
    DrawDelegate<? extends VxElement> getDelegate(VxElement el)
    {
        if (el == null)
            throw new NullPointerException();
        else if (el instanceof VxNode) {
            return getDelegate((VxNode)el);
        }
        else if (el instanceof VxEdge) {
            return getDelegate((VxEdge)el);
        }
        else if (el instanceof VxBox) {
            return getDelegate((VxBox)el);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    
    // ---------------------------------------------------------------------- //
    //                   Methods which manipulate styles                      //   
    // ---------------------------------------------------------------------- //

    /**
     * Sets the default node style. The default node style is used for 
     * top-level nodes of which the style name has not been set.
     * 
     * @param style
     *      the new default node style
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    public void setDefaultNodeStyle(Style style)
    {
        if (style == null) throw new NullPointerException();
        defaultNodeStyle = style;
    }
    
    /**
     * Returns the default node style.
     */
    public Style getDefaultNodeStyle()
    {
        if (defaultNodeStyle == null) defaultNodeStyle = new Style();
        return defaultNodeStyle;
    }

    /**
     * Sets the default edge style. The default edge style is used for edges
     * of which the style name has not been set.
     * 
     * @param style
     *      the new default edge style
     * @throw NullPointerException
     *      if {@code style} is {@code null}
     */
    public void setDefaultEdgeStyle(Style style) 
    {
        if (style == null) throw new NullPointerException();
        defaultEdgeStyle = style;
    }
    
    /**
     * Returns the default edge style.
     */
    public Style getDefaultEdgeStyle()
    {
        if (defaultEdgeStyle == null) defaultEdgeStyle = new Style();
        return defaultEdgeStyle;
    }
    
    /**
     * Sets the default node style. The default node style is used for 
     * label nodes of which the style name has not been set.
     * 
     * @param style
     *      the new default node style
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    public void setDefaultLabelStyle(Style style)
    {
        if (style == null) throw new NullPointerException();
        defaultLabelStyle = style;
    }
    
    /**
     * Returns the default label style.
     */
    public Style getDefaultLabelStyle()
    {
        if (defaultLabelStyle == null) {
            defaultLabelStyle = new Style();
            defaultLabelStyle.setShape(Shape.createCoordinate());
        }
        return defaultLabelStyle;
    }

    /**
     * Sets the default box style. The default box style is used for boxes
     * of which the style name has not been set.
     * 
     * @param style
     *      the new default box style
     * @throw NullPointerException
     *      if {@code style} is {@code null}
     */
    public void setDefaultBoxStyle(Style style) 
    {
        if (style == null) throw new NullPointerException();
        defaultBoxStyle = style;
    }
    
    /**
     * Returns the default edge style.
     */
    public Style getDefaultBoxStyle()
    {
        if (defaultBoxStyle == null) defaultBoxStyle = new Style();
        return defaultBoxStyle;
    }
    
    /**
     * Returns the style of a VxGraph element.
     */
    public Style getStyle(VxElement element)
    {
        String name = element.getStyle();
        Style style = name == null ? null : styles.get(name);
        if (style == null) {
            if (element instanceof VxLabelNode) {
                style = getDefaultLabelStyle();
            }
            else if (element instanceof VxNode) {
                style = getDefaultNodeStyle();
            }
            else if (element instanceof VxEdge) {
                style = getDefaultEdgeStyle();
            }
            else if (element instanceof VxBox) {
                style = getDefaultBoxStyle();
            }
            else {
                // shouldn't happen
                style = new Style();
            }
        }
        return style;
    }
    
    /**
     * Returns a StyleMap which provides a view on this GraphDrawer's styles.
     */
    public StyleMap getStyleMap()
    {
        return styles;    
    }
    
    
    // ---------------------------------------------------------------------- //
    //            Methods for obtaining properties of graph elements          //
    // ---------------------------------------------------------------------- //
    
    /**
     * Returns the shape which draw an edge.
     * 
     * @param edge
     *      edge of which to obtain the java shape
     * @return 
     *      java shape of {@code edge}
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     */
    public java.awt.Shape getEdgeLine(VxEdge edge)
    {
        return getDelegate(edge).getLineShape();
    }
    
    /**
     * Returns the position of a node.
     * 
     * @param node 
     *      node of which to return the position
     * @return
     *      position of {@code node}
     * @throws NullPointerException
     *      if {@code node} is {@code null}
     */
    public Point2D getPosition(VxNode node)
    {
        if (node instanceof VxExplicitNode) {
            return ((VxExplicitNode)node).position();
        }
        else {
            NodeDrawDelegate nodeDD = getDelegate(node);
            return nodeDD.position();
        }
    }
    
    /**
     * Returns a point on the border of a node.
     * 
     * @param node
     *      node on the border of which a point is to be returned
     * @param angle
     *      angle from the center of the node to point on the border which is
     *      to be returned
     * @return
     *      point on the border of the node at the specified angle
     * @throws NullPointerException
     *      if {@code node} is {@code null}
     */
    public Point2D getBorderPoint(VxNode node, double angle)
    {
        NodeDrawDelegate nodeDD = getDelegate(node);
        return nodeDD.getBorderPoint(angle);
    }
    
    /**
     * Returns the point on the border of a node which is on the line from
     * a given point to the center of the node.
     * 
     * @param node
     *      node on the border of which a point is to be returned
     * @param point
     *      point which is used to select the point on the border of {@code node}
     * @return 
     *      point on the border of {@code node} which is one the line from
     *      {@code node}'s center to {@code point}
     * @throws NullPointerException
     *      if {@code node} or {@code point} is {@code null}
     */
    public Point2D getBorderPoint(VxNode node, Point2D point)
    {
        NodeDrawDelegate nodeDD = getDelegate(node);
        Point2D nodePos = nodeDD.position();
        double dx = point.getX() - nodePos.getX();
        double dy = point.getY() - nodePos.getY();
        return nodeDD.getBorderPoint(Math.atan2(dy, dx));
    }
    
    /**
     * Returns the angle at which an edge enters its target.
     * 
     * @param edge
     *      edge of which to return the incoming angle
     * @return
     *      incoming angle of {@code edge}
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     */
    public double getInAngle(VxEdge edge)
    {
        EdgeDrawDelegate edgeDD = getDelegate(edge);
        return edgeDD.getInAngle();
    }
    
    /**
     * Returns the angle at which an edge leaves its source.
     * 
     * @param edge
     *      edge of which to return the outgoing angle
     * @return
     *      outgoing angle of {@code edge}
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     */
    public double getOutAngle(VxEdge edge)
    {
        EdgeDrawDelegate edgeDD = getDelegate(edge);
        return edgeDD.getOutAngle();
    }
    
    
    // ---------------------------------------------------------------------- //
    //        Methods which return physical properties of graph elements      //
    // ---------------------------------------------------------------------- //
        
    /**
     * Returns a rectangle which completely encloses all elements in a group.
     * If the group doesn't contain any elements, {@code null} is returned.
     * Otherwise, if {@code buf} is non-null, it is used to store the result and
     * returned; and if {@code buf} is null, a new Rectangle2D containing the
     * result is returned.
     * 
     * @param group
     *      group of which the bounds are to be returned
     * @param buf
     *      Rectangle2D object where the result is to be stored, or {@code null}
     *      if the result is to be returned in a newly created object
     * @return
     *      a rectangle which completely encloses all elements in this group,
     *      or {@code null} if this group doesn't contain any elements
     * @throws NullPointException
     *      if {@code group} is {@code null}
     */
    public Rectangle2D getBounds(VxGroup group, Rectangle2D buf)
    {
        if (group.isEmpty()) return null;
        boolean first = true;
        
        for (VxNode el : group.getNodes()) {
            NodeDrawDelegate elDD = getDelegate(el);
            if (first) {
                buf = elDD.getBounds(buf);
                first = false;
            }
            else {
                buf.add(elDD.getBounds());
            }
        }

        for (VxEdge el : group.getEdges()) {
            EdgeDrawDelegate elDD = getDelegate(el);
            if (first) {
                buf = elDD.getBounds(buf);
                first = false;
            }
            else {
                buf.add(elDD.getBounds());
            }
        }

        return buf;
    }
    
    /**
     * Returns a rectangle which completely encloses all elements in a 
     * collection.
     * If the collection doesn't contain any elements, {@code null} is returned.
     * Otherwise, if {@code buf} is non-null, it is used to store the result and
     * returned; and if {@code buf} is null, a new Rectangle2D containing the
     * result is returned.
     * 
     * @param els
     *      collection of which the bounds are to be returned
     * @param buf
     *      Rectangle2D object where the result is to be stored, or {@code null}
     *      if the result is to be returned in a newly created object
     * @return
     *      a rectangle which completely encloses all elements in this group,
     *      or {@code null} if this group doesn't contain any elements
     * @throws NullPointerException
     *      when {@code els} is {@code null}
     */
    public Rectangle2D getBounds(Collection<? extends VxElement> els, Rectangle2D buf)
    {
        if (els.isEmpty()) return null;
        boolean first = true;
        
        for (VxElement el : els) {
            DrawDelegate<?> elDD = getDelegate(el);
            if (first) {
                buf = elDD.getBounds(buf);
                first = false;
            }
            else {
                buf.add(elDD.getBounds());
            }
        }

        return buf;
    }
    
    /**
     * Returns a rectangle which completely encloses all elements in a group.
     * If the group doesn't contain any elements, {@code null} is returned.
     * 
     * @param group
     *      group of which the bounds are to be returned
     * @return
     *      a rectangle which completely encloses all elements in this group,
     *      or {@code null} if this group doesn't contain any elements
     * @throws NullPointerException
     *      if {@code group} is {@code null}
     */
    public Rectangle2D getBounds(VxGroup group)
    {
        return getBounds(group, null);
    }
    
    /**
     * Returns a rectangle which completely encloses all elements in a 
     * collection.
     * If the collection doesn't contain any elements, {@code null} is returned.
     * 
     * @param els
     *      collection of which the bounds are to be returned
     * @return
     *      a rectangle which completely encloses all elements in this group,
     *      or {@code null} if this group doesn't contain any elements
     */
    public Rectangle2D getBounds(Collection<? extends VxElement> els)
    {
        return getBounds(els, null);
    }

	/**
	 * Returns a rectangle which completely encloses a graph, including its
	 * boxes.
	 * If the graph doesn't contain any elements or boxes, {@code null} is
	 * returned.
	 *
	 * @param graph
	 *      graph of which the bounding box is to be returned
	 * @return
	 *      a rectangle which completely encloses the given graph
	 *      or {@code null} if the graph is empty
	 */
	public Rectangle2D getBounds(VxGraph graph)
	{
		Rectangle2D b = getBounds(graph.getAllGroup());
		if (b == null) {
			// no elements
			if (graph.getBoxes().isEmpty()) {
				// no elements and no boxes
				return null;
			} else {
				// no elements but boxes
				b = new Rectangle2D.Double();
			}
		}
		for (VxBox box : graph.getBoxes()) {
			DrawDelegate<?> elDD = getDelegate(box);
			b.add(elDD.getBounds());
		}
		return b;
	}

    /**
     * Moves the nodes of a group so that the top-left corner of the group
     * is located at a given position.
     * 
     * @param group
     *      group to move
     * @param point
     *      point to move the group to
     */
    public void moveTo(VxGroup group, Point2D point)
    {
        Rectangle2D bds = getBounds(group);
        if (bds == null) {
        	/* graph is empty, nothing to do! */
        	return;
        }
        double dx = point.getX() - bds.getMinX();
        double dy = point.getY() - bds.getMinY();
        for (VxExplicitNode node : group.getNodes()) {
            Point2D pos = node.position();
            node.moveTo(pos.getX() + dx, pos.getY() + dy);
        }
    }

    /**
     * Moves the nodes of a group by the given amount.
     * 
     * @param group
     *      group to move
     * @param point
     *      point to move the group by
     */
    public void moveOriginTo(VxGroup group, Point2D point)
    {
        double dx = point.getX();
        double dy = point.getY();
        for (VxExplicitNode node : group.getNodes()) {
            Point2D pos = node.position();
            node.moveTo(pos.getX() + dx, pos.getY() + dy);
        }
    }

    /**
     * Determines whether a point lies on a graph element.
     * 
     * @param el
     *      graph element of which it will be determined whether {@code p}
     *      lies on it
     * @param p
     *      the point of which it will be tested whether it lies on
     *      {@code el} 
     * @return
     *      {@code true} if {@code p} lies on {@code el}, {@code false}
     *      otherwise
     */
    public boolean contains(VxElement el, Point2D p)
    {
        DrawDelegate<?> delegate = getDelegate(el);
        return delegate.contains(p);
    }
    
    /**
     * Returns the element which is at a specified position.
     * 
     * @param point
     *      the position to find an element at
     * @return
     *      the element which is at the specified position, or {@code null} if
     *      no such element can be found
     */
    public VxComponent getElementAt(VxGroup group, Point2D point)
    {
        for (VxNode node : group.getNodes()) {
            NodeDrawDelegate nodeDD = getDelegate(node);
            if (nodeDD.contains(point)) return node;
        }
        for (VxEdge edge : group.getEdges()) {
            for (VxLabelNode label : edge.getLabels()) {
                NodeDrawDelegate labelDD = getDelegate(label);
                if (labelDD.contains(point)) return label;
            }
            EdgeDrawDelegate edgeDD = getDelegate(edge);
            if (edgeDD.contains(point)) return edge;
        }
        return null;
    }

    /**
     * Draws a VxGraph element to a graphics context using the styles set in
     * this StyleMap.
     * 
     * @param g
     *      graphics context to draw to
     * @param el
     *      element to draw
     * @param selected
     *      whether the element is to be drawn in selected state
     */
    public void draw(Graphics2D g, VxElement el, boolean selected)
    {
        DrawDelegate<?> delegate = getDelegate(el);
        delegate.draw(g, selected);
    }    
    
    /**
     * Draws a graph to a graphics context.
     * 
     * @param g
     *      graphics context to draw to
     * @param graph
     *      graph to draw
     * @throws NullPointerException
     *      if either {@code g} or {@code graph} is {@code null}
     */
    public void draw(Graphics2D g, VxGraph graph)
    {
        for (VxBox box : graph.getBoxes()) {
            draw(g, box, false);
        }
        for (VxNode node : graph.getNodes()) {
            draw(g, node, false);
        }
        for (VxEdge edge : graph.getEdges()) {
            for (VxNode label : edge.getLabels()) {
                draw(g, label, false);
            }
            draw(g, edge, false);
        }
    }
    
    /**
     * Returns a subgroup of those elements of a group which are, completely
     * or partly, within a rectangle. 
     * 
     * @param group
     *      the large group
     * @param rect
     *      rectangle to find the elements in
     */
    public Collection<VxComponent> getElementsIn(VxGroup group, Rectangle2D rect)
    {
        Set<VxComponent> sub = new HashSet<>();
        
        for (VxExplicitNode node : group.getNodes()) {
            if (rect.contains(node.position())) {
                sub.add(node);
            }
        }
        
        for (VxEdge edge : group.getEdges()) {
            NodeDrawDelegate sourceDD = getDelegate(edge.getSource());
            NodeDrawDelegate targetDD = getDelegate(edge.getTarget());
            if (rect.contains(sourceDD.position()) && rect.contains(targetDD.position())) {
                sub.add(edge);
            }
        }
        
        return sub;
    }

    
    // ---------------------------------------------------------------------- //
    //                           Misc. methods                                //
    // ---------------------------------------------------------------------- //
    
    /**
     * Clears and disables the DrawDelegates used by this GraphDrawer.
     */
    public void clearState()
    {
        for (NodeDrawDelegate del : nodeDelegates.values()) del.disable();
        nodeDelegates.clear();
        
        for (EdgeDrawDelegate del : edgeDelegates.values()) del.disable();
        edgeDelegates.clear();
        
        for (BoxDrawDelegate del : boxDelegates.values()) del.disable();
        boxDelegates.clear();
    }
}
