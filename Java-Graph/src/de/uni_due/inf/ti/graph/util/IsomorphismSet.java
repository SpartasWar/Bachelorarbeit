package de.uni_due.inf.ti.graph.util;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * A set of Graphs which ensures that no two isomorphic graphs are contained
 * in the set. 
 *  
 * @author H.J. Sander Bruggink
 */
public interface IsomorphismSet extends Set<Graph>
{
    /**
     * Determines whether this IsomorphismSet contains a graph which is 
     * isomorphic to a given graph. If it does, an isomorphism from the given
     * graph to the graph in the IsomorphismSet is returned; otherwise,
     * {@code null} is returned.
     * 
     * @param graph
     *      graph to find in this IsomorphismSet
     * @return
     *      an isomorphism from {@code graph} to a graph in this IsomorphismSet,
     *      or {@code null} if this IsomorphismSet does not contain a graph
     *      which is isomorphic to {@code graph}
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    public @Nullable Morphism containsIsomorphic(Graph graph);
    
    
    /**
     * Makes sure that a graph isomorphic to a given graph is present in this
     * IsomorphismSet (optional operation). 
     * If a graph isomorphic to the given graph is already contained in the set,
     * a morphism from the given graph to it is returned.
     * Otherwise, the given graph is added to the set and the identity 
     * morphism is returned.
     * 
     * @param graph
     *      graph to be added to this IsomorphismSet
     * @return
     *      an isomorphism from {@code graph} to an isomorphic graph in this
     *      set
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     * @throws UnsupportedOperation
     *      if this IsomorphismSet does not support adding elements
     */
    public Morphism addIsomorphic(Graph graph);
}
