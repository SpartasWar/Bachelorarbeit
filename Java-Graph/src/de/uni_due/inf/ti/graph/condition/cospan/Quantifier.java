package de.uni_due.inf.ti.graph.condition.cospan;

/**
 * A quantifier for conditions ({@link #Exists} or {@link #Forall}).
 */
public enum Quantifier {
	Exists,
	Forall;

	/**
	 * Returns the opposite of this quantifier.
	 */
	public Quantifier other() {
		return this == Exists ? Forall : Exists;
	}
}
