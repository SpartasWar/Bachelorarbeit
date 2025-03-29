package de.uni_due.inf.ti.graph.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Abstract base class for n-ary logical connectives ({@link Conjunction}, {@link Disjunction}).
 */
public abstract class NAryConnective implements GraphCondition {
	protected final List<GraphCondition> conditions;
	protected final Graph origin;

	protected NAryConnective(Graph origin, GraphCondition... conditions) {
		this.conditions = Arrays.asList(conditions);
		this.origin = origin;
		checkSameOrigin();
	}

	protected static Graph guessOriginOfConditions(GraphCondition[] conditions) throws IllegalArgumentException {
		if (conditions.length == 0) {
			throw new IllegalArgumentException("Empty logical connective requires explicitly passing origin of condition");
		} else {
			return conditions[0].getOrigin();
		}
	}

	protected static Graph guessOriginOfConditions(List<GraphCondition> conditions) throws IllegalArgumentException {
		if (conditions.size() == 0) {
			throw new IllegalArgumentException("Empty logical connective requires explicitly passing origin of condition");
		} else {
			return conditions.get(0).getOrigin();
		}
	}

	private void checkSameOrigin() {
		// Verify they all have the same origin.
		for (GraphCondition g : conditions) {
			if (!g.getOrigin().equals(origin)) {
				if (g.getOrigin().isIsomorphic(origin)) {
					throw new ArrayStoreException("Child conditions of logical connectives must be over the same graph (is just isomorphic)");
				}
				throw new ArrayStoreException("Child conditions of logical connectives must be over the same graph");
			}
		}
	}

	@Override
	public Graph getOrigin() {
		return this.origin;
	}

	@Override
	public Set<Label> getSignature() {
		/* If we have any children, then use the union of their signatures.
		 * If we have no children, we infer from the origin. As all children
		 * include the origin's signature, we do not explicitly include it
		 * in that case.
		 */
		if (conditions.isEmpty()) {
			return origin.getSignature();
		}
		Set<Label> labels = new HashSet<>();
		for (GraphCondition cond : conditions) {
			labels.addAll(cond.getSignature());
		}
		return labels;
	}

	@Override
	public int getNestingLevel() {
		return this.conditions.stream().mapToInt(c -> c.getNestingLevel()).max().orElseThrow();
	}

	public List<GraphCondition> getChildConditions() {
		return Collections.unmodifiableList(this.conditions);
	}

	/**
	 * Creates a new n-ary connective of the same type as the given object.
	 */
	public abstract NAryConnective newSameType(Graph newOrigin, List<GraphCondition> newConditions);

	/**
	 * Creates a new n-ary connective of the same type as the given object.
	 */
	public abstract NAryConnective newSameType(Graph newOrigin, Stream<GraphCondition> newConditions);

	/**
	 * Creates a new n-ary connective of the opposite type as the given object
	 * (i.e. a conjunction becomes a disjunction).
	 */
	public abstract NAryConnective newOppositeType(Graph newOrigin, List<GraphCondition> newConditions);

	/**
	 * Creates a new n-ary connective of the opposite type as the given object
	 * (i.e. a conjunction becomes a disjunction).
	 */
	public abstract NAryConnective newOppositeType(Graph newOrigin, Stream<GraphCondition> newConditions);

	@Override
	public GraphCondition shift(Morphism m) {
		// TODO: Check if shifting really distributes over conjunction/disjunction
		// TODO: Figure out whether disjunctions require flattening for e.g. preserving CNF (and whether it should be done here)
		Stream<GraphCondition> oldConds;
		if (this.conditions.size() > 10) {
			oldConds = this.conditions.parallelStream();
		} else {
			oldConds = this.conditions.stream();
		}
		Stream<GraphCondition> newConds = oldConds.map(cj -> cj.shift(m));
		return this.newSameType(m.getCodomain(), newConds);
	}

	protected String toString(String connectiveName) {
		return connectiveName + "(\n" + this.conditions.stream().map(c -> c.toString()).collect(Collectors.joining(",\n")).indent(4) + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NAryConnective))
			return false;
		NAryConnective other = (NAryConnective) obj;
		return Objects.equals(conditions, other.conditions) && Objects.equals(origin, other.origin);
	}

	@Override
	public int hashCode() {
		return Objects.hash(conditions, origin);
	}
}
