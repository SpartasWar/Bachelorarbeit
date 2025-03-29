package de.uni_due.inf.ti.gcsolvers;

public enum SolverResult {
	/**
	 * Satisfiable with a finite model
	 */
	Satisfiable,
	/**
	 * Not satisfiable at all
	 */
	Unsatisfiable,
	/**
	 * Not known and the given solver has given up
	 */
	Unknown;
}
