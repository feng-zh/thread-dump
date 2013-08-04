package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;

class JmxStackFrame extends StackTraceElementWrapper {

	private List<JmxLockInfo> lockInfos;

	public JmxStackFrame(StackTraceElement stackFrame) {
		super(stackFrame);
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

	public void buildStackTrace(StringBuilder builder) {
		super.buildStackTrace(builder);
		if (lockInfos != null) {
			for (JmxLockInfo lockInfo : lockInfos) {
				builder.append("\n");
				lockInfo.buildStrackString(builder);
			}
		}
	}

}
