package de.uni_due.inf.ti.gcsolvers;

import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.GraphCondition;

/**
 * A potential partial model for some original condition C (not stored
 * in this object). If {@code leafCondition} is satisfied by {@code a},
 * then {@code partialModel ; a} is a model for the original condition.
 * <p>
 * The provided condition should be alternating, so that the tableau
 * procedure can continue working on the model candidates.
 * @author Lara Stoltenow
 * @param partialModel a potential partial model that was previously obtained
 * @param leafCondition a condition originating at the domain of {@code partialModel}
 *                      that has to be satisfiable in order to obtain an actual model
 *
 */
public record ModelCandidate(Morphism partialModel, GraphCondition leafCondition) {

}
