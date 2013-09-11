package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;

class JmxThreadSampler implements ThreadSampler {

	private ThreadMXBean mbean;
	private boolean objMonitorSupported;
	private boolean synchSupported;
	private boolean ignoreSamplingThread;

	public JmxThreadSampler(ThreadMXBean mbean) {
		this.mbean = mbean;
		this.objMonitorSupported = mbean.isObjectMonitorUsageSupported();
		this.synchSupported = mbean.isSynchronizerUsageSupported();
	}

	public ThreadSamplingState sampling() {
		ThreadSamplingState samplingState = new ThreadSamplingState();
		samplingState.setSamplingTime(System.currentTimeMillis());
		samplingState.startSampling();
		ThreadInfo[] threadInfos = mbean.dumpAllThreads(objMonitorSupported,
				synchSupported);
		samplingState.endSampling();
		List<JmxThreadStackTrace> stackTraces = new ArrayList<JmxThreadStackTrace>(
				threadInfos.length);
		for (int i = 0, n = threadInfos.length; i < n; i++) {
			JmxThreadStackTrace jmxThreadStackTrace = new JmxThreadStackTrace(
					threadInfos[i]);
			if (ignoreSamplingThread
					&& isSamplingStackTrace(jmxThreadStackTrace)) {
				continue;
			}
			stackTraces.add(jmxThreadStackTrace);
		}
		samplingState.setStackTraces(stackTraces
				.toArray(new JmxThreadStackTrace[stackTraces.size()]));
		return samplingState;
	}

	private boolean isSamplingStackTrace(JmxThreadStackTrace threadStackTrace) {
		// suppose we can find it in last 3 methods call
		int max = 3;
		for (ThreadStackFrame stackFrame : threadStackTrace.getStackFrames()) {
			if (max-- < 0) {
				break;
			}
			if (stackFrame.getClassName().equals("sun.management.ThreadImpl")
					&& stackFrame.getMethodName().equals("dumpAllThreads")) {
				return true;
			}
		}
		return false;
	}
}
