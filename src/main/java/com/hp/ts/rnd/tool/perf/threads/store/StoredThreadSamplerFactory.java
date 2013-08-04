package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class StoredThreadSamplerFactory implements ThreadSamplerFactory {

	private ThreadSamplerFactory delegate;

	private ThreadStoreRepository repository;

	private ThreadSampler sampler;

	public StoredThreadSamplerFactory(ThreadSamplerFactory delegate) {
		this.delegate = delegate;
		this.repository = new ThreadStoreRepository();
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			final ThreadSampler delegateSampler = delegate.getSampler();
			sampler = new ThreadSampler() {

				@Override
				public ThreadSamplingState sampling()
						throws ThreadSamplingException {
					ThreadSamplingState sampling = delegateSampler.sampling();
					ThreadSamplingState storedSampling = new ThreadSamplingState(
							sampling);
					ThreadCallState[] callStates = sampling.getCallStates();
					StoredThreadCallState[] storedCallStates = new StoredThreadCallState[callStates.length];
					for (int i = 0, n = callStates.length; i < n; i++) {
						storedCallStates[i] = new StoredThreadCallState(
								repository, callStates[i]);
					}
					storedSampling.setCallStates(storedCallStates);
					return storedSampling;
				}
			};
		}
		return sampler;
	}

	@Override
	public void close() {
		delegate.close();
	}

}
