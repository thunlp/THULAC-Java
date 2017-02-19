/**
 * Created锛�May 9, 2013 12:22:21 PM  
 * Project锛�ThulacJava  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename锛�CBModel.java  
 * description锛�  
 */
package org.thunlp.character;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
		ll_weights = new int[l * l];
		fl_weights = new int[f * l];
		ave_ll_weights = new double[l * l];
		ave_fl_weights = new double[f * l];
	}
	
	public void reset_ave_weights(){
		ave_ll_weights = new double[l_size * l_size];
		ave_fl_weights = new double[f_size * l_size];
	}
	
	public void update_ll_weights(int i, int j, int delta, long steps){
		int ind = i * l_size + j;
		ll_weights[ind] += delta;
		ave_ll_weights[ind] += steps * delta;
	}
	
	public void update_fl_weights(int i, int j, int delta, long steps){
		int ind = i * l_size + j;
		fl_weights[ind] += delta;
		ave_fl_weights[ind] += steps * delta;
	}
	
	public void average(int step){
		for(int i = 0; i < l_size * f_size; i ++){
			fl_weights[i] = (int)(((double)fl_weights[i] - ave_fl_weights[i] /(double)step)*DEC + 0.5);
		}
		for(int i = 0; i < l_size * l_size; i ++){
			ll_weights[i] = (int)((((double)ll_weights[i]) - ave_ll_weights[i] /(double)step)*DEC + 0.5);
		}
	}
	
	public static int bytesToInt(byte[] bb, int index) {    
		return (int) ((((bb[index + 3] & 0xff) << 24) 
				| ((bb[index + 2] & 0xff) << 16) 
				| ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
	}
	
	public static byte[] intToBytes(int n){  
	    byte[] b = new byte[4];  
	    for(int i = 0;i < 4;i++){  
	        b[i] = (byte)(n >> (8 * i));   
	    }  
	    return b;  
	}
	
	public CBModel(String filename) throws IOException{
		File file = new File(filename);
		FileInputStream in = new FileInputStream(file);
		
		byte[] tempbytes = new byte[4];
		in.read(tempbytes);
		l_size = bytesToInt(tempbytes, 0);
		in.read(tempbytes);
		f_size = bytesToInt(tempbytes, 0);
		
		ll_weights = new int[l_size * l_size];
		tempbytes = new byte[4 * ll_weights.length];
		in.read(tempbytes);
		for(int i = 0; i < ll_weights.length; i ++){
			ll_weights[i] = bytesToInt(tempbytes, 4 * i);
		}
		
		fl_weights = new int[f_size * l_size];
		tempbytes = new byte[4 * fl_weights.length];
		in.read(tempbytes);
		for(int i = 0; i < fl_weights.length; i ++){
			fl_weights[i] = bytesToInt(tempbytes, 4 * i);
		}
		in.close();
	}
	
	public void save(String filename) throws IOException{
		FileOutputStream out = new FileOutputStream(filename);
		out.write(intToBytes(l_size));
		out.write(intToBytes(f_size));
		out.flush();
		for(int i = 0; i < ll_weights.length; i ++){
			out.write(intToBytes(ll_weights[i]));
		}
		out.flush();
		for(int i = 0; i < fl_weights.length; i ++){
			out.write(intToBytes(fl_weights[i]));
		}
		out.flush();
		out.close();
	}
	
	/**
	 * <p>Title:main</p>
	 * <p>Description:<p>
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		int[] weights = new int[3];
		weights[0] = 1;
		weights[1] = 3;
		weights[2] = 5;
		for(int i = 0;i < weights.length ; i ++){
			System.out.println(weights[i]);
		}
		weights = new int[4];
		for(int i = 0;i < weights.length ; i ++){
			System.out.println(weights[i]);
		}
		*/
	}

}
