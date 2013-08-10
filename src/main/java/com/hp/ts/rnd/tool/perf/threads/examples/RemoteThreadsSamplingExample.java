package com.hp.ts.rnd.tool.perf.threads.examples;

import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.calltree.CallTreeAnalyzer;
import com.hp.ts.rnd.tool.perf.threads.proxy.ProxyThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.proxy.ProxyThreadSamplerFactoryBuilder;

public class RemoteThreadsSamplingExample {

	public static void main(String[] args) throws Exception {
		ProxyThreadSamplerFactoryBuilder factoryBuilder = new ProxyThreadSamplerFactoryBuilder(
				JMXConnectorFactory
						.connect(
								new JMXServiceURL(
										"service:jmx:rmi:///jndi/rmi://127.0.0.1:10103/threads"))
						.getMBeanServerConnection());
		ProxyThreadSamplerFactory service = factoryBuilder
				.getJvmSamplerProxy(true);
		final CallTreeAnalyzer callTree = new CallTreeAnalyzer();
		try {
			service.executeSampling(new ThreadSamplingHandler() {

				@Override
				public void onSampling(ThreadSamplingState state) {
					callTree.addThreadSampling(state);
					// samplingWriter.writeThreadSampling(state);
					// System.out.println(TimeUnit.NANOSECONDS.toMillis(state
					// .getDurationTimeNanos()));
					System.out.print(".");
				}

				@Override
				public void onError(ThreadSamplingException exception)
						throws ThreadSamplingException {
					throw exception;
				}

				@Override
				public void onEnd() {
					System.out.println();
					// callTree.print(System.out);
				}
			});
		} finally {
			// dataOutput.close();
			service.close();
		}
	}

}
