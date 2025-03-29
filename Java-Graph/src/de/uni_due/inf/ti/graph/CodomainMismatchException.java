package de.uni_due.inf.ti.graph;

/**
 * Indicates that two given morphisms have different codomains.
 */
public class CodomainMismatchException extends IncompatibleMorphismsException {
	private static final long serialVersionUID = 4911508070938824580L;

	/**
	 * Creates a new exception indicating that the given morphisms have
	 * different codomains.
	 * @param firstMorphism first morphism
	 * @param secondMorphism second morphism
	 * @param firstName name of first morphism
	 * @param secondName name of second morphism
	 */
	public CodomainMismatchException(Morphism firstMorphism, Morphism secondMorphism, String firstName, String secondName) {
		super(
			"Morphisms " + firstName + " and " + secondName + " have no common codomain",
			firstMorphism, secondMorphism,
			firstName, secondName);
	}
}
