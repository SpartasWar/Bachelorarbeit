package de.uni_due.inf.ti.graph.ext;

import java.util.Objects;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;

/**
 * Represents a graph cospan, that is, a pair of {@link Morphism}s with
 * common codomain.
 * <p>
 * {@code L --left-> M <-right-- R }
 */
@NonNullByDefault
public class Cospan {
	private final Morphism left, right;

	/**
	 * Constructs a cospan from two morphisms.
	 * @param left left morphism
	 * @param right right morphism
	 * @throws IllegalArgumentException if the codomains do not match
	 */
	public Cospan(Morphism left, Morphism right) {
		if (left.getCodomain() != right.getCodomain()) {
			throw new IllegalArgumentException("The codomains of two morphisms that make up a cospan must match");
		}
		this.left = left;
		this.right = right;
	}

	/**
	 * @return left morphism of the cospan
	 */
	public Morphism getLeftMorphism() {
		return left;
	}

	/**
	 * @return right morphism of the cospan
	 */
	public Morphism getRightMorphism() {
		return right;
	}

	/**
	 * @return left graph of the cospan, i.e. the domain of the left morphism
	 */
	public Graph getLeftGraph() {
		return left.getDomain();
	}

	/**
	 * @return middle graph of the cospan, i.e. the common codomain
	 */
	public Graph getMiddleGraph() {
		return left.getCodomain();
	}

	/**
	 * @return right graph of the cospan, i.e. the domain of the right morphism
	 */
	public Graph getRightGraph() {
		return right.getDomain();
	}

	/**
	 * @return whether the cospan is an isomorphism (which is the case iff both morphisms are isos)
	 */
	public boolean isIsomorphism() {
		return left.isIsomorphism() && right.isIsomorphism();
	}

	/**
	 * @return whether the two morphisms are jointly epi
	 */
	public boolean isJointlyEpi() {
		for (Node n : getMiddleGraph().getNodes()) {
			if (!(left.inRange(n) || right.inRange(n))) {
				return false;
			}
		}
		for (Edge e : getMiddleGraph().getEdges()) {
			if (!(left.inRange(e) || right.inRange(e))) {
				return false;
			}
		}
		return true;
	}

	private String toString(String indent) {
		String fmt = "%1$sCospan {\n" +
				"%1$s  Left: %2$s\n" +
				"%1$s  LeftMor: %3$s\n" +
				"%1$s  Middle: %4$s\n" +
				"%1$s  RightMor: %5$s\n" +
				"%1$s  Right: %6$s\n" +
				"%1$s}";
		return String.format(fmt,
				indent,
				getLeftGraph().toString(indent + "        ").substring(indent.length() + 8),
				getLeftMorphism().toString(indent + "           ").substring(indent.length() + 11),
				getMiddleGraph().toString(indent + "          ").substring(indent.length() + 10),
				getRightMorphism().toString(indent + "            ").substring(indent.length() + 12),
				getRightGraph().toString(indent + "         ").substring(indent.length() + 9)
		);
	}

	@Override
	public String toString() {
		return toString("");
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Cospan))
			return false;
		Cospan other = (Cospan) obj;
		return Objects.equals(left, other.left) && Objects.equals(right, other.right);
	}

	public static Cospan concatenate(Cospan a, Cospan b) {
		if (a.getRightGraph() != b.getLeftGraph()) {
			throw new IllegalArgumentException("Cannot compose cospans");
		}
		// compute pushout of a_R and b_L
		Square po = Pushout.compute(a.getRightMorphism(), b.getLeftMorphism());
		// compute new cospan
		return new Cospan(
			Morphism.createComposition(a.getLeftMorphism(), po.getBottomLeft()),
			Morphism.createComposition(b.getRightMorphism(), po.getBottomRight())
		);
	}
}
