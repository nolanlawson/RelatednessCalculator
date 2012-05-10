package com.nolanlawson.relatedness;

import static com.nolanlawson.relatedness.BasicRelation.*;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.parser.RelativeNameParser;

public class ParserTest {

	@Test
	public void testBasicParses() {
		
		testEquals("cousin", Cousin);
		testEquals("sister", Sibling);
		testEquals("brother", Sibling);
		testEquals("dad", Parent);
		testEquals("mum", Parent);
		testEquals("half-sister", HalfSibling);
		testEquals("half sister", HalfSibling);
		testEquals("half-brother", HalfSibling);
		testEquals("uncle", AuntOrUncle);
		testEquals("aunt", AuntOrUncle);
		testEquals("Grandpa", Grandparent);
		testEquals("Gramma", Grandparent);
		testEquals("Great gramma", GreatGrandparent);
		testEquals("Great-grandma", GreatGrandparent);
	}
	
	@Test
	public void testComplexParses() {
		testEquals("mom's dad", Grandparent);
		testEquals("dad's brother", AuntOrUncle);
		testEquals("dad's brother's daughter", Cousin);
		testEquals("dad's uncle", GreatAuntOrUncle);
		testEquals("sister's son", NieceOrNephew);
	}

	@Test
	public void testReallyComplexParses() {
		// overheard on a message board - "she's my gramma's cousin's daughter"
		testEquals("gramma's cousin's daughter", new Relation(
				new CommonAncestor(4, 3), new CommonAncestor(4, 3)));
		
		// "half-nephew," I guess?
		testEquals("half-sister's son", new Relation(
				new CommonAncestor(1, 2)
				));
		
		
		// "half-grand-nephew"?  getting ridiculous now
		testEquals("half-sister's grandson", new Relation(
				new CommonAncestor(1, 3)
				));
		testEquals("half-sister's son's son", new Relation(
				new CommonAncestor(1, 3)
				));
	}
	
	private void testEquals(String name, BasicRelation basicRelation) {
		testEquals(name, basicRelation.getRelation());
	}
	
	private void testEquals(String name, Relation expectedRelation) {
		Relation relation = RelativeNameParser.parse(name);
		Assert.assertEquals(expectedRelation, relation);	
	}
}
