package com.hp.ts.rnd.tool.perf.threads;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.Thread.State;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class GeneralThreadStackTrace implements ThreadStackTrace, Serializable {

	private static final long serialVersionUID = 4117673568266112823L;

	private long threadIdentifier;

	private String threadName;

	private Thread.State threadState;

	private GeneralThreadStackFrame[] stackFrames;

	private String proxyType;

	private Map<String, String> extendedInfo;

	private static Map<String, Constructor<? extends ThreadStackTrace>> cachedConstructors = new HashMap<String, Constructor<? extends ThreadStackTrace>>();

	@ConstructorProperties({ "proxyType", "threadIdentifier", "threadName",
			"threadState", "stackFrames", "extendedInfo" })
	public GeneralThreadStackTrace(String proxyType, long threadIdentifier,
			String threadName, State threadState,
			GeneralThreadStackFrame[] stackFrames,
			Map<String, String> extendedInfo) {
		this.proxyType = proxyType;
		this.threadIdentifier = threadIdentifier;
		this.threadName = threadName;
		this.threadState = threadState;
		this.stackFrames = stackFrames;
		this.extendedInfo = extendedInfo;
	}

	@Override
	public long getThreadIdentifier() {
		return threadIdentifier;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public State getThreadState() {
		return threadState;
	}

	@Override
	public GeneralThreadStackFrame[] getStackFrames() {
		return stackFrames;
	}

	public String getProxyType() {
		return proxyType;
	}

	public Map<String, String> getExtendedInfo() {
		return extendedInfo;
	}

	@Override
	public GeneralThreadStackTrace toGeneralTrace() {
		return this;
	}

	// TODO log
	public ThreadStackTrace toStackTrace() {
		Constructor<? extends ThreadStackTrace> constructor = cachedConstructors
				.get(proxyType);
		if (constructor == null) {
			Class<?> type;
			try {
				type = Class.forName(proxyType);
			} catch (ClassNotFoundException e) {
				return this;
			}
			if (!ThreadStackTrace.class.isAssignableFrom(type)) {
				return this;
			}
			Class<? extends ThreadStackTrace> traceType = type
					.asSubclass(ThreadStackTrace.class);
			try {
				constructor = traceType
						.getDeclaredConstructor(GeneralThreadStackTrace.class);
				constructor.setAccessible(true);
			} catch (NoSuchMethodException e) {
				return this;
			}
			ThreadStackTrace trace;
			try {
				trace = constructor.newInstance(this);
				cachedConstructors.put(proxyType, constructor);
			} catch (Exception e) {
				trace = this;
			}
			return trace;
		} else {
			ThreadStackTrace trace;
			try {
				trace = constructor.newInstance(this);
			} catch (Exception e) {
				trace = this;
			}
			return trace;
		}
	}

}
