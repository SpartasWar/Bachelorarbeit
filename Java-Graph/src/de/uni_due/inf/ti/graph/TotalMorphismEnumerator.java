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
import java.util.Set;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.graph.util.ThreeBool;

/**
 * Instances of this class enumerate total morphisms between two graphs.
 * Instances of this class are not supposed to be created directly; use
 * {@link MorphismFinder} to create instances of this class.
 * 
 * @author H.J. Sander Bruggink
 */
class TotalMorphismEnumerator extends Enumerator<Morphism>
{    
    /** 
     * Stack frame for implementing recursion.
     */
    /** 
     * Stack frame for implementing recursion.
     */
    private static class MIStackFrame
    {
        int edgeNr, candNr;
        Map<Node,Node> nodeMap;
        Map<Edge,Edge> edgeMap;       
        @Nullable List<Edge> curCodEdges;

        MIStackFrame(
                int edgeNr, int candNr, 
                Map<Node,Node> nodeMap, 
                Map<Edge,Edge> edgeMap, 
                @Nullable List<Edge> curCodEdges)
        {
            this.edgeNr = edgeNr;
            this.candNr = candNr;
            this.nodeMap = nodeMap;
            this.edgeMap = edgeMap;
            this.curCodEdges = curCodEdges;
        }        
    }

    // information about domain graph
    private Graph dom;
    private @MonotonicNonNull List<Edge> domEdges;
    private @MonotonicNonNull List<Node> isoNodes;
    private long domModCount;

    // information about codomain graph
    private Graph cod;
    private @MonotonicNonNull Map<Label,List<Edge>> allCodEdges;
    private @Nullable List<Edge> curCodEdges;
    private @Nullable List<Node> codNodes;
    private long codModCount;
    
    // data structure used for mapping isolated nodes
    int[] isoMaps = new int[0];

    // flags 
    private boolean injective;  // only injective morphisms?
    private boolean isoHash;    // check iso hash when mapping? (should only be true if injective == true)

    // local variables for morphism finding
    private @MonotonicNonNull Deque<MIStackFrame> stack;
    private int edgeNr, candNr;
    private @MonotonicNonNull Map<Node,Node> nodeMap;
    private @MonotonicNonNull Map<Edge,Edge> edgeMap;
    
    // local variables for iteration
    private boolean done;
    
    // special case for when it is immediately clear that only a single
    // morphism can be found
    private @Nullable Morphism single = null;
    
    
    @SideEffectFree
    TotalMorphismEnumerator(
            Graph domain, 
            Graph codomain, 
            boolean injective, 
            boolean isoHash, 
            @Nullable Morphism base) 
    {
        dom = domain;
        domModCount = domain.getModCount();
        
        cod = codomain;
        codModCount = codomain.getModCount();
        
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
        this.isoHash = isoHash;
        
        allCodEdges = new HashMap<>();
        for (Edge e : cod.getEdges()) {
            List<Edge> labelEdges = allCodEdges.get(e.getLabel());
            if (labelEdges == null) {
                labelEdges = new ArrayList<>();
                allCodEdges.put(e.getLabel(), labelEdges);
            }
            labelEdges.add(e);
        }
        
        if (base == null) {
            domEdges = new ArrayList<>(dom.getEdges());
            if (!domEdges.isEmpty()) {
                Collections.sort(domEdges, labelFrequencyComparator(allCodEdges));
                MorphismFinder.arrangeConnected(domEdges, 1);
            }
            edgeNr = 0;
            candNr = 0;
            nodeMap = new HashMap<>();
            edgeMap = new HashMap<>();                 
        }
        else {
            Set<Edge> baseEdges = base.getEdgeMap().keySet();
            domEdges = new ArrayList<>(baseEdges);
            int startIdx = domEdges.size();
            for (Edge e : dom.getEdges()) {
                if (!baseEdges.contains(e)) 
                    domEdges.add(e);
            }
            MorphismFinder.arrangeConnected(domEdges, startIdx);
            edgeNr = startIdx;
            candNr = 0;
            nodeMap = new HashMap<>(base.getNodeMap());
            edgeMap = new HashMap<>(base.getEdgeMap());
        }
        
        setCurCodEdges();

        isoNodes = new ArrayList<>(dom.getIsolatedNodes());
        if (base != null) isoNodes.removeAll(base.getNodeMap().keySet());
        if (!isoNodes.isEmpty()) {
            codNodes = new ArrayList<>(cod.getNodes());
            isoMaps = new int[isoNodes.size()];
            for (int i = 0; i < isoMaps.length; i++)
                isoMaps[i] = 0;
        }
        
        stack = new LinkedList<>();
        done = false;
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
    @Pure
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
        MIStackFrame sf = new MIStackFrame(
                edgeNr, 
                candNr, 
                new HashMap<>(nodeMap), 
                new HashMap<>(edgeMap), curCodEdges);
        stack.push(sf);
    }
    
    /**
     * Pop a previous state from the stack.
     */
    @RequiresNonNull("this.stack")
    private void popState()
    {
        MIStackFrame sf = stack.pop();
        if (sf != null) {
            edgeNr = sf.edgeNr;
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
        if (injective && edgeMap.containsValue(cand)) return false;
        
        List<Node> edgeNodes = edge.getNodes();
        List<Node> candNodes = cand.getNodes();
        assert edgeNodes.size() == candNodes.size();
        
        for (int i = 0; i < edgeNodes.size(); i++) {
            Node edgeNode = edgeNodes.get(i);
            Node candNode = candNodes.get(i);
            
            if (isoHash && edgeNode.getIsoHash() != candNode.getIsoHash())
                return false;
            
            Node existingCandNode = nodeMap.get(edgeNode);
            
            if (existingCandNode != candNode) {
                if (existingCandNode != null) 
                    return false;
                if (injective && nodeMap.containsValue(candNode)) 
                    return false;
            }
            
            nodeMap.put(edgeNode, candNode);
        }
        
        edgeMap.put(edge, cand);
        return true;
    }
    
    /** 
     * Changes the {@code curCorEdges} member variable to the list of all
     * edges with the right label.
     */
    @RequiresNonNull(value={"domEdges", "allCodEdges"})
    private void setCurCodEdges(@UnknownInitialization TotalMorphismEnumerator this)
    {
        if (edgeNr >= domEdges.size()) {
            curCodEdges = null;
        }
        else {
            curCodEdges = allCodEdges.get(domEdges.get(edgeNr).getLabel());
            if (curCodEdges == null)
                curCodEdges = Collections.emptyList();
        }
    }
    
    /**
     * Returns the next morphism in the enumeration.
     */
    @RequiresNonNull(value={"this.stack", 
                            "this.domEdges", "this.allCodEdges", "this.curCodEdges",
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
            if (edgeNr >= domEdges.size()) { 
                if (isoNodes == null || isoNodes.isEmpty()) {
                    Map<Edge,Edge> newEdgeMap = new HashMap<>(edgeMap);
                    Map<Node,Node> newNodeMap = new HashMap<>(nodeMap);
                    Morphism m = new Morphism(dom, cod, newNodeMap, newEdgeMap);
                    m.total = ThreeBool.TRUE;
                    if (injective) m.injective = ThreeBool.TRUE;
                    popState();
                    candNr++;
                    return m;
                }
                else {
                    boolean overflow = false;
                    Morphism m;
                    do {
                        Map<Edge,Edge> newEdgeMap = new HashMap<>(edgeMap);
                        Map<Node,Node> newNodeMap = new HashMap<>(nodeMap);
                        for (int i = 0; i < isoMaps.length; i++) {
                            newNodeMap.put(isoNodes.get(i), castNonNull(codNodes).get(isoMaps[i]));
                        }
                        m = new Morphism(dom, cod, newNodeMap, newEdgeMap);
                        m.total = ThreeBool.TRUE;
                        overflow = Utils.next(isoMaps, castNonNull(codNodes).size());
                        if (injective && !m.isInjective()) m = null;
                    } while (!overflow && m == null); 
                        // TODO: optimize loop for when injective == true
                    if (overflow) {
                        if (stack.isEmpty()) {
                            done = true;
                            return m;
                        }
                        popState();
                        candNr++;
                    }
                    if (m != null) return m;
                }
            }

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
            
            Edge edge = domEdges.get(edgeNr);
            
            // map current edge to candidate edge
            Edge cand = curCodEdges.get(candNr);
            if (!isoHash || edge.getIsoHash() == cand.getIsoHash()) {
                pushState();
                if (setEdge(edge, cand)) {
                    edgeNr++;
                    setCurCodEdges();
                    candNr = 0;
                    continue;
                }
                else popState();
            }
            candNr++;
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
        // assert curCodEdges != null : "@AssumeAssertion(nullness)"; // XXX see below
        // assert codNodes != null : "@AssumeAssertion(nullness)"; // XXX this assertion sometimes does not hold but it does not appear to hurt - figure out what's wrong
        assert nodeMap != null : "@AssumeAssertion(nullness)";
        assert edgeMap != null : "@AssumeAssertion(nullness)";

        Morphism result = getNextMorphism();
        if (result == null) done = true;
        
        return result;
    }
}
