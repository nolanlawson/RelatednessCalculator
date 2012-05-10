package com.nolanlawson.relatedness.parser;

import static com.nolanlawson.relatedness.BasicRelation.AuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.Child;
import static com.nolanlawson.relatedness.BasicRelation.Cousin;
import static com.nolanlawson.relatedness.BasicRelation.DoubleFirstCousin;
import static com.nolanlawson.relatedness.BasicRelation.Grandchild;
import static com.nolanlawson.relatedness.BasicRelation.Grandparent;
import static com.nolanlawson.relatedness.BasicRelation.GreatAuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandchild;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandparent;
import static com.nolanlawson.relatedness.BasicRelation.HalfSibling;
import static com.nolanlawson.relatedness.BasicRelation.NieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.Parent;
import static com.nolanlawson.relatedness.BasicRelation.SecondCousin;
import static com.nolanlawson.relatedness.BasicRelation.Sibling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.nolanlawson.relatedness.BasicRelation;
import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relation;
/**
 * Parses English names for relatives, e.g. "grandma" or "cousin" or "grandma's cousin" or "dad's cousin's daughter."
 * @author nolan
 *
 */
public class RelativeNameParser {

	private static final List<String> GRANDPARENT_NAMES = Arrays.asList(
			"grandpa", "grandma", "grandparent", "grammy", "grampy", "gramps", "gramma");
	private static final List<String> GRANDCHILD_NAMES = Arrays.asList(
			"grandchild", "grandson", "granddaughter");
	private static final List<String> AUNT_OR_UNCLE_NAMES = Arrays.asList(
			"aunt", "uncle", "auntie", "unkie");
	
	
	private static final ImmutableMultimap<BasicRelation, String> VOCABULARY = 
			new ImmutableMultimap.Builder<BasicRelation, String>()
			.putAll(Parent, "parent", "father", "mother", "dad", "mom", "mum", "pop", 
					"daddy", "mommy", "mama", "mamma", "pops")
			.putAll(Child, "son", "daughter", "child", "kid")
			.putAll(Sibling, "sibling", "brother", "sister", "sis", "bro")
			.putAll(HalfSibling, "half brother", "half sister")
			.putAll(Cousin, "cousin", "first cousin")
			.putAll(Grandparent, GRANDPARENT_NAMES)
			.putAll(Grandchild, GRANDCHILD_NAMES)
			.putAll(AuntOrUncle, AUNT_OR_UNCLE_NAMES)
			.putAll(NieceOrNephew, "niece", "nephew")
			.putAll(SecondCousin, "second cousin", "2nd cousin")
			.putAll(DoubleFirstCousin, "double cousin", "double first cousin")
			.putAll(GreatAuntOrUncle, Iterables.transform(AUNT_OR_UNCLE_NAMES, addGreat()))
			.putAll(GreatGrandparent, Iterables.transform(GRANDPARENT_NAMES, addGreat()))
			.putAll(GreatGrandchild, Iterables.transform(GRANDCHILD_NAMES, addGreat()))
			.build();
	
	private static final ImmutableMap<String, BasicRelation> REVERSE_VOCABULARY = createReverseVocabulary();
	
	// 's is the English possessive clitic
	private static final String BASIC_RELATIVE_PATTERN = "%1$s";
	
	private static final Pattern RELATIVE_PATTERN = createRelativePattern();
	
	/**
	 * Figure out the relation from a given string, e.g. the relation for "dad's second cousin's daughter"
	 * @param name
	 * @return
	 */
	public static Relation parse(String name) {
		Matcher matcher = RELATIVE_PATTERN.matcher(name.trim());
		List<CommonAncestor> currentAncestors = null;
		while (matcher.find()) {
			Relation relation = REVERSE_VOCABULARY.get(matcher.group().toLowerCase()).getRelation();
			if (currentAncestors == null) {
				currentAncestors = ((Relation)relation.clone()).getCommonAncestors();
			} else {
				currentAncestors = doRelativeAddition(currentAncestors, relation.getCommonAncestors());
			}
		}
		if (currentAncestors == null) {
			throw new IllegalArgumentException("unknown relation: " + name);
		}
		
		return new Relation(currentAncestors);
	}

	private static ImmutableMap<String, BasicRelation> createReverseVocabulary() {
		Multimap<String, BasicRelation> multimap = Multimaps.invertFrom(
				VOCABULARY, ArrayListMultimap.<String, BasicRelation>create());
		
		Map<String, BasicRelation> result = new HashMap<String, BasicRelation>();
		
		for (Entry<String, BasicRelation> entry : multimap.entries()) {
			for (String match : expandMatches(entry.getKey())) {
				result.put(match, entry.getValue());
			}
		}
		return ImmutableMap.copyOf(result);
	}

	/**
	 * This is the math that determines the right common ancestors for expressions like "uncle's cousin".  It only
	 * seems to work when you don't have two common ancestors in both relations, and if you don't do something self-referential
	 * like "father's son's father's son's father..."
	 * @param currentAncestors
	 * @param commonAncestors
	 */
	private static List<CommonAncestor> doRelativeAddition(List<CommonAncestor> first,
			List<CommonAncestor> second) {
		
		if (first.size() > 1 && second.size() > 1) {
			throw new IllegalArgumentException("Cannot parse relation with multiple common ancestors in each group");
		}
		
		List<CommonAncestor> result = new ArrayList<CommonAncestor>();
		
		for (CommonAncestor ancestor1 : first) {
			for (CommonAncestor ancestor2 : second) {
				// add their distances together
				CommonAncestor newAncestor = new CommonAncestor(
						ancestor1.getDistanceFromFirst() + ancestor2.getDistanceFromFirst(),
						ancestor1.getDistanceFromSecond() + ancestor2.getDistanceFromSecond()
						);
				result.add(newAncestor);
			}
		}
		
		return result;
	}

	/**
	 * Create the Pattern to identify relative phrases, e.g. "cousin" or "grandma's uncle";
	 * @return
	 */
	private static Pattern createRelativePattern() {
		
		// sort from longest to shortest to avoid greedy regexes taking the shorter string
		String vocabTermsAsPattern = Joiner.on('|').join(
				orderByLength().reverse().sortedCopy(
				Iterables.transform(VOCABULARY.values(), expandRegex())));
		
		String patternString = String.format(BASIC_RELATIVE_PATTERN, vocabTermsAsPattern.toString());
		
		return Pattern.compile(patternString, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
	}
	
	private static Ordering<String> orderByLength() {
		return new Ordering<String>() {

			@Override
			public int compare(String left, String right) {
				return left.length() - right.length();
			}
			
		};
	}
	
	/**
	 * spaces should be replaceable with "" or "-", e.g. in the case of half-sister, half sister,
	 * great grandpa, great-grandpa, greatgrandpa, etc.
	 * @return
	 */
	private static Function<String, String> expandRegex() {
		return new Function<String, String>() {

			public String apply(String input) {
				return CharMatcher.is(' ').replaceFrom(input, "[ -]?");
			}
		};
	}
	
	/**
	 * spaces should be replaceable with "" or "-", e.g. in the case of half-sister, half sister,
	 * great grandpa, great-grandpa, greatgrandpa, etc.
	 * @return
	 */
	private static List<String> expandMatches(String input) {
		// expand ' ' into '-' and ""
		return new ArrayList<String>(new HashSet<String>(Arrays.asList(
				input,
				CharMatcher.is(' ').replaceFrom(input, '-'),
				CharMatcher.is(' ').replaceFrom(input, "")
				)));
	}
	
	private static Function<String, String> addGreat() {
		return new Function<String, String>() {

			public String apply(String input) {
				return "great " + input;
			}
		};
	}
}
