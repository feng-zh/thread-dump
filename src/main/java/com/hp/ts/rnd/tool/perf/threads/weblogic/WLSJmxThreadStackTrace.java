package com.hp.ts.rnd.tool.perf.threads.weblogic;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class WLSJmxThreadStackTrace implements ThreadStackTrace {

	private String threadName;
	private State threadState;
	private String lockClassName;
	private int lockHashIdentifier;
	private String threadDetail;
	private List<StackTraceElementWrapper> stackFrames = new ArrayList<StackTraceElementWrapper>(
			20);

	@Override
	public long getThreadIdentifier() {
		return 0;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public State getThreadState() {
		return threadState;
	}

	@Override
	public ThreadStackFrame[] getStackFrames() {
		return stackFrames.toArray(new StackTraceElementWrapper[stackFrames
				.size()]);
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public void setThreadState(State threadState) {
		this.threadState = threadState;
	}

	public void setLockClassName(String lockClassName) {
		this.lockClassName = lockClassName;
	}

	public void setLockHashIdentifier(int lockHashIdentifier) {
		this.lockHashIdentifier = lockHashIdentifier;
	}

	public void setThreadDetail(String detail) {
		this.threadDetail = detail;
	}

	public String getLockClassName() {
		return lockClassName;
	}

	public int getLockHashIdentifier() {
		return lockHashIdentifier;
	}

	public String getThreadDetail() {
		return threadDetail;
	}

	public List<StackTraceElementWrapper> getStackFrameList() {
		return stackFrames;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('"').append(getThreadName()).append('"');
		if (getLockClassName() != null) {
			builder.append(" waiting for lock ").append(getLockClassName())
					.append("@")
					.append(Integer.toHexString(getLockHashIdentifier()));
		}
		builder.append(" ").append(getThreadState());
		if (getThreadDetail() != null) {
			builder.append(getThreadDetail());
		}
		builder.append('\n');
		for (StackTraceElementWrapper stackFrame : getStackFrameList()) {
			builder.append("\t").append(stackFrame.toString()).append("\n");
		}
		return builder.toString();
	}
}
