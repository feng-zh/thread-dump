package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class ThreadSamplingReader {

	private DataInput input;
	private byte majorVersion;
	private byte minorVersion;
	private long createdTime;

	public ThreadSamplingReader(DataInput input) throws IOException {
		this.input = input;
		readHeader();
	}

	public void accept(ThreadSamplingVisitor visitor) throws IOException {
		acceptLoop(visitor, true);
	}

	boolean acceptLoop(ThreadSamplingVisitor visitor, boolean loop)
			throws IOException {
		return readRoot(input, visitor, loop);
	}

	private DataInput readBlock(DataInput input) throws IOException {
		int len = input.readInt();
		byte[] buf = new byte[len];
		input.readFully(buf);
		return new BlockDataInput(new DataInputStream(new ByteArrayInputStream(
				buf)));
	}

	private boolean readRoot(DataInput input, ThreadSamplingVisitor visitor,
			boolean loop) throws IOException {
		do {
			byte type;
			try {
				type = input.readByte();
			} catch (EOFException e) {
				return false;
			}

			DataInput blockInput = readBlock(input);
			switch (type) {
			case ThreadSamplingWriter.TYPE_THREAD_NAME: {
				readThreadName(blockInput, visitor);
				break;
			}
			case ThreadSamplingWriter.TYPE_STACK_FRAME: {
				readStackFrame(blockInput, visitor);
				break;
			}
			case ThreadSamplingWriter.TYPE_SAMPLING_STATE: {
				readSamplingState(blockInput, visitor);
				break;
			}
			default:
				throw new IOException("unknown type: " + type);
			}
		} while (loop);
		return true;
	}

	private void readSamplingState(DataInput input,
			ThreadSamplingVisitor visitor) throws IOException {
		long samplingTime = input.readLong();
		long startTimeMillis = input.readLong();
		long durationTimeNanos = input.readLong();
		int stackTracesLen = input.readInt();
		ThreadSamplingState samplingState = new ThreadSamplingState(
				startTimeMillis, durationTimeNanos);
		samplingState.setSamplingTime(samplingTime);
		ThreadStackTrace[] stackTraces = new ThreadStackTrace[stackTracesLen];
		for (int i = 0; i < stackTracesLen; i++) {
			long threadIdentifier = input.readLong();
			int threadNameId = input.readInt();
			byte threadStateId = input.readByte();
			Thread.State threadState = threadStateId == 0 ? null : Thread.State
					.values()[threadStateId - 1];
			int stackFrameLen = input.readInt();
			long[] stackFrameIds = new long[stackFrameLen];
			for (int j = 0; j < stackFrameLen; j++) {
				stackFrameIds[j] = input.readLong();
			}
			stackTraces[i] = visitor.visitStackTrace(threadIdentifier,
					threadNameId, threadState, stackFrameIds);
		}
		samplingState.setStackTraces(stackTraces);
		visitor.visitThreadSampling(samplingState);
	}

	private void readStackFrame(DataInput input, ThreadSamplingVisitor visitor)
			throws IOException {
		long stackFrameId = input.readLong();
		String className = input.readUTF();
		String methodName = input.readUTF();
		String fileName;
		if (input.readByte() == 0) {
			fileName = null;
		} else {
			fileName = input.readUTF();
		}
		int lineNumber = input.readInt();
		visitor.visitStackFrame(stackFrameId, className, methodName, fileName,
				lineNumber);
	}

	private void readThreadName(DataInput input, ThreadSamplingVisitor visitor)
			throws IOException {
		// thread id
		int threadNameId = input.readInt();
		String threadName = input.readUTF();
		visitor.visitThreadName(threadNameId, threadName);
	}

	private void readHeader() throws IOException {
		for (char c : ThreadSamplingWriter.HEADER.toCharArray()) {
			if (c != input.readUnsignedByte()) {
				throw new IOException("invalid thread sampling file header");
			}
		}
		if (input.readByte() != 0) {
			throw new IOException("invalid thread sampling file header");
		}
		majorVersion = input.readByte();
		minorVersion = input.readByte();
		// only support 1.x
		if (majorVersion != 1) {
			throw new IOException("unsupported version: " + majorVersion + "."
					+ minorVersion);
		}
		createdTime = input.readLong();
	}

	public byte getMajorVersion() {
		return majorVersion;
	}

	public byte getMinorVersion() {
		return minorVersion;
	}

	public long getCreatedTime() {
		return createdTime;
	}

}
