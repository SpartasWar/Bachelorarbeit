package de.uni_due.inf.ti.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains static utility methods concerning randomization.
 * 
 * @author H.J. Sander Bruggink
 */
public class RandomUtils
{
    // Prohibit instantiation
    private RandomUtils() { } 
    
    
    private static java.util.Random globalRng = null;
    
    
    /**
     * Returns a Java random number generator object. 
     * This method will always return the same instance of the Java
     * random number generator.
     * 
     * @return
     *      a java.util.Random object
     */
    public static java.util.Random getRandomNumberGenerator()
    {
        if (globalRng == null) globalRng = new java.util.Random();
        return globalRng;
    }
    
    
    /**
     * Returns a random element from a collection. All elements of the 
     * collection have approximately the same chance of being chosen.
     * 
     * @param collection
     *      collection to choose a random element from
     * @return
     *      a random element from {@code collection}
     * @throw NullPointerException
     *      if {@code collection} is {@code null}
     * @throw IllegalArgumentException 
     *      if {@code collection} is empty
     */
    public static <T> T getRandomElement(Collection<T> collection)
    {
        if (collection.isEmpty()) throw new IllegalArgumentException();
        
        List<T> list = (collection instanceof List ? 
                (List<T>)collection : 
                    new ArrayList<T>(collection));
        
        int index = getRandomNumberGenerator().nextInt(list.size());
        return list.get(index);
    }
}
