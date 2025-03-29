package de.uni_due.inf.ti.Test;

import de.uni_due.inf.ti.tableaugc.TableauSolver;
import de.uni_due.inf.ti.gc_cvc.*;
import de.uni_due.inf.ti.gcsolvers.*;
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

public class TestforPractise {

	public static void main(String[] args) {
			
		
		Graph gempty = new Graph();
		Graph gloop = new Graph();
		Node n1 = gloop.addNode();
		gloop.addEdge(new Label("A"), n1,n1);
		
	
		GraphCondition condition1 = RandomGraph.randomCondition();	
		GraphCondition condition2 = new Existential(new Morphism(gempty,gloop),new True(gloop));
		
		GraphCondition condition = condition2;
		
		System.out.print("RandomCondition: "+ condition);
		if(!MakeAlternating.isAlternating(condition)) {
			condition = MakeAlternating.makeAlternating(condition);
		}
		System.out.print("Alternating: "+ condition);
		TableauSolver solver = new TableauSolver(condition);
		System.out.print("Result: "+ solver.getResult());
		System.out.print("Model: "+ solver.getModel());

	}
	
	
}
