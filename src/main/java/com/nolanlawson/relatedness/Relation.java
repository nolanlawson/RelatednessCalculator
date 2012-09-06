package com.nolanlawson.relatedness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Relation between two people, e.g. brother-sister, son-daughter, cousin-cousin.
 * @author nolan
 *
 */
public class Relation implements Cloneable {

	private List<CommonAncestor> commonAncestors;
	
	// normally 1.  Basically just a fix for identical twins, whose relatedness
	// cannot be calculated only by looking at common ancestors
	private int relatednessFactor;
	
	public Relation(CommonAncestor... commonAncestors) {
		this(1, commonAncestors);
	}
	
	public Relation(List<CommonAncestor> commonAncestors) {
		this(1, commonAncestors);
	}
	
	public Relation(int relatednessFactor, CommonAncestor... commonAncestors) {
		this(relatednessFactor, Arrays.asList(commonAncestors));
	}
	
	public Relation(int relatednessFactor, List<CommonAncestor> commonAncestors) {
	    this.relatednessFactor = relatednessFactor;
	    this.commonAncestors = commonAncestors;
	}

	public List<CommonAncestor> getCommonAncestors() {
		return commonAncestors;
	}
	
	public int getRelatednessFactor() {
	    return relatednessFactor;
	}
	
	public Object clone() {
		List<CommonAncestor> newCommonAncestors = new ArrayList<CommonAncestor>();
		for (CommonAncestor commonAncestor : commonAncestors) {
			newCommonAncestors.add((CommonAncestor)commonAncestor.clone());
		}
		return new Relation(relatednessFactor, newCommonAncestors);
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime
		    * result
		    + ((commonAncestors == null) ? 0 : commonAncestors
			    .hashCode());
	    result = prime * result + relatednessFactor;
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
	    Relation other = (Relation) obj;
	    if (commonAncestors == null) {
		if (other.commonAncestors != null)
		    return false;
	    } else if (!commonAncestors.equals(other.commonAncestors))
		return false;
	    if (relatednessFactor != other.relatednessFactor)
		return false;
	    return true;
	}

	@Override
	public String toString() {
	    return "Relation [commonAncestors=" + commonAncestors
		    + ", relatednessFactor=" + relatednessFactor + "]";
	}
}
