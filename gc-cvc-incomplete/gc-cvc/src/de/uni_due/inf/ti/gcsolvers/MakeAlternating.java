package de.uni_due.inf.ti.gcsolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
 * This class implements conversion of conditions to alternating conditions as
 * defined in the paper
 * "Coinductive Techniques for Checking Satisfiability of Generalized Nested Conditions".
 * That paper defines two types of conditions: disjunctions of existentials and
 * conjunctions of universals. An alternating condition alternates (with
 * increasing depth) between universal and existential condition.
 * <p>
 * The transformation rules are inspired by Pennemann's rules for converting
 * to CNF (see: Development of Correct Graph Transformation Systems).
 * <p>
 * Some of the following rules are not strictly necessary to produce an
 * alternating condition. However, they provide additional simplification at
 * negligible cost.
 * <ol>
 * <li>Get rid of all negations:
 *   <ol>
 *   <li> ¬∀(a,c) ≡ ∃(a,¬c)
 *   <li> ¬∃(a,c) ≡ ∀(a,¬c)
 *   <li> ¬¬c ≡ c
 *   <li> ¬true ≡ false
 *   <li> ¬false ≡ true
 *   <li> ¬(∨ j∈J c_j ) ≡ (∧ j∈J ¬c_j )
 *   <li> ¬(∧ j∈J c_j ) ≡ (∨ j∈J ¬c_j )
 *   </ol>
 *   Afterwards the condition has no more negations.
 * <li>Obvious simplifications, mostly to decrease condition size, apply as long as possible:
 *   <ol>
 *   <li> Trivial equivalences:
 *     <ol>
 *     <li> ∃(id, c) ≡ c
 *     <li> ∀(id, c) ≡ c
 *     <li> ∃(iso, c) ≡ f(c), where f incorporates the iso into c
 *     <li> ∀(iso, c) ≡ f(c), see above
 *     <li> ∃(a, false) ≡ false
 *     <li> ∀(a, true) ≡ true
 *     <li> ∃(a, ∃(b,c)) ≡ ∃(b ◦ a, c)
 *     <li> ∀(a, ∀(b,c)) ≡ ∀(b ◦ a, c)
 *     <li> ∃(a,c) ≡ false if a notin Monos (? or just consider this invalid)
 *     <li> ∀(a,c) ≡ true if a notin Monos (? see above)
 *     </ol>
 *   <li> Remove trivial children from junctions:
 *     <ol>
 *     <li> ∨ j∈J c_j ≡ true         if ∃k ∈J. c_k = true
 *     <li> ∨ j∈J c_j ≡ ∨j∈J\{k} c_j if ∃k ∈J. c_k = false
 *     <li> ∧ j∈J c_j ≡ ∧j∈J\{k} c_j if ∃k ∈J. c_k = true
 *     <li> ∧ j∈J c_j ≡ false        if ∃k ∈J. c_k = false
 *     <li> ∨ j∈∅ c_j ≡ false
 *     <li> ∧ j∈∅ c_j ≡ true
 *     </ol>
 *     The last two rules are later reversed again! We use them here to allow
 *     some of the other optimizations.
 *   <li> Remove useless (singleton or nested identical) junctions:
 *     <ol>
 *     <li> ∨ j∈J c_j ≡ c_1 if J={1}
 *     <li> ∧ j∈J c_j ≡ c_1 if J={1}
 *     <li> ∨ j∈J (∨ k∈Kj) ≡ ∨ k∈K*
 *     <li> ∧ j∈J (∧ k∈Kj) ≡ ∧ k∈K*
 *     </ol>
 *   <li> Pull junctions out of compatible quantifiers, so that it is less
 *   likely for them to require insertion of identities:
 *     <ol>
 *     <li> ∃(a, ∨ j∈J c_j ) ≡ ∨ j∈J ∃(a, c_j )
 *     <li> ∀(a, ∧ j∈J c_j ) ≡ ∧ j∈J ∀(a, c_j )
 *     </ol>
 *     These two rules by themselves do not reduce complexity of the
 *     condition. So it is important that they do not lead to infinite loops.
 *   </ol>
 *   These are not directly related to making the condition alternating, but
 *   can provide some additional simplification, especially considering the
 *   following insertion of identities.
 * <li> Remove constants:
 *   <ol>
 *   <li> false ≡ ∨ j∈∅ c_j
 *   <li> true ≡ ∧ j∈∅ c_j
 *   </ol>
 *   Pennemann has these rules backwards, which makes the condition simpler in
 *   some way, but our goal is to reduce the number of special cases to as few
 *   as possible. So we do it the other way round.
 *   The condition now has only NAryConnectives and Quantifiers.
 * <li> Actually make condition alternating:
 *   <ol>
 *   <li> Existential: wrap into a singleton disjunction,
 *     then make child (conjunction or universal) alternating
 *   <li> Universal: wrap into a singleton conjunction,
 *     then make child (disjunction or existential) alternating
 *   <li> Disjunction: wrap any child c_i that is not an existential as ∃(id, c_i),
 *     then make all children of existential children (conjunction or universal) alternating
 *   <li> Conjunction: wrap any child c_i that is not a universal as ∀(id, c_i),
 *     then make all children of universal children (disjunction or existential) alternating
 *   </ol>
 *   The rules of the previous steps ensure that all considered children are
 *   of the specified types only.
 * </ol>
 */
public class MakeAlternating {
	/* Prevent instantiation */
	private MakeAlternating() {
	}

	/**
	 * Eliminate negations at the top level of the condition. Does not recurse,
	 * use {@link #applyRules(GraphCondition, Function)} for that.
	 * <p>If none of the rules were applicable, a condition that is {@link #equals(Object)} to the
	 * original condition is returned. There is no guarantee that an identical ({@code ==}) condition is returned.
	 */
	private static GraphCondition eliminateNegations(GraphCondition condition) {
		if (!(condition instanceof Negation neg)) return condition;
		GraphCondition inner = neg.getChildCondition();

		if (inner instanceof QuantifiedCondition innerQc) {
			// ¬∀(a,c) ≡ ∃(a,¬c) and ¬∃(a,c) ≡ ∀(a,¬c)
			return innerQc.newOppositeType(innerQc.getMorphism(), new Negation(innerQc.getChildCondition()));
		} else if (inner instanceof Negation innerNeg) {
			// ¬¬c ≡ c
			return innerNeg.getChildCondition();
		} else if (inner instanceof Constant innerConst) {
			// ¬true ≡ false and ¬false ≡ true
			return innerConst.newOpposite(innerConst.getOrigin());
		} else if (inner instanceof NAryConnective innerNAry) {
			// ¬(∨ j∈J c_j ) ≡ (∧ j∈J ¬c_j ) and
			// ¬(∧ j∈J c_j ) ≡ (∨ j∈J ¬c_j )
			Stream<GraphCondition> newStream = innerNAry.getChildConditions().stream().map(Negation::new);
			return innerNAry.newOppositeType(innerNAry.getOrigin(), newStream);
		} else {
			throw new DNFUnknownConditionException(inner, "negation elimination");
		}
	}

	/**
	 * Applies optional simplifications to the condition. Does not recurse,
	 * use {@link #applyRules(GraphCondition, Function)} for that.
	 * <p>If none of the rules were applicable, a condition that is {@link #equals(Object)} to the
	 * original condition is returned. There is no guarantee that an identical ({@code ==}) condition is returned.
	 */
	private static GraphCondition applySimplifications(GraphCondition condition) {
		if (condition instanceof QuantifiedCondition qc) {
			GraphCondition inner = qc.getChildCondition();
			if (qc instanceof Existential && inner instanceof False) {
				// ∃(a, false) ≡ false
				return new False(qc.getOrigin());
			} else if (qc instanceof ForAll && inner instanceof True) {
				// ∀(a, true) ≡ true
				return new True(qc.getOrigin());
			} else if (inner instanceof QuantifiedCondition innerQc && (
					(qc instanceof Existential && inner instanceof Existential) ||
					(qc instanceof ForAll && inner instanceof ForAll)
			)) {
				// ∃(a, ∃(b,c)) ≡ ∃(b ◦ a, c)
				// ∀(a, ∀(b,c)) ≡ ∀(b ◦ a, c)
				return qc.newSameType(qc.getMorphism().compose(innerQc.getMorphism()), innerQc.getChildCondition());
			} else if (isIdentityMorphism(qc.getMorphism())) {
				// ∃(id, c) ≡ c
				// ∀(id, c) ≡ c
				// ∃(iso, c) ≡ f(c), where f incorporates the iso into c
				// ∀(iso, c) ≡ f(c), see above
				return eliminateIsomorphism(qc.getMorphism(), inner);
			} else if (inner instanceof NAryConnective innerNc && (
					(qc instanceof Existential && inner instanceof Disjunction) ||
					(qc instanceof ForAll && inner instanceof Conjunction)
			)) {
				// ∃(a, ∨ j∈J c_j ) ≡ ∨ j∈J ∃(a, c_j )
				// ∀(a, ∧ j∈J c_j ) ≡ ∧ j∈J ∀(a, c_j )
				return innerNc.newSameType(qc.getOrigin(),
						innerNc.getChildConditions().stream().map(cj -> qc.newSameType(qc.getMorphism(), cj))
				);
			} else {
				return condition;
			}
		} else if (condition instanceof NAryConnective nc) {
			assert nc instanceof Conjunction || nc instanceof Disjunction;
			if (nc.getChildConditions().isEmpty()) {
				// ∨ j∈∅ c_j ≡ false
				// ∧ j∈∅ c_j ≡ true
				return nc instanceof Conjunction ? new True(nc.getOrigin()) : new False(nc.getOrigin());
			} else if (nc.getChildConditions().size() == 1) {
				// ∨ j∈J c_j ≡ c_1 if J={1}
				// ∧ j∈J c_j ≡ c_1 if J={1}
				return nc.getChildConditions().get(0);
			}
			
			if (nc instanceof Conjunction) {
				List<GraphCondition> newChildren = new ArrayList<>();
				for (GraphCondition cj : nc.getChildConditions()) {
					// ∧ j∈J c_j ≡ false        if ∃k ∈J. c_k = false
					if (cj instanceof False) { return cj; }
					// ∧ j∈J c_j ≡ ∧j∈J\{k} c_j if ∃k ∈J. c_k = true
					else if (cj instanceof True) { }
					// ∧ j∈J (∧ k∈Kj) ≡ ∧ k∈K*
					else if (cj instanceof Conjunction cjJ) { newChildren.addAll(cjJ.getChildConditions()); }
					else { newChildren.add(cj); }
				}
				return new Conjunction(nc.getOrigin(), newChildren);
			} else { // Disjunction
				List<GraphCondition> newChildren = new ArrayList<>();
				for (GraphCondition cj : nc.getChildConditions()) {
					// ∨ j∈J c_j ≡ true         if ∃k ∈J. c_k = true
					if (cj instanceof True) { return cj; }
					// ∨ j∈J c_j ≡ ∨j∈J\{k} c_j if ∃k ∈J. c_k = false
					else if (cj instanceof False) { }
					// ∨ j∈J (∨ k∈Kj) ≡ ∨ k∈K*
					else if (cj instanceof Disjunction cjJ) { newChildren.addAll(cjJ.getChildConditions()); }
					else { newChildren.add(cj); }
				}
				return new Disjunction(nc.getOrigin(), newChildren);
			}
		} else if (condition instanceof Constant) {
			return condition;
		} else {
			throw new DNFUnknownConditionException(condition, "simplifications");
		}
	}

	/**
	 * Eliminates constants from the condition. Does not recurse,
	 * use {@link #applyRules(GraphCondition, Function)} for that.
	 * <p>If none of the rules were applicable, a condition that is {@link #equals(Object)} to the
	 * original condition is returned. There is no guarantee that an identical ({@code ==}) condition is returned.
	 */
	private static GraphCondition eliminateConstants(GraphCondition condition) {
		if (condition instanceof Constant) {
			assert condition instanceof True || condition instanceof False;
			if (condition instanceof True) {
				// true ≡ ∧ j∈∅ c_j
				return new Conjunction(condition.getOrigin());
			} else {
				// false ≡ ∨ j∈∅ c_j
				return new Disjunction(condition.getOrigin());
			}
		} else {
			return condition;
		}
	}

	/**
	 * Turns the condition into an alternating one. Expects a condition
	 * that is just n-ary connectives and quantifiers. Unlike the other
	 * methods, this does recurse.
	 */
	private static GraphCondition makeAlternatingFinal(GraphCondition condition) {
		if (condition instanceof Existential ce) {
			// wrap into a singleton disjunction,
			// then make child (conjunction or universal) alternating
			GraphCondition inner = ce.getChildCondition();
			if (!(inner instanceof Conjunction || inner instanceof ForAll)) {
				throw new DNFUnknownConditionException(inner, "child of existential");
			}
			return new Disjunction(new Existential(ce.getMorphism(), makeAlternatingFinal(inner)));
		} else if (condition instanceof ForAll cf) {
			// wrap into a singleton conjunction,
			// then make child (disjunction or existential) alternating
			GraphCondition inner = cf.getChildCondition();
			if (!(inner instanceof Disjunction || inner instanceof Existential)) {
				throw new DNFUnknownConditionException(inner, "child of universal");
			}
			return new Conjunction(new ForAll(cf.getMorphism(), makeAlternatingFinal(inner)));
		} else if (condition instanceof Disjunction cd) {
			// wrap any child c_i that is not an existential as ∃(id, c_i),
			// then make all children of existential children (conjunction or universal) alternating
			GraphCondition[] oldChildren = cd.getChildConditions().toArray(new GraphCondition[0]);
			GraphCondition[] newChildren = new GraphCondition[oldChildren.length];
			for (int i = 0; i < oldChildren.length; i++) {
				if (oldChildren[i] instanceof Existential ie) {
					GraphCondition iec = ie.getChildCondition();
					if (!(iec instanceof Conjunction || iec instanceof ForAll)) {
						throw new DNFUnknownConditionException(iec, "child of existential in disjunction");
					}
					newChildren[i] = new Existential(ie.getMorphism(), makeAlternatingFinal(iec));
				} else if (oldChildren[i] instanceof ForAll || oldChildren[i] instanceof Conjunction) {
					newChildren[i] = new Existential(
							Morphism.createIdentity(cd.getOrigin()),
							makeAlternatingFinal(oldChildren[i])
					);
				} else {
					throw new DNFUnknownConditionException(oldChildren[i], "element of disjunction");
				}
			}
			return new Disjunction(condition.getOrigin(), newChildren);
		} else if (condition instanceof Conjunction cc) {
			// wrap any child c_i that is not a universal as ∀(id, c_i),
			// then make all children of universal children (disjunction or existential) alternating
			GraphCondition[] oldChildren = cc.getChildConditions().toArray(new GraphCondition[0]);
			GraphCondition[] newChildren = new GraphCondition[oldChildren.length];
			for (int i = 0; i < oldChildren.length; i++) {
				if (oldChildren[i] instanceof ForAll iu) {
					GraphCondition iuc = iu.getChildCondition();
					if (!(iuc instanceof Disjunction || iuc instanceof Existential)) {
						throw new DNFUnknownConditionException(iuc, "child of universal in conjunction");
					}
					newChildren[i] = new ForAll(iu.getMorphism(), makeAlternatingFinal(iuc));
				} else if (oldChildren[i] instanceof Existential || oldChildren[i] instanceof Disjunction) {
					newChildren[i] = new ForAll(
							Morphism.createIdentity(cc.getOrigin()),
							makeAlternatingFinal(oldChildren[i])
					);
				} else {
					throw new DNFUnknownConditionException(oldChildren[i], "element of conjunction");
				}
			}
			return new Conjunction(condition.getOrigin(), newChildren);
		} else {
			throw new DNFUnknownConditionException(condition, "makeAlternating");
		}
	}

	/**
	 * Applies given rules recursively until no changes occur anymore.
	 */
	private static GraphCondition applyRules(GraphCondition condition, Function<GraphCondition, GraphCondition> transformer, String transformerName) {
		GraphCondition lastCondition = null;
		GraphCondition currCondition = condition;
		while (!currCondition.equals(lastCondition)) {
			lastCondition = currCondition;
			currCondition = transformer.apply(currCondition);
			/* Recurse */
			if (currCondition instanceof QuantifiedCondition qc) {
				GraphCondition nchild = applyRules(qc.getChildCondition(), transformer, transformerName);
				currCondition = qc.newSameType(qc.getMorphism(), nchild);
			} else if (currCondition instanceof Negation neg) {
				GraphCondition nchild = applyRules(neg.getChildCondition(), transformer, transformerName);
				currCondition = new Negation(nchild);
			} else if (currCondition instanceof NAryConnective nary) {
				List<GraphCondition> nchildren = nary.getChildConditions().stream()
						.map(c -> applyRules(c, transformer, transformerName))
						.collect(Collectors.toList());
				currCondition = nary.newSameType(nary.getOrigin(), nchildren);
			} else if (currCondition instanceof Constant) {
				// nothing
			} else {
				throw new DNFUnknownConditionException(currCondition, "recursive rule application of " + transformerName);
			}
		}
		return currCondition;
	}

	/**
	 * Applies given rules recursively until no changes occur anymore.
	 */
	private static GraphCondition applyRules(GraphCondition condition, Function<GraphCondition, GraphCondition> transformer) {
		return applyRules(condition, transformer, transformer.toString());
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

	public static GraphCondition makeAlternating(GraphCondition condition) {
		GraphCondition withoutNegs  = applyRules(condition,   MakeAlternating::eliminateNegations, "eliminateNegations");
		GraphCondition simplified   = applyRules(withoutNegs, MakeAlternating::applySimplifications, "applySimplifications");
		GraphCondition withoutConst = applyRules(simplified,  MakeAlternating::eliminateConstants, "eliminateConstants");
		return makeAlternatingFinal(withoutConst);
	}
	
	/**
	 * Checks if the condition is alternating, i.e., a Disjunction of Existentials
	 * or a Conjunction of ForAlls, and they alternate from each level to the next.
	 * @param condition
	 * @return
	 */
	public static boolean isAlternating(GraphCondition condition) {
		if (condition instanceof Disjunction disj) {
			for (GraphCondition child : disj.getChildConditions()) {
				if (child instanceof Existential exChild) {
					if (!(exChild.getChildCondition() instanceof Conjunction)) return false;
					if (!isAlternating(exChild.getChildCondition())) return false;
				} else {
					return false;
				}
			}
			return true;
		} else if (condition instanceof Conjunction conj) {
			for (GraphCondition child : conj.getChildConditions()) {
				if (child instanceof ForAll faChild) {
					if (!(faChild.getChildCondition() instanceof Disjunction)) return false;
					if (!isAlternating(faChild.getChildCondition())) return false;
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
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
					+ " - DNF transform impossible");
		}
	}
}
