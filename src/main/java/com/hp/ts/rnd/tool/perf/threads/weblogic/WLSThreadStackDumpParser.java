package com.hp.ts.rnd.tool.perf.threads.weblogic;

import java.io.IOException;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.ts.rnd.tool.perf.threads.StackTraceElementWrapper;

class WLSThreadStackDumpParser {

	private String text;

	private int nextIndex;

	private enum ThreadParseState {
		StartThread,

		ThreadLine(
				"^\"(.*)\" (waiting for lock ([^@]+)@([0-9a-f]+) )?(WAITING|RUNNABLE|BLOCKED|TIMED_WAITING)(.*)$"),

		StackTrace("\t(.+)\\.([^(]+)\\((.+)\\)"),

		EndThread;

		private Pattern pattern;

		private ThreadParseState() {
		}

		private ThreadParseState(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

	private static ThreadParseState[] ThreadParseStates = ThreadParseState
			.values();

	public WLSThreadStackDumpParser(String text) throws IOException {
		this.text = text;
		this.nextIndex = 0;
	}

	public WLSJmxThreadEntry nextThread() throws IOException {
		WLSJmxThreadEntry thread = null;
		BitSet possibleStates = new BitSet();
		possibleStates.set(ThreadParseState.StartThread.ordinal());
		ThreadParseState[] states = ThreadParseStates;
		StringBuilder history = new StringBuilder();
		String line;
		NEXTLINE: while ((line = nextLine()) != null) {
			history.append(line).append('\n');
			int nextState;
			while ((nextState = possibleStates.nextSetBit(0)) >= 0) {
				possibleStates.clear(nextState);
				ThreadParseState state = states[nextState];
				Matcher matcher;
				switch (state) {
				case StartThread:
					if (thread != null) {
						throw new IllegalStateException("unexpect state: "
								+ nextState + "\nHistory:\n" + history);
					}
					possibleStates.set(ThreadParseState.ThreadLine.ordinal());
					thread = new WLSJmxThreadEntry();
					break;
				case ThreadLine:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						thread.setThreadName(matcher.group(1));
						if (matcher.group(3) != null) {
							thread.setLockClassName(matcher.group(3));
						}
						if (matcher.group(4) != null) {
							thread.setLockHashIdentifier(Integer.parseInt(
									matcher.group(4), 16));
						}
						thread.setThreadState(Thread.State.valueOf(matcher
								.group(5)));
						thread.setThreadDetail(matcher.group(6));
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackTrace
								.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					} else {
						// End of threads
						return null;
					}
				case StackTrace:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						String fileInfo = matcher.group(3);
						int lineNoIndex = fileInfo.lastIndexOf(':');
						int lineNo = -1;
						if (lineNoIndex > 0) {
							try {
								lineNo = Integer.parseInt(fileInfo
										.substring(lineNoIndex + 1));
							} catch (NumberFormatException e) {
							}
							fileInfo = fileInfo.substring(0, lineNoIndex);
						}
						StackTraceElement traceElement = new StackTraceElement(
								matcher.group(1), matcher.group(2), fileInfo,
								fileInfo.equals("Native Method") ? -2 : lineNo);
						StackTraceElementWrapper stacktrace = new StackTraceElementWrapper(
								traceElement);
						thread.getStacktraces().add(stacktrace);
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackTrace
								.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case EndThread:
					if (line.length() == 0 || line.equals("null")) {
						return thread;
					} else {
						throw new IllegalStateException(
								"should be empty line on end thread state"
										+ "\nHistory:\n" + history);
					}
				default:
					throw new IllegalStateException("unknown state: "
							+ nextState + "\nHistory:\n" + history);
				}
			}
			throw new IllegalStateException("no next state process"
					+ "\nHistory:\n" + history);
		}
		return thread;
	}

	private String nextLine() {
		if (nextIndex >= text.length()) {
			return null;
		}
		int eolIndex = text.indexOf('\n', nextIndex);
		if (eolIndex != -1) {
			String line = text.substring(nextIndex, eolIndex);
			nextIndex = eolIndex + 1;
			return line;
		} else {
			String line = text.substring(nextIndex);
			nextIndex = text.length();
			return line;
		}
	}
}
