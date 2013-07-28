package com.hp.ts.rnd.tool.perf.threads;

import java.util.Date;

public class ThreadSamplingState {

	private long samplingTime;

	private ThreadCallState[] callStates;

	public long getSamplingTime() {
		return samplingTime;
	}

	public void setSamplingTime(long samplingTime) {
		this.samplingTime = samplingTime;
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
		return builder.toString();
	}

}
