package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadSamplingHandler {

	public void onSampling(ThreadSamplingState state);

	public void onError(ThreadSamplingException exception)
			throws ThreadSamplingException;

	public void onEnd();

}
