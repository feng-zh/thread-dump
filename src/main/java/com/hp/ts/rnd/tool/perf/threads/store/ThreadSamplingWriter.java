package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class ThreadSamplingWriter implements ThreadSamplingHandler {

	static final String HEADER = "PERF THREAD SAMPLING";

	static final int TYPE_SAMPLING_STATE = 1;

	static final int TYPE_THREAD_NAME = 2;

	static final int TYPE_STACK_FRAME = 3;

	private DataOutput out;

	private Set<Long> existStackFrameIds = new HashSet<Long>();

	private IndexTable<String> threadNameIds = new IndexTable<String>();

	private ThreadStoreRepository repository;

	public ThreadSamplingWriter(DataOutput output) throws IOException {
		this.out = output;
		this.repository = new ThreadStoreRepository();
		writeHeader(new Date());
	}

	ThreadSamplingWriter(DataOutput output, ThreadStoreRepository repository) {
		this.out = output;
		this.repository = repository;
	}

	void writeHeader(Date createdTime) throws IOException {
		out.writeBytes(HEADER);
		out.writeByte(0);
		// version 1.0
		out.writeByte(1);
		out.writeByte(0);
		// created time
		out.writeLong(createdTime.getTime());
	}

	int writeThreadName(String threadName) throws IOException {
		int threadNameIndex = threadNameIds.indexOf(threadName);
		if (threadNameIndex != -1) {
			// not exists
			return threadNameIndex;
		}
		threadNameIndex = threadNameIds.add(threadName);

		BlockDataOutput dataBuffer = new BlockDataOutput();
		dataBuffer.writeInt(threadNameIndex);
		dataBuffer.writeUTF(threadName);
		out.writeByte(TYPE_THREAD_NAME);
		dataBuffer.writeTo(out);
		return threadNameIndex;
	}

	public void writeThreadSampling(ThreadSamplingState samplingState)
			throws IOException {
		BlockDataOutput dataBuffer = new BlockDataOutput();
		write(dataBuffer, samplingState);
		out.writeByte(TYPE_SAMPLING_STATE);
		dataBuffer.writeTo(out);
	}

	private void write(DataOutput output, ThreadSamplingState samplingState)
			throws IOException {
		ThreadStackTrace[] stackTraces = samplingState.getStackTraces();
		output.writeLong(samplingState.getSamplingTime());
		output.writeLong(samplingState.getStartTimeMillis());
		output.writeLong(samplingState.getDurationTimeNanos());
		output.writeInt(stackTraces.length);
		for (ThreadStackTrace state : stackTraces) {
			write(output, state);
		}
	}

	private void write(DataOutput output, ThreadStackTrace state)
			throws IOException {
		output.writeLong(state.getThreadIdentifier());
		int threadNameId = writeThreadName(state.getThreadName());
		output.writeInt(threadNameId);
		output.writeByte(state.getThreadState() == null ? 0 : (state
				.getThreadState().ordinal() + 1));
		ThreadStackFrame[] stackFrames = state.getStackFrames();
		output.writeInt(stackFrames.length);
		for (ThreadStackFrame stackFrame : stackFrames) {
			StoredThreadStackFrame storedFrame = null;
			if (stackFrame instanceof StoredThreadStackFrame) {
				storedFrame = (StoredThreadStackFrame) stackFrame;
			}
			if (storedFrame == null
					|| storedFrame.getRepository() != repository) {
				storedFrame = new StoredThreadStackFrame(repository, stackFrame);
			}
			write(output, storedFrame);
		}
	}

	private void write(DataOutput output, StoredThreadStackFrame stackFrame)
			throws IOException {
		long stackFrameId = stackFrame.getStackFrameId();
		writeThreadStackFrame(stackFrame, stackFrameId);
		output.writeLong(stackFrameId);
	}

	void writeThreadStackFrame(ThreadStackFrame stackFrame, long frameId)
			throws IOException {
		// check if frame id write
		if (existStackFrameIds.contains(frameId)) {
			return;
		}
		BlockDataOutput dataBuffer = new BlockDataOutput();
		dataBuffer.writeLong(frameId);
		dataBuffer.writeUTF(stackFrame.getClassName());
		dataBuffer.writeUTF(stackFrame.getMethodName());
		if (stackFrame.getFileName() == null) {
			dataBuffer.writeByte(0);
		} else {
			dataBuffer.writeByte(1);
			dataBuffer.writeUTF(stackFrame.getFileName());
		}
		dataBuffer.writeInt(stackFrame.getLineNumber());
		// write to out
		out.writeByte(TYPE_STACK_FRAME);
		dataBuffer.writeTo(out);
		existStackFrameIds.add(frameId);
	}

	@Override
	public void onSampling(ThreadSamplingState state) {
		try {
			writeThreadSampling(state);
		} catch (IOException e) {
			throw new EndOfSamplingException("write sampling error", e);
		}
	}

	@Override
	public void onError(ThreadSamplingException exception)
			throws ThreadSamplingException {
	}

	@Override
	public void onEnd() {
		if (out instanceof Closeable) {
			try {
				((Closeable) out).close();
			} catch (IOException ignored) {
			}
		}
	}
}
