package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class DiskThreadSampler implements ThreadSampler, ThreadSamplingVisitor {

	private ThreadSamplingReader reader;
	private ThreadSamplingState lastSamplingState;
	private Map<Integer, Integer> threadNameMapTable = new HashMap<Integer, Integer>();
	private Map<Long, Long> stackFrameMapTable = new HashMap<Long, Long>();
	private ThreadStoreRepository repository = new ThreadStoreRepository();

	private long threadNameTotalLength;
	private long stackFrameTotalLength;
	private long samplingCount;
	private long traceCount;

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
		threadNameTotalLength += threadName.length();
	}

	@Override
	public void visitThreadSampling(ThreadSamplingState state) {
		lastSamplingState = state;
		samplingCount++;
	}

	@Override
	public void visitStackFrame(long stackFrameId, String className,
			String methodName, String fileName, int lineNumber) {
		long frameId = repository.createStackFrameId(className, methodName,
				fileName, lineNumber);
		stackFrameMapTable.put(stackFrameId, frameId);
		stackFrameTotalLength += className.length();
		stackFrameTotalLength += methodName.length();
		stackFrameTotalLength += 1;
		stackFrameTotalLength += fileName == null ? 0 : fileName.length();
		stackFrameTotalLength += 2;
	}

	@Override
	public ThreadStackTrace visitStackTrace(long threadIdentifier,
			int threadNameIndex, State threadState, long[] stackFrameIds) {
		long[] frameIds = new long[stackFrameIds.length];
		for (int i = 0; i < frameIds.length; i++) {
			frameIds[i] = stackFrameMapTable.get(stackFrameIds[i]);
		}
		ThreadStackTrace stackTrace = new StoredThreadStackTrace(repository,
				threadIdentifier, threadNameMapTable.get(threadNameIndex),
				threadState, frameIds);
		traceCount += frameIds.length;
		return stackTrace;
	}

	long getThreadNameTotalLength() {
		return threadNameTotalLength;
	}

	long getStackFrameTotalLength() {
		return stackFrameTotalLength;
	}

	int getThreadNameCount() {
		return threadNameMapTable.size();
	}

	int getStackFrameCount() {
		return stackFrameMapTable.size();
	}

	long getSamplingCount() {
		return samplingCount;
	}

	long getTraceCount() {
		return traceCount;
	}

}
