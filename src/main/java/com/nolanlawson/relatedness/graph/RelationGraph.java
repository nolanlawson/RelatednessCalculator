package com.nolanlawson.relatedness.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nolanlawson.relatedness.CommonAncestor;
import com.nolanlawson.relatedness.Relation;

public class RelationGraph {

	private static final String TEMPLATE = "digraph relationgraph {\n" +
			"// This attribute applies to the graph itself\n" +
			"size=\"10,10\";\n" +
			"%s" +
			"}\n";
	
	private Map<LabelKey,String> labels = new HashMap<LabelKey,String>();
	private Set<String> nodeConnections = new HashSet<String>();
	
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
		
		for (CommonAncestor commonAncestor : relation.getCommonAncestors()) {
			for (int i = 1; i <= commonAncestor.getDistanceFromFirst(); i++) {
				addNode(sourceName, targetName, i, commonAncestor.getDistanceFromFirst(), commonAncestor.getDistanceFromSecond());
			}
			for (int i = 1; i <= commonAncestor.getDistanceFromSecond(); i++) {
				addNode(targetName, sourceName, i, commonAncestor.getDistanceFromSecond(), commonAncestor.getDistanceFromFirst());
			}			
		}
		
	}
	
	private void addNode(String name1, String name2, int i, int totalDistance, int distanceOfSecond) {
		
		String ancestorId = (i == totalDistance && distanceOfSecond == 0) ? getId(name2, 0) : getId(name1, i);
		String descendantId = ((i-1) == totalDistance && distanceOfSecond == 0) ? getId(name2, 0) : getId(name1, (i-1));
		addNode(ancestorId, descendantId);
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
			stringBuilder.append(nodeConnection).append('\n');
		}
		return String.format(TEMPLATE, stringBuilder);
	}

	private String createHumanReadableLabel(LabelKey labelKey) {
		// TODO allow for languages other than English
		StringBuilder stringBuilder = new StringBuilder(labelKey.getLabel());
		for (int i = 0; i < labelKey.getAncestorDistance(); i++) {
			stringBuilder.append("'s parent");
		}
		return stringBuilder.toString();
	}

	private void addNode(String ancestorId, String descendantId) {
		// DOT notation for a directed graph
		nodeConnections.add(String.format("%s -> %s", ancestorId, descendantId));
		
	}

	private String getId(String label, int ancestorDistance) {
		LabelKey labelKey = new LabelKey(label, ancestorDistance);
		if (!labels.containsKey(labelKey)) {
			labels.put(labelKey, nameIterator.next());
		}
		return labels.get(labelKey);
	}
	
	private static class LabelKey {
		private String label;
		private int ancestorDistance;
		
		private LabelKey(String label, int ancestorDistance) {
			super();
			this.label = label;
			this.ancestorDistance = ancestorDistance;
		}
		
		
		public String getLabel() {
			return label;
		}


		public int getAncestorDistance() {
			return ancestorDistance;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ancestorDistance;
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
					+ ancestorDistance + "]";
		}
	}
	
}
