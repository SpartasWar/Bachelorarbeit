package de.uni_due.inf.ti.graph.condition;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Given a condition over C, c_C, with nesting level n, ¬c_C is a condition over C with nesting level n.
 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
 */
public final class Negation implements NestingGraphCondition {
	private final GraphCondition c;

	public Negation(GraphCondition c) {
		this.c = c;
	}

	@Override
	public GraphCondition getChildCondition() {
		return this.c;
	}

	@Override
	public Graph getOrigin() {
		return this.c.getOrigin();
	}

	@Override
	public int getNestingLevel() {
		return this.c.getNestingLevel();
	}

	@Override
	public boolean isLiteral() {
		return this.isNegativeLiteral();
	}

	@Override
	public boolean isPositiveLiteral() {
		return false;
	}

	@Override
	public boolean isNegativeLiteral() {
		return this.c instanceof Existential;
	}

	@Override
	public boolean isClause() {
		return this.isNegativeLiteral();
	}

	@Override
	public boolean isProduct() {
		return this.isNegativeLiteral();
	}

	@Override
	public List<NestingGraphCondition> getClauseLiterals() {
		if (!this.isNegativeLiteral()) {
			throw new ArrayStoreException("This negation is not a literal and cannot be used as a clause");
		}
		return List.of(this);
	}

	@Override
	public List<NestingGraphCondition> getProductLiterals() {
		if (!this.isNegativeLiteral()) {
			throw new ArrayStoreException("This negation is not a literal and cannot be used as a product term");
		}
		return List.of(this);
	}

	@Override
	public boolean isConjunctionOfClauses() {
		return this.isNegativeLiteral();
	}

	@Override
	public boolean isDisjunctionOfProducts() {
		return this.isNegativeLiteral();
	}

	@Override
	public List<GraphCondition> getClauses() {
		if (!this.isNegativeLiteral()) {
			throw new ArrayStoreException("This negation is not a literal and cannot be used as a set of clauses");
		}
		return List.of(this);
	}

	@Override
	public List<GraphCondition> getProducts() {
		if (!this.isNegativeLiteral()) {
			throw new ArrayStoreException("This negation is not a literal and cannot be used as a set of product terms");
		}
		return List.of(this);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return !this.c.satisfiedBy(q);
	}

	@Override
	public GraphCondition shift(Morphism m) {
		return new Negation(this.c.shift(m));
	}

	@Override
	public String toString() {
		return "Not(" + this.c.toString() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Negation))
			return false;
		Negation other = (Negation) obj;
		return Objects.equals(c, other.c);
	}

	@Override
	public int hashCode() {
		return Objects.hash(c);
	}
}
