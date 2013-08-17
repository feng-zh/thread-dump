package com.hp.ts.rnd.tool.perf.threads.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer;

class ThreadSamplerAgent implements ThreadSamplingHandler {

	private ThreadSamplingStatus status;
	private ThreadSamplerFactory samplerFactory;
	private ThreadSamplingService samplingService;
	private ScheduledExecutorService scheduler;
	private CallTreeAnalyzer callTree;
	private volatile ObjectOutput eventStream;
	private volatile ScheduledFuture<?> monitorFuture;
	private ThreadSamplerAgentEntry agentInfo;

	public ThreadSamplerAgent(ThreadSamplerAgentEntry agentInfo,
			ThreadSamplerFactory factory, ScheduledExecutorService scheduler) {
		this.agentInfo = agentInfo;
		this.samplerFactory = factory;
		this.scheduler = scheduler;
	}

	public void close() {
		samplerFactory.close();
	}

	public ThreadDumpResult dumpStack() {
		return ThreadDumpResult.valueOf(samplerFactory.getSampler().sampling());
	}

	public void startSampling(int durationSec, int intervalMs) {
		samplingService = ThreadSamplings
				.createScheduledSamplingService(samplerFactory.getSampler(),
						scheduler, durationSec, intervalMs);
		status = new ThreadSamplingStatus();
		status.startedOn = System.currentTimeMillis();
		status.expectedFinished = status.startedOn
				+ TimeUnit.SECONDS.toMillis(durationSec);
		status.uuid = UUID.randomUUID().toString();
		callTree = new CallTreeAnalyzer();
		samplingService.executeSampling(this);
	}

	public ThreadSamplingStatus getSamplingStatus() {
		printCallTree();
		return status;
	}

	public void monitorSamplingStatus(ObjectOutput eventStream) {
		ObjectOutput oldEventStream = this.eventStream;
		this.eventStream = eventStream;
		if (oldEventStream != null) {
			try {
				oldEventStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (monitorFuture == null && !status.done) {
			monitorFuture = this.scheduler.scheduleWithFixedDelay(
					new Runnable() {

						@Override
						public void run() {
							printCallTree();
							ObjectOutput objectOutput = ThreadSamplerAgent.this.eventStream;
							if (objectOutput != null) {
								try {
									objectOutput.writeObject(status);
									objectOutput.flush();
								} catch (IOException e) {
									// TODO
									e.printStackTrace();
									closeMonitor();
								}
							}
						}
					}, 0, 1, TimeUnit.SECONDS);
		}
	}

	private void closeMonitor() {
		ScheduledFuture<?> scheduledFuture = monitorFuture;
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			monitorFuture = null;
			try {
				eventStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			eventStream = null;
		}
	}

	public void closeSampling() {
		samplingService.closeSampling();
		closeMonitor();
	}

	@Override
	public void onSampling(ThreadSamplingState state) {
		status.sampleCount++;
		status.traceCount += state.getStackTraces().length;
		callTree.addThreadSampling(state);

	}

	@Override
	public void onError(ThreadSamplingException exception)
			throws ThreadSamplingException {
		status.error = exception;
	}

	@Override
	public void onEnd() {
		printCallTree();
		status.done = true;
		status.finishedOn = System.currentTimeMillis();
		callTree = null;
		closeMonitor();
	}

	private void printCallTree() {
		CallTreeAnalyzer tree = callTree;
		if (tree != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			tree.print(ps);
			status.callTree = baos.toString();
			System.out.println("print call tree");
		}
	}

	public ThreadSamplerAgentEntry getAgentEntry() {
		return agentInfo;
	}

}
