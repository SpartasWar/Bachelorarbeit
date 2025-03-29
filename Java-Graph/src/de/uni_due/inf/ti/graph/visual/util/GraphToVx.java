package de.uni_due.inf.ti.graph.visual.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.visual.GraphStyleNames;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxGroup;
import de.uni_due.inf.ti.visigraph.VxLabelNode;
import de.uni_due.inf.ti.visigraph.VxNode;

public class GraphToVx {
	private GraphToVx() {
		// prevent instantiation
	}

	/**
	 * Creates VxNode and VxEdge instances for the given graph and adds them
	 * to the given VxGroup.
	 *
	 * If no particular group is desired, {@code vxGraph.getAllGroup()} can be used as a group.
	 *
	 * All nodes will be positioned at the origin.
	 *
	 * @param graph Graph to process
	 * @param vxGroup VxGroup to place the new elements into
	 * @param params style names and other settings on how to generate the elements
	 * @return mappings of nodes to VxNodes, edges to VxEdges, and other info
	 */
	public static GraphVxGroup addGraphToGroup(Graph graph, VxGroup vxGroup, GraphToVxParams params) {
		VxGraph vxGraph = vxGroup.getGraph();
		GraphStyleNames graphStyles = params.getGraphStyleNames();

		/* Decide on the actual rendering mode */
		boolean hyper;
		switch (params.getEdgeMode()) {
		case AlwaysBinary:
			if (!graph.isBinary()) {
				throw new IllegalArgumentException("AlwaysBinary rendering requested, but there exists a non-binary edge");
			}
			hyper = false;
			break;
		case AlwaysHyperedges:
			hyper = true;
			break;
		case Auto:
			hyper = !graph.isBinary();
			break;
		default:
			throw new IllegalArgumentException("Unknown edge rendering mode requested: " + params.getEdgeMode());
		}

		Map<Node,VxExplicitNode> nodeMap = new HashMap<>();
		Map<Edge,VxComponent> edgeMap = new HashMap<>();

		for (Node node : graph.getNodes()) {
			String nodeLabel = params.getCreateNodeLabels() ? Integer.toString(node.getId()) : null;
			VxExplicitNode vxn = vxGraph.addNode(0.0, 0.0, nodeLabel, graphStyles.getNodeStyleName());
			vxn.setReference(node);
			vxGroup.add(vxn);
			nodeMap.put(node, vxn);
		}

		for (Edge edge : graph.getEdges()) {
			if (!hyper) {
				VxNode source = nodeMap.get(edge.getSource());
				VxNode target = nodeMap.get(edge.getTarget());

				VxEdge vxe = vxGraph.addEdge(source, target, graphStyles.getEdgeStyleName());

				if (params.getCreateEdgeLabels()) {
					VxLabelNode label = vxe.addLabel(edge.getLabel().getName());
					label.setStyle(graphStyles.getLabelStyleName());
				}

				vxe.setReference(edge);
				vxGroup.add(vxe);
				edgeMap.put(edge, vxe);
			} else {
				// The hyperedge node itself
				String nodeLabel = params.getCreateEdgeLabels() ? edge.getLabel().getName() : null;
				VxNode hyperEdge = vxGraph.addNode(0.0, 0.0, nodeLabel, graphStyles.getHyperedgeStyleName());
				hyperEdge.setReference(edge);
				vxGroup.add(hyperEdge);
				edgeMap.put(edge, hyperEdge);

				// Connections from hyperedge node to the graph nodes
				List<Node> nodes = edge.getNodes();
				for (int i = 0; i < nodes.size(); i++) {
					VxEdge con = vxGraph.addEdge(hyperEdge, nodeMap.get(nodes.get(i)), graphStyles.getConnectionStyleName());
					VxLabelNode label = con.addLabel(Integer.toString(i));
					label.setStyle(graphStyles.getArityStyleName());
					label.setPositionOnEdge(.3);
					vxGroup.add(con);
				}
			}
		}

		if (graph.isEmpty() && params.getCreateEmptyGraphSymbol()) {
			vxGroup.add(vxGraph.addNode("\u2205", params.getTextnodeSymbolStyleName()));
		}

		return new GraphVxGroup(graph, vxGroup, nodeMap, edgeMap);
	}
}
