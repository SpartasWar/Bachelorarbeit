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

import java.math.BigInteger;

/**
 * A random value generator which randomly generates values of a certain kind
 * using a specified distribution.
 * <p>
 * This methods and method names of this class are heavily influenced by
 * {@link java.util.Random}. However, this class does not extend that class.
 * First, instances of this class in general are required only to return
 * values of a single type; second, instances of this class do not always 
 * return *uniformly* distributed values (it depends on the user-specified
 * distribution).
 * 
 * @param T 
 *      type of the values returned by this random value generator
 * 
 * @author 
 *      H.J. Sander Bruggink
 */
public class Random<T> 
{
    private java.util.Random delegate;
    private Distribution<T> distribution;
    
    
    /**
     * Creates a new random number generator which uses a user-defined 
     * distribution.
     * 
     * @param distribution
     *      the distribution of the new random number generator
     * @throws NullPointerException
     *      if {@code distribution} is {@code null}
     */
    public Random(Distribution<T> distribution)
    {
        if (distribution == null) throw new NullPointerException();
        this.delegate = RandomUtils.getRandomNumberGenerator();
        this.distribution = distribution; 
    }
    
    
    /** 
     * Sets the underlying random number generator.
     * 
     * @param random
     *      the new underlying random number generator
     */
    public void setRandom(java.util.Random random)
    {
        delegate = random;
    }
    
    
    /**
     * Returns the underlying random number generator.
     */
    public java.util.Random getRandom()
    {
        return delegate;
    }
    
    
    /**
     * Returns the distribution used by this random number generator.
     */
    public Distribution<T> getDistribution()
    {
        return distribution;
    }
    
    
    /**
     * Sets the distribution used by this random number generator.
     */
    public void setDistribution(Distribution<T> dist)
    {
        distribution = dist;
    }
    
    
    /**
     * Returns a randomly generated float.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to float and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated float
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final float nextFloat()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).floatValue();
        }
        else {
            throw new ClassCastException();
        }
    }

    
    /**
     * Returns a randomly generated double.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to double and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated double
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final double nextDouble()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        else {
            throw new ClassCastException();
        }
    }
    
    
    /**
     * Returns a randomly generated byte.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to byte and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated byte
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final byte nextByte()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).byteValue();
        }
        else {
            throw new ClassCastException();
        }
    }

    
    /**
     * Returns a randomly generated short.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to short and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated short
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final short nextShort()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).shortValue();
        }
        else {
            throw new ClassCastException();
        }
    }

    
    /**
     * Returns a randomly generated int.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to double and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated double
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final int nextInt()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        else {
            throw new ClassCastException();
        }
    }


    /**
     * Returns a randomly generated long.
     * This method calls {@link #nextValue()}. If the returned value is 
     * numerical, it is cast to long and returned. Otherwise, a
     * {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated long
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final long nextLong()
    {
        T value = nextValue();
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        else {
            throw new ClassCastException();
        }
    }
    
    
    /**
     * Returns a randomly generated BigInteger.
     * This method calls {@link #nextValue()}. If the returned value is a
     * BigInteger, it is returned. If the returned value is another numerical
     * value, a new BigInteger with the same (integer) value is returned. 
     * Otherwise, a {@code ClassCastException} is thrown.
     * 
     * @return
     *      randomly generated long
     * @throws ClassCastException
     *      if the next value returned by this random value generator is not
     *      a numerical value
     */
    public final BigInteger nextBigInteger()
    {
        T value = nextValue();
        if (value instanceof BigInteger) {
            return (BigInteger)value;
        }
        else if (value instanceof Number) {
            return BigInteger.valueOf(((Number)value).longValue());
        }
        else {
            throw new ClassCastException();
        }
    }
    
    
    /**
     * Returns the next value of this random value generator.
     */
    public T nextValue()
    {
        return distribution.nextValue(delegate);
    }
    
    
    /**
     * Creates a new random value generator with a given distribution.
     * 
     * @param dist
     *      distribution of the new random value generator
     * @return
     *      random value generator with the given distribution
     */
    public static <T> Random<T> create(Distribution<T> dist)
    {
        return new Random<T>(dist); 
    }
}
