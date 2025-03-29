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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.InterfaceGraph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.NacRule;
import de.uni_due.inf.ti.graph.NamedObject;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.Trace;
import de.uni_due.inf.ti.graph.TransformationSystem;
import de.uni_due.inf.ti.graph.TransitionSystem;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Constant;
import de.uni_due.inf.ti.graph.condition.Disjunction;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.False;
import de.uni_due.inf.ti.graph.condition.ForAll;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.condition.Negation;
import de.uni_due.inf.ti.graph.condition.True;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition.ChildTuple;
import de.uni_due.inf.ti.graph.condition.cospan.Quantifier;
import de.uni_due.inf.ti.graph.ext.Cospan;
import de.uni_due.inf.ti.graph.io.SgfTerm.ConditionAndOrTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.ConditionNegTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.ConstantConditionTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.CopyTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.CospanConditionDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.CospanConditionTupleTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.CospanDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.EdgeDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.GraphConditionDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.GraphDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.GtsDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.IfGraphDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.LetTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.MapstoTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.MorphismDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.NodeDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.QuickCospanDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.QuickRuleDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.RefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.RuleDefTerm;
import de.uni_due.inf.ti.graph.io.SgfTerm.TraceDefTerm;

/**
 * Class which builds objects from abstract syntax trees.
 *
 * @author H.J. Sander Bruggink
 */
class SgfBuilder {
	/** Symbol table. */
	Map<String, Object> symbols;

	/** Cache of graphs. */
	private IdentityHashMap<Graph, GraphBuildInfo> graphs = new IdentityHashMap<>();
	// WeakHashMap doesn't work here, because most objects will be stored in
	// symbol table anyway (currently, only buildIfGraph generates auxiliary
	// graph objects).

	/**
	 * Creates a new SgfBuilder.
	 */
	private SgfBuilder() {
		symbols = new HashMap<String, Object>();
	}

	/**
	 * This class represents a graph together with the names of its nodes and edges.
	 */
	private static class GraphBuildInfo {
		public Map<String, Node> nodeNames;
		public Map<String, Edge> edgeNames;
		public int line;
		public int column;

		public GraphBuildInfo(Map<String, Node> nodeNames, Map<String, Edge> edgeNames, int line, int column) {
			this.nodeNames = nodeNames;
			this.edgeNames = edgeNames;
			this.line = line;
			this.column = column;
		}
	}

	/**
	 * Builds a graph from a graph definition term.
	 */
	private Graph buildGraph(GraphDefTerm term) throws SgfBuildException {
		Graph graph = new Graph();
		Map<String, Node> nodeNames = new HashMap<String, Node>();
		Map<String, Edge> edgeNames = new HashMap<String, Edge>();
		
		/* Check if all node names match v%d. If yes, then use these IDs as
		 * node IDs, to better recreate the original graph (SgfWriter stores
		 * node IDs like that). If not, simply assign new IDs as necessary.
		 */
		boolean useNodeIds = true;
		Pattern vnum = Pattern.compile("v([0-9]+)");
		terms: for (SgfTerm elem : term.getElements()) {
			if (elem instanceof NodeDefTerm ne) {
				if (!vnum.matcher(ne.getName()).matches()) {
					useNodeIds = false;
					break terms;
				}
			} else if (elem instanceof EdgeDefTerm ee) {
				for (String nodeName : ee.getNodes()) {
					if (!vnum.matcher(nodeName).matches()) {
						useNodeIds = false;
						break terms;
					}
				}
			}
		}

		for (SgfTerm elem : term.getElements()) {
			if (elem instanceof NodeDefTerm) {
				NodeDefTerm ne = (NodeDefTerm) elem;
				String name = ne.getName();
				if (edgeNames.containsKey(name)) {
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							String.format("Name \"%s\" already used for an edge.", name));
				}
				if (!nodeNames.containsKey(name)) {
					if (useNodeIds) {
						Matcher m = vnum.matcher(name);
						m.matches();
						nodeNames.put(name, graph.addNodeWithId(Integer.parseInt(m.group(1))));
					} else {
						nodeNames.put(name, graph.addNode());
					}
				}
			} else {
				EdgeDefTerm ee = (EdgeDefTerm) elem;
				String name = ee.getName();
				if (name != null && (nodeNames.containsKey(name) || edgeNames.containsKey(name)))
					throw new SgfBuildException(elem.getLine(), elem.getColumn(), "Duplicate name: " + name);
				List<Node> in = new ArrayList<Node>();
				for (String nodeName : ee.getNodes()) {
					if (edgeNames.containsKey(nodeName))
						throw new SgfBuildException(elem.getLine(), elem.getColumn(), nodeName + " is not a node.");
					Node node = nodeNames.get(nodeName);
					if (node == null) {
						if (useNodeIds) {
							Matcher m = vnum.matcher(nodeName);
							m.matches();
							node = graph.addNodeWithId(Integer.parseInt(m.group(1)));
						} else {
							node = graph.addNode();
						}
						nodeNames.put(nodeName, node);
					}
					in.add(node);
				}
				Label label = new Label(ee.getLabel(), in.size());
				Edge edge = graph.addEdge(label, in);
				if (name != null)
					edgeNames.put(name, edge);
			}
		}

		graphs.put(graph, new GraphBuildInfo(nodeNames, edgeNames, term.getLine(), term.getColumn()));
		return graph;
	}

	/**
	 * Returns the graph represented by a term. The term can be either a name
	 * referring to a previously built graph in the symbol table, or a new graph
	 * definition term.
	 */
	private Graph getGraph(SgfTerm term) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						"No graph with name \"" + name + "\" found.");
			else if (!(ob instanceof Graph))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						"Object \"" + name + "\" is not a graph.");
			return (Graph) ob;
		} else if (term instanceof GraphDefTerm) {
			return buildGraph((GraphDefTerm) term);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Expected: graph definition or reference.");
		}
	}

	/**
	 * Builds a graph from an interface graph definition term.
	 */
	private Graph buildIfGraph(IfGraphDefTerm term) throws SgfBuildException {
		Graph asGraph = buildGraph(term);
		GraphBuildInfo info = graphs.get(asGraph);
		assert info != null;
		InterfaceGraph ifGraph = new InterfaceGraph(asGraph);

		{
			List<RefTerm> leftNames = term.getLeftInterface();
			for (RefTerm name : leftNames) {
				Node node = info.nodeNames.get(name.getName());
				if (node == null)
					throw new SgfBuildException(name.getLine(), name.getColumn(), "Unknown node:" + name.getName());
				ifGraph.getInnerInterface().add(node);
			}
		}

		{
			List<RefTerm> rightNames = term.getRightInterface();
			for (RefTerm name : rightNames) {
				Node node = info.nodeNames.get(name.getName());
				if (node == null)
					throw new SgfBuildException(name.getLine(), name.getColumn(), "Unknown node:" + name.getName());
				ifGraph.getOuterInterface().add(node);
			}
		}

		graphs.put(ifGraph, new GraphBuildInfo(info.nodeNames, info.edgeNames, term.getLine(), term.getColumn()));
		return ifGraph;
	}

	/**
	 * Returns the interface graph represented by a term. The term can be either a
	 * name referring to a previously built interface graph in the symbol table, or
	 * a new interface graph definition term.
	 */
	@SuppressWarnings("unused")
	private Graph getIfGraph(SgfTerm term) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						"No graph with name \"" + name + "\" found.");
			else if (!(ob instanceof Graph))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						"Object \"" + name + "\" is not an interface graph.");
			Graph graph = (Graph) ob;
			if (!(graph instanceof InterfaceGraph))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						"Object \"" + name + "\" is not an interface graph.");
			return graph;
		} else if (term instanceof IfGraphDefTerm) {
			return buildIfGraph((IfGraphDefTerm) term);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
					"Expected: interface graph definition or reference.");
		}
	}

	/**
	 * Builds a morphism from a morphism definition term.
	 */
	private Morphism buildMorphism(MorphismDefTerm term, Graph defFrom, Graph defTo) throws SgfBuildException {
		Graph from = getGraph(term.getFrom());
		if (from == null) {
			if (defFrom == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(), "Source graph not specified.");
			from = defFrom;
		} else if (defFrom != null && defFrom != from) {
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Illegal source graph.");
		}

		Graph to = getGraph(term.getTo());
		if (to == null) {
			if (defTo == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(), "Target graph not specified.");
			to = defTo;
		} else if (defTo != null && defTo != to) { // XXX: defTo != from or != to??
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Illegal target graph.");
		}

		Morphism morph = new Morphism(from, to);

		GraphBuildInfo fromInfo = graphs.get(from);
		GraphBuildInfo toInfo = graphs.get(to);

		for (MapstoTerm elem : term.getMaps()) {
			String srcName = elem.getLeft();
			String tgtName = elem.getRight();

			Edge srcEdge = fromInfo.edgeNames.get(srcName);
			if (srcEdge != null) {
				Edge tgtEdge = toInfo.edgeNames.get(tgtName);
				if (tgtEdge == null)
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							tgtName + " is not an edge of the target graph.");
				if (!morph.checkAndPut(srcEdge, tgtEdge))
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							"Illegal morphism: " + srcName + " cannot be mapped to " + tgtName + ".");
			} else {
				Node srcNode = fromInfo.nodeNames.get(srcName);
				if (srcNode == null)
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							srcName + " is not a node or edge of the source graph.");
				Node tgtNode = toInfo.nodeNames.get(tgtName);
				if (tgtNode == null)
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							tgtName + " is not a node of the target graph.");
				if (!morph.checkAndPut(srcNode, tgtNode))
					throw new SgfBuildException(elem.getLine(), elem.getColumn(),
							"Illegal morphism: " + srcName + " cannot be mapped to " + tgtName + ".");
			}
		}

		return morph;
	}

	/**
	 * Returns the morphism represented by a term. The term can be either a name
	 * referring to a previously built morphism in the symbol table, or a new
	 * morphism definition term.
	 */
	private Morphism getMorphism(SgfTerm term, Graph defFrom, Graph defTo) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("No morphism with name \"%s\" found.", name));
			else if (!(ob instanceof Morphism))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("\"%s\" is not a morphism.", name));
			return (Morphism) ob;
		} else if (term instanceof MorphismDefTerm) {
			return buildMorphism((MorphismDefTerm) term, defFrom, defTo);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
					"Expected: morphism definition or reference.");
		}
	}

	/**
	 * Returns a trace represented by a term.
	 */
	private Trace buildTrace(TraceDefTerm term) throws SgfBuildException {
		Trace trace = new Trace();
		List<SgfTerm> items = term.getMorphisms();

		if (!items.isEmpty()) {
			SgfTerm first = items.get(0);
			boolean sh = false;
			if (first instanceof RefTerm) {
				String name = ((RefTerm) first).getName();
				Object ob = symbols.get(name);
				if (ob == null) {
					throw new SgfBuildException(first.getLine(), first.getColumn(), "Unknown object: " + name);
				} else if (ob instanceof Graph) {
					sh = true;
				}
			} else if (first instanceof GraphDefTerm) {
				sh = true;
			}

			if (sh) {
				Iterator<SgfTerm> it = items.iterator();
				Graph end = getGraph(it.next());
				while (it.hasNext()) {
					Graph newEnd = getGraph(it.next());
					Morphism m = createMorphism(end, newEnd, false);
					trace.addTransition(m);
					end = newEnd;
				}
			} else {
				Graph end = null;
				for (SgfTerm mt : items) {
					Morphism m = getMorphism(mt, end, null);
					end = m.getCodomain();
					trace.addTransition(m);
				}
			}
		}

		return trace;
	}

	/**
	 * Builds a cospan from a cospan definition term.
	 */
	private Cospan buildCospan(CospanDefTerm term) throws SgfBuildException {
		Morphism left = getMorphism(term.getLeft(), null, null);
		Morphism right = getMorphism(term.getRight(), null, null);
		return new Cospan(left, right);
	}

	/**
	 * Builds a cospan from a cospan definition term in shorthand form.
	 */
	private Cospan buildCospan(QuickCospanDefTerm term) throws SgfBuildException {
		Graph left = getGraph(term.getLeft());
		Graph middle = getGraph(term.getMiddle());
		Graph right = getGraph(term.getRight());

		Morphism morphleft = createMorphism(left, middle, false);
		Morphism morphright = createMorphism(right, middle, false);

		return new Cospan(morphleft, morphright);
	}

	/**
	 * Returns the cospan represented by a term. The term can be either a name
	 * referring to a previously built cospan in the symbol table, or a new
	 * cospan definition term.
	 */
	private Cospan getCospan(SgfTerm term) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("No cospan with name \"%s\" found.", name));
			else if (!(ob instanceof Cospan))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("\"%s\" is not a cospan.", name));
			return (Cospan) ob;
		} else if (term instanceof CospanDefTerm) {
			return buildCospan((CospanDefTerm) term);
		} else if (term instanceof QuickCospanDefTerm) {
			return buildCospan((QuickCospanDefTerm) term);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
					"Expected: cospan definition or reference.");
		}
	}

	/**
	 * Builds a rule from a rule definition term.
	 */
	private Rule buildRule(RuleDefTerm term) throws SgfBuildException {
		Graph left = getGraph(term.getLeft());
		Graph right = getGraph(term.getRight());

		Object oldLeft = symbols.put("left", left);
		Object oldRight = symbols.put("right", right);

		Morphism morph = getMorphism(term.getMorphism(), left, right);

		if (oldLeft != null)
			symbols.put("left", oldLeft);
		else
			symbols.remove("left");
		if (oldRight != null)
			symbols.put("right", oldRight);
		else
			symbols.remove("right");

		return new Rule(morph);
	}

	/**
	 * Builds a morphism by mapping nodes and edges with the same name to each
	 * other.
	 *
	 * @param dom       domain of the morphism
	 * @param cod       codomain of the morphism
	 * @param makeTotal if true, the morphism will be made total by adding to the
	 *                  codomain nodes and edges for each node and edge of the
	 *                  domain which is not explicitly mapped to another node or
	 *                  edge
	 */
	private Morphism createMorphism(Graph dom, Graph cod, boolean makeTotal) throws SgfBuildException {
		GraphBuildInfo domInfo = graphs.get(dom);
		GraphBuildInfo codInfo = graphs.get(cod);
		Morphism morph = new Morphism(dom, cod);

		for (Map.Entry<String, Node> entry : domInfo.nodeNames.entrySet()) {
			String name = entry.getKey();
			Node srcNode = entry.getValue();
			Node tgtNode = codInfo.nodeNames.get(name);
			if (tgtNode != null) {
				if (!morph.checkAndPut(srcNode, tgtNode))
					throw new SgfBuildException(codInfo.line, codInfo.column,
							"Illegal morphism: " + name + " cannot be mapped to " + name + ".");
			} else {
				if (codInfo.edgeNames.containsKey(name)) {
					throw new SgfBuildException(codInfo.line, codInfo.column,
							String.format("Node %s is mapped to an edge.", name));
				}
			}
		}

		for (Map.Entry<String, Edge> entry : domInfo.edgeNames.entrySet()) {
			String name = entry.getKey();
			Edge srcEdge = entry.getValue();
			Edge tgtEdge = codInfo.edgeNames.get(name);
			if (tgtEdge != null) {
				if (!morph.checkAndPut(srcEdge, tgtEdge))
					throw new SgfBuildException(codInfo.line, codInfo.column,
							"Illegal morphism: " + name + " cannot be mapped to " + name + ".");
			}
		}

		if (makeTotal) {
			for (Node domNode : dom.getNodes()) {
				Node codNode = morph.get(domNode);
				if (codNode == null) {
					codNode = cod.addNode();
					morph.put(domNode, codNode);
				}
			}
			for (Edge domEdge : dom.getEdges()) {
				Edge codEdge = morph.get(domEdge);
				if (codEdge == null) {
					List<Node> rans = new ArrayList<>(domEdge.getNodes().size());
					for (Node lan : domEdge.getNodes()) {
						Node ran = morph.get(lan);
						if (ran == null) {
							ran = cod.addNode();
							morph.put(lan, ran);
						}
						rans.add(ran);
					}
					codEdge = cod.addEdge(domEdge.getLabel(), rans);
					morph.put(domEdge, codEdge);
				}
			}
		}

		return morph;
	}

	/**
	 * Builds a rule from a rule definition term in shorthand form.
	 */
	private Rule buildRule(QuickRuleDefTerm term) throws SgfBuildException {
		Graph left = getGraph(term.getLeft());
		Graph right = getGraph(term.getRight());

		Morphism morph = createMorphism(left, right, false);

		if (term.getNacs() != null) {
			NacRule result = new NacRule(morph);
			for (SgfTerm t : term.getNacs()) {
				Graph nac = getGraph(t);
				morph = createMorphism(left, nac, true);
				result.addNac(morph);
			}
			return result;
		} else {
			return new Rule(morph);
		}
	}

	/**
	 * Returns a rule which is represented by a term.
	 */
	private Rule getRule(SgfTerm term) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null)
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("No rule with name \"%s\" found.", name));
			else if (!(ob instanceof Rule))
				throw new SgfBuildException(term.getLine(), term.getColumn(),
						String.format("\"%s\" is not a rule.", name));
			return (Rule) ob;
		} else if (term instanceof RuleDefTerm) {
			return buildRule((RuleDefTerm) term);
		} else if (term instanceof QuickRuleDefTerm) {
			return buildRule((QuickRuleDefTerm) term);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
					"Expected: morphism definition or reference.");
		}
	}

	/**
	 * Build a GTS from a GTS definition term.
	 */
	private TransformationSystem buildGts(GtsDefTerm term) throws SgfBuildException {
		Graph initial = getGraph(term.getInitial());
		Object oldInitial = symbols.put("initial", initial);

		List<Rule> rules = new LinkedList<Rule>();
		for (SgfTerm elem : term.getRules()) {
			Rule rule = getRule(elem);
			rules.add(rule);
		}

		if (oldInitial != null)
			symbols.put("initial", oldInitial);
		else
			symbols.remove("initial");

		return TransformationSystem.create(initial, rules);
	}

	/**
	 * Build an explicit transition system from a system definition term.
	 */
	private ExplicitTransitionSystem buildSystem(GtsDefTerm term) throws SgfBuildException {
		Graph initial = getGraph(term.getInitial());
		Object oldInitial = symbols.put("initial", initial);

		Set<Morphism> morphisms = new HashSet<Morphism>();

		for (SgfTerm elem : term.getRules()) {
			Morphism m = getMorphism(elem, null, null);
			morphisms.add(m);
		}

		if (oldInitial != null)
			symbols.put("initial", oldInitial);
		else
			symbols.remove("initial");

		return ExplicitTransitionSystem.createWithMorphisms(initial, morphisms);
	}

	/**
	 * Returns the condition represented by a term.
	 * The term must be a name referring to a previously built condition in the symbol table, or a condition term.
	 */
	private Object getCondition(SgfTerm term) throws SgfBuildException {
		if (term == null) {
			return null;
		} else if (term instanceof RefTerm) {
			RefTerm ref = (RefTerm) term;
			String name = ref.getName();
			Object ob = symbols.get(name);
			if (ob == null) {
				throw new SgfBuildException(term.getLine(), term.getColumn(), "No condition with name \"" + name + "\" found.");
			} else if (ob instanceof CospanCondition) {
				// fine
			} else if (ob instanceof GraphCondition) {
				// fine(?)
			} else {
				throw new SgfBuildException(term.getLine(), term.getColumn(), "Object \"" + name + "\" is not a graph condition or cospan condition.");
			}
			return ob;
		} else if (term instanceof CospanConditionDefTerm) {
			return buildCondition((CospanConditionDefTerm) term);
		} else if (term instanceof GraphConditionDefTerm) {
			return buildCondition((GraphConditionDefTerm) term);
		} else if (term instanceof ConditionNegTerm) {
			return buildNegCondition((ConditionNegTerm) term);
		} else if (term instanceof ConditionAndOrTerm) {
			return buildAndOrCondition((ConditionAndOrTerm) term);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Expected: condition term or reference. Got: " + term.getClass().getSimpleName());
		}
	}

	/**
	 * Builds a cospan condition from a condition definition term
	 * (<code>exists { ... }</code> or <code>forall { ... }</code>).
	 */
	private CospanCondition buildCondition(CospanConditionDefTerm term) throws SgfBuildException {
		List<ChildTuple> children = new ArrayList<>();

		for (CospanConditionTupleTerm childTuple : term.getChildTuples()) {
			Cospan mor = getCospan(childTuple.getMorphism());

			CospanCondition child;
			SgfTerm childTerm = childTuple.getChildCondition();
			if (childTerm instanceof ConstantConditionTerm) {
				ConstantConditionTerm cct = (ConstantConditionTerm) childTerm;
				child = cct.getValue() ? CospanCondition.True(mor) : CospanCondition.False(mor);
			} else {
				Object ochild = getCondition(childTerm);
				if (ochild instanceof CospanCondition) {
					child = (CospanCondition) ochild;
				} else {
					throw new SgfBuildException(childTerm.getLine(), childTerm.getColumn(),
						"Expected cospan condition term, but received " + ochild.getClass().getSimpleName());
				}
			}

			children.add(new ChildTuple(mor, child));
		}

		Graph root;
		if (term.getRoot() != null) {
			root = getGraph(term.getRoot());
		} else if (children.size() > 0) {
			root = children.get(0).getMorphism().getLeftGraph();
		} else {
			throw new SgfBuildException(term.getLine(), "Empty condition requires explicitly specified origin");
		}

		return new CospanCondition(root, term.getQuantifier(), children);
	}

	/**
	 * Builds a graph condition from a condition definition term
	 * (<code>exists(...)</code> or <code>forall(...)</code>).
	 */
	private GraphCondition buildCondition(GraphConditionDefTerm term) throws SgfBuildException {
		Morphism a = getMorphism(term.getMorphism(), null, null);
		SgfTerm childTerm = term.getChildCondition();
		GraphCondition child;
		if (childTerm instanceof ConstantConditionTerm cct) {
			if (cct.getRoot() != null) {
				if (getGraph(cct.getRoot()) != a.getCodomain()) {
					throw new SgfBuildException(childTerm.getLine(), childTerm.getColumn(),
						"Constant term specifies an origin that is not compatible with the morphism of the parent condition"
					);
				}
			}
			child = cct.getValue() ? new True(a.getCodomain()) : new False(a.getCodomain());
		} else {
			Object ochild = getCondition(childTerm);
			if (ochild instanceof GraphCondition) {
				child = (GraphCondition) ochild;
			} else {
				throw new SgfBuildException(childTerm.getLine(), childTerm.getColumn(),
					"Expected graph condition term, but received " + ochild.getClass().getSimpleName());
			}
		}

		if (term.getQuantifier() == Quantifier.Exists) {
			return new Existential(a, child);
		} else {
			return new ForAll(a, child);
		}
	}

	private Object buildConstCondition(ConstantConditionTerm term) throws SgfBuildException {
		if (term.getRoot() == null) {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
				"Constant term outside of quantifier must specify origin (over ...)");
		}
		if (term.getValue()) {
			return new True(getGraph(term.getRoot()));
		} else {
			return new False(getGraph(term.getRoot()));
		}
	}

	private Object buildNegCondition(ConditionNegTerm term) throws SgfBuildException {
		Object child = getCondition(term.getChild());
		if (child instanceof CospanCondition) {
			return ((CospanCondition) child).negate();
		} else if (child instanceof GraphCondition) {
			return new Negation((GraphCondition) child);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
				"Expected graph condition or cospan condition, but received "
				+ child.getClass().getSimpleName());
		}
	}

	private Object buildAndOrCondition(ConditionAndOrTerm term) throws SgfBuildException {
		if (term.getChildren().size() == 0) {
			// FIXME: we *could* probably allow this, with extra info or extra syntax
			throw new SgfBuildException(term.getLine(), term.getColumn(),
				"Empty conjunction/disjunction not allowed (type/origin unclear)");
		}

		// Determine type (graph/cospan)
		Object first = getCondition(term.getChildren().get(0));
		if (first instanceof CospanCondition) {
			List<CospanCondition> children = new ArrayList<>();
			for (SgfTerm childTerm : term.getChildren()) {
				Object ochild = getCondition(childTerm);
				if (ochild instanceof CospanCondition) {
					children.add((CospanCondition) ochild);
				} else {
					throw new SgfBuildException(childTerm.getLine(), childTerm.getColumn(),
						"Expected cospan condition term, but received " + ochild.getClass().getSimpleName());
				}
			}
			if (term.getQuantifier() == Quantifier.Exists) {
				return CospanCondition.Or(children);
			} else {
				return CospanCondition.And(children);
			}
		} else if (first instanceof GraphCondition) {
			List<GraphCondition> children = new ArrayList<>();
			for (SgfTerm childTerm : term.getChildren()) {
				Object ochild = getCondition(childTerm);
				if (ochild instanceof GraphCondition) {
					children.add((GraphCondition) ochild);
				} else {
					throw new SgfBuildException(childTerm.getLine(), childTerm.getColumn(),
						"Expected graph condition term, but received " + ochild.getClass().getSimpleName());
				}
			}
			if (term.getQuantifier() == Quantifier.Exists) {
				return new Disjunction(children);
			} else {
				return new Conjunction(children);
			}
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
				"Expected graph condition or cospan conditions, but received "
				+ first.getClass().getSimpleName());
		}
	}

	/**
	 * Builds a copy of an object based on a copy term.
	 */
	private Object buildCopy(CopyTerm term) throws SgfBuildException {
		String name = term.getName();
		Object ob = symbols.get(name);
		if (ob == null) {
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Unknown object: " + name);
		}

		if (ob instanceof InterfaceGraph) {
			return new InterfaceGraph((InterfaceGraph) ob);
		} else if (ob instanceof Graph) {
			return new Graph((Graph) ob);
		} else if (ob instanceof Rule) {
			return ((Rule) ob).clone();
		} else if (ob instanceof Trace) {
			return new Trace((Trace) ob);
		} else if (ob instanceof ExplicitTransitionSystem) {
			return new ExplicitTransitionSystem((ExplicitTransitionSystem) ob);
		} else {
			throw new SgfBuildException(term.getLine(), term.getColumn(),
					String.format("Object \"%s\" cannot be copied.", name));
		}
	}

	/**
	 * Build an object from a definition term.
	 */
	private Object buildObject(SgfTerm input) throws SgfBuildException {
		if (input instanceof IfGraphDefTerm)
			return buildIfGraph((IfGraphDefTerm) input);
		else if (input instanceof GraphDefTerm)
			return buildGraph((GraphDefTerm) input);
		else if (input instanceof RuleDefTerm)
			return buildRule((RuleDefTerm) input);
		else if (input instanceof QuickRuleDefTerm)
			return buildRule((QuickRuleDefTerm) input);
		else if (input instanceof CospanDefTerm)
			return buildCospan((CospanDefTerm) input);
		else if (input instanceof QuickCospanDefTerm)
			return buildCospan((QuickCospanDefTerm) input);
		else if (input instanceof MorphismDefTerm)
			return buildMorphism((MorphismDefTerm) input, null, null);
		else if (input instanceof TraceDefTerm)
			return buildTrace((TraceDefTerm) input);
		else if (input instanceof GtsDefTerm) {
			GtsDefTerm gtsInput = (GtsDefTerm) input;
			if (gtsInput.isExplicitTransitionSystem())
				return buildSystem(gtsInput);
			else
				return buildGts((GtsDefTerm) input);
		} else if (input instanceof GraphConditionDefTerm) {
			return buildCondition((GraphConditionDefTerm) input);
		} else if (input instanceof CospanConditionDefTerm) {
			return buildCondition((CospanConditionDefTerm) input);
		} else if (input instanceof ConditionNegTerm) {
			return buildNegCondition((ConditionNegTerm) input);
		} else if (input instanceof ConditionAndOrTerm) {
			return buildAndOrCondition((ConditionAndOrTerm) input);
		} else if (input instanceof ConstantConditionTerm cct) {
			return buildConstCondition(cct);
		} else if (input instanceof CopyTerm) {
			return buildCopy((CopyTerm) input);
		} else
			throw new SgfBuildException(input.getLine(), input.getColumn(), "Illegal definition.");
	}

	/**
	 * Builds objects from a list of definition terms.
	 */
	private Map<String, Object> buildObjects(List<SgfTerm> terms) throws SgfBuildException {
		for (SgfTerm term : terms) {
			if (term instanceof LetTerm) {
				LetTerm letTerm = (SgfTerm.LetTerm) term;
				String name = letTerm.getName();
				if (symbols.containsKey(name))
					throw new SgfBuildException(term.getLine(), term.getColumn(), "Duplicate name: " + name);
				Object ob = buildObject(letTerm.getValue());
				symbols.put(name, ob);
			} else {
				Object ob = buildObject(term);
				String name = getAnonymousObjectName(ob);
				symbols.put(name, ob);
			}
		}

		return symbols;
	}

	/**
	 * Constructs a graph from a graph definition term.
	 *
	 * @param term the term to convert to a graph
	 * @return the graph represented by {@code term}
	 * @throws NullPointerException if {@code term} is {@code null}
	 * @throws SgfBuildException    if {@code term} does not define a graph or if
	 *                              another semantic error occurs in the term
	 */
	public static Graph termToGraph(SgfTerm term) throws SgfBuildException {
		if (term == null)
			throw new NullPointerException();

		if (term instanceof GraphDefTerm) {
			SgfBuilder builder = new SgfBuilder();
			return builder.buildGraph((GraphDefTerm) term);
		} else
			throw new SgfBuildException(term.getLine(), term.getColumn(), "Not a graph definition.");
	}

	/**
	 * Constructs a collection of named objects from a list of terms.
	 *
	 * @param terms the list of term to construct objects for
	 * @return a map mapping names to objects
	 * @throws NullPointerException if {@code terms} is {@code null} or contains
	 *                              {@code null} elements
	 * @throws SgfBuildException    if a semantic error occurs somewhere in the
	 *                              terms
	 */
	public static Map<String, Object> termsToObjects(List<SgfTerm> terms) throws SgfBuildException {
		SgfBuilder builder = new SgfBuilder();
		builder.buildObjects(terms);

		for (Map.Entry<String, Object> entry : builder.symbols.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof NamedObject) {
				((NamedObject) value).setName(key);
			}
		}

		return builder.symbols;
	}

	private static int graphNum = 0;
	private static int ruleNum = 0;
	private static int systemNum = 0;
	private static int morphismNum = 0;
	private static int defaultNum = 0;

	/**
	 * Returns a new name for an anonymous object.
	 */
	private static String getAnonymousObjectName(Object o) {
		if (o instanceof Graph) {
			return String.format("graph-%d", graphNum++);
		} else if (o instanceof Rule) {
			return String.format("rule-%d", ruleNum++);
		} else if (o instanceof TransitionSystem) {
			return String.format("system-%d", systemNum++);
		} else if (o instanceof Morphism) {
			return String.format("morphism-%d", morphismNum++);
		} else {
			return String.format("object-%d", defaultNum++);
		}
	}
}
