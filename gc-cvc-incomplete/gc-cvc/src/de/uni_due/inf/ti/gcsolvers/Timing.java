package de.uni_due.inf.ti.gcsolvers;

public interface Timing {
	/**
	 * Record a timing event, usually to indicate that some step of computation
	 * is about to start or has just finished. Use the {@code what} parameter
	 * to indicate details of the exact step, for instance by passing certain
	 * enum values.
	 * @param what details of the step that is taking place
	 */
	void log(Object what);
}
