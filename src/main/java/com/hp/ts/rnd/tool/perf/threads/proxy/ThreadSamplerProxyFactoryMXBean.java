package com.hp.ts.rnd.tool.perf.threads.proxy;

import javax.management.ObjectName;

public interface ThreadSamplerProxyFactoryMXBean {

	public ObjectName createJstackSampler(int pid)
			throws InstantiationException;

	public ObjectName createJvmSampler() throws InstantiationException;
	
	public ObjectName findJvmSampler();

	public ObjectName createWebLogicSampler(String hostport, String username,
			String password) throws InstantiationException;

	public ObjectName createJmxSampler(String jmxUrl, String username,
			String password) throws InstantiationException;

}
