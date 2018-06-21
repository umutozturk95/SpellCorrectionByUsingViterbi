
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Umut Ozturk
 */
public class SpellCorrection {
    //The unigram variable is used the properties of Unigram class.

    private Unigram unigram = null;
    //The bigram variable is used the properties of Bigram class such as hashmap etc...
    private Bigram bigram = null;
    /*
    The following hashmap is used to store the misspelled words in the data set for calculating emission probabilities.
    Also this hashmap is used to keep the unique error words in the dataset.
     */
    private HashMap<String, String> allMisspelledWords = null;//misspelled->white space word.
    /*
    If the minimum edit distance is 1 between the misspelled word of error data set and the correct word of cleaned data set,
    this misspelled and correct word are stored in the following hashmap.
    The key of this hashmap is misspelled word, while the value of the key is arraylist. So this arraylist keeps the correct words.
    The misspelled words can be directly connected to correct words in the cleaned data set according to edit distance.
     */
    private HashMap<String, ArrayList<String>> misspelledWordWithOneEditDistanceCorrectWord = new HashMap<String, ArrayList<String>>();
    //The following hashmap is used to store the count of deletion dictionaries.
    private HashMap<String, Integer> soundEventForDeletion = new HashMap<String, Integer>();
    //The following hashmap is used to store the count of insertion dictionaries.
    private HashMap<String, Integer> soundEventForInsertion = new HashMap<String, Integer>();
    //The following hashmap is used to store the count of substitution dictionaries.
    private HashMap<String, Integer> soundEvenetForSubstitution = new HashMap<String, Integer>();
    //The following variable keeps the total number of found correct words in the viterbi algorithm.
    private int countCorrectFoundWord = 0;
    private int totalMisspelledWordNumber = 0;
    //The following constructor  initializes this class.

    public SpellCorrection() {

        unigram = new Unigram();
        bigram = new Bigram(unigram);
        allMisspelledWords = new HashMap<String, String>();
    }
    //The following function triggers the starting of spelling correction.

    public void startSpellCorrection(String inputFile, String outputFile) {
        cleanDataSet(inputFile);
        startFindOneEditDistanceWords();
        readFileAgainForViterbi(inputFile, outputFile);
    }
//The following function is used to read  the error data set.

    public void cleanDataSet(String inputFile) {

        try {
            File file = new File(inputFile);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            while (line != null) {

                if (line.trim().equals("") == false && line.trim().length() > 1) {
                    //The following function cleans the error data set.
                    findCorrectAndMisspelledWords(line.trim());
                }
                line = br.readLine();
            }
            //find the unique word number and total word number in clean data set.
            unigram.setUniqueWordNumber(unigram.getUnigramCount().size());
            unigram.findTotalWordNumber();
            br.close();
        } catch (Exception e) {

        }

    }

    /*
    The following function reads the error dataset for viterbi algorithm again.
    The output file is created to write possible right sentences and evaluation.
     */
    public void readFileAgainForViterbi(String inputFile, String outputFile) {

        try {
            File file = new File(inputFile);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            Locale enLocale = Locale.forLanguageTag("en_US");
            PrintWriter wt = new PrintWriter(new File(outputFile));
            wt.println("----------All sentences are created by using Viterbi algorithm.----------");
            wt.println("");
            while (line != null) {

                if (line.trim().equals("") == false && line.trim().length() > 1) {
                    //applyingViterbi function applies the viterbi algorithm to detect the possible correct sentences.
                    String newSentence = applyingViterbi(line.trim());
                    if (newSentence != null) {
                        //If the possible correct sentences are appended into output file.
                        wt.println(newSentence);
                    } else {
                        newSentence = line.trim().replaceAll("^\\p{Punct}+|\\s*\\p{Punct}+\\s*$", "");
                        newSentence = newSentence.toLowerCase(enLocale);
                        wt.println(newSentence);
                    }
                }
                line = br.readLine();

            }
            //The evaluation value is calculated ,then this value is printed into output file.
            double evaluation = (double) countCorrectFoundWord / (double) totalMisspelledWordNumber;
            wt.println("");
            wt.println("");
            wt.println("EVALUATION : " + evaluation);
            br.close();
            wt.flush();
            wt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
    The following function applies the viterbi algorithm.It tries to find the possible correct sentence.
   
     */
    public String applyingViterbi(String testLine) {
        //If the sentence in the error dataset does not have error word, then this sentence is not accepted into viterbi algorithm.
        if (testLine.contains("<ERR") == false) {
            return null;
        }
        //The sentence must be lower case.
        Locale enLocale = Locale.forLanguageTag("en_US");
        testLine = testLine.toLowerCase(enLocale);
        /*
         The error words are detected by using regex,then these error words with </err> tag is not deleted.
        
         */
        Pattern pattern = Pattern.compile(">(.*?)</err>");
        Matcher matcher = pattern.matcher(testLine);

        Pattern pattern2 = Pattern.compile("<err targ=(.*?)>");
        Matcher matcher2 = pattern2.matcher(testLine);        
        //The following hashmap is used to store the misspelled words in the sentence.
        HashMap<String,String> errorWordsInSentence=new HashMap<String,String>();
        
        while (matcher.find()) {
            String misspelledWord = matcher.group(1).trim();
            /*
            After detecting error words, the white spaces are removed between </err> and the error word for splitting.
            Also the some words that have 2 words must be taken as single word.So the white spaces that are between two words
            replace "---".
             */
            testLine = testLine.replaceAll(">\\s*" + misspelledWord + "\\s*</err>", ">" + misspelledWord.replaceAll(" ", "---") + "</err>");
             if (matcher2.find()) {
                String correctWord = matcher2.group(1);
                errorWordsInSentence.put(misspelledWord.trim(),correctWord.trim());
             }
            
            
        }
        //The correct form is deleted.
        testLine = testLine.replaceAll("<err targ=(.*?)>", "");
        //This sentence is splitted by using space.
        String words[] = testLine.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            //When splitting the sentence,several punctuations take place as single word.
            //So these punctuations must be removed from sentence.
            if (words[i].trim().matches("\\p{Punct}+")) {
                testLine = testLine.replaceAll("\\" + words[i].trim(), "");

            }
        }

        words = testLine.split("\\s+");
        //After splitting sentence again, "---" replaces the white space.
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim().replace("---", " ");
        }
        //Firstly, the first word of sentence is used to calculate emission probability
        /*
        If the first word is correct , the emission probability is 1.0
        If the fisrt word is not correct ,the emission probabilities are caculated according to one edit distace
         */
        HashMap<String, Double> emissionValues = getEmissionProbability(words[0]);
        //The following hashmap is used to keep the previous probabilty values.
        HashMap<String, Double> previousProbValue = new HashMap<String, Double>();
        double transition = 0.0, prob = 0.0;
        //This following list keeps the possible paths to detect the correct sentence.
        List<HashMap<String, String>> backtrace = new ArrayList<HashMap<String, String>>();

        for (Map.Entry<String, Double> entry : emissionValues.entrySet()) {

            String key = entry.getKey();
            //The initial probability is calculated to start viterbi algorithm.
            if (bigram.getBimapCount().containsKey("<s>" + " " + key)) {

                transition = (double) bigram.getBimapCount().get("<s> " + key) / (double) unigram.getUnigramCount().get("<s>");

            } else {
                //The double key is not in hashmap, the value of double key is assigned to Double.MIN_VALUE.
                transition = Double.MIN_VALUE;

            }
            //The log probability is calculated to prevent the underflow problem.
            prob = (Math.log(transition) / Math.log(2)) + (Math.log(emissionValues.get(key)) / Math.log(2));
            previousProbValue.put(key, Math.pow(2, prob));

        }

        for (int i = 1; i < words.length; i++) {

            emissionValues = getEmissionProbability(words[i]);
            HashMap<String, Double> nextProbValues = new HashMap<String, Double>();
            HashMap<String, String> path = new HashMap<String, String>();
            for (Map.Entry<String, Double> entryCurrent : emissionValues.entrySet()) {
                double max = Double.NEGATIVE_INFINITY;
                String currentKey = entryCurrent.getKey();
                String maxPreviousPath = "";
                for (Map.Entry<String, Double> entryPrevious : previousProbValue.entrySet()) {
                    String previousKey = entryPrevious.getKey();

                    if (bigram.getBimapCount().containsKey(previousKey + " " + currentKey)) {
                        transition = (double) bigram.getBimapCount().get(previousKey + " " + currentKey) / (double) unigram.getUnigramCount().get(previousKey);

                    } else {
                        //The double key is not in hashmap, the value of double key is assigned to Double.MIN_VALUE.
                        transition = Double.MIN_VALUE;
                    }
                    /*
                      If previous probability is zero, then log probability can be infinity.This is important problem.
                      So Double.Min_Value is assigned to the previous probability.
                     */

                    if (previousProbValue.get(previousKey) == 0.0) {

                        prob = (Math.log(transition) / Math.log(2)) + (Math.log(Double.MIN_VALUE) / Math.log(2)) + (Math.log(emissionValues.get(currentKey)) / Math.log(2));
                    } else {

                        prob = (Math.log(transition) / Math.log(2)) + (Math.log(previousProbValue.get(previousKey)) / Math.log(2)) + (Math.log(emissionValues.get(currentKey)) / Math.log(2));
                    }
                    //Maximum probability of previous keys is selected.
                    if (prob > max) {
                        max = prob;
                        maxPreviousPath = previousKey;

                    }

                }

                nextProbValues.put(currentKey, Math.pow(2, max));
                path.put(currentKey, maxPreviousPath);

            }
            //The previous probabilities are updated.
            previousProbValue = nextProbValues;
            backtrace.add(path);

        }
        //The last word must be detected for correct path.
        String lastTag = "";
        double max = Double.NEGATIVE_INFINITY;
        for (String word : previousProbValue.keySet()) {
            if (previousProbValue.get(word) > max) {

                max = previousProbValue.get(word);
                lastTag = word;
            }

        }
        //Backtrace operation is implemented 
        Stack<String> stack = new Stack<String>();
        stack.push(lastTag);
        for (int i = words.length - 2; i >= 0; i--) {

            stack.push(backtrace.get(i).get(stack.peek()));

        }
        String result = "";
        //The possible found word is  compared with actual correct word by using backtrace.
        //If the possible found word equals to actual correct word, then countCorrectFoundWord is increased by 1.  
        for (int i = 0; i < words.length; i++) {
            String correctWord = stack.pop();

            if (words[i].contains("</err>")) {
                String cleanedWord = words[i].replaceAll("</err>", "").replaceAll("^\\p{Punct}+|\\s*\\p{Punct}+\\s*$", "");
                if (errorWordsInSentence.get(cleanedWord).equals(correctWord)) {                  
                    countCorrectFoundWord++;
                }
            }
            result += correctWord + " ";
        }
    
        return result;
    }
//The following function calculates the emission probability of misspelled word.

    public HashMap<String, Double> getEmissionProbability(String word) {

        HashMap<String, Double> emissionValues = new HashMap<String, Double>();
        //If the word is not misspelled word,then emission probability of this word is assigned 1.
        if (word.contains("</err>") == false) {
            //The punctuations are removed the end of words.
            word = word.replaceAll("^\\p{Punct}+|\\s*\\p{Punct}+\\s*$", "");
            emissionValues.put(word, 1.0);

        } else {
            word = word.replaceAll("</err>", "").replaceAll("^\\p{Punct}+|\\s*\\p{Punct}+\\s*$", "");
            ArrayList<String> correctWordFormWithSoundEvent = misspelledWordWithOneEditDistanceCorrectWord.get(word);
            /*
              If the misspelled word does not have the possible correct words according to one edit distance,
             then emission probability of this word is assigned  1.
             */
            if (correctWordFormWithSoundEvent.size() == 0) {
                emissionValues.put(word, Double.MIN_VALUE);
                return emissionValues;
            }
            /*
            If the misspelled word has the possible correct words, the emission probabilities are calculated
            according to e deletion, insertion and substitution dictionaries.
             */
            int count = 0;
            double prob = 0.0;
            for (int i = 0; i < correctWordFormWithSoundEvent.size(); i++) {
                String tokens[] = correctWordFormWithSoundEvent.get(i).split("=");
                String key[] = tokens[1].split("\\s+");
                if (tokens[2].equals("deletion")) {

                    count = getCountSoundEvent(key[0] + "" + key[1]);
                    prob = (double) soundEventForDeletion.get(tokens[1]) / (double) count;
                    emissionValues.put(tokens[0], prob);

                } else if (tokens[2].equals("insertion")) {

                    count = getCountSoundEvent(key[0]);
                    prob = (double) soundEventForInsertion.get(tokens[1]) / (double) count;
                    emissionValues.put(tokens[0], prob);
                } else if (tokens[2].equals("substitution")) {

                    count = getCountSoundEvent(key[0]);
                    prob = (double) soundEvenetForSubstitution.get(tokens[1]) / (double) count;
                    emissionValues.put(tokens[0], prob);
                }

            }

        }

        return emissionValues;
    }
//The following function calculates  the denominator of deletion, insertion and substitution dictionaries.

    public int getCountSoundEvent(String key) {
        int count = 0;
        if (key.length() == 1) {
            //If key is word boundry , then the total word number is assigned to count variable.
            if (key.equals("#")) {

                count = unigram.getTotalWordNumber();
            } else {
                //If key is not #,then the key is detected in all words of cleaned dataset.
                for (Map.Entry<String, Integer> entry : unigram.getUnigramCount().entrySet()) {
                    String s = entry.getKey();
                    count += ((s.split(key, -1).length - 1) * unigram.getUnigramCount().get(s));

                }

            }

        } else {
            if (key.contains("#")) {
                key = key.replace("#", "");

            }
            for (Map.Entry<String, Integer> entry : unigram.getUnigramCount().entrySet()) {
                String s = entry.getKey();
                count += ((s.split(key, -1).length - 1) * unigram.getUnigramCount().get(s));

            }
        }

        return count;
    }
//The following function is used to replace the misspelled word with the correct word for creating language model.

    public void findCorrectAndMisspelledWords(String line) {
        Locale enLocale = Locale.forLanguageTag("en_US");
        line = line.toLowerCase(enLocale);

        Pattern pattern = Pattern.compile(">(.*?)</err>");
        Matcher matcher = pattern.matcher(line);

        Pattern pattern2 = Pattern.compile("<err targ=(.*?)>");
        Matcher matcher2 = pattern2.matcher(line);

        while (matcher.find()) {
            String misspelledWord = matcher.group(1);
            if (matcher2.find()) {
                String correctWord = matcher2.group(1);
                line = line.replaceAll(misspelledWord, correctWord);
                totalMisspelledWordNumber++;
                allMisspelledWords.put(misspelledWord.trim()," ");
            }

        }

        line = line.replaceAll("</err>", "");
        line = line.replaceAll("<err targ=(.*?)>", "");
        line = deletePunctuations(line);
        line = "<s> " + line + " </s>";
        createLanguageModel(line);
    }
//The following function creates the language model.

    public void createLanguageModel(String line) {
        unigram.seperateUnigramWord(line);
        bigram.seperateBigramWord(line);
    }
//The following function removes the punctuations from the end of words and sentence by using regex.

    public String deletePunctuations(String line) {
        String tokens[] = line.split("\\s+");
        String newLine = "";
        for (int i = 0; i < tokens.length; i++) {
            newLine += tokens[i].replaceAll("^\\p{Punct}+|\\s*\\p{Punct}+\\s*$", "") + " ";
        }
        return newLine;
    }
//The following function triggers  finding the possible correct words by using one edit distance.

    public void startFindOneEditDistanceWords() {
        for (Map.Entry<String, String> entry : allMisspelledWords.entrySet()) {
            String misspelledWord = entry.getKey();
            misspelledWordWithOneEditDistanceCorrectWord.put(misspelledWord, new ArrayList<String>());
            for (Map.Entry<String, Integer> entry2 : unigram.getUnigramCount().entrySet()) {
                String correctWord = entry2.getKey();
                findOneEditDistance(misspelledWord, correctWord, unigram.getUnigramCount().get(correctWord));
            }

        }

    }

    /*
    The following function finds the one edit distance between two words.
    Also the insertion,deletion and substitution dictionaries are detected ,and are stored into hashmap.
     */
    public void findOneEditDistance(String misspelledWord, String correctWord, int factor) {
        if (misspelledWord == null || correctWord == null) {
            return;
        }
        int m = misspelledWord.length();
        int n = correctWord.length();
        if (Math.abs(m - n) > 1) {
            return;
        }

        int misspelledIndex = 0;
        int correctIndex = 0;
        int count = 0;
        int editIndex = 0;

        while (misspelledIndex < m && correctIndex < n) {
            if (misspelledWord.charAt(misspelledIndex) == correctWord.charAt(correctIndex)) {
                misspelledIndex++;
                correctIndex++;
            } else {

                count++;
                //editIndex variable is first index  where two characters do not match.
                editIndex = correctIndex;

                if (count > 1) {
                    return;
                }

                if (m > n) {
                    misspelledIndex++;
                } else if (m < n) {
                    correctIndex++;
                } else {
                    misspelledIndex++;
                    correctIndex++;
                }
            }
        }

        if (misspelledIndex < m || correctIndex < n) {

            count++;
        }
        /*
        If the edit dfistance of two words is 1 ,then the sound events  are detected such as insertion,deletion and substitution.    
         */

        if (count == 1) {

            ArrayList<String> oneEditDistanceWordsWithSoundEvent = misspelledWordWithOneEditDistanceCorrectWord.get(misspelledWord);
            //deletion
            if (m < n) {
                String key = "";
                if (correctIndex < n) {
                    key = correctWord.charAt(correctWord.length() - 2) + " " + correctWord.charAt(correctWord.length() - 1);

                } else if (editIndex - 1 >= 0) {
                    key = correctWord.charAt(editIndex - 1) + " " + correctWord.charAt(editIndex);

                } else {
                    key = "# " + correctWord.charAt(editIndex);
                }

                int valueKey = 0;
                if (soundEventForDeletion.containsKey(key)) {
                    valueKey = soundEventForDeletion.get(key);
                }
                soundEventForDeletion.put(key, valueKey + factor);
                oneEditDistanceWordsWithSoundEvent.add(correctWord + "=" + key + "=deletion");
            } //insertion
            else if (m > n) {
                String key = "";
                if (misspelledIndex < m) {
                    key = misspelledWord.charAt(misspelledWord.length() - 2) + " " + misspelledWord.charAt(misspelledWord.length() - 1);

                } else if (editIndex - 1 >= 0) {
                    key = misspelledWord.charAt(editIndex - 1) + " " + misspelledWord.charAt(editIndex);

                } else {
                    key = "# " + misspelledWord.charAt(editIndex);
                }

                int valueKey = 0;
                if (soundEventForInsertion.containsKey(key)) {
                    valueKey = soundEventForInsertion.get(key);

                }
                soundEventForInsertion.put(key, valueKey + factor);
                oneEditDistanceWordsWithSoundEvent.add(correctWord + "=" + key + "=insertion");
            }//substitution
            else {
                String key = "";
                key = correctWord.charAt(editIndex) + " " + misspelledWord.charAt(editIndex);
                int valueKey = 0;

                if (soundEvenetForSubstitution.containsKey(key)) {
                    valueKey = soundEvenetForSubstitution.get(key);
                }

                soundEvenetForSubstitution.put(key, valueKey + factor);

                oneEditDistanceWordsWithSoundEvent.add(correctWord + "=" + key + "=substitution");
            }

        }

    }

}
