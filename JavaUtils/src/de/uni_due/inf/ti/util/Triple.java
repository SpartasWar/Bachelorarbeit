package de.uni_due.inf.ti.util;

/* 
 * Copyright (c) 2010-2014, H.J. Sander Bruggink 
 * Copyright (c) 2010-2014, University of Duisburg-Essen
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

import java.io.Serializable;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

/**
 * This class represents a tuple of three values.
 * <p>
 * This class is mainly intended for fast prototyping and temporary use.
 * Using this class in the programmer's interface for a library is not 
 * recommended. In such cases it is better to write a custom class in which
 * the pair elements have meaningful names.
 * 
 * @param <S>
 *      type of the first value of the triple
 * @param <T>
 *      type of the second value of the triple
 * @param <U>
 *      type of the third value of the triple
 * 
 * @author H. J. Sander Bruggink
 */
public final class Triple<S,T,U> implements Serializable
{
    private static final long serialVersionUID = -431705260105536909L;
    
    private S first;
    private T second;
    private U third;

    /**
     * Creates a new Triple of three objects.
     * 
     * @param first
     *      the first object of the triple
     * @param second
     *      the second object of the triple
     * @param third 
     *      the third object of the triple
     */
    public Triple(@Nullable S first, @Nullable T second, @Nullable U third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Returns the first component of this triple.
     */
    public @Nullable S getFirst()
    {
        return first;
    }

    /**
     * Returns the second component of this triple.
     */
    public @Nullable T getSecond()
    {
        return second;
    }
    
    /**
     * Returns the third component of this triple.
     */
    public @Nullable U getThird()
    {
        return third;
    }

    /**
     * Returns a string representation of this triple.
     */
    @Override
    public @NonNull String toString()
    {
        return String.format("<%s,%s,%s>",
                first == null ? "NULL" : first.toString(),
                second == null ? "NULL" : second.toString(),
                third == null ? "NULL" : third.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (o instanceof Triple<?,?,?>) {
            Triple<?,?,?> to = (Triple<?,?,?>) o;
            return (first == null ? to.first == null : first.equals(to.first))
                && (second == null ? to.second == null : second.equals(to.second))
                && (third == null ? to.third == null : third.equals(to.third));
        }
        return false;
    }

    /**
     * Returns the hash code of this Triple.
     * 
     * @return 
     *      hash code of this Triple
     */
    @Override
    public int hashCode()
    {
        int h1 = (first == null ? 0 : first.hashCode());
        int h2 = (second == null ? 0 : second.hashCode());
        int h3 = (third == null ? 0 : third.hashCode());
        return 961 * h1 + 31 * h2 + h3;
    }

    /**
     * Creates a triple with specified components. This is a convenience factory
     * method. It does the same as the constructor, but will automatically infer
     * the generic type.
     * 
     * @param fst
     *      the first element of the new triple
     * @param snd
     *      the second element of the new triple
     * @param thr
     *      the third element of the new triple
     * @return 
     *      a pair consisting of the given elements
     */
    public static <U,V,W> @NonNull Triple<U,V,W> create(
            @Nullable U fst, 
            @Nullable V snd,
            @Nullable W thr)
    {
        return new Triple<U,V,W>(fst, snd, thr);
    }
}
