package de.uni_due.inf.ti.graph.ext;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.Enumerators;

/**
 * Enumerates pairs of jointly epi morphisms originating from a given pair of
 * graphs.
 *
 * Given two graphs L and R, we are looking for diagrams like these:
 *
 * <pre>
 *  L --l-> J <--r-- R
 * </pre>
 *
 * where l, r are jointly epi. This amounts to J containing only elements that
 * are in either L or R or both.
 */
public class JointlyEpiMorphismsEnumerator extends Enumerator<Cospan> {
	private final Graph L, R;
	private final Enumerator<Morphism> partialEnumerator;

	public JointlyEpiMorphismsEnumerator(Graph leftGraph, Graph rightGraph) {
		this.L = leftGraph;
		this.R = rightGraph;

		/*
		 * Set up the partial morphism enumerator that next() will use.
		 *
		 * We construct jointly epi pairs of morphisms by enumerating the partial
		 * morphisms. The partial morphism relates exactly those elements of L and R,
		 * which have a common image in J. We then just add the remaining elements later
		 * on.
		 *
		 * XXX figure out whether non-injective makes sense at all and whether it should
		 * be provided as an option
		 * XXX to make sense, the approach would have to be changed: just using noninjective
		 * partial morphisms does not, for example, allow mapping from 2Nodes and 2Nodes to
		 * 1Node (which is possible to do using jointly epi morphimsms)
		 * XXX idea: move the "noninjective part" of the result to the input. that means:
		 * the possibility of something being merged by botleft or botright (i.e. BL or BR is noninj)
		 * is simulated by first fusing then doing injective overlaps.
		 * the result is then the fusing morphism prepended to BL/BR.
		 * this should be able to generate 2Node,2Node -> 1Node: one possible fusing is fuse to 1
		 * on both sides, then injectively overlapping these two nodes.
		 * XXX only thing remaining is the handling of noninjective TL/TR (no matter if overlaps
		 * are done injectively or not)
		 */
		this.partialEnumerator = Morphism.getPartialMorphisms(leftGraph, rightGraph, true, null);
	}

	/* FIXME Think of better API, move code elsewhere or whatever. package-access for JointlyEpiSquareEnumerator */
	public JointlyEpiMorphismsEnumerator(Graph leftGraph, Graph rightGraph, Morphism base) {
		this.L = leftGraph;
		this.R = rightGraph;
		this.partialEnumerator = Morphism.getPartialMorphisms(leftGraph, rightGraph, true, base);
	}

	@Override
	public Cospan next() {
		Morphism mPart = this.partialEnumerator.next();
		if (mPart == null) {
			return null;
		}
		/* inverting once then looking up is more efficient than calling
		 * .getPreimage several times (which does a linear search)
		 */
		Morphism mPartPreimage = mPart.getInverse();

		/*
		 * For the given partial morphism, construct J and two total morphisms L->J,
		 * R->J by doing the following: 1. Start with J = L (giving the total morphism
		 * L->J).
		 */
		Morphism L_J = Morphism.createIsomorphism(L);
		Graph J = L_J.getCodomain();

		/*
		 * 2. Then, add to J all elements of R that are not mapped by the partial
		 * morphism. Add these to the morphism R->J.
		 */
		Morphism R_J = Morphism.create(R, J);
		for (Node n : R.getNodes()) {
			if (mPartPreimage.get(n) == null) {
				/*
				 * Not in the image of L->R -- Add it as a new node
				 */
				Node np = J.addNode();
				R_J.put(n, np);
			} else {
				/*
				 * This node is in the image of L->R. Figure out the preimage (in L) and create
				 * a mapping to the image in L->J
				 */
				R_J.put(n, L_J.get(mPartPreimage.get(n)));
			}
		}
		for (Edge e : R.getEdges()) {
			if (mPartPreimage.get(e) == null) {
				/*
				 * Not in the image of L->R -- Add it as a new edge
				 */
				Edge ep = J.addEdge(e.getLabel(),
						Enumerators.enumerate(e.getNodes()).map(x -> R_J.get(x)).createList());
				R_J.put(e, ep);
			} else {
				/*
				 * Edge was in the image of L->R. Figure out the preimage in L and map the edge
				 * to the image of that one in L->J
				 */
				R_J.put(e, L_J.get(mPartPreimage.get(e)));
			}
		}

		/* We (should) have a pair of jointly epi morphisms now */

		/* Optional: Verify that the pair is indeed jointly epi */
		if (!L_J.isTotal()) {
			throw new Error("Bug: constructed L_J is not total");
		}
		if (!R_J.isTotal()) {
			throw new Error("Bug: constructed R_J is not total");
		}
		for (Node n : J.getNodes()) {
			if (R_J.getPreimage(n) == null && L_J.getPreimage(n) == null) {
				throw new Error("Bug: node " + n + " has no preimage in L_J or R_J" + this);
			}
		}
		for (Edge e : J.getEdges()) {
			if (R_J.getPreimage(e) == null && L_J.getPreimage(e) == null) {
				throw new Error("Bug: edge " + e + " has no preimage in L_J or R_J");
			}
		}

		return new Cospan(L_J, R_J);
	}

}
