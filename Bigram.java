/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Umut Ozturk
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class Bigram {
      //The bigram hashmap is used to store the count of double words. 
      private HashMap<String, Integer> bimapCount = new HashMap<>();
       //Unigram class variable is used to access the important variables in the Unigram class such as unique word number...
       private Unigram unigram=null;
       
       
       public Bigram(Unigram unigram){
           this.unigram=unigram;
       }

       public HashMap<String,Integer> getBimapCount(){
           return this.bimapCount;
       }
       public Unigram getUnigram(){
           
           return this.unigram;
       }
       /*
       The following function is used to seperate the words as double,
       then the single words are inserted into  bigram hashmap.
       */
       public void seperateBigramWord(String line) {
      
        String parts[]=line.split("\\s+");
        int count=0;
        
        for(int i=0;i<parts.length-1;i++){
            count=0;
            String key=parts[i].trim()+" "+parts[i+1].trim();
            
            if(bimapCount.containsKey(key)){
               count=bimapCount.get(key);
            }
            bimapCount.put(key, count + 1);    
        }
     
    }
       
}
