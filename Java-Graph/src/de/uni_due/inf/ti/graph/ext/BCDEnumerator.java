package de.uni_due.inf.ti.graph.ext;

import java.util.ArrayList;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.util.Enumerator;

/**
 * Enumerates borrowed context diagrams starting from a pair of cospans (top and left).
 */
public class BCDEnumerator extends Enumerator<BorrowedContextDiagram> {
	private final JointlyEpiSquareEnumerator jese;
	private final Cospan top, left;

	public BCDEnumerator(Cospan top, Cospan left) {
		this.top = top;
		this.left = left;

		/*
		 * We construct BCDs by enumerating jointly epi squares, then completing pushout
		 * complements and pullback if possible.
		 */
		this.jese = new JointlyEpiSquareEnumerator(left.getLeftMorphism(), top.getLeftMorphism());
	}

	@Override
	public BorrowedContextDiagram next() {
		while (true) {
			Square s = this.jese.next();
			if (s == null) {
				return null;
			}
			
			Cospan right, bottom;
			Morphism topCenter, leftCenter, rightCenter, bottomCenter;
			topCenter = s.getBottomRight();
			leftCenter = s.getBottomLeft();
			
			Square pocTR = PushoutComplement.compute(top.getRightMorphism(), topCenter);
			if (pocTR == null) {
				continue;
			}
			rightCenter = pocTR.getBottomRight();
			
			Square pocBL = PushoutComplement.compute(left.getRightMorphism(), leftCenter);
			if (pocBL == null) {
				continue;
			}
			bottomCenter = pocBL.getBottomRight();
			
			Square pb = Pullback.compute(rightCenter, bottomCenter);
			right = new Cospan(pocTR.getTopRight(),  pb.getTopLeft());
			bottom = new Cospan(pocBL.getTopRight(), pb.getTopRight());
			
			return new BorrowedContextDiagram(top, left, right, bottom, topCenter, leftCenter, rightCenter, bottomCenter);
		}
	}
}
