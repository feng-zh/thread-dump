package com.hp.ts.rnd.tool.perf.threads.rest;

import java.util.concurrent.TimeUnit;

class ThreadSamplingRequestForm {

	private int duration;

	private String durationUnit;

	private int interval;

	private String intervalUnit;

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDurationUnit() {
		return durationUnit;
	}

	public void setDurationUnit(String durationUnit) {
		this.durationUnit = durationUnit;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getIntervalUnit() {
		return intervalUnit;
	}

	public void setIntervalUnit(String intervalUnit) {
		this.intervalUnit = intervalUnit;
	}

	static int normilizeTime(int time, String unit, TimeUnit timeUnit) {
		TimeUnit sourceUnit;
		if ("minute".equals(unit)) {
			sourceUnit = TimeUnit.MINUTES;
		} else if ("second".equals(unit)) {
			sourceUnit = TimeUnit.SECONDS;
		} else if ("hour".equals(unit)) {
			sourceUnit = TimeUnit.HOURS;
		} else if ("millisec".equals(unit)) {
			sourceUnit = TimeUnit.MILLISECONDS;
		} else {
			throw new IllegalArgumentException("invalid timeunit " + unit);
		}
		return (int) timeUnit.convert(time, sourceUnit);
	}

}