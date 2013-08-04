package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadStackFrame {

	public String getFileName();

	public int getLineNumber();

	public String getClassName();

	public String getMethodName();

	public Object getStackFrameId();

}
