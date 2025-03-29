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

// 2014-05-?? - Initial version
// 2015-02-18 - Added Javadocs

package de.uni_due.inf.ti.visigraph.swing;

import java.awt.geom.Point2D;

import de.uni_due.inf.ti.visigraph.VxEdge;
import de.uni_due.inf.ti.visigraph.VxGraph;
import de.uni_due.inf.ti.visigraph.VxNode;

/**
 * Interface which is implemented by objects which synchronize the VxGraph
 * instance being shown in an editor with an (application-specific) 
 * underlying model.
 * 
 * @author H.J. Sander Bruggink
 */
public interface GraphEditSynchronizer
{
	/**
	 * Determines whether a node can act as a source node.
	 * 
	 * @param source
	 * 		the node of which it is to be determined whether or not
	 * 		it can act as a source node
	 * @return
	 * 		{@code true} when {@code source} can be the source of
	 * 		an edge
	 */
    public boolean isPossibleSourceNode(VxNode source);
    
    /**
     * Determines whether a node can be added to a graph.
     * 
     * @param graph
     * 		graph of which it is to be determined whether a node
     *      can be added to it
     * @return
     * 		{@code true} when a node can be added to {@code graph}
     */
    public boolean canAddNode(VxGraph graph);
    
    /**
     * Determines whether an edge can be added to a graph.
     * 
     * @param source
     * 		source node of the edge
     * @param target
     * 		target node of the edge
     * @return
     * 		{@code true} when it is allowed to add an edge from
     *      {@code source} to {@code target}
     */
    public boolean canAddEdge(VxNode source, VxNode target);
    
    /**
     * Determines if a label can be added to a given edge.
     * 
     * @param edge
     * 		edge to which a label is to be added
     * @return
     * 		{@code true} when it is allowed to add (another)
     * 	    label to {@code edge}
     */
    public boolean canAddLabel(VxEdge edge);
    
    /**
     * Determines whether the label of a node can be edited.
     * 
     * @param node
     * 		a node of the view graph
     * @return
     * 		{@code true} if the user can edit the label of the
     *      given node
     */
    public boolean canEditLabel(VxNode node);
    
    /**
     * Adds a node to the graph. 
     * This method adds a node to the view, and makes sure that
     * the underlying (application-specific) model is updated.
     * 
     * @param graph
     * 		view graph
     * @param pt
     * 		coordinate at which the node is to be added
     */
    public void addNode(VxGraph graph, Point2D pt);
    
    /**
     * Adds an edge to the graph.
     * This method adds an edge to the view, and makes sure that
     * the underlying (application-specific) model is updated.
     * 
     * @param source
     * 		source node of the new edge
     * @param target
     * 		target node of the new edge
     */
    public void addEdge(VxNode source, VxNode target);
    
    /**
     * Adds a new label to an edge.
     * This method adds a label to the view, and updates the
     * underlying (application-specific) model to reflect the
     * new label.
     * 
     * @param edge
     * 		edge to which a label is added
     * @param label
     * 		new label of the edge
     */
    public void addLabel(VxEdge edge, String label);
    
    /**
     * Changes the label of a node.
     * This method changes the label of the node in the view,
     * and updates the underlying (application-specific) model
     * to reflect this change.
     * 
     * @param node
     * 		node of which the label is edited
     * @param newLabel
     * 		new label of the node
     */
    public void editLabel(VxNode node, String newLabel);
}
