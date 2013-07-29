package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.io.IOException;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.Utils;

public class JstackThreadSamplerFactory implements ThreadSamplerFactory {

	private int pid;
	private Object vm;
	private JstackThreadSampler sampler;

	public JstackThreadSamplerFactory(int pid) {
		this.pid = pid;
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			try {
				vm = Utils.attachJvm(pid);
			} catch (IOException e) {
				throw new ThreadSamplingException(e);
			}
			sampler = new JstackThreadSampler(vm);
		}
		return sampler;
	}

	@Override
	public void close() {
		if (vm != null) {
			try {
				Utils.detachJvm(vm);
			} catch (IOException e) {
				// ignored
			}
			vm = null;
			sampler = null;
		}
	}

}
