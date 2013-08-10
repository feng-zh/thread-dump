package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadSamplingService {

	public void executeSampling(ThreadSamplingHandler handler)
			throws ThreadSamplingException;

	public void closeSampling();

}
