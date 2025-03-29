package de.uni_due.inf.ti.graph.visual;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.visigraph.VxGraph;

/**
 * The style names to use when converting {@link Graph}s to {@link VxGraph}s.
 */
public class GraphStyleNames {
	private String nodeStyleName;
	private String hyperedgeStyleName;
	private String edgeStyleName;
	private String labelStyleName;
	private String connectionStyleName;
	private String arityStyleName;

	/**
	 * Creates a new instance with the default style names.
	 */
	public GraphStyleNames() {
		this.nodeStyleName       = "node";
		this.hyperedgeStyleName  = "hyperedge";
		this.edgeStyleName       = "edge";
		this.labelStyleName      = "label";
		this.connectionStyleName = "connection";
		this.arityStyleName      = "arity";
	}

	/**
	 * Creates a new instance with the style names copied from an existing one.
	 */
	public GraphStyleNames(GraphStyleNames copy) {
		this.nodeStyleName       = copy.getNodeStyleName();
		this.hyperedgeStyleName  = copy.getHyperedgeStyleName();
		this.edgeStyleName       = copy.getEdgeStyleName();
		this.labelStyleName      = copy.getLabelStyleName();
		this.connectionStyleName = copy.getConnectionStyleName();
		this.arityStyleName      = copy.getArityStyleName();
	}

	/**
	 * Appends the given string to all style names.
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames appendAll(String suffix) {
		this.nodeStyleName       += suffix;
		this.hyperedgeStyleName  += suffix;
		this.edgeStyleName       += suffix;
		this.labelStyleName      += suffix;
		this.connectionStyleName += suffix;
		this.arityStyleName      += suffix;
		return this;
	}

	/* Style name getters and setters (chainable) */

	/** @return the style name for graph nodes */
	public String getNodeStyleName() { return nodeStyleName; }
	/** @return the style name for the nodes representing a hyperedge */
	public String getHyperedgeStyleName() { return hyperedgeStyleName; }
	/** @return the style name for binary edges */
	public String getEdgeStyleName() { return edgeStyleName; }
	/** @return the style name for binary edge labels */
	public String getLabelStyleName() { return labelStyleName; }
	/** @return the style name for connections between graph nodes and the nodes representing hyperedges */
	public String getConnectionStyleName() { return connectionStyleName; }
	/** @return the style name for labels of hyperedge to node connections */
	public String getArityStyleName() { return arityStyleName; }

	/**
	 * Sets a new style name for graph nodes.
	 * @param nodeStyleName the new style name
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setNodeStyleName(String nodeStyleName) {
		this.nodeStyleName = nodeStyleName;
		return this;
	}
	/**
	 * Sets a new style name for the nodes representing a hyperedge.
	 * @param hyperedgeStyleName the hyperedgeStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setHyperedgeStyleName(String hyperedgeStyleName) {
		this.hyperedgeStyleName = hyperedgeStyleName;
		return this;
	}
	/**
	 * Sets a new style name for binary edges.
	 * @param edgeStyleName the edgeStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setEdgeStyleName(String edgeStyleName) {
		this.edgeStyleName = edgeStyleName;
		return this;
	}
	/**
	 * Sets a new style name for the labels of binary edges.
	 * @param labelStyleName the labelStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setLabelStyleName(String labelStyleName) {
		this.labelStyleName = labelStyleName;
		return this;
	}
	/**
	 * Sets a new style name for connections between graph nodes and the
	 * nodes representing hyperedges.
	 * @param connectionStyleName the connectionStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setConnectionStyleName(String connectionStyleName) {
		this.connectionStyleName = connectionStyleName;
		return this;
	}
	/**
	 * Sets a new style name for the labels of hyperedge to node connections.
	 * @param arityStyleName the arityStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphStyleNames setArityStyleName(String arityStyleName) {
		this.arityStyleName = arityStyleName;
		return this;
	}
}
