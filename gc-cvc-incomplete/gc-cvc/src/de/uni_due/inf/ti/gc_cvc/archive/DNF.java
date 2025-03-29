package de.uni_due.inf.ti.gc_cvc.archive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_due.inf.ti.graph.Morphism;
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

/**
 * This class implements the transformation to DNF, based on the following rules:
 * (the rules are due to Pennemann, modified from CNF conversion to DNF conversion)
 * <ol>
 * <li> ∃a ≡ ∃(a,true)
 * <li> ∀(a,c) ≡ ¬∃(a,¬c)
 * <li> ∃(a,c) ≡ false if a notin Monos
 * <li> ¬¬c ≡ c
 * <li> ¬true ≡ false
 * <li> ¬false ≡ true
 * <li> ∃(a, ∨ j∈J c_j ) ≡ ∨ j∈J ∃(a, c_j )
 * <li> ¬(∨ j∈J c_j ) ≡ (∧ j∈J ¬c_j )
 * <li> ¬(∧ j∈J c_j ) ≡ (∨ j∈J ¬c_j )
 * <li> ((∨ j∈J c_j ) ∧ c) ≡ (∨ j∈J (c_j ∧ c))
 * <li> ∃(id, c) ≡ c
 * <li> ∃(a, false) ≡ false
 * <li> ∃(a, ∃(b,c)) ≡ ∃(b ◦ a, c)
 * <li> ∨ j∈J c_j ≡ true         if ∃k ∈J. c_k = true
 * <li> ∨ j∈J c_j ≡ ∨j∈J\{k} c_j if ∃k ∈J. c_k =false
 * <li> ∧ j∈J c_j ≡ ∧j∈J\{k} c_j if ∃k ∈J. c_k =true
 * <li> ∧ j∈J c_j ≡ false        if ∃k ∈J. c_k = false
 * <li> ∨ j∈∅ c_j ≡ false
 * <li> ∧ j∈∅ c_j ≡ true
 * </ol>
 * @see "Pennemann: Development of Correct Graph Transformation Systems"
 */
public class DNF {
	/* Prevent instantiation */
	private DNF() {
	}

	/**
	 * Applies the rules of Pennemann once to {@code condition}.
	 *
	 * <p>The rules apply just to the condition itself, not to any child conditions.
	 * (For instance, ∃(a,¬¬c) is not rewritten to ∃(a,c) by this method.)
	 *
	 * <p>If none of the rules were applicable, a condition that is {@link #equals(Object)} to the
	 * original condition is returned. There is no guarantee that an identical ({@code ==}) condition is returned.
	 */
	private static GraphCondition applyPennemannRulesOnce(GraphCondition condition) {
		/* Not all of the rules need to be tested. This is because of our implementation.
		 * For instance,
		 * 1. ∃a ≡ ∃(a,true)
		 * never applies because there is no explicit class for an ∃a expression (it must always be simulated in the code)
		 */

		if (condition instanceof Negation) {
			GraphCondition inner = ((Negation) condition).getChildCondition();
			if (inner instanceof Negation) {
				// 4. ¬¬c ≡ c
				return ((Negation) inner).getChildCondition();
			} else if (inner instanceof Constant) {
				// 5. ¬true ≡ false
				// 6. ¬false ≡ true
				return ((Constant) inner).newOpposite(inner.getOrigin());
			} else if (inner instanceof NAryConnective) {
				// 8. ¬(∨ j∈J c_j ) ≡ (∧ j∈J ¬c_j )
				// 9. ¬(∧ j∈J c_j ) ≡ (∨ j∈J ¬c_j )
				NAryConnective junction = (NAryConnective) inner;
				Stream<GraphCondition> newStream = junction.getChildConditions().stream().map(Negation::new);
				return junction.newOppositeType(junction.getOrigin(), newStream);
			}
		} else if (condition instanceof ForAll) {
			// 2. ∀(a,c) ≡ ¬∃(a,¬c)
			ForAll forall = (ForAll) condition;
			return new Negation(new Existential(forall.getMorphism(), new Negation(forall.getChildCondition())));
		} else if (condition instanceof Existential) {
			Existential exist = (Existential) condition;
			GraphCondition child = exist.getChildCondition();
			if (! exist.getMorphism().isInjective()) {
				// 3. ∃(a,c) ≡ false if a notin Monos
				return new False(exist.getOrigin());
			} else if (child instanceof Disjunction) {
				// 7. ∃(a, ∨ j∈J c_j ) ≡ ∨ j∈J ∃(a, c_j )
				Stream<GraphCondition> disjChildren = ((Disjunction) child).getChildConditions().stream();
				Stream<GraphCondition> newConds = disjChildren.map(cj -> new Existential(exist.getMorphism(), cj));
				return new Disjunction(exist.getOrigin(), newConds);
			} else if (isIdentityMorphism(exist.getMorphism())) {
				// 11. ∃(id, c) ≡ c
				return eliminateIsomorphism(exist.getMorphism(), child);
			} else if (child instanceof False) {
				// 12. ∃(a, false) ≡ false
				return new False(exist.getOrigin());
			} else if (child instanceof Existential) {
				// 13. ∃(a, ∃(b,c)) ≡ ∃(b ◦ a, c)
				Existential childEx = (Existential) child;
				Morphism comp = Morphism.createComposition(exist.getMorphism(), childEx.getMorphism());
				return new Existential(comp, childEx.getChildCondition());
			}
		} else if (condition instanceof Disjunction) {
			Disjunction disjunction = (Disjunction) condition;
			List<GraphCondition> newConditions = new ArrayList<>();
			for (GraphCondition cj : disjunction.getChildConditions()) {
				if (cj instanceof True) {
					// 14. ∨ j∈J c_j ≡ true if ∃k ∈J. c_k = true
					return cj;
				} else if (cj instanceof False) {
					// 15. ∨ j∈J c_j ≡ ∨j∈J\{k} c_j if ∃k ∈J. c_k =false
					// do not add to new list
				} else if (cj instanceof Disjunction) {
					// flatten nested disjunctions
					newConditions.addAll(((Disjunction) cj).getChildConditions());
				} else {
					newConditions.add(cj);
				}
			}

			switch (newConditions.size()) {
			case 0:
				// 18. ∨ j∈∅ c_j ≡ false
				return new False(disjunction.getOrigin());
			case 1:
				// resolve single-element disjunction
				return newConditions.get(0);
			default:
				return new Disjunction(disjunction.getOrigin(), newConditions);
			}
		} else if (condition instanceof Conjunction) {
			Conjunction conjunction = (Conjunction) condition;
			List<GraphCondition> newConditions = new ArrayList<>();
			for (GraphCondition cj : conjunction.getChildConditions()) {
				if (cj instanceof False) {
					// 17. ∧ j∈J c_j ≡ false if ∃k ∈J. c_k = false
					return cj;
				} else if (cj instanceof True) {
					// 16. ∧ j∈J c_j ≡ ∧j∈J\{k} c_j if ∃k ∈J. c_k =true
					// do not add to new list
				} else if (cj instanceof Conjunction) {
					// flatten nested conjunctions
					newConditions.addAll(((Conjunction) cj).getChildConditions());
				} else {
					newConditions.add(cj);
				}
			}

			// 10. ((∨ j∈J c_j ) ∧ c) ≡ (∨ j∈J (c_j ∧ c))
			for (int i = 0; i < newConditions.size(); i++) {
				GraphCondition ncI = newConditions.get(i);
				if (ncI instanceof Disjunction) {
					Disjunction disjJ = (Disjunction) ncI;
					newConditions.remove(i); // newConditions is now just a conjunction of remaining "c"

					List<GraphCondition> distrClauses = new ArrayList<>();
					for (GraphCondition cj : disjJ.getChildConditions()) {
						List<GraphCondition> n = new ArrayList<>();
						n.add(cj);
						n.addAll(newConditions);
						distrClauses.add(new Conjunction(conjunction.getOrigin(), n));
					}
					return new Disjunction(conjunction.getOrigin(), distrClauses);
				}
			}

			switch (newConditions.size()) {
			case 0:
				// 19. ∧ j∈∅ c_j ≡ true
				return new True(conjunction.getOrigin());
			case 1:
				// resolve single-element disjunction
				return newConditions.get(0);
			default:
				return new Conjunction(conjunction.getOrigin(), newConditions);
			}
		}

		// if we reach this, no rule was applicable. return original
		return condition;
	}

	/**
	 * Applies the rules of Pennemann until no changes occur anymore.
	 */
	public static GraphCondition applyPennemannRules(GraphCondition condition) {
		GraphCondition lastCondition = null;
		GraphCondition currCondition = condition;
		while (!currCondition.equals(lastCondition)) {
			lastCondition = currCondition;
			currCondition = applyPennemannRulesOnce(currCondition);
			/*
			 * Recurse
			 * Note that we do not handle ForAll here, since it should have been decomposed
			 * into Not(Exists(a, Not(c))) in the previous step already.
			 */
			if (currCondition instanceof Existential) {
				Existential cc = (Existential) currCondition;
				GraphCondition nchild = applyPennemannRules(cc.getChildCondition());
				currCondition = new Existential(cc.getMorphism(), nchild);
			} else if (currCondition instanceof Negation) {
				Negation cc = (Negation) currCondition;
				GraphCondition nchild = applyPennemannRules(cc.getChildCondition());
				currCondition = new Negation(nchild);
			} else if (currCondition instanceof NAryConnective) {
				NAryConnective cc = (NAryConnective) currCondition;
				List<GraphCondition> nchildren = cc.getChildConditions().stream().map(c -> applyPennemannRules(c)).collect(Collectors.toList());
				currCondition = cc.newSameType(cc.getOrigin(), nchildren);
			}
		}
		return currCondition;
	}

	private static boolean isIdentityMorphism(Morphism m) {
		/* Note that even though it is named "Identity" (after the conversion rules of Pennemann),
		 * it actually checks for isomorphism. Otherwise, the following rule could not be
		 * converted to DNF:
		 * Exists(empty -> N, Not(Exists(N -> N', new True(N'))))
		 * where N' is isomorphic, but not identical, to the single-node graph N
		 */
		return m.isIsomorphism();
	}

	/**
	 * Starting from a condition Exists(morphism, child) over C, where {@code morphism} is an isomorphism,
	 * modifies {@code child} such that it is directly specified over C.
	 * <p>Note that this operation does not implement shifting: it will silently produce incorrect results
	 * if {@code morphism} is not an isomorphism. (It will not check whether {@code morphism} really is an isomorphism.)
	 */
	private static GraphCondition eliminateIsomorphism(Morphism morphism, GraphCondition child) {
		if (child instanceof Constant) {
			return ((Constant) child).newSame(morphism.getDomain());
		} else if (child instanceof Negation) {
			Negation childN = (Negation) child;
			return new Negation(eliminateIsomorphism(morphism, childN.getChildCondition()));
		} else if (child instanceof QuantifiedCondition) {
			QuantifiedCondition childQ = (QuantifiedCondition) child;
			return childQ.newSameType(morphism.compose(childQ.getMorphism()), childQ.getChildCondition());
		} else if (child instanceof NAryConnective) {
			NAryConnective childD = (NAryConnective) child;
			Stream<GraphCondition> nchildren = childD.getChildConditions().stream().map(c -> eliminateIsomorphism(morphism, c));
			return childD.newSameType(morphism.getDomain(), nchildren);
		} else {
			throw new DNFUnknownConditionException(child, "isomorphism elimination");
		}
	}

	public static class DNFUnknownConditionException extends IllegalArgumentException {
		private static final long serialVersionUID = -2131492484639024966L;

		public DNFUnknownConditionException(GraphCondition condition) {
			this(condition, null);
		}

		public DNFUnknownConditionException(GraphCondition condition, String inside) {
			super("Unknown condition " + condition + " (type " + condition.getClass() + ") "
					+ (inside == null ? "" : "inside " + inside)
					+ "- DNF transform impossible");
		}
	}
}
