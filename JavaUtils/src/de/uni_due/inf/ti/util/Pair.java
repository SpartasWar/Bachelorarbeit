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

package de.uni_due.inf.ti.util;

import java.io.Serializable;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

/**
 * This class represents a pair of two values.
 * <p>
 * This class is mainly intended for fast prototyping and temporary use.
 * Using this class in the programmer's interface for a library is not 
 * recommended. In such cases it is better to write a custom class in which
 * the pair elements have meaningful names.
 * 
 * @param <S>
 *      type of the first value of the pair
 * @param <T>
 *      type of the second value of the pair
 * 
 * @author H. J. Sander Bruggink
 */
public final class Pair<S,T> implements Serializable
{
    private static final long serialVersionUID = 4950400536342857965L;

    private @Nullable S first;
    private @Nullable T second;

    /**
     * Creates a new Pair of two objects.
     * 
     * @param first
     *      the first object of the pair
     * @param second
     *      the second object of the pair
     */
    public Pair(@Nullable S first, @Nullable T second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first object of this pair.
     */
    public @Nullable S getFirst()
    {
        return first;
    }

    /**
     * Returns the second object of this pair.
     */
    public @Nullable T getSecond()
    {
        return second;
    }

    /**
     * Returns a string representation of this Pair.
     */
    @Override
    public @NonNull String toString()
    {
        return String.format("<%s,%s>",
                first != null ? first.toString() : "NULL",
                second != null ? second.toString() : "NULL");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (o instanceof Pair<?, ?>) {
            Pair<?, ?> po = (Pair<?, ?>) o;
            return (first == null ? po.first == null : first.equals(po.first))
                    && (second == null ? po.second == null : second
                            .equals(po.second));
        }
        return false;
    }

    /**
     * Returns the hash code of this Pair.
     * 
     * @return 
     *      hash code of this Pair
     */
    @Override
    public int hashCode()
    {
        int h1 = (first == null ? 0 : first.hashCode());
        int h2 = (second == null ? 0 : second.hashCode());
        return 31 * h1 + h2;
    }

    /**
     * Returns a new pair which is the inverse of this pair. The returned pair
     * has this pair's second element as first element and this pair's first
     * element as second element.
     * 
     * @return inverse of this pair
     */
    public @NonNull Pair<T,S> inverse()
    {
        return create(second, first);
    }

    /**
     * Creates a pair with specified components. This is a convenience factory
     * method. It does the same as the constructor, but will automatically infer
     * the generic type.
     * 
     * @param fst
     *      the first element of the new pair
     * @param snd
     *      the second element of the new pair
     * @return 
     *      a pair consisting of the given elements
     */
    public static <U,V> @NonNull Pair<U,V> create(
            @Nullable U fst, 
            @Nullable V snd)
    {
        return new Pair<U,V>(fst, snd);
    }
}
