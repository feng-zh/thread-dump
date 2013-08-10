package com.hp.ts.rnd.tool.perf.threads.proxy;

import java.io.EOFException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;

import com.hp.ts.rnd.tool.perf.threads.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.GeneralThreadStackTrace;
import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class ProxyThreadSamplerFactory implements ThreadSamplerFactory,
		ThreadSamplingService {

	private ThreadSamplerProxyMXBean proxyMBean;
	private ThreadSampler sampler;
	private AtomicReference<ThreadSamplingHandler> unEndHandler;
	private NotificationListener notificationListener;
	private ProxyThreadSamplerFactoryBuilder factoryBuilder;
	private boolean stopSamplingOnFinish = false;
	private NotificationListener checkClosedNotificationListener;
	private NotificationFilterSupport checkClosedNotificationFilter;

	ProxyThreadSamplerFactory(ThreadSamplerProxyMXBean proxyMBean,
			ProxyThreadSamplerFactoryBuilder factoryBuilder) {
		this.proxyMBean = proxyMBean;
		this.factoryBuilder = factoryBuilder;
		this.sampler = new ThreadSampler() {

			@Override
			public ThreadSamplingState sampling()
					throws ThreadSamplingException, EndOfSamplingException {
				ThreadSamplerProxyMXBean proxyMXBean = ProxyThreadSamplerFactory.this.proxyMBean;
				if (proxyMXBean == null) {
					throw new ThreadSamplingException("proxy mxbean is closed");
				}
				try {
					return toThreadSamplingState(proxyMXBean.sampling());
				} catch (RuntimeException e) {
					ThreadSamplingException error = new ThreadSamplingException(
							e.getMessage());
					error.setStackTrace(e.getStackTrace());
					throw error;
				} catch (EOFException e) {
					EndOfSamplingException error = new EndOfSamplingException(
							e.getMessage());
					error.setStackTrace(e.getStackTrace());
					throw error;
				}
			}

		};
		checkClosedNotificationFilter = new NotificationFilterSupport();
		checkClosedNotificationFilter
				.enableType(ThreadSamplerProxyFactory.PROXY_REMOVE);
		checkClosedNotificationListener = new NotificationListener() {

			@Override
			public void handleNotification(Notification notification,
					Object handback) {
				NotificationEmitter emitter = (NotificationEmitter) handback;
				try {
					emitter.removeNotificationListener(this,
							checkClosedNotificationFilter, handback);
				} catch (ListenerNotFoundException ignored) {
				}
				onSamplingClosed(ProxyThreadSamplerFactory.this.proxyMBean);
				ProxyThreadSamplerFactory.this.proxyMBean = null;
			}
		};
		((NotificationEmitter) factoryBuilder.getProxyFactoryMBean())
				.addNotificationListener(checkClosedNotificationListener,
						checkClosedNotificationFilter,
						factoryBuilder.getProxyFactoryMBean());
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		return sampler;
	}

	@Override
	public void close() {
		if (proxyMBean != null) {
			onSamplingClosed(proxyMBean);
			proxyMBean.closeSampler();
		}
	}

	static ThreadSamplingState toThreadSamplingState(
			ThreadSamplingStateProxy sampling) {
		ThreadSamplingState state = new ThreadSamplingState(
				sampling.getStartTimeMillis(), sampling.getDurationTimeNanos());
		state.setSamplingTime(sampling.getSamplingTime());
		GeneralThreadStackTrace[] generalTraces = sampling.getStackTraces();
		ThreadStackTrace[] traces = new ThreadStackTrace[generalTraces.length];
		for (int i = 0; i < generalTraces.length; i++) {
			traces[i] = generalTraces[i].toStackTrace();
		}
		state.setStackTraces(traces);
		return state;
	}

	@Override
	public void executeSampling(final ThreadSamplingHandler handler)
			throws ThreadSamplingException {
		if (proxyMBean == null) {
			throw new ThreadSamplingException("proxy mxbean is closed");
		}
		if (!proxyMBean.isSampling()) {
			proxyMBean.setCompressMode(factoryBuilder.isCompressMode());
			proxyMBean.setNotifyPeriodMultiple(factoryBuilder
					.getNotifyPeriodMultiple());
			proxyMBean.setSamplingPeriod(factoryBuilder.getSamplingPeriod());
			proxyMBean.setUseExecutor(factoryBuilder.isUseExecutor());
		} else if (!factoryBuilder.isReuseSampling()) {
			throw new ThreadSamplingException(
					"proxy is in sampling, but request is NOT reuse");
		}
		unEndHandler = new AtomicReference<ThreadSamplingHandler>(handler);
		notificationListener = new NotificationListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void handleNotification(Notification notification,
					Object handback) {
				String type = notification.getType();
				List<ThreadSamplingStateProxy> list = null;
				if (ThreadSamplerProxy.COMPRESSED_SAMPLING.equals(type)) {
					list = ThreadSamplerProxy.decomprss((byte[]) notification
							.getUserData());
				} else if (ThreadSamplerProxy.SAMPLING.equals(type)) {
					list = (List<ThreadSamplingStateProxy>) notification
							.getUserData();
				}
				if (list != null) {
					if (unEndHandler.compareAndSet(handler, null)) {
						if (list.size() == 0) {
							synchronized (unEndHandler) {
								try {
									handler.onEnd();
								} finally {
									unEndHandler.notifyAll();
								}
							}
						} else {
							unEndHandler.set(null);
							try {
								for (ThreadSamplingStateProxy state : list) {
									handler.onSampling(toThreadSamplingState(state));
								}
							} finally {
								unEndHandler.set(handler);
							}
						}
					}
				}
			}
		};
		((NotificationEmitter) proxyMBean).addNotificationListener(
				notificationListener, null, proxyMBean);
		if (!proxyMBean.isSampling()) {
			proxyMBean.startSampling();
		}
		stopSamplingOnFinish = factoryBuilder.isStopSamplingOnFinish();
		if (factoryBuilder.isSamplingBlock()) {
			synchronized (unEndHandler) {
				try {
					unEndHandler.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Override
	public void closeSampling() {
		try {
			if (proxyMBean != null && stopSamplingOnFinish) {
				proxyMBean.stopSampling();
			}
		} catch (Exception ignored) {
		}
		onSamplingClosed(proxyMBean);
	}

	private void onSamplingClosed(Object proxy) {
		if (notificationListener != null) {
			try {
				((NotificationEmitter) proxy).removeNotificationListener(
						notificationListener, null, proxy);
				notificationListener = null;
			} catch (ListenerNotFoundException ignored) {
			}
		}
		if (unEndHandler != null) {
			ThreadSamplingHandler handler = unEndHandler.getAndSet(null);
			try {
				if (handler != null) {
					handler.onEnd();
				}
			} finally {
				synchronized (unEndHandler) {
					unEndHandler.notifyAll();
				}
			}
		}
	}
}
