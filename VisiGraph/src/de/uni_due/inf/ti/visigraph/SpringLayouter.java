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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_due.inf.ti.util.Partition;

/**
 * Layouter which layouts graphs by using a spring-based approach.
 * 
 * @author H.J. Sander Bruggink
 */
public class SpringLayouter extends NodeLayouter implements Layouter
{
    private static class NodeData
    {
        VxExplicitNode node;
        boolean fixed = false;
        double dx = 0;
        double dy = 0;
        
        NodeData(VxExplicitNode node) 
        {
            assert node != null;
            this.node = node;
        }
    }
    
    private static class ConnectionData
    {
        NodeData source;
        NodeData target;
        
        ConnectionData(NodeData source, NodeData target)
        {
            this.source = source;
            this.target = target;
        }
    }
    
    private int iterations              = 500;
    private boolean borderPassable      = true;
    private double pushStrength         = 7;
    private double pullStrength         = 13;
    private double breakThreshold       = 1;
    private double optimalEdgeLength    = 100;
    
    
    private static double updatePositions(Collection<NodeData> datas)
    {
        double totalDist = 0;
        
        for (NodeData data : datas) {
            if (!data.fixed) { 
                totalDist += Math.hypot(data.dx,  data.dy);
                Point2D pos = data.node.position();
                double x = pos.getX() + data.dx;
                double y = pos.getY() + data.dy;
                pos.setLocation(x, y);
                data.dx = 0;
                data.dy = 0;
            }
        }
        
        return totalDist;
    }
    
    
    private double repulsionForce(double x) 
    {
        double f = pushStrength / (x / optimalEdgeLength);
        if (Double.isNaN(f) || f > optimalEdgeLength / 2) {
            f = optimalEdgeLength / 2;
        }
        return f;
    }
    
    
    private double attractionForce(double x)
    {
        double f = pullStrength * Math.log(x / optimalEdgeLength) + pushStrength;
        if (Double.isNaN(f) || f < 0) {
            f = 0;
        }
        return f;
    }

    
    private void pushNodes(NodeData data1, NodeData data2)
    {
        double dx = data2.node.position().getX() - data1.node.position().getX();
        double dy = data2.node.position().getY() - data1.node.position().getY();
        
        if (dx == 0 && dy == 0) {
            double f = repulsionForce(0);
            data1.dx -= f;
            data2.dx += f;
        }
        else {
            double dist = Math.hypot(dx, dy);
            assert dist != 0;
            double f = repulsionForce(dist) / 2; // /2 because both nodes are moved
            double fx = f * (dx / dist);
            double fy = f * (dy / dist);
            data1.dx -= fx; 
            data1.dy -= fy;
            data2.dx += fx;
            data2.dy += fy;
        }
    }
    
    
    private void pullNodes(NodeData data1, NodeData data2) 
    {
        assert data1 != data2;
        double dx = data2.node.position().getX() - data1.node.position().getX();
        double dy = data2.node.position().getY() - data1.node.position().getY();
        if (dx != 0 && dy != 0) {
            double dist = Math.hypot(dx, dy);
            assert dist != 0;
            double f = attractionForce(dist) / 2; // /2 because both nodes are moved
            double fx = f * (dx / dist);
            double fy = f * (dy / dist);
            data1.dx += fx;
            data1.dy += fy;
            data2.dx -= fx;
            data2.dy -= fy;
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutNodes(VxGroup group, boolean useOthers)
    {
        if (group.getNodes().isEmpty()) return;
                
        // initialize nodes list
        Map<VxNode,NodeData> corr = new HashMap<>();
        List<NodeData> nodes = new ArrayList<>();
        Collection<VxExplicitNode> allNodes = 
                useOthers ? group.getGraph().getNodes() 
                          : group.getNodes();
                
        for (VxExplicitNode node : allNodes) {
            NodeData data = new NodeData(node);
            if (useOthers && !group.contains(node)) data.fixed = true;
            nodes.add(data);
            corr.put(node, data);
        }
        
        // initialize connection list
        List<ConnectionData> connections = new LinkedList<>();
        Partition<NodeData> part = new Partition<>();
        for (NodeData data : nodes) {
            part.makeSet(data);
        }
        for (VxNode source : allNodes) {
            for (VxEdge edge : source.outEdges) {
                VxNode target = edge.getTarget();
                if (target != source && (useOthers || allNodes.contains(target))) {
                    NodeData sourceData = corr.get(source);
                    NodeData targetData = corr.get(target);
                    ConnectionData data = new ConnectionData(sourceData, targetData);
                    connections.add(data);
                    part.union(sourceData, targetData);
                }
            }
        }
        
        // if there are unconnected components, connect them with "invisible"
        // connections
        Set<Set<NodeData>> components = part.getEquivalenceClasses();
        if (components.size() > 1) { 
            NodeData lastAnchor = null;
            for (Set<NodeData> cl : components) {
                assert !cl.isEmpty(); // partitions do not contain empty sets
                Iterator<NodeData> clIt = cl.iterator();
                NodeData thisAnchor = clIt.next();
                if (lastAnchor != null) {
                    ConnectionData data = new ConnectionData(lastAnchor, thisAnchor);
                    connections.add(data);
                    if (clIt.hasNext()) thisAnchor = clIt.next();
                }
                lastAnchor = thisAnchor;
            }
        }

        // iterate the optimization step
        for (int i = 0; i < iterations; i++) {
            // push nodes away from each other
            for (int n1 = 0; n1 < nodes.size() - 1; n1++) {
                for (int n2 = n1 + 1; n2 < nodes.size(); n2++) {
                    pushNodes(nodes.get(n1), nodes.get(n2));
                }
            }
            
            // pull connected nodes towards each other
            for (ConnectionData conn : connections) {
                pullNodes(conn.source, conn.target);
            }
            
            // move the nodes
            double totalDist = updatePositions(nodes);
            if (totalDist <= breakThreshold * nodes.size()) break;
        }
    }


    /**
     * Returns the maximal number of iterations of the spring layouter.
     */
    public int getIterations()
    {
        return iterations;
    }


    /**
     * Sets the maximal number of iterations of the spring layouter. 
     * A high number potentially produces better results, a low number 
     * produces results faster. The default value is 500.
     * 
     * @param iterations
     *      number of iterations of this spring layouter
     * @throws IllegalArgumentException
     *      if {@code iterations} is smaller than 0
     */
    public void setIterations(int iterations)
    {
        if (iterations < 0) throw new IllegalArgumentException();
        this.iterations = iterations;
    }


    /**
     * Returns the strength in which nodes are pushed away. 
     */
    public double getPushStrength()
    {
        return pushStrength;
    }


    /**
     * Sets the strength in which nodes are pushed away. The default value
     * is 0.7.
     * 
     * @param pushStrength
     *      the strength in which nodes are pushed away
     */
    public void setPushStrength(double pushStrength)
    {
        this.pushStrength = pushStrength;
    }


    /**
     * Returns the strength in which nodes are pulled together.
     */
    public double getPullStrength()
    {
        return pullStrength;
    }


    /**
     * Sets the strength in which nodes are pulled together. The default 
     * value is 0.4.
     * 
     * @param pullStrength
     *      the strength in which nodes are pulled together.
     */
    public void setPullStrength(double pullStrength)
    {
        this.pullStrength = pullStrength;
    }


    /**
     * Returns the break threshold of this spring layouter.
     */
    public double getBreakThreshold()
    {
        return breakThreshold;
    }


    /**
     * Sets the break threshold of this spring layouter. The break threshold 
     * is the number of pixels that nodes must move on average. As soon as 
     * soon as the average number of pixels that the nodes move in a single
     * iteration drops below the break threshold, the algorithm is terminated,
     * even if the maximum number of iterations has not been reached.
     * <p>
     * In general, a low break threshold will produce better results, but a
     * with a high break threshold the algorithm will produce results faster.
     * The default value is 0.5.
     *   
     * @param breakThreshold
     *      the break threshold of this spring layouter
     */
    public void setBreakThreshold(double breakThreshold)
    {
        this.breakThreshold = breakThreshold;
    }


    /**
     * Returns the optimal length of a single edge.
     */
    public double getOptimalEdgeLength()
    {
        return optimalEdgeLength;
    }


    /**
     * Sets the optimal length of a single edge.
     */
    public void setOptimalEdgeLength(double optimalEdgeLength)
    {
        this.optimalEdgeLength = optimalEdgeLength;
    }


    /**
     * Returns whether nodes can be pushed outside of the set bounds.
     */
    public boolean isBorderPassable()
    {
        return borderPassable;
    }


    /**
     * Sets whether nodes can be pushed outside of the set bounds.
     * If borderPassable is set to true, nodes can be pushed outside of the 
     * bounds; if borderPassable is set to false, nodes which are pushed outside
     * of the bounds are moved to the closest point on the bounds.
     * <p>
     * The default value is {@code true}. If no bounds are set, this 
     * parameter has no effect.
     * 
     * @param borderClips
     *      whether nodes can be pushed outside of the set bounds
     */
    public void setBorderPassable(boolean passable)
    {
        borderPassable = passable;
    }
}
