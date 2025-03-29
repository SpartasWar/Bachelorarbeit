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

package de.uni_due.inf.ti.graph.util;

import org.checkerframework.dataflow.qual.Pure;

/**
 * This enum represents a value in three-valued logic.
 * 
 * @author H.J. Sander Bruggink
 */
public enum ThreeBool {
    TRUE, FALSE, UNKNOWN;
    
    /**
     * Returns {@code true} if this is {@code ThreeBool.TRUE}. This method
     * is used to easily convert between booleans and ThreeBools.
     * 
     * @return
     *      {@code true} if this is {@code ThreeBool.TRUE}.
     */
    @Pure
    public boolean isTrue()
    {
        return (this == TRUE);
    }
    
    
    /**
     * Returns {@code true} is this is {@code ThreeBool.TRUE} or
     * {@code ThreeBool.UNKNOWN}. This method is used to easily convert 
     * between booleans and ThreeBools.
     * 
     * @return
     *      {@code true} if this ThreeBool is unequal to {@code ThreeBool.FALSE}
     */
    @Pure
    public boolean isNotFalse()
    {
        return (this != FALSE);
    }
    
    
    /**
     * Returns {@code ThreeBool.TRUE} if {@code b == true} and 
     * {@code ThreeBool.FALSE} if {@code b == false}. This method is used to
     * easily convert between booleans and ThreeBools.
     * 
     * @param b
     *      boolean to convert into a ThreeBool value
     * @return
     *      {@code ThreeBool.TRUE} if {@code b == true} and 
     *      {@code ThreeBool.FALSE} if {@code b == false}.
     */
    @Pure
    public static ThreeBool asThreeBool(boolean b)
    {
        return (b ? TRUE : FALSE);
    }
}
