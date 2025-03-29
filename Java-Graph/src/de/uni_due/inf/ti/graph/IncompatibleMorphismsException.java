package de.uni_due.inf.ti.graph;

/**
 * Indicates that two given morphisms cannot be combined in the way there were
 * intended to (e.g. not composable, no common domain, etc.)
 */
public class IncompatibleMorphismsException extends IllegalArgumentException {
	private static final long serialVersionUID = -49103393508830725L;

	private final Morphism m1, m2;
	private final String nameM1, nameM2;

	/**
	 * Creates a new exception indicating that the given morphisms are not
	 * compatible.
	 * @param message a message describing the incompatibility
	 * @param firstMorphism first morphism
	 * @param secondMorphism second morphism
	 * @param firstName name of first morphism
	 * @param secondName name of second morphism
	 */
	IncompatibleMorphismsException(String message,
			Morphism firstMorphism, Morphism secondMorphism,
			String firstName, String secondName) {
		super(message);
		this.m1 = firstMorphism;
		this.m2 = secondMorphism;
		this.nameM1 = firstName;
		this.nameM2 = secondName;
	}

	public Morphism getFirstMorphism() { return m1; }
	public Morphism getSecondMorphism() { return m2; }
	public String getFirstName() { return nameM1; }
	public String getSecondName() { return nameM2; }
}
