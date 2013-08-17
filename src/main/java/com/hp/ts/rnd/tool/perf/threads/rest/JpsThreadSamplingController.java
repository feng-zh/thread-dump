package com.hp.ts.rnd.tool.perf.threads.rest;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;
import com.hp.ts.rnd.tool.perf.threads.util.Utils;
import com.hp.ts.rnd.tool.perf.web.annotation.RestMethod;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPath;
import com.hp.ts.rnd.tool.perf.web.annotation.RestPathParameter;

class JpsThreadSamplingController {

	private ThreadSamplerAgentController agentController;

	public JpsThreadSamplingController(
			ThreadSamplerAgentController agentController) {
		this.agentController = agentController;
	}

	@RestPath("jps/pid")
	public List<JpsEntry> jps() throws Exception {
		Map<Integer, String> map = Utils.jps();
		int currentPid = Utils.getCurrentPid();
		List<JpsEntry> list = new ArrayList<JpsEntry>();
		for (Entry<Integer, String> entry : map.entrySet()) {
			JpsEntry jpsEntry = new JpsEntry();
			jpsEntry.setPid(entry.getKey());
			jpsEntry.setMainClass(entry.getValue());
			jpsEntry.setSelf(currentPid == entry.getKey());
			list.add(jpsEntry);
		}
		return list;
	}

	@RestPath("jps/pid/{pid}")
	public JpsEntry jpsDetail(@RestPathParameter("pid") int pid)
			throws Exception {
		int currentPid = Utils.getCurrentPid();
		if (pid == 0) {
			pid = currentPid;
		}
		Map<Integer, String> map = Utils.jps();
		String mainClass = map.get(pid);
		if (mainClass == null) {
			throw new FileNotFoundException("pid not found: " + pid);
		}
		JpsEntry jpsEntry = new JpsEntry();
		jpsEntry.setPid(pid);
		jpsEntry.setSelf(currentPid == pid);
		jpsEntry.setMainClass(mainClass);
		return jpsEntry;
	}

	@RestMethod("POST")
	@RestPath("jps/pid/{pid}/sampler")
	public ThreadSamplerAgentEntry connectAgent(
			@RestPathParameter("pid") int pid) throws Exception {
		ThreadSamplerAgentEntry agentInfo = new ThreadSamplerAgentEntry();
		agentInfo.setSamplerType("jps");
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
