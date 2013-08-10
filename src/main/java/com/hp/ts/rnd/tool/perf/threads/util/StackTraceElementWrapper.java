package com.hp.ts.rnd.tool.perf.threads.util;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

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
		return null;
	}

	public String toString() {
		return frame.toString();
	}

	public StackTraceElement toTraceElement() {
		return frame;
	}

	public void buildStackTrace(StringBuilder builder) {
		builder.append("\tat ");
		builder.append(toString());
	}

	@Override
	public GeneralThreadStackFrame toGeneralFrame() {
		return new GeneralThreadStackFrame(getClassName(), getMethodName(),
				getFileName(), getLineNumber(), null, null);
	}

}
