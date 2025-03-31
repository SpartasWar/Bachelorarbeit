package de.uni_due.inf.ti.tableaugc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import de.uni_due.inf.ti.gcsolvers.IterativeSolver;
import de.uni_due.inf.ti.gcsolvers.MakeAlternating;
import de.uni_due.inf.ti.gcsolvers.ModelCandidate;
import de.uni_due.inf.ti.gcsolvers.SolverResult;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.condition.Conjunction;
import de.uni_due.inf.ti.graph.condition.Disjunction;
import de.uni_due.inf.ti.graph.condition.Existential;
import de.uni_due.inf.ti.graph.condition.ForAll;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.io.SgfMap;
import de.uni_due.inf.ti.graph.io.SgfParser;
import de.uni_due.inf.ti.graph.io.SgfWriter;
import de.uni_due.inf.ti.util.Enumerators;

public class TableauSolver extends IterativeSolver {
	public TableauSolver(GraphCondition condition) {
		this.condition = condition;
		initializeModelCandidates(condition);
	}

	TableauSolver(GraphCondition condition, List<ModelCandidate> candidates) {
		this.condition = condition;
		this.candidates = new LinkedList<>(candidates);
	}

	/**
	 * Compute the result of pulling {@code fp_Ap} forward. {@code fp_Ap}
	 * must be a child of {@code leaf}, and its morphism must be an iso.
	 * 
	 * Returns a Disjunction of {@code Exists f_p;g_j . (B_j and (others shifted by f_p;g_j))}.
	 */
	private static Disjunction pullForward(Conjunction leaf, ForAll fp_Ap) {
		Morphism f_p = fp_Ap.getMorphism();
		Disjunction A_p = (Disjunction) fp_Ap.getChildCondition();
		/* A_p again consists of several existentials g_j.B_j */
		
		List<ForAll> others = new ArrayList<>((List) leaf.getChildConditions());
		if (!others.remove(fp_Ap)) {
			// was not removed because it was not in the list
			throw new IllegalArgumentException("given fp_Ap is not a child of given leaf");
		}

		/* disj is a list of new Existentials for the resulting disjunction */
		List<GraphCondition> disj = new ArrayList<>();
		for (GraphCondition c : A_p.getChildConditions()) {
			Existential gj_Bj = (Existential) c;
			Morphism g_j = gj_Bj.getMorphism();
			Conjunction B_j = (Conjunction) gj_Bj.getChildCondition();
			Morphism fp_gj = Morphism.createComposition(f_p, g_j);
			
			Stream<ForAll> othersStream = others.size() > 1000 ? others.parallelStream() : others.stream();
			List<ForAll> othersShifted = othersStream.flatMap((ForAll fm_Am) -> {
				Conjunction fm_Am_shifted = (Conjunction) fm_Am.shift(fp_gj);
				List<ForAll> newChildren = (List) fm_Am_shifted.getChildConditions();
				return Enumerators.stream(newChildren);
			}).toList();

			/* newChildren should be purely ForAlls */
			List<GraphCondition> newChildren = new ArrayList<>(B_j.getChildConditions());
			newChildren.addAll(othersShifted);
			Conjunction newA = new Conjunction(fp_gj.getCodomain(), newChildren);
			disj.add(new Existential(fp_gj, newA));
		}
		return new Disjunction(f_p.getDomain(), disj);
	}

	@Override
	public void step() {
		if (result != null) {
			throw new Error("solver has finished and cannot do any further steps");
		}

		if (candidates.isEmpty()) {
			result = SolverResult.Unsatisfiable;
			return;
		}

		/* obtain a model candidate from the queue.
		 * This also removes it from the queue!
		 * Note that it is guaranteed to be universal.
		 */
		ModelCandidate candidate = candidateSelector.apply(candidates);
		Conjunction leaf = (Conjunction) candidate.leafCondition();

		/* Check if we can (or need to) do a tableau step, i.e.,
		 * if we have some iso child that can be pulled forward
		 */
		for (GraphCondition leafChild : leaf.getChildConditions()) {
			ForAll leafForAll = (ForAll) leafChild;
			Morphism f_p = leafForAll.getMorphism();
			if (f_p.isIsomorphism()) {
				/* we could pull this forward
				 * TODO: figure out if it is a good idea to always pull forward the first
				 * candidate, or if that could get us trapped in infinite loops (or is
				 * somehow bad for other reasons)
				 * This probably MUST be done, to ensure the fairness condition!
				 * If not keeping a queue, one could at least pick a random candidate.
				 */
				Disjunction disj = pullForward(leaf, leafForAll);
				for (GraphCondition dc : disj.getChildConditions()) {
					Existential c = (Existential) dc;
					candidates.add(new ModelCandidate(
							Morphism.createComposition(candidate.partialModel(), c.getMorphism()),
							c.getChildCondition()
					));
				}
				return;
			}
		}
		
		/* if we reach this, then the leaf had no iso children.
		 * Then, the identity is a model!
		 */
		result = SolverResult.Satisfiable;
		model = candidate.partialModel();
	}
	
	// test method
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java ...TableauSolver <sgf-filename> <condition-name>");
			System.exit(1);
		}
		
		SgfMap sgf;
		try {
			sgf = SgfParser.parseSgfFile(args[0]);
		} catch (IOException e) {
			System.err.println("Error parsing sgf file " + args[0] + ":");
			e.printStackTrace();
			System.exit(1);
			return; // needed to avoid "uninitialized" error below
		}
		
		GraphCondition cond = sgf.getGraphCondition(args[1]);
		cond = MakeAlternating.makeAlternating(cond);
		
		TableauSolver solver = new TableauSolver(cond);
		do {
			solver.step();
		} while (solver.getResult() == null);
		System.out.println("Solver result: " + solver.getResult());
		if (solver.getResult() == SolverResult.Satisfiable) {
			System.out.println("Model:");
			SgfWriter w = new SgfWriter(System.out);
			w.writeMorphism(solver.getModel());
		}
		System.exit(0);
	}
}
