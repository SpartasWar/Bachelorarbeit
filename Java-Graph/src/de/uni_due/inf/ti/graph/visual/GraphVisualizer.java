/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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

package de.uni_due.inf.ti.graph.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.Transition;
import de.uni_due.inf.ti.graph.visual.util.GraphToVx;
import de.uni_due.inf.ti.graph.visual.util.GraphToVxParams;
import de.uni_due.inf.ti.graph.visual.util.GraphToVxParams.EdgeMode;
import de.uni_due.inf.ti.graph.visual.util.GraphVxGroup;
import de.uni_due.inf.ti.graph.visual.util.VxToolbox;
import de.uni_due.inf.ti.visigraph.ArrowHead;
import de.uni_due.inf.ti.visigraph.Shape;
import de.uni_due.inf.ti.visigraph.Style;
import de.uni_due.inf.ti.visigraph.StyleMap;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxGroup;
import de.uni_due.inf.ti.visigraph.VxLabelNode;
import de.uni_due.inf.ti.visigraph.VxNode;

/**
 * Instances of this class convert Graph instances to VxGraph instances.
 * <p>
 * Note that the conversion only takes care of creating the required VxGraph
 * elements. The elements are not layouted in any particular way. The caller
 * must take care of this.
 *
 * @deprecated
 * This class is being phased out in favor of the {@link GraphToVx} and
 * {@link VxToolbox} classes, since the name more clearly states what it
 * does and what it does not do, and it offers additional methods to make
 * implementation of custom {@code visualizeThing} methods easier.
 *
 * @author H.J. Sander Bruggink
 */
@Deprecated
public class GraphVisualizer
{
    /**
     * Class used to return rule visualizations.
     */
    public static class RuleVisi 
    {
        private VxGraph visi;
        private VxGroup leftGroup;
        private VxGroup rightGroup;
        
        RuleVisi(VxGraph visi, VxGroup leftGroup, VxGroup rightGroup)
        {
            this.visi = visi;
            this.leftGroup = leftGroup;
            this.rightGroup = rightGroup;
        }
        
        public VxGraph getVxGraph()
        {
            return visi;
        }
        
        public VxGroup getLeftGroup()
        {
            return leftGroup;
        }
        
        public VxGroup getRightGroup()
        {
            return rightGroup;
        }
    }

    static GraphStyleNames graphStyleNames = new GraphStyleNames();
    static GraphStyleNames createdStyleNames = new GraphStyleNames().appendAll(".created");
    static GraphStyleNames deletedStyleNames = new GraphStyleNames().appendAll(".deleted");
    public static String morphismStyleName              = "morphism";
    public static String stateStyleName                 = "state";
    public static String transitionStyleName            = "transition";
    public static String boxStyleName                   = "box";
    public static String textnodeStyleName              = "textnode";
    public static String textnodeSymbolStyleName        = "textnode.bigsymbol";
    
    
    // Properties //
    
    /** Whether or not graphs are always visualized using hypergraph style. */
    private boolean hyper;
    
    /** Whether or not nodes are labeled with id numbers. */
    private boolean nodeLabels;
    
    /** Whether or not edges are labeled. */
    private boolean edgeLabels;
    
    /** Bounds for the positions of new elements.
     * @deprecated no users, only the default value is ever used. and
     * everyone who uses layouters does not care about it because they
     * position nodes indepdently
     */
    @Deprecated(forRemoval = true)
    private Rectangle2D bounds;
    
    
    /**
     * Creates a new GraphVisualizer instance.
     */
    public GraphVisualizer()
    {
        hyper = false;
        nodeLabels = true;
        edgeLabels = true;
        
        bounds = new Rectangle2D.Double(0, 0, 400, 400);
    }

	private static Style createTintedNodeStyle(Style baseNodeStyle, Color baseColor) {
		Color midBase = baseColor.darker();
		Color darkBase = midBase.darker();

		Style style = baseNodeStyle.createChild();
		style.setLineColor(darkBase);
		style.setLabelColor(darkBase);
		style.setFillColor(baseColor);
		style.setFillColor(true, midBase);

		return style;
	}

	private static Style createTintedLabelStyle(Style baseLabelStyle, Color baseColor) {
		Color midBase = baseColor.darker();
		Color darkBase = midBase.darker();

		Style style = baseLabelStyle.createChild();
		style.setLabelColor(midBase);
		style.setLabelColor(true, darkBase);

		return style;
	}

	private static Style createTintedEdgeStyle(Style baseEdgeStyle, Color baseColor) {
		Color midBase = baseColor.darker();

		Style style = baseEdgeStyle.createChild();
		style.setLineColor(baseColor);
		style.setLineColor(true, midBase);

		return style;
	}

	/**
	 * Adds default styles to the style names of this GraphVisualizer.
	 * For each style name, this method determines if a style is already set
	 * in the style map; if not, a suitable default is added to the style map.
	 *
	 * If the style map is partially initialized, then the additional styles
	 * will be derived from the existing styles, where applicable (for
	 * instance, if the map contains a style for nodes but not for deleted
	 * nodes, the new deleted node style will be based on the existing
	 * regular node style).
	 *
	 * @param styleMap
	 *      style map to add default style to
	 */
	public void addDefaultStyles(StyleMap styleMap) {
		Color white = Color.WHITE;
		Color gray = new Color(.9f, .9f, .9f);
		Color midGray = new Color(.6f, .6f, .6f);
		Color darkGray = new Color(.3f, .3f, .3f);
		Color morphismGreen = new Color(.2f, .8f, .2f);

		Color red = new Color(1f, .6f, .6f);
		Color midRed = new Color(.75f, .45f, .45f);
		Color blue = new Color(.6f, .6f, 1f);
		Color midBlue = new Color(.45f, .45f, .75f);

		BasicStroke defaultStroke = Style.getDefaultStroke(Style.LineWidth.MEDIUM, Style.DashType.SOLID);

		/* Node styles */

		Style generalNodeStyle = new Style();
		generalNodeStyle.setFilled(true);
		generalNodeStyle.setStroke(defaultStroke);

		Style nodeStyle = styleMap.computeIfAbsent(graphStyleNames.getNodeStyleName(), __ -> {
			Style s = generalNodeStyle.createChild();
			s.setLineColor(darkGray);
			s.setFillColor(gray);
			s.setFillColor(true, midGray);
			s.setLabelColor(darkGray);
			s.setLabelColor(true, white);
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getNodeStyleName(), createTintedNodeStyle(nodeStyle, blue));
		styleMap.putIfAbsent(deletedStyleNames.getNodeStyleName(), createTintedNodeStyle(nodeStyle, red));

		/* Hyperedge style (applied to VxNodes) */

		Style hyperedgeStyle = styleMap.computeIfAbsent(graphStyleNames.getHyperedgeStyleName(), __ -> {
			Style s = generalNodeStyle.createChild();
			s.setLineColor(darkGray);
			s.setFillColor(gray);
			s.setFillColor(true, midGray);
			s.setShape(Shape.createRectangle(40, 20));
			s.setFlexibleShape(true);
			s.setStroke(defaultStroke);
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getHyperedgeStyleName(), createTintedNodeStyle(hyperedgeStyle, blue));
		styleMap.putIfAbsent(deletedStyleNames.getHyperedgeStyleName(), createTintedNodeStyle(hyperedgeStyle, red));

		/* normal edges */

		Style edgeStyle = styleMap.computeIfAbsent(graphStyleNames.getEdgeStyleName(), __ -> {
			Style s = new Style();
			s.setLineColor(darkGray);
			s.setLineColor(true, midGray);
			s.setFrontArrow(ArrowHead.newTriangleArrowHead(8, 8));
			s.setStroke(defaultStroke);
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getEdgeStyleName(), createTintedEdgeStyle(edgeStyle, blue));
		styleMap.putIfAbsent(deletedStyleNames.getEdgeStyleName(), createTintedEdgeStyle(edgeStyle, red));

		/* edges connecting nodes with hyperedge VxNodes */

		Style connectionStyle = styleMap.computeIfAbsent(graphStyleNames.getConnectionStyleName(), __ -> {
			Style s = new Style();
			s.setLineColor(midGray);
			s.setLineColor(true, darkGray);
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getConnectionStyleName(), createTintedEdgeStyle(connectionStyle, midBlue));
		styleMap.putIfAbsent(deletedStyleNames.getConnectionStyleName(), createTintedEdgeStyle(connectionStyle, midRed));

		/* Label styles (of edges) */

		Style labelStyle = styleMap.computeIfAbsent(graphStyleNames.getLabelStyleName(), __ -> {
			Style s = new Style();
			s.setShape(Shape.createRectangle(10, 5));
			s.setFlexibleShape(true);
			s.setDrawn(false);
			s.setFilled(false);
			s.setLabelColor(darkGray);
			s.setLabelColor(true, darkGray);
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getLabelStyleName(), createTintedLabelStyle(labelStyle, blue));
		styleMap.putIfAbsent(deletedStyleNames.getLabelStyleName(), createTintedLabelStyle(labelStyle, red));

		Style arityStyle = styleMap.computeIfAbsent(graphStyleNames.getArityStyleName(), __ -> {
			Style s = new Style(styleMap.get(graphStyleNames.getLabelStyleName()));
			s.setLabelFont(s.getLabelFont().deriveFont(s.getLabelFont().getSize() * 0.8f));
			return s;
		});

		styleMap.putIfAbsent(createdStyleNames.getArityStyleName(), createTintedLabelStyle(arityStyle, blue));
		styleMap.putIfAbsent(deletedStyleNames.getArityStyleName(), createTintedLabelStyle(arityStyle, red));

		/* Morphism mapping arrows */

		Style morphismStyle = new Style();
		morphismStyle.setStroke(new BasicStroke(
			Style.LineWidth.MEDIUM.getValue(),
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
			new float[] { 4.0f, 5.0f }, 0f));
		morphismStyle.setLineColor(morphismGreen);
		morphismStyle.setFrontArrow(ArrowHead.newLineArrowHead(6, 6));
		styleMap.putIfAbsent(morphismStyleName, morphismStyle);

		/* Styles for transition systems */

		Style stateStyle = new Style();
		stateStyle.setFilled(true);
		stateStyle.setFillColor(false, gray);
		stateStyle.setFillColor(true, midGray);
		stateStyle.setLabelColor(false, gray);
		stateStyle.setLabelColor(true, white);
		stateStyle.setShape(Shape.createRoundedRectangle(15, 15, 5));
		stateStyle.setFlexibleShape(true);
		styleMap.putIfAbsent(stateStyleName, stateStyle);

		Style transitionStyle = new Style();
		transitionStyle.setFrontArrow(ArrowHead.newLineArrowHead(6, 6));
		styleMap.putIfAbsent(transitionStyleName, transitionStyle);

		/* Auxiliary element styles */

		Style boxStyle = new Style();
		boxStyle.setStroke(defaultStroke);
		boxStyle.setLineColor(Color.LIGHT_GRAY);
		boxStyle.setLabelColor(Color.LIGHT_GRAY);
		boxStyle.setFilled(false);
		boxStyle.setInset(10.0);
		styleMap.putIfAbsent(boxStyleName, boxStyle);

		Style textnodeStyle = styleMap.computeIfAbsent(textnodeStyleName, __ -> {
			Style s = new Style();
			s.setLabelColor(Color.DARK_GRAY);
			s.setFilled(false);
			s.setDrawn(false);
			s.setMinimumWidth(25.0);
			s.setMinimumHeight(25.0);
			return s;
		});

		Style bigtextnodeStyle = new Style(textnodeStyle);
		bigtextnodeStyle.setLabelFont(textnodeStyle.getLabelFont().deriveFont(22.0f));
		bigtextnodeStyle.setLabelColor(textnodeStyle.getLabelColor().brighter().brighter());
		styleMap.putIfAbsent(textnodeSymbolStyleName, bigtextnodeStyle);
    }

    
    /**
     * Sets the bounds of the generated graphs. The generated graphs will have
     * random positions within these bounds.
     * 
     * @param x
     *      x-coordinate of the top-left corner of the bounding rectangle
     * @param y
     *      y-coordinate of the top-left corner of the bounding rectangle
     * @param width
     *      width of the bounding rectangle
     * @param height
     *      height of the bounding rectangle
     */
	@Deprecated(forRemoval = true)
    public void setBounds(double x, double y, double width, double height)
    {
        bounds.setRect(x, y, width, height);
    }

    
    /**
     * Sets the bounds of the generated graphs to a rectangle. The generated 
     * graphs will have random positions within these bounds.
     * 
     * @param rect
     *      the new bounding rectangle
     */
	@Deprecated(forRemoval = true)
    public void setBounds(Rectangle2D rect)
    {
        bounds.setRect(rect);
    }
    
    
    /**
     * Returns the bounds of the generated graphs. If the second parameter is
     * non-null, it is modified to hold the bounds and returned. If it is
     * null, a new {@link Rectangle2D} object holding the bounds is created and
     * returned. 
     * 
     * @param result
     *      buffer to hold the return value
     * @return
     *      rectangle in which new elements are created by this
     *      GraphVisualizer
     */
	@Deprecated(forRemoval = true)
    public Rectangle2D getBounds(Rectangle2D result)
    {
        if (result == null) {
            result = new Rectangle2D.Double();
        }
        result.setRect(bounds);
        return result;
    }

    
    /**
     * Returns the bounds of the generated graphs. 
     * 
     * @param result
     *      buffer to hold the return value
     * @return
     *      rectangle in which new elements are created by this
     *      GraphVisualizer
     */
	@Deprecated(forRemoval = true)
    public Rectangle2D getBounds()
    {
        return getBounds(null);
    }

    
    /**
     * Sets whether this GraphVisualizer will always generated hypergraph
     * representations. If {@code true}, the GraphVisualizer will generate
     * hypergraph representations of graphs which contain only binary edges.
     * 
     * @param h
     *      whether this GraphVisualizer will always generate hypergraph
     *      representations
     */
    public void setHyperStyle(boolean h)
    {
        hyper = h;
    }
    
    
    /**
     * Returns whether this GraphVisualizer will always generate hypergraph
     * representations.
     * 
     * @see #setHyperStyle(boolean)
     */
    public boolean hasHyperStyle()
    {
        return hyper;
    }
    
    
    /**
     * Sets whether graph visualizations have node labels. If set to true,
     * graph visualizations created afterwards will have the nodes' id numbers
     * as labels.
     * <p>
     * The default value is {@code true}.
     * 
     * @param nodeLabels
     *      whether or not nodes will have their id numbers as labels
     * @see #hasNodeLabels()
     */
    public void setNodeLabels(boolean nodeLabels)
    {
        this.nodeLabels = nodeLabels;
    }
    

    /**
     * Determines whether graph visualizations have node labels.
     *
     * @return 
     *      whether graph visualizations created by this GraphVisualizer have
     *      node labels
     * @see #setNodeLabels(boolean)
     */
    public boolean hasNodeLabels()
    {
        return nodeLabels;
    }
    
    
    /**
     * Sets whether graph visualizations have edge labels. If set to 
     * {@code true}, edges in graph visualization created with this 
     * GraphVisualizer afterwards will have labels. Since edge labels are 
     * semantically significant in the graphs modelled by {@link Graph Graph} 
     * objects, setting this property to {@code false} is mainly useful if it
     * is known that only one label can occur in a graph. 
     * <p>
     * The default value of this property is {@code true}.
     *  
     * @param edgeLabels
     *      whether or not edges will have labels
     * @see #hasEdgeLabels()
     */
    public void setEdgeLabels(boolean edgeLabels)
    {
        this.edgeLabels = edgeLabels;
    }
    
    
    /**
     * Determines whether graph visualizations have edge labels.
     * 
     * @return
     *      whether graph visualizations created by this GraphVisualizer have
     *      edge labels
     * @see #setEdgeLabels(boolean)
     */
    public boolean hasEdgeLabels()
    {
        return edgeLabels;
    }
    
    
    /**
     * Worker method to visualize a graph. This method is called by various
     * other methods which need to visualize a graph in different situations.
     * 
     * @param input
     *      input graph
     * @param output
     *      output VxGraph; the visual representation of graph is added to 
     *      this
     * @param hyper
     *      whether to use hypergraph style
     * @param nodeMap
     *      map where node connections are stored; should be a newly created
     *      empty map or {@code null}
     * @param edgeMap
     *      map where edge connections are stored; should be a newly created
     *      empty map or {@code null}
     * @param bounds
     *      bounds of the graph; if {@code null}, the bounds from the
     *      bounds attribute are used
     * @param group
     *      group where newly added nodes and edges are added to; 
     *      can be {@code null}
     */
    private void visualizeGraph(
            Graph input, 
            VxGraph output, 
            boolean hyper,
            Map<Node,VxNode> nodeMap,
            Map<Edge,VxComponent> edgeMap,
            Rectangle2D bounds,
            VxGroup group)
    {
        if (group == null) group = output.createGroup();
        if (bounds == null) bounds = this.bounds;

        GraphToVxParams params = new GraphToVxParams();
        params.setCreateNodeLabels(nodeLabels);
        params.setCreateEdgeLabels(edgeLabels);
        params.setEdgeMode(hyper ? EdgeMode.AlwaysHyperedges : EdgeMode.AlwaysBinary);
        params.setGraphStyleNames(graphStyleNames);

        GraphVxGroup res = GraphToVx.addGraphToGroup(input, group, params);
        // randomize node positions
        for (VxExplicitNode vxn : res.getNodeMap().values()) {
            vxn.position().setLocation(randomPos(bounds));
        }
        if (hyper) {
            // place hyperedges (if any) in the middle of the related nodes
            for (Entry<Edge, VxComponent> kv : res.getEdgeMap().entrySet()) {
                assert kv.getValue() instanceof VxExplicitNode;
                Edge edge = kv.getKey();
                VxExplicitNode vxe = (VxExplicitNode) kv.getValue();
                VxNode[] nodes = edge.getNodes().stream().map(n -> res.getNodeMap().get(n)).toArray(VxNode[]::new);
                vxe.position().setLocation(getBarycenter(nodes));
            }
        }

        // take care of nodeMap, edgeMap
        if (nodeMap != null) nodeMap.putAll(res.getNodeMap());
        if (edgeMap != null) edgeMap.putAll(res.getEdgeMap());
    }
    
    
    /**
     * Creates a VxGraph which visualizes a graph.
     * <p>
     * If either {@code getHyperStyle() == true}, or this graph contains
     * non-binary edges, the graph is visualized using a hypergraph 
     * representation; otherwise an ordinary graph representation is used.
     * <p>
     * The nodes and hyperedges of the graphical representation are put on
     * arbitrary positions, whereas all edges will be straight lines from the 
     * source to the target node. In most cases, it will be necessary to layout
     * the resulting graphical representation, either manually or with one
     * of the graph layout algorithm provided with the VisiGraph library.
     * <p>
     * The result visualization uses the following style names:
     * <ul>
     * <li><tt>nodeStyleName</tt>:
     *     the style for the nodes of the graph
     * <li><tt>edgeStyleName</tt>:
     *     the style for the edges in the graph (only in ordinary graph
     *     representations)
     * <li><tt>hyperedgeStyleName</tt>:
     *     the style for hyperedges in the graph (only in hypergraph
     *     representations)
     * <li><tt>connectionStyleName</tt>:
     *     the style for connections between hyperedges and nodes (only in 
     *     hypergraph representations)
     * <li><tt>labelStyleName</tt>:
     *     the style of labels of edges and connections 
     * </ul>  
     * 
     * @param graph
     *      the graph of which a visualization is generated
     * @return
     *      new VxGraph instance which visualizes the graph
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    public VxGraph visualizeGraph(Graph graph)
    {
        if (graph == null) {
            throw new NullPointerException();
        }
        
        VxGraph result = new VxGraph();
        visualizeGraph(
                graph, 
                result, 
                hyper || !graph.isBinary(), 
                null, null, null, null);
        return result;
    }
    
    
    /**
     * Creates a VxGraph which visualizes a rule. The visual representation
     * will consist of the left-hand side and the right-hand side with 
     * explicit morphism arrows between nodes which are mapped to one another
     * by the correspondence morphism. The parts of the representation 
     * belonging to the left-hand side and right-hand side are added to the
     * {@code leftGroup} and {@code rightGroup} groups, respectively.
     * <p>
     * If either {@code getHyperStyle() == true}, this rule contains
     * non-binary edges, or the correspondence morphism maps at least one edge
     * of the left-hand side to one edge in the right-hand side, the graph is 
     * visualized using a hypergraph representation; otherwise an ordinary
     * graph representation is used.
     * <p>
     * The nodes and hyperedges of the graphical representation are put on
     * random positions, whereas all edges will be straight lines from the 
     * source to the target node. In most cases, it will be necessary to layout
     * the resulting graphical representation, either manually or with one
     * of the graph layout algorithm provided with the VisiGraph library.
     * <p>
     * The resulting visualization uses the following style names:
     * <ul>
     * <li><tt>nodeStyleName</tt>:
     *     the style for the nodes of the graph
     * <li><tt>edgeStyleName</tt>:
     *     the style for the edges in the graph (only in ordinary graph
     *     representations)
     * <li><tt>hyperedgeStyleName</tt>:
     *     the style for hyperedges in the graph (only in hypergraph
     *     representations)
     * <li><tt>connectionStyleName</tt>:
     *     the style for connections between hyperedges and nodes (only in 
     *     hypergraph representations)
     * <li><tt>labelStyleName</tt>:
     *     the style of labels of edges and connections 
     * <li><tt>morphismStyleName</tt> (edge style):
     *     the style for the morphism arrows
     * </ul>  
     * 
     * @param morph
     *      the morphism of which a visualization is generated
     * @return
     *      RuleVisi which contains a new VxGraph instance visualizing the 
     *      morphism, and the groups which contain the nodes and edges of the 
     *      domain and codomain respectively
     * @throws NullPointerException
     *      if {@code morph} is {@code null}
     */
    public RuleVisi visualizeMorphism(Morphism morph)
    {
        if (morph == null) {
            throw new NullPointerException();
        }
        
        Graph left = morph.getDomain();
        Graph right = morph.getCodomain();
        boolean edgesMapped = !morph.getEdgeMap().isEmpty();
        
        boolean hyper = this.hyper || !left.isBinary() || !right.isBinary()
                || edgesMapped;
        
        double halfWidth = bounds.getWidth() / 2;
        Rectangle2D leftBnds = new Rectangle2D.Double(
                bounds.getX(), bounds.getY(), halfWidth, bounds.getHeight());
        Rectangle2D rightBnds = new Rectangle2D.Double(
                bounds.getX() + halfWidth, bounds.getY(), halfWidth, bounds.getHeight());
        
        Map<Node,VxNode> leftNodeMap = new HashMap<>();
        Map<Node,VxNode> rightNodeMap = new HashMap<>();
        Map<Edge,VxComponent> leftEdgeMap = null;
        Map<Edge,VxComponent> rightEdgeMap = null;
        if (edgesMapped) {
            leftEdgeMap = new HashMap<>();
            rightEdgeMap = new HashMap<>();
        }
        
        VxGraph result = new VxGraph();
        VxGroup leftGroup = result.createGroup();
        VxGroup rightGroup = result.createGroup();
        
        visualizeGraph(left, result, hyper, leftNodeMap, leftEdgeMap, leftBnds, leftGroup);
        visualizeGraph(right, result, hyper, rightNodeMap, rightEdgeMap, rightBnds, rightGroup);
        
        for (Map.Entry<Node,Node> entry : morph.getNodeMap().entrySet()) {
            VxNode src = leftNodeMap.get(entry.getKey());
            VxNode tgt = rightNodeMap.get(entry.getValue());
            result.addEdge(src, tgt, morphismStyleName);
        }
        
        if (edgesMapped) {
            for (Map.Entry<Edge,Edge> entry : morph.getEdgeMap().entrySet()) {
                VxNode src = (VxNode)leftEdgeMap.get(entry.getKey());
                VxNode tgt = (VxNode)rightEdgeMap.get(entry.getValue());
                result.addEdge(src, tgt, morphismStyleName);
            }
        }
        
        return new RuleVisi(result, leftGroup, rightGroup);
    }

    
    /**
     * Creates a VxGraph which visualizes a rule, where the rule is displayed
     * as a single graph with different style for deleted and created parts.
     * For this visualization to work, the rule must have an injective
     * correspondence morphism.
     * <p>
     * If either {@code getHyperStyle() == true}, or this rule contains
     * non-binary edges, the rule is visualized using a hypergraph 
     * representation; otherwise an ordinary graph representation is used.
     * <p>
     * The nodes and hyperedges of the graphical representation are put on
     * random positions, whereas all edges will be straight lines from the 
     * source to the target node. In most cases, it will be necessary to layout
     * the resulting graphical representation, either manually or with one
     * of the graph layout algorithms provided with the VisiGraph library.
     * <p>
     * The result visualization uses the styles associated with the following
     * style identifiers:
     * <ul>
     * <li><tt>createNodeStyleName</tt>:
     *     for nodes which are in the right-hand side of the rule, but have no
     *     corresponding node in the left-hand side, that is, nodes 
     *     <em>created</em> by this rule
     * <li><tt>deletedNodeStyleName</tt>:
     *     for nodes which are in the left-hand side of the rule, but have no
     *     corresponding node in the right-hand side, that is, nodes 
     *     <em>deleted</em> by this rule
     * <li><tt>nodeStyleName</tt>:
     *     for nodes which are neither created nor deleted by the rule
     * <li><tt>createdEdgeStyleName</tt>:
     *     for edges which are created by this rule (only in ordinary
     *     graph representation)
     * <li><tt>deletedEdgeStyleName</tt>:
     *     for edge which are deleted by this rule (only in ordinary 
     *     graph representation)
     * <li><tt>edgeStyleName</tt>:
     *     for edges which are neither deleted nor created by this rule
     * <li><tt>createdHyperedgeStyleName</tt>:
     *     for hyperedges which are created by this rule (only in hypergraph
     *     representation)
     * <li><tt>deletedHyperedgeStyleName</tt>:
     *     for hyperedges which are deleted by this rule (only in hypergraph
     *     representation)
     * <li><tt>hyperedgeStyle</tt>:
     *     for hyperedges which are neither created nor deleted by this rule
     *     (only in hypergraph representation)
     * <li><tt>createdConnectionStyleName</tt>:
     *     for connections between hyperedge and node, for hyperedges which
     *     are created by the rule (only in hypergraph representation)
     * <li><tt>deletedConnectionStyleName</tt> (edge style):
     *     for connections between hyperedge and node, for hyperedges which
     *     are deleted by the rule (only in hypergraph representation)
     * <li><tt>connectionStyleName</tt> (edge style):
     *     for connections between hyperedge and node, for hyperedges which
     *     are neither deleted nor created by this rule (only in hypergraph
     *     representation)
     * <li><tt>createdLabelStyleName</tt>:
     *     for labels of edges and connections which are created by the rule
     * <li><tt>deletedLabelStyleName</tt>:
     *     for labels of edges and connections which are deleted by the rule
     * <li><tt>labelStyleName</tt>:
     *     for labels of edges and connections which are neither created nor
     *     deleted by the rule
     * </ul>  
     * 
     * @param rule
     *      the rule to be visualized
     * @return
     *      a visual representation of the rule
     * @throws NullPointerException
     *      if {@code rule} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code rule} does not have an injective correspondence
     *      morphism
     */
    public VxGraph visualizeFlatMorphism(Morphism morph) 
    {
        Graph left = morph.getDomain();
        Graph right = morph.getCodomain();
        
        if (!morph.isInjective()) {
            throw new IllegalArgumentException("Morphism must be injective.");
        }
        
        boolean hyper = this.hyper || !left.isBinary() || !right.isBinary();

        Map<Node,VxNode> nodeMap = new HashMap<>();
        
        VxGraph result = new VxGraph();
        
        for (Node node : left.getNodes()) {
            Point2D pos = randomPos();
            String sn = morph.getNodeMap().containsKey(node)
                      ? graphStyleNames.getNodeStyleName()
                      : deletedStyleNames.getNodeStyleName();
            VxNode vxn = result.addNode(
                    pos.getX(), 
                    pos.getY(), 
                    nodeLabels ? Integer.toString(node.getId()) : null,
                    sn);
            vxn.setReference(node);
            nodeMap.put(node, vxn);
        }
        
        for (Edge edge : left.getEdges()) {
            List<Node> nodes = edge.getNodes();
            
            GraphStyleNames eo = morph.getEdgeMap().containsKey(edge) ? graphStyleNames : deletedStyleNames;
            String esn = eo.getEdgeStyleName();
            String hsn = eo.getHyperedgeStyleName();
            String csn = eo.getConnectionStyleName();
            String lsn = eo.getLabelStyleName();

            if (hyper) {
                VxNode[] vxs = new VxNode[nodes.size()];
                for (int i = 0; i < nodes.size(); i++) {
                    vxs[i] = nodeMap.get(nodes.get(i));
                }
                VxNode hyperEdge = result.addNode(
                        getBarycenter(vxs),
                        edgeLabels ? edge.getLabel().getName() : null,
                        hsn);
                hyperEdge.setReference(edge);
                for (int i = 0; i < vxs.length; i++) {
                    VxEdge newEdge = result.addEdge(
                            hyperEdge,
                            vxs[i],
                            csn);
                    VxLabelNode label = newEdge.addLabel(Integer.toString(i));
                    label.setStyle(lsn);
                    label.setPositionOnEdge(.3);
                }
            }
            else {
                VxNode source = nodeMap.get(nodes.get(0));
                VxNode target = nodeMap.get(nodes.get(1));
                assert (source != null && target != null);
                VxEdge vxe = result.addEdge(source, target, esn);
                if (edgeLabels) {
                    VxLabelNode label = vxe.addLabel(edge.getLabel().getName());
                    label.setStyle(lsn);
                }
                vxe.setReference(edge);
            }
        }
        
        for (Node node : right.getNodes()) {
            if (!morph.inRange(node)) {
                Point2D pos = randomPos();
                VxNode vxn = result.addNode(
                        pos.getX(), 
                        pos.getY(), 
                        nodeLabels ? Integer.toString(node.getId()) : null,
                        createdStyleNames.getNodeStyleName());
                vxn.setReference(node);
                nodeMap.put(node, vxn);
            }
        }
        
        Morphism corrInv = morph.getInverse();
        
        for (Edge edge : right.getEdges()) {
            if (!morph.inRange(edge)) {
                List<Node> nodes = edge.getNodes();
                if (hyper) {
                    VxNode[] vxs = new VxNode[nodes.size()];
                    for (int i = 0; i < nodes.size(); i++) {
                        Node n = nodes.get(i);
                        if (morph.inRange(n)) {
                            n = corrInv.get(n);
                        }
                        assert n != null;
                        vxs[i] = nodeMap.get(n);
                    }
                    VxNode hyperEdge = result.addNode(
                            edgeLabels ? edge.getLabel().getName() : null,
                            createdStyleNames.getHyperedgeStyleName());
                    hyperEdge.setReference(edge);
                    for (int i = 0; i < vxs.length; i++) {
                        VxEdge newEdge = result.addEdge(
                                hyperEdge,
                                vxs[i],
                                createdStyleNames.getConnectionStyleName());
                        VxLabelNode label = newEdge.addLabel(Integer.toString(i));
                        label.setStyle(createdStyleNames.getLabelStyleName());
                        label.setPositionOnEdge(.3);
                    }
                }
                else {
                    Node src = edge.getNodes().get(0);
                    if (morph.inRange(src)) {
                        src = corrInv.get(src);
                    }
                    Node tgt = edge.getNodes().get(1);
                    if (morph.inRange(tgt)) {
                        tgt = corrInv.get(tgt);
                    }
                    VxNode vxSrc = nodeMap.get(src);
                    VxNode vxTgt = nodeMap.get(tgt);
                    assert (vxSrc != null && vxTgt != null);
                    VxEdge vxe = result.addEdge(
                            vxSrc, 
                            vxTgt, 
                            createdStyleNames.getEdgeStyleName());
                    if (edgeLabels) {
                        VxLabelNode label = vxe.addLabel(edge.getLabel().getName());
                        label.setStyle(createdStyleNames.getLabelStyleName());
                    }
                    vxe.setReference(edge);
                }
            }
        }
        
        return result;
    }
    
    
    /**
     * Visualizes a transition system. This method generates a VxGraph
     * representing the labeled transition system of the given system. It does
     * not visualize the objects of the transition system.
     * <p>
     * This method ignores the property set by {@link #setHyperStyle(boolean)}
     * and will always produce binary, directed graphs.
     * <p>
     * The result visualization uses the styles associated with the following
     * style identifiers:
     * <ul>
     * <li><tt>stateStyleName</tt> (node style):
     *     The style used for the objects in the transition system.
     * <li><tt>transitionStyleName</tt> (edge style):
     *     The style used for the transitions in the transition system.
     * </ul>
     * 
     * @param system
     *      system for which a visual representation is to be generated
     * @return
     *      VxGraph which represents the given transition system
     */
    public VxGraph visualizeSystem(ExplicitTransitionSystem system)
    {
        Map<Graph,VxNode> nodes = new HashMap<>();
        VxGraph visi = new VxGraph();
        
        for (Transition t : system.getTransitions()) {
            Graph src = t.getSource();
            VxNode srcNode = nodes.get(src);
            if (srcNode == null) {
                Point2D pos = randomPos();
                srcNode = visi.addNode(pos, src.getName(), stateStyleName);
                srcNode.setReference(src);
                nodes.put(src, srcNode);
            }
            Graph tgt = t.getTarget();
            VxNode tgtNode = nodes.get(tgt);
            if (tgtNode == null) {
                Point2D pos = randomPos();
                tgtNode = visi.addNode(pos, tgt.getName(), stateStyleName);
                tgtNode.setReference(tgt);
                nodes.put(tgt, tgtNode);
            }
            VxEdge te = visi.addEdge(srcNode, tgtNode, transitionStyleName);
            te.addLabel(t.getLabel());
            te.setReference(t);
        }
        
        return visi;
    }

    // ---------------------------------------------------------------------- //
    //                       Private utility methods                          //
    // ---------------------------------------------------------------------- //
    
    /**
     * Returns a random position within this GraphVisualizer's bounds.
     */
    private Point2D randomPos()
    {
        return randomPos(bounds);
    }

    
    /**
     * Returns a random position within user-specified bounds.
     */
    private static Point2D randomPos(Rectangle2D bounds)
    {
        return new Point2D.Double(
                bounds.getX() + Math.random() * bounds.getWidth(),
                bounds.getY() + Math.random() * bounds.getHeight());
    }
    
    
    /**
     * Returns the barycenter of an array of nodes, or a random position if
     * one of the nodes has no explicit position.
     */
    private Point2D getBarycenter(VxNode[] nodes) 
    {
        double x = 0;
        double y = 0;
        int c = 0;
        
        for (VxNode node : nodes) {
            if (node instanceof VxExplicitNode) {
                VxExplicitNode enode = (VxExplicitNode)node;
                Point2D pos = enode.position();
                x += pos.getX();
                y += pos.getY();
                c++;
            }
            else {
                return randomPos();
            }
        }
        
        return new Point2D.Double(x / (double)c, y / (double)c);
    }
}
