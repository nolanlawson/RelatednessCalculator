package com.nolanlawson.relatedness;

import java.util.Arrays;
import java.util.List;

/**
 * Relation between two people, e.g. brother-sister, son-daughter, cousin-cousin.
 * @author nolan
 *
 */
public class Relation {

	private List<CommonAncestor> commonAncestors;
	
	public Relation(CommonAncestor... commonAncestors) {
		this.commonAncestors = Arrays.asList(commonAncestors);
	}

	public List<CommonAncestor> getCommonAncestors() {
		return commonAncestors;
	}
}
