package com.nolanlawson.relatedness;

public class CommonAncestor {

	private int distanceFromFirst;
	private int distanceFromSecond;
	
	public CommonAncestor(int distanceFromFirst, int distanceFromSecond) {
		this.distanceFromFirst = distanceFromFirst;
		this.distanceFromSecond = distanceFromSecond;
	}
	
	public int getDistanceFromFirst() {
		return distanceFromFirst;
	}
	public int getDistanceFromSecond() {
		return distanceFromSecond;
	}
}
