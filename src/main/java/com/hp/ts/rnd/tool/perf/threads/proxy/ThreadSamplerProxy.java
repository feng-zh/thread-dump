package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;

public class ThreadSamplerProxy implements ThreadSamplerProxyMXBean,
		NotificationEmitter {

	private ThreadSamplerFactory factory;
	private String samplerType;
	private String samplerInfo;
	ThreadSamplerProxyFactory proxyFactory;
	private NotificationBroadcasterSupport notificationSupport;
	private volatile boolean useExecutor;
	private int samplingPeriod = 100;
	private ScheduledFuture<?> scheduledFuture;
	private AtomicLong seq = new AtomicLong();
	private ObjectName objectName;

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
				});
	}

	@Override
	public void startSampling() {
		if (scheduledFuture != null) {
			throw new IllegalStateException("sampling is started");
		}
		scheduledFuture = proxyFactory.getExecutor().scheduleAtFixedRate(
				new Runnable() {

					@Override
					public void run() {
						ThreadSamplingState samplingState = sampling();
						Notification notification = new Notification(
								"Sampling", this, seq.incrementAndGet());
						notification.setUserData(samplingState);
						notificationSupport.sendNotification(notification);
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
	public ThreadSamplingState sampling() {
		return factory.getSampler().sampling();
	}

	void setSamplerType(String type) {
		this.samplerType = type;
	}

	void setSamplerInfo(String info) {
		this.samplerInfo = info;
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

}
