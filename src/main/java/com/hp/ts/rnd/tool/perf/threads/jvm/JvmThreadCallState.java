package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.Utils;

class JvmThreadCallState implements ThreadCallState {

	private String threadName;
	private long threadIdentifier;
	private ThreadStackTrace[] stackTraces;
	private State threadState;
	private boolean daemon;
	private int priority;

	public JvmThreadCallState(Thread thread, StackTraceElement[] traces) {
		this.threadName = thread.getName();
		this.threadIdentifier = thread.getId();
		this.threadState = thread.getState();
		this.daemon = thread.isDaemon();
		this.priority = thread.getPriority();
		this.stackTraces = Utils.createStackTraceWrappers(traces);
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

	public boolean isDaemon() {
		return daemon;
	}

	public int getPriority() {
		return priority;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('"').append(getThreadName()).append('"');
		builder.append(isDaemon() ? " daemon" : "");
		builder.append(" prio=").append(getPriority());
		builder.append(" id=").append(getThreadIdentifier());
		builder.append('\n');
		if (getThreadState() != null) {
			builder.append("   java.lang.Thread.State: ").append(
					getThreadState());
			builder.append('\n');
			for (ThreadStackTrace stacktrace : stackTraces) {
				builder.append(stacktrace).append("\n");
			}
		}
		return builder.toString();
	}

}
