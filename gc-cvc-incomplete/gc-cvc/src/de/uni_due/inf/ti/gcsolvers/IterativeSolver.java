package de.uni_due.inf.ti.gcsolvers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Disjunction;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.GraphCondition;

public abstract class IterativeSolver extends Solver {
	/** 
	 * candidate models. Each one contains an alternating leaf condition.
	 * The condition also must be universal (i.e., conjunction of forall).
	 */
	protected Queue<ModelCandidate> candidates;

	protected Function<Queue<ModelCandidate>, ModelCandidate> candidateSelector = CandidateSelectors::fifo;

	/**
	 * Runs a single computation step of this solver.
	 * <p>
	 * A computation step is expected to finish relatively quickly and not block for a potentially unlimited time.
	 * It is however reasonable that any further step takes a little more time than the previous one.
	 * <p>
	 * This will update the result status of the solver, so check {@link #getResult()} after one step to figure out if the solver has already finished.
	 */
	public abstract void step();

	/**
	 * To be used in a constructor: initializes the candidate list of this IterativeSolver based on the given graph condition.
	 * The condition must be alternating, which is checked by this method (otherwise exception).
	 * @param condition
	 */
	protected void initializeModelCandidates(GraphCondition condition) {
		if (!MakeAlternating.isAlternating(condition)) {
			throw new IllegalArgumentException("given condition is not alternating");
		}
		this.candidates = new LinkedList<>();
		if (condition instanceof Conjunction) {
			this.candidates.add(new ModelCandidate(
					Morphism.createIdentity(condition.getOrigin()),
					condition
			));
		} else if (condition instanceof Disjunction disj) {
			for (GraphCondition child : disj.getChildConditions()) {
				if (child instanceof Existential exChild) {
					this.candidates.add(new ModelCandidate(
							exChild.getMorphism(),
							exChild.getChildCondition()
					));
				} else {
					throw new Error("condition is alternating, but disjunction does not have existential child?");
				}
			}
		} else {
			throw new Error("condition is alternating, but neither conjunction nor disjunction?");
		}
	}
}