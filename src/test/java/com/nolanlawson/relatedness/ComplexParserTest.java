package com.nolanlawson.relatedness;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.parser.ParseError;
import com.nolanlawson.relatedness.parser.RelationParseResult;
import com.nolanlawson.relatedness.parser.RelativeNameParser;

public class ComplexParserTest {

    @Test
    public void testNTimesRemoved() {
	testAmbiguousParse("cousin once removed", "cousin's child", "parent's cousin");
	testAmbiguousParse("second cousin, 2 times removed", "second cousin's grandchild", "grandparent's second cousin");
	testAmbiguousParse("second cousin once removed's daughter", "second cousin's child's daughter", 
		"parent's second cousin's daughter");
	
	testAmbiguousParse("father's second cousin twice removed", "father's grandparent's second cousin", 
		"father's second cousin's grandchild");
    }
    
    private void testAmbiguousParse(String ambiguousString, String... possibleResolutions) {
	RelationParseResult result = RelativeNameParser.parse(ambiguousString);
	
	Assert.assertEquals(ParseError.Ambiguity, result.getParseError());
	
	Assert.assertEquals(
		new HashSet<String>(Arrays.asList(possibleResolutions)), 
		new HashSet<String>(result.getAmbiguityResolutions()));
    }
}
