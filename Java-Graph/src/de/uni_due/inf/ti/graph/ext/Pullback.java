package de.uni_due.inf.ti.graph.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.util.Pair;

/**
 * Class that computes pullback squares for spans or for pairs of morphisms.
 */
public class Pullback {
	// prevent instantiation
	private Pullback() {
	}

	/**
	 * Completes the given pair of morphisms to a pullback square.
	 */
	public static Square compute(Morphism bottomLeft, Morphism bottomRight) {
		return compute(new Cospan(bottomLeft, bottomRight));
	}

	/**
	 * Completes the given cospan to a pullback square.
	 */
	public static Square compute(Cospan bottom) {
		Morphism B_A = bottom.getLeftMorphism();
		Morphism C_A = bottom.getRightMorphism();

		Graph B = bottom.getLeftGraph();
		Graph C = bottom.getRightGraph();

		/* implementation/comments according to
		 * Habel, Müller, Plump: Double-pushout graph transformation revisited
		 * Lemma A.9
		 */

		/* D = { <x,y> in B x C | b(x) = c(y) }, separately for nodes and edges */
		Graph D = new Graph();
		Map<Pair<Node, Node>, Node> nodePairToD = new HashMap<>();
		for (Node x : B.getNodes()) {
			for (Node y : C.getNodes()) {
				if (B_A.get(x) == C_A.get(y)) {
					nodePairToD.put(new Pair<>(x, y), D.addNode());
				}
			}
		}

		Map<Pair<Edge, Edge>, Edge> edgePairToD = new HashMap<>();
		for (Edge x : B.getEdges()) {
			for (Edge y : C.getEdges()) {
				if (B_A.get(x) == C_A.get(y)) {
					/* edge endpoint map (i e.g. 0 source, 1 target, ...):
					 * p_i(<x,y>) = <p_i(x), p_i(y)>
					 */
					Node[] eNodes = new Node[x.getArity()];
					List<Node> xNodes = x.getNodes();
					List<Node> yNodes = y.getNodes();
					for (int i = 0; i < eNodes.length; i++) {
						eNodes[i] = nodePairToD.get(new Pair<>(xNodes.get(i), yNodes.get(i)));
					}
					edgePairToD.put(new Pair<>(x, y), D.addEdge(x.getLabel(), eNodes));
				}
			}
		}

		/*
		 * Construct D_B, D_C as the domain restrictions of the projections from B x C,
		 * that is, D_B(<x,y>) = x etc.
		 */
		Morphism D_B = new Morphism(D, B);
		Morphism D_C = new Morphism(D, C);
		for (Entry<Pair<Node, Node>, Node> xyD : nodePairToD.entrySet()) {
			D_B.put(xyD.getValue(), xyD.getKey().getFirst());
			D_C.put(xyD.getValue(), xyD.getKey().getSecond());
		}
		for (Entry<Pair<Edge, Edge>, Edge> xyD : edgePairToD.entrySet()) {
			D_B.put(xyD.getValue(), xyD.getKey().getFirst());
			D_C.put(xyD.getValue(), xyD.getKey().getSecond());
		}

		return new Square(new Span(D_B, D_C), bottom);
	}
}
