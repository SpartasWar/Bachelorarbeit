package de.uni_due.inf.ti.graph.Optimiser;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition.ChildTuple;
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
	public static boolean RSML = true;
	public static int max_depth_for_RSML = 3;
	public static boolean compose_over_id = true;
	public static boolean remove_iso = true;
	
	public static CospanCondition optimise(CospanCondition c) {
		c = makeAlternating2(c);
		changed = true;
		while(changed) {
			changed = false;
			c = remove_Identities(c); // Kann man auch weglassen
			c = going_Through_Condition(c);
			c = remove_first_Id(c);
		} 
		return c;
	}
	
	
	private static List<ChildTuple> optimise_single_child(List<ChildTuple> ct) {
		List<ChildTuple> new_ct = new LinkedList<>();
		ct = pre_process_CT_List(ct);
		for(ChildTuple ct1: ct) {
			new_ct.add(new ChildTuple(ct1.getMorphism(), going_Through_Condition(ct1.getChildCondition())));
		}	
		return new_ct;
	}
	
	private static List<ChildTuple> optimise_multi_child(List<ChildTuple> ct_list){
		List<Integer> removed = new LinkedList<>();
		List<ChildTuple> new_CT = new LinkedList<>();
		ct_list = pre_process_CT_List(ct_list);
		for(int i = 0; i < ct_list.size();i++) {
			if(removed.contains(i)) continue;
			for(int j=0; j < ct_list.size();j++) {
				if(i==j) continue;
				ChildTuple ct_c1 = ct_list.get(i);
				ChildTuple ct_c2 = ct_list.get(j);			
				Cospan l = check_subgraph(ct_c1.getMorphism(), ct_c2.getMorphism());
				if (l == null) continue;
				if(RSML && RSML(ct_c1.getChildCondition(),ct_c2.getChildCondition(),l,0)) {
					removed.add(j);
				}				
				else if(POCOL && POCOL(ct_c1.getChildCondition(), ct_c2.getChildCondition())) {	
					removed.add(j);
				}
				else if(POCB) {
					if(!removed.contains(j) && check_subgraph(ct_c2.getMorphism(), ct_c1.getMorphism()) != null) {
						CospanCondition POCB_OP = POCB(ct_c1.getChildCondition(),ct_c2.getChildCondition());
						new_CT.add(new ChildTuple(ct_c1.getMorphism(), going_Through_Condition(POCB_OP)));
						removed.add(i);
						removed.add(j);						
					}				
				}
			}
		}
		if(removed.size() != 0) {
			changed = true;
		}
		for(int i=0; i < ct_list.size();i++) {
			if(removed.contains(i)) continue;
			new_CT.add(new ChildTuple(ct_list.get(i).getMorphism(), going_Through_Condition(ct_list.get(i).getChildCondition())));
		}
		return new_CT;
	}
	
	
	// Pre-Processes the ChildTuple List if any of the optimizations are activated
	private static List<ChildTuple> pre_process_CT_List(List<ChildTuple> ct_list){
		if(true) {
			List<ChildTuple> processed_ct_List = new LinkedList<>();
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(remove_id(ct));
			}
			ct_list = processed_ct_List;
		}
		if(compose_over_id) {
			List<ChildTuple> processed_ct_List = new LinkedList<>();
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(compose_over_id(ct));
			}
			ct_list = processed_ct_List;
		}
		if(remove_iso) {
			List<ChildTuple> processed_ct_List = new LinkedList<>();
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(remove_Iso(ct));
			}
			ct_list = processed_ct_List;
		}
		return ct_list;
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
		return c1.getCompletions(c2, true).next();
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
		for(ChildTuple ct: c1.getChildren()) new_ct.add(ct);
		for(ChildTuple ct: c2.getChildren()) new_ct.add(new ChildTuple(reroot(ct,c1.getOrigin()), ct.getChildCondition()));
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
		List<ChildTuple> list_ct_c1 = c1.getChildren();
		List<ChildTuple> list_ct_c2 = c2.getChildren();
		List<BCDEnumerator> list_bcde = new LinkedList<BCDEnumerator>();
		for(ChildTuple ct_c1 : list_ct_c1) {		
			 list_bcde.add(new BCDEnumerator(l,ct_c1.getMorphism()));	
		}
		List<Integer> used_c2_ct = new LinkedList<>();
		for(BCDEnumerator bcde : list_bcde) {
			for(BorrowedContextDiagram bcd: bcde) {
				for(int i = 0; i < list_ct_c2.size();i++) {
					if(used_c2_ct.contains(i) || check_subgraph(list_ct_c2.get(i).getMorphism(), bcd.getRight()) == null) continue;
					used_c2_ct.add(i);
				}
				if(used_c2_ct.size() == list_ct_c2.size()) return true;
			}
		}			
		return false;
	}
	
	/**
	 * Repräsentaive Squares Multi Levels
	 * 
	 * @param c1 is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2 is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2 
	 * @param l is the Cospan m3 from m1;m3 = m2
	 * @param depth is the current Level. Used to determine how many layers are searched 
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean RSML(CospanCondition c1, CospanCondition c2, Cospan l, int depth) {
		if(depth > max_depth_for_RSML || max_depth_of_Condition(c1)-2 > max_depth_for_RSML || max_depth_of_Condition(c2)-2 > max_depth_for_RSML) return false;
		if(max_depth_of_Condition(c1) != min_depth_of_Condition(c2) || min_depth_of_Condition(c1) != max_depth_of_Condition(c2) || max_depth_of_Condition(c1) != max_depth_of_Condition(c2)) return false;
		if(max_depth_of_Condition(c1) == 2) return RSTL(c1,c2,l);
		List<ChildTuple> list_ct_c1 = c1.getChildren();
		List<ChildTuple> list_ct_c2 = c2.getChildren();
		List<Integer> used_c2_ct = new LinkedList<>();
		for(ChildTuple ct_c1 : list_ct_c1) {
			for(BorrowedContextDiagram bcd: new BCDEnumerator(l,ct_c1.getMorphism())) {
				for(int i= 0; i < list_ct_c2.size();i++) {
					if(used_c2_ct.contains(i) || check_subgraph(list_ct_c2.get(i).getMorphism(), bcd.getRight()) == null) continue;		
					Cospan next_l = check_subgraph(Cospan.concatenate(l, list_ct_c2.get(i).getMorphism()),ct_c1.getMorphism());
					if(next_l != null && RSML(ct_c1.getChildCondition(),list_ct_c2.get(i).getChildCondition(),next_l,depth+1)) used_c2_ct.add(i);
				}
				if(used_c2_ct.size() == list_ct_c2.size()) return true;
			}
		
		}
		return false;
	}

	
	/**
	 * Uses the rules:
	 * ∀id.∃id.∀M ≡ ∀M
	 * ∃id.∀id.∃M ≡ ∃M
	 * 
	 * @param ct
	 * @return
	 */
	
	private static List<ChildTuple> remove_id(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		CospanCondition c1 = ct.getChildCondition();
		Cospan id = createIdentity(c1.getOrigin());
		if(!ct.getMorphism().equals(id) ||c1.getChildren().size() != 1) return Arrays.asList(new ChildTuple[] {ct});
		ChildTuple ct_c1 = c1.getChildren().get(0);
		if(ct_c1.getMorphism().equals(id)) {
			for(ChildTuple ct_c2: ct_c1.getChildCondition().getChildren()) {
				new_ct.add(ct_c2);
			}
			changed = true;
			return new_ct;
		}		
		return Arrays.asList(new ChildTuple[] {ct});
	}
	
	
	/**
	 * Uses the rules:
	 * ∀g.∃id.∀ ≡ ∀
	 * ∀g.∃id.∀f.GC ≡ ∀g;f.GC
	 * 
	 * ∃g.∀id.∃ ≡ ∃
	 * ∃g.∀id.∃f.GC ≡ ∃g;f.GC
	 * 
	 * @param ct
	 * @return
	 */
	private static List<ChildTuple> compose_over_id(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		CospanCondition c1 = ct.getChildCondition();
		Cospan id = createIdentity(c1.getOrigin());
		if(c1.getChildren().size() != 1) return Arrays.asList(new ChildTuple[] {ct});
		ChildTuple ct_c1 = c1.getChildren().get(0);
		if(ct_c1.getMorphism().equals(id)) {
			for(ChildTuple ct_c2 : ct_c1.getChildCondition().getChildren()) {
				new_ct.add(new ChildTuple(Cospan.concatenate(ct.getMorphism(), ct_c2.getMorphism()), ct_c2.getChildCondition()));
			}
			//System.out.print("Compose over ID \n");
			changed = true;
			return new_ct;
		}		
		return Arrays.asList(new ChildTuple[] {ct});
	}
	
	
	/**
	 * Uses the rules:
	 * ∀iso.∃id/iso.∀g.GC ≡ ∀g´.GC
	 * ∀iso.∃id/iso.∀ ≡ ∀
	 * ∃iso.∀id/iso.∃g.GC ≡ ∃g´.GC
	 * ∃iso.∀id/iso.∃ ≡ ∃
	 * 
	 * @param ct
	 * @return
	 */
	private static List<ChildTuple> remove_Iso(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		CospanCondition c1 = ct.getChildCondition();
		Cospan id = createIdentity(c1.getOrigin());
		if(!ct.getMorphism().isIsomorphism() || c1.getChildren().size() != 1) return Arrays.asList(new ChildTuple[] {ct});
		ChildTuple ct_c1 = c1.getChildren().get(0);
		if(ct_c1.getMorphism().isIsomorphism() || ct_c1.getMorphism().equals(id)) {
			for(ChildTuple ct_c2: ct_c1.getChildCondition().getChildren()) {
				new_ct.add(new ChildTuple(reroot(ct_c2, ct.getMorphism().getLeftGraph()), ct_c2.getChildCondition()));
			}
			changed = true;
			return new_ct;
		}		
		return Arrays.asList(new ChildTuple[] {ct});
	}
	
	
	/**
	 * Only used on the top of a Condition
	 * 
	 * ∀id.GC ≡ GC
	 * ∀iso.GC ≡ GC´
	 * ∃id.GC ≡ GC
	 * ∃iso.GC ≡ GC´ 
	 * 
	 * @param c
	 * @return
	 */

	private static CospanCondition remove_first_Id(CospanCondition c) {
		Cospan id = createIdentity(c.getOrigin());
		if(c.getChildren().size() != 1) return c;
		ChildTuple ct = c.getChildren().get(0);
		if(ct.getMorphism().equals(id)) {
			changed = true;
			return ct.getChildCondition();
		}
		else if(ct.getMorphism().isIsomorphism()) {
			List<ChildTuple> new_ct = new LinkedList<>();
			for(ChildTuple ct_c1 : ct.getChildCondition().getChildren()) {
				new_ct.add(new ChildTuple(reroot(ct_c1, c.getOrigin()), ct_c1.getChildCondition()));
			}
			changed = true;
			return new CospanCondition(c.getOrigin(),c.getQuantifier().other(),new_ct);
		}				
		return c;
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
	private static CospanCondition remove_Identities(CospanCondition c) {
		List<ChildTuple> list_ct = pre_process_CT_List(c.getChildren());
		List<ChildTuple> new_ct = new LinkedList<>();
		for(ChildTuple ct : list_ct) {
			new_ct.add(new ChildTuple(ct.getMorphism(), remove_Identities(ct.getChildCondition())));
		}
		return new CospanCondition(c.getOrigin(),c.getQuantifier(),new_ct);
	}
	
	
	//TODO Reroot Methode schreiben häufiger genutzt als gedacht
	
	private static Cospan reroot(ChildTuple ct, Graph origin) {
		Graph left = ct.getMorphism().getLeftGraph();
		Morphism LtoO = Morphism.getIsomorphism(left, origin);
		if(LtoO != null) {
			return new Cospan(LtoO.getInverse().compose(ct.getMorphism().getLeftMorphism()),ct.getMorphism().getRightMorphism());
		}
		return null;
		
	}
	
	
	// Settings for the Optimiser
	
	public static void set_POCOL(boolean b) {
		POCOL = b;
	}
	
	public static void set_POCB(boolean b) {
		POCB = b;
	}
	
	public static void set_RSML(boolean b,int depth) {
		RSML = b;
		max_depth_for_RSML = depth;
	}
	
	public static void set_compose_over_id(boolean b) {
		compose_over_id = b;
	}
	
	public static void set_remove_iso(boolean b) {
		remove_iso = b;
	}
		
	
	private static CospanCondition makeAlternating(CospanCondition c1) {

		List<ChildTuple> new_ct = new LinkedList<>();
		boolean compose = false;
		for(ChildTuple ct_c1 : c1.getChildren()) {
			if(c1.getQuantifier() == ct_c1.getChildCondition().getQuantifier()) {
				for(ChildTuple ct_c2 : ct_c1.getChildCondition().getChildren()) {
					new_ct.add(new ChildTuple(Cospan.concatenate(ct_c1.getMorphism(), ct_c2.getMorphism()), ct_c2.getChildCondition()));
				}
				compose = true;
			}
			else {
				new_ct.add(new ChildTuple(ct_c1.getMorphism(), makeAlternating(ct_c1.getChildCondition())));
			}
		}
		if(compose) return makeAlternating(new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct));
		return new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct);
	}
	
	private static CospanCondition makeAlternating2(CospanCondition c1) {
		List<ChildTuple> new_c1 = new LinkedList<>();
		for(ChildTuple ct_c1 : c1.getChildren()) {
			new_c1.add(new ChildTuple(ct_c1.getMorphism(), makeAlternating2(ct_c1.getChildCondition())));
		}
		List<ChildTuple> new_ct = new LinkedList<>();		
		for(ChildTuple ct_c1 : new_c1) {
			if(c1.getQuantifier() == ct_c1.getChildCondition().getQuantifier()) {
				for(ChildTuple ct_c2 : ct_c1.getChildCondition().getChildren()) {
					new_ct.add(new ChildTuple(Cospan.concatenate(ct_c1.getMorphism(), ct_c2.getMorphism()), ct_c2.getChildCondition()));
				}
			}
			else {
				new_ct.add(new ChildTuple(ct_c1.getMorphism(),ct_c1.getChildCondition()));
			}
		}
		return new CospanCondition(c1.getOrigin(),c1.getQuantifier(),new_ct);
	}
	
	
	private static Cospan createIdentity(Graph graph) {
		return new Cospan(Morphism.getMatch(graph, graph, true),Morphism.getMatch(graph, graph, true));
	}
		
}