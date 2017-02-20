package org.thunlp.base;

/**
 * topological information about a node
 * definition of type: defaulted to 0, if the node is a starting node than +1, if the
 * node is a ending node then +2
 */
public class Node {
	public int type;
	public int[] predecessors;//ends with a -1
	public int[] successors;//ends with a -1
}
