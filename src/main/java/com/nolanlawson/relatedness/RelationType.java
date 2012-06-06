package com.nolanlawson.relatedness;

/**
 * There are two types of relations: descending, ascending, and arcing.
 * Descending relations are those that descend from A (e.g. grandson)
 * Ascending realtions ascend from A (e.g. grandfather)
 * Arcing relations require a separate common ancestor (or more than one) who is neither
 * A nor B (e.g. sibling, cousin)
 * 
 * @author nolan
 *
 */
public enum RelationType {

	Descending, Ascending, Arcing;
	
	public static RelationType fromRelation(Relation relation) {
		// only need one common ancestor, since we're assuming no incest
		CommonAncestor ancestor = relation.getCommonAncestors().iterator().next();
		
		if (ancestor.getDistanceFromFirst() == 0) { 
			return Descending;
		} else if (ancestor.getDistanceFromSecond() == 0) {
			return Ascending;
		}
		return Arcing;
	}
	
	/**
	 * When parsing user queries, certain relation type successions are invalid. For instance,
	 * "cousin's sister's cousin" makes no sense, because it's ambigous.  Also,
	 * "mother's daughter's mother's daughter" makes no sense.
	 * 
	 * The best way to separate valid from invalid queries is to use RelationType progressions.
	 * This is a limitation whereby certain RelationTypes cannot be followed by other relationTypes.
	 * 
	 * E.g. ascending + arcing + descending is fine (parent's cousin's daughter).
	 * 
	 * @param previous
	 * @param next
	 * @return
	 */
	public static boolean isValidProgression(RelationType previous, RelationType next) {
		
		switch (previous) {
			case Ascending:
				return next == Arcing || next == Ascending;
			case Arcing:
				return next == Descending;
			case Descending:
			default:
				return next == Descending; 
		}
	}
}
