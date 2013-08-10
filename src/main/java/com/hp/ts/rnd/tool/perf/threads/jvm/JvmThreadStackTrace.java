package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.lang.Thread.State;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.StackTraceElementWrapper;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class JvmThreadStackTrace implements ThreadStackTrace {

	private String threadName;
	private long threadIdentifier;
	private StackTraceElementWrapper[] stackFrames;
	private State threadState;
	private boolean daemon;
	private int priority;

	public JvmThreadStackTrace(Thread thread, StackTraceElement[] stackFrames) {
		this.threadName = thread.getName();
		this.threadIdentifier = thread.getId();
		this.threadState = thread.getState();
		this.daemon = thread.isDaemon();
		this.priority = thread.getPriority();
		this.stackFrames = Utils.createStackFrames(stackFrames);
	}

	public JvmThreadStackTrace(GeneralThreadStackTrace generalTrace) {
		if (generalTrace.getProxyType().equals(getClass().getName())) {
			throw new ClassCastException(generalTrace.getProxyType());
		}
		this.threadName = generalTrace.getThreadName();
		this.threadIdentifier = generalTrace.getThreadIdentifier();
		this.threadState = generalTrace.getThreadState();
		this.daemon = Boolean.valueOf(generalTrace.getExtendedInfo().get(
				"daemon"));
		this.priority = Integer.parseInt(generalTrace.getExtendedInfo().get(
				"priority"));
		this.stackFrames = Utils.convertStackFrames(generalTrace
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

	@Override
	public GeneralThreadStackTrace toGeneralTrace() {
		ThreadStackFrame[] stackFrames = getStackFrames();
		GeneralThreadStackFrame[] frames = new GeneralThreadStackFrame[stackFrames.length];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = stackFrames[i].toGeneralFrame();
		}
		Map<String, String> extendedInfo = new LinkedHashMap<String, String>();
		extendedInfo.put("deamon", String.valueOf(daemon));
		extendedInfo.put("priority", String.valueOf(priority));
		return new GeneralThreadStackTrace(getClass().getName(),
				getThreadIdentifier(), getThreadName(), getThreadState(),
				frames, extendedInfo);
	}

}
