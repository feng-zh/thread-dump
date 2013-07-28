package com.hp.ts.rnd.tool.perf.threads.jmx;

class JmxLockInfo {

	private String lockState;

	private boolean ownLock;

	private int lockIdentityHashCode;

	private String lockClassName;

	public String getLockState() {
		return lockState;
	}

	public void setLockState(String lockState) {
		this.lockState = lockState;
	}

	public boolean isOwnLock() {
		return ownLock;
	}

	public void setOwnLock(boolean ownLock) {
		this.ownLock = ownLock;
	}

	public int getLockIdentityHashCode() {
		return lockIdentityHashCode;
	}

	public void setLockIdentityHashCode(int lockIdentityHashCode) {
		this.lockIdentityHashCode = lockIdentityHashCode;
	}

	public String getLockClassName() {
		return lockClassName;
	}

	public void setLockClassName(String lockClassName) {
		this.lockClassName = lockClassName;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\t- ");
		builder.append(getLockState());
		builder.append(" <0x");
		String idString = String.valueOf(Long
				.toHexString(getLockIdentityHashCode()));
		for (int i = idString.length(); i < 16; i++) {
			builder.append('0');
		}
		builder.append(idString);
		builder.append("> (a ");
		builder.append(getLockClassName());
		builder.append(")");
		return builder.toString();
	}

}
