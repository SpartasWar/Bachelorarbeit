package de.uni_due.inf.ti.gcsolvers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TimingImpl implements Timing {
	private Map<Object, Long> events = new HashMap<>();

	@Override
	public void log(Object what) {
		events.put(what, System.nanoTime());
	}

	public Map<Object, Long> getEvents() {
		return Collections.unmodifiableMap(events);
	}

	public long differenceNs(Object start, Object end) {
		return events.get(end) - events.get(start);
	}

	public long differenceUs(Object start, Object end) {
		return differenceNs(start, end) / 1_000;
	}
	
	public long differenceMs(Object start, Object end) {
		return differenceNs(start, end) / 1_000_000;
	}
}
