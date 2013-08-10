package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class JstackThreadSampler implements ThreadSampler {

	private Object vm;

	public JstackThreadSampler(Object vm) {
		this.vm = vm;
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		try {
			samplingState.startSampling();
			InputStream input = Utils.remoteDataDump(vm);
			samplingState.endSampling();
			JstackOutputParser parser = new JstackOutputParser(input);
			samplingState.setSamplingTime(parser.getSampleTime().getTime());
			JstackThreadStackTrace stackTrace;
			List<JstackThreadStackTrace> threads = new ArrayList<JstackThreadStackTrace>();
			while ((stackTrace = parser.nextThreadStackTrace()) != null) {
				threads.add(stackTrace);
			}
			samplingState.setStackTraces(threads
					.toArray(new JstackThreadStackTrace[threads.size()]));
			input.close();
		} catch (IOException e) {
			throw new ThreadSamplingException(e);
		} catch (RuntimeException e) {
			throw new ThreadSamplingException(e);
		}
		return samplingState;
	}

}
