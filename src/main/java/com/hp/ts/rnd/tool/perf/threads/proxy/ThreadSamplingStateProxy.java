package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class ThreadSamplingStateProxy implements Serializable {

	private static final long serialVersionUID = -8482566985824954941L;

	private long samplingTime;

	private long startTimeMillis;

	private long durationTimeNanos;

	private GeneralThreadStackTrace[] stackTraces;

	@ConstructorProperties({ "samplingTime", "startTimeMillis",
			"durationTimeNanos", "stackTraces" })
	public ThreadSamplingStateProxy(long samplingTime, long startTimeMillis,
			long durationTimeNanos, GeneralThreadStackTrace[] stackTraces) {
		this.samplingTime = samplingTime;
		this.startTimeMillis = startTimeMillis;
		this.durationTimeNanos = durationTimeNanos;
		this.stackTraces = stackTraces;
	}

	public ThreadSamplingStateProxy(ThreadSamplingState sampling) {
		this.samplingTime = sampling.getSamplingTime();
		this.startTimeMillis = sampling.getStartTimeMillis();
		this.durationTimeNanos = sampling.getDurationTimeNanos();
		ThreadStackTrace[] traces = sampling.getStackTraces();
		this.stackTraces = new GeneralThreadStackTrace[traces.length];
		for (int i = 0; i < traces.length; i++) {
			this.stackTraces[i] = traces[i].toGeneralTrace();
		}
	}

	public long getSamplingTime() {
		return samplingTime;
	}

	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public long getDurationTimeNanos() {
		return durationTimeNanos;
	}

	public GeneralThreadStackTrace[] getStackTraces() {
		return stackTraces;
	}

}
