/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink 
 * Copyright (c) 2013-2014, University of Duisburg-Essen
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

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Common superclass for classes which have a single int attribute.
 * 
 * @author H.J. Sander Bruggink
 */
public class AttributeObject
{
    private int attribute;
    
    /**
     * Creates a new AttributeObject. The initial attribute of the new object
     * is set to 0.
     */
    @SideEffectFree
    public AttributeObject()
    {
        attribute = 0;
    }
    
    /**
     * Creates a new AttributeObject with a given attribute.
     * 
     * @param attribute
     *      the attribute of the new AttributeObject.
     */
    @SideEffectFree
    public AttributeObject(int attribute) 
    {
        this.attribute = attribute;
    }
    
    /**
     * Create a new AttributeObject which is a copy of another one.
     *
     * @param orig
     *      the AttributeObject of which the new one is a copy
     * @throws NullPointerException
     *      when {@code orig} is {@code null}
     */
    @SideEffectFree
    public AttributeObject(AttributeObject orig)
    {
        this.attribute = orig.attribute;
    }

    /**
     * Returns the attribute of this AttributeObject.
     */
    @Pure
    public final int getAttribute()
    {
        return attribute;
    }

    /**
     * Sets the attribute of this AttributeObject.
     */
    public final void setAttribute(int attribute)
    {
        this.attribute = attribute;
    }
}
