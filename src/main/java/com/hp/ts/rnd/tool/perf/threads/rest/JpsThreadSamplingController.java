package com.hp.ts.rnd.tool.perf.threads.rest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;
import com.hp.ts.rnd.tool.perf.web.RestEntity;
import com.hp.ts.rnd.tool.perf.web.RestMethod;
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

	public static class JpsSampling {

		private int duration;

		private String durationUnit;

		private int interval;

		private String intervalUnit;

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public String getDurationUnit() {
			return durationUnit;
		}

		public void setDurationUnit(String durationUnit) {
			this.durationUnit = durationUnit;
		}

		public int getInterval() {
			return interval;
		}

		public void setInterval(int interval) {
			this.interval = interval;
		}

		public String getIntervalUnit() {
			return intervalUnit;
		}

		public void setIntervalUnit(String intervalUnit) {
			this.intervalUnit = intervalUnit;
		}

		static int normilizeTime(int time, String unit) {
			if ("minute".equals(unit)) {
				return (int) TimeUnit.MINUTES.toSeconds(time);
			} else if ("second".equals(unit)) {
				return time;
			} else if ("hour".equals(unit)) {
				return (int) TimeUnit.HOURS.toSeconds(time);
			} else if ("millisec".equals(unit)) {
				return (int) TimeUnit.MILLISECONDS.toSeconds(time);
			} else {
				throw new IllegalArgumentException("invalid timeunit " + unit);
			}
		}

	}

	private Map<String, SamplingEntry> currentSampling = new HashMap<String, SamplingEntry>();

	private static class SamplingEntry {
		ThreadSamplingService service;
		ByteArrayOutputStream output;
		String uuid;
		boolean done;
		long startedOn;
		long expectedFinished;
		long finishedOn;
		long sampleCount;
		long traceCount;
		JpsSampling samplingRequest;
		ThreadSamplingException error;
		public ThreadSamplerFactory factory;
	}

	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	@RestPath("jps/pid")
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

	@RestPath("jps/pid/{pid}")
	public JpsEntry jpsDetail(@RestParameter("pid") int pid) throws Exception {
		ThreadSamplerFactory factory;
		int currentPid = Utils.getCurrentPid();
		if (pid == 0) {
			pid = currentPid;
			factory = ThreadSamplings.createJvmThreadSamplerFactory();
		} else {
			factory = ThreadSamplings.createJstackThreadSamplerFactory(pid);
		}
		Map<Integer, String> map = Utils.jps();
		JpsEntry jpsEntry = new JpsEntry();
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

	@RestMethod("POST")
	@RestPath("jps/pid/{pid}/sampling")
	public String sampling(@RestParameter("pid") int pid,
			@RestEntity JpsSampling request) throws Exception {
		final SamplingEntry samplingEntry = new SamplingEntry();
		ThreadSamplerFactory factory;
		int currentPid = Utils.getCurrentPid();
		if (pid == 0 || currentPid == pid) {
			factory = ThreadSamplings.createJvmThreadSamplerFactory();
		} else {
			factory = ThreadSamplings.createJstackThreadSamplerFactory(pid);
		}
		samplingEntry.factory = factory;
		samplingEntry.uuid = UUID.randomUUID().toString();
		samplingEntry.samplingRequest = request;
		int durationSeconds = JpsSampling.normilizeTime(request.getDuration(),
				request.getDurationUnit());
		ThreadSamplingService service = ThreadSamplings
				.createSimpleSamplingService(factory.getSampler(),
						durationSeconds, JpsSampling.normilizeTime(
								request.getInterval(),
								request.getIntervalUnit()));
		samplingEntry.service = service;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(bytes);
		samplingEntry.output = bytes;
		samplingEntry.startedOn = System.currentTimeMillis();
		samplingEntry.expectedFinished = samplingEntry.startedOn
				+ durationSeconds * 1000;
		service.executeSampling(ThreadSamplings.createChainedSamplingHandler(
				ThreadSamplings.createCallTree(printer),
				new ThreadSamplingHandler() {

					@Override
					public void onSampling(ThreadSamplingState state) {
						samplingEntry.sampleCount++;
						samplingEntry.traceCount += state.getStackTraces().length;
					}

					@Override
					public void onError(ThreadSamplingException exception)
							throws ThreadSamplingException {
						samplingEntry.error = exception;
					}

					@Override
					public void onEnd() {
						samplingEntry.done = true;
						samplingEntry.finishedOn = System.currentTimeMillis();
					}
				}));
		currentSampling.put(samplingEntry.uuid, samplingEntry);
		System.out.println(bytes.toString());
		return samplingEntry.uuid;
	}

	@RestPath("jps/pid/{pid}/sampling/{uuid}")
	public String getSampling(@RestParameter("pid") int pid,
			@RestParameter("uuid") String uuid) {
		SamplingEntry entry = currentSampling.get(uuid);
		if (entry == null) {
			return "NOT FOUND";
		} else if (entry.done) {
			currentSampling.remove(uuid);
			return entry.output.toString();
		} else {
			return "IN PROGRESS";
		}
	}

	@RestMethod("DELETE")
	@RestPath("jps/pid/{pid}/sampling/{uuid}")
	public void stopSampling(@RestParameter("pid") int pid,
			@RestParameter("uuid") String uuid) {
		SamplingEntry entry = currentSampling.remove(uuid);
		if (entry != null && !entry.done) {
			entry.service.closeSampling();
			entry.factory.close();
		}
	}
}
