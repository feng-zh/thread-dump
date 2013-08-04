package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;

class JstackStackFrame extends StackTraceElementWrapper {

	private List<JstackLockInfo> lockInfos;

	public JstackStackFrame(StackTraceElement stackFrame) {
		super(stackFrame);
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

	public void buildStackTrace(StringBuilder builder) {
		super.buildStackTrace(builder);
		if (lockInfos != null) {
			for (JstackLockInfo lockInfo : lockInfos) {
				builder.append("\n");
				lockInfo.buildStrackString(builder);
			}
		}
	}

}
