package de.uni_due.inf.ti.Test;

import de.uni_due.inf.ti.tableaugc.TableauSolver;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;


import de.uni_due.inf.ti.gc_cvc.*;
import de.uni_due.inf.ti.gcsolvers.*;
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
		Morphism m = Morphism.getMatch(gloop, gloop);
		
		GraphCondition condition1 = RandomGraph.randomCondition();	
		GraphCondition condition2 = new ForAll(m,new True(gloop));
		GraphCondition condition = condition2;
		
		
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
	
	public static GraphCondition remove_ID(GraphCondition condition) {
		if(condition instanceof QuantifiedCondition qc) {		
			if(qc.getMorphism().isIsomorphism()) {
				return qc.getChildCondition();
			}
		}		
		return condition;
	}
	
	public static GraphCondition going_through_Condition(GraphCondition condition) {
		
		if(condition instanceof Constant) return condition;
		if(condition instanceof NAryConnective NAc) {
			List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
			for(GraphCondition inner: NAc.getChildConditions()) {
				newChilds.add(going_through_Condition(inner));
			}
			return NAc.newSameType(NAc.getOrigin(), newChilds);
		}
		if(condition instanceof QuantifiedCondition qc) {
			return remove_ID(qc.newSameType(qc.getMorphism(), going_through_Condition(qc.getChildCondition())));
		}		
		if(condition instanceof Negation Nc) {
			return new Negation(going_through_Condition(Nc.getChildCondition()));
		}
		return condition;
	}
	
	
	
	
}
