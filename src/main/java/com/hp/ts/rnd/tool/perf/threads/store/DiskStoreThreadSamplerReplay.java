package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class DiskStoreThreadSamplerReplay implements ThreadSamplerFactory,
		ThreadSamplingService {

	private ThreadSamplingReader reader;
	private DataInputStream dataInput;
	private DiskThreadSampler sampler;
	private volatile boolean loop = false;
	private volatile boolean closed = false;

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
		closeSampling();
		closeInput();
	}

	private void closeInput() {
		if (!closed) {
			try {
				dataInput.close();
			} catch (IOException e) {
			}
			System.out.println(sampler.getSamplingCount());
			System.out.println(sampler.getTraceCount());
			System.out.println(sampler.getStackFrameCount());
			System.out.println(sampler.getStackFrameTotalLength());
			System.out.println(sampler.getThreadNameCount());
			System.out.println(sampler.getThreadNameTotalLength());
			closed = true;
		}
	}

	@Override
	public void closeSampling() {
		loop = false;
	}

	@Override
	public void executeSampling(ThreadSamplingHandler handler)
			throws ThreadSamplingException {
		loop = true;
		try {
			while (loop) {
				try {
					ThreadSamplingState state = getSampler().sampling();
					handler.onSampling(state);
				} catch (EndOfSamplingException e) {
					closeInput();
					loop = false;
				} catch (ThreadSamplingException e) {
					handler.onError(e);
				}
			}
		} finally {
			handler.onEnd();
		}
	}

}
