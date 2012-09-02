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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.nolanlawson.relatedness.BasicRelation;

/**
 * TODO: convert to CFG and support more than just English
 * 
 * @author nolan
 * 
 */
public class ParseVocabulary {
    private static final List<String> GRANDPARENT_NAMES = Arrays.asList(
	    "grandpa", "grandma", "grandparent", "grammy", "grampy", "gramps",
	    "gramma", "grandfather", "grandmother", "granddad", "granddaddy",
	    "grand dad", "grand daddy", "granpa", "grampa");
    private static final List<String> GRANDCHILD_NAMES = Arrays.asList(
	    "grandchild", "grandson", "granddaughter");
    private static final List<String> AUNT_OR_UNCLE_NAMES = Arrays.asList(
	    "aunt", "uncle", "auntie", "unkie");

    public static final Set<BasicRelation> GREATABLE_RELATIONS = EnumSet.of(
	    Grandparent, Grandchild, AuntOrUncle, NieceOrNephew);
    public static final Set<BasicRelation> HALFABLE_RELATIONS = EnumSet.of(
	    Sibling, Cousin, AuntOrUncle, NieceOrNephew, SecondCousin, ThirdCousin, FourthCousin,
	    FifthCousin, SixthCousin, SeventhCousin, EighthCousin);

    public static final ImmutableMultimap<BasicRelation, String> VOCABULARY = new ImmutableMultimap.Builder<BasicRelation, String>()
	    .putAll(Parent, "parent", "father", "mother", "dad", "mom", "mum",
		    "pop", "daddy", "mommy", "mama", "mamma", "pops")
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

    public static final Pattern SPACES_AND_HYPHENS = Pattern
	    .compile("[\\-\\s]");

    public static final String GREAT = "great";
    public static final String HALF = "half";
    public static final String YOU = "You";
    public static final String YOUR = "Your";

    // once removed, three times removes, etc.
    public static final String REMOVED_PATTERN = 
	    "(?:once|twice|thrice|" +
	    "(?:one|two|three|four|five|six|seven|eight|nine|1|2|3|4|5|6|7|8|9) times) removed";
    
    // 's is the English possessive clitic
    public static final String POSSESSIVE = "'s";
    
    public static final String BASIC_RELATIVE_PATTERN = "((?:" + POSSESSIVE + "\\s+)?)" + // optional
									   // possessive
									   // "'s"
	    "((?:" + GREAT + "[ -]?)*)" + // greats
	    "((?:" + HALF + "[ -]?)?)" + // half
	    "(%s)" +
	    "([ ,]+" + REMOVED_PATTERN + ")?";

    private static final List<String> REMOVED_RESOLUTIONS = Arrays.asList("on", "tw", "thr", 
	    "four", "five", "six", "seven", "eight", "nine");
    
    public static final Pattern RELATIVE_PATTERN = createRelativePattern();

    /**
     * Given a string like "once removed", "3 times removed", return an int corresponding to the ordinal
     * @param removedString
     * @return
     */
    public static int determineTimesRemoved(String removedString) {
	for (int i = 0; i < REMOVED_RESOLUTIONS.size(); i++) {
	    if (removedString.contains(REMOVED_RESOLUTIONS.get(i)) 
		    || removedString.contains(Integer.toString(i + 1))) {
		return i + 1;
	    }
	}
	throw new RuntimeException("invalid 'removed' string: " + removedString); // should not happen
    }
    
    public static final List<String> ORDERED_ASCENDING_RELATIONS = fillList("parent", "grandparent", 9);
    public static final List<String> ORDERED_DESCENDING_RELATIONS = fillList("child", "grandchild", 9);
    
    
    /**
     * Create the Pattern to identify relative phrases, e.g. "cousin" or
     * "grandma's uncle";
     * 
     * @return
     */
    private static Pattern createRelativePattern() {

	// sort from longest to shortest to avoid greedy regexes taking the
	// shorter string
	String vocabTermsAsPattern = Joiner.on('|').join(
		orderByLength().reverse()
			.sortedCopy(
				Iterables.transform(VOCABULARY.values(),
					expandRegex())));

	String patternString = String.format(BASIC_RELATIVE_PATTERN,
		vocabTermsAsPattern.toString());

	return Pattern.compile(patternString, Pattern.CASE_INSENSITIVE
		| Pattern.DOTALL);
    }

    /**
     * generate a list of English relations up to the number of times, e.g. "parent", "grandparent", "great-grandparent", etc.
     * @param firstRelation
     * @param secondRelation
     * @param times
     * @return
     */
    private static List<String> fillList(String firstRelation, String secondRelation, int times) {
	List<String> result = new ArrayList<String>(Arrays.asList(firstRelation, secondRelation));
	for (int i = 0; i < times - 2; i ++) {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (int j = 0; j < i + 1 ; j++) {
		stringBuilder.append(GREAT).append(' ');
	    }
	    stringBuilder.append(secondRelation);
	    result.add(stringBuilder.toString());
	}
	return result;
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
     * spaces should be replaceable with "" or "-", e.g. in the case of
     * half-sister, half sister, great grandpa, great-grandpa, greatgrandpa,
     * etc.
     * 
     * @return
     */
    private static Function<String, String> expandRegex() {
	return new Function<String, String>() {

	    public String apply(String input) {
		return CharMatcher.is(' ').replaceFrom(input, "[ -]?");
	    }
	};
    }

}
