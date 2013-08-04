package com.hp.ts.rnd.tool.perf.threads;

import java.io.File;
import java.io.IOException;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class Utils {

	public static File getJavaHome() {
		return new File(System.getProperty("java.home"));
	}

	public static File getToolsJarFile() {
		File javaHome = getJavaHome();
		File toolsJarFile = new File(new File(javaHome, "lib"), "tools.jar");
		if (!toolsJarFile.exists()) {
			toolsJarFile = new File(new File(javaHome.getParentFile(), "lib"),
					"tools.jar");
		}
		return toolsJarFile;
	}

	public static Object attachJvm(int pid) throws IOException {
		try {
			try {
				return VirtualMachine.attach(String.valueOf(pid));
			} catch (AttachNotSupportedException e) {
				throw new IOException(e);
			}
		} catch (NoClassDefFoundError e) {
			throw new UnsupportedOperationException("Not support attach to JVM");
		}
	}

	public static void detachJvm(Object vm) throws IOException {
		((VirtualMachine) vm).detach();
	}

	public static StackTraceElementWrapper[] createStackTraceWrappers(
			StackTraceElement[] traceElements) {
		StackTraceElementWrapper[] wrapper = new StackTraceElementWrapper[traceElements.length];
		for (int i = 0, n = traceElements.length; i < n; i++) {
			wrapper[i] = new StackTraceElementWrapper(traceElements[i]);
		}
		return wrapper;
	}
}
