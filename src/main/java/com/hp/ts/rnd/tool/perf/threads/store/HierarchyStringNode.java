package com.hp.ts.rnd.tool.perf.threads.store;

class HierarchyStringNode {

	private final String chars;

	private final int parentIndex;

	private Object value;

	private int index;

	public HierarchyStringNode(int parentIndex, String chars) {
		if (chars == null) {
			throw new NullPointerException("null chars");
		}
		this.parentIndex = parentIndex;
		this.chars = chars;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getChars() {
		return chars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + parentIndex;
		result = prime * result + chars.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HierarchyStringNode))
			return false;
		HierarchyStringNode other = (HierarchyStringNode) obj;
		if (parentIndex != other.parentIndex)
			return false;
		if (!chars.equals(other.chars))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("HierarchyStringNode [parentIndex=%s, index=%s, chars=%s, value=%s]",
						parentIndex, index, chars, value);
	}

}
