package com.nolanlawson.relatedness.parser;

import java.util.List;

import com.nolanlawson.relatedness.Relation;
import com.nolanlawson.relatedness.graph.RelationGraph;

/**
 * Result of parsing a sentence
 * @author nolan
 *
 */
public class RelationParseResult {

    private ParseError parseError;
    private Relation relation;
    private List<String> ambiguityResolutions;
    private RelationGraph graph;
    
    public ParseError getParseError() {
        return parseError;
    }
    public void setParseError(ParseError parseError) {
        this.parseError = parseError;
    }
    public Relation getRelation() {
        return relation;
    }
    public void setRelation(Relation relation) {
        this.relation = relation;
    }
    public List<String> getAmbiguityResolutions() {
        return ambiguityResolutions;
    }
    public void setAmbiguityResolutions(List<String> ambiguityResolutions) {
        this.ambiguityResolutions = ambiguityResolutions;
    }
    public RelationGraph getGraph() {
        return graph;
    }
    public void setGraph(RelationGraph graph) {
        this.graph = graph;
    }
    @Override
    public String toString() {
	return "RelationParseResult [parseError=" + parseError + ", relation="
		+ relation + ", ambiguousRelations=" + ambiguityResolutions
		+ ", graph=" + graph + "]";
    }
}
