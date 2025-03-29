package de.uni_due.inf.ti.graph.visual.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.visual.GraphVisualizer;
import de.uni_due.inf.ti.visigraph.GraphDrawer;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxElement;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxGroup;
import de.uni_due.inf.ti.visigraph.VxNode;

@NonNullByDefault
public class VxToolbox {
	private final VxGraph vxGraph;
	private final GraphDrawer drawer;

	/**
	 * Sets up a new toolbox for modifying the given VxGraph.
	 * @param vxGraph the vxGraph to modify
	 */
	public VxToolbox(VxGraph vxGraph) {
		this.vxGraph = vxGraph;
		this.drawer = new GraphDrawer();
	}

	/**
	 * Sets up a new toolbox for modifying the given VxGraph.
	 * @param vxGraph the vxGraph to modify
	 * @param drawer the graph drawer to use
	 */
	public VxToolbox(VxGraph vxGraph, GraphDrawer drawer) {
		this.vxGraph = vxGraph;
		this.drawer = drawer;
	}

	/**
	 * @return the VxGraph that this toolbox operates on.
	 */
	public VxGraph getVxGraph() {
		return vxGraph;
	}

	/**
	 * @return the GraphDrawer that is used by this toolbox
	 */
	public GraphDrawer getDrawer() {
		return drawer;
	}

	/**
	 * Creates VxNode and VxEdge instances for the given graph and adds them
	 * to a newly created VxGroup, which is then returned.
	 * @param graph Graph to process
	 * @return mappings of nodes to VxNodes, edges to VxEdges, and other info
	 */
	public GraphVxGroup addGraphToNewGroup(Graph graph) {
		return GraphToVx.addGraphToGroup(graph, vxGraph.createGroup(), new GraphToVxParams());
	}

	/**
	 * Draw arrows between the elements that are related by the given graph
	 * morphisms.
	 *
	 * <p>The elements must already be present in the current VxGraph. The
	 * provided maps indicate where the domain and codomain nodes can be
	 * found.
	 *
	 * @param morphism the morphism to render
	 * @param domainMap mapping of the domain to VxGraph elements
	 * @param codomainMap mapping of the codomain to VxGraph elements
	 * @return a newly created VxGroup containing the added mapping arrows
	 */
	public VxGroup drawMorphism(Morphism morphism, GraphVxMapping domainMap, GraphVxMapping codomainMap) {
		if (domainMap.getVxGraph() != vxGraph || codomainMap.getVxGraph() != vxGraph) {
			throw new IllegalArgumentException("domainMap or codomainMap refer to different VxGraph");
		}

		Map<Node, VxExplicitNode> srcnodemap = domainMap.getNodeMap();
		Map<Node, VxExplicitNode> dstnodemap = codomainMap.getNodeMap();

		/* Draw arrows between nodes */
		VxGroup edgeGroup = vxGraph.createGroup();
		for (Map.Entry<Node, Node> entry : morphism.getNodeMap().entrySet()) {
			VxNode src = srcnodemap.get(entry.getKey());
			VxNode tgt = dstnodemap.get(entry.getValue());
			VxEdge e = vxGraph.addEdge(src, tgt, GraphVisualizer.morphismStyleName);
			edgeGroup.add(e);
		}
		return edgeGroup;
	}

	/**
	 * Draw arrows between the elements that are related by the given graph
	 * morphisms, and assigns the given style variant to all elements of
	 * the codomain that are newly created by this morphism.
	 *
	 * <p>The elements must already be present in the current VxGraph. The
	 * provided maps indicate where the domain and codomain nodes can be
	 * found.
	 *
	 * @param morphism the morphism to render
	 * @param domainMap mapping of the domain to VxGraph elements
	 * @param codomainMap mapping of the codomain to VxGraph elements
	 * @param appendStr string to append to the style name
	 * @return a newly created VxGroup containing the added mapping arrows
	 * @see #drawMorphism(Morphism, GraphVxMapping, GraphVxMapping)
	 * @see #styleMorphismCreated(Morphism, GraphVxMapping, String)
	 */
	public VxGroup drawAndColorMorphism(Morphism morphism, GraphVxMapping domainMap, GraphVxMapping codomainMap, String appendStr) {
		VxGroup g = drawMorphism(morphism, domainMap, codomainMap);
		styleMorphismCreated(morphism, codomainMap, appendStr);
		return g;
	}

	/**
	 * Given a morphism and a representation of the codomain, change the
	 * style names of the elements depending on whether they have a preimage
	 * under the morphism or not.
	 * <p>
	 * In particular, elements that do not have a preimage get the given
	 * string appended to all style names.
	 *
	 * @param morphism the morphism
	 * @param codomainMap mapping of the codomain to VxGraph elements
	 * @param appendStr string to append to the style name
	 */
	public void styleMorphismCreated(Morphism morphism, GraphVxMapping codomainMap, String appendStr) {
		if (codomainMap.getGraph() != morphism.getCodomain()) {
			throw new IllegalArgumentException("codomainMap and codomain of morphism do not match");
		}

		for (Entry<Node, VxExplicitNode> kv : codomainMap.getNodeMap().entrySet()) {
			if (!morphism.inRange(kv.getKey())) {
				// node added by morphism
				applyStyleVariant(kv.getValue(), appendStr);
			}
		}

		for (Entry<Edge, VxComponent> kv : codomainMap.getEdgeMap().entrySet()) {
			if (!morphism.inRange(kv.getKey())) {
				// edge added by morphism
				VxComponent c = kv.getValue();
				// style the element itself
				applyStyleVariant(c, appendStr);
				if (c instanceof VxEdge) {
					// also style the edge label
					((VxEdge) c).getLabels().forEach(ln -> applyStyleVariant(ln, appendStr));
				} else if (c instanceof VxExplicitNode) {
					// hyperedge - also style incident connections...
					((VxExplicitNode) c).getOutEdges().forEach(con -> {
						applyStyleVariant(con, appendStr);
						// ... and their labels
						con.getLabels().forEach(ln -> applyStyleVariant(ln, appendStr));
					});
				} else {
					// ???
				}
			}
		}
	}

	/**
	 * Given a morphism and a representation of the codomain, change the
	 * style names of the elements such that all elements that have a preimage
	 * under the given morphism receive the specified style variant.
	 * <p>
	 * In particular, elements that do not have a preimage get the given
	 * string appended to all style names.
	 *
	 * @param morphism the morphism
	 * @param codomainMap mapping of the codomain to VxGraph elements
	 * @param appendStr string to append to the style name
	 */
	public void styleMorphismHasPreimage(Morphism morphism, GraphVxMapping codomainMap, String appendStr) {
		if (codomainMap.getGraph() != morphism.getCodomain()) {
			throw new IllegalArgumentException("codomainMap and codomain of morphism do not match");
		}

		for (Entry<Node, VxExplicitNode> kv : codomainMap.getNodeMap().entrySet()) {
			if (morphism.inRange(kv.getKey())) {
				// node added by morphism
				applyStyleVariant(kv.getValue(), appendStr);
			}
		}

		for (Entry<Edge, VxComponent> kv : codomainMap.getEdgeMap().entrySet()) {
			if (morphism.inRange(kv.getKey())) {
				// edge added by morphism
				VxComponent c = kv.getValue();
				// style the element itself
				applyStyleVariant(c, appendStr);
				if (c instanceof VxEdge) {
					// also style the edge label
					((VxEdge) c).getLabels().forEach(ln -> applyStyleVariant(ln, appendStr));
				} else if (c instanceof VxExplicitNode) {
					// hyperedge - also style incident connections...
					((VxExplicitNode) c).getOutEdges().forEach(con -> {
						applyStyleVariant(con, appendStr);
						// ... and their labels
						con.getLabels().forEach(ln -> applyStyleVariant(ln, appendStr));
					});
				} else {
					// ???
				}
			}
		}
	}

	private static void applyStyleVariant(VxElement element, String variant) {
		if (!element.getStyle().endsWith(variant)) {
			element.setStyle(element.getStyle() + variant);
		}
	}

	/**
	 * Creates a new VxNode that displays the given text. The new node is
	 * styled not as a node, but rather as a label that annotates the
	 * graph representation.
	 * <p>
	 * Example usages:
	 * <ul>
	 * <li>In a rendering with multiple graphs where some of the graphs
	 * may be empty, it may be desirable to show that an empty graph is
	 * indeed present (as opposed to not drawing anything at all). For this,
	 * a text node containing an empty set symbol could be placed.
	 * <li>When drawing a graph that interfaces to another graph, but the
	 * other graph is not displayed (or known), a text node can be placed
	 * to indicate where another graph could be inserted.
	 * </ul>
	 * @param text the text to draw
	 * @return a newly created VxNode
	 */
	public VxExplicitNode drawTextNode(String text) {
		return drawTextNode(text, GraphVisualizer.textnodeStyleName);
	}

	private VxExplicitNode drawTextNode(String text, String style) {
		return vxGraph.addNode(text, style);
	}

	/**
	 * Checks if any nodes of the given group are positioned very close to
	 * each other and if yes, pushes them apart.
	 *
	 * @see #separateOverlappingNodes(VxGroup) separateOverlappingNodes(VxGroup)
	 * for the intended use case and general remarks
	 * @param group group that contains the nodes to be checked and moved
	 * @param distance minimum distance between any two node center points
	 * @param iterations the (maximum) number of iterations
	 * @param pushStep the distance two nodes are pushed apart in each step
	 */
	public static void separateOverlappingNodesFixedDistance(VxGroup group, double distance, int iterations, double pushStep) {
		List<VxExplicitNode> nodes = new ArrayList<>(group.getNodes());
		int nodeCount = nodes.size();
		while (iterations > 0) {
			iterations--;

			boolean pushed = false;
			for (int i1 = 0; i1 < nodeCount; i1++) {
				VxExplicitNode n1 = nodes.get(i1);
				Point2D p1 = n1.position();
				for (int i2 = i1 + 1; i2 < nodeCount; i2++) {
					VxExplicitNode n2 = nodes.get(i2);
					Point2D p2 = n2.position();
					if (p1.distance(p2) < distance) {
						double angle;
						if (p1.equals(p2)) {
							// Equal position = push them away from each other at a random angle
							angle = Math.random() * Math.PI; // halfcircle
						} else {
							angle = Points.angle(Points.subtract(p2, p1));
						}
						Point2D pushP = Points.ofAngle(angle, pushStep);
						Points.subtractFrom(pushP, p1);
						Points.addTo(pushP, p2);
						pushed = true;
					}
				}
			}
			if (!pushed) {
				break;
			}
		}
	}

	/**
	 * Checks if any nodes of the given group are positioned very close to
	 * each other and if yes, pushes them apart.
	 *
	 * @see #separateOverlappingNodes(VxGroup) separateOverlappingNodes(VxGroup)
	 * for the intended use case and general remarks
	 * @param group group that contains the nodes to be checked and moved
	 * @param distance minimum distance between any two node center points
	 */
	public static void separateOverlappingNodesFixedDistance(VxGroup group, double distance) {
		double defaultStep = 3.;
		int iterations = (int) (distance / defaultStep);
		iterations = Math.min(300, Math.max(5, iterations));
		separateOverlappingNodesFixedDistance(group, distance, iterations, defaultStep);
	}

	/**
	 * Checks if any nodes of the given group are positioned very close to
	 * each other and if yes, pushes them apart.
	 *
	 * The actual pushed distance will be the given factor of the minimum
	 * distance of the nodes based on their (bounding box) size. That is, the
	 * size of a node is assumed to be a circle that encloses the bounding box
	 * of the node. A factor of 1.0 then means that these circles barely touch.
	 * (Circular nodes are thus pushed apart a bit with a factor of 1.0, since
	 * the bounding box diagonal is larger than their diameter.)
	 *
	 * @see #separateOverlappingNodes(VxGroup) separateOverlappingNodes(VxGroup)
	 * for the intended use case and general remarks
	 * @param group group that contains the nodes to be checked and moved
	 * @param distanceFactor factor for minimum distance between any two nodes
	 * @param iterations the (maximum) number of iterations
	 * @param pushStep the distance two nodes are pushed apart in each step
	 */
	public void separateOverlappingNodes(VxGroup group, double distanceFactor, int iterations, double pushStep) {
		List<VxExplicitNode> nodes = new ArrayList<>(group.getNodes());
		int nodeCount = nodes.size();

		Map<VxExplicitNode,Double> nodeSizeMap = new HashMap<>();
		for (VxExplicitNode n : nodes) {
			Rectangle2D bounds = drawer.getBounds(Collections.singleton(n));
			double diag = Math.hypot(bounds.getWidth(), bounds.getHeight());
			nodeSizeMap.put(n, diag / 2.);
		}

		while (iterations > 0) {
			iterations--;

			boolean pushed = false;
			for (int i1 = 0; i1 < nodeCount; i1++) {
				VxExplicitNode n1 = nodes.get(i1);
				Point2D p1 = n1.position();
				double r1 = nodeSizeMap.get(n1);
				for (int i2 = i1 + 1; i2 < nodeCount; i2++) {
					VxExplicitNode n2 = nodes.get(i2);
					Point2D p2 = n2.position();
					double r2 = nodeSizeMap.get(n2);
					if (p1.distance(p2) < (r1 + r2) * distanceFactor) {
						double angle;
						if (p1.equals(p2)) {
							// Equal position = push them away from each other at a random angle
							angle = Math.random() * Math.PI; // halfcircle
						} else {
							angle = Points.angle(Points.subtract(p2, p1));
						}
						Point2D pushP = Points.ofAngle(angle, pushStep);
						Points.subtractFrom(pushP, p1);
						Points.addTo(pushP, p2);
						pushed = true;
					}
				}
			}
			if (!pushed) {
				break;
			}
		}
	}

	/**
	 * Checks if any nodes of the given group are positioned very close to
	 * each other and if yes, pushes them apart.
	 *
	 * <p>The actual pushed distance will be the given factor of the minimum
	 * distance of the nodes based on their (bounding box) size. That is, the
	 * size of a node is assumed to be a circle that encloses the bounding box
	 * of the node. A factor of 1.0 then means that these circles barely touch.
	 * (Circular nodes are thus pushed apart a bit with a factor of 1.0, since
	 * the bounding box diagonal is larger than their diameter.)
	 *
	 * @see #separateOverlappingNodes(VxGroup) separateOverlappingNodes(VxGroup)
	 * for the intended use case and general remarks
	 * @param group group that contains the nodes to be checked and moved
	 * @param distanceFactor factor for minimum distance between any two nodes
	 */
	public void separateOverlappingNodes(VxGroup group, double distanceFactor) {
		double defaultStep = 3.;
		int iterations = (int) (40. * distanceFactor / defaultStep);
		iterations = Math.min(300, Math.max(5, iterations));
		separateOverlappingNodes(group, distanceFactor, iterations, defaultStep);
	}

	/**
	 * Checks if any nodes of the given group are positioned very close to
	 * each other and if yes, pushes them apart.
	 *
	 * Nodes will be pushed away until they barely touch, and a bit more.
	 *
	 * <p>This method is supposed to be used as a postprocessing step after graph
	 * layouting where some of the nodes have been manually positioned.
	 * Suppose two graphs are rendered, and some relation between the nodes
	 * exists. Related nodes should be positioned the same. If one node is
	 * related to two nodes in the other graph, this other graph may have two
	 * identically positioned nodes. These should be pushed apart, so that they
	 * have both the same distance from their truly intended position.
	 *
	 * <p>Note that such cases are not bugs in the layouter used, since this would
	 * happen when the layouter is instructed to <em>not</em> touch some nodes.
	 * @param group group that contains the nodes to be checked and moved
	 */
	public void separateOverlappingNodes(VxGroup group) {
		separateOverlappingNodes(group, 1.125);
	}

	/**
	 * For a morphism whose codomain has already been layouted, place the
	 * corresponding elements of the domain graph in a consistent way.
	 * @param morphism the underlying graph morphism
	 * @param domainMap mapping of domain to VxGraph elements
	 * @param codomainMap mapping of codomain to VxGraph elements
	 */
	public void arrangePreimageElements(Morphism morphism, GraphVxGroup domainMap, GraphVxGroup codomainMap) {
		if (morphism.getDomain() != domainMap.getGraph()) {
			throw new IllegalArgumentException("domainMap does not match morphism's domain");
		}
		if (morphism.getCodomain() != codomainMap.getGraph()) {
			throw new IllegalArgumentException("codomainMap does not match morphism's codomain");
		}
		for (Entry<Node, VxExplicitNode> kv : domainMap.getNodeMap().entrySet()) {
			Node node = kv.getKey();
			VxExplicitNode vxNode = kv.getValue();

			Node mappedNode = morphism.get(node);
			VxExplicitNode mappedVxNode = codomainMap.getNodeMap().get(mappedNode);
			vxNode.position().setLocation(mappedVxNode.position());
		}
		for (Entry<Edge, VxComponent> kv : domainMap.getEdgeMap().entrySet()) {
			Edge edge = kv.getKey();
			if (kv.getValue() instanceof VxEdge) {
				// TODO: What about hyperedges? This code wouldn't layout them properly at all!
				// TODO: Also explodes on partial morphisms
				VxEdge vxEdge = (VxEdge) kv.getValue();

				Edge mappedEdge = morphism.get(edge);
				if (mappedEdge != null) { // TOOD Better handling of partial morphisms
				// FIXME the following cast *should* succeed since the image of a binary edge should be a binary edge again, but will it?
					VxEdge mappedVxEdge = (VxEdge) codomainMap.getEdgeMap().get(mappedEdge);
					vxEdge.setCurveIn(mappedVxEdge.getCurveInAngle(), mappedVxEdge.getCurveInStrength());
					vxEdge.setCurveOut(mappedVxEdge.getCurveOutAngle(), mappedVxEdge.getCurveOutStrength());
					vxEdge.setLoopAngle(mappedVxEdge.getLoopAngle());
				}
			}
		}

		/*
		 * noninjective morphisms can cause multiple nodes to be placed on
		 * top of each other, resolve this if necessary
		 */
		separateOverlappingNodes(domainMap.getVxGroup());
		// FIXME: Figure out what we should do with edges attached to nodes that have just been separated.
	}
}
