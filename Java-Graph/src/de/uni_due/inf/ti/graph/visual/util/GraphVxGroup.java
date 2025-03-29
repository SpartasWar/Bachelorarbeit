package de.uni_due.inf.ti.graph.visual.util;

import java.util.Map;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGroup;

/**
 * Contains information about a VxGroup that represents a Graph.
 */
@NonNullByDefault
public class GraphVxGroup extends GraphVxMapping {
	private final VxGroup vxGroup;

	GraphVxGroup(Graph graph, VxGroup vxGroup, Map<Node,VxExplicitNode> nodeMap, Map<Edge,VxComponent> edgeMap) {
		super(graph, vxGroup.getGraph(), nodeMap, edgeMap);
		this.vxGroup = vxGroup;
	}

	/** @return the VxGroup containing the elements of the graph. */
	public VxGroup getVxGroup() { return vxGroup; }
}
