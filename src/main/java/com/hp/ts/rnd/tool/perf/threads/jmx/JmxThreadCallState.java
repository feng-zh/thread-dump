package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class JmxThreadCallState implements ThreadCallState {

	private String threadName;
	private long threadIdentifier;
	private State threadState;
	private String detailState;
	private JmxStackTrace[] stackTraces;

	public JmxThreadCallState(ThreadInfo threadInfo) {
		this.threadName = threadInfo.getThreadName();
		this.threadIdentifier = threadInfo.getThreadId();
		this.threadState = threadInfo.getThreadState();
		StackTraceElement[] traces = threadInfo.getStackTrace();
		JmxStackTrace[] stackTraces = new JmxStackTrace[traces.length];
		for (int i = 0, n = traces.length; i < n; i++) {
			JmxStackTrace jmxStackTrace = new JmxStackTrace(traces[i]);
			if (i == 0) {
				LockInfo lockInfo = threadInfo.getLockInfo();
				if (lockInfo != null) {
					JmxLockInfo jmxLockInfo = new JmxLockInfo();
					jmxLockInfo.setLockClassName(lockInfo.getClassName());
					jmxLockInfo.setLockIdentityHashCode(lockInfo
							.getIdentityHashCode());
					jmxLockInfo.setOwnLock(false);
					jmxStackTrace.addLockInfo(jmxLockInfo);
					this.detailState = "on object monitor";
					boolean parking = false;
					if ("sun.misc.Unsafe".equals(jmxStackTrace.getClassName())
							&& "park".equals(jmxStackTrace.getMethodName())) {
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
					jmxStackTrace.addLockInfo(jmxLockInfo);
				}
			}
			stackTraces[i] = jmxStackTrace;
		}
		this.stackTraces = stackTraces;
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

	public ThreadStackTrace[] getStrackTraces() {
		return stackTraces;
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
			for (ThreadStackTrace stacktrace : stackTraces) {
				builder.append(stacktrace).append("\n");
			}
		}
		return builder.toString();
	}

}
