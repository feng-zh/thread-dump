package com.hp.ts.rnd.tool.perf.threads.rest;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.web.annotation.RestEntity;
import com.hp.ts.rnd.tool.perf.web.annotation.RestMethod;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPath;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPathParameter;

class RemoteJvmThreadSamplingController {

	private ThreadSamplerAgentController agentController;

	private Map<String, RemoteJvmEntry> remoteList = new HashMap<String, RemoteJvmEntry>();

	public RemoteJvmThreadSamplingController(
			ThreadSamplerAgentController agentController) {
		this.agentController = agentController;
	}

	@RestMethod("PUT")
	@RestPath("remote/url")
	public void sync(@RestEntity List<RemoteJvmEntry> entity) throws Exception {
		for (RemoteJvmEntry entry : entity) {
			RemoteJvmEntry oldEntry = remoteList.get(entry.getAgentId());
			if (oldEntry == null) {
				remoteList.put(entry.getAgentId(), entry);
			} else if (entry.isConfirmed() && !oldEntry.isConfirmed()) {
				remoteList.put(entry.getAgentId(), entry);
			}
		}
	}

	@RestMethod("POST")
	@RestPath("remote/url")
	public RemoteJvmEntry createRemoteJvmEntry(@RestEntity RemoteJvmEntry entry)
			throws Exception {
		if (!remoteList.containsKey(entry.getAgentId())) {
			remoteList.put(entry.getAgentId(), entry);
		}
		return remoteList.get(entry.getAgentId());
	}

	@RestMethod("DELETE")
	@RestPath("remote/url/{id}")
	public void deleteRemoteJvmEntry(@RestPathParameter("id") String agentId)
			throws Exception {
		remoteList.remove(agentId);
	}

	@RestPath("remote/url")
	public Collection<RemoteJvmEntry> list() throws Exception {
		return remoteList.values();
	}

	@RestPath("remote/url/{id}")
	public RemoteJvmEntry getRemoteDetail(
			@RestPathParameter("id") String agentId) throws Exception {
		RemoteJvmEntry entry = remoteList.get(agentId);
		if (entry == null) {
			throw new FileNotFoundException("remote agent not found: "
					+ agentId);
		}
		return entry;
	}

	@RestMethod("POST")
	@RestPath("remote/url/{id}/sampler")
	public ThreadSamplerAgentEntry connectAgent(
			@RestPathParameter("id") String id) throws Exception {
		RemoteJvmEntry entry = getRemoteDetail(id);
		ThreadSamplerAgentEntry agentInfo = new ThreadSamplerAgentEntry();
		agentInfo.setSamplerType(entry.getRemoteType());
		Map<String, String> info = new LinkedHashMap<String, String>();
		info.put("url", entry.getUrl());
		info.put("user", entry.getUser());
		info.put("password", entry.getPassword());
		agentInfo.setSamplerInfo(info);
		agentInfo.generateAgentId();
		String agentId = agentInfo.getAgentId();
		if (!agentController.agentExist(agentId)) {
			ThreadSamplerFactory factory;
			if ("WebLogic".equals(entry.getRemoteType())) {
				factory = ThreadSamplings.createWLSJmxThreadSamplerFactory(
						entry.getUrl(), entry.getUser(), entry.getPassword());
			} else {
				factory = ThreadSamplings
						.createJmxThreadSamplerFactory(
								new JMXServiceURL(
										"service:jmx:rmi:///jndi/rmi://"
												+ entry.getUrl()),
								(entry.getUser() == null || entry.getUser()
										.length() == 0) ? new String[0]
										: new String[] { entry.getUser(),
												entry.getPassword() });
			}
			try {
				factory.getSampler();
				entry.setConfirmed(true);
			} catch (Exception e) {
				factory.close();
				throw e;
			}
			agentController.createSamplerAgent(factory, agentInfo);
		}
		return agentController.getAgent(agentId).getAgentEntry();
	}
}
