package com.hp.ts.rnd.tool.perf.threads.proxy;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public interface ThreadSamplerProxyMXBean {

	public void startSampling();

	public void stopSampling();

	public boolean isSampling();

	// default not use
	public void setUseExecutor(boolean useExecutor);

	public boolean isUseExecutor();

	// default 100 ms
	public void setSamplingPeriod(int period);

	public int getSamplingPeriod();

	public String getSamplerType();

	public String getSamplerInfo();

	public void closeSampler();

	public ThreadSamplingState sampling();

}
