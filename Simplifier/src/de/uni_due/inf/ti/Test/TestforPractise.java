package de.uni_due.inf.ti.Test;

import de.uni_due.inf.ti.tableaugc.TableauSolver;

import java.util.Map;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;


import de.uni_due.inf.ti.gc_cvc.*;
import de.uni_due.inf.ti.gcsolvers.*;
import de.uni_due.inf.ti.gcsolvers.MakeAlternating.DNFUnknownConditionException;
import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Constant;
import de.uni_due.inf.ti.graph.condition.Disjunction;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.False;
import de.uni_due.inf.ti.graph.condition.ForAll;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.condition.NAryConnective;
import de.uni_due.inf.ti.graph.condition.Negation;
import de.uni_due.inf.ti.graph.condition.QuantifiedCondition;
import de.uni_due.inf.ti.graph.condition.True;
import de.uni_due.inf.ti.graph.io.SgfWriter;

public class TestforPractise {

	public static void main(String[] args) {
			
		
		Graph gempty = new Graph();
		Graph gnode = new Graph();
		gnode.addNode();
		Graph gloop = new Graph();
		Node n1 = gloop.addNode();
		gloop.addEdge(new Label("A"), n1,n1);
		Morphism m = Morphism.getMatch(gempty, gnode);
		Morphism m1 = Morphism.getMatch(gnode, gloop);
		Morphism m2 = Morphism.getMatch(gloop, gloop);
		
		GraphCondition condition1 = RandomGraph.randomCondition();	
		GraphCondition condition2 = new Existential(m1,new Existential(m2,new True(gloop)));
		GraphCondition condition3 = new Disjunction(condition2,condition2);
		GraphCondition condition4 = new Existential(m, condition3);
		GraphCondition condition = condition1;
		
		
		System.out.print("RandomCondition: \n"+ condition +"\n");
		condition = going_through_Condition(condition);		
		System.out.print("Condition: \n"+ condition +"\n");
		
		
		if(!MakeAlternating.isAlternating(condition)) {
			condition = MakeAlternating.makeAlternating(condition);
		}
		System.out.print("Alternating: \n"+ condition+"\n");
		TableauSolver solver = new TableauSolver(condition);
		do {
			solver.step();
		} while (solver.getResult() == null);
		System.out.println("Solver result: " + solver.getResult());
		if (solver.getResult() == SolverResult.Satisfiable) {
			System.out.println("Model:");
			SgfWriter w = new SgfWriter(System.out);
			w.writeMorphism(solver.getModel());
		}

	}
	
	public static GraphCondition SimplificationQuantified(GraphCondition condition) {
		if(condition instanceof QuantifiedCondition qc) {
			Morphism m = qc.getMorphism();
			if(m.isIsomorphism()) {
				return eliminateIsomorphism(m, qc.getChildCondition());
			}		
		}
		if(condition instanceof Existential Exc) {
			GraphCondition inner = Exc.getChildCondition();
			if(inner instanceof False) {
				return new False(Exc.getOrigin());
			}
			if(inner instanceof Existential innerExc) {
				return Exc.newSameType(Exc.getMorphism().compose(innerExc.getMorphism()), innerExc.getChildCondition());
			}
			if(inner instanceof Disjunction Dis) {
				List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
				for(GraphCondition ininner: Dis.getChildConditions()) {
					newChilds.add(new Existential(Exc.getMorphism(), ininner));
				}
				return Dis.newSameType(Exc.getOrigin(), newChilds);
			}
		}
		if(condition instanceof ForAll FoA) {
			GraphCondition inner = FoA.getChildCondition();
			if(inner instanceof True) {
				return new True(FoA.getOrigin());
			}
			if(inner instanceof ForAll innerFoA) {
				return FoA.newSameType(FoA.getMorphism().compose(innerFoA.getMorphism()), innerFoA.getChildCondition());
			}
			if(inner instanceof Disjunction Dis) {
				List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
				for(GraphCondition ininner: Dis.getChildConditions()) {
					newChilds.add(new Existential(FoA.getMorphism(), ininner));
				}
				return Dis.newSameType(FoA.getOrigin(), newChilds);
			}
		}
		return condition;
	}
	
	
	
	public static GraphCondition SimplificationNary(GraphCondition condition) {	
		// ^ / and
		if(condition instanceof Conjunction Con) {
			List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
			for(GraphCondition inner: Con.getChildConditions()) {
				if(inner instanceof False innerf) {
					return innerf.newSame(Con.getOrigin());
				}
				else if(inner instanceof Conjunction innerCon){
					newChilds.addAll(innerCon.getChildConditions());
				}
				else {
					newChilds.add(inner);
				}
			}
			if(newChilds.size() == 0) return new True(Con.getOrigin());
			return Con.newSameType(Con.getOrigin(), newChilds);
		}
		// v / or
		if(condition instanceof Disjunction Dis) {
			List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
			for(GraphCondition inner: Dis.getChildConditions()) {
				if(inner instanceof True innert) {
					return innert.newSame(Dis.getOrigin());
				}
				else if(inner instanceof Disjunction innerDis) {
					newChilds.addAll(innerDis.getChildConditions());
				}
				else {
					newChilds.add(inner);
				}
			}
			if(newChilds.size() == 0) return new False(Dis.getOrigin());
			return Dis.newSameType(Dis.getOrigin(), newChilds);
		}
		
		return condition;
	}
	
	
	public static GraphCondition going_through_Condition(GraphCondition condition) {
		
		if(condition instanceof Constant) return condition;
		if(condition instanceof NAryConnective NAc) {
			// Abzweigungen um verschiedene Pfade zu erstellen
			List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
			for(GraphCondition inner: NAc.getChildConditions()) {
				newChilds.add(going_through_Condition(inner));
			}
			return SimplificationNary(NAc.newSameType(NAc.getOrigin(), newChilds));
		}
		if(condition instanceof QuantifiedCondition qc) {
			return SimplificationQuantified(qc.newSameType(qc.getMorphism(), going_through_Condition(qc.getChildCondition())));
		}		
		if(condition instanceof Negation Nc) {
			GraphCondition Nccon = Nc.getChildCondition();
			if(Nccon instanceof QuantifiedCondition qc) {
				return going_through_Condition(qc.newOppositeType(qc.getMorphism(), new Negation(qc.getChildCondition())));
			}
			if(Nccon instanceof NAryConnective NAc) {
				Stream<GraphCondition> newStream = NAc.getChildConditions().stream().map(Negation::new);
				return going_through_Condition(NAc.newOppositeType(NAc.getOrigin(), newStream));
			}
			if(Nccon instanceof Constant c) {
				return c instanceof True ? new False(c.getOrigin()): new True(c.getOrigin());
			}
			if(Nccon instanceof Negation n) {
				return going_through_Condition(n.getChildCondition());
			}
			return new Negation(going_through_Condition(Nc.getChildCondition()));
		}
		return condition;
	}
	
	
	private static GraphCondition eliminateIsomorphism(Morphism morphism, GraphCondition child) {
		if (child instanceof Constant) {
			return ((Constant) child).newSame(morphism.getDomain());
		} else if (child instanceof Negation childN) {
			return new Negation(eliminateIsomorphism(morphism, childN.getChildCondition()));
		} else if (child instanceof QuantifiedCondition childQ) {
			return childQ.newSameType(morphism.compose(childQ.getMorphism()), childQ.getChildCondition());
		} else if (child instanceof NAryConnective childD) {
			Stream<GraphCondition> nchildren = childD.getChildConditions().stream()
					.map(c -> eliminateIsomorphism(morphism, c));
			return childD.newSameType(morphism.getDomain(), nchildren);
		} else {
			throw new DNFUnknownConditionException(child, "isomorphism elimination");
		}
	}
	
}
