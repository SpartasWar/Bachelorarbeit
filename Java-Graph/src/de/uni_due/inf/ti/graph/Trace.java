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

// created: 2014-03-21

package de.uni_due.inf.ti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Object which represent a single trace. A trace is a list of consecutive
 * transitions.
 * 
 * @author H.J. Sander Bruggink
 */
public final class Trace extends NamedObject implements TransitionSystem
{
    private List<Transition> transitions;
    
    /**
     * Creates a new, empty trace.
     */
    @SideEffectFree
    public Trace()
    {
        transitions = new ArrayList<>();
    }
    
    /** 
     * Creates a new trace which is a copy of an existing one.
     * 
     * @param orig
     *      the trace of which the newly created trace is a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public Trace(Trace orig)
    {
        super(orig);
        transitions = new ArrayList<>(orig.transitions.size());
        for (Transition tr : orig.transitions) {
            transitions.add(new Transition(tr));
        }
    }
    
    /**
     * Returns the first graph of the trace. The first graph of the trace is
     * the source graph of the first transition. If this trace does not contain
     * any transitions, this method returns {@code null}.
     * 
     * @return
     *      the initial graph of this trace
     */
    @Override
    @Pure
    public @Nullable Graph getInitialGraph()
    {
        if (transitions.isEmpty()) {
            return null;
        }
        else {
            return transitions.get(0).getSource();
        }
    }
    
    /**
     * Returns the list of graphs of this trace. The returned list contains
     * the graphs occurring in this trace in trace order, beginning with the
     * source graph of the first transition in the trace and ending with the
     * target graph of the last transition in the trace.
     * <p>
     * The returned list is modifiable. Changed made to the list do not
     * reflect back into this trace, and changes made to the trace are not
     * reflected in the returned list.
     * 
     * @return
     *      list of graphs in this trace, in trace order
     */
    @SideEffectFree
    public List<Graph> getGraphs()
    {
        if (transitions.isEmpty()) return new ArrayList<>();
        List<Graph> result = new ArrayList<>(transitions.size() + 1);
        result.add(transitions.get(0).getSource());
        for (Transition tr : transitions) {
            result.add(tr.getTarget());
        }
        return result;
    }
    
    /**
     * Returns all transitions of this trace that begin at a specific
     * source graph.
     * 
     * @param source
     *      source graph of the returned transitions
     * @return
     *      collection of transitions in this trace which have the given
     *      graph as source
     * @throws NullPointerException
     *      if {@code source} is {@code null}
     */
    @Override
    @SideEffectFree
    public Collection<Transition> getTransitions(Graph source)
    {
        if (source == null) throw new NullPointerException("\"source\" must be non-null");
        
        List<Transition> result = new LinkedList<>();
        for (Transition trans : transitions) {
            for (Morphism iso : Morphism.getIsomorphisms(trans.getSource(), source)) {
                result.add(trans.changeSource(iso));
            }
        }
        return result;
    }
    
    /**
     * Returns a list of all transitions in this Trace. The list contains the
     * transitions in trace order.
     */
    @SideEffectFree
    public List<Transition> getTransitions()
    {
        return Collections.unmodifiableList(transitions);
    }
    
    /**
     * Adds a transition to this trace.
     * 
     * @param t
     *      the transition which is added to this trace
     * @throws NullPointerException
     *      if {@code t} is {@code null}
     * @throws IllegalArgumentException
     *      if the source graph of {@code t} is different than the target 
     *      graph of the last transition of this trace
     */
    public void addTransition(Transition t)
    {
        if (!transitions.isEmpty() && 
                t.getSource() != transitions.get(transitions.size() - 1).getTarget()) {
            throw new IllegalArgumentException("Source of new transition must be the same as the target of the last transition");
        }
        transitions.add(t);
    }
    
    /**
     * Adds a new transition to this trace which is created from a morphism.
     * 
     * @param m
     *      the morphism from which the new transition is created
     * @throw NullPointerException
     *      if {@code m} is {@code null}
     * @throw IllegalArgumentException
     *      if the domain of {@code m} is different that the target graph of the
     *      last transition of this trace
     */
    public void addTransition(Morphism m) 
    {
        addTransition(new Transition(m));
    }  
    
    /**
     * Returns a ExplicitTransitionSystem consisting of the transitions
     * of this Trace. The returned transition system is a newly created object
     * and represents the state of this trace at the moment in which this 
     * method is called: changes to the trace are not reflected by the 
     * transition system and vice versa.
     *  
     * @return
     *      new ExplicitTransitionSystem object representing the transitions
     *      of this Trace 
     */
    public ExplicitTransitionSystem createTransitionSystem()
    {
        return ExplicitTransitionSystem.create(getInitialGraph(), transitions);
    }
    
    /**
     * Returns a string representation of this trace.
     */
    @Override
    @Pure
    public String toString()
    {
        if (transitions.isEmpty()) return "trace { }";
        
        StringBuilder buf = new StringBuilder();
        buf.append("trace {\n");
        for (Transition t : transitions) {
            Graph g = t.getSource();
            buf.append(g.toString("    "));
            buf.append('\n');
            Morphism m = t.getMorphism();
            buf.append(m.toString("    "));
            buf.append('\n');
        }
        buf.append(transitions.get(transitions.size() - 1).getTarget().toString("    "));
        buf.append("\n}");
        return buf.toString();
    }
}
