package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.*;

public class MorphismTest
{
    private TestGraphs tg;
    
    
    private void testMorphismInvariant(Morphism m)
    {
        String morphismName = tg.getName(m);
        
        for (Map.Entry<Edge,Edge> entry : m.getEdgeMap().entrySet()) {
            Edge domEdge = entry.getKey();
            Edge codEdge = entry.getValue();
            assertNotNull("Morphism " + morphismName + " has (one or more) null keys.", domEdge);
            assertNotNull("Morphism " + morphismName + " has (one or more) null values.", codEdge);
            assertTrue("Morphism " + morphismName + " maps an edge to an edge with different arity",
                    domEdge.getNodes().size() == codEdge.getNodes().size());
            assertEquals("Morphism " + morphismName + " maps an edge to an edge with a different label", 
                    domEdge.getLabel(), codEdge.getLabel());
            for (int i = 0; i < domEdge.getNodes().size(); i++) {
                Node domNode = domEdge.getNodes().get(i);
                Node codNode = codEdge.getNodes().get(i);
                assertTrue("Morphism " + morphismName + " does not satisfy morphism invariant"
                + " (mapping " + domEdge + "=>" + codEdge + " requires " + domNode + "=>" + codNode + " not =>" + m.get(domNode) + ")",
                        m.get(domNode) == codNode);
            }
        }
    }
    
    private void testTotal(Morphism m, boolean ev)
    {
        String message = String.format("(%s).isTotal() == %b", tg.getName(m), !ev);
        assertTrue(message, m.isTotal() == ev);
    }
    
    private void testSurjective(Morphism m, boolean ev)
    {
        String message = String.format("(%s).isSurjective() == %b", tg.getName(m), !ev);
        assertTrue(message, m.isSurjective() == ev);
    }
    
    private void testIsomorphism(Morphism m, boolean ev)
    {
        String message = String.format("(%s).isIsomorphism() == %b", tg.getName(m), !ev);
        assertTrue(message, m.isIsomorphism() == ev);
    }
    
    
    @Before
    public void generateNewObjects()
    {
        tg = new TestGraphs();
    }

    
    @Test
    public void testMorphisms()
    {
        testMorphismInvariant(tg.morph_AA_A);
        testMorphismInvariant(tg.morph_AB_AbB_1);
        testMorphismInvariant(tg.morph_AB_AbB_2);
    }

    
    @Test
    public void testIsTotal()
    {
        testTotal(tg.morph_AB_AbB_1, true);
        testTotal(tg.morph_AB_AbB_2, true);
        testTotal(tg.morph_AA_A, false);
    }

        
    @Test
    public void testIsSurjective()
    {
        testSurjective(tg.morph_AB_AbB_1, false);
        testSurjective(tg.morph_AB_AbB_2, false);
        testSurjective(tg.morph_AA_A, true);
    }

    
    @Test
    public void testIsIsomorphism()
    {
        testIsomorphism(tg.morph_AB_AbB_1, false);
        testIsomorphism(tg.morph_AB_AbB_2, false);
        testIsomorphism(tg.morph_AA_A, false);
    }
    
    
    @Test
    public void testComposition()
    {
        Morphism compo;
        
        compo = Morphism.createComposition(tg.morph_v_A, tg.match_A_AA_1);
        assertNotNull("Composition 1 == null", compo);
        assertTrue("Composition 1 wrong", compo.equals(tg.morph_v_AA));
    }

	@Test
	public void testIllegalEdgeMaps() {
		Graph g1 = new Graph();
		Node g1n0 = g1.addNode();
		Node g1n1 = g1.addNode();
		Edge g1ea = g1.addEdge(tg.aLab, g1n0, g1n1);
		
		Graph g2 = new Graph();
		Node g2n0 = g2.addNode();
		Node g2n1 = g2.addNode();
		Edge g2eb = g2.addEdge(tg.bLab, g2n0, g2n1);

		Morphism m = Morphism.create(g1, g2);
		assertFalse("mapping A to B should not be possible", m.checkAndPut(g1ea, g2eb));
		testMorphismInvariant(m);

		Graph g3 = new Graph();
		Node g3n0 = g3.addNode();
		Edge g3ea = g3.addEdge(tg.aLab, g3n0, g3n0);

		/* mapping n1 --A-> n2 to n1 --A-> n1 should be possible and produce a noninjective morphism */
		Morphism m2 = Morphism.create(g1, g3);
		assertTrue("mapping edge to loop should be possible", m2.checkAndPut(g1ea, g3ea, false));
		testMorphismInvariant(m2);
		assertTrue("test morphism is total", m2.isTotal());
		assertFalse("test morphism is not injective", m2.isInjective());

		/* mapping n1 --A-> n1 to n1 --A-> n2 should not be possible */
		Morphism m3 = Morphism.create(g3, g1);
		boolean m3_possible = m3.checkAndPut(g3ea, g1ea, false);
		assertFalse("mapping loop to edge should not be possible, but produced " + m3, m3_possible);
		testMorphismInvariant(m3);
	}
}
