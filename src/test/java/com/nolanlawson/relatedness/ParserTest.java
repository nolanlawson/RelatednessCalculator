package com.nolanlawson.relatedness;

import static com.nolanlawson.relatedness.BasicRelation.AuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.Cousin;
import static com.nolanlawson.relatedness.BasicRelation.Grandparent;
import static com.nolanlawson.relatedness.BasicRelation.GreatAuntOrUncle;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandchild;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandparent;
import static com.nolanlawson.relatedness.BasicRelation.GreatNieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.HalfSibling;
import static com.nolanlawson.relatedness.BasicRelation.NieceOrNephew;
import static com.nolanlawson.relatedness.BasicRelation.Parent;
import static com.nolanlawson.relatedness.BasicRelation.Sibling;

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
	public void testGreats() {
		testEquals("great-grandson", GreatGrandchild);
		testEquals("great niece", GreatNieceOrNephew);
		testEquals("greatgrammy", GreatGrandparent);
		testEquals("great-great-grandma", new Relation(new CommonAncestor(4,0)));
		testEquals("great-great-uncle", new Relation(new CommonAncestor(4,1), new CommonAncestor(4,1)));
		testEquals("great-great-half-aunt", new Relation(new CommonAncestor(4,1)));
	}
	
	@Test
	public void testHalfs() {
		testEquals("halfsister", HalfSibling);
		testEquals("half-sister", HalfSibling);
		testEquals("half sister", HalfSibling);
		testEquals("half cousin", new Relation(new CommonAncestor(2,2)));
		testEquals("half-cousin", new Relation(new CommonAncestor(2,2)));
		testEquals("halfcousin", new Relation(new CommonAncestor(2,2)));
		testEquals("half aunt", new Relation(new CommonAncestor(2,1)));
	}

	@Test(expected=UnknownRelationException.class)
	public void testUnknown1() {
		RelativeNameParser.parse("half mother");
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown2() {
		RelativeNameParser.parse("half double cousin");
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown3() {
		RelativeNameParser.parse("great son"); // no such thing, hah
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown4() {
		RelativeNameParser.parse("grand cousin");
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown5() {
		RelativeNameParser.parse("double mother"); // parser is not PC enough yet
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown6() {
		RelativeNameParser.parse("foobar dad's brother");
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown7() {
		RelativeNameParser.parse("dad's foobar brother");
	}
	@Test(expected=UnknownRelationException.class)
	public void testUnknown8() {
		RelativeNameParser.parse("dad's brother foobar"); // we don't talk about Uncle Foobar anymore
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
