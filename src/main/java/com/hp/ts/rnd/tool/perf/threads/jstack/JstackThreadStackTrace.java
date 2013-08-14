package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.StackTraceElementWrapperWithLocks;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class JstackThreadStackTrace implements ThreadStackTrace {

	private String threadName;

	private boolean daemon;

	private int priority;

	private long tid;

	private long nid;

	private String status;

	// may not provide in native thread
	private Thread.State threadState;

	// may not provide in native thread
	private String detailState;

	private List<StackTraceElementWrapperWithLocks> stackFrames = new ArrayList<StackTraceElementWrapperWithLocks>(
			32);

	public JstackThreadStackTrace() {
	}

	public JstackThreadStackTrace(GeneralThreadStackTrace generalTrace) {
		if (generalTrace.getProxyType().equals(getClass().getName())) {
			throw new ClassCastException(generalTrace.getProxyType());
		}
		this.threadName = generalTrace.getThreadName();
		this.nid = generalTrace.getThreadIdentifier();
		this.threadState = generalTrace.getThreadState();
		Map<String, String> extendedInfo = generalTrace.getExtendedInfo();
		this.daemon = Boolean.valueOf(extendedInfo.get("daemon"));
		this.priority = Integer.parseInt(extendedInfo.get("priority"));
		this.tid = Long.parseLong(extendedInfo.get("tid"));
		this.status = extendedInfo.get("status");
		this.detailState = extendedInfo.get("detailState");
		this.stackFrames.addAll(Arrays.asList(Utils
				.convertStackFramesWithLocks(generalTrace.getStackFrames())));
	}

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

	public long getNid() {
		return nid;
	}

	public void setNid(long nid) {
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

	public List<StackTraceElementWrapperWithLocks> getStackFrameList() {
		return stackFrames;
	}

	public void setStackFrameList(
			List<StackTraceElementWrapperWithLocks> stackFrames) {
		this.stackFrames = stackFrames;
	}

	public long getThreadIdentifier() {
		return nid;
	}

	public ThreadStackFrame[] getStackFrames() {
		return stackFrames
				.toArray(new StackTraceElementWrapperWithLocks[stackFrames
						.size()]);
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
			if (getDetailState() != null && getDetailState().length() > 0) {
				builder.append(" (").append(getDetailState()).append(")");
			}
			builder.append('\n');
			for (StackTraceElementWrapperWithLocks stackFrame : getStackFrameList()) {
				stackFrame.buildStackTrace(builder);
				builder.append('\n');
			}
		}
		return builder.toString();
	}

	@Override
	public GeneralThreadStackTrace toGeneralTrace() {
		ThreadStackFrame[] stackFrames = getStackFrames();
		GeneralThreadStackFrame[] frames = new GeneralThreadStackFrame[stackFrames.length];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = stackFrames[i].toGeneralFrame();
		}
		Map<String, String> extendedInfo = new LinkedHashMap<String, String>();
		extendedInfo.put("daemon", String.valueOf(daemon));
		extendedInfo.put("priority", String.valueOf(priority));
		extendedInfo.put("tid", String.valueOf(tid));
		extendedInfo.put("status", status);
		extendedInfo.put("detailState", detailState == null ? "" : detailState);
		return new GeneralThreadStackTrace(getClass().getName(),
				getThreadIdentifier(), getThreadName(), getThreadState(),
				frames, extendedInfo);
	}
}
