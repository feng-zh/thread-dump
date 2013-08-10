package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

class StoredThreadStackFrame implements ThreadStackFrame {

	final private ThreadStoreRepository repository;

	final private long frameId;

	public StoredThreadStackFrame(ThreadStoreRepository repository,
			ThreadStackFrame stackFrame) {
		this.repository = repository;
		this.frameId = repository.createStackFrameId(stackFrame);
	}

	StoredThreadStackFrame(ThreadStoreRepository repository, long frameId) {
		this.repository = repository;
		this.frameId = frameId;
	}

	public String getFileName() {
		return repository.getFileNameByFrameId(frameId);
	}

	public int getLineNumber() {
		return repository.getLineNumberByFrameId(frameId);
	}

	public String getClassName() {
		return repository.getClassNameByFrameId(frameId);
	}

	public String getMethodName() {
		return repository.getMethodNameByFrameId(frameId);
	}

	public Long getStackFrameId() {
		return frameId;
	}

	public StackTraceElement toTraceElement() {
		return repository.getStackTraceElementByFrameId(frameId);
	}

	ThreadStoreRepository getRepository() {
		return repository;
	}

	@Override
	public GeneralThreadStackFrame toGeneralFrame() {
		return new GeneralThreadStackFrame(getClassName(), getMethodName(),
				getFileName(), getLineNumber(), String.valueOf(frameId), null);
	}

}
