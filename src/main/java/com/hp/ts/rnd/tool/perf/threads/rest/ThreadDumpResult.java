package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class ThreadDumpResult {

	private long samplingOn;

	private long samplingDuration;

	private String stackTraces;

	public static ThreadDumpResult valueOf(ThreadSamplingState sampling) {
		ThreadDumpResult result = new ThreadDumpResult();
		result.samplingOn = sampling.getStartTimeMillis();
		result.samplingDuration = TimeUnit.NANOSECONDS.toMillis(sampling
				.getDurationTimeNanos());
		StringBuilder builder = new StringBuilder();
		for (ThreadStackTrace trace : sampling.getStackTraces()) {
			builder.append(trace);
			builder.append("\n");
		}
		result.stackTraces = builder.toString();
		return result;
	}

	public long getSamplingOn() {
		return samplingOn;
	}

	public long getSamplingDuration() {
		return samplingDuration;
	}

	public String getStackTraces() {
		return stackTraces;
	}

}
