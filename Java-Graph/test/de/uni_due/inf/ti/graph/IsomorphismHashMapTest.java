package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uni_due.inf.ti.graph.util.IsomorphismHashMap;
import de.uni_due.inf.ti.graph.util.IsomorphismMap;
import de.uni_due.inf.ti.graph.util.IsomorphismSet;

public class IsomorphismHashMapTest
{

    @Test
    public void test()
    {
        IsomorphismMap<String> map = new IsomorphismHashMap<>();
        map.put(createAA(), "AA-1");
        map.put(createABA(), "ABA-1");
        map.put(createAStar(3), "AStar(3)");
        
        String val = map.get(createAA());
        assertNotNull("get AA null", val);
        assertTrue("get AA failed", val.equals("AA-1"));
        
        val = map.get(createAStar(4));
        assertNull("get AStar(4) not null", val);
        
        IsomorphismSet set = map.keySet();
        assertTrue("keySet error", set.contains(createABA()));
        assertFalse("keySet error", set.contains(createTriangleA()));
        set.remove(createAA());
        
        val = map.get(createAA());
        assertNull("keySet remove failed", val);
        
        val = map.remove(createABA());
        assertNotNull("map.remove returns null", val);
        
        val = map.get(createABA());
        assertNull("map contains ABA after remove", val);
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
