/* 
 * Copyright (c) 2012-2014, H.J. Sander Bruggink,
 * Copyright (c) 2012-2014, University of Duisburg-Essen
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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * This class represents an edge label.
 * <p>
 * A label has a name and an arity. The name represents the text of the label,
 * while the arity is the number of nodes that must be incident to the label.
 * When the arity of a label is -1, this means that an edge labeled with
 * the label may be incident with any number of nodes.
 * <p>
 * Instances of this class are immutable.
 * 
 * @author H.J. Sander Bruggink
 */
public final class Label
{
    /** Label text of this label. */
    private String name;
    
    /** Arity of this label, that is, the number of nodes that must be
     *  incident to this label. */
    private int arity;
    
    /**
     * Creates a new label with a given name and arity.
     * 
     * @param name          
     *      the name of the new label
     * @param arity         
     *      the arity of the new label
     * @throws NullPointerException
     *      if {@code name} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code arity} is smaller than 0
     */
    @SideEffectFree
    public Label(String name, int arity)
    {
        if (arity < 0) 
            throw new IllegalArgumentException("Arity of a label must positive (>= 0).");
        if (name == null) 
            throw new NullPointerException("Label name must be non-null.");
        
        this.name = name;
        this.arity = arity;
    }
    
    /**
     * Create a new binary label.
     * The new label has a specified name and arity 2.
     * 
     * @param name
     *      the name of the new label
     * @throws NullPointerException
     *      if {@code name} is {@code null}
     */
    @SideEffectFree
    public Label(String name)
    {
        this(name, 2);
    }
    
    /**
     * Creates a new Label which is a copy of another one.
     * Since direct instances of Label are immutable, using this constructor
     * is rarely necessary. It is mainly provided for the sake of subclasses
     * with mutable state which provide a copy constructors.
     *  
     * @param orig
     *      the label that the new label is a copy of
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public Label(Label orig)
    {
        name = orig.name;
        arity = orig.arity;
    }
    
    /**
     * Returns the name of this label.
     */
    @Pure
    public final String getName()
    {
        return name;
    }
    
    /**
     * Returns the arity of this label.
     */
    @Pure
    public final int getArity()
    {
        return arity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public String toString() 
    {
        return String.format("%s:%d", name, arity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final int hashCode()
    {
        return name.hashCode() * 31 + arity;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final boolean equals(@Nullable Object o)
    {
        if (!(o instanceof Label))
            return false;
        if (this == o) 
            return true;
        
        Label l = (Label)o;
        
        return (name.equals(l.name) && arity == l.arity);
    }
}
