package de.uni_due.inf.ti.graph.condition;

import java.util.Set;

import de.uni_due.inf.ti.graph.Label;

/**
 * Common interface for all types of graph condition that contain exactly one nested child condition.
 */
public interface NestingGraphCondition extends GraphCondition {
	/**
	 * Returns the child condition of this condition.
	 */
	GraphCondition getChildCondition();

	@Override
	default Set<Label> getSignature() {
		return getChildCondition().getSignature();
	}
}
