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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.random.RandomGraphGenerator.Connectedness;
import de.uni_due.inf.ti.random.Distribution;
import de.uni_due.inf.ti.random.NormalDistribution;
import de.uni_due.inf.ti.random.Random;
import de.uni_due.inf.ti.random.SingletonDistribution;

/**
 * Instances of this class generate random rules.
 * 
 * @author H.J. Sander Bruggink
 */
public class RandomRuleGenerator
{
    private static final AtomicLong randomRuleNr = new AtomicLong(0);
    private static final String RANDOM_NAME_FMT = "random-rule-%d";
    
    private java.util.Random rng;
    
    private RandomGraphGenerator leftGen;
    private RandomGraphGenerator rightGen;
    
    private Distribution<Integer> ifSizeDistribution;
    private Random<Integer> ifSizeRand;
    
    private boolean discrete;
    
    /**
     * Creates a new RandomRuleGenerator.
     */
    public RandomRuleGenerator()
    {
        rng = null;
        leftGen = new RandomGraphGenerator(
                        new NormalDistribution.Integer(3.0, 1.5),
                        new NormalDistribution.Double(.2, .05),
                        new SingletonDistribution<Label>(new Label("A", 2)),
                        Connectedness.CONNECTED
                  );
        leftGen.setRandom(rng);
        rightGen = new RandomGraphGenerator(
                         new NormalDistribution.Integer(3.0, 1.5),
                         new NormalDistribution.Double(.2, .05),
                         new SingletonDistribution<Label>(new Label("A", 2)),
                         Connectedness.CONNECTED
                   );
        rightGen.setRandom(rng);
        ifSizeDistribution = new NormalDistribution.Integer(3, 2);
        ifSizeRand = null;
        discrete = true;
    }
    
    /**
     * Creates a RandomRuleGenerator with the same parameters as an existing
     * one.
     */
    public RandomRuleGenerator(RandomRuleGenerator orig)
    {
        rng = null;
        leftGen = new RandomGraphGenerator(orig.leftGen);
        rightGen = new RandomGraphGenerator(orig.rightGen);
        ifSizeDistribution = orig.ifSizeDistribution;
        ifSizeRand = null;
        discrete = orig.discrete;
    }
    
    /**
     * Returns the Java random number generator used by this 
     * RandomRuleGenerator.
     */
    public java.util.Random getRandom()
    {
        return rng;
    }
    
    /**
     * Sets the Java random number generator used by this 
     * RandomRuleGenerator.
     */
    public void setRandom(java.util.Random rng)
    {
        this.rng = rng;
        leftGen.setRandom(rng);
        rightGen.setRandom(rng);
    }

    /**
     * Returns the RandomGraphGenerator used to generate the left-hand sides
     * of the random rules.
     */
    public RandomGraphGenerator getLeftGenerator()
    {
        return leftGen;
    }

    /**
     * Returns the RandomGraphGenerator used to generate the right-hand sides
     * of the random rules.
     */    
    public RandomGraphGenerator getRightGenerator()
    {
        return rightGen;
    }
    
    /**
     * Returns the distribution for the size of the interfaces.
     */
    public Distribution<Integer> getInterfaceSizeDistribution()
    {
        return ifSizeDistribution;
    }

    /**
     * Sets the distribution for the size of the interfaces.
     * 
     * @param d
     *      the new distribution for the interface sizes
     * @throws NullPointerException
     *      if {@code d} is {@code null}
     */
    public void setInterfaceSizeDistribution(Distribution<Integer> d)
    {
        if (d == null) {
            throw new NullPointerException();
        }
        ifSizeDistribution = d;
    }

    /**
     * Sets the signature of the rules generated by this
     * RandomRuleGenerator. This method sets the signature of the 
     * RandomGraphGenerators for the left-hand sides and the right-hand sides
     * to the given signature.
     * 
     * @param signature
     *      new signature of this RandomRuleGenerator
     * @throws NullPointerException
     *      if {@code signature} is {@code null}
     */
    public void setSignature(Collection<Label> signature) 
    {
        leftGen.setSignature(signature);
        rightGen.setSignature(signature);
    }
    
    /**
     * Returns the signature of rules generated by this RandomRuleGenerator.
     * The returned object is generated on-the-fly by taking the union of the
     * signatures of the RandomGraphGenerators for the left-hand sides and
     * the right-hand sides.
     * 
     * @return
     *      signature of rules generated by this RandomRuleGenerator
     */
    public Set<Label> getSignature()
    {
        Set<Label> sig = new HashSet<>(leftGen.getSignature());
        sig.addAll(rightGen.getSignature());
        return sig;
    }
        
    /**
     * Returns whether this RandomRuleGenerator generates only rules with
     * discrete interfaces.
     */
    public boolean getDiscrete()
    {
        return discrete;
    }

    /**
     * Sets whether this RandomRuleGenerator generates only rules with
     * discrete interfaces.
     */
    public void setDiscrete(boolean b)
    {
        discrete = b;
    }

    /**
     * Generates a random rule.
     * 
     * @return
     *      a newly created, random Rule instance
     */
    public Rule nextRule()
    {
        if (ifSizeRand == null) {
            ifSizeRand = Random.create(ifSizeDistribution);
        }
        if (rng == null) {
            rng = new java.util.Random();
        }
        if (leftGen.getRandom() == null) {
            leftGen.setRandom(rng);
        }
        if (rightGen.getRandom() == null) {
            rightGen.setRandom(rng);
        }
        if (ifSizeRand.getRandom() != null) {
            ifSizeRand.setRandom(rng);
        }
        
        Graph left = leftGen.nextGraph();
        List<Node> leftNodes = left.getNodes();
        
        int ifsize = ifSizeRand.nextInt();
        if (ifsize > leftNodes.size()) ifsize = leftNodes.size();
        Set<Node> ifNodes = new HashSet<>();
        {
            List<Node> temp = new ArrayList<>(leftNodes);
            for (int i = 0; i < ifsize; i++) {
                int idx = rng.nextInt(temp.size());
                ifNodes.add(temp.get(idx));
                temp.remove(idx);
            }
        }
        
        Graph right = new Graph();
        Map<Node,Node> nodeMap = new HashMap<>();
        for (Node ifNode : ifNodes) {
            Node tgtNode = right.addNode();
            nodeMap.put(ifNode, tgtNode);
        }
        
        Morphism corr;        
        if (discrete) {
            rightGen.fillGraph(right);
            corr = Morphism.create(left, right, nodeMap);
        }
        else {
            Map<Edge,Edge> edgeMap = new HashMap<>();
            for (Edge ifEdge : left.getEdges()) {
                if (ifNodes.containsAll(ifEdge.getNodes())) {
                    List<Node> ifEns = ifEdge.getNodes();
                    Node[] ens = new Node[ifEns.size()];
                    for (int i = 0; i < ens.length; i++) {
                        ens[i] = nodeMap.get(ifEns.get(i));
                    }
                    Edge tgtEdge = right.addEdge(ifEdge.getLabel(), ens);
                    edgeMap.put(ifEdge, tgtEdge);
                }
            }
            rightGen.fillGraph(right);
            corr = Morphism.create(left, right, nodeMap, edgeMap);
        }
        
        Rule rule = new Rule(corr);
        rule.setName(String.format(RANDOM_NAME_FMT, randomRuleNr.incrementAndGet()));
        return rule;
    }
}
