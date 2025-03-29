/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, University of Duisburg-Essen
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.Transition;

/**
 * Abstract superclass of classes which write graphical representations of 
 * graphs, rules and transition systems to files.
 *  
 * @author H.J. Sander Bruggink
 */
public abstract class GraphExporter
{
    public static enum Format {
        PDF("pdf"),
        PNG("png"),
        GIF("gif"),
        PS("ps"), 
        SVG("svg");
        
        private String suffix;
        
        private Format(String name) 
        {
            suffix = "." + name;
        }
        
        public String getSuffix() 
        {
            return suffix;
        }
    }
    
    
    private Format format;

    
    /**
     * Creates a new GraphExporter instance.
     * 
     * @param format
     *      graphics format this GraphExporter exports to
     * @throws NullPointerException
     *      if {@code format} is {@code null}
     */
    public GraphExporter(Format format)
    {
        if (format == null) throw new NullPointerException();
        this.format = format;
    }
    
    /**
     * Returns the graphics format this GraphExporter exports to.
     */
    public final Format getFormat()
    {
        return format;
    }
    
    /**
     * Writes a visual representation of a graph to a binary stream.
     * 
     * @param graph
     *      graph to export
     * @param binOut
     *      binary stream where the visual representation of the graph is
     *      written to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     */
    protected abstract void exportGraphToStream(
            Graph graph, 
            OutputStream binOut)
        throws IOException;
    
    /**
     * Writes a visual representation of a rule to a binary stream.
     * 
     * @param graph
     *      graph to export
     * @param binOut
     *      binary stream where the visual representation of the graph is
     *      written to
     */
    protected abstract void exportRuleToStream(
            Rule graph, 
            OutputStream binOut)
        throws IOException;
    
    /**
     * Writes a visual representation of a labeled transition system to a 
     * binary stream. This method only writes a representation of the structure
     * of the transition system, not of the graph in it.
     * 
     * @param system
     *      system to write a visual representation of
     * @param binOut
     *      stream where the visual representation is written to
     * @param graphIds 
     *      id strings of graphs in the system
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if an IO error occurs
     */
    protected abstract void exportSystemToStream(
            ExplicitTransitionSystem system,
            OutputStream binOut)
        throws IOException;
    
    /**
     * Exports a graph.
     * 
     * @param graph         
     *      the graph to write a visual representation of
     * @param file          
     *      the file to write the visual representation to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if an IO error occurs
     */
    public void exportGraph(
            Graph graph, 
            File file)
        throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        exportGraphToStream(graph, out);
        out.flush();
        out.close(); 
    }
    
    /**
     * Exports a rule.
     * 
     * @param rule         
     *      the graph to write a visual representation of
     * @param file          
     *      the file to write the visual representation to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if an IO error occurs
     */
    public void exportRule(Rule rule, File file)
        throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        exportRuleToStream(rule, out);
        out.flush();
        out.close();
    }
    
    /**
     * Writes a visual representation of an explicit transition system to 
     * a file. The written file is ZIP file containing visual representations
     * of the graphs of the system and of the system itself. 
     * 
     * @param system        
     *      transition system to write a visual representation of
     * @param file
     *      file to write the visual representation to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if an IO error occurs
     */
    public void exportTransitionSystem(
            ExplicitTransitionSystem system, 
            File file)
        throws IOException
    {
        int graphNum = 0;
        
        FileOutputStream out = new FileOutputStream(file);
        ZipOutputStream zout = new ZipOutputStream(out);
        
        try {
            for (Graph g : system.getGraphs()) {
                String fileName = g.getName();
                if (fileName == null) {
                    String name = String.format("G%d", graphNum++);
                    g.setName(name);
                    fileName = name + format.getSuffix();
                }
                else {
                    if (fileName.charAt(0) == 'G') {
                        fileName = 'G' + fileName; 
                    }
                    fileName += format.getSuffix();
                }
                zout.putNextEntry(new ZipEntry(fileName));
                exportGraphToStream(g, zout);
                zout.closeEntry();
            }

            zout.putNextEntry(new ZipEntry("main" + format.getSuffix()));
            exportSystemToStream(system, zout);
            zout.closeEntry();
        }
        finally {
            zout.flush();
            zout.close();
        }
    }
    
    /**
     * Write a representation of a transformation sequence to a file.
     * <p>
     * Calling {@code exportTransformationSequence(sequence, out)} is
     * equivalent to calling 
     * {@link #exportTransitionSystem(ExplicitTransitionSystem, File, Map) 
     *   exportTransitionSystem(sequence, out, null)}.
     * 
     * @param sequence
     *      transformation sequence to be exported
     * @param out
     *      file to export the transformation sequence to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if an IO error occurs
     */
    public void exportTransformationSequence(
            List<Transition> sequence,
            File file)
        throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        ZipOutputStream zout = new ZipOutputStream(out);
        
        try {
            int graphNum = 0;
            Graph last = null;
            
            for (Transition trans : sequence) {
                String filename = String.format("graph%d%s", graphNum++, format.getSuffix());
                zout.putNextEntry(new ZipEntry(filename));
                
                Morphism match = trans.getMatch();
                if (match != null) {
                    exportGraphToStream(trans.getSource(), zout);
                }
                else {
                    exportGraphToStream(trans.getSource(), zout);
                }
                
                zout.closeEntry();
                last = trans.getTarget();
            }

            // show last graph of the sequence
            String filename = String.format("graph%d%s", graphNum++, format.getSuffix());
            zout.putNextEntry(new ZipEntry(filename));
            exportGraphToStream(last, zout);
            zout.closeEntry();
        }
        finally {
            zout.flush();
            zout.close();
        }
    }
}
