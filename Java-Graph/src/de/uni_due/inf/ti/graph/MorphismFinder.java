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

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.util.Enumerator;
import de.uni_due.inf.ti.util.Enumerators;

/**
 * The MorphismFinder provides tools to enumerate morphisms from one graph
 * to another.
 * 
 * @author H.J. Sander Bruggink
 */
public class MorphismFinder
{
    static final String EXC_NULL_DOMAIN         = "Domain must be non-null";
    static final String EXC_NULL_CODOMAIN       = "Codomain must be non-null";
    static final String EXC_DOMAIN_CHANGED      = "The domain of this morphism has been changed.";
    static final String EXC_CODOMAIN_CHANGED    = "The codomain of this morphism has been changed.";
    static final String EXC_DOM_COD_MISMATCH    = "Domain and codomain of base morphism do not match.";
    
    private boolean total;
    private boolean injective;
    private boolean iso;
    
    /**
     * Creates a new MorphismFinder instance.
     */
    @SideEffectFree
    public MorphismFinder()
    {
        total = true;
        injective = false;
        iso = false;
    }
    
    /**
     * Sets whether morphisms created by this MorphismFinder are total.
     * Setting this property does not change the behavior of enumerators and
     * streams created previously.
     * <p>
     * The default value of this property is {@code true}.
     * 
     * @param b
     *      whether or not only total morphisms are created
     */
    public void setTotal(boolean b) 
    {
        total = b;
        if (!b) iso = false;
    }
    
    /**
     * Returns whether morphisms created by this MorphismFinder are total.
     * The default value of this property is {@code true}.
     * 
     * @return
     *      whether create enumerators enumerate only total morphisms
     */
    @Pure
    public boolean isTotal()
    {
        return total;
    }
    
    /**
     * Sets whether morphisms created by this MorphismFinder are injective.
     * Setting this property does not change the behavior of enumerators and
     * streams created previously.
     * <p>
     * The default value of this property is {@code false}.
     * 
     * 
     * @param b
     *      whether create enumerators enumerate only injective morphisms
     */
    public void setInjective(boolean b)
    {
        injective = b;
        if (!b) iso = false;
    }
    
    /**
     * Returns whether morphisms created by this MorphismFinder are injective.
     * enumerate only injective morphisms.
     * The default value of this property is {@code false}.
     * 
     * @return
     *      whether create enumerators enumerate only injective morphisms
     */
    @Pure
    public boolean isInjective()
    {
        return injective;
    }
    
    /**
     * Sets whether morphisms created by this MorphismFinder are isomorphisms.
     * If this property is set to {@code true}, then also {@code setTotal(true)}
     * and {@code setInjective(true)} are called.
     * <p>
     * Setting this property does not change the behavior of enumerators and
     * streams created previously.
     * <p>
     * The default value of this property is {@code false}.
     * 
     * @param b
     *      whether create enumerators enumerate only isomorphisms
     */
    public void setIsomorphic(boolean b)
    {
        iso = b;
        if (b) {
            total = true;
            injective = true;
        }
    }
    
    /**
     * Returns whether morphisms created by this MorphismFinder are isomorphisms.
     * The default value of this property is {@code false}.
     */
    @Pure
    public boolean isIsomorphic()
    {
        return iso;
    }
    
    /**
     * Creates a morphism enumerator which enumerates those morphisms between
     * two graphs, that have the specified properties and extend a given base
     * morphism.
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>. 
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   MorphismFinder finder = new MorphismFinder();
     *   ... // set properties of the finder
     *   
     *   for (Morphism m : factory.enumerate(g1, g2, base)) { 
     *     ... 
     *   }
     * </pre>
     * 
     * @param domain
     *      domain of the enumerated morphisms
     * @param codomain
     *      codomain of the enumerated morphisms
     * @param base
     *      base morphism
     * @return
     *      a morphism enumerator which enumerates the specified morphisms
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public Enumerator<Morphism> enumerate(
            Graph domain, 
            Graph codomain, 
            @Nullable Morphism base)
    {
        if (domain == null) throw new NullPointerException(EXC_NULL_DOMAIN);
        if (codomain == null) throw new NullPointerException(EXC_NULL_CODOMAIN);
        
        if (base != null) {
            if (base.getDomain() != domain ||
                base.getCodomain() != codomain) 
                throw new IllegalArgumentException(EXC_DOM_COD_MISMATCH);
        }

        if (iso) {
            if (!isomorphismPossible(domain, codomain)) {
                return Enumerators.emptyEnumerator();
            }
            Enumerator<Morphism> e = new TotalMorphismEnumerator(
                    domain, codomain, injective, iso, base);    
            return e.filter((Morphism m) -> m.isIsomorphism());
        }
        else if (total) {
            if (injective && !injectionPossible(domain, codomain)) 
                return Enumerators.emptyEnumerator();
            if (!injective && codomain.isEmpty() && !domain.isEmpty()) {
                return Enumerators.emptyEnumerator();
            }
            Enumerator<Morphism> e = new TotalMorphismEnumerator(
                    domain, codomain, injective, iso, base);
            return e;
        }
        else {
			/*
			 * FIXME: partial morphism has a strange bug when a base morphism is given,
			 * it might emit invalid morphisms that have an edge mapped inconsistently to
			 * its adjacent nodes.
			 *
			 * Testcase:
			 * gL = graph { n0 --e01:A-> n1; n0 --e02:A-> n2; };
			 * gR = graph { n0 --e01:A-> n1; n2 --e45:A-> n3; };
			 * base = morphism from gL to gR { n0 => n0; n1 => n1; e01 => e01; };
			 * and then ask for injective partial morphisms from gL to gR extending base
			 *
			 * Current workaround is to filter out the illegal morphisms and hope for the best
			 */
			return new PartialMorphismEnumerator(domain, codomain, injective, base)
			.filter(m -> {
				for (Entry<Edge,Edge> entry : m.getEdgeMap().entrySet()) {
					Edge e1 = entry.getKey();
					Edge e2 = entry.getValue();
					List<Node> en1 = e1.getNodes();
					List<Node> en2 = e2.getNodes();
					if (en1.size() != en2.size()) {
						// arity mismatch
						return false;
					}
					for (int i = 0; i < en1.size(); i++) {
						if (m.get(en1.get(i)) != en2.get(i)) {
							// inconsistent mapping
							return false;
						}
					}
				}
				return true;
			});
        }
    }
    
    /**
     * Creates a morphism enumerator which enumerates those morphisms between
     * two graphs, that have the specified properties.
     * <p>
     * Typically, this method is used in code like:
     * <p> 
     * <pre>
     *   MorphismFinder finder = new MorphismFinder();
     *   ... // set properties of the finder
     *   
     *   for (Morphism m : factory.enumerate(g1, g2)) { 
     *     ... 
     *   }
     * </pre>
     * Calling {@code factory.create(domain, codomain)} is equivalent to
     * calling {@code factory.create(domain, codomain, null)}.
     * 
     * @param domain
     *      domain of the enumerated morphisms
     * @param codomain
     *      codomain of the enumerated morphisms
     * @return
     *      a morphism enumerator which enumerates the specified morphisms
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public Enumerator<Morphism> enumerate(Graph domain, Graph codomain)
    {
        return enumerate(domain, codomain, null);
    }

    /**
     * Creates a stream of morphisms between two graphs that have the specified
     * properties and extend a given base morphism.
     * A morphism <i>m</i> is an extension of a morphism <i>m'</i>, when it
     * has the same domain and codomain, and each node and edge mapped by
     * <i>m'</i> is mapped to the same node or edge by <i>m</i>.
     *  
     * @param domain
     *      domain of the morphisms in the stream
     * @param codomain
     *      codomain of the morphisms in the stream
     * @param base
     *      base morphism, or {@code null} if no morphism is to be extended
     * @return
     *      stream of morphisms from {@code domain} to {@code codomain} which
     *      extend the morphism {@code base} (if non-null)
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public Stream<Morphism> stream(Graph domain, Graph codomain, Morphism base)
    {
        return StreamSupport.stream(enumerate(domain, codomain, base), false);
    }
    
    /**
     * Creates a stream of morphisms between two graphs that have the specified
     * properties.
     *  
     * @param domain
     *      domain of the morphisms in the stream
     * @param codomain
     *      codomain of the morphisms in the stream
     * @return
     *      stream of morphisms from {@code domain} to {@code codomain}
     * @throws NullPointerException
     *      if {@code domain} or {@code codomain} is {@code null}
     */
    @SideEffectFree
    public Stream<Morphism> stream(Graph domain, Graph codomain)
    {
        return StreamSupport.stream(enumerate(domain, codomain), false);        
    }

    /**
     * Arrange the nodes in a list of nodes such that each edge has as most
     * nodes in common with the previous ones as possible.
     * <p>
     * Currently, this method runs in O(n^2), where n is the size of the list.
     */
    static void arrangeConnected(List<Edge> edges, int startIdx)
    {
        if (startIdx >= edges.size()) return; // nothing to do
        
        Set<Node> seen = new HashSet<>();
        for (int i = 0; i < startIdx; i++) {
            seen.addAll(edges.get(i).getNodes());
        }
        
        for (int start = startIdx; start < edges.size() - 1; start++) {            
            // look for the edge which has the most nodes in common with 
            // "old" edges
            int max = -1;
            int best = start;
            for (int i = start; i < edges.size(); i++) {
                int cnt = 0;
                for (Node n : edges.get(i).getNodes()) {
                    if (seen.contains(n)) cnt++;
                }
                if (cnt > max) {
                    best = i;
                    max = cnt;
                }
            }
            // move best edge to the current position
            if (best != start) {
                seen.addAll(edges.get(best).getNodes());
                Edge e = edges.remove(best);
                edges.add(start, e);
            }
        }
    }

    /* ---------------------------------------------------------------------- *
     *                       Static Utility Methods                           *
     * ---------------------------------------------------------------------- */

    /**
     * Fast check if an injective morphism can exist from {@code graph1} to
     * {@code graph2}. If this method returns {@code false}, such a morphism
     * cannot exist; if it returns {@code true}, such a morphism may or may
     * not exist so another (more time-consuming) morphism finder must still
     * be performed.
     * 
     * @param graph1
     *      first graph
     * @param graph2
     *      second graph
     * @return 
     *      {@code true} if it is possible that an injective morphism from
     *      {@code graph1} to {@code graph2} exists
     * @throws NullPointerException
     *      when {@code graph1} or {@code graph2} is {@code null}
     */
    @Pure
    private static boolean injectionPossible(Graph graph1, Graph graph2) 
    {
        if (graph1.nodes.size() > graph2.nodes.size() || 
                graph1.edges.size() > graph2.edges.size()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Fast check if {@code graph1} can be isomorphic to {@code graph2}.
     * If this method returns {@code false}, {@code graph1} and {@code graph2}
     * cannot be isomorphic; if it returns {@code true} they may or may not
     * be isomorphic, so another (more time-consuming) isomorphism check
     * must be performed.
     * 
     * @param graph1
     *      first graph
     * @param graph2
     *      second graph
     * @return 
     *      {@code true} if it is possible that an isomorphism from
     *      {@code graph1} to {@code graph2} exists
     * @throws NullPointerException
     *      when {@code graph1} or {@code graph2} is {@code null}
     */
    @Pure
    private static boolean isomorphismPossible(Graph graph1, Graph graph2) 
    {
        if (graph1.nodes.size() != graph2.nodes.size() || 
                graph1.edges.size() != graph2.edges.size() || 
                graph1.getIsoHash() != graph2.getIsoHash())
            return false;
        
        return true;
    }
}
