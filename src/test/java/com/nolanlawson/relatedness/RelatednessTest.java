package com.nolanlawson.relatedness;

import static com.nolanlawson.relatedness.BasicRelation.AuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.Child;
import static com.nolanlawson.relatedness.BasicRelation.Cousin;
import static com.nolanlawson.relatedness.BasicRelation.DoubleFirstCousin;
import static com.nolanlawson.relatedness.BasicRelation.Grandchild;
import static com.nolanlawson.relatedness.BasicRelation.Grandparent;
import static com.nolanlawson.relatedness.BasicRelation.GreatAuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandchild;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandparent;
import static com.nolanlawson.relatedness.BasicRelation.GreatNieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.HalfSibling;
import static com.nolanlawson.relatedness.BasicRelation.NieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.Parent;
import static com.nolanlawson.relatedness.BasicRelation.SecondCousin;
import static com.nolanlawson.relatedness.BasicRelation.Self;
import static com.nolanlawson.relatedness.BasicRelation.Sibling;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.parser.ParseError;
import com.nolanlawson.relatedness.parser.RelationParseResult;
import com.nolanlawson.relatedness.parser.RelativeNameParser;

/**
 * Tests taken from calculations on http://en.wikipedia.org/wiki/Coefficient_of_relationship.
 * @author nolan
 *
 */
public class RelatednessTest {

	@Test
	public void testBasicRelations() {
		
		testRelatedness(0, 1.0, Self);
		testRelatedness(1, 0.5, Parent);
		testRelatedness(1, 0.5, Child);
		testRelatedness(2, 0.5, Sibling);
		testRelatedness(2, 0.25, Grandparent);
		testRelatedness(2, 0.25, Grandchild);
		testRelatedness(2, 0.25, HalfSibling);
		testRelatedness(3, 0.25, AuntOrUncle);
		testRelatedness(3, 0.25, NieceOrNephew);
		testRelatedness(3, 0.125, GreatGrandparent);
		testRelatedness(3, 0.125, GreatGrandchild);
		testRelatedness(4, 0.125, Cousin);
		testRelatedness(6, 0.03125, SecondCousin);
		testRelatedness(4, 0.125, GreatAuntOrUncle);
		testRelatedness(4, 0.125, GreatNieceOrNephew);
		testRelatedness(4, 0.25, DoubleFirstCousin);
	}
	
	@Test
	public void testWeirdRelation() {
		// overheard on a message board: "her dad is my grandma's cousin"
		// had to draw this one out for it to make sense... his two great-great-grandparents are the same
		// as her two great-grandparents
		testRelatedness(7, 0.015625, new Relation(new CommonAncestor(4, 3), new CommonAncestor(4, 3)));
		
	}
	
	@Test
	public void testTwins() {
	    testRelatedness(2, 1.0, RelativeNameParser.parse("identical twin").getRelation());
	    testRelatedness(2, 0.5, RelativeNameParser.parse("fraternal twin").getRelation());
	    testRelatedness(3, 0.25, RelativeNameParser.parse("fraternal twin's daughter").getRelation());
	    testRelatedness(3, 0.5, RelativeNameParser.parse("identical twin's son").getRelation());
	}
	
	@Test
	public void testTwinAmbiguity() {
	    testAmbiguity("twin", "fraternal twin", "identical twin");
	    testAmbiguity("twin's daughter", "fraternal twin's daughter", "identical twin's daughter");
	    testAmbiguity("father's twin", "father's fraternal twin", "father's identical twin");
	}

	
	private void testAmbiguity(String input, String... resolutions) {
	    RelationParseResult result = RelativeNameParser.parse(input);
	    Assert.assertEquals(ParseError.Ambiguity, result.getParseError());
	    Assert.assertEquals(Arrays.asList(resolutions), result.getAmbiguityResolutions());
	}
	
	private void testRelatedness(double expectedDegree, double expectedCoefficient, Relation relation) {
		Relatedness relatedness = RelatednessCalculator.calculate(relation);
		Assert.assertEquals(expectedDegree, relatedness.getAverageDegree(), 0.0);
		Assert.assertEquals(expectedCoefficient, relatedness.getCoefficient(), 0.000001);
	}
	
	private void testRelatedness(double expectedDegree, double expectedCoefficient, BasicRelation basicRelation) {
		testRelatedness(expectedDegree, expectedCoefficient, basicRelation.getRelation());
	}
}
