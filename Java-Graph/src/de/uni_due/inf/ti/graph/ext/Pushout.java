package de.uni_due.inf.ti.graph.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.util.Partition;

/**
 * Class that computes pushout squares for spans or for pairs of morphisms.
 */
public class Pushout {
	// prevent instantiation
	private Pushout() {
	}

	/**
	 * Completes the given pair of morphisms to a pushout square.
	 */
	public static Square compute(Morphism topLeft, Morphism topRight) {
		return compute(new Span(topLeft, topRight));
	}

	/**
	 * Completes the given span to a pushout square.
	 */
	public static Square compute(Span top) {
		Morphism A_rB = top.getLeftMorphism();
		Morphism A_rC = top.getRightMorphism();

		Graph A = top.getMiddleGraph();
		Graph rB = top.getLeftGraph();
		Graph rC = top.getRightGraph();

		/*
		 * The three graphs must be made disjoint. Otherwise, too many nodes
		 * might be merged. Example: A=empty, B=C=some graph - no elements of
		 * B and C have a common preimage in A, so the result should be two
		 * copies of B, basically.
		 *
		 * We could track pairs of node and graph identifier below (i.e. (node,A)
		 * always is different from (node,B)) but that makes the code even more
		 * unreadable than it already is.
		 *
		 * Instead, we make copies of B and C (if they are identical to each other
		 * or to A), compute the pushout for the copies, then recover the mapping
		 * from the original graphs later on.
		 */
		Graph B, C;
		Morphism rB_B, rC_C;
		Morphism A_B, A_C;
		if (A == rB || A == rC || rB == rC) {
			// clone B and C
			//System.err.println("creating clones for strange pushout");
			rB_B = Morphism.createIsomorphism(rB); B = rB_B.getCodomain();
			rC_C = Morphism.createIsomorphism(rC); C = rC_C.getCodomain();
			A_B = A_rB.compose(rB_B);
			A_C = A_rC.compose(rC_C);
		} else {
			// they already are disjoint
			rB_B = Morphism.createIdentity(rB); B = rB; A_B = A_rB;
			rC_C = Morphism.createIdentity(rC); C = rC; A_C = A_rC;
		}

		/*
		 * First create a partition of the nodes. Throw in all nodes of A, B, C.
		 * It is now valid to assume that the sets of nodes are all disjoint
		 * because of the cloning done above
		 */
		Partition<Node> nodePart = new Partition<>();
		A.getNodes().forEach(n -> nodePart.makeSet(n));
		B.getNodes().forEach(n -> nodePart.makeSet(n));
		C.getNodes().forEach(n -> nodePart.makeSet(n));
		/* Now specify which nodes must be merged - this is given by A_B and A_C */
		A_B.getNodeMap().forEach((na, nb) -> nodePart.union(na, nb));
		A_C.getNodeMap().forEach((na, nc) -> nodePart.union(na, nc));

		/* Partition the edges in a similar way */
		Partition<Edge> edgePart = new Partition<>();
		A.getEdges().forEach(e -> edgePart.makeSet(e));
		B.getEdges().forEach(e -> edgePart.makeSet(e));
		C.getEdges().forEach(e -> edgePart.makeSet(e));
		A_B.getEdgeMap().forEach((ea, eb) -> edgePart.union(ea, eb));
		A_C.getEdgeMap().forEach((ea, ec) -> edgePart.union(ea, ec));

		/*
		 * The nodePart and edgePart now contain one class for each node or edge in D.
		 * We can now populate D with the nodes
		 */
		Graph D = new Graph();
		Set<Set<Node>> nodeEquiv = nodePart.getEquivalenceClasses();
		Map<Set<Node>, Node> nodeClassToD = new HashMap<>();
		Map<Node, Set<Node>> nodeReprToClass = new HashMap<>();
		for (Set<Node> nClass : nodeEquiv) {
			Node repr = nodePart.find(nClass.iterator().next());
			nodeReprToClass.put(repr, nClass);
			Node nD = D.addNode();
			nodeClassToD.put(nClass, nD);
		}
		/* Add the edges as well */
		Set<Set<Edge>> edgeEquiv = edgePart.getEquivalenceClasses();
		Map<Set<Edge>, Edge> edgeClassToD = new HashMap<>();
		Map<Edge, Set<Edge>> edgeReprToClass = new HashMap<>();
		for (Set<Edge> eClass : edgeEquiv) {
			Edge repr = edgePart.find(eClass.iterator().next());
			edgeReprToClass.put(repr, eClass);
			// our repr edge is connected to nodes in any of A, B or C.
			// get the list and then replace each with the corresponding node of D
			Node[] eNodes = repr.getNodes().toArray(Node[]::new);
			for (int i = 0; i < eNodes.length; i++) {
				eNodes[i] = nodeClassToD.get(nodeReprToClass.get(nodePart.find(eNodes[i])));
			}
			// finally add edge
			Edge eD = D.addEdge(repr.getLabel(), eNodes);
			edgeClassToD.put(eClass, eD);
		}

		/*
		 * Construct B_D, C_D by mapping each element of B, C to its representative in D.
		 */
		Morphism B_D = new Morphism(B, D);
		Morphism C_D = new Morphism(C, D);
		for (Morphism X_D : new Morphism[] { B_D, C_D }) {
			Graph X = X_D.getDomain();
			for (Node nX : X.getNodes()) {
				X_D.put(nX, nodeClassToD.get(nodeReprToClass.get(nodePart.find(nX))));
			}
			for (Edge eX : X.getEdges()) {
				X_D.put(eX, edgeClassToD.get(edgeReprToClass.get(edgePart.find(eX))));
			}
		}

		// Finally, undo the mapping of the clones
		Morphism rB_D = rB_B.compose(B_D);
		Morphism rC_D = rC_C.compose(C_D);

		return new Square(top, new Cospan(rB_D, rC_D));
	}
}
