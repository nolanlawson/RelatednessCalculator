package com.nolanlawson.relatedness;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.util.WordWrapper;

public class WordWrapperTest {

	@Test
	public void testWordWrapper() {
		Assert.assertEquals("this is\nmy father", WordWrapper.wordWrap("  this is my father ", 16));
		Assert.assertEquals("this is just my\ngreat-grandfather", WordWrapper.wordWrap("  this is just my great-grandfather ", 20));
		Assert.assertEquals("your nephew's\nparent", WordWrapper.wordWrap("  your nephew's parent ", 16));
		Assert.assertEquals("great-great-great-\ngreat-grandfather", 
				WordWrapper.wordWrap("great-great-great-great-grandfather", 20));
		Assert.assertEquals("your half-\nbrother's\ndaughter", 
				WordWrapper.wordWrap("your half-brother's daughter", 16));
	}
	
}
