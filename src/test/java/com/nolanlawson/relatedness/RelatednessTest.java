package com.nolanlawson.relatedness;

import static com.nolanlawson.relatedness.BasicRelation.Cousins;
import static com.nolanlawson.relatedness.BasicRelation.GrandparentGrandchild;
import static com.nolanlawson.relatedness.BasicRelation.GreatGrandparentGreatGrandchild;
import static com.nolanlawson.relatedness.BasicRelation.GreatUncleGreatNephew;
import static com.nolanlawson.relatedness.BasicRelation.HalfSiblings;
import static com.nolanlawson.relatedness.BasicRelation.ParentChild;
import static com.nolanlawson.relatedness.BasicRelation.SecondCousins;
import static com.nolanlawson.relatedness.BasicRelation.Self;
import static com.nolanlawson.relatedness.BasicRelation.Siblings;
import static com.nolanlawson.relatedness.BasicRelation.UncleNephew;

import org.junit.Assert;
import org.junit.Test;

import com.nolanlawson.relatedness.BasicRelation;
import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relatedness;
import com.nolanlawson.relatedness.RelatednessCalculator;
import com.nolanlawson.relatedness.Relation;

/**
 * Tests taken from calculations on http://en.wikipedia.org/wiki/Coefficient_of_relationship.
 * @author nolan
 *
 */
public class RelatednessTest {

	@Test
	public void testBasicRelations() {
		
		testRelatedness(0, 1.0, Self);
		testRelatedness(1, 0.5, ParentChild);
		testRelatedness(2, 0.5, Siblings);
		testRelatedness(2, 0.25, GrandparentGrandchild);
		testRelatedness(2, 0.25, HalfSiblings);
		testRelatedness(3, 0.25, UncleNephew);
		testRelatedness(3, 0.125, GreatGrandparentGreatGrandchild);
		testRelatedness(4, 0.125, Cousins);
		testRelatedness(6, 0.03125, SecondCousins);
		testRelatedness(5, 0.0625, GreatUncleGreatNephew);
		testRelatedness(4, 0.25, BasicRelation.DoubleFirstCousins);
	}
	
	@Test
	public void testWeirdRelation() {
		// overheard on a message board: "her dad is my grandma's cousin"
		// had to draw this one out for it to make sense... his two great-great-grandparents are the same
		// as her two great-grandparents
		testRelatedness(7, 0.015625, new Relation(new CommonAncestor(4, 3), new CommonAncestor(4, 3)));
		
	}
	private void testRelatedness(double expectedDegree, double expectedCoefficient, Relation relation) {
		Relatedness relatedness = RelatednessCalculator.calculate(relation);
		Assert.assertEquals(expectedDegree, relatedness.getAverageDegree(), 0.0);
		Assert.assertEquals(expectedCoefficient, relatedness.getCoefficient(), 0.000001);
	}
	
	private void testRelatedness(double expectedDegree, double expectedCoefficient, BasicRelation basicRelation) {
		testRelatedness(expectedDegree, expectedCoefficient, basicRelation.getRelation());
	}
}
