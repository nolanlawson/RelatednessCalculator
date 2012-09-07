package com.nolanlawson.relatedness.parser;

import static com.nolanlawson.relatedness.parser.ParseVocabulary.GREAT;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.GREATABLE_RELATIONS;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.HALFABLE_RELATIONS;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.RELATIVE_PATTERN;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.SPACES_AND_HYPHENS;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.VOCABULARY;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.YOU;
import static com.nolanlawson.relatedness.parser.ParseVocabulary.YOUR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.nolanlawson.relatedness.BasicRelation;
import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relation;
import com.nolanlawson.relatedness.RelationType;
import com.nolanlawson.relatedness.UnknownRelationException;
import com.nolanlawson.relatedness.graph.RelationGraph;

/**
 * Parses English names for relatives, e.g. "grandma" or "cousin" or
 * "grandma's cousin" or "dad's cousin's daughter."
 * 
 * @author nolan
 * 
 */
public class RelativeNameParser {

    private static final ImmutableMap<String, BasicRelation> REVERSE_VOCABULARY = createReverseVocabulary();

    /**
     * Same as the other method, except doesn't draw a graph.  (createGraph is false)
     * @param name
     * @return
     */
    public static RelationParseResult parse(String name) {
	return parse(name, false);
    }
    
    /**
     * Figure out the relation from a given string, e.g. the relation for
     * "dad's second cousin's daughter"
     * 
     * Returns a parse result along with potentially a graph, if you ask for it.
     * 
     * @param name the string to parse
     * @param createGraph whether or not to draw a graph
     * @return
     */
    public static RelationParseResult parse(String name, boolean createGraph) {

	RelationGraph graph = createGraph ? new RelationGraph() : null;

	name = name.trim();

	if (ParseVocabulary.STEP_PATTERN.matcher(name).find()) {
	    RelationParseResult result = new RelationParseResult();
	    result.setParseError(ParseError.StepRelation);
	    return result;
	}
	
	Matcher ambiguousTwinMatcher = ParseVocabulary.AMBIGUOUS_TWIN_PATTERN.matcher(name);
	if (ambiguousTwinMatcher.find()) {
	    return createAmbiguousTwinResult(ambiguousTwinMatcher, name);
	}
	
	Matcher matcher = RELATIVE_PATTERN.matcher(name);
	List<CommonAncestor> currentAncestors = null;
	Relation previousRelation = null;
	int lastIndex = 0;
	int currentRelatednessFactor = 1;
	while (matcher.find()) {

	    // test to make sure there weren't any characters we skipped over
	    CharSequence interimText = name.subSequence(lastIndex,
		    matcher.start());
	    if (containsRelevantCharacters(interimText)) {
		throw new UnknownRelationException(String.format(
			"Cannot parse '%s': unknown string '%s'", name,
			interimText));
	    }

	    // the possessive "'s" is disallowed in the first token and required
	    // afterwards
	    if (currentAncestors == null && matcher.group(1).length() > 0) {
		throw new UnknownRelationException(String.format(
			"Cannot parse '%s': string unacceptable: '%s'.", name,
			matcher.group(1)));
	    } else if (currentAncestors != null
		    && matcher.group(1).length() == 0) {
		throw new UnknownRelationException(String.format(
			"Cannot parse '%s': possessive \"'s\" is required.",
			name));
	    }

	    Relation relation = parseSingleRelation(matcher);

	    if (previousRelation != null
		    && !RelationType.isValidProgression(
			    RelationType.fromRelation(previousRelation),
			    RelationType.fromRelation(relation))) {
		throw new UnknownRelationException(String.format(
			"Cannot parse \"%s\" - this relationship makes no sense. "
				+ "Please think of a better way to phrase it.",
			name.subSequence(0, matcher.end())));
	    }
	    
	    List<String> ambiguityResolutions = determineAmbiguityResolutionsIfApplicable(relation, matcher, name);
	    if (ambiguityResolutions != null) { // ambiguity
		RelationParseResult result = new RelationParseResult();
		result.setParseError(ParseError.Ambiguity);
		result.setAmbiguityResolutions(ambiguityResolutions);
		return result;
	    }

	    if (createGraph) {
		// first relation will just be called "you", since that will
		// make sense to most people
		String nameOfFirst = currentAncestors == null ? YOU : YOUR
			+ " "
			+ name.substring(0, matcher.start()).trim()
				.toLowerCase();
		String nameOfSecond = YOUR + " "
			+ name.substring(0, matcher.end()).trim().toLowerCase();
		graph.addRelation(nameOfFirst, nameOfSecond, relation);
	    }

	    if (currentAncestors == null) { // no other relations, e.g. dad's
					    // sister's daughter's...
		currentAncestors = relation.getCommonAncestors();
	    } else { // 'add' the relations together
		currentAncestors = doRelativeAddition(currentAncestors,
			relation.getCommonAncestors());
	    }
	    currentRelatednessFactor *= relation.getRelatednessFactor();
	    lastIndex = matcher.end();
	    previousRelation = relation;
	}
	if (currentAncestors == null) {
	    throw new UnknownRelationException("unknown relation: " + name);
	} else if (containsRelevantCharacters(name.subSequence(lastIndex,
		name.length()))) { // trailing text was not used
	    throw new UnknownRelationException(String.format(
		    "Cannot parse '%s': unknown string '%s'", name,
		    name.subSequence(lastIndex, name.length())));
	}

	RelationParseResult result = new RelationParseResult();
	result.setRelation(new Relation(currentRelatednessFactor, currentAncestors));
	result.setGraph(graph);
	return result;
    }

    private static RelationParseResult createAmbiguousTwinResult(
	    final Matcher matcher, final String fullString) {
	
	// return the string with "fraternal twin" or "identical twin" replacing "twin"
	List<String> resolutions = Lists.newArrayList(Iterables.transform(ParseVocabulary.AMBIGUOUS_TWIN_RESOLUTIONS, 
		new Function<String, String>(){

		    public String apply(String input) {
			return new StringBuilder(fullString)
				.replace(matcher.start(), matcher.end(), input)
				.toString();
		    }}
		
		));
	
	RelationParseResult result = new RelationParseResult();
	result.setParseError(ParseError.Ambiguity);
	result.setAmbiguityResolutions(resolutions);
	return result;
    }

    /**
     * handle cases like "second cousin, twice removed" or "cousin, once removed".  Offer disambiguations to the user.
     * @param relation
     * @param matcher
     * @return
     */
    private static List<String> determineAmbiguityResolutionsIfApplicable(
	    Relation relation, Matcher matcher, String fullString) {
	String removedString = matcher.group(7);
	if (removedString == null || removedString.length() == 0) {
	    return null; //  no ambiguity
	}
	
	int timesRemoved = ParseVocabulary.determineTimesRemoved(removedString);
	
	String ascendingRelation = ParseVocabulary.ORDERED_ASCENDING_RELATIONS.get(timesRemoved - 1);
	String descendingRelation = ParseVocabulary.ORDERED_DESCENDING_RELATIONS.get(timesRemoved - 1);
	
	// return the string either followed by "'s <X>child" or preceded by "<X>parent's"
	StringBuilder first = new StringBuilder(fullString);
	first.replace(matcher.start(7), matcher.end(7), "");
	first.insert(matcher.start(2), ascendingRelation + ParseVocabulary.POSSESSIVE + " ");
	
	StringBuilder second = new StringBuilder(fullString);
	second.replace(matcher.start(7), matcher.end(7), ParseVocabulary.POSSESSIVE + " " + descendingRelation);
	
	return Arrays.asList(first.toString(), second.toString());
    }

    private static boolean containsRelevantCharacters(CharSequence interimText) {

	return CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAnyOf(interimText);
    }

    private static Relation parseSingleRelation(final Matcher matcher) {
	
	String greatsAndHalfs = Joiner.on("").join(Iterables.transform(
		Arrays.asList(2,3,4,5), new Function<Integer,String>(){

	    public String apply(Integer input) {
		return Strings.nullToEmpty(matcher.group(input));
	    }
	}));
	
	int numGreats = countGreats(greatsAndHalfs.toLowerCase());
	boolean isHalf = greatsAndHalfs.toLowerCase().contains(ParseVocabulary.HALF);
	
	BasicRelation basicRelation = REVERSE_VOCABULARY
		.get(collapseName(matcher.group(6)));

	if (numGreats > 0 && !GREATABLE_RELATIONS.contains(basicRelation)) {
	    // not an aunt, uncle, grandparent, grandkid, etc.
	    throw new UnknownRelationException("impossible relation: "
		    + matcher.group());
	} else if (isHalf && !HALFABLE_RELATIONS.contains(basicRelation)) {
	    throw new UnknownRelationException("impossible relation: "
		    + matcher.group());
	}

	Relation relation = (Relation) basicRelation.getRelation().clone();

	if (numGreats > 0) {
	    applyGreats(relation, numGreats);
	}
	if (isHalf) {
	    applyHalf(relation);
	}

	return relation;

    }

    /**
     * All this requires is deleting one of the common ancestors, e.g. in the
     * case of half-siblings, it's one of the parents.
     * 
     * @param relation
     */
    private static void applyHalf(Relation relation) {
	relation.getCommonAncestors().remove(0);
    }

    /**
     * Apply any number of "greats" to a relation.
     * 
     * @param relation
     * @param numGreats
     */
    private static void applyGreats(Relation relation, int numGreats) {
	// 'great' relationships are applied by simply increasing the max
	// distance of the common ancestors. Trust me,
	// the math works out.
	for (CommonAncestor commonAncestor : relation.getCommonAncestors()) {
	    if (commonAncestor.getDistanceFromFirst() > commonAncestor
		    .getDistanceFromSecond()) {
		commonAncestor.setDistanceFromFirst(commonAncestor
			.getDistanceFromFirst() + numGreats);
	    } else {
		commonAncestor.setDistanceFromSecond(commonAncestor
			.getDistanceFromSecond() + numGreats);
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

    /**
     * This is the math that determines the right common ancestors for
     * expressions like "uncle's cousin". It only seems to work when you don't
     * have two common ancestors in both relations, and if you don't do
     * something self-referential like "father's son's father's son's father..."
     * 
     * @param currentAncestors
     * @param commonAncestors
     */
    private static List<CommonAncestor> doRelativeAddition(
	    List<CommonAncestor> first, List<CommonAncestor> second) {

	if (first.size() > 1 && second.size() > 1) {
	    throw new UnknownRelationException(
		    "Cannot parse relation with multiple common ancestors in each group");
	}

	List<CommonAncestor> result = new ArrayList<CommonAncestor>();

	for (CommonAncestor ancestor1 : first) {
	    for (CommonAncestor ancestor2 : second) {
		// add their distances together
		CommonAncestor newAncestor = new CommonAncestor(
			ancestor1.getDistanceFromFirst()
				+ ancestor2.getDistanceFromFirst(),
			ancestor1.getDistanceFromSecond()
				+ ancestor2.getDistanceFromSecond());
		result.add(newAncestor);
	    }
	}

	return result;
    }

    private static ImmutableMap<String, BasicRelation> createReverseVocabulary() {
	Multimap<String, BasicRelation> multimap = Multimaps.invertFrom(
		VOCABULARY, ArrayListMultimap.<String, BasicRelation> create());

	Map<String, BasicRelation> result = new HashMap<String, BasicRelation>();

	for (Entry<String, BasicRelation> entry : multimap.entries()) {
	    result.put(collapseName(entry.getKey()), entry.getValue());
	}
	return ImmutableMap.copyOf(result);
    }

    /**
     * spaces and hyphens should collapse to "", e.g. in the case of
     * half-sister, half sister, great grandpa, great-grandpa, greatgrandpa,
     * etc.
     * 
     * @return
     */
    private static String collapseName(String input) {
	return SPACES_AND_HYPHENS.matcher(input).replaceAll("").trim()
		.toLowerCase();
    }

}
