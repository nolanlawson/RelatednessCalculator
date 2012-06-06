package com.nolanlawson.relatedness.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class WordWrapper {

	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	
	/**
	 * Attempt to replace spaces with newlines until all lines are less than the 
	 * desiredMaxLength.  Obviously not guaranteed to work perfectly, if tokens
	 * are longer than the desiredMaxLength.
	 * @param str
	 * @param desiredMaxLength
	 * @return
	 */
	public static String wordWrap(String str, int desiredMaxLength) {
		List<List<String>> lines = new ArrayList<List<String>>(
				Collections.singletonList(
						Arrays.asList(SPACE_PATTERN.split(str.trim()))));
		
		int index;
		while ((index = shouldSplit(lines, desiredMaxLength)) != -1) {
			lines.addAll(index, splitUp(lines.remove(index)));
		}
		
		StringBuilder result = new StringBuilder();
		int i = 0;
		for (List<String> line : lines) {
			if (i > 0) {
				result.append('\n');
			}
			result.append(join(' ', line));
			i++;
		}
		return result.toString();
	}

	private static List<List<String>> splitUp(List<String> list) {
		
		// find the best breakpoint given the desired max size
		int bestBreakpoint = -1;
		int lowestDeviation = Integer.MAX_VALUE;
		for (int i = 1; i < list.size(); i++) {
			int leftSize = expectedLength(list.subList(0, i));
			int rightSize = expectedLength(list.subList(i, list.size()));
			int deviation = Math.abs(leftSize - rightSize);
			if (deviation < lowestDeviation) {
				lowestDeviation = deviation;
				bestBreakpoint = i;
			}
		}
		
		List<String> left = list.subList(0, bestBreakpoint);
		List<String> right = list.subList(bestBreakpoint, list.size());
		
		@SuppressWarnings("unchecked")
		List<String>[] result = new List[]{left, right};
		return (List<List<String>>)Arrays.asList(result);
	}

	private static int shouldSplit(List<List<String>> lines, int desiredMaxLength) {
		for (int i = 0; i < lines.size(); i++) {
			List<String> line = lines.get(i);
			if (expectedLength(line) > desiredMaxLength) {
				if (line.size() > 1) { // can be split further
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Determine the length of the string, assuming the tokens are joined with a space.
	 * @param tokens
	 */
	private static int expectedLength(List<String> tokens) {
		int length = 0;
		for (String token : tokens) {
			length += token.length();
		}
		return length + (tokens.size() - 1);
	}
	
	private static CharSequence join(char delimiter, Collection<String> tokens) {
		StringBuilder stringBuilder = new StringBuilder();
		int i = 0;
		for (String token : tokens) {
			if (i > 0) {
				stringBuilder.append(delimiter);
			}
			stringBuilder.append(token);
			i++;
		}
		return stringBuilder;
	}
}
