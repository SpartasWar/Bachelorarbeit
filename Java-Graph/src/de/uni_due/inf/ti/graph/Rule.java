/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, 
 * Copyright (c) 2013-2014, University of Duisburg-Essen
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import com.google.common.collect.Sets;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.Pair;
import de.uni_due.inf.ti.util.StringUtils;

/**
 * This class represents a graph transformation rule.
 * A rule consists of a left-hand side, a right-hand side and a correspondence
 * morphism, which is a partial morphism which connects a subset of the nodes
 * and edges of the left-hand side to the nodes and edges of the right-hand 
 * side.
 * <p>
 * Formally, the Rule class represents graph transformation rules in the
 * single-pushout approach. The class also provides utility methods to
 * simulate double-pushout graph transformation, in particular
 * {@link #isConflictFree(Morphism) isConflictFree} and
 * {@link #satisfiesDanglingEdgeCondition(Morphism) satisfiesDanglingEdgeCondition}.
 * 
 * @author H.J. Sander Bruggink
 */
public class Rule extends NamedObject implements Cloneable
{
    private static final String EXC_NON_INJECTIVE = "Rule morphism must be injective.";
    private static final String EXC_MATCH_TYPE = "Match must be morphism with left-hand side as domain.";
    private static final String EXC_PARAMS_NULL = "Transformation parameters cannot be null.";
    
    protected TransformationParams params;

    private Morphism correspondence;
    
    private @Nullable Set<Node> deletedNodes;
    private @Nullable Set<Edge> deletedEdges;
    
    private @Nullable Set<Node> addedNodes;
    private @Nullable Set<Edge> addedEdges;
    
    /**
     * Private morphism class which clears this Rule's auxiliary data structures
     * when modified.
     */
    private class RuleMorphism extends Morphism
    {
        @SideEffectFree
        RuleMorphism(Morphism orig) 
        {
            super(orig);
        }
        
        protected void onModification()
        {
            deletedNodes = null;
            deletedEdges = null;
            addedNodes = null;
            addedEdges = null;
        }
    }
    
    /**
     * Class which holds the return information of the 
     * {@link #getDoublePushoutMorphisms()} method.
     */
    public static class DoublePushoutMorphisms
    {
        private Morphism left;
        private Morphism right;
        
        /**
         * Creates a new DoublePushoutMorphisms instance.
         */
        DoublePushoutMorphisms(Morphism left, Morphism right)
        {
            assert left.getDomain() == right.getDomain();
            this.left = left;
            this.right = right;
        }
        
        /**
         * Returns the morphism from the interface to the left-hand side.
         */
        public Morphism getLeftMorphism()
        {
            return left;
        }

        /**
         * Returns the morphism from the interface to the right-hand side.
         */
        public Morphism getRightMorphism()
        {
            return right;
        }
        
        /** 
         * Returns the interface graph.
         */
        public Graph getInterface()
        {
            return left.getDomain();
        }
        
        /** 
         * Returns the left-hand side.
         */
        public Graph getLeft()
        {
            return left.getCodomain();
        }
        
        /** 
         * Returns the right-hand side.
         */
        public Graph getRight()
        {
            return right.getCodomain();
        }
    }

    
    /**
     * Constructs a new rule with given transformation parameters.
     * <p>
     * Currently, only injective correspondence morphisms are supported.
     * 
     * @param corr          
     *      correspondence morphism of the new rule
     * @throw NullPointerException
     *      when {@code corr} or {@code params} is {@code null}
     * @throw IllegalArgumentException
     *      when {@code corr} is not an injective morphism
     */
    @SideEffectFree
    public Rule(Morphism corr, TransformationParams params)
    {
        if (!corr.isInjective()) 
            throw new IllegalArgumentException(EXC_NON_INJECTIVE);
        if (params == null) 
            throw new NullPointerException(EXC_PARAMS_NULL);
        
        this.params = params;
        this.correspondence = new RuleMorphism(corr);
    }
    
    /**
     * Constructs a new rule.
     * <p>
     * Currently, only injective correspondence morphisms are supported.
     * 
     * @param corr          
     *      correspondence morphism of the new rule
     * @throw IllegalArgumentException
     *      when {@code corr} is not an injective morphism
     */
    @SideEffectFree
    public Rule(Morphism corr)
    {
        this(corr, TransformationParams.create());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public Rule clone()
    {
        Morphism leftIso = Morphism.createIsomorphism(correspondence.getDomain());
        Morphism rightIso = Morphism.createIsomorphism(correspondence.getCodomain());
        Morphism newCorr = Morphism.createComposition(
                leftIso.getInverse(), 
                correspondence, 
                rightIso);
        Rule r = new Rule(newCorr, params);
        r.setName(getName());
        return r;
    }
    
    /**
     * Returns the inverse of this rule. The inverse of a rule <i>r</i> 
     * is the rule which has <i>r</i> right-hand side as left-hand side, 
     * <i>r</i>'s left-hand side as right-hand side and the inverse of 
     * <i>r</i>'s correspondence morphism as correspondence morphism
     * (see {@link Morphism#getInverse()} for a note about what happens if
     * the correspondence morphism of this rule is not injective).
     * <p>
     * Note that it is not necessarily the case that the inverse rule
     * induces the inverse transformation relation. In fact, this is only
     * the case when the correspondence morphism is total, injective and
     * surjective on nodes.
     * <p>
     * Subclasses can override this method to make sure that an instance of
     * the correct type is returned and to incorporate additional information
     * in the inverse rule. 
     * 
     * @return
     *      a new Rule which is the inverse of this Rule
     */
    @SideEffectFree
    public Rule getInverse()
    {
        Morphism invCorr = correspondence.getInverse();
        Rule r = new Rule(invCorr, params);
        r.setName(String.format("%s (inverse)", getName()));
        return r;
    }
    
    /**
     * Returns the left-hand side of this rule.
     * 
     * @return
     *      the left-hand side of this rule
     */
    @Pure
    public final Graph getLeft()
    {
        return correspondence.getDomain();
    }

    /**
     * Returns the right-hand side of this rule.
     * 
     * @return
     *      the right-hand side of this rule
     */
    @Pure
    public final Graph getRight()
    {
        return correspondence.getCodomain();
    }
    
    /**
     * Returns the correspondence morphism of this rule.
     * 
     * @return
     *      the correspondence morphism of this rule
     */
    @Pure
    public final Morphism getCorrespondence()
    {
        return correspondence;
    }
    
    /**
     * Returns an Enumerator which enumerates matches of this rule to a given 
     * graph.
     * 
     * @return 
     *      an Enumerator which enumerates matches of this rule to a given graph
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @SideEffectFree
    public Enumerator<Morphism> getMatches(Graph graph)
    {
        return Morphism.getMatches(getLeft(), graph, 
                getTransformationParams().getInjectiveMatches());
    }
    
    /**
     * Applies this rule to a graph with respect to a match.
     * The match must be a total morphism with the left side of this rule
     * as domain. The codomain of the match is the graph to be rewritten, the
     * <em>source graph</em>.
     * <p>
     * If it is not possible to apply this rule to the source graph over the 
     * given match (for example because dangling edge conditions are not 
     * satisfied) this method returns {@code null}.
     * 
     * @param match
     *      the match with respect to which a rewriting step is performed
     * @return              
     *      the transition which is obtained by applying this rule to the given
     *      match, or {@code null} if a transition is not possible
     * @throws NullPointerException
     *      if {@code match} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code match} is not a total morphism, or if 
     *      {@code match.getDomain() != this.getLeft()}
     */
    @SideEffectFree
    public final @Nullable Transition applyToMatch(Morphism match)
    {
        if (!match.isTotal()) 
            throw new IllegalArgumentException("Match must be total.");
        if (match.getDomain() != getLeft())
            throw new IllegalArgumentException("Match domain must be left-hand side.");
        
        // No transition is possible if additional conditions are not 
        // satisfied.
        if (!checkMatch(match)) {
            return null;
        }
        
        calculateNodeSets();
        Collection<Node> dn = deletedNodes;
        Collection<Edge> de = deletedEdges;
        Collection<Node> an = addedNodes;
        Collection<Edge> ae = addedEdges;
        
        Map<Node,Node> nodeMatch = match.getNodeMap();
        Map<Edge,Edge> edgeMatch = match.getEdgeMap();
        
        Graph source = match.getCodomain();
                
        // determine which nodes and edges of the source graph are to be 
        // deleted
        Set<Node> srcDelN = new HashSet<>();
        Set<Edge> srcDelE = new HashSet<>();
        for (Node n : dn) {
            srcDelN.add(castNonNull(nodeMatch.get(n)));
        }
        for (Edge e : de) {
            srcDelE.add(castNonNull(edgeMatch.get(e)));
        }
        for (Edge e : source.getEdges()) {
            for (Node n : e.getNodes()) {
                if (srcDelN.contains(n)) {
                    srcDelE.add(e);
                    continue;
                }
            }
        }

        // create target graph with non-deleted nodes and edges
        Graph target = new Graph();
        Map<Node,Node> nodeST = new HashMap<>();
        Map<Edge,Edge> edgeST = new HashMap<>();
        for (Node n : source.getNodes()) {
            if (!srcDelN.contains(n)) {
                Node newNode = target.addNode();
                nodeST.put(n, newNode);
            }
        }
        for (Edge e : source.getEdges()) {
            if (!srcDelE.contains(e)) {
                List<Node> eNodes = e.getNodes();
                List<Node> in = new ArrayList<>(eNodes.size());
                for (Node n : eNodes) {
                    Node newN = nodeST.get(n);
                    in.add(castNonNull(newN));
                }
                Edge newEdge = target.addEdge(e.getLabel(), in);
                edgeST.put(e, newEdge);
            }
        }
        
        // add added nodes and edges of the right-hand side to target graph
        Map<Node,Node> nodeRT = new HashMap<>();
        for (Node n : getLeft().getNodes()) {
            Node rNode = correspondence.getNodeMap().get(n);
            if (rNode != null) {
                Node sNode = nodeMatch.get(n);
                Node tNode = nodeST.get(sNode);
                nodeRT.put(rNode, castNonNull(tNode));
            }
        }
        for (Node n : an) {
            Node tNode = target.addNode();
            nodeRT.put(n, tNode);
        }
        edgeFor:
        for (Edge e : ae) {
            List<Node> eNodes = e.getNodes();
            List<Node> in = new ArrayList<>(eNodes.size());
            for (Node n : eNodes) {
                Node newN = nodeRT.get(n);
                if (newN == null)       // edge cannot be added because an 
                    continue edgeFor;   // incident node was deleted
                in.add(newN);
            }
            target.addEdge(e.getLabel(), in);
        }
        
        // return the transition
        Morphism morph = new Morphism(source, target, nodeST, edgeST);
        Transition trans = new Transition(morph);
        trans.setRule(this);
        trans.setMatch(match);
        return trans;
    }
    
    /**
     * Determines if a morphism satisfies the dangling edge condition
     * with respect to this rule. 
     * A morphism satisfies the dangling edge condition, if no deleted node of 
     * the left-hand side of this rule (a node which is not in the pre-image of 
     * the correspondence morphism) is mapped to a node which is incident to
     * an edge not in the image of the match.
     * 
     * @param match
     *      the match of which it is to be determined, if it satisfies the
     *      dangling edge condition
     * @return
     *      {@code true} if {@code match} satisfies the dangling edge 
     *      condition
     * @throws NullPointerException
     *      if {@code match} is {@code null}
     */
    @Pure
    public boolean satisfiesDanglingEdgeCondition(Morphism match)
    {
        if (match.getDomain() != getLeft()) 
            throw new IllegalArgumentException(EXC_MATCH_TYPE);
        
        calculateNodeSets();

        for (Node n : deletedNodes) {
            Node nim = match.getNodeMap().get(n);
            for (Edge e : match.getCodomain().getEdges()) {
                if (e.getNodes().contains(nim)) {
                    if (!match.getEdgeMap().values().contains(e)) 
                        return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Determines whether a match is conflict-free with respect to this rule.
     * A match is conflict-free, if for all nodes and edges in the codomain
     * of the match that have more than one pre-image in the left-hand side of
     * this rule, it holds that either all pre-images are in the pre-image of the
     * correspondence morphism or all are not.
     * <p>
     * Conflict-freeness corresponds to the identification condition in
     * double-pushout graph transformation. 
     * <p>
     * Although {@code match} is not required to be a total morphism, in
     * most practical situations {@code match} should be total.
     * 
     * @param match
     *      the match of which it is to be determined whether or not it is 
     *      conflict free
     * @return              
     *      {@code true} if {@code match} is conflict-free with respect to this
     *      rule, {@code false} otherwise
     * @throws NullPointerException 
     *      if {@code match} is {@code null}
     * @throws IllegalArgumentException 
     *      if the domain of {@code match} is not the left-hand side of this
     *      rule 
     */
    @Pure
    public boolean isConflictFree(Morphism match)
    {
        if (match.getDomain() != getLeft()) 
            throw new IllegalArgumentException(EXC_MATCH_TYPE);
        
        if (match.isInjective()) // usually pre-calculated, so fast
            return true;
        
        calculateNodeSets();
        // Quasi no-op for the nullness checker
        Collection<Node> deletedNodes = this.deletedNodes;
        
        {
            List<Node> leftNodes = new ArrayList<>(getLeft().getNodes());
            for (int i = 0; i < leftNodes.size(); i++) {
                Node n = leftNodes.get(i);
                Node nim = match.getNodeMap().get(n);
                if (nim == null) continue;
                for (int j = i + 1; j < leftNodes.size(); j++) {
                    Node m = leftNodes.get(j);
                    Node mim = match.getNodeMap().get(m);
                    if (nim == mim) {
                        if (deletedNodes.contains(n) && !deletedNodes.contains(m))
                            return false;
                        if (deletedNodes.contains(m) && !deletedNodes.contains(n))
                            return false;
                    }
                }
            }
        } // restrict scope of leftNodes

        {
            List<Edge> leftEdges = new ArrayList<>(getLeft().getEdges());
            for (int i = 0; i < leftEdges.size(); i++) {
                Edge n = leftEdges.get(i);
                Edge nim = match.getEdgeMap().get(n);
                if (nim == null) continue;
                for (int j = i + 1; j < leftEdges.size(); j++) {
                    Edge m = leftEdges.get(j);
                    Edge mim = match.getEdgeMap().get(m);
                    if (nim == mim) {
                        // TODO: Shouldn't these be deletedEdges? (Only relevant for noninjective matches anyway, need tests!)
                        if (deletedNodes.contains(n) && !deletedNodes.contains(m))
                            return false;
                        if (deletedNodes.contains(m) && !deletedNodes.contains(n))
                            return false;
                    }
                }
            }
        } // restrict scope of leftEdges

        return true;
    }
    
    /**
     * Returns a rule isomorphism between this rule and another given rule.
     * A rule isomorphism is a pair of morphisms between the left-hand sides
     * and the right-hand sides which commute with the rules' correspondence
     * morphisms.
     * <p>
     * The returned object is a pair of isomorphisms which have this rule's
     * left-hand and right-hand sides as domain, and the other rules left- and
     * right-hand sides as codomain. The returned object's {@code getFirst()}
     * morphism is an isomorphism between the left-hand sides, while its
     * {@code getSecond()} morphism is an isomorphism between the right-hand
     * sides.
     * <p>
     * If the two rules are not isomorphic, {@code null} is returned.
     * 
     * @param other
     *      the target rule of the returned isomorphisms
     * @return              
     *      commuting isomorphisms for the left-hand sides and right-hand sides,
     *      or {@code null} if these do not exist
     * @throws NullPointerException
     *      if {@code other} is {@code null}
     */
    @SideEffectFree
    public @Nullable Pair<Morphism,Morphism> getRuleIsomorphism(Rule other)
    { 
        for (Morphism leftIso : Morphism.getIsomorphisms(getLeft(), other.getLeft())) {
            for (Morphism rightIso : Morphism.getIsomorphisms(getRight(), other.getRight())) {
                Morphism comp1 = Morphism.createComposition(leftIso, other.getCorrespondence());
                Morphism comp2 = Morphism.createComposition(correspondence, rightIso);
                if (comp1.equals(comp2))
                    return Pair.create(leftIso, rightIso);
            }
        }       
        
        return null;
    }
    
    /**
     * Determines whether this rule is isomorphic to another rule.
     * Testing {@code r1.isIsomorphic(r2)} is equivalent to testing
     * {@code r1.getRuleIsomorphism(r2) != null}.
     *
     * @param other
     *      the rule to test whether it is isomorphic to this rule
     * @return              
     *      {@code true} if this rule is isomorphic to the rule {@code other}, 
     *      {@code false} otherwise
     * @throws NullPointerException
     *      if {@code other} is {@code null}
     * @see BaseRule#getRuleIsomorphism(BaseRule)
     */
    @Pure
    public boolean isIsomorphic(Rule other)
    {
        for (Morphism leftIso : Morphism.getIsomorphisms(getLeft(), other.getLeft())) {
            for (Morphism rightIso : Morphism.getIsomorphisms(other.getRight(), getRight())) {
                Morphism comp = Morphism.createComposition(leftIso, other.getCorrespondence(), rightIso);
                if (comp.equals(correspondence))
                    return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines if a given match satisfies additional application conditions
     * for this rule.
     * <p>
     * The default implementation checks this rule's transformation parameters,
     * and calls {@link #satisfiesDanglingEdgeCondition(Morphism) satisfiesDanglingEdgeCondition(m)} and
     * {@link #isConflictFree(Morphism) isConflictFree(m)} when required.
     * Subclasses may override this method if additional checks are required.
     * 
     * @param m             
     *      the match to check
     * @return
     *      {@code true} if {@code m} satisfies all application conditions for
     *      this rule
     * @throws NullPointerException
     *      if {@code m} is {@code null}
     */
    @Pure
    protected boolean checkMatch(Morphism m) 
    {
        if (params.getRequireDanglingEdgeCondition() && !satisfiesDanglingEdgeCondition(m))
            return false;
        else if (params.getRequireConflictFree() && !isConflictFree(m))
            return false;
        else
            return true;
    }
    
    /**
     * Returns the transformation parameters used by this rule.
     * 
     * @return             
     *      transformation parameters for this rule
     */
    @Pure
    public final TransformationParams getTransformationParams()
    {
        return params;
    }
    
    /**
     * Sets the transformation parameters for this rule.
     * TransformationParams objects can be shared among various different
     * objects, so that parameters can be modified for groups of related
     * objects at the same time.
     * 
     * @param params
     *      new transformation parameters of this rule
     * @throws NullPointerException
     *      if {@code params} is {@code null}
     */
    public final void setTransformationParams(TransformationParams params)
    {
        if (params == null) throw new NullPointerException();
        this.params = params;
    }
    
    /**
     * Returns the signature of this rule. The signature is the set of labels
     * which occur either in the left-hand side or in the right-hand side of
     * this rule.
     * <p>
     * The returned set is unmodifiable.
     * 
     * @return
     *      the signature of this rule
     */
    @SideEffectFree
    public Set<Label> getSignature()
    {
        return Sets.union(getLeft().getSignature(), getRight().getSignature());
    }
    
    /**
     * Counts the number of unique nodes in this rule.
     * The number of unique nodes is equal to the nodes in the left-hand side
     * plus the nodes in the right-hand side that have no corresponding node
     * in the left-hand side. 
     * <p>
     * Subclasses may override this method so that also other nodes are
     * counted (for example nodes in negative application conditions).
     * 
     * @return
     *      the number of unique nodes in this rule
     */
    @Pure
    public int countUniqueNodes()
    {
        calculateNodeSets();
        int numOfAddedNodes = addedNodes.size();
        int numOfLeftNodes = getLeft().getNodes().size();
        
        return numOfLeftNodes + numOfAddedNodes;
    }
    
    /**
     * Counts the number of unique edges in this rule.
     * The number of unique edges is equal to the edges in the left-hand side
     * plus the edges in the right-hand side that have no corresponding edge
     * in the left-hand side. 
     * <p>
     * Subclasses may override this method so that also other edges are
     * counted (for example edges in negative application conditions).
     * 
     * @return
     *      the number of unique nodes in this rule
     */
    @Pure
    public int countUniqueEdges()
    {
        calculateNodeSets();
        int numOfAddedEdges = addedEdges.size();
        int numOfLeftEdges = getLeft().getEdges().size();
        return numOfLeftEdges + numOfAddedEdges;
    }
    
    /**
     * Returns the pair of morphisms which build the rule when we interpret
     * this rule as a double pushout rule. 
     * <p>
     * The Graph Transformation Library does not directly support double
     * pushout graph transformation: rules are represented as single pushout 
     * rules. However, double pushout graph transformation can be simulated
     * by enabling additional application conditions. This method provides an
     * interface to client software that whishes to analyze graph transformation
     * rule in their double pushout form.
     * 
     * @return
     *      the two morphisms which build the rule in the double pushout
     *      approach 
     */
    public DoublePushoutMorphisms getDoublePushoutMorphisms()
    {
        Graph lhs = getLeft();
        Graph rhs = getRight();
        
        Graph inf = new Graph();
        Morphism left = new Morphism(inf, lhs);
        Morphism right = new Morphism(inf, rhs);
        
        Map<Node,Node> leftToInf = new HashMap<>();
        
        for (Map.Entry<Node,Node> entry : correspondence.getNodeMap().entrySet()) {
            Node leftNode = entry.getKey();
            Node rightNode = entry.getValue();
            Node infNode = inf.addNode();
            left.put(infNode, leftNode);
            right.put(infNode, rightNode);
            leftToInf.put(leftNode, infNode);
        }
        
        for (Map.Entry<Edge,Edge> entry : correspondence.getEdgeMap().entrySet()) {
            Edge leftEdge = entry.getKey();
            Edge rightEdge = entry.getValue();
            List<Node> ans = new ArrayList<>(leftEdge.getArity());
            for (Node n : leftEdge.getNodes()) {
                ans.add(leftToInf.get(n));
            }
            Edge infEdge = inf.addEdge(leftEdge.getLabel(), ans);
            left.put(infEdge, leftEdge);
            right.put(infEdge, rightEdge);
        }
        
        return new DoublePushoutMorphisms(left, right);
    }
    
    /**
     * Calculate the sets of deleted and added nodes and edges.
     */
    @EnsuresNonNull(value={"this.deletedNodes", 
                           "this.deletedEdges",
                           "this.addedNodes",
                           "this.addedEdges"})
    @SideEffectFree // method does not change *visible* state
    private void calculateNodeSets()
    {
        correspondence.checkCoordinateDeletion();
        
        if (deletedNodes == null) {
            deletedNodes = new HashSet<>(getLeft().getNodes());
            deletedNodes.removeAll(correspondence.getNodeMap().keySet());
        }

        if (deletedEdges == null) {
            deletedEdges = new HashSet<>(getLeft().getEdges());
            deletedEdges.removeAll(correspondence.getEdgeMap().keySet());
        }

        if (addedNodes == null) {
            addedNodes = new HashSet<>(getRight().getNodes());
            addedNodes.removeAll(correspondence.getNodeMap().values());
        }

        if (addedEdges == null) {
            addedEdges = new HashSet<>(getRight().getEdges());
            addedEdges.removeAll(correspondence.getEdgeMap().values());
        }
        
        // required for stupid Checking Framework's nullness checker
        assert deletedNodes != null : "@AssumeAssertion(nullness)";
        assert deletedEdges != null : "@AssumeAssertion(nullness)";
        assert addedNodes != null : "@AssumeAssertion(nullness)";
        assert addedEdges != null : "@AssumeAssertion(nullness)";
    }
    
    /**
     * Returns a string representation of this Rule with a user-defined 
     * indentation string and a subclass-defined information string.
     * <p>
     * The subclass defined information string must do its own formatting;
     * in particular, it should respect the indentation (the subclass can 
     * assume that the string is printed in the first column). The string
     * {@code sub} should end with a carriage return.
     * 
     * @param indent
     *      indentation string which is inserted before each line of the string
     *      representation
     * @param sub
     *      string created by the subclass to represents its data, or 
     *      {@code null} if there is no such string
     * @return
     *      a string representation of this Rule
     */
    @Pure
    protected String toString(String indent, @Nullable String sub)
    {
        String left = "left =\n" + getLeft().toString("  ");
        String corr = "correspondence =\n" + getCorrespondence().toString("  ");
        String right = "right =\n" + getRight().toString("  ");
        String my = StringUtils.columnize(indent + "  ", 2, left, corr, right);
        
        if (sub == null || sub.isEmpty()) {
            final String fmt = 
                    "%1$srule {\n" +
                    "%2$s" +
                    "%1$s}";
            return String.format(fmt, indent, my); 
        }
        else {
            final String fmt = 
                    "%1$srule {\n" +
                    "%2$s" +
                    "%3$s" +
                    "%1$s}";
            return String.format(fmt, indent, my, sub);
        }
    }
        
    /**
     * Returns a string representation of this Rule with a user-defined
     * indentation string.
     *  
     * @param indent
     *      string which is displayed to the left of each line of the string
     *      representation
     * @return
     *      string representation of this rule
     */
    @Pure
    public String toString(String indent)
    {
        return toString(indent, null);
    }

    /**
     * Returns a string representation of this Rule.
     * This method is equivalent to {@link #toString(String) toString("")}.
     * 
     * @return
     *      string representation of this rule
     */
    @Pure
    public String toString()
    {
        return toString("", null);
    }
}