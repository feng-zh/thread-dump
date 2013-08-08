package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class JmxThreadStackTrace implements ThreadStackTrace {

	private String threadName;
	private long threadIdentifier;
	private State threadState;
	private String detailState;
	private JmxStackFrame[] stackFrames;

	public JmxThreadStackTrace(ThreadInfo threadInfo) {
		this.threadName = threadInfo.getThreadName();
		this.threadIdentifier = threadInfo.getThreadId();
		this.threadState = threadInfo.getThreadState();
		StackTraceElement[] stackTrace = threadInfo.getStackTrace();
		this.stackFrames = new JmxStackFrame[stackTrace.length];
		for (int i = 0, n = stackTrace.length; i < n; i++) {
			JmxStackFrame jmxStackFrame = new JmxStackFrame(stackTrace[i]);
			if (i == 0) {
				LockInfo lockInfo = threadInfo.getLockInfo();
				if (lockInfo != null) {
					JmxLockInfo jmxLockInfo = new JmxLockInfo();
					jmxLockInfo.setLockClassName(lockInfo.getClassName());
					jmxLockInfo.setLockIdentityHashCode(lockInfo
							.getIdentityHashCode());
					jmxLockInfo.setOwnLock(false);
					jmxStackFrame.addLockInfo(jmxLockInfo);
					this.detailState = "on object monitor";
					boolean parking = false;
					if ("sun.misc.Unsafe".equals(jmxStackFrame.getClassName())
							&& "park".equals(jmxStackFrame.getMethodName())) {
						// parking
						parking = true;
						this.detailState = "parking";
					}
					switch (threadState) {
					case BLOCKED:
						jmxLockInfo.setLockState("blocked on");
						break;
					case WAITING:
						jmxLockInfo.setLockState("waiting on");
						break;
					case TIMED_WAITING:
						jmxLockInfo.setLockState("waiting on");
						break;
					default:
					}
					if (parking) {
						jmxLockInfo.setLockState("parking to wait for ");
					}
				}
			}
			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					JmxLockInfo jmxLockInfo = new JmxLockInfo();
					jmxLockInfo.setLockClassName(mi.getClassName());
					jmxLockInfo.setLockIdentityHashCode(mi
							.getIdentityHashCode());
					jmxLockInfo.setOwnLock(true);
					jmxLockInfo.setLockState("locked");
					jmxStackFrame.addLockInfo(jmxLockInfo);
				}
			}
			this.stackFrames[i] = jmxStackFrame;
		}
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
			if (getDetailState() != null) {
				builder.append(" (").append(getDetailState()).append(")");
			}
			builder.append('\n');
			for (JmxStackFrame stackFrame : stackFrames) {
				stackFrame.buildStackTrace(builder);
				builder.append('\n');
			}
		}
		return builder.toString();
	}

}
