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

package de.uni_due.inf.ti.visigraph.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import de.uni_due.inf.ti.gui.GraphicsUtils;
import de.uni_due.inf.ti.visigraph.GraphDrawer;
import de.uni_due.inf.ti.visigraph.GraphEvent;
import de.uni_due.inf.ti.visigraph.GraphListener;
import de.uni_due.inf.ti.visigraph.ShapeUtils;
import de.uni_due.inf.ti.visigraph.Style;
import de.uni_due.inf.ti.visigraph.StyleMap;
import de.uni_due.inf.ti.visigraph.VxBox;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxLabelNode;
import de.uni_due.inf.ti.visigraph.VxNode;

public abstract class AbstractGraphPanel extends JComponent implements ItemSelectable
{
    private static final long serialVersionUID = 7967820583862504661L;
    
    private static final double ROTATE_CORNER_SIZE      = 10;
    private static final double LEFT_GAP                = 3.0;
    private static final double TOP_GAP                 = 3.0;
    private static final double RIGHT_GAP               = 3;
    private static final double BOTTOM_GAP              = 3;
       
    /** The graph which is currently displayed/edited by this panel. */
    private VxGraph graph;
    
    /** The graph drawer containing the style information and which draws the
        graph to the canvas. */
    private GraphDrawer drawer;
    
    /** Whether or not nodes in this GraphPanel can be selected. */
    private boolean nodesSelectable = true;
    
    /** Whether or not nodes in this GraphPanel can be moved by the user. */
    private boolean nodesMovable = true;
    
    /** Whether or not nodes in this GraphPanel can be added by the user. */
    private boolean nodesAddable = false;
    
    /** Whether or not edges in this GraphPanel can be selected. */
    private boolean edgesSelectable = true;
    
    /** Whether or not (the control points of) the edges in this GraphPanel can
        be moved by the user. */
    private boolean edgesMovable = true;
    
    /** Whether or not the user can add edges. */
    private boolean edgesAddable = false;
    
    /** Whether or not multiple objects can be selected at once. */
    private boolean multiSelect = true;
    
    /** Whether or not the panel is anti-aliased. */
    private boolean antiAliased = true;
    
    /** The color of the selection rectangle. */
    private Color selectionColor = new Color(.3f, .3f, .6f, .6f);
    
    /** Color of the line around the selection box. */
    private Color selectionLineColor = selectionColor.darker().darker();
    
    protected enum MouseAction {
        NONE,
        DRAGGING_NODE,
        DRAGGING_CP,
        SELECTING,
        DRAGGING_SELECTION,
        ROTATING_SELECTION,
        DRAGGING_EDGE
    }
    
    // Internal state
    
    protected static class MouseActionInfo 
    {
        double offsetX = 0;
        double offsetY = 0;
        public int dragButton;
    }
    
    protected MouseAction currentAction = MouseAction.NONE;
    protected MouseActionInfo actionInfo = null;
    private Rectangle2D selectionBox = null;
    protected Collection<VxComponent> selection = null;
    private Point2D origin = new Point2D.Double(0,0);
    protected Point2D mousePos = null;
    private MyGraphListener graphListener;
    
    private List<? extends ControlPoint> controlPoints = null;
    
    
    /**
     * Creates a new AbstractGraphPanel without a graph.
     */
    public AbstractGraphPanel() 
    {
        this(null);
    }
    
    /**
     * Creates a new AbstractGraphPanel.
     * 
     * @param graph
     *      the graph of this graph panel, or {@code null} if the new
     *      AbstractGraphPanel should be created without a graph
     */
    public AbstractGraphPanel(VxGraph graph)
    {
        setBackground(Color.white);
        setOpaque(true);
        setMinimumSize(new Dimension(300, 200));
        
        drawer = new SwingGraphDrawer(this);
        
        graphListener = new MyGraphListener();
        this.graph = graph;
        if (graph != null) {
            graph.addGraphListener(graphListener);
            alignGraph(LEFT_GAP, TOP_GAP);
        }
        
        MouseAdapter mh = new MouseHandler();
        addMouseListener(mh);
        addMouseMotionListener(mh);
        
        calculateSize();
    }

    /**
     * Adds a listener to receive item events when the state of an item is
     * changed by the user.
     * 
     * @param l
     *      item listener which is to be added to this panel
     * @throws NullPointerException
     *      if {@code l} is {@code null}
     */
    @Override
    public void addItemListener(ItemListener l)
    {
        listenerList.add(ItemListener.class, l);
    }
    
    /**
     * Removes an item listener.
     * 
     * @param l
     *      item listener to be removed from this graph panel
     * @throws NullPointerException
     *      if {@code l} is {@code null}
     */
    public void removeItemListener(ItemListener l)
    {
        listenerList.remove(ItemListener.class, l);
    }
    
    /**
     * Returns the objects which are currently selected in this GraphPanel.
     */
    @Override
    public VxComponent[] getSelectedObjects()
    {
        if (selection == null) {
            return null;
        }
        else {
            if (selection.size() == 0) {
                return null;
            }
            else {
                return selection.toArray(new VxComponent[selection.size()]);
            }
        }
    }
    
    /**
     * Returns the graph of this GraphPanel.
     * 
     * @return
     *      the graph displayed or edited by this AbstractGraphPanel, or
     *      {@code null} if no graph is currently displayed or edited
     */
    public VxGraph getGraph()
    {
        return graph;
    }
    
    /**
     * Sets the graph of this GraphPanel. The graph can be <em>null</em>; if
     * the graph is set to <em>null</em>, nothing will be displayed and all
     * editing features of the GraphPanel are disabled.
     * 
     * @param graph
     *      the new graph of this GraphPanel
     */
    public void setGraph(VxGraph graph)
    {
        controlPoints = null;
        if (this.graph != null) {
            this.graph.removeGraphListener(graphListener);
            drawer.clearState();
        }
        this.graph = graph;
        if (graph != null) {
            graph.addGraphListener(graphListener);
            alignGraph(LEFT_GAP, TOP_GAP);
        }
        selection = null;
        currentAction = MouseAction.NONE;
        actionInfo = null;
        calculateSize();
        repaint();
    }
    
    /**
     * Returns the GraphDrawer responsible for drawing graph components of
     * this AbstactGraphPanel.
     */
    public GraphDrawer getDrawer()
    {
        return drawer;
    }
    
    /**
     * Associates a style with a style name. 
     * This is a convenience method for 
     * {@code getDrawer.getStyleMap().put(name, style)}.
     * 
     * @param name
     *      name of a style
     * @param style
     *      style to associate with the name
     */
    public void setStyle(String name, Style style)
    {
        drawer.getStyleMap().put(name, style);
    }
    
    /**
     * Returns the style associated with a style name.
     * This is a convenience method for 
     * {@code getDrawer().getStyleMap().get(name)}.
     * 
     * @param name
     *      name of a style
     * @return
     *      style associated with the given name
     */
    public Style getStyle(String name)
    {
        return drawer.getStyleMap().get(name);
    }
    
    /**
     * Removes a style name from the style associations.
     * This is a convenience method for 
     * {@code getDrawer().getStyleMap().remove(name)}.
     * 
     * @param name
     *      style name to remove
     */
    public void removeStyle(String name)
    {
        drawer.getStyleMap().remove(name);
    }

    /**
     * Returns whether or not the edges in this GraphPanel can be selected by
     * the user.
     */
    public boolean getEdgesSelectable()
    {
        return edgesSelectable;
    }
    
    /**
     * Sets whether or not the edges in this GraphPanel can be selected by
     * the user.
     */
    protected void setEdgesSelectable(boolean b)
    {
        edgesSelectable = b;
        if (selection != null) {
            selection = null;
            repaint();
        }
    }
    
    /**
     * Returns whether or not (the control points of) the edges in this
     * GraphPanel can be moved by the user.
     */
    public boolean getEdgesMovable()
    {
        return edgesMovable;
    }
    
    /**
     * Sets whether or not (the control points of) the edges in this GraphPanel
     * can be moved by the user. If {@code getEdgesSelectable() == false} this
     * property has no effect.
     */
    protected void setEdgesMovable(boolean b)
    {
        edgesMovable = b;
    }
    
    /**
     * Returns whether the user can add edges.
     */
    public boolean getEdgesAddable()    
    {
        return edgesAddable;
    }
    
    /**
     * Sets whether the user can add edges.
     */
    protected void setEdgesAddable(boolean b)
    {
        edgesAddable = b;
    }
    
    /**
     * Returns whether nodes in this GraphPanel can be selected by the user.
     */
    public boolean getNodesSelectable()
    {
        return nodesSelectable;
    }
    
    /**
     * Sets whether nodes in this GraphPanel can be selected by the user.
     */
    protected void setNodesSelectable(boolean b)
    {
        nodesSelectable = b;
        if (selection != null) {
            selection = null;
            repaint();
        }
    }

    /**
     * Returns whether nodes in this GraphPanel can be moved by the user.
     */
    public boolean getNodesMovable()
    {
        return nodesMovable;
    }
    
    /**
     * Sets whether nodes in this GraphPanel can be moved by the user.
     */
    protected void setNodesMovable(boolean b)
    {
        nodesMovable = b;
    }
    
    /**
     * Returns whether the user can add nodes to the graph of this 
     * AbstractGraphPanel.
     */
    public boolean getNodesAddable()
    {
        return nodesAddable;
    }
    
    /** 
     * Sets whether the user can add nodes to the graph of this
     * AbstractGraphPanel.
     */
    protected void setNodesAddable(boolean b)
    {
        nodesAddable = b;
    }
    
    /**
     * Returns whether multiple elements can be selected at the same time.
     */
    public boolean getMultiSelect()
    {
        return multiSelect;
    }

    /**
     * Sets whether multiple elements can be selected at the same time.
     */
    protected void setMultiSelect(boolean b)
    {
        multiSelect = b;
        if (selection != null) {
            selection = null;
            repaint();
        }
    }
    
    /**
     * Returns whether this component is anti-aliased.
     */
    public boolean isAntiAliased()
    {
        return antiAliased;
    }
    
    /**
     * Sets whether this component is anti-aliased.
     */
    public void setAntiAliased(boolean antiAliased)
    {
        this.antiAliased = antiAliased;
        repaint();
    }
    
    /**
     * Returns the style map of this graph panel.
     */
    public StyleMap getStyleMap()
    {
        return drawer.getStyleMap();
    }
    
    /**
     * Creates "active" control points of an edge.
     */
    private List<? extends ControlPoint> createControlPoints(VxEdge edge)
    {
        if (edge.isLoop()) {
            return Collections.singletonList(new LoopControlPoint(edge));
        }
        else {
            ControlPoint cp1 = new CurveControlPoint(edge, 1);
            ControlPoint cp2 = new CurveControlPoint(edge, 2);
            ControlPoint comb = new CombinedCurveControlPoint(edge, cp1, cp2);
            return Arrays.asList(cp1, cp2, comb);
        }
    }
    
    /**
     * Paints this component.
     */
    @Override
    protected void paintComponent(Graphics graphics)
    {
        Graphics2D g = (Graphics2D)graphics;
        Object value = antiAliased ? 
                RenderingHints.VALUE_ANTIALIAS_ON :
                RenderingHints.VALUE_ANTIALIAS_OFF;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, value);
        
        if (isOpaque()) {
            Color cur = g.getColor();
            g.setColor(getBackground());
            g.fill(g.getClip());
            g.setColor(cur);
        }

        if (graph != null) {
            AffineTransform oldTrans = g.getTransform();
            AffineTransform trans = new AffineTransform(oldTrans);
            trans.translate(-origin.getX(), -origin.getY());
            g.setTransform(trans);
            
            paintGraph(g);
            
            g.setTransform(oldTrans);
        }
        
        revalidate();
    }
    
    /**
     * Draws the graph of this component.
     * This method is called by {@code paintComponent}. When the method is 
     * called, the affine transform has already been adapted, so the method can
     * draw to the given graphics context in logical coordinate space. 
     * 
     * <strong>Subclassing note:</strong>
     * Subclasses may override this method if they need to perform custom graph
     * drawing actions. If this method is overridden, it is recommended that
     * this method is called somewhere in the implementation (with
     * {@code super.paintGraph(g)}.
     * 
     * @param g
     *      graphics context to draw the graph on
     * @throws NullPointerException 
     *      if {@code g} is {@code null}
     */
    protected void paintGraph(Graphics2D g)
    {
        for (VxBox box : graph.getBoxes()) {
            drawer.draw(g, box, false);
        }
        for (VxNode node : graph.getNodes()) {
            drawer.draw(g, node, isSelected(node));
        }
        for (VxEdge edge : graph.getEdges()) {
            for (VxLabelNode label : edge.getLabels()) {
                drawer.draw(g, label, isSelected(label));
            }
            drawer.draw(g, edge, isSelected(edge));
        }
        
        if (selection != null) {
            drawSelectionBox(g);
        }        
        
        if (controlPoints != null) {
            Ellipse2D ell = new Ellipse2D.Double();
            for (ControlPoint cp : controlPoints) {
                if (cp.isUsed()) {
                    ell.setFrame(cp.getX() - 3, cp.getY() - 3, 6, 6);
                    if (cp.getControlPointType() == 1) {
                        g.setColor(Color.BLACK);
                        g.draw(ell);
                        g.setColor(Color.YELLOW);
                        g.fill(ell);
                    }
                    else {
                        g.setColor(Color.BLACK);
                        g.fill(ell);

                        if (cp instanceof CurveControlPoint) {
                            // draw line to associated node
                            VxNode n = ((CurveControlPoint) cp).getAssociatedNode();
                            g.setColor(Color.BLUE);
                            g.draw(new Line2D.Double(cp, drawer.getPosition(n)));
                        }
                    }
                }
            }
        }  
        revalidate();
    }
    
    /**
     * Checks whether the given position is on the rotation corner of the 
     * selection box.
     */
    protected boolean onRotateCorner(Point2D p)
    {
        if (selectionBox == null)
            return false;
        else {
            double refX = selectionBox.getX() + selectionBox.getWidth();
            double refY = selectionBox.getY();
            if (p.getX() >= refX - ROTATE_CORNER_SIZE && p.getX() <= refX && p.getY() >= refY
                    && p.getY() <= refY + ROTATE_CORNER_SIZE
                    && Math.hypot(refX - p.getX(), p.getY() - refY) <= ROTATE_CORNER_SIZE)
                return true;
            else
                return false;
        }
    }
    
    /**
     * Draws the selection box.
     */
    private void drawSelectionBox(Graphics2D g)
    {
        if (selectionBox == null) return;
        
        if (currentAction == MouseAction.ROTATING_SELECTION) {
            RotatingSelectionInfo info = (RotatingSelectionInfo)actionInfo;
    
            Point2D ul = new Point2D.Double(selectionBox.getX(), 
                                            selectionBox.getY());
            Point2D ur = new Point2D.Double(selectionBox.getX() + selectionBox.getWidth(), 
                                            selectionBox.getY());
            Point2D dr = new Point2D.Double(selectionBox.getX() + selectionBox.getWidth(), 
                                            selectionBox.getY() + selectionBox.getHeight());
            Point2D dl = new Point2D.Double(selectionBox.getX(), 
                                            selectionBox.getY() + selectionBox.getHeight());
            rotatePoint(ul, info.rotation, info.anchor);
            rotatePoint(ur, info.rotation, info.anchor);
            rotatePoint(dr, info.rotation, info.anchor);
            rotatePoint(dl, info.rotation, info.anchor);
            Path2D p = new GeneralPath();
            p.moveTo(ul.getX(), ul.getY());
            p.lineTo(ur.getX(), ur.getY());
            p.lineTo(dr.getX(), dr.getY());
            p.lineTo(dl.getX(), dl.getY());
            p.closePath();
            g.setColor(selectionColor);
            g.fill(p);
            g.setColor(selectionLineColor);
            g.draw(p);
            g.draw(new Arc2D.Double(
                    ur.getX() - ROTATE_CORNER_SIZE, ur.getY() - ROTATE_CORNER_SIZE,
                    ROTATE_CORNER_SIZE * 2, ROTATE_CORNER_SIZE * 2,
                    -Math.toDegrees(info.rotation) + 180,
                    90,
                    Arc2D.OPEN
                    ));
        }
        else {
            g.setColor(selectionColor);
            g.fill(selectionBox);
            g.setColor(selectionLineColor);
            g.draw(selectionBox);
            if (selection != null && selection.size() > 1 && 
                    currentAction != MouseAction.SELECTING) {
                g.draw(new Arc2D.Double(
                        (selectionBox.getX() + selectionBox.getWidth()) - ROTATE_CORNER_SIZE,
                        selectionBox.getY() - ROTATE_CORNER_SIZE,
                        ROTATE_CORNER_SIZE * 2, ROTATE_CORNER_SIZE * 2,
                        180,
                        90,
                        Arc2D.OPEN
                        ));
            }
        }
    }

    /** 
     * Sets the selection box to a rectangle encompassing all selected 
     * elements.
     */
    protected void updateSelectionBox()
    {
        Rectangle2D bounds = drawer.getBounds(selection);
        if (bounds != null) {
            selectionBox.setRect(
                    bounds.getX() - 3,
                    bounds.getY() - 3,
                    bounds.getWidth() + 6,
                    bounds.getHeight() + 6);
        }
        else {
            selectionBox = null;
        }
    }
    
    /**
     * Sets the mouse position attribute. 
     * 
     * @param phys
     *      mouse position in physical coordinate space
     */
    protected void setMousePos(Point2D phys)
    {
        if (mousePos == null) {
            mousePos = new Point2D.Double();
        }
        mousePos.setLocation(phys.getX() + origin.getX(), 
                             phys.getY() + origin.getY());
    }
    
    /**
     * Calculates the physical coordinate corresponding to a logical 
     * coordinate.
     */
    protected Point calculatePhysicalCoordinate(Point2D logical)
    {
        return new Point((int)(logical.getX() - origin.getX()),
                         (int)(logical.getY() - origin.getY()));
    }
    
    /**
     * Returns whether an element of the graph is selected.
     */
    private boolean isSelected(VxComponent el)
    {
        return (selection != null && selection.contains(el)); 
    }
    
    /**
     * Sets the offset attributes of a MouseActionInfo object.
     */
    protected void setOffset(MouseActionInfo info, double anchorX, double anchorY,
            Point2D mouse)
    {
        info.offsetX = mouse.getX() - anchorX;
        info.offsetY = mouse.getY() - anchorY;
    }

    /**
     * Calculates size of this component. This method is called when something
     * changes on which the preferred size of the component depends.
     */
    protected void calculateSize()
    {
        if (graph == null || graph.isEmpty()) {
            setPreferredSize(getMinimumSize());
        }
        else {
            Rectangle2D bounds = drawer.getBounds(graph.getAllGroup());
            assert bounds != null; // because graph is non-empty
            double leftX = origin.getX();
            if (leftX < bounds.getX() && bounds.getX() - leftX < LEFT_GAP) { 
                leftX = bounds.getX() - 3;
            }
            double topY = origin.getY();
            if (topY < bounds.getY() && bounds.getY() - topY < TOP_GAP) {
                topY = bounds.getY() - 3;
            }
            double rightX = bounds.getX() + bounds.getWidth() + RIGHT_GAP;
            double bottomY = bounds.getY() + bounds.getHeight() + BOTTOM_GAP;
            setPreferredSize(new Dimension((int)(rightX - leftX), (int)(bottomY - topY)));
        }
        Component parent = getParent();
        if (parent != null) parent.invalidate();
    }
    
    /**
     * Makes sure that the graph is displayed in the top left corner of this
     * GraphPanel. This panel does not change the positions of the graph's
     * nodes, it only adds an additional translation to the coordinates so that
     * it is displayed on another location.
     * <p>
     * The parameters of this method control how much whitespace is kept
     * between the borders of this component and the displayed graph.
     * 
     * @param leftGap
     *      whitespace between the left side of this GraphPanel and the graph
     * @param topGap
     *      whitespace between the top side of this GraphPanel and the graph
     */
    public void alignGraph(double leftGap, double topGap)
    {
        if (graph == null || graph.isEmpty()) {
            return;
        }
        
        Rectangle2D bounds = drawer.getBounds(graph.getAllGroup());
        assert bounds != null; // because graph is not empty
        origin.setLocation(bounds.getX() - leftGap, bounds.getY() - topGap);
        
        calculateSize();
        repaint();
    }
    
    /**
     * Rotates a point around an anchor point.
     * 
     * @param p
     *      the point which is to be rotated
     * @param angle
     *      the angle by which the point is to be rotated
     * @param anchor
     *      the anchor point around which the point is to be rotated
     */
    private static void rotatePoint(Point2D p, double angle, Point2D anchor)
    {
        double dx = p.getX() - anchor.getX();
        double dy = p.getY() - anchor.getY();
        double theta = Math.atan2(dy, dx);
        double r = Math.hypot(dx, dy);
        theta += angle;
        p.setLocation(
                anchor.getX() + Math.cos(theta) * r,
                anchor.getY() + Math.sin(theta) * r
                );
    }
    
    /**
     * Notifies all item listeners that the state of an item has changed.
     * 
     * @param item
     *      item of which the state changed
     * @param stateChange
     *      the state change of the item, one of:
     *      {@code ItemEvent.SELECTED} or {@code ItemEvent.DESELECTED}
     */
    protected void fireItemStateChanged(VxComponent item, int stateChange)
    {
        ItemEvent event = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemListener.class) {
                if (event == null) {
                    event = new ItemEvent(
                            this, 
                            ItemEvent.ITEM_STATE_CHANGED, 
                            item, 
                            stateChange);
                    ((ItemListener)listeners[i+1]).itemStateChanged(event);
                }
            }
        }
    }
    
    /**
     * Notifies all item listeners that the state of a collection of items had
     * changed.
     * 
     * @param items
     *      items of which the state changed
     * @param stateChange
     *      the state change of the item, one of:
     *      {@code ItemEvent.SELECTED} or {@code ItemEvent.DESELECTED}
     */
    protected void fireItemStateChanged(Collection<VxComponent> items, int stateChange)
    {
        for (VxComponent el : items) {
            fireItemStateChanged(el, stateChange);
        }
    }

    
    // ---------------------------------------------------------------------- //
    //                              Event handlers                            //
    // ---------------------------------------------------------------------- //
    
    /**
     * GraphListener which handles changes to this GraphPanel's graph. Its 
     * only responsibility is to repaint the component when the graph is
     * modified, either by the GraphPanel itself or by an external object.
     */
    class MyGraphListener implements GraphListener
    {
        @Override
        public void elementAdded(GraphEvent e)
        {
            calculateSize();
            repaint();
        }

        @Override
        public void elementRemoved(GraphEvent e)
        {
            calculateSize();
            repaint();
        }

        @Override
        public void elementMoved(GraphEvent e)
        {
            calculateSize();
            repaint();
        }

        @Override
        public void styleChanged(GraphEvent e)
        {
            calculateSize();
            repaint();
        }
    }
    
    
    public static class DragNodeInfo extends MouseActionInfo
    {
        public VxNode draggedNode;
        public boolean nodeAlreadySelected;
        public boolean nodeDragged;
    }

    
    static class DragControlPointInfo extends MouseActionInfo
    {
        ControlPoint draggedCP;
    }

    
    static class RotatingSelectionInfo extends MouseActionInfo
    {
        Point2D anchor = new Point2D.Double();
        Point2D oldMousePos = new Point2D.Double();
        double rotation;
    }

    
    static class DraggingSelectionInfo extends MouseActionInfo
    {
        Point2D oldMousePos = new Point2D.Double();
    }

    
    static class SelectingInfo extends MouseActionInfo
    {
        Point2D startPos = new Point2D.Double();
    }
    

    /**
     * Checks if the mouse position (as given by mousePos) is within the 
     * current selection. If so, initializes a drag or a rotation (depending on
     * the mouse location) and returns true. Otherwise, the method returns
     * false.
     */
    private boolean checkAndInitializeDragOrRotate(int button)
    {
        if (selectionBox != null) {
            if (onRotateCorner(mousePos)) {
                currentAction = MouseAction.ROTATING_SELECTION;
                RotatingSelectionInfo info = new RotatingSelectionInfo();
                setOffset(info, 
                          (double)(selectionBox.getX() + selectionBox.getWidth()),
                          (double)(selectionBox.getY() + selectionBox.getHeight()),
                          mousePos);
                info.oldMousePos.setLocation(mousePos);
                info.anchor = new Point2D.Double(selectionBox.getCenterX(), 
                        selectionBox.getCenterY());
                info.rotation = 0;
                info.dragButton = button;
                actionInfo = info;
                return true;
            }
            else if (selectionBox.contains(mousePos)) {
                currentAction = MouseAction.DRAGGING_SELECTION;
                DraggingSelectionInfo info = new DraggingSelectionInfo();
                setOffset(info, selectionBox.getX(), selectionBox.getY(), mousePos);
                info.oldMousePos.setLocation(mousePos);
                info.dragButton = button;
                actionInfo = info;
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Checks if the selection consists of a single edge and if the mouse
     * position (given by mousePos) is on a control point of this edge. 
     * If so, initializes an edge move.
     */
    private boolean checkAndInitializeControlPointDrag(int button)
    {
        if (controlPoints != null) {
            for (ControlPoint cp : controlPoints) {
                if (cp.isUsed() && mousePos.distance(cp) < 3) {
                    currentAction = MouseAction.DRAGGING_CP;
                    DragControlPointInfo info = new DragControlPointInfo();
                    setOffset(info, cp.getX(), cp.getY(), mousePos);
                    info.draggedCP = cp;
                    info.dragButton = button;
                    actionInfo = info;
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Changes the selection to the component at the mouse position
     * (given by the global mousePos).
     */
    private void changeSelection(int button)
    {
        VxComponent el = drawer.getElementAt(graph.getAllGroup(), mousePos);
        
        if (el == null) {
            if (selection != null) {
                fireItemStateChanged(selection, ItemEvent.DESELECTED);
                selection = null;
            }
            controlPoints = null;
            selectionBox = null;
            if (multiSelect) {
                currentAction = MouseAction.SELECTING;
                SelectingInfo info = new SelectingInfo();
                info.startPos.setLocation(mousePos);
                info.dragButton = button;
                actionInfo = info;
            }
        }            
        else if (el instanceof VxNode) {
            VxNode node = (VxNode)el;
            controlPoints = null;
            boolean alreadySelected = false;
            if (selection != null) {
                if (selection.size() == 1) {
                    VxComponent selEl = selection.iterator().next();
                    if (selEl == el) alreadySelected = true;
                }
                fireItemStateChanged(selection, ItemEvent.DESELECTED);
            }
            selectionBox = null;
            if (nodesSelectable) {
                selection = Collections.singleton(el);
                fireItemStateChanged(el, ItemEvent.SELECTED);
            }
            else {
                selection = null;
            }
            if (nodesSelectable || nodesMovable) {
                currentAction = MouseAction.DRAGGING_NODE;
                DragNodeInfo info = new DragNodeInfo();
                Point2D nodePos = drawer.getPosition(node);
                setOffset(info, nodePos.getX(), nodePos.getY(), mousePos);
                info.draggedNode = node;
                info.nodeAlreadySelected = alreadySelected;
                info.nodeDragged = false;
                info.dragButton = button;
                actionInfo = info;
            }
        }
        else if (el instanceof VxEdge) {
            if (selection != null) {
                fireItemStateChanged(selection, ItemEvent.DESELECTED);
            }
            selectionBox = null;
            if (edgesMovable) {
                controlPoints = createControlPoints((VxEdge)el);
            }
            if (edgesSelectable) {
                selection = Collections.singleton(el);
                fireItemStateChanged(el, ItemEvent.SELECTED);
            }
            else {
                selection = null;
            }
        }
        
        repaint();
    }

    /**
     * Perform a dragging action on a node.
     */
    private void dragNode()
    {
        assert (actionInfo instanceof DragNodeInfo);
        DragNodeInfo info = (DragNodeInfo)actionInfo;
        if (info.draggedNode instanceof VxExplicitNode) {
            double x = mousePos.getX() - info.offsetX;
            double y = mousePos.getY() - info.offsetY;
            ((VxExplicitNode)info.draggedNode).position().setLocation(x, y);
            info.nodeDragged = true;
            calculateSize();
            repaint();
        }
    }

    /** 
     * Perform a dragging action on a control point.
     */
    private void dragControlPoint()
    {
        assert (actionInfo instanceof DragControlPointInfo);
        DragControlPointInfo info = (DragControlPointInfo)actionInfo;
        double x = mousePos.getX() - info.offsetX;
        double y = mousePos.getY() - info.offsetY;
        info.draggedCP.setLocation(x, y);
        calculateSize();
        repaint();
    }

    /**
     * Performs a dragging action while drawing a selection box.
     */
    private void dragWhenSelecting()
    {
        assert (actionInfo instanceof SelectingInfo);
        SelectingInfo info = (SelectingInfo)actionInfo;
        double x, y, width, height;
        
        if (info.startPos.getX() < mousePos.getX()) {
            x = info.startPos.getX();
            width = mousePos.getX() - info.startPos.getX();
        }
        else {
            x = mousePos.getX();
            width = info.startPos.getX() - mousePos.getX();
        }
        
        if (info.startPos.getY() < mousePos.getY()) {
            y = info.startPos.getY();
            height = mousePos.getY() - info.startPos.getY();
        }
        else {
            y = mousePos.getY();
            height = info.startPos.getY() - mousePos.getY();
        }
        
        if (selectionBox == null) {
            selectionBox = new Rectangle2D.Double(x, y, width, height);
        }
        else {
            selectionBox.setRect(x, y, width, height);
        }
        
        Rectangle2D.Double rect = new Rectangle2D.Double();
        rect.setRect(selectionBox);
        rect.x -= 10;
        rect.y -= 10;
        rect.width += 20;
        rect.height += 20;
        selection = drawer.getElementsIn(graph.getAllGroup(), rect);
        repaint();
    }
    
    /**
     * Perform a dragging action on the selection box.
     */
    private void dragSelection()
    {
        assert (actionInfo instanceof DraggingSelectionInfo);
        DraggingSelectionInfo info = (DraggingSelectionInfo)actionInfo;
        double dx = mousePos.getX() - info.oldMousePos.getX();
        double dy = mousePos.getY() - info.oldMousePos.getY();
        
        graph.startModification();
        for (VxComponent comp : selection) {
            if (comp instanceof VxExplicitNode) {
                VxExplicitNode xnode = (VxExplicitNode)comp;
                Point2D pos = xnode.position();
                pos.setLocation(pos.getX() + dx, pos.getY() + dy);
            }
        }
        graph.endModification();
        
        selectionBox.setRect(
                selectionBox.getX() + dx,
                selectionBox.getY() + dy,
                selectionBox.getWidth(),
                selectionBox.getHeight());
        info.oldMousePos.setLocation(mousePos);
        calculateSize();
        repaint();
    }
    
    /**
     * Perform a dragging action when rotating the selection box.
     */
    private void dragWhenRotating()
    {
        assert (actionInfo instanceof RotatingSelectionInfo);
        RotatingSelectionInfo info = (RotatingSelectionInfo)actionInfo;
        
        double oldAngle = Math.atan2(
                info.oldMousePos.getY() - info.anchor.getY(), 
                info.oldMousePos.getX() - info.anchor.getX());
        double newAngle = Math.atan2(
                mousePos.getY() - info.anchor.getY(), 
                mousePos.getX() - info.anchor.getX());
        double dif = GraphicsUtils.normalizeRelativeAngle(newAngle - oldAngle);
        info.rotation += dif;

        graph.startModification();
        double angle = -dif;
        double anchorX = info.anchor.getX();
        double anchorY = info.anchor.getY();
        double sina = Math.sin(angle);
        double cosa = Math.cos(angle);
        double msina = -sina;
        for (VxComponent comp : selection) {
            if (comp instanceof VxExplicitNode) {
                VxExplicitNode node = (VxExplicitNode)comp;
                double x = node.position().getX() - anchorX;
                double y = node.position().getY() - anchorY;
                node.position().setLocation(
                    x * cosa + y * sina + anchorX,
                    x * msina + y * cosa + anchorY
                    );
            }
            else if (comp instanceof VxEdge) {
                VxEdge edge = (VxEdge)comp;
                if (edge.isLoop()) {
                    double a = edge.getLoopAngle();
                    edge.setLoopAngle(GraphicsUtils.normalizeAngle(a - angle));
                }
            }
        }
        graph.endModification();

        info.oldMousePos.setLocation(mousePos);
        calculateSize();
        repaint();
    }
    
    /**
     * Handle press event.
     */
    protected void mousePressed(MouseEvent e)
    {
        int button = e.getButton();
        if (!checkAndInitializeDragOrRotate(button) &&
            !checkAndInitializeControlPointDrag(button)) {
            changeSelection(button);
        }
    }

    /**
     * Handle drag event.
     */
    protected void mouseDragged(MouseEvent e)
    {
        switch (currentAction) {
            case DRAGGING_NODE: 
                dragNode();
                break;
            case DRAGGING_CP: 
                dragControlPoint();
                break;
            case SELECTING: 
                dragWhenSelecting();
                break;
            case DRAGGING_SELECTION: 
                dragSelection();
                break;
            case ROTATING_SELECTION: 
                dragWhenRotating();
                break;
            default:
                // do nothing
        }
    }
    
    /**
     * Handle release event.
     */
    protected void mouseReleased(MouseEvent e)
    {
        switch (currentAction) {
            case SELECTING:
                if (selection != null) {
                    fireItemStateChanged(selection, ItemEvent.SELECTED);
                    updateSelectionBox();
                }
                break;
            case ROTATING_SELECTION:
                if (selection != null) {
                    updateSelectionBox();
                }
                break;
            default:
                // do nothing
        }
        currentAction = MouseAction.NONE;
        actionInfo = null;
        repaint();
    } 
    
    /**
     * Class which handles the mouse events for this GraphPanel.
     * (A nested class so that GraphPanel itself does not need to implement
     * the MouseListener and MouseMotionListener interfaces.)
     */
    private class MouseHandler extends MouseAdapter 
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            setMousePos(e.getPoint());          
            if (graph == null) return;
            AbstractGraphPanel.this.mousePressed(e);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            setMousePos(e.getPoint());
            if (graph == null) return;
            AbstractGraphPanel.this.mouseDragged(e);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            setMousePos(e.getPoint());
            AbstractGraphPanel.this.mouseReleased(e);
        }
    }
    
    
    // ---------------------------------------------------------------------- //
    //                     Control points for edges                           //
    // ---------------------------------------------------------------------- //
        
    /**
     * Abstract super class for the control points which specify the curve of 
     * an edge. The number of control points and the way in which they
     * specify the curve depend on the kind of curve under consideration.
     */
    private abstract static class ControlPoint extends Point2D
    {
        protected VxEdge edge;
        
        /**
         * Creates a new ControlPoint instance.
         * 
         * @param edge
         *      edge which is controlled by this control point
         */
        ControlPoint(VxEdge edge)
        {
            this.edge = edge;
        }
        
        /**
         * Returns an int which specifies the type of this control point. 
         * The type is an arbitrary integer which may be used by other classes
         * to make their behavior depending on the type of the control point.

         * @return
         *      the type of this control point
         */
        public int getControlPointType()
        {
            return 0;
        }
        
        /**
         * Determines whether this control point is used. Some control
         * points are composed from different control points and may not always
         * exist. If this method returns {@code true}, changing this 
         * point's location will change the edge's curve. 
         */
        public boolean isUsed() 
        {
            return true;
        }
    }
    
    
    /**
     * Control points of the cubic curve.
     */
    private class CurveControlPoint extends ControlPoint
    {
        private int cpNum;

        /**
         * Creates a new CurveControlPoint instance.
         * 
         * @param edge
         *      edge which is controlled by the new control point
         * @param cpNum
         *      control point number of the new control point
         */
        CurveControlPoint(VxEdge edge, int cpNum)
        {
            super(edge);
            assert !edge.isLoop();
            this.cpNum = cpNum;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX()
        {
            Shape shape = drawer.getEdgeLine(edge);
            
            if (shape instanceof Line2D) {
                Line2D line = (Line2D)shape;
                double dx = line.getX2() - line.getX1(); 
                if (cpNum == 1) {
                    return line.getX1() + dx * edge.getCurveOutStrength();
                }
                else {
                    assert (cpNum == 2);
                    return line.getX2() - dx * edge.getCurveInStrength();
                }
            }
            else if (shape instanceof CubicCurve2D) {
                CubicCurve2D curve = (CubicCurve2D)shape;
                if (cpNum == 1) {
                    return curve.getCtrlX1();
                }
                else {
                    assert (cpNum == 2);
                    return curve.getCtrlX2();
                }
            }
            else {
                assert false; // shouldn't happen
                return 0;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getY()
        {
            Shape shape = drawer.getEdgeLine(edge);
            
            if (shape instanceof Line2D) {
                Line2D line = (Line2D)shape;
                double dy = line.getY2() - line.getY1();
                if (cpNum == 1) {
                    return line.getY1() + dy * edge.getCurveOutStrength();
                }
                else {
                    assert (cpNum == 2);
                    return line.getY2() - dy * edge.getCurveInStrength();
                }
            }
            else if (shape instanceof CubicCurve2D) {
                CubicCurve2D curve = (CubicCurve2D)shape;
                if (cpNum == 1) {
                    return curve.getCtrlY1();
                }
                else {
                    assert (cpNum == 2);
                    return curve.getCtrlY2();
                }
            }
            else {
                assert false; // shouldn't happen
                return 0;
            }
        }

        @Override
        public void setLocation(double x, double y)
        {
            VxNode source = edge.getSource();
            VxNode target = edge.getTarget();
            Point2D srcPos = drawer.getPosition(source);
            Point2D tgtPos = drawer.getPosition(target);
            double dist = edge.isLoop() ? GraphDrawer.LOOP_DISTANCE : srcPos.distance(tgtPos);

            graph.startModification();
            if (cpNum == 1) {
                double angle = Math.atan2(tgtPos.getY() - srcPos.getY(), tgtPos.getX() - srcPos.getX());
                edge.setCurveOutAngle(Math.atan2(y - srcPos.getY(), x - srcPos.getX()) - angle);
                srcPos = drawer.getBorderPoint(source, drawer.getOutAngle(edge));
                tgtPos = drawer.getBorderPoint(target, drawer.getInAngle(edge));
                edge.setCurveOutStrength(srcPos.distance(x, y) / dist);
            }
            else {
                assert (cpNum == 2);
                double angle = Math.atan2(srcPos.getY() - tgtPos.getY(), srcPos.getX() - tgtPos.getX());
                edge.setCurveInAngle(Math.atan2(y - tgtPos.getY(), x - tgtPos.getX()) - angle);
                srcPos = drawer.getBorderPoint(source, drawer.getOutAngle(edge));
                tgtPos = drawer.getBorderPoint(target, drawer.getInAngle(edge));
                edge.setCurveInStrength(tgtPos.distance(x, y) / dist);
            }
            edge.notifyModification();
            graph.endModification();
        }

        public VxNode getAssociatedNode() {
            if (cpNum == 1) {
                return edge.getSource();
            } else {
                assert (cpNum == 2);
                return edge.getTarget();
            }
        }
    }
    
    
    /**
     * Virtual control point which is at the intersection of the line segments
     * from the source to the first control point and from the target to the
     * second control point.
     */
    private class CombinedCurveControlPoint extends ControlPoint
    {
        private Point2D cp1;
        private Point2D cp2;
        private Point2D pos = null;
        
        /**
         * Creates a new CombinedCurveControlPoint instance.
         */
        CombinedCurveControlPoint(VxEdge edge, Point2D cp1, Point2D cp2)
        {
            super(edge);
            assert !edge.isLoop();
            this.cp1 = cp1;
            this.cp2 = cp2;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int getControlPointType()
        {
            return 1;
        }
        
        /**
         * Updates the cached position in pos to the current coordinates based
         * on the position of the real curve control points.
         */
        private void updatePosition()
        {
            Point2D srcPos = drawer.getPosition(edge.getSource());
            Point2D tgtPos = drawer.getPosition(edge.getTarget());
            
            if (srcPos.equals(cp1) || tgtPos.equals(cp2)) {
                pos = null;
            }
            else {
                pos = ShapeUtils.getIntersection(srcPos, cp1, tgtPos, cp2, pos);
                if (pos == null) return;
                double srcx = srcPos.getX();
                double srcy = srcPos.getY();
                double tgtx = tgtPos.getX();
                double tgty = tgtPos.getY();
                double posx = pos.getX();
                double posy = pos.getY();
                if (Math.signum(posx - srcx) != Math.signum(cp1.getX() - srcx) ||
                    Math.signum(posy - srcy) != Math.signum(cp1.getY() - srcy) ||
                    Math.signum(posx - tgtx) != Math.signum(cp2.getX() - tgtx) ||
                    Math.signum(posy - tgty) != Math.signum(cp2.getY() - tgty)) {
                    pos = null;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUsed()
        {
            updatePosition();
            return (pos != null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX()
        {
            updatePosition();
            if (pos != null) {
                return pos.getX();
            }
            else {
                return 0;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getY()
        {
            updatePosition();
            if (pos != null) {
                return pos.getY();
            }
            else {
                return 0;
            }
        }

        /**
         * Changes the real control points so that the given new location is
         * at their intersection.
         */
        @Override
        public void setLocation(double x, double y)
        {
            Point2D srcPos = drawer.getPosition(edge.getSource());
            Point2D tgtPos = drawer.getPosition(edge.getTarget());
            double angle = Math.atan2(tgtPos.getY() - srcPos.getY(), tgtPos.getX() - srcPos.getX());
            edge.setCurveOutAngle(Math.atan2(y - srcPos.getY(), x - srcPos.getX()) - angle);
            angle = Math.atan2(srcPos.getY() - tgtPos.getY(), srcPos.getX() - tgtPos.getX());
            edge.setCurveInAngle(Math.atan2(y - tgtPos.getY(), x - tgtPos.getX()) - angle);
            edge.notifyModification();
        }
    }
    
    
    /**
     * Control point which is used to control the loop angle.
     */
    private class LoopControlPoint extends ControlPoint
    {
        private double dist = GraphDrawer.LOOP_DISTANCE *.5;

        /**
         * Creates a new LoopControlPoint instance.
         * 
         * @param edge
         *      edge which is controlled by this control point
         */
        LoopControlPoint(VxEdge edge)
        {
            super(edge);
            assert edge.isLoop();
        }
        
        /** 
         * Loop control points are "virtual" control points.
         */
        @Override
        public int getControlPointType()
        {
            return 1;
        }
        
        /**
         * Returns the x-coordinate of the loop control point.
         */
        @Override
        public double getX()
        {
            return drawer.getPosition(edge.getSource()).getX() + 
                   Math.cos(edge.getLoopAngle()) * dist;
        }

        /**
         * Return the y-coordinate of the loop control point.
         */
        @Override
        public double getY()
        {
            return drawer.getPosition(edge.getSource()).getY() + 
                   Math.sin(edge.getLoopAngle()) * dist;
        }

        /**
         * Sets the location of the loop control point. This method calculates
         * the angle and the distance and sets the corresponding attributes
         * accordingly.
         */
        @Override
        public void setLocation(double x, double y)
        {
            Point2D pos = drawer.getPosition(edge.getSource());
            double dx = x - pos.getX();
            double dy = y - pos.getY();
            edge.setLoopAngle(Math.atan2(dy, dx));
            dist = Math.hypot(dx, dy);
            edge.notifyModification();
        }
    }
}