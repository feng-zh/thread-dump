package com.hp.ts.rnd.tool.perf.threads;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ThreadSamplingState {

	private long samplingTime;

	private long startTimeMillis;

	private long durationTimeNanos;

	private ThreadCallState[] callStates;

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

	public ThreadCallState[] getCallStates() {
		return callStates;
	}

	public void setCallStates(ThreadCallState[] callStates) {
		this.callStates = callStates;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(new Date(getSamplingTime()));
		builder.append("\n\n");
		for (ThreadCallState state : getCallStates()) {
			builder.append(state);
			builder.append("\n");
		}
		builder.append("Sampling at: " + new Date(getStartTimeMillis())
				+ ", in: " + TimeUnit.NANOSECONDS.toMillis(durationTimeNanos)
				+ " ms");
		return builder.toString();
	}

}
