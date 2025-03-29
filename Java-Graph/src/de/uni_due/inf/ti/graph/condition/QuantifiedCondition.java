package de.uni_due.inf.ti.graph.condition;

import java.util.Objects;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.ext.JointlyEpiSquareEnumerator;
import de.uni_due.inf.ti.util.Enumerator;

/**
 * Abstract base class for Exists and ForAll type conditions.
 */
public abstract class QuantifiedCondition implements NestingGraphCondition {
	protected final Morphism a;
	protected final GraphCondition c;

	protected QuantifiedCondition(Morphism a, GraphCondition c) {
		this.a = a;
		this.c = c;
	}

	@Override
	public Graph getOrigin() {
		return this.a.getDomain();
	}

	public Morphism getMorphism() {
		return this.a;
	}

	@Override
	public GraphCondition getChildCondition() {
		return this.c;
	}

	@Override
	public int getNestingLevel() {
		return this.c.getNestingLevel() + 1;
	}

	@Override
	public GraphCondition shift(Morphism m) {
		/*
		 * To implement Shift(m, Q(a, c_C)) = V[ (a',m') in F ]  Q(a', Shift(m', c_C))
		 * where F = { (a', m') jointly surjective | m' mono and diagram below commutes }
		 *
		 * P --m--> P'
		 * |     (l)|
		 * a        a'
		 * |        |
		 * v(r)     v
		 * C --m'-> C'
		 *
		 * P is what Q(...) is specified over
		 * C is what c_C is specified over
		 */
		// TODO check if this merger of the reactive systems definition and the leen-lambers definition is legal
		JointlyEpiSquareEnumerator jeme = new JointlyEpiSquareEnumerator(m, this.a);
		// TODO filter out non-monos
		Enumerator<GraphCondition> newChildren = jeme.map(ap_mp -> this.newSameType(ap_mp.getBottomLeft(), this.c.shift(ap_mp.getBottomRight())));
		if (this instanceof Existential) {
			return new Disjunction(m.getCodomain(), newChildren.createList());
		} else if (this instanceof ForAll) {
			return new Conjunction(m.getCodomain(), newChildren.createList());
		} else {
			throw new IllegalArgumentException("Abstract implementation of shift cannot handle " + this.getClass().getSimpleName());
		}
	}

	/**
	 * Creates a new quantified condition of the same type as the given object.
	 */
	public abstract QuantifiedCondition newSameType(Morphism newA, GraphCondition newC);

	/**
	 * Creates a new quantified condition of the opposite type as the given object.
	 */
	public abstract QuantifiedCondition newOppositeType(Morphism newA, GraphCondition newC);

	@Override
	public int hashCode() {
		return Objects.hash(a, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof QuantifiedCondition))
			return false;
		QuantifiedCondition other = (QuantifiedCondition) obj;
		return Objects.equals(a, other.a) && Objects.equals(c, other.c);
	}
}
