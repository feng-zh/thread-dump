package com.hp.ts.rnd.tool.perf.threads.jstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JstackOutputParser {

	private BufferedReader reader;

	private Date sampleTime;

	private enum ThreadParseState {
		StartThread,

		ThreadLine(
				"^\"(.*)\" (daemon )?prio=([0-9]+) tid=(0x[0-9a-f]+) nid=(0x[0-9a-f]+) (.*)$"),

		ThreadState(" +java\\.lang\\.Thread\\.State: ([^ ]+)(?: \\((.+)\\))?"),

		StackTrace("\tat (.+)\\.([^(]+)\\((.+)\\)"),

		StackLock("\t- (.*) <(0x[0-9a-f]+)> \\(a (.+)\\)"),

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

	private static SimpleDateFormat DateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	private static ThreadParseState[] ThreadParseStates = ThreadParseState
			.values();

	public JstackOutputParser(InputStream input) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(input),
				256 * 1024);
		readDumpTime();
		if (!readTillEmptyLine()) {
			throw new IllegalStateException("expect empty line");
		}
	}

	private void readDumpTime() throws IOException {
		String dumpTimeLine = reader.readLine();
		if (dumpTimeLine == null) {
			throw new IllegalStateException("expect dump data time line");
		}
		try {
			synchronized (DateFormat) {
				sampleTime = DateFormat.parse(dumpTimeLine);
			}
		} catch (ParseException e) {
			throw new IllegalStateException("parse dump date time error: "
					+ dumpTimeLine, e);
		}
	}

	public Date getSampleTime() {
		return sampleTime;
	}

	private boolean readTillEmptyLine() throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				return true;
			}
		}
		return false;
	}

	public JstackThreadEntry nextThread() throws IOException {
		JstackThreadEntry thread = null;
		BitSet possibleStates = new BitSet();
		possibleStates.set(ThreadParseState.StartThread.ordinal());
		ThreadParseState[] states = ThreadParseStates;
		StringBuilder history = new StringBuilder();
		String line;
		NEXTLINE: while ((line = reader.readLine()) != null) {
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
					thread = new JstackThreadEntry();
					break;
				case ThreadLine:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						thread = new JstackThreadEntry();
						thread.setThreadName(matcher.group(1));
						thread.setDaemon(matcher.group(2) != null);
						thread.setPriority(Integer.parseInt(matcher.group(3)));
						thread.setTid(Long.parseLong(matcher.group(4)
								.substring(2), 16));
						thread.setNid(Integer.parseInt(matcher.group(5)
								.substring(2), 16));
						thread.setStatus(matcher.group(6));
						possibleStates.clear();
						possibleStates.set(ThreadParseState.ThreadState
								.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					} else {
						// End of threads
						return null;
					}
				case ThreadState:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						thread.setThreadState(Thread.State.valueOf(matcher
								.group(1)));
						thread.setDetailState(matcher.group(2));
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackTrace
								.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
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
								matcher.group(1), matcher.group(2),
								(lineNo == -1 && "Unknown Source"
										.equals(fileInfo)) ? null : fileInfo,
								fileInfo.equals("Native Method") ? -2 : lineNo);
						JstackStackTrace stacktrace = new JstackStackTrace(
								traceElement);
						thread.getStacktraces().add(stacktrace);
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackTrace
								.ordinal());
						possibleStates
								.set(ThreadParseState.StackLock.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case StackLock:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						List<JstackStackTrace> list = thread.getStacktraces();
						JstackStackTrace stacktrace = list.get(list.size() - 1);
						JstackLockInfo lockInfo = new JstackLockInfo();
						lockInfo.setLockState(matcher.group(1));
						lockInfo.setLockIdentityHashCode(Long.parseLong(matcher
								.group(2).substring(2), 16));
						lockInfo.setLockClassName(matcher.group(3));
						if ("locked".equals(lockInfo.getLockState())) {
							lockInfo.setOwnLock(true);
						}
						stacktrace.addLockInfo(lockInfo);
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackTrace
								.ordinal());
						possibleStates
								.set(ThreadParseState.StackLock.ordinal());
						possibleStates
								.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case EndThread:
					if (line.length() == 0) {
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
}
