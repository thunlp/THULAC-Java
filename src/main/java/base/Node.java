/**
 * Created：May 10, 2013 2:16:51 PM  
 * Project：ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename：Node.java  
 * description：  
 */
package base;

/**
 * topological information about a node
 * type的定义： 默认0，如果是开始节点+1，如果是结尾节点+2
 */
public class Node {
	public int type;
	public int[] predecessors;//ends with a -1
	public int[] successors;//ends with a -1
}
