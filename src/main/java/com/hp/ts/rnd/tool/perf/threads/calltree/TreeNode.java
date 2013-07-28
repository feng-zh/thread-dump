package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TreeNode<K, V> {

	private K key;

	private V value;

	private Map<K, TreeNode<K, V>> children;

	public TreeNode(K key) {
		this.key = key;
	}

	public K getKey() {
		return this.key;
	}

	public TreeNode<K, V> getChild(K key, boolean createIfNotFound) {
		if (children == null) {
			if (!createIfNotFound) {
				return null;
			} else {
				children = new HashMap<K, TreeNode<K, V>>();
			}
		}
		TreeNode<K, V> child = children.get(key);
		if (child == null) {
			if (!createIfNotFound) {
				return null;
			} else {
				child = new TreeNode<K, V>(key);
				children.put(key, child);
				return child;
			}
		} else {
			return child;
		}
	}

	public Collection<TreeNode<K, V>> listChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children.values();
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

}