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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.graph.util.IsomorphismHashMap;
import de.uni_due.inf.ti.graph.util.IsomorphismMap;

/**
 * This class represents a graph transition system in which all graphs and
 * transitions are explicitly stored. The transition system represented
 * in this way must be finite.
 */
public class ExplicitTransitionSystem
    extends NamedObject
    implements TransitionSystem
{   
    private @Nullable Graph initial;
    private Map<Graph,Set<Transition>> transitions;
    
    /** 
     * Creates an empty ExplicitTransitionSystem without an initial graph.
     * <p>
     * ExplicitTransitionSystems can also be constructed with 
     * the <tt>create...(...)</tt> static factory methods.
     */
    @SideEffectFree
    public ExplicitTransitionSystem()
    {
        this((Graph)null);
    }
    
    /**
     * Creates a new ExplicitTransitionSystem with a given initial graph and
     * no transitions.
     * <p>
     * ExplicitTransitionSystems can also be constructed with 
     * the <tt>create...(...)</tt> static factory methods.
     * 
     * @param initial       
     *      the initial graph of the new ExplicitTransitionSystem, or 
     *      {@code null} if the system has no initial graph
     */
    @SideEffectFree
    public ExplicitTransitionSystem(@Nullable Graph initial)
    {
        this.initial = initial;
        transitions = new HashMap<Graph,Set<Transition>>();
    }
    
    /**
     * Creates a new ExplicitTransitionSystem which is a copy of another one.
     * <p>
     * This constructor constructs a shallow copy of an 
     * ExplicitTransitionSystem.
     * That is, it contains the same objects as graphs and transitions as the 
     * original.
     * 
     * @param orig          
     *      the ExplicitTransitionSystem of which the newly constructed one is
     *      a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public ExplicitTransitionSystem(ExplicitTransitionSystem orig)
    {
        initial = orig.initial;
        transitions = new HashMap<Graph,Set<Transition>>();
        for (Map.Entry<Graph,Set<Transition>> entry : orig.transitions.entrySet()) {
            Set<Transition> value = entry.getValue();
            if (value != null) {
                Set<Transition> newValue = new HashSet<Transition>(value);
                transitions.put(entry.getKey(), newValue);
            }
        }                
    }
    
    /**
     * {@inheritDoc}
     */
    @Override@Pure
    public @Nullable Graph getInitialGraph()
    {
        return initial;
    }

    /**
     * Sets the initial graph. To remove the initial graph from this
     * system, call this method with {@code null}.
     * 
     * @param initial       
     *      the new initial graph of this ExplicitTransitionSystem
     */
    public void setInitialGraph(@Nullable Graph initial)
    {
        this.initial = initial;
    }
    
    /**
     * Adds a transition to this ExplicitTransitionSystem.
     * The transition which is added is either the given transition, or, in 
     * the case that the transition system already contains a graph which is 
     * isomorphic to the transition's source, the isomorphic transition which
     * has the isomorphic graph as source.
     * <p>
     * The transition which is added to the transition system is returned; 
     * that is the returned morphism is either the argument {@code transition},
     * or a transition which is isomorphic to {@code transition}.
     * 
     * @param transition
     *      transition which is to be added to the transition system
     * @return
     *      the transition which is added to the transition system
     * @throws NullPointerException 
     *      if {@code transition == null}
     */
    public Transition addTransition(Transition transition) 
    {
        Graph source = transition.getSource();
        Set<Transition> outs = transitions.get(source);
        if (outs == null) {
            for (Map.Entry<Graph,Set<Transition>> entry : transitions.entrySet()) {
                Morphism iso = Morphism.getIsomorphism(entry.getKey(), source);
                if (iso != null) {
                    Morphism morph = Morphism.createComposition(iso, transition.getMorphism());
                    Transition result = new Transition(morph);
                    result.setRule(transition.getRule());
                    result.setCustomLabel(transition.getCustomLabel());
                    result.setWeight(transition.getWeight());
                    entry.getValue().add(result);
                    return result;
                }
            }
            outs = new HashSet<Transition>();
            outs.add(transition);
            transitions.put(source, outs);
            return transition;
        }
        else {
            outs.add(transition);
            return transition;
        }
    }
    
    /**
     * Adds a transition to this ExplicitTransitionSystem by giving the
     * new transition's correspondence morphism.
     * <p>
     * The transition which is added to the transition system is returned; 
     * that is the returned morphism is either the argument {@code transition},
     * or a transition which is isomorphic to {@code transition}.
     * 
     * @param morph         
     *      morphism of a transition which is to be added to the transition
     *      system
     * @return
     *      the transition which is added to the transition system
     * @throws NullPointerException 
     *      if {@code morph} is {@code null}
     */
    public Transition addTransition(Morphism morph)
    {
        return addTransition(new Transition(morph));
    }
    
    /**
     * Adds transitions to this transition system.
     * This is a convenience method to add more transitions to this transition
     * system in one method call. This method calls
     * {@link ExplicitTransitionSystem#addTransition(Morphism) addTransition}
     * on all arguments.
     * 
     * @param first         
     *      first transition to be added to this transition system
     * @param more
     *      the rest of the transitions to be add to this transition system
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public void addTransitions(Transition first, Transition... more)
    {
        addTransition(first);
        for (Transition t : more) {
            addTransition(t);
        }
    }
    
    /**
     * Adds transitions to this transition system.
     * This is a convenience method to add more transitions to this transition
     * system in one method call. This method calls
     * {@link ExplicitTransitionSystem#addTransition(Morphism) addTransition}
     * on all arguments.
     * 
     * @param first         
     *      morphism of the first transition to be added to this transition
     *      system
     * @param more          
     *      morphisms of the rest of the transitions to be add to this
     *      transition system
     * @throws NullPointerException 
     *      if one of the arguments is {@code null}
     */
    public void addTransitions(Morphism first, Morphism... more)
    {
        addTransition(first);
        for (Morphism t : more) {
            addTransition(t);
        }
    }
    
    /**
     * Adds transitions to this transition system.
     * This is a convenience method to add more than one transition to this
     * transition system in one method call. This method calls
     * {@link ExplicitTransitionSystem#addTransition(Morphism) addTransition}
     * on all morphism in the given collection.
     * 
     * @param transitions   
     *      collection of transitions which are to be added to this transition
     *      system
     * @throws NullPointerException
     *      if {@code transitions} is {@code null} or contains {@code null}
     *      references
     */
    public void addTransitions(Collection<Transition> transitions)
    {
        if (transitions != null) {
            for (Transition t : transitions) {
                addTransition(t);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The Set returned by this method can be modified by the caller, but
     * changes made to the set are not reflected by this transition system.
     * 
     * @param source
     *      the source of the transitions which are returned
     * @return  
     *      a set of transition with the given source
     * @throws NullPointerException
     *      if {@code source} is {@code null}
     */
    @Override@Pure
    public Set<Transition> getTransitions(Graph source)
    {
        if (source == null) throw new NullPointerException("\"source\" cannot be null");
        
        if (transitions.containsKey(source)) {
            return new HashSet<Transition>(transitions.get(source));
        }
        else {
            for (Map.Entry<Graph, Set<Transition>> entry : transitions.entrySet()) {
                Morphism iso = Morphism.getIsomorphism(source, entry.getKey());
                if (iso != null) {
                    Set<Transition> ts = new HashSet<Transition>();
                    for (Transition t : entry.getValue()) {
                        Morphism morph = iso.compose(t.getMorphism());
                        Transition trans = new Transition(morph);
                        trans.setRule(t.getRule());
                        trans.setCustomLabel(t.getCustomLabel());
                        ts.add(trans);
                    }
                    return ts;
                }
            }
            return new HashSet<Transition>(); // return empty set
        }
    }
    
    /**
     * Returns a set of all transitions in this ExplicitTransitionSystem.
     * <p>
     * The returned set can be changed by the caller, but changes made to the 
     * sets are not reflected in this transition system.
     * 
     * @return
     *      set of all transitions of this transition system
     */
    @Pure
    public Set<Transition> getTransitions()
    {
        Set<Transition> result = new HashSet<>();
        
        for (Map.Entry<Graph,Set<Transition>> entry : transitions.entrySet()) {
            result.addAll(entry.getValue());
        }
        
        return result;
    }

    /**
     * Returns the set of graphs which occur in this transition system.
     * <p>
     * The returned Set object can be modified by the caller, but the changes
     * are not reflected in this ExplicitTransitionSystem.
     * 
     * @return             
     *      set of all graphs which occur in this transition system
     */
    @Pure
    public Set<Graph> getGraphs()
    {
        Set<Graph> graphs = new HashSet<Graph>();
        if (initial != null) graphs.add(initial);
        
        for (Map.Entry<Graph,Set<Transition>> entry : transitions.entrySet()) {
            graphs.add(entry.getKey());
            for (Transition t : entry.getValue()) {
                graphs.add(t.getTarget());
            }
        }
        
        return graphs;
    }
    
    /**
     * Returns a map which assigns a depth to each graph in this
     * ExplicitTransitionSystem. The initial graph has depth 0, and a graph
     * reachable in one step from a graph with depth n (and not from a graph
     * with lower depth) has depth n + 1.
     * <p>
     * The returned map is newly created and may be modified by the caller.
     * 
     * @return
     *      map which assigns a depth of each graph in this 
     *      ExplicitTransitionSystem
     * @throws IllegalStateException
     *      if this ExplicitTransitionSystem does not have an initial graph
     */
    @RequiresNonNull("this.initial")
    @Pure
    public IsomorphismMap<Integer> getDepths()
    {
        Graph init = initial;
        
        if (init != null) {
            IsomorphismMap<Integer> map = new IsomorphismHashMap<>();
            LinkedList<Graph> queue = new LinkedList<>();
        
            map.put(init, 0);
            queue.offer(init);
        
            while (!queue.isEmpty()) {
                Graph g = castNonNull(queue.poll());
                int depth = castNonNull(map.get(g));
                for (Transition t : getTransitions(g)) {
                    Graph tgt = t.getTarget();
                    if (!map.containsKey(tgt)) {
                        map.put(tgt, depth + 1);
                        queue.offer(tgt);
                    }
                }
            }
        
            return map;
        }
        else throw new IllegalStateException();
    }
    
    /**
     * Returns the signature of this transition system. The signature consists
     * of all labels which occur in some graph in the transition system.
     * 
     * @return
     *      set of labels which occur in this transition system
     */
    @Pure
    public Set<Label> getSignature()
    {
        Set<Label> sig = initial == null ? new HashSet<Label>() : initial.getSignature();
        for (Map.Entry<Graph,Set<Transition>> entry : transitions.entrySet()) {
            for (Transition t : entry.getValue()) {
                sig.addAll(t.getSource().getSignature());
                sig.addAll(t.getTarget().getSignature());
            }
        }
        return sig;
    }
    
    /**
     * Makes sure that all graphs which occur in this ExplicitTransitionSystem
     * have a name. All newly given names are guaranteed to be unique, already
     * existing duplicate names are not removed.
     */
    public void nameAllGraphs()
    {  
        NamedObject.nameAll(getGraphs());
    }
    
    /**
     * Creates a new ExplicitTransitionSystem.
     * 
     * @param initial       
     *      initial graph of the new ExplicitTransitionSystem
     * @param transitions   
     *      transitions of the new ExplicitTransitionSystem
     * @return              
     *      a new ExplicitTransitionSystem with the given initial graph and
     *      transitions
     */
    @Pure
    public static ExplicitTransitionSystem create(
            Graph initial, 
            Morphism... transitions)
    {
        ExplicitTransitionSystem result = new ExplicitTransitionSystem(initial);
        for (Morphism t : transitions) {
            result.addTransition(t);
        }
        return result;
    }

    /**
     * Creates a new ExplicitTransitionSystem.
     * 
     * @param initial       
     *      initial graph of the new ExplicitTransitionSystem, or {@code null}
     *      if the new ExplicitTransitionSystem doesn't have an initial graph
     * @param transitions
     *      transitions of the new ExplicitTransitionSystem
     * @return
     *      a new ExplicitTransitionSystem with the given initial graph and
     *      transitions
     */
    @Pure
    public static ExplicitTransitionSystem create(
            @Nullable Graph initial, 
            Collection<Transition> transitions)
    {
        ExplicitTransitionSystem result = new ExplicitTransitionSystem(initial);
        result.addTransitions(transitions);
        return result;
    }    

    /**
     * Creates a new ExplicitTransitionSystem by giving a collection of 
     * transition morphisms.
     * 
     * @param initial       
     *      initial graph of the new ExplicitTransitionSystem, or {@code null}
     *      if the new ExplicitTransitionSystem doesn't have an initial graph
     * @param morphisms
     *      transition morphisms of the new ExplicitTransitionSystem
     * @return              
     *      a new ExplicitTransitionSystem with the given initial graph and
     *      transitions
     */
    @Pure
    public static ExplicitTransitionSystem createWithMorphisms(
            @Nullable Graph initial, 
            Collection<Morphism> morphisms)
    {
        ExplicitTransitionSystem result = new ExplicitTransitionSystem(initial);
        for (Morphism m : morphisms) {
            Transition trans = new Transition(m);
            result.addTransition(trans);
        }
        return result;
    }    
}