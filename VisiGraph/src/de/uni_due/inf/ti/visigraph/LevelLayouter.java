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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Layouter which lays out graphs in levels.
 * 
 * @author H.J. Sander Bruggink
 */
public class LevelLayouter extends NodeLayouter implements Layouter
{    
    private Direction direction;
    private double levelSkip;
    private double siblingSkip;
    
    
    /**
     * Creates a new LevelLayouter instance with custom direction and 
     * distance parameters.
     * 
     * @param direction
     *      direction of the layouts produced by this LevelLayouter
     * @param levelSkip
     *      distance of two levels (in pixels)
     * @param siblingSkip
     *      distance between siblings in the same level (in pixels)
     */
    public LevelLayouter(Direction direction, double levelSkip, double siblingSkip)
    {
        this.direction = direction;
        this.levelSkip = levelSkip;
        this.siblingSkip = siblingSkip;
    }
    
    
    /**
     * Creates a new LevelLayouter instance with a custom direction.
     * 
     * @param direction
     *      direction of the layouts produced by this LevelLayouter
     */
    public LevelLayouter(Direction direction)
    {
        this(direction, 100, 100);
    }
    
    
    /**
     * Creates a new LevelLayouter instance.
     */
    public LevelLayouter()
    {
        this(Direction.TOP_TO_BOTTOM, 100, 100);
    }
    
    
    /**
     * Returns the direction of the LevelLayouter.
     */
    public Direction getDirection()
    {
        return direction;
    }


    /**
     * Sets the direction of this LevelLayouter.
     */
    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }


    /**
     * Returns the distance of the baselines of two levels levels in this 
     * LevelLayouter.
     */
    public double getLevelSkip()
    {
        return levelSkip;
    }


    /**
     * Sets the distance of the baselines of two levels in this LevelLayouter.
     */
    public void setLevelSkip(double levelSkip)
    {
        this.levelSkip = levelSkip;
    }


    /**
     * Returns the distance between the center points of neighboring nodes in 
     * the same level.
     */
    public double getSiblingSkip()
    {
        return siblingSkip;
    }


    /**
     * Sets the distance between the center points of neighboring nodes in 
     * the same level.
     */
    public void setSiblingSkip(double siblingSkip)
    {
        this.siblingSkip = siblingSkip;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        long levelLong = Double.doubleToLongBits(levelSkip);
        long siblingLong = Double.doubleToLongBits(siblingSkip);
        
        int hash = 1;
        hash = 31 * hash + direction.hashCode();
        hash = 31 * hash + (int)(levelLong ^ (levelLong >>> 32));
        hash = 31 * hash + (int)(siblingLong ^ (siblingLong >>> 32));
        
        return hash;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof LevelLayouter)) {
            return false;
        }
        else {
            LevelLayouter oll = (LevelLayouter)o;
            return (oll.direction == direction && 
                    oll.levelSkip == levelSkip && 
                    oll.siblingSkip == siblingSkip);
        }
    }


    /**
     * Returns a list of start nodes for a group of a graph.
     * The start nodes are either:
     * <ul>
     * <li>all nodes of the graph which have in-degree 0; or
     * <li>if the graph contains no nodes with in-degree 0, one arbitrary node
     *     of the nodes with lowest in-degree.
     * </ul>
     * For the calculation of the in-degrees, only edges within the group are 
     * considered.
     * 
     * @param group
     *      group to find the start nodes of
     * @return
     *      the list of start nodes of the group
     */
    private static List<VxNode> findStartNodes(VxGroup group)
    {
        List<VxNode> result = new ArrayList<VxNode>();
        int leastInDegree = Integer.MAX_VALUE;
        
        for (VxNode node : group.getNodes()) {
            List<VxEdge> inEdges = node.inEdges;
            int inDegree = 0;
            for (VxEdge edge : inEdges) {
                if (group.contains(edge) && !edge.isLoop()) inDegree++;
            }
            if (leastInDegree == 0) {
                if (inDegree == 0) {
                    result.add(node);
                }
            }
            else if (inDegree < leastInDegree) {
                leastInDegree = inDegree;
                result.clear();
                result.add(node);
            }
        }

        // sort the list (group.getNodes gives no defined order)
        result.sort(Comparator.comparing(vxn -> vxn.getLabel()));

        return result;
    }
    

    /**
     * Converts a level and sibling-in-level pair to screen coordinates 
     * according to this LevelLayouter's properties.
     */
    private Point2D getNodePoint(int level, int index, int levelSize)
    {
        double lev = (double)level * levelSkip;
        double levelStart = -((double)levelSize * siblingSkip) / 2;
        double sib = levelStart + (siblingSkip / 2) + ((double)index * siblingSkip);
        
        switch (direction) {
            case LEFT_TO_RIGHT:
                return new Point2D.Double(lev, sib);
            case TOP_TO_BOTTOM:
                return new Point2D.Double(sib, lev);
            case RIGHT_TO_LEFT:
                return new Point2D.Double(-lev, sib);
            case BOTTOM_TO_TOP:
                return new Point2D.Double(sib, -lev);
            default:
                assert false;
                return null; // doesn't occur
        }
    }
    
    
    /**
     * Layouts the nodes of a group.
     * 
     * @param group
     *      the group to be layouted
     * @param useOther
     *      this parameter is ignored by this implementation
     */
    protected void layoutNodes(VxGroup group, boolean useOthers) 
    {
        VxGraph graph = group.getGraph();
        
        try {
            graph.startModification();

            List<VxNode> nodes = findStartNodes(group);
            Set<VxNode> placed = new HashSet<>();
            int level = 0;

            while (!nodes.isEmpty()) {            
                for (int i = 0; i < nodes.size(); i++) {
                    VxNode node = nodes.get(i);
                    if (node instanceof VxExplicitNode) {
                        VxExplicitNode eNode = (VxExplicitNode)node;
                        Point2D nodePoint = getNodePoint(level, i, nodes.size());
                        eNode.moveTo(nodePoint);
                    }
                    placed.add(node);
                }

                List<VxNode> newNodes = new ArrayList<>();
                for (VxNode node : nodes) {
                    for (VxEdge out : node.outEdges) {
                        VxNode other = out.getTarget();
                        if (!placed.contains(other) && group.contains(other)  
                                && !newNodes.contains(other)) {
                            newNodes.add(other);
                        }
                    }
                    for (VxEdge in : node.inEdges) {
                        VxNode other = in.getSource();
                        if (!placed.contains(other) && group.contains(other)  
                                && !newNodes.contains(other)) {
                            newNodes.add(other);
                        }
                    }
                }

                nodes = newNodes;
                level++;
            }
        }
        finally {
            graph.endModification();
        }
    }
}
