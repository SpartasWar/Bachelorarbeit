/* 
 * Copyright (c) 2012-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingList;

import de.uni_due.inf.ti.util.Functions2;
import de.uni_due.inf.ti.util.StringUtils;


/**
 * This class represents a hypergraph with an inner and an outer interface.
 * This is more or less equivalent to cospan of graph morphisms, where the
 * domains of the two morphisms are discrete graphs.
 */
public final class InterfaceGraph extends Graph
{
    private List<Node> inIf;
    private Interface publicInIf = null;
    private List<Node> outIf;
    private Interface publicOutIf = null;
    
    /**
     * List which updates the parent class's modCount when it is modified.
     */
    public class Interface extends ForwardingList<Node>
    {
        private List<Node> delegate;
        
        public Interface(List<Node> delegate) 
        {
            this.delegate = delegate;
        }
        
        @Override@SideEffectFree
        protected List<Node> delegate()
        {
            return delegate;
        }
        
        @Override
        public boolean add(Node el)
        {
            if (!inIf.contains(el)) throw new IllegalArgumentException();
            return delegate.add(el); 
        }
        
        @Override
        public void add(int index, Node el)
        {
            if (!inIf.contains(el)) throw new IllegalArgumentException();
            delegate.add(index, el);
        }
        
        @Override
        public boolean addAll(Collection<? extends Node> col)
        {
            for (Node n : col) 
                if (!inIf.contains(n)) throw new IllegalArgumentException();
            return delegate.addAll(col);
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends Node> col)
        {
            for (Node n : col) 
                if (!inIf.contains(n)) throw new IllegalArgumentException();
            return delegate.addAll(index, col);
        }
        
        @Override
        public void clear() 
        {
            delegate.clear();
        }
        
        @Override
        public Node remove(int index)
        {
            return delegate.remove(index);
        }
        
        @Override
        public boolean removeAll(Collection<?> col)
        {
            return delegate.removeAll(col);
        }

        @Override
        public boolean retainAll(Collection<?> col)
        {
            return delegate.retainAll(col);
        }
        
        @Override
        public Node set(int index, Node el) 
        {
            return delegate.set(index, el);
        }
        
        @Override
        public Iterator<Node> iterator()
        {
            return standardIterator();
        }
        
        @Override
        public ListIterator<Node> listIterator()
        {
            return standardListIterator();
        }
        
        @Override
        public ListIterator<Node> listIterator(int start)
        {
            return standardListIterator(start);
        }
        
        @Override
        public List<Node> subList(int fromIndex, int toIndex)
        {
            return standardSubList(fromIndex, toIndex);
        }
        
        /**
         * Determines whether this interface is injective.
         * This interface is injective, when all nodes occur in it at most once.
         * 
         * @return          {@code true} when this interface is injective,
         *                  {@code false} otherwise
         */
        @Pure
        public boolean isInjective() 
        {
            for (int i = 0; i < size(); i++) {
                for (int j = i; j < size(); j++) {
                    if (get(i) == get(j)) return false;
                }
            }
            return true;
        }
        
        /**
         * Returns this interface as a morphism from a given domain to the
         * graph of which this is an interface.
         * The given domain must be a discrete graph with the same number of
         * nodes as this interface.
         * <p>
         * The morphism is built as follows: it maps the <i>i</i>th node of 
         * the domain to the <i>i</i>th node of this interface, where the 
         * order of the nodes in the graph is the order in which they are 
         * returned by the underlying node set's iterator.
         * 
         * @param domain
         *      the domain of the new morphism
         * @return
         *      a morphism which represents this interface
         * @throws NullPointerException
         *      if {@code domain} is {@code null}
         * @throws IllegalArgumentException 
         *      if the domain graph is not discrete or has a different number
         *      of nodes as this interface
         */
        @SideEffectFree
        public Morphism asMorphism(Graph domain)
        {
            ArrayList<Node> nodes = new ArrayList<Node>(domain.getNodes());
            
            if (nodes.size() != size()) {
                throw new IllegalArgumentException("Number of nodes in " +
                		"domain must be equal to interface size.");
            }
            if (!domain.isDiscrete()) {
                throw new IllegalArgumentException("Domain must a discrete " +
                		"graph.");
            }
            
            Map<Node,Node> nodeMap = new HashMap<Node,Node>(nodes.size());
            for (int i = 0; i < size(); i++) {
                nodeMap.put(nodes.get(i), delegate.get(i));
            }
            
            return Morphism.create(domain, InterfaceGraph.this, nodeMap);
        }
        
        /**
         * Returns this interface as a morphism to the graph of which this is
         * an interface. The domain of the morphism is a newly created 
         * discrete graph with the same number of nodes as this interface.
         * <p>
         * The newly created domain can be obtained by using the
         * {@link Morphism#getDomain()} method.
         * <p>
         * The morphism is built as follows: it maps the <i>i</i>th node of 
         * the domain to the <i>i</i>th node of this interface, where the 
         * order of the nodes in the graph is the order in which they returned
         * by the underlying node set's iterator.
         * 
         * @return
         *      a morphism which represents this interface
         * @see #asMorphism(Graph)
         */
        @SideEffectFree
        public Morphism asMorphism() 
        {
            Graph domain = GraphFactory.createDiscreteGraph(size());
            return asMorphism(domain);
        }
    }
    
    
    /**
     * Creates a new empty InterfaceGraph with empty interfaces.
     */
    @SideEffectFree
    public InterfaceGraph()
    {
        inIf = new ArrayList<Node>();
        outIf = new ArrayList<Node>();
    }
    
    /**
     * Creates a new InterfaceGraph which is a copy of another InterfaceGraph
     * or Graph.
     * <p>
     * If the argument is an InterfaceGraph, than the new InterfaceGraph is
     * its exact copy.
     * Otherwise, it is a copy of the graph with two empty interfaces added.
     * 
     * @param orig          
     *      the graph of which the new InterfaceGraph will be a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public InterfaceGraph(Graph orig) 
    {
        // don't call super copy constructor, because this constructor
        // will copy everything itself (a necessary hack, because auxiliary
        // data structures need to be retained).
        
        Map<Node,Node> nodeMap = new HashMap<Node,Node>();
        
        for (Node node : orig.getNodes()) {
            Node newNode = addNode(node.getId());
            nodeMap.put(node, newNode);
        }
        
        for (Edge edge : orig.getEdges()) {
            List<Node> ans = new ArrayList<Node>();
            for (Node n : edge.getNodes()) {
                assert (n.getGraph() == orig);
                ans.add(nodeMap.get(n));
            }
            addEdge(edge.getLabel(), ans);
        }

        inIf = new ArrayList<Node>();
        outIf = new ArrayList<Node>();
        
        if (orig instanceof InterfaceGraph) {
            InterfaceGraph o = (InterfaceGraph)orig;
            for (Node n : o.inIf) 
                inIf.add(nodeMap.get(n));
            for (Node n : o.outIf)
                outIf.add(nodeMap.get(n));
        }
        
        setAttribute(orig.getAttribute());
        setName(orig.getName());
    }

    /**
     * Returns the inner interface of this graph.
     */
    @Pure
    public Interface getInnerInterface() 
    {
        if (publicInIf == null) {
            publicInIf = new Interface(inIf);
        }
        return publicInIf;
    }
    
    /**
     * Returns the inner interface of this graph.
     * This method is an alias for {@link InterfaceGraph#getInnerInterface()}.
     */
    @Pure
    public Interface getLeftInterface()
    {
        return getInnerInterface();
    }
    
    /**
     * Returns the outer interface of this graph.
     */
    @Pure
    public Interface getOuterInterface()
    {
        if (publicOutIf == null) {
            publicOutIf = new Interface(outIf);
        }
        return publicOutIf;
    }
    
    /**
     * Returns the outer interface of this graph.
     * This method is an alias for {@link InterfaceGraph#getOuterInterface()}.
     */
    @Pure
    public Interface getRightInterface()
    {
        return getOuterInterface();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void mergeNodes(Collection<Node> nodes, Node target)
    {
        super.mergeNodes(nodes, target);
        
        Function<Node,Node> rename = Functions2.newRenameFunction(nodes, target);
        Utils.map(inIf, rename);
        Utils.map(outIf, rename);
    }
    
    /**
     * Compose another graph to this graph.
     */
    public void compose(InterfaceGraph other) 
    {
        List<Node> if1 = this.outIf;
        List<Node> if2 = other.inIf;
        if (if1.size() != if2.size())
            throw new IllegalArgumentException("Graph is not composable to " +
            		"this graph.");
        
        // add the nodes of other, or find the nodes of this graph with which
        // they are merged
        Map<Node,Node> nodeMap = new HashMap<Node,Node>();
        for (Node n : other.nodes) {
            int nIdx = other.inIf.indexOf(n);
            if (nIdx >= 0) {
                nodeMap.put(n, outIf.get(nIdx));
            }
            else {
                Node newN = addNode();
                nodeMap.put(n, newN);
            }
        }
        
        // add the edges of other
        for (Edge e : other.edges) {
            Label label = e.getLabel();
            List<Node> ns = new ArrayList<Node>(e.getNodes());
            Utils.map(ns, nodeMap);
            addEdge(label, ns);
        }
        
        // change the outer interface (here, because then it is updated
        // during the merging thereafter)
        Node[] oldOutIf = outIf.toArray(new Node[0]);
        outIf.clear();
        outIf.addAll(other.outIf);
        Utils.map(outIf, nodeMap);
    
        // merge nodes if other.inIf is not injective
        List<Node> oIn = new ArrayList<Node>(other.inIf);
        for (int i = 0; i < oIn.size(); i++) {
            Node ni = oIn.get(i);
            if (ni != null) {
                Set<Node> mergeSet = new HashSet<Node>();
                mergeSet.add(oldOutIf[i]);
                for (int j = i; j < oIn.size(); j++) {
                    Node nj = oIn.get(j);
                    if (ni == nj) mergeSet.add(oldOutIf[j]);    
                }
                if (mergeSet.size() > 1) {
                    mergeNodes(mergeSet);
                }
            }
        }
    }
    
    /**
     * Decomposes this InterfaceGraph in all possible ways such that the 
     * first InterfaceGraph of the decomposition equals a given
     * InterfaceGraph.
      */
    public Iterable<InterfaceGraph> getDecompositions(InterfaceGraph op)
    {
        Set<InterfaceGraph> result = new HashSet<InterfaceGraph>();
        
        if (op.inIf.size() != inIf.size())
            return result;
        
        DecompositionGenerator gen = new DecompositionGenerator(op, this, result);
        gen.run();
        
        return result;
    }
    
    /**
     * Returns a string representation of this InterfaceGraph with a 
     * user-defined indentation string.
     */
    @Pure
    public String toString(String indent)
    {
        return String.format(
                "%1$sgraph {\n" +
                "%1$s  nodes: %2$s\n" +
                "%1$s  edges:\n" + 
                "%1$s    %3$s\n" +
                "%1$s  inIf: %4$s\n" +
                "%1$s  outIf: %5$s\n" +
                "%1$s}",
            indent,
            StringUtils.listString(nodes, ", "),
            StringUtils.listString(edges, ",\n" + indent + "    "),
            StringUtils.listString(inIf, ", "),
            StringUtils.listString(outIf, ", "));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override@Pure
    public String toString()
    {
        return toString("");
    }
    

    /* ---------------------------------------------------------------------- *
     *                      DecompositionGenerator                            *
     * ---------------------------------------------------------------------- */

    private static class DecompositionGenerator implements Runnable
    {
        private InterfaceGraph op;
        private InterfaceGraph comp;
        private Set<InterfaceGraph> out;
        
        public DecompositionGenerator(InterfaceGraph op, InterfaceGraph comp, 
                Set<InterfaceGraph> out) 
        {
            this.op = op;
            this.comp = comp;
            this.out = out;
        }
        
        
        private void generatePushoutComplement(Morphism match) 
        {
            InterfaceGraph rest = new InterfaceGraph();
            Map<Node,Node> crMap = new HashMap<Node,Node>();
            
            // Nodes of op's outer interface must occur in rest
            // and be mapped according to the match
            for (Node opNode : op.outIf) {
                Node compNode = match.get(opNode);
                Node restNode = crMap.get(compNode);
                if (restNode == null) {
                    restNode = rest.addNode();
                    crMap.put(compNode, restNode);
                }
                rest.inIf.add(restNode);
            }
            
            // Check the identification condition
            for (Node opNode : op.nodes) {
                Node compNode = match.get(opNode);
                if (!op.outIf.contains(opNode) && crMap.containsKey(compNode))
                    // no pushout complement exists
                    return;
            }
            
            // Determine edges of comp which are not in the range of the match
            Set<Edge> compEdges = new HashSet<Edge>(comp.getEdges());
            compEdges.removeAll(match.getEdgeMap().values());
            
            // Check dangling condition
            for (Edge e : compEdges) {
                for (Node n : e.getNodes()) {
                    if (match.inRange(n) && !crMap.containsKey(n))
                        // no pushout complement exists
                        return;
                }
            }
            
            // Add nodes of the outer interface to rest
            for (Node compNode : comp.outIf) {
                Node restNode = crMap.get(compNode);
                if (restNode == null) {
                    if (match.inRange(compNode)) {
                        // pushout complement is not compatible with given
                        // outer interface of comp
                        return;
                    }
                    else {
                        restNode = comp.addNode();
                        crMap.put(compNode, restNode);
                    }
                }
                rest.outIf.add(restNode);
            }
            
            // Add edges which are not in the range of match to rest.
            for (Edge compEdge : compEdges) {
                List<Node> in = new ArrayList<Node>(compEdge.getNodes().size());
                for (Node n : compEdge.getNodes()) {
                    Node n2 = crMap.get(n);
                    if (n2 == null) {
                        n2 = rest.addNode();
                        crMap.put(n, n2);
                    }
                    in.add(n2);
                }
                rest.addEdge(compEdge.getLabel(), in);
            }
            
            out.add(rest);
        }
        
        
        public void run() 
        {
            assert (op.inIf.size() == comp.inIf.size());
            
            Morphism base = new Morphism(op, comp);
            for (int i = 0; i < op.inIf.size(); i++) {
                Node opNode = op.inIf.get(i);
                Node compNode = comp.inIf.get(i);
                base.put(opNode, compNode);
            }
            
            for (Morphism match : Morphism.getMatches(op, comp, base)) {
                generatePushoutComplement(match);
            }
        }


        // TODO check, compare, get rid of it?
/*            
        private void generatePushoutComplement(int[] nodeMap) 
        { 
            // partial map of nodes of comp to new nodes
            int numOfRestNodes = 0;
            int[] compToRest = new int[comp.numOfNodes];
            for (int i = 0; i < comp.numOfNodes; i++) 
                compToRest[i] = -1;
            
            // fill compToRest for the nodes in the outer interface of the 
            // operation
            boolean[] nodeRes = new boolean[op.numOfNodes];
            for (int j : op.outIf) {
                nodeRes[j] = true;
                int compNode = nodeMatch[j];
                if (compToRest[compNode] < 0) {
                    compToRest[compNode] = numOfRestNodes++;
                }
            }
            
            // check identification condition
            for (int i = 0; i < op.numOfNodes; i++) {
                if (!nodeRes[i] && compToRest[nodeMatch[i]] >= 0) {
                    // identification condition not satisfied; no pushout
                    // complement exists
                    return;
                }
            }
            
            // determine which nodes of comp are in the range of the match
            boolean[] inMatch = new boolean[comp.numOfNodes];
            for (int n : nodeMatch) {
                inMatch[n] = true;
            } 
            
            // determine edges of comp which are not in the range of the match
            Multiset<Edge> restEdges = HashMultiset.create(comp.edges);
            for (int i : edgeMatch) {
                restEdges.remove(compEdges[i]);
            }
                        
            // check dangling condition
            for (Edge e : restEdges) {
                for (int n : e.getNodes()) {
                    if (inMatch[n] && compToRest[n] < 0) {
                        // dangling condition not satisfied, pushout 
                        // complement does not exist
                        return; 
                    }
                }
            } 
            
            // create pushout complement graph
            int[] inIf = new int[op.outIf.length];
            for (int i = 0; i < inIf.length; i++) {
                inIf[i] = compToRest[nodeMatch[op.outIf[i]]];
            }
            
            int[] outIf = new int[comp.outIf.length];
            for (int i = 0; i < outIf.length; i++) {
                int n = comp.outIf[i];
                int a = compToRest[n];
                if (a < 0) {
                    if (inMatch[n]) {
                        // pushout complement is not compatible with
                        // (given) outer interface of comp
                        return;
                    }
                    else {
                        int m = numOfRestNodes++;
                        compToRest[n] = m;
                        outIf[i] = m;
                    }
                }
                else outIf[i] = a;
            } 
            
            Multiset<Edge> edges = HashMultiset.create();
            for (Edge e : restEdges) {
                IntList an = e.getNodes();
                int[] an2 = new int[an.size()];
                for (int i = 0; i < an.size(); i++) {
                    int n = an.getInt(i);
                    if (compToRest[n] < 0) 
                        compToRest[n] = numOfRestNodes++;
                    an2[i] = compToRest[n];
                }
                edges.add(new Edge(e.getLabel(), an2));
            }
            
            out.add(new InterfaceGraph(numOfRestNodes, edges, inIf, outIf));
        }  */        
    }
   
}
