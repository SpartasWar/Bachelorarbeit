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
import java.util.LinkedList;
import java.util.List;

/**
 * This is convenience class to implement layouters.
 * Subclasses of this class layout a graph by determining positions for the
 * nodes. After the nodes have been placed, the edges' curves will be set 
 * according to a generic algorithm.
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class NodeLayouter extends AbstractLayouter implements Layouter
{
    public static enum EdgeMethod {
        OPTIMAL_CURVE,
        PARALLEL_CURVE,
        STRAIGHT_ONLY
    }
    
    /** Method for drawing edges. */
    private EdgeMethod edgeMethod = EdgeMethod.OPTIMAL_CURVE;
    
    /**
     * Layouts the nodes of a group.
     * 
     * @param group
     *      the group to layout the nodes of
     * @param useOthers
     *      hint as to whether nodes not in the group should be considered when
     *      calculating the layout for the nodes in the group; overriding
     *      methods need not consider this parameter
     */
    protected abstract void layoutNodes(VxGroup group, boolean useOthers);
    

    /**
     * Layouts edges between two different nodes according to the
     * "Parallel Curves" method.
     * 
     * @param cons
     *      edges to layout. All edges in the list must have the same source
     *      and target.
     */
    private static void layoutParallelEdges(List<VxEdge> cons)
    {
        switch (cons.size()) {         
            case 0:
                // nothing to do
                break;
            case 1: {
                VxEdge edge = cons.get(0);
                edge.setCurveOut(0, .3);
                edge.setCurveIn(0, .3);
                break;
            }
            default: {
                VxNode source = cons.get(0).getSource();
                
                double ta = Math.min((Math.PI / 6) + cons.size() * 
                        (Math.PI / 18), Math.PI / 2);
                double ea = ta / (cons.size() - 1);
                
                for (int i = 0; i < cons.size(); i++) {
                    VxEdge edge = cons.get(i);
                    double ofs = -(ta / 2) + (ea * (double)i);
                    if (edge.getSource() == source) {
                        edge.setCurveOut(ofs, .4);
                        edge.setCurveIn(-ofs, .4);
                    }
                    else {
                        edge.setCurveOut(-ofs, .4);
                        edge.setCurveIn(ofs, .4);
                    }
                }
            }
        }
    }
    

    /**
     * Layouts the edges of a group.
     */
    private void layoutEdges(VxGroup group) {
        layoutEdges(group, false);
    }

    /**
     * Layouts the edges of a group.
     */
    private void layoutEdges(VxGroup group, boolean useOthers)
    {
        VxGraph graph = group.getGraph();
        VxGroup placed = graph.createGroup(useOthers ? graph.getNodes() : group.getNodes());

        // first, layout the non-loops ...
        switch (edgeMethod) {
            case OPTIMAL_CURVE: {
                for (VxEdge edge : group.getEdges()) {
                    if (!edge.isLoop()) {
                        LayouterUtils.setToOptimalPath(getGraphDrawer(), edge, placed);
                        placed.add(edge);
                    }
                }
                break;
            }
            case PARALLEL_CURVE: {
                List<VxExplicitNode> nodes = new ArrayList<>(group.getNodes());
                final int size = nodes.size();
                for (int i = 0; i < size - 1; i++) {
                    VxNode iNode = nodes.get(i);
                    for (int j = i + 1; j < size; j++) {
                        VxNode jNode = nodes.get(j);
                        List<VxEdge> edges = new LinkedList<>(graph.getEdges(iNode, jNode));
                        edges.addAll(graph.getEdges(jNode, iNode));
                        //System.out.printf("%s - %s: %d%n", iNode.toString(), jNode.toString(), edges.size());
                        layoutParallelEdges(edges);
                    }
                }
                break;
            }
            case STRAIGHT_ONLY: {
                for (VxEdge edge : group.getEdges()) {
                    if (!edge.isLoop()) {
                        edge.setCurveIn(0, .4);
                        edge.setCurveOut(0, .4);
                    }
                }
            }
        }
        
        // ... and then the loops
        for (VxEdge edge : group.getEdges()) {
            if (edge.isLoop()) {
                LayouterUtils.setToOptimalPath(getGraphDrawer(), edge, placed);
                placed.add(edge);
            }
        }
    }
    
    
    @Override
    public void layoutGroup(VxGroup group, boolean useOthers)
    {
        VxGraph graph = group.getGraph();
        graph.startModification();
        try {
            layoutNodes(group, useOthers);
            layoutEdges(group);
        }
        finally {
            graph.endModification();
        }
    }

    @Override
    public void layoutEdgeGroup(VxGroup group, boolean useOthers)
    {
        VxGraph graph = group.getGraph();
        graph.startModification();
        try {
            layoutEdges(group, useOthers);
        }
        finally {
            graph.endModification();
        }
    }

    /**
     * Sets the algorithm for layouting the edges.
     * Three values are possible: 
     * <dl>
     * <dt>EdgeMethod.OPTIMAL_CURVE</dt>
     * <dd>The curve which intersects the least nodes is chosen; additionally,
     *     it is tried to layout edges in such a way, that no two edges are
     *     connected to the same node in the same position.</dd>
     * <dt>EdgeMethod.PARALLEL_CURVE</dt>
     * <dd>The placement of other nodes is not considered, but parallel edges
     *     are curved so that they do not overlap.</dd>
     * <dt>EdgeMethod.STRAIGHT_ONLY</dt>
     * <dd>Non-loops are drawn as straight lines only. Parallel edges will
     *     overlap each other.</dd>
     * </dl>
     *  
     * @param method
     *      method with which edge will be drawn
     */
    public void setEdgeLayoutMethod(EdgeMethod method)
    {
        edgeMethod = method;
    }
    
    
    /**
     * Returns the algorithm with which edges are layouted by this layouter.
     */
    public EdgeMethod getEdgeLayoutMethod()
    {
        return edgeMethod;
    }
}
