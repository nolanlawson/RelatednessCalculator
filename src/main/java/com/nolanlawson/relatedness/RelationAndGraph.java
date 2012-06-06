package com.nolanlawson.relatedness;

import com.nolanlawson.relatedness.graph.RelationGraph;

public class RelationAndGraph {
	
	private Relation relation;
	private RelationGraph graph;
	
	public RelationAndGraph(Relation relation, RelationGraph graph) {
		this.relation = relation;
		this.graph = graph;
	}
	
	public Relation getRelation() {
		return relation;
	}
	public RelationGraph getGraph() {
		return graph;
	}
	
	
}
