package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;

class JmxStackTrace extends StackTraceElementWrapper {

	private List<JmxLockInfo> lockInfos;

	public JmxStackTrace(StackTraceElement stackTrace) {
		super(stackTrace);
	}

	public List<JmxLockInfo> getLockInfos() {
		return lockInfos == null ? Collections.<JmxLockInfo> emptyList()
				: lockInfos;
	}

	public void addLockInfo(JmxLockInfo lockInfo) {
		if (lockInfos == null) {
			lockInfos = new ArrayList<JmxLockInfo>(2);
		}
		lockInfos.add(lockInfo);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		if (lockInfos != null) {
			for (JmxLockInfo lockInfo : lockInfos) {
				builder.append("\n");
				builder.append(lockInfo);
			}
		}
		return builder.toString();
	}
}
