package de.uni_due.inf.ti.graph.util;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Map of which the keys are graphs modulo isomorphism.
 * 
 * @author H.J. Sander Bruggink
 *
 * @param <T>
 *      type of the values
 */
public class IsomorphismHashMap<T> 
        extends AbstractMap<Graph, T> 
        implements IsomorphismMap<T>
{
    private Map<Integer,List<SimpleEntry<Graph,T>>> elements;
    
    /**
     * Iterator which iterates through the entries of this IsomorphismHashMap.
     */
    private class EntryIterator implements Iterator<Map.Entry<Graph,T>>
    {
        private Iterator<List<SimpleEntry<Graph,T>>> listIterator;
        private Iterator<SimpleEntry<Graph,T>> entryIterator;
        private List<SimpleEntry<Graph,T>> curList;
        
        public EntryIterator()
        {
            listIterator = elements.values().iterator();
            if (listIterator.hasNext()) {
                curList = listIterator.next();
                entryIterator = curList.iterator();
            }
        }
        
        @Override
        public boolean hasNext()
        {
            return (entryIterator.hasNext() || listIterator.hasNext());
        }
        
        @Override
        public Map.Entry<Graph,T> next()
        {
            if (!entryIterator.hasNext()) {
                curList = listIterator.next();
                entryIterator = curList.iterator();
            }
            return entryIterator.next();
        }
        
        @Override
        public void remove()
        {
            entryIterator.remove();
            if (curList.isEmpty()) {
                listIterator.remove();
            }
        }
    }
    
    /**
     * Iterator which delegates all of its methods (except next) to another
     * iterator.
     */
    private abstract class DelegateIterator<I> implements Iterator<I>
    {
        protected EntryIterator entryIterator;

        DelegateIterator()
        {
            entryIterator = new EntryIterator();
        }
        
        @Override
        public boolean hasNext()
        {
            return entryIterator.hasNext();
        }

        @Override
        public void remove()
        {
            entryIterator.remove();
        }
    }
    
    /**
     * Key set of this IsomorphismHashMap.
     */
    private class KeySet extends AbstractSet<Graph> implements IsomorphismSet
    {
        @Override
        public Iterator<Graph> iterator()
        {
            return new DelegateIterator<Graph>() {
                @Override
                public Graph next()
                {
                    return entryIterator.next().getKey();
                }
            };
        }

        @Override
        public int size()
        {
            return IsomorphismHashMap.this.size();
        }
        
        @Override
        public boolean isEmpty()
        {
            return IsomorphismHashMap.this.isEmpty();
        }
        
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
        
        @Override
        public boolean containsAll(Collection<?> col)
        {
             for (Object ob : col) {
                 if (!contains(ob)) return false;
             }
             return true;
        }
        
        @Override
        public @Nullable Morphism containsIsomorphic(Graph graph)
        {
            int isoHash = graph.getIsoHash();
            List<SimpleEntry<Graph,T>> list = elements.get(isoHash);
            if (list != null) {
                for (SimpleEntry<Graph,T> el : list) {
                    Morphism m = Morphism.getIsomorphism(graph, el.getKey());
                    if (m != null) {
                        return m;
                    }
                }
            }
            return null;
        }
        
        @Override
        public Morphism addIsomorphic(Graph graph)
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void clear()
        {
            IsomorphismHashMap.this.clear();
        }
        
        @Override
        public boolean remove(Object graph)
        {
            T old = IsomorphismHashMap.this.remove(graph);
            return (old != null);
        }
    }
    

    /**
     * Creates a new empty IsomorphismHashMap.
     */
    public IsomorphismHashMap()
    {
        elements = new HashMap<>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable T get(Object key)
    {
        if (!(key instanceof Graph)) return null;
        Graph graph = (Graph)key;
        
        int hash = graph.getIsoHash();
        List<SimpleEntry<Graph,T>> list = elements.get(hash);
        if (list != null) {
            for (Map.Entry<Graph,T> entry : list) {
                if (Morphism.getIsomorphism(entry.getKey(), graph) != null) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable T put(Graph key, T value)
    {
        int hash = key.getIsoHash();
        List<SimpleEntry<Graph,T>> list = elements.get(hash);
        if (list == null) {
            list = new LinkedList<>();
            SimpleEntry<Graph,T> entry = new SimpleEntry<>(key, value);
            list.add(entry);
            elements.put(hash, list);
            return null;
        }
        else {
            for (Map.Entry<Graph,T> entry : list) {
                if (Morphism.getIsomorphism(entry.getKey(), key) != null) {
                    return entry.setValue(value);
                }
            }
            SimpleEntry<Graph,T> entry = new SimpleEntry<>(key, value);
            list.add(entry);            
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override 
    public void putAll(Map<? extends Graph, ? extends T> map)
    {
        for (Map.Entry<? extends Graph, ? extends T> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        int size = 0;
        for (List<SimpleEntry<Graph,T>> list : elements.values()) {
            size += list.size();
        }
        return size;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<Graph,T>> entrySet()
    {
        return new AbstractSet<Map.Entry<Graph,T>>() {
            @Override
            public Iterator<Map.Entry<Graph,T>> iterator()
            {
                return new EntryIterator();
            }

            @Override
            public int size()
            {
                return IsomorphismHashMap.this.size();
            }
        };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IsomorphismSet keySet()
    {
        return new KeySet();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> values()
    {
        return new AbstractCollection<T>() {
            @Override
            public Iterator<T> iterator()
            {
                return new DelegateIterator<T>() {
                    @Override
                    public T next()
                    {
                        return entryIterator.next().getValue();
                    }
                };
            }

            @Override
            public int size()
            {
                return IsomorphismHashMap.this.size();
            }
        };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable T remove(Object key)
    {
        if (key == null) throw new NullPointerException();
        if (key instanceof Graph) {
            Graph graph = (Graph)key;
            int isoHash = graph.getIsoHash();
            List<SimpleEntry<Graph,T>> list = elements.get(isoHash);
            for (Iterator<SimpleEntry<Graph,T>> it = list.iterator(); it.hasNext(); ) {
                SimpleEntry<Graph,T> entry = it.next();
                Morphism iso = Morphism.getIsomorphism(graph, entry.getKey());
                if (iso != null) {
                    it.remove();
                    if (list.isEmpty()) {
                        elements.remove(isoHash);
                    }
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
