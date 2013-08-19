package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.concurrent.TimeUnit;

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

	public int getEstimatedFinishedSec() {
		long estimated = expectedFinished - System.currentTimeMillis();
		if (estimated <= 0) {
			return 0;
		} else {
			return (int) TimeUnit.MILLISECONDS.toSeconds(estimated);
		}
	}

	public int getEstimatedProgress() {
		int progress = (int) ((expectedFinished - System.currentTimeMillis()) * 100.0 / (expectedFinished - startedOn));
		return 100 - Math.min(100, Math.max(0, progress));
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