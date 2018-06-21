/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Umut Ozturk
 */
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Unigram {
    //The following hashmap is used to store the words as single.
    private HashMap<String, Integer> unigramCount = new HashMap<>();
   //The following variable is used to keep the total number of words in the dataset.
    private int totalWordNumber=0;
    //The following variable is the unique word number of data set. (unigram hashmap size)
    private int uniqueWordNumber=0;
   
    public int getUniqueWordNumber(){
       return this.uniqueWordNumber;
       
    }
    public void setUniqueWordNumber(int uniqueWordNumber){
        this.uniqueWordNumber=uniqueWordNumber;
        
    }
    public int getTotalWordNumber(){
        return this.totalWordNumber;
    }
   
    public HashMap<String,Integer> getUnigramCount(){
           return this.unigramCount;
     }
    
    //The following function seperates the words as single in the dataset.
     public void seperateUnigramWord(String line) {

     
        String part[]=line.split("\\s+");
        int count=0;
       
        for (int i = 0; i <part.length; i++) {
            count=0;
            String key=part[i].trim();
	    if(unigramCount.containsKey(key)){
               count=unigramCount.get(key);
            }
            unigramCount.put(key, count + 1); 
        }
    }
     //The following function is used to find the total number of words in the dataset.
     public void findTotalWordNumber(){
         int count=0;
         for (HashMap.Entry<String, Integer> entry : unigramCount.entrySet()) {
             String key=entry.getKey();
             count+=unigramCount.get(key);   
         }  
         this.totalWordNumber=count;
          
     }

}
