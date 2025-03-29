package de.uni_due.inf.ti.graph.ext;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * <img src="doc-files/square-1.png"> <!-- topLeft: T->L, bottomLeft: L->B,
 * topRight: T->R, bottomRight: R->B -->
 * <p>
 * Represents a square of morphisms as depicted above. A square need not
 * commute, however the participating morphisms must be compatible (matching
 * domains/codomains).
 */
@NonNullByDefault
public class Square {
	private final Span top;
	private final Cospan bottom;

	/**
	 * Constructs a square of the given four morphisms. The square need not
	 * commute (see {@link #isCommuting()}), but the domains and codomains
	 * must be compatible.
	 * @param topLeft top left morphism
	 * @param topRight top right morphism
	 * @param bottomLeft bottom left morphism
	 * @param bottomRight bottom right morphism
	 * @throws IllegalArgumentException
	 *     if the given morphisms' domains or codomains are not compatible
	 */
	public Square(Morphism topLeft, Morphism topRight, Morphism bottomLeft, Morphism bottomRight) {
		if (topLeft.getDomain() != topRight.getDomain()) {
			throw new IllegalArgumentException("topLeft and topRight must have the same domain");
		}
		if (bottomLeft.getCodomain() != bottomRight.getCodomain()) {
			throw new IllegalArgumentException("bottomLeft and bottomRight must have the same codomain");
		}
		Morphism.ensureComposable(topLeft, bottomLeft, "topLeft", "bottomLeft");
		Morphism.ensureComposable(topRight, bottomRight, "topRight", "bottomRight");

		this.top = new Span(topLeft, topRight);
		this.bottom = new Cospan(bottomLeft, bottomRight);
	}

	/**
	 * Constructs a square where the top two morphisms are given by a span,
	 * and the bottom two morphisms are given by a cospan.
	 * @param top top half of the square
	 * @param bottom bottom half of the square
	 * @throws IllegalArgumentException
	 *     if the left/right objects of given span and cospan do not match
	 */
	public Square(Span top, Cospan bottom) {
		if (top.getLeftGraph() != bottom.getLeftGraph()) {
			throw new IllegalArgumentException("top and bottom must have the same left graph");
		}
		if (top.getRightGraph() != bottom.getRightGraph()) {
			throw new IllegalArgumentException("top and bottom must have the same right graph");
		}

		this.top = top;
		this.bottom = bottom;
	}

	/**
	 * @return the top left morphism of the square
	 */
	public Morphism getTopLeft() {
		return top.getLeftMorphism();
	}

	/**
	 * @return the top right morphism of the square
	 */
	public Morphism getTopRight() {
		return top.getRightMorphism();
	}

	/**
	 * @return the bottom left morphism of the square
	 */
	public Morphism getBottomLeft() {
		return bottom.getLeftMorphism();
	}

	/**
	 * @return the bottom right morphism of the square
	 */
	public Morphism getBottomRight() {
		return bottom.getRightMorphism();
	}

	/**
	 * @return the two top morphisms combined into a span
	 */
	public Span getTopSpan() {
		return top;
	}

	/**
	 * @return the two bottom morphisms combined into a cospan
	 */
	public Cospan getBottomCospan() {
		return bottom;
	}

	/**
	 * @return the top graph of the square
	 */
	public Graph getTopGraph() {
		return top.getMiddleGraph();
	}

	/**
	 * @return the left graph of the square
	 */
	public Graph getLeftGraph() {
		return top.getLeftGraph();
	}

	/**
	 * @return the right graph of the square
	 */
	public Graph getRightGraph() {
		return top.getRightGraph();
	}

	/**
	 * @return the bottom graph of the square
	 */
	public Graph getBottomGraph() {
		return bottom.getMiddleGraph();
	}

	/**
	 * Checks if the diagram commutes, that is, bottomLeft ◦ topLeft = bottomRight ◦
	 * topRight.
	 * @return whether the square commutes
	 */
	public boolean isCommuting() {
		Morphism leftPath = top.getLeftMorphism().compose(bottom.getLeftMorphism());
		Morphism rightPath = top.getRightMorphism().compose(bottom.getRightMorphism());
		return leftPath.equals(rightPath);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Square [\n");
		appendIndent(sb, "    T = ", getTopGraph());
		appendIndent(sb, "    L = ", getLeftGraph());
		appendIndent(sb, "    R = ", getRightGraph());
		appendIndent(sb, "    B = ", getBottomGraph());
		appendIndent(sb, "    tl = ", getTopLeft());
		appendIndent(sb, "    tr = ", getTopRight());
		appendIndent(sb, "    bl = ", getBottomLeft());
		appendIndent(sb, "    br = ", getBottomRight());
		sb.append("]");
		return sb.toString();
	}

	private static StringBuilder appendIndent(StringBuilder sb, String prefix, String stuff) {
		sb.append(prefix);
		String indented = stuff.toString().indent(prefix.length());
		sb.append(indented, prefix.length(), indented.length());
		return sb;
	}

	private static StringBuilder appendIndent(StringBuilder sb, String prefix, Object stuff) {
		return appendIndent(sb, prefix, stuff.toString());
	}
}
