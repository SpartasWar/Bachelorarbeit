package de.uni_due.inf.ti.gcsolvers;

/**
 * An implementation of the {@link Timing} interface that does not record any of its events.
 * @author Lara Stoltenow
 *
 */
public class NullTiming implements Timing {
	public static final NullTiming INSTANCE = new NullTiming();

	@Override
	public void log(Object what) {
		// intentionally empty
	}
}
