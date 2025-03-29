/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink
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

// 2014-??-?? - Created

package de.uni_due.inf.ti.graph.io;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.ExplicitTransitionSystem;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.InterfaceGraph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.NacRule;
import de.uni_due.inf.ti.graph.NamedObject;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.Trace;
import de.uni_due.inf.ti.graph.TransformationSystem;
import de.uni_due.inf.ti.graph.Transition;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Constant;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.condition.NAryConnective;
import de.uni_due.inf.ti.graph.condition.Negation;
import de.uni_due.inf.ti.graph.condition.QuantifiedCondition;
import de.uni_due.inf.ti.graph.condition.True;

public class SgfWriter
{
    private enum ObjectType {
        GRAPH, 
        MORPHISM, 
        RULE,
        GTS,
        TRACE, 
        TR_SYS,
        CONDITION
    }
    
    private static final EnumMap<ObjectType,String> typeStrings;
    static {
        typeStrings = new EnumMap<>(ObjectType.class);
        typeStrings.put(ObjectType.GRAPH, "Graph");
        typeStrings.put(ObjectType.MORPHISM, "Morphism");
        typeStrings.put(ObjectType.RULE, "Rule");
        typeStrings.put(ObjectType.GTS, "Gts");
        typeStrings.put(ObjectType.TRACE, "Trace");
        typeStrings.put(ObjectType.TR_SYS, "TrSys");
        typeStrings.put(ObjectType.CONDITION, "GCond");
    }
    
    private PrintStream out;
    private int lastEdgeId = 0;
    private int lastObjId = 0;
    private IdentityHashMap<Object,String> objectNames;
    private IdentityHashMap<Edge,Integer> edgeIds;
    
    /**
     * Creates a new SgfWriter instance.
     * 
     * @param out
     *      OutputStream where the SGF objects are written to
     * @throws NullPointerException
     *      if {@code out} is {@code null}
     */
    public SgfWriter(PrintStream out)
    {
        if (out == null) throw new NullPointerException();
        this.out = out;
        objectNames = new IdentityHashMap<>();
        edgeIds = new IdentityHashMap<>();
    }
    
    /**
     * Writes a graph to this SgfWriter's stream.
     * 
     * @param graph
     *      graph which is written to the stream
     * @return 
     *      name under which the graph was written
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    public String writeGraph(Graph graph) 
    {
        if (graph == null) throw new NullPointerException("graph cannot be null");
        
        String name = objectNames.get(graph);
        
        if (name == null) {
            name = getName(ObjectType.GRAPH, graph);
        
            out.printf("%s = graph {%n", name);
            Set<Node> nodes = new HashSet<>(graph.getNodes());
            for (Edge edge : graph.getEdges()) {
                StringBuilder buf = new StringBuilder();
                boolean comma = false;
                for (Node node : edge.getNodes()) {
                    if (comma) buf.append(','); else comma = true;
                    buf.append(String.format("v%d", node.getId()));
                    nodes.remove(node);
                }
                int edgeId = lastEdgeId++;
                edgeIds.put(edge, edgeId);
                out.printf("  e%d:%s(%s);%n", 
                        edgeId, edge.getLabel().getName(), buf.toString());
            }
            for (Node node : nodes) {
                out.printf("  node v%d;%n", node.getId());
            }
            out.printf("};%n");
        }
        return name;
    }
    
    /**
     * Writes an interface graph to this SgfWriter's stream.
     * 
     * @param graph
     *      graph which is written to the stream
     * @return 
     *      name under which the graph was written
     * @throws NullPointerException
     *      if {@code graph} is {@code null}
     */
    public String writeInterfaceGraph(InterfaceGraph graph)
    {
        if (graph == null) throw new NullPointerException("graph cannot be null");
        
        String name = objectNames.get(graph);

        if (name == null) {
            name = getName(ObjectType.GRAPH, graph);
            
            out.printf("%s = ifgraph {%n", name);
            
            out.print("  [");
            boolean fst = true;
            for (Node node : graph.getLeftInterface()) {
                if (!fst) out.print(',');
                fst = false;
                out.printf("v%d", node.getId());
            }
            out.println("] =>");
            
            out.println("  {");
            Set<Node> nodes = new HashSet<>(graph.getNodes());
            for (Edge edge : graph.getEdges()) {
                StringBuilder buf = new StringBuilder();
                boolean comma = false;
                for (Node node : edge.getNodes()) {
                    if (comma) buf.append(','); else comma = true;
                    buf.append(String.format("v%d", node.getId()));
                    nodes.remove(node);
                }
                int edgeId = lastEdgeId++;
                edgeIds.put(edge, edgeId);
                out.printf("    e%d:%s(%s);%n", 
                        edgeId, edge.getLabel().getName(), buf.toString());
            }
            out.println("  }");
            
            out.print("  <= [");
            fst = true;
            for (Node node : graph.getRightInterface()) {
                if (!fst) out.print(',');
                fst = false;
                out.printf("v%d", node.getId());
            }
            out.println("  ]");
            
            out.println("};");
        }
        
        return name;
    }
    
    /**
     * Writes a morphism to this SgfWriter's stream.
     * 
     * @param morphism
     *      morphism which is to be written 
     * @return
     *      name under which the morphism was written
     * @throws NullPointerException
     *      if {@code morphism} is {@code null}
     */
    public String writeMorphism(Morphism morphism)
    {
        if (morphism == null) throw new NullPointerException("morphism cannot be null");
        
        String name = objectNames.get(morphism);
        
        if (name == null) {
            name = getName(ObjectType.MORPHISM, morphism);
            
            String domName = writeGraph(morphism.getDomain());
            String codName = writeGraph(morphism.getCodomain());
            
            out.printf("%s = morphism from %s to %s {%n", name, domName, codName);
            for (Map.Entry<Node,Node> ne : morphism.getNodeMap().entrySet()) {
                out.printf("  v%d => v%d;%n", ne.getKey().getId(), ne.getValue().getId());
            }
            for (Map.Entry<Edge,Edge> ne : morphism.getEdgeMap().entrySet()) {
                int domId = edgeIds.get(ne.getKey());
                int codId = edgeIds.get(ne.getValue());
                out.printf("  e%d => e%d;%n", domId, codId);
            }
            out.println("};");
        }
        
        return name;
    }
    
    /**
     * Writes a rule to this SgfWriter's output stream.
     * 
     * @param rule
     *      rule to write to the output stream
     * @return
     *      name under which the rule was written to the stream
     * @throws NullPointerException
     *      if {@code rule} is {@code null}
     */
    public String writeRule(Rule rule)
    {
        if (rule == null) throw new NullPointerException("rule cannot be null");
        
        String name = objectNames.get(rule);
        
        if (name == null) {
            name = getName(ObjectType.RULE, rule);
            
            String leftName = writeGraph(rule.getLeft());
            String rightName = writeGraph(rule.getRight());
            String morphName = writeMorphism(rule.getCorrespondence());
            
            List<String> nacNames = null;
            if (rule instanceof NacRule) {
                List<Morphism> nacs = ((NacRule)rule).getNacs();
                nacNames = new ArrayList<>(nacs.size());
                for (Morphism nac : nacs) {
                    nacNames.add(writeMorphism(nac));
                }
            }
            
            out.printf("%s = rule {%n", name);
            out.printf("  left = %s;%n", leftName);
            out.printf("  right = %s;%n", rightName);
            out.printf("  morphism = %s;%n", morphName);
            
            if (nacNames != null) {
                out.print("  nacs = [");
                for (int i = 0; i < nacNames.size(); i++) {
                    if (i > 0) out.println(","); else out.println();
                    out.printf("    %s", nacNames.get(i));
                }
                if (!nacNames.isEmpty()) out.printf("%n  ");
                out.printf("];%n");
            }
            
            out.printf("};%n");
        }
        
        return name;
    }
    
    /**
     * Writes a graph transformation system to this SgfWriter's output stream.
     * 
     * @param system
     *      transformation system to write
     * @return
     *      name under which the transformation system was written
     * @throws NullPointerException
     *      if {@code system} is {@code null}
     */
    public String writeTransformationSystem(TransformationSystem system)
    {
        if (system == null) throw new NullPointerException("system cannot be null");
        
        String name = objectNames.get(system);
        
        if (name == null) {
            name = getName(ObjectType.GTS, system);

            Graph initial = system.getInitialGraph();
            String initialName = initial == null ? null : writeGraph(initial); 
            
            Set<Rule> rules = system.getRules();
            List<String> ruleNames = new ArrayList<>(rules.size());
            for (Rule rule : rules) {
                ruleNames.add(writeRule(rule));
            }
            
            out.printf("%s = gts {%n", name);
            
            if (initialName != null) {
                out.printf("  initial = %s%n", initialName);
            }
           
            out.print("  rules = [");
            
            boolean fst = true;
            for (String ruleName : ruleNames) {
                if (!fst) out.print(",");
                fst = false;
                out.printf("%n    %s", ruleName);
            }
            
            if (fst) out.printf(" ];%n"); 
                else out.printf("%n  ];%n");
            
            out.println("};");
        }
        
        return name;
    }
    
    /**
     * Writes a trace to this SgfWriter's stream.
     * 
     * @param trace
     *      trace to write to the stream
     * @return
     *      name under which the trace is written
     * @throws NullPointerException
     *      if {@code trace} is {@code null}
     */
    public String writeTrace(Trace trace)
    {
        if (trace == null) throw new NullPointerException("trace cannot be null");
        
        String name = objectNames.get(trace);
        
        if (name == null) {
            name = getName(ObjectType.TRACE, trace);
            
            List<Transition> transitions = trace.getTransitions();
            List<String> transNames = new ArrayList<>(transitions.size());
            for (Transition tr : transitions) {
                Morphism m = tr.getMorphism();
                transNames.add(writeMorphism(m));
            }
            
            out.printf("%s = trace {%n", name);
            
            boolean comma = false;
            for (String tr : transNames) {
                out.printf("  %s%s%n", comma ? ", " : "  ", tr);
                comma = true;
            }
            
            out.printf("};%n");
        }
        
        return name;
    }
    
    /**
     * Writes a transition system to this SgfWriter's stream.
     * 
     * @param system
     *      transition system to write to the stream
     * @return
     *      name under which the system is written
     * @throws NullPointerException
     *      if {@code system} is {@code null}
     */
    public String writeTransitionSystem(ExplicitTransitionSystem system)
    {
        if (system == null) throw new NullPointerException();
        
        String name = objectNames.get(system);
        
        if (name == null) {
            name = getName(ObjectType.TR_SYS, system);

            Graph initialGraph = system.getInitialGraph();
            String initial = initialGraph == null ? null : writeGraph(initialGraph);

            List<String> transitions = new LinkedList<>();
            for (Transition tr : system.getTransitions()) {
                Morphism m = tr.getMorphism();
                transitions.add(writeMorphism(m));
            }
            
            out.printf("%s = system {%n", name);
            if (initial != null) out.printf("  initial = %s;%n", initial);

            out.printf("  transitions = [%n");
            boolean comma = false;
            for (String tr : transitions) {
                out.printf("    %s%s%n", comma ? ", " : "  ", tr);
                comma = true;
            }
            out.printf("  ];%n");
            
            out.printf("};%n");
        }
        
        return name;
    }

	/**
	 * Writes a rule to this SgfWriter's output stream.
	 * 
	 * @param rule
	 *      rule to write to the output stream
	 * @return
	 *      name under which the rule was written to the stream
	 * @throws NullPointerException
	 *      if {@code rule} is {@code null}
	 */
	public String writeGraphCondition(GraphCondition cond)
	{
		if (cond == null) throw new NullPointerException("cond cannot be null");
	
		String name = objectNames.get(cond);
	
		if (name == null) {
			name = getName(ObjectType.CONDITION, cond);
			
			if (cond instanceof QuantifiedCondition qc) {
				String morphName = writeMorphism(qc.getMorphism());
				String childName = writeGraphCondition(qc.getChildCondition());
				out.printf("%s = %s(%s, %s);%n",
						name,
						qc instanceof Existential ? "exists" : "forall",
						morphName, childName);
			} else if (cond instanceof NAryConnective nc) {
				List<GraphCondition> children = nc.getChildConditions();
				
				if (children.isEmpty()) {
					/* FIXME: We serialize as a constant here, so we obtain at
					 * least an equivalent condition. Otherwise, the condition
					 * would be written as or[] or and[], which the parser cannot
					 * read because the root object is not known.
					 */
					String rootName = writeGraph(nc.getOrigin());
					out.printf("%s = %s over %s;%n", name, nc instanceof Conjunction ? "true" : "false", rootName);
				} else {
					String childNames = "";
					for (int i = 0; i < children.size(); i++) {
						if (i != 0) childNames += ", ";
						childNames += writeGraphCondition(children.get(i));
					}
					out.printf("%s = %s[%s];%n", name, nc instanceof Conjunction ? "and" : "or", childNames);
				}
			} else if (cond instanceof Constant co) {
				String rootName = writeGraph(co.getOrigin());
				out.printf("%s = %s over %s;%n", name, co instanceof True ? "true" : "false", rootName);
			} else if (cond instanceof Negation ne) {
				String childName = writeGraphCondition(ne.getChildCondition());
				out.printf("%s = not %s;%n", name, childName);
			} else {
				throw new UnsupportedOperationException("unexpected condition type " + cond.getClass().getName());
			}
		}
	
		return name;
	}

    /**
     * Returns the name of an object. If the object does not have a name,
     * a new name is created. 
     * 
     * @param type
     *      type of the object
     * @param ob
     *      object to return the name of
     * @return
     *      the name of the object
     */
	private String getName(ObjectType type, Object ob) {
		assert type != null;
		assert ob != null;

		String name = null;
		if (ob instanceof NamedObject) {
			name = ((NamedObject)ob).getName();
			if (name != null) {
				name = mangleName(name);
			}
		}
		if (name == null) {
			name = typeStrings.get(type) + lastObjId;
			lastObjId++;
		}
		objectNames.put(ob, name);
		return name;
	}

	private String mangleName(String name) {
		return name.replaceAll("[^0-9A-Za-z@_.]", "_");
	}

	public Map<Object, String> getNameMap() {
		return Collections.unmodifiableMap(this.objectNames);
	}
}
