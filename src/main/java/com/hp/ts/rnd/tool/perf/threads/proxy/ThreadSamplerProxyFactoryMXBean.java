package com.hp.ts.rnd.tool.perf.threads.proxy;

import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

public interface ThreadSamplerProxyFactoryMXBean {

	public ObjectName createJstackSampler(int pid)
			throws ThreadSamplingException;

	public ObjectName createJvmSampler() throws ThreadSamplingException;

	public ObjectName createWebLogicSampler(String hostport, String username,
			String password) throws ThreadSamplingException;

	public ObjectName createJmxSampler(String jmxUrl, String username,
			String password) throws ThreadSamplingException;

}
