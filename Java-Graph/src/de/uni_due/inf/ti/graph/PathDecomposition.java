/* 
 * Copyright (c) 2012-2014, H.J. Sander Bruggink
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * This class represents a path decomposition of a graph.
 * 
 * @author H.J. Sander Bruggink
 */
public class PathDecomposition
{
    /** The result of testing whether a PathDecomposition object is a legal
        path decomposition. */
    public enum TestResult {
        OK, NOT_CONNECTED, NODE_MISSING, EDGE_MISSING;
    }
    
    
    private List<Set<Node>> bags = new ArrayList<Set<Node>>();
    private int width = -1;
    
    
    /** 
     * Adds a new bag to the end of this PathDecomposition.
     * 
     * @param bag
     *      the bag to be added to this PathDecomposition
     * @throws NullPointerException
     *      if {@code bag} is {@code null}
     */
    public void addBag(Set<Node> bag) 
    {
        bags.add(bag);
        width = -1;
    }
    
    /**
     * Returns the bag at a specified index.
     * 
     * @param index
     *      the index of the bag to be returned
     * @return
     *      the bag which is at the given index in this PathDecomposition
     * @throws IndexOutOfBoundsException 
     *      if {@code index < 0} or {@code index >= number of bags}
     */
    @Pure
    public Set<Node> getBag(int index)
    {
        return bags.get(index);
    }
    
    /**
     * Returns the number of bags in this path decomposition.
     */
    @Pure
    public int getNumOfBags() 
    {
        return bags.size();
    }
    
    /**
     * Returns the width of this path decomposition.
     * The width of a path decomposition is the size of its largest bag 
     * minus 1.
     * 
     * @return
     *      the width of this path decomposition
     */
    @Pure
    public int getWidth() 
    {
        if (width < 0) {
            for (Set<Node> bag : bags) {
                int size = bag.size();
                if (size > width)
                    width = size;
            }
        }
        return width;
    }
    
    /**
     * Tests whether this PathDecomposition is a legal path decomposition
     * for a given graph.
     * <p>
     * This method returns a {@code TestData} object which describes the
     * result of the test. If this is a path decomposition of {@code graph},
     * {@code this.testGraph(graph).isOkay()} return {@code true}.
     * Otherwise the returned {@code TestData} contains information about
     * the reason that this is not a path decomposition for {@code graph}. 
     * 
     * @param graph
     *      the graph to test
     * @return
     *      a {@code TestData} object which describes the result of the test
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @SideEffectFree
    public TestData testGraph(Graph graph) 
    {
        if (graph == null) throw new NullPointerException();
        
        // test connected subpath condition
        Set<Node> seen = new HashSet<Node>();
        Set<Node> removed = new HashSet<Node>();
        
        for (Set<Node> bag : bags) {
            for (Node n : graph.getNodes()) {
                if (bag.contains(n)) {
                    if (removed.contains(n)) {
                        return TestData.newNotConnected(n);
                    }
                    seen.add(n);
                }
                else {
                    if (seen.contains(n)) {
                        removed.add(n);
                    }
                }
            }
        }
        
        // "removed" set can be garbage-collected
        removed = null;
        
        // test all nodes condition
        for (Node n : graph.getNodes()) {
            if (!seen.contains(n)) {
                return TestData.newNodeMissing(n);
            }
        }
        
        // "seen" set can be garbage-collected
        seen = null; 
        
        // test edges condition
        EDGE_FOR:
        for (Edge edge : graph.getEdges()) {
            for (Set<Node> bag : bags) {
                if (bag.containsAll(edge.getNodes())) 
                    continue EDGE_FOR;
            }
            return TestData.newEdgeMissing(edge);
        }
        
        // all tests succesful
        return TestData.newOk();
    }
    
    
// -------------------------------------------------------------------------- //
//                             TestData class                                 //
// -------------------------------------------------------------------------- //
    
    
    /**
     * This class represents the result of checking whether a path decomposition
     * is really a path decomposition of a graph.
     */
    public static class TestData 
    {
        private TestResult result;
        private @Nullable Node node;
        private @Nullable Edge edge;
        
        /**
         * Creates a new TestData instance.
         */
        @SideEffectFree
        private TestData(TestResult result)
        {
            this.result = result;
            node = null;
            edge = null;
        }
        
        /**
         * Creates a new TestData object which denotes success.
         * 
         * @return          a new TestData object
         */
        @SideEffectFree        
        static TestData newOk() 
        {
            return new TestData(TestResult.OK);
        }
        
        /**
         * Creates a new TestData object which denotes failure do to a node
         * occurring in disconnected subpaths.
         * 
         * @param node      the node occurring in disconnected subpaths
         * @return          a new TestData object
         */
        @SideEffectFree
        static TestData newNotConnected(Node node)
        {
            TestData r = new TestData(TestResult.NOT_CONNECTED);
            r.node = node;
            return r;
        }

        /**
         * Creates a new TestData object which denotes failure do to a node
         * occurring in none of the bags.
         * 
         * @param node      the node which does not occur in any of the bags
         * @return          a new TestData object
         */
        @SideEffectFree
        static TestData newNodeMissing(Node node)
        {
            TestData r = new TestData(TestResult.NODE_MISSING);
            r.node = node;
            return r;
        }
        
        /**
         * Creates a new TestData object which denotes failure do to an edge
         * of which the nodes are not contained together in any bag
         * 
         * @param edge      
         *      the edge of which the nodes are not contained together in 
         *      any bag
         * @return
         *      a new TestData object
         */
        @SideEffectFree
        static TestData newEdgeMissing(Edge edge)
        {
            TestData r = new TestData(TestResult.EDGE_MISSING);
            r.edge = edge;
            return r;
        }
        
        /**
         * Returns whether this TestData denotes success or failure, and which
         * kind of failure it denotes.
         * <p>
         * This method returns one of the following values:
         * <ul>
         * <li>{@code TestResult.OK} - success</li>
         * <li>{@code TestResult.NOT_CONNECTED} - failure; one node occurs in
         *     non-connected subpaths</li>
         * <li>{@code TestResult.EDGE_MISSING} - failure; there is an edge
         *     of which the nodes are not contained together in some bag</li>
         * <li>{@code TestResult.NODE_MISSING} - failure; there is a node
         *     which does not occur in some bag</li>
         * </ul>
         * 
         * @return
         *      success or kind of failure of this {@code TestData} object
         */
        @Pure
        public TestResult getResult() 
        {
            return result;
        }
        
        /**
         * Returns the node which caused failure.
         * This method returns {@code null} if this {@code TestData} object
         * denotes success or an error related to an edge.
         * 
         * @return
         *      the node which caused the failure denoted by this
         *      {@code TestData} object
         */
        @Pure
        public @Nullable Node getErrorNode() 
        {
            return node;
        }
        
        /**
         * Returns the edge which caused failure.
         * This method returns {@code null} if this {@code TestData} object
         * denotes success or an error related to a node.
         * 
         * @return
         *      the edge which caused the failure denoted by this
         *      {@code TestData} object
         */
        @Pure
        public @Nullable Edge getErrorEdge() 
        {
            return edge;
        }
        
        /**
         * Determines whether this {@code TestData} object denotes success.
         * 
         * @return
         *      {@code true} if this {@code TestData} object denotes success, 
         *      {@code false} otherwise
         */
        @Pure
        public boolean isOkay()
        {
            return result == TestResult.OK;
        }
    }   
}
