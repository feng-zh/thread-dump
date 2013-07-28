package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampling;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class JmxThreadSampling implements ThreadSampling {

	private ThreadMXBean mbean;

	public JmxThreadSampling(ThreadMXBean mbean) {
		this.mbean = mbean;
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		ThreadInfo[] threadInfos = mbean.dumpAllThreads(true, true);
		JmxThreadCallState[] states = new JmxThreadCallState[threadInfos.length];
		for (int i = 0, n = threadInfos.length; i < n; i++) {
			states[i] = new JmxThreadCallState(threadInfos[i]);
		}
		samplingState.setCallStates(states);
		return samplingState;
	}
}
