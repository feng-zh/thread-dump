package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

class JmxThreadSampler implements ThreadSampler {

	private ThreadMXBean mbean;
	private boolean objMonitorSupported;
	private boolean synchSupported;

	public JmxThreadSampler(ThreadMXBean mbean) {
		this.mbean = mbean;
		this.objMonitorSupported = mbean.isObjectMonitorUsageSupported();
		this.synchSupported = mbean.isSynchronizerUsageSupported();
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		samplingState.startSampling();
		ThreadInfo[] threadInfos = mbean.dumpAllThreads(objMonitorSupported,
				synchSupported);
		samplingState.endSampling();
		JmxThreadCallState[] states = new JmxThreadCallState[threadInfos.length];
		for (int i = 0, n = threadInfos.length; i < n; i++) {
			states[i] = new JmxThreadCallState(threadInfos[i]);
		}
		samplingState.setCallStates(states);
		return samplingState;
	}
}
