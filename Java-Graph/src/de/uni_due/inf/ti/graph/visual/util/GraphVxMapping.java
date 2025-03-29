package de.uni_due.inf.ti.graph.visual.util;

import java.util.Map;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;

/**
 * Identifies the components of a Graph with the elements of a VxGraph.
 * This includes things like mapping of nodes to VxNodes.
 */
@NonNullByDefault
public class GraphVxMapping {
	private final Graph graph;
	private final VxGraph vxGraph;
	private final Map<Node,VxExplicitNode> nodeMap;
	private final Map<Edge,VxComponent> edgeMap;

	GraphVxMapping(Graph graph, VxGraph vxGraph, Map<Node,VxExplicitNode> nodeMap, Map<Edge,VxComponent> edgeMap) {
		this.graph = graph;
		this.vxGraph = vxGraph;
		this.nodeMap = nodeMap;
		this.edgeMap = edgeMap;
	}

	/** @return the graph represented in this group. */
	public Graph getGraph() { return graph; }

	/** @return the VxGraph containing the mapped elements. */
	public VxGraph getVxGraph() { return vxGraph; }

	/** @return the mapping of graph nodes to VxNodes. */
	public Map<Node, VxExplicitNode> getNodeMap() { return nodeMap; }

	/** @return the mapping of graph edges to VxEdge (for binary graphs) or VxExplicitNodes (for hyperedges). */
	public Map<Edge, VxComponent> getEdgeMap() { return edgeMap; }
}
