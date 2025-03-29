package de.uni_due.inf.ti.gcsolvers;

import de.uni_due.inf.ti.graph.condition.GraphCondition;

public class MultiSolver extends IterativeSolver {
	public MultiSolver(GraphCondition condition) {
		this.condition = condition;
		initializeModelCandidates(condition);
	}

	@Override
	public void step() {
		/* TODO: should be restructured somehow. The CVC5 solver should be able
		 * to operate in background somehow, so it can be used in cooperation
		 * with other solvers like the tableau solver.
		 * 
		 * Anyway, first do a tableau solver step
		 */
		throw new UnsupportedOperationException();
	}
}
