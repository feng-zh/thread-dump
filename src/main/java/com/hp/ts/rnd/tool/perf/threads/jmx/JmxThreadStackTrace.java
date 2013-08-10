package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.StackTraceElementWrapperWithLocks;
import com.hp.ts.rnd.tool.perf.threads.util.ThreadStackLockInfo;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class JmxThreadStackTrace implements ThreadStackTrace {

	private String threadName;
	private long threadIdentifier;
	private State threadState;
	private String detailState;
	private StackTraceElementWrapperWithLocks[] stackFrames;

	public JmxThreadStackTrace(ThreadInfo threadInfo) {
		this.threadName = threadInfo.getThreadName();
		this.threadIdentifier = threadInfo.getThreadId();
		this.threadState = threadInfo.getThreadState();
		StackTraceElement[] stackTrace = threadInfo.getStackTrace();
		this.stackFrames = new StackTraceElementWrapperWithLocks[stackTrace.length];
		for (int i = 0, n = stackTrace.length; i < n; i++) {
			StackTraceElementWrapperWithLocks jmxStackFrame = new StackTraceElementWrapperWithLocks(
					stackTrace[i]);
			if (i == 0) {
				LockInfo lockInfo = threadInfo.getLockInfo();
				if (lockInfo != null) {
					this.detailState = "on object monitor";
					boolean parking = false;
					if ("sun.misc.Unsafe".equals(jmxStackFrame.getClassName())
							&& "park".equals(jmxStackFrame.getMethodName())) {
						// parking
						parking = true;
						this.detailState = "parking";
					}
					String lockState;
					switch (threadState) {
					case BLOCKED:
						lockState = "blocked on";
						break;
					case WAITING:
						lockState = "waiting on";
						break;
					case TIMED_WAITING:
						lockState = "waiting on";
						break;
					default:
						lockState = null;
						break;
					}
					if (parking) {
						lockState = "parking to wait for ";
					}
					ThreadStackLockInfo jmxLockInfo = new ThreadStackLockInfo(
							lockInfo.getClassName(),
							lockInfo.getIdentityHashCode(), lockState, false);
					jmxStackFrame.addLockInfo(jmxLockInfo);
				}
			}
			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					ThreadStackLockInfo jmxLockInfo = new ThreadStackLockInfo(
							mi.getClassName(), mi.getIdentityHashCode(),
							"locked", true);
					jmxStackFrame.addLockInfo(jmxLockInfo);
				}
			}
			this.stackFrames[i] = jmxStackFrame;
		}
	}

	public JmxThreadStackTrace(GeneralThreadStackTrace generalTrace) {
		if (generalTrace.getProxyType().equals(getClass().getName())) {
			throw new ClassCastException(generalTrace.getProxyType());
		}
		this.threadName = generalTrace.getThreadName();
		this.threadIdentifier = generalTrace.getThreadIdentifier();
		this.threadState = generalTrace.getThreadState();
		Map<String, String> extendedInfo = generalTrace.getExtendedInfo();
		this.detailState = extendedInfo.get("detailState");
		this.stackFrames = Utils.convertStackFramesWithLocks(generalTrace
				.getStackFrames());
	}

	public long getThreadIdentifier() {
		return threadIdentifier;
	}

	public String getThreadName() {
		return threadName;
	}

	public State getThreadState() {
		return threadState;
	}

	public ThreadStackFrame[] getStackFrames() {
		return stackFrames;
	}

	public String getDetailState() {
		return detailState;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('"').append(getThreadName()).append('"');
		builder.append(" id=").append(getThreadIdentifier()).append('\n');
		if (getThreadState() != null) {
			builder.append("   java.lang.Thread.State: ").append(
					getThreadState());
			if (getDetailState() != null && getDetailState().length() > 0) {
				builder.append(" (").append(getDetailState()).append(")");
			}
			builder.append('\n');
			for (StackTraceElementWrapperWithLocks stackFrame : stackFrames) {
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
		extendedInfo.put("detailState", detailState == null ? "" : detailState);
		return new GeneralThreadStackTrace(getClass().getName(),
				getThreadIdentifier(), getThreadName(), getThreadState(),
				frames, extendedInfo);
	}

}
