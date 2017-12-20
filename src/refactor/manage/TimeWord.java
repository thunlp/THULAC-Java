package manage;

import base.WordWithTag;

import java.util.HashSet;
import java.util.List;

public class TimeWord {
		HashSet<Integer> arabicNumSet;
		//std::set<int> chineseNumSet;
		HashSet<Integer> timeWordSet;
		HashSet<Integer> otherSet;
		public TimeWord()
		{
			arabicNumSet =new HashSet<Integer>();
			timeWordSet =new HashSet<Integer>();
			otherSet =new HashSet<Integer>();
			
			for(int i = 48; i < 58; i ++){
				arabicNumSet.add(i);
			}
			for(int i = 65296; i < 65306; i ++){
				arabicNumSet.add(i);
			}
			/*
			int chineseNums[] = {12295,19968,20108,19977,22235,20116,20845,19971,20843,20061}; 
			for(int i = 0; i < 10; i ++){
				chineseNumSet.insert(chineseNums[i]);
			}
			*/

			//��:24180 ��:26376 ��:26085 ��:21495 ʱ:26102 ��:28857 ��:20998 ��:31186
			int timeWord[] = {24180, 26376, 26085, 21495, 26102, 28857, 20998, 31186};
			int len = 8;
			for(int i = 0; i < len; i ++){
				timeWordSet.add(timeWord[i]);
			}

			for(int i = 65; i < 91; i ++){
				otherSet.add(i);
			}
			for(int i = 97; i < 123; i ++){
				otherSet.add(i);
			}
			for(int i = 48; i < 58; i ++){
				otherSet.add(i);
			}

			int other[] = {65292, 12290, 65311, 65281, 65306, 65307, 8216, 8217, 8220, 8221, 12304, 12305,
						12289, 12298, 12299, 126, 183, 64, 124, 35, 65509, 37, 8230, 38, 42, 65288,
						65289, 8212, 45, 43, 61, 44, 46, 60, 62, 63, 47, 33, 59, 58, 39, 34, 123, 125,
						91, 93, 92, 124, 35, 36, 37, 94, 38, 42, 40, 41, 95, 45, 43, 61, 9700, 9734, 9733};
			len = 63;
			for(int i = 0; i < len; i ++){
				otherSet.add(other[i]);
			}
		};

		public boolean isArabicNum(String word){
			boolean allArabic = true;
			for(int i = 0; i < word.length(); i ++){
				if(arabicNumSet.contains(Integer.valueOf(word.charAt(i)))){
					allArabic = false;
					break;
				}
			}
			return allArabic;
		}

		public boolean isTimeWord(String word){
			if(word.length() == 0 || word.length() > 1){
				return false;
			}
			if(!timeWordSet.contains((int)word.charAt(0))){
				return false;
			}else{
				return true;
			}
		}

		public boolean isDoubleWord(String word, String postWord)
		{
			if(word.length() != 1 || postWord.length() != 1){
				return false;
			}else
			{
				int wordInt = word.charAt(0);
				int postWordInt = postWord.charAt(0);
				if(wordInt == postWordInt){
					if(!otherSet.contains((int)wordInt)){
						return true;
					}else{
						return false;
					}
				}else{
					return false;
				}
			}
		}
		
		boolean isHttpWord(String word){
			if(word.length() < 5){
				return false;
			}else{
				if(word.charAt(0) == 'h' && word.charAt(1) == 't' && word.charAt(2) == 't' && word.charAt(3) == 'p' ){
					return true;
				}else{
					return false;
				}
			}
		}
		
		   public List<WordWithTag> adjustDouble(List<WordWithTag> sentence){
			int size = sentence.size();
			String word;
			boolean hasTimeWord = false;

			for(int i = size - 1; i >= 0; i --){
				word = sentence.get(i).word;
				if(isTimeWord(word)){
					hasTimeWord = true;
				}else{
					if(hasTimeWord){
						//if(isArabicNum(word) || isChineseNum(word)){
						if(isArabicNum(word)){
							sentence.get(i).word += sentence.get(i+1).word;
							sentence.remove(i + 1);
							sentence.get(i).tag = "t";
						}
					}
					hasTimeWord = false;
				}
			}

			size = sentence.size();
			String postWord;
			for(int i = size - 2; i >= 0; i --){
				word = sentence.get(i).word;
				postWord = sentence.get(i + 1).word;
				if(isDoubleWord(word, postWord)){
					sentence.get(i).word += sentence.get(i+1).word;
					sentence.remove(i + 1);
				}
			}

			size = sentence.size();
			for(int i = 0; i < size; i ++){
				word = sentence.get(i).word;
				if(isHttpWord(word)){
					sentence.get(i).tag = "x";
				}
			}

			size = sentence.size();
			String preWord;
			for(int i = 1; i < size; i ++){
				preWord = sentence.get(i-1).word;
				word = sentence.get(i).word;
				if(preWord.length() == 1 && preWord.charAt(0) == 64){
					if((word.length() != 1) || (word.charAt(0) != 64)){
						sentence.get(i).tag = "np";
					}
				}
			}
		   return sentence;
	    }

}
