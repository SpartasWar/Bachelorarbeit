package de.uni_due.inf.ti.graph.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * A set of Graphs which ensures that no two isomorphic graphs are contained
 * in the set. 
 * <p>
 * Graphs should not be modified while they are in an IsomorphismHashSet. 
 * If graphs are modified, the behavior is unspecified.
 *  
 * @author H.J. Sander Bruggink
 */
public class IsomorphismHashSet implements IsomorphismSet
{
    // INVARIANT: elements does not contain empty lists.
    private Map<Integer,List<Graph>> elements;

    /**
     * Iterator for IsomorphismSet instances.
     */
    private class MyIterator implements Iterator<Graph>
    {
        private Iterator<List<Graph>> listIterator;
        private Iterator<Graph> graphIterator;
        private List<Graph> curList;
        
        public MyIterator()
        {
            listIterator = elements.values().iterator();
            if (listIterator.hasNext()) {
                curList = listIterator.next();
                graphIterator = curList.iterator();
            }
        }
        
        @Override
        public boolean hasNext()
        {
            return (graphIterator.hasNext() || listIterator.hasNext());
        }
        
        @Override
        public Graph next()
        {
            if (!graphIterator.hasNext()) {
                curList = listIterator.next();
                graphIterator = curList.iterator();
            }
            return graphIterator.next();
        }
        
        @Override
        public void remove()
        {
            graphIterator.remove();
            if (curList.isEmpty()) {
                listIterator.remove();
            }
        }
    }

    
    /**
     * Creates a new IsomorphismSet instance.
     */
    public IsomorphismHashSet()
    {
        elements = new HashMap<>();
    }
    
    /**
     * Returns an iterator which iterates over the elements of this
     * IsomorphismSet.
     */
    @Override
    public Iterator<Graph> iterator()
    {
        return new MyIterator();
    }
    
    /**
     * Adds a graph to this IsomorphismSet.
     *  
     * @param graph
     *      graph to be added to this IsomorphismSet
     * @return
     *      {@code true} if the set changed because of the call, 
     *      {@code false} if not (that is, an isomorphic graph is already
     *      in the set)
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @Override
    public boolean add(Graph graph) 
    {
        int isoHash = graph.getIsoHash();
        List<Graph> list = elements.get(isoHash);
        if (list == null) {
            list = new ArrayList<>();
            list.add(graph);
            elements.put(isoHash, list);
            return true;
        }
        else {
            for (Graph el : list) {
                if (Morphism.getIsomorphism(el, graph) != null) {
                    return false;
                }
            }
            list.add(graph);
            return true;
        }
    }
    
    /**
     * Makes sure that a graph isomorphic to a given graph is present in this
     * IsomorphismSet. If a graph isomorphic to the given graph is already
     * contained in the set, a morphism from the given graph to it is returned.
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
     */
    public Morphism addIsomorphic(Graph graph)
    {
        int isoHash = graph.getIsoHash();
        List<Graph> list = elements.get(isoHash);
        if (list == null) {
            list = new ArrayList<>();
            list.add(graph);
            elements.put(isoHash, list);
            return Morphism.createIdentity(graph);
        }
        else {
            for (Graph el : list) {
                Morphism iso = Morphism.getIsomorphism(el, graph);
                if (iso != null) {
                    return iso;
                }
            }
            list.add(graph);
            return Morphism.createIdentity(graph);
        }
    }
    
    /**
     * Adds all graphs in a collection to this IsomorphismSet.
     * 
     * @param col
     *      the collection to add to this IsoMorphismSet
     * @return
     *      {@code true} if the set changed because of the call, 
     *      {@code false} otherwise
     * @throws NullPointerException
     *      if {@code col} is {@code null} or contains {@code null} elements
     */
    @Override
    public boolean addAll(Collection<? extends Graph> col)
    {
        boolean changed = false;
        for (Graph graph : col) {
            changed |= add(graph);
        }
        return changed;
    }
    
    /**
     * Removes the specified element from this IsomorphismSet.
     * 
     * @param o
     *      the object which is removed from this IsomorphismSet
     * @return 
     *      {@code true} if this IsomorphismSet contained the object,
     *      {@code false} otherwise 
     * @throws NullPointerException
     *      if {@code o} is {@code null}
     */
    @Override
    public boolean remove(Object o) 
    {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o instanceof Graph) {
            Graph graph = (Graph)o;
            int isoHash = graph.getIsoHash();
            List<Graph> list = elements.get(isoHash);
            for (Iterator<Graph> it = list.iterator(); it.hasNext(); ) {
                Graph itGraph = it.next();
                Morphism iso = Morphism.getIsomorphism(graph, itGraph);
                if (iso != null) {
                    it.remove();
                    if (list.isEmpty()) {
                        elements.remove(isoHash);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Removes all elements of a specified collection from this IsomorphismSet.
     * 
     * @param col
     *      the collection to remove from this IsomorphismSet
     * @return 
     *      {@code true} if any elements were removed, {@code false} otherwise
     * @throws NullPointerException
     *      if {@code col} is {@code null} or contains {@code null} elements
     */
    @Override
    public boolean removeAll(Collection<?> col)
    {
        boolean changed = false;
        for (Object o : col) {
            changed |= remove(o);
        }
        return changed;
    }
    
    /**
     * Removes all elements which do not have an isomorphic graph in a given
     * collection.
     * 
     * @param col
     *      the collection which is retained in this IsomorphismSet
     * @return 
     *      {@code true} if any elements were removed, {@code false} otherwise
     * @throws NullPointerException
     *      if {@code col} is {@code null} or contains {@code null} elements
     */
    @Override
    public boolean retainAll(Collection<?> col)
    {
        boolean changed = false;
        
        for (Iterator<Graph> it = iterator(); it.hasNext(); ) {
            Graph graph = it.next();
            if (!collectionContainsIsomorphicGraph(col, graph)) {
                it.remove();
                changed = true;
            }
        }
        
        return changed;
    }
    
    /**
     * Determines whether this IsomorphismSet contains a graph which is 
     * isomorphic to a given graph.
     * 
     * @param graph
     *      graph to find in the IsomorphismSet
     * @return
     *      a Morphism from {@code graph} to an isomorphic graph in this set,
     *      or {@code null} if no isomorphic graph was found
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    public Morphism containsIsomorphic(Graph graph)
    {
        int isoHash = graph.getIsoHash();
        List<Graph> list = elements.get(isoHash);
        if (list != null) {
            for (Graph el : list) {
                Morphism m = Morphism.getIsomorphism(graph, el);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * Determines whether this IsomorphismSet contains a graph which is 
     * isomorphic to a given graph.
     * 
     * @param graph
     *      graph to find in the IsomorphismSet
     * @return
     *      {@code true} if this IsomorphismSet contains a graph which is
     *      isomorphic to {@code graph}, {@code false} otherwise
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @Override
    public boolean contains(Object graph)
    {
        if (!(graph instanceof Graph)) {
            return false;
        }
        else {
            return containsIsomorphic((Graph)graph) != null;
        }
    }
    
    /**
     * Determines whether this IsomorphismHashSet contains isomorphic elements
     * for all elements of a given collection.
     * 
     * @param col
     *      collection to check for containment in this IsomorphismHashSet
     * @return
     *      {@code true} if this IsomorphismHashSet contains an isomorphic
     *      element for each element of {@code col}
     * @throws NullPointerException
     *      if {@code col} is {@code null} or contains {@code null} elements 
     */
    @Override
    public boolean containsAll(Collection<?> col)
    {
        for (Object ob : col) {
            if (!contains(ob)) return false;
        }
        return true;
    }
    
    /**
     * Removes all elements from this IsomorphismList.
     */
    @Override
    public void clear()
    {
        elements.clear();
    }

    /**
     * Determines whether this IsomorphismHashSet is empty.
     * 
     * @return 
     *      {@code true} if this IsomorphismHashSet does not contain any 
     *      elements, {@code false} otherwise
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    /**
     * Returns the number of graphs in this IsomorphismSet.
     */
    @Override
    public int size()
    {
        int size = 0;
        for (List<Graph> list : elements.values()) {
            size += list.size();
        }
        return size;
    }
    
    /**
     * Returns an array containing all the graphs in this IsomorphismHashSet.
     * 
     * @return
     *      an array containing all graphs in this IsomorphismHashSet.
     */
    @Override
    public Object[] toArray()
    {
        List<Graph> list = new ArrayList<>();
        iterator().forEachRemaining(list::add);
        return list.toArray();
    }

    /**
     * Returns an array containing all the graphs in this IsomorphismHashSet;
     * the runtime type of the returned array is that of the specified array.
     * 
     * @param arr
     *      the array into which the elements of this set are to be stored, if
     *      it is big enough; otherwise, a new array of the same runtime type
     *      is allocated for this purpose.
     * @return
     *      an array containing all graphs in this IsomorphismHashSet.
     * @throws NullPointerException
     *      if {@code arr} is {@code null}
     * @throws ArrayStoreException
     *      if the runtime type of the specified array is not a supertype of
     *      the runtime type of every element in this set   
     */
    @Override
    public <T> T[] toArray(T[] arr)
    {
        List<Graph> list = new ArrayList<>();
        iterator().forEachRemaining(list::add);
        return list.toArray(arr);
    }
    
    /** 
     * Returns the hash code of this IsomorphismHashSet. This implementation 
     * returns the sum of all isohashes of the graphs in this
     * IsomorphismHashSet.
     *      
     * @return
     *      the hash code of this IsomorphismHashSet.
     */
    @Override
    public int hashCode()
    {
        int hash = 31;
        for (Graph el : this) {
            hash += el.getIsoHash();
        }
        return hash;
    }
    
    /**
     * Returns whether these two isomorphism sets contain the same elements
     * (modulo isomorphism).
     */
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (o == null || !(o instanceof IsomorphismHashSet)) {
            return false;
        }
        IsomorphismHashSet io = (IsomorphismHashSet)o;
        return (containsAll(io) && io.containsAll(this));
    }
    
    /**
     * Determines whether a collection contains a Graph instance which is
     * isomorphic to a given graph.
     */
    private static boolean collectionContainsIsomorphicGraph(Collection<?> col, Graph graph)
    {
        for (Object ob : col) {
            if (ob instanceof Graph) {
                Graph el = (Graph)ob;
                if (Morphism.getIsomorphism(el, graph) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
