package de.uni_due.inf.ti.graph.util;

import java.util.Map;

import de.uni_due.inf.ti.graph.Graph;

/**
 * A map which maps graphs (modulo isomorphism) to objects.
 * The only thing this interface changes to its super-interface {@link Map} is 
 * the return type of the {@link #keySet} method; this interface specializes
 * the return type to a {@link IsomorphismSet}.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <V>
 *      the type of the values of the map
 */
public interface IsomorphismMap<V> extends Map<Graph,V>
{
    /**
     * Returns a view on the keys of this IsomorphismMap. 
     * Changes to the IsomorphismMap are reflected by the view and vice versa.
     * The view supports removing elements (in which case the corresponding
     * key-value pair is removed from the map), but it does not support adding
     * elements.
     * 
     * @return
     *      a view on the set of keys of this IsomorphismMap
     */
    @Override
    public IsomorphismSet keySet();
}
