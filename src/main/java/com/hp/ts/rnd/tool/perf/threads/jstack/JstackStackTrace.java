package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;

class JstackStackTrace extends StackTraceElementWrapper {

	private List<JstackLockInfo> lockInfos;

	public JstackStackTrace(StackTraceElement stackTrace) {
		super(stackTrace);
	}

	public List<JstackLockInfo> getLockInfos() {
		return lockInfos == null ? Collections.<JstackLockInfo> emptyList()
				: lockInfos;
	}

	public void addLockInfo(JstackLockInfo lockInfo) {
		if (lockInfos == null) {
			lockInfos = new ArrayList<JstackLockInfo>(2);
		}
		lockInfos.add(lockInfo);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		if (lockInfos != null) {
			for (JstackLockInfo lockInfo : lockInfos) {
				builder.append("\n");
				builder.append(lockInfo);
			}
		}
		return builder.toString();
	}

}
