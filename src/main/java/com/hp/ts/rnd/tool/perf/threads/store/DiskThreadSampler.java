package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class DiskThreadSampler implements ThreadSampler, ThreadSamplingVisitor {

	private ThreadSamplingReader reader;
	private ThreadSamplingState lastSamplingState;
	private Map<Integer, Integer> threadNameMapTable = new HashMap<Integer, Integer>();
	private Map<Long, Long> stackFrameMapTable = new HashMap<Long, Long>();
	private ThreadStoreRepository repository = new ThreadStoreRepository();

	DiskThreadSampler(ThreadSamplingReader reader) {
		this.reader = reader;
	}

	@Override
	public ThreadSamplingState sampling() throws ThreadSamplingException {
		while (lastSamplingState == null) {
			try {
				if (!reader.acceptLoop(this, false)) {
					// EOF
					throw new EndOfSamplingException();
				}
			} catch (IOException e) {
				throw new ThreadSamplingException(e);
			}
		}
		ThreadSamplingState samplingState = lastSamplingState;
		lastSamplingState = null;
		return samplingState;
	}

	@Override
	public void visitThreadName(int threadNameIndex, String threadName) {
		int threadNameId = repository.createThreadNameId(threadName);
		threadNameMapTable.put(threadNameIndex, threadNameId);
	}

	@Override
	public void visitThreadSampling(ThreadSamplingState state) {
		lastSamplingState = state;
	}

	@Override
	public void visitStackFrame(long stackFrameId, String className,
			String methodName, String fileName, int lineNumber) {
		long frameId = repository.createStackFrameId(className, methodName,
				fileName, lineNumber);
		stackFrameMapTable.put(stackFrameId, frameId);
	}

	@Override
	public ThreadCallState visitCallState(long threadIdentifier,
			int threadNameIndex, State threadState, long[] stackFrameIds) {
		long[] frameIds = new long[stackFrameIds.length];
		for (int i = 0; i < frameIds.length; i++) {
			frameIds[i] = stackFrameMapTable.get(stackFrameIds[i]);
		}
		ThreadCallState callState = new StoredThreadCallState(repository,
				threadIdentifier, threadNameMapTable.get(threadNameIndex),
				threadState, frameIds);
		return callState;
	}
}
