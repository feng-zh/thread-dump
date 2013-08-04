package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

public class DiskStoreThreadSamplerReplay implements ThreadSamplerFactory {

	private ThreadSamplingReader reader;
	private DataInputStream dataInput;
	private ThreadSampler sampler;

	public DiskStoreThreadSamplerReplay(InputStream input) throws IOException {
		dataInput = new DataInputStream(input);
		this.reader = new ThreadSamplingReader(dataInput);
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			sampler = new DiskThreadSampler(reader);
		}
		return sampler;
	}

	@Override
	public void close() {
		try {
			dataInput.close();
		} catch (IOException e) {
		}
	}

}
