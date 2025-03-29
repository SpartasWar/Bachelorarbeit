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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.StringUtils;

/**
 * This class represents a hypergraph. A hypergraph consists of a finite number
 * of nodes and a finite number of edges. Each edge has a label and is connected
 * with a finite list of nodes.
 * <p>
 * This class manages the storage of nodes and edges and provides methods to
 * determine some properties of graphs and other utility methods.
 * Subclasses may provide additional structure to the graph.
 */
public class Graph extends NamedObject implements Cloneable
{
    private static final String EXC_N_NEGATIVE          = "n must be >= 0.";
    private static final String EXC_EDGE_NULL           = "edge cannot be null.";
    private static final String EXC_NODE_NULL           = "node cannot be null.";
    private static final String EXC_EDGES_NULL          = "edges cannot be null.";
    private static final String EXC_NODES_NULL          = "nodes cannot be null.";
    private static final String EXC_EDGE_NOT_IN_GRAPH   = "Edge is not in this graph.";
    private static final String EXC_NODE_NOT_IN_GRAPH   = "Node is not in this graph.";
    
    /** The id for the next node created in this graph. */
    protected int nextNodeId = 0;
    
	/** The number of times one or more nodes or edges were removed from this
        graph. */
    private long delCount = 0;
    
    /** The number of times one or more nodes or edges were added to this
        graph. */
    private long addCount = 0;
    
    /** The nodes in this graph. */
    protected List<Node> nodes;
    
    /** The collection of edges in this graph. */
    protected Set<Edge> edges;
    
    /** Mapping between cloned graph elements. */
    protected Map<Node,Node> nodeMap;
    
    /** The isomorphism certificate of this graph. */
    protected int isoHash = 0;
    
    /**
     * Creates a new, empty graph.
     */
    @SideEffectFree
    public Graph()
    {
        nodes = new LinkedList<>();
        edges = new HashSet<>();
    }
    
    /**
     * Creates a new, empty graph with a given name.
     */
    @SideEffectFree
    public Graph(String name)
    {
        super(name);
        nodes = new LinkedList<>();
        edges = new HashSet<>();
    }
    
    /**
     * Creates a new graph which is a copy of another graph.
     * 
     * @param orig          
     *      the graph of which the new graph must be a copy
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public Graph(Graph orig)
    {
        super(orig);
        
        nodes = new LinkedList<>();
        edges = new HashSet<>();

        nodeMap = new HashMap<>();
        
        for (Node node : orig.getNodes()) {
            Node newNode = new Node(this, node.getId());
            nodes.add(newNode);
            nodeMap.put(node, newNode);
        }
        for (Edge edge : orig.getEdges()) {
            List<Node> ans = new ArrayList<>();
            for (Node n : edge.getNodes()) {
                assert (n.getGraph() == orig);
                Node an = nodeMap.get(n);
                assert an != null : "@AssumeAssertion(nullness)"; // by construction
                ans.add(an);
            }
            Edge newEdge = new Edge(this, edge.getLabel(), ans);
            edges.add(newEdge);
        }
        
        nextNodeId = orig.nextNodeId;
        isoHash = orig.isoHash;
    }
    
    /**
     * Returns a copy of this graph.
     */
    @SideEffectFree
    public Graph clone()
    {
        return new Graph(this);
    }
    
    /**
     * Increases the addition count of this graph. This method must always
     * be called after one or more nodes and edges are added to this graph. 
     */
    protected final void increaseAddCount()
    {
        addCount++;
        isoHash = 0;
    }
    
    /**
     * Increases the deletion count of this graph. This method must always
     * be called after one or more nodes and edges are deleted from this graph.
     */
    protected final void increaseDelCount()
    {
        delCount++;
        isoHash = 0;
    }
    
    /**
     * Returns {@code true} if this graph is an empty graph.
     * A graph is empty if it does not contain any nodes or edges.
     * 
     * @return              
     *      {@code true} if this graph is empty, {@code false} otherwise
     */
    @Pure
    public final boolean isEmpty()
    {
        return (nodes.isEmpty() && edges.isEmpty());
    }
    
    /**
     * Returns the set of nodes of this graph.
     * The returned set is unmodifiable.
     * 
     * @return
     *      the list of nodes of this graph
     */
    @SideEffectFree
    public final List<Node> getNodes()
    {
        return Collections.unmodifiableList(nodes);
    }
    
    /** 
     * Determines the set of isolated nodes of this graph.
     * An isolated node is a node which is incident to no edge.
     * <p>
     * The returned {@code Set} is not part of the internal structure of this
     * graph, and can be modified by the caller.
     * 
     * @return              
     *      a set consisting of all isolated nodes of this graph
     */
    @SideEffectFree
    public final Set<Node> getIsolatedNodes()
    {
        Set<Node> result = new HashSet<>(nodes);
        
        for (Edge edge : edges) {
            result.removeAll(edge.getNodes());
        }
        
        return result;
    }
    
    /**
     * Returns the edges of this graph.
     * The returned set is unmodifiable.
     * 
     * @return
     *      the set of edges of this graph
     */
    @SideEffectFree
    public final Set<Edge> getEdges()
    {
        return Collections.unmodifiableSet(edges);
    }
    
    /**
     * Adds a node to this graph.
     * 
     * @return              
     *      the {@code Node} object which was added to this graph
     */
    public Node addNode()
    {
        Node node = new Node(this, nextNodeId++);
        nodes.add(node);
        increaseAddCount();
        return node;
    }
    
    /**
     * Adds a number of nodes to this graph. This method adds a user-specified
     * number of nodes to the graph and returns an array which contains 
     * exactly the nodes which were added to the graph.
     * <p>
     * <strong>Note:</strong> the parameter <i>n</i> may be zero, in which case
     * no nodes are added to the graph and an empty array is returned. 
     * This method <i>never</i> returns <i>null</i>.
     * 
     * @param n
     *      the number of nodes to add to this graph
     * @return
     *      array of the nodes which were added to this graph
     * @throws IllegalArgumentException
     *      if {@code n < 0}
     */
    public Node[] addNodes(int n)
    {
        if (n < 0) throw new IllegalArgumentException(EXC_N_NEGATIVE);
        
        Node[] newNodes = new Node[n];
        if (n == 0) return newNodes;
        
        for (int i = 0; i < n; i++) {
            newNodes[i] = new Node(this, nextNodeId++);
            nodes.add(newNodes[i]);
        }
        
        increaseAddCount();
        return newNodes;
    }
    
    /**
     * Adds a node to this graph with a given id number.
     * The caller must make sure that each id number occurs at most once in
     * a graph.
     * 
     * @param id
     *      id of the new node
     */
    Node addNode(int id)
    {
        if (id >= nextNodeId) {
            nextNodeId = id + 1;
        }
        Node node = new Node(this, id);
        nodes.add(node);
        increaseAddCount();
        return node;
    }

    /**
     * Adds a node to this graph with a given id number.
     * The caller must make sure that each id number occurs at most once in
     * a graph.
     * 
     * @param id
     *      id of the new node
     */
    public Node addNodeWithId(int id)
    {
    	for (Node n : nodes) {
    		if (n.getId() == id) {
    			throw new IllegalArgumentException("Node id " + id + " already exists in this graph");
    		}
    	}
    	return addNode(id);
    }

    /**
     * Removes a node and all edges that are incident to it from this graph.
     * <p>
     * If a subclass overrides this method, it is recommended to call this
     * overridden method (by {@code super.removeNode(node)}) 
     * 
     * @param node          
     *      the node which is to be removed
     * @return              
     *      {@code true} if this graph was modified by this operation
     * @throws NullPointerException
     *      if {@code node} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code node} does not belong to this Graph
     */
    public boolean removeNode(Node node)
    {
        if (node == null) 
            throw new NullPointerException(EXC_NODE_NULL);
        if (node.getGraph() != this)
            throw new IllegalArgumentException(EXC_NODE_NOT_IN_GRAPH);
        
        if (nodes.remove(node)) {
            for (Iterator<Edge> it = edges.iterator(); it.hasNext(); ) {
                Edge edge = it.next();
                if (edge.getNodes().contains(node)) {
                    edge.removeFromGraph();
                    it.remove();
                }
            }
            node.removeFromGraph();
            increaseDelCount();
            return true;
        }

        return false;
    }

    /**
     * Adds a new edge to this graph.
     * This method creates an new Edge object with the specified properties
     * and add it to the edge set of this graph.
     * 
     * @param label         
     *      the label of the new graph
     * @param nodes         
     *      the nodes to which the new edge is adjacent
     * @return              
     *      the edge which was added to this graph
     * @throws NullPointerException
     *      if {@code label} is {@code null}
     */
    public Edge addEdge(Label label, Node... nodes)
    {
        if (label == null) throw new NullPointerException();
        
        List<Node> ns = new ArrayList<>();
        if (nodes != null) {
            for (Node n : nodes) {
                ns.add(n);
            }
        }
        Edge e = new Edge(this, label, ns);
        edges.add(e);
        increaseAddCount();
        return e;
    }
    
    /**
     * Adds a new edge to this graph.
     * This method creates an new Edge object with the specified properties
     * and add it to the edge set of this graph.
     * 
     * @param label         
     *      the label of the new graph
     * @param nodes         
     *      the nodes to which the new edge is adjacent
     * @return             
     *      the edge which was added to this graph
     * @throws NullPointerException
     *      if {@code label} or {@code nodes} is {@code null}
     */
    public Edge addEdge(Label label, List<Node> nodes)
    {
        if (label == null || nodes == null) throw new NullPointerException();
        
        List<Node> ns = new ArrayList<>(nodes);
        Edge e = new Edge(this, label, ns);
        edges.add(e);
        increaseAddCount();
        return e;
    }
    
    /**
     * Removes a single edge from this graph.
     * <p>
     * If a subclass override this method, it is recommended to call this
     * overridden method by using {@code super.removeEdge(edge)}.
     * 
     * @param edge          
     *      the edge which is to be removed from the graph
     * @returns
     *      {@code true} if the graph was modified because of this action
     * @throws NullPointerException
     *      if {@code edge} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code edge} does not belong to this graph.
     */
    public void removeEdge(Edge edge)
    {
        if (edge == null) 
            throw new IllegalArgumentException(EXC_EDGE_NULL);
        if (edge.getGraph() != this)
            throw new IllegalArgumentException(EXC_EDGE_NOT_IN_GRAPH);
        
        if (edges.remove(edge)) {
            edge.removeFromGraph();
            increaseDelCount();
        }
    }
    
    /**
     * Merge a number of nodes in the graph.
     * <p>
     * This method calls {@link #mergeNodes(Collection, Node) mergeNodes(nodes, target)}, 
     * where {@code target} is an arbitrary node from the collection 
     * {@code nodes}.
     * Subclasses which need to perform custom merging should override 
     * {@code mergeNodes(Collection<Node>, Node)}.  
     * <p>
     * <strong>Warning:</strong> Morphisms which have this graph as domain or
     * codomain will still be (partial) morphisms after the completion of this
     * method, but it is unspecified what mappings of the morphism remain.   
     * 
     * @param nodes         
     *      collection of nodes which must be merged
     * @throws NullPointerException
     *      if {@code nodes} is {@code null}
     */
    public final void mergeNodes(Collection<Node> nodes) 
    {
        if (nodes.size() <= 1) return; // nothing to do
        Node target = nodes.iterator().next(); // get an arbitrary node from nodes
        mergeNodes(nodes, target);
    }

    /**
     * Merge nodes in this graph.
     * <p>
     * This method calls {@link #mergeNodes(Collection, Node) mergeNodes(nodes, target)}, 
     * where {@code target} is an arbitrary node from the array {@code nodes}.
     * Subclasses which need to perform custom merging should override 
     * {@code mergeNodes(Collection<Node>, Node)}.  
     * <p> 
     * <strong>Warning:</strong> Morphisms which have this graph as domain or
     * codomain will still be morphisms after the completion of this method,
     * but it is unspecified what mappings of the morphism remain.     
     * 
     * @param nodes         
     *      the nodes to be merged
     */
    public final void mergeNodes(Node... nodes)
    {
        Set<Node> nodeSet = new HashSet<>();
        for (Node n : nodes)
            nodeSet.add(n);
        mergeNodes(nodeSet);
    }

    /**
     * Merges a number of nodes in the graph.
     * <p>
     * Subclasses in which merging needs additional work, should override
     * this method. 
     * <p>
     * Typically, {@code target} is an element of {@code nodes}, although
     * that is not required for this method, and should neither be required
     * by methods that override it.
     * 
     * @param mergedNodes         
     *      collection of nodes which must be merged
     * @param target        
     *      the node into which all nodes will be merged
     * @throws NullPointerException
     *      if {@code mergedNodes} or {@code target} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code target} does not belong to this graph
     */
    protected void mergeNodes(Collection<Node> mergedNodes, Node target)
    {
        if (target.getGraph() != this) 
            throw new IllegalArgumentException();
        
        if (mergedNodes.size() <= 1) return; // nothing to do
                
        List<Edge> newEdges = new LinkedList<>();
        for (Iterator<Edge> it = edges.iterator(); it.hasNext(); ) {
            Edge e = it.next();
            
            if (!Collections.disjoint(e.getNodes(), mergedNodes)) {
                e.removeFromGraph();
                it.remove();
                List<Node> an = new ArrayList<>();
                for (Node n : e.getNodes()) {
                    if (mergedNodes.contains(n)) 
                        an.add(target);
                    else
                        an.add(n);
                }
                newEdges.add(new Edge(this, e.getLabel(), an));
            }
        }
        
        for (Node node : mergedNodes) {
            if (node != target) {
                node.removeFromGraph();
                nodes.remove(node);
            }
        }
        
        edges.addAll(newEdges);    
        increaseAddCount();
        increaseDelCount();
    }
    
    /**
     * Determines if this graph is a binary graph. 
     * A graph is binary, if all of its edges are incident to exactly two
     * nodes.

     * @return              
     *      {@code true} if this graph is binary, {@code false} otherwise
     */
    @Pure
    public final boolean isBinary()
    {
        for (Edge e : edges) {
            if (e.getNodes().size() != 2) 
                return false;
        }
        return true;
    }
    
    /**
     * Determines if this graph is discrete.
     * A graph is discrete if it does not contain any edges.
     * 
     * @return              
     *      {@code true} if this graph is discrete, {@code false} otherwise
     */
    @Pure
    public final boolean isDiscrete()
    {
        return edges.isEmpty();
    }
    
    /**
     * Returns the modification count of this Graph.
     * The modification count is the number of times the graph was structurally
     * modified since it was created. 
     * (In a single modification, any number of nodes or edges may be added
     * or removed.) 
     * Some classes use the modification count to verify that a graph has 
     * not been modified within a certain time frame.
     * 
     * @return              
     *      the modification count of this Graph
     */
    @Pure
    public final long getModCount()
    {
        return addCount + delCount;
    }    
    
    /**
     * Returns the deletion count of this Graph.
     * The deletion count of a graph is the number of times nodes or edges were
     * deleted from it since its creation. 
     * By comparing the value returned by this method with the stored return
     * value at an earlier time it can be detected whether nodes or edges were\
     * deleted from this graph.
     * <p>
     * In a single delete action any number of nodes or edges may be deleted, 
     * that is, the deletion count is <em>not</em> equal to the number of nodes
     * or edges deleted from this Graph since its creation.
     */
    @Pure
    public final long getDelCount()
    {
        return delCount;
    }
    
    /**
     * Returns the addition count of this Graph.
     * The addition count of a graph is the number of times nodes or edges were
     * added to it since its creation. 
     * By comparing the value returned by this method with the stored return
     * value at an earlier time it can be detected whether nodes or edges were
     * added this graph.
     * <p>
     * In a single addition any number of nodes or edges may be added, 
     * that is, the add count is <em>not</em> equal to the number of nodes
     * or edges added to this Graph since its creation.
     */
    @Pure
    public final long getAddCount()
    {
        return addCount;
    }
    
    /**
     * Returns an "isomorphism certificate" for this graph.
     * If two graphs {@code graph1} and {@code graph2} have different 
     * isomorphism certificates, then they cannot be isomorphismic. 
     * (The converse does not hold: non-isomorphic graphs may have the same 
     * isomorphism certificate.)
     * 
     * @return              
     *      isomorphism certificate of this graph
     */
    @Pure
    public int getIsoHash()
    {  
        if (isoHash == 0) {
            int[] nodeArs = new int[10];
            for (int i = 0; i < nodeArs.length; i++) {
                nodeArs[i] = 0;
            }
            for (Node node : nodes) {
                int ie = node.incidentEdges;
                if (ie >= nodeArs.length) ie = nodeArs.length - 1;
                nodeArs[ie]++;
            }
            int sizeHash = nodes.size() << 10 + edges.size(); // FIXME: this should be (_ << _) + _
            int nodeArityHash = 0;
            for (int i = 0; i < nodeArs.length; i++) {
                nodeArityHash = 3 * nodeArityHash + nodeArs[i];
            }
            int edgeHash = 0;
            for (Edge e : edges) 
                edgeHash ^= e.getIsoHash();
            isoHash = sizeHash * 33;
            isoHash = isoHash * 33 + nodeArityHash;
            isoHash = isoHash * 33 + edgeHash;
        }
        return isoHash; 
    }
    
    /**
     * Returns the signature of this graph. The signature is the set of all
     * labels occurring in this graph.
     * <p>
     * The returned set is a newly created, modifiable set. Modifications to
     * the set are not reflected in this graph.
     * 
     * @return
     *      the set of all labels occurring in this graph
     */
    @SideEffectFree
    public final Set<Label> getSignature()
    {
        Set<Label> sig = new HashSet<>();
        for (Edge e : edges) {
            sig.add(e.getLabel());
        }
        return sig;
    }
    
    /**
     * Returns a string representation of this graph with a user-defined 
     * indentation string. The indentation string is written before each line
     * of the string representation. 
     * 
     * @param indent
     *      indentation string of the string representation; if {@code null},
     *      {@code "NULL"} will be used as the indentation string
     * @return
     *      string representation of this graph using the given indentation
     *      string
     */
    @Pure
    public String toString(String indent)
    {
        String fmt = 
                "%1$sGraph {\n" +
                "%1$s  Nodes: %2$s\n" +
                "%1$s  Edges:%4$s" + 
                "%1$s    %3$s\n" +
                "%1$s}";
        return String.format(fmt,
                indent,
                StringUtils.listString(nodes, ", "),
                StringUtils.listString(edges, ",\n" + indent + "    "),
                edges.isEmpty() ? "" : "\n");
    }
    
    /**
     * Returns a string representation of this graph. This implementation
     * calls {@link #toString(String)} with an empty parameters; subclasses
     * may have different behavior.
     * 
     * @return
     *      string representation of this Graph
     */
    @Override@Pure
    public String toString()
    {
        return toString("");
    }
    
    /**
     * Returns an injective morphism whose image is a subgraph of this graph.
     * The image of the returned morphism consists of the edges of 
     * {@code edges}, the nodes of {@code nodes} and the nodes incident to edges
     * of {@code edges}. The domain of the returned morphism is a new graph,
     * the codomain is this graph.
     * 
     * @param nodes         
     *      nodes of the image of the inclusion; this parameter may be 
     *      {@code null}
     * @param edges         
     *      edges of the image of the inclusion
     * @return              
     *      an injective morphism from a new graph to this graph the image of
     *      which consists of the nodes and edges in {@code nodes} and 
     *      {@code edges}
     * @throws IllegalArgumentException 
     *      if one the nodes or edges in {@code nodes} or {@code edges} belong 
     *      to a different graph than this graph
     * @throws NullPointerException
     *      if {@code edges} is {@code null} or if {@code nodes} or 
     *      {@code edges} contains a {@code null} element
     */
    @SideEffectFree
    public Morphism getInclusion(
            @Nullable Collection<Node> nodes, 
            Collection<Edge> edges)
    {
        if (edges == null) throw new NullPointerException(EXC_EDGES_NULL);
        
        Set<Node> allNodes = new HashSet<>();
        if (nodes != null) {
            allNodes.addAll(nodes);
        }
        for (Edge e : edges) {
            allNodes.addAll(e.getNodes());
        }
        
        Graph sub = new Graph();
        Map<Node,Node> nodeMap = new HashMap<>();
        Map<Node,Node> nodeRevMap = new HashMap<>();
        Map<Edge,Edge> edgeMap = new HashMap<>();
        
        for (Node tgtNode : allNodes) {
            if (tgtNode.getGraph() != this)
                throw new IllegalArgumentException(EXC_NODE_NOT_IN_GRAPH);
            Node srcNode = sub.addNode(tgtNode.getId());
            nodeMap.put(srcNode, tgtNode);
            nodeRevMap.put(tgtNode, srcNode);
        }
        
        for (Edge tgtEdge : edges) {
            if (tgtEdge.getGraph() != this)
                throw new IllegalArgumentException(EXC_EDGE_NOT_IN_GRAPH);
            List<Node> tgtIN = tgtEdge.getNodes();
            List<Node> srcIN = new ArrayList<>();
            for (Node n : tgtIN) {
                Node an = nodeRevMap.get(n);
                assert an != null : "@AssumeAssertion(nullness)";
                srcIN.add(an);
            }
            Edge srcEdge = sub.addEdge(tgtEdge.getLabel(), srcIN);
            edgeMap.put(srcEdge, tgtEdge);
        }
        
        return new Morphism(sub, this, nodeMap, edgeMap);
    }
    
    /**
     * Returns an injective morphism whose image is a subgraph of this graph.
     * The image of the returned morphism consists of the edges of 
     * {@code edges} and the nodes which are incident to edges
     * of {@code edges}. The domain of the returned morphism is a new graph,
     * the codomain is this graph.
     * <p>
     * A method call {@code graph.getInclusion(edges)} is equivalent to
     * {@code graph.getInclusion(null, edges)}.
     * 
     * @param edges         
     *      edges of the image of the inclusion
     * @return              
     *      an injective morphism from a new graph to this graph the image of 
     *      which consists of the edges in {@code edges} and the nodes incident
     *      to them
     * @throws NullPointerException
     *      if {@code edges} is {@code null}
     * @throws IllegalArgumentException 
     *      if one of the edges in {@code edges} belongs to a different graph 
     *      than this graph
     */
    @SideEffectFree
    public Morphism getInclusion(Collection<Edge> edges)
    {
        return getInclusion(null, edges);
    }

    /**
     * Returns an injective morphism that has this graph as domain, whose 
     * image is an induced subgraph of this graph. 
     * The image of the returned morphism consists of the nodes in {@code nodes}
     * and the edges which are only incident to nodes in {@code nodes}.
     * 
     * @param nodes         
     *      the nodes of the image of the inclusion
     * @return            
     *      an injective morphism whose image contains all nodes in 
     *      {@code nodes} and all edges which are only incident to nodes in 
     *      {@code nodes}
     * @throws NullPointerException
     *      when {@code nodes} is {@code null} 
     * @throws IllegalArgumentException 
     *      if one of the nodes in {@code nodes} belong to a different graph 
     *      than this graph.
     */
    @SideEffectFree
    public Morphism getInducedInclusion(@Nullable Collection<Node> nodes) 
    {
        if (nodes == null) throw new NullPointerException(EXC_NODES_NULL);
        
        List<Edge> indEdges = new LinkedList<>();
        
        for (Edge edge : edges) {
            if (nodes.containsAll(edge.getNodes()))
                indEdges.add(edge);
        }
        
        return getInclusion(nodes, indEdges);
    }
    
    /**
     * Calculates the weakly connected closure of a collection of nodes.
     * The weakly connected closure of a collection of nodes consists of all
     * nodes which can be reached from a node in the collection, where edges
     * can be traversed from any port to any other.
     * 
     * @param nodes
     *      the collection of nodes of which to calculate the weakly connected
     *      closure
     * @return
     *      the weakly connected closure of {@code nodes}
     * @throws NullPointerException
     *      if {@code nodes} is {@code null}
     */
    @SideEffectFree
    public Set<Node> weaklyConnectedClosure(Collection<Node> nodes) 
    {
        if (nodes == null) throw new NullPointerException(EXC_NODES_NULL);
        
        boolean changed;
        Set<Node> result = new HashSet<>(nodes);
        
        do {
            changed = false;
            for (Edge edge : edges) {
                if (!Collections.disjoint(edge.getNodes(), result)) {
                    changed |= result.addAll(edge.getNodes());
                }
            }
        } while (changed);
        
        return result;
    }
    
    /**
     * Calculates the weakly connected closure of a node.
     * The weakly connected closure of a node consists of all nodes which can 
     * be reached from it, where edges can be traversed from any port to any
     * other.
     * 
     * @param node
     *      the node of which to calculate the weakly connected closure
     * @return
     *      the weakly connected closure of {@code node}
     */
    @SideEffectFree
    public Set<Node> weaklyConnectedClosure(Node node) 
    {
        return weaklyConnectedClosure(Collections.singleton(node));
    }
    
    /**
     * Determines if this graph is isomorphic to another graph.
     * <p>
     * <strong>Note:</strong> The {@link Morphism} class defines more elaborate
     * methods to test isomorphism and obtain isomorphisms between graphs.
     * 
     * @param other
     *      the graph to check of, whether or not it is isomorphic to this
     *      graph
     * @return
     *      {@code true} if this graph is isomorphic to {@code other},
     *      {@code false} otherwise
     * @throws NullPointerException
     *      if {@code other} is {@code null}
     */
    @Pure
    public boolean isIsomorphic(Graph other)
    {
        if (other == null) {
            throw new NullPointerException();
        }
        if (other == this) { // prevent creating Morphism object
            return true;
        }
        else {
            return Morphism.getIsomorphism(this, other) != null;
        }
    }

    /**
     * Returns an enumerator which enumerates all inclusions into this graph.
     * An inclusion is an injective morphism from a subgraph to this graph.
     * 
     * @return
     *      an Enumerator enumerating all inclusions into this graph
     */
    public Enumerator<Morphism> getInclusions()
    {
        return new SubgraphEnumerator(this);
    }
    
    /**
     * Returns an Enumerator which enumerates all subgraphs of this graph.
     */
    public Enumerator<Graph> getSubgraphs()
    {
        return getInclusions().map((Morphism m) -> m.getDomain());
    }
    
    /**
	 * Returns the next node ID of this graph
	 * 
	 * @return 
	 * 			the nextNodeId
	 */
	public int getNextNodeId() {
		return nextNodeId;
	}
}