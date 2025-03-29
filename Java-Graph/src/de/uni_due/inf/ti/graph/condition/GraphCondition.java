package de.uni_due.inf.ti.graph.condition;

import java.util.List;
import java.util.Set;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Label;
import de.uni_due.inf.ti.graph.Morphism;

/**
 * Base interface for all graph conditions over an origin graph.
 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
 */
public interface GraphCondition {
	/**
	 * The graph over which this condition is specified.
	 */
	public Graph getOrigin();

	/**
	 * The set of all labels occurring in this graph condition and any
	 * of its children (where applicable).
	 * @implSpec
	 * This should be at least the signature of the origin graph. Nesting
	 * conditions should also include the signatures of their children.
	 */
	public Set<Label> getSignature();

	/**
	 * The nesting level of the condition.
	 */
	public int getNestingLevel();

	/**
	 * Checks whether the given morphism satisfies this condition.
	 */
	public boolean satisfiedBy(Morphism q);

	/**
	 * Checks whether the given graph satisfies this condition.
	 * Requires the condition to be over the empty graph.
	 */
	default public boolean satisfiedBy(Graph g) {
		// check for non-empty origin
		if (!this.getOrigin().isEmpty()) {
			throw new IllegalArgumentException("Graphs can only satisfy graph conditions over the empty graph");
		}

		/* Since the test requires not just isomorphic but identical domain graphs,
		 * we use the actual condition root here. (We know it's empty)
		 */
		return this.satisfiedBy(Morphism.create(this.getOrigin(), g));
	}

	/**
	 * A literal l is either a condition of the form ∃(a,d) or ¬∃(a,d).
	 * We say that a literal of the form ∃(a,d) is a positive literal and
	 * of the form ¬∃(a,d) is a negative literal.
	 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	public boolean isLiteral();

	/**
	 * A literal of the form ∃(a,d) is a positive literal.
	 * @see #isLiteral
	 */
	public boolean isPositiveLiteral();

	/**
	 * A literal of the form ¬∃(a,d) is a negative literal.
	 * @see #isLiteral
	 */
	public boolean isNegativeLiteral();

	/**
	 * Each disjunction of literals is also called a clause.
	 * <p>Design choice: A literal counts as a clause as well, since it is effectively equivalent
	 * to a disjunction of a single literal.
	 * <p>Design choice: A conjunction with one literal (or with a disjunction of one literal) could be seen as
	 * a clause as well, but currently is not. TODO
	 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	public boolean isClause();

	/**
	 * Each conjunction of literals is called a product term.
	 * <p>Design choice: A literal counts as a product term as well, since it is effectively equivalent
	 * to a conjunction of a single literal.
	 * <p>Design choice: A disjunction with one literal (or with a conjunction of one literal) could be seen as
	 * a product term as well, but currently is not. TODO
	 * @see adapted from CNF definition of "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	public boolean isProduct();

	/**
	 * If the condition is a clause, returns a list of literals in this clause.
	 * If this condition is not a clause, throws {@link UnsupportedOperationException} or {@link ArrayStoreException}.
	 */
	public List<NestingGraphCondition> getClauseLiterals();

	/**
	 * If the condition is a product term, returns a list of literals in this product term.
	 * If this condition is not a product term, throws {@link UnsupportedOperationException} or {@link ArrayStoreException}.
	 */
	public List<NestingGraphCondition> getProductLiterals();

	/**
	 * Checks if this condition can be used as a conjunction of clauses.
	 * <p>Design choice: This includes clauses and literals as well, since they can be seen as
	 * a one-clause set or a one-clause-of-one-disjunction-set.
	 */
	public boolean isConjunctionOfClauses();

	/**
	 * Checks if this condition can be used as a disjunction of product terms.
	 * <p>Design choice: This includes product terms and literals as well.
	 */
	public boolean isDisjunctionOfProducts();

	/**
	 * If the condition is a conjunction of clauses, returns a list of clauses.
	 * If this condition is not a clause, throws an exception.
	 * <p>Note that the elements of the returned list are not guaranteed to be of type {@link Disjunction}.
	 * However, {@link #isClause()} will return true and {@link #getClauseLiterals()} can be called on them.
	 */
	public List<GraphCondition> getClauses();

	/**
	 * If the condition is a disjunction of product terms, returns a list of product terms.
	 * If this condition is not a disjunction of product terms, throws an exception.
	 * <p>Note that the elements of the returned list are not guaranteed to be of type {@link Conjunction}.
	 * However, {@link #isProductterm()} will return true and {@link #getProductLiterals()} can be called on them.
	 */
	public List<GraphCondition> getProducts();

	/**
	 * A condition is in weak conjunctive normal form (CNF) if it is either true, or false,
	 * or a conjunction of clauses ∧ j∈J c_j, with c_j = ∨ k∈K_j l_k, where for each literal,
	 * l_k = ∃(a_k,d_k) or l_k = ¬∃(a_k,d_k), d_k is a condition in weak CNF again.
	 * Moreover, for each negative literal l_k = ¬∃(a_k,d_k) it holds that a_k is a monomorphism,
	 * but not an isomorphism.
	 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	default public boolean isWeakCNF() {
		return this.isCNFOrWeakCNF(true);
	}

	/**
	 * A condition c is in CNF if it is in weak CNF and in addition for each literal ∃(a,d) or
	 * ¬∃(a,d) occurring in c it holds that a is a monomorphism, but not an isomorphism.
	 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	default public boolean isCNF() {
		return this.isCNFOrWeakCNF(false);
	}

	/**
	 * There is a transformation Shift such that for each condition c_P over P and
	 * each morphism b : P → P_0, it returns a condition Shift(b,c_P) over P_0 such that
	 * for each morphism n : P_0 → H it holds that n◦b |= c_P if and only if n |= Shift(b,c_P).
	 * @see "Lambers, Orejas: Tableau-based reasoning for graph properties"
	 */
	public GraphCondition shift(Morphism m);

	private boolean isCNFOrWeakCNF(boolean weak) {
		if (!this.isConjunctionOfClauses()) {
			return false;
		}
		for (GraphCondition clause : this.getClauses()) {
			// isConjunctionOfClauses guarantees that it really is a clause
			for (NestingGraphCondition literal : clause.getClauseLiterals()) {
				Existential litPos;
				if (literal.isNegativeLiteral()) {
					litPos = (Existential) literal.getChildCondition();
				} else {
					litPos = (Existential) literal;
				}
				Morphism a = litPos.getMorphism();
				GraphCondition d = litPos.getChildCondition();
				if ((weak && !d.isWeakCNF()) || (!weak && !d.isCNF())) { // TODO: if checking for CNF, shouldn't d also be in CNF?
					return false;
				}
				if ((!weak) || literal.isNegativeLiteral()) {
					if (!a.isInjective()) {
						return false;
					}
					if (a.isIsomorphism()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}

/*

L          is a clause and a CoC
L v L v L  is a clause and a CoC (a single clause)
L ^ L ^ L  is a CoC (it contains three things which are clause-ish)

*/
