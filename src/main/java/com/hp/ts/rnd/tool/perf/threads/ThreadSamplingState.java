package com.hp.ts.rnd.tool.perf.threads;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ThreadSamplingState {

	private long samplingTime;

	private long startTimeMillis;

	private long durationTimeNanos;

	private ThreadStackTrace[] stackTraces;

	public ThreadSamplingState() {
	}

	public ThreadSamplingState(long startTimeMillis, long durationTimeNanos) {
		this.startTimeMillis = startTimeMillis;
		this.durationTimeNanos = durationTimeNanos;
	}

	public long getSamplingTime() {
		return samplingTime;
	}

	public void setSamplingTime(long samplingTime) {
		this.samplingTime = samplingTime;
	}

	public void startSampling() {
		startTimeMillis = System.currentTimeMillis();
		durationTimeNanos = -System.nanoTime();
	}

	public void endSampling() {
		durationTimeNanos += System.nanoTime();
	}

	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public long getDurationTimeNanos() {
		return durationTimeNanos;
	}

	public void setDurationTimeNanos(long durationTimeNanos) {
		this.durationTimeNanos = durationTimeNanos;
	}

	public ThreadStackTrace[] getStackTraces() {
		return stackTraces;
	}

	public void setStackTraces(ThreadStackTrace[] stackTraces) {
		this.stackTraces = stackTraces;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(new Date(getSamplingTime()));
		builder.append("\n\n");
		for (ThreadStackTrace state : getStackTraces()) {
			builder.append(state);
			builder.append("\n");
		}
		builder.append("Sampling at: " + new Date(getStartTimeMillis())
				+ ", in: " + TimeUnit.NANOSECONDS.toMillis(durationTimeNanos)
				+ " ms");
		return builder.toString();
	}

}
