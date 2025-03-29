package de.uni_due.inf.ti.graph.condition;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * true is a condition over C. We say that true has nesting level 0.
 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
 */
public final class True extends Constant {
	public True(Graph origin) {
		super(origin);
	}

	@Override
	public Constant newSame(Graph newOrigin) {
		return new True(newOrigin);
	}

	@Override
	public Constant newOpposite(Graph newOrigin) {
		return new False(newOrigin);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return true;
	}

	@Override
	public String toString() {
		return "true";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof True) {
			return this.origin.equals(((True) other).origin);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.origin.hashCode() ^ True.class.hashCode();
	}
}
