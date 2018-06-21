# Data Structure
* HashMap<String, String> allMisspelledWords<br/>
   This hashmap is used to keep the misspelled words in the error dataset for calculating emission probabilities.
   <br/>
* HashMap<String, ArrayList<String>> misspelledWordWithOneEditDistanceCorrectWord <br/>
  If the minimum edit distance is 1 between the misspelled word of error dataset and the correct words of cleaned dataset,this misspelled and possible correct words are stored in this hashmap.
  <br/>
* HashMap<String, Integer> soundEventForDeletion <br/>
  This hashmap is used to store the count of deletion dictionaries. <br/>
* HashMap<String, Integer> soundEventForInsertion <br/>
  This hashmap is used to store the count of insertion dictionaries. <br/>
* HashMap<String, Integer> soundEvenetForSubstitution <br/>
  This hashmap is used to store the count of substitution dictionaries. <br/>
* HashMap<String, Integer> unigramCount<br/>
  This  hashmap keeps  the count of words as single <br/>
* HashMap<String, Integer> bimapCount <br/>
  This bigram hashmap is used to store the count of double words <br/>
<br/>

# Important points about this application
* There are 4 classes in this application. Bigram class represents the bigram language model.Also Unigram class represents the unigram language model.<br/>
  SpellCorrection class handles the main operations of homework such as viterbi,spelling correction and language model.Main class is used to trigger the spelling correction operation.<br/>
* The language model is being created when the cleaning incorrect dataset is done. In other words, the cleaned dataset is not written back to another file for creating language model. This status makes this application faster.<br/>
* The regex is used the cleaning dataset extensively.<br/>
* The table/matrix is not used to calculate the minimum edit distance.Because the runtime of application takes too long by using table/matrix.So two words are compared by looking at each character.This makes application faster and interactive.<br/>
* The misspelled words are compared with correct words in cleaned dataset by using unigram hashmap. Also the emission probabilities are calculated by using the deletion, insertion and substitution dictionaries.<br/>
* The punctuations are removed from the end of word or sentence by using regex.<br/>
* The all words are converted to lowercase by using tolowerCase method in Java.<br/>
* "#" character represents the word boundary in the deletion, insertion and substitution dictionaries.Also the count of "#" equals to the total count of all words in the cleaned dataset.<br/>
* Stack is used to make backtrace in Viterbi algorithm.<br/>
* Initial probabilities are calculated by using sentence boundary.<br/>
* The log probability is used to prevent underflow problem.<br/>
* The emission probabilities are calculated by using edit distance.<br/>
* If the initial or transition probability is zero,then infinity problem takes place in Viterbi algorithm. In other words, the log probability is calculated to prevent underflow problem in Viterbi ,but
  if the parameter of log is zero , the infinity problem occurs. So these initial and transition probabilities are assigned Double.MIN_VALUE.<br>
* When the transition probabilities are zero,then these probabilities are assigned to Double.MIN_VALUE.<br/>
* Also if there is no candidate word of misspelled word,then the emission probabilities of these misspelled words are assigned to Double.MIN_VALUE.<br>
* Double.NEGATIVE_INFINITY is used to initialize the max variable that is used to detect the maximum probability of current word in Viterbi.<br/>
* Also Double.NEGATIVE_INFINITY is used to detect the last word in the sentence.The last word is very important because of backtrace.<br/>
* The sentences that does not have error/misspelled words  are not included in the Viterbi algorithm. <br/>
* The output file has the generating sentences by using Viterbi and evaluation value.<br/>
* The working time of this application is approximately 6 seconds for all operations.