package de.uni_due.inf.ti.graph.ext;

import java.util.Objects;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Represents a graph span, that is, a pair of {@link Morphism}s with
 * common domain.
 * <p>
 * {@code L <-left-- M --right-> R }
 */
@NonNullByDefault
public class Span {
	private final Morphism left, right;

	/**
	 * Constructs a span from two morphisms.
	 * @param left left morphism
	 * @param right right morphism
	 * @throws IllegalArgumentException if the domains do not match
	 */
	public Span(Morphism left, Morphism right) {
		if (left.getDomain() != right.getDomain()) {
			throw new IllegalArgumentException("The domains of two morphisms that make up a span must match");
		}
		this.left = left;
		this.right = right;
	}

	/**
	 * @return left morphism of the span
	 */
	public Morphism getLeftMorphism() {
		return left;
	}

	/**
	 * @return right morphism of the span
	 */
	public Morphism getRightMorphism() {
		return right;
	}

	/**
	 * @return left graph of the span, i.e. the codomain of the left morphism
	 */
	public Graph getLeftGraph() {
		return left.getCodomain();
	}

	/**
	 * @return middle graph of the span, i.e. the common domain
	 */
	public Graph getMiddleGraph() {
		return left.getDomain();
	}

	/**
	 * @return right graph of the span, i.e. the codomain of the right morphism
	 */
	public Graph getRightGraph() {
		return right.getCodomain();
	}

	@Override
	public String toString() {
		return "Span [left=" + left + ", right=" + right + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Span))
			return false;
		Span other = (Span) obj;
		return Objects.equals(left, other.left) && Objects.equals(right, other.right);
	}
}
