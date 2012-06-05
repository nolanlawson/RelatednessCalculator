package com.nolanlawson.relatedness;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.graph.RelationGraph;
import com.nolanlawson.relatedness.parser.RelativeNameParser;

public class GraphTest {

	@Test
	public void testSimpleGraph() {
		testGraph(BasicRelation.GreatGrandchild.getRelation(), "you", "your great-grandson");
		testGraph(BasicRelation.Parent.getRelation(), "you", "your dad");
		testGraph(BasicRelation.Grandparent.getRelation(), "you", "your grandpa");
		testGraph(BasicRelation.Child.getRelation(), "you", "your son");
		testGraph(BasicRelation.Grandchild.getRelation(), "you", "your grandson");
		testGraph(BasicRelation.Cousin.getRelation(), "you", "your cousin");
		testGraph(BasicRelation.AuntOrUncle.getRelation(), "you", "your uncle");
	}
	
	@Test
	public void testParsedGraph() {
		testParsedGraph("sister", 4, 4);
		testParsedGraph("grandpa", 2, 3);
		testParsedGraph("grandson", 2, 3);
		testParsedGraph("father's cousin's daughter", 8, 8);
		testParsedGraph("double cousin", 12, 10);
		testParsedGraph("cousin", 6, 6);
		testParsedGraph("uncle", 5, 5);
		testParsedGraph("nephew", 5, 5);
		
	}
	
	private void testParsedGraph(String text, int expectedNumRelations, int expectedNumNodes) {
		System.out.println(text);
		String parsedGraph = RelativeNameParser.parseGraph(text).drawGraph();
		System.out.println(parsedGraph);
		Assert.assertEquals(expectedNumRelations, countOf(parsedGraph, "->"));
		Assert.assertEquals(expectedNumNodes, countOf(parsedGraph, "[label"));
		
	}

	private void testGraph(Relation relation, String sourceName, String targetName) {
		RelationGraph relationGraph = new RelationGraph();
		relationGraph.addRelation(sourceName, targetName, relation);
		System.out.println(sourceName + " -> " + targetName);
		System.out.println(relationGraph.drawGraph());
	}
	

	private int countOf(String str, String substr) {
		int index = 0;
		int count = 0;
		while ((index = str.indexOf(substr, index)) != -1) {
			count++;
			index += substr.length();
		}
		return count;
	}
}
