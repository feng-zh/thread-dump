package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class MemoryStoreThreadSamplerFactory implements ThreadSamplerFactory {

	private ThreadSamplerFactory delegate;

	private ThreadStoreRepository repository;

	private ThreadSampler sampler;

	public MemoryStoreThreadSamplerFactory(ThreadSamplerFactory delegate) {
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
							sampling.getStartTimeMillis(),
							sampling.getDurationTimeNanos());
					storedSampling.setSamplingTime(sampling.getSamplingTime());
					ThreadStackTrace[] stackTraces = sampling.getStackTraces();
					StoredThreadStackTrace[] storedStackTraces = new StoredThreadStackTrace[stackTraces.length];
					for (int i = 0, n = stackTraces.length; i < n; i++) {
						storedStackTraces[i] = new StoredThreadStackTrace(
								repository, stackTraces[i]);
					}
					storedSampling.setStackTraces(storedStackTraces);
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
