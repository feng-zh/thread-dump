package com.hp.ts.rnd.tool.perf.threads;

public class ThreadSamplingException extends RuntimeException {

	private static final long serialVersionUID = -3238067164019520140L;

	public ThreadSamplingException() {
		super();
	}

	public ThreadSamplingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ThreadSamplingException(String message) {
		super(message);
	}

	public ThreadSamplingException(Throwable cause) {
		super(cause);
	}

}
