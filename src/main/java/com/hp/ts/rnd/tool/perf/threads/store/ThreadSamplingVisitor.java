package com.hp.ts.rnd.tool.perf.threads.store;

import java.lang.Thread.State;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public interface ThreadSamplingVisitor {

	public void visitThreadName(int threadNameIndex, String threadName);

	public void visitThreadSampling(ThreadSamplingState state);

	public void visitStackFrame(long stackFrameId, String className,
			String methodName, String fileName, int lineNumber);

	public ThreadStackTrace visitStackTrace(long threadIdentifier,
			int threadNameIndex, State threadState, long[] stackFrameIds);
}
