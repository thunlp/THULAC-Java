package base;


import java.io.*;
import java.util.Vector;

public class Dat {
	
	public Vector<Entry> dat;
	public int datSize;

	public Dat(){
		dat = new Vector<Entry>();
		datSize = 0;
	}
	public Dat(int datSize, Vector<Entry> olddat){
		this.datSize = datSize;
        dat= new Vector<Entry>();
		for(int i = 0; i < datSize; i ++){
			dat.add(new Entry());
			dat.get(i).base = olddat.get(i).base;
			dat.get(i).check = olddat.get(i).check;
		}
	}
	public Dat(String filename) throws IOException {
		filename = Dat.class.getClassLoader().getResource(filename).getFile();
		File file = new File(filename);
		datSize = (int)(file.length() / 8);
		//System.out.println(datSize);

		FileInputStream in = new FileInputStream(file);

		byte[] tempbytes = new byte[8 * datSize];
		dat = new Vector<Entry>();
		in.read(tempbytes);
		for(int i = 0; i < datSize; i ++){
			Entry entry = new Entry();
			entry.base = bytesToInt(tempbytes, 8 * i);

			dat.add(entry);
			dat.get(i).check = bytesToInt(tempbytes, 8 * i + 4);
		}

		in.close();
	}

	public static int bytesToInt(byte[] bb, int index) {
		return (int) (((((int)bb[index + 3] & 0xff) << 24)
				| (((int)bb[index + 2] & 0xff) << 16)
				| (((int)bb[index + 1] & 0xff) << 8) | (((int)bb[index + 0] & 0xff) << 0)));
	}

	public static byte[] intToBytes(int n){
	    byte[] b = new byte[4];
	    for(int i = 0;i < 4;i++){
	        b[i] = (byte)(n >> (8 * i));
	    }
	    return b;
	}

	public void save(String filename) throws IOException{
		FileOutputStream out = new FileOutputStream(filename);
		for(Entry e : dat){
			out.write(intToBytes(e.base));
		}
		out.flush();
		for(Entry e : dat){
			out.write(intToBytes(e.check));
		}
		out.flush();
		out.close();
	}

	public boolean search(String sentence, Vector<Integer> bs, Vector<Integer> es){
		bs.clear();
		es.clear();
		boolean empty = true;
		for(int offset = 0; offset < sentence.length(); offset ++){
			int preBase = 0;
			int preInd = 0;
			int ind = 0;
			for(int i = offset; i < sentence.length(); i ++){
				ind = preBase + sentence.charAt(i);
				if(ind < 0 || ind >= datSize || dat.get(ind).check != preInd)break;
				preInd = ind;
				preBase = dat.get(ind).base;
				ind = preBase;
				if(!(ind < 0 || ind >= datSize || dat.get(ind).check != preInd)){
					bs.add(offset);
					es.add(i + 1);
					if(empty){
						empty = false;
					}
				}
			}
		}
		return !empty;
	}

	public int match(String word){
		int ind = 0;
		int base = 0;
		for(int i = 0; i < word.length(); i ++){
			ind = dat.get(ind).base + word.charAt(i);
			if((ind >= datSize) || (dat.get(ind).check != base)) return -1;
			base = ind;
		}
		ind = dat.get(base).base;
		if((ind < datSize) && (dat.get(ind).check == base)){
			return ind;
		}
		return -1;
	}

	public void update(String word, int value){
		int base = match(word);
		if(base >= 0){
			dat.get(base).base = value;
		}
	}

	public int getInfo(String prefix){
		int ind = 0;
		int base = 0;
		for(int i = 0; i < prefix.length(); i ++){
			ind = dat.get(ind).base + prefix.charAt(i);
			if((ind >= datSize) || dat.get(ind).check != base) return i;
			base = ind;
		}
		return -base;
	}

	public int getDatSize(){
		return datSize;
	}

	public Vector<Entry> getDat(){
		return dat;
	}
}
