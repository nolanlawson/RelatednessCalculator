package com.nolanlawson.relatedness.autosuggest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Simple, efficient implementation of a trie that maps prefixes to objects of
 * the generic type.
 * 
 * @author nlawson
 * 
 */
public class Trie<T> {

    private TrieNode root = new TrieNode();

    private Trie() {
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
    public List<T> getAll(CharSequence charSequence) {
	TrieNode currentNode = root;
	for (Character ch : Lists.charactersOf(charSequence)) {
	    currentNode = currentNode.next.get(ch);
	    if (currentNode == null) { // reached a leaf node in the trie
		return Collections.emptyList();
	    }
	}
	List<T> result = Lists.newArrayList();
	if (currentNode.value != null) {
	    result.add(currentNode.value);
	}
	getAllRecursive(currentNode, result);
	return result;
    }
    
    private void getAllRecursive(TrieNode rootNode, List<T> list) {
	for (TrieNode trieNode : rootNode.next.values()) {
	    if (trieNode.value != null) {
		list.add(trieNode.value);
	    }
	    getAllRecursive(trieNode, list);
	}
    }

    /**
     * Returns the value associated with the shortest known prefix of this
     * charSequence, or null if no known prefix exists.
     * 
     * @param charSequence
     */
    public T get(CharSequence charSequence) {
	TrieNode currentNode = root;
	for (Character ch : Lists.charactersOf(charSequence)) {
	    currentNode = currentNode.next.get(ch);
	    if (currentNode == null) { // reached a leaf node in the trie
		return null;
	    } else if (currentNode.value != null) {
		return currentNode.value;
	    }
	}
	return null;
    }

    public String toString() {
	return toStringRecursive(root, new StringBuilder(),
		Objects.toStringHelper(this)).toString();
    }

    private ToStringHelper toStringRecursive(TrieNode trieNode,
	    StringBuilder prefix, ToStringHelper toString) {

	if (trieNode.value != null) {
	    toString.add(prefix.toString(), trieNode.value);
	}

	for (Entry<Character, TrieNode> entry : trieNode.next.entrySet()) {
	    // reuse the stringbuilder if possible
	    StringBuilder newPrefix = trieNode.next.size() == 1 ? prefix
		    : new StringBuilder(prefix);

	    toStringRecursive(entry.getValue(),
		    newPrefix.append(entry.getKey()), toString);
	}

	return toString;
    }

    /**
     * Construct a new, empty Trie.
     * 
     * @return the new Trie
     */
    public static <T> Trie<T> newTrie() {
	return new Trie<T>();
    }

    private class TrieNode {

	T value;
	Map<Character, TrieNode> next = Maps.newHashMap();

    }
}