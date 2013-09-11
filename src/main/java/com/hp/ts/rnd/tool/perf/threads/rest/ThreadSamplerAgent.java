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
import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer.CallCount;
import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer.PrintFilter;
import com.hp.ts.rnd.tool.perf.threads.calltree.TreeNode;

class ThreadSamplerAgent implements ThreadSamplingHandler, Runnable {

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
		if (oldEventStream != null) {
			try {
				oldEventStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.eventStream = null;
		}
		if (monitorFuture != null) {
			this.eventStream = eventStream;
		} else if (status != null) {
			if (monitorFuture == null && !status.done) {
				this.eventStream = eventStream;
				monitorFuture = this.scheduler.scheduleWithFixedDelay(this, 0,
						1, TimeUnit.SECONDS);
			}
			if (status.done) {
				try {
					eventStream.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
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
		scheduler.schedule(this, 0, TimeUnit.SECONDS);
	}

	private void printCallTree() {
		CallTreeAnalyzer tree = callTree;
		if (tree != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			tree.printFilter(ps, new PrintFilter() {

				@Override
				public boolean acceptNode(TreeNode<Object, CallCount> item) {
					if (item.getValue() != null) {
						return item.getValue().getCount() > status.threshold;
					} else {
						return true;
					}
				}

				@Override
				public Boolean acceptTrace(TreeNode<Object, CallCount> item) {
					if ((status.include == null || status.include.length() == 0)
							&& (status.exclude == null || status.exclude
									.length() == 0)) {
						return true;
					}
					if (item.getValue() != null) {
						if (status.include != null
								&& status.include.trim().length() > 0
								&& String.valueOf(item.getValue().getName())
										.indexOf(status.include) >= 0) {
							return true;
						}
						if (status.exclude != null
								&& status.exclude.trim().length() > 0
								&& String.valueOf(item.getValue().getName())
										.indexOf(status.exclude) >= 0) {
							return false;
						}
					}
					return null;
				}

			});
			status.callTree = baos.toString();
		}
	}

	public ThreadSamplerAgentEntry getAgentEntry() {
		return agentInfo;
	}

	@Override
	public synchronized void run() {
		printCallTree();
		ObjectOutput objectOutput = ThreadSamplerAgent.this.eventStream;
		if (objectOutput != null) {
			try {
				if (status.sampleCount > 0) {
					objectOutput.writeObject(status);
					objectOutput.flush();
				}
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			} finally {
				if (status.done) {
					closeMonitor();
				}
			}
		}
	}

	public void applyFilter(String include, String exclude, int threshold) {
		if (status != null) {
			status.include = include;
			status.exclude = exclude;
			status.threshold = threshold;
		}
	}

}
