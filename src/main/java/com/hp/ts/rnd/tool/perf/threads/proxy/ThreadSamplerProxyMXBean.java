package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.io.EOFException;

public interface ThreadSamplerProxyMXBean {

	public void startSampling();

	public void stopSampling();

	public boolean isSampling();

	// default not use
	public void setUseExecutor(boolean useExecutor);

	public boolean isUseExecutor();

	public long getSamplingCount();

	// default 100 ms
	public void setSamplingPeriod(int period);

	public int getSamplingPeriod();

	public void setNotifyPeriodMultiple(int multiple);

	public int getNotifyPeriodMultiple();

	public String getSamplerType();

	public String getSamplerInfo();

	public void closeSampler();

	public void setCompressMode(boolean mode);

	public boolean isCompressMode();

	public ThreadSamplingStateProxy sampling() throws EOFException;

}
