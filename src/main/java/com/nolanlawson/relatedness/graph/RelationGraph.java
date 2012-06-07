package com.nolanlawson.relatedness.graph;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relation;
import com.nolanlawson.relatedness.util.WordWrapper;

public class RelationGraph {

	private static final String TEMPLATE = "digraph a {\n" +
			"size=\"10,10\";\n" +
			"ordering=\"out\";\n" + // forces nodes to be drawn in the order I list them
			"%s" +
			"}\n";
	
	// we want to keep the graph nice and skinny
	private static final int TARGET_LABEL_LENGTH_TWO_ANCESTORS = 16;
	private static final int TARGET_LABEL_LENGTH_FOUR_ANCESTORS = 8;
	private static final int TARGET_LABEL_LENGTH_ONE_ANCESTOR = 32;	
	
	// so we can say 'your parent', 'your other parent,' 'your third grandparent', etc.
	private static final String[] COUNTER_WORDS = {"","other ","third ","fourth "};
	
	// sort by name so that e.g. "grandparent" appears before "other grandparent" appears before "third grandparent"...
	private Map<LabelKey,String> labels = new TreeMap<LabelKey,String>();
	private Set<String> nodeConnections = new HashSet<String>();
	
	private NodeNameIterator nameIterator = new NodeNameIterator();
	private int maxAncestorsInSingleGeneration = 1;
	
	public RelationGraph() {
	}
	
	/**
	 * Add a relation to the drawing
	 * @param sourceName
	 * @param targetName
	 * @param relation
	 */
	public void addRelation(String sourceName, String targetName, Relation relation) {
		
	    	// update the max ancestors in a single generation
	    	maxAncestorsInSingleGeneration = Math.max(maxAncestorsInSingleGeneration, 
	    		relation.getCommonAncestors().size());
	    
		// draw the relation between two labels
		
		// algorithm:
		// For each common ancestor between A & B:
		// 1) Walk from A to B along the family tree, creating a link at each step.  Name nodes relative to A
		// 2) When you reach the common ancestor, decide whether to name it based on B, if B *is* the ancestor
		// 3) As you walk away from the common ancestor toward B, name it relative to B
		// 4) Finish when you reach B; name B that.
		
		for (int i = 0; i < relation.getCommonAncestors().size(); i++) {
			CommonAncestor commonAncestor = relation.getCommonAncestors().get(i);
			int distFrom1 = commonAncestor.getDistanceFromFirst();
			int distFrom2 = commonAncestor.getDistanceFromSecond();
			
			// this basically just fixes a bug with double cousins, where there are 4 common ancestors,
			// so uniqueness has to be applied to the four grandparents as well as the four parents
			int unique = i / 2;
			
			// name the common ancestor relative to A, unless B is the common ancestor
			String commonAncestorId = (distFrom2 == 0) 
					? getId(targetName, 0, i)
					: getId(sourceName, distFrom1, i);
			
			// add links between relatives on A's side, including A and the common ancestor
			for (int j = 0; j < distFrom1; j++) {
				String leftId = getId(sourceName, j, j == 0 ? 0 : unique);
				String rightId;
				
				if (j + 1 == distFrom1) { // common ancestor; use the predetermined common ancestor name
					rightId = commonAncestorId;
				} else { // use a name relative to A
					rightId = getId(sourceName, j + 1, unique);
				}
				addEdge(rightId, leftId);
			}
			
			// add relatives on B's side, starting from common ancestor, naming everyone relative to B
			// except the common ancestor, and except if A is the common ancestor
			for (int j = distFrom2; j > 0; j--) {
				String leftId;
				if (j == distFrom2) { // left is the common ancestor
					leftId = commonAncestorId;
				} else if (distFrom1 == 0){ // name after A if A is the common ancestor
					leftId = getId(sourceName, -(distFrom2 - j), unique);
				} else { // name after B
					leftId = getId(targetName, j, unique);
				}
				String rightId;
				if (j != 1 && distFrom1 == 0) { // the ancestor is A, and B is not the one on the right
					rightId = getId(sourceName, -(distFrom2 - (j - 1)), unique);
				} else if (j != 1) { // B is not the one on the right
					rightId = getId(targetName, j - 1, unique);
				} else {
					rightId = getId(targetName, j - 1);
				}
				addEdge(leftId, rightId);
			}
		}
		
	}

	/**
	 * Output a DOT-format graph string
	 * @return
	 */
	public String drawGraph() {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<LabelKey,String> entry : labels.entrySet()) {
			LabelKey labelKey = entry.getKey();
			String id = entry.getValue();
			
			// DOT format for declaring a node
			stringBuilder.append(String.format("%s [label=\"%s\"];", id, createHumanReadableLabel(labelKey)))
				.append('\n');
		}
		for (String nodeConnection : nodeConnections) {
			stringBuilder.append(nodeConnection).append(";\n");
		}
		return String.format(TEMPLATE, stringBuilder);
	}

	private String createHumanReadableLabel(LabelKey labelKey) {
	    // TODO allow for non-English

	    int maxLabelLength = determineMaxLabelLength();

	    String label;

	    if (labelKey.getAncestorDistance() == 0) {
		// simplest case, no possessives needed, so just name as A or B, e.g. "you", "niece"
		label = labelKey.getLabel();
	    } else {
		// else name the ancestor relative to A or B

		// special hack for double cousins
	    	// normally we could just name the grandparents based on the ancestorId,
		// but because of how dot orders the nodes, this
		// will give us a weird-looking left-to-right ordering of the grandparent nodes
		// so we change their label ids as follows:
		// 0 -> 1, 1 -> 0, 2 -> 3, 3 -> 2		
		int counterIdx = labelKey.getAncestorId();
		if (maxAncestorsInSingleGeneration == 4 // double cousin case, where there are 4 grandparents
			&& labelKey.getAncestorDistance() == 2) { // this node is one of the 4 grandparents
		    	counterIdx += ((counterIdx % 2 == 0) ? 1 : - 1);
		} 
		
		String possessive = labelKey.getLabel().equalsIgnoreCase("you") ? "r" : "'s";
		label = new StringBuilder()
		.append(labelKey.getLabel())
		.append(possessive)
		.append(' ')
		.append(COUNTER_WORDS[counterIdx])
		.append(createRelationString(labelKey.getAncestorDistance()))
		.toString();
	    }

	    // add newlines where appropriate
	    label = WordWrapper.wordWrap(label, maxLabelLength);

	    return label.replace("\n", "\\n"); // escape newlines for the DOT format
	}

	private int determineMaxLabelLength() {
	    // How much we want to wrap the text depends on how many "columnns" dot is going to output.
	    // And the number of columns is determined by the max number of ancestors in a single generation,
	    // e.g. 1 for "parent," 2 for "sister," and 4 for "double cousin".
	    switch (maxAncestorsInSingleGeneration) {
	    	case 4:
			return TARGET_LABEL_LENGTH_FOUR_ANCESTORS;
	    	case 1:
			return TARGET_LABEL_LENGTH_ONE_ANCESTOR;
	    	case 2:
		default:
		    	return TARGET_LABEL_LENGTH_TWO_ANCESTORS;
	    }
	}

	private CharSequence createRelationString(int distance) {
		// TODO: allow for non-English
		switch (distance) {
			case 1:
				return "parent";
			case 2:
				return "grandparent";
			case -1:
				return "child";
			case -2:
				return "grandchild";
		}
		// else add in a bunch of "great"s
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = Math.abs(distance); i > 2; i--) {
			stringBuilder.append("great-");
		}
		if (distance > 0) {
			stringBuilder.append("grandparent");
		} else {
			stringBuilder.append("grandchild");
		}
		return stringBuilder;
		
	}

	private void addEdge(String ancestorId, String descendantId) {
		// DOT notation for a directed graph
		nodeConnections.add(String.format("%s -> %s", ancestorId, descendantId));
		
	}

	private String getId(String label, int ancestorDistance) {
		return getId(label, ancestorDistance, 0);
	}
	
	private String getId(String label, int ancestorDistance, int ancestorId) {
		LabelKey labelKey = new LabelKey(label, ancestorDistance, ancestorId);
		if (!labels.containsKey(labelKey)) {
			labels.put(labelKey, nameIterator.next());
		}
		return labels.get(labelKey);
	}
	
	private static class LabelKey implements Comparable<LabelKey> {
		private String label;
		private int ancestorDistance;
		private int ancestorId;
		
		private LabelKey(String label, int ancestorDistance, int ancestorId) {
			this.label = label;
			this.ancestorDistance = ancestorDistance;
			this.ancestorId = ancestorId;
		}
		
		
		public String getLabel() {
			return label;
		}


		public int getAncestorDistance() {
			return ancestorDistance;
		}
		
		public int getAncestorId() {
			return ancestorId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ancestorDistance;
			result = prime * result + ancestorId;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
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
			LabelKey other = (LabelKey) obj;
			if (ancestorDistance != other.ancestorDistance)
				return false;
			if (ancestorId != other.ancestorId)
				return false;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "LabelKey [label=" + label + ", ancestorDistance="
					+ ancestorDistance + ", ancestorId=" + ancestorId + "]";
		}


		public int compareTo(LabelKey other) {
		    // sort by label, ancestorDistance, ancestorId
		    // this ensures proper left-to-right ordering
		    int compare;
		    if ((compare = label.compareTo(other.label)) != 0) {
			return compare;
		    } else if ((compare = (ancestorDistance - other.ancestorDistance)) != 0) {
			return compare;
		    }
		    return ancestorId - other.ancestorId;
		}
	}
	
}
