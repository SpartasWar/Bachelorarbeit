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

/**
 * This class represents a normal (Gaussian) distribution.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class NormalDistribution<T extends Number> 
    implements Distribution<T>
{
    double average;
    double stdDev;

    
    /**
     * Returns a new NormalDistribution instance with a given average and
     * standard deviation.
     * 
     * @param average
     *      average of the distribution
     * @param stdDev
     *      standard deviation of the distribution
     */
    private NormalDistribution(double average, double stdDev)
    {
        this.average = average;
        this.stdDev = stdDev;
    }


    /**
     * Returns the average of this StandardDistribution.
     */
    public double getAverage()
    {
        return average;
    }


    /**
     * Returns the standard deviation of this StandardDistribution.
     */
    public double getStandardDeviation()
    {
        return stdDev;
    }
    
    
    /**
     * Normal distribution of double values.
     */
    public static class Double extends NormalDistribution<java.lang.Double> 
    {
        /**
         * Creates a new NormalDistribution.Double instance.
         * 
         * @param average
         *      average of the distribution
         * @param stdDev
         *      standard deviation of the distribution
         */
        public Double(double average, double stdDev)
        {
            super(average, stdDev);
        }

        /**
         * Returns the next double value in the distribution.
         */
        @Override
        public java.lang.Double nextValue(java.util.Random rng)
        {
            return (rng.nextGaussian() * stdDev) + average;
        }
    }
    
    
    /**
     * Normal distribution of integer values. Although the normal distribution
     * returns normally distributed integer values, its average and
     * standard deviation are doubles.
     */
    public static class Integer extends NormalDistribution<java.lang.Integer> 
    {
        /**
         * Creates a new NormalDistribution.Integer instance.
         * 
         * @param average
         *      average of the distribution
         * @param stdDev
         *      standard deviation of the distribution
         */
        public Integer(double average, double stdDev)
        {
            super(average, stdDev);
        }

        /**
         * Returns the next Integer value in the distribution.
         */
        @Override
        public java.lang.Integer nextValue(java.util.Random rng)
        {
            return (int)Math.round((rng.nextGaussian() * stdDev) + average);
        }
    }
    
    
    /**
     * Normal distribution of long values. Although this distribution returns
     * normally distributed long values, its average and standard deviation
     * are doubles.
     */
    public static class Long extends NormalDistribution<java.lang.Long>
    {
        /**
         * Creates a new NormalDistribution.Long instance.
         * 
         * @param average
         *      average of the distribution
         * @param stdDev
         *      standard deviation of the distribution
         */
        public Long(double average, double stdDev)
        {
            super(average, stdDev);
        }
        
        /**
         * Returns the next Long value in the distribution.
         */
        @Override
        public java.lang.Long nextValue(java.util.Random rng)
        {
            return Math.round((rng.nextGaussian() * stdDev) + average);
        }
    }
}