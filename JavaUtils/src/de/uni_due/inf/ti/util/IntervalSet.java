package de.uni_due.inf.ti.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

// Created: 31 January 2014

/**
 * An unmodifiable set which contains all integers in a certain interval.
 * 
 * @author H.J. Sander Bruggink
 */
public class IntervalSet extends AbstractSet<Integer>
{
    private int min;
    private int max;
        
    
    /**
     * Creates a new IntervalSet.
     * 
     * @param min
     *      lowest value in the set 
     * @param max
     *      lowest value not in the set
     */
    public IntervalSet(int min, int max)
    {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public Iterator<Integer> iterator()
    {
        return new Iterator<Integer>() {
            private int cur = min;
               
            @Override
            public boolean hasNext()
            {
                return cur < max; 
            }

            @Override
            public Integer next()
            {
                return cur++;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
    

    @Override
    public int size()
    {
        return max - min;
    }
    
    
    @Override
    public boolean contains(Object o)
    {
        if (!(o instanceof Integer)) {
            return false;
        }
        else {
            int i = (Integer)o;
            return (i >= min && i < max);
        }
    }
    
    
    @Override
    public boolean containsAll(Collection<?> col) 
    {
        for (Object o : col) {
            if (!contains(o)) return false;
        }
        return true;
    }
}
