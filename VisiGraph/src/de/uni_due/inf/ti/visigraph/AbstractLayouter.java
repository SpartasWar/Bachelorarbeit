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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractLayouter implements Layouter
{
    public enum Direction {
        LEFT_TO_RIGHT,
        TOP_TO_BOTTOM,
        RIGHT_TO_LEFT,
        BOTTOM_TO_TOP
    }

    /** Direction in which groups are layouted. */
    private Direction groupDirection = Direction.LEFT_TO_RIGHT;
    
    /** Gap between groups. */
    private double gap = 100.0;
    
    /** Style information. */
    private GraphDrawer drawer = null;

    
    /**
     * Sets the gap which is left between two consecutive groups.
     */
    public void setGap(double gap)
    {
        this.gap = gap;
    }
    
    
    /**
     * Returns the horizontal gap which is left between two consecutive groups.
     */
    public double getGap()
    {
        return gap;
    }
    
    
    /**
     * Sets the direction in which a list of groups is layouted.
     */
    public void setGroupDirection(Direction dir)
    {
        groupDirection = dir;
    }
    

    /**
     * Returns the direction in which a list of groups is layouted.
     */
    public Direction getGroupDirection()
    {
        return groupDirection;
    }

    @Override
    public void setGraphDrawer(GraphDrawer drawer)
    {
        if (drawer == null) throw new NullPointerException();
        this.drawer = drawer;
    }

    @Override
    public GraphDrawer getGraphDrawer()
    {
        if (drawer == null) drawer = new GraphDrawer(); 
        return drawer;
    }
    
    
    /**
     * Layouts a graph. This method will call 
     * {@link #layoutGroup(VxGroup) layoutGroup(graph.getAllGroup())}.
     * Subclasses may override this method if this behavior is not wanted.
     * 
     * @param graph
     *      the graph to be layouted
     */
    public void layoutGraph(VxGraph graph)
    {
        layoutGroup(graph.getAllGroup(), false);
    }

    /**
     * Layouts a group of a graph. Subclasses must override this method.
     *
     * @param group
     *      group to be layouted
     * @param useOthers
     *      hint as to whether the nodes and edges which are not in the
     *      layouted group may be used to determine the positions of the nodes
     *      and edges which are in the group
     */
    @Override
    public abstract void layoutGroup(VxGroup group, boolean useOthers);

    /**
     * Layouts the edges of a group of a graph. Subclasses must override this method.
     *
     * @param group
     *      group to be layouted
     * @param useOthers
     *      hint as to whether the nodes and edges which are not in the
     *      layouted group may be used to determine the positions of the nodes
     *      and edges which are in the group
     */
    @Override
    public abstract void layoutEdgeGroup(VxGroup group, boolean useOthers);

    /**
     * Layouts a list of groups of a graph. This default implementation 
     * layouts all groups of the list separately and then positions them
     * from left to right next to each other.
     * 
     * @param groups
     *      the groups to be layouted
     * @throw IllegalArgumentException
     *      if one of the group belongs to a different graph than the others
     */
    @Override
    public void layoutGroups(VxGroup... groups)
    {
        layoutGroups(false, groups);
    }

	/**
	 * Layouts a list of groups of a graph. This default implementation
	 * layouts all groups of the list separately and then positions them
	 * from left to right next to each other.
	 *
	 * @param retainEmpty
	 *      whether empty groups should occupy space or should be ignored
	 * @param groups
	 *      the groups to be layouted
	 * @throw IllegalArgumentException
	 *      if one of the group belongs to a different graph than the others
	 */
	@Override
	public void layoutGroups(boolean retainEmpty, VxGroup... groups) {
		for (VxGroup g : groups) {
			layoutGroup(g, false);
		}
		arrangeGroups(retainEmpty, groups);
	}

	@Override
	public void arrangeGroups(boolean retainEmpty, VxGroup... groups) {
		double left = 0.0;
		double top = 0.0;

		GraphDrawer drawer = getGraphDrawer();

		for (VxGroup g : groups) {
			Rectangle2D bounds;
			if (drawer.getBounds(g) == null) {
				if (retainEmpty) {
					bounds = new Rectangle2D.Double();
				} else {
					continue;
				}
			} else {
				drawer.moveTo(g, new Point2D.Double(left, top));
				bounds = drawer.getBounds(g);
			}
			switch (groupDirection) {
			case LEFT_TO_RIGHT:
				left = bounds.getMaxX() + gap;
				break;
			case RIGHT_TO_LEFT:
				left = bounds.getMinX() - gap;
				break;
			case TOP_TO_BOTTOM:
				top = bounds.getMaxY() + gap;
				break;
			case BOTTOM_TO_TOP:
				top = bounds.getMinY() - gap;
				break;
			}
		}
	}

    /**
     * Gives a hint as to what the bounds of the produced graph layouts should
     * be. This default implementation does nothing. Subclasses who want to
     * consider maximal bounds should override this method.
     */
    public void setBounds(Rectangle2D bounds) 
    {
        // do nothing
    }
}
