package com.nolanlawson.relatedness;

import org.junit.Test;

import com.nolanlawson.relatedness.graph.RelationGraph;

public class GraphTest {

	@Test
	public void testSimpleGraph() {
		testGraph(BasicRelation.Parent.getRelation(), "you", "your dad");
		testGraph(BasicRelation.Grandparent.getRelation(), "you", "your grandpa");
		testGraph(BasicRelation.Child.getRelation(), "you", "your son");
		testGraph(BasicRelation.Grandchild.getRelation(), "you", "your grandson");
	}
	
	private void testGraph(Relation relation, String sourceName, String targetName) {
		RelationGraph relationGraph = new RelationGraph();
		relationGraph.addRelation(sourceName, targetName, relation);
		System.out.println(relationGraph.drawGraph());
	}
}
