package de.uni_due.inf.ti.graph.ext;

import de.uni_due.inf.ti.annotation.NonNullByDefault;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Represents a borrowed context diagram of graphs.
 */
@NonNullByDefault
public class BorrowedContextDiagram {
	/* Outer cospans */
	private final Cospan top, left, right, bottom;
	/* Inner morphisms to the center object */
	private final Morphism topCenter, leftCenter, rightCenter, bottomCenter;

	/**
	 * Constructs a square of the given four cospans for the outer morphisms,
	 * and the given four morphisms to the center object. The square need not
	 * commute (see {@link #isCommuting()}), but all domains and codomains
	 * must be compatible.
	 * @throws IllegalArgumentException
	 *     if the given morphisms' domains or codomains are not compatible
	 */
	public BorrowedContextDiagram(
			Cospan top, Cospan left, Cospan right, Cospan bottom,
			Morphism topCenter, Morphism leftCenter, Morphism rightCenter, Morphism bottomCenter
	) {
		/* outers */
		if (top.getLeftGraph() != left.getLeftGraph()) {
			throw new IllegalArgumentException("top and left must have the same domain");
		}
		if (bottom.getRightGraph() != right.getRightGraph()) {
			throw new IllegalArgumentException("bottom and right must have the same codomain");
		}
		if (top.getRightGraph() != right.getLeftGraph()) {
			throw new IllegalArgumentException("top and right must be composable");
		}
		if (left.getRightGraph() != bottom.getLeftGraph()) {
			throw new IllegalArgumentException("left and bottom must be composable");
		}
		/* inners */
		if (top.getMiddleGraph() != topCenter.getDomain()) {
			throw new IllegalArgumentException("topCenter does not originate from top's center graph");
		}
		if (left.getMiddleGraph() != leftCenter.getDomain()) {
			throw new IllegalArgumentException("leftCenter does not originate from left's center graph");
		}
		if (right.getMiddleGraph() != rightCenter.getDomain()) {
			throw new IllegalArgumentException("rightCenter does not originate from right's center graph");
		}
		if (bottom.getMiddleGraph() != bottomCenter.getDomain()) {
			throw new IllegalArgumentException("bottomCenter does not originate from bottom's center graph");
		}
		if (topCenter.getCodomain() != leftCenter.getCodomain()) {
			throw new IllegalArgumentException("topCenter and leftCenter do not agree on center object");
		}
		if (topCenter.getCodomain() != rightCenter.getCodomain()) {
			throw new IllegalArgumentException("topCenter and rightCenter do not agree on center object");
		}
		if (topCenter.getCodomain() != bottomCenter.getCodomain()) {
			throw new IllegalArgumentException("topCenter and bottomCenter do not agree on center object");
		}
		
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.topCenter = topCenter;
		this.leftCenter = leftCenter;
		this.rightCenter = rightCenter;
		this.bottomCenter = bottomCenter;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BCD [\n");
		appendIndent(sb, "    top    = ", top);
		appendIndent(sb, "    left   = ", left);
		appendIndent(sb, "    right  = ", right);
		appendIndent(sb, "    bottom = ", bottom);
		appendIndent(sb, "    center = ", topCenter.getCodomain());
		appendIndent(sb, "    tc = ", topCenter);
		appendIndent(sb, "    lc = ", leftCenter);
		appendIndent(sb, "    rc = ", rightCenter);
		appendIndent(sb, "    bc = ", bottomCenter);
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


	public Cospan getTop() {
		return top;
	}


	public Cospan getLeft() {
		return left;
	}


	public Cospan getRight() {
		return right;
	}


	public Cospan getBottom() {
		return bottom;
	}


	public Morphism getTopCenter() {
		return topCenter;
	}


	public Morphism getLeftCenter() {
		return leftCenter;
	}


	public Morphism getRightCenter() {
		return rightCenter;
	}


	public Morphism getBottomCenter() {
		return bottomCenter;
	}
}
