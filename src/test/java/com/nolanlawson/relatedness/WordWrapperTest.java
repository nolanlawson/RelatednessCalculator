package com.nolanlawson.relatedness;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.util.WordWrapper;

public class WordWrapperTest {

	@Test
	public void testWordWrapper() {
		Assert.assertEquals("this is\nmy father", WordWrapper.wordWrap("  this is my father ", 16));
		Assert.assertEquals("this is my\ngreat-grandfather", WordWrapper.wordWrap("  this is my great-grandfather ", 16));
		Assert.assertEquals("your nephew's\nparent", WordWrapper.wordWrap("  your nephew's parent ", 16));
		
	}
	
}
