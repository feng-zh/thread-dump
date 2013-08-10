package com.hp.ts.rnd.tool.perf.threads.examples;

import java.util.concurrent.TimeUnit;

class UsageMeasurement {

	static private final Runtime runtime = Runtime.getRuntime();
	private static long startMem;
	private static long trace;
	private static long sample;
	private static long startTime;

	public static void startMeasure() {
		runtime.gc();
		startMem = runtime.totalMemory() - runtime.freeMemory();
		startTime = System.nanoTime();
	}

	public static void incSampling(int sampleCount, int traceCount) {
		sample += sampleCount;
		trace += traceCount;
	}

	public static void stopMeasure() {
		long duration = System.nanoTime() - startTime;
		System.gc();
		long endMem = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Sampling: " + trace + ", Traces: " + sample);
		System.out.println("- take time(ms) : "
				+ TimeUnit.NANOSECONDS.toMillis(duration));
		System.out.println("- start on memory usage: " + startMem / 1024
				+ " KB");
		System.out.println("- end   on memory usage: " + endMem / 1024 + " KB");
		System.out.println("--increase memory usage: " + (endMem - startMem)
				/ 1024 + " KB");
	}
}
