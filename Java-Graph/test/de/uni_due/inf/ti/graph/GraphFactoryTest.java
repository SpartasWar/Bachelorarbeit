package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class GraphFactoryTest
{

    @Test
    public void testCreateDiscreteGraph()
    {
        for (int i = 0; i < 20; i++) {
            Graph dg = GraphFactory.createDiscreteGraph(i);
            String message = String.format("createDiscreteGraph(%d) not discrete", i);
            assertTrue(message, dg.isDiscrete());
        }
    }
    
    private static void testComplete(Graph g, boolean loops) 
    {
        for (Node n1 : g.getNodes()) {
            for (Node n2 : g.getNodes()) {
                if (loops || n1 != n2) {
                    boolean edgeFnd = false;
                    for (Edge e : g.getEdges()) {
                        List<Node> edgeNodes = e.getNodes();
                        assertTrue(edgeNodes.size() == 2);
                        if (edgeNodes.get(0) == n1 && edgeNodes.get(1) == n2) {
                            edgeFnd = true;
                            break;
                        }                
                    }
                    assertTrue(edgeFnd);
                }
            }
        }
    }

    @Test
    public void testCreateCompleteGraphIntLabelBoolean()
    {
        Label a = new Label("a", 2);
        for (int i = 0; i < 20; i++) {
            Graph cg = GraphFactory.createCompleteGraph(i, a, true);
            testComplete(cg, true);
            Graph cgl = GraphFactory.createCompleteGraph(i, a, false);
            testComplete(cgl, false);
        }
    }


}
