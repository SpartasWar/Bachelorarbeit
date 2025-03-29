package de.uni_due.inf.ti.gcsolvers;

import java.util.LinkedList;
import java.util.Queue;

public class CandidateSelectors {
	// prohibit instantiation
	private CandidateSelectors() { }
	
	private static ModelCandidate removeIndex(Queue<ModelCandidate> queue, int index) {
		if (!(queue instanceof LinkedList)) {
			throw new Error("The removeIndex helper currently supports just linked lists");
		}
		LinkedList<ModelCandidate> ll = (LinkedList<ModelCandidate>) queue;
		return ll.remove(index);
	}

	/**
	 * Always removes the head of the queue (FIFO algorithm, or breadth-first search)
	 */
	static ModelCandidate fifo(Queue<ModelCandidate> queue) {
		return queue.remove();
	}

	/**
	 * Always removes the tail of the queue (LIFO algorithm, or depth-first search)
	 */
	private static ModelCandidate lifo(Queue<ModelCandidate> queue) {
		if (!(queue instanceof LinkedList)) {
			throw new Error("The removeIndex helper currently supports just linked lists");
		}
		LinkedList<ModelCandidate> ll = (LinkedList<ModelCandidate>) queue;
		return ll.removeLast();
	}

	/**
	 * Picks a random element from the first 70% of the queue
	 */
	private static ModelCandidate randomFirst70(Queue<ModelCandidate> queue) {
		int randomIdx = (int) (Math.random() * (queue.size() * 0.7));
		return removeIndex(queue, randomIdx);
	}

	/**
	 * Picks first element (FIFO) in 50% of the cases and a random one otherwise
	 */
	private static ModelCandidate randomOrFirst(Queue<ModelCandidate> queue) {
		if (queue.size() <= 1 || Math.random() < 0.5) {
			return queue.remove();
		}
		int randomIdx = Math.max(
			(int) (Math.random() * (queue.size() - 2)) + 1,
			queue.size() - 1);
		return removeIndex(queue, randomIdx);
	}

	/**
	 * Picks a random element from the queue
	 */
	private static ModelCandidate random(Queue<ModelCandidate> queue) {
		if (queue.size() <= 1) {
			return queue.remove();
		}
		int randomIdx = Math.max(
			(int) (Math.random() * (queue.size() - 1)),
			queue.size() - 1);
		return removeIndex(queue, randomIdx);
	}
}
