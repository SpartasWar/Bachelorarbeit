package de.uni_due.inf.ti.graph.condition;

import java.util.List;
import java.util.Set;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;

public abstract sealed class Constant implements GraphCondition permits True, False {
	protected final Graph origin;

	protected Constant(Graph origin) {
		this.origin = origin;
	}

	@Override
	public Graph getOrigin() {
		return this.origin;
	}

	@Override
	public Set<Label> getSignature() {
		return this.origin.getSignature();
	}

	@Override
	public int getNestingLevel() {
		return 0;
	}

	@Override public boolean isLiteral() { return false; }
	@Override public boolean isPositiveLiteral() { return false; }
	@Override public boolean isNegativeLiteral() { return false; }
	@Override public boolean isClause() { return false; }
	@Override public boolean isProduct() { return false; }
	@Override public List<NestingGraphCondition> getClauseLiterals() { throw new UnsupportedOperationException(); }
	@Override public List<NestingGraphCondition> getProductLiterals() { throw new UnsupportedOperationException(); }
	@Override public boolean isConjunctionOfClauses() { return false; }
	@Override public boolean isDisjunctionOfProducts() { return false; }
	@Override public List<GraphCondition> getClauses() { throw new UnsupportedOperationException(); }
	@Override public List<GraphCondition> getProducts() { throw new UnsupportedOperationException(); }
	@Override public boolean isWeakCNF() { return true; }
	@Override public boolean isCNF() { return true; }

	public abstract Constant newSame(Graph newOrigin);
	public abstract Constant newOpposite(Graph newOrigin);

	@Override
	public GraphCondition shift(Morphism m) {
		return this.newSame(m.getCodomain());
	}
}