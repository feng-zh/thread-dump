package com.hp.ts.rnd.tool.perf.threads.util;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class ThreadStackLockInfo implements Serializable {

	private static final long serialVersionUID = 6872751729170847532L;

	private String lockState;

	private boolean ownLock;

	private long lockIdentityHashCode;

	private String lockClassName;

	@ConstructorProperties({ "lockClassName", "lockIdentityHashCode",
			"lockState", "ownLock" })
	public ThreadStackLockInfo(String lockClassName, long lockIdentityHashCode,
			String lockState, boolean ownLock) {
		this.lockClassName = lockClassName;
		this.lockIdentityHashCode = lockIdentityHashCode;
		this.lockState = lockState;
		this.ownLock = ownLock;
	}

	public String getLockState() {
		return lockState;
	}

	public boolean isOwnLock() {
		return ownLock;
	}

	public long getLockIdentityHashCode() {
		return lockIdentityHashCode;
	}

	public String getLockClassName() {
		return lockClassName;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
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
