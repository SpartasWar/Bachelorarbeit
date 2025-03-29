package de.uni_due.inf.ti.graph.condition.cospan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.Enumerators;
import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.annotation.Nullable;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.ext.BCDEnumerator;
import de.uni_due.inf.ti.graph.ext.BorrowedContextDiagram;
import de.uni_due.inf.ti.graph.ext.Cospan;
import de.uni_due.inf.ti.graph.ext.PushoutComplement;
import de.uni_due.inf.ti.graph.ext.Square;

/**
 * A condition using graph cospans.
 *
 * This is effectively reactive systems conditions instantiated in the category of input-linear graph cospans.
 */
@NonNullByDefault
public class CospanCondition {
	private final Graph origin;
	private final Quantifier quantifier;
	/**
	 * Child tuples. Field value always is an unmodifiable list (can be returned
	 * directly)
	 */
	private final List<ChildTuple> children;

	/**
	 * Constructs a new condition with given origin and children.
	 * Children are given as a (varargs) array containing child morphisms
	 * and the associated child condition, alternatingly.
	 * <p>
	 * {@link Boolean} constants may be passed instead of a child
	 * condition, in which case a compatible true or false condition
	 * is automatically constructed.
	 * @param origin
	 *     origin of the new condition
	 * @param quantifier
	 *     quantifier of the new condition ({@link Quantifier#Exists} or
	 *     {@link Quantifier#Forall})
	 * @param morphismsAndChildConds
	 *     (varargs) array containing morphisms and compatible child
	 *     conditions (or {@link Boolean}), alternatingly
	 * @throws IllegalArgumentException
	 *     if {@code morphismsAndChildConds} does not alternate between
	 *     {@code Cospan} and {@code CospanCondition}; if any child
	 *     morphism's domain does not match {@code origin}; or if any
	 *     child morphism is incompatible with its child condition
	 */
	public CospanCondition(Graph origin, Quantifier quantifier, Object... morphismsAndChildConds) {
		this(0, origin, quantifier, unflattenChildTuple(morphismsAndChildConds));
	}

	/**
	 * Constructs a new cospan condition with given origin and list of children.
	 * @param origin
	 *     origin of the new condition
	 * @param quantifier
	 *     quantifier of the new condition ({@link Quantifier#Exists} or
	 *     {@link Quantifier#Forall})
	 * @param children
	 *     (varargs) array of {@link ChildTuple}s
	 * @throws IllegalArgumentException
	 *     if any child morphism's domain does not match {@code origin} or
	 *     if any child morphism is incompatible with its child condition
	 */
	public CospanCondition(Graph origin, Quantifier quantifier, ChildTuple... children) {
		this(0, origin, quantifier, List.of(children));
	}

	/**
	 * Constructs a new cospan condition with given origin and list of children.
	 * @param origin
	 *     origin of the new condition
	 * @param quantifier
	 *     quantifier of the new condition ({@link Quantifier#Exists} or
	 *     {@link Quantifier#Forall})
	 * @param children
	 *     list of {@link ChildTuple}s
	 * @throws IllegalArgumentException
	 *     if any child morphism's domain does not match {@code origin} or
	 *     if any child morphism is incompatible with its child condition
	 */
	public CospanCondition(Graph origin, Quantifier quantifier, List<ChildTuple> children) {
		this(0, origin, quantifier, List.copyOf(children));
	}

	/**
	 * Constructs a new cospan condition with given origin and children obtained from a stream.
	 * @param origin
	 *     origin of the new condition
	 * @param quantifier
	 *     quantifier of the new condition ({@link Quantifier#Exists} or
	 *     {@link Quantifier#Forall})
	 * @param children
	 *     stream of {@link ChildTuple}s
	 * @throws IllegalArgumentException
	 *     if any child morphism's domain does not match {@code origin} or
	 *     if any child morphism is incompatible with its child condition
	 */
	public CospanCondition(Graph origin, Quantifier quantifier, Stream<ChildTuple> children) {
		this(0, origin, quantifier, children.collect(Collectors.toUnmodifiableList()));
	}

	/**
	 * Internal constructor for cospan conditions. This constructor assumes
	 * that the given list of child tuples is not changed after construction
	 * (create a copy if necessary) and that it is unmodifiable (it is returned
	 * unchanged by the {@link #getChildren()} method).
	 * @param _internal ignored, used for overloading signature only
	 * @param origin origin of the new condition
	 * @param quantifier quantifier of the new condition
	 * @param children unmodifiable list of child tuples
	 * @throws IllegalArgumentException
	 *     if any child morphism's domain does not match {@code origin} or
	 *     if any child morphism is incompatible with its child condition
	 */
	private CospanCondition(int _internal, Graph origin, Quantifier quantifier, List<ChildTuple> children) {
		this.origin = origin;
		this.quantifier = quantifier;
		this.children = children;
		checkChildrenMatchOrigin();
	}

	private static List<ChildTuple> unflattenChildTuple(Object[] morphismsAndChildConds) {
		if (morphismsAndChildConds.length % 2 != 0) {
			throw new IllegalArgumentException("Must have even number of arguments (Cospan + child condition)");
		}
		List<ChildTuple> childTuples = new ArrayList<>();
		for (int i = 0; i < morphismsAndChildConds.length; i += 2) {
			if (!(morphismsAndChildConds[i+0] instanceof Cospan)) {
				throw new IllegalArgumentException("Argument " + i + " after quantifier is not a Cospan");
			}
			Cospan mor = (Cospan) morphismsAndChildConds[i+0];

			Object rawCond = morphismsAndChildConds[i+1];
			CospanCondition cond;
			if (rawCond instanceof CospanCondition) {
				cond = (CospanCondition) rawCond;
			} else if (rawCond instanceof Boolean) {
				// Create a matching True or False
				if (((Boolean) rawCond).booleanValue()) {
					cond = True(mor);
				} else {
					cond = False(mor);
				}
			} else {
				throw new IllegalArgumentException("Argument " + (i+1) + " after quantifier is not a CospanCondition");
			}
			childTuples.add(new ChildTuple(mor, cond));
		}
		return Collections.unmodifiableList(childTuples);
	}

	private void checkChildrenMatchOrigin() {
		int i = 0;
		for (ChildTuple child : children) {
			Cospan childMor = child.getMorphism();
			CospanCondition childCond = child.getChildCondition();
			if (!(childMor.getLeftGraph().equals(origin))) {
				throw new IllegalArgumentException(
						"Child[" + i + "] morphism"
						+ " domain " + childMor.getLeftGraph()
						+ " doesn't match origin " + origin);
			}
			if (!(childCond.getOrigin().equals(childMor.getRightGraph()))) {
				throw new IllegalArgumentException(
						"Child[" + i + "] condition"
						+ " origin " + childCond.getOrigin()
						+ " doesn't match child morphism"
						+ " codomain " + childMor.getRightGraph());
			}
			i++;
		}
	}

	/**
	 * @return The graph over which this condition is specified.
	 */
	public Graph getOrigin() {
		return origin;
	}

	/**
	 * @return The quantifier of this condition.
	 */
	public Quantifier getQuantifier() {
		return quantifier;
	}

	/**
	 * @return The child tuples of this condition.
	 */
	public List<ChildTuple> getChildren() {
		return children; // as documented above, field contains unmodifiable list
	}

	/**
	 * Checks whether the given cospan satisfies this condition.
	 * @param a cospan to check
	 * @return whether the given cospan satisfies this condition
	 */
	public boolean satisfiedBy(Cospan a) {
		if (quantifier == Quantifier.Forall) {
			return Enumerators.enumerate(children).stream().allMatch(
				t -> Cospan_getCompletions(t.getMorphism(), a, true /* XXX */)
				.stream().allMatch(
					g -> t.getChildCondition().satisfiedBy(g)
				)
			);
		} else {
			return Enumerators.enumerate(children).stream().anyMatch(
				t -> Cospan_getCompletions(t.getMorphism(), a, true /* XXX */)
				.stream().anyMatch(
					g -> t.getChildCondition().satisfiedBy(g)
				)
			);
		}
	}

	/**
	 * @return a condition that is the negative of this condition
	 */
	public CospanCondition negate() {
		Stream<ChildTuple> negatedChildren = children.stream().map(
			ct -> new ChildTuple(ct.getMorphism(), ct.getChildCondition().negate())
		);
		return new CospanCondition(origin, quantifier.other(), negatedChildren);
	}

	/*
	TODO
	**
	 * Shifts this condition over the morphism {@code c}.
	 * The overlaps are computed using borrowed context diagrams.
	 * <p>The shift operation can be understood as a partial evaluation of a condition
	 * {@code A} under the assumption that some arrow {@code c} is already present.
	 * <p>c;d satisfies A if and only if d satisfies (A shifted by c)
	 *
	 * @param c morphism to shift over/to assume as given
	public CospanCondition shift(Cospan c) {
		return shift(c, __BorrowedContextRepresentativeSquares);
	}

	public CospanCondition shift(Cospan c, IRepresentativeSquareClass<C> kappa) {
		return new CospanCondition(
			c.getCodomain(),
			this.quantifier,
			Enumerators.enumerate(this.children).flatMap((IChildTuple<C> ct) -> {
				IMorphism<C> h = ct.getMorphism();
				ICondition<C> childCond = ct.getChildCondition();
				return kappa.enumerateSquares(new Span<>(h, c)).map(rsq -> {
					IMorphism<C> alpha = rsq.getBottomLeft();
					IMorphism<C> beta = rsq.getBottomRight();
					return new ChildTuple<>(beta, childCond.shift(alpha, kappa));
				});
			}).createList()
		);
	}
	*/

	@Override
	public int hashCode() {
		return Objects.hash(origin, quantifier, children);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CospanCondition))
			return false;
		CospanCondition other = (CospanCondition) obj;
		return Objects.equals(origin, other.origin) && quantifier == other.quantifier
				&& Objects.equals(children, other.children);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Condition [\n");
		sb.append(("origin=" + origin + ",").indent(4));
		sb.append((quantifier + ",").toString().indent(4));
		sb.append("    children=[");
		if (children.size() > 0) {
			sb.append("\n");
			children.forEach(t -> sb.append(t.toEasyString().indent(4)));
		}
		sb.append("]\n]");
		return sb.toString();
	}
	
	public String toEasyString() {
		if (children.size() == 0) {
			return quantifier == Quantifier.Forall ? "true" : "false";
		} else if (children.size() > 1) {
			return toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(quantifier == Quantifier.Forall ? "forall " : "exists ");
		sb.append(children.get(0).toEasyString());
		return sb.toString();
	}

	/**
	 *  A child tuple of a condition (child morphism and child condition).
	 */
	public static final class ChildTuple {
		private final Cospan morphism;
		private final CospanCondition childCondition;

		/**
		 * Constructs a new child tuple for a cospan condition.
		 * @param morphism the cospan to match
		 * @param childCondition the child condition
		 */
		public ChildTuple(Cospan morphism, CospanCondition childCondition) {
			this.morphism = morphism;
			this.childCondition = childCondition;
		}

		/**
		 * @return the child cospan/morphism
		 */
		public Cospan getMorphism() {
			return morphism;
		}

		/**
		 * @return the child condition
		 */
		public CospanCondition getChildCondition() {
			return childCondition;
		}

		@Override
		public int hashCode() {
			return Objects.hash(childCondition, morphism);
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof ChildTuple))
				return false;
			ChildTuple other = (ChildTuple) obj;
			return Objects.equals(childCondition, other.childCondition) && Objects.equals(morphism, other.morphism);
		}

		@Override
		public String toString() {
			return "(\n" + morphism.toString().indent(4) + ",\n" + childCondition.toEasyString().indent(4) + ")";
		}

		/**
		 * FIXME: outputs certain cospans in a very simplified way, if possible.
		 * resorts to toString otherwise.
		 * this is mostly for some very specific debugging purpose
		 * @return
		 */
		public String toEasyString() {
			Cospan c = getMorphism();
			if (!c.getLeftGraph().isDiscrete() || !c.getRightGraph().isDiscrete() || !c.getMiddleGraph().isDiscrete()) {
				// has edges
				return toString();
			}
			for (Entry<Node, Node> entry : c.getLeftMorphism().getNodeMap().entrySet()) {
				if (entry.getKey().getId() != entry.getValue().getId()) {
					// non trivial mapping
					return toString();
				}
			}
			for (Entry<Node, Node> entry : c.getRightMorphism().getNodeMap().entrySet()) {
				if (entry.getKey().getId() != entry.getValue().getId()) {
					// non trivial mapping
					return toString();
				}
			}
			// ok we can really stringify by giving list of node ids!
			String out = "{ ";
			if (c.getLeftGraph().isEmpty()) {
				out += "empty";
			} else {
				out += c.getLeftGraph().getNodes().stream().map(n -> ""+n.getId()).collect(Collectors.joining(" "));
			}
			out += " -> ";
			if (c.getMiddleGraph().isEmpty()) {
				out += "empty";
			} else {
				out += c.getMiddleGraph().getNodes().stream().map(n -> ""+n.getId()).collect(Collectors.joining(" "));
			}
			out += " <- ";
			if (c.getRightGraph().isEmpty()) {
				out += "empty";
			} else {
				out += c.getRightGraph().getNodes().stream().map(n -> ""+n.getId()).collect(Collectors.joining(" "));
			}
			out += " } ";
			out += getChildCondition().toEasyString();
			return out;
		}
	}

	/**
	 * Returns a condition originating at {@code origin} that is satisfied by all arrows.
	 * @param origin origin graph for the condition
	 */
	public static CospanCondition True(Graph origin) {
		return new CospanCondition(origin, Quantifier.Forall);
	}

	/**
	 * Returns a condition originating at {@code o} that is satisfied by no arrow.
	 * @param origin origin graph for the condition
	 */
	public static CospanCondition False(Graph origin) {
		return new CospanCondition(origin, Quantifier.Exists);
	}

	/**
	 * Returns a condition originating at the codomain of {@code m} that is satisfied by
	 * all arrows.
	 * @return a true condition over {@code m}'s codomain
	 *
	 * @apiNote The intended usage is {@code new ChildTuple(m, True(m))}.
	 */
	public static CospanCondition True(Cospan m) {
		return True(m.getRightGraph());
	}

	/**
	 * Returns a condition originating at the codomain of {@code m} that is satisfied by
	 * no arrow.
	 * @return a false condition over {@code m}'s codomain
	 *
	 * @apiNote The intended usage is {@code new ChildTuple(m, False(m))}.
	 */
	public static CospanCondition False(Cospan m) {
		return False(m.getRightGraph());
	}

	/**
	 * Returns a condition that is a conjunction of the given conditions, i.e.
	 * <tt>(ForAll, { (id, c[0]), (id, c[1]), ... })</tt>
	 */
	public static CospanCondition And(CospanCondition... conditions) {
		return createAndOr(Quantifier.Forall, conditions);
	}

	/**
	 * Returns a condition that is a conjunction of the given conditions, i.e.
	 * <tt>(ForAll, { (id, c[0]), (id, c[1]), ... })</tt>
	 */
	public static CospanCondition And(List<CospanCondition> conditions) {
		return createAndOr(Quantifier.Forall, conditions);
	}

	/**
	 * Returns a condition that is a disjunction of the given conditions, i.e.
	 * <tt>(Exists, { (id, c[0]), (id, c[1]), ... })</tt>
	 */
	public static CospanCondition Or(CospanCondition... conditions) {
		return createAndOr(Quantifier.Exists, conditions);
	}

	/**
	 * Returns a condition that is a disjunction of the given conditions, i.e.
	 * <tt>(Exists, { (id, c[0]), (id, c[1]), ... })</tt>
	 */
	public static CospanCondition Or(List<CospanCondition> conditions) {
		return createAndOr(Quantifier.Exists, conditions);
	}

	private static CospanCondition createAndOr(Quantifier quantifier, CospanCondition[] conditions) {
		Graph origin = conditions[0].getOrigin();
		Morphism idO = Morphism.createIdentity(origin);
		Cospan id = new Cospan(idO, idO);
		List<ChildTuple> childTuples = new ArrayList<>();

		for (CospanCondition c : conditions) {
			childTuples.add(new ChildTuple(id, c));
		}
		return new CospanCondition(0, origin, quantifier, childTuples);
	}

	private static CospanCondition createAndOr(Quantifier quantifier, List<CospanCondition> conditions) {
		Graph origin = conditions.get(0).getOrigin();
		Morphism idO = Morphism.createIdentity(origin);
		Cospan id = new Cospan(idO, idO);
		List<ChildTuple> childTuples = new ArrayList<>();

		for (CospanCondition c : conditions) {
			childTuples.add(new ChildTuple(id, c));
		}
		return new CospanCondition(0, origin, quantifier, childTuples);
	}

	/**
	 * Computes all completions of the cospan {@code f} to another morphism. This means
	 * that for each {@code g} in the stream, {@code large = f;g}.
	 * @param f morphism that shall be completed
	 * @param large morphism that {@code f} shall be completed to
	 * @param mono whether only injective morphisms {@code g} are considered
	 */
	private static Enumerator<Cospan> Cospan_getCompletions(Cospan f, Cospan large, boolean mono) {
		// XXX figure out what to do with the mono parameter, in particular since we're input linear anyway
		Morphism fL = f.getLeftMorphism();
		Morphism fLpL = large.getLeftMorphism();
		Morphism gRpR = large.getRightMorphism();
		Morphism fR = f.getRightMorphism();
		/* referring to the diagram, we know fL and fL;pL - determine (possible values of) pL */
		return fL.getCompletions(fLpL, mono).flatMap((Morphism pL) -> {
			/* compute the pushout complement (if it exists), giving us gL and pR */
			// TODO: Check if multiple POCs need to be considered here
			Square poc = PushoutComplement.compute(fR, pL);
			if (poc == null) {
				return Enumerators.emptyEnumerator();
			}
			Morphism  gL = poc.getTopRight();
			Morphism pR = poc.getBottomRight();
			/* we know pR and gR;pR and we need gR */
			return Morphism_getReverseCompletions(pR, gRpR, mono).map((Morphism gR) -> {
				/* construct g */
				return new Cospan(gL, gR);
			});
		});
	}

	private static Enumerator<Morphism> Morphism_getReverseCompletions(Morphism f, Morphism large, boolean mono) {
		// A --g-> B --f-> C
		// enumerate *all* possible g, then return only those that actually are a completion
		Enumerator<Morphism> rcompEnum = Morphism.getMatches(large.getDomain(), f.getDomain(), mono);
		return rcompEnum.filter(g -> g.compose(f).equals(large));
	}

	public CospanCondition shift(Cospan cospan) {
		List<ChildTuple> resultChildren = new ArrayList<>();
		
		for (ChildTuple child : this.children) {
			/* enumerate bcds:
			 * top = cospan from child cond
			 * left = cospan to shift over
			 * right = cospan to shift child conds over
			 * bottom = cospan for shifted child tuple
			 */
			BCDEnumerator bcde = new BCDEnumerator(child.getMorphism(), cospan);
			
			for (BorrowedContextDiagram bcd : bcde) {
				System.out.println(bcd);
				ChildTuple ch = new ChildTuple(
						bcd.getBottom(),
						child.getChildCondition().shift(bcd.getRight())
				);
				resultChildren.add(ch);
			}
		}
		return new CospanCondition(cospan.getRightGraph(), this.quantifier, resultChildren);
	}
}
