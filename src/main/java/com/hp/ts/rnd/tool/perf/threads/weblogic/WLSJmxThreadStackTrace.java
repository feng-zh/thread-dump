package com.hp.ts.rnd.tool.perf.threads.weblogic;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.StackTraceElementWrapper;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class WLSJmxThreadStackTrace implements ThreadStackTrace {

	private String threadName;
	private State threadState;
	private String lockClassName;
	private int lockHashIdentifier;
	private String threadDetail;
	private List<StackTraceElementWrapper> stackFrames = new ArrayList<StackTraceElementWrapper>(
			20);

	public WLSJmxThreadStackTrace() {
	}

	public WLSJmxThreadStackTrace(GeneralThreadStackTrace generalTrace) {
		if (generalTrace.getProxyType().equals(getClass().getName())) {
			throw new ClassCastException(generalTrace.getProxyType());
		}
		this.threadName = generalTrace.getThreadName();
		this.threadState = generalTrace.getThreadState();
		Map<String, String> extendedInfo = generalTrace.getExtendedInfo();
		this.lockClassName = extendedInfo.get("lockClassName");
		this.lockHashIdentifier = Integer.parseInt(extendedInfo
				.get("lockHashIdentifier"));
		this.threadDetail = extendedInfo.get("threadDetail");
		this.stackFrames.addAll(Arrays.asList(Utils
				.convertStackFramesWithLocks(generalTrace.getStackFrames())));
	}

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
		if (getThreadDetail() != null && getThreadDetail().length() > 0) {
			builder.append(getThreadDetail());
		}
		builder.append('\n');
		for (StackTraceElementWrapper stackFrame : getStackFrameList()) {
			builder.append("\t").append(stackFrame.toString()).append("\n");
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
		extendedInfo.put("lockClassName", lockClassName == null ? ""
				: lockClassName);
		extendedInfo.put("lockHashIdentifier",
				String.valueOf(lockHashIdentifier));
		extendedInfo.put("threadDetail", threadDetail == null ? ""
				: threadDetail);
		return new GeneralThreadStackTrace(getClass().getName(),
				getThreadIdentifier(), getThreadName(), getThreadState(),
				frames, extendedInfo);
	}

}
