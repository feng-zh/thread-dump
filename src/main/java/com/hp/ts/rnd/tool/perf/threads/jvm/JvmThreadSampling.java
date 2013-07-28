package com.hp.ts.rnd.tool.perf.threads.jvm;

import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampling;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class JvmThreadSampling implements ThreadSampling {

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
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
