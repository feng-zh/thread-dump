package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.HashSet;
import java.util.Set;

import com.hp.ts.rnd.tool.perf.web.WebResourceApplication;

public class ThreadsRestApplication implements WebResourceApplication {

	private Set<Object> instances = new HashSet<Object>();

	public ThreadsRestApplication() {
		ThreadSamplerAgentController agentController = new ThreadSamplerAgentController();
		instances.add(agentController);
		instances.add(new JpsThreadSamplingController(agentController));
	}

	public Set<Object> getSingletons() {
		return instances;
	}

	public String getContextPath() {
		return "/threads";
	}

}
