package com.hp.ts.rnd.tool.perf.threads.store;

import java.io.DataInput;
import java.io.IOException;

import com.hp.ts.rnd.tool.perf.threads.util.Utils;

class BlockDataInput implements DataInput {

	private DataInput input;

	public BlockDataInput(DataInput input) {
		this.input = input;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		input.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		input.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return input.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return input.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return input.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return input.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return (short) Utils.readVInt(input);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Utils.readVInt(input);
	}

	@Override
	public char readChar() throws IOException {
		return (char) Utils.readVInt(input);
	}

	@Override
	public int readInt() throws IOException {
		return Utils.readVInt(input);
	}

	@Override
	public long readLong() throws IOException {
		return Utils.readVLong(input);
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public String readLine() throws IOException {
		return input.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return input.readUTF();
	}

}
