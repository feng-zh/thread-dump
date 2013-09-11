package com.hp.ts.rnd.tool.perf.threads.rest;

import java.io.FileNotFoundException;
import java.io.ObjectOutput;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.web.annotation.RestEntity;
import com.hp.ts.rnd.tool.perf.web.annotation.RestMethod;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPath;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPathParameter;
import com.hp.ts.rnd.tool.perf.web.annotation.RestServerSentEventStream;

class ThreadSamplerAgentController {

	private Map<String, ThreadSamplerAgent> agents = new HashMap<String, ThreadSamplerAgent>();
	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1, new ThreadFactory() {

				private ThreadFactory threadFactory = Executors
						.defaultThreadFactory();

				@Override
				public Thread newThread(Runnable r) {
					Thread thread = threadFactory.newThread(r);
					thread.setDaemon(true);
					return thread;
				}
			});

	@RestPath("samplers")
	public Map<String, ThreadSamplerAgent> list() {
		return agents;
	}

	// not rest method
	void createSamplerAgent(ThreadSamplerFactory factory,
			ThreadSamplerAgentEntry agentInfo) {
		if (!agents.containsKey(agentInfo.getAgentId())) {
			ThreadSamplerAgent agent = new ThreadSamplerAgent(agentInfo,
					factory, scheduler);
			agents.put(agentInfo.getAgentId(), agent);
		}
	}

	@RestMethod("DELETE")
	@RestPath("samplers/{id}")
	public void delete(@RestPathParameter("id") String agentId) {
		ThreadSamplerAgent agent = agents.remove(agentId);
		if (agent != null) {
			agent.close();
		}
	}

	@RestMethod("GET")
	@RestPath("samplers/{id}/dumpStack")
	public ThreadDumpResult dumpStack(@RestPathParameter("id") String agentId)
			throws FileNotFoundException {
		return locate(agentId).dumpStack();
	}

	@RestMethod("POST")
	@RestPath("samplers/{id}/sampling")
	public void startSampling(@RestPathParameter("id") String agentId,
			@RestEntity ThreadSamplingRequestForm request)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = locate(agentId);
		agent.startSampling(ThreadSamplingRequestForm.normilizeTime(
				request.getDuration(), request.getDurationUnit(),
				TimeUnit.SECONDS), ThreadSamplingRequestForm.normilizeTime(
				request.getInterval(), request.getIntervalUnit(),
				TimeUnit.MILLISECONDS));
	}

	@RestMethod("POST")
	@RestPath("samplers/{id}/filter")
	public void applyFilter(@RestPathParameter("id") String agentId,
			@RestEntity ThreadSamplingFilter request)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = locate(agentId);
		agent.applyFilter(request.getInclude(), request.getExclude(),
				request.getThreshold());
	}

	private ThreadSamplerAgent locate(String agentId)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = agents.get(agentId);
		if (agent != null) {
			return agent;
		} else {
			throw new FileNotFoundException("agent not found: " + agentId);
		}
	}

	@RestPath("samplers/{id}/sampling")
	public ThreadSamplingStatus getSampling(
			@RestPathParameter("id") String agentId)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = locate(agentId);
		return agent.getSamplingStatus();
	}

	@RestMethod("DELETE")
	@RestPath("samplers/{id}/sampling")
	public void removeSampling(@RestPathParameter("id") String agentId)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = locate(agentId);
		agent.closeSampling();
	}

	@RestPath("samplers/{id}/sampling-monitor")
	public void monitorSampling(@RestPathParameter("id") String agentId,
			@RestServerSentEventStream ObjectOutput eventStream)
			throws FileNotFoundException {
		ThreadSamplerAgent agent = locate(agentId);
		agent.monitorSamplingStatus(eventStream);
	}

	boolean agentExist(String agentId) {
		return agents.containsKey(agentId);
	}

	public ThreadSamplerAgent getAgent(String agentId)
			throws FileNotFoundException {
		return locate(agentId);
	}

	static String generateSHA1Id(String... args) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update(Integer.toString(args.length).getBytes());
		for (String s : args) {
			md.update(s.getBytes());
		}
		return byteArrayToHexString(md.digest());
	}

	private static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

}
