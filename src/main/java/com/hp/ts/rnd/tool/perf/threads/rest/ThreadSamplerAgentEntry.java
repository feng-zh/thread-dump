package com.hp.ts.rnd.tool.perf.threads.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md.update(samplerType.getBytes());
		for (Entry<String, String> entry : samplerInfo.entrySet()) {
			md.update(entry.getKey().getBytes());
			md.update(entry.getValue().getBytes());
		}
		this.agentId = byteArrayToHexString(md.digest());
	}

	static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
