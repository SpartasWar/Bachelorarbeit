/* 
 * Copyright (c) 2014, H.J. Sander Bruggink, University of Duisburg-Essen
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

package de.uni_due.inf.ti.graph.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.NamedObject;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.visual.GraphVisualizer;
import de.uni_due.inf.ti.visigraph.GraphDrawer;
import de.uni_due.inf.ti.visigraph.Layouter;
import de.uni_due.inf.ti.visigraph.LevelLayouter;
import de.uni_due.inf.ti.visigraph.VxGraph;

/**
 * Graph exporter which exports graphs in PNG format.
 * 
 * @author H.J. Sander Bruggink
 */
public class PngExporter extends GraphExporter
{
    private GraphVisualizer visualizer;
    private Layouter layouter;
    private Map<NamedObject,VxGraph> oldVisis;
    private GraphDrawer drawer;
    
    /**
     * Creates a new PNG exporter.
     */
    public PngExporter()
    {
        super(Format.PNG);
        oldVisis = new HashMap<>();
        drawer = new GraphDrawer();
    }
    
    /**
     * Sets the GraphVisualizer object which transforms Graph instances to
     * VxGraph instances.
     * 
     * @param visualizer
     *      the new GraphVisualizer used by this PngExporter
     * @throws NullPointerException
     *      when {@code visualizer} is {@code null}
     */
    public void setVisualizer(GraphVisualizer visualizer)
    {
        if (visualizer == null) throw new NullPointerException();
        this.visualizer = visualizer;
    }
    
    /**
     * Returns the GraphVisualizer which is used to transform Graph instances
     * to VxGraph instances.
     */
    public GraphVisualizer getVisualizer()
    {
        if (visualizer == null) visualizer = new GraphVisualizer();
        return visualizer;
    }
    
    /**
     * GraphDrawer object which is used to draw VxGraph instances.
     */
    public GraphDrawer getDrawer()
    {
        return drawer;
    }
    
    /**
     * Sets the Layouter object which this PngExporter uses to layout new
     * VxGraph instances.
     * 
     * @param layouter
     *      the new layouter used by this PngExporter
     * @throws NullPointerException
     *      when {@code layouter} is {@code null}
     */
    public void setLayouter(Layouter layouter)
    {
        if (layouter == null) throw new NullPointerException();
        this.layouter = layouter;
    }
    
    /**
     * Returns the layouter object which this PngExporter uses to layout
     * new VxGraph instances.
     */
    public Layouter getLayouter()
    {
        if (layouter == null) layouter = new LevelLayouter();
        return layouter;
    }
    
    /**
     * Associates an existing visualization object to an object.
     */
    public void setVisualization(NamedObject obj, VxGraph visi)
    {
        oldVisis.put(obj, visi);
    }
    
    /**
     * Writes a PNG representation of a VxGraph to an OutputStream.
     */
    private void exportVisi(VxGraph visi, OutputStream binOut)
        throws IOException
    {
        Rectangle2D dim = drawer.getBounds(visi);
        if (dim == null) dim = new Rectangle(1, 1);
        // prevent truncation of shape borders
        final double border = 2.0;
        dim.setRect(
            dim.getX() - border, dim.getY() - border,
            dim.getWidth() + 2*border, dim.getHeight() + 2*border);
        
        BufferedImage im = new BufferedImage(
                (int)dim.getWidth(), 
                (int)dim.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(-dim.getX(), -dim.getY());
        
        g.setColor(Color.WHITE);
        g.fill(dim); // TODO: offer an option to do transparent images (TYPE_INT_ARGB and no fill)
        
        drawer.draw(g, visi);
        
        ImageIO.write(im, "png", binOut);
    }

    public void exportVxGraphToStream(VxGraph vxgraph, OutputStream binOut) throws IOException {
        exportVisi(vxgraph, binOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportGraphToStream(Graph graph, OutputStream binOut)
        throws IOException
    {
        VxGraph visi = oldVisis.get(graph);
        if (visi == null) {
            visi = getVisualizer().visualizeGraph(graph);
            getLayouter().layoutGraph(visi);
            oldVisis.put(graph, visi);
        }
        
        exportVisi(visi, binOut);        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void exportRuleToStream(Rule rule, OutputStream binOut)
        throws IOException
    {
        VxGraph visi = oldVisis.get(rule);
        if (visi == null) {
            visi = getVisualizer().visualizeMorphism(rule.getCorrespondence()).getVxGraph();
            getLayouter().layoutGraph(visi);
            oldVisis.put(rule, visi);
        }
        
        exportVisi(visi, binOut);        
    }

    public void exportFlatRuleToStream(Rule rule, OutputStream binOut)
            throws IOException
        {
            VxGraph visi = oldVisis.get(rule);
            if (visi == null) {
                visi = getVisualizer().visualizeFlatMorphism(rule.getCorrespondence());
                getLayouter().layoutGraph(visi);
                oldVisis.put(rule, visi);
            }

            exportVisi(visi, binOut);
        }

    public void exportMorphismToStream(Morphism morphism, OutputStream binOut)
        throws IOException
    {
        VxGraph visi = getVisualizer().visualizeMorphism(morphism).getVxGraph();
        getLayouter().layoutGraph(visi);
        exportVisi(visi, binOut);
    }

    public void exportFlatMorphismToStream(Morphism morphism, OutputStream binOut)
        throws IOException
    {
        VxGraph visi = getVisualizer().visualizeFlatMorphism(morphism);
        getLayouter().layoutGraph(visi);
        exportVisi(visi, binOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportSystemToStream(
            ExplicitTransitionSystem system,
            OutputStream binOut) 
        throws IOException
    {
        VxGraph visi = oldVisis.get(system);
        if (visi == null) {
            visi = getVisualizer().visualizeSystem(system);
            getLayouter().layoutGraph(visi);
            oldVisis.put(system, visi);
        }
        
        exportVisi(visi, binOut);
    }
}
