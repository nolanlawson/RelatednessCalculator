package com.nolanlawson.relatedness;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nolanlawson.relatedness.autosuggest.RelationSuggester;

public class SuggesterTest {

    RelationSuggester suggester;
    
    @Before
    public void setUp() {
	suggester = new RelationSuggester();
    }
    
    @Test
    public void testSuggestions1() {
	testSuggestions(6, "grand", "grandchild", "granddaughter", "grandma", "grandpa", "grandparent", "grandson");
	testSuggestions(10, "great", "great-aunt", "great-grandchild", "great-granddaughter", 
		"great-grandma", "great-grandpa", "great-grandparent", "great-grandson", "great-nephew",
		"great-niece", "great-uncle");
	testSuggestions(10, "half", "half-fourth cousin", "half-sibling", "half-second cousin", "half-cousin", 
		"half-niece", "half-fifth cousin", "half-uncle", "half-nephew", "half-aunt", "half-third cousin");
	testSuggestions(10, "", "grandchild", "child", "grandpa", "granddaughter", "daughter", "grandparent", 
		"cousin", "aunt", "grandma", "father");
	testSuggestions(10, "grandpa", "grandpa's great-aunt", "grandpa's aunt", "grandpa's grandparent", 
		"grandpa's great-grandma", "grandpa", "grandpa's grandma", "grandpa's father", "grandpa's cousin", 
		"grandpa's grandpa", "grandparent");
	testSuggestions(1, "grandparentxxxxxx");
	testSuggestions(1, "grandpa'", "grandpa's aunt");
	testSuggestions(1, "grandpa's", "grandpa's aunt");
	testSuggestions(1, "grandpa's ", "grandpa's aunt");
	testSuggestions(1, "grandpa's cous", "grandpa's cousin");
	testSuggestions(1, "grandpa's grandso"); // illogical
	testSuggestions(1, "father's brother's daugh", "father's brother's daughter");
	testSuggestions(1, "father's brother's daughter's daugh", "father's brother's daughter's daughter");
	testSuggestions(1, "first cousin tw", "first cousin twice removed");
	testSuggestions(1, "second cousin o", "second cousin once removed");
	
    }
    
    private void testSuggestions(int limit, String input, String... outputs) {
	List<String> result = suggester.suggest(input, limit);
	Assert.assertEquals(new HashSet<String>(Arrays.asList(outputs)), new HashSet<String>(result));
    }
    
}
