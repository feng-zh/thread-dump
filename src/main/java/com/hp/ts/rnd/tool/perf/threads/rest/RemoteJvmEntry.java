package com.hp.ts.rnd.tool.perf.threads.rest;

class RemoteJvmEntry {

	private String remoteType;

	private String url;

	private String user;

	private String password;

	private String name;

	private boolean confirmed;

	private String agentId;

	public String getRemoteType() {
		return remoteType;
	}

	public void setRemoteType(String remoteType) {
		this.remoteType = remoteType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getAgentId() {
		if (agentId == null) {
			agentId = ThreadSamplerAgentController.generateSHA1Id(remoteType,
					remoteType);
		}
		return agentId;
	}

}
