package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_due.inf.ti.graph.util.IsomorphismHashSet;

public class IsomorphismHashSetTest
{
    @Test
    public void test()
    {
        IsomorphismHashSet set = new IsomorphismHashSet();        
        assertTrue("Initial size != 0", set.size() == 0);
        
        set.add(createAA());
        assertTrue("Size error after adding first AA.", set.size() == 1);
        
        set.add(createTriangleA());
        assertTrue("Size error after adding TriangleA", set.size() == 2);
        
        set.add(createAA());
        assertTrue("Size error after adding second AA.", set.size() == 2);
        
        set.add(createABA());
        assertTrue("Size error after adding ABA.", set.size() == 3);

        set.add(createAStar(3));
        assertTrue("Size error after adding first AStar(3).", set.size() == 4);

        set.add(createAStar(3));
        assertTrue("Size error after adding second AStar(3).", set.size() == 4);
        
        set.add(createAStar(5));
        assertTrue("Size error after adding AStar(5).", set.size() == 5);

        assertTrue("Contains error: AA", set.contains(createAA()));
        assertFalse("Contains error: AStar(4)", set.contains(createAStar(4)));
        
        List<Graph> graphs1 = new ArrayList<>();
        graphs1.add(createAA());
        graphs1.add(createABA());
        assertTrue("containsAll error", set.containsAll(graphs1));  
        
        List<Graph> graphs2 = new ArrayList<>(graphs1);
        graphs2.add(createAStar(4));
        assertFalse("constainsAll error", set.containsAll(graphs2));
        
        List<Graph> graphs3 = new ArrayList<>();
        graphs3.add(createTriangleA());
        graphs3.add(createAStar(3));
        graphs3.add(createAStar(5));
        graphs3.add(createAA());
        assertTrue("containsAll error", set.containsAll(graphs3));
        
        set.retainAll(graphs2);
        assertTrue("containsAll error after retainAll", set.containsAll(graphs1));
        assertFalse("containsAll error after retainAll", set.containsAll(graphs3));
    }
    
    private static Graph createAA()
    {
        Label a = new Label("A");        
        Graph graph = new Graph();
        Node v1 = graph.addNode();
        Node v2 = graph.addNode();
        Node v3 = graph.addNode();
        graph.addEdge(a, v1, v2);
        graph.addEdge(a, v2, v3);
        return graph;
    }   
    
    private static Graph createTriangleA()
    {
        Label a = new Label("A");        
        Graph graph = new Graph();
        Node v1 = graph.addNode();
        Node v2 = graph.addNode();
        Node v3 = graph.addNode();
        graph.addEdge(a, v1, v2);
        graph.addEdge(a, v2, v3);
        graph.addEdge(a, v3, v1);
        return graph;        
    } 
    
    private static Graph createABA()
    {
        Label a = new Label("A");
        Label b = new Label("B");
        Graph graph = new Graph();
        Node v1 = graph.addNode();
        Node v2 = graph.addNode();
        Node v3 = graph.addNode();
        Node v4 = graph.addNode();
        graph.addEdge(a, v1, v2);
        graph.addEdge(b, v2, v3);
        graph.addEdge(a, v3, v4);
        return graph;        
    }
    
    private static Graph createAStar(int points)
    {
        Label a = new Label("A");
        Graph graph = new Graph();
        Node vmid = graph.addNode();
        for (int i = 0; i < points; i++) {
            Node v = graph.addNode();
            graph.addEdge(a, v, vmid);
        }
        return graph;
    }
}
