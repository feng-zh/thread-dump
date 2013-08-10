package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.jmx.JmxThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jstack.JstackThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.weblogic.WLSJmxThreadSamplerFactory;

public class ThreadSamplerProxyFactory implements
		ThreadSamplerProxyFactoryMXBean, NotificationEmitter, MBeanRegistration {

	public static ObjectName FACTORY_OBJECTNAME;
	public static String PROXY_CREATE = "PROXY_CREATE";
	public static String PROXY_REMOVE = "PROXY_REMOVE";

	private MBeanServer mbeanServer;
	private ObjectName objectName;
	private ScheduledExecutorService executor;
	private NotificationBroadcasterSupport notificationBroadcaster;
	private AtomicLong seq = new AtomicLong();

	static {
		try {
			FACTORY_OBJECTNAME = ObjectName.getInstance(
					ThreadSamplerProxyFactory.class.getPackage().getName(),
					"type", ThreadSamplerProxyFactory.class.getSimpleName());
		} catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public ThreadSamplerProxyFactory() {
		notificationBroadcaster = new NotificationBroadcasterSupport(
				new MBeanNotificationInfo[] { new MBeanNotificationInfo(
						new String[] { PROXY_CREATE, PROXY_REMOVE },
						ObjectName.class.getName(), "Proxy Create/Remove") });
	}

	@Override
	public ObjectName createJstackSampler(int pid)
			throws InstantiationException {
		try {
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
		} catch (ThreadSamplingException e) {
			throw toInstantiationException(e);
		}
	}

	private InstantiationException toInstantiationException(
			ThreadSamplingException e) {
		InstantiationException exception = new InstantiationException(
				e.getMessage());
		exception.setStackTrace(e.getStackTrace());
		if (e.getCause() != null
				&& e.getCause().getClass().getPackage().getName()
						.startsWith("java")) {
			exception.initCause(e.getCause());
		}
		return exception;
	}

	@Override
	public ObjectName createJvmSampler() throws InstantiationException {
		try {
			ThreadSamplerProxy mbean = new ThreadSamplerProxy(
					new JvmThreadSamplerFactory(), this);
			return registerMBean(mbean, "jvm", "");
		} catch (ThreadSamplingException e) {
			throw toInstantiationException(e);
		}
	}

	@Override
	public ObjectName createWebLogicSampler(String hostport, String username,
			String password) throws InstantiationException {
		try {
			WLSJmxThreadSamplerFactory wlsJmxThreadSamplerFactory = new WLSJmxThreadSamplerFactory(
					hostport, username, password);
			wlsJmxThreadSamplerFactory.checkAccess();
			ThreadSamplerProxy mbean = new ThreadSamplerProxy(
					wlsJmxThreadSamplerFactory, this);
			return registerMBean(mbean, "weblogic_jmx", "hostport=" + hostport
					+ ", username=" + username);
		} catch (ThreadSamplingException e) {
			throw toInstantiationException(e);
		}
	}

	@Override
	public ObjectName createJmxSampler(String jmxUrl, String username,
			String password) throws InstantiationException {
		try {
			JmxThreadSamplerFactory jmxThreadSamplerFactory;
			try {
				jmxThreadSamplerFactory = new JmxThreadSamplerFactory(
						new JMXServiceURL(jmxUrl), username == null
								|| username.length() == 0 ? null
								: new String[] { username, password });
			} catch (Exception e) {
				throw new ThreadSamplingException(e);
			}
			ThreadSamplerProxy mbean = new ThreadSamplerProxy(
					jmxThreadSamplerFactory, this);
			return registerMBean(mbean, "jmx", "jmxUrl="
					+ jmxUrl
					+ ((username == null || username.length() == 0) ? ""
							: (", username=" + username)));
		} catch (ThreadSamplingException e) {
			throw toInstantiationException(e);
		}
	}

	private ObjectName registerMBean(ThreadSamplerProxy mbean, String type,
			String info) {
		mbean.setSamplerType(type);
		mbean.setSamplerInfo(info);
		try {
			ObjectName name = new ObjectName(objectName.getDomain() + ":type="
					+ ThreadSamplerProxy.class.getSimpleName() + ", proxyType="
					+ type + (info.length() > 0 ? (", " + info) : ""));
			mbeanServer.registerMBean(mbean, name);
			mbean.setObjectName(name);
			return name;
		} catch (Exception e) {
			mbean.closeSampler();
			throw new ThreadSamplingException("register mbean error: "
					+ e.getMessage(), e);
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

	@Override
	public ObjectName findJvmSampler() {
		return findMBean("jvm", "");
	}

	private ObjectName findMBean(String type, String info) {
		ObjectName name;
		try {
			name = new ObjectName(objectName.getDomain() + ":type="
					+ ThreadSamplerProxy.class.getSimpleName() + ", proxyType="
					+ type + (info.length() > 0 ? (", " + info) : ""));
		} catch (MalformedObjectNameException e) {
			return null;
		}
		if (mbeanServer.queryNames(name, null).size() == 1) {
			return name;
		} else {
			return null;
		}
	}

	void onProxyCreate(ObjectName proxyObjectName) {
		Notification notification = new Notification(PROXY_CREATE, this,
				seq.incrementAndGet());
		notification.setUserData(proxyObjectName);
		notificationBroadcaster.sendNotification(notification);
	}

	void onProxyRemoved(ObjectName proxyObjectName) {
		Notification notification = new Notification(PROXY_REMOVE, this,
				seq.incrementAndGet());
		notification.setUserData(proxyObjectName);
		notificationBroadcaster.sendNotification(notification);
	}

	@Override
	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		notificationBroadcaster.addNotificationListener(listener, filter, handback);		
	}

	@Override
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		notificationBroadcaster.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return notificationBroadcaster.getNotificationInfo();
	}

	@Override
	public void removeNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws ListenerNotFoundException {
		notificationBroadcaster.removeNotificationListener(listener, filter, handback);
	}

}
