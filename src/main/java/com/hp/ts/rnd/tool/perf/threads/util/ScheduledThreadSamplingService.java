package com.hp.ts.rnd.tool.perf.threads.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class ScheduledThreadSamplingService implements ThreadSamplingService {

	private ThreadSampler sampler;

	private int samplingDurationSeconds;

	private int samplingPeriodMillis;

	private ScheduledExecutorService executor;

	private volatile ScheduledFuture<?> scheduledFuture;

	private AtomicReference<ThreadSamplingHandler> unEndHandler;

	public ScheduledThreadSamplingService(ThreadSampler sampler,
			ScheduledExecutorService executor, int samplingDurationSeconds,
			int samplingPeriodMillis) {
		this.sampler = sampler;
		this.samplingDurationSeconds = samplingDurationSeconds;
		this.samplingPeriodMillis = samplingPeriodMillis;
		this.executor = executor;
	}

	@Override
	public void executeSampling(final ThreadSamplingHandler handler)
			throws ThreadSamplingException {
		this.unEndHandler = new AtomicReference<ThreadSamplingHandler>(handler);
		scheduledFuture = executor.scheduleAtFixedRate(new Runnable() {

			private long samplingTime = samplingDurationSeconds <= 0 ? Long.MAX_VALUE
					: (System.nanoTime() + TimeUnit.SECONDS
							.toNanos(samplingDurationSeconds));

			@Override
			public void run() {
				ScheduledFuture<?> future = scheduledFuture;
				if (future == null) {
					return;
				}
				unEndHandler.set(null);
				try {
					long inSampling = System.nanoTime();
					if (samplingTime < inSampling) {
						future.cancel(false);
						scheduledFuture = null;
					} else {
						try {
							ThreadSamplingState state = sampler.sampling();
							handler.onSampling(state);
						} catch (EndOfSamplingException e) {
							future.cancel(false);
							scheduledFuture = null;
						} catch (ThreadSamplingException e) {
							handler.onError(e);
						} catch (Exception e) {
							handler.onError(new ThreadSamplingException(
									"unxpected exception", e));
						}
					}
				} finally {
					// was closed during execution
					if (scheduledFuture == null) {
						// perform end here
						handler.onEnd();
						// no end processed
						unEndHandler.set(null);
					} else {
						unEndHandler.set(handler);
					}
				}
			}
		}, samplingPeriodMillis, samplingPeriodMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void closeSampling() {
		ScheduledFuture<?> future = scheduledFuture;
		if (future != null) {
			future.cancel(true);
			scheduledFuture = null;
			ThreadSamplingHandler handler = unEndHandler.getAndSet(null);
			if (handler != null) {
				handler.onEnd();
			}
		}
	}

}
