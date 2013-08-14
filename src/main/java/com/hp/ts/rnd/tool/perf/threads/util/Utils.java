package com.hp.ts.rnd.tool.perf.threads.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackFrame;

public class Utils {

	private static Class<?> VirtualMachineClass;

	public static File getJavaHome() {
		return new File(System.getProperty("java.home"));
	}

	private static File getToolsJarFile() {
		File javaHome = getJavaHome();
		File toolsJarFile = new File(new File(javaHome, "lib"), "tools.jar");
		if (!toolsJarFile.exists()) {
			toolsJarFile = new File(new File(javaHome.getParentFile(), "lib"),
					"tools.jar");
		}
		return toolsJarFile;
	}

	@SuppressWarnings("resource")
	private static Class<?> loadVirtualMachineClass() {
		try {
			try {
				return Class.forName("com.sun.tools.attach.VirtualMachine");
			} catch (ClassNotFoundException cnfe) {
				File toolsJarFile = getToolsJarFile();
				try {
					if (toolsJarFile.exists()) {
						ClassLoader loader = new URLClassLoader(
								new URL[] { toolsJarFile.toURI().toURL() },
								Utils.class.getClassLoader());
						Class<?> clz = loader
								.loadClass("com.sun.tools.attach.VirtualMachine");
						return clz;
					} else {
						throw cnfe;
					}
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new NoClassDefFoundError(e.getMessage());
		}
	}

	public static Object attachJvm(int pid) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			try {
				return VirtualMachineClass.getMethod("attach", String.class)
						.invoke(null, String.valueOf(pid));
			} catch (Exception e) {
				throw new IOException(e);
			}
		} catch (NoClassDefFoundError e) {
			throw new UnsupportedOperationException("Not support attach to JVM");
		}
	}

	public static void detachJvm(Object vm) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			VirtualMachineClass.getMethod("detach").invoke(vm);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static StackTraceElementWrapper[] createStackFrames(
			StackTraceElement[] traceElements) {
		StackTraceElementWrapper[] stackFrames = new StackTraceElementWrapper[traceElements.length];
		for (int i = 0, n = traceElements.length; i < n; i++) {
			stackFrames[i] = new StackTraceElementWrapper(traceElements[i]);
		}
		return stackFrames;
	}

	public static InputStream remoteDataDump(Object vm) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			Method method = vm.getClass().getMethod("remoteDataDump",
					Object[].class);
			return (InputStream) (method.invoke(vm,
					new Object[] { new Object[0] }));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static StackTraceElementWrapper[] convertStackFrames(
			GeneralThreadStackFrame[] stackFrames) {
		StackTraceElementWrapper[] ret = new StackTraceElementWrapper[stackFrames.length];
		for (int i = 0; i < stackFrames.length; i++) {
			ret[i] = new StackTraceElementWrapper(
					stackFrames[i].toTraceElement());
		}
		return ret;
	}

	public static StackTraceElementWrapperWithLocks[] convertStackFramesWithLocks(
			GeneralThreadStackFrame[] stackFrames) {
		StackTraceElementWrapperWithLocks[] ret = new StackTraceElementWrapperWithLocks[stackFrames.length];
		for (int i = 0; i < stackFrames.length; i++) {
			StackTraceElementWrapperWithLocks value = new StackTraceElementWrapperWithLocks(
					stackFrames[i].toTraceElement());
			List<ThreadStackLockInfo> lockInfos = stackFrames[i].getLockInfos();
			if (lockInfos != null) {
				for (ThreadStackLockInfo lockInfo : lockInfos) {
					value.addLockInfo(lockInfo);
				}
			}
			ret[i] = value;
		}
		return ret;
	}

	// refer to Lucense IndexOutput
	public static final void writeVInt(DataOutput output, int i)
			throws IOException {
		for (; (i & 0xFFFFFF80) != 0; i >>>= 7) {
			output.writeByte((byte) (i & 0x7F | 0x80));
		}
		output.writeByte((byte) i);
	}

	public static final void writeVLong(DataOutput out, long l)
			throws IOException {
		for (; (l & 0xFFFFFFFFFFFFFF80L) != 0; l >>>= 7) {
			out.writeByte((byte) (l & 0x7F | 0x80));
		}
		out.writeByte((byte) l);
	}

	// refer to Lucense IndexInput
	public static final int readVInt(DataInput input) throws IOException {
		byte b = input.readByte();
		int i = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = input.readByte();
			i |= (b & 0x7F) << shift;
		}
		return i;
	}

	public static final long readVLong(DataInput in) throws IOException {
		byte b = in.readByte();
		long l = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = in.readByte();
			l |= ((long) (b & 0x7F)) << shift;
		}
		return l;
	}

	public static Map<Integer, String> jps() throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			Method method = VirtualMachineClass.getMethod("list");
			List<?> vmList = (List<?>) method.invoke(null);
			Map<Integer, String> ret = new LinkedHashMap<Integer, String>();
			for (Object vm : vmList) {
				Integer pid = Integer.parseInt((String) vm.getClass()
						.getMethod("id").invoke(vm));
				String displayName = (String) vm.getClass()
						.getMethod("displayName").invoke(vm);
				ret.put(pid, displayName);
			}
			return ret;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static int getCurrentPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int pos = name.indexOf('@');
		if (pos >= 0) {
			try {
				return Integer.parseInt(name.substring(0, pos));
			} catch (NumberFormatException ignored) {
			}
		}
		return 0;
	}

}
