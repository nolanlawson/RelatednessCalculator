package com.nolanlawson.relatedness;

import org.junit.Test;

import com.nolanlawson.relatedness.parser.RelativeNameParser;

public class InvalidProgressionsTest {

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical1() {
		RelativeNameParser.parse("mom's daughter");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical2() {
		RelativeNameParser.parse("son's dad");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical3() {
		RelativeNameParser.parse("sister's brother");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical4() {
		RelativeNameParser.parse("cousin's brother");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical5() {
		RelativeNameParser.parse("uncle's brother");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical6() {
		RelativeNameParser.parse("grandma's daughter");
	}

	@Test(expected = UnknownRelationException.class)
	public void testNonsensical7() {
		RelativeNameParser.parse("double cousin's double cousin");
	}
	@Test(expected=UnknownRelationException.class)
	public void testNonsensical8() {
		RelativeNameParser.parse("grandson's father");
	}	
	@Test(expected=UnknownRelationException.class)
	public void testNonsensical9() {
		RelativeNameParser.parse("cousin's cousin");
	}	
	@Test(expected=UnknownRelationException.class)
	public void testNonsensical10() {
		RelativeNameParser.parse("half-brother's sister");
	}
}

