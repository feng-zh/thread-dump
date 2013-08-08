package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class ThreadSamplingReader {

	static class BlockDataInput extends DataInputStream {

		public BlockDataInput(int len, DataInput input) throws IOException {
			super(createBlockStream(input, len));
		}

		private static InputStream createBlockStream(DataInput input, int len)
				throws IOException {
			byte[] bytes = new byte[len];
			input.readFully(bytes);
			return new ByteArrayInputStream(bytes);
		}

	}

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
		do {
			byte type;
			try {
				type = input.readByte();
			} catch (EOFException e) {
				return false;
			}
			DataInputStream blockInput = new BlockDataInput(input.readInt(),
					input);
			switch (type) {
			case ThreadSamplingWriter.TYPE_THREAD_NAME: {
				// thread id
				int threadNameId = blockInput.readInt();
				String threadName = blockInput.readUTF();
				blockInput.close();
				visitor.visitThreadName(threadNameId, threadName);
				break;
			}
			case ThreadSamplingWriter.TYPE_STACK_FRAME: {
				long stackFrameId = blockInput.readLong();
				String className = blockInput.readUTF();
				String methodName = blockInput.readUTF();
				String fileName;
				if (blockInput.readByte() == 0) {
					fileName = null;
				} else {
					fileName = blockInput.readUTF();
				}
				int lineNumber = blockInput.readInt();
				blockInput.close();
				visitor.visitStackFrame(stackFrameId, className, methodName,
						fileName, lineNumber);
				break;
			}
			case ThreadSamplingWriter.TYPE_SAMPLING_STATE: {
				long samplingTime = blockInput.readLong();
				long startTimeMillis = blockInput.readLong();
				long durationTimeNanos = blockInput.readLong();
				int stackTracesLen = blockInput.readInt();
				ThreadSamplingState samplingState = new ThreadSamplingState(
						startTimeMillis, durationTimeNanos);
				samplingState.setSamplingTime(samplingTime);
				ThreadStackTrace[] stackTraces = new ThreadStackTrace[stackTracesLen];
				for (int i = 0; i < stackTracesLen; i++) {
					long threadIdentifier = blockInput.readLong();
					int threadNameId = blockInput.readInt();
					byte threadStateId = blockInput.readByte();
					Thread.State threadState = threadStateId == 0 ? null
							: Thread.State.values()[threadStateId - 1];
					int stackFrameLen = blockInput.readInt();
					long[] stackFrameIds = new long[stackFrameLen];
					for (int j = 0; j < stackFrameLen; j++) {
						stackFrameIds[j] = blockInput.readLong();
					}
					stackTraces[i] = visitor.visitStackTrace(threadIdentifier,
							threadNameId, threadState, stackFrameIds);
				}
				samplingState.setStackTraces(stackTraces);
				blockInput.close();
				visitor.visitThreadSampling(samplingState);
				break;
			}
			default:
				blockInput.close();
				throw new IOException("unknown type: " + type);
			}
		} while (loop);
		return true;
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
