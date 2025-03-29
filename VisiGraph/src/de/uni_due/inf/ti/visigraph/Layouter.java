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

import java.awt.geom.Rectangle2D;

/**
 * Interface which represents objects which implement automatic graph layouters.
 * Graph layouters rearrange nodes and edges to create a good representation of
 * a VxGraph, or parts of it.
 *
 * <p>As the placement of elements may depend on their appearance (e.g. the
 * size of nodes influences how close they may be placed to each other), a
 * layouter has access to a {@link GraphDrawer} that provides style
 * information.
 * @author H.J. Sander Bruggink
 */
public interface Layouter
{
	/**
	 * Returns the GraphDrawer which provides the style and bound information
	 * for the graph layouted by this layouter.
	 *
	 * @return
	 *      the GraphDrawer used by this Layouter
	 */
	public GraphDrawer getGraphDrawer();

	/**
	 * Sets the GraphDrawer which provides the style and bound information for
	 * the graph layouted by this Layouter.
	 *
	 * @param drawer
	 *      the new GraphDrawer used by this Layouter
	 */
	public void setGraphDrawer(GraphDrawer drawer);

    /**
     * Layouts a graph.
     * 
     * @param graph
     *      the graph to be layouted
     */
    public void layoutGraph(VxGraph graph);
    
    /**
     * Layouts a group of a graph. 
     * 
     * @param group
     *      the group to be layouted
     * @param useOthers
     *      hint as to whether the nodes and edges which are not in the group
     *      may be used to determine the positions of the nodes and edges which
     *      are in the group; implementations need not consider this
     *      parameter
     */
    public void layoutGroup(VxGroup group, boolean useOthers);

    /**
     * Layouts the edges of a group of a graph.
     *
     * @param group
     *      group to be layouted
     * @param useOthers
     *      hint as to whether the nodes and edges which are not in the
     *      layouted group may be used to determine the positions of the nodes
     *      and edges which are in the group; implementations need not consider
     *      this parameter
     */
    public void layoutEdgeGroup(VxGroup group, boolean useOthers);

    /**
     * Layouts a list of group of a graph.
     * 
     * @param groups
     *      the group to be layouted
     * @throws IllegalArgumentException
     *      if one of the groups belongs to a different graph than the others
     */
    public void layoutGroups(VxGroup... groups);

    /**
     * Layouts a list of group of a graph.
     *
     * @param retainEmpty
     *      whether space for empty groups should be reserved
     * @param groups
     *      the group to be layouted
     * @throws IllegalArgumentException
     *      if one of the groups belongs to a different graph than the others
     */
    public void layoutGroups(boolean retainEmpty, VxGroup... groups);

    /**
     * Gives a hint as to what the bounds of the produced graph layouts
     * should be. Implementations need not consider this value.
     * 
     * @param bounds
     *      hint as to what the maximal size of the produced graph layouts is
     */
    public void setBounds(Rectangle2D bounds);

	/**
	 * Places the given groups next to each other. The elements inside each
	 * group are kept as they are.
	 * @param retainEmpty whether space for empty groups should be reserved
	 * @param groups the groups to be arranged
	 */
	public void arrangeGroups(boolean retainEmpty, VxGroup... groups);
}
