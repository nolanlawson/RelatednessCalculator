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

	private void testEquals(String name, BasicRelation basicRelation) {
		
		Relation relation = RelativeNameParser.parse(name);
		Assert.assertEquals(basicRelation.getRelation(), relation);
		
	}
}
