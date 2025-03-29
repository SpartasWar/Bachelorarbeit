package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class MorphismTestParam
{    
    private static TestGraphs tg;
    
    private String name;
    private Graph domain;
    private Graph codomain;
    private int numNormal;
    private int numInjective;
    private int numIso;
    
    public MorphismTestParam(String nm, Graph dom, Graph cod, int n, int inj, int iso)
    {
        name = nm;
        domain = dom;
        codomain = cod;
        numNormal = n;
        numInjective = inj;
        numIso = iso;
    }
    
    
    @Parameters(name="{0}")
    public static Collection<Object[]> createParams()
    {
        tg = new TestGraphs();
        
        Object[][] params = 
            {
                { "e->e", tg.graphEmpty, tg.graphEmpty2, 1, 1, 1 },
                { "v->AAA", tg.graphv, tg.graphAAA, 4, 4, 0 },
                { "A->AA", tg.graphA, tg.graphAA, 2, 2, 0 },
                { "AA->A", tg.graphAA, tg.graphA, 0, 0, 0 },
                { "AA->AA-copy", tg.graphAA, tg.graphAA_copy, 1, 1, 1 },
                { "Aa->Aa", tg.graphAa, tg.graphAa, 2, 2, 2 },
                { "AA->Aa", tg.graphAA, tg.graphAa, 2, 0, 0 },
                { "A->TriA", tg.graphA, tg.graphTriA, 3, 3, 0 },
                { "AA->TriA", tg.graphAA, tg.graphTriA, 3, 3, 0 },
                { "A->AoA", tg.graphA, tg.graphAoA, 2, 1, 0 },
                { "Ara->Ara", tg.graphAra, tg.graphAra, 4, 2, 2 },
                { "AAvv->AABB", tg.graphAAvv, tg.graphAABB, 25, 2, 0 }, 
            };
        return Arrays.asList(params);
    }
    
    
    static void testMorphismInvariant(String morphismName, Morphism m)
    {
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
                assertTrue("Morphism " + morphismName + " does not satisfy morphism invariant",
                        m.get(domNode) == codNode);
            }
        }
    }

    
    @Test
    public void testGetMatches() 
    {
        int count = 0;
        String testName = String.format("(normal %s->%s)", tg.getName(domain), tg.getName(codomain));
        
        for (Morphism match : Morphism.getMatches(domain, codomain)) {
            testMorphismInvariant(testName + count, match);
            count++;
        }
        
        if (numNormal >= 0) {
            String message = String.format("%s has wrong number of morphisms (exp: %d, real: %d)",
                    testName,
                    numNormal,
                    count);
            assertTrue(message, count == numNormal);
        }
    }
    
    
    @Test
    public void testGetInjectiveMatches()
    {
    	int count = 0;
    	String testName = String.format("(injective %s->%s)", tg.getName(domain), tg.getName(codomain));
    	
    	for (Morphism match : Morphism.getMatches(domain, codomain, true)) {
    		testMorphismInvariant(testName + count, match);
    		count++;
    	}
    	
    	if (numInjective >= 0) {
            String message = String.format("%s has wrong number of injective morphisms (exp: %d, real: %d)",
                    testName,
                    numInjective,
                    count);
            assertTrue(message, count == numInjective);
    	}
    }
    
    
    @Test
    public void testGetIsomorphisms()
    {
        int count = 0;
        String testName = String.format("(iso %s->%s)", tg.getName(domain), tg.getName(codomain));
        
        for (Morphism match : Morphism.getIsomorphisms(domain, codomain)) {
            String mName = testName + "-" + count;
            testMorphismInvariant(mName, match);
            assertTrue(mName + " not isomorphic", match.isIsomorphism());
            count++;
        }
        
        if (numIso >= 0) {
            String message = String.format("%s has wrong number of isomorphisms (exp: %d, real: %d)",
                    testName,
                    numIso,
                    count);
            assertTrue(message, count == numIso);
        }        
    }
    
    
    public String toString() 
    {
        return name;
    }
}
