package manage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Preprocesser {
	static HashSet<Integer> otherSet = new HashSet<Integer>();	// store all the number, digit and punctuation
	static HashSet<Integer> singlePunSet = new HashSet<Integer>();		// store all the punctuation that need to split
	static HashSet<Integer> httpSet = new HashSet<Integer>();			// store all the number, digit and punctuation that may be a charactor in a url

	static {
		init();
	}

	public Preprocesser(){}

	private static void init() {
		for(int i = 65; i < 91; i ++){
			otherSet.add(i);
			httpSet.add(i);
		}
		for(int i = 97; i < 123; i ++){
			otherSet.add(i);
			httpSet.add(i);
		}
		for(int i = 48; i < 58; i ++){
			otherSet.add(i);
			httpSet.add(i);
		}
		int other[] = {65292, 12290, 65311, 65281, 65306, 65307, 8216, 8217, 8220, 8221, 12304, 12305,
				12289, 12298, 12299, 126, 183, 64, 124, 35, 65509, 37, 8230, 38, 42, 65288,
				65289, 8212, 45, 43, 61, 44, 46, 60, 62, 63, 47, 33, 59, 58, 39, 34, 123, 125,
				91, 93, 92, 124, 35, 36, 37, 94, 38, 42, 40, 41, 95, 45, 43, 61, 9700, 9734, 9733};
		int len = 63;
		for(int i = 0; i < len; i ++){
			otherSet.add(other[i]);
		}

		// singlePun correspond to (see otherSet)
		int singlePun[] = {65292, 12290, 65311, 65281, 65306, 65307, 8216, 8217, 8220, 8221, 1230, 12304,
				12305, 12289, 12298, 12299, 64,35, 65288, 65289, 34, 91, 93, 126, 47, 44, 58,
				63, 9700, 9734, 9733, 8230, 39, 33, 42, 43, 62, 40, 41};
		len = 39;
		for(int i = 0 ; i < len; i ++){
			singlePunSet.add(singlePun[i]);
		}

		char httpChar[] = {'/', '.', ':', '#', '"', '_', '-', '=', '+', '&', '$', ';'};
		len = 12;
		for(int i = 0; i < len; i ++){
			httpSet.add((int)httpChar[i]);
		}
	}

	public boolean isOther(int c){
		if(otherSet.contains(c)){
			return true;
		}else{
			return false;
		}
	}

	public boolean isSinglePun(int c){
		if(singlePunSet.contains(c)){
			return true;
		}else{
			return false;
		}
	}

	public boolean isHttp(int c){
		if(httpSet.contains(c)){
			return true;
		}else{
			return false;
		}
	}

	public String clean(String sentence, List<Integer> graph){
		String senClean= new String();
		graph.clear();
		boolean hasSpace = false;		//use to check whether the char is a space
		boolean hasOther = false;		//use to check whether isOther(char);
		boolean hasSinglePun = false;	//use to check whether isSinglePun(char);
		boolean hasHttp = false;		//use to check whether isHttp(char);
		boolean hasAt = false;			//use to check whether the char is @
		boolean hasTitle = false;		//use to check whether the sentence has 《》
		List<Integer> httpStartVec =new ArrayList<>();
		int httpStart = -1;
		List<List<Integer>> httpVec =new ArrayList<>();
		int c = -1;
		List<Integer> tmpRaw =new ArrayList<Integer>();
		List<Integer> npRaw =new ArrayList<Integer>();
		int npStart = -1;
		List<Integer> npStartVec = new ArrayList<Integer>();
		List<List<Integer>> npVec =new ArrayList<List<Integer>>();
		List<Integer> titleRaw =new ArrayList<Integer>();
		int titleStart = -1;
		List<Integer> titleStartVec =new ArrayList<Integer>();
		List<List<Integer>> titleVec =new ArrayList<List<Integer>>();
		for(int i = 0; i < sentence.length(); i++){
			c = sentence.charAt(i);
			if(c == 32 || c == 12288){
				hasOther = false;
				if(hasSpace){
					continue;
				}else{
					if(graph.size()>0){
						int o=graph.get(graph.size() - 1)&12;
						graph.set(graph.size()-1, o);
					}
					hasSpace=true;
				}

				if(hasAt){
					npVec.add(npRaw);
					npStartVec.add(npStart);
					hasAt = false;
				}
			}else if(isOther(c)){
				if(hasSpace){
					senClean+=sentence.charAt(i);
					if(isSinglePun(c)){
						graph.add(8);
						hasSinglePun = true;
					}else{
						graph.add(9);
						hasSinglePun = false;
					}
					hasSpace = false;
				}else if(hasOther){
					if(isSinglePun(c)){
						if(graph.size()>0){
							int o=graph.get(graph.size() - 1)&12;
							graph.set(graph.size()-1, o);
						}
						senClean+=sentence.charAt(i);
						graph.add(8);
						hasSinglePun = true;

					}else{
						if(hasSinglePun){
							senClean+=sentence.charAt(i);
							graph.add(9);
						}else{
							if (graph.get(graph.size() - 1) == 0) {
								graph.set(graph.size()-1, 7);
							}
							senClean+=sentence.charAt(i);
							graph.add(2);
						}
						hasSinglePun = false;
					}
				}else{
					senClean+=sentence.charAt(i);
					graph.add(9);
					if(isSinglePun(c)){
						hasSinglePun = true;
					}else{
						hasSinglePun = false;
					}
				}
				if(c == 41 || c == 65289){
					if(hasAt){
						npVec.add(npRaw);
						npStartVec.add(npStart);
						hasAt = false;
					}
				}
				if(c == 12299){
					if(hasTitle){
						titleVec.add(titleRaw);
						titleStartVec.add(titleStart);
						hasTitle = false;
					}
				}
				hasOther = true;
			}else{
				if(hasSpace){
					senClean+=sentence.charAt(i);
					graph.add(9);
				}else if(hasOther){
					int o=graph.get(graph.size() - 1)&12;
					graph.set(graph.size()-1, o);
					if(hasSinglePun){
						senClean+=sentence.charAt(i);
						graph.add(9);
						hasSinglePun = false;
					}else{
						senClean+=sentence.charAt(i);
						graph.add(15);
					}
				}else{
					senClean+=sentence.charAt(i);
					graph.add(15);
				}
				hasSpace = false;
				hasOther = false;
			}

			if(isHttp(c)){
				if(!hasHttp){
					if(c == 'h'){
						httpStart = graph.size() - 1;
						tmpRaw.clear();
						tmpRaw.add(c);
						hasHttp = true;
					}
				}else{
					tmpRaw.add(c);
				}
			}else{
				if(hasHttp){
					httpVec.add(tmpRaw);
					httpStartVec.add(httpStart);
					hasHttp = false;
				}
			}

			if(c == 64){
				if(hasAt){
					npVec.add(npRaw);
					npStartVec.add(npStart);
					npRaw.clear();
				}
				hasAt = true;
				npStart = graph.size() - 1;
				npRaw.clear();
			}else if(hasAt){
				npRaw.add(c);
			}

			if(c == 12298){
				hasTitle = true;
				titleStart = graph.size() - 1;
				titleRaw.clear();
			}else if(hasTitle){
				titleRaw.add(c);
			}
		}
		if(tmpRaw.size() != 0){
			httpVec.add(tmpRaw);
			httpStartVec.add(httpStart);
		}
		if(npRaw.size() != 0){
			npVec.add(npRaw);
			npStartVec.add(npStart);
		}

		String str;
		for(int i = 0 ; i < httpVec.size(); i ++){
			str=httpVec.get(i).toString();
			int found = str.indexOf("http");
			if(found != -1){
				int start = httpStartVec.get(i);
				int size = str.length();
				graph.set(start, 1);
				for(int j = start + 1; j < start + size - 1; j ++){
					graph.set(j, 2);
				}
				graph.set(start + size - 1, 4);

			}
		}

		for(int i = 0; i < npVec.size(); i ++){
			npRaw = npVec.get(i);
			if(npRaw.size() < 15 && npRaw.size() > 0){
				int start = npStartVec.get(i);
				int size = npRaw.size();
				graph.set(start, 1);
				for(int j = start + 1; j < start + size - 1; j ++){
					graph.set(j, 2);
				}
				graph.set(start + size - 1, 4);

			}
		}

		for(int i = 0; i < titleVec.size(); i ++){
			titleRaw = titleVec.get(i);
			if(isPossibleTitle(titleRaw)){
				int start = titleStartVec.get(i);
				int size = titleRaw.size();
				graph.set(start, 1);
				for(int j = start + 1; j < start + size - 1; j ++){
					graph.set(j, 2);
				}
				graph.set(start + size - 1, 4);
			}
		}

		if(graph.size()!=0){
			graph.set(0, graph.get(0));
			graph.set(graph.size()-1, graph.get(graph.size() - 1)&12);

			if(graph.get(0)==0) graph.set(0,9);

			if(graph.size()-1==0) graph.set(graph.size()-1, 12);
		}

		return senClean;
	}

	boolean isPossibleTitle(List<Integer> titleRaw){
		if(titleRaw.size() > 10 || titleRaw.size() == 0){
			return false;
		}else{
			for(int i = 0; i < titleRaw.size(); i ++){
				if(isOther(titleRaw.get(i))){
					return false;
				}
			}
			return true;
		}
	}
}
