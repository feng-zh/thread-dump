package com.hp.ts.rnd.tool.perf.threads.store;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

public class StoredThreadCallState implements ThreadCallState {

	final private ThreadStoreRepository repository;
	final private int threadNameId;
	final private State threadState;
	final private long[] stackFrameIds;
	final private long threadId;

	public StoredThreadCallState(ThreadStoreRepository repository,
			ThreadCallState callState) {
		this.repository = repository;
		this.threadNameId = repository.createThreadNameId(callState
				.getThreadName());
		this.threadId = (callState.getThreadIdentifier() == THREAD_ID_NOTSUPPOT) ? threadNameId
				: callState.getThreadIdentifier();
		this.threadState = callState.getThreadState();
		this.stackFrameIds = repository.createStackFrameIds(callState
				.getStackFrames());
	}

	StoredThreadCallState(ThreadStoreRepository repository,
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
