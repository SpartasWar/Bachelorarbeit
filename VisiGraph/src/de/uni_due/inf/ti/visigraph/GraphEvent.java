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
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * An event that indicates that a graph was modified in some way.
 * 
 * @author H.J. Sander Bruggink
 */
public class GraphEvent extends EventObject
{
    private static final long serialVersionUID = 7095119104151746821L;

    private List<VxElement> elements;
    
    /**
     * Creates a new GraphEvent instance.
     * 
     * @param source
     *      graph which triggered the event
     * @param el
     *      element of the graph to which the event applies
     */
    GraphEvent(Object source, VxElement el)
    {
        super(source);
        elements = new ArrayList<>();
        elements.add(el);
    }
    
    /**
     * Creates a new GraphEvent instance. The caller should forget all 
     * references to the object referred to by {@code els}.
     * 
     * @param source
     *      graph which triggered the event
     * @param els
     *      elements of the graph to which the event applies
     */
    GraphEvent(Object source, List<VxElement> els)
    {
        super(source);
        elements = els;
    }
    
    /**
     * Returns the elements of the graph to which the event applies.
     */
    public List<VxElement> getElements()
    {
        return Collections.unmodifiableList(elements);
    }
}
