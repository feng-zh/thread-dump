package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

public class ThreadSamplingWriter {

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

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream dataBuffer = new DataOutputStream(buffer);
		dataBuffer.writeInt(threadNameIndex);
		dataBuffer.writeUTF(threadName);
		dataBuffer.close();

		out.writeByte(TYPE_THREAD_NAME);
		out.writeInt(buffer.size());
		out.write(buffer.toByteArray());
		return threadNameIndex;
	}

	public void writeThreadSampling(ThreadSamplingState samplingState)
			throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream dataBuffer = new DataOutputStream(buffer);
		write(dataBuffer, samplingState);
		dataBuffer.close();
		out.writeByte(TYPE_SAMPLING_STATE);
		out.writeInt(buffer.size());
		out.write(buffer.toByteArray());
	}

	private void write(DataOutput output, ThreadSamplingState samplingState)
			throws IOException {
		ThreadCallState[] callStates = samplingState.getCallStates();
		output.writeLong(samplingState.getSamplingTime());
		output.writeLong(samplingState.getStartTimeMillis());
		output.writeLong(samplingState.getDurationTimeNanos());
		output.writeInt(callStates.length);
		for (ThreadCallState state : callStates) {
			write(output, state);
		}
	}

	private void write(DataOutput output, ThreadCallState state)
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
		long stackFrameId = (Long) stackFrame.getStackFrameId();
		writeThreadStackFrame(stackFrame, stackFrameId);
		output.writeLong(stackFrameId);
	}

	void writeThreadStackFrame(ThreadStackFrame stackFrame, long frameId)
			throws IOException {
		// check if frame id write
		if (existStackFrameIds.contains(frameId)) {
			return;
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream dataBuffer = new DataOutputStream(buffer);
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
		dataBuffer.close();
		// write to out
		out.writeByte(TYPE_STACK_FRAME);
		out.writeInt(buffer.size());
		out.write(buffer.toByteArray());
		existStackFrameIds.add(frameId);
	}
}
