package com.nolanlawson.relatedness.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Simple implementation of a WFST (Weighted Finite State Transducer) that is
 * intended to be used for autosuggestions.
 * 
 * Define a vocabulary, define links between vocabulary items, and define
 * linking text between the vocabulary items. Vocabulary items are weighted as
 * log probabilities that get added (i.e. probabilities are multipled) as the
 * algorithm walks down the tree.
 * 
 * 
 * @author nlawson
 * 
 */
public class Trie<T> {

    private TrieNode root = new TrieNode();

    private Trie() {
    }

    /**
     * Reduces this Trie to a low-memory version.  Your mileage may vary.  Also, does
     * not support changes after the compilation is complete
     */
    public void compile() {
	compileRecursive(root);
    }
    
    
    
    private void compileRecursive(TrieNode trieNode) {
	if (trieNode.next != null) {
	    trieNode.next = SparseCharArray.fromMap(trieNode.next);
	    for (TrieNode value : trieNode.next.values()) {
		compileRecursive(value);
	    }
	}
    }

    /**
     * Map the prefix to the given value.
     * 
     * @param prefix
     * @param value
     */
    public void put(CharSequence prefix, T value) {
	TrieNode currentNode = root;
	for (Character ch : Lists.charactersOf(prefix)) {
	    TrieNode nextNode = currentNode.next.get(ch);
	    if (nextNode == null) {
		nextNode = new TrieNode();
		currentNode.next.put(ch, nextNode);
	    }
	    currentNode = nextNode;
	}
	currentNode.value = value;
    }

    /**
     * Find all possible leaf nodes from this prefix
     * 
     * @param charSequence
     * @return
     */
    public List<TrieLeaf<T>> getAll(CharSequence charSequence) {
	TrieNode currentNode = root;
	for (Character ch : Lists.charactersOf(charSequence)) {
	    currentNode = currentNode.next.get(ch);
	    if (currentNode == null) { // reached a leaf node in the trie
		return Collections.emptyList();
	    }
	}
	List<TrieLeaf<T>> result = Lists.newArrayList();
	getAllRecursive(currentNode, result, new StringBuilder(charSequence));
	return result;
    }

    private void getAllRecursive(TrieNode rootNode, List<TrieLeaf<T>> list, StringBuilder stringBuilder) {
	if (rootNode.value != null) {
	    list.add(new TrieLeaf<T>(stringBuilder, rootNode.value));
	}
	for (Entry<Character,TrieNode> entry : rootNode.next.entrySet()) {
	    getAllRecursive(entry.getValue(), list, new StringBuilder(stringBuilder).append(entry.getKey()));
	}
    }

    /**
     * Construct a new, empty Trie.
     * 
     * @return the new Trie
     */
    public static <T> Trie<T> newTrie() {
	return new Trie<T>();
    }
    
    public String toString() {
	SortedMap<String, T> allEndNodes = Maps.newTreeMap();
	toStringRecursive(root, allEndNodes, new StringBuilder());
	return "Trie<\n" + Joiner.on('\n').withKeyValueSeparator(": ").join(allEndNodes) + "\n>";
    }

    private void toStringRecursive(TrieNode node,
	    SortedMap<String, T> map, StringBuilder stringBuilder) {
	if (node.value != null) {
	    map.put(stringBuilder.toString(), node.value);
	}
	for (Entry<Character, TrieNode> entry : node.next.entrySet()) {
	    toStringRecursive(entry.getValue(), map, new StringBuilder(stringBuilder).append(entry.getKey()));
	}
    }

    /**
     * Represents an end node in the Trie structure.
     * 
     * @author nolan
     * 
     * @param <E>
     */
    public static class TrieLeaf<E> {

	private CharSequence key;
	private E value;

	private TrieLeaf(CharSequence key, E value) {
	    this.key = key;
	    this.value = value;
	}

	public CharSequence getKey() {
	    return key;
	}

	public E getValue() {
	    return value;
	}

    }

    private class TrieNode {

	T value;
	Map<Character, TrieNode> next = Maps.newHashMap();

    }
}