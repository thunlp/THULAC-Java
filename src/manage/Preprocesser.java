package manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import base.Dat;
import base.POCGraph;
import base.Raw;
import base.TaggedSentence;

public class Preprocesser {
	HashMap<Integer,Integer> t2s;
	HashMap<Integer,Integer> s2t;
	HashSet<Integer> otherSet;		// store all the number, digit and punctuation
	HashSet<Integer> singlePunSet;		// store all the punctuation that need to split
	HashSet<Integer> httpSet;			// store all the number, digit and punctuation that may be a charactor in a url
	public Preprocesser(){
		otherSet =new HashSet<Integer>();
		singlePunSet = new HashSet<Integer>();
		httpSet = new HashSet<Integer>();
		t2s = new HashMap<Integer,Integer>();
		s2t =new HashMap<Integer,Integer>();
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
			/*
			for(int i = 65296; i < 65306; i ++){
				otherSet.insert(i);
			}
			int chineseNums[] = {12295,19968,20108,19977,22235,20116,20845,19971,20843,20061};
			for(int i = 0; i < 10; i ++){
				otherSet.insert(chineseNums[i]);
			}
			*/

			// other correspond to ，:65292 。:12290 ？:65311 ！:65281 ：:65306 
			//						；:65307 ‘:8216 ’:8217 “:8220 ”:8221 【:12304 】:12305 、
			//						:12289 《:12298 》:12299 ~:126 ·:183 @:64 |:124 #:35 ￥:65509 
			//						%:37 ……:8230 8230 &:38 *:42 （:65288 ）:65289 ——:8212 8212 -:45 
			//						+:43 =:61 ...:46 46 46 。。。:12290 12290 12290 ,:44 .:46 <:60 >:62 
			//						?:63 /:47 ~:126 !:33 @:64 ;:59 ::58 ':39 ":34 {:123 }:125 [:91 ]:93 
			//						\:92 |:124 @:64 #:35 $:36 %:37 ^:94 &:38 *:42 (:40 ):41 _:95 -:45 +:43 =:61 
			//						◤:9700 ☆:9734 ★:9733
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
								63, 9700, 9734, 9733, 8230, 39, 33, 42, 43, 62, 40, 41, 59, 61};
			len = 41;
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

		public void setT2SMap(String filename) throws IOException{
			File file = new File(filename);
			int datSize = (int)(file.length() / 8);
			FileInputStream in = new FileInputStream(file);
			int[] tra = new int[datSize];
			int[] sim = new int[datSize];
			byte[] tempbytes = new byte[4 * datSize];
			in.read(tempbytes);
			for(int i=0;i<datSize;i++)
			{
				tra[i] = bytesToInt(tempbytes, 4 * i);
			}
			in.read(tempbytes);
			for(int i=0;i<datSize;i++)
			{
				sim[i] = bytesToInt(tempbytes, 4 * i);
			}
			for(int i = 0; i < datSize; i ++){
				t2s.put(tra[i], sim[i]);
				s2t.put(sim[i], tra[i]);
			}
			in.close();
		}

		public static int bytesToInt(byte[] bb, int index) {    
			return (int) (((((int)bb[index + 3] & 0xff) << 24) 
					| (((int)bb[index + 2] & 0xff) << 16) 
					| (((int)bb[index + 1] & 0xff) << 8) | (((int)bb[index + 0] & 0xff) << 0)));
		}
	    public String clean(String sentence, POCGraph graph){
	        String senClean= new String();
	        graph.clear();
	        boolean hasSpace = false;		//use to check whether the char is a space 
			boolean hasOther = false;		//use to check whether isOther(char);
			boolean hasSinglePun = false;	//use to check whether isSinglePun(char);
			boolean hasHttp = false;		//use to check whether isHttp(char);
			// boolean hasAt = false;			//use to check whether the char is @
			boolean hasTitle = false;		//use to check whether the sentence has 《》
			Vector<Integer> httpStartVec =new Vector<Integer>();
			int httpStart = -1;
			Vector<Raw> httpVec =new Vector<Raw>();
	        int c = -1;
			Raw tmpRaw =new Raw();
			Raw npRaw =new Raw();
			int npStart = -1;
			// Vector<Integer> npStartVec = new Vector<Integer>();
			// Vector<Raw> npVec =new Vector<Raw>();
			Raw titleRaw =new Raw();
			int titleStart = -1;
			Vector<Integer> titleStartVec =new Vector<Integer>();
			Vector<Raw> titleVec =new Vector<Raw>();
			for(int i = 0; i < (int)sentence.length(); i++){
	            c = sentence.charAt(i);
//	            System.out.print(c+" ");
				// if the sentence has space
	            if(c == 32 || c == 12288){
					hasOther = false;
	                if(hasSpace){
	                    continue;
	                }else{
	                    if(graph.size()>0){
	                        int o=graph.lastElement()&12;
	                        graph.setElementAt(o, graph.size()-1);
	                    }
	                    hasSpace=true;
	                }

					// if(hasAt){
					// 	npVec.add(npRaw);
					// 	npStartVec.add(npStart);
					// 	hasAt = false;
					// }
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
								int o=graph.lastElement()&12;
		                        graph.setElementAt(o, graph.size()-1);
							}
							senClean+=sentence.charAt(i);
							graph.add(8);
							hasSinglePun = true;

						}else{
							if(hasSinglePun){
								senClean+=sentence.charAt(i);
								graph.add(9);
							}else{						
								if(graph.lastElement()==0) 
								{
			                        graph.setElementAt(7, graph.size()-1);
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
					// if(c == 41 || c == 65289){
					// 	if(hasAt){
					// 		npVec.add(npRaw);
					// 		npStartVec.add(npStart);
					// 		hasAt = false;
					// 	}
					// }
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
                        graph.setElementAt(graph.lastElement()&12, graph.size()-1);
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

				// if(isHttp(c)){
				// 	if(!hasHttp){
				// 		if(c == 'h'){
				// 			httpStart = graph.size() - 1;
				// 			tmpRaw.clear();
				// 			tmpRaw.add(c);
				// 			hasHttp = true;
				// 		}
				// 	}else{
				// 		tmpRaw.add(c);
				// 	}
				// }else{
				// 	if(hasHttp){
				// 		httpVec.add(tmpRaw);
				// 		httpStartVec.add(httpStart);
				// 		hasHttp = false;
				// 	}
				// }

				// if(c == 64){
				// 	if(hasAt){
				// 		npVec.add(npRaw);
				// 		npStartVec.add(npStart);
				// 		npRaw.clear();
				// 	}
				// 	hasAt = true;
				// 	npStart = graph.size() - 1;
				// 	npRaw.clear();
				// }else if(hasAt){
				// 	npRaw.add(c);
				// }

				if(c == 12298){
					hasTitle = true;
					titleStart = graph.size() - 1;
					titleRaw.clear();
				}else if(hasTitle){
					titleRaw.add(c);
				}
			}
			// if(tmpRaw.size() != 0){
			// 	httpVec.add(tmpRaw);
			// 	httpStartVec.add(httpStart);
			// }
			// if(npRaw.size() != 0){
			// 	npVec.add(npRaw);
			// 	npStartVec.add(npStart);
			// }

			// String str;
			// for(int i = 0 ; i < httpVec.size(); i ++){
			// 	str=httpVec.get(i).toString();
			// 	int found = str.indexOf("http");
			// 	if(found != -1){
			// 		int start = httpStartVec.get(i);
			// 		int size = str.length();
			// 		//std::cout<<std::endl<<sentence<<":Here:"<<str<<":"<<start<<":"<<size<<":"<<graph.size()<<std::endl;
					
			// 		graph.setElementAt(1, start);
			// 		for(int j = start + 1; j < start + size - 1; j ++){
			// 			graph.setElementAt(2, j);
			// 		}
			// 		graph.setElementAt(4, start + size - 1);
					
			// 	}
			// }

			// for(int i = 0; i < npVec.size(); i ++){
			// 	npRaw = npVec.get(i);
			// 	if(npRaw.size() < 15 && npRaw.size() > 0){
			// 		int start = npStartVec.get(i);
			// 		int size = npRaw.size();
					
			// 		graph.setElementAt(1, start);
			// 		for(int j = start + 1; j < start + size - 1; j ++){
			// 			graph.setElementAt(2, j);
			// 		}
			// 		graph.setElementAt(4, start + size - 1);
					
			// 	}
			// }

			for(int i = 0; i < titleVec.size(); i ++){
				titleRaw = titleVec.get(i);
				if(isPossibleTitle(titleRaw)){
					int start = titleStartVec.get(i);
					int size = titleRaw.size();

					graph.setElementAt(1, start);
					for(int j = start + 1; j < start + size - 1; j ++){
						graph.setElementAt(2, j);
					}
					graph.setElementAt(4, start + size - 1);
				}
			}
			
			if(graph.size()!=0){
	            graph.setElementAt(graph.get(0)&9,0);
	            graph.setElementAt(graph.lastElement()&12,graph.size()-1);
			    if(graph.get(0)==0) graph.setElementAt(9,0);
		    	if(graph.lastElement()==0) graph.setElementAt(12,graph.size()-1);
	        }
			
			//System.out.println(senClean);
	        return senClean;
	    };

		boolean isPossibleTitle(Raw titleRaw){
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

		int getT2S(int c){
			if(t2s.containsKey(c)){
				return t2s.get(c);
			}else{
				return c;
			}
		}

		int getS2T(int c){
			if(s2t.containsKey(c)){
				return s2t.get(c);
			}else{
				return c;
			}
		}
		
		boolean containsT(String sentence){
			for(int i = 0; i < sentence.length(); i ++){
				if(t2s.containsKey(sentence.charAt(i))){
					return true;
				}
			}
			return false;
		}

		public String T2S(String sentence){
			String newSentence ="";
			for(int i = 0; i < sentence.length();i ++){
				newSentence+=(char)(getT2S(sentence.charAt(i)));
			}
			return newSentence;
		}
		
		void S2T(TaggedSentence sentence, String oriSentence){
			int count = 0;
			for(int i = 0; i < sentence.size();i ++){
				for(int j = 0; j < sentence.get(i).word.length(); j ++){
					sentence.get(i).word = sentence.get(i).word.substring(0,j-1)+oriSentence.charAt(count)
							+sentence.get(i).word.substring(j+1);
					count ++;
				}
			}
		}

		/*
	    int cleanAndT2S(RawSentence& sentence, RawSentence& senClean, POCGraph& graph){
	        senClean.clear();
	        graph.clear();
	        bool hasSpace = false;		//use to check whether the char is a space 
			bool hasOther = false;		//use to check whether isOther(char);
			bool hasSinglePun = false;	//use to check whether isSinglePun(char);
			bool hasHttp = false;		//use to check whether isHttp(char);
			std::vector<int> httpStartVec;
			int httpStart = -1;
			std::vector<Raw> httpVec;
	        int c = -1;
			Raw tmpRaw;
			for(int i = 0; i < (int)sentence.size(); i++){
	            c = sentence.at(i);
				// if the sentence has space
	            if(c == 32 || c == 12288){
					hasOther = false;
	                if(hasSpace){
	                    continue;
	                }else{
	                    if(graph.size()){
	                        graph.back()&=12;
	                    }
	                    hasSpace=true;
	                }
	            }else if(isOther(c)){
					if(hasSpace){
						senClean.push_back(getT2S(c));
						if(isSinglePun(c)){
							graph.push_back(8);
							hasSinglePun = true;
						}else{
							graph.push_back(9);
							hasSinglePun = false;
						}
						hasSpace = false;
					}else if(hasOther){
						if(isSinglePun(c)){
							if(graph.size()){
								graph.back() &= 12;
							}
							senClean.push_back(getT2S(c));
							graph.push_back(8);
							hasSinglePun = true;
						}else{
							if(hasSinglePun){
								senClean.push_back(getT2S(c));
								graph.push_back(9);
							}else{						
								if(!graph.back()) graph.back() = 7;
								senClean.push_back(getT2S(c));
								graph.push_back(2);
							}
							hasSinglePun = false;
						}
					}else{
						senClean.push_back(getT2S(c));
						graph.push_back(9);
						if(isSinglePun(c)){
							hasSinglePun = true;
						}else{
							hasSinglePun = false;
						}
					}
					hasOther = true;
				}else{
					if(hasSpace){
						senClean.push_back(getT2S(c));
						graph.push_back(9);
					}else if(hasOther){
						graph.back() &= 12;
						if(hasSinglePun){
							senClean.push_back(getT2S(c));
							graph.push_back(9);
							hasSinglePun = false;
						}else{					
							senClean.push_back(getT2S(c));
							graph.push_back(15);
						}
					}else{
						senClean.push_back(getT2S(c));
						graph.push_back(15);
					}
					hasSpace = false;
					hasOther = false;
				}

				if(isHttp(c)){
					if(!hasHttp){
						if(c == 'h'){
							httpStart = graph.size() - 1;
							tmpRaw.clear();
							tmpRaw.push_back(c);
							hasHttp = true;
						}
					}else{
						tmpRaw.push_back(c);
					}
				}else{
					if(hasHttp){
						httpVec.push_back(tmpRaw);
						httpStartVec.push_back(httpStart);
						hasHttp = false;
					}
				}
			}
			if(tmpRaw.size() != 0){
				httpVec.push_back(tmpRaw);
				httpStartVec.push_back(httpStart);
			}
			
			std::ostringstream ost;
			std::string str;
			for(int i = 0 ; i < httpVec.size(); i ++){
				ost.str("");
				ost<<httpVec[i];
				str = ost.str();
				std::size_t found = str.find("http");
				if(found != std::string::npos){
					int start = httpStartVec[i];
					int size = str.size();
					//std::cout<<std::endl<<sentence<<":Here:"<<str<<":"<<start<<":"<<size<<":"<<graph.size()<<std::endl;
					
					graph[start] = 1;
					for(int j = start + 1; j < start + size - 1; j ++){
						graph[j] = 2;
					}
					graph[start + size - 1] = 4;
					
				}
			}
			
			if(graph.size()!=0){
	            graph[0]&=9;
	            graph.back()&=12;
			if(!graph[0]) graph[0]=9;
		    	if(!graph.back()) graph.back()=12;
	        }
	        return 0;
	    };
		*/

	    int cleanSpace(String sentence, String senClean, POCGraph graph){
	        senClean="";
	        graph.clear();
	        boolean hasSpace=false;//use to check whether the char is a space 
	        int c = -1;
			int wordLength = 0;
	        for(int i=0;i<(int)sentence.length();i++){
	            c = sentence.charAt(i);
			// if the sentence has space
	            if(c==32 || c==12288){
	                if(hasSpace){
	                    continue;
	                }else{
	                    if(graph.size()>0){
							if(wordLength == 1){
								graph.setElementAt(8,graph.size()-1);
							}else{
								graph.setElementAt(4,graph.size()-1);
							}
	                    }
	                    hasSpace=true;
	                }
					wordLength = 0;
	            }else{
	                if(hasSpace){
	                    senClean+=sentence.charAt(i);
	                    graph.add(1);
	                    hasSpace=false;
	                }else{
	                	senClean+=sentence.charAt(i);
						if(graph.size() == 0){
							graph.add(1);
						}else{
							graph.add(2);
						}
	                }
					wordLength ++;
	            }
	        }
	        if(graph.size()>0){
				if(wordLength == 1){
					graph.setElementAt(8,graph.size()-1);
				}else{
					graph.setElementAt(4,graph.size()-1);
				}
	        }
	        return 0;
	    };
	    
	    public static void main(String[] args) throws IOException {          
			Preprocesser preprocesser = new Preprocesser();
			preprocesser.setT2SMap("models/t2s.dat");
			/*
			String testStrings[] = {"���","锛�","锛�",".","?","!"};
			for(int i = 0; i < 6; i ++){
				String test = testStrings[i];
				System.out.println(test);
				System.out.println(dat.match(test));
				System.out.println(dat.getInfo(test));
			}
			*/
			
			//Dat dat = new Dat("res/javaPun.dat");
			//dat.print("res/javaoutput3.txt");
		}

}
