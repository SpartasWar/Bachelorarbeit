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

package de.uni_due.inf.ti.visigraph.swing;

import java.awt.ItemSelectable;

import de.uni_due.inf.ti.visigraph.VxGraph;

/**
 * A Swing component which displays VxGraph objects.
 * 
 * @author H.J. Sander Bruggink
 */
public class GraphPanel extends AbstractGraphPanel 
        implements ItemSelectable
{
    private static final long serialVersionUID = -5670660354728597752L;
    
    /**
     * Creates a new GraphPanel without a graph.
     */
    public GraphPanel() 
    {
        this(null);
    }
    
    /**
     * Creates a new GraphPanel.
     * 
     * @param graph
     *      the graph of this graph panel
     */
    public GraphPanel(VxGraph graph)
    {
        super(graph);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgesSelectable(boolean b)
    {
        super.setEdgesSelectable(b);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgesMovable(boolean b)
    {
        super.setEdgesMovable(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodesSelectable(boolean b)
    {
        super.setNodesSelectable(b);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodesMovable(boolean b)
    {
        super.setNodesMovable(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMultiSelect(boolean b)
    {
        super.setMultiSelect(b);
    }
}