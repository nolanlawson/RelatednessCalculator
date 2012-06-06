package com.nolanlawson.relatedness.graph;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relation;
import com.nolanlawson.relatedness.util.WordWrapper;

public class RelationGraph {

	private static final String TEMPLATE = "digraph relationgraph {\n" +
			"// This attribute applies to the graph itself\n" +
			"size=\"10,10\";\n" +
			"%s" +
			"}\n";
	
	// we want to keep the graph nice and skinny
	private static final int MAX_DESIRED_LABEL_LENGTH = 16;
	
	private Map<LabelKey,String> labels = new LinkedHashMap<LabelKey,String>();
	private Set<String> nodeConnections = new LinkedHashSet<String>();
	
	private NodeNameIterator nameIterator = new NodeNameIterator();
	
	public RelationGraph() {
	}
	
	/**
	 * Add a relation to the drawing
	 * @param sourceName
	 * @param targetName
	 * @param relation
	 */
	public void addRelation(String sourceName, String targetName, Relation relation) {
		
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
			
			int unique = i / 2;  // this basically just fixes a bug with double cousins, where there are 4 common ancestors
			
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
				addNode(rightId, leftId);
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
				addNode(leftId, rightId);
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
		
		if (labelKey.getAncestorDistance() == 0) {
			return labelKey.getLabel();
		}
		
		String possessive = labelKey.getLabel().equalsIgnoreCase("you") ? "r" : "'s";
		String label = new StringBuilder()
				.append(labelKey.getLabel())
				.append(possessive)
				.append(labelKey.getAncestorId() > 0 ? " other " : " ")
				.append(createRelationString(labelKey.getAncestorDistance()))
				.toString();
		
		// add newlines where appropriate
		label = WordWrapper.wordWrap(label, MAX_DESIRED_LABEL_LENGTH);
		
		return label.replace("\n", "\\n"); // escape newlines for the DOT format
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

	private void addNode(String ancestorId, String descendantId) {
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
	
	private static class LabelKey {
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
	}
	
}
