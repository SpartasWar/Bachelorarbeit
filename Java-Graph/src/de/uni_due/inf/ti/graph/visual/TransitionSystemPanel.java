package de.uni_due.inf.ti.graph.visual;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Transition;
import de.uni_due.inf.ti.visigraph.Layouter;
import de.uni_due.inf.ti.visigraph.LevelLayouter;
import de.uni_due.inf.ti.visigraph.StyleMap;
import de.uni_due.inf.ti.visigraph.VxComponent;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.swing.GraphPanel;

/**
 * A panel which displays a transition system.
 * 
 * @author H.J. Sander Bruggink
 */
public class TransitionSystemPanel extends JComponent
{
    /** For serializing. */
	private static final long serialVersionUID = -3956900483893815385L;

	/** Visualization cache. */
    private WeakHashMap<Object,VxGraph> visis;
    
    /** GraphPanel used to display the system. */
    private GraphPanel graphPanel;
    
    /** Object which is displayed in the associated state panel. */
    private Object displayedObject = null;
    
    /** GraphPanel used to display the selected states and transitions. */
    private GraphPanel statePanel = null;
    
    /** Visualizer which is used to create and maintain visualizations of
        graphs. */
    private GraphVisualizer visualizer;
    
    /** Layouter which is used to layout graphs. */
    private Layouter layouter;
    
    /** 
     * Creates a new TransitionSystemPanel instance with a shared visualization
     * cache. 
     * 
     * @param cache
     *      cache for visualizations of objects; if {@code null} a new cache
     *      is created, otherwise the given cache is used so that components
     *      can share their visualization cache.
     */
    public TransitionSystemPanel(WeakHashMap<Object,VxGraph> cache)
    {
        visis = (cache != null) ? cache : new WeakHashMap<Object,VxGraph>();
        graphPanel = new GraphPanel();
        graphPanel.addItemListener(new MyItemListener());
        
        visualizer = new GraphVisualizer();
        visualizer.addDefaultStyles(graphPanel.getDrawer().getStyleMap());
        
        layouter = new LevelLayouter();
        
        setLayout(new BorderLayout());
        add(graphPanel, BorderLayout.CENTER);
    }
    
    
    /**
     * Creates a new TransitionSystemPanel instance. 
     * The new instance has it own visualization cache.
     */
    public TransitionSystemPanel()
    {
        this(null);
    }
    

    /**
     * Clears the cache used to store visualizations of objects.
     */
    public void clearVisualizationCache()
    {
        visis.clear();
    }
    
    
    /**
     * Sets the GraphPanel where selected states and transitions are displayed.
     */
    public void setConnectedPanel(GraphPanel panel)
    {
        statePanel = panel;
        if (displayedObject instanceof Graph) {
            setDisplayedGraph((Graph)displayedObject);
        }
        else if (displayedObject instanceof Transition) {
            setDisplayedTransition((Transition)displayedObject);
        }
        else {
            displayedObject = null;
        }
    }
    
    
    /**
     * Returns the GraphPanel where selected states and transitions are
     * displayed.
     */
    public GraphPanel getConnectedPanel()
    {
        return statePanel;
    }
    
    
    /**
     * Returns the visualizer objects which is used to generate visualizations
     * of graphs, transitions and transition systems.
     */
    public GraphVisualizer getVisualizer()
    {
        return visualizer;
    }
    

    /**
     * Sets the visualizer objects which is used to generate visualizations
     * of graph, transitions and transition systems.
     * 
     * @param vis
     *      the new visualizer object
     * @throws NullPointerException
     *      when {@code vis == null}
     */
    public void setVisualizer(GraphVisualizer vis)
    {
        visualizer = vis;
        StyleMap sm = graphPanel.getStyleMap();
        sm.clear();
        visualizer.addDefaultStyles(sm);
    }
    
    
    /**
     * Returns the layouter object which is used to calculate positions for
     * the nodes and edges of the visualizations.
     */
    public Layouter getLayouter()
    {
        return layouter;
    }
    
    
    /**
     * Sets the layouter object which is used to calculate positions for
     * the nodes and edges of the visualizations.
     * 
     * @param l
     *      the new layouter used by this TransitionSystemPanel
     * @throws NullPointerException
     *      when {@code l} is {@code null}
     */
    public void setLayouter(Layouter l)
    {
        if (l == null) throw new NullPointerException();
        layouter = l;
    }
    
    
    /**
     * Sets the transition system which is displayed by this 
     * TransitionSystemPanel.
     * This method generates a visualization graph for the system (or tries
     * to obtain a visualization graph from the cache) and displays this graph.
     * Changes made to the transition system after calling this method are
     * not reflected by the component.
     * 
     * @param system
     *      the transition system which is to be displayed by this panel
     */
    public void setSystem(ExplicitTransitionSystem system)
    {
        if (system == null) {
            clearStatePanel();
            graphPanel.setGraph(null);
        }
        else {
            VxGraph visi = visis.get(system);
            if (visi == null) {
                visi = visualizer.visualizeSystem(system);
                layouter.layoutGraph(visi);
                visis.put(system, visi);
            }
            graphPanel.setGraph(visi);
        }
        clearStatePanel();
    }
    

    /**
     * Sets the visualization of an object to a custom visualization.
     * 
     * @param o
     *      object for which the custom visualization is set
     * @param visi
     *      visualization for the object {@code o}
     * @throws NullPointerException
     *      when either {@code o} or {@code visi} is {@code null}
     */
    public void setVisualization(Object o, VxGraph visi)
    {
        if (o == null || visi == null) throw new NullPointerException();
        
        visis.put(o, visi);
        
        if (displayedObject == o) {
            statePanel.setGraph(visi);
        }
    }
    
    
    /**
     * Returns the style map used by this TransitionSystemPanel. The style
     * map is modifiable and changes are reflected by the panel.
     */
    public StyleMap getStyleMap()
    {
        return graphPanel.getStyleMap();
    }
    
    
    /**
     * Displays a graph in the associated state panel.
     */
    private void setDisplayedGraph(Graph graph)
    {
        if (graph == null) {
            clearStatePanel();
        }
        if (statePanel != null) {
            displayedObject = graph;
            VxGraph visi = visis.get(graph);
            if (visi == null) {
                visi = visualizer.visualizeGraph(graph);
                layouter.layoutGraph(visi);
                visis.put(graph, visi);
            }
            statePanel.setGraph(visi);
        }
    }
    

    /**
     * Displays a transition in the associated state panel.
     */
    private void setDisplayedTransition(Transition trans)
    {
        if (trans == null) {
            clearStatePanel();
        }
        else if (statePanel != null) {
            displayedObject = trans;
            VxGraph visi = visis.get(trans);
            if (visi == null) {
                visi = visualizer.visualizeFlatMorphism(trans.getMorphism());
                layouter.layoutGraph(visi);
                visis.put(trans, visi);
            }
            statePanel.setGraph(visi);
        }
    }
    
    
    /**
     * Clears the associated state panel.
     */
    private void clearStatePanel()
    {
        displayedObject = null;
        if (statePanel != null) statePanel.setGraph(null);
    }
    
    
    /**
     * Listener which listens to selection events.
     */
    private class MyItemListener implements ItemListener
    {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            if (graphPanel.getSelectedObjects().length == 1) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    VxComponent component = (VxComponent)e.getItem();
                    Object ref = component.getReference();
                    if (ref instanceof Graph) {
                        setDisplayedGraph((Graph)ref);
                    }
                    else if (ref instanceof Transition) {
                        setDisplayedTransition((Transition)ref);
                    }
                    else {
                        clearStatePanel();
                    }
                }
            }
            else {
                clearStatePanel();
            }
        }
    }
}
