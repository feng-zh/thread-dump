package com.hp.ts.rnd.tool.perf.threads.store;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class StoredThreadCallState implements ThreadCallState {

	final private ThreadStoreRepository repository;
	final private int threadNameId;
	final private State threadState;
	final private long[] stacktraces;
	final private long threadId;

	public StoredThreadCallState(ThreadStoreRepository repository,
			ThreadCallState callState) {
		this.repository = repository;
		this.threadId = repository.createThreadId(
				callState.getThreadIdentifier(), callState.getThreadName());
		this.threadNameId = repository.createThreadNameId(callState
				.getThreadName());
		this.threadState = callState.getThreadState();
		this.stacktraces = repository.createStackTraceIds(callState
				.getStrackTraces());
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

	public ThreadStackTrace[] getStrackTraces() {
		return repository.getStackTracesByTraceIds(stacktraces);
	}

}
