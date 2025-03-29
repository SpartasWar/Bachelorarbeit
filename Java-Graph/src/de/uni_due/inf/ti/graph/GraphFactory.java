/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the names of its copyright holder(s) nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.uni_due.inf.ti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;


/**
 * This is a factory class which contains methods which create 
 * {@code Graph} and {@code InterfaceGraph} objects.
 */
public class GraphFactory
{
    private static @MonotonicNonNull Random rand = null;
    
    private static final String EXC_LABEL_NON_BINARY = "Edge label must be binary.";
    private static final String EXC_LABEL_NULLARY = "Edge label cannot be nullary.";

    
    private GraphFactory()
    {
        // prevent instantiation
    }
    
    
    /* ---------------------------------------------------------------------- *
     *                     Factory methods for Graph                          *
     * ---------------------------------------------------------------------- */

    /**
     * Creates a new discrete graph.
     * A discrete graph is a graph which contains no edges.
     * 
     * @param numOfNodes    
     *      the number of nodes in the new graph
     * @return              
     *      a discrete graph with {@code numOfNodes} nodes
     */
    @SideEffectFree
    public static Graph createDiscreteGraph(int numOfNodes)
    {
        Graph graph = new Graph();
        for (int i = 0; i < numOfNodes; i++) 
            graph.addNode();
        return graph;
    }
    
    /**
     * Creates a new complete graph.
     * The created graph has {@code numOfNodes} nodes and an edge with the
     * specified label (which must be binary) between each pair of nodes.
     * If the boolean argument {@code loops} is {@code false}, then no 
     * loops are added, that is, no edges are added between each node and 
     * itself.
     * 
     * @param numOfNodes    
     *      number of nodes in the new graph
     * @param label         
     *      label of all the edges in the graph; the label must be binary, that
     *      is {@code label.getArity() == 2}
     * @param loops         
     *      if {@code true}, the returned graph also contains loops
     * @return
     *      a complete graph with {@code numOfNodes} nodes
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code label} an arity different than 2
     */
    @SideEffectFree
    public static Graph createCompleteGraph(
            int numOfNodes, 
            Label label, 
            boolean loops)
    {
        if (label.getArity() != 2)
            throw new IllegalArgumentException(EXC_LABEL_NON_BINARY);
        
        Graph graph = new Graph();
        List<Node> nodes = new ArrayList<Node>(numOfNodes);
        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(graph.addNode());
        }
        
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = 0; j < numOfNodes; j++) {
                if (i != j || loops) 
                    graph.addEdge(label, nodes.get(i), nodes.get(j));
            }
        }
        
        return graph;
    }
    
    /**
     * Creates a new complete graph.
     * The created graph has {@code numOfNodes} nodes and an edge with the
     * specified label (which must be binary) between each pair of nodes.
     * <p>
     * The method call {@code createCompleteGraph(non, l)} is equivalent to
     * the method call {@code createCompleteGraph(non, l, true)}.
     * 
     * @param numOfNodes
     *      number of nodes in the new graph
     * @param label
     *      label of all the edges in the graph; the label must be binary, that
     *      is {@code label.getArity() == 2}
     * @return
     *      a complete graph with {@code numOfNodes} nodes
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code label} an arity different than 2
     * @see #createCompleteGraph(int,Label,boolean)
     */
    @SideEffectFree
    public static Graph createCompleteGraph(int numOfNodes, Label label)
    {
        return createCompleteGraph(numOfNodes, label, true);
    }
    
    /**
     * Creates a new cycle graph.
     * 
     * @param numOfNodes    
     *      the number of nodes in the new graph
     * @param label
     *      label of all the edges in the graph; the label must be binary, that
     *      is {@code label.getArity() == 2}
     * @return
     *      a new cycle graph with {@code numOfNodes} nodes
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code label} an arity different than 2
     */
    @SideEffectFree
    public static Graph createCycleGraph(int numOfNodes, Label label)
    {
        if (label.getArity() != 2)  
            throw new IllegalArgumentException(EXC_LABEL_NON_BINARY);
        
        Graph graph = new Graph();
        List<Node> nodes = new ArrayList<Node>(numOfNodes);
        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(graph.addNode());
        }
        
        for (int i = 0; i < numOfNodes - 1; i++) {
            graph.addEdge(label, nodes.get(i), nodes.get(i + 1));
        }
        graph.addEdge(label, nodes.get(numOfNodes - 1), nodes.get(0));
        
        return graph;
    }
    
    /**
     * Creates a new path graph.
     * 
     * @param numOfNodes    
     *      the number of nodes in the new graph
     * @param label         
     *      label of all the edges in the graph; the label must be binary, that
     *      is {@code label.getArity() == 2}
     * @return
     *      a new cycle graph with {@code numOfNodes} nodes
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code label} an arity different than 2
     */
    @SideEffectFree
    public static Graph createPathGraph(int numOfNodes, Label label)
    {
        if (label.getArity() != 2)
            throw new IllegalArgumentException(EXC_LABEL_NON_BINARY);
        
        Graph graph = new Graph();
        List<Node> nodes = new ArrayList<Node>(numOfNodes);
        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(graph.addNode());
        }
        
        for (int i = 0; i < numOfNodes - 1; i++) {
            graph.addEdge(label, nodes.get(i), nodes.get(i + 1));
        }
        
        return graph;
    }
    
    /**
     * Creates a flower graph of a given signature. The flower graph of a 
     * signature is a graph with a single node and one edge for each label
     * in the signature, of which each port is connected to the single node
     * of the graph.
     * 
     * @param signature
     *      signature for which a flower graph is created
     * @return
     *      a flower graph for the given signature
     * @throws NullPointerException
     *      if {@code signature} is {@code null} or contains a {@code null}
     *      reference
     */
    @SideEffectFree
    public static Graph createFlowerGraph(Collection<Label> signature)
    {
        Graph result = new Graph();
        Node node = result.addNode();
        
        for (Label label : signature) {
            List<Node> atNodes = new ArrayList<>(label.getArity());
            for (int i = 0; i < label.getArity(); i++) {
                atNodes.add(node);
            }
            result.addEdge(label, atNodes);
        }
        
        return result;
    }
    
    /**
     * Creates a new random graph of which the path width will not exceed
     * a given bound.
     * <p>
     * The new graph has {@code numOfNodes} nodes and 
     * at least {@code density} * {@code numOfNodes}<sup>2</sup> edges.
     * Edge labels are randomly selected from a given set of possible
     * edge labels.
     * <p>
     * The given signature {@code sig} may not contain labels of arity 0.
     * 
     * @param numOfNodes
     *      number of nodes in the new graph
     * @param density
     *      density of the new graph
     * @param pathWidth
     *      path width of the new graph
     * @param sig
     *      set of labels which can occur in the new graph
     * @return
     *      a new, randomly generated graph with the specified characteristics
     * @throws NullPointerException
     *      if {@code sig} is {@code null} or contains a {@code null}
     *      reference
     * @throws IllegalArgumentException 
     *      if {@code sig} contains labels of arity 0
     */
    @SideEffectFree
    public static Graph createRandomGraphWithPathWidth(
            int numOfNodes, 
            double density, 
            int pathWidth, 
            Set<Label> sig)
    {
        for (Label label : sig) {
            if (label.getArity() == 0) 
                throw new IllegalArgumentException(EXC_LABEL_NULLARY);
        }
        
        if (rand == null) rand = new Random();

        Label[] labelArray = sig.toArray(new Label[sig.size()]); 
        
        Graph graph = new Graph();
        
        List<List<Node>> bags = new ArrayList<List<Node>>();
        
        List<Node> cur = new ArrayList<Node>();
        for (int i = 0; i < pathWidth; i++) {
            cur.add(graph.addNode());
        }
        List<Node> bag = new ArrayList<Node>(cur);
        bags.add(bag);
        for (int i = 0; i < (numOfNodes - pathWidth); i++) {
            int removeIdx = rand.nextInt(pathWidth);
            cur.remove(removeIdx);
            cur.add(graph.addNode());
            bag = new ArrayList<Node>(cur);
            bags.add(bag);
        }
        
        int numOfEdges = (int)(2 * density * (double)pathWidth);

        for (int i = 0; i < numOfEdges; i++) {
            int bagNr = rand.nextInt(bags.size());
            bag = bags.get(bagNr);
            Label label = labelArray[rand.nextInt(labelArray.length)];
            int arity = label.getArity();
            if (arity < 0) arity = rand.nextInt(3) + 1;
            List<Node> nodes = new ArrayList<Node>(arity);
            for (int n = 0; n < arity; n++) {
                int con = rand.nextInt(pathWidth);
                nodes.add(bag.get(con));
            }
            graph.addEdge(label, nodes);
        }
        
        return graph;
    }
    
    
    
    /* ---------------------------------------------------------------------- *
     *                 Factory methods for InterfaceGraph                     *
     * ---------------------------------------------------------------------- */
    
    /**
     * Creates a new interface graph which represents the identity operation.
     * 
     * @param ifsize        
     *      interface size of the new interface graph
     * @return              
     *      InterfaceGraph which represents the identity operation with 
     *      interface size {@code ifsize}
     */
    @SideEffectFree
    public static InterfaceGraph createIdentity(int ifsize) 
    {
        InterfaceGraph op = new InterfaceGraph();
        for (int i = 0; i < ifsize; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);
        }
        return op;
    }
    
    /**
     * Creates a new interface graph which represents the vertex operation.
     * 
     * @param ifsize
     *      inner interface size of the new interface graph
     * @param newNodes
     *      number of nodes which is added by the new interface graph
     * @return 
     *      InterfaceGraph object which represents the vertex operation with 
     *      inner interface size {@code ifSize} that adds {@code newNodes} new 
     *      nodes
     */
    @SideEffectFree
    public static InterfaceGraph createVertex(int ifsize, int newNodes)
    {
        InterfaceGraph op = new InterfaceGraph();
        for (int i = 0; i < ifsize; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);
        }
        for (int i = ifsize; i < ifsize + newNodes; i++) {
            Node n = op.addNode();
            op.getOuterInterface().add(n);            
        }
        return op; 
    }
    
    /**
     * Creates a new interface graph which represents the vertex operation.
     * <p>
     * The method call {@code createVertex(n)} is equivalent to
     * {@code createVertex(n, 1)}.
     * 
     * @param ifsize
     *      inner interface size of the new interface graph
     * @return  
     *      InterfaceGraph object which represents the operation with inner 
     *      interface size {@code ifSize} that adds a new node
     */
    @SideEffectFree
    public static InterfaceGraph createVertex(int ifsize)
    {
        return createVertex(ifsize, 1);
    }
    
    /**
     * Creates a new interface graph which corresponds to the "connect" 
     * atomic operation.
     * Composing an interface graph with the new interface graph has the 
     * effect of adding an edge. 
     * <p>
     * This form of the method allows to specify exactly how the new edge is to
     * be attached to the interface nodes by using the {@code attach} 
     * parameter. The <i>n</i>th integer in this list specifies the index within
     * the outer interface of the <i>n</i>th node attached to the new edge.
     * 
     * @param ifsize        
     *      size of the inner interface of the new interface graph
     * @param label
     *      label of the edge to be added
     * @param attach
     *      attachment information
     * @return
     *      an InterfaceGraph which correspond to the atomic operation "connect"
     * @throws NullPointerException 
     *      if {@code label} or {@code attach} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code label}'s arity is larger than the size of {@code attach}
     */
    @SideEffectFree
    public static InterfaceGraph createConnect(int ifsize, Label label, List<Integer> attach) 
    {
        int ar = label.getArity();
        if (ar < 0) {
            ar = attach.size();
        }
        else {
            if (ar > attach.size()) {
                throw new IllegalArgumentException("Size of the attachment " +
                		"list cannot be lower than the arity of the label.");
            }
        }
        
        InterfaceGraph op = new InterfaceGraph();
        
        for (int i = 0; i < ifsize; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);
        }
        
        List<Node> nodes = new ArrayList<Node>(ar);
        for (int i = 0; i < ar; i++) {
            int n = attach.get(i);
            if (n >= ar) {
                throw new IllegalArgumentException("New edge is attached to " +
                        "non-existing node.");
            }
            nodes.add(op.getInnerInterface().get(n));
        }
        
        op.addEdge(label, nodes);
        
        return op;
    }
    
    /**
     * Creates a new interface graph which corresponds to the "connect" 
     * atomic operation.
     * Composing an interface graph with the new interface graph has the 
     * effect of adding an edge.
     * <p>
     * This form of the method produces an interface graph which attaches the 
     * new edge to the first nodes of the outer interface. 
     * 
     * @param ifsize        
     *      size of the inner interface of the new interface graph
     * @param label
     *      label of the edge to be added
     * @return
     *      an InterfaceGraph which correspond to the atomic operation "connect"
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     */
    @SideEffectFree
    public static InterfaceGraph createConnect(int ifsize, Label label)
    {
        int ar = label.getArity();
        if (ar < 0) {
            throw new IllegalArgumentException("Label must have an explicit " +
            		"arity.");
        }
        if (ifsize < ar) {
            throw new IllegalArgumentException("Arity of the label cannot be " +
            		"greater than interface size.");
        }
        
        InterfaceGraph op = new InterfaceGraph();
        List<Node> nodes = new ArrayList<Node>(ar);
        
        for (int i = 0; i < ar; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);
            nodes.add(n);
        }
        for (int i = ar; i < ifsize; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);            
        }
                
        op.addEdge(label, nodes);
        
        return op;
    }

    /**
     * Creates an interface graph which corresponds to the "restrict" atomic
     * operation, which removes a number of nodes from the interface.
     * 
     * @param ifsize       
     *      interface size of the new operation
     * @param nodes         
     *      number of nodes that are removed by the operation
     * @return
     *      a new graph which corresponds to the "restrict" operation
     */
    @SideEffectFree
    public static InterfaceGraph createRestrict(int ifsize, int nodes)
    {
        InterfaceGraph op = new InterfaceGraph();
        
        int stay = ifsize - nodes;
        
        for (int i = 0; i < stay; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);
            op.getOuterInterface().add(n);
        }
        for (int i = stay; i < ifsize; i++) {
            Node n = op.addNode();
            op.getInnerInterface().add(n);            
        }
        
        return op; 
    }
    
    /**
     * Creates an interface graph which corresponds to the "restrict" atomic
     * operation, which removes a single node from the interface.
     * 
     * @param ifsize       
     *      interface size of the new operation
     * @return
     *      a new graph which corresponds to the "restrict" operation
     */
    @SideEffectFree
    public static InterfaceGraph createRestrict(int ifsize) 
    {
        return createRestrict(ifsize, 1);
    }
}
