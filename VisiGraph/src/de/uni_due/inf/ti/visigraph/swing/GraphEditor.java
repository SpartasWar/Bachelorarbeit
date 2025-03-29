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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxNode;

/**
 * GraphPanel which can be used for editing graphs.
 * <p>
 * <em>Warning:</em> This class is in alpha stage.
 * 
 * @author H.J. Sander Bruggink
 */
public class GraphEditor extends AbstractGraphPanel
{
    private static final long serialVersionUID  = 3977012543930621889L;
    
    private static final String CANCEL_KEY      = "cancel-edit";
    
    public enum EditMode {
        SELECT_AND_MOVE,
        ADD_NODE,
        ADD_LABEL,
        ADD_EDGE
    }
    
    private static class DragNewEdgeInfo extends MouseActionInfo
    {
        VxNode source;
        Point2D endPos;
    }
    
    // Internal state
    
    private JTextField labelEditor = null;
    private VxNode editedNode = null;
    
    // Properties
    
    private GraphEditSynchronizer syncer;
    private EditMode editMode = EditMode.SELECT_AND_MOVE;
    private Color draggedEdgeColor = Color.GRAY;
    
    /**
     * Creates a new GraphEditor instance.
     */
    public GraphEditor()
    {
        this(null);
    }
    
    /**
     * Creates a new GraphEditor instance with a graph.
     * 
     * @param graph
     *      graph instance edited by the new GraphEditor
     */
    public GraphEditor(VxGraph graph)
    {
        super(graph);

        setNodesAddable(true);
        setEdgesAddable(true);
    }
    
    /**
     * Returns the edit synchronizer for this editor.
     */
    public GraphEditSynchronizer getEditSynchronizer()
    {
        if (syncer == null) {
            syncer = new BasicGraphEditSynchronizer();
        }
        return syncer;
    }

    /**
     * Sets the edit synchronizer for this editor. VxGraph objects are 
     * supposed to be visual representation of various underlying objects.
     * The edit synchronizer modifies the underlying object when the
     * visual representation is modified by the GraphEditor.
     * 
     * @param syncer
     *      the new edit synchronizer of this editor; if {@code null}, no
     *      edit synchronizer is used
     */
    public void setEditSynchronizer(GraphEditSynchronizer syncer)
    {
        this.syncer = syncer;
    }
    
    
    // ---------------------------------------------------------------------- //
    //                               User Actions                             //
    // ---------------------------------------------------------------------- //

    /** 
     * Adds a node to the graph of this GraphEditor at the specified point.
     * 
     * @param point
     *      coordinates at which the new node is to be added
     * @throws NullPointerException
     *      if {@code point} is {@code null}
     */
    public void addNode(Point2D point)
    {
        if (point == null) throw new NullPointerException();
        VxGraph g = getGraph();
        if (g == null) return;
        
        GraphEditSynchronizer s = getEditSynchronizer();
        if (s.canAddNode(g)) {
            s.addNode(g, point);
        }
    }

    /**
     * Adds an edge to the graph of this GraphEditor.
     * 
     * @param source
     *      source node of the edge to add
     * @param target
     *      target node of the edge to add
     * @throws NullPointerException
     *      if {@code source} or {@code target} is {@code null}
     */
    public void addEdge(VxNode source, VxNode target)
    {
        if (source == null || target == null) throw new NullPointerException();
        
        GraphEditSynchronizer s = getEditSynchronizer();
        if (s.canAddEdge(source, target)) {
            s.addEdge(source, target);
        }
    }
    
    /**
     * Adds a label to the graph of this GraphEditor.
     * 
     * @param edge
     *      edge to add a label to
     * @param label
     *      string of the label to add to the edge
     * @throws NullPointerException
     *      if {@code edge} or {@code label} is {@code null}
     */
    public void addLabel(VxEdge edge, String label) 
    {
        if (edge == null || label == null) throw new NullPointerException();
        
        GraphEditSynchronizer s = getEditSynchronizer();
        if (s.canAddLabel(edge)) {
            s.addLabel(edge, label);
        }
    }
    
    /**
     * Changes the label of a node.
     * 
     * @param node
     *      node to change the label of
     * @param newLabel
     *      new label of the node
     */
    public void editLabel(VxNode node, String newLabel) 
    {
        if (node == null || newLabel == null) throw new NullPointerException();
        
        GraphEditSynchronizer s = getEditSynchronizer();
        if (s.canEditLabel(node)) {
            s.editLabel(node, newLabel);
        }
    }
    
    /**
     * Checks whether a node is under the mouse cursor and initializes an
     * edge drag action.
     */
    private boolean checkAndInitializeEdgeDrag(int button)
    {
        VxComponent el = getDrawer().getElementAt(getGraph().getAllGroup(), mousePos);
        if (el != null && el instanceof VxNode) {
            GraphEditSynchronizer s = getEditSynchronizer();
            VxNode node = (VxNode)el;
            if (s.isPossibleSourceNode(node)) {
                DragNewEdgeInfo info = new DragNewEdgeInfo();
                info.dragButton = button;
                info.source = node;
                info.endPos = mousePos;
                actionInfo = info;
                currentAction = MouseAction.DRAGGING_EDGE;
                return true;                
            }
        }
        return false;
    }
    
    /**
     * Starts a label edit.
     * 
     * @param node
     * 		node of which the label is going to be edited
     */
    private void startLabelEdit(final VxNode node)
    {
        Point mid = calculatePhysicalCoordinate(getDrawer().getPosition(node));
        editedNode = node;
        labelEditor = new JTextField(node.getLabel());
        labelEditor.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelEditor);
        
        labelEditor.setBounds(mid.x - 20, mid.y - 10, 40, 20);
        
        labelEditor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                finishLabelEdit(true);
            }
        });
        
        InputMap im = labelEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL_KEY);
        ActionMap am = labelEditor.getActionMap();
        am.put(CANCEL_KEY, new AbstractAction() {

			private static final long serialVersionUID = -10731115184099540L;

			@Override
            public void actionPerformed(ActionEvent e)
            {
                finishLabelEdit(false);
            }
        });
        
        labelEditor.setVisible(true);
        labelEditor.requestFocusInWindow();
    }
    
    /**
     * Finished a label edit.
     * 
     * @param accept
     * 		whether or not the new label is accepted or discarded
     */
    private void finishLabelEdit(boolean accept)
    {
        if (accept) {
            editedNode.setLabel(labelEditor.getText());
        }
        editedNode = null;
        labelEditor.setVisible(false);
        this.remove(labelEditor);
        labelEditor = null;
    }
    
    /**
     * Called by the system when the mouse button is pressed.
     */
    @Override
    protected void mousePressed(MouseEvent e)
    {
        if (labelEditor != null) {
            finishLabelEdit(true);
        }
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() >= 2) {
                addNode(mousePos);
            }
            else {
                switch (editMode) {
                    case SELECT_AND_MOVE:
                        super.mousePressed(e);
                        break;
                    case ADD_NODE:
                        addNode(mousePos);
                        break;
                    case ADD_EDGE:
                        checkAndInitializeEdgeDrag(e.getButton());
                        break;
                    default:
                        // do nothing
                }
            }
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            checkAndInitializeEdgeDrag(e.getButton());
        }
    }
    
    /**
     * Called by the system when the mouse is moved while a button is being
     * pressed.
     */
    @Override
    protected void mouseDragged(MouseEvent e) 
    {
        if (labelEditor != null) {
            finishLabelEdit(true);
        }
        if (currentAction == MouseAction.DRAGGING_EDGE) {
            repaint();
        }
        else {
            super.mouseDragged(e);
        }
    }
    
    /**
     * Called by the system when the mouse button is released.
     */
    @Override
    protected void mouseReleased(MouseEvent e)
    {
        if (labelEditor != null) {
            finishLabelEdit(true);
        }
        
        switch (currentAction) {
            case DRAGGING_NODE: {
                assert actionInfo instanceof DragNodeInfo;
                DragNodeInfo info = (DragNodeInfo)actionInfo;
                if (info.nodeAlreadySelected && !info.nodeDragged) {
                    GraphEditSynchronizer s = getEditSynchronizer();
                    if (s.canEditLabel(info.draggedNode)) {
                        startLabelEdit(info.draggedNode);
                    } 
                    currentAction = MouseAction.NONE;
                    actionInfo = null;
                }
                else {
                    super.mouseReleased(e);
                }
                break;
            }
            case DRAGGING_EDGE: {
                assert actionInfo instanceof DragNewEdgeInfo;
                DragNewEdgeInfo info = (DragNewEdgeInfo)actionInfo;
                VxComponent el = getDrawer().getElementAt(getGraph().getAllGroup(), info.endPos);
                if (el != null && el instanceof VxNode) {
                    VxNode node = (VxNode)el;
                    addEdge(info.source, node);
                }
                currentAction = MouseAction.NONE;
                actionInfo = null;
                repaint();
                break;
            }
            default:
                super.mouseReleased(e);
        }        
    }
    
    /**
     * Paints the graph to this component.
     */
    @Override
    protected void paintGraph(Graphics2D g)
    {
        super.paintGraph(g);
        
        if (currentAction == MouseAction.DRAGGING_EDGE) {
            assert actionInfo instanceof DragNewEdgeInfo;
            DragNewEdgeInfo info = (DragNewEdgeInfo)actionInfo;
            Point2D borderPos = getDrawer().getBorderPoint(info.source, info.endPos);
            g.setColor(draggedEdgeColor);
            Line2D line = new Line2D.Double(borderPos, info.endPos);
            g.draw(line);
        }
        
        System.out.println();
    }
}
