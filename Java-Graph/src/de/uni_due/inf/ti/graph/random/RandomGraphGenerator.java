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

package de.uni_due.inf.ti.graph.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.random.DiscreteDistribution;
import de.uni_due.inf.ti.random.Distribution;
import de.uni_due.inf.ti.random.NormalDistribution;
import de.uni_due.inf.ti.random.Random;
import de.uni_due.inf.ti.random.SingletonDistribution;
import de.uni_due.inf.ti.random.UniformDistribution;

/**
 * Instances of this class generate random graphs.
 *
 * @author H.J. Sander Bruggink
 */
public class RandomGraphGenerator
{
    private static AtomicLong randomGraphNr = new AtomicLong(0);
    private static final String RANDOM_NAME_FMT = "random-graph-%d";
    
    public enum Connectedness
    { 
        ARBITRARY, NON_ISOLATED, CONNECTED
    }
    
    private java.util.Random rng;
    private Random<Integer> nodeCountRand;
    private Random<Double> densityRand;
    private Random<Label> labelRand; 
    
    private Distribution<Integer> nodeCountDistribution;
    private Distribution<Double> densityDistribution;
    private DiscreteDistribution<Label> labelDistribution;
    private Connectedness connectedness;
    
    
    /**
     * Creates a new RandomGraphGenerator.
     */
    public RandomGraphGenerator() 
    {
        this(new NormalDistribution.Integer(6.0, 2),
             new NormalDistribution.Double(.2, .05),
             new SingletonDistribution<Label>(new Label("A", 2)),
             Connectedness.CONNECTED);
    }
    
    /**
     * Returns a new RandomGraphGenerator with the same parameters as an
     * existing one.
     */
    public RandomGraphGenerator(RandomGraphGenerator orig)
    {
        this(orig.nodeCountDistribution, 
             orig.densityDistribution,
             orig.labelDistribution,
             orig.connectedness);
        rng = orig.rng;
    }
    
    /**
     * Creates a new RandomGraphGenerator with user-specified distributions for
     * the number of nodes and the density and a user-specified connectedness
     * setting.
     * 
     * @param nodeCountDistribution
     *      distribution for the number of nodes
     * @param densityDistribution
     *      distribution for the density
     * @param labelDistribution
     *      distribution of the labels occurring in the generated graphs
     * @param connected
     *      connectedness setting
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public RandomGraphGenerator(
            Distribution<Integer> nodeCountDistribution,
            Distribution<Double> densityDistribution,
            DiscreteDistribution<Label> labelDistribution,
            Connectedness connectedness)
    {
        if (nodeCountDistribution == null || 
            densityDistribution == null ||
            labelDistribution == null)
            throw new NullPointerException();
        
        this.nodeCountDistribution = nodeCountDistribution;
        this.densityDistribution = densityDistribution;
        this.labelDistribution = labelDistribution;
        this.connectedness = connectedness;
        
        rng = null;
        nodeCountRand = null;
        densityRand = null;
        labelRand = null;
    }
 
    /**
     * Creates a new RandomGraphGenerator with user-specified distributions for
     * the number of nodes and the density. The new RandomGraphGenerator will
     * always generate connected graphs.
     * 
     * @param nodeCountDistribution
     *      distribution for the number of nodes
     * @param densityDistribution
     *      distribution for the density
     * @param labelDistribution
     *      label distribution
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public RandomGraphGenerator(
            Distribution<Integer> nodeCountDistribution,
            Distribution<Double> densityDistribution,
            DiscreteDistribution<Label> labelDistribution)
    {
        this(nodeCountDistribution, 
             densityDistribution, 
             labelDistribution, 
             Connectedness.CONNECTED);
    }
    
    /**
     * Creates a new RandomGraphGenerator which generates graph with a
     * user-specified number of nodes and density.
     * 
     * @param numOfNodes
     *      number of nodes of the graphs generated by the new 
     *      RandomGraphGenerator
     * @param density
     *      (approximate) density of the generated graphs
     * @param signature
     *      the set of labels that can occur in the generated graphs
     * @param connectedness
     *      connectedness setting of the new RandomGraphGenerator
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public RandomGraphGenerator(
            int numOfNodes, 
            double density,
            Collection<Label> signature,
            Connectedness connectedness)
    {
        this(new SingletonDistribution<Integer>(numOfNodes),
             new SingletonDistribution<Double>(density),
             new UniformDistribution<Label>(signature),
             connectedness);
    }
    
    /**
     * Creates a new RandomGraphGenerator which generates graph with a
     * user-specified number of nodes and density. The new RandomGraphGenerator
     * will only generate connected graphs.
     * 
     * @param numOfNodes
     *      number of nodes of the graphs generated by the new 
     *      RandomGraphGenerator
     * @param density
     *      density of the graph generated by the new RandomGraphGenerator
     * @throws NullPointerException
     *      if {@code signature} is {@code null}
     */
    public RandomGraphGenerator(
            int numOfNodes, 
            double density,
            Collection<Label> signature) 
    {
        this(numOfNodes, density, signature, Connectedness.CONNECTED);
    }

    /**
     * Returns the distribution of the number of nodes of the graphs generated
     * by this RandomGraphGenerator.
     */
    public Distribution<Integer> getNodeCountDistribution()
    {
        return nodeCountDistribution;
    }

    /**
     * Sets the distribution of the number of nodes of the graph generated
     * by this RandomGraphGenerator.
     * 
     * @param nodeCountDistribution
     *      the new node count distribution of this RandomGraphGenerator
     * @throws NullPointerException
     *      if {@code nodeCountDistribution} is {@code null}
     */
    public void setNodeCountDistribution(Distribution<Integer> nodeCountDistribution)
    {
        if (nodeCountDistribution == null) throw new NullPointerException();
        this.nodeCountDistribution = nodeCountDistribution;
        nodeCountRand = null;
    }

    /**
     * Returns the distribution of the densities of the graphs generated
     * by this RandomGraphGenerator.
     */
    public Distribution<Double> getDensityDistribution()
    {
        return densityDistribution;
    }

    /**
     * Sets the distribution of the densities of the graphs generated
     * by this RandomGraphGenerator.
     * 
     * @param densityDistribution
     *      the new density distribution of this RandomGraphGenerator
     * @throws NullPointerException
     *      if {@code densityDistribution} is {@code null}
     */
    public void setDensityDistribution(Distribution<Double> densityDistribution)
    {
        if (densityDistribution == null) throw new NullPointerException();
        this.densityDistribution = densityDistribution;
        densityRand = null;
    }

    /**
     * Returns the connectedness setting of this RandomGraphGenerator.
     */
    public Connectedness getConnectedness()
    {
        return connectedness;
    }

    /**
     * Sets the connectedness setting of this RandomGraphGenerator.
     */
    public void setConnectedness(Connectedness c)
    {
        connectedness = c;
    }
    
    /**
     * Sets the signature of this RandomGraphGenerator. The signature is
     * the set of labels that can occur in the generated graphs. If the 
     * signature is set by using this method, the labels will be uniformly
     * distributed in the generated graphs. If this is not wanted, the
     * {@link #setLabelDistribution} method should be used.
     * 
     * @param signature
     *      sets the signature of this RandomGraphGenerator
     * @throws NullPointerException
     *      if {@code signature} is {@code null}
     */
    public void setSignature(Collection<Label> signature)
    {
        if (signature == null) throw new NullPointerException();
        labelDistribution = new UniformDistribution<Label>(signature);
        labelRand = null;
    }
    
    /**
     * Returns the signature of this RandomGraphGenerator.
     */
    public Set<Label> getSignature()
    {
        return labelDistribution.getElements();
    }
    
    /**
     * Sets the label distribution of this RandomGraphGenerator.
     * 
     * @param labels
     *      the new label distribution of this RandomGraphGenerator
     * @throws NullPointerException
     *      if {@code labels} is {@code null}
     */
    public void setLabelDistribution(DiscreteDistribution<Label> labels)
    {
        if (labels == null) throw new NullPointerException();
        labelDistribution = labels;
        labelRand = null;
    }
    
    /**
     * Returns the label distribution of this RandomGraphGenerator.
     */
    public DiscreteDistribution<Label> getLabelDistribution()
    {
        return labelDistribution;
    }
    
    /**
     * Returns the random number generating used by this RandomGraphGenerator.
     */
    public java.util.Random getRandom()
    {
        return rng;
    }
    
    /**
     * Sets the random number generator used by this RandomGraphGenerator.
     */
    public void setRandom(java.util.Random rng) 
    {
        this.rng = rng;
    }

    /**
     * Returns a new, random graph.
     */
    public Graph nextGraph()
    {
        return fillGraph(null);
    }
    
    /**
     * Randomly completes a graph to a number of nodes and density.
     * The number of nodes and density are randomly taken from the 
     * node number and density distribution of this RandomGraphGenerator.
     * 
     * @param graph
     *      graph which is extended, or {@code null} if a new graph should
     *      be created
     * @return
     *      the created or extended graph: if {@code graph} is non-null, then
     *      {@code graph} is returned, otherwise a newly created graph 
     */
    public Graph fillGraph(Graph graph)
    {
        if (rng == null) {
            rng = new java.util.Random();
        }
        if (nodeCountRand == null) {
            nodeCountRand = Random.create(nodeCountDistribution);
        }
        if (densityRand == null) {
            densityRand = Random.create(densityDistribution);
        }
        if (labelRand == null) {
            labelRand = Random.create(labelDistribution);
        }
        if (rng != null) {
            nodeCountRand.setRandom(rng);
            densityRand.setRandom(rng);
            labelRand.setRandom(rng);
        }
        
        boolean newGraph = false;
        if (graph == null) {
            newGraph = true;
            graph = new Graph();
        }
        
        // determine number of nodes of extended graph
        int numOfNodes = nodeCountRand.nextInt();
        if (numOfNodes < 1) {
            return graph; // return empty graph
        }
        else if (!newGraph) {
            int numOfGraphNodes = graph.getNodes().size();
            if (numOfNodes < numOfGraphNodes) numOfNodes = numOfGraphNodes;
        }
        
        // create and fill node array
        List<Node> nodes;
        if (newGraph) {
            nodes = new ArrayList<>(numOfNodes);
            for (int i = 0; i < numOfNodes; i++) {
                nodes.add(graph.addNode());
            }
        }
        else {
            Collection<Node> graphNodes = graph.getNodes();
            nodes = new ArrayList<>(graphNodes);
            for (int i = graphNodes.size(); i < numOfNodes; i++) {
                nodes.add(graph.addNode());
            }
        }
        
        // determine number of edges to add
        double density = densityRand.nextDouble();
        if (density < 0.0) density = 0.0;
        int numOfEdges = (int)(density * (double)(numOfNodes * numOfNodes));
        if (!newGraph) {
            numOfEdges -= graph.getEdges().size();
        }
        
        // solve connectedness condition
        switch (connectedness) {
            case NON_ISOLATED: {
                Set<Node> isoNodes = graph.getIsolatedNodes();
                for (Node node : nodes) {
                    if (isoNodes.contains(node)) {
                        Label label;
                        int arity;
                        do {
                            label = labelRand.nextValue();
                            arity = label.getArity();
                            if (arity < 0) arity = 2;
                        } while (arity == 0);
                        Node[] ens = new Node[arity];
                        int pos = rng.nextInt(arity);
                        ens[pos] = node;
                        isoNodes.remove(node);
                        for (int i = 0; i < arity; i++) {
                            if (ens[i] == null) {
                                int otherNr = rng.nextInt(numOfNodes);
                                ens[i] = nodes.get(otherNr);
                                isoNodes.remove(ens[i]);
                            }
                        }
                        graph.addEdge(label, ens);
                        numOfEdges--;
                    }
                }
                break;
            }
            case CONNECTED: {
                Set<Node> connected = new HashSet<>();
                connected.add(nodes.get(0));
                if (!newGraph) connected = graph.weaklyConnectedClosure(connected);
                
                Set<Node> unconnected = new HashSet<>(graph.getNodes());
                unconnected.removeAll(connected);
                
                while (!unconnected.isEmpty()) {
                    Node unconnectedNode = getSomeElement(unconnected);
                    Node connectedNode = getRandomElement(rng, connected);
                    
                    Label label;
                    int arity;
                    do {
                        label = labelRand.nextValue();
                        arity = label.getArity();
                    }
                    while (arity >= 0 && arity < 2);
                    if (arity < 0) arity = 2;
                   
                    int unconnectedIdx = rng.nextInt(arity);
                    if (arity == 2) {
                        if (unconnectedIdx == 0) {
                            graph.addEdge(label, unconnectedNode, connectedNode);
                            numOfEdges--;
                        }
                        else {
                            graph.addEdge(label, connectedNode, unconnectedNode);
                            numOfEdges--;
                        }
                    }
                    else {
                        int connectedIdx = rng.nextInt(arity - 1);
                        if (connectedIdx >= unconnectedIdx) connectedIdx++;
                        Node[] ens = new Node[arity];
                        ens[unconnectedIdx] = unconnectedNode;
                        ens[connectedIdx] = connectedNode;
                        for (int i = 0; i < arity; i++) {
                            if (ens[i] == null) ens[i] = nodes.get(rng.nextInt(numOfNodes));
                            connected.add(ens[i]);
                            unconnected.remove(ens[i]);
                        }
                        graph.addEdge(label, ens);
                        numOfEdges--;
                    }
                    
                    unconnected.remove(unconnectedNode);
                    connected.add(unconnectedNode);
                }
            }
            default:
                // do nothing
        }
        
        for (; numOfEdges > 0; numOfEdges--) {
            Label label = labelRand.nextValue();
            int arity = label.getArity();
            if (arity < 0) arity = 2;
            Node[] ens = new Node[arity];
            for (int i = 0; i < arity; i++) {
                ens[i] = nodes.get(rng.nextInt(numOfNodes));
            }
            graph.addEdge(label, ens);
        }
        
        String name = String.format(RANDOM_NAME_FMT, randomGraphNr.getAndDecrement());
        graph.setName(name);
        
        return graph;
    }

    
    // ---------------------------------------------------------------------- //
    //                        Private utility methods                         //
    // ---------------------------------------------------------------------- //
    
    /**
     * Returns an arbitrary (but not necessarily random) element from a 
     * collection. In most cases, this method is considerably faster than the
     * {@link getRandomElement(Collection) randomElement} method.
     * 
     * @param col
     *      collection to return some element of
     * @returns
     *      an arbitrary element of {@code col}, or {@code null} if 
     *      {@code col} is empty
     */
    static <T> T getSomeElement(Collection<T> col) 
    {
        if (col.isEmpty()) return null;
        
        if (col instanceof List) {
            List<T> list = (List<T>)col;
            return list.get(0);            
        }
        else {
            Iterator<T> it = col.iterator();
            assert it.hasNext();
            return it.next();
        }
    }
    
    
    /**
     * Returns a random element from a collection.
     * 
     * @param rng
     *      random number generator to use
     * @param col
     *      collection of which a random element is to be returned
     * @returns
     *      a random element of {@code col}, or {@code null} if 
     *      {@code col} is empty
     */
    static <T> T getRandomElement(java.util.Random rng, Collection<T> col) 
    {
        if (col.isEmpty()) return null;
        
        int index = rng.nextInt(col.size());
        
        if (col instanceof List) {
            List<T> list = (List<T>)col;
            return list.get(index);
        }
        else {
            Iterator<T> it = col.iterator();
            for (int i = 0; i < index; i++) {
                assert it.hasNext();
                it.next();
            }
        
            assert it.hasNext();
            return it.next();
        }
    }
}
