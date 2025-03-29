package de.uni_due.inf.ti.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EnumeratorTest
{
    private static final boolean OUTPUT = true;
    private static final int NUM_OF_TESTS = 6;
    
    private int els;

    public EnumeratorTest(int els) 
    {
        this.els = els;
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> createParams()
    {
        List<Object[]> params = new ArrayList<>(NUM_OF_TESTS);
        for (int i = 0; i < NUM_OF_TESTS; i++)
            params.add(new Object[] { Integer.valueOf(i) });
        return params;
    }
    
    private static Set<Integer> createIntSet(int els)
    {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < els; i++) set.add(i);
        return set;
    }
    
    @Test
    public void testSubsetEnumerator()
    {
        if (OUTPUT) System.out.printf("Test SubsetEnumerator with %d elements:%n%n", els);
        
        Set<Integer> set = createIntSet(els);
        
        int count = 0;
        Enumerator<Set<Integer>> subsetEnum = Enumerators.createSubsetEnumerator(set); 
        Iterator<Set<Integer>> subsetIt = subsetEnum.iterator();
        
        while (subsetIt.hasNext()) {
            Set<Integer> subset = subsetIt.next();
            if (OUTPUT) System.out.println(subset.toString());
            count++;
        }
        
        int correct = 1 << els;
        
        if (OUTPUT) {
            System.out.printf("Elements: %d    Correct: %d%n%n", count, correct);
        }
        assertEquals(count, correct);
    }
    
    @Test
    public void testMapEnumerator()
    {
        if (OUTPUT) System.out.printf("Test MapEnumerator from and to set with %d elements:%n%n", els);
        
        Set<Integer> keys = createIntSet(els);
        Set<Integer> values = createIntSet(els);
        
        int count = 0;
        Enumerator<HashMap<Integer,Integer>> mapEnum = Enumerators.createMapEnumerator(keys, values);
        Iterator<HashMap<Integer,Integer>> mapIt = mapEnum.iterator();
        
        while (mapIt.hasNext()) {
            HashMap<Integer,Integer> map = mapIt.next();
            if (OUTPUT) System.out.println(map.toString());
            count++;
        }
        
        int correct = 1;
        for (int i = 0; i < els; i++) {
            correct *= els;
        }
        
        if (OUTPUT) {
            System.out.printf("Elements: %d   Correct: %d%n%n", count, correct);
        }
        assertEquals(count, correct);
    }
}
