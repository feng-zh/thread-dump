package com.hp.ts.rnd.tool.perf.threads.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingException;

public class JmxThreadSamplerFactory implements ThreadSamplerFactory {

	private ThreadMXBean threadMBean;
	private ThreadSampler sampler;
	private JMXConnector connector;

	public JmxThreadSamplerFactory() {
		// current JVM
		threadMBean = ManagementFactory.getThreadMXBean();
	}

	public JmxThreadSamplerFactory(JMXServiceURL jmxURL, String[] userInfo)
			throws IOException {
		// remote JVM
		Map<String, Object> env = new HashMap<String, Object>();
		if (userInfo != null) {
			env.put(JMXConnector.CREDENTIALS, userInfo);
		}
		connector = JMXConnectorFactory.connect(jmxURL, env);
		threadMBean = ManagementFactory.newPlatformMXBeanProxy(
				connector.getMBeanServerConnection(),
				ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			sampler = new JmxThreadSampler(threadMBean);
		}
		return sampler;
	}

	@Override
	public void close() throws ThreadSamplingException {
		sampler = null;
		threadMBean = null;
		if (connector != null) {
			try {
				connector.close();
			} catch (IOException ignored) {
			}
			connector = null;
		}
	}

}
