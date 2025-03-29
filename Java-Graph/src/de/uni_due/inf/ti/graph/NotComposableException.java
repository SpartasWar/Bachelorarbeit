package de.uni_due.inf.ti.graph;

/**
 * Indicates that two given morphisms are not composable ({@code m2 o m1}
 * is not defined), i.e. the codomain of m1 does not equal the domain of m2.
 */
public class NotComposableException extends IncompatibleMorphismsException {
	private static final long serialVersionUID = 5094069350444493845L;

	/**
	 * Creates a new exception indicating that the given morphisms are not
	 * composable ({@code secondMorphism o firstMorphism} is not defined),
	 * i.e. the codomain of firstMorphism does not equal the domain of
	 * secondMorphism.
	 * @param firstMorphism first morphism
	 * @param secondMorphism second morphism
	 * @param firstName name of first morphism
	 * @param secondName name of second morphism
	 */
	public NotComposableException(Morphism firstMorphism, Morphism secondMorphism, String firstName, String secondName) {
		super(
			"Morphisms " + firstName + " and " + secondName + " are not composable",
			firstMorphism, secondMorphism,
			firstName, secondName);
	}
}
