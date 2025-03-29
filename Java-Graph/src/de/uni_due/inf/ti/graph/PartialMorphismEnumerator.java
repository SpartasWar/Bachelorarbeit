/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, 
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

// 2014-09-26 - Created

package de.uni_due.inf.ti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.graph.util.ThreeBool;

public class PartialMorphismEnumerator extends Enumerator<Morphism>
{
    /** 
     * Stack frame for implementing recursion.
     */
    private static class MIStackFrame
    {
        int elNr, candNr;
        Map<Node,Node> nodeMap;
        Map<Edge,Edge> edgeMap;       
        @Nullable List<Edge> curCodEdges;

        MIStackFrame(
                int elNr, int candNr, 
                Map<Node,Node> nodeMap, 
                Map<Edge,Edge> edgeMap, 
                @Nullable List<Edge> curCodEdges)
        {
            this.elNr = elNr;
            this.candNr = candNr;
            this.nodeMap = nodeMap;
            this.edgeMap = edgeMap;
            this.curCodEdges = curCodEdges;
        }        
    }

    // base morphism
    private @Nullable Map<Edge,Edge> baseEdgeMap = null;
    private @Nullable Map<Node,Node> baseNodeMap = null;

    // information about domain graph
    private Graph dom;
    private @MonotonicNonNull List<Edge> domEdges;
    private @MonotonicNonNull List<Node> domNodes;
    private long domModCount;

    // information about codomain graph
    private Graph cod;
    private List<Node> codNodes;
    private @MonotonicNonNull Map<Label,List<Edge>> allCodEdges;
    private @Nullable List<Edge> curCodEdges;

    
    private long codModCount;
    
    // flags 
    private boolean injective;  // only injective morphisms?

    // local variables for morphism finding
    private @MonotonicNonNull Deque<MIStackFrame> stack;
    private int elNr, candNr;
    private @MonotonicNonNull Map<Node,Node> nodeMap;
    private @MonotonicNonNull Map<Edge,Edge> edgeMap;
    
    // local variable for iteration
    private boolean done;
    
    // special case for when it is immediately clear that only a single
    // morphism can be found
    private @Nullable Morphism single = null;
    

    /** 
     * Creates a new PartialMorphismEnumerator.
     * 
     * @param domain
     *      domain of the enumerated morphisms
     * @param codomain
     *      codomain of the enumerated morphisms
     * @param injective
     *      whether only injective morphisms are enumerated
     * @param base
     *      base morphism of the enumerated morphisms
     */
    @SideEffectFree
    PartialMorphismEnumerator(
            final Graph domain, 
            final Graph codomain, 
            final boolean injective, 
            final @Nullable Morphism base) 
    {
        dom = domain;
        domModCount = domain.getModCount();
        
        cod = codomain;
        codModCount = codomain.getModCount();
        codNodes = cod.getNodes();

        if (domain.isEmpty()) {
            single = new Morphism(domain, codomain);
            return;
        }
        
        if (base != null) {
            if (injective && !base.isInjective()) 
                throw new IllegalArgumentException("Base morphism is not injective.");
            if (base.isTotal()) {
                single = base;
                return;
            }              
        }
                
        this.injective = injective;

        allCodEdges = new HashMap<>();
        for (Edge e : cod.getEdges()) {
            List<Edge> labelEdges = allCodEdges.get(e.getLabel());
            if (labelEdges == null) {
                labelEdges = new ArrayList<>();
                allCodEdges.put(e.getLabel(), labelEdges);
            }
            labelEdges.add(e);
        }
                
        domEdges = new ArrayList<>(dom.getEdges());
        if (!domEdges.isEmpty()) {
            Collections.sort(domEdges, labelFrequencyComparator(allCodEdges));
            MorphismFinder.arrangeConnected(domEdges, 1);
        }
        
        domNodes = new ArrayList<>(dom.getNodes());
        if (base != null) {
            baseNodeMap = base.getNodeMap();
            domNodes.removeAll(baseNodeMap.keySet());
            baseEdgeMap = base.getEdgeMap();
            domEdges.removeAll(baseEdgeMap.keySet());
        }
        else {
            baseEdgeMap = null;
            baseNodeMap = null;
        }
        
        elNr = 0;
        candNr = -1;
        nodeMap = new HashMap<>();
        edgeMap = new HashMap<>();                 

        stack = new LinkedList<>();
        done = false;

        setCurCodEdges();
    }
    
    /**
     * Returns the characteristics of this TotalMorphismEnumerator.
     */
    @Override@Pure
    public int characteristics()
    {
        return IMMUTABLE | DISTINCT | NONNULL;
    }
    
    /**
     * Returns a Comparator object which compares edges.
     * Edges are compared with respect to a map which assigns a collection
     * to each label: the first edge is smaller, equal or larger than the
     * second edge if the size of collection assigned to its label is 
     * smaller, equal or larger than the size of the collection assigned
     * to the second's label.
     */
    @SideEffectFree
    private static Comparator<Edge> labelFrequencyComparator(
            final Map<Label,List<Edge>> freqs)
    {
        return new Comparator<>() {
            public int compare(Edge e1, Edge e2) {
                Collection<Edge> ecol1 = freqs.get(e1.getLabel());
                int cnt1 = ecol1 == null ? 0 : ecol1.size();
                Collection<Edge> ecol2 = freqs.get(e2.getLabel());
                int cnt2 = ecol2 == null ? 0 : ecol2.size();
                if (cnt1 < cnt2) 
                    return -1;
                else if (cnt1 == cnt2) 
                    return 0;
                else 
                    return 1;
            }
        };
    }
    
    /**
     * Push the current state to the stack.
     */
    @RequiresNonNull(value={"this.stack", "this.nodeMap", "this.edgeMap"})
    private void pushState() 
    {
        stack.push(new MIStackFrame(
                elNr, candNr, 
                new HashMap<>(nodeMap), 
                new HashMap<>(edgeMap), curCodEdges));
    }
    
    /**
     * Pop a previous state from the stack.
     */
    @RequiresNonNull("stack")
    private void popState()
    {
        MIStackFrame sf = stack.pop();
        if (sf != null) {
            elNr = sf.elNr;
            candNr = sf.candNr;
            curCodEdges = sf.curCodEdges;
            nodeMap = sf.nodeMap;
            edgeMap = sf.edgeMap;
        }
    }
    
    /**
     * Maps an edge to an edge.
     * This method updates {@code edgeMap} so that {@code edge} maps to 
     * {@code cand}.
     * Additionally, it updates {@code nodeMap} so, that it maps all 
     * incident nodes of {@code edge} to the corresponding incident nodes
     * of {@code cand}.
     * <p>
     * If one of the nodes cannot be mapped to the corresponding node
     * (either because it is already mapped to another node, or because its
     * corresponding node is already mapped to and the morphism is supposed
     * to be injective), this method returns {@code false}, otherwise it
     * returns {@code true}.
     * <p>
     * <b>Important:</b> this method changes {@code edgeMap} and 
     * {@code nodeMap} even if it returns {@code false}. 
     */
    @RequiresNonNull(value={"this.nodeMap", "this.edgeMap"})
    private boolean setEdge(Edge edge, Edge cand) 
    {
        assert edge != null;
        assert cand != null;
        
        if (injective) {
            if (edgeMap.containsValue(cand)) return false;
            if (baseEdgeMap != null &&
                baseEdgeMap.containsValue(cand)) return false;
        }
        
        List<Node> edgeNodes = edge.getNodes();
        List<Node> candNodes = cand.getNodes();
        assert edgeNodes.size() == candNodes.size();
        
        for (int i = 0; i < edgeNodes.size(); i++) {
            Node edgeNode = edgeNodes.get(i);
            Node candNode = candNodes.get(i);
            
            Node existingCandNode = nodeMap.get(edgeNode);
            
            if (existingCandNode != candNode && existingCandNode != null) 
                return false; 

            if (!setNode(edgeNode, candNode)) return false;
        }
        
        edgeMap.put(edge, cand);
        return true;
    }
    
    /**
     * Maps a node to an node.
     * This method updates {@code nodeMap} so that {@code node} maps to 
     * {@code cand}.
     * <p>
     * If the node {@code node} can be mapped to {@code cand}, this method
     * returns {@code true}, otherwise it returns {@code false}.
     */
    @RequiresNonNull("this.nodeMap")
    private boolean setNode(Node node, Node cand)
    {
        if (injective) {
            if (!checkNewEntryForInjectivity(node, cand, nodeMap)) return false;
            if (baseNodeMap != null && !checkNewEntryForInjectivity(node, cand, baseNodeMap)) 
                return false;
        }
        
        nodeMap.put(node, cand);
        return true;
    }
    
    /** 
     * Changes the {@code curCorEdges} member variable to the list of all
     * edges with the right label.
     */
    @RequiresNonNull(value={"domEdges", "allCodEdges"})
    private void setCurCodEdges(@UnknownInitialization PartialMorphismEnumerator this)
    {
        if (elNr >= domEdges.size()) {
            curCodEdges = null;
        }
        else {
            curCodEdges = allCodEdges.get(domEdges.get(elNr).getLabel());
            if (curCodEdges == null)
                curCodEdges = Collections.emptyList();
        }
    }
    
    /**
     * Returns the next morphism in the enumeration.
     */
    @RequiresNonNull(value={"this.stack", 
                            "this.domEdges", "this.allCodEdges", "this.curCodEdges",
                            "this.domNodes",
                            "this.nodeMap", "this.edgeMap"})
    private @Nullable Morphism getNextMorphism()
    {
        // Precondition: this method only functions if the domain graph
        // is not empty and the base morphism is not total.
        
        if (domModCount != dom.getModCount()) {
            throw new ConcurrentModificationException(MorphismFinder.EXC_DOMAIN_CHANGED);
        }
        if (codModCount != cod.getModCount()) {
            throw new ConcurrentModificationException(MorphismFinder.EXC_CODOMAIN_CHANGED);
        }
        
        while (true) {
            if (elNr < domEdges.size()) {
                if (candNr >= castNonNull(curCodEdges).size()) {
                    if (stack.size() > 0) {
                        popState();
                        candNr++;
                        continue;
                    }
                    else {
                        return null;
                    }
                }
            
                Edge edge = domEdges.get(elNr);
            
                if (candNr >= 0) {
                    // map current edge to candidate edge
                    Edge cand = curCodEdges.get(candNr);
                    pushState();
                    if (setEdge(edge, cand)) {
                        elNr++;
                        setCurCodEdges();
                        candNr = -1;
                        continue;
                    }
                    else {
                        popState();
                        candNr++;
                    }
                }
                else {
                    // do not map current edge (when finding non-total morphisms)
                    pushState();
                    elNr++;
                    setCurCodEdges();
                    candNr = -1;
                }
            }
            else {
                // ignore nodes which are already mapped to something
                int nodeNr = elNr - domEdges.size();
                while (nodeNr < domNodes.size()) {
                    Node node = domNodes.get(nodeNr);
                    if (!nodeMap.containsKey(node) &&
                        (baseNodeMap == null || !baseNodeMap.containsKey(node))) {
                        break;
                    }
                    elNr++;
                    nodeNr++;
                }
                
                if (nodeNr >= domNodes.size()) {
                    Morphism m = new Morphism(dom, cod);
                    m.edgeMap = new HashMap<>(edgeMap);
                    m.nodeMap = new HashMap<>(nodeMap);
                    if (baseEdgeMap != null) {
                        m.edgeMap.putAll(baseEdgeMap);
                        m.nodeMap.putAll(castNonNull(baseNodeMap));
                    }
                    m.domainAddCount = dom.getAddCount();
                    m.domainDelCount = dom.getDelCount();
                    m.codomainAddCount = cod.getAddCount();
                    m.codomainDelCount = cod.getDelCount();
                    if (injective) m.injective = ThreeBool.TRUE;
                    popState();
                    candNr++;
                    return m;
                }
                else {
                    if (candNr >= codNodes.size()) {
                        if (stack.size() > 0) {
                            popState();
                            candNr++;
                            continue;
                        }
                        else {
                            return null;
                        }
                    }
                    
                    Node node = domNodes.get(nodeNr);

                    if (candNr >= 0) {
                        // map current node to candidate node
                        Node cand = codNodes.get(candNr);
                        pushState();
                        if (setNode(node, cand)) {
                            elNr++;
                            candNr = -1;
                            continue;
                        }
                        else {
                            popState();
                            candNr++;
                        }
                    }
                    else {
                        // do not map current edge (when finding non-total morphisms)
                        pushState();
                        elNr++;
                        candNr = -1;
                    }
                }
            }
        }
    }
    
    
    @Override
    public @Nullable Morphism next()
    {
        if (done) return null;
        
        if (single != null) {
            Morphism s = single;
            single = null;
            done = true;
            return s;
        }
        
        assert stack != null : "@AssumeAssertion(nullness)";
        assert domEdges != null : "@AssumeAssertion(nullness)";
        assert allCodEdges != null : "@AssumeAssertion(nullness)";
        // assert curCodEdges != null : "@AssumeAssertion(nullness)"; // XXX figure out whats wrong
        assert domNodes != null : "@AssumeAssertion(nullness)";
        assert codNodes != null : "@AssumeAssertion(nullness)";
        assert nodeMap != null : "@AssumeAssertion(nullness)";
        assert edgeMap != null : "@AssumeAssertion(nullness)";
        
        Morphism result = getNextMorphism();
        if (result == null) done = true;
        
        return result;
    }
    
    /**
     * Returns {@code true} if there is either no object which is mapped to
     * {@code cand} by {@code map}, or this object is equal to {@code el}.
     * <p>
     * <strong>Precondition:</strong> It is assumed that {@code map} is 
     * injective.
     */
    @SideEffectFree
    private static <T> boolean checkNewEntryForInjectivity(T el, T cand, Map<T,T> map)
    {
        for (Map.Entry<@KeyFor("map") T, @KeyFor("map") T> entry : map.entrySet()) {
            if (entry.getValue() == cand) {
                return entry.getKey() == el;
            }
        }
        return true;
    }
}
