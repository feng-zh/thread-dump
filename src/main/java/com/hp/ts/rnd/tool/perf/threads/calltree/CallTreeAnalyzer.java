package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadCallState;
import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class CallTreeAnalyzer {

	private static class CallCount {

		long count;

		String name;

	}

	private TreeNode<Object, CallCount> callTree = new TreeNode<Object, CallCount>(
			null);

	public void addThreadSampling(ThreadSamplingState samplingState) {
		for (ThreadCallState callState : samplingState.getCallStates()) {
			addThreadCallState(callState);
		}
	}

	public void addThreadCallState(ThreadCallState callState) {
		Object threadId = callState.getThreadIdentifier();
		TreeNode<Object, CallCount> tree = callTree.getChild(threadId, true);
		CallCount callCount = tree.getValue();
		if (callCount == null) {
			callCount = new CallCount();
			tree.setValue(callCount);
			callCount.name = callState.getThreadName();
		}
		callCount.count++;
		ThreadStackTrace[] strackTraces = callState.getStrackTraces();
		// from bottom to top
		for (int i = strackTraces.length - 1; i >= 0; i--) {
			ThreadStackTrace stacktrace = strackTraces[i];
			tree = tree.getChild(stacktrace.getTraceIdentifier(), true);
			callCount = tree.getValue();
			if (callCount == null) {
				callCount = new CallCount();
				tree.setValue(callCount);
				callCount.name = new StackTraceElement(
						stacktrace.getClassName(), stacktrace.getMethodName(),
						stacktrace.getFileName(), stacktrace.getLineNumber())
						.toString();
			}
			callCount.count++;
		}
	}

	public void print(PrintStream out) {
		print(0, "", callTree, out);
	}

	private void print(int level, String prefix,
			TreeNode<Object, CallCount> item, PrintStream out) {
		printTreeNode(prefix, item, out);
		Collection<TreeNode<Object, CallCount>> children = listTreeChildren(
				level, item);
		int i = 0;
		int n = children.size();
		if (n > 0) {
			level++;
			for (TreeNode<Object, CallCount> child : children) {
				String np = i < n - 1 ? prefix + "  |" : prefix + "   ";
				print(level, np, child, out);
				i++;
			}
		} else {
			printTreeNode(prefix + "   ", null, out);
		}
	}

	protected Collection<TreeNode<Object, CallCount>> listTreeChildren(
			int level, TreeNode<Object, CallCount> item) {
		Collection<TreeNode<Object, CallCount>> list = item.listChildren();
		List<TreeNode<Object, CallCount>> children = new ArrayList<TreeNode<Object, CallCount>>();
		if (level == 0) {
			for (TreeNode<Object, CallCount> child : list) {
				if (child.hasChildren()) {
					children.add(child);
				}
			}
		} else {
			children.addAll(list);
		}
		Collections.sort(children,
				new Comparator<TreeNode<Object, CallCount>>() {

					public int compare(TreeNode<Object, CallCount> t1,
							TreeNode<Object, CallCount> t2) {
						if (t1.getKey() instanceof Long) {
							return Long.compare((Long) t1.getKey(),
									(Long) t2.getKey());
						} else {
							return -Long.compare(t1.getValue().count,
									t2.getValue().count);
						}
					}

				});
		return children;
	}

	protected void printTreeNode(String prefix,
			TreeNode<Object, CallCount> item, PrintStream out) {
		if (prefix.length() > 0) {
			out.print(prefix.substring(0, prefix.length() - 1));
			if (item != null) {
				out.print("|-- ");
			}
		}
		if (item != null) {
			CallCount callCount = item.getValue();
			if (callCount != null && callCount.name != null) {
				out.print(callCount.name + " " + callCount.count);
			}
		}
		out.println();
	}
}
