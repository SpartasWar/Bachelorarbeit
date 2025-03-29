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

/**
 * This is the abstract superclass of components of graphs, that is, nodes and
 * edges.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class VxComponent extends VxElement
{
    /** Box to which this component belongs. */
    VxBox box = null;  
    
    /**
     * Creates a new VxComponent instance.
     *
     * @param graph
     *      graph to which the new component belongs
     */
    protected VxComponent(VxGraph graph)
    {
        super(graph);
    }
    
    /**
     * Creates a new VxComponent instance.
     *
     * @param graph
     *      graph to which the new component belongs
     * @param style
     *      style of the new component
     */
    protected VxComponent(VxGraph graph, String style)
    {
        super(graph, style);
    }
    
    /**
     * Returns the box to which this component belongs. 
     * <p>
     * <strong>Remark.</strong> Components can be added to and removed from
     * boxes with the {@link VxBox#add(VxComponent) add(VxComponent)} and 
     * {@link VxBox#remove(VxComponent) remove(VxComponent)} methods of the 
     * {@link VxBox} class. 
     * 
     * @return
     *      the box to which this component belongs, or {@code null} if this
     *      component does not belong to any box
     */
    public VxBox getBox()
    {
        return box;
    }
}
