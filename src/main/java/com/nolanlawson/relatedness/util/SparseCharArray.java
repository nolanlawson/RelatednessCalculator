package com.nolanlawson.relatedness.util;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 * Basic replacement for either a Map<Character,X>. Low-memory, because it uses
 * arrays internally instead of hash maps.
 * 
 * Assumes your key class can be cast to int - otherwise, it'll fail on runtime.
 * 
 * @author nolan
 * 
 * @param <T>
 */
public class SparseCharArray<T> extends AbstractMap<Character, T> {

    private SparseCharArray() {
    }

    /**
     * Creates a new SparseCharArray from the given map.  
     * @param inputMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> SparseCharArray<T> fromMap(Map<Character, T> inputMap) {
	if (inputMap.isEmpty()) {
	    return (SparseCharArray<T>) EMPTY;
	} else if (inputMap.size() == 1) {
	    return new SingletonSparseCharArray<T>(inputMap.keySet().iterator()
		    .next(), inputMap.values().iterator().next());
	}
	
	MultiSparseCharArray<T> result = new MultiSparseCharArray<T>();

	result.startIdx = Ordering.natural().min(inputMap.keySet());
	int maxIdx = Ordering.natural().max(inputMap.keySet());

	result.data = new Object[(maxIdx - result.startIdx) + 1];
	result.entries = new int[inputMap.size()];
	int i = 0;
	for (Entry<Character, T> entry : inputMap.entrySet()) {
	    int keyIdx = ((int) entry.getKey().charValue()) - result.startIdx;
	    result.data[keyIdx] = entry.getValue();
	    result.entries[i++] = keyIdx;
	}

	return result;
    }

    @Override
    public Set<Entry<Character, T>> entrySet() {
	throw new UnsupportedOperationException(); // implemented in subclasses
    }

    /**
     * Unchanging empty sparse char array.
     */
    @SuppressWarnings("rawtypes")
    private static final MultiSparseCharArray<?> EMPTY = new MultiSparseCharArray();
    static {
	EMPTY.startIdx = 0;
	EMPTY.data = new Object[0];
	EMPTY.entries = new int[0];
    }
    
    /**
     * SparseCharArray with >1 entries.
     * @author nolan
     *
     * @param <T>
     */
    private static class MultiSparseCharArray<T> extends SparseCharArray<T> {
	    private int startIdx;
	    private Object[] data;
	    private int[] entries;
	    
	    @SuppressWarnings("unchecked")
	    @Override
	    public T get(Object key) {
		int idx = ((int)(Character) key) - startIdx;
		if (idx < 0 || idx >= data.length) {
		    return null;
		}
		return (T) data[idx];
	    }
	    
	    @SuppressWarnings("unchecked")
	    @Override
	    public Set<Entry<Character, T>> entrySet() {
		Set<Entry<Character, T>> result = Sets.newHashSet();
		for (int i = 0; i < entries.length; i++) {
		    int entry = entries[i];
		    result.add(new SCAEntry<T>(Character
			    .valueOf(((char) (entry + startIdx))), (T) data[entry]));
		}
		return result;
	    }
    }

    /**
     * SparseCharArray with only 1 entry.  Incredibly, this takes up less space than Collections.singletonMap(),
     * because singletonMap relies on an inner singletonSet.
     * @author nolan
     *
     * @param <T>
     */
    private static class SingletonSparseCharArray<T> extends SparseCharArray<T> {

	private int singleKey;
	private T singleValue;

	SingletonSparseCharArray(int singleKey, T singleValue) {
	    this.singleKey = singleKey;
	    this.singleValue = singleValue;
	}

	@Override
	public Set<Entry<Character, T>> entrySet() {
	    return Collections.<Entry<Character, T>> singleton(new SCAEntry<T>(
		    Character.valueOf((char) singleKey), singleValue));
	}

	@Override
	public T get(Object key) {
	    if (key instanceof Character && ((Character) key).charValue() == singleKey) {
		return singleValue;
	    }
	    return null;
	}

    }

    /**
     * Basic Map.Entry object, nothing fancy.
     * @author nolan
     *
     * @param <T>
     */
    private static class SCAEntry<T> implements Entry<Character, T> {

	private Character key;
	private T value;

	private SCAEntry(Character key, T value) {
	    this.key = key;
	    this.value = value;
	}

	public Character getKey() {
	    return key;
	}

	public T getValue() {
	    return value;
	}

	public T setValue(T value) {
	    T old = this.value;
	    this.value = value;
	    return old;
	}

    }

}
