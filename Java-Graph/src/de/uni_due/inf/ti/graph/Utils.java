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

package de.uni_due.inf.ti.graph;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.base.Function;

class Utils
{
    private Utils() { }   // prohibit instantiating

    /**
     * Applies a function to each element of a list.
     * 
     * @param list
     *      the list to apply the function on
     * @param function
     *      function which is applied to each element of {@code list}
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public static <T> void map(List<T> list, Function<T,T> function) 
    {
        for (ListIterator<T> it = list.listIterator(); it.hasNext(); ) {
            T el = it.next();
            T el2 = function.apply(el);
            if (el != el2 && el2 != null) it.set(el2);
        }
    }
 
    /**
     * Applies a map to each element of a list.
     * 
     * @param list
     *      list to apply the map on
     * @param map
     *      map which is applied to each element of the list
     * @throws NullPointerException
     *      if one of the arguments is {@code null}
     */
    public static <T> void map(List<T> list, Map<T,T> map)
    {
        for (ListIterator<T> it = list.listIterator(); it.hasNext(); ) {
            T el = it.next();
            T el2 = map.get(el);
            if (el2 != null && el != el2) it.set(el2);
        }
    }
    
    /**
     * Increments an array of integers, as if it were a number to a certain
     * base.
     * 
     * @param digits        
     *      the array of integers which is to be incremented
     * @param max           
     *      the maximum number (exclusive), that is the base of the number
     * @return              
     *      {@code true} if the increment caused an overflow, {@code false}
     *      otherwise
     */
    public static boolean next(int[] digits, int max) 
    {
        return next(digits, 0, max);
    }
    
    /**
     * Increments an array of integers, as if it were a number to a certain
     * base.
     * 
     * @param digits        
     *      the array of integers which is to be incremented
     * @param min
     *      the minimum number (inclusive)
     * @param max           
     *      the maximum number (exclusive)
     * @return              
     *      {@code true} if the increment caused an overflow, {@code false}
     *      otherwise
     */
    public static boolean next(int[] digits, int min, int max) 
    {
        for (int i = 0; i < digits.length; i++) {
            if (digits[i] < max - 1) {
                digits[i]++;
                return false;
            }
            else digits[i] = min;
        }
        return true;
    }
}
