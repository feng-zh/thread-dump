package com.hp.ts.rnd.tool.perf.threads.store;

public class TraceIdHelper {

	private static interface TraceIdMask {

		public long decode(long value);

		public long encode(long acid, long value);

		public long mask(long acid, boolean mask);

		public int getRightBit();

	}

	private static class SingleBitsMask implements TraceIdMask {

		private final String name;

		private final int leftBit;
		private final int rightBit;
		private final int unmaskBit;
		private final long minValue;
		private final long maxValue;
		private final long maskValue;
		private final long unmaskValue;

		SingleBitsMask(String name, int leftBit, int maskBit) {
			if (name == null) {
				throw new IllegalArgumentException("null mask name");
			}
			this.name = name;
			if (leftBit < 0 || leftBit > 64) {
				throw new IndexOutOfBoundsException(
						"left bit is out of range [0, 64]: " + leftBit);
			}
			this.leftBit = leftBit;
			if (maskBit < 0 || maskBit > 64) {
				throw new IndexOutOfBoundsException(
						"mask bit is out of range [0, 64]: " + maskBit);
			}
			this.rightBit = 64 - maskBit - leftBit;
			if (rightBit < 0 || rightBit > 64) {
				throw new IndexOutOfBoundsException(
						"right bit is out of range [0, 64]: " + maskBit);
			}
			this.unmaskBit = 64 - maskBit;
			this.minValue = 0;
			this.maxValue = (1L << maskBit) - 1;
			// for or operation to set, like 000_1111_000
			this.maskValue = (~0L) << unmaskBit >>> leftBit;
			// for and operation to clear, like 111_0000_111
			this.unmaskValue = ~maskValue;
		}

		public int getRightBit() {
			return rightBit;
		}

		public long decode(long value) {
			return (value << leftBit >>> unmaskBit);
		}

		public long encode(long acid, long value) {
			if (value >= minValue && value <= maxValue) {
				// clear mask bits
				acid &= unmaskValue;
				// set mask bits
				acid |= (value << rightBit) & maskValue;
				return acid;
			}
			throw new IllegalArgumentException(name
					+ " is OutOfRange. Value is: " + value
					+ ", accept range is is: [" + minValue + ", " + maxValue
					+ "]");
		}

		@Override
		public long mask(long acid, boolean mask) {
			if (mask) {
				return acid | maskValue;
			} else {
				return acid & unmaskValue;
			}
		}

	}

	private static TraceIdHelper instance = new TraceIdHelper();

	// 36 ~ 47 (12) - method name (supports 2048)
	// 48 ~ 63 (16) - line no (supports 65535)
	// reserve: (16) 0 - 15
	private final TraceIdMask reserveMask = new SingleBitsMask("Reserve", 0, 16);
	// reserve: (1) 16 - 16
	private final TraceIdMask versionMask = new SingleBitsMask("Version",
			64 - reserveMask.getRightBit(), 1);
	// file name: (3) 17 - 19 (0:default, 1: unknown, 2: native, 3~7: others)
	private final TraceIdMask fileNameIdMask = new SingleBitsMask("FileName",
			64 - versionMask.getRightBit(), 3);
	// class name: (16) 20 - 35
	private final TraceIdMask classNameMask = new SingleBitsMask("ClassName",
			64 - fileNameIdMask.getRightBit(), 16);
	// method name: (12) 36 - 47
	private final TraceIdMask methodNameMask = new SingleBitsMask("MethodName",
			64 - classNameMask.getRightBit(), 12);
	// line number: (16) 48 - 63
	private final TraceIdMask lineNoMask = new SingleBitsMask("LineNumber",
			64 - methodNameMask.getRightBit(), methodNameMask.getRightBit());

	private final int RESEVED_VALUE = 0;

	private TraceIdHelper() {
	}

	public static TraceIdHelper getInstance() {
		return instance;
	}

	public long getTraceId(int fileNameId, int classNameId, int methodNameId,
			int lineNo) {
		long id = 0L;

		id = reserveMask.encode(id, RESEVED_VALUE);
		id = versionMask.encode(id, RESEVED_VALUE);
		id = fileNameIdMask.encode(id, fileNameId);
		id = classNameMask.encode(id, classNameId);
		id = methodNameMask.encode(id, methodNameId);
		id = lineNoMask.encode(id, lineNo);

		return id;
	}

	public int getFileNameId(long id) {
		return (int) fileNameIdMask.decode(id);
	}

	public int getClassNameId(long id) {
		return (int) classNameMask.decode(id);
	}

	public int getMethodNameId(long id) {
		return (int) methodNameMask.decode(id);
	}

	public int getLineNo(long id) {
		return (int) lineNoMask.decode(id);
	}

	public static boolean isUnassigned(long acid) {
		return acid == 0;
	}

	public String toHexString(long id) {
		return Long.toHexString(id).toUpperCase();
	}

	public long parseHexString(String str) {
		return Long.parseLong(str, 16);
	}

	public String toString(long id) {
		if (isUnassigned(id)) {
			return toHexString(id) + "(" + id + "->unassigned)";
		}
		int fileName = getFileNameId(id);
		int className = getClassNameId(id);
		int methodName = getMethodNameId(id);
		int lineNo = getLineNo(id);
		String result = "0x" + toHexString(id) + "(" + id + "->F"
				+ String.valueOf(fileName) + "_C" + className + "_M"
				+ methodName + "_L" + lineNo + ")";
		return result;
	}

	public long setClassNameId(long id, int value) {
		return classNameMask.encode(id, value);
	}

	public long setMethodNameId(long id, int value) {
		return methodNameMask.encode(id, value);
	}

	public long setLineNo(long id, int value) {
		return lineNoMask.encode(id, value);
	}

	public long setFileNameId(long id, int value) {
		return fileNameIdMask.encode(id, value);
	}

}
