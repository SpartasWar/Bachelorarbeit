package de.uni_due.inf.ti.graph;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.util.Enumerator;

/**
 * Enumerator which enumerates subgraphs of a given graph. The objects 
 * returned by this Enumerator are morphisms with the subgraph as domain and
 * the given graph as codomain.
 * 
 * @author H.J. Sander Bruggink
 */
class SubgraphEnumerator extends Enumerator<Morphism>
{
    private Enumerator<Morphism> morphismEnum;

    /**
     * Creates a new SubgraphEnumerator which enumerates subgraphs of a given
     * graph.
     * 
     * @param graph
     *      graph of which the new SubgraphEnumerator will enumerate the
     *      subgraphs
     */
    public SubgraphEnumerator(Graph graph)
    {
        MorphismFinder factory = new MorphismFinder();
        factory.setTotal(false);
        factory.setInjective(true);
        morphismEnum = factory.enumerate(graph, graph);
    }
    
    /**
     * Returns the next subgraph.
     */
    @Override
    public @Nullable Morphism next()
    {
        Morphism morph = morphismEnum.next();
        if (morph == null) {
            return null;
        }
        else {
            Set<Node> nodeSet = morph.getNodeMap().keySet();
            Set<Edge> edgeSet = morph.getEdgeMap().keySet();
            return morph.getDomain().getInclusion(nodeSet, edgeSet);
        }
    }
}
