package com.hp.ts.rnd.tool.perf.threads.store;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class StoredThreadStackTrace implements ThreadStackTrace {

	final private ThreadStoreRepository repository;
	final private int threadNameId;
	final private State threadState;
	final private long[] stackFrameIds;
	final private long threadId;

	public StoredThreadStackTrace(ThreadStoreRepository repository,
			ThreadStackTrace stackTrace) {
		this.repository = repository;
		this.threadNameId = repository.createThreadNameId(stackTrace
				.getThreadName());
		this.threadId = (stackTrace.getThreadIdentifier() == THREAD_ID_NOTSUPPOT) ? threadNameId
				: stackTrace.getThreadIdentifier();
		this.threadState = stackTrace.getThreadState();
		this.stackFrameIds = repository.createStackFrameIds(stackTrace
				.getStackFrames());
	}

	StoredThreadStackTrace(ThreadStoreRepository repository,
			long threadIdentifier, int threadNameId, State threadState,
			long[] stackFrameIds) {
		this.repository = repository;
		this.threadNameId = threadNameId;
		this.threadId = (threadIdentifier == THREAD_ID_NOTSUPPOT) ? threadNameId
				: threadIdentifier;
		this.threadState = threadState;
		this.stackFrameIds = stackFrameIds;
	}

	public long getThreadIdentifier() {
		return threadId;
	}

	public String getThreadName() {
		return repository.getThreadNameByThreadNameId(threadNameId);
	}

	public State getThreadState() {
		return threadState;
	}

	public ThreadStackFrame[] getStackFrames() {
		return repository.getStackFramesByFrameIds(stackFrameIds);
	}

}
