package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class ThreadStoreRepository {

	private static final TraceIdHelper TRACEID_HELPER = TraceIdHelper
			.getInstance();

	private IndexTable<HierarchyStringNode> classNameNodes = new IndexTable<HierarchyStringNode>();

	private IndexTable<String> threadNameTable = new IndexTable<String>();

	{
		// 0 - is null
		classNameNodes.add(null);
		// 0 - is not support thread id
		threadNameTable.add(null);
	}

	private static class ClassInternal {
		private IndexTable<String> methodTable = new IndexTable<String>();
		final static int FILENAME_DEFAULT = 0;
		final static int FILENAME_UKNOWN = 1;
		final static int FILENAME_NATIVE = 2;
		final static int FILENAME_RESERVED = 3;
		private IndexTable<String> fileNameTable;
		private final String className;

		public ClassInternal(String className) {
			this.className = className;
		}

		public int locateMethod(String methodName) {
			return methodTable.putIfAbsent(methodName);
		}

		public int locateFileName(String className, String fileName) {
			int index;
			if (fileNameTable != null) {
				index = fileNameTable.indexOf(fileName);
				if (index != -1) {
					return index + FILENAME_RESERVED;
				}
			}
			if (fileName == null) {
				return FILENAME_UKNOWN;
			} else if (fileName.endsWith(".java")) {
				int lenClassName = fileName.length() - 5;
				if (className.startsWith(fileName.substring(0, lenClassName))) {
					if (className.length() == lenClassName
							|| className.charAt(lenClassName) == '$') {
						// full match or outer class name match
						return FILENAME_DEFAULT;
					}
				}
			} else if ("Native Method".equals(fileName)) {
				return FILENAME_NATIVE;
			}
			if (fileNameTable == null) {
				fileNameTable = new IndexTable<String>(4);
			}
			// other type
			index = fileNameTable.add(fileName);
			return index + FILENAME_RESERVED;
		}

		public String getMethodName(int methodNameId) {
			return methodTable.get(methodNameId);
		}

		public String getFileName(int fileNameId, String classSimpleName) {
			switch (fileNameId) {
			case FILENAME_DEFAULT:
				int indexOfOuterName = classSimpleName.indexOf('$');
				if (indexOfOuterName == -1) {
					return classSimpleName + ".java";
				} else {
					return classSimpleName.substring(0, indexOfOuterName)
							+ ".java";
				}
			case FILENAME_UKNOWN:
				return null;
			case FILENAME_NATIVE:
				return "Native Method";
			default:
				return fileNameTable.get(fileNameId - 3);
			}
		}

		public String getClassName() {
			return className;
		}

		@Override
		public String toString() {
			return String
					.format("ClassInternal [className=%s, method-count=%s, filename=%s]",
							className, methodTable.size(),
							fileNameTable == null ? "0" : fileNameTable);
		}

	}

	public String getFileNameByTraceId(long traceId) {
		HierarchyStringNode classNode = getClassNode(traceId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getFileName(TRACEID_HELPER.getFileNameId(traceId),
				classNode.getChars());
	}

	public int getLineNumberByTraceId(long traceId) {
		int fileNameId = TRACEID_HELPER.getFileNameId(traceId);
		int lineNo = TRACEID_HELPER.getLineNo(traceId);
		if (lineNo == 0) {
			if (fileNameId == ClassInternal.FILENAME_NATIVE) {
				return -2;
			} else if (fileNameId == ClassInternal.FILENAME_UKNOWN) {
				return -1;
			}
		}
		return lineNo;
	}

	public String getClassNameByTraceId(long traceId) {
		HierarchyStringNode classNode = getClassNode(traceId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getClassName();
	}

	private HierarchyStringNode getClassNode(long traceId) {
		int classNameId = TRACEID_HELPER.getClassNameId(traceId);
		HierarchyStringNode node = classNameNodes.get(classNameId);
		if (node == null) {
			throw new IllegalStateException(
					"not found class node at class name id: " + classNameId
							+ ", current node size: " + classNameNodes.size()
							+ ", traceid: " + TRACEID_HELPER.toString(traceId));
		}
		return node;
	}

	public String getMethodNameByTraceId(long traceId) {
		HierarchyStringNode classNode = getClassNode(traceId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getMethodName(TRACEID_HELPER.getMethodNameId(traceId));
	}

	public long createTraceId(ThreadStackTrace trace) {
		String fileName = trace.getFileName();
		String className = trace.getClassName();
		String methodName = trace.getMethodName();
		int lineNo = trace.getLineNumber();

		// Separate class name with '.'
		int packageIndex = 0;
		int nodeIndex = 0;
		HierarchyStringNode node;
		do {
			int prevIndex = packageIndex;
			packageIndex = className.indexOf('.', prevIndex);
			if (packageIndex >= 0) {
				// not simple name
				node = new HierarchyStringNode(nodeIndex, className.substring(
						prevIndex, packageIndex));
			} else {
				// last simple name
				node = new HierarchyStringNode(nodeIndex,
						className.substring(prevIndex));
			}
			synchronized (classNameNodes) {
				int nIndex = classNameNodes.indexOf(node);
				if (nIndex == -1) {
					// package not found
					nIndex = classNameNodes.add(node);
					node.setIndex(nIndex);
					nodeIndex = nIndex;
				} else {
					nodeIndex = nIndex;
				}
				node = classNameNodes.get(nodeIndex);
			}
			packageIndex++;
		} while (packageIndex > 0);

		// process class node
		synchronized (node) {
			ClassInternal classInternal = (ClassInternal) node.getValue();
			if (classInternal == null) {
				classInternal = new ClassInternal(className);
				node.setValue(classInternal);
			}
			int classIndex = node.getIndex();
			int methodIndex = classInternal.locateMethod(methodName);
			int fileNameIndex = classInternal.locateFileName(node.getChars(),
					fileName);
			return toStackIndex(classIndex, methodIndex, fileNameIndex, lineNo);
		}
	}

	private long toStackIndex(int classIndex, int methodIndex,
			int fileNameIndex, int lineNo) {
		// make sure line no is positive
		if (lineNo < 0) {
			if (fileNameIndex == ClassInternal.FILENAME_NATIVE
					|| fileNameIndex == ClassInternal.FILENAME_UKNOWN) {
				lineNo = 0;
			}
		}
		return TRACEID_HELPER.getTraceId(fileNameIndex, classIndex,
				methodIndex, lineNo);
	}

	public int createThreadNameId(String threadName) {
		return threadNameTable.putIfAbsent(threadName);
	}

	public long[] createStackTraceIds(ThreadStackTrace[] stackTraces) {
		int n = stackTraces.length;
		long[] ret = new long[n];
		for (int i = 0; i < n; i++) {
			ret[i] = createTraceId(stackTraces[i]);
		}
		return ret;
	}

	public String getThreadNameByThreadNameId(int threadNameId) {
		return threadNameTable.get(threadNameId);
	}

	public long createThreadId(long threadIdentifier, String threadName) {
		if (threadIdentifier == ThreadCallState.THREAD_ID_NOTSUPPOT) {
			return createThreadNameId(threadName);
		}
		return threadIdentifier;
	}

	public ThreadStackTrace[] getStackTracesByTraceIds(long[] stackTraces) {
		int n = stackTraces.length;
		ThreadStackTrace[] ret = new ThreadStackTrace[n];
		for (int i = 0; i < n; i++) {
			ret[i] = getStackTraceByTraceId(stackTraces[i]);
		}
		return ret;
	}

	public ThreadStackTrace getStackTraceByTraceId(long traceId) {
		// validate class node exists
		getClassNode(traceId);
		return new StoredThreadStackTrace(this, traceId);
	}

}
