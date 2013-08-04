package com.hp.ts.rnd.tool.perf.threads.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndexTable<T> {

	private List<T> list;
	private Map<T, Integer> map;

	public IndexTable() {
		list = new ArrayList<T>();
		map = new HashMap<T, Integer>();
	}

	public IndexTable(int initialSize) {
		list = new ArrayList<T>(initialSize);
		map = new HashMap<T, Integer>(initialSize);
	}

	public int indexOf(T element) {
		Integer index = map.get(element);
		if (index == null) {
			return -1;
		} else {
			return index;
		}
	}

	public synchronized int add(T element) {
		int index = list.size();
		map.put(element, index);
		list.add(element);
		return index;
	}

	public synchronized int putIfAbsent(T element) {
		int index = indexOf(element);
		if (index == -1) {
			return add(element);
		} else {
			return index;
		}
	}

	public T get(int index) {
		return list.get(index);
	}

	public int size() {
		return list.size();
	}

	@Override
	public String toString() {
		return String.format("IndexTable [list=%s]", list);
	}

}
