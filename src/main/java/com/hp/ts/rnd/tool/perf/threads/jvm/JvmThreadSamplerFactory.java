package com.hp.ts.rnd.tool.perf.threads.jvm;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;

public class JvmThreadSamplerFactory implements ThreadSamplerFactory {

	private boolean ignoreSamplingThread = true;

	@Override
	public ThreadSampler getSampler() {
		return new JvmThreadSampler(ignoreSamplingThread);
	}

	public boolean isIgnoreSamplingThread() {
		return ignoreSamplingThread;
	}

	public void setIgnoreSamplingThread(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	@Override
	public void close() {
	}

}
