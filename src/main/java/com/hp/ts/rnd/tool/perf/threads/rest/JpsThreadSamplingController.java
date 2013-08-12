package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.util.Utils;
import com.hp.ts.rnd.tool.perf.web.RestPath;

class JpsThreadSamplingController {

	public static class JpsEntry {
		private int pid;
		private String mainClass;

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public String getMainClass() {
			return mainClass;
		}

		public void setMainClass(String mainClass) {
			this.mainClass = mainClass;
		}

	}

	@RestPath("/jps/pid")
	public List<JpsEntry> jps() throws Exception {
		Map<Integer, String> map = Utils.jps();
		List<JpsEntry> list = new ArrayList<JpsEntry>();
		for (Entry<Integer, String> entry : map.entrySet()) {
			JpsEntry jpsEntry = new JpsEntry();
			jpsEntry.setPid(entry.getKey());
			jpsEntry.setMainClass(entry.getValue());
			list.add(jpsEntry);
		}
		return list;
	}

	@RestPath("/jps/pid/36440")
	public JpsEntry jpsDetail() throws Exception {
		int pid = 36440;
		Map<Integer, String> map = Utils.jps();
		JpsEntry jpsEntry = new JpsEntry();
		jpsEntry.setPid(pid);
		jpsEntry.setMainClass(map.get(pid));
		return jpsEntry;
	}
}
