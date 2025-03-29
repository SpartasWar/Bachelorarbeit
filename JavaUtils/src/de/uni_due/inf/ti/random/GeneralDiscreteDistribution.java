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

package de.uni_due.inf.ti.random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_due.inf.ti.util.Pair;

public class GeneralDiscreteDistribution<T> implements DiscreteDistribution<T>
{
    private List<Pair<Double,T>> values;
    private double length;
    
    
    /**
     * Creates a new DiscreteDistribution instance.
     */
    public GeneralDiscreteDistribution()
    {
        values = new ArrayList<Pair<Double, T>>();
    }
    
    
    /**
     * Adds a value to this DiscreteDistribution.
     * 
     * @param strength
     *      relative probability of the new value
     * @param value
     *      the value to be added to the distribution
     */
    public void add(double strength, T value)
    {
        length += strength;
        Pair<Double,T> newPair = Pair.create(length, value);
        values.add(newPair);
    }
    
    
    /**
     * {@inheritDoc}
     * <p>
     * If no values have been added to this distribution, this method returns
     * {@code null}.
     */
    @Override
    public T nextValue(java.util.Random rng)
    {
        if (values.isEmpty()) {
            return null;
        }
       
        double rand = rng.nextDouble() * length;

        int i = 0;
        while (values.get(i).getFirst() < rand) i++;
        
        return (values.get(i).getSecond());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<T> getElements()
    {
        Set<T> set = new HashSet<T>();
        for (Pair<Double,T> entry : values) {
            set.add(entry.getSecond());
        }
        return set;
    }
}
