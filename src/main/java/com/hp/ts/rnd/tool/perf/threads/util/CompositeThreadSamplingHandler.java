package com.hp.ts.rnd.tool.perf.threads.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class CompositeThreadSamplingHandler implements ThreadSamplingHandler {

	private List<ThreadSamplingHandler> handlers = new ArrayList<ThreadSamplingHandler>();

	public CompositeThreadSamplingHandler(ThreadSamplingHandler... handlers) {
		this.handlers.addAll(Arrays.asList(handlers));
	}

	public CompositeThreadSamplingHandler add(ThreadSamplingHandler handler) {
		this.handlers.add(handler);
		return this;
	}

	@Override
	public void onSampling(ThreadSamplingState state) {
		for (ThreadSamplingHandler handler : handlers) {
			handler.onSampling(state);
		}
	}

	@Override
	public void onError(ThreadSamplingException exception)
			throws ThreadSamplingException {
		ThreadSamplingException anotherError = null;
		for (ThreadSamplingHandler handler : handlers) {
			try {
				handler.onError(exception);
			} catch (ThreadSamplingException e) {
				anotherError = e;
			}
		}
		if (anotherError != null) {
			throw anotherError;
		}
	}

	@Override
	public void onEnd() {
		for (ThreadSamplingHandler handler : handlers) {
			try {
				handler.onEnd();
			} catch (Throwable ignored) {
			}
		}
	}

}
