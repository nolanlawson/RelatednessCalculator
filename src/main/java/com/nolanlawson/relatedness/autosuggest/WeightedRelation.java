package com.nolanlawson.relatedness.autosuggest;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.nolanlawson.relatedness.util.Trie.TrieLeaf;


public class WeightedRelation implements Comparable<WeightedRelation> {

    public static final double FIRST_PRIORITY = 1;
    public static final double SECOND_PRIORITY = 0.5; 
    
    private String relation;
    private double weight = 1;
    
    public WeightedRelation(String relation, double weight) {
	this.relation = relation;
	this.weight = weight;
    }

    public String getRelation() {
        return relation;
    }
    public void setRelation(String relation) {
        this.relation = relation;
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * sort by greatest weight, then A-Z on the relation name
     */
    public int compareTo(WeightedRelation other) {
	return ComparisonChain.start()
		.compare(other.weight, weight)
		.compare(relation, other.relation)
		.result();
    }
    
    public static Function<WeightedRelation, String> getRelationFunction = new Function<WeightedRelation, String>() {

	public String apply(WeightedRelation input) {
	    return input.getRelation();
	}
    };
    
    public static Function<TrieLeaf<Double>, WeightedRelation> fromTrieLeafFunction = 
	    new Function<TrieLeaf<Double>, WeightedRelation>() {

		public WeightedRelation apply(TrieLeaf<Double> input) {
		    return new WeightedRelation(input.getKey().toString(), input.getValue());
		}
    };
}
