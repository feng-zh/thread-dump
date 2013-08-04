package com.hp.ts.rnd.tool.perf.threads;

public class StackTraceElementWrapper implements ThreadStackFrame {

	final private StackTraceElement frame;

	public StackTraceElementWrapper(StackTraceElement frame) {
		this.frame = frame;
	}

	public String getFileName() {
		return frame.getFileName();
	}

	public int getLineNumber() {
		return frame.getLineNumber();
	}

	public String getClassName() {
		return frame.getClassName();
	}

	public String getMethodName() {
		return frame.getMethodName();
	}

	public Object getStackFrameId() {
		return frame.toString();
	}

	public String toString() {
		return frame.toString();
	}

	public void buildStackTrace(StringBuilder builder) {
		builder.append("\tat ");
		builder.append(toString());
	}

}
