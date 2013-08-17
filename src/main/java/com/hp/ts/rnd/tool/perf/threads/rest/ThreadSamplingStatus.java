package com.hp.ts.rnd.tool.perf.threads.rest;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

class ThreadSamplingStatus {
	String uuid;
	boolean done;
	long startedOn;
	long expectedFinished;
	long finishedOn;
	long sampleCount;
	long traceCount;
	ThreadSamplingRequestForm samplingRequest;
	ThreadSamplingException error;
	String callTree;

	public String getUuid() {
		return uuid;
	}

	public boolean isDone() {
		return done;
	}

	public long getStartedOn() {
		return startedOn;
	}

	public long getExpectedFinished() {
		return expectedFinished;
	}

	public long getFinishedOn() {
		return finishedOn;
	}

	public long getSampleCount() {
		return sampleCount;
	}

	public long getTraceCount() {
		return traceCount;
	}

	public ThreadSamplingRequestForm getSamplingRequest() {
		return samplingRequest;
	}

	public ThreadSamplingException getError() {
		return error;
	}

	public String getCallTree() {
		return callTree;
	}

}