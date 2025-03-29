package de.uni_due.inf.ti.graph.condition;

import java.util.List;

import de.uni_due.inf.ti.graph.Morphism;

/**
 * Forall condition, that is, in all cases where a morphism can be decomposed into a and some remainder,
 * the remainder satisfies c.
 *
 * <p>Implemented as ¬∃(a,¬c) using {@link Existential} and {@link Negation}.
 */
public class ForAll extends QuantifiedCondition {
	private Negation nOuter, nInner;
	private Existential ex;

	public ForAll(Morphism a, GraphCondition c) {
		super(a, c);
		// Check compatibility of a : C -> D and the condition c (over D)
		if (!a.getCodomain().equals(c.getOrigin())) {
			if (a.getCodomain().isIsomorphic(c.getOrigin())) {
				throw new IllegalArgumentException("Domain mismatch for forall condition (is just isomorphic)");
			}
			throw new IllegalArgumentException("Domain mismatch for forall condition");
		}
		this.nInner = new Negation(c);
		this.ex = new Existential(a, this.nInner);
		this.nOuter = new Negation(this.ex);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return this.nOuter.satisfiedBy(q);
	}

	@Override public boolean isLiteral() { return false; }
	@Override public boolean isPositiveLiteral() { return false; }
	/**
	 * {@inheritDoc}
	 * <p>Note that even though ForAll(a,c) is equivalent to ¬∃(a,¬c), which is a negative literal,
	 * this method still returns false in this case.
	 */
	@Override public boolean isNegativeLiteral() { return false; }
	@Override public boolean isClause() { return false; }
	@Override public boolean isProduct() { return false; }
	@Override public List<NestingGraphCondition> getClauseLiterals() { throw new UnsupportedOperationException(); }
	@Override public List<NestingGraphCondition> getProductLiterals() { throw new UnsupportedOperationException(); }
	@Override public boolean isConjunctionOfClauses() { return false; }
	@Override public boolean isDisjunctionOfProducts() { return false; }
	@Override public List<GraphCondition> getClauses() { throw new UnsupportedOperationException(); }
	@Override public List<GraphCondition> getProducts() { throw new UnsupportedOperationException(); }
	@Override public boolean isWeakCNF() { return false; }
	@Override public boolean isCNF() { return false; }

	@Override
	public QuantifiedCondition newSameType(Morphism newA, GraphCondition newC) {
		return new ForAll(newA, newC);
	}

	@Override
	public QuantifiedCondition newOppositeType(Morphism newA, GraphCondition newC) {
		return new Existential(newA, newC);
	}

	@Override
	public String toString() {
		return "Forall(\n    " + Existential.strMorphismFull(this.ex.a) + ",\n"
				+ this.nInner.getChildCondition().toString().indent(4) + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ForAll) {
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ ForAll.class.hashCode();
	}
}
