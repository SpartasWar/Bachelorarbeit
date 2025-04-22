package de.uni_due.inf.ti.Test;


import de.uni_due.inf.ti.util.Pair;

import java.util.Map;
import java.util.stream.Stream;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition.ChildTuple;
import de.uni_due.inf.ti.graph.condition.cospan.Quantifier;
import de.uni_due.inf.ti.graph.ext.Cospan;
import de.uni_due.inf.ti.graph.ext.PushoutComplement;
import de.uni_due.inf.ti.graph.ext.Square;
import de.uni_due.inf.ti.graph.io.SgfWriter;

public class TestforPractise {

	public static void main(String[] args) {
		Graph gempty = new Graph();
		Graph gAline = new Graph();
		Node n1 = gAline.addNode();
		Node n2 = gAline.addNode();
		Graph twoNodes = gAline.clone();
		gAline.addEdge(new Label("A"), n1,n2);
		Graph gABLine = new Graph();
		n1 = gABLine.addNode();
		n2 = gABLine.addNode();
		Node n3 = gABLine.addNode();
		Graph treeNodes = gABLine.clone();
		gABLine.addEdge(new Label("A"), n1,n2);
		gABLine.addEdge(new Label("B"), n2,n3);
		Morphism m_emptym_gAline = Morphism.getMatch(gempty, gAline, true);
		Morphism m_twoNodes_gAline = Morphism.getMatch(twoNodes, gAline, true);
		Morphism m_emptym_gABline = Morphism.getMatch(gempty, gABLine, true);
		Morphism m_treeNodes_gABline = Morphism.getMatch(treeNodes, gABLine, true);
		Morphism m_gAline_gABline = Morphism.getMatch(gAline, gABLine, true);
		
		
		Cospan c1 = new Cospan(m_emptym_gAline, m_twoNodes_gAline);
		Cospan c2 = new Cospan(m_emptym_gABline, m_treeNodes_gABline);
		CospanCondition False_gAline = CospanCondition.False(c1);
		CospanCondition False_gABline = CospanCondition.False(c2);
		ChildTuple ct1 = new ChildTuple(c1, False_gAline);
		ChildTuple ct2 = new ChildTuple(c2, False_gABline);
		CospanCondition con1 = new CospanCondition(gempty, Quantifier.Forall, ct1);
		CospanCondition con2 = new CospanCondition(gempty, Quantifier.Forall, ct2);
		CospanCondition Conjunktion = CospanCondition.And(con1,con2,con1,con2);
		CospanCondition Conjunktion2 = CospanCondition.And(Conjunktion,con1);
		//System.out.print(Conjunktion2+"\n");
		System.out.print("Conjunktion: \n" + Conjunktion + "\n" );
		System.out.print("Simplify: \n" + Simplify(Conjunktion) + "\n");
		
		
	}
	
	
	
	
	
	
	
	
	/**
	 * Needs a condition of the form 
	 * @param con1 ForAll c1 GC or Exist c1 GC
	 * @param con2 ForAll c2 GC or Exist c2 GC
	 * @return returns a List of {@link CospanCondition}s, if it was possible to simplify the list contains only one Object
	 */
	public static Pair<List<CospanCondition>, List<CospanCondition>> simplify_Leaf(CospanCondition condition1,CospanCondition condition2) {
		List<CospanCondition> NewConList = new ArrayList<>();
		List<CospanCondition> RemovedList = new ArrayList<>();
		if(condition1.getChildren().size() == 1 && condition2.getChildren().size() == 1 && condition1.getQuantifier() == condition2.getQuantifier()) {
			ChildTuple ct1 = condition1.getChildren().getFirst();
			ChildTuple ct2 = condition2.getChildren().getFirst();	
			Cospan c1 = ct1.getMorphism();
			Cospan c2 = ct2.getMorphism();			
			CospanCondition con1 = ct1.getChildCondition();
			CospanCondition con2 = ct2.getChildCondition();		
			boolean leaf_Condition = con1.getChildren().size() == 0 && con2.getChildren().size() == 0;
			boolean from_c1_to_c2 = false;
			boolean from_c2_to_c1 = false;
			CospanCondition newCon1 = null; // wird nie passieren!
			CospanCondition newCon2 = null; // wird nie passieren!
			if(check_subgraph(c1, c2)) {
				from_c1_to_c2 = true;
				newCon1 = condition1;
				newCon2 = condition2;
			}
			if(check_subgraph(c2, c1)) {					
				from_c2_to_c1 = true;
				newCon1 = condition2;
				newCon2 = condition1;
			}			
			if(from_c1_to_c2 && from_c2_to_c1 && con1.getQuantifier() == con2.getQuantifier()) {
				if(!leaf_Condition) {
					List<ChildTuple> NewChilds = con1.getChildren();
					 NewChilds.addAll(con2.getChildren());
					 CospanCondition newUnderCon = new CospanCondition(con1.getOrigin(),con1.getQuantifier(),NewChilds);
					 ChildTuple nct1 = new ChildTuple(c2, newUnderCon);
					 NewConList.add(new CospanCondition(condition1.getOrigin(),condition1.getQuantifier(),nct1));
					 RemovedList.add(condition2);
					 RemovedList.add(condition1);
				}
				else {
					NewConList.add(newCon1);
				}
			}
			else if(from_c1_to_c2 || from_c2_to_c1) {
				if(condition1.getQuantifier() == Quantifier.Forall) {
					NewConList.add(newCon1);
					RemovedList.add(newCon2);
				}
				else {
					NewConList.add(newCon2);
					RemovedList.add(newCon1);
				}
			}
			else {
				NewConList.add(condition1);
				NewConList.add(condition2);
			}
		}
		else {
			NewConList.add(condition1);
			NewConList.add(condition2);
		}
		Pair<List<CospanCondition>, List<CospanCondition>> result = new Pair<>(NewConList,RemovedList);
		return result;
	}
	
	/**
	 * @return returns true if c1 is subGraph of c2
	 */
	public static boolean check_subgraph(Cospan c1,Cospan c2) {
		Morphism m1 = Morphism.getMatch(c1.getMiddleGraph(),c2.getMiddleGraph(), true);	
		if(m1 == null) return false;
		Square s1 = PushoutComplement.compute(c1.getRightMorphism(), m1);
		if(s1 == null || Morphism.getMatch(c2.getRightGraph(), s1.getRightGraph(), true) == null) return false;		
		return true;
	}
	
	public static CospanCondition Simplify(CospanCondition condition) {
		List<ChildTuple> childtuples = condition.getChildren();
		if(childtuples.size() == 0) return condition;
		if(childtuples.size() == 1) return new CospanCondition(condition.getOrigin(),condition.getQuantifier(),new ChildTuple(childtuples.getFirst().getMorphism(),Simplify(childtuples.getFirst().getChildCondition())));
		if(childtuples.size() > 1) return new CospanCondition(condition.getOrigin(),condition.getQuantifier(),applysimplify_Leaf(childtuples));
		return condition;
	}
	
	
	public static List<ChildTuple> applysimplify_Leaf(List<ChildTuple> childs){
		List<ChildTuple> newChilds = new ArrayList<>();
		List<CospanCondition> newCons = new ArrayList<>();
		List<CospanCondition> removedCon = new ArrayList<>();
		for(int i = 0; i < childs.size();i++) {
			for(int j = i; j < childs.size();j++) {
				if(i==j) continue;
				Pair<List<CospanCondition>, List<CospanCondition>> leaf_opti = simplify_Leaf(childs.get(i).getChildCondition(),childs.get(j).getChildCondition());
				removedCon.addAll(leaf_opti.getSecond());
				newCons.addAll(leaf_opti.getFirst());
			}
		}	
		for(CospanCondition con: newCons) {
			Graph origin = con.getOrigin();
			Morphism idO = Morphism.createIdentity(origin);
			Cospan id = new Cospan(idO, idO);
			ChildTuple newChild = new ChildTuple(id, Simplify(con));
			if(!newChilds.contains(newChild) && !removedCon.contains(con)) newChilds.add(newChild);
		}
		return newChilds;
	}
	
}
