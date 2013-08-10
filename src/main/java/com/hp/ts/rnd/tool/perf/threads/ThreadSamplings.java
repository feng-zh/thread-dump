package com.hp.ts.rnd.tool.perf.threads;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer;
import com.hp.ts.rnd.tool.perf.threads.jmx.JmxThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jstack.JstackThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.proxy.ProxyThreadSamplerFactoryBuilder;
import com.hp.ts.rnd.tool.perf.threads.store.DiskStoreThreadSamplerReplay;
import com.hp.ts.rnd.tool.perf.threads.store.ThreadSamplingWriter;
import com.hp.ts.rnd.tool.perf.threads.util.CompositeThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.util.ScheduledThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.util.SimpleThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.weblogic.WLSJmxThreadSamplerFactory;

/*
 * The facade class to sampler factory, sampling service
 */
final public class ThreadSamplings {

	/* Factory */

	public static ThreadSamplerFactory createWLSJmxThreadSamplerFactory(
			String hostport, String username, String password) {
		return new WLSJmxThreadSamplerFactory(hostport, username, password);
	}

	public static ThreadSamplerFactory createJvmThreadSamplerFactory() {
		return new JvmThreadSamplerFactory();
	}

	public static ThreadSamplerFactory createJmxThreadSamplerFactory(
			JMXServiceURL jmxURL, String[] userInfo) throws IOException {
		return new JmxThreadSamplerFactory(jmxURL, userInfo);
	}

	public static ThreadSamplerFactory createJstackThreadSamplerFactory(int pid) {
		return new JstackThreadSamplerFactory(pid);
	}

	public static ThreadSamplerFactory createDiskReplayThreadSamplerFactory(
			InputStream input) throws IOException {
		return new DiskStoreThreadSamplerReplay(input);
	}

	public static ProxyThreadSamplerFactoryBuilder createProxyFactoryBuilder(
			MBeanServerConnection mbsc) {
		return new ProxyThreadSamplerFactoryBuilder(mbsc);
	}

	public static ThreadSamplerFactory createHandlerPassThroughFactory(
			final ThreadSamplerFactory factory,
			final ThreadSamplingHandler handler) {
		return new ThreadSamplerFactory() {

			private ThreadSampler sampler;
			private AtomicBoolean unprocessEnd = new AtomicBoolean(true);

			@Override
			public ThreadSampler getSampler() throws ThreadSamplingException {
				if (sampler == null) {
					final ThreadSampler interanlSampler = factory.getSampler();
					sampler = new ThreadSampler() {

						@Override
						public ThreadSamplingState sampling()
								throws ThreadSamplingException,
								EndOfSamplingException {
							try {
								ThreadSamplingState sampling = interanlSampler
										.sampling();
								handler.onSampling(sampling);
								return sampling;
							} catch (EndOfSamplingException e) {
								if (unprocessEnd.getAndSet(false)) {
									handler.onEnd();
								}
								throw e;
							} catch (ThreadSamplingException e) {
								handler.onError(e);
								throw e;
							}
						}
					};
				}
				return sampler;
			}

			@Override
			public void close() {
				factory.close();
				if (unprocessEnd.getAndSet(false)) {
					handler.onEnd();
				}
			}
		};
	}

	/* Sampling Service */

	public static ThreadSamplingService createSimpleSamplingService(
			ThreadSampler sampler, int samplingDurationSeconds,
			int samplingPeriodMillis) {
		return new SimpleThreadSamplingService(sampler,
				samplingDurationSeconds, samplingPeriodMillis);
	}

	public static ThreadSamplingService createScheduledSamplingService(
			ThreadSampler sampler, ScheduledExecutorService executor,
			int samplingDurationSeconds, int samplingPeriodMillis) {
		return new ScheduledThreadSamplingService(sampler, executor,
				samplingDurationSeconds, samplingPeriodMillis);
	}

	public static ThreadSamplingService createDiskReplaySamplingService(
			InputStream input) throws IOException {
		return new DiskStoreThreadSamplerReplay(input);
	}

	/* Service Handler */

	public static ThreadSamplingHandler createDiskStoreSamplingHandler(
			DataOutput output) throws IOException {
		return new ThreadSamplingWriter(output);
	}

	public static ThreadSamplingHandler createChainedSamplingHandler(
			ThreadSamplingHandler... handlers) {
		return new CompositeThreadSamplingHandler(handlers);
	}

	public static ThreadSamplingHandler createCallTree(final PrintStream out) {
		return new ThreadSamplingHandler() {

			private CallTreeAnalyzer callTree = new CallTreeAnalyzer();

			@Override
			public void onSampling(ThreadSamplingState state) {
				callTree.addThreadSampling(state);
			}

			@Override
			public void onError(ThreadSamplingException exception)
					throws ThreadSamplingException {
			}

			@Override
			public void onEnd() {
				callTree.print(out);
			}
		};
	}

}
