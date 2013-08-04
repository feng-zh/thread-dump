package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadSampler {

	public ThreadSamplingState sampling() throws ThreadSamplingException,
			EndOfSamplingException;

}
