/**
 * Created锛�May 9, 2013 12:22:21 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�CBModel.java  
 * description锛�  
 */
package manage;

public class CBModel {
	
	private static int DEC = 1000;
	
	public int l_size; //size of the labels
	public int f_size; //size of the features
	
	public int[] ll_weights; // weights of (label, label)
	public int[] fl_weights; // weights of (feature, label)
	
	public double[] ave_ll_weights;
	public double[] ave_fl_weights;
	
	public CBModel(int l, int f){
		l_size = l;
		f_size = f;
	}
}
