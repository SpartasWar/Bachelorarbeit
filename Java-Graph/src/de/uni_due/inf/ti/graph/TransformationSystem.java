/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.Pair;
import de.uni_due.inf.ti.util.StringUtils;

/**
 * This class represents a graph transformation system.
 * 
 * @author H.J. Sander Bruggink
 */
public class TransformationSystem 
    extends NamedObject
    implements TransitionSystem
{
    private @Nullable Graph initial;
    private Set<Rule> rules;
    private TransformationParams params;

    /**
     * Create a new TransformationSystem.
     * This constructor can only be called from within this class. Other
     * classes should use the factory methods (create*).
     * <p>
     * The caller must make sure that no references to the rule are
     * dereferenced after calling this constructor.
     * 
     * @param initial
     *      initial graph of the new TransformationSystem
     * @param rules
     *      rules of the new TransformationSystem
     */
    @SideEffectFree
    private TransformationSystem(@Nullable Graph initial, Set<Rule> rules)
    {
        this.initial = initial;
        this.rules = rules;
        this.params = TransformationParams.getDefaults().newChild();
    }
    
    /**
     * Returns the transformation parameters for this transformation system.
     */
    @Pure
    public TransformationParams getTransformationParams()
    {
        return params;
    }
    
    /**
     * Returns the initial graph of this transformation system.
     */
    @Override
    @Pure
    public @Nullable Graph getInitialGraph()
    {
        return initial;
    }
    
    /**
     * Sets the initial graph of this graph transformation system.
     * 
     * @param initial
     *      new initial graph of this TransformationSystem, or {@code null} to
     *      remove the initial graph from the system
     */
    public void setInitialGraph(@Nullable Graph initial)
    {
        this.initial = initial;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public Collection<Transition> getTransitions(Graph source)
    {
        return getTransitions(source, -1);
    }

    /**
     * Returns the transitions of this transformation system from a certain
     * source graph, with a limit on how many transitions will be returned.
     * 
     * @param source 
     *      source of the returned transitions
     * @param limit
     *      maximum number of returned transitions; if smaller than or equal
     *      to 0 there is no maximum number of transitions
     * @return 
     *      Collection of transitions from source graph {@code source};
     *      if {@code limit > 0}, the returned collection has a size of at
     *      most {@code limit}.
     * @throws NullPointerException
     *      if {@code source} is {@code null}
     */
    @SideEffectFree
    public Collection<Transition> getTransitions(Graph source, int limit)
    {
        Collection<Transition> result = new LinkedList<Transition>();
        
        for (Rule rule : rules) {
            assert rule != null;
            
            boolean injective = rule.getTransformationParams().getInjectiveMatches();
            Iterable<Morphism> matches = Morphism.getMatches(rule.getLeft(), source, injective); 
            for (Morphism match : matches) {
                Transition trans = rule.applyToMatch(match);
                if (trans != null) {
                    result.add(trans);
                    if (limit > 0 && result.size() >= limit) return result;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Creates a new rule based on the given correspondence morphism and adds
     * it to this transformation system. 
     * 
     * @param corr          
     *      the correspondence morphism of the new rule; the left-hand side of 
     *      the new rule will be {@code corr.getDomain()}, the right-hand side 
     *      will be {@code corr.getCodomain()}
     * @return              
     *      the newly created rule
     * @throws NullPointerException 
     *      if {@code corr} is {@code null}
     */
    public Rule addRule(Morphism corr) 
    {
        Rule newRule = new Rule(corr);
        rules.add(newRule);
        return newRule;
    }
    
    /**
     * Adds an existing rule to this transformation system.
     * 
     * @param rule          
     *      the rule which is to be added to this transformation system
     * @throws NullPointerException
     *      if {@code rule} is {@code null}
     */
    public void addRule(Rule rule) 
    {
        if (rule == null) throw new NullPointerException("\"rule\" cannot be null");
        rules.add(rule);
    }
    
    /**
     * Returns the set of rules of this transformation system.
     * The returned set of unmodifiable.
     */
    @SideEffectFree
    public Set<Rule> getRules() 
    {
        return Collections.unmodifiableSet(rules);
    }
    
    /**
     * Returns the signature of this TransformationSystem. The signature 
     * is the set of all labels which occur either in the initial graph or
     * in one of the rules.
     * 
     * @return
     *      the signature of this TransformationSystem
     */
    @SideEffectFree
    public Set<Label> getSignature()
    {
        Set<Label> sig = initial == null ? new HashSet<Label>()
                                         : initial.getSignature();
        for (Rule rule : rules) {
            sig.addAll(rule.getSignature());
        }
        
        return sig;
    }
    
    /**
     * Creates an ExplicitTransitionSystem which is equal to this 
     * TransformationSystem up to a finite depth.
     * <p>
     * The {@code maxDepth} parameter controls how much transitions from the
     * initial graph are generated. If it is positive, only graphs and 
     * transitions which are reachable within {@code maxDepth} steps from
     * the initial graph are generated. If it is negative, no bound is imposed
     * and the complete transition system is generated. 
     * <p>
     * <strong>Warning!</strong> Do not set {@code maxDepth} to a negative
     * value when there are infinitely many graphs reachable from the initial
     * graph; in this case the behavior of this method is undefined (most
     * probably, it will take a while to eat up all memory and terminate with
     * an exception).
     * 
     * @param maxDepth
     *      the number of transitions from the initial graph which are generated
     * @return
     *      an ExplicitTransitionSystem which contains all transitions of this
     *      TransformationSystem up to {@code maxDepth} transitions from the
     *      initial graph
     */
    @SideEffectFree
    public ExplicitTransitionSystem createExplicitTransitionSystem(int maxDepth)
    {
        Queue<Pair<Graph,Integer>> queue = new LinkedList<Pair<Graph,Integer>>();
        Map<Integer,List<Graph>> graphs = new HashMap<Integer,List<Graph>>();
        List<Transition> transitions = new ArrayList<Transition>();
        
        // Say to Checker Framework: initial cannot become null during the
        // execution of this method.
        Graph initial = this.initial;
        
        queue.add(Pair.create(initial, 0));
        List<Graph> newList = new ArrayList<Graph>();
        newList.add(initial);
        graphs.put(initial.getNodes().size(), newList);
        
        while (!queue.isEmpty()) {
            Pair<Graph,Integer> pair = castNonNull(queue.poll());
            Graph current = pair.getFirst();
            int curDepth = pair.getSecond();
            
            for (Rule r : rules) {
                TransformationParams ruleParams = r.getTransformationParams();
                Iterable<Morphism> matches = 
                        Morphism.getMatches(r.getLeft(), current, 
                                ruleParams.getInjectiveMatches());
                for (Morphism match : matches) {
                    Transition trans = r.applyToMatch(match);
                    
                    if (trans != null) {
                        Graph target = trans.getTarget();
                    
                        // check if current target is isomorphic to one in the 
                        // transition system
                        Morphism iso = null;
                        List<Graph> potGraphs = graphs.get(current.getNodes().size());
                        if (potGraphs == null) {
                            potGraphs = new ArrayList<Graph>();
                            graphs.put(current.getNodes().size(), potGraphs);
                        }
                        else {
                            for (Graph oldGraph : potGraphs) {
                                iso = Morphism.getIsomorphism(target, oldGraph);
                                // if yes, add transition to isomorphic graph
                                if (iso != null) {
                                    Morphism m = Morphism.createComposition(trans.getMorphism(), iso);
                                    transitions.add(new Transition(m));
                                    break; // there can only be one isomorphic graph
                                }
                            }
                        }
                    
                        // if not, add it and the transition
                        if (iso == null) {
                            transitions.add(trans);
                            potGraphs.add(target);
                            if (maxDepth < 0 || curDepth < maxDepth)
                                queue.add(Pair.create(target, curDepth + 1));
                        }
                    }
                }
            }
        }
        
        return ExplicitTransitionSystem.create(initial, transitions);
    }
    
    /**
     * Determines whether this transformation system is isomorphic to another
     * transformation system. Two transformation systems are isomorphic if they
     * have the same number of rules and for each rule of the one there exists
     * an isomorphic rule in the other.
     * 
     * @param other
     *      transformation system to test isomorphism with
     * @return
     *      {@code true} if this transformation system is isomorphic to the
     *      given transformation system, {@code false} otherwise
     * @throws NullPointerException
     *      if {@code other} is {@code null}
     */
    @Pure
    public boolean isIsomorphic(TransformationSystem other)
    {
        Collection<Rule> otherRules = other.getRules();
        if (otherRules.size() != rules.size()) return false;
        
        List<Rule> orules = new ArrayList<>(otherRules);
        
        for (Rule myRule : rules) {
            boolean fnd = false;
            for (Iterator<Rule> it = orules.iterator(); it.hasNext();) {
                Rule hisRule = it.next();
                if (myRule.isIsomorphic(hisRule)) {
                    it.remove();
                    fnd = true;
                    break;
                }
            }
            if (!fnd) return false;
        }
        
        return orules.isEmpty();
    }

    /**
     * Returns a user-readable string representation of this transformation
     * system with a given indentation string. The indentation string is
     * inserted at the start of each line of the string representation.
     * 
     * @param indent        
     *      indentation string of the string representation
     * @return
     *      string representation of this transformation system
     */
    @Pure
    public String toString(String indent) 
    {
        if (rules.isEmpty()) return "{ }";
        final String ind = (indent == null) ? "" : indent;
        
        Stream<String> stream =
                rules.stream()
                     .map((Rule r) -> r.toString(ind + "    "));
        
        String open  = String.format("%s{%n%s    ", ind, ind);
        String sep   = String.format("%n%s,%n%s    ", ind, ind);
        String close = String.format("%n%s}", ind);
        
        return StringUtils.listString(stream, open, sep, close);
    }

    /**
     * Returns a short string representation of this TransformationSystem.
     */
    @Pure
    public String getShortString()
    {
        Stream<String> stream =
                rules.stream()
                     .map((Rule r) -> r.getName());
        return StringUtils.listString(stream, "{", ", ", "}");
    }
    
    /**
     * Returns a user-readable string representation of this transformation
     * system.
     */
    @Pure
    public String toString() 
    {
        return toString("");
    }
    
    /**
     * Creates a new transformation system.
     * 
     * @param initial       
     *      initial graph of the transformation system, or {@code null} if
     *      the system has no initial graph
     * @param rules
     *      rules of the transformation system
     * @return
     *      a newly created transformation system
     */
    @SideEffectFree
    public static TransformationSystem create(
            @Nullable Graph initial, 
            Rule... rules)
    {
        Set<Rule> rs = new HashSet<Rule>(rules.length);
        for (Rule rule : rules) {
            rs.add(rule);
        }
        return new TransformationSystem(initial, rs);
    }
    
    /**
     * Creates a new transformation system.
     * 
     * @param initial
     *      initial graph of the transformation system, or {@code null} if
     *      the system has no initial graph
     * @param rules
     *      rules of the transformation system
     * @return
     *      a newly created transformation system
     */
    @SideEffectFree
    public static TransformationSystem create(
            @Nullable Graph initial, 
            Collection<Rule> rules)
    {        
        Set<Rule> rs = new HashSet<Rule>(rules.size());
        rs.addAll(rules);
        return new TransformationSystem(initial, rs);
    }   
}
