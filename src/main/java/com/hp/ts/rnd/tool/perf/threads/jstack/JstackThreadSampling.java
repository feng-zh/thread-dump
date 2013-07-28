package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sun.tools.attach.HotSpotVirtualMachine;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampling;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class JstackThreadSampling implements ThreadSampling {

	private Object vm;

	public JstackThreadSampling(Object vm) {
		this.vm = vm;
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		try {
			InputStream input = ((HotSpotVirtualMachine) vm).remoteDataDump();

			JstackOutputParser parser = new JstackOutputParser(input);
			samplingState.setSamplingTime(parser.getSampleTime().getTime());
			JstackThreadEntry threadEntry;
			List<JstackThreadEntry> threads = new ArrayList<JstackThreadEntry>();
			while ((threadEntry = parser.nextThread()) != null) {
				threads.add(threadEntry);
			}
			samplingState.setCallStates(threads
					.toArray(new JstackThreadEntry[threads.size()]));
			input.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return samplingState;
	}

}
