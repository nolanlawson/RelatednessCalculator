package com.nolanlawson.relatedness.parser;

import static com.nolanlawson.relatedness.BasicRelation.AuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.Child;
import static com.nolanlawson.relatedness.BasicRelation.Cousin;
import static com.nolanlawson.relatedness.BasicRelation.DoubleFirstCousin;
import static com.nolanlawson.relatedness.BasicRelation.EighthCousin;
import static com.nolanlawson.relatedness.BasicRelation.FifthCousin;
import static com.nolanlawson.relatedness.BasicRelation.FourthCousin;
import static com.nolanlawson.relatedness.BasicRelation.Grandchild;
import static com.nolanlawson.relatedness.BasicRelation.Grandparent;
import static com.nolanlawson.relatedness.BasicRelation.NieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.Parent;
import static com.nolanlawson.relatedness.BasicRelation.SecondCousin;
import static com.nolanlawson.relatedness.BasicRelation.SeventhCousin;
import static com.nolanlawson.relatedness.BasicRelation.Sibling;
import static com.nolanlawson.relatedness.BasicRelation.SixthCousin;
import static com.nolanlawson.relatedness.BasicRelation.ThirdCousin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.nolanlawson.relatedness.UnknownRelationException;
import com.nolanlawson.relatedness.graph.RelationGraph;
/**
 * Parses English names for relatives, e.g. "grandma" or "cousin" or "grandma's cousin" or "dad's cousin's daughter."
 * @author nolan
 *
 */
public class RelativeNameParser {

	private static final List<String> GRANDPARENT_NAMES = Arrays.asList(
			"grandpa", "grandma", "grandparent", "grammy", "grampy", "gramps", "gramma", "grandfather", "grandmother",
			"granddad", "granddaddy", "grand dad", "grand daddy");
	private static final List<String> GRANDCHILD_NAMES = Arrays.asList(
			"grandchild", "grandson", "granddaughter");
	private static final List<String> AUNT_OR_UNCLE_NAMES = Arrays.asList(
			"aunt", "uncle", "auntie", "unkie");
	
	private static final Set<BasicRelation> GREATABLE_RELATIONS = EnumSet.of(Grandparent, Grandchild, AuntOrUncle, NieceOrNephew);
	private static final Set<BasicRelation> HALFABLE_RELATIONS = EnumSet.of(Sibling, Cousin, AuntOrUncle, NieceOrNephew);
	
	private static final ImmutableMultimap<BasicRelation, String> VOCABULARY = 
			new ImmutableMultimap.Builder<BasicRelation, String>()
			.putAll(Parent, "parent", "father", "mother", "dad", "mom", "mum", "pop", 
					"daddy", "mommy", "mama", "mamma", "pops")
			.putAll(Child, "son", "daughter", "child", "kid")
			.putAll(Sibling, "sibling", "brother", "sister", "sis", "bro")
			.putAll(Cousin, "cousin", "first cousin", "1st cousin")
			.putAll(SecondCousin, "second cousin", "2nd cousin")
			.putAll(ThirdCousin, "third cousin", "3rd cousin")
			.putAll(FourthCousin, "fourth cousin", "4th cousin")
			.putAll(FifthCousin, "fifth cousin", "5th cousin")
			.putAll(SixthCousin, "sixth cousin", "6th cousin")
			.putAll(SeventhCousin, "seventh cousin", "7th cousin")
			.putAll(EighthCousin, "eighth cousin", "8th cousin")
			.putAll(Grandparent, GRANDPARENT_NAMES)
			.putAll(Grandchild, GRANDCHILD_NAMES)
			.putAll(AuntOrUncle, AUNT_OR_UNCLE_NAMES)
			.putAll(NieceOrNephew, "niece", "nephew")
			.putAll(DoubleFirstCousin, "double cousin", "double first cousin")
			.build();
	
	private static final ImmutableMap<String, BasicRelation> REVERSE_VOCABULARY = createReverseVocabulary();
	
	private static final String GREAT = "great";
	private static final String HALF = "half";
	private static final String YOU = "You";
	private static final String YOUR = "Your";
	
	// 's is the English possessive clitic
	private static final String BASIC_RELATIVE_PATTERN = 
			"((?:'s\\s+)?)" + // optional possessive "'s"
			"((?:" + GREAT + "[ -]?)*)" + // greats
			"((?:" + HALF + "[ -]?)?)" + // half
			"(%s)";
	
	
	private static final Pattern RELATIVE_PATTERN = createRelativePattern();
	
	
	/**
	 * Create a graph for the given relation
	 * @param name
	 * @return
	 */		
	public static RelationGraph parseGraph(String name) {
		return parse(name, true).getGraph();
	}
	
	/**
	 * Figure out the relation from a given string, e.g. the relation for "dad's second cousin's daughter"
	 * @param name
	 * @return
	 */
	public static Relation parse(String name) {
		return parse(name, false).getRelation();
	}

	private static RelationAndGraph parse(String name, boolean createGraph) {
		
		RelationGraph graph = createGraph ? new RelationGraph() : null;
		
		name = name.trim();
		
		Matcher matcher = RELATIVE_PATTERN.matcher(name);
		List<CommonAncestor> currentAncestors = null;
		int lastIndex = 0;
		while (matcher.find()) {
			
			// test to make sure there weren't any characters we skipped over
			CharSequence interimText = name.subSequence(lastIndex, matcher.start());
			if (containsRelevantCharacters(interimText)) {
				throw new UnknownRelationException(String.format(
						"Cannot parse '%s': unknown string '%s'", name, interimText));
			}
			
			// the possessive "'s" is disallowed in the first token and required afterwards
			if (currentAncestors == null && matcher.group(1).length() > 0) {
				throw new UnknownRelationException(String.format(
						"Cannot parse '%s': string unacceptable: '%s'.", name, matcher.group(1)));
			} else if (currentAncestors != null && matcher.group(1).length() == 0) {
				throw new UnknownRelationException(String.format(
						"Cannot parse '%s': possessive \"'s\" is required.", name));
			}
			
			Relation relation = parseSingleRelation(matcher);

			
			if (createGraph) {
				// first relation will just be called "you", since that will make sense to most people
				String nameOfFirst = currentAncestors == null 
						? YOU 
						: YOUR + " " + name.substring(0, matcher.start()).trim().toLowerCase();
				String nameOfSecond = YOUR + " " + name.substring(0, matcher.end()).trim().toLowerCase();
				graph.addRelation(nameOfFirst, nameOfSecond, relation);
			}
			
			
			if (currentAncestors == null) { // no other relations, e.g. dad's sister's daughter's...
				currentAncestors = relation.getCommonAncestors();
			} else { // 'add' the relations together
				currentAncestors = doRelativeAddition(currentAncestors, relation.getCommonAncestors());
			}
			
			lastIndex = matcher.end();
		}
		if (currentAncestors == null) {
			throw new UnknownRelationException("unknown relation: " + name);
		} else if (containsRelevantCharacters(name.subSequence(lastIndex, name.length()))) { // trailing text was not used
			throw new UnknownRelationException(String.format(
					"Cannot parse '%s': unknown string '%s'", name, name.subSequence(lastIndex, name.length())));
		}
		
		return new RelationAndGraph(new Relation(currentAncestors), graph);
	}

	private static boolean containsRelevantCharacters(CharSequence interimText) {
		// wish I could use Guava's CharMatcher here... but I'm trying to keep this jar light.  Sigh.
		for (int i = 0; i < interimText.length(); i++) {
			if (Character.isLetterOrDigit(interimText.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private static Relation parseSingleRelation(Matcher matcher) {
		int numGreats = countGreats(matcher.group(2).toLowerCase());
		boolean isHalf = matcher.group(3).length() > 0;
		BasicRelation basicRelation = REVERSE_VOCABULARY.get(matcher.group(4).toLowerCase());
		
		if (numGreats > 0 && !GREATABLE_RELATIONS.contains(basicRelation)) {
			// not an aunt, uncle, grandparent, grandkid, etc.
			throw new UnknownRelationException("impossible relation: " + matcher.group());
		} else if (isHalf && !HALFABLE_RELATIONS.contains(basicRelation)) {
			throw new UnknownRelationException("impossible relation: " + matcher.group());
		}
		
		Relation relation = (Relation)basicRelation.getRelation().clone();
		
		if (numGreats > 0) {
			applyGreats(relation, numGreats);
		}
		if (isHalf) {
			applyHalf(relation);
		}
		
		return relation;
		
	}

	/**
	 * All this requires is deleting one of the common ancestors, e.g. in the case of half-siblings,
	 * it's one of the parents.
	 * @param relation
	 */
	private static void applyHalf(Relation relation) {
		relation.getCommonAncestors().remove(0);
	}

	/**
	 * Apply any number of "greats" to a relation.
	 * @param relation
	 * @param numGreats
	 */
	private static void applyGreats(Relation relation, int numGreats) {
		// 'great' relationships are applied by simply increasing the max distance of the common ancestors.  Trust me,
		// the math works out.
		for (CommonAncestor commonAncestor : relation.getCommonAncestors()) {
			if (commonAncestor.getDistanceFromFirst() > commonAncestor.getDistanceFromSecond()) {
				commonAncestor.setDistanceFromFirst(commonAncestor.getDistanceFromFirst() + numGreats);
			} else {
				commonAncestor.setDistanceFromSecond(commonAncestor.getDistanceFromSecond() + numGreats);
			}
		}
	}

	private static int countGreats(String group) {
		int count = 0, idx = 0;
		while ((idx = group.indexOf(GREAT, idx)) != -1) {
			idx += GREAT.length();
			count++;
		}
		return count;
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
			throw new UnknownRelationException("Cannot parse relation with multiple common ancestors in each group");
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
	
	private static class RelationAndGraph {
		private Relation relation;
		private RelationGraph graph;
		private RelationAndGraph(Relation relation, RelationGraph graph) {
			this.relation = relation;
			this.graph = graph;
		}
		public Relation getRelation() {
			return relation;
		}
		public RelationGraph getGraph() {
			return graph;
		}
		
		
	}
}
