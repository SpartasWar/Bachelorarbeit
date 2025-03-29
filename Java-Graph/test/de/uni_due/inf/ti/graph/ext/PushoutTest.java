package de.uni_due.inf.ti.graph.ext;

import static de.uni_due.inf.ti.graph.Morphism.createIdentity;
import static de.uni_due.inf.ti.graph.Morphism.getIsomorphism;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;

@RunWith(Parameterized.class)
public class PushoutTest {
	@Parameters(name="{0}")
	public static Collection<Object[]> getPushoutTestData() {
		return Arrays.asList(new Object[][] {
			{ "Verkleben 1", makeVerkleben1() },
			{ "Verkleben 2", makeVerkleben2() },
			{ "Verkleben 3", makeVerkleben3() },
			{ "Verkleben 4", makeVerkleben4() },
			{ "Merge 1", makeMerge1() }
		});
	}

	private final Square reference;

	public PushoutTest(String name, Square reference) {
		this.reference = reference;
	}

	@Test
	public void testPushout() {
		assertPushoutExpected(reference);
	}

	private static Square makeVerkleben1() {
		Label labW = new Label("WL");
		Label labH = new Label("HR");
		Label labF = new Label("F");

		Graph T = new Graph("T");
		Node[] Tn = T.addNodes(2);

		Graph L = new Graph("L");
		Node[] Ln = L.addNodes(2);
		Edge Lw = L.addEdge(labW, Ln[1], Ln[0]);

		Graph R = new Graph("R");
		Node[] Rn = R.addNodes(3);
		Edge Rf = R.addEdge(labF, Rn[0], Rn[2]);
		Edge Rh = R.addEdge(labH, Rn[2], Rn[1]);

		Morphism phi1 = Morphism.create(T, L);
		phi1.put(Tn[0], Ln[0]);
		phi1.put(Tn[1], Ln[1]);

		Morphism phi2 = Morphism.create(T, R);
		phi2.put(Tn[0], Rn[0]);
		phi2.put(Tn[1], Rn[1]);

		// expected result
		Graph B = new Graph("B expected");
		Node[] Bn = B.addNodes(3);
		Edge Bw = B.addEdge(labW, Bn[1], Bn[0]);
		Edge Bf = B.addEdge(labF, Bn[0], Bn[2]);
		Edge Bh = B.addEdge(labH, Bn[2], Bn[1]);
		Morphism psi1 = Morphism.create(L, B);
		psi1.put(Lw, Bw);
		Morphism psi2 = Morphism.create(R, B);
		psi2.put(Rh, Bh);
		psi2.put(Rf, Bf);

		return new Square(phi1, phi2, psi1, psi2);
	}

	private static Square makeVerkleben2() {
		Label labA = new Label("A");

		Graph T = new Graph("T");
		Node[] Tn = T.addNodes(2);

		Graph L = new Graph("L");
		Node[] Ln = L.addNodes(1);

		Graph R = new Graph("R");
		Node[] Rn = R.addNodes(2);
		Edge Ra = R.addEdge(labA, Rn[0], Rn[1]);

		Graph B = new Graph("B expected");
		Node[] Bn = B.addNodes(1);
		Edge Ba = B.addEdge(labA, Bn[0], Bn[0]);

		Morphism phi1 = Morphism.create(T, L);
		phi1.put(Tn[0], Ln[0]);
		phi1.put(Tn[1], Ln[0]);
		assertFalse(phi1.isInjective());

		Morphism phi2 = Morphism.create(T, R);
		phi2.put(Tn[0], Rn[0]);
		phi2.put(Tn[1], Rn[1]);

		Morphism psi1 = Morphism.create(L, B);
		psi1.put(Ln[0], Bn[0]);

		Morphism psi2 = Morphism.create(R, B);
		psi2.put(Ra, Ba);
		assertFalse(psi2.isInjective());

		return new Square(phi1, phi2, psi1, psi2);
	}

	private static Square makeVerkleben3() {
		Label labA = new Label("A");
		Label labB = new Label("B");

		Graph T = new Graph("T");
		Node[] Tn = T.addNodes(3);

		Graph L = new Graph("L");
		Node[] Ln = L.addNodes(2);
		Edge La = L.addEdge(labA, Ln[0], Ln[1]);

		Graph R = new Graph("R");
		Node[] Rn = R.addNodes(2);
		Edge Rb = R.addEdge(labB, Rn[0], Rn[1]);

		Graph B = new Graph("B expected");
		Node[] Bn = B.addNodes(1);
		Edge Ba = B.addEdge(labA, Bn[0], Bn[0]);
		Edge Bb = B.addEdge(labB, Bn[0], Bn[0]);

		Morphism phi1 = Morphism.create(T, L);
		phi1.put(Tn[0], Ln[0]);
		phi1.put(Tn[1], Ln[1]);
		phi1.put(Tn[2], Ln[1]);
		assertFalse(phi1.isInjective());

		Morphism phi2 = Morphism.create(T, R);
		phi2.put(Tn[0], Rn[0]);
		phi2.put(Tn[1], Rn[0]);
		phi2.put(Tn[2], Rn[1]);

		Morphism psi1 = Morphism.create(L, B);
		psi1.put(La, Ba);

		Morphism psi2 = Morphism.create(R, B);
		psi2.put(Rb, Bb);

		return new Square(phi1, phi2, psi1, psi2);
	}

	private static Square makeVerkleben4() {
		Label labA = new Label("A");
		Label labB = new Label("B");
		Label labC = new Label("C");

		Graph T = new Graph("T");
		Node[] Tn = T.addNodes(2);
		Edge Tb = T.addEdge(labB, Tn[0], Tn[1]);

		Graph L = new Graph("L");
		Node[] Ln = L.addNodes(3);
		Edge La = L.addEdge(labA, Ln[0], Ln[1]);
		Edge Lb = L.addEdge(labB, Ln[1], Ln[2]);

		Graph R = new Graph("R");
		Node[] Rn = R.addNodes(3);
		Edge Rb = R.addEdge(labB, Rn[0], Rn[1]);
		Edge Rc = R.addEdge(labC, Rn[1], Rn[2]);

		Graph B = new Graph("B expected");
		Node[] Bn = B.addNodes(4);
		Edge Ba = B.addEdge(labA, Bn[0], Bn[1]);
		Edge Bb = B.addEdge(labB, Bn[1], Bn[2]);
		Edge Bc = B.addEdge(labC, Bn[2], Bn[3]);

		Morphism phi1 = Morphism.create(T, L);
		phi1.put(Tb, Lb);

		Morphism phi2 = Morphism.create(T, R);
		phi2.put(Tb, Rb);

		Morphism psi1 = Morphism.create(L, B);
		psi1.put(La, Ba);
		psi1.put(Lb, Bb);

		Morphism psi2 = Morphism.create(R, B);
		psi2.put(Rb, Bb);
		psi2.put(Rc, Bc);

		return new Square(phi1, phi2, psi1, psi2);
	}

	private static Square makeMerge1() {
		Graph T = new Graph("T");
		Node[] Tn = T.addNodes(5);

		Graph L = new Graph("L");
		Node[] Ln = L.addNodes(3);

		Graph R = new Graph("R");
		Node[] Rn = R.addNodes(3);

		Graph B = new Graph("B expected");
		Node[] Bn = B.addNodes(1);

		Morphism phi1 = Morphism.create(T, L); // 135 ; 2 ; 4
		phi1.put(Tn[0], Ln[0]);
		phi1.put(Tn[2], Ln[0]);
		phi1.put(Tn[4], Ln[0]);
		phi1.put(Tn[1], Ln[1]);
		phi1.put(Tn[3], Ln[2]);

		Morphism phi2 = Morphism.create(T, R); // 1 ; 3 ; 245
		phi2.put(Tn[0], Rn[0]);
		phi2.put(Tn[2], Rn[1]);
		phi2.put(Tn[1], Rn[2]);
		phi2.put(Tn[3], Rn[2]);
		phi2.put(Tn[4], Rn[2]);

		Morphism psi1 = Morphism.create(L, B);
		psi1.put(Ln[0], Bn[0]);
		psi1.put(Ln[1], Bn[0]);
		psi1.put(Ln[2], Bn[0]);

		Morphism psi2 = Morphism.create(R, B);
		psi2.put(Rn[0], Bn[0]);
		psi2.put(Rn[1], Bn[0]);
		psi2.put(Rn[2], Bn[0]);

		return new Square(phi1, phi2, psi1, psi2);
	}

	private static void assertPushoutExpected(Square expected) {
		// make sure there are no mistakes in setting up the morphisms
		assertTrue(expected.getTopLeft().isTotal());
		assertTrue(expected.getTopRight().isTotal());
		assertTrue(expected.getBottomLeft().isTotal());
		assertTrue(expected.getBottomRight().isTotal());
		// compute
		Square actual = Pushout.compute(expected.getTopSpan());
		assertSame(expected.getTopSpan(), actual.getTopSpan());
		// compare
		assertSquaresEqualUptoIso(expected, actual);
	}

	/**
	 * Checks if the expected square is isomorphic to the actual square. Rationale
	 * is that some constructions that generate squares are only unique up to unique
	 * isomorphism, and hence cannot be compared directly.
	 */
	private static void assertSquaresEqualUptoIso(Square expected, Square actual) {
		Morphism expTL = expected.getTopLeft();
		Morphism expTR = expected.getTopRight();
		Morphism expBL = expected.getBottomLeft();
		Morphism expBR = expected.getBottomRight();
		Morphism actTL = actual.getTopLeft();
		Morphism actTR = actual.getTopRight();
		Morphism actBL = actual.getBottomLeft();
		Morphism actBR = actual.getBottomRight();
		Graph expT = expected.getTopGraph();
		Graph expL = expected.getLeftGraph();
		Graph expR = expected.getRightGraph();
		Graph expB = expected.getBottomGraph();
		Graph actT = actual.getTopGraph();
		Graph actL = actual.getLeftGraph();
		Graph actR = actual.getRightGraph();
		Graph actB = actual.getBottomGraph();
		/* compute morphisms that map actual result to expected results.
		 * In case these parts come from the input, just use identity, otherwise figure out some iso
		 */
		Morphism atoeT = actT == expT ? createIdentity(actT) : getIsomorphism(actT, expT);
		Morphism atoeL = actL == expL ? createIdentity(actL) : getIsomorphism(actL, expL);
		Morphism atoeR = actR == expR ? createIdentity(actR) : getIsomorphism(actR, expR);
		Morphism atoeB = actB == expB ? createIdentity(actB) : getIsomorphism(actB, expB);
		assertNotNull("Top objects are isomorphic or identical", atoeT);
		assertNotNull("Left objects are isomorphic or identical", atoeL);
		assertNotNull("Right objects are isomorphic or identical", atoeR);
		assertNotNull("Bottom objects are isomorphic or identical", atoeB);
		assertTrue(new Square(actTR, atoeT, atoeR, expTR).isCommuting());
		assertTrue(new Square(actTL, atoeT, atoeL, expTL).isCommuting());
		assertTrue(new Square(actBR, atoeR, atoeB, expBR).isCommuting());
		assertTrue(new Square(actBL, atoeL, atoeB, expBL).isCommuting());
	}
}
