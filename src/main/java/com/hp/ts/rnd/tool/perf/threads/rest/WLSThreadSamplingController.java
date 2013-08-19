package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.web.annotation.RestEntity;
import com.hp.ts.rnd.tool.perf.web.annotation.RestMethod;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPath;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPathParameter;

class WLSThreadSamplingController {

	private ThreadSamplerAgentController agentController;

	private Map<String, WLSEntry> wlsList = new HashMap<String, WLSEntry>();

	public WLSThreadSamplingController(
			ThreadSamplerAgentController agentController) {
		this.agentController = agentController;
	}

	@RestMethod("PUT")
	@RestPath("wls/url")
	public void sync(@RestEntity List<WLSEntry> entity) throws Exception {
		for (WLSEntry entry : entity) {
			WLSEntry oldEntry = wlsList.get(entry.getWlsUrl());
			if (oldEntry == null) {
				wlsList.put(entry.getWlsUrl(), entry);
			} else if (entry.getAgentId() != null
					&& oldEntry.getAgentId() == null) {
				wlsList.put(entry.getWlsUrl(), entry);
			}
		}
	}

	@RestPath("wls/url")
	public Collection<WLSEntry> list() throws Exception {
		return wlsList.values();
	}

	@RestPath("wls/url/{id}")
	public WLSEntry wlsDetail(@RestPathParameter("id") String agentId)
			throws Exception {

	}

	@RestMethod("POST")
	@RestPath("wls/url/{id}/sampler")
	public ThreadSamplerAgentEntry connectAgent(
			@RestPathParameter("id") String agentId) throws Exception {
		ThreadSamplerAgentEntry agentInfo = new ThreadSamplerAgentEntry();
		agentInfo.setSamplerType("wls");
		Map<String, String> info = new LinkedHashMap<String, String>();
		info.put("pid", String.valueOf(pid));
		agentInfo.setSamplerInfo(info);
		agentInfo.generateAgentId();
		String agentId = agentInfo.getAgentId();
		if (!agentController.agentExist(agentId)) {
			ThreadSamplerFactory factory;
			if (pid == 0) {
				factory = ThreadSamplings.createJvmThreadSamplerFactory();
			} else {
				JpsEntry jpsDetail = jpsDetail(pid);
				factory = ThreadSamplings
						.createJstackThreadSamplerFactory(jpsDetail.getPid());
			}
			agentController.createSamplerAgent(factory, agentInfo);
		}
		return agentController.getAgent(agentId).getAgentEntry();
	}

}
