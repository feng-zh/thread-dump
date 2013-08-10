package com.hp.ts.rnd.tool.perf.threads.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;

public class StackTraceElementWrapperWithLocks extends StackTraceElementWrapper {

	private List<ThreadStackLockInfo> lockInfos;

	public StackTraceElementWrapperWithLocks(StackTraceElement stackFrame) {
		super(stackFrame);
	}

	public List<ThreadStackLockInfo> getLockInfos() {
		return lockInfos == null ? Collections
				.<ThreadStackLockInfo> emptyList() : lockInfos;
	}

	public void addLockInfo(ThreadStackLockInfo lockInfo) {
		if (lockInfos == null) {
			lockInfos = new ArrayList<ThreadStackLockInfo>(2);
		}
		lockInfos.add(lockInfo);
	}

	public void buildStackTrace(StringBuilder builder) {
		super.buildStackTrace(builder);
		if (lockInfos != null) {
			for (ThreadStackLockInfo lockInfo : lockInfos) {
				builder.append("\n\t- ");
				builder.append(lockInfo);
			}
		}
	}

	@Override
	public GeneralThreadStackFrame toGeneralFrame() {
		return new GeneralThreadStackFrame(getClassName(), getMethodName(),
				getFileName(), getLineNumber(), null, lockInfos);
	}

}
