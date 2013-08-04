package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;
import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.Utils;

class JvmThreadCallState implements ThreadCallState {

	private String threadName;
	private long threadIdentifier;
	private StackTraceElementWrapper[] stackFrames;
	private State threadState;
	private boolean daemon;
	private int priority;

	public JvmThreadCallState(Thread thread, StackTraceElement[] stackFrames) {
		this.threadName = thread.getName();
		this.threadIdentifier = thread.getId();
		this.threadState = thread.getState();
		this.daemon = thread.isDaemon();
		this.priority = thread.getPriority();
		this.stackFrames = Utils.createStackTraceWrappers(stackFrames);
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
			for (StackTraceElementWrapper stackFrame : stackFrames) {
				stackFrame.buildStackTrace(builder);
				builder.append('\n');
			}
		}
		return builder.toString();
	}

}
