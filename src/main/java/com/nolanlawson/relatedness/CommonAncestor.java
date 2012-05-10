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
	public void setDistanceFromFirst(int distanceFromFirst) {
		this.distanceFromFirst = distanceFromFirst;
	}
	public void setDistanceFromSecond(int distanceFromSecond) {
		this.distanceFromSecond = distanceFromSecond;
	}

	public Object clone() {
		return new CommonAncestor(distanceFromFirst, distanceFromSecond);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + distanceFromFirst;
		result = prime * result + distanceFromSecond;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonAncestor other = (CommonAncestor) obj;
		if (distanceFromFirst != other.distanceFromFirst)
			return false;
		if (distanceFromSecond != other.distanceFromSecond)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CommonAncestor [distanceFromFirst=" + distanceFromFirst
				+ ", distanceFromSecond=" + distanceFromSecond + "]";
	}
}
