package de.uni_due.inf.ti.graph.condition;

import java.util.List;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * For every morphism a : C → D and condition c_D over graph D with nesting level n
 * such that n ≥ 0, ∃(a,c_D) = {@code Existential(a,c)} is a condition over C with nesting level n+1.
 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
 */
public class Existential extends QuantifiedCondition {
	public Existential(Morphism a, GraphCondition c) {
		super(a, c);
		// Check compatibility of a : C -> D and the condition c (over D)
		if (!a.getCodomain().equals(c.getOrigin())) {
			if (a.getCodomain().isIsomorphic(c.getOrigin())) {
				throw new IllegalArgumentException("Domain mismatch for existential condition (is just isomorphic)");
			}
			throw new IllegalArgumentException("Domain mismatch for existential condition");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>A morphism {@code q : C -> G} satisfies {@code Existential(a, c)}, denoted {@code q |= Existential(a,c)},
	 * if there exists a monomorphism {@code qprime : D -> G} such that {@code qprime o a = q} and {@code qprime |= c}.
	 */
	@Override
	public boolean satisfiedBy(Morphism q) {
		// Check compatibility
		if (!this.getOrigin().equals(q.getDomain())) {
			if (this.getOrigin().isIsomorphic(q.getDomain())) {
				throw new IllegalArgumentException("Domain mismatch for existential condition (is just isomorphic)");
			}
			throw new IllegalArgumentException("Domain mismatch for existential condition");
		}

		for (Morphism qprime : this.a.getCompletions(q, true)) {
			if (this.c.satisfiedBy(qprime)) {
				return true;
			}
		}
		return false;
	}

	@Override public boolean isLiteral() { return true; }
	@Override public boolean isPositiveLiteral() { return true; }
	@Override public boolean isNegativeLiteral() { return false; }
	@Override public boolean isClause() { return true; }
	@Override public boolean isProduct() { return true; }
	@Override public List<NestingGraphCondition> getClauseLiterals() { return List.of(this); }
	@Override public List<NestingGraphCondition> getProductLiterals() { return List.of(this); }
	@Override public boolean isConjunctionOfClauses() { return true; }
	@Override public boolean isDisjunctionOfProducts() { return true; }
	@Override public List<GraphCondition> getClauses() { return List.of(this); }
	@Override public List<GraphCondition> getProducts() { return List.of(this); }

	@Override
	public QuantifiedCondition newSameType(Morphism newA, GraphCondition newC) {
		return new Existential(newA, newC);
	}

	@Override
	public QuantifiedCondition newOppositeType(Morphism newA, GraphCondition newC) {
		return new ForAll(newA, newC);
	}

	@Override
	public String toString() {
		if (this.a.getDomain().hasName() && this.a.getCodomain().hasName()) {
			return "Exists( " + this.a.getDomain().getName() + " -> " + this.a.getCodomain().getName() + " , " + this.c.toString() + ")";
		} else {
			return "Exists(\n    " + strMorphismFull(this.a) + ",\n" + this.c.toString().indent(4) + ")";
		}
	}

	static String strMorphismFull(Morphism m) {
		return strMorphism(m) + "\n      (" + strGraph(m.getDomain()) + "\n    => " + strGraph(m.getCodomain()) + ")";
	}

	static String strMorphism(Morphism m) {
		return m.toString().replace('\n', ' ') + "@" + Integer.toHexString(m.hashCode());
	}

	static String strGraph(Graph g) {
		return g.toString().replaceFirst("Graph", "Graph_" + Integer.toHexString(g.hashCode())).replace('\n', ' ');
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Existential) {
			return super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ Existential.class.hashCode();
	}
}
