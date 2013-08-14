package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;
import com.hp.ts.rnd.tool.perf.web.RestParameter;
import com.hp.ts.rnd.tool.perf.web.RestPath;

class JpsThreadSamplingController {

	public static class JpsEntry {
		private int pid;

		private boolean self;

		private String mainClass;

		private long samplingOn;

		private long samplingDuration;

		private String stackTraces;

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public String getMainClass() {
			return mainClass;
		}

		public void setMainClass(String mainClass) {
			this.mainClass = mainClass;
		}

		public String getStackTraces() {
			return stackTraces;
		}

		public void setStackTraces(String stackTraces) {
			this.stackTraces = stackTraces;
		}

		public long getSamplingOn() {
			return samplingOn;
		}

		public void setSamplingOn(long samplingOn) {
			this.samplingOn = samplingOn;
		}

		public long getSamplingDuration() {
			return samplingDuration;
		}

		public void setSamplingDuration(long samplingDuration) {
			this.samplingDuration = samplingDuration;
		}

		public boolean isSelf() {
			return self;
		}

		public void setSelf(boolean self) {
			this.self = self;
		}

	}

	@RestPath("/jps/pid")
	public List<JpsEntry> jps() throws Exception {
		Map<Integer, String> map = Utils.jps();
		int currentPid = Utils.getCurrentPid();
		List<JpsEntry> list = new ArrayList<JpsEntry>();
		for (Entry<Integer, String> entry : map.entrySet()) {
			JpsEntry jpsEntry = new JpsEntry();
			jpsEntry.setPid(entry.getKey());
			jpsEntry.setMainClass(entry.getValue());
			jpsEntry.setSelf(currentPid == entry.getKey());
			list.add(jpsEntry);
		}
		return list;
	}

	@RestPath("/jps/pid/{pid}")
	public JpsEntry jpsDetail(@RestParameter("pid") int pid) throws Exception {
		Map<Integer, String> map = Utils.jps();
		JpsEntry jpsEntry = new JpsEntry();
		ThreadSamplerFactory factory;
		int currentPid = Utils.getCurrentPid();
		if (pid == 0) {
			pid = currentPid;
			factory = ThreadSamplings.createJvmThreadSamplerFactory();
		} else {
			factory = ThreadSamplings.createJstackThreadSamplerFactory(pid);
		}
		jpsEntry.setPid(pid);
		jpsEntry.setSelf(currentPid == pid);
		jpsEntry.setMainClass(map.get(pid));
		try {
			ThreadSamplingState state = factory.getSampler().sampling();
			StringBuilder builder = new StringBuilder();
			for (ThreadStackTrace trace : state.getStackTraces()) {
				builder.append(trace);
				builder.append("\n");
			}
			jpsEntry.setSamplingOn(state.getSamplingTime());
			jpsEntry.setSamplingDuration(TimeUnit.NANOSECONDS.toMillis(state
					.getDurationTimeNanos()));
			jpsEntry.setStackTraces(builder.toString());
		} finally {
			factory.close();
		}
		return jpsEntry;
	}
}
