package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

class JvmThreadSampler implements ThreadSampler {

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		samplingState.startSampling();
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
		samplingState.endSampling();
		JvmThreadCallState[] states = new JvmThreadCallState[stacks.size()];
		int index = 0;
		for (Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
			states[index++] = new JvmThreadCallState(entry.getKey(),
					entry.getValue());
		}
		samplingState.setCallStates(states);
		return samplingState;
	}

}
