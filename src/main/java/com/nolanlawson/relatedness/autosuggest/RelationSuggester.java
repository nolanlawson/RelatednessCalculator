package com.nolanlawson.relatedness.autosuggest;
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
import static com.nolanlawson.relatedness.BasicRelation.SixthCousin;
import static com.nolanlawson.relatedness.BasicRelation.ThirdCousin;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.nolanlawson.relatedness.BasicRelation;
import com.nolanlawson.relatedness.UnknownRelationException;
import com.nolanlawson.relatedness.parser.ParseVocabulary;
import com.nolanlawson.relatedness.parser.RelationParseResult;
import com.nolanlawson.relatedness.parser.RelativeNameParser;

/**
 * Used for autosuggesting relations.
 * 
 * This is really some of the worst code I've ever written.  I wish I had started from scratch with a Finite
 * State Transducer or something.  This is just ugly as hell.  But hey, at least it works.
 * @author nolan
 *
 */
public class RelationSuggester {

    private static final int NUM_GREATS_TO_SUGGEST = 5;
    
    // determines how many names to use for each basic relation.  some of them have more common names
    // than others, so prioritize these
    private static final Set<BasicRelation> THREE_NAME_RELATIONS = EnumSet.of(Parent, Child,
	    Grandparent, Grandchild);
    private static final Set<BasicRelation> TWO_NAME_RELATIONS = EnumSet.of(
	    AuntOrUncle, NieceOrNephew);
    
    private static final Set<BasicRelation> COUSINS = EnumSet.of(Cousin, SecondCousin, ThirdCousin, FourthCousin,
	    FifthCousin, SixthCousin, SeventhCousin, EighthCousin);
    
    
    // relations to de-prioritize in the suggestions results because they're uncommon, also make sure
    // that e.g. third cousins are suggested before fourth cousins, etc.
    private static final Map<BasicRelation, Double> DEPRIORITIZED_RELATIONS = 
	    new ImmutableMap.Builder<BasicRelation, Double>()
	    .put(DoubleFirstCousin, 1.0 /2)
	    .put(ThirdCousin, 1.0 / 2)
	    .put(FourthCousin, 1.0 / 3)
	    .put(FifthCousin, 1.0 / 4)
	    .put(SixthCousin, 1.0 / 5)
	    .put(SeventhCousin, 1.0 / 6)
	    .put(EighthCousin, 1.0 / 7)
	    .build();
    
    private Trie<WeightedRelation> trie;
    
    public RelationSuggester() {
	initTrie();
    }
    
    private void initTrie() {
	trie = Trie.newTrie();
	List<WeightedRelation> suggestions = generateSuggestions();
	for (WeightedRelation suggestion : suggestions) {
	    trie.put(suggestion.getRelation(), suggestion);
	}
    }
    
    private List<WeightedRelation> generateSuggestions() {
	List<WeightedRelation> result = Lists.newArrayList();
	for (Entry<BasicRelation, Collection<String>> entry : ParseVocabulary.VOCABULARY.asMap().entrySet()) {
	    BasicRelation basicRelation = entry.getKey();
	    List<String> names = Lists.newArrayList(entry.getValue());
	    
	    // weight the more common relations first
	    double initialWeight = DEPRIORITIZED_RELATIONS.containsKey(basicRelation) 
		    ? DEPRIORITIZED_RELATIONS.get(basicRelation) 
		    : WeightedRelation.FIRST_PRIORITY;
	    
	    // by default, just prioritize the first name
	    int numNamesToPrioritize = 1;
	    if (THREE_NAME_RELATIONS.contains(basicRelation)) {
		numNamesToPrioritize = 3;
	    } else if (TWO_NAME_RELATIONS.contains(basicRelation)) {
		numNamesToPrioritize = 2;
	    }
	    
	    Iterable<String> namesToPrioritize = Iterables.limit(names, numNamesToPrioritize);
	    
	    for (int i = 0 ; i < names.size(); i++) {
		String name = names.get(i);
		double reducedWeight = i < numNamesToPrioritize ? initialWeight : initialWeight / 2;
		result.add(new WeightedRelation(name, reducedWeight));
	    }
	    
	    if (ParseVocabulary.GREATABLE_RELATIONS.contains(basicRelation)) {
		// add "greats" as well, but prioritize only the first few
		double greatWeight = initialWeight;
		for (int i = 0; i < NUM_GREATS_TO_SUGGEST; i++) {

		    for (String nameToUse : namesToPrioritize) {
			String greats = Strings.repeat(ParseVocabulary.GREAT + "-", i + 1);
			result.add(new WeightedRelation(greats + nameToUse, greatWeight));
		    }
		    greatWeight /= 2;
		}
	    }
	    
	    if (ParseVocabulary.HALFABLE_RELATIONS.contains(basicRelation)) {
		// add "half"s as well
		for (String nameToUse : namesToPrioritize) {
		    result.add(new WeightedRelation(ParseVocabulary.HALF + "-" + nameToUse, initialWeight));
		}
	    }
	    
	    // I'm also going to allow "X removed" for all cousins, without the comma
	    if (COUSINS.contains(basicRelation)) {
		double removedWeight = initialWeight;
		for (String timesRemoved : ParseVocabulary.REMOVED_STRINGS_TO_SUGGEST) {
		    removedWeight /= 4;
		    for (String name : names) {
			String removedName = name + " " + timesRemoved + " " + ParseVocabulary.REMOVED;
			result.add(new WeightedRelation(removedName, removedWeight));
		    }
		}
	    }
	}
	return result;
    }

    public List<String> suggest(String input, int limit) {
	// sort by weight, then the relation string, then limit the list and return it
	List<WeightedRelation> result = Lists.newArrayList(trie.getAll(input.toLowerCase()));
	
	if (Iterables.contains(Iterables.transform(result, WeightedRelation.getRelationFunction),input.toLowerCase())) {
	    // there's very few results and it contains the same name as the input (e.g. "grandpa"), so expand it with
	    // possible additional relations, such as "grandpa's cousin" or "grandpa's second cousin"
	    result.addAll(expandWithCompoundRelations(0, input, "", limit));
	}
	// also account for cases where the user has just typed "'", "'s", or "'s "
	String fullPossessive = ParseVocabulary.POSSESSIVE + " ";
	for (int i = 0; i < fullPossessive.length(); i++) {
	    if (input.endsWith(fullPossessive.substring(0, fullPossessive.length() - i))) {
		result.addAll(expandWithCompoundRelations(fullPossessive.length() - i, input, "", limit));
	    }
	}
	// next, account for cases where the user typed 's plus something else
	int lastIndexOfPossessive = input.lastIndexOf(fullPossessive);
	int endOfLastPossessive = lastIndexOfPossessive + fullPossessive.length();
	if (lastIndexOfPossessive != -1 && endOfLastPossessive < input.length() - 1) {
	    String postPossessiveString = input.substring(endOfLastPossessive);
	    result.addAll(expandWithCompoundRelations(0, input.substring(0, lastIndexOfPossessive), 
		    postPossessiveString, limit));
	}
	
	// sort, limit, and transform
	
	return Lists.newArrayList(
		Iterables.transform( 
			Iterables.limit(Ordering.natural().sortedCopy(result),
			limit),
		WeightedRelation.getRelationFunction));
    }

    private List<WeightedRelation> expandWithCompoundRelations(final int possessiveStringIndex, final String input, 
	    String searchString, int limit) {
	
	final String fullPossessive = ParseVocabulary.POSSESSIVE + " ";
	
	// have to check and make sure we don't add nonsensical relations, like "cousin's uncle"
	List<WeightedRelation> allPossibleWeightedRelations = trie.getAll(searchString);
	
	// order them first, so we don't waste too much time checking them all
	List<WeightedRelation> sortedPossibleRelations = Lists.newArrayList(
		Iterables.transform(
			Ordering.natural().sortedCopy(allPossibleWeightedRelations), 
		new Function<WeightedRelation, WeightedRelation>(){

		    public WeightedRelation apply(WeightedRelation weightedRelation) {
			String relationName = input + fullPossessive.substring(possessiveStringIndex) + weightedRelation.getRelation();
			double weight = weightedRelation.getWeight() / 2; // cut it in half for compound relations to de-prioritize them
			return new WeightedRelation(relationName, weight);
		    }
		}));
	
	List<WeightedRelation> result = Lists.newArrayList();
	// check one-by-one that the relation makes sense
	for (WeightedRelation possibleRelation : sortedPossibleRelations) {
	    if (result.size() >= limit) {
		break;
	    } else {
		try {
		    RelationParseResult parseResult = RelativeNameParser.parse(possibleRelation.getRelation());
		    if (parseResult.getParseError() == null) { // no error, so add
			result.add(possibleRelation);
		    }
		} catch (UnknownRelationException ignore) {}
	    }
	}
	return result;
    }
}
