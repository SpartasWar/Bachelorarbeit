package de.uni_due.inf.ti.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_due.inf.ti.util.Enumerator;

@RunWith(Parameterized.class)
public class MorphismEnumeratorTest
{
    private static TestGraphs tg;
    
    private String name;
    private Graph domain;
    private Graph codomain;
    private int numNormal;
    private int numInjective;
    private int numIso;
    private int numPartial;
    private int numPartialInjective;
    
    public MorphismEnumeratorTest(String name, Graph domain, Graph codomain, int n, int i, int iso, int p, int pi)
    {
        this.name = name;
        this.domain = domain;
        this.codomain = codomain;
        
        numNormal = n;
        numInjective = i;
        numIso = iso;
        numPartial = p;
        numPartialInjective = pi;
    }
        
    @Parameters(name="{0}")
    public static Collection<Object[]> createParams()
    {
        tg = new TestGraphs();
        
        Object[][] params = 
            {
                { "0->0", tg.graphEmpty, tg.graphEmpty2, 1, 1, 1, 1, 1 },
                { "v->AAA", tg.graphv, tg.graphAAA, 4, 4, 0, 5, 5 },
                { "A->AA", tg.graphA, tg.graphAA, 2, 2, 0, 18, 15 },
                { "AA->A", tg.graphAA, tg.graphA, 0, 0, 0, 33, 15 },
                { "AA->AA", tg.graphAA, tg.graphAA_copy, 1, 1, 1, 81, 43 },
                { "Aa->Aa", tg.graphAa, tg.graphAa, 2, 2, 2, 15, 13 },
                { "AA->Aa", tg.graphAA, tg.graphAa, 2, 0, 0, 41, 17 },
                { "A->TriA", tg.graphA, tg.graphTriA, 3, 3, 0, -1, -1 },
                { "AA->TriA", tg.graphAA, tg.graphTriA, 3, 3, 0, -1, -1 },
                { "A->AoA", tg.graphA, tg.graphAoA, 2, 1, 0, -1, -1 },
                { "Ara->Ara", tg.graphAra, tg.graphAra, 4, 2, 2, 84, 44 },
                { "AAvv->AABB", tg.graphAAvv, tg.graphAABB, 25, 2, 0, -1, -1 }, 
            };
        return Arrays.asList(params);
    }
    
    static void testMorphismInvariant(String morphismName, Morphism m)
    {
        for (Map.Entry<Edge,Edge> entry : m.getEdgeMap().entrySet()) {
            Edge domEdge = entry.getKey();
            Edge codEdge = entry.getValue();
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
    
    static int enumerate(
            String testName, 
            Enumerator<Morphism> e, 
            boolean tot, 
            boolean inj, 
            boolean iso)
    {
        int count = 0;
        while (true) {
            Morphism next = e.next();
            if (next == null) return count;            
            count++;
            
            String name = String.format("%s-#%d", testName, count);
            testMorphismInvariant(name, next);
            assertTrue("Morphism " + name + " is not total.", 
                    !(tot && !next.isTotal())); 
            assertTrue("Morphism " + name + " is not injective.", 
                    !(inj && !next.isInjective()));
            assertTrue("Morphism " + name + " is not an isomorphism.",
                    !(iso && !next.isIsomorphism()));
        }
    }

    private void test(boolean tot, boolean inj, boolean iso)
    {
        String type;
        int correct;
        
        if (iso) {
            type = "iso";
            correct = numIso;
        }
        else if (tot) {
            if (inj) {
                type = "totinj";
                correct = numInjective;
            }
            else {
                type = "tot";
                correct = numNormal;
            }
        }
        else if (inj) {
            type = "parinj";
            correct = numPartialInjective;
        }
        else {
            type = "par";
            correct = numPartial;
        }
        
        MorphismFinder f = new MorphismFinder();
        f.setTotal(tot);
        f.setInjective(inj);
        f.setIsomorphic(iso);
        
        String testName = String.format("(%s %s)", 
                type, 
                name);
        int count = enumerate(testName, f.enumerate(domain, codomain), tot, inj, iso);
        System.out.printf("%s: %d morphisms%n", testName, count);
        
        if (correct >= 0) {
            String message = String.format("%s has wrong number of morphisms (exp: %d, real: %d)",
                    testName,
                    numNormal,
                    count);
            assertTrue(message, count == correct);
        }
    }

    @Test
    public void testTotal() 
    {
        test(true, false, false);
    }
    
    @Test
    public void testTotalInjective()
    {
        test(true, true, false);
    }
    
    @Test
    public void testPartial()
    {
        test(false, false, false);
    }
    
    @Test
    public void testPartialInjective()
    {
        test(false, true, false);
    }
    
    @Test
    public void testIso()
    {
        test(true, true, true);
    }
}
