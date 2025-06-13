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
 **/
public class Optimiser {
	
	public static boolean Identity = true;
	public static boolean POCOL = true;
	public static boolean POCB = true;
	public static boolean RSZE = true;
	
	public static CospanCondition optimise(CospanCondition c) {
		c = makeAlternating(c);
		c = removeIdentities(c);
		c = going_Through_Condition(c);
		return c;
	}
	
	
	private static ChildTuple optimise_single_child(ChildTuple ct, Quantifier q) {
		return new ChildTuple(ct.getMorphism(), going_Through_Condition(ct.getChildCondition()));
	}
	
	private static List<ChildTuple> optimise_multi_child(List<ChildTuple> ct_list){
		List<Integer> removed = new LinkedList<>();
		List<CospanCondition> new_C = new LinkedList<>();
		List<ChildTuple> new_CT = new LinkedList<>();
		for(int i = 0; i < ct_list.size();i++) {
			for(int j=0; j < ct_list.size();j++) {
				if(removed.contains(i) || i==j) continue;
				ChildTuple ct_c1 = ct_list.get(i);
				ChildTuple ct_c2 = ct_list.get(j);			
				Cospan l = check_subgraph(ct_c1.getMorphism(), ct_c2.getMorphism());
				if (l == null) continue;
				if(RSZE && RSZE(ct_c1.getChildCondition(),ct_c2.getChildCondition(),l)) {					
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
						new_C.add(POCB_OP);
						removed.add(i);
						removed.add(j);
					}
					
				}
			}
		}
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
		if(childtuples.size() == 1) return new CospanCondition(condition.getOrigin(),condition.getQuantifier(),optimise_single_child(childtuples.getFirst(),condition.getQuantifier()));
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
		if (m2 == null) return null;
		return new Cospan(s1.getTopRight(), m2);
	}		

	/**
	 * 
	 * @param c1, is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2, is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean POCOL(CospanCondition c1, CospanCondition c2) {
		if(max_depth_of_Condition(c1)!=1 || max_depth_of_Condition(c2)!=1 || c1.getChildren().size() != 0 || c2.getChildren().size() != 0) return false;
		if(c1.getQuantifier() != c2.getQuantifier()) return false;
		return true;
	}
	
	/**
	 * 
	 * @param c1, is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2 
	 * @param c2, is the ChildCondition of the ChildTuple(m2,c2) where m2;m3 = m1
	 * @return returns Condition c1 with all childs of c2 additional added
	 */
	private static CospanCondition POCB(CospanCondition c1, CospanCondition c2) {
		List<ChildTuple> new_ct = new LinkedList<>();
		for(ChildTuple ct: c1.getChildren()) new_ct.add(ct);
		for(ChildTuple ct: c2.getChildren()) new_ct.add(ct);		
		return new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct);
	}
	
	/**
	 * 
	 * @param c1, is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2, is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2 
	 * @param l, is the Cospan m3 from m1;m3 = m2
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean RSZE(CospanCondition c1, CospanCondition c2, Cospan l) {
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
	private static CospanCondition removeIdentities(CospanCondition c) {
		// 0 Kinder fertig
		List<ChildTuple> childtuples = c.getChildren();
		if(childtuples.size() == 0) return c;
		// 1 Kind überprüfe c_m ID und m_c_q == c_q
		if(childtuples.size() == 1) {
			Cospan identity = createIdentity(c.getOrigin());
			ChildTuple ct = childtuples.get(0);
			CospanCondition c_c = ct.getChildCondition();
			if(!ct.getMorphism().equals(identity)) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), removeIdentities(c_c)));
			if(c.getQuantifier() == c_c.getQuantifier()) return removeIdentities(c_c);
			if(c_c.getChildren().size() != 1) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), removeIdentities(c_c)));
			if(c_c.getChildren().get(0).getMorphism().equals(identity)) return removeIdentities(c_c.getChildren().get(0).getChildCondition());
			return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), removeIdentities(c_c)));
		}
		// >1 Kinder überprüfe für jedes Kind ob ID notwendig ist
		if(childtuples.size() > 1) {
			List<ChildTuple> new_ct = new LinkedList<>();
			for(ChildTuple ct : childtuples) {
				Cospan identity = createIdentity(c.getOrigin());
				CospanCondition c_c = ct.getChildCondition();
				if(ct.getMorphism().equals(identity)) {
					if(c.getQuantifier() == c_c.getQuantifier()) {
						c_c = removeIdentities(c_c);
						new_ct.addAll(c_c.getChildren());
					}
					else if(c_c.getChildren().size() == 1 && c_c.getChildren().get(0).getMorphism().equals(identity)) {
						c_c = removeIdentities(c_c.getChildren().get(0).getChildCondition());
						new_ct.addAll(c_c.getChildren());
					}
					else {
						new_ct.add(new ChildTuple(ct.getMorphism(), removeIdentities(c_c)));
					}
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