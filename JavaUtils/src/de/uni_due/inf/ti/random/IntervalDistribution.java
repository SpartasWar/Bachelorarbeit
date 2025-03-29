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

import java.util.Set;

import de.uni_due.inf.ti.util.IntervalSet;

/**
 * This class represents a uniform distribution of values in a certain interval.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class IntervalDistribution<T extends Number> 
    implements Distribution<T>
{
    /**
     * Interval distribution with a float precision.
     */
    public static class Float extends IntervalDistribution<java.lang.Float>
    {
        private float min;
        private float max;

        /**
         * Creates a new IntervalDistribution.Float instance.
         * This is a uniform distribution of all floats from <i>min</i> 
         * (inclusive) to <i>max</i> (exclusive).
         * 
         * @param min
         *      the minimum value of the interval (inclusive)
         * @param max
         *      the maximum value of the interval (exclusive)
         */
        Float(float min, float max)
        {
            if (min > max) {
                float temp = min;
                min = max;
                max = temp;
            }
            this.min = min;
            this.max = max;
        }

        /**
         * Returns the minimum value of the interval.
         */
        public float getMinimum()
        {
            return min;
        }
        
        /**
         * Returns the maximum value of the interval.
         */
        public float getMaximum()
        {
            return max;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.lang.Float nextValue(java.util.Random rng)
        {
            return (float)((rng.nextFloat() * (max - min)) + min);
        }
    }

    
    /**
     * Interval distribution with a double precision.
     */
    public static class Double extends IntervalDistribution<java.lang.Double>
    {
        private double min;
        private double max;

        /**
         * Creates a new IntervalDistribution.Double instance. 
         * The create distribution is a uniform distribution of all double
         * values between <i>min</i> (inclusive) and <i>max</i> (exclusive).
         * 
         * @param min
         *      minimum value of the interval (inclusive)
         * @param max
         *      maximum value of the interval (exclusive)
         */
        Double(double min, double max)
        {
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }
            this.min = min;
            this.max = max;
        }

        /**
         * Returns the minimum value (inclusive) of the interval of this
         * UniformDistribution.
         */
        public double getMinimum()
        {
            return min;
        }
        
        /**
         * Returns the maximum value (exclusive) of the interval of this
         * UniformDistribution.
         */
        public double getMaximum()
        {
            return max;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.lang.Double nextValue(java.util.Random rng)
        {
            return (rng.nextDouble() * (max - min)) + min;
        }
    }
    
    /**
     * Interval distribution with a int precision.
     */
    public static class Integer 
        extends IntervalDistribution<java.lang.Integer>
        implements DiscreteDistribution<java.lang.Integer>
    {
        private int min;
        private int max;

        /**
         * Creates a new IntervalDistribution.Integer instance.
         * The created distribution is a uniform distribution of all values
         * between <i>min</i> (inclusive) and <i>max</i> (exclusive).
         * 
         * @param min
         *      minimum value of the distribution (inclusive)
         * @param max
         *      maximum value of the distribution (exclusive)
         */
        public Integer(int min, int max)
        {
            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }
            this.min = min;
            this.max = max;
        }

        /**
         * Returns the minimum value (inclusive) of the interval of this
         * UniformDistribution.
         */
        public int getMinimum()
        {
            return min;
        }
        
        /**
         * Returns the maximum value (inclusive) of the interval of this
         * UniformDistribution.
         */
        public int getMaximum()
        {
            return max;
        }
        
        /**
         * Returns the set of values in this interval.
         */
        @Override
        public Set<java.lang.Integer> getElements()
        {
            return new IntervalSet(min, max);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.lang.Integer nextValue(java.util.Random rng)
        {
            return rng.nextInt(max - min) + min;
        }
    }
    
    
    /**
     * Creates a new uniform distribution of integers in the interval from
     * <i>min</i> (inclusive) to <i>max</i> (exclusive).
     * 
     * @param min
     *      minimum value of the interval (inclusive)
     * @param max
     *      maximum value of the interval (exclusive)
     * @return
     *      a uniform distribution of all integers in the interval from
     *      <i>min</i> (inclusive) to <i>max</i> (exclusive)
     */
    public static IntervalDistribution.Integer create(int min, int max)
    {
        return new Integer(min, max);
    }
    
    
    /**
     * Creates a new uniform distribution of floats in the interval from
     * <i>min</i> (inclusive) to <i>max</i> (exclusive).
     * 
     * @param min
     *      minimum value of the interval (inclusive)
     * @param max
     *      maximum value of the interval (exclusive)
     * @return
     *      a uniform distribution of all floats in the interval from
     *      <i>min</i> (inclusive) to <i>max</i> (exclusive)
     */
    public static IntervalDistribution.Float create(float min, float max)
    {
        return new Float(min, max);
    }
    
    
    /**
     * Creates a new uniform distribution of doubles in the interval from
     * <i>min</i> (inclusive) to <i>max</i> (exclusive).
     * 
     * @param min
     *      minimum value of the interval (inclusive)
     * @param max
     *      maximum value of the interval (exclusive)
     * @return
     *      a uniform distribution of all doubles in the interval from
     *      <i>min</i> (inclusive) to <i>max</i> (exclusive)
     */
    public static IntervalDistribution.Double create(double min, double max)
    {
        return new Double(min, max);
    }
}
