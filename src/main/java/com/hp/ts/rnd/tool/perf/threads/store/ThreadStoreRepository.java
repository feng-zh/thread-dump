package com.hp.ts.rnd.tool.perf.threads.store;

import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

class ThreadStoreRepository {

	public String getFileNameByTraceId(long traceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLineNumberByTraceId(long traceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getClassNameByTraceId(long traceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMethodNameByTraceId(long traceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public long createTraceId(ThreadStackTrace trace) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int createThreadNameId(String threadName) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long[] createStackTraceIds(ThreadStackTrace[] strackTraces) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getThreadNameByThreadNameId(int threadNameId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ThreadStackTrace[] getStackTracesByTraceIds(long[] stacktraces) {
		// TODO Auto-generated method stub
		return null;
	}

	public long createThreadId(Long threadIdentifier, String threadName) {
		// TODO Auto-generated method stub
		return 0;
	}

}
