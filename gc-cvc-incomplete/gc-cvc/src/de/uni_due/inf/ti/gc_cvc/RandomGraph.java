package de.uni_due.inf.ti.gc_cvc;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.random.RandomUtils;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Constant;
import de.uni_due.inf.ti.graph.condition.Disjunction;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.False;
import de.uni_due.inf.ti.graph.condition.ForAll;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.condition.Negation;
import de.uni_due.inf.ti.graph.condition.QuantifiedCondition;
import de.uni_due.inf.ti.graph.condition.True;

public class RandomGraph {
	private static Graph empty = new Graph();

	/** When randomly extending graphs, the probability that there is an additional round of adding elements
	 * (higher = graphs grow faster)
	 */
	private static double paramGraphExtendRoundProb = 0.75;
	/** When randomly extending graphs, the probability for returning an unextended graph instead */
	private static double paramGraphNoExtendProb = 0.04;

	public static GraphCondition randomCondition() {
		while (true) {
			GraphCondition candidate = randomCondition(empty);
			if (candidate instanceof Constant) {
				continue;
			} else if (candidate instanceof QuantifiedCondition cand) {
				boolean primitiveChild = cand.getChildCondition() instanceof Constant;
				if (cand.getMorphism().getCodomain().getNodes().size() <= 1 && primitiveChild) {
					continue;
				}
				
				if (Math.random() <= 0.4) {
					/* sometimes, strip off the outer quantifier, so we obtain
					 * conditions that are not rooted at empty graph
					 */
					candidate = cand.getChildCondition();
				}
			}
			return candidate;
		}
	}

	private static GraphCondition randomCondition(Graph origin) {
		double what = Math.random();
		if (what <= 0.2) {
			return new False(origin);
		} else if (what <= 0.4) {
			return new True(origin);
		} else if (what <= 0.5) {
			/* Conjunct or disjunct */
			int howMany = (int) Math.floor(Math.random() * (4 - 0.4) + 0.4);
			GraphCondition[] subconds = new GraphCondition[howMany];
			for (int i = 0; i < howMany; i++) {
				subconds[i] = randomCondition(origin);
			}
			/* Decide which one to use */
			if (Math.random() <= 0.5) {
				return new Conjunction(origin, subconds);
			} else {
				return new Disjunction(origin, subconds);
			}
		} else if (what <= 0.7) {
			return new Negation(randomCondition(origin));
		} else {
			/* Quantification */
			/* Randomly extend the existing graph */
			Morphism a = randomGraphExtension(origin);
			/* Decide which one to use */
			if (Math.random() <= 0.5) {
				return new Existential(a, randomCondition(a.getCodomain()));
			} else {
				return new ForAll(a, randomCondition(a.getCodomain()));
			}
		}
	}

	public static Morphism randomGraphExtension(Graph origin) {
		Morphism m = cloneAndExtendGraph(origin);
		if (Math.random() <= paramGraphNoExtendProb) {
			return m;
		}
		Graph next = m.getCodomain();
		while (next.isEmpty() || Math.random() <= paramGraphExtendRoundProb) {
			double what = Math.random();
			if (what <= 0.07) {
				// unchanged
			} else if (what <= 0.5 || next.isEmpty() ) {
				next.addNode();
			} else {
				Node n1 = RandomUtils.getRandomElement(next.getNodes());
				Node n2 = RandomUtils.getRandomElement(next.getNodes());
				Label newLabel = Math.random() <= 0.5 ? new Label("A") : new Label("B");
				next.addEdge(newLabel, n1, n2);
			}
		}
		return m;
	}

	private static Morphism cloneAndExtendGraph(Graph g) {
		Graph h = new Graph(g);
		Morphism m = new Morphism(g, h);
		for (Node n : g.getNodes()) {
			// cloning a graph retains its IDs, find the matching one
			for (Node nn : h.getNodes()) {
				if (n.getId() == nn.getId()) {
					m.put(n, nn);
				}
			}
		}
		matchUpEdges(g, h, m); // alternatively, Morphism.getIsomorphism(g, h, m);
		//assert m != null && m.isIsomorphism() : "A clone should have an iso: " + g.toString();
		return m;
	}

	private static void matchUpEdges(Graph g, Graph h, Morphism m) {
		List<Edge> hEdges = new ArrayList<>(h.getEdges()); // LinkedList in theory better, in practice not so much
		for (Edge eg : g.getEdges()) {
			findHEdge: for (Iterator<Edge> iterator = hEdges.iterator(); iterator.hasNext();) {
				Edge eh = iterator.next();
				if (!eg.getLabel().equals(eh.getLabel())) continue;
				if (eg.getArity() != eh.getArity()) continue;
				List<Node> egNodes = eg.getNodes();
				List<Node> ehNodes = eh.getNodes();
				for (int i = 0; i < egNodes.size(); i++) {
					if (m.get(egNodes.get(i)) != ehNodes.get(i)) continue findHEdge;
				}
				// apparently it fits
				m.put(eg, eh);
				iterator.remove();
				break;
			}
		}
	}
}
