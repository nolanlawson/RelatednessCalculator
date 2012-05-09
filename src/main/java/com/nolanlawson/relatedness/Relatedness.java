package com.nolanlawson.relatedness;

public class Relatedness {

	private double averageDegree;
	private double coefficient;
	
	public Relatedness(double averageDegree, double coefficient) {
		this.averageDegree = averageDegree;
		this.coefficient = coefficient;
	}
	public double getAverageDegree() {
		return averageDegree;
	}
	public double getCoefficient() {
		return coefficient;
	}
	
	@Override
	public String toString() {
		return "Relatedness [averageDegree=" + averageDegree + ", coefficient=" + coefficient
				+ "]";
	}
}
