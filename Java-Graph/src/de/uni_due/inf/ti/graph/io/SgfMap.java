package de.uni_due.inf.ti.graph.io;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Rule;
import de.uni_due.inf.ti.graph.TransformationSystem;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.condition.cospan.CospanCondition;
import de.uni_due.inf.ti.graph.ext.Cospan;

public class SgfMap implements Map<String,Object> {
	private final Map<String, Object> map;

	SgfMap(Map<String, Object> map) {
		this.map = map;
	}
	
	public Graph getGraph(String key) {
		return (Graph) map.get(key);
	}

	public Morphism getMorphism(String key) {
		return (Morphism) map.get(key);
	}

	public GraphCondition getGraphCondition(String key) {
		return (GraphCondition) map.get(key);
	}
	
	public Rule getRule(String key) {
		return (Rule) map.get(key);
	}
	
	public Cospan getCospan(String key) {
		return (Cospan) map.get(key);
	}
	
	public TransformationSystem getSystem(String key) {
		return (TransformationSystem) map.get(key);
	}
	
	public CospanCondition getCospanCondition(String key) {
		return (CospanCondition) map.get(key);
	}

	/* delegates to map */

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public Object put(String key, Object value) {
		return map.put(key, value);
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Collection<Object> values() {
		return map.values();
	}

	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	public Object getOrDefault(Object key, Object defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super String, ? super Object> action) {
		map.forEach(action);
	}

	public void replaceAll(BiFunction<? super String, ? super Object, ? extends Object> function) {
		map.replaceAll(function);
	}

	public Object putIfAbsent(String key, Object value) {
		return map.putIfAbsent(key, value);
	}

	public boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}

	public boolean replace(String key, Object oldValue, Object newValue) {
		return map.replace(key, oldValue, newValue);
	}

	public Object replace(String key, Object value) {
		return map.replace(key, value);
	}

	public Object computeIfAbsent(String key, Function<? super String, ? extends Object> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}

	public Object computeIfPresent(String key,
			BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}

	public Object compute(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
		return map.compute(key, remappingFunction);
	}

	public Object merge(String key, Object value,
			BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}
}
