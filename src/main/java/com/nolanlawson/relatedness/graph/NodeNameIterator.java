package com.nolanlawson.relatedness.graph;

import java.util.Iterator;

/**
 * Stupid iterator I have to write because DOT is very picky about the possible names for nodes.
 * 
 * Iterates through all lowercase alphabetic names in base-26 format, e.g. a, b, c, d, ..., ba, bb, bc, bd, etc.
 * @author nolan
 *
 */
public class NodeNameIterator implements Iterator<String> {

	private static final int LOW = 97; // 'a'
	private static final int HIGH = 123; // 'z' + 1
	
	private int index = 0;

	public boolean hasNext() {
		return true; // always has more
	}

	public String next() {
		
		// convert to base (123 - 97);
		
		int base = (HIGH - LOW);
		int value = index;
		StringBuilder result = new StringBuilder();
		while (true) {
			int remainder = value % base;
			result.append((char)(LOW + remainder));
			value /= base;
			if (value == 0) {
				break;
			}
		}
		
		index++;
		return result.reverse().toString();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
