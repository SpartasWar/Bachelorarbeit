package de.uni_due.inf.ti.graph.ext;

import java.util.ArrayList;
import java.util.List;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;

/**
 * Class that computes pushout complements for composable pairs of morphisms.
 *
 * @implNote Currently does not handle non-injective morphisms well.
 * In particular, for non-injective morphisms, the pushout complement is not necessarily unique
 * and the implementation below returns an arbitrary one.
 * For better implementation hints, refer to "Construction of Pushout Complements
 * in the Category of Hypergraphs".
 */
public class PushoutComplement {
	// prevent instantiation
	private PushoutComplement() {
	}

	/**
	 * Computes the pushout complement of morphisms T_L and L_B, if it exists.
	 *
	 * @param T_L first morphism
	 * @param L_B second morphism
	 * @return the pushout complement, or {@code null} if it does not exist
	 */
	public static Square compute(Morphism T_L, Morphism L_B) {
		Morphism.ensureComposable(T_L, L_B, "T_L", "L_B");
		if (!(T_L.isInjective() && L_B.isInjective())) {
			throw new IllegalArgumentException("Non-injective morphisms given, but currently only injective morphisms are supported");
		}

		/* Basic construction: Given T --> L --> B, ... */
		Graph T = T_L.getDomain();
		Graph B = L_B.getCodomain();
		Morphism T_L_B = Morphism.createComposition(T_L, L_B);

		/* ... Construct R and T->R, R->B */
		Morphism T_R = Morphism.createIsomorphism(T);
		Graph R = T_R.getCodomain();

		Morphism R_B = new Morphism(R, B);
		for (Node n : T.getNodes()) {
			R_B.put(T_R.get(n), T_L_B.get(n));
		}
		for (Edge e : T.getEdges()) {
			R_B.put(T_R.get(e), T_L_B.get(e));
		}

		/*
		 * R is still missing some elements. Check which elements are contained in B
		 * that are not being mapped to by L->B. First the nodes (this should always
		 * succeed)
		 */
		for (Node n : B.getNodes()) {
			if (L_B.getPreimage(n) == null) {
				/* Add this node to R and include the mapping in R->B */
				Node nc = R.addNode();
				R_B.put(nc, n);
			}
		}

		/* Then the edges (this might not succeed, dangling edge condition) */
		for (Edge e : B.getEdges()) {
			if (L_B.getPreimage(e) == null) {
				/*
				 * Add this edge to R, including the mapping in R->B. Obviously all the nodes
				 * that this edge connects to are contained in B. However, not all of these
				 * nodes might be also contained in R (If they are not, the pushout complement
				 * does not exist)
				 */
				List<Node> conns = new ArrayList<>();
				for (Node conn : e.getNodes()) {
					if (R_B.getPreimage(conn) == null) {
						/*
						 * Has no preimage in R, so the DEC is violated. No pushout complement exists.
						 */
						return null;
					} else {
						/* Record the preimage in R as a connection for the new edge */
						conns.add(R_B.getPreimage(conn));
					}
				}
				Edge ec = R.addEdge(e.getLabel(), conns);
				R_B.put(ec, e);
			}
		}

		return new Square(T_L, T_R, L_B, R_B);
	}
}
