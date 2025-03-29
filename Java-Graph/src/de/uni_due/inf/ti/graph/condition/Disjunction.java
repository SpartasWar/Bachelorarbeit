package de.uni_due.inf.ti.graph.condition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.util.Enumerators;

/**
 * Disjunction, that is, a condition which is satisfied if at least one child condition is satisfied.
 */
public class Disjunction extends NAryConnective {
	public Disjunction(GraphCondition... conditions) {
		this(guessOriginOfConditions(conditions), conditions);
	}

	public Disjunction(List<GraphCondition> conditions) {
		this(guessOriginOfConditions(conditions), conditions);
	}

	public Disjunction(Graph origin, List<GraphCondition> conditions) {
		this(origin, conditions.toArray(GraphCondition[]::new));
	}

	public Disjunction(Graph origin, Stream<GraphCondition> conditions) {
		this(origin, conditions.toArray(GraphCondition[]::new));
	}

	public Disjunction(Graph origin, GraphCondition... conditions) {
		super(origin, conditions);
	}

	@Override public boolean isLiteral() { return false; }
	@Override public boolean isPositiveLiteral() { return false; }
	@Override public boolean isNegativeLiteral() { return false; }

	@Override
	public boolean isClause() {
		return Enumerators.enumerate(this.conditions).stream().allMatch(cj -> cj.isLiteral());
	}

	@Override public boolean isProduct() { return false; }
	@Override public List<NestingGraphCondition> getProductLiterals() { throw new UnsupportedOperationException(); }

	@Override
	public List<NestingGraphCondition> getClauseLiterals() {
		if (!this.isClause()) {
			throw new ArrayStoreException("This disjunction is not a clause");
		}
		// cast is safe since isClause() guarantees that all child conditions
		// are Existential or Negation (both NestingGraphCondition)
		@SuppressWarnings("unchecked")
		List<NestingGraphCondition> ret = (List<NestingGraphCondition>) (Object) this.getChildConditions();
		return ret;
	}

	/**
	 * {@inheritDoc}
	 * <p>If this disjunction is a clause, then it is also a conjunction of clauses
	 * (the conjunction containing just a single clause).
	 */
	@Override
	public boolean isConjunctionOfClauses() {
		return this.isClause();
	}

	@Override
	public boolean isDisjunctionOfProducts() {
		return Enumerators.enumerate(this.conditions).stream().allMatch(cj -> cj.isProduct());
	}

	@Override
	public List<GraphCondition> getProducts() {
		if (!this.isDisjunctionOfProducts()) {
			throw new ArrayStoreException("This disjunction does not contain only products");
		}
		return Collections.unmodifiableList(this.conditions);
	}

	@Override
	public List<GraphCondition> getClauses() {
		if (!this.isClause()) {
			throw new ArrayStoreException("This disjunction is not a clause and cannot be used as a conjunction of clause(s)");
		}
		return List.of(this);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return Enumerators.enumerate(this.conditions).stream().anyMatch(c -> c.satisfiedBy(q));
	}

	@Override
	public NAryConnective newSameType(Graph newOrigin, List<GraphCondition> newConditions) {
		return new Disjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newSameType(Graph newOrigin, Stream<GraphCondition> newConditions) {
		return new Disjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newOppositeType(Graph newOrigin, List<GraphCondition> newConditions) {
		return new Conjunction(newOrigin, newConditions);
	}

	@Override
	public NAryConnective newOppositeType(Graph newOrigin, Stream<GraphCondition> newConditions) {
		return new Conjunction(newOrigin, newConditions);
	}

	@Override
	public String toString() {
		return this.toString("Or");
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Disjunction) {
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Disjunction.class.hashCode();
	}
}
