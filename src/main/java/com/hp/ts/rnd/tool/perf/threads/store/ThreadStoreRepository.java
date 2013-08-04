package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

class ThreadStoreRepository {

	private static final FrameIdHelper FRAMEID_HELPER = FrameIdHelper
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

	public String getFileNameByFrameId(long frameId) {
		HierarchyStringNode classNode = getClassNode(frameId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getFileName(FRAMEID_HELPER.getFileNameId(frameId),
				classNode.getChars());
	}

	public int getLineNumberByFrameId(long frameId) {
		int fileNameId = FRAMEID_HELPER.getFileNameId(frameId);
		int lineNo = FRAMEID_HELPER.getLineNo(frameId);
		if (lineNo == 0) {
			if (fileNameId == ClassInternal.FILENAME_NATIVE) {
				return -2;
			} else if (fileNameId == ClassInternal.FILENAME_UKNOWN) {
				return -1;
			}
		}
		return lineNo;
	}

	public String getClassNameByFrameId(long frameId) {
		HierarchyStringNode classNode = getClassNode(frameId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getClassName();
	}

	private HierarchyStringNode getClassNode(long frameId) {
		int classNameId = FRAMEID_HELPER.getClassNameId(frameId);
		HierarchyStringNode node = classNameNodes.get(classNameId);
		if (node == null) {
			throw new IllegalStateException(
					"not found class node at class name id: " + classNameId
							+ ", current node size: " + classNameNodes.size()
							+ ", frameId: " + FRAMEID_HELPER.toString(frameId));
		}
		return node;
	}

	public String getMethodNameByFrameId(long frameId) {
		HierarchyStringNode classNode = getClassNode(frameId);
		ClassInternal classData = (ClassInternal) classNode.getValue();
		return classData.getMethodName(FRAMEID_HELPER.getMethodNameId(frameId));
	}

	public long createStackFrameId(ThreadStackFrame frame) {
		String fileName = frame.getFileName();
		String className = frame.getClassName();
		String methodName = frame.getMethodName();
		int lineNo = frame.getLineNumber();
		return createStackFrameId(className, methodName, fileName, lineNo);
	}

	long createStackFrameId(String className, String methodName,
			String fileName, int lineNo) {
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
			return toStackFrameIndex(classIndex, methodIndex, fileNameIndex,
					lineNo);
		}
	}

	private long toStackFrameIndex(int classIndex, int methodIndex,
			int fileNameIndex, int lineNo) {
		// make sure line no is positive
		if (lineNo < 0) {
			if (fileNameIndex == ClassInternal.FILENAME_NATIVE
					|| fileNameIndex == ClassInternal.FILENAME_UKNOWN) {
				lineNo = 0;
			}
		}
		return FRAMEID_HELPER.getFrameId(fileNameIndex, classIndex,
				methodIndex, lineNo);
	}

	public int createThreadNameId(String threadName) {
		return threadNameTable.putIfAbsent(threadName);
	}

	public long[] createStackFrameIds(ThreadStackFrame[] stackFrames) {
		int n = stackFrames.length;
		long[] ret = new long[n];
		for (int i = 0; i < n; i++) {
			ret[i] = createStackFrameId(stackFrames[i]);
		}
		return ret;
	}

	public String getThreadNameByThreadNameId(int threadNameId) {
		return threadNameTable.get(threadNameId);
	}

	public ThreadStackFrame[] getStackFramesByFrameIds(long[] stackFrameIds) {
		int n = stackFrameIds.length;
		ThreadStackFrame[] ret = new ThreadStackFrame[n];
		for (int i = 0; i < n; i++) {
			ret[i] = getStackFrameByFrameId(stackFrameIds[i]);
		}
		return ret;
	}

	public ThreadStackFrame getStackFrameByFrameId(long stackFrameId) {
		// validate class node exists
		getClassNode(stackFrameId);
		return new StoredThreadStackFrame(this, stackFrameId);
	}

}
