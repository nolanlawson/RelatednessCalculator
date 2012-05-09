package com.nolanlawson.relatedness;

public enum BasicRelation {

	// ancestor is oneself
	Self (new Relation(new CommonAncestor(0,0))),
	
	// parent is the common ancestor
	ParentChild (new Relation(new CommonAncestor(1, 0))),
	
	// share two parents
	Siblings (new Relation(new CommonAncestor(1,1), new CommonAncestor(1,1))),
	
	// share one parent
	HalfSiblings (new Relation(new CommonAncestor(1,1))),
	
	// share two grandparents
	Cousins (new Relation(new CommonAncestor(2,2), new CommonAncestor(2,2))),
	
	// grandparent is the ancestor
	GrandparentGrandchild (new Relation(new CommonAncestor(2,0))),
	
	// or AuntNiece, to be gender-neutral...
	// share two ancestors, each a parent/grandparent
	UncleNephew (new Relation(new CommonAncestor(2, 1), new CommonAncestor(2, 1))),
	
	// share two great-grandparents
	SecondCousins(new Relation(new CommonAncestor(3, 3), new CommonAncestor(3,3))),
	
	// great-grandparent is the ancestor
	GreatGrandparentGreatGrandchild (new Relation(new CommonAncestor(3, 0))),
	
	// or GreatAuntGreatNiece, to be gender-neutral...
	// share two ancestors, each a grandparent/great-grandparent
	GreatUncleGreatNephew (new Relation(new CommonAncestor(3, 2), new CommonAncestor(3, 2))),
	
	// Rare case where two siblings from one family each marry two siblings from another family,
	// so their kids share all 4 grandparents
	DoubleFirstCousins (new Relation(new CommonAncestor(2, 2), new CommonAncestor(2, 2), 
			new CommonAncestor(2, 2) , new CommonAncestor(2, 2))),
	;
	
	private Relation relation;

	private BasicRelation(Relation relation) {
		this.relation = relation;
	}
	
	public Relation getRelation() {
		return this.relation;
	}
}
