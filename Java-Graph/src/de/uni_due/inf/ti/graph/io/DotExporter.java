/* 
 * Copyright (c) 2013-2015, H.J. Sander Bruggink
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

package de.uni_due.inf.ti.graph.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.GraphElement;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.Transition;

import static org.checkerframework.checker.nullness.NullnessUtils.castNonNull;

/**
 * Class which exports graphs to GraphViz's DOT format.
 * 
 * @author H.J. Sander Bruggink
 */
public final class DotExporter extends GraphExporter
{
    /** Default style for graph nodes. */
    private DotStyle defaultNodeStyle;
    
    /** Default style for hyperedges. */
    private DotStyle defaultEdgeStyle;
    
    /** Default style for transition system nodes. */
    private DotStyle defaultGraphStyle; 
    
    /** Default style for line (binary edges, hyperedge connections, 
        transitions, etc.) */
    private DotStyle defaultLineStyle;
    
    /** Style used for highlighting parts of the representation. */
    private DotStyle highlightStyle;
    
    /** Custom styles. */
    private @Nullable Map<Object,DotStyle> customStyles;
    
    static private EnumMap<@KeyFor("formatOptions") Format,String> formatOptions;
    static {
        formatOptions = new EnumMap<>(Format.class);
        formatOptions.put(Format.PDF, "-Tpdf");
        formatOptions.put(Format.PS, "-Tps");
        formatOptions.put(Format.GIF, "-Tgif");
        formatOptions.put(Format.PNG, "-Tpng");
        formatOptions.put(Format.SVG, "-Tsvg");
    }
 
    /** Tool name of the Neato tool. */
    private static final String TOOL_NEATO = "neato";
    
    /** Tool name of the Dot tool. */
    private static final String TOOL_DOT   = "dot";

    /** Whether or not binary edges are represented as hyperedges. */
    private boolean alwaysHyper;
    
    
    /**
     * Creates a new DotExporter instance which will represent edges as arrows
     * in non-hypergraphs.
     */
    public DotExporter()
    {
        this(false, Format.PDF);        
    }
    
    /**
     * Creates a new DotExporter instance.
     * 
     * @param alwaysHyper
     *      if {@code true}, all edges are always represented as hyperedges,
     *      that is, as boxes; if {@code false}, edges of graph which only
     *      contain binary edges will be represented by arrows.
     * @param format
     *      format of the graphical files this DotExported generates
     */
    public DotExporter(boolean alwaysHyper, Format format)
    {
        super(format);
        this.alwaysHyper = alwaysHyper;
        
        defaultNodeStyle = new DotStyle(DotStyle.Shape.ELLIPSE, DotStyle.Color.BLACK, DotStyle.LineStyle.NORMAL);
        defaultEdgeStyle = new DotStyle(DotStyle.Shape.RECTANGLE, DotStyle.Color.BLACK, DotStyle.LineStyle.NORMAL);
        defaultGraphStyle = defaultEdgeStyle;
        defaultLineStyle = new DotStyle(DotStyle.Shape.DEFAULT, DotStyle.Color.BLACK, DotStyle.LineStyle.NORMAL);
        
        highlightStyle = DotStyle.getColorStyle(DotStyle.Color.RED);
    }
    
    /**
     * Sets the default style for graph nodes.
     */
    public void setDefaultNodeStyle(DotStyle style) 
    {
        defaultNodeStyle = style;
    }
    
    /**
     * Returns the default style for graph nodes.
     */
    public DotStyle getDefaultNodeStyle()
    {
        return defaultNodeStyle;
    }
    
    /**
     * Sets the default style for hyperedges.
     */
    public void setDefaultEdgeStyle(DotStyle style)
    {
        defaultEdgeStyle = style;
    }
    
    /**
     * Returns the default style for hyperedges.
     */
    public DotStyle getDefaultEdgeStyle()
    {
        return defaultEdgeStyle;
    }
   
    /**
     * Sets the default style for the graphs in transition systems.
     */
    public void setDefaultGraphStyle(DotStyle style)
    {
        defaultGraphStyle = style;
    }
    
    /**
     * Returns the default style for the graphs in transition systems.
     */
    public DotStyle getDefaultGraphStyle()
    {
        return defaultGraphStyle;
    }
    
    /**
     * Sets the default style for lines. This style is used for binary edges,
     * hyperedge connections, transitions, etc.
     */
    public void setDefaultLineStyle(DotStyle style)
    {
        defaultLineStyle = style;
    }
    
    /**
     * Returns the default style for lines.
     */
    public DotStyle getDefaultLineStyle()
    {
        return defaultLineStyle;
    }
    
    /**
     * Sets the highlight style of this DotExporter. 
     * 
     * @param style
     *      the new highlight style of this DotExporter
     */
    public void setHighlightStyle(DotStyle style)
    {
        highlightStyle = style;
    }
    
    /**
     * Returns the highlight style of this DotExporter.
     */
    public DotStyle getHighlightStyle()
    {
        return highlightStyle;
    }
    
    /**
     * Sets the custom styles for this DotExporter. Custom styles can be used
     * to use a style different than the default style for certain objects.
     * <p> 
     * A reference to the same object is stored within this DotExporter; 
     * changes to the style map done after the call to this method will be
     * visible to the DotExporter.
     * 
     * @param styles
     *      map which associates custom styles to certain objects, or 
     *      {@code null} to remove all custom styles
     */
    public void setCustomStyles(@Nullable Map<Object,DotStyle> styles)
    {
        customStyles = styles;
    }
    
    /**
     * Returns the DOT parameter string for a given shape.
     * This method returns {@code null} if the shape is not 
     * recognized or supported.
     * 
     * @param shape
     *      shape of which to return the DOT parameter string
     * @return
     *      DOT parameter string of {@code shape}
     * @throws NullPointerException
     *      if {@code shape} is {@code null}
     */
    private static @Nullable String getShapeString(DotStyle.Shape shape)
    {
        switch (shape) {
            case RECTANGLE: return "shape=box";
            case ELLIPSE:   return "shape=ellipse";
            case PLAINTEXT: return "shape=plaintext";
            default:        return null;
        }
    }

    /**
     * Returns the DOT parameter string for a given line style.
     * This method returns {@code null} if the line style is not 
     * recognized or supported.
     * 
     * @param lineStyle
     *      line style for which to return the DOT paremeter string
     * @return 
     *      DOT parameter string of {@code lineStyle}
     * @throws NullPointerException
     *      if {@code lineStyle} is {@code null}
     */
    private static @Nullable String getLineStyleString(DotStyle.LineStyle lineStyle)
    {
        switch (lineStyle) {
            case DASHED: return "style=dashed";
            case DOTTED: return "style=dotted";
            default:     return null;
        }
    }
    
    /**
     * Returns the DOT parameter string for a given color.
     * This method returns {@code null} if the color is not 
     * recognized or supported.
     * 
     * @param color
     *      color for which to return the DOT parameter string
     * @return
     *      DOT parameter string for {@code color}
     * @throws NullPointerException
     *      if {@code color} is {@code null}
     */
    private static @Nullable String getColorString(DotStyle.Color color)
    {
        switch (color) {
            case RED:   return "color=red";
            case GREEN: return "color=green";
            case BLUE:  return "color=blue";
            case YELLOW:return "color=yellow";
            case CYAN:  return "color=cyan";
            default:    return null;
        }
    }
    
    /**
     * Returns the DOT string which represents a given style.
     * 
     * @param style
     *      style for which to return the string
     * @param rest
     *      additional information to be put in the string which is not
     *      represented by {@code style}
     * @return
     *      style string for {@code style}, with ',' and {@code rest} added to 
     *      if (if rest is non-null and non-empty)
     * @throws NullPointerException
     *      if {@code style} is {@code null}
     */
    private static String getStyleString(DotStyle style, @Nullable String rest) 
    {
        String shapes = getShapeString(style.getShape());
        String colors = getColorString(style.getColor());
        String lines  = getLineStyleString(style.getLineStyle());

        StringBuilder buf = new StringBuilder();

        boolean first = true;
        if (shapes != null) { 
            if (!first) buf.append(","); 
            first = false;
            buf.append(shapes);
        }
        if (colors != null) { 
            if (!first) buf.append(","); 
            first = false;
            buf.append(colors);
        }
        if (lines != null) { 
            if (!first) buf.append(","); 
            first = false;
            buf.append(lines);
        }
        if (rest != null && !rest.isEmpty()) {
            if (!first) buf.append(",");
            first = false;
            buf.append(rest);
        }
        
        return bracketize(buf.toString());
    }
    
    /**
     * Calls a GraphViz tool to visualize a graph.
     * 
     * @param program
     *      GraphViz tool to call
     * @param dotFile
     *      DOT file which is the input of the tool
     * @param binOut
     *      output stream where the result of the DOT program is written to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     * @throws IOException
     *      if the DOT tool could not be executed or was interrupted, or if
     *      the output of the tool could not be written to the {@code binOut}
     *      stream
     */
    private void callDot(
            String program, 
            File dotFile, 
            OutputStream binOut) throws IOException
    {
        File figFile = File.createTempFile("fig", getFormat().getSuffix());
        figFile.deleteOnExit();
        try {
            Runtime rt = Runtime.getRuntime();
            String[] args = { 
                    program, 
                    formatOptions.get(getFormat()), 
                    "-o" + figFile.getAbsolutePath(),
                    dotFile.getAbsolutePath()
            };
            Process p = rt.exec(args);
            p.waitFor();
        }
        catch (InterruptedException e) {
            String msg = String.format("Graph visualization failed due to %s interruption.", program);
            throw new IOException(msg, e);
        }

        FileInputStream binIn = new FileInputStream(figFile);
        int b = binIn.read();           
        while (b >= 0) {
            binOut.write(b);
            b = binIn.read();
        }
        binIn.close();
    }
    
    /**
     * Prints a DOT representation of this graph to a PrintWriter, optionally
     * with custom styles.
     *
     * @param graph
     *      graph to write a DOT representation of
     * @param out           
     *      the PrintWriter to write to
     * @param styles
     *      styles for some objects of graph; or {@code null} if no custom
     *      styles are needed
     * @throws NullPointerException
     *      if {@code graph} or {@code out} is {@code null}
     */
    public void printDot(Graph graph, PrintWriter out, @Nullable Map<Object,DotStyle> styles)
    {
        boolean hyper = alwaysHyper || !graph.isBinary();
        if (styles == null) styles = Collections.emptyMap();
        castNonNull(styles);
        
        out.println(hyper ? "graph {" : "digraph {");
        
        for (Node n : graph.getNodes()) {
            String style = getStyleString(
                    defaultNodeStyle.getSubStyle(styles.get(n)), 
                    String.format("label=\"%d\"", n.getId()));
            out.printf("  node%d %s;%n", n.getId(), style); 
        }
        
        int id = 0;
        for (Edge e : graph.getEdges()) {
            List<Node> nodes = e.getNodes();
            if (hyper) {
                int edgeId = id++;
                String style = getStyleString(
                        defaultEdgeStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("  edge%d %s;%n", edgeId, style);
                for (int i = 0; i < nodes.size(); i++) {
                    style = getStyleString(
                              defaultLineStyle,
                              String.format("taillabel=\"%d\"", i));
                    out.printf("  edge%d -- node%d %s;%n", 
                            edgeId, nodes.get(i).getId(), style);
                }
            }
            else {
                String style = getStyleString(
                        defaultLineStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("  node%d -> node%d %s;%n",
                        nodes.get(0).getId(), nodes.get(1).getId(), style);
            }
        }
        
        out.println('}');
    }
    
    /**
     * Prints a DOT representation of a graph to a PrintWriter.
     * <p>
     * Calling {@code printDot(graph, out)} is equivalent to 
     * calling {@code printDot(graph, out, null)}.
     * 
     * @param graph
     *      graph to write a DOT representation of
     * @param out           
     *      the PrintWriter to write to
     * @throws NullPointerException
     *      if {@code graph} or {@code out} is {@code null}
     */
    public void printDot(Graph graph, PrintWriter out)
    {
        printDot(graph, out, null);
    }
    
    /**
     * Prints a DOT representation of a rule to a PrintWriter.
     * 
     * @param rule
     *      rule to print a DOT representation of
     * @param out
     *      PrintWriter to print the DOT representation to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     */
    public void printDot(Rule rule, PrintWriter out)
    {
        boolean hyper =
                alwaysHyper 
                || !rule.getCorrespondence().getEdgeMap().isEmpty()
                || !rule.getLeft().isBinary() 
                || !rule.getRight().isBinary();
        Map<Object,DotStyle> styles = customStyles;
        if (styles == null) styles = Collections.emptyMap();
        castNonNull(styles);
        
        int id = 0;
        Map<GraphElement,Integer> idMap = new HashMap<>();
        
        out.println("digraph {");
        
        // print left-hand side        
        out.println("  subgraph cluster_Left {");
        if (hyper) out.println("    edge [dir=none]");
        
        for (Node n : rule.getLeft().getNodes()) {
            int nodeNum = id++;
            idMap.put(n, nodeNum);
            String style = getStyleString(
                    defaultNodeStyle.getSubStyle(styles.get(n)), 
                    String.format("label=\"%d\"", nodeNum));
            out.printf("    node%d %s;%n", nodeNum, style); 
        }
        
        for (Edge e : rule.getLeft().getEdges()) {
            List<Node> nodes = e.getNodes();
            if (hyper) {
                int edgeId = id++;
                idMap.put(e, edgeId);
                String style = getStyleString(
                        defaultEdgeStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("    edge%d %s;%n", edgeId, style);
                for (int i = 0; i < nodes.size(); i++) {
                    style = getStyleString(
                              defaultLineStyle,
                              String.format("taillabel=\"%d\"", i));
                    out.printf("    edge%d -- node%d %s;%n", 
                            edgeId, idMap.get(nodes.get(i)), style);
                }
            }
            else {
                String style = getStyleString(
                        defaultLineStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("    node%d -> node%d %s;%n",
                        idMap.get(nodes.get(0)), idMap.get(nodes.get(1)), style);
            }
        }
        
        out.println("  }");

        // print right-hand side        
        out.println("  subgraph cluster_Right {");
        if (hyper) out.println("    edge [dir=none]");
        
        for (Node n : rule.getRight().getNodes()) {
            int nodeNum = id++;
            idMap.put(n, nodeNum);
            String style = getStyleString(
                    defaultNodeStyle.getSubStyle(styles.get(n)), 
                    String.format("label=\"%d\"", nodeNum));
            out.printf("    node%d %s;%n", nodeNum, style); 
        }
        
        for (Edge e : rule.getRight().getEdges()) {
            List<Node> nodes = e.getNodes();
            if (hyper) {
                int edgeId = id++;
                idMap.put(e, edgeId);
                String style = getStyleString(
                        defaultEdgeStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("    edge%d %s;%n", edgeId, style);
                for (int i = 0; i < nodes.size(); i++) {
                    style = getStyleString(
                              defaultLineStyle,
                              String.format("taillabel=\"%d\"", i));
                    out.printf("    edge%d -- node%d %s;%n", 
                            edgeId, idMap.get(nodes.get(i)), style);
                }
            }
            else {
                String style = getStyleString(
                        defaultLineStyle.getSubStyle(styles.get(e)),
                        String.format("label=\"%s\"", e.getLabel().getName()));
                out.printf("    node%d -> node%d %s;%n",
                        idMap.get(nodes.get(0)), idMap.get(nodes.get(1)), style);
            }
        }
        
        out.println("  }");
        
        // Print correspondence morphism
        for (Map.Entry<Node,Node> entry : rule.getCorrespondence().
                getNodeMap().entrySet()) {
            int fromId = castNonNull(idMap.get(entry.getKey()));
            int toId = castNonNull(idMap.get(entry.getValue()));
            out.printf("  node%d -> node%d [style=dashed];%n", fromId, toId);
        }
        for (Map.Entry<Edge,Edge> entry : rule.getCorrespondence().
                getEdgeMap().entrySet()) {
            int fromId = castNonNull(idMap.get(entry.getKey()));
            int toId = castNonNull(idMap.get(entry.getValue()));
            out.printf("  node%d -> node%d [style=dashed];%n", fromId, toId);
        }
        
        out.println('}');
    }
    
    /**
     * Write a DOT representation of this ExplicitTransitionSystem to a
     * PrintWriter. 
     * 
     * @param system
     *      the transition system to be exported
     * @param out           
     *      the PrintWriter to write the DOT representation of the 
     *      ExplicitTransitionSystem to
     * @throws NullPointerException
     *      if any of the arguments is {@code null}
     */
    public void printDot(
            ExplicitTransitionSystem system, 
            PrintWriter out) 
    {
        int graphNum = 0;
        Map<Graph,Integer> graphNums = new HashMap<Graph,Integer>();
        Map<Object,DotStyle> styles = customStyles;
        if (styles == null) styles = Collections.emptyMap();
        castNonNull(styles);
        
        out.println("digraph {");
        out.println("  start [shape=plaintext,label=\"start\"];");
        
        for (Graph g : system.getGraphs()) {
            String id = g.getName();
            if (id != null) {
                String style = getStyleString(
                        defaultGraphStyle.getSubStyle(styles.get(g)),
                        String.format("label=\"%s\"", id));
                out.printf("  g%d %s;%n", graphNum, style);
            }
            else {
                String style = getStyleString(
                        defaultGraphStyle.getSubStyle(styles.get(g)), null);
                out.printf("  g%d %s;%n", graphNum, style);
            }
            graphNums.put(g, graphNum);
            graphNum++;
        }
        
        out.printf("  start -> g%d;%n", graphNums.get(system.getInitialGraph()));
        
        for (Transition t : system.getTransitions()) {
            String label = t.getLabel();
            String labelStr = null;
            if (label != null && !label.isEmpty()) {
                labelStr = String.format("label=\"%s\"", label);
            }
            
            String style = getStyleString( 
                    defaultLineStyle.getSubStyle(styles.get(t)),
                    labelStr);
            
            Graph dom = t.getSource();
            Graph cod = t.getTarget();
            
            out.printf("  g%d -> g%d %s;\n", 
                    graphNums.get(dom), 
                    graphNums.get(cod),
                    style);
        }
        
        out.println("}");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void exportGraphToStream(
            Graph graph, 
            OutputStream binOut)
        throws IOException
    {
        File dotFile = File.createTempFile("graph", ".dot");
        dotFile.deleteOnExit();
        PrintWriter dotOut = new PrintWriter(dotFile);
        printDot(graph, dotOut);
        dotOut.flush();
        dotOut.close();

        callDot(TOOL_NEATO, dotFile, binOut);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void exportRuleToStream(
            Rule rule,
            OutputStream binOut)
        throws IOException
    {
        File dotFile = File.createTempFile("graph", ".dot");
        dotFile.deleteOnExit();
        PrintWriter dotOut = new PrintWriter(dotFile);
        printDot(rule, dotOut);
        dotOut.flush();
        dotOut.close();

        callDot(TOOL_DOT, dotFile, binOut);        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void exportSystemToStream(
            ExplicitTransitionSystem system,
            OutputStream binOut)
        throws IOException
    {
        File dotFile = File.createTempFile("graph", ".dot");
        dotFile.deleteOnExit();
        PrintWriter dotOut = new PrintWriter(dotFile);
        printDot(system, dotOut);
        dotOut.flush();
        dotOut.close();
        
        callDot(TOOL_DOT, dotFile, binOut);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
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
                String filename = String.format("graph%d%s", graphNum++, getFormat().getSuffix());
                zout.putNextEntry(new ZipEntry(filename));
                
                Morphism match = trans.getMatch();
                if (match != null) {
                    Map<Object,DotStyle> myStyles;
                    if (customStyles == null) myStyles = new HashMap<>();
                                         else myStyles = new HashMap<>(customStyles);
                                   
                    for (Node n : match.getNodeMap().values()) {
                        DotStyle s = myStyles.get(n);
                        if (s == null) s = DotStyle.UNSET_STYLE;
                        s = s.getSubStyle(highlightStyle);
                        myStyles.put(n, s);
                    }
                    for (Edge e : match.getEdgeMap().values()) {
                        DotStyle s = myStyles.get(e);
                        if (s == null) s = DotStyle.UNSET_STYLE;
                        s = s.getSubStyle(highlightStyle);
                        myStyles.put(e, s);
                    }
                    Map<Object,DotStyle> savedStyles = customStyles;
                    customStyles = myStyles;
                    exportGraphToStream(trans.getSource(), zout);
                    customStyles = savedStyles;
                }
                else {
                    exportGraphToStream(trans.getSource(), zout);
                }
                
                zout.closeEntry();
                last = trans.getTarget();
            }

            // show last graph of the sequence
            String filename = String.format("graph%d%s", graphNum++, getFormat().getSuffix());
            zout.putNextEntry(new ZipEntry(filename));
            exportGraphToStream(last, zout);
            zout.closeEntry();
        }
        finally {
            zout.flush();
            zout.close();
        }
    }
    
    /**
     * Returns the empty string if {@code s} is empty or {@code null}, and
     * {@code s} enclosed in brackets otherwise.
     */
    private static String bracketize(String s) 
    {
        if (s == null || s.isEmpty())
            return "";
        else 
            return String.format("[%s]", s);
    }
}
