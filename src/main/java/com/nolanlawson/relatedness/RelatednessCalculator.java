package com.nolanlawson.relatedness;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Calculates the Relatedness between two people.  Richard Dawkins gives a good explanation
 * for this calculation in <i>The Selfish Gene</i>:
 * 
 * First identify all the common ancestors of A and B. 
 * For instance, the common ancestors of a pair of first cousins 
 * are their shared grandfather and grandmother. Once you have 
 * found a common ancestor, it is of course logically true that 
 * all his ancestors are common to A and B as well. However, we 
 * ignore all but the most recent common ancestors. In this sense, 
 * first cousins have only two common ancestors. If B is a lineal descendant 
 * of A, for instance his great grandson, then A himself is the 
 * ‘common ancestor’ we are looking for.
 * 
 * Having located the common ancestor(s) of A and B, count the 
 * generation distance as follows. Starting at A, climb up the 
 * family tree until you hit a common ancestor, and then climb 
 * down again to B. The total number of steps up the tree and 
 * then down again is the generation distance. For instance, 
 * if A is B’s uncle, the generation distance is 3. The common 
 * ancestor is A’s father (say) and B’s grandfather. Starting 
 * at A you have to climb up one generation in order to hit 
 * the common ancestor. Then to get down to B you have to 
 * descend two generations on the other side. Therefore the 
 * generation distance is 1 + 2 = 3.
 * 
 * Having found the generation distance between A and B via 
 * a particular common ancestor, calculate that part of their 
 * relatedness for which that ancestor is responsible. To do 
 * this, multiply 1/2 by itself once for each step of the generation 
 * distance. If the generation distance is 3, this means calculate 
 * 1/2 x 1/2 x 1/2 or (1/2)^3. If the generation distance via a 
 * particular ancestor is equal to g steps, the portion of 
 * relatedness due to that ancestor is (1/2)^g.
 * 
 * But this is only part of the relatedness between A and B. If they 
 * have more than one common ancestor we have to add on the equivalent 
 * figure for each ancestor. It is usually the case that the generation 
 * distance is the same for all common ancestors of a pair of individuals. 
 * Therefore, having worked out the relatedness between A and B due to any 
 * one of the ancestors, all you have to do in practice is to multiply by 
 * the number of ancestors. First cousins, for instance, have two common 
 * ancestors, and the generation distance via each one is 4. Therefore their 
 * relatedness is 2 x (1/2)^4 = 1/8. If A is B’s great-grandchild, the
 *  generation distance is 3 and the number of common ‘ancestors’ is 1 
 *  (B himself), so the relatedness is 1 x (1/2)^3 = 1/8.
 * 
 * @param relation
 * @return
 * @see http://en.wikipedia.org/wiki/Coefficient_of_relationship
 */
public class RelatednessCalculator {


	/**
	 * TODO: allow for multiple common ancestors at different degrees (corner case)
	 * 
	 * @param relation
	 * @return
	 */
	public static Relatedness calculate(Relation relation) {
		
		// calculate average degree, in the case where more than one common ancestor is shared at multiple levels
		List<Integer> degrees = new ArrayList<Integer>();
		double coefficient = 0.0;

		for (CommonAncestor commonAncestor : relation.getCommonAncestors()) {
			int degree = commonAncestor.getDistanceFromFirst() + commonAncestor.getDistanceFromSecond();
			degrees.add(degree);
			coefficient += Math.pow(0.5, degree);
		}
		
		double averageDegree = calculateAverage(degrees);
		
		return new Relatedness(averageDegree, coefficient);
	}

	private static double calculateAverage(List<Integer> degrees) {
		double sum = 0;
		for (Integer degree : degrees) {
			sum += degree;
		}
		return sum / degrees.size();
	}
	
}
