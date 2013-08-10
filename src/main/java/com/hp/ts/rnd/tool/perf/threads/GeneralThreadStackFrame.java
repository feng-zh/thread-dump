package com.hp.ts.rnd.tool.perf.threads;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.util.ThreadStackLockInfo;

public class GeneralThreadStackFrame implements ThreadStackFrame, Serializable {

	private static final long serialVersionUID = -3869137781610163661L;

	private String fileName;

	private int lineNumber;

	private String className;

	private String methodName;

	private String stackFrameId;

	private List<ThreadStackLockInfo> lockInfos;

	@ConstructorProperties({ "className", "methodName", "fileName",
			"lineNumber", "stackFrameId", "lockInfos" })
	public GeneralThreadStackFrame(String className, String methodName,
			String fileName, int lineNumber, String stackFrameId,
			List<ThreadStackLockInfo> lockInfos) {
		this.className = className;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.stackFrameId = stackFrameId;
		this.lockInfos = lockInfos;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getStackFrameId() {
		return stackFrameId;
	}

	public StackTraceElement toTraceElement() {
		return new StackTraceElement(className, methodName, fileName,
				lineNumber);
	}

	public List<ThreadStackLockInfo> getLockInfos() {
		return lockInfos;
	}

	@Override
	public GeneralThreadStackFrame toGeneralFrame() {
		return this;
	}

}
