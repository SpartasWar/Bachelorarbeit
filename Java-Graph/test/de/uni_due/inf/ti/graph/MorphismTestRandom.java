package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import de.uni_due.inf.ti.graph.random.RandomGraphGenerator;

public class MorphismTestRandom
{
    private static final int TESTS = 10; 

    
    static void testMorphismInvariant(String morphismName, Morphism m)
    {
        for (Map.Entry<Edge,Edge> entry : m.getEdgeMap().entrySet()) {
            Edge domEdge = entry.getKey();
            Edge codEdge = entry.getValue();
            assertNotNull(morphismName + ">>>NULL KEYS<<<", domEdge);
            assertNotNull(morphismName + ">>>NULL VALUES<<<", codEdge);
            assertTrue(morphismName + ">>>EDGE ARITY MISMATCH<<<",
                    domEdge.getNodes().size() == codEdge.getNodes().size());
            assertEquals(morphismName + ">>>EDGE LABEL MISMATCH<<<", 
                    domEdge.getLabel(), codEdge.getLabel());
            for (int i = 0; i < domEdge.getNodes().size(); i++) {
                Node domNode = domEdge.getNodes().get(i);
                Node codNode = codEdge.getNodes().get(i);
                assertTrue(morphismName + ">>>NO MORPHISM<<<",
                        m.get(domNode) == codNode);
            }
        }
    }

    @Test
    public void testGetMatches()
    {        
        Random rand = new Random();
        
        for (int i = 1; i < TESTS; i++) {
            Label a = new Label("a", rand.nextInt(3) + 1);
            Label b = new Label("a", rand.nextInt(3) + 1);
            Label c = new Label("c", 2);
            Set<Label> sig = new HashSet<Label>();
            sig.add(a);
            sig.add(b);
            sig.add(c);
            
            RandomGraphGenerator rnd = new RandomGraphGenerator();
            rnd.setSignature(sig);
            
            Graph domain = rnd.nextGraph();
            Graph codomain = rnd.nextGraph();
            
            String name = String.format("%s\n  =>\n%s\n", domain.toString(), codomain.toString());
            
            for (Morphism match : Morphism.getMatches(domain, codomain)) {
                testMorphismInvariant(name, match);   
                assertTrue(name + ">>>NOT TOTAL<<<", match.isTotal());
            }
            
            for (Morphism match : Morphism.getMatches(domain, codomain, true)) {
                testMorphismInvariant(name, match);   
                assertTrue(name + ">>>NOT TOTAL<<<", match.isTotal());
                assertTrue(name + ">>>NOT INJECTIVE<<<", match.isInjective());
            }            
        }
    }

}
