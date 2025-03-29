/* 
 * Copyright (c) 2013-2015, H.J. Sander Bruggink
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

package de.uni_due.inf.ti.visigraph;

import java.util.ArrayList;
import java.util.List;

import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.annotation.Nullable;

/**
 * This class is the superclass for all elements of VxGraphs.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class VxElement
{

    /** Graph to which this element belongs. */
    VxGraph graph;
    
    /** Reference object of this element. */
    private Object reference;
    
    /** Style name of this element. */
    private String style;
    
    /** DrawDelegates of this element. */
    private List<DrawDelegate<?>> delegates;
    
    /**
     * Creates a new VxElement instance.
     * 
     * @param graph
     *      graph to which the new element belongs
     */
    protected VxElement(@NonNull VxGraph graph)
    {
        this(graph, null);
    }
    
    /**
     * Creates a new VxElement instance.
     * 
     * @param graph
     *      graph to which the new element belongs
     * @param style
     *      style name of the new element, or {@code null} if the default style
     *      is to be used
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    protected VxElement(
            @NonNull VxGraph graph, 
            @Nullable String style)
    {
        if (graph == null) throw new NullPointerException();
        this.graph = graph;
        this.style = style;
        delegates = null;
    }
    
    /**
     * Returns the reference object of this element. The reference object is
     * a user-defined object which is used to establish a connection between
     * the graph and the underlying data. It is not used internally.
     * 
     * @return
     *      the reference object of this element
     */
    public final @Nullable Object getReference()
    {
        return reference;
    }
    
    /**
     * Sets the reference object of this element. The reference object is
     * a user-defined object which is used to establish a connection between
     * the graph and the underlying data. It is not used internally.
     * 
     * @param 
     *      the new reference object of this element, or {@code null} to remove
     *      the reference object
     */
    public void setReference(@Nullable Object reference)
    {
        this.reference = reference;
    }
    
    /**
     * Returns the style of this element.
     *
     * @return
     *      the style of this element, or {@code null} if the default style is
     *      used
     */
    public final @Nullable String getStyle()
    {
        return style;
    }
    
    /**
     * Sets the style name of this element.
     *
     * @param style
     *      the new style of this element, or {@code null} if the default
     *      style is to be used
     */
    public final void setStyle(@Nullable String style)
    {
        this.style = style;
    }
    
    /**
     * Returns the graph to which this element belongs.
     * If this element has already been removed from its graph, this method
     * returns {@code null}.
     */
    public final @Nullable VxGraph getGraph()
    {
        return graph;
    }
    
    /**
     * Registers a new DrawDelegate for this element.
     * 
     * @param delegate 
     *      the new delegate to add to this element's draw delegates
     * @throws NullPointerException
     *      if {@code delegate} is {@code null}
     */
    void addDelegate(@NonNull DrawDelegate<?> delegate)
    {
        if (delegate == null) throw new NullPointerException();
        if (delegates == null) delegates = new ArrayList<>();
        delegates.add(delegate);
    }
    
    /**
     * Unregisters a DrawDelegate for this element.
     */
    void removeDelegate(@NonNull DrawDelegate<?> delegate)
    {
        if (delegates != null) {
            delegates.remove(delegate);
        }
    }
    
    /**
     * Clears the cache information of all registered draw delegates of this
     * element.
     */
    protected void clearDelegateCaches()
    {
        if (delegates != null) {
            for (DrawDelegate<?> delegate : delegates) {
                delegate.clearCache();
            }
        }
    }
    
    /**
     * Notifies this element that an other element on which this element
     * depends has changed in a significant way. 
     * <p>
     * This default implementation does nothing. Subclasses may override this
     * method to notify elements that depend on this element and send necessary
     * event objects.
     */
    public void notifyModification()
    {
        // do nothing
    }
}