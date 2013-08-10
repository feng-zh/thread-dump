package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class BlockDataOutput implements DataOutput {

	private DataOutput output;
	private InternalBuffer buffer;

	private static class InternalBuffer extends ByteArrayOutputStream {

		public void writeContent(DataOutput out) throws IOException {
			out.write(buf, 0, count);
		}
	}

	public BlockDataOutput(DataOutput output) {
		this.output = output;
	}

	public BlockDataOutput() {
		this.buffer = new InternalBuffer();
		this.output = new DataOutputStream(buffer);
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		output.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		output.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		output.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		if (v < Byte.MIN_VALUE || v > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("invalid byte: " + v);
		}
		output.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		if (v < Short.MIN_VALUE || v > Short.MAX_VALUE) {
			throw new IllegalArgumentException("invalid short: " + v);
		}
		Utils.writeVInt(output, v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		if (v < Character.MIN_VALUE || v > Character.MAX_VALUE) {
			throw new IllegalArgumentException("invalid char: " + ((int) v));
		}
		Utils.writeVInt(output, v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		Utils.writeVInt(output, v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		Utils.writeVLong(output, v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		output.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int v = s.charAt(i);
			writeChar(v);
		}
	}

	@Override
	public void writeUTF(String s) throws IOException {
		output.writeUTF(s);
	}

	public void writeTo(DataOutput out) throws IOException {
		if (out == this) {
			throw new IllegalArgumentException("self out cannot be writeTo");
		}
		out.writeInt(buffer.size());
		buffer.writeContent(out);
	}

}
