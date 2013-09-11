package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.ThreadSamplingState;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackFrame;
import com.hp.ts.rnd.tool.perf.threads.ThreadStackTrace;

public class CallTreeAnalyzer {

	private static final String INDENT = " ";

	public static class CallCount {

		long count;

		Object name;

		public long getCount() {
			return count;
		}

		public Object getName() {
			return name;
		}

	}

	public static interface PrintFilter {

		public boolean acceptNode(TreeNode<Object, CallCount> item);

		// true: keep the trace (up and all down), otherwise ignore the trace
		public Boolean acceptTrace(TreeNode<Object, CallCount> item);

	}

	private TreeNode<Object, CallCount> callTree = new TreeNode<Object, CallCount>(
			null);

	public void addThreadSampling(ThreadSamplingState samplingState) {
		for (ThreadStackTrace stackTrace : samplingState.getStackTraces()) {
			addThreadStackTrace(stackTrace);
		}
	}

	public void addThreadStackTrace(ThreadStackTrace stackTrace) {
		long threadId = stackTrace.getThreadIdentifier();
		TreeNode<Object, CallCount> tree = callTree.getChild(
				threadId == 0L ? stackTrace.getThreadName() : threadId, true);
		CallCount callCount = tree.getValue();
		if (callCount == null) {
			callCount = new CallCount();
			tree.setValue(callCount);
			callCount.name = stackTrace.getThreadName();
		}
		callCount.count++;
		ThreadStackFrame[] stackFrames = stackTrace.getStackFrames();
		// from bottom to top
		for (int i = stackFrames.length - 1; i >= 0; i--) {
			ThreadStackFrame stackFrame = stackFrames[i];
			Object stackFrameId = stackFrame.getStackFrameId();
			if (stackFrameId == null) {
				stackFrameId = stackFrame.toTraceElement();
			}
			tree = tree.getChild(stackFrameId, true);
			callCount = tree.getValue();
			if (callCount == null) {
				callCount = new CallCount();
				tree.setValue(callCount);
				callCount.name = stackFrame;
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
		boolean printChildren = false;
		level++;
		for (TreeNode<Object, CallCount> child : children) {
			String np = prefix + INDENT.substring(0, INDENT.length() - 1)
					+ (i < n - 1 ? "|" : " ");
			print(level, np, child, out);
			printChildren = true;
			i++;
		}
		if (!printChildren) {
			printTreeNode(prefix + INDENT, null, out);
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
							return compare((Long) t1.getKey(),
									(Long) t2.getKey());
						} else {
							return -compare(t1.getValue().count,
									t2.getValue().count);
						}
					}

					private int compare(long l1, long l2) {
						return (l1 < l2) ? -1 : ((l1 == l2) ? 0 : 1);
					}

				});
		return children;
	}

	protected void printTreeNode(String prefix,
			TreeNode<Object, CallCount> item, PrintStream out) {
		if (prefix.length() > 0) {
			out.print(prefix);
			if (item != null) {
				out.print("\\- ");
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

	public void printFilter(PrintStream out, PrintFilter filter) {
		TreeNode<Object, CallCount> node = callTree;
		if (filter != null) {
			TreeNode<Object, CallCount> newRoot = new TreeNode<Object, CallTreeAnalyzer.CallCount>(
					null);
			filterNode(newRoot, node, filter, false);
			node = newRoot;
		}
		print(0, "", node, out);
	}

	private boolean filterNode(TreeNode<Object, CallCount> newNode,
			TreeNode<Object, CallCount> node, PrintFilter filter,
			boolean acceptDownstream) {
		boolean acceptNode = acceptDownstream;
		for (TreeNode<Object, CallCount> child : node.listChildren()) {
			if (!filter.acceptNode(child)) {
				// not accept this child node and subs
				continue;
			} else {
				Boolean acceptTrace = filter.acceptTrace(child);
				if (acceptTrace == null) {
					TreeNode<Object, CallCount> newChild = newNode.getChild(
							child.getKey(), true);
					newChild.setValue(child.getValue());
					boolean accept = filterNode(newChild, child, filter,
							acceptDownstream);
					if (accept) {
						acceptNode = true;
					} else {
						newNode.removeChild(child.getKey());
					}
				} else if (acceptTrace.booleanValue()) {
					TreeNode<Object, CallCount> newChild = newNode.getChild(
							child.getKey(), true);
					newChild.setValue(child.getValue());
					filterNode(newChild, child, filter, true);
					acceptNode = true;
				} else {
					acceptNode = false;
					continue;
				}
			}
		}
		return acceptNode;
	}
}
