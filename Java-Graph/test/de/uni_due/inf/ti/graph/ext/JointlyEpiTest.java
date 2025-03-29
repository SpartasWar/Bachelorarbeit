package de.uni_due.inf.ti.graph.ext;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Node;

public class JointlyEpiTest {
	private static Graph empty, A, N, NN;
	private static Node N0, NN0, NN1, A0, A1;
	static {
		empty = new Graph();

		N = new Graph();
		N0 = N.addNode();

		NN = new Graph();
		NN0 = NN.addNode();
		NN1 = NN.addNode();

		A = new Graph();
		A0 = A.addNode();
		A1 = A.addNode();
		A.addEdge(new Label("A"), A0, A1);
	}

	@Test
	public void test_empty_empty() {
		List<Cospan> jes = new JointlyEpiMorphismsEnumerator(empty, empty).createList();
		assertEquals("Produces exactly one pair of morphisms", 1, jes.size());
	}

	@Test
	public void test_empty_A() {
		List<Cospan> jes = new JointlyEpiMorphismsEnumerator(empty, A).createList();
		assertEquals("Produces exactly one pair of morphisms", 1, jes.size());

		List<Cospan> jes2 = new JointlyEpiMorphismsEnumerator(A, empty).createList();
		assertEquals("Produces exactly one pair of morphisms", 1, jes2.size());
	}

	@Test
	public void test_N_N() {
		List<Cospan> jes = new JointlyEpiMorphismsEnumerator(N, N).createList();
		assertEquals(2, jes.size());

		Cospan one = jes.stream().filter(je -> (je.getMiddleGraph().getNodes().size() == 1)).findFirst().orElseThrow();
		assertEquals(one.getMiddleGraph().getNodes().get(0), one.getLeftMorphism().get(N0));
		assertEquals(one.getMiddleGraph().getNodes().get(0), one.getRightMorphism().get(N0));
		jes.remove(one);

		Cospan two = jes.get(0);
		assertNotEquals(two.getLeftMorphism().get(N0), two.getRightMorphism().get(N0));
	}

	@Test
	public void test_NN_A() {
		List<Cospan> jes = new JointlyEpiMorphismsEnumerator(NN, A).createList();
		/* The possibilities are:
		 * 1 merge no nodes (J has 4 nodes)
		 * 2 merge 1N with 1A (J has 3 nodes, same for next three)
		 * 3 merge 1N with 2A
		 * 4 merge 2N with 1A
		 * 5 merge 2N with 2A
		 * 6 merge 1N/1A, 2N/2A (J has 2 nodes, same for next)
		 * 7 merge 1N/2A, 2N/1A
		 */
		assertEquals(7, jes.size());

		Cospan one = jes.stream().filter(je -> (je.getMiddleGraph().getNodes().size() == 4)).findFirst().orElseThrow();
		jes.remove(one);
		assertNotEquals(one.getLeftMorphism().get(NN0), one.getRightMorphism().get(A0));
		assertNotEquals(one.getLeftMorphism().get(NN0), one.getRightMorphism().get(A1));
		assertNotEquals(one.getLeftMorphism().get(NN1), one.getRightMorphism().get(A0));
		assertNotEquals(one.getLeftMorphism().get(NN1), one.getRightMorphism().get(A1));

		// TODO complete tests
	}

	@Test
	public void test_A_A() {
		List<Cospan> jes = new JointlyEpiMorphismsEnumerator(A, A).createList();
		/* The possibilities are:
		 * 1 merge no nodes (J has 4 nodes)
		 * 2 merge 1N with 1A (J has 3 nodes, same for next three)
		 * 3 merge 1N with 2A
		 * 4 merge 2N with 1A
		 * 5 merge 2N with 2A
		 * 6 merge 1N/1A, 2N/2A (J has 2 nodes, same for next)
		 * 7 merge 1N/2A, 2N/1A
		 * 8 like 6 but map the edge as well
		 */
		assertEquals(8, jes.size());

		// TODO complete tests
	}
}
