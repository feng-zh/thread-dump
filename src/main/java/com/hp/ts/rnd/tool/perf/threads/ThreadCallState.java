package com.hp.ts.rnd.tool.perf.threads;

public interface ThreadCallState {

	public long getThreadIdentifier();

	public String getThreadName();

	public Thread.State getThreadState();

	public ThreadStackTrace[] getStrackTraces();

}
