package com.hp.ts.rnd.tool.perf.threads.examples;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplings;

public class TestDiskMain {

	public static void main(String[] args) throws Exception {
		File inputFile = new File("sampling.out.gz");
		File outputFile = new File("test.out.gz");
		// SampleMeExample sampleMe = new SampleMeExample();
		UsageMeasurement.startMeasure();
		// sampleMe.start();
		ThreadSamplingService service = ThreadSamplings
				.createDiskReplaySamplingService(new GZIPInputStream(
						new FileInputStream(inputFile)));
		service.executeSampling(ThreadSamplings
				.createDiskStoreSamplingHandler(new DataOutputStream(
						new BufferedOutputStream(new GZIPOutputStream(
								new FileOutputStream(outputFile))))));
		UsageMeasurement.stopMeasure();
		System.out.println(inputFile.length() + " <=> " + outputFile.length());
		// sampleMe.stop();
	}

}
