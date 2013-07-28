package com.hp.ts.rnd.tool.perf.threads;

public class StackTraceElementWrapper implements ThreadStackTrace {

	final private StackTraceElement stackTrace;

	public StackTraceElementWrapper(StackTraceElement stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getFileName() {
		return stackTrace.getFileName();
	}

	public int getLineNumber() {
		return stackTrace.getLineNumber();
	}

	public String getClassName() {
		return stackTrace.getClassName();
	}

	public String getMethodName() {
		return stackTrace.getMethodName();
	}

	public Object getTraceIdentifier() {
		return stackTrace.toString();
	}

	public String toString() {
		return "\tat " + stackTrace.toString();
	}

}
