package de.uni_due.inf.ti.graph.condition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.util.Enumerators;

/**
 * Given conditions over C, c_C and c'_C, with nesting level n and n', respectively,
 * c_C ∧ c'_C = {@code Conjunction(c, cprime)} is a condition over C with nesting level max(n,n').
 * We restrict ourselves to finite conditions, i.e. each conjunction of conditions is finite.
 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
 */
public class Conjunction extends NAryConnective {
	public Conjunction(GraphCondition... conditions) {
		this(guessOriginOfConditions(conditions), conditions);
	}

	public Conjunction(List<GraphCondition> conditions) {
		this(guessOriginOfConditions(conditions), conditions);
	}

	public Conjunction(Graph origin, List<GraphCondition> conditions) {
		this(origin, conditions.toArray(GraphCondition[]::new));
	}

	public Conjunction(Graph origin, Stream<GraphCondition> conditions) {
		this(origin, conditions.toArray(GraphCondition[]::new));
	}

	public Conjunction(Graph origin, GraphCondition... conditions) {
		super(origin, conditions);
	}

	@Override public boolean isLiteral() { return false; }
	@Override public boolean isPositiveLiteral() { return false; }
	@Override public boolean isNegativeLiteral() { return false; }

	@Override
	public boolean isProduct() {
		return Enumerators.enumerate(this.conditions).stream().allMatch(cj -> cj.isLiteral());
	}

	@Override public boolean isClause() { return false; }
	@Override public List<NestingGraphCondition> getClauseLiterals() { throw new UnsupportedOperationException(); }

	@Override
	public boolean isConjunctionOfClauses() {
		return Enumerators.enumerate(this.conditions).stream().allMatch(cj -> cj.isClause());
	}

	/**
	 * {@inheritDoc}
	 * <p>If this conjunction is a product, then it is also a conjunction of clauses
	 * (the conjunction containing just a single clause).
	 */
	@Override
	public boolean isDisjunctionOfProducts() {
		return this.isProduct();
	}

	@Override
	public List<NestingGraphCondition> getProductLiterals() {
		if (!this.isProduct()) {
			throw new ArrayStoreException("This conjunction is not a product");
		}
		// cast is safe since isProduct() guarantees that all child conditions
		// are Existential or Negation (both NestingGraphCondition)
		@SuppressWarnings("unchecked")
		List<NestingGraphCondition> ret = (List<NestingGraphCondition>) (Object) this.getChildConditions();
		return ret;
	}

	@Override
	public List<GraphCondition> getProducts() {
		if (!this.isProduct()) {
			throw new ArrayStoreException("This conjunction is not a product and cannot be used as a disjunction of product(s)");
		}
		return List.of(this);
	}

	@Override
	public List<GraphCondition> getClauses() {
		if (!this.isConjunctionOfClauses()) {
			throw new ArrayStoreException("This conjunction does not contain only clauses");
		}
		return Collections.unmodifiableList(this.conditions);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return Enumerators.enumerate(this.conditions).stream().allMatch(c -> c.satisfiedBy(q));
	}

	@Override
	public NAryConnective newSameType(Graph newOrigin, List<GraphCondition> newConditions) {
		return new Conjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newSameType(Graph newOrigin, Stream<GraphCondition> newConditions) {
		return new Conjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newOppositeType(Graph newOrigin, List<GraphCondition> newConditions) {
		return new Disjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newOppositeType(Graph newOrigin, Stream<GraphCondition> newConditions) {
		return new Disjunction(newOrigin, newConditions);
	}

	@Override
	public String toString() {
		return this.toString("And");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Conjunction) {
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Conjunction.class.hashCode();
	}
}
