package de.uni_due.inf.ti.graph.Optimiser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition.ChildTuple;
import de.uni_due.inf.ti.graph.condition.cospan.Quantifier;
import de.uni_due.inf.ti.graph.ext.BCDEnumerator;
import de.uni_due.inf.ti.graph.ext.BorrowedContextDiagram;
import de.uni_due.inf.ti.graph.ext.Cospan;
import de.uni_due.inf.ti.graph.ext.PushoutComplement;
import de.uni_due.inf.ti.graph.ext.Square;


/**
 * This Optimiser works on injective Cospan Conditions
 */
public class Optimiser {
	
	private static boolean changed = false;
	
	public static boolean POCOL = true;
	public static boolean POCB = true;
	public static boolean RSTL = true;
	public static boolean compose_over_id = true;
	
	public static CospanCondition optimise(CospanCondition c) {
		//System.out.print("Before: \n" + c + "\n");
		changed = true;
		while(changed) {
			changed = false;
			c = makeAlternating(c);
			c = remove_Identities_and_Iso(c);
			c = going_Through_Condition(c);
		}
		//System.out.print("After: \n" + c + "\n");
		return c;
	}
	
	
	private static List<ChildTuple> optimise_single_child(List<ChildTuple> ct) {
		List<ChildTuple> new_ct = new LinkedList<>();
		if(compose_over_id) {
			ct = compose_over_id(ct.get(0));
		}
		for(ChildTuple ct1: ct) {
			new_ct.add(new ChildTuple(ct1.getMorphism(), going_Through_Condition(ct1.getChildCondition())));
		}
		
		return new_ct;
	}
	
	private static List<ChildTuple> optimise_multi_child(List<ChildTuple> ct_list){
		List<Integer> removed = new LinkedList<>();
		List<ChildTuple> new_CT = new LinkedList<>();
		List<ChildTuple> processed_ct_List = new LinkedList<>();
		if(compose_over_id) {
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(compose_over_id(ct));
			}
			ct_list = processed_ct_List;
		}
		for(int i = 0; i < ct_list.size();i++) {
			for(int j=0; j < ct_list.size();j++) {
				if(removed.contains(i) || i==j) continue;
				ChildTuple ct_c1 = ct_list.get(i);
				ChildTuple ct_c2 = ct_list.get(j);			
				Cospan l = check_subgraph(ct_c1.getMorphism(), ct_c2.getMorphism());
				if (l == null) continue;
				if(RSTL && RSTL(ct_c1.getChildCondition(),ct_c2.getChildCondition(),l)) {					
					removed.add(i);					
				}
				if(POCOL && POCOL(ct_c1.getChildCondition(), ct_c2.getChildCondition())) {					
					if(ct_c1.getChildCondition().getQuantifier() == Quantifier.Exists) {
						removed.add(j);
					}
					else {
						removed.add(i);
					}				
				}
				if(POCB) {
					if(!removed.contains(j) && check_subgraph(ct_c2.getMorphism(), ct_c1.getMorphism()) != null) {
						CospanCondition POCB_OP = POCB(ct_c1.getChildCondition(),ct_c2.getChildCondition());
						new_CT.add(new ChildTuple(ct_c1.getMorphism(), going_Through_Condition(POCB_OP)));
						removed.add(i);
						removed.add(j);
					}
					
				}
			}
		}
		if(removed.size() != 0) changed = true;
		for(int i=0; i < ct_list.size();i++) {
			if(removed.contains(i)) continue;
			new_CT.add(new ChildTuple(ct_list.get(i).getMorphism(), going_Through_Condition(ct_list.get(i).getChildCondition())));
		}
		return new_CT;
	}
	
	/**
	 * Used to run through the conditions
	 * @param condition
	 * @return
	 */
	private static CospanCondition going_Through_Condition(CospanCondition condition) {
		List<ChildTuple> childtuples = condition.getChildren();
		if(childtuples.size() == 0) return condition;
		if(childtuples.size() == 1) return new CospanCondition(condition.getOrigin(),condition.getQuantifier(),optimise_single_child(childtuples));
		if(childtuples.size() > 1) return new CospanCondition(condition.getOrigin(),condition.getQuantifier(),optimise_multi_child(childtuples));
		return condition;
	}
	
	/**
	 * This funktion works on injective Cospans
	 * @return if c1;c3 = c2 it returns c3, if c1;c3 != c2 it returns null
	 */
	private static Cospan check_subgraph(Cospan c1,Cospan c2) {
		Morphism m1 = Morphism.getMatch(c1.getMiddleGraph(),c2.getMiddleGraph(), true);	
		if(m1 == null) return null;
		Square s1 = PushoutComplement.compute(c1.getRightMorphism(), m1);
		if(s1 == null) return null;
		Morphism m2 = Morphism.getMatch(c2.getRightGraph(), s1.getRightGraph(), true);
		if (m2 == null || !s1.getTopRight().isInjective() || !s1.getBottomRight().isInjective() ) return null;
		return new Cospan(s1.getTopRight(), m2);
	}		

	/**
	 * Pushout Complement One Layer
	 * 
	 * @param c1 is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2 is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean POCOL(CospanCondition c1, CospanCondition c2) {
		if(max_depth_of_Condition(c1)!=1 || max_depth_of_Condition(c2)!=1 || c1.getChildren().size() != 0 || c2.getChildren().size() != 0) return false;
		if(c1.getQuantifier() != c2.getQuantifier()) return false;
		return true;
	}
	
	/**
	 * Pushout Complement Bijektiv
	 * 
	 * @param c1 is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2 
	 * @param c2 is the ChildCondition of the ChildTuple(m2,c2) where m2;m3 = m1
	 * @return returns Condition c1 with all childs of c2 additional added
	 */
	private static CospanCondition POCB(CospanCondition c1, CospanCondition c2) {
		List<ChildTuple> new_ct = new LinkedList<>();
		if(c1.getChildren().size() == 0 || c2.getChildren().size() == 0) return new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct);
		for(ChildTuple ct: c1.getChildren()) new_ct.add(new ChildTuple(new Cospan(Morphism.getMatch(c1.getOrigin(), ct.getMorphism().getMiddleGraph(), true), ct.getMorphism().getRightMorphism()), ct.getChildCondition()));
		for(ChildTuple ct: c2.getChildren()) new_ct.add(new ChildTuple(new Cospan(Morphism.getMatch(c1.getOrigin(), ct.getMorphism().getMiddleGraph(), true), ct.getMorphism().getRightMorphism()), ct.getChildCondition()));
		return new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct);
	}
	
	/**
	 * Repräsentaive Squares Two Levels
	 * 
	 * @param c1 is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2 is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2 
	 * @param l is the Cospan m3 from m1;m3 = m2
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean RSTL(CospanCondition c1, CospanCondition c2, Cospan l) {
		List<ChildTuple> ct_c1 = process_ChildTuple_RSZE(c1);
		List<ChildTuple> ct_c2 = process_ChildTuple_RSZE(c2);
		if(ct_c1 == null || ct_c2 == null) return false;		
		Quantifier q = c1.getQuantifier();
		Boolean some_u_found = false;
		for(ChildTuple ct : c1.getChildren()) {
			BCDEnumerator bcde = new BCDEnumerator(l,ct.getMorphism());		
				for(BorrowedContextDiagram bcd : bcde) {
					Cospan u_exists = null;
					for(ChildTuple ct_2 : c2.getChildren()) {					
						u_exists = check_subgraph(ct_2.getMorphism(), bcd.getRight());	
					}
					if(u_exists == null && q == Quantifier.Exists) return false;
					if(u_exists != null && q == Quantifier.Forall) {
						some_u_found = true;
						continue;
					}
				}
				if(q == Quantifier.Forall && !some_u_found) return false;
		}		
		return true;
	}
	
	// Helper funktion to determine the childs for RSZE
	private static List<ChildTuple> process_ChildTuple_RSZE(CospanCondition c1){
		List<ChildTuple> ct_c1 = new LinkedList<>();
		if(max_depth_of_Condition(c1) == 2 && min_depth_of_Condition(c1) == 2) {
			for (ChildTuple ct: c1.getChildren()) {
				if(ct.getChildCondition().getChildren().size() != 0) return null;
				ct_c1.add(ct);
			}
		}
		else {
			return null;
		}
		return ct_c1;
	}
	
	
	private static List<ChildTuple> compose_over_id(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		new_ct.add(ct);
		CospanCondition c1_ct = ct.getChildCondition();
		if(c1_ct.getChildren().size() != 1) return new_ct;
		Cospan identity = createIdentity(c1_ct.getOrigin());
		ChildTuple ct_c1 = c1_ct.getChildren().get(0);
		if(!ct_c1.getMorphism().equals(identity)) return new_ct;
		List<ChildTuple> lct_c2 = ct_c1.getChildCondition().getChildren();
		if(lct_c2.size() == 0) {
			changed = true;
			return new LinkedList<>();
		}
		new_ct.remove(0);
		for(ChildTuple ct_c2 : lct_c2) {
			new_ct.add(new ChildTuple(Cospan.concatenate(ct.getMorphism(), ct_c2.getMorphism()), ct_c2.getChildCondition()));
		}
		changed = true;
		return new_ct;
	}
	
	/**
	 * Determines the max depth of a condition
	 * @param c1
	 * @return
	 */
	public static int max_depth_of_Condition(CospanCondition c1) {
		if (c1.getChildren().size() == 0) return 1;
		int depth = 0;
		for(ChildTuple ct : c1.getChildren()) {
			int new_depth = max_depth_of_Condition(ct.getChildCondition());
			if(new_depth > depth) depth = new_depth;
		}			
		return 1+depth;
	}
	
	/**
	 * Determines the min depth of a condition
	 * @param c1
	 * @return
	 */
	public static int min_depth_of_Condition(CospanCondition c1) {
		if (c1.getChildren().size() == 0) return 1;
		int depth = 0;
		for(ChildTuple ct : c1.getChildren()) {
			int new_depth = min_depth_of_Condition(ct.getChildCondition());
			if(new_depth < depth || depth == 0) depth = new_depth;
		}			
		return 1+depth;
	}
	
	/**
	 * Removes all identities of the form ∀id.∃id.∀M ≡ ∀M , ∀id.∀M ≡ ∀M. Same for flipped Quantifier 
	 * @param c
	 * @return
	 */	
	private static CospanCondition remove_Identities_and_Iso(CospanCondition c) {
		// 0 Kinder fertig
		List<ChildTuple> childtuples = c.getChildren();
		if(childtuples.size() == 0) return c;
		// 1 Kind überprüfe c_m ID und m_c_q == c_q
		if(childtuples.size() == 1) {
			Cospan identity = createIdentity(c.getOrigin());
			ChildTuple ct = childtuples.get(0);
			CospanCondition c_c = ct.getChildCondition();
			if(ct.getMorphism().equals(identity)) {
			if(c.getQuantifier() == c_c.getQuantifier()) return remove_Identities_and_Iso(c_c);
			if(c_c.getChildren().size() != 1) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
			if(c_c.getChildren().get(0).getMorphism().equals(identity)) return remove_Identities_and_Iso(c_c.getChildren().get(0).getChildCondition());
			}
			if(ct.getMorphism().isIsomorphism() && c_c.getChildren().size() == 1 && c_c.getChildren().get(0).getMorphism().equals(identity)) {
				System.out.print("Iso");
				ChildTuple ct_c_c = c_c.getChildren().get(0);
				Graph left = c_c.getChildren().get(0).getMorphism().getLeftGraph();
				Morphism LtoO = Morphism.getIsomorphism(left,ct.getMorphism().getLeftGraph());
				System.out.print("Iso");
				if (LtoO == null) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
				System.out.print("Iso");
				Cospan new_co = new Cospan(LtoO.getInverse().compose(ct_c_c.getMorphism().getLeftMorphism()),ct_c_c.getMorphism().getRightMorphism());
				return new CospanCondition(ct.getMorphism().getLeftGraph(),c.getQuantifier(),new ChildTuple(new_co,remove_Identities_and_Iso(ct_c_c.getChildCondition())));
			}
			return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
		}
		// >1 Kinder überprüfe für jedes Kind ob ID notwendig ist
		if(childtuples.size() > 1) {
			List<ChildTuple> new_ct = new LinkedList<>();
			for(ChildTuple ct : childtuples) {
				Cospan identity = createIdentity(c.getOrigin());
				CospanCondition c_c = ct.getChildCondition();
				if(ct.getMorphism().equals(identity)) {
					if(c.getQuantifier() == c_c.getQuantifier()) {
						c_c = remove_Identities_and_Iso(c_c);
						new_ct.addAll(c_c.getChildren());
					}
					else if(c_c.getChildren().size() == 1 && c_c.getChildren().get(0).getMorphism().equals(identity)) {
						c_c = remove_Identities_and_Iso(c_c.getChildren().get(0).getChildCondition());
						new_ct.addAll(c_c.getChildren());
					}		
					else {
						new_ct.add(new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
					}
				}
				else if(ct.getMorphism().isIsomorphism() &&c_c.getChildren().size() == 1 && c_c.getChildren().get(0).getMorphism().equals(identity)) {
					System.out.print("Iso");
					ChildTuple ct_c_c = c_c.getChildren().get(0);
					Graph left = c_c.getChildren().get(0).getMorphism().getLeftGraph();
					Morphism LtoO = Morphism.getIsomorphism(left,ct.getMorphism().getLeftGraph());
					System.out.print("Iso");
					if (LtoO != null) {
						System.out.print("Iso");
						Cospan new_co = new Cospan(LtoO.getInverse().compose(ct_c_c.getMorphism().getLeftMorphism()),ct_c_c.getMorphism().getRightMorphism());
						new_ct.add(new ChildTuple(new_co,remove_Identities_and_Iso(ct_c_c.getChildCondition())));
					}
					else {
						new_ct.add(new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
					}				
				}
				else {
					new_ct.add(new ChildTuple(ct.getMorphism(), remove_Identities_and_Iso(c_c)));
				}
			}
			return new CospanCondition(c.getOrigin(),c.getQuantifier(),new_ct);
		}		
		return null;
	}
	
	
	// from CospanCondition -and CospanHelper to avoid loops
	
	private static CospanCondition makeAlternating(CospanCondition condition) {
		List<ChildTuple> newChildren = new ArrayList<>();
		for(ChildTuple child : condition.getChildren())
		{
			if(condition.getQuantifier() == child.getChildCondition().getQuantifier())
			{
				ChildTuple replacementChild = new ChildTuple(child.getMorphism(),createIdentityLayer(child.getChildCondition()));
				newChildren.add(replacementChild);
			}else
			{
				ChildTuple newChild = new ChildTuple(child.getMorphism(), makeAlternating(child.getChildCondition()));
				newChildren.add(newChild);
			}
		}
		return new CospanCondition(condition.getOrigin(), condition.getQuantifier(), newChildren);
	}
	
	private static CospanCondition createIdentityLayer(CospanCondition childCondition)
	{
		Quantifier identityLayerQuantifier = childCondition.getQuantifier().other();
		Cospan identity = createIdentity(childCondition.getOrigin());
		ChildTuple identityLayerChildTuple = new ChildTuple(identity, makeAlternating(childCondition));
		return new CospanCondition(childCondition.getOrigin(), identityLayerQuantifier, identityLayerChildTuple);
	}
	
	private static Cospan createIdentity(Graph graph) {
		return new Cospan(Morphism.createIdentity(graph),Morphism.createIdentity(graph));
	}
	

}