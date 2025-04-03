package de.uni_due.inf.ti.Test;

import de.uni_due.inf.ti.tableaugc.TableauSolver;
import de.uni_due.inf.ti.util.Pair;

import java.util.Map;
import java.util.stream.Stream;



import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

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
			
		GraphCondition randomcondition = RandomGraph.randomCondition();
		
		Graph gempty = new Graph();
		Graph gnode = new Graph();
		gnode.addNode();
		Graph gline = new Graph();
		Node n1 = gline.addNode();
		Node n2 = gline.addNode();
		gline.addEdge(new Label("A"), n1,n2);
		Graph glineloop = new Graph();
		n1 = glineloop.addNode();
		n2 = glineloop.addNode();
		glineloop.addEdge(new Label("A"), n1,n2);
		glineloop.addEdge(new Label("B"), n1,n1);
		Graph glineline = new Graph();
		n1 = glineline.addNode();
		n2 = glineline.addNode();
		Node n3 = glineline.addNode();
		glineline.addEdge(new Label("A"), n1,n2);
		glineline.addEdge(new Label("C"), n2,n3);
		Graph glinelineback = new Graph();
		n1 = glinelineback.addNode();
		n2 = glinelineback.addNode();
		n3 = glinelineback.addNode();
		glinelineback.addEdge(new Label("A"), n1,n2);
		glinelineback.addEdge(new Label("C"), n2,n3);
		glinelineback.addEdge(new Label("D"), n3,n2);

		Morphism gempty_gnode = Morphism.getMatch(gempty, gnode);
		Morphism gnode_gline = Morphism.getMatch(gnode, gline);
		Morphism gline_glineloop = Morphism.getMatch(gline, glineloop);
		Morphism gnode_glineline = Morphism.getMatch(gnode, glineline);
		Morphism glineline_glinelineback = Morphism.getMatch(glineline, glinelineback);
		
		GraphCondition con1_1 = new ForAll(gline_glineloop, new False(glineloop));
		GraphCondition con1 = new Existential(gnode_gline,con1_1);
		GraphCondition con2_1 = new ForAll(glineline_glinelineback, new False(glinelineback));
		GraphCondition con2 = new Existential(gnode_glineline, con2_1);
		GraphCondition con_dis = new Disjunction(con1,con2);
		GraphCondition con_con = new Conjunction(con1,con2);
		GraphCondition con_fA_dis = new ForAll(gempty_gnode, con_dis);
		GraphCondition con_fA_con = new ForAll(gempty_gnode, con_con);

		GraphCondition condition = randomcondition;
		
		
		System.out.print("RandomCondition: \n"+ condition +"\n");
		GraphCondition condition_Final = simplifiy_Condition(condition);
		System.out.print("Simplify Final: \n"+ condition_Final+"\n");
		
		if(!MakeAlternating.isAlternating(condition_Final)) {
			condition_Final = MakeAlternating.makeAlternating(condition_Final);
		}
		System.out.print("Alternating Final: \n"+ condition_Final+"\n");
		
		if(!MakeAlternating.isAlternating(condition)) {
			 condition = MakeAlternating.makeAlternating(condition);
		}
		
		System.out.print("Alternating: \n"+ condition +"\n");
		
		TableauSolver solver_opti = new TableauSolver(condition_Final);
		do {
			solver_opti.step();
		} while (solver_opti.getResult() == null);
		System.out.println("Solver_opti result: " + solver_opti.getResult());
		if (solver_opti.getResult() == SolverResult.Satisfiable) {
			System.out.println("Model:");
			SgfWriter w = new SgfWriter(System.out);
			w.writeMorphism(solver_opti.getModel());
		}
		
		
		
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
						newChilds.add(SimplificationQuantified(new Existential(Exc.getMorphism(), ininner)));
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
				if(inner instanceof Conjunction Con) {
					List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
					for(GraphCondition ininner: Con.getChildConditions()) {
						newChilds.add(SimplificationQuantified(new Existential(FoA.getMorphism(), ininner)));
					}
					return Con.newSameType(FoA.getOrigin(), newChilds);
				}
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
				return going_through_Condition(qc.newOppositeType(qc.getMorphism(),new Negation(qc.getChildCondition())));
			}
			if(Nccon instanceof NAryConnective NAc) {
				List<GraphCondition> newChilds= new ArrayList<GraphCondition>();
				for(GraphCondition inner: NAc.getChildConditions()) {
					newChilds.add(new Negation(inner));
				}
				return going_through_Condition(NAc.newOppositeType(NAc.getOrigin(), newChilds));
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
	
	
	
	public static GraphCondition complex_simplified(GraphCondition condition) {
		if(condition instanceof Constant) return condition;
		if(condition instanceof QuantifiedCondition qc) {
			return qc.newSameType(qc.getMorphism(),complex_simplified(qc.getChildCondition()));
		}
		if(condition instanceof Negation Nc) {
			return new Negation(complex_simplified(Nc.getChildCondition()));
		}
		if(condition instanceof NAryConnective NAc) {
			List<GraphCondition> childs = NAc.getChildConditions();
			Map<GraphCondition,List<GraphCondition>> istotal = total_pairs(childs);
			List<GraphCondition> used = new ArrayList<>();
			List<GraphCondition> newNacChilds = new ArrayList<>();
			if(condition instanceof Disjunction Dis) {
				List<Pair<GraphCondition,List<GraphCondition>>> sorted_pairs = create_sorted_List(istotal);
				for(Pair<GraphCondition,List<GraphCondition>> pair: sorted_pairs) {
					if(used.contains(pair.getFirst())) continue;
					QuantifiedCondition con = (QuantifiedCondition)pair.getFirst();
					List<GraphCondition> newDisChilds = new ArrayList<>();
					newDisChilds.add(con.getChildCondition());
					for(GraphCondition reducecon: pair.getSecond()) {
						if(used.contains(reducecon)) continue;
						if(reducecon instanceof QuantifiedCondition qc) {
							if(qc.getChildCondition() instanceof QuantifiedCondition innerqc) {
								newDisChilds.add(innerqc.newSameType(Morphism.getMatch(con.getMorphism().getCodomain(), innerqc.getMorphism().getCodomain()),
										innerqc.getChildCondition()));
							}
							if(qc.getChildCondition() instanceof Constant c) {
								newDisChilds.add(c.newSame(con.getMorphism().getCodomain()));
							}
							if(qc.getChildCondition() instanceof NAryConnective innerNAc) {
								newDisChilds.add(innerNAc);
							}
							used.add(reducecon);
						}
					}
					Disjunction newDis = new Disjunction(newDisChilds);
					newNacChilds.add(con.newSameType(con.getMorphism(), newDis));
					used.add(pair.getFirst());
				}
				for(GraphCondition not_used:childs) {
					if(!used.contains(not_used)) newNacChilds.add(not_used);
				}
				return NAc.newSameType(NAc.getOrigin(), newNacChilds);
			}
			if(condition instanceof Conjunction Con) {
				System.out.print("Conjunktion\n");
				
			}

		}
		
		return condition;
	}
	
	
	private static List<Pair<GraphCondition,List<GraphCondition>>> create_sorted_List(Map<GraphCondition,List<GraphCondition>> istotal){
		List<Pair<GraphCondition,List<GraphCondition>>> result = new ArrayList<>();
		for(GraphCondition key: istotal.keySet()) {
			result.add(new Pair<GraphCondition, List<GraphCondition>>(key, istotal.get(key)));
		}
		result.sort((obj1,obj2) -> {
			Pair<GraphCondition,List<GraphCondition>> p1 = obj1;
			Pair<GraphCondition,List<GraphCondition>> p2 = obj2;
			if(p1.getSecond().size() < p2.getSecond().size()) return 1;
			if(p1.getSecond().size() > p2.getSecond().size()) return -1;
			return 0;
		});		
		return result;
	}
	
	
	
	private static Map<GraphCondition,List<GraphCondition>> total_pairs(List<GraphCondition> childs){
		Map<GraphCondition,List<GraphCondition>> istotal = new HashMap<>();
		for(GraphCondition i_con : childs) {
			for(GraphCondition j_con: childs) {
				if(!i_con.equals(j_con)){
					if(i_con instanceof QuantifiedCondition qc_i 
						&& j_con instanceof QuantifiedCondition qc_j  
						&& i_con.getClass().equals(j_con.getClass())) {
						if(istotal(qc_i.getMorphism(), qc_j.getMorphism())) 
						{
							List<GraphCondition> newList = new ArrayList<GraphCondition>();
							if(istotal.containsKey(i_con)) newList = istotal.get(i_con);
							newList.add(j_con);
							istotal.put(i_con, newList);
						}					
					}
				}
			}
		}
		return istotal;
	}
	
	
	private static boolean istotal(Morphism m1, Morphism m2) {
		Morphism m3 = Morphism.getMatch(m1.getCodomain(), m2.getCodomain(), true);
		if(m3 != null && m3.isTotal() && m3.isInjective()) return true;
		return false;
	}
	
	public static GraphCondition simplifiy_Condition(GraphCondition condition) {
		GraphCondition lastcon = null;
		GraphCondition currcon = condition;
		while(!currcon.equals(lastcon)) {
			lastcon = currcon;
			currcon = going_through_Condition(currcon);
			currcon = complex_simplified(currcon);
			//System.out.print("Current Simpli \n" + currcon + "\n");
		}		
		return currcon;
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
