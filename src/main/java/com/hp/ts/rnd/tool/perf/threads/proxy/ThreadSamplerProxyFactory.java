package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.jmx.JmxThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jstack.JstackThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.weblogic.WLSJmxThreadSamplerFactory;

public class ThreadSamplerProxyFactory implements
		ThreadSamplerProxyFactoryMXBean, MBeanRegistration {

	private MBeanServer mbeanServer;
	private ObjectName objectName;
	private ScheduledExecutorService executor;

	@Override
	public ObjectName createJstackSampler(int pid)
			throws ThreadSamplingException {
		JstackThreadSamplerFactory jstackThreadSamplerFactory = new JstackThreadSamplerFactory(
				pid);
		try {
			jstackThreadSamplerFactory.checkAccess();
		} catch (IOException e) {
			throw new ThreadSamplingException("cannot access jvm process: "
					+ pid, e);
		}
		ThreadSamplerProxy mbean = new ThreadSamplerProxy(
				jstackThreadSamplerFactory, this);
		return registerMBean(mbean, "jstack", "pid=" + pid);
	}

	@Override
	public ObjectName createJvmSampler() throws ThreadSamplingException {
		ThreadSamplerProxy mbean = new ThreadSamplerProxy(
				new JvmThreadSamplerFactory(), this);
		return registerMBean(mbean, "jvm", "");
	}

	@Override
	public ObjectName createWebLogicSampler(String hostport, String username,
			String password) throws ThreadSamplingException {
		WLSJmxThreadSamplerFactory wlsJmxThreadSamplerFactory = new WLSJmxThreadSamplerFactory(
				hostport, username, password);
		wlsJmxThreadSamplerFactory.checkAccess();
		ThreadSamplerProxy mbean = new ThreadSamplerProxy(
				wlsJmxThreadSamplerFactory, this);
		return registerMBean(mbean, "weblogic_jmx", "hostport=" + hostport
				+ ", username=" + username);
	}

	@Override
	public ObjectName createJmxSampler(String jmxUrl, String username,
			String password) throws ThreadSamplingException {
		JmxThreadSamplerFactory jmxThreadSamplerFactory;
		try {
			jmxThreadSamplerFactory = new JmxThreadSamplerFactory(
					new JMXServiceURL(jmxUrl), username == null
							|| username.length() == 0 ? null : new String[] {
							username, password });
		} catch (Exception e) {
			throw new ThreadSamplingException(e);
		}
		ThreadSamplerProxy mbean = new ThreadSamplerProxy(
				jmxThreadSamplerFactory, this);
		return registerMBean(mbean, "jmx", "jmxUrl="
				+ jmxUrl
				+ ((username == null || username.length() == 0) ? ""
						: (", username=" + username)));
	}

	private ObjectName registerMBean(ThreadSamplerProxy mbean, String type,
			String info) {
		mbean.setSamplerType(type);
		mbean.setSamplerInfo(info);
		try {
			ObjectName name = new ObjectName(objectName.getDomain() + ":type="
					+ ThreadSamplerProxy.class.getName() + ", proxyType="
					+ type + (info.length() > 0 ? (", " + info) : ""));
			mbeanServer.registerMBean(mbean, name);
			mbean.setObjectName(name);
			return name;
		} catch (Exception e) {
			mbean.closeSampler();
			throw new ThreadSamplingException("register mbean error", e);
		}
	}

	@Override
	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		this.mbeanServer = server;
		this.objectName = name;
		return name;
	}

	@Override
	public void postRegister(Boolean registrationDone) {
		if (Boolean.TRUE.equals(registrationDone)) {
			executor = Executors.newScheduledThreadPool(2);
		}
	}

	@Override
	public void preDeregister() throws Exception {
		executor.shutdownNow();
	}

	@Override
	public void postDeregister() {
		this.mbeanServer = null;
		this.objectName = null;
	}

	ScheduledExecutorService getExecutor() {
		return executor;
	}

	void unregisterMBean(ObjectName proxyName, ThreadSamplerProxy proxy) {
		try {
			mbeanServer.unregisterMBean(proxyName);
		} catch (MBeanRegistrationException ignored) {
			// TODO log
		} catch (InstanceNotFoundException ignored) {
			// TODO log
		}
	}

}
