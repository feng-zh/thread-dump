package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.util.ArrayList;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class JstackThreadStackTrace implements ThreadStackTrace {

	private String threadName;

	private boolean daemon;

	private int priority;

	private long tid;

	private int nid;

	private String status;

	// may not provide in native thread
	private Thread.State threadState;

	// may not provide in native thread
	private String detailState;

	private List<JstackStackFrame> stackFrames = new ArrayList<JstackStackFrame>(
			32);

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Thread.State getThreadState() {
		return threadState;
	}

	public void setThreadState(Thread.State threadState) {
		this.threadState = threadState;
	}

	public String getDetailState() {
		return detailState;
	}

	public void setDetailState(String detailState) {
		this.detailState = detailState;
	}

	public List<JstackStackFrame> getStackFrameList() {
		return stackFrames;
	}

	public void setStackFrameList(List<JstackStackFrame> stackFrames) {
		this.stackFrames = stackFrames;
	}

	public long getThreadIdentifier() {
		return nid;
	}

	public ThreadStackFrame[] getStackFrames() {
		return stackFrames.toArray(new JstackStackFrame[stackFrames.size()]);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('"').append(getThreadName()).append('"');
		builder.append(isDaemon() ? " daemon" : "");
		builder.append(" prio=").append(getPriority());
		String tidString = Long.toHexString(getTid());
		builder.append(" tid=0x");
		for (int i = tidString.length(); i < 16; i++) {
			builder.append('0');
		}
		builder.append(tidString);
		builder.append(" nid=0x").append(Long.toHexString(getNid()));
		builder.append(getStatus().length() > 0 ? (" " + getStatus()) : "")
				.append('\n');
		if (getThreadState() != null) {
			builder.append("   java.lang.Thread.State: ").append(
					getThreadState());
			if (getDetailState() != null) {
				builder.append(" (").append(getDetailState()).append(")");
			}
			builder.append('\n');
			for (JstackStackFrame stackFrame : getStackFrameList()) {
				stackFrame.buildStackTrace(builder);
				builder.append('\n');
			}
		}
		return builder.toString();
	}
}
