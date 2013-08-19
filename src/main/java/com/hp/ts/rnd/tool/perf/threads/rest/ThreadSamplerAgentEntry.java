package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class ThreadSamplerAgentEntry {

	private String agentId;

	private String samplerType;

	private Map<String, String> samplerInfo;

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getSamplerType() {
		return samplerType;
	}

	public void setSamplerType(String samplerType) {
		this.samplerType = samplerType;
	}

	public Map<String, String> getSamplerInfo() {
		return samplerInfo;
	}

	public void setSamplerInfo(Map<String, String> samplerInfo) {
		this.samplerInfo = samplerInfo;
	}

	void generateAgentId() {
		List<String> list = new ArrayList<String>();
		list.add(samplerType);
		for (Entry<String, String> entry : samplerInfo.entrySet()) {
			list.add(entry.getKey());
			list.add(entry.getValue());
		}
		this.agentId = ThreadSamplerAgentController.generateSHA1Id(list
				.toArray(new String[list.size()]));
	}

}
