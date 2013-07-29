package com.hp.ts.rnd.tool.perf.threads.weblogic;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

class WLSJmxThreadSampler implements ThreadSampler {

	private MBeanServerConnection mbsc;
	private ObjectName jvmObjName;

	public WLSJmxThreadSampler(MBeanServerConnection mbsc, ObjectName jvmObjName) {
		this.mbsc = mbsc;
		this.jvmObjName = jvmObjName;
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
			WLSJmxThreadEntry threadEntry;
			List<WLSJmxThreadEntry> threads = new ArrayList<WLSJmxThreadEntry>();
			while ((threadEntry = parser.nextThread()) != null) {
				threads.add(threadEntry);
			}
			samplingState.setCallStates(threads
					.toArray(new WLSJmxThreadEntry[threads.size()]));
		} catch (Exception e) {
			throw new ThreadSamplingException(e);
		}
		return samplingState;
	}
}