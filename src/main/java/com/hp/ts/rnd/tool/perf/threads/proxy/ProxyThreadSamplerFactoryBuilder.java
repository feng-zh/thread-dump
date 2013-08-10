package com.hp.ts.rnd.tool.perf.threads.proxy;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

public class ProxyThreadSamplerFactoryBuilder {

	private ThreadSamplerProxyFactoryMXBean proxyFactoryMBean;

	private MBeanServerConnection mbsc;

	private boolean useExecutor = true;

	private int samplingPeriod = 100;

	private boolean compressMode = true;

	private int notifyPeriodMultiple = 10;

	private boolean samplingBlock = true;

	private boolean stopSamplingOnFinish = true;

	private boolean reuseSampling = true;

	public ProxyThreadSamplerFactoryBuilder(MBeanServerConnection mbsc,
			ObjectName factoryObjectName) {
		this.mbsc = mbsc;
		proxyFactoryMBean = JMX.newMXBeanProxy(mbsc, factoryObjectName,
				ThreadSamplerProxyFactoryMXBean.class, true);
	}

	public ProxyThreadSamplerFactoryBuilder(MBeanServerConnection mbsc) {
		this(mbsc, ThreadSamplerProxyFactory.FACTORY_OBJECTNAME);
	}

	// default not use
	public void setUseExecutor(boolean useExecutor) {
		this.useExecutor = useExecutor;
	}

	public boolean isUseExecutor() {
		return useExecutor;
	}

	public void setSamplingPeriod(int period) {
		this.samplingPeriod = period;
	}

	public int getSamplingPeriod() {
		return samplingPeriod;
	}

	public void setNotifyPeriodMultiple(int multiple) {
		this.notifyPeriodMultiple = multiple;
	}

	public int getNotifyPeriodMultiple() {
		return notifyPeriodMultiple;
	}

	public void setCompressMode(boolean mode) {
		this.compressMode = mode;
	}

	public boolean isCompressMode() {
		return compressMode;
	}

	public boolean isSamplingBlock() {
		return samplingBlock;
	}

	public void setSamplingBlock(boolean samplingBlock) {
		this.samplingBlock = samplingBlock;
	}

	public boolean isStopSamplingOnFinish() {
		return stopSamplingOnFinish;
	}

	public void setStopSamplingOnFinish(boolean stopSamplingOnFinish) {
		this.stopSamplingOnFinish = stopSamplingOnFinish;
	}

	public boolean isReuseSampling() {
		return reuseSampling;
	}

	public void setReuseSampling(boolean reuseSampling) {
		this.reuseSampling = reuseSampling;
	}

	public ProxyThreadSamplerFactory createJvmSamplerProxy() {
		try {
			return createSamplerFactory(proxyFactoryMBean.createJvmSampler());
		} catch (InstantiationException e) {
			throw toSamplingException(e);
		}
	}

	public ProxyThreadSamplerFactory getJvmSamplerProxy(boolean createIfNotFound) {
		ObjectName objectName = proxyFactoryMBean.findJvmSampler();
		if (objectName == null) {
			if (createIfNotFound) {
				try {
					objectName = proxyFactoryMBean.createJvmSampler();
				} catch (InstantiationException e) {
					throw toSamplingException(e);
				}
			} else {
				return null;
			}
		}
		return createSamplerFactory(objectName);
	}

	public ProxyThreadSamplerFactory createJmxSamplerProxy(String jmxUrl,
			String username, String password) {
		try {
			return createSamplerFactory(proxyFactoryMBean.createJmxSampler(
					jmxUrl, username, password));
		} catch (InstantiationException e) {
			throw toSamplingException(e);
		}
	}

	public ProxyThreadSamplerFactory createJstackSamplerProxy(int pid) {
		try {
			return createSamplerFactory(proxyFactoryMBean
					.createJstackSampler(pid));
		} catch (InstantiationException e) {
			throw toSamplingException(e);
		}
	}

	public ProxyThreadSamplerFactory createWebLogicSamplerProxy(
			String hostport, String username, String password) {
		try {
			return createSamplerFactory(proxyFactoryMBean
					.createWebLogicSampler(hostport, username, password));
		} catch (InstantiationException e) {
			throw toSamplingException(e);
		}
	}

	private ThreadSamplingException toSamplingException(InstantiationException e) {
		ThreadSamplingException exception = new ThreadSamplingException(
				e.getMessage());
		ThreadSamplingException cause = new ThreadSamplingException(
				e.getMessage(), e.getCause());
		cause.setStackTrace(e.getStackTrace());
		exception.initCause(cause);
		return exception;
	}

	protected ProxyThreadSamplerFactory createSamplerFactory(
			ObjectName proxyMBeanObjectName) {
		ThreadSamplerProxyMXBean proxyMBean = JMX.newMXBeanProxy(mbsc,
				proxyMBeanObjectName, ThreadSamplerProxyMXBean.class, true);
		return new ProxyThreadSamplerFactory(proxyMBean, this);
	}

	ThreadSamplerProxyFactoryMXBean getProxyFactoryMBean() {
		return this.proxyFactoryMBean;
	}
}
