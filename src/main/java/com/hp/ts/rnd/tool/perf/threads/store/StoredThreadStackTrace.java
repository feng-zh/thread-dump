package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class StoredThreadStackTrace implements ThreadStackTrace {

	final private ThreadStoreRepository repository;

	final private long traceId;

	public StoredThreadStackTrace(ThreadStoreRepository repository,
			ThreadStackTrace trace) {
		this.repository = repository;
		this.traceId = repository.createTraceId(trace);
	}

	StoredThreadStackTrace(ThreadStoreRepository repository, long traceId) {
		this.repository = repository;
		this.traceId = traceId;
	}

	public String getFileName() {
		return repository.getFileNameByTraceId(traceId);
	}

	public int getLineNumber() {
		return repository.getLineNumberByTraceId(traceId);
	}

	public String getClassName() {
		return repository.getClassNameByTraceId(traceId);
	}

	public String getMethodName() {
		return repository.getMethodNameByTraceId(traceId);
	}

	public Object getTraceIdentifier() {
		return traceId;
	}

}
