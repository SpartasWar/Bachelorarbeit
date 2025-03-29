package de.uni_due.inf.ti.graph.visual.util;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.visual.GraphStyleNames;
import de.uni_due.inf.ti.visigraph.VxBox;
import de.uni_due.inf.ti.visigraph.VxGraph;

/**
 * Contains settings on how to convert {@link Graph}s to {@link VxGraph}s,
 * like the style names to use or whether all edges should be rendered as
 * hyperedges.
 */
public class GraphToVxParams {
	/**
	 * Describes how edges should be rendered (always as binary edges,
	 * always as hyperedges, decide automatically, ...)
	 */
	public static enum EdgeMode {
		/**
		 * Automatic mode. A graph will be rendered with binary edges (as if
		 * mode was {@link #AlwaysBinary}) whenever possible, and with
		 * hyperedges (as if mode was {@link #AlwaysHyperedges}) if required.
		 
		 * <p>An obvious reason would be that the graph contains non-binary
		 * edges. Subclasses may impose additional conditions. For instance,
		 * when visualizing morphisms that contain mappings for edges, it may
		 * be required to create hyperedges nodes.
		 * TODO we should, in this case, attach a VxLabelNode to the edge and draw the arrow between those
		 */
		Auto,
		/**
		 * Always show edges as hyperedges.
		 */
		AlwaysHyperedges,
		/**
		 * Always show edges as binary edges. Setting this mode may cause
		 * rendering methods to fail if some edge cannot be drawn in this
		 * way, e.g. if it is non-binary.
		 */
		AlwaysBinary;
	}

	/* Style names */
	private GraphStyleNames graphStyleNames = new GraphStyleNames();
	private String morphismStyleName              = "morphism";
	private String boxStyleName                   = "box";
	private String textnodeStyleName              = "textnode";
	private String textnodeSymbolStyleName        = "textnode.bigsymbol";

	/* What elements to create */
	private boolean createNodeLabels = true;
	private boolean createEdgeLabels = true;
	private boolean createEmptyGraphSymbol = true;

	private EdgeMode edgeMode = EdgeMode.Auto;

	/**
	 * Creates a new parameter object with default settings (automatic edge
	 * mode, create node and edge labels) and default style names.
	 */
	public GraphToVxParams() {
	}

	/* Style name getters and setters (chainable) */

	/** @return the object containing the style names for graph elements */
	public GraphStyleNames getGraphStyleNames() { return graphStyleNames; }
	/**
	 * Sets all style names for created graph elements.
	 * @param graphStyleNames the new style names
	 */
	public void setGraphStyleNames(GraphStyleNames graphStyleNames) {
		this.graphStyleNames = graphStyleNames;
	}

	/** @return the style name for arrows that relate the elements that are mapped by a morphism */
	public String getMorphismStyleName() { return morphismStyleName; }
	/** @return the style name for {@link VxBox}es */
	public String getBoxStyleName() { return boxStyleName; }
	/** @return the style name for freely placed text labels */
	public String getTextnodeStyleName() { return textnodeStyleName; }
	/** @return the style name for freely placed text labels that contain just a single symbol (e.g. empty set) */
	public String getTextnodeSymbolStyleName() { return textnodeSymbolStyleName; }

	/**
	 * Sets a new style name for arrows that relate the elements that are
	 * mapped by a morphism.
	 * @param morphismStyleName the morphismStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setMorphismStyleName(String morphismStyleName) {
		this.morphismStyleName = morphismStyleName;
		return this;
	}
	/**
	 * Sets a new style name for {@link VxBox}es.
	 * @param boxStyleName the boxStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setBoxStyleName(String boxStyleName) {
		this.boxStyleName = boxStyleName;
		return this;
	}
	/**
	 * Sets a new style name for freely placed text labels.
	 * @param textnodeStyleName the textnodeStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setTextnodeStyleName(String textnodeStyleName) {
		this.textnodeStyleName = textnodeStyleName;
		return this;
	}
	/**
	 * Sets a new style name for freely placed text labels that contain just a
	 * single symbol (e.g. empty set)
	 * @param textnodeSymbolStyleName the textnodeSymbolStyleName to set
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setTextnodeSymbolStyleName(String textnodeSymbolStyleName) {
		this.textnodeSymbolStyleName = textnodeSymbolStyleName;
		return this;
	}

	/* Getters and setters for what elements to create */

	/**
	 * @return whether nodes created for graph nodes will be labeled with their IDs
	 */
	public boolean getCreateNodeLabels() {
		return createNodeLabels;
	}
	/**
	 * @return whether created edges (binary or hyperedge) will carry edge labels
	 */
	public boolean getCreateEdgeLabels() {
		return createEdgeLabels;
	}
	/**
	 * @return whether an empty graph is symbolized by an empty graph symbol, to make it
	 * recognizable that a graph was rendered at all
	 */
	public boolean getCreateEmptyGraphSymbol() {
		return createEmptyGraphSymbol;
	}
	/**
	 * @param createNodeLabels whether nodes created for graph nodes should be labeled with their IDs
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setCreateNodeLabels(boolean createNodeLabels) {
		this.createNodeLabels = createNodeLabels;
		return this;
	}
	/**
	 * @param createEdgeLabels whether created edges (binary or hyperedge) should carry edge labels
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setCreateEdgeLabels(boolean createEdgeLabels) {
		this.createEdgeLabels = createEdgeLabels;
		return this;
	}
	/**
	 * @param createEmptyGraphSymbol whether an empty graph should be symbolized by an empty graph
	 * symbol, to make it recognizable that a graph was rendered at all
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setCreateEmptyGraphSymbol(boolean createEmptyGraphSymbol) {
		this.createEmptyGraphSymbol = createEmptyGraphSymbol;
		return this;
	}

	/**
	 * @return how edges are drawn (binary edges, hyperedges, etc.)
	 * @see EdgeMode
	 */
	public EdgeMode getEdgeMode() {
		return edgeMode;
	}

	/**
	 * Sets how edges are drawn (always binary edges, always hyperedges, etc.)
	 * @param edgeMode the new edge mode
	 * @see EdgeMode
	 * @return {@code this} (to allow multiple set operations to be chained)
	 */
	public GraphToVxParams setEdgeMode(EdgeMode edgeMode) {
		this.edgeMode = edgeMode;
		return this;
	}
}
