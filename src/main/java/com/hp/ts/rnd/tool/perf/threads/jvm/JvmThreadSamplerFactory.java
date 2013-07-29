package com.hp.ts.rnd.tool.perf.threads.jvm;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;

public class JvmThreadSamplerFactory implements ThreadSamplerFactory {

	@Override
	public ThreadSampler getSampler() {
		return new JvmThreadSampler();
	}

	@Override
	public void close() {
	}

}
