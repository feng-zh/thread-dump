package com.hp.ts.rnd.tool.perf.threads;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer;
import com.hp.ts.rnd.tool.perf.threads.jstack.JstackThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.store.StoredThreadSamplerFactory;

public class ThreadsExample implements Runnable {

	private static int SAMPLING_TIME_SEC = 20;

	private static long SAMPLING_INC_MS = 10;

	public static void main(String[] args) throws IOException {
		// Thread samplingThread = new Thread(new ThreadsExample());
		// samplingThread.start();
		ThreadSamplerFactory samplerFactory =
		// new WLSJmxThreadSamplerFactory("g1u2201.austin.hp.com:50002",
		// "username", "password");
		new StoredThreadSamplerFactory(new JstackThreadSamplerFactory(26198));
		long samplingTime = System.nanoTime()
				+ TimeUnit.SECONDS.toNanos(SAMPLING_TIME_SEC);
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long startMem = runtime.totalMemory() - runtime.freeMemory();
		CallTreeAnalyzer callTree = new CallTreeAnalyzer();
		try {
			ThreadSampler sampling = samplerFactory.getSampler();
			while (true) {
				long inSampling = System.nanoTime();
				if (samplingTime < inSampling) {
					break;
				}
				ThreadSamplingState samplingState = sampling.sampling();
				callTree.addThreadSampling(samplingState);
				// System.out.println(TimeUnit.NANOSECONDS.toMillis(samplingState
				// .getDurationTimeNanos()));
				// System.out.print(".");
				inSampling = System.nanoTime() - inSampling;
				long waitSampling = TimeUnit.MILLISECONDS
						.toNanos(SAMPLING_INC_MS) - inSampling;
				try {
					TimeUnit.NANOSECONDS.sleep(waitSampling);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			System.out.println();
			System.gc();
			// callTree.print(System.out);
			// samplingThread.interrupt();
			// try {
			// samplingThread.join();
			// } catch (InterruptedException e) {
			// Thread.currentThread().interrupt();
			// }
			long endMem = runtime.totalMemory() - runtime.freeMemory();
			System.out.println("- start on memory usage: " + startMem / 1024
					+ " KB");
			System.out.println("- end   on memory usage: " + endMem / 1024
					+ " KB");
			System.out.println("--increase memory usage: "
					+ (endMem - startMem) / 1024 + " KB");
		} finally {
			samplerFactory.close();
		}

	}

	public void run() {
		CallTreeAnalyzer callTree = new CallTreeAnalyzer();
		ThreadSamplerFactory samplerFactory = new JvmThreadSamplerFactory();
		try {
			ThreadSampler sampling = samplerFactory.getSampler();
			while (true) {
				long inSampling = System.nanoTime();
				ThreadSamplingState samplingState = sampling.sampling();
				System.out.println(samplingState);
				System.out.println(sampling.sampling());
				if (samplingState != null) {
					return;
				}
				callTree.addThreadSampling(samplingState);
				inSampling = System.nanoTime() - inSampling;
				long waitSampling = TimeUnit.MILLISECONDS
						.toNanos(SAMPLING_INC_MS) - inSampling;
				TimeUnit.NANOSECONDS.sleep(waitSampling);
			}
		} catch (InterruptedException e) {
			callTree.print(System.out);
		}
	}
}
