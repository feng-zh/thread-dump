package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

class JvmThreadSampler implements ThreadSampler {

	private boolean ignoreSamplingThread;

	JvmThreadSampler(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		samplingState.startSampling();
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
		samplingState.endSampling();
		List<JvmThreadStackTrace> stackTraces = new ArrayList<JvmThreadStackTrace>(
				stacks.size());
		for (Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
			if (ignoreSamplingThread
					&& isDumpThreadStackTrace(entry.getValue())) {
				continue;
			}
			stackTraces.add(new JvmThreadStackTrace(entry.getKey(), entry
					.getValue()));
		}
		samplingState.setStackTraces(stackTraces
				.toArray(new JvmThreadStackTrace[stackTraces.size()]));
		return samplingState;
	}

	// only support following
	// at java.lang.Thread.dumpThreads(Native Method)
	// at java.lang.Thread.getAllStackTraces(Thread.java:?)
	private boolean isDumpThreadStackTrace(StackTraceElement[] elements) {
		if (elements.length > 3) {
			if (Thread.class.getName().equals(elements[0].getClassName())
					&& "dumpThreads".equals(elements[0].getMethodName())
					&& Thread.class.getName()
							.equals(elements[1].getClassName())
					&& "getAllStackTraces".equals(elements[1].getMethodName())) {
				return true;
			}
		}
		return false;
	}

}
