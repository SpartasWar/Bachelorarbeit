package de.uni_due.inf.ti.graph.Optimiser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ListResourceBundle;

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
	public static boolean RSTL = false;
	public static boolean RSML = true;
	public static int max_depth_for_RSML = 3;
	public static boolean compose_over_id = true;
	public static boolean remove_iso = true;
	
	public static CospanCondition optimise(CospanCondition c) {
		//System.out.print("Before: \n" + c + "\n");
		changed = true;
		while(changed) {
			changed = false;
			//System.out.print("Start Round: \n" + c +"\n");
			c = makeAlternating(c);
			//System.out.print("Alternating: \n" + c + "\n");
			c = remove_first_Id(c);
			//System.out.print("Without First id: \n" + c + "\n");
			c = remove_Identities(c);
			//System.out.print("Without id: \n" + c + "\n");
			c = going_Through_Condition(c);
			//System.out.print("Intern After opti: \n" + c + "\n");
		}
		//System.out.print("After: \n" + c + "\n");
		return c;
	}
	
	
	private static List<ChildTuple> optimise_single_child(List<ChildTuple> ct) {
		List<ChildTuple> new_ct = new LinkedList<>();
		//System.out.print("Single Child before: \n" + ct + "\n");
		ct = pre_process_CT_List(ct);
		//System.out.print("Single Child After: \n" + ct + "\n");
		for(ChildTuple ct1: ct) {
			new_ct.add(new ChildTuple(ct1.getMorphism(), going_Through_Condition(ct1.getChildCondition())));
		}	
		return new_ct;
	}
	
	private static List<ChildTuple> optimise_multi_child(List<ChildTuple> ct_list){
		List<Integer> removed = new LinkedList<>();
		List<ChildTuple> new_CT = new LinkedList<>();
		ct_list = pre_process_CT_List(ct_list);
		//System.out.print("CTList \n"+ct_list+"\n");
		for(int i = 0; i < ct_list.size();i++) {
			for(int j=0; j < ct_list.size();j++) {
				if(removed.contains(i) || i==j) continue;
				ChildTuple ct_c1 = ct_list.get(i);
				ChildTuple ct_c2 = ct_list.get(j);			
				Cospan l = check_subgraph(ct_c1.getMorphism(), ct_c2.getMorphism());
				if (l == null) continue;
				if(RSML && RSML(ct_c1.getChildCondition(),ct_c2.getChildCondition(),l,0)) {
					removed.add(j);
					//System.out.print("C1\n"+ct_c1+"\n C2\n"+ct_c2+"\n");
					//System.out.print("RSML\n");
				}
				if(RSTL && RSTL(ct_c1.getChildCondition(),ct_c2.getChildCondition(),l)) {
					removed.add(j);
					//System.out.print("RSTL\n");
				}
				if(POCOL && POCOL(ct_c1.getChildCondition(), ct_c2.getChildCondition())) {					
					if(ct_c1.getChildCondition().getQuantifier() == Quantifier.Exists) {
						removed.add(j);
					}
					else {
						//removed.add(i);
						removed.add(j);
					}	
					//System.out.print("POCOL\n");
				}
				if(POCB) {
					if(!removed.contains(j) && check_subgraph(ct_c2.getMorphism(), ct_c1.getMorphism()) != null) {
						CospanCondition POCB_OP = POCB(ct_c1.getChildCondition(),ct_c2.getChildCondition());
						new_CT.add(new ChildTuple(ct_c1.getMorphism(), going_Through_Condition(POCB_OP)));
						removed.add(i);
						removed.add(j);
						//System.out.print("POCB\n");
					}
					
				}
			}
		}
		if(removed.size() != 0) {
			//System.out.print("Changed: 1\n");
			changed = true;
		}
		for(int i=0; i < ct_list.size();i++) {
			if(removed.contains(i)) continue;
			new_CT.add(new ChildTuple(ct_list.get(i).getMorphism(), going_Through_Condition(ct_list.get(i).getChildCondition())));
		}
		//System.out.print("NEW CTList \n"+new_CT+"\n");
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
		//System.out.print("After id remove: \n" + ct_list + "\n");
		if(compose_over_id) {
			List<ChildTuple> processed_ct_List = new LinkedList<>();
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(compose_over_id(ct));
			}
			ct_list = processed_ct_List;
		}
		//System.out.print("After id compose: \n" + ct_list + "\n");
		if(remove_iso) {
			List<ChildTuple> processed_ct_List = new LinkedList<>();
			for(ChildTuple ct : ct_list) {
				processed_ct_List.addAll(remove_Iso(ct));
			}
			ct_list = processed_ct_List;
		}
		//System.out.print("After remove Iso: \n" + ct_list + "\n");
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
		List<ChildTuple> ct_c1 = process_ChildTuple_RSTL(c1);
		List<ChildTuple> ct_c2 = process_ChildTuple_RSTL(c2);
		if(ct_c1 == null || ct_c2 == null) return false;		
		// alle childs von c2 müssen mit einem erreicht werden
		List<Integer> used_c2_ct = new LinkedList<>();
		List<BCDEnumerator> list_bcde = new LinkedList<BCDEnumerator>();
		for(ChildTuple ct : c1.getChildren()) {		
			 list_bcde.add(new BCDEnumerator(l,ct.getMorphism()));	
		}	
		for(BCDEnumerator bcde : list_bcde) {
			for(BorrowedContextDiagram bcd: bcde) {
				for(int i=0 ; i < ct_c2.size();i++) {
					if(used_c2_ct.contains(i)) continue;
					if(check_subgraph(ct_c2.get(i).getMorphism(), bcd.getRight()) == null) continue;
					used_c2_ct.add(i);
				}
				if(used_c2_ct.size() == ct_c2.size()) return true;
			}
		}
		return false;
	}
	
	
	// Helper funktion to determine the childs for RSTL
	private static List<ChildTuple> process_ChildTuple_RSTL(CospanCondition c1){
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
	 * Repräsentaive Squares Multi Levels
	 * 
	 * @param c1 is the ChildCondition of the ChildTuple(m1,c1) where m1;m3 = m2
	 * @param c2 is the ChildCondition of the ChildTuple(m2,c2) where m1;m3 = m2 
	 * @param l is the Cospan m3 from m1;m3 = m2
	 * @param depth is the current Level. Used to determine how many layers are searched 
	 * @return returns true if the optimization can be applied otherwise false
	 */
	private static boolean RSML(CospanCondition c1, CospanCondition c2, Cospan l, int depth) {
		if(depth > max_depth_for_RSML) return false;
		if(max_depth_of_Condition(c1) != max_depth_of_Condition(c2) || min_depth_of_Condition(c1) != min_depth_of_Condition(c2) || max_depth_of_Condition(c1) != min_depth_of_Condition(c2)) return false;
		if(max_depth_of_Condition(c1) == 2 && min_depth_of_Condition(c1) == 2 && max_depth_of_Condition(c2) == 2 && min_depth_of_Condition(c2) == 2) return RSTL(c1,c2,l);
		List<ChildTuple> ct_c1 = c1.getChildren();
		List<ChildTuple> ct_c2 = c2.getChildren();
		if(ct_c1.size() == 0 || ct_c2.size() == 0) return false;
		for(ChildTuple ct2: ct_c2) {
			if(ct2.getChildCondition().getChildren().size() == 0) return false;
			boolean next_l_found = false;
			for(ChildTuple ct1 : ct_c1) {
				Cospan next_l = check_subgraph(ct1.getMorphism(),ct2.getMorphism());
				if(next_l == null) continue;
				next_l_found = RSML(ct1.getChildCondition(),ct2.getChildCondition(),next_l,depth+1);
				if(next_l_found) break;
			}
			if(!next_l_found) return false;
		}			
		return true;
	}
	
	
	private static List<ChildTuple> remove_id(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		new_ct.add(ct);
		CospanCondition c1_ct = ct.getChildCondition();
		Cospan identity = createIdentity(c1_ct.getOrigin());
		if(!ct.getMorphism().equals(identity)) return new_ct;
		if(c1_ct.getChildren().isEmpty()) return new_ct;
		new_ct.remove(0);
		List<ChildTuple> new_ct_c1 = new LinkedList<>();
		for(ChildTuple ct_c1: c1_ct.getChildren()) {
			if(ct_c1.getMorphism().equals(identity)) {
				if(!ct_c1.getChildCondition().getChildren().isEmpty()) {
					new_ct.addAll(ct_c1.getChildCondition().getChildren());
				}
				System.out.print("Changed: 2\n");
				changed = true;
				continue;
			}
			new_ct_c1.add(ct_c1);
		}
		if(!new_ct_c1.isEmpty()) {
			new_ct.add(new ChildTuple(ct.getMorphism(), new CospanCondition(c1_ct.getOrigin(),c1_ct.getQuantifier(),new_ct_c1)));
		}		
		return new_ct;
	}
	
	
	/**
	 * Uses the rules:
	 * forall g exists id forall => forall
	 * forall g exists id forall f GC => forall g;f GC
	 * exists g forall id exists => exists
	 * exists g forall id exists f GC => exists g;f GC
	 * 
	 * @param ct
	 * @return
	 */
	
	private static List<ChildTuple> compose_over_id(ChildTuple ct){
		//System.out.print("Intern compose old:\n"+ ct+"\n");
		List<ChildTuple> new_ct = new LinkedList<>();
		new_ct.add(ct);
		CospanCondition c1_ct = ct.getChildCondition();
		Cospan identity = createIdentity(c1_ct.getOrigin());
		List<ChildTuple> new_ct_c1 = new LinkedList<>();
		if(c1_ct.getChildren().isEmpty()) return new_ct;
		new_ct.remove(0);
		for(ChildTuple ct_c1: c1_ct.getChildren()) {
			if(ct_c1.getMorphism().equals(identity)) {
				for(ChildTuple ct_c2:ct_c1.getChildCondition().getChildren()) {
					new_ct.add(new ChildTuple(Cospan.concatenate(ct.getMorphism(),ct_c2.getMorphism() ), ct_c2.getChildCondition()));			
					//hier noch weiter machen
				}
				//System.out.print("Changed: 3\n");
				changed = true;
			}
			else {
				new_ct_c1.add(ct_c1);
			}
		}
		if(!new_ct_c1.isEmpty()) {
			new_ct.add(new ChildTuple(ct.getMorphism(), new CospanCondition(c1_ct.getOrigin(),c1_ct.getQuantifier(),new_ct_c1)));
		}
		//System.out.print("Intern compose new:\n"+ new_ct+"\n");
		return new_ct;
	}
	
	/**
	 * Uses the rules:
	 * forall iso exists id/iso forall g GC => forall g´ GC
	 * forall iso exists id/iso forall => forall
	 * exists iso forall id/iso exists g GC => exists g´ GC
	 * exists iso forall id/iso exists => exists
	 * 
	 * in case of:
	 * exists iso (forall id/iso exists g GC and forall f exists h GC) => exists g´ GC or exists iso forall f exists h GC
	 * forall iso (exists id/iso forall g GC and exists f forall h GC) => forall g´ GC or forall iso exists f forall h GC
	 *
	 * @param ct
	 * @return
	 */
	
	private static List<ChildTuple> remove_Iso(ChildTuple ct){
		List<ChildTuple> new_ct = new LinkedList<>();
		new_ct.add(ct);
		if(!ct.getMorphism().isIsomorphism()) return new_ct;
		CospanCondition c1_ct = ct.getChildCondition();
		Cospan identity = createIdentity(ct.getMorphism().getRightGraph());
		if(c1_ct.getChildren().isEmpty()) return new_ct;
		//System.out.print("Before iso:\n"+new_ct+"\n Amount: \n" + new_ct.size()+"\n");
		new_ct.remove(0);
		List<ChildTuple> new_ct_c = new LinkedList<>();
		for(ChildTuple ct_c1: ct.getChildCondition().getChildren()) {
			List<ChildTuple> new_ct_c1 = new LinkedList<>();
			CospanCondition c2 = ct_c1.getChildCondition();
			if(ct_c1.getMorphism().isIsomorphism() || ct_c1.getMorphism().equals(identity)) {
				if(c2.getChildren().isEmpty()) new_ct_c.add(ct_c1);
				for(ChildTuple ct_c2: c2.getChildren()){
					Cospan new_co = reroot(ct_c2, ct.getMorphism().getLeftGraph());
					if(new_co != null) {
						new_ct.add(new ChildTuple(new_co, ct_c2.getChildCondition()));
						changed = true;
						//System.out.print("Changed: 5\n");
					}
					else {
						new_ct_c1.add(ct_c2);
					}
				}
				if(!new_ct_c1.isEmpty()) {
					new_ct_c.add(new ChildTuple(ct_c1.getMorphism(), new CospanCondition(c2.getOrigin(),c2.getQuantifier() ,new_ct_c1)));
				}
			}
			else {
				new_ct_c.add(ct_c1);
			}
		}
		if(!new_ct_c.isEmpty()) {
			new_ct.add(new ChildTuple(ct.getMorphism(), new CospanCondition(c1_ct.getOrigin(),c1_ct.getQuantifier(),new_ct_c)));
		}
		//System.out.print("After iso:\n"+new_ct+"\n Amount: \n" + new_ct.size()+"\n");
		return new_ct;
	}
	
	private static CospanCondition remove_first_Id(CospanCondition c) {
		//System.out.print("Condition:\n"+ c +"\n");
		List<ChildTuple> ct_c = c.getChildren();
		if(ct_c.size() != 1) return c;
		Cospan identity = createIdentity(ct_c.get(0).getMorphism().getLeftGraph());
		if(ct_c.get(0).getMorphism().equals(identity)) {
			//System.out.print("Changed 7:\n" +ct_c.get(0).getChildCondition()+"");
			changed = true;
			return ct_c.get(0).getChildCondition();
		}
		if(ct_c.get(0).getMorphism().isIsomorphism()) {
			ChildTuple ct = ct_c.get(0);
			List<ChildTuple> new_ct = new LinkedList<>();
			for(ChildTuple ct_1: ct.getChildCondition().getChildren() ) {
				Cospan new_co = reroot(ct_1, ct.getMorphism().getLeftGraph());
				if(new_co != null) {
					new_ct.add(new ChildTuple(new_co, ct_1.getChildCondition()));
				}
				else {
					return c;
				}
			}
			//System.out.print("Changed 7:\n"+ new CospanCondition(c.getOrigin(),ct.getChildCondition().getQuantifier(),new_ct) +"\n");
			changed = true;
			return new CospanCondition(c.getOrigin(),ct.getChildCondition().getQuantifier(),new_ct);
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
	
	//TODO remove_Identities neu schreiben
	/**
	 * Removes all identities of the form ∀id.∃id.∀M ≡ ∀M , ∀id.∀M ≡ ∀M. Same for flipped Quantifier 
	 * @param c
	 * @return
	 */	
	private static CospanCondition remove_Identities(CospanCondition c) {
		// 0 Kinder fertig
		List<ChildTuple> childtuples = c.getChildren();
		if(childtuples.size() == 0) return c;
		// 1 Kind überprüfe c_m ID und m_c_q == c_q
		if(childtuples.size() == 1) {
			Cospan identity = createIdentity(c.getOrigin());
			ChildTuple ct = childtuples.get(0);
			CospanCondition c_c = ct.getChildCondition();
			if(!ct.getMorphism().equals(identity)) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities(c_c)));
			if(c.getQuantifier() == c_c.getQuantifier()) return remove_Identities(c_c);
			if(c_c.getChildren().size() != 1) return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities(c_c)));
			if(c_c.getChildren().get(0).getMorphism().equals(identity)) return remove_Identities(c_c.getChildren().get(0).getChildCondition());
			return new CospanCondition(c.getOrigin(),c.getQuantifier(),new ChildTuple(ct.getMorphism(), remove_Identities(c_c)));
		}
		// >1 Kinder überprüfe für jedes Kind ob ID notwendig ist
		if(childtuples.size() > 1) {
			List<ChildTuple> new_ct = new LinkedList<>();
			for(ChildTuple ct : childtuples) {
				Cospan identity = createIdentity(c.getOrigin());
				CospanCondition c_c = ct.getChildCondition();
				if(ct.getMorphism().equals(identity)) {
					if(c.getQuantifier() == c_c.getQuantifier()) {
						c_c = remove_Identities(c_c);
						new_ct.addAll(c_c.getChildren());
						continue;
					}
					else if(c_c.getChildren().size() == 1 && c_c.getChildren().get(0).getMorphism().equals(identity)) {
						c_c = remove_Identities(c_c.getChildren().get(0).getChildCondition());
						new_ct.addAll(c_c.getChildren());
						continue;
					}		
				}
				new_ct.add(new ChildTuple(ct.getMorphism(), remove_Identities(c_c)));			
			}
			return new CospanCondition(c.getOrigin(),c.getQuantifier(),new_ct);
		}		
		return null;
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