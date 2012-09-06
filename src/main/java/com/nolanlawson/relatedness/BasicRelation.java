package com.nolanlawson.relatedness;

public enum BasicRelation {

	// ancestor is oneself
	Self (new Relation(new CommonAncestor(0,0))),
	
	// parent is the common ancestor
	Parent (new Relation(new CommonAncestor(1, 0))),
	Child (new Relation(new CommonAncestor(0, 1))),
	
	// share two parents
	Sibling (new Relation(new CommonAncestor(1,1), new CommonAncestor(1,1))),
	
	// share one parent
	HalfSibling (new Relation(new CommonAncestor(1,1))),
	
	// share two grandparents
	Cousin (new Relation(new CommonAncestor(2,2), new CommonAncestor(2,2))),
	
	// grandparent is the ancestor
	Grandparent (new Relation(new CommonAncestor(2,0))),
	Grandchild (new Relation(new CommonAncestor(0,2))),
	
	// share two ancestors, each a parent/grandparent
	AuntOrUncle (new Relation(new CommonAncestor(2, 1), new CommonAncestor(2, 1))),
	NieceOrNephew (new Relation(new CommonAncestor(1, 2), new CommonAncestor(1, 2))),
	
	// share two great-grandparents, etc.
	SecondCousin(new Relation(new CommonAncestor(3, 3), new CommonAncestor(3,3))),
	ThirdCousin(new Relation(new CommonAncestor(4, 4), new CommonAncestor(4,4))),
	FourthCousin(new Relation(new CommonAncestor(5, 5), new CommonAncestor(5,5))),
	FifthCousin(new Relation(new CommonAncestor(6, 6), new CommonAncestor(6,6))),
	SixthCousin(new Relation(new CommonAncestor(7, 7), new CommonAncestor(7,7))),
	SeventhCousin(new Relation(new CommonAncestor(8, 8), new CommonAncestor(8,8))),
	EighthCousin(new Relation(new CommonAncestor(9, 9), new CommonAncestor(9,9))),
	// assume no one will search past the eighth cousin...
	
	// great-grandparent is the ancestor
	GreatGrandparent (new Relation(new CommonAncestor(3, 0))),
	GreatGrandchild (new Relation(new CommonAncestor(0, 3))),
	
	
	// share two ancestors, each a grandparent/great-grandparent
	GreatAuntOrUncle (new Relation(new CommonAncestor(3, 1), new CommonAncestor(3, 1))),
	GreatNieceOrNephew (new Relation(new CommonAncestor(1, 3), new CommonAncestor(1, 3))),
	
	// Rare case where two siblings from one family each marry two siblings from another family,
	// so their kids share all 4 grandparents
	DoubleFirstCousin (new Relation(new CommonAncestor(2, 2), new CommonAncestor(2, 2), 
			new CommonAncestor(2, 2) , new CommonAncestor(2, 2))),
			
	IdenticalTwin (new Relation(2, new CommonAncestor(1,1), new CommonAncestor(1,1)));
	
	;
	
	private Relation relation;

	private BasicRelation(Relation relation) {
		this.relation = relation;
	}
	
	public Relation getRelation() {
		return this.relation;
	}
}
