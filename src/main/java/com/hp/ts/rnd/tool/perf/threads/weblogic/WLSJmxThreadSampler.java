package com.hp.ts.rnd.tool.perf.threads.weblogic;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

class WLSJmxThreadSampler implements ThreadSampler {

	private MBeanServerConnection mbsc;
	private ObjectName jvmObjName;
	private boolean ignoreSamplingThread;

	public WLSJmxThreadSampler(MBeanServerConnection mbsc,
			ObjectName jvmObjName, boolean ignoreSamplingThread) {
		this.mbsc = mbsc;
		this.jvmObjName = jvmObjName;
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	public ThreadSamplingState sampling() throws ThreadSamplingException {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		try {
			samplingState.startSampling();
			String threadDump = (String) mbsc.getAttribute(jvmObjName,
					"ThreadStackDump");
			samplingState.endSampling();
			WLSThreadStackDumpParser parser = new WLSThreadStackDumpParser(
					threadDump);
			WLSJmxThreadStackTrace threadStackTrace;
			List<WLSJmxThreadStackTrace> threads = new ArrayList<WLSJmxThreadStackTrace>();
			while ((threadStackTrace = parser.nextThreadStackTrace()) != null) {
				if (ignoreSamplingThread
						&& isSamplingStackTrace(threadStackTrace)) {
					continue;
				}
				threads.add(threadStackTrace);
			}
			samplingState.setStackTraces(threads
					.toArray(new WLSJmxThreadStackTrace[threads.size()]));
		} catch (Exception e) {
			throw new ThreadSamplingException(e);
		}
		return samplingState;
	}

	private boolean isSamplingStackTrace(WLSJmxThreadStackTrace threadStackTrace) {
		// suppose we can find it in last 8 methods call
		int max = 8;
		for (ThreadStackFrame stackFrame : threadStackTrace.getStackFrameList()) {
			if (max-- < 0) {
				break;
			}
			if (stackFrame.getClassName().endsWith("JVMRuntime")
					&& stackFrame.getMethodName().equals("getThreadStackDump")) {
				return true;
			}
		}
		return false;
	}
}
