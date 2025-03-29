package de.uni_due.inf.ti.graph;

/**
 * Indicates that two given morphisms have different domains.
 */
public class DomainMismatchException extends IncompatibleMorphismsException {
	private static final long serialVersionUID = 8090312313133011433L;

	/**
	 * Creates a new exception indicating that the given morphisms have
	 * different domains.
	 * @param firstMorphism first morphism
	 * @param secondMorphism second morphism
	 * @param firstName name of first morphism
	 * @param secondName name of second morphism
	 */
	public DomainMismatchException(Morphism firstMorphism, Morphism secondMorphism, String firstName, String secondName) {
		super(
			"Morphisms " + firstName + " and " + secondName + " have no common domain",
			firstMorphism, secondMorphism,
			firstName, secondName);
	}
}
