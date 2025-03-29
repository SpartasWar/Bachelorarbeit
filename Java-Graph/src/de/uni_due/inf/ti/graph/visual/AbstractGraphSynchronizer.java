package de.uni_due.inf.ti.graph.visual;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxExplicitNode;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxLabelNode;
import de.uni_due.inf.ti.visigraph.VxNode;
import de.uni_due.inf.ti.visigraph.swing.GraphEditSynchronizer;

/**
 * @deprecated It appears that this implementation is not used anywhere.
 */
@Deprecated
public abstract class AbstractGraphSynchronizer implements GraphEditSynchronizer
{
    protected abstract boolean getBinaryArrows();
    
    protected abstract String getNodeStyleName();
    
    protected abstract String getEdgeStyleName();
    
    protected abstract String getHyperedgeStyleName();
    
    protected abstract String getConnectionStyleName();
    
    protected abstract String getLabelStyleName();
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPossibleSourceNode(VxNode source)
    {
        return !(source instanceof VxLabelNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddNode(VxGraph graph)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddEdge(VxNode source, VxNode target)
    {
        if (source instanceof VxLabelNode || target instanceof VxLabelNode) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddLabel(VxEdge edge)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canEditLabel(VxNode node)
    {
        if (node instanceof VxLabelNode) {
            VxEdge edge = ((VxLabelNode) node).getEdge();
            return edge.getReference() != null;
        }
        else return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(VxGraph graph, Point2D point)
    {
        Object ref = graph.getReference();
        if (ref == null || !(ref instanceof Graph)) {
            throw new IllegalStateException();
        }
        Graph mgraph = (Graph)ref;
        
        Node mnode = mgraph.addNode();
        VxNode node = graph.addNode(point);
        node.setStyle(getNodeStyleName());
        node.setReference(mnode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addEdge(VxNode source, VxNode target)
    {
        if (source == null || target == null) 
            throw new NullPointerException("source and target must be non-null");
        VxGraph graph = source.getGraph();
        if (target.getGraph() != graph) 
            throw new IllegalArgumentException("source and target must belong "
                    + "to the same graph");
        if (!(source instanceof VxExplicitNode) || !(target instanceof VxExplicitNode))
            return; // nothing to do
                
        Object srcRef = source.getReference();
        Object tgtRef = target.getReference();
        
        if (srcRef instanceof Edge && tgtRef instanceof Node) {
            int number = addConnection((Edge)srcRef, (Node)tgtRef);
            VxEdge newEdge = graph.addEdge(source, target, getConnectionStyleName());
            VxLabelNode label = newEdge.addLabel(Integer.toString(number));
            label.setPositionOnEdge(.3);
            label.setStyle(getLabelStyleName());
        }
        else if (srcRef instanceof Node && tgtRef instanceof Edge) {
            int number = addConnection((Edge)tgtRef, (Node)srcRef);
            VxEdge newEdge = graph.addEdge(target, source, getConnectionStyleName());
            VxLabelNode label = newEdge.addLabel(Integer.toString(number));
            label.setPositionOnEdge(.3);
            label.setStyle(getLabelStyleName());
        }
        else if (srcRef instanceof Node && tgtRef instanceof Node) {
            Node mSrc = (Node)srcRef;
            Node mTgt = (Node)tgtRef;
            Graph mGraph = mSrc.getGraph();
            
            VxExplicitNode eSrc = (VxExplicitNode)source;
            VxExplicitNode eTgt = (VxExplicitNode)target;
            
            Edge mEdge = mGraph.addEdge(new Label(""), mSrc, mTgt);
            
            if (getBinaryArrows()) {
                VxEdge edge = graph.addEdge(eSrc, eTgt, getEdgeStyleName());
                edge.setStyle(getEdgeStyleName());
                VxLabelNode node = edge.addLabel("");
                node.setStyle(getLabelStyleName());
            }
            else {
                Point2D srcPos = eSrc.position();
                Point2D tgtPos = eTgt.position();
                double newX = (srcPos.getX() + tgtPos.getX()) / 2;
                double newY = (srcPos.getY() + tgtPos.getY()) / 2;
                VxNode he = graph.addNode(newX, newY);
                he.setStyle(getHyperedgeStyleName());
                he.setReference(mEdge);
                VxEdge con = graph.addEdge(he, eSrc, getConnectionStyleName());
                VxLabelNode label = con.addLabel("0");
                label.setPositionOnEdge(.3);
                label.setStyle(getLabelStyleName());
                con = graph.addEdge(he, eTgt, getConnectionStyleName());
                label = con.addLabel("1");
                label.setPositionOnEdge(.3);
                label.setStyle(getLabelStyleName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLabel(VxEdge edge, String label)
    {
        // Do nothing: not supported.
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void editLabel(VxNode node, String newLabel)
    {
        Object ref = node.getReference();
        if (ref instanceof Edge) {
            Edge edge = (Edge)ref;
            Graph graph = edge.getGraph();
            Edge newEdge = graph.addEdge(new Label(newLabel), edge.getNodes());
            graph.removeEdge(edge);
            node.setReference(newEdge);
            node.setLabel(newLabel);
        }
    }
    
    private static int addConnection(Edge edge, Node node)
    {
        Graph graph = edge.getGraph();
        ArrayList<Node> nodes = new ArrayList<>(edge.getNodes());
        int result = nodes.size();
        nodes.add(node);
        nodes.trimToSize();
        graph.addEdge(edge.getLabel(), nodes);
        graph.removeEdge(edge);
        return result;
    }
}
