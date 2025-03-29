package de.uni_due.inf.ti.graph;

import java.util.HashMap;
import java.util.Map;

class TestGraphs
{
    Label aLab;
    Label bLab;
    
    Graph graphEmpty;
    Graph graphEmpty2;
    Graph graphv;
    Graph graphA;
    Graph graphAA;
    Graph graphAAA;
    Graph graphAABB;
    Graph graphAA_copy;
    Graph graphAa;
    Graph graphAoA;
    Graph graphAB;
    Graph graphAbB;
    Graph graphAra;
    Graph graphTriA;
    Graph graphTriAAB;
    Graph graphAAvv;
    Graph graphB;
    Graph graphAArA;
    Graph graphAArB;
    Graph graphABrB;

    Morphism morph_AA_A;            // injective morphism (non-total)
    Morphism morph_AB_AbB_1;        // injective, total morphism
    Morphism morph_AB_AbB_2;        // injective, total morphism
    
    Morphism morph_v_A; 
    Morphism morph_v_AA;
    
    Morphism match_A_TriA;
    Morphism match_A_AB;
    Morphism match_A_AA_1;    
    Morphism match_A_AA_2;
    
    Rule rule_A_B;
    Rule rule_B_A;
    Rule rule_AA_AB;
    Rule rule_A_AB;
    Rule rule_AB_A;
    Rule rule_A_AA;
    
    private Map<Graph,String> graphNames;
    private Map<Morphism,String> morphismNames;
    
    static Graph createYGraph(Label lab1, Label lab2, Label lab3)
    {
        Graph g = new Graph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        Node n4 = g.addNode();
        g.addEdge(lab1, n1, n2);
        g.addEdge(lab2, n2, n3);
        g.addEdge(lab3, n2, n4);
        return g;
    }

    TestGraphs()
    {
        graphNames = new HashMap<Graph,String>();
        morphismNames = new HashMap<Morphism,String>();

        aLab = new Label("A", 2);
        bLab = new Label("B", 2);

        graphEmpty = new Graph();
        graphEmpty2 = new Graph();
        graphNames.put(graphEmpty, "0");
        graphNames.put(graphEmpty2, "0(2)");
        
        graphv = new Graph();
        Node nodev1 = graphv.addNode();
        graphNames.put(graphv, "v");
        
        graphA = new Graph();
        Node nodeA1 = graphA.addNode();
        Node nodeA2 = graphA.addNode();
        Edge edgeA1 = graphA.addEdge(aLab, nodeA1, nodeA2);
        graphNames.put(graphA, "A");
        
        graphB = new Graph();
        Node nodeB1 = graphB.addNode();
        Node nodeB2 = graphB.addNode();
        graphB.addEdge(bLab, nodeB1, nodeB2);
        graphNames.put(graphB, "B");
        
        graphAAvv = new Graph();
        Node nodeAAvv1 = graphAAvv.addNode();
        Node nodeAAvv2 = graphAAvv.addNode();
        Node nodeAAvv3 = graphAAvv.addNode();
        graphAAvv.addNode();
        graphAAvv.addNode();
        graphAAvv.addEdge(aLab, nodeAAvv1, nodeAAvv2);
        graphAAvv.addEdge(aLab, nodeAAvv2, nodeAAvv3);
        graphNames.put(graphAAvv, "AAvv");

        graphAA = new Graph();
        Node nodeAA1 = graphAA.addNode();
        Node nodeAA2 = graphAA.addNode();
        Node nodeAA3 = graphAA.addNode();
        Edge edgeAA1 = graphAA.addEdge(aLab, nodeAA1, nodeAA2);
        Edge edgeAA2 = graphAA.addEdge(aLab, nodeAA2, nodeAA3);
        graphNames.put(graphAA, "AA");
        
        graphAAA = GraphFactory.createPathGraph(4, aLab);
        graphNames.put(graphAAA, "AAA");
        
        graphAA_copy = new Graph();
        Node nodeAA1_copy = graphAA_copy.addNode();
        Node nodeAA2_copy = graphAA_copy.addNode();
        Node nodeAA3_copy = graphAA_copy.addNode();
        graphAA_copy.addEdge(aLab, nodeAA1_copy, nodeAA2_copy);
        graphAA_copy.addEdge(aLab, nodeAA2_copy, nodeAA3_copy);
        graphNames.put(graphAA_copy, "AA-copy");
        
        graphAABB = new Graph();
        Node nodeAABB1 = graphAABB.addNode();
        Node nodeAABB2 = graphAABB.addNode();
        Node nodeAABB3 = graphAABB.addNode();
        Node nodeAABB4 = graphAABB.addNode();
        Node nodeAABB5 = graphAABB.addNode();
        graphAABB.addEdge(aLab, nodeAABB1, nodeAABB2);
        graphAABB.addEdge(aLab, nodeAABB2, nodeAABB3);
        graphAABB.addEdge(bLab, nodeAABB3, nodeAABB4);
        graphAABB.addEdge(bLab, nodeAABB4, nodeAABB5);
        graphNames.put(graphAABB, "AABB");
        
        graphAa = new Graph();
        Node nodeAa1 = graphAa.addNode();
        Node nodeAa2 = graphAa.addNode();
        graphAa.addEdge(aLab, nodeAa1, nodeAa2);
        graphAa.addEdge(aLab, nodeAa2, nodeAa1);
        graphNames.put(graphAa, "Aa");

        graphAoA = new Graph();
        Node nodeAoA1 = graphAoA.addNode();
        Node nodeAoA2 = graphAoA.addNode();
        graphAoA.addEdge(aLab, nodeAoA1, nodeAoA1);
        graphAoA.addEdge(aLab, nodeAoA1, nodeAoA2);
        graphNames.put(graphAoA, "AoA");
        
        graphAB = new Graph();
        Node nodeAB1 = graphAB.addNode();
        Node nodeAB2 = graphAB.addNode();
        Node nodeAB3 = graphAB.addNode();
        Edge edgeAB1 = graphAB.addEdge(aLab, nodeAB1, nodeAB2);
        Edge edgeAB2 = graphAB.addEdge(bLab, nodeAB2, nodeAB3);
        graphNames.put(graphAB, "AB");
        
        graphAbB = new Graph();
        Node nodeAbB1 = graphAbB.addNode();
        Node nodeAbB2 = graphAbB.addNode();
        Node nodeAbB3 = graphAbB.addNode();
        Edge edgeAbB1 = graphAbB.addEdge(aLab, nodeAbB1, nodeAbB2);
        Edge edgeAbB2 = graphAbB.addEdge(bLab, nodeAbB2, nodeAbB3);
        Edge edgeAbB3 = graphAbB.addEdge(bLab, nodeAbB2, nodeAbB1);
        graphNames.put(graphAbB, "AbB");
        
        graphTriA = new Graph();
        Node nodeTriA1 = graphTriA.addNode();
        Node nodeTriA2 = graphTriA.addNode();
        Node nodeTriA3 = graphTriA.addNode();
        Edge edgeTriA1 = graphTriA.addEdge(aLab, nodeTriA1, nodeTriA2);
        graphTriA.addEdge(aLab, nodeTriA2, nodeTriA3);
        graphTriA.addEdge(aLab, nodeTriA3, nodeTriA1);
        graphNames.put(graphTriA, "TriA");
        
        graphTriAAB = new Graph();
        Node nodeTriAAB1 = graphTriAAB.addNode();
        Node nodeTriAAB2 = graphTriAAB.addNode();
        Node nodeTriAAB3 = graphTriAAB.addNode();
        graphTriAAB.addEdge(aLab, nodeTriAAB1, nodeTriAAB2);
        graphTriAAB.addEdge(aLab, nodeTriAAB2, nodeTriAAB3);
        graphTriAAB.addEdge(bLab, nodeTriAAB3, nodeTriAAB1);
        graphNames.put(graphTriAAB, "TriAAB");
        
        graphAra = new Graph();
        Node nodeAra1 = graphAra.addNode();
        Node nodeAra2 = graphAra.addNode();
        Node nodeAra3 = graphAra.addNode();
        graphAra.addEdge(aLab, nodeAra2, nodeAra1);
        graphAra.addEdge(aLab, nodeAra2, nodeAra3);
        graphNames.put(graphAra, "Ara");
        
        graphAArA = createYGraph(aLab, aLab, aLab);
        graphNames.put(graphAArA, "AArA");
        graphAArB = createYGraph(aLab, aLab, bLab);
        graphNames.put(graphAArB, "AArB");
        graphABrB = createYGraph(aLab, bLab, bLab);
        graphNames.put(graphABrB, "ABrB");
        
        morph_v_A = new Morphism(graphv, graphA);
        morph_v_A.put(nodev1, nodeA1);
        morphismNames.put(morph_v_A, "v->A");
        
        morph_v_AA = new Morphism(graphv, graphAA);
        morph_v_AA.put(nodev1, nodeAA1);
        morphismNames.put(morph_v_AA, "v->AA");
        
        morph_AA_A = new Morphism(graphAA, graphA);
        morph_AA_A.put(edgeAA1, edgeA1);
        morphismNames.put(morph_AA_A, "AA->A");
        
        morph_AB_AbB_1 = new Morphism(graphAB, graphAbB);
        morph_AB_AbB_1.put(edgeAB1, edgeAbB1);
        morph_AB_AbB_1.put(edgeAB2, edgeAbB2);
        morphismNames.put(morph_AB_AbB_1, "AB->AbB (1)");
        
        morph_AB_AbB_2 = new Morphism(graphAB, graphAbB);
        morph_AB_AbB_2.put(edgeAB1, edgeAbB1);
        morph_AB_AbB_2.put(edgeAB2, edgeAbB3);
        morphismNames.put(morph_AB_AbB_2, "AB->AbB (2)");
        
        match_A_TriA = new Morphism(graphA, graphTriA);
        match_A_TriA.put(edgeA1, edgeTriA1);
        morphismNames.put(match_A_TriA, "A->TriA");
        
        match_A_AA_1 = new Morphism(graphA, graphAA);
        match_A_AA_1.put(edgeA1, edgeAA1);
        morphismNames.put(match_A_AA_1, "A->AA(1)");

        match_A_AA_2 = new Morphism(graphA, graphAA);
        match_A_AA_2.put(edgeA1, edgeAA2);
        morphismNames.put(match_A_AA_2, "A->AA(2)");
        
        match_A_AB = new Morphism(graphA, graphAB);
        match_A_AB.put(edgeA1, edgeAB1);
        morphismNames.put(match_A_AB, "A->AB");
        
        {
            Morphism m = new Morphism(graphA, graphB);
            m.put(nodeA1, nodeB1);
            m.put(nodeA2, nodeB2);
            rule_A_B = new Rule(m);
        } {
            Morphism m = new Morphism(graphB, graphA);
            m.put(nodeB1, nodeA1);
            m.put(nodeB2, nodeA2);
            rule_B_A = new Rule(m);
        } {
            Morphism m = new Morphism(graphAA, graphAB);
            m.put(nodeAA1, nodeAB1);
            m.put(nodeAA2, nodeAB2);
            m.put(nodeAA3, nodeAB3);
            rule_AA_AB = new Rule(m);
        } {
            Morphism m = new Morphism(graphA, graphAA);
            m.put(edgeA1, edgeAA1);
            rule_A_AA = new Rule(m);
        } {
            Morphism m = new Morphism(graphA, graphAB);
            m.put(nodeA1, nodeAB1);
            m.put(nodeA2, nodeAB2);
            rule_A_AB = new Rule(m);
        } {
            Morphism m = new Morphism(graphAB, graphA);
            m.put(nodeAB1, nodeA1);
            m.put(nodeAB2, nodeA2);
            rule_AB_A = new Rule(m);
        }
    }
    
    
    String getName(Graph graph)
    {
        String name = graphNames.get(graph);
        if (name == null)
            return "Unnamed graph";
        else
            return name;
    }


    String getName(Morphism morph)
    {
        String name = morphismNames.get(morph);
        if (name == null)
            return "Unnamed morphism";
        else
            return name;
    }
}
