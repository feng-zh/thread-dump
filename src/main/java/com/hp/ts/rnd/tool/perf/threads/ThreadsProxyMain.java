package com.hp.ts.rnd.tool.perf.threads;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;
import java.util.Map;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;

import com.hp.ts.rnd.tool.perf.threads.proxy.ThreadSamplerProxyFactory;

public class ThreadsProxyMain {

	public static void main(String[] args) throws Exception {
		ManagementFactory.getPlatformMBeanServer().registerMBean(
				new ThreadSamplerProxyFactory(),
				ThreadSamplerProxyFactory.FACTORY_OBJECTNAME);
		if (args.length > 0) {
			String portStr = args[0];
			try {
				int port = Integer.parseInt(portStr);
				setupJMXConnectorServer(port);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"need port number as argument", e);
			}
		} else {
			Thread.sleep(Long.MAX_VALUE);
		}
	}

	private static void setupJMXConnectorServer(int port) throws IOException {
		String theHost = InetAddress.getLocalHost().getHostName();
		LocateRegistry.createRegistry(port);
		String serviceURL = "service:jmx:rmi:///jndi/rmi://" + theHost + ":"
				+ port + "/threads";
		Map<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.rmi.registry.RegistryContextFactory");
		environment.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
		JMXConnectorServer connectorServer = JMXConnectorServerFactory
				.newJMXConnectorServer(new JMXServiceURL(serviceURL),
						environment, ManagementFactory.getPlatformMBeanServer());
		connectorServer.start();
		System.out.printf("==> Target JMX Service URL is %s%n", serviceURL);
	}

}
