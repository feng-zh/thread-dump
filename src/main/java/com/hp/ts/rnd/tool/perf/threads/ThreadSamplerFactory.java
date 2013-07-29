package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadSamplerFactory {

	public ThreadSampler getSampler() throws ThreadSamplingException;

	public void close();

}
