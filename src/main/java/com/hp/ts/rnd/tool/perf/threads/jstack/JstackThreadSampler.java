package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sun.tools.attach.HotSpotVirtualMachine;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

class JstackThreadSampler implements ThreadSampler {

	private Object vm;

	public JstackThreadSampler(Object vm) {
		this.vm = vm;
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		try {
			samplingState.startSampling();
			InputStream input = ((HotSpotVirtualMachine) vm).remoteDataDump();
			samplingState.endSampling();
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
			throw new ThreadSamplingException(e);
		} catch (RuntimeException e) {
			throw new ThreadSamplingException(e);
		}
		return samplingState;
	}

}
