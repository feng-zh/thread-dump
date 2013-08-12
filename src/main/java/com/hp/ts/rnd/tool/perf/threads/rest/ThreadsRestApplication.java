package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.HashSet;
import java.util.Set;

import com.hp.ts.rnd.tool.perf.web.WebResourceApplication;

public class ThreadsRestApplication implements WebResourceApplication {

	private Set<Object> instances = new HashSet<Object>();

	public ThreadsRestApplication() {
		instances.add(new JpsThreadSamplingController());
	}

	public Set<Object> getSingletons() {
		return instances;
	}

	public String getContextPath() {
		return "/threads";
	}

}
