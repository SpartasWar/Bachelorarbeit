package de.uni_due.inf.ti.gcsolvers;

import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.GraphCondition;

public abstract class Solver {
	protected GraphCondition condition;
	protected SolverResult result = null;
	protected Morphism model = null;
	protected Timing timing = NullTiming.INSTANCE;

	/**
	 * Returns the original condition which is to be solved.
	 */
	public final GraphCondition getCondition() {
		return condition;
	}

	/**
	 * Returns the satisfiability result of this solver, or null if it has not come to a conclusive result yet.
	 */
	public final SolverResult getResult() {
		return result;
	}

	/**
	 * Returns a model for the condition, or null if such a model does not exist or has not yet been found.
	 */
	public final Morphism getModel() {
		return model;
	}

}