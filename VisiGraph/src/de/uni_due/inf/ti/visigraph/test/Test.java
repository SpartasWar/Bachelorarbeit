package de.uni_due.inf.ti.visigraph.test;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.uni_due.inf.ti.visigraph.ArrowHead;
import de.uni_due.inf.ti.visigraph.Shape;
import de.uni_due.inf.ti.visigraph.Style;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.swing.BasicGraphEditSynchronizer;
import de.uni_due.inf.ti.visigraph.swing.GraphEditor;

public class Test
{
    private static final String STYLE_EDGE  = "edge-style";
    private static final String STYLE_LABEL = "label-style";
    private static final String STYLE_NODE  = "node-style";
    
    public static void createAndShowGui()
    {
        Style labelStyle = new Style();
        labelStyle.setDrawn(false);
        labelStyle.setFillColor(false, new Color(.8f, .8f, .8f));
        labelStyle.setFillColor(true, new Color(.4f, .4f, .4f));
        labelStyle.setShape(Shape.createRectangle(30, 20));
        labelStyle.setFlexibleShape(true);
        
        Style nodeStyle = new Style();
        nodeStyle.setFilled(true);
        nodeStyle.setFillColor(false, Color.WHITE);
        nodeStyle.setFillColor(true, Color.LIGHT_GRAY);
        
        Style edgeStyle = new Style();
        edgeStyle.setFrontArrow(ArrowHead.newLineArrowHead(5, 5));
        
        BasicGraphEditSynchronizer syncer = new BasicGraphEditSynchronizer();
        syncer.setNewEdgeStyleName(STYLE_EDGE);
        syncer.setNewNodeStyleName(STYLE_NODE);
        syncer.setNewLabelStyleName(STYLE_LABEL);
        syncer.setAutoAddLabels(true);
        
        VxGraph graph = new VxGraph();
        graph.addNode(100, 100).setStyle(STYLE_NODE);
        graph.addNode(200, 200).setStyle(STYLE_NODE);
        
        JFrame frame = new JFrame("VisiGraph Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GraphEditor editor = new GraphEditor();
        editor.setStyle(STYLE_LABEL, labelStyle);
        editor.setStyle(STYLE_NODE, nodeStyle);
        editor.setStyle(STYLE_EDGE, edgeStyle);
        editor.setGraph(graph);
        editor.setEditSynchronizer(syncer);
        editor.setAntiAliased(true);
        frame.getContentPane().add(editor, BorderLayout.CENTER);
        
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
    
    public static void main(String... args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui();
            }
        });
    }
}
