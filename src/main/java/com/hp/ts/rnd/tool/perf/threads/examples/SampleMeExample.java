package com.hp.ts.rnd.tool.perf.threads.examples;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer;
import com.hp.ts.rnd.tool.perf.threads.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.util.SimpleThreadSamplingService;

public class SampleMeExample implements Runnable, ThreadSamplingHandler {

	private CallTreeAnalyzer callTree = new CallTreeAnalyzer();
	private Thread thread;

	public SampleMeExample() {
		thread = new Thread(this);
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void run() {
		ThreadSampler sampling = new JvmThreadSamplerFactory().getSampler();
		SimpleThreadSamplingService service = new SimpleThreadSamplingService(
				sampling, 0, 10);
		try {
			service.executeSampling(this);
		} finally {
			service.closeSampling();
		}
	}

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
		callTree.print(System.out);
	}
}
