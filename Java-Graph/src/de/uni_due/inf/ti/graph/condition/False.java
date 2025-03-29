package de.uni_due.inf.ti.graph.condition;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * A condition satisfied by no morphism.
 *
 * <p>This could also be implemented as the negation of true.
 */
public final class False extends Constant {
	public False(Graph origin) {
		super(origin);
	}

	@Override
	public Constant newSame(Graph newOrigin) {
		return new False(newOrigin);
	}

	@Override
	public Constant newOpposite(Graph newOrigin) {
		return new True(newOrigin);
	}

	@Override
	public boolean satisfiedBy(Morphism q) {
		return false;
	}

	@Override
	public String toString() {
		return "false";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof False) {
			return this.origin.equals(((False) other).origin);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.origin.hashCode() ^ False.class.hashCode();
	}
}
