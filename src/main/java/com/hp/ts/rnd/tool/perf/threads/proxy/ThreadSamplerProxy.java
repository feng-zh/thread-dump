package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

public class ThreadSamplerProxy implements ThreadSamplerProxyMXBean,
		NotificationEmitter, MBeanRegistration {

	public static final String SAMPLING = "SAMPLING";
	public static final String COMPRESSED_SAMPLING = "COMPRESSED_SAMPLING";

	private final ThreadSamplerFactory factory;
	private String samplerType;
	private String samplerInfo;
	private volatile boolean useExecutor;
	private int samplingPeriod = 100;
	private ScheduledFuture<?> scheduledFuture;
	private ObjectName objectName;
	private NotificationBroadcasterSupport notificationSupport;
	private ThreadSamplerProxyFactory proxyFactory;
	private AtomicLong seq = new AtomicLong();
	private AtomicLong samplingCount = new AtomicLong();
	private boolean compressMode;
	private int notifyPeriodMultiple;

	public static interface NotificationInfoSerializerInterface {
		public List<ThreadSamplingStateProxy> getSampling();

		public void setSampling(List<ThreadSamplingStateProxy> info);
	}

	public static class NotificationInfoSerializer extends StandardMBean
			implements NotificationInfoSerializerInterface {

		private List<ThreadSamplingStateProxy> info;

		private static NotificationInfoSerializer me = new NotificationInfoSerializer();

		public NotificationInfoSerializer() {
			super(NotificationInfoSerializerInterface.class, true);
		}

		public List<ThreadSamplingStateProxy> getSampling() {
			return info;
		}

		public void setSampling(List<ThreadSamplingStateProxy> info) {
			this.info = info;
		}

		public static Object serializeSampling(
				List<ThreadSamplingStateProxy> info) {
			try {
				me.setSampling(info);
				return me.getAttribute("Sampling");
			} catch (Exception ex) {
				throw new RuntimeException("Unexpected exception", ex);
			}
		}
	}

	ThreadSamplerProxy(ThreadSamplerFactory factory,
			ThreadSamplerProxyFactory proxyFactory) {
		this.factory = factory;
		this.proxyFactory = proxyFactory;
		this.notificationSupport = new NotificationBroadcasterSupport(
				new Executor() {

					@Override
					public void execute(Runnable command) {
						if (useExecutor) {
							ThreadSamplerProxy.this.proxyFactory.getExecutor()
									.execute(command);
						} else {
							command.run();
						}
					}
				}, new MBeanNotificationInfo[] {
						new MBeanNotificationInfo(new String[] { SAMPLING },
								ThreadSamplerProxy.class.getName(),
								"Thread Sampling"),
						new MBeanNotificationInfo(
								new String[] { COMPRESSED_SAMPLING },
								byte[].class.getName(),
								"Compressed Thread Sampling") });
	}

	@Override
	public void startSampling() {
		if (scheduledFuture != null) {
			throw new IllegalStateException("sampling is started");
		}
		scheduledFuture = proxyFactory.getExecutor().scheduleAtFixedRate(
				new Runnable() {

					List<ThreadSamplingStateProxy> store = new ArrayList<ThreadSamplingStateProxy>();

					@Override
					public void run() {
						try {
							try {
								ThreadSamplingStateProxy samplingState = samplingInternal();
								store.add(samplingState);
								sendData(notifyPeriodMultiple);
							} catch (EndOfSamplingException e) {
								if (!store.isEmpty()) {
									sendData(0);
								}
								// send EOF
								sendData(0);
							}
						} catch (Throwable th) {
							// TODO
							th.printStackTrace();
						}
					}

					private void sendData(int sendLevel) {
						if (store.size() >= sendLevel) {
							List<ThreadSamplingStateProxy> info = new ArrayList<ThreadSamplingStateProxy>(
									store);
							store.clear();
							if (compressMode) {
								Notification notification = new Notification(
										COMPRESSED_SAMPLING,
										ThreadSamplerProxy.this,
										ThreadSamplerProxy.this.seq
												.incrementAndGet());
								notification.setUserData(compress(info));
								notificationSupport
										.sendNotification(notification);
							} else {
								Notification notification = new Notification(
										SAMPLING, ThreadSamplerProxy.this,
										ThreadSamplerProxy.this.seq
												.incrementAndGet());
								notification
										.setUserData(NotificationInfoSerializer
												.serializeSampling(info));
								notificationSupport
										.sendNotification(notification);
							}
						}
					}
				}, samplingPeriod, samplingPeriod, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stopSampling() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
			scheduledFuture = null;
		}
	}

	@Override
	public boolean isSampling() {
		return scheduledFuture != null;
	}

	@Override
	public long getSamplingCount() {
		return samplingCount.get();
	}

	@Override
	public void setUseExecutor(boolean useExecutor) {
		if (isSampling()) {
			throw new IllegalStateException("cannot change during sampling");
		}
		this.useExecutor = useExecutor;
	}

	@Override
	public boolean isUseExecutor() {
		return useExecutor;
	}

	@Override
	public void setSamplingPeriod(int period) {
		if (isSampling()) {
			throw new IllegalStateException("cannot change during sampling");
		}
		if (period <= 0) {
			throw new IllegalArgumentException("invalid period: " + period);
		}
		this.samplingPeriod = period;
	}

	@Override
	public int getSamplingPeriod() {
		return samplingPeriod;
	}

	@Override
	public String getSamplerType() {
		return samplerType;
	}

	@Override
	public String getSamplerInfo() {
		return samplerInfo;
	}

	void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

	@Override
	public void closeSampler() {
		stopSampling();
		if (objectName != null) {
			proxyFactory.unregisterMBean(objectName, this);
		}
	}

	@Override
	public ThreadSamplingStateProxy sampling() throws EOFException {
		try {
			return samplingInternal();
		} catch (EndOfSamplingException e) {
			EOFException exception = new EOFException(e.getMessage());
			exception.setStackTrace(e.getStackTrace());
			throw exception;
		} catch (ThreadSamplingException e) {
			RuntimeException exception = new RuntimeException(e.getMessage());
			exception.setStackTrace(e.getStackTrace());
			throw exception;
		}
	}

	ThreadSamplingStateProxy samplingInternal() {
		ThreadSamplingStateProxy ret = new ThreadSamplingStateProxy(factory
				.getSampler().sampling());
		samplingCount.incrementAndGet();
		return ret;
	}

	void setSamplerType(String type) {
		this.samplerType = type;
	}

	void setSamplerInfo(String info) {
		this.samplerInfo = info;
	}

	public void setNotifyPeriodMultiple(int multiple) {
		this.notifyPeriodMultiple = multiple;
	}

	public int getNotifyPeriodMultiple() {
		return notifyPeriodMultiple;
	}

	public void setCompressMode(boolean mode) {
		compressMode = mode;
	}

	public boolean isCompressMode() {
		return compressMode;
	}

	static byte[] compress(List<ThreadSamplingStateProxy> data) {
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(new DeflaterOutputStream(baOut)));
			out.writeObject(data);
			out.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return baOut.toByteArray();
	}

	@SuppressWarnings("unchecked")
	static List<ThreadSamplingStateProxy> decomprss(byte[] bytes) {
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new BufferedInputStream(
					new InflaterInputStream(new ByteArrayInputStream(bytes))));
			return (List<ThreadSamplingStateProxy>) input.readObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	@Override
	public void addNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws IllegalArgumentException {
		notificationSupport.addNotificationListener(listener, filter, handback);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		notificationSupport.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return notificationSupport.getNotificationInfo();
	}

	@Override
	public void removeNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws ListenerNotFoundException {
		notificationSupport.removeNotificationListener(listener, filter,
				handback);
	}

	@Override
	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		return name;
	}

	@Override
	public void postRegister(Boolean registrationDone) {
		if (Boolean.TRUE.equals(registrationDone)) {
			proxyFactory.onProxyRemoved(objectName);
		}
	}

	@Override
	public void preDeregister() throws Exception {
		proxyFactory.onProxyRemoved(objectName);
	}

	@Override
	public void postDeregister() {
	}

}
