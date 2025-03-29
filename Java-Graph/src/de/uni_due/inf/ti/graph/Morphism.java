/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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

package de.uni_due.inf.ti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.Enumerators;
import de.uni_due.inf.ti.annotation.NonNull;
import de.uni_due.inf.ti.graph.util.ThreeBool;
import de.uni_due.inf.ti.util.Pair;
import de.uni_due.inf.ti.util.StringUtils;
import de.uni_due.inf.ti.util.Utilities;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * This class represents a (partial) morphism.
 * A morphism maps the nodes and edges of one graph (the domain) to the nodes
 * and edges of another graph (the codomain). Hereby, the following condition
 * is always satisfied:
 * <blockquote>
 *   if the edge {@code e1} is mapped to the edge {@code e2}, then 
 *   {@code e1} and {@code e2} have the same label, are incident to the same
 *   number of nodes, and the nodes which are incident to {@code e1} are 
 *   mapped, in order, to the nodes which are incident to {@code e2}. 
 * </blockquote>
 * It is not necessarily the case that <em>all</em> nodes and edges of the
 * domain are mapped to a node or edge of the codomain. The method 
 * {@link #isTotal()} can be used to check if this is the case.
 * <p>
 * It is also not necessarily the case that the morphism is injective, that is,
 * that different nodes and edges of the domain are mapped to different nodes
 * and edges of the codomain. This fact can be checked by the 
 * {@link #isInjective()} method.
 * <p>
 * Morphism instances check whether the domain and codomain graph of the
 * morphism are modified. When nodes or edges are removed from either the
 * domain or codomain graph, mappings containing these nodes and edges are
 * removed. This can result in a total morphism to become partial, a
 * non-injective morphism to become injective, etc. 
 */
public class Morphism implements Cloneable
{
    private static final String EXC_INVARIANT_VIOLATION = "Inconsistent node and edge maps.";
    private static final String EXC_OUTSIDE_NODE = "Morphism has node outside of domain or codomain.";
    private static final String EXC_OUTSIDE_EDGE = "Morphism has edge outside of domain or codomain.";
    private static final String EXC_ARITY = "Edges have different arity.";
    private static final String EXC_NOT_TOTAL = "Argument must be a total morphism.";
    private static final String EXC_DOMAIN_OR_CODOMAIN_NULL = "Domain and codomain may not be null.";
    private static final String EXC_GRAPH_NULL = "Graph cannot be null.";
    private static final String EXC_NULL_PARAM = "\"%s\" cannot be null";
    
    // Default visibility so that they can be modified by morphism enumerators
    // (which are defined in a separate class).
    
    /** Domain (source) graph of this Morphism. */
    Graph domain;
    
    /** Codomain (target) graph of this Morphism. */
    Graph codomain;
    
    /** Map mapping nodes of the domain to nodes of the codomain. */
    Map<Node,Node> nodeMap;
    
    /** Map mapping edges of the domain to edges of the codomain. */
    Map<Edge,Edge> edgeMap;

    /** Whether or not this Morphism is injective. ThreeBool.UNKNWON specifies
        that injectivity must still be determined. */
    ThreeBool injective = ThreeBool.UNKNOWN;

    /** Whether or not this Morphism is total. ThreeBool.UNKNWON specifies
    that totality must still be determined. */
    ThreeBool total = ThreeBool.UNKNOWN;
    
    /** Whether or not this Morphism is surjective. ThreeBool.UNKNOWN specifies
        that surjectivity must still be determined. */
    ThreeBool surjective = ThreeBool.UNKNOWN;    

    /** Last known deletion count of the domain graph (used to determine if
        elements were deleted from the domain. */
    long domainDelCount;

    /** Last known add count of the domain graph (used to determine if
        elements were added to the domain. */
    long domainAddCount;

    /** Last known deletion count of the codomain graph (used to determine if
        elements were deleted from the domain. */
    long codomainDelCount;

    /** Last known add count of the codomain graph (used to determine if
        elements were added to the codomain. */
    long codomainAddCount;
    
    /** Cached hash code of this Morphism. */
    private int hash = 0;
    
    
    /**
     * This class is returned by methods that need to return a new morphism
     * and information about how the nodes and edges of the domain and codomain
     * of the new morphism correspond to the nodes and edges of the domain and
     * codomain of this morphism. 
     */
    public static final class MorphismResult
    {
        private Morphism result;
        private Morphism domCorr;
        private Morphism codCorr;

        /**
         * Creates a new MorphismResult object.
         */
        @SideEffectFree
        MorphismResult(Morphism result, Morphism domCorr, Morphism codCorr)
        {
            this.result = result;
            this.domCorr = domCorr;
            this.codCorr = codCorr;
        }
        
        /**
         * Returns the result morphism.
         */
        @SideEffectFree
        public Morphism getResult()
        {
            return result;
        }

        /**
         * Returns the domain correspondence morphism. The codomain of this
         * morphism is the domain of the morphism returned by 
         * {@code getResult()}.
         * 
         * @return          
         *      domain correspondence morphism
         */
        @SideEffectFree
        public Morphism getDomainCorrespondence()
        {
            return domCorr;
        }
        
        /**
         * Returns the codomain correspondence morphism. The codomain of this
         * morphism is the codomain of the morphism returned by 
         * {@code getResult()}.
         * 
         * @return          
         *      codomain correspondence morphism
         */
        @SideEffectFree
        public Morphism getCodomainCorrespondence()
        {
            return codCorr;
        }
    }

    
    /**
     * Creates a new Morphism which is a copy of another one.
     * Later changes to the original Morphism do not reflect upon the newly
     * created one; however the new Morphism's domain and codomain are the
     * same as the domain and codomain of the original Morphism. 
     * 
     * @param orig          
     *      the Morphism the new Morphism is a copy of
     * @throws NullPointerException
     *      if {@code orig} is {@code null}
     */
    @SideEffectFree
    public Morphism(Morphism orig) 
    {
        this.domain = orig.domain;
        this.domainAddCount = orig.domainAddCount;
        this.domainDelCount = orig.domainDelCount;
        
        this.codomain = orig.codomain;
        this.codomainAddCount = orig.codomainAddCount;
        this.codomainDelCount = orig.codomainDelCount;
        
        nodeMap = new HashMap<>(orig.nodeMap);
        edgeMap = new HashMap<>(orig.edgeMap);
        
        injective = orig.injective;
        total = orig.total;
        surjective = orig.surjective;
    }
    
    /**
     * Creates a new empty Morphism.
     * 
     * @param domain        
     *      domain of the new morphism
     * @param codomain      
     *      codomain of the new morphism
     * @throws NullPointerException
     *      if {@code domain} or {@code domain} is {@code null}
     */
    @SideEffectFree
    public Morphism(Graph domain, Graph codomain) 
    {
        if (domain == null || codomain == null) 
            throw new NullPointerException(EXC_DOMAIN_OR_CODOMAIN_NULL);
        
        this.domain = domain;
        this.domainAddCount = domain.getAddCount();
        this.domainDelCount = domain.getDelCount();
        
        this.codomain = codomain;
        this.codomainAddCount = codomain.getAddCount();
        this.codomainDelCount = codomain.getDelCount();
        
        edgeMap = new HashMap<>();
        nodeMap = new HashMap<>();
    }
    
    /**
     * Creates a new morphism with a specified domain, codomain, node map and
     * edge map. 
     * <p>
     * This constructor should be used with care, for the following reasons.
     * <ul>
     * <li>The given node and edge maps are stored in the morphism as-is, no
     *     copy is made. This means that the caller should get rid of all 
     *     references to the maps after calling this constructor.
     * <li>It is not checked that the arguments are non-null. If any of the
     *     arguments is null, {@code NullPointerException} is thrown either
     *     immediately or later in the program.
     * <li>It is not verified that the morphism invariant holds. This means
     *     that the caller must make sure that the morphism invariant holds
     *     for the node and edge maps.
     * </ul>
     * The recommended method to construct new Morphism objects are the
     * static factory methods (named <tt>create*</tt>).
     * 
     * @param domain        
     *      domain of the new morphism
     * @param codomain      
     *      codomain of the new morphism
     * @param nodeMap       
     *      node map of the new morphism
     * @param edgeMap       
     *      edge map of the new morphism
     */
    @SideEffectFree
    Morphism(Graph domain, Graph codomain, Map<Node,Node> nodeMap, Map<Edge,Edge> edgeMap)
    {
        this.domain = domain;
        this.domainAddCount = domain.getModCount();
        this.domainDelCount = domain.getDelCount();
        
        this.codomain = codomain;
        this.codomainAddCount = codomain.getModCount();
        this.codomainDelCount = codomain.getDelCount();

        this.nodeMap = nodeMap;
        this.edgeMap = edgeMap;
    }
    
    /**
     * Returns a copy of this Morphism. In each invocation of this method,
     * a newly created object will be returned.
     * <p>
     * Later changes to the original Morphism do not reflect upon the newly
     * created one; however the new Morphism's domain and codomain are the
     * same as the domain and codomain of the original Morphism.
     * 
     * @return
     *      a copy of this Morphism
     */
    @Override@SideEffectFree
    public Morphism clone()
    {
        return new Morphism(this);
    }
    
    /**
     * If a node or edge was deleted from the domain or codomain graph, this
     * method removes all key-value-pairs of which the key is not part of the
     * domain anymore, or the value not part of the codomain.
     */
    @SideEffectFree // this method only changes non-visible state of this Morphism
    final void checkCoordinateDeletion()
    {
        boolean domDel = domainDelCount < domain.getDelCount();
        boolean codDel = codomainDelCount < codomain.getDelCount();
        
        if (domDel || codDel) {
            for (Iterator<Map.Entry<Node,Node>> it = nodeMap.entrySet().iterator(); 
                    it.hasNext(); ) {
                Map.Entry<Node,Node> entry = it.next();
                Node domNode = entry.getKey();
                Graph domGraph = domNode.getGraph();
                Node codNode = entry.getValue();
                Graph codGraph = codNode.getGraph();
                if (domGraph == null || codGraph == null) {
                    it.remove();
                    if (domGraph != null) total = ThreeBool.FALSE;
                    if (codGraph != null) surjective = ThreeBool.UNKNOWN;
                    if (injective == ThreeBool.FALSE) injective = ThreeBool.UNKNOWN;
                }
            }
            for (Iterator<Map.Entry<Edge,Edge>> it = edgeMap.entrySet().iterator(); 
                    it.hasNext(); ) {
                Map.Entry<Edge,Edge> entry = it.next();
                Edge domEdge = entry.getKey();
                Graph domGraph = domEdge.getGraph();
                Edge codEdge = entry.getValue();
                Graph codGraph = codEdge.getGraph();
                if (domGraph == null || codGraph == null) {
                    it.remove();
                    if (domGraph != null) total = ThreeBool.FALSE;
                    if (codGraph != null) surjective = ThreeBool.UNKNOWN;                    
                    if (injective == ThreeBool.FALSE) injective = ThreeBool.UNKNOWN;
                }
            }
            domainDelCount = domain.getDelCount();
            codomainDelCount = codomain.getDelCount();
            onModification();
        }
    }
    
    /**
     * If a node or edge was added to the domain, discard cached results that
     * are potentially invalidated by the addition.
     */
    private void checkDomainAddition()
    {
        if (domainAddCount < domain.getAddCount()) {
            total = ThreeBool.UNKNOWN;
            domainAddCount = domain.getAddCount();
        }
    }
    
    /**
     * If a node or edge was added to the codomain, discard cached results that
     * are potentially invalidated by the addition.
     */
    private void checkCodomainAddition()
    {
        if (codomainAddCount < codomain.getAddCount()) {
            surjective = ThreeBool.UNKNOWN;
            codomainAddCount = codomain.getAddCount();
        }
    }
    
    /**
     * Maps a node of the domain to a node of the codomain.
     * 
     * @param domNode       
     *      node in the domain graph
     * @param codNode       
     *      node in the codomain graph
     * @throws NullPointerException
     *      if {@code domNode} or {@code codNode} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code domNode} is not a node of the domain, {@code codNode} is 
     *      not a node of the codomain or mapping {@code domNode} to
     *      {@code codNode} would lead to an invariant violation
     */
    public void put(Node domNode, Node codNode) 
    {
        if (!checkAndPut(domNode, codNode))
            throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
    }

    /**
     * Checks whether {@code domNode} can be mapped to {@code codNode} and
     * maps {@code domNode} to {@code codNode} if possible.
     * 
     * @param domNode       
     *      node in the domain graph
     * @param codNode       
     *      node in the codomain graph
     * @return              
     *      {@code true} if {@code domNode} could be mapped to {@code codNode}, 
     *      {@code false} otherwise
     * @throws NullPointerException
     *      if {@code domNode} or {@code codNode} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code domNode} is not a node of the domain or {@code codNode}
     *      is not a node of the codomain.
     */
    public boolean checkAndPut(Node domNode, Node codNode) 
    {        
        if (domNode.getGraph() != domain) 
            throw new IllegalArgumentException(EXC_OUTSIDE_NODE);
        
        if (codNode.getGraph() != codomain)
            throw new IllegalArgumentException(EXC_OUTSIDE_NODE);
        
        checkCoordinateDeletion();

        Node oldCodNode = nodeMap.get(domNode);
        if (oldCodNode != null && oldCodNode != codNode) {
            for (Edge edge : domain.getEdges()) {
                if (edge.getNodes().contains(domNode))
                    return false;
            }            
        }
        
        nodeMap.put(domNode, codNode);
        
        if (total == ThreeBool.FALSE) total = ThreeBool.UNKNOWN; 
        if (injective == ThreeBool.TRUE) injective = ThreeBool.UNKNOWN;
        if (surjective == ThreeBool.FALSE) surjective = ThreeBool.UNKNOWN;
        hash = 0;
        
        onModification();
        
        return true;
    }
    
    /**
     * Maps an edge of the domain to an edge of the codomain.
     * Additionally, this method maps all nodes incident to {@code domEdge}
     * to the respective nodes of {@code codEdge}.
     * 
     * @param domEdge       
     *      edge of the domain graph
     * @param codEdge       
     *      edge of the codomain graph
     * @throws NullPointerException
     *      if {@code domEdge} or {@code codEdge} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code domEdge} is not an edge of the domain, {@code codEdge} is 
     *      not an edge of the codomain or mapping {@code domEdge} to 
     *      {@code codEdge} would lead to an invariant violation
     */
    public void put(Edge domEdge, Edge codEdge)
    {
        if (domEdge.getNodes().size() != codEdge.getNodes().size())
            throw new IllegalArgumentException(EXC_ARITY);
        
        if (!checkAndPut(domEdge, codEdge))
            throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
    }

	/**
	 * Checks whether {@code domEdge} can be mapped to {@code codEdge} and
	 * maps {@code domEdge} to {@code codEdge} if possible.
	 * 
	 * @param domEdge
	 *      edge in the domain graph
	 * @param codEdge
	 *      edge in the codomain graph
	 * @return
	 *      {@code true} if {@code domEdge} could be mapped to {@code codEdge},
	 *      {@code false} otherwise
	 * @throws NullPointerException
	 *      if {@code domEdge} or {@code codEdge} is {@code null}
	 * @throws IllegalArgumentException
	 *      if {@code domNode} is not an edge of the domain or {@code codEdge}
	 *      is not an edge of the codomain.
	 */
	public boolean checkAndPut(Edge domEdge, Edge codEdge) {
		return checkAndPut(domEdge, codEdge, true);
	}

	/**
	 * Checks whether {@code domEdge} can be mapped to {@code codEdge} and
	 * maps {@code domEdge} to {@code codEdge} if possible.
	 * 
	 * @param domEdge
	 *      edge in the domain graph
	 * @param codEdge
	 *      edge in the codomain graph
	 * @param allowRemapNode
	 *      if it should be possible to have an existing node mapping be changed
	 *      by mapping this edge
	 * @return
	 *      {@code true} if {@code domEdge} could be mapped to {@code codEdge},
	 *      {@code false} otherwise
	 * @throws NullPointerException
	 *      if {@code domEdge} or {@code codEdge} is {@code null}
	 * @throws IllegalArgumentException
	 *      if {@code domNode} is not an edge of the domain or {@code codEdge}
	 *      is not an edge of the codomain.
	 */
	boolean checkAndPut(Edge domEdge, Edge codEdge, boolean allowRemapNode) {
		if (domEdge.getGraph() != domain)
			throw new IllegalArgumentException(EXC_OUTSIDE_EDGE);
		if (codEdge.getGraph() != codomain)
			throw new IllegalArgumentException(EXC_OUTSIDE_EDGE);

		checkCoordinateDeletion();

		if (!domEdge.getLabel().equals(codEdge.getLabel()))
			return false;

		List<Node> domNodes = domEdge.getNodes();
		List<Node> codNodes = codEdge.getNodes();

		/* nodes may not be mapped somewhere else if this edge is already mapped,
		 * or if remapping is forbidden */
		if (!allowRemapNode || edgeMap.get(domEdge) != null) {
			/* this map is needed to make sure that n1 --A-> n1 cannot be
			 * mapped to n1 --A-> n2 (if the morphism was previously empty)
			 */
			Map<Node,Node> nodeMapCopy = new HashMap<>(domNodes.size());
			for (int i = 0; i < domNodes.size(); i++) {
				Node dn = domNodes.get(i);
				Node cn = nodeMapCopy.get(dn);
				if (cn == null) cn = nodeMap.get(dn);
				if (cn != null && cn != codNodes.get(i))
					return false;
				nodeMapCopy.put(dn, codNodes.get(i));
			}
		}

		edgeMap.put(domEdge, codEdge);
		for (int i = 0; i < domNodes.size(); i++)
			nodeMap.put(domNodes.get(i), codNodes.get(i));

		if (total == ThreeBool.FALSE)
			total = ThreeBool.UNKNOWN;
		if (injective == ThreeBool.TRUE)
			injective = ThreeBool.UNKNOWN;
		if (surjective == ThreeBool.FALSE)
			surjective = ThreeBool.UNKNOWN;
		hash = 0;

		onModification();

		return true;
	}

    /**
     * Removes the mapping for a node in the domain.
     * 
     * @param domNode       
     *      the node which is to be removed
     * @return              
     *      the node to which the node was previously mapped, or {@code null}
     *      if the node was not previously mapped
     * @throws NullPointerException
     *      when {@code domNode} is {@code null}
     * @throws IllegalArgumentException 
     *      if the node does not belong to the domain graph or is connected to
     *      an edge which is still mapped by this morphism
     */
    public @Nullable Node remove(Node domNode)
    {
        if (domNode == null) 
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "domNode"));
        
        Collection<Node> domNodes = domain.getNodes();
        if (!domNodes.contains(domNode)) 
            throw new IllegalArgumentException(EXC_OUTSIDE_NODE);
        
        for (Edge edge : domain.getEdges()) {
            if (edge.getNodes().contains(domNode) && edgeMap.containsKey(edge))
                throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
        }
        
        checkCoordinateDeletion();
        
        Node result = nodeMap.remove(domNode);
        
        total = ThreeBool.FALSE; 
        if (injective == ThreeBool.FALSE) injective = ThreeBool.UNKNOWN;
        if (surjective == ThreeBool.TRUE) surjective = ThreeBool.UNKNOWN;
        hash = 0;
        
        onModification();
        
        return result;
    }
    
    /**
     * Returns the node to which a given node is mapped. 
     * 
     * @param node
     *      the node of the domain the image of which is to be returned
     * @return 
     *      the node of the codomain to which {@code node} is mapped, or 
     *      {@code null} if {@code node} is not mapped to anything.
     * @throws NullPointerException
     *      when {@code node} is {@code null}
     */
    @Pure
    public @Nullable Node get(Node node)
    {
        if (node == null) 
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "node"));
        
        checkCoordinateDeletion();
        return nodeMap.get(node);
    }
    
    /**
     * Returns a node which is mapped to a given node.
     * If there are more than one nodes mapped to the given node, it is 
     * unspecified which of those is returned.
     * 
     * @param value         
     *      a node in the codomain graph
     * @return              
     *      a node of the domain graph which is mapped to {@code value}, or 
     *      {@code null} if no such node exists
     * @throws NullPointerException
     *      when {@code value} is {@code null}
     */
    @Pure
    public @Nullable Node getPreimage(Node value)
    {
        if (value == null)
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "value"));
        
        checkCoordinateDeletion();
        for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
            if (entry.getValue() == value) 
                return entry.getKey();
        }
        return null;
    }

    /**
     * Returns the pre-image of a collection of nodes. 
     * The returned collection contains all nodes of the domain graph which
     * are mapped to a node in {@code values}.
     * 
     * @param values
     *      the collection of nodes the pre-image of which is to be determined
     * @return
     *      the pre-image of {@code values}
     * @throws NullPointerException
     *      when {@code values} is {@code null}
     */
    @Pure
    public Collection<Node> getPreimageOfNodes(Collection<Node> values) 
    {
        if (values == null) 
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "value"));
        
        checkCoordinateDeletion();
        Set<Node> result = new HashSet<>();
        for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
            if (values.contains(entry.getValue()))
                result.add(entry.getKey());
        }
        return result;
    }

    /**
     * Returns the edge to which a given edge is mapped. 
     * 
     * @param edge
     *      the edge of the domain the image of which is to be returned
     * @return 
     *      the edge of the codomain to which {@code edge} is mapped, or 
     *      {@code null} if {@code edge} is not mapped to anything.
     * @throws NullPointerException
     *      when {@code edge} is {@code null}
     */
    @Pure
    public @Nullable Edge get(Edge edge)
    {
        if (edge == null) 
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "edge"));
        
        checkCoordinateDeletion();
        return edgeMap.get(edge);
    }
    
    /**
     * Returns an edge which is mapped to a given edge.
     * If there are more than one edges mapped to the given edge, it is 
     * unspecified which of those is returned.
     * 
     * @param value         
     *      a node in the codomain graph
     * @return             
     *      a node of the domain graph which is mapped to {@code value}, or 
     *      {@code null} if no such node exists
     * @throws NullPointerException
     *      if {@code value} is {@code null}
     */
    @Pure
    public @Nullable Edge getPreimage(Edge value)
    {
        if (value == null) 
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "value"));
        
        checkCoordinateDeletion();
        for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
            if (entry.getValue() == value) 
                return entry.getKey();
        }
        return null;
    }
    
    /**
     * Returns the pre-image of a collection of nodes. 
     * The returned collection contains all nodes of the domain graph which
     * are mapped to a node in {@code values}.
     * 
     * @param values
     *      the collection of nodes the pre-image of which is to be determined
     * @return
     *      the pre-image of {@code values}
     * @throws NullPointerException
     *      when {@code values} is {@code null}
     */
    @Pure
    public Collection<Edge> getPreimageOfEdges(Collection<Edge> values) 
    {
        if (values == null)
            throw new NullPointerException(String.format(EXC_NULL_PARAM, "values"));
        
        checkCoordinateDeletion();
        Set<Edge> result = new HashSet<>();
        for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
            if (values.contains(entry.getValue()))
                result.add(entry.getKey());
        }
        return result;
    }

    /**      
     * Removes the mapping for an edge in the domain.
     * 
     * @param domEdge       
     *      the edge which is to be removed
     * @return
     *      the edge to which the edge was previously mapped
     * @throws NullPointerException 
     *      if {@code domEdge} is {@code null}
     * @throws IllegalArgumentException
     *      if {@code domEdge} doesn't belong to the domain of this morphism
     */
    public @Nullable Edge remove(Edge domEdge)
    {
        if (domEdge.getGraph() != domain) 
            throw new IllegalArgumentException(EXC_OUTSIDE_EDGE);
        
        checkCoordinateDeletion();
        
        Edge result = edgeMap.remove(domEdge);

        if (total == ThreeBool.TRUE) total = ThreeBool.UNKNOWN; 
        if (injective == ThreeBool.FALSE) injective = ThreeBool.UNKNOWN;
        if (surjective == ThreeBool.TRUE) surjective = ThreeBool.UNKNOWN;
        hash = 0;
        
        onModification();
        
        return result;
    }
    
    /**
     * Determines whether a node occurs in the range of this morphism.
     */
    @Pure
    public boolean inRange(Node node)
    {
        checkCoordinateDeletion();
        return nodeMap.containsValue(node);
    }
    
    /**
     * Determines whether an edge occurs in the range of this morphism.
     */
    @Pure
    public boolean inRange(Edge edge)
    { 
        checkCoordinateDeletion();
        return edgeMap.containsValue(edge);
    }
    
    /**
     * Returns a new Morphism which is the inverse of this morphism.
     * The inverse of this morphism is a morphism from the codomain of this
     * morphism to the domain of this morphism. It maps each element (node or
     * edge) of the codomain to some element of the domain which is mapped to
     * it by this morphism.
     * <p>
     * If this morphism is total, then the inverse morphism is surjective;
     * if this morphism is surjective, then the inverse morphism is 
     * total. The inverse morphism is always injective.
     * <p>
     * The returned morphism is only uniquely defined if this morphism is 
     * injective, but no injectivity checks are performed. If this morphism
     * is not injective, then there exist more than one morphisms which satisfy
     * the condition of the first paragraph; one of those is returned
     * arbitrarily.
     * 
     * @return
     *      inverse of this morphism
     */
    @SideEffectFree
    public Morphism getInverse()
    {
        checkCoordinateDeletion();
        
        Map<Node,Node> invNodeMap = new HashMap<>();
        Map<Edge,Edge> invEdgeMap = new HashMap<>();
        
        for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
            invNodeMap.put(entry.getValue(), entry.getKey());
        }
        
        for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
            invEdgeMap.put(entry.getValue(), entry.getKey());
        }
        
        Morphism result = new Morphism(codomain, domain, invNodeMap, invEdgeMap);
        result.total = surjective;
        result.surjective = total;
        result.injective = ThreeBool.TRUE;
        return result;
    }
    
    /**
     * Determines if this morphism is total.
     * A morphism is total if it maps all nodes and edges of the domain to
     * nodes and edges of the codomain.
     * 
     * @return          
     *      {@code true} if this morphism is total, {@code false} if not
     */
    @Pure
    public boolean isTotal()
    {
        checkCoordinateDeletion();
        checkDomainAddition();
        
        if (total == ThreeBool.UNKNOWN) {
            if (nodeMap.keySet().containsAll(domain.getNodes()) &&
                edgeMap.keySet().containsAll(domain.getEdges()))
                total = ThreeBool.TRUE;
            else 
                total = ThreeBool.FALSE;
        }
        
        return total.isTrue();
    }
    
    /**
     * Determines if this morphism is surjective.
     * A morphism is surjective if all nodes and edges of the codomain are
     * mapped to.
     * 
     * @return
     *      {@code true} if this morphism is surjective, {@code false} if not
     */
    @Pure
    public boolean isSurjective()
    {
        checkCoordinateDeletion();
        checkCodomainAddition();
        
        if (surjective == ThreeBool.UNKNOWN) {
            if (nodeMap.values().containsAll(codomain.getNodes()) &&
                edgeMap.values().containsAll(codomain.getEdges()))
                surjective = ThreeBool.TRUE;
            else
                surjective = ThreeBool.FALSE;
        }
        
        return surjective.isTrue();
    }
    
    /**
     * Determines whether this morphism is injective.
     * A morphism is injective if different nodes and edges of the domain are
     * mapped to different nodes and edges of the codomain.
     * 
     * @return
     *      {@code true} if this morphism is injective, {@code false} otherwise
     */
    @Pure
    public boolean isInjective()
    {
        checkCoordinateDeletion();
        
        if (injective == ThreeBool.UNKNOWN) {
            if (Utilities.isInjective(nodeMap) && 
                Utilities.isInjective(edgeMap))
                injective = ThreeBool.TRUE;
            else
                injective = ThreeBool.FALSE;
        }
        
        return injective.isTrue();
    }
    
    /**
     * Determines whether this morphism is an isomorphism.
     * A morphism is an isomorphism if it is total, injective and surjective.
     * 
     * @return
     *      {@code true} if this morphism is an isomorphism, {@code false} 
     *      otherwise
     */
    @Pure
    public boolean isIsomorphism()
    {
        return isTotal() && isInjective() && isSurjective();
    }
    
    /**
     * Returns the node map of this morphism.
     * The returned map is unmodifiable.
     */
    @SideEffectFree
    public Map<Node,Node> getNodeMap()
    {
        checkCoordinateDeletion();
        return Collections.unmodifiableMap(nodeMap);
    }
    
    /**
     * Returns the edge map of this morphism.
     * The returned map is unmodifiable.
     */
    @SideEffectFree
    public Map<Edge,Edge> getEdgeMap()
    {
        checkCoordinateDeletion();
        return Collections.unmodifiableMap(edgeMap);
    }
    
    /**
     * Returns the domain of this morphism.
     * <p>
     * The domain graph may be modified by the user. If nodes or edges are
     * added to the domain graph, this Morphism will not map them to any 
     * nodes or edges of the codomain graph (that is, the morphism will not
     * be total after the addition). If nodes or edges are removed from the
     * domain graph, corresponding mappings will be removed from this 
     * Morphism.
     * 
     * @return
     *      the domain graph of this morphism
     */
    @Pure
    public @NonNull Graph getDomain()
    {
        return domain;
    }
    
    /**
     * Returns the codomain of this morphism.
     * <p>
     * The codomain graph may be modified by the user. If nodes or edges are
     * added to the codomain graph, this Morphism will not map to them (that
     * is, the morphism will not be surjective after the addition). If nodes
     * or edges are removed from the codomain graph, mappings to them will be
     * removed from this Morphism.
     * 
     * @return 
     *      the codomain graph of this morphism
     */
    @Pure
    public @NonNull Graph getCodomain()
    {
        return codomain;
    }
    
    /**
     * This method returns a pair of total and surjective morphisms 
     * ({@code f},{@code g}) such that {@code srcInc ; this == f ; g}.
     * If {@code srcInc} is injective, this pair of morphisms is uniquely
     * determined; otherwise, arbitrary morphisms for which the mentioned
     * condition holds will be returned.
     * <p>
     * The argument {@srcInc} must be a total morphism; if not, an 
     * {@code IllegalArgumentException} is thrown.
     * 
     * @param srcInc        
     *      morphism which identifies a subgraph of the domain of this morphism
     * @throws IllegalArgumentException
     *      if {@code srcInc}'s codomain is not the same graph as this 
     *      morphisms domain 
     * @return
     *      a pair of morphisms {@code p} such that 
     *      {@code srcInc ; this == p.getFirst() ; p.getSecond()}.
     * @throws NullPointerException
     *      when {@code srcInc} is {@code null}
     * @throws IllegalArgumentException
     *      when {@code srcInc}'s codomain is different than this Morphism's
     *      domain or is not a total morphism
     */
    @SideEffectFree
    public Pair<Morphism,Morphism> getSubMorphism(Morphism srcInc)
    {
        ensureComposable(srcInc, this, "srcInc", "this");
        
        Map<Node,Node> subNodeMap = new HashMap<>();
        Map<Edge,Edge> subEdgeMap = new HashMap<>();
        Map<Node,Node> rightNodeMap = new HashMap<>();
        Map<Edge,Edge> rightEdgeMap = new HashMap<>();
        
        Map<Node,Node> rightInvNodeMap = new HashMap<>();
        Map<Edge,Edge> rightInvEdgeMap = new HashMap<>();
        
        Graph src = srcInc.getDomain();
        Graph tgt = new Graph();
        
        for (Node srcNode : src.getNodes()) {
            Node domNode = srcInc.get(srcNode);
            if (domNode == null)
                throw new IllegalArgumentException(EXC_NOT_TOTAL);
            Node codNode = this.get(domNode);
            if (codNode != null) {
                Node tgtNode = rightInvNodeMap.get(codNode);
                if (tgtNode == null) {
                    tgtNode = tgt.addNode();
                    rightInvNodeMap.put(codNode, tgtNode);
                    rightNodeMap.put(tgtNode, codNode);
                }
                subNodeMap.put(srcNode, tgtNode);
            }
        }

        for (Edge srcEdge : src.getEdges()) {
            Edge domEdge = srcInc.get(srcEdge);
            if (domEdge == null)
                throw new IllegalArgumentException(EXC_NOT_TOTAL);
            Edge codEdge = this.get(domEdge);
            if (codEdge != null) {
                Edge tgtEdge = rightInvEdgeMap.get(codEdge);
                if (tgtEdge == null) {
                    List<Node> inodes = new ArrayList<>(srcEdge.getNodes());
                    for (int i = 0; i < inodes.size(); i++) {
                        Node tan = subNodeMap.get(inodes.get(i));
                        assert tan != null : "@AssumeAssertion(nullness)"; // by construction 
                        inodes.set(i, tan);
                    }
                    tgtEdge = tgt.addEdge(srcEdge.getLabel(), inodes);
                    rightInvEdgeMap.put(codEdge, tgtEdge);
                    rightEdgeMap.put(tgtEdge, codEdge);
                }
                subEdgeMap.put(srcEdge, tgtEdge);
            }
        }
        
        // at this point, totality of srcInc has been verified
        srcInc.total = ThreeBool.TRUE;
        
        Morphism subMorph = new Morphism(src, tgt, subNodeMap, subEdgeMap);
        Morphism rightMorph = new Morphism(tgt, codomain, rightNodeMap, rightEdgeMap);
        
        return Pair.create(subMorph, rightMorph);
    }
    
    /**
     * Returns a string representation of this morphism with a user-defined
     * indentation string.
     * 
     * @param indent
     *      indentation string which is put at the start of each line of the
     *      returned string representation
     * @return 
     *      string representation of this morphism
     */
    @Pure
    public String toString(String indent)
    {
        String intInd = ",\n" + indent + "  ";
        
        if (edgeMap.isEmpty() && nodeMap.isEmpty()) {
            return indent + "morphism { }";
        }
        else if (edgeMap.isEmpty()) {
            String fmt = 
                    "%1$smorphism {\n" +
                    "%1$s  %2$s\n" +
                    "%1$s}";
            return String.format(fmt,
                indent, 
                StringUtils.mapString(nodeMap, " => ", intInd));
        } 
        else {
            String fmt = 
                    "%1$smorphism {\n" +
                    "%1$s  %2$s,\n" +
                    "%1$s  %3$s\n" +
                    "%1$s}";
            return String.format(fmt,
                indent, 
                StringUtils.mapString(nodeMap, " => ", intInd),
                StringUtils.mapString(edgeMap, " => ", intInd));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override@SideEffectFree
    public String toString()
    {
        return toString("");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override@Pure
    public int hashCode()
    {
        if (hash == 0) {
            hash = 17;
            hash = 31 * hash + nodeMap.hashCode();
            hash = 31 * hash + edgeMap.hashCode();
            hash = 31 * hash + domain.hashCode();
            hash = 31 * hash + codomain.hashCode();
        }
        return hash;
    }
    
    /**
     * Determines if this morphism is equal to a given object.
     * A morphism is equal to another object, if the object is also a morphism
     * with the same domain and codomain and equal node and edge maps.
     * 
     * @param o
     *      the object of which is to be determined whether it is equal to this
     *      morphism
     * @return
     *      {@code true} if {@code o} is a morphism with the same domain and 
     *      codomain and equal node and edge maps
     */
    @Override@Pure
    public boolean equals(@Nullable Object o) 
    {
        if (!(o instanceof Morphism)) 
            return false;
        
        Morphism m = (Morphism)o;
        
        return (domain == m.domain &&
                codomain == m.codomain &&
                nodeMap.equals(m.nodeMap) &&
                edgeMap.equals(m.edgeMap));
    }
    
    /**
     * This method is called after this morphism is modified with one of 
     * the {@link #put} or {@link #remove} methods.
     * <p>
     * Subclasses may override this method to perform post-processing, for
     * example notifying other objects of the modification.
     * The default implementation does nothing.
     */
    protected void onModification()
    { }
    
    /**
     * Composes this morphism with another morphism.
     * <p>
     * The method call {@code m1.compose(m2)} is equivalent to the method call
     * {@code Morphism.createComposition(m1,m2)}.
     * 
     * @param m             
     *      the morphism to compose to this morphism.
     * @return              
     *      the morphism which is equal to this morphism with {@code m} 
     *      composed to it
     * @throws NullPointerException
     *      if {@code m} is {@code null}
     */
    @SideEffectFree
    public @NonNull Morphism compose(Morphism m)
    {
        return createComposition(this, m);
    }
    
    /**
     * Returns an Enumerator which enumerates morphisms which, when composed to
     * this morphism, result in a given morphism.
     * 
     * @param large
     *      the result morphism
     * @param injective     
     *      flag which controls if the returned morphisms must be injective
     * @return
     *      a collection of morphisms such that for each <tt><it>f</it></tt> in
     *      the collection it holds that <tt>this ; <em>f</em> = large</tt>.
     * @throws NullPointerException 
     *      if {@code large} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code getDomain() != large.getDomain()}
     */
    @SideEffectFree
    public Enumerator<Morphism> getCompletions(Morphism large, boolean injective) 
    {
        ensureCommonDomain(large, this, "large", "this");
        
        checkCoordinateDeletion();
        
        Morphism base = new Morphism(codomain, large.getCodomain());
        
        for (Node node : domain.getNodes()) {
            @Nullable Node lNode = large.get(node);
            @Nullable Node thisNode = get(node);
            if (thisNode != null && lNode != null)
                base.put(thisNode, lNode);
        }
        for (Edge edge : domain.getEdges()) {
            @Nullable Edge lEdge = large.get(edge);
            @Nullable Edge thisEdge = get(edge);
            if (thisEdge != null && lEdge != null) 
                base.put(thisEdge, lEdge);
        }
        
        if (injective && !base.isInjective())
            return Enumerators.emptyEnumerator();
        
        return Morphism.getMatches(codomain, large.getCodomain(), injective, base);
    }
    
    /**
     * Returns an Enumerator which enumerates all morphisms which, when composed
     * to this morphism, result in a given morphism.
     * <p>
     * The call {@code getCompletions(m)} is equivalent to 
     * {@code getCompletions(m, false)}. 
     * 
     * @param large
     *      the result morphism
     * @return
     *      a collection of morphisms such that for each <tt><it>f</it></tt> 
     *      in the collection it holds that <tt>this ; <em>f</em> = large</tt>.
     * @throws NullPointerException
     *      if {@code large} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code getDomain() != large.getDomain()}
     */
    @SideEffectFree
    public Enumerator<Morphism> getCompletions(Morphism large) 
    {
        return getCompletions(large, false);
    }
    
    /**
     * Returns an enumerator which enumerates all injective morphisms which,
     * when composed to this morphism, result in a given morphism.
     * <p>
     * The call {@code getInjectiveCompletions(m)} is equivalent to 
     * {@code getCompletions(m, true)}. 
     * 
     * @param large         
     *      the result morphism
     * @return              
     *      a collection of morphisms such that for each <tt><it>f</it></tt> 
     *      in the collection it holds that <tt>this ; <em>f</em> = large</tt>.
     * @throws NullPointerException
     *      if {@code large} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code getDomain() != large.getDomain()}
     */
    @SideEffectFree
    public Enumerator<Morphism> getInjectiveCompletions(Morphism large)
    {
        return getCompletions(large, true);
    }
    
    /**
     * Determines whether this morphism is an extension of another morphism.
     * A morphism {@code m1} is an extension of a morphism {@code m2}, if
     * all nodes and edges for which {@code m2} is defined are mapped to the
     * same objects by {@code m1} and {@code m2}.
     * 
     * @param m
     *      the morphism for which it is to be determined whether or not this
     *      morphism is an extension of it
     * @return              
     *      {@code true} if this morphism is an extension of {@code m}, 
     *      {@code false} otherwise
     * @throws NullPointerException
     *      if {@code m} is {@code null}
     * @throws IllegalArgumentException
     *      when {@code m} has a different domain than this Morphisms or
     *      a different codomain than this Morphism
     */
    @Pure
    public boolean isExtensionOf(Morphism m)
    {
        ensureCommonDomain  (m, this, "m", "this");
        ensureCommonCodomain(m, this, "m", "this");
        
        checkCoordinateDeletion();
        
        for (Map.Entry<Node,Node> entry : m.nodeMap.entrySet()) {
            Node forM = entry.getValue();
            if (forM != null && forM != get(entry.getKey()))
                return false;
        }
        
        for (Map.Entry<Edge,Edge> entry : m.edgeMap.entrySet()) {
            Edge forM = entry.getValue();
            if (forM != null && forM != get(entry.getKey()))
                return false;
        }
        
        return true;
    }
    
    /**
     * Creates a new morphism which is equal to this morphism, except that
     * the codomain is restricted to the range of this morphism. In particular,
     * this method returns:
     * <ul>
     * <li>
     *   a surjective morphism <i>f</i> and
     * <li>
     *   a injective morphism <i>c</i> 
     * </ul>
     * such that <tt>this</tt> = <i>c</i> <tt>o</tt> <i>f</i>. 
     * 
     * @return
     *      a MorphismResult object {@code mr}, where {@code mr.getResult()} 
     *      equals <i>f</i> as above, {@code mr.getCodomainCorrespondence()} 
     *      equals <i>c</i> as above, and {@code mr.getDomainCorrespondence()} 
     *      is an identity morphism for the domain of this morphism. 
     */
    @SideEffectFree
    public MorphismResult getSurjectiveMorphism()
    {
        checkCoordinateDeletion();
        
        Map<Node,Node> newNodeMap = new HashMap<>();
        Map<Edge,Edge> newEdgeMap = new HashMap<>();
        
        Graph newCodomain = new Graph();

        Map<Node,Node> nodeCodCorr = new HashMap<>();
        Map<Edge,Edge> edgeCodCorr = new HashMap<>();

        for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
            Node node = entry.getKey();
            Node img = entry.getValue();
            if (node != null && img != null) {
                Node newImg = nodeCodCorr.get(img);
                if (newImg == null) {
                    newImg = newCodomain.addNode(img.getId());
                    nodeCodCorr.put(img, newImg);
                }
                newNodeMap.put(node, newImg);
            }
        }

        for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
            Edge edge = entry.getKey();
            Edge img = entry.getValue();
            if (edge != null && img != null) {
                // look for corresponding edge in the codomain
                Edge newImg = edgeCodCorr.get(img);
                if (newImg == null) {
                    // if it doesn't exist, create new edge in the codomain
                    List<Node> jnodes = new ArrayList<>(img.getNodes().size());
                    for (Node n : img.getNodes()) {
                        Node an = nodeCodCorr.get(n);
                        assert an != null : "@AssumeAssertion(nullness)"; // by construction
                        jnodes.add(an);
                    }
                    newImg = newCodomain.addEdge(img.getLabel(), jnodes);
                    edgeCodCorr.put(img, newImg);
                }
                
                // map domain edge to codomain edge
                newEdgeMap.put(edge, newImg);
            }
        }

        Morphism result = new Morphism(domain, newCodomain, newNodeMap, newEdgeMap);
        Morphism domCorr = Morphism.createIdentity(domain);
        Morphism codCorr = new Morphism(codomain, newCodomain, nodeCodCorr, edgeCodCorr);
        return new MorphismResult(result, domCorr, codCorr);
    }
    
    /**
     * Creates a new morphism which is equal to this one, except that the
     * domain and codomain are restricted to be the definition domain and
     * range of this morphism. In particular this method returns:
     * <ul>
     * <li> 
     *   a total injective morphism <i>f</i>;
     * <li>
     *   partial, surjective morphisms <i>c</i><sub>d</sub> and
     *   <i>c</i><sub>c</sub>
     * </ul> 
     * such that
     * <tt>this</tt> = <i>c</i><sub>c</sub> <tt>o</tt> <i>f</i> <tt>o</tt>
     * <i>c</i><sub>d</sub>.
     * 
     * @return              
     *      the three morphisms described above as a MorphismResult object
     */
    @SideEffectFree
    public MorphismResult getTotalSurjectiveMorphism()
    {
        checkCoordinateDeletion();
        
        Map<Node,Node> newNodeMap = new HashMap<>();
        Map<Edge,Edge> newEdgeMap = new HashMap<>();
        
        Map<Node,Node> nodeDomCorr = new HashMap<>();
        Map<Edge,Edge> edgeDomCorr = new HashMap<>();
        
        Map<Node,Node> nodeCodCorr = new HashMap<>();
        Map<Edge,Edge> edgeCodCorr = new HashMap<>();
        
        Graph newDomain = new Graph();
        Graph newCodomain = new Graph();
        
        for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
            Node node = entry.getKey();
            Node img = entry.getValue();
            if (node != null && img != null) {
                Node newNode = newDomain.addNode(node.getId());
                nodeDomCorr.put(node, newNode);
                Node newImg = nodeCodCorr.get(img);
                if (newImg == null) {
                    newImg = newCodomain.addNode(img.getId());
                    nodeCodCorr.put(img, newImg);
                }
                newNodeMap.put(newNode, newImg);
            }
        }

        for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
            Edge edge = entry.getKey();
            Edge img = entry.getValue();
            if (edge != null && img != null) {
                // create new edge in the domain
                List<Node> inodes = new ArrayList<>(edge.getNodes().size());
                for (Node n : edge.getNodes()) {
                    Node an = nodeDomCorr.get(n);
                    assert an != null : "@AssumeAssertion(nullness)"; // by construction
                    inodes.add(an);
                }
                Edge newEdge = newDomain.addEdge(edge.getLabel(), inodes);
                edgeDomCorr.put(edge, newEdge);
                
                // look for corresponding edge in the codomain
                Edge newImg = edgeCodCorr.get(img);
                if (newImg == null) {
                    // if it doesn't exist, create new edge in the codomain
                    List<Node> jnodes = new ArrayList<>(img.getNodes().size());
                    for (Node n : img.getNodes()) {
                        Node an = nodeCodCorr.get(n);
                        assert an != null : "@AssumeAssertion(nullness)"; // by construction
                        jnodes.add(an);
                    }
                    newImg = newCodomain.addEdge(img.getLabel(), jnodes);
                    edgeCodCorr.put(img, newImg);
                }
                
                // map domain edge to codomain edge
                newEdgeMap.put(newEdge, newImg);
            }
        }
        
        Morphism result = new Morphism(newDomain, newCodomain, newNodeMap, newEdgeMap);
        Morphism domCorr = new Morphism(domain, newDomain, nodeDomCorr, edgeDomCorr);
        Morphism codCorr = new Morphism(codomain, newCodomain, nodeCodCorr, edgeCodCorr);
        return new MorphismResult(result, domCorr, codCorr);
    }

    
    /* ---------------------------------------------------------------------- *
     *                       Static factory methods                           *                          
     * ---------------------------------------------------------------------- */
    
    /**
     * Creates a new, empty morphism with given domain and codomain.
     * 
     * @param domain        
     *      domain of the new morphism
     * @param codomain      
     *      codomain of the new morphism
     * @return              
     *      a new, empty morphism from {@code domain} to {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static @NonNull Morphism create(Graph domain, Graph codomain)
    {
        return new Morphism(domain, codomain);
    }
    
    /**
     * Creates a new morphism which only maps nodes.
     * If {@code nodeMap == null}, this constructor creates an empty
     * morphism from {@code domain} to {@code codomain}.
     * 
     * @param domain        
     *      domain graph of the morphism
     * @param codomain      
     *      codomain graph of the morphism
     * @param nodeMap      
     *      map from nodes of the domain graph to nodes of the codomain graph
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code nodeMap} contains a key which is not a node of 
     *      {@code domain}, or a value which is not a node of {@code codomain}
     */
    @SideEffectFree
    public static Morphism create(
            Graph domain, 
            Graph codomain, 
            @Nullable Map<Node,Node> nodeMap)
    {
        if (domain == null || codomain == null) 
            throw new NullPointerException(EXC_DOMAIN_OR_CODOMAIN_NULL);

        Map<Edge,Edge> newEdgeMap = new HashMap<>();
        Map<Node,Node> newNodeMap = new HashMap<>();
        
        // fill the nodeMap and check that all nodes are in the 
        // domain/codomain
        if (nodeMap != null) {
            Collection<Node> domNodes = domain.getNodes();
            Collection<Node> codNodes = codomain.getNodes();
            for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
                Node domn = entry.getKey();
                Node codn = entry.getValue();
                if (!domNodes.contains(domn) || !codNodes.contains(codn)) {
                    throw new IllegalArgumentException(EXC_OUTSIDE_NODE);
                }
                newNodeMap.put(domn, codn);
            }
        }
        
        return new Morphism(domain, codomain, newNodeMap, newEdgeMap);
    }
    
    /**
     * Creates a new morphism from a node map and an edge map. 
     * The new morphism maps all edges as they mapped by {@code nodeMap}.
     * Additionally, a nodes are mapped to each other when this is either
     * given by {@code nodeMap} or implicitly required by {@code edgeMap}.
     * <p>
     * Both {@code nodeMap} and {@code edgeMap} may be {@code null}; where
     * passing {@code null} has the same effect as passing an empty map.
     * 
     * @param domain
     *      domain of the new morphism
     * @param codomain      
     *      codomain of the new morphism
     * @param nodeMap       
     *      map which maps nodes of the domain to nodes of the codomain
     * @param edgeMap       
     *      map which maps edges of the domain to edges of the codomain
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     * @throws IllegalArgumentException 
     *      if nodes or edges outside of the domain or codomain occur as key or
     *      value in of the maps, or if creating a morphism according to the
     *      maps will result in an invariant violation
     */
    @SideEffectFree
    public static Morphism create(
            Graph domain, 
            Graph codomain, 
            @Nullable Map<Node,Node> nodeMap, 
            @Nullable Map<Edge,Edge> edgeMap)
    {
        if (domain == null || codomain == null) 
            throw new NullPointerException(EXC_DOMAIN_OR_CODOMAIN_NULL);

        Collection<Node> domNodes = domain.getNodes();
        Collection<Node> codNodes = codomain.getNodes();
        Collection<Edge> domEdges = domain.getEdges();
        Collection<Edge> codEdges = codomain.getEdges();
        
        @MonotonicNonNull Map<Node,Node> newNodeMap = null;
        @MonotonicNonNull Map<Edge,Edge> newEdgeMap = null;
        
        if (nodeMap != null) {
            // Check that all nodes are in domain/codomain
            for (Map.Entry<Node,Node> entry : nodeMap.entrySet()) {
                Node domn = entry.getKey();
                Node codn = entry.getValue();
                if (!domNodes.contains(domn) || !codNodes.contains(codn)) 
                    throw new IllegalArgumentException(EXC_OUTSIDE_NODE);
            }
            newNodeMap = new HashMap<>(nodeMap);
        }
        else {
            newNodeMap = new HashMap<>();
        }

        // Check morphism invariant
        if (edgeMap != null) {
            for (Map.Entry<Edge,Edge> entry : edgeMap.entrySet()) {
                Edge domEdge = entry.getKey();
                Edge codEdge = entry.getValue();
                if (!domEdges.contains(domEdge) || !codEdges.contains(codEdge)) 
                    throw new IllegalArgumentException(EXC_OUTSIDE_EDGE);
                else if (!domEdge.getLabel().equals(codEdge.getLabel())) 
                    throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
                else {
                    List<Node> domAns = domEdge.getNodes();
                    List<Node> codAns = codEdge.getNodes();
                    if (domAns.size() != codAns.size()) 
                        throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
                    for (int i = 0; i < domAns.size(); i++) {
                        Node domNode = domAns.get(i);
                        Node codNode1 = newNodeMap.get(domNode);
                        Node codNode2 = codAns.get(i);
                        if (codNode1 == null) 
                            newNodeMap.put(domNode, codNode2);
                        else if (codNode1 != codNode2)  
                            throw new IllegalArgumentException(EXC_INVARIANT_VIOLATION);
                    }
                }
            }
            newEdgeMap = new HashMap<>(edgeMap);
        }
        else {
            newEdgeMap = new HashMap<>();
        }
        
        return new Morphism(domain, codomain, newNodeMap, newEdgeMap);
    }
    
    /**
     * Creates a new identity morphism.
     * 
     * @param graph         
     *      the domain and codomain of the new identity morphism
     * @return
     *      a new identity morphism on {@code graph}
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @SideEffectFree
    public static @NonNull Morphism createIdentity(Graph graph)
    {
        if (graph == null) throw new NullPointerException(EXC_GRAPH_NULL);
        
        Morphism id = new Morphism(graph, graph);
        for (Node n : graph.getNodes()) {
            id.nodeMap.put(n, n);
        }
        for (Edge e : graph.getEdges()) {
            id.edgeMap.put(e, e);
        }
        
        return id;
    }

    /**
     * Creates a new graph which is isomorphic to a given graph and returns 
     * an isomorphism between that graph and the new one.
     * <p>
     * The newly created graph is the codomain of the returned morphism,
     * while {@code createIsomorphism(graph).getDomain() == graph}.
     *  
     * @return              
     *      isomorphism between this graph and a newly created graph which is 
     *      isomorphic to it
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    @SideEffectFree
    public static Morphism createIsomorphism(Graph graph) 
    {
        if (graph == null) throw new NullPointerException(EXC_GRAPH_NULL);
        
        Map<Node,Node> nodeMap = new HashMap<>();
        Map<Edge,Edge> edgeMap = new HashMap<>();
        
        Graph newGraph = new Graph();
        for (Node node : graph.getNodes()) {
            Node newNode = newGraph.addNode();
            nodeMap.put(node, newNode);
        }
        for (Edge edge : graph.getEdges()) {
            List<Node> ans = new ArrayList<>();
            for (Node n : edge.getNodes()) {
                assert (n.getGraph() == graph);
                Node an = nodeMap.get(n);
                assert an != null : "@AssumeAssertion(nullness)"; // by construction
                ans.add(an);
            }
            Edge newEdge = newGraph.addEdge(edge.getLabel(), ans);
            edgeMap.put(edge, newEdge);
        }
     
        return new Morphism(graph, newGraph, nodeMap, edgeMap);
    }
    
    /**
     * Creates a new Morphism which is the composition of a number of 
     * morphisms.
     * <p>
     * If <tt>f<sub>1</sub>,...,f<sub>n</sub></tt> are composable morphisms
     * (mathematically, <tt>f<sub>n</sub> o ... o f<sub>1</sub></tt> is 
     * defined), then 
     * <tt>Morphism.createComposition(f<sub>1</sub>,...,f<sub>n</sub>)</tt>
     * returns the composition of <tt>f<sub>1</sub>,...,f<sub>n</sub></tt>.
     * 
     * @param fs        
     *      the Morphisms which are to be composed
     * @return          
     *      a new Morphism which is composition of 
     *      <tt>f<sub>1</sub>,...,f<sub>n</sub></tt>
     * @throws NullPointerException
     *      if {@code fs} contains one or more null elements
     * @throws IllegalArgumentException 
     *      if {@code fs} is empty or if the argument morphisms are not 
     *      composable
     */
    @SideEffectFree
    public static @NonNull Morphism createComposition(Morphism... fs)
    {
        if (fs.length == 0) {
            throw new IllegalArgumentException("createComposition requires at least one argument.");
        }
        if (fs.length == 1) return new Morphism(fs[0]);
        
        ThreeBool newInjective = fs[0].injective;
        ThreeBool newSurjective = fs[0].surjective;
        
        fs[0].checkCoordinateDeletion();
        fs[0].checkDomainAddition();
        fs[0].checkCodomainAddition();
        
        Map<Node,Node> nodeMap = new HashMap<>(fs[0].nodeMap);
        Map<Edge,Edge> edgeMap = new HashMap<>(fs[0].edgeMap);
       
        for (int i = 1; i < fs.length; i++) {
            if (fs[i-1].getCodomain() != fs[i].getDomain()) {
                throw new IllegalArgumentException(String.format("Arguments %d and %d are not composable.", i - 1, i));
            }
            
            fs[i].checkCoordinateDeletion();
            fs[i].checkDomainAddition();
            fs[i].checkCodomainAddition();
            if (fs[i].injective != ThreeBool.TRUE) newInjective = ThreeBool.UNKNOWN;
            if (fs[i].surjective != ThreeBool.TRUE) newSurjective = ThreeBool.UNKNOWN;
            
            for (Iterator<Map.Entry<Node,Node>> it = nodeMap.entrySet().iterator(); 
                    it.hasNext(); ) {
                Map.Entry<Node,Node> entry = it.next();
                Node newTgt = fs[i].get(entry.getValue());
                if (newTgt == null) {
                    it.remove();
                }
                else {
                    entry.setValue(newTgt);
                }
            }
            
            for (Iterator<Map.Entry<Edge,Edge>> it = edgeMap.entrySet().iterator(); 
                    it.hasNext(); ) {
                Map.Entry<Edge,Edge> entry = it.next();
                Edge newTgt = fs[i].get(entry.getValue());
                if (newTgt == null) {
                    it.remove();
                }
                else {
                    entry.setValue(newTgt);
                }
            }
        }
        
        Morphism result = new Morphism(
                fs[0].getDomain(), 
                fs[fs.length - 1].getCodomain(), 
                nodeMap, 
                edgeMap);
        result.injective = newInjective;
        result.surjective = newSurjective;
        return result;
    }
    
    /**
     * Returns an Enumerator which enumerates all the total morphisms from one
     * graph to another that are extensions of a given (partial) morphism.
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>. 
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getMorphisms(g1, g2, base)) { 
     *     ... 
     *   }
     * </pre>
     *  
     * @param domain        
     *      the domain of the morphisms
     * @param codomain      
     *      the codomain of the morphisms
     * @param base          
     *      the morphism of which all returned morphism are an extension
     * @return              
     *      Enumerator which enumerates all total morphisms from {@code domain}
     *      to {@code codomain} that are an extension of {@code base}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getMatches(
            Graph domain, 
            Graph codomain, 
            @Nullable Morphism base) 
    {
        MorphismFinder factory = new MorphismFinder();
        factory.setTotal(true);
        factory.setInjective(false);
        return factory.enumerate(domain, codomain, base);
    }
    
    /**
     * Returns an Enumerator which enumerates all total morphisms from one
     * graph to another.
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getMorphisms(g1, g2)) { 
     *     ... 
     *   }
     * </pre>
     * <strong>Note:</strong> see {@link MorphismCollection} for performance
     * notes about the returned collection.
     *  
     * @param domain        
     *      the domain of the morphisms
     * @param codomain      
     *      the codomain of the morphisms
     * @return              
     *      an Enumerator which enumerates all morphisms from {@code domain} to 
     *      {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getMatches(
            Graph domain, 
            Graph codomain) 
    {
        return getMatches(domain, codomain, null);
    }
    
    /**
     * Returns a total morphism from a given domain to a given codomain.
     * If there are more than one total morphisms from the domain to the
     * codomain, an arbitrary one is returned. If there are no such morphisms,
     * {@code null} is returned.
     * 
     * @param domain
     *      domain of the morphism
     * @param codomain      
     *      codomain of the morphism
     * @return              
     *      a morphism from {@code domain} to {@code codomain}, or {@code null}
     *      if such a morphism does not exist
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static @Nullable Morphism getMatch(
            Graph domain, 
            Graph codomain)
    {
        return getMatches(domain, codomain, null).next();
    }
    
    /**
     * Returns a total morphism from a given domain to a given codomain which
     * is an extension of a given morphism.
     * If there are more than one such morphisms, an arbitrary one is returned. 
     * If there are no such morphisms, {@code null} is returned.
     * 
     * @param domain        
     *      domain of the morphism
     * @param codomain      
     *      codomain of the morphism
     * @param base          
     *      morphism of which the returned morphism is an extension
     * @return     
     *      a morphism from {@code domain} to {@code codomain} which is an 
     *      extension of {@code base}, or {@code null} if such a morphism does
     *      not exist
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */    
    @SideEffectFree
    public static @Nullable Morphism getMatch(
            Graph domain,
            Graph codomain, 
            @Nullable Morphism base)
    {
        return getMatches(domain, codomain, base).next();
    }
    
    /**
     * Returns an Enumerator which enumerates all isomorphisms between two
     * graphs which are extensions of a given injective morphism.
     * <p>
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>. 
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getIsomorphisms(g1, g2, base)) { 
     *     ... 
     *   }
     * </pre>
     * 
     * @param domain        
     *      the domain of the morphisms
     * @param codomain      
     *      the codomain of the morphisms
     * @param base
     *      the base morphism of which all enumerated morphisms are an
     *      extension
     * @return              
     *      an Enumerator which enumerates all isomorphisms between
     *      {@code domain} and {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code base} is non-null and not an injective morphism from
     *      {@code domain} to {@code domain}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getIsomorphisms(
            Graph domain, 
            Graph codomain, 
            @Nullable Morphism base) 
    {
        MorphismFinder factory = new MorphismFinder();
        factory.setIsomorphic(true);
        return factory.enumerate(domain, codomain, base);
    }
    
    /**
     * Returns an Enumerator which enumerates all isomorphism between two graphs.
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getIsomorphisms(g1, g2)) { 
     *     ... 
     *   }
     * </pre>
     * <strong>Note:</strong> see {@link MorphismCollection} for performance
     * notes about the returned collection.
     * 
     * @param domain
     *      the domain of the morphisms
     * @param codomain      
     *      the codomain of the morphisms
     * @return              
     *      a Collection of all isomorphisms between {@code domain} and 
     *      {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     * @throws IllegalArgumentException 
     *      when {@code base} is non-null and not an injective morphism
     */
    @SideEffectFree
    public static Enumerator<Morphism> getIsomorphisms(
            Graph domain, 
            Graph codomain) {
        return getIsomorphisms(domain, codomain, null);
    }
    
    /**
     * Returns an isomorphism from a given domain to a given codomain graph
     * which is an extension of a given morphism.
     * If there are multiple such isomorphisms, an arbitrary one is returned.
     * If there are no such isomorphisms, {@code null} is returned.
     * 
     * @param domain        
     *      domain of the morphism
     * @param codomain      
     *      codomain of the morphism
     * @param base          
     *      morphism of which the returned morphism must be an extension
     * @return              
     *      an isomorphism from {@code domain} to {@code codomain}, or 
     *      {@code null} if {@code domain} and {@code codomain} are not 
     *      isomorphic
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     * @throws IllegalArgumentException 
     *      if {@code base} is non-null and not an injective morphism from
     *      {@code domain} to {@code codomain}
     */
    @SideEffectFree
    public static @Nullable Morphism getIsomorphism(
            Graph domain, 
            Graph codomain, 
            @Nullable Morphism base)
    {
        return getIsomorphisms(domain, codomain, base).next();
    }
    
    /**
     * Returns an isomorphism from a given domain to a given codomain graph.
     * If there are multiple isomorphisms, an arbitrary one is returned.
     * If there are no isomorphisms, {@code null} is returned.
     * 
     * @param domain        
     *      domain of the morphism
     * @param codomain      
     *      codomain of the morphism
     * @return              
     *      an isomorphism from {@code domain} to {@code codomain}, or 
     *      {@code null} if {@code domain} and {@code codomain} are not
     *      isomorphic
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static @Nullable Morphism getIsomorphism(
            Graph domain, 
            Graph codomain)
    {
        return getIsomorphisms(domain, codomain, null).next();
    }
    
    /**
     * Returns an enumerator which enumerates all the total morphisms from one
     * graph to another which are extensions of a given (partial) morphism.
     * <p>
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>. 
     * <p>
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>. 
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getMorphisms(g1, g2, inj, base)) { 
     *     ... 
     *   }
     * </pre>
     *  
     * @param domain        
     *      the domain of the morphisms
     * @param codomain
     *      the codomain of the morphisms
     * @param injective
     *      flag which controls whether only injective morphisms are to be 
     *      returned
     * @param base          
     *      the morphism of which all returned morphism are an extension
     * @return              
     *      an Enumerator which enumerates all (injective) morphisms
     *      from {@code domain} to {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getMatches(
            Graph domain,
            Graph codomain, 
            boolean injective, 
            @Nullable Morphism base)
    {
        MorphismFinder factory = new MorphismFinder();
        factory.setTotal(true);
        factory.setInjective(injective);
        return factory.enumerate(domain, codomain, base);
    }
    
    /**
     * Returns an Enumerator which enumerates all total morphisms from one
     * graph to another.
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   for (Morphism m : Morphism.getMorphisms(g1, g2, inj)) { 
     *     ... 
     *   }
     * </pre>
     * <p>
     * This method is equivalent to 
     * <tt>{@link #getMatches(Graph,Graph,boolean,Morphism) getMatches}(domain, 
     * codomain, injective, null)}</tt>.
     * 
     * @param domain        
     *      the domain of the morphisms
     * @param codomain      
     *      the codomain of the morphisms
     * @param injective     
     *      flag which controls whether only injective morphisms are to be 
     *      returned
     * @return        
     *      an Enumerator which enumerates all morphisms from {@code domain} to
     *      {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getMatches(
            Graph domain,
            Graph codomain, 
            boolean injective)
    {
        return getMatches(domain, codomain, injective, null);
    }
    
    /**
     * Returns a morphism from a given domain to a given codomain which is
     * an extension of a given base morphism.
     * 
     * @param domain        
     *      the domain of the morphism
     * @param codomain      
     *      the codomain of the morphism
     * @param injective     
     *      whether or not the morphism must be injective
     * @param base          
     *      base morphism 
     * @return              
     *      morphism from {@code domain} to {@code codomain} which is an
     *      extension of {@code base}, or {@code null} if such a morphism does
     *      not exist
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static @Nullable Morphism getMatch(
            Graph domain, 
            Graph codomain, 
            boolean injective, 
            @Nullable Morphism base)
    {
        return getMatches(domain, codomain, injective, base).next();
    }
    
    /**
     * Returns a morphism from a given domain to a given codomain.
     * 
     * @param domain        
     *      the domain of the morphism
     * @param codomain      
     *      the codomain of the morphism
     * @param injective     
     *      flag which controls whether or not the morphism must be injective
     * @return              
     *      morphism from {@code domain} to {@code codomain}, or {@code null} 
     *      if such a morphism does not exist
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public static @Nullable Morphism getMatch(
            Graph domain, 
            Graph codomain,
            boolean injective)
    {
        return getMatches(domain, codomain, injective, null).next();
    }
    
    /**
     * Returns a MorphismCollection partial morphisms from a domain to a
     * codomain which are extensions of a given base morphism.
     * 
     * @param domain        
     *      the domain of the morphisms 
     * @param codomain      
     *      the codomain of the morphisms
     * @param injective     
     *      flag which controls whether the returned collection should contain
     *      only injective partial morphisms
     * @param base          
     *      morphism of which all returned morphisms are an extension
     * @return              
     *      MorphismCollection of partial morphisms from {@code domain} to 
     *      {@code codomain}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getPartialMorphisms(
            final Graph domain, 
            final Graph codomain, 
            final boolean injective, 
            @Nullable final Morphism base)
    {
        MorphismFinder factory = new MorphismFinder();
        factory.setTotal(false);
        factory.setInjective(injective);
        return factory.enumerate(domain, codomain, base);
    }

    /**
     * Returns a Enumerator which enumerates partial morphisms from a domain to
     * a codomain which are extension of a given base morphism.
     * 
     * @param domain        
     *      the domain of the morphisms 
     * @param codomain      
     *      the codomain of the morphisms
     * @param base
     *      morphism of which all returned morphisms are an extension
     * @return             
     *      MorphismCollection of partial morphisms from {@code domain} to 
     *      {@code codomain}
     */
    @SideEffectFree
    public static Enumerator<Morphism> getPartialMorphisms(
            final Graph domain, 
            final Graph codomain, 
            @Nullable final Morphism base)
    {
        return getPartialMorphisms(domain, codomain, false, base);
    }

	/**
	 * Ensures that the given morphisms are composable ({@code m2 o m1} is
	 * defined), i.e. the codomain of m1 equals the domain of m2.
	 * @param m1 first morphism
	 * @param m2 second morphism
	 * @param nameM1 name of first morphism, for error messages
	 * @param nameM2 name of second morphism, for error messages
	 * @throws NotComposableException if the given morphisms are not composable
	 */
	public static void ensureComposable(Morphism m1, Morphism m2, String nameM1, String nameM2) {
		if (m1.getCodomain() != m2.getDomain()) {
			throw new NotComposableException(m1, m2, nameM1, nameM2);
		}
	}

	/**
	 * Ensures that the given morphisms have the same domain.
	 * @param m1 first morphism
	 * @param m2 second morphism
	 * @param nameM1 name of first morphism, for error messages
	 * @param nameM2 name of second morphism, for error messages
	 * @throws DomainMismatchException if the given morphisms have different domains
	 */
	public static void ensureCommonDomain(Morphism m1, Morphism m2, String nameM1, String nameM2) {
		if (m1.getDomain() != m2.getDomain()) {
			throw new DomainMismatchException(m1, m2, nameM1, nameM2);
		}
	}

	/**
	 * Ensures that the given morphisms have the same codomain.
	 * @param m1 first morphism
	 * @param m2 second morphism
	 * @param nameM1 name of first morphism, for error messages
	 * @param nameM2 name of second morphism, for error messages
	 * @throws CodomainMismatchException if the given morphisms have different codomains
	 */
	public static void ensureCommonCodomain(Morphism m1, Morphism m2, String nameM1, String nameM2) {
		if (m1.getCodomain() != m2.getCodomain()) {
			throw new CodomainMismatchException(m1, m2, nameM1, nameM2);
		}
	}
}
