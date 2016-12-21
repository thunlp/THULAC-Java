/**
 * Created锛�May 10, 2013 2:18:41 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�AlphaBeta.java  
 * description锛�  
 */
package base;


//a structure for alphas and betas
public class AlphaBeta {
	public int value;
	public int nodeId;
	public int labelId;
	
	public AlphaBeta() {
		super();
		value = 0;
		nodeId = -2;
		labelId = 0;
	}

	public AlphaBeta(int value, int nodeId, int labelId) {
		super();
		this.value = value;
		this.nodeId = nodeId;
		this.labelId = labelId;
	}



}